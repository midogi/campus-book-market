package com.skc04.campusbookmarket.post.repository;

import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradePostSort;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTradePostRepository implements TradePostRepository {

    private static final RowMapper<TradePost> TRADE_POST_ROW_MAPPER =
            (resultSet, rowNumber) -> new TradePost(
                    resultSet.getLong("id"),
                    resultSet.getString("title"),
                    resultSet.getLong("price"),
                    resultSet.getString("seller_name"),
                    resultSet.getString("description"),
                    TradeStatus.valueOf(resultSet.getString("status")),
                    resultSet.getTimestamp("created_at").toLocalDateTime(),
                    resultSet.getTimestamp("updated_at").toLocalDateTime()
            );

    private final JdbcTemplate jdbcTemplate;

    public JdbcTradePostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<TradePost> findAll() {
        String sql = """
                SELECT id, title, price, seller_name, description, status, created_at, updated_at
                FROM trade_post
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, TRADE_POST_ROW_MAPPER);
    }

    @Override
    public List<TradePost> search(String keyword, TradeStatus status) {
        return search(keyword, status, TradePostSort.LATEST);
    }

    @Override
    public List<TradePost> search(
            String keyword,
            TradeStatus status,
            TradePostSort sort
    ) {
        return executeSearch(keyword, status, sort, null, null);
    }

    @Override
    public List<TradePost> search(
            String keyword,
            TradeStatus status,
            TradePostSort sort,
            int limit,
            int offset
    ) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit은 1 이상이어야 합니다.");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset은 0 이상이어야 합니다.");
        }

        return executeSearch(keyword, status, sort, limit, offset);
    }

    @Override
    public long count(String keyword, TradeStatus status) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM trade_post
                WHERE 1 = 1
                """);

        List<Object> parameters = new ArrayList<>();
        appendSearchConditions(sql, parameters, keyword, status);

        Long count = jdbcTemplate.queryForObject(
                sql.toString(),
                Long.class,
                parameters.toArray()
        );
        return count == null ? 0 : count;
    }

    private List<TradePost> executeSearch(
            String keyword,
            TradeStatus status,
            TradePostSort sort,
            Integer limit,
            Integer offset
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, title, price, seller_name, description, status, created_at, updated_at
                FROM trade_post
                WHERE 1 = 1
                """);

        List<Object> parameters = new ArrayList<>();
        appendSearchConditions(sql, parameters, keyword, status);
        appendOrderBy(sql, sort);

        if (limit != null && offset != null) {
            sql.append(" LIMIT ? OFFSET ?");
            parameters.add(limit);
            parameters.add(offset);
        }

        return jdbcTemplate.query(
                sql.toString(),
                TRADE_POST_ROW_MAPPER,
                parameters.toArray()
        );
    }

    private void appendSearchConditions(
            StringBuilder sql,
            List<Object> parameters,
            String keyword,
            TradeStatus status
    ) {
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND LOWER(title) LIKE LOWER(?)");
            parameters.add("%" + keyword + "%");
        }

        if (status != null) {
            sql.append(" AND status = ?");
            parameters.add(status.name());
        }
    }

    private void appendOrderBy(StringBuilder sql, TradePostSort sort) {
        TradePostSort normalizedSort = sort == null ? TradePostSort.LATEST : sort;

        sql.append(switch (normalizedSort) {
            case LATEST -> " ORDER BY created_at DESC, id DESC";
            case PRICE_ASC -> " ORDER BY price ASC, id DESC";
            case PRICE_DESC -> " ORDER BY price DESC, id DESC";
        });
    }

    @Override
    public Optional<TradePost> findById(Long id) {
        String sql = """
                SELECT id, title, price, seller_name, description, status, created_at, updated_at
                FROM trade_post
                WHERE id = ?
                """;

        List<TradePost> posts = jdbcTemplate.query(sql, TRADE_POST_ROW_MAPPER, id);
        return posts.stream().findFirst();
    }

    @Override
    public TradePost save(String title, long price, String sellerName, String description) {
        String sql = """
                INSERT INTO trade_post (title, price, seller_name, description)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setString(1, title);
            statement.setLong(2, price);
            statement.setString(3, sellerName);
            statement.setString(4, description);
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException("게시글 ID를 생성하지 못했습니다.");
        }

        return findById(generatedId.longValue())
                .orElseThrow(() -> new IllegalStateException("저장한 게시글을 조회하지 못했습니다."));
    }

    @Override
    public Optional<TradePost> update(
            Long id,
            String title,
            long price,
            String sellerName,
            String description
    ) {
        String sql = """
                UPDATE trade_post
                SET title = ?, price = ?, seller_name = ?, description = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        int updatedRows = jdbcTemplate.update(
                sql,
                title,
                price,
                sellerName,
                description,
                id
        );

        if (updatedRows == 0) {
            return Optional.empty();
        }

        return findById(id);
    }

    @Override
    public Optional<TradePost> updateStatus(Long id, TradeStatus status) {
        String sql = """
                UPDATE trade_post
                SET status = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        int updatedRows = jdbcTemplate.update(sql, status.name(), id);
        if (updatedRows == 0) {
            return Optional.empty();
        }

        return findById(id);
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM trade_post WHERE id = ?";
        return jdbcTemplate.update(sql, id) == 1;
    }
}
