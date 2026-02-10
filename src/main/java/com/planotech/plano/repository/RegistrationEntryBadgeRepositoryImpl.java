package com.planotech.plano.repository;

import com.planotech.plano.model.RegistrationEntry;
import com.planotech.plano.request.BadgeFilterRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
@RequiredArgsConstructor
public class RegistrationEntryBadgeRepositoryImpl
        implements RegistrationEntryCustomRepository {

    @PersistenceContext
    private EntityManager em;

    public Page<RegistrationEntry> findBadgesWithFilters(Long eventId, BadgeFilterRequest request, Pageable pageable) {
        List<String> orderByExpressions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                    SELECT r.*
                    FROM registration_entry r
                    WHERE r.event_id = :eventId
                """);

        Map<String, Object> params = new HashMap<>();

        int index = 0;

        for (BadgeFilterRequest.Filter filter : request.getFilters()) {
            String key = filter.getFieldKey();
            String param = "val" + index++;

            switch (filter.getOperator()) {

                case EQUALS -> {
                    sql.append("""
                                AND JSON_UNQUOTE(
                                    JSON_EXTRACT(r.responses_json, '$.%s')
                                ) = :%s
                            """.formatted(key, param));

                    params.put(param, filter.getValue());
                }

                case CONTAINS -> {

                    String[] values = filter.getValue().split(",");

                    // WHERE (OR)
                    sql.append(" AND (");

                    for (int i = 0; i < values.length; i++) {
                        String paramKey = param + "_" + i;

                        if (i > 0) sql.append(" OR ");

                        sql.append("""
            JSON_CONTAINS(
                JSON_EXTRACT(r.responses_json, '$.%s'),
                JSON_QUOTE(:%s)
            )
        """.formatted(key, paramKey));

                        params.put(paramKey, values[i].trim());
                    }

                    sql.append(") ");

                    // ORDER BY score (auto-ranking)
                    String scoreExpr = IntStream.range(0, values.length)
                            .mapToObj(i -> """
            JSON_CONTAINS(
                JSON_EXTRACT(r.responses_json, '$.%s'),
                JSON_QUOTE(:%s)
            )
        """.formatted(key, param + "_" + i))
                            .collect(Collectors.joining(" + "));

                    orderByExpressions.add("(" + scoreExpr + ") DESC");
                }
            }
        }

        if (!orderByExpressions.isEmpty()) {
            sql.append(" ORDER BY ");
            sql.append(String.join(", ", orderByExpressions));
        } else {
            sql.append(" ORDER BY r.submitted_at DESC ");
        }

        Query query = em.createNativeQuery(sql.toString(), RegistrationEntry.class);
        params.forEach(query::setParameter);
        query.setParameter("eventId", eventId);

        // Pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<RegistrationEntry> result = query.getResultList();

        // Count query
        Query countQuery = em.createNativeQuery(
                "SELECT COUNT(*) FROM (" + sql + ") x"
        );
        params.forEach(countQuery::setParameter);
        countQuery.setParameter("eventId", eventId);

        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(result, pageable, total);
    }

    @Override
    public Page<RegistrationEntry> searchBadges(Long eventId, String search, Pageable pageable) {
        String sql = """
        SELECT r.*
        FROM registration_entry r
        WHERE r.event_id = :eventId
          AND (
            LOWER(r.badge_code) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(r.email) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(r.phone) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(r.responses_json) LIKE LOWER(CONCAT('%', :search, '%'))
          )
        ORDER BY r.submitted_at DESC
    """;

        Query query = em.createNativeQuery(sql, RegistrationEntry.class);
        query.setParameter("eventId", eventId);
        query.setParameter("search", search);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<RegistrationEntry> result = query.getResultList();

        Query countQuery = em.createNativeQuery("""
        SELECT COUNT(*)
        FROM registration_entry r
        WHERE r.event_id = :eventId
          AND (
            LOWER(r.badge_code) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(r.email) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(r.phone) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(r.responses_json) LIKE LOWER(CONCAT('%', :search, '%'))
          )
    """);

        countQuery.setParameter("eventId", eventId);
        countQuery.setParameter("search", search);

        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(result, pageable, total);
    }
}