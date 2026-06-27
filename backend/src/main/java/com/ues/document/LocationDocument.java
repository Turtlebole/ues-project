package com.ues.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Elasticsearch representation of a Location ("mesto").
 * Mirrors the JPA Location id so results can be loaded back from the relational DB.
 */
@Document(indexName = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String address;

    @Field(type = FieldType.Keyword)
    private String type;

    /** Free-form text parsed from the attached PDF document. */
    @Field(type = FieldType.Text)
    private String pdfContent;
}
