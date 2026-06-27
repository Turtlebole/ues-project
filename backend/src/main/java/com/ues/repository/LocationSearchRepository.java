package com.ues.repository;

import com.ues.document.LocationDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationSearchRepository extends ElasticsearchRepository<LocationDocument, String> {
}
