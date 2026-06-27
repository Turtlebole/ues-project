package com.ues.service;

import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ues.document.LocationDocument;
import com.ues.model.Location;
import com.ues.repository.LocationSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Indexes locations into Elasticsearch and performs full-text search (S1)
 * across name, description and parsed PDF content.
 */
@Service
public class LocationSearchService {

    private static final Logger log = LoggerFactory.getLogger(LocationSearchService.class);

    private final LocationSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public LocationSearchService(LocationSearchRepository searchRepository,
                                 ElasticsearchOperations elasticsearchOperations) {
        this.searchRepository = searchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /** Index (or re-index) a location. Failures are logged but never break CRUD. */
    public void index(Location location, String pdfContent) {
        try {
            LocationDocument doc = LocationDocument.builder()
                    .id(String.valueOf(location.getId()))
                    .name(location.getName())
                    .description(location.getDescription())
                    .address(location.getAddress())
                    .type(location.getType())
                    .pdfContent(pdfContent)
                    .build();
            searchRepository.save(doc);
        } catch (Exception e) {
            log.warn("Failed to index location {} in Elasticsearch: {}", location.getId(), e.getMessage());
        }
    }

    /** Returns the PDF text currently indexed for a location, or null. */
    public String getIndexedPdfContent(Long id) {
        try {
            return searchRepository.findById(String.valueOf(id))
                    .map(LocationDocument::getPdfContent)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public void delete(Long id) {
        try {
            searchRepository.deleteById(String.valueOf(id));
        } catch (Exception e) {
            log.warn("Failed to delete location {} from Elasticsearch: {}", id, e.getMessage());
        }
    }

    /**
     * Full-text search across name, description and PDF content.
     * Returns matching location ids ordered by relevance.
     */
    public List<Long> search(String text) {
        Query query = MultiMatchQuery.of(m -> m
                .query(text)
                .fields("name^3", "description^2", "pdfContent")
                .fuzziness("AUTO")
        )._toQuery();

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .build();

        SearchHits<LocationDocument> hits =
                elasticsearchOperations.search(nativeQuery, LocationDocument.class);

        return hits.getSearchHits().stream()
                .map(hit -> Long.valueOf(hit.getContent().getId()))
                .collect(Collectors.toList());
    }
}
