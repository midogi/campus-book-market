package com.skc04.campusbookmarket.trade;

import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.member.repository.SpringDataMemberRepository;
import com.skc04.campusbookmarket.post.domain.*;
import com.skc04.campusbookmarket.post.repository.SpringDataTradePostRepository;
import com.skc04.campusbookmarket.post.service.*;
import com.skc04.campusbookmarket.trade.domain.RequestStatus;
import com.skc04.campusbookmarket.trade.repository.TradeRequestRepository;
import com.skc04.campusbookmarket.trade.service.*;
import com.skc04.campusbookmarket.web.session.SessionConst;
import com.skc04.campusbookmarket.web.trade.TradeCsrf;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 실제 Flyway/H2/JPA/서비스 트랜잭션을 사용한다. 동시성 테스트에는 바깥 테스트 트랜잭션을 두지 않는다. */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:trade-flow;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.jpa.show-sql=false", "app.file.upload-dir=./build/test-uploads"})
@AutoConfigureMockMvc
class TradeFlowTest {
    @Autowired TradeService trades;
    @Autowired TradePostService postService;
    @Autowired SpringDataMemberRepository members;
    @Autowired SpringDataTradePostRepository posts;
    @Autowired TradeRequestRepository requests;
    @Autowired MockMvc mvc;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;
    Member seller, buyer, other;
    TradePost post;

    @BeforeEach void fixture() {
        seller = member("Seller"); buyer = member("Buyer"); other = member("Other");
        post = posts.save(new TradePost("JPA textbook", 15000, seller, "Clean book"));
    }

    @Test void applyReserveCompleteAndHistory() {
        Long id = trades.apply(post.getId(), buyer.getId());
        Long waiting = trades.apply(post.getId(), other.getId());
        assertThat(posts.findById(post.getId()).orElseThrow().getStatus()).isEqualTo(TradeStatus.SALE);
        trades.reserve(id, seller.getId());
        assertThat(posts.findById(post.getId()).orElseThrow().getStatus()).isEqualTo(TradeStatus.RESERVED);
        trades.complete(id, seller.getId());
        assertThat(posts.findById(post.getId()).orElseThrow().getStatus()).isEqualTo(TradeStatus.SOLD);
        assertThat(requests.findById(id).orElseThrow().getStatus()).isEqualTo(RequestStatus.COMPLETED);
        assertThat(requests.findById(waiting).orElseThrow().getStatus()).isEqualTo(RequestStatus.REJECTED);
        assertThat(trades.history(buyer.getId(), 1).getContent()).anySatisfy(r -> {
            assertThat(r.status()).isEqualTo(RequestStatus.COMPLETED);
            assertThat(r.price()).isEqualTo(15000);
        });
        assertThat(trades.history(seller.getId(), 1).getTotalElements()).isEqualTo(2);
    }

    @Test void ownPostAndDuplicateAreRejected() {
        assertThatThrownBy(() -> trades.apply(post.getId(), seller.getId())).isInstanceOf(TradeConflictException.class);
        trades.apply(post.getId(), buyer.getId());
        assertThatThrownBy(() -> trades.apply(post.getId(), buyer.getId())).isInstanceOf(TradeConflictException.class);
    }

    @Test void reservedPostCannotReceiveOrAcceptAnotherRequest() {
        Long a = trades.apply(post.getId(), buyer.getId());
        Long b = trades.apply(post.getId(), other.getId());
        trades.reserve(a, seller.getId());
        assertThatThrownBy(() -> trades.reserve(b, seller.getId())).isInstanceOf(TradeConflictException.class);
        assertThatThrownBy(() -> trades.apply(post.getId(), member("Late").getId())).isInstanceOf(TradeConflictException.class);
    }

    @Test void onlySellerCanAcceptRejectAndComplete() {
        Long id = trades.apply(post.getId(), buyer.getId());
        assertThatThrownBy(() -> trades.reserve(id, other.getId())).isInstanceOf(PostAccessDeniedException.class);
        assertThatThrownBy(() -> trades.reject(id, buyer.getId())).isInstanceOf(PostAccessDeniedException.class);
        trades.reserve(id, seller.getId());
        assertThatThrownBy(() -> trades.complete(id, buyer.getId())).isInstanceOf(PostAccessDeniedException.class);
        assertThatThrownBy(() -> trades.cancel(id, other.getId())).isInstanceOf(PostAccessDeniedException.class);
    }

    @Test void cancellationReopensAndAllowsAnotherBuyer() {
        Long a = trades.apply(post.getId(), buyer.getId());
        Long b = trades.apply(post.getId(), other.getId());
        trades.reserve(a, seller.getId());
        trades.cancel(a, buyer.getId());
        assertThat(posts.findById(post.getId()).orElseThrow().getStatus()).isEqualTo(TradeStatus.SALE);
        trades.reserve(b, seller.getId());
        trades.complete(b, seller.getId());
        assertThat(requests.findById(a).orElseThrow().getStatus()).isEqualTo(RequestStatus.CANCELLED);
    }

