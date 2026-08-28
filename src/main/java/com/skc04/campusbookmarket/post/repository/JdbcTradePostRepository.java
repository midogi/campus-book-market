package com.skc04.campusbookmarket.post.repository;

import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradePostSearchType;
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

/**
 * JdbcTemplate 기반의 거래 게시글 저장소다.
 *
 * <p>현재 애플리케이션의 기본 구현은 {@link JpaTradePostRepository}이며, 이 클래스는
 * JDBC와 JPA의 차이를 비교하고 기존 저장소 테스트를 유지하기 위해 남겨 둔다.</p>
 */
@Repository
public class JdbcTradePostRepository implements TradePostRepository {

    // JDBC 조회 결과 한 행의 컬럼을 TradePost 생성자 인수에 대응시킨다.
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
        return executeSearch(
                keyword,
                TradePostSearchType.TITLE,
                status,
                sort,
                null,
                null
        );
    }

    @Override
    public List<TradePost> search(
            String keyword,
            TradePostSearchType searchType,
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

        return executeSearch(keyword, searchType, status, sort, limit, offset);
    }

    @Override
    public long count(
            String keyword,
            TradePostSearchType searchType,
            TradeStatus status
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM trade_post
                WHERE 1 = 1
                """);

        List<Object> parameters = new ArrayList<>();
        appendSearchConditions(sql, parameters, keyword, searchType, status);

        Long count = jdbcTemplate.queryForObject(
                sql.toString(),
                Long.class,
                parameters.toArray()
        );
        return count == null ? 0 : count;
    }

    private List<TradePost> executeSearch(
            String keyword,
            TradePostSearchType searchType,
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
        appendSearchConditions(sql, parameters, keyword, searchType, status);
        appendOrderBy(sql, sort);

        // limit이 전달된 검색에서만 페이징 구문과 바인딩 값을 추가한다.
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
            TradePostSearchType searchType,
            TradeStatus status
    ) {
        // 사용자 입력은 SQL에 직접 연결하지 않고 ? 파라미터로 바인딩한다.
        if (keyword != null && !keyword.isBlank()) {
            TradePostSearchType normalizedSearchType = searchType == null
                    ? TradePostSearchType.TITLE
                    : searchType;

            sql.append(switch (normalizedSearchType) {
                case TITLE -> " AND LOWER(title) LIKE LOWER(?)";
                case SELLER -> " AND LOWER(seller_name) LIKE LOWER(?)";
            });
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

        // INSERT 후 DB가 생성한 identity 값을 KeyHolder로 돌려받는다.
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
