package com.skc04.campusbookmarket.post.repository;

import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradePostSearchType;
import com.skc04.campusbookmarket.post.domain.TradePostSort;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * EntityManager를 사용해 거래 게시글을 저장하고 조회한다.
 *
 * <p>JdbcTradePostRepository도 같은 인터페이스를 구현하므로, 애플리케이션에서는
 * {@link Primary}가 지정된 이 구현체를 기본 저장소로 사용한다.</p>
 */
@Repository
@Primary
public class JpaTradePostRepository implements TradePostRepository {

    private static final int UNPAGED_LIMIT = Integer.MAX_VALUE;

    private final EntityManager entityManager;

    public JpaTradePostRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradePost> findAll() {
        String jpql = """
                SELECT post
                FROM TradePost post
                ORDER BY post.id
                """;

        return entityManager.createQuery(jpql, TradePost.class)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradePost> search(String keyword, TradeStatus status) {
        return search(
                keyword,
                TradePostSearchType.TITLE,
                status,
                TradePostSort.LATEST,
                UNPAGED_LIMIT,
                0
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradePost> search(
            String keyword,
            TradeStatus status,
            TradePostSort sort
    ) {
        return search(
                keyword,
                TradePostSearchType.TITLE,
                status,
                sort,
                UNPAGED_LIMIT,
                0
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradePost> search(
            String keyword,
            TradePostSearchType searchType,
            TradeStatus status,
            TradePostSort sort,
            int limit,
            int offset
    ) {
        validatePagination(limit, offset);

        StringBuilder jpql = new StringBuilder("""
                SELECT post
                FROM TradePost post
                WHERE 1 = 1
                """);

        appendSearchConditions(jpql, keyword, searchType, status);
        appendOrderBy(jpql, sort);

        TypedQuery<TradePost> query = entityManager.createQuery(
                jpql.toString(),
                TradePost.class
        );
        bindSearchParameters(query, keyword, status);

        // JPA가 DB 방언에 맞는 OFFSET/LIMIT 구문으로 변환한다.
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public long count(
            String keyword,
            TradePostSearchType searchType,
            TradeStatus status
    ) {
        StringBuilder jpql = new StringBuilder("""
                SELECT COUNT(post)
                FROM TradePost post
                WHERE 1 = 1
                """);

        // 목록과 개수 쿼리에 같은 조건을 적용해야 전체 페이지 수가 정확해진다.
        appendSearchConditions(jpql, keyword, searchType, status);

        TypedQuery<Long> query = entityManager.createQuery(
                jpql.toString(),
                Long.class
        );
        bindSearchParameters(query, keyword, status);
        return query.getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TradePost> findById(Long id) {
        TradePost post = entityManager.find(TradePost.class, id);
        return Optional.ofNullable(post);
    }

    @Override
    @Transactional
    public TradePost save(
            String title,
            long price,
            String sellerName,
            String description
    ) {
        TradePost post = new TradePost(title, price, sellerName, description);
        entityManager.persist(post);
        return post;
    }

    @Override
    @Transactional
    public Optional<TradePost> update(
            Long id,
            String title,
            long price,
            String sellerName,
            String description
    ) {
        TradePost post = entityManager.find(TradePost.class, id);
        if (post == null) {
            return Optional.empty();
        }

        // 영속 엔티티의 값을 변경하면 커밋 시점에 UPDATE가 자동 실행된다.
        post.updateDetails(title, price, sellerName, description);
        return Optional.of(post);
    }

    @Override
    @Transactional
    public Optional<TradePost> updateStatus(Long id, TradeStatus status) {
        TradePost post = entityManager.find(TradePost.class, id);
        if (post == null) {
            return Optional.empty();
        }

        post.changeStatus(status);
        return Optional.of(post);
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        TradePost post = entityManager.find(TradePost.class, id);
        if (post == null) {
            return false;
        }

        // remove()는 영속 상태의 엔티티를 삭제 대상으로 등록한다.
        entityManager.remove(post);
        return true;
    }

    private void appendSearchConditions(
            StringBuilder jpql,
            String keyword,
            TradePostSearchType searchType,
            TradeStatus status
    ) {
        if (hasKeyword(keyword)) {
            TradePostSearchType normalizedSearchType = searchType == null
                    ? TradePostSearchType.TITLE
                    : searchType;

            jpql.append(switch (normalizedSearchType) {
                case TITLE -> " AND LOWER(post.title) LIKE LOWER(:keyword)";
                case SELLER -> " AND LOWER(post.sellerName) LIKE LOWER(:keyword)";
            });
        }

        if (status != null) {
            jpql.append(" AND post.status = :status");
        }
    }

    private void appendOrderBy(StringBuilder jpql, TradePostSort sort) {
        TradePostSort normalizedSort = sort == null ? TradePostSort.LATEST : sort;

        // id를 보조 정렬 기준으로 사용해 페이지 사이의 순서를 안정적으로 유지한다.
        jpql.append(switch (normalizedSort) {
            case LATEST -> " ORDER BY post.createdAt DESC, post.id DESC";
            case PRICE_ASC -> " ORDER BY post.price ASC, post.id DESC";
            case PRICE_DESC -> " ORDER BY post.price DESC, post.id DESC";
        });
    }

    private void bindSearchParameters(
            TypedQuery<?> query,
            String keyword,
            TradeStatus status
    ) {
        if (hasKeyword(keyword)) {
            query.setParameter("keyword", "%" + keyword + "%");
        }
        if (status != null) {
            query.setParameter("status", status);
        }
    }

    private boolean hasKeyword(String keyword) {
        return keyword != null && !keyword.isBlank();
    }

    private void validatePagination(int limit, int offset) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit은 1 이상이어야 합니다.");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset은 0 이상이어야 합니다.");
        }
    }
}
