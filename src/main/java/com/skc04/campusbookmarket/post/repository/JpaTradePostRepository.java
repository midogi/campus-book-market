package com.skc04.campusbookmarket.post.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.skc04.campusbookmarket.post.domain.QTradePost;
import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradePostSearchType;
import com.skc04.campusbookmarket.post.domain.TradePostSort;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

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
    private final SpringDataTradePostRepository springDataRepository;
    private final JPAQueryFactory queryFactory;

    public JpaTradePostRepository(
            SpringDataTradePostRepository springDataRepository,
            JPAQueryFactory queryFactory
    ) {

        this.springDataRepository = springDataRepository;
        this.queryFactory = queryFactory;
    }

    @Override
    public List<TradePost> findAll() {
        QTradePost post = QTradePost.tradePost;

        return queryFactory
                .selectFrom(post)
                .orderBy(post.id.asc())
                .fetch();
    }

    @Override
    public Optional<TradePost> findById(Long id) {
        return springDataRepository.findById(id);
    }

    @Override
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
    public List<TradePost> search(
            String keyword,
            TradePostSearchType searchType,
            TradeStatus status,
            TradePostSort sort,
            int limit,
            int offset
    ) {
        validatePagination(limit, offset);
        QTradePost post = QTradePost.tradePost;

        return queryFactory
                .selectFrom(post)
                .where(
                        keywordCondition(post, keyword, searchType),
                        statusCondition(post, status)
                )
                .orderBy(orderSpecifiers(post, sort))
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    @Override
    public long count(
            String keyword,
            TradePostSearchType searchType,
            TradeStatus status
    ) {
        QTradePost post = QTradePost.tradePost;

        Long result = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        keywordCondition(post, keyword, searchType),
                        statusCondition(post, status)
                )
                .fetchOne();

        return result == null ? 0L : result;
    }

    @Override
    public TradePost save(
            String title,
            long price,
            String sellerName,
            String description
    ) {
        TradePost post = new TradePost(title, price, sellerName, description);
        return springDataRepository.save(post);
    }

    @Override
    public Optional<TradePost> update(
            Long id,
            String title,
            long price,
            String sellerName,
            String description
    ) {
        Optional<TradePost> foundPost = springDataRepository.findById(id);
        if (foundPost.isEmpty()) {
            return Optional.empty();
        }
        TradePost post = foundPost.get();
        post.updateDetails(title, price, sellerName, description);

        return Optional.of(post);
    }

    @Override
    public Optional<TradePost> updateStatus(Long id, TradeStatus status) {

        Optional<TradePost> foundPost = springDataRepository.findById(id);
        if (foundPost.isEmpty()) {
            return Optional.empty();
        }
        TradePost post = foundPost.get();
        post.changeStatus(status);
        return Optional.of(post);
    }

    @Override
    public boolean deleteById(Long id) {
        Optional<TradePost> foundPost = springDataRepository.findById(id);
        if (foundPost.isEmpty()) {
            return false;
        }

        springDataRepository.delete(foundPost.get());
        return true;
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

    private BooleanExpression keywordCondition(
            QTradePost post,
            String keyword,
            TradePostSearchType searchType
    ) {
        if (!hasKeyword(keyword)) {
            return null;
        }

        TradePostSearchType normalizedSearchType =
                searchType == null
                        ? TradePostSearchType.TITLE
                        : searchType;

        return switch (normalizedSearchType) {
            case TITLE -> post.title.containsIgnoreCase(keyword);
            case SELLER -> post.sellerName.containsIgnoreCase(keyword);
        };
    }

    private BooleanExpression statusCondition(
            QTradePost post,
            TradeStatus status
    ) {
        if (status == null) {
            return null;
        }
        return post.status.eq(status);
    }

    private OrderSpecifier<?>[] orderSpecifiers(
            QTradePost post,
            TradePostSort sort
    ) {
        TradePostSort normalizedSort = sort == null
                ? TradePostSort.LATEST
                : sort;

        return switch (normalizedSort) {
            case LATEST -> new OrderSpecifier<?>[]{
                    post.createdAt.desc(),
                    post.id.desc()
            };
            case PRICE_ASC -> new OrderSpecifier<?>[]{
                    post.price.asc(),
                    post.id.desc()
            };
            case PRICE_DESC -> new OrderSpecifier<?>[]{
                    post.price.desc(),
                    post.id.desc()
            };
        };
    }
}
