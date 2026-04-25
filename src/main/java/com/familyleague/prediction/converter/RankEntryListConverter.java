package com.familyleague.prediction.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.familyleague.prediction.dto.RankEntry;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

/**
 * JPA converter: List&lt;RankEntry&gt; ↔ JSON string.
 * The column is typed as JSONB in PostgreSQL; Hibernate treats it as TEXT on the Java side.
 */
@Converter
public class RankEntryListConverter implements AttributeConverter<List<RankEntry>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<RankEntry>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<RankEntry> entries) {
        if (entries == null) return null;
        try {
            return MAPPER.writeValueAsString(entries);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize rank entries to JSON", e);
        }
    }

    @Override
    public List<RankEntry> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return MAPPER.readValue(json, TYPE_REF);
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize rank entries from JSON", e);
        }
    }
}