    @Test void sellerCanCancelReservationAndBuyerCanReapply() {
        Long id = trades.apply(post.getId(), buyer.getId());
        trades.reserve(id, seller.getId());
        trades.cancel(id, seller.getId());
        assertThat(trades.apply(post.getId(), buyer.getId())).isNotEqualTo(id);
    }

    @Test void terminalStatesCannotBeChangedAndCannotCompletePending() {
        Long id = trades.apply(post.getId(), buyer.getId());
        assertThatThrownBy(() -> trades.complete(id, seller.getId())).isInstanceOf(TradeConflictException.class);
        trades.reject(id, seller.getId());
        assertThatThrownBy(() -> trades.reserve(id, seller.getId())).isInstanceOf(TradeConflictException.class);
        assertThatThrownBy(() -> trades.cancel(id, buyer.getId())).isInstanceOf(TradeConflictException.class);
        Long newId = trades.apply(post.getId(), buyer.getId());
        trades.reserve(newId, seller.getId()); trades.complete(newId, seller.getId());
        assertThatThrownBy(() -> trades.complete(newId, seller.getId())).isInstanceOf(TradeConflictException.class);
        assertThatThrownBy(() -> trades.cancel(newId, seller.getId())).isInstanceOf(TradeConflictException.class);
    }

    @Test void postEditsAndDeletionPreserveNegotiationAndHistory() {
        Long id = trades.apply(post.getId(), buyer.getId());
        assertThatThrownBy(() -> postService.update(post.getId(), seller.getId(), "Changed", 1, "Changed"))
                .isInstanceOf(TradeConflictException.class);
        assertThatThrownBy(() -> postService.findOwnedById(post.getId(), seller.getId()))
                .isInstanceOf(TradeConflictException.class);
        trades.cancel(id, buyer.getId());
        postService.update(post.getId(), seller.getId(), "Changed", 20000, "Changed");
        assertThat(requests.findById(id).orElseThrow().getPrice()).isEqualTo(15000);
        assertThatThrownBy(() -> postService.delete(post.getId(), seller.getId())).isInstanceOf(TradeConflictException.class);
    }

    @Test void manualStatusCannotBypassWorkflowAndLegacyCanReopen() {
        assertThatThrownBy(() -> postService.updateStatus(post.getId(), seller.getId(), TradeStatus.SOLD))
                .isInstanceOf(TradeConflictException.class);
        post.changeStatus(TradeStatus.RESERVED); posts.save(post);
        postService.updateStatus(post.getId(), seller.getId(), TradeStatus.SALE);
        Long id = trades.apply(post.getId(), buyer.getId()); trades.reserve(id, seller.getId());
        assertThatThrownBy(() -> postService.updateStatus(post.getId(), seller.getId(), TradeStatus.SALE))
                .isInstanceOf(TradeConflictException.class);
    }

    @Test void privateHistoryAndPagination() throws Exception {
        trades.apply(post.getId(), buyer.getId());
        assertThatThrownBy(() -> trades.received(post.getId(), buyer.getId(), 1)).isInstanceOf(PostAccessDeniedException.class);
        assertThat(trades.history(other.getId(), 1).getTotalElements()).isZero();
        for (int i = 0; i < 11; i++) {
            TradePost another = posts.save(new TradePost("Book", 1000, seller, "Description"));
            trades.apply(another.getId(), buyer.getId());
        }
        assertThat(trades.history(buyer.getId(), 1).getContent()).hasSize(10);
        assertThat(trades.history(buyer.getId(), 2).getContent()).hasSize(2);
        assertThat(trades.history(buyer.getId(), Integer.MIN_VALUE).getNumber()).isZero();
        mvc.perform(get("/trades").session(session(buyer)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("/trades?page=2")));
        mvc.perform(get("/trades").param("page", "2").session(session(buyer)))
                .andExpect(status().isOk()).andExpect(content().string(containsString("/trades?page=1")));
    }

    @Test void reservationAndPostStatusRollBackTogether() {
        Long id = trades.apply(post.getId(), buyer.getId());
        var transaction = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            trades.reserve(id, seller.getId());
            throw new IllegalStateException("Simulated failure before commit");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(requests.findById(id).orElseThrow().getStatus()).isEqualTo(RequestStatus.REQUESTED);
        assertThat(posts.findById(post.getId()).orElseThrow().getStatus()).isEqualTo(TradeStatus.SALE);
    }

    @Test void reservedAndCompletedPostsCannotBeEditedOrDeleted() {
        Long id = trades.apply(post.getId(), buyer.getId());
        trades.reserve(id, seller.getId());
        assertThatThrownBy(() -> postService.update(post.getId(), seller.getId(), "Changed", 1, "Changed"))
                .isInstanceOf(TradeConflictException.class);
        assertThatThrownBy(() -> postService.delete(post.getId(), seller.getId())).isInstanceOf(TradeConflictException.class);
        trades.complete(id, seller.getId());
        assertThatThrownBy(() -> postService.update(post.getId(), seller.getId(), "Changed", 1, "Changed"))
                .isInstanceOf(TradeConflictException.class);
        assertThatThrownBy(() -> postService.delete(post.getId(), seller.getId())).isInstanceOf(TradeConflictException.class);
    }

    @Test void completionRacingCancellationLeavesConsistentStates() throws Exception {
        Long id = trades.apply(post.getId(), buyer.getId());
        trades.reserve(id, seller.getId());
        assertThat(race(() -> trades.complete(id, seller.getId()), () -> trades.cancel(id, buyer.getId())))
                .containsExactlyInAnyOrder(true, false);
        RequestStatus state = requests.findById(id).orElseThrow().getStatus();
        TradeStatus postState = posts.findById(post.getId()).orElseThrow().getStatus();
        assertThat(postState).isEqualTo(state == RequestStatus.COMPLETED ? TradeStatus.SOLD : TradeStatus.SALE);
    }

    @Test void concurrentReservationsHaveExactlyOneWinner() throws Exception {
        Long a = trades.apply(post.getId(), buyer.getId());
        Long b = trades.apply(post.getId(), other.getId());
        assertThat(race(() -> trades.reserve(a, seller.getId()), () -> trades.reserve(b, seller.getId())))
                .containsExactlyInAnyOrder(true, false);
        assertThat(requests.findByPostIdAndStatus(post.getId(), RequestStatus.RESERVED)).hasSize(1);
    }

    @Test void concurrentDuplicateRequestsHaveExactlyOneWinner() throws Exception {
        assertThat(race(() -> trades.apply(post.getId(), buyer.getId()), () -> trades.apply(post.getId(), buyer.getId())))
                .containsExactlyInAnyOrder(true, false);
        assertThat(requests.findByPostIdAndStatus(post.getId(), RequestStatus.REQUESTED)).hasSize(1);
    }

    @Test void webFlowUsesSessionCsrfPrgAndRendersBothLanguages() throws Exception {
        MockHttpSession buyerSession = session(buyer);
        mvc.perform(get("/posts/" + post.getId()).session(buyerSession)).andExpect(status().isOk())
                .andExpect(content().string(containsString("_tradeCsrf")));
        mvc.perform(post("/posts/" + post.getId() + "/requests").session(buyerSession))
                .andExpect(status().isForbidden());
        mvc.perform(post("/posts/" + post.getId() + "/requests").session(buyerSession).param("_tradeCsrf", "test-token"))
                .andExpect(redirectedUrl("/trades"));
        Long id = requests.findByPostIdAndStatus(post.getId(), RequestStatus.REQUESTED).get(0).getId();
        mvc.perform(get("/trades").session(buyerSession)).andExpect(status().isOk())
                .andExpect(content().string(containsString("JPA textbook")));
        mvc.perform(get("/posts/" + post.getId() + "/requests").session(session(seller)).header("Accept-Language", "en"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Confirm reservation")));
        mvc.perform(post("/trades/" + id + "/reserve").session(session(seller)).param("_tradeCsrf", "test-token"))
                .andExpect(redirectedUrl("/trades"));
        mvc.perform(post("/trades/" + id + "/complete").session(session(seller)).param("_tradeCsrf", "test-token"))
                .andExpect(redirectedUrl("/trades"));
        mvc.perform(post("/trades/" + id + "/complete").session(session(seller)).param("_tradeCsrf", "test-token"))
                .andExpect(status().isConflict());
    }

    @Test void anonymousProtectedAndMissingAndPublicRoutes() throws Exception {
        mvc.perform(get("/trades")).andExpect(status().is3xxRedirection());
        mvc.perform(post("/posts/" + post.getId() + "/requests")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/posts/" + post.getId())).andExpect(status().isOk());
        mvc.perform(post("/trades/9999999/reserve").session(session(seller)).param("_tradeCsrf", "test-token"))
                .andExpect(status().isNotFound());
    }

    private List<Boolean> race(Callable<Long> a, Callable<Long> b) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1);
        try {
            Callable<Boolean> first = racing(a, ready, start), second = racing(b, ready, start);
            Future<Boolean> fa = pool.submit(first), fb = pool.submit(second);
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue(); start.countDown();
            return List.of(fa.get(15, TimeUnit.SECONDS), fb.get(15, TimeUnit.SECONDS));
        } finally { start.countDown(); pool.shutdownNow(); }
    }
    private Callable<Boolean> racing(Callable<Long> action, CountDownLatch ready, CountDownLatch start) {
        return () -> { ready.countDown(); start.await();
            try { action.call(); return true; } catch (TradeConflictException expected) { return false; }
        };
    }
    private Member member(String name) { return members.save(new Member(UUID.randomUUID().toString(), "test-hash", name)); }
    private MockHttpSession session(Member member) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, member); session.setAttribute(TradeCsrf.TOKEN, "test-token");
        return session;
    }
}
