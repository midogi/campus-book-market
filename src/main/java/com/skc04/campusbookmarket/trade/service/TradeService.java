package com.skc04.campusbookmarket.trade.service;

import com.skc04.campusbookmarket.member.domain.Member;
import com.skc04.campusbookmarket.member.repository.SpringDataMemberRepository;
import com.skc04.campusbookmarket.post.domain.TradePost;
import com.skc04.campusbookmarket.post.domain.TradeStatus;
import com.skc04.campusbookmarket.post.repository.TradePostRepository;
import com.skc04.campusbookmarket.post.service.PostAccessDeniedException;
import com.skc04.campusbookmarket.post.service.PostNotFoundException;
import com.skc04.campusbookmarket.trade.domain.RequestStatus;
import com.skc04.campusbookmarket.trade.domain.TradeRequest;
import com.skc04.campusbookmarket.trade.repository.TradeRequestRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TradeService {
    private static final List<RequestStatus> ACTIVE = List.of(RequestStatus.REQUESTED, RequestStatus.RESERVED);
    private final TradePostRepository posts;
    private final TradeRequestRepository requests;
    private final SpringDataMemberRepository members;

    public TradeService(TradePostRepository posts, TradeRequestRepository requests, SpringDataMemberRepository members) {
        this.posts = posts;
        this.requests = requests;
        this.members = members;
    }

    @Transactional
    public Long apply(Long postId, Long buyerId) {
        TradePost post = lockPost(postId);
        if (post.isWrittenBy(buyerId)) throw new TradeConflictException("trade.error.self");
        if (post.getSellerId() == null || post.getStatus() != TradeStatus.SALE) {
            throw new TradeConflictException("trade.error.unavailable");
        }
        if (requests.existsByPostIdAndBuyerIdAndStatusIn(postId, buyerId, ACTIVE)) {
            throw new TradeConflictException("trade.error.duplicate");
        }
        Member buyer = members.findById(buyerId).orElseThrow(PostAccessDeniedException::new);
        Member seller = members.findById(post.getSellerId()).orElseThrow(PostAccessDeniedException::new);
        return requests.save(new TradeRequest(post, buyer, seller)).getId();
    }

    @Transactional
    public Long reserve(Long requestId, Long memberId) {
        TradeRequest request = lockedRequest(requestId);
        requireSeller(request, memberId);
        requireStatus(request, RequestStatus.REQUESTED);
        if (request.getPost().getStatus() != TradeStatus.SALE) {
            throw new TradeConflictException("trade.error.unavailable");
        }
        request.reserve();
        posts.updateStatus(request.getPost().getId(), TradeStatus.RESERVED);
        return request.getPost().getId();
    }

    @Transactional
    public Long complete(Long requestId, Long memberId) {
        TradeRequest request = lockedRequest(requestId);
        requireSeller(request, memberId);
        requireStatus(request, RequestStatus.RESERVED);
        requirePostReserved(request);
        request.complete();
        posts.updateStatus(request.getPost().getId(), TradeStatus.SOLD);
        // 예약 취소 시 다른 신청을 선택할 수 있게 대기 신청은 완료 시점에 거절한다.
        requests.findByPostIdAndStatus(request.getPost().getId(), RequestStatus.REQUESTED)
                .forEach(TradeRequest::reject);
        return request.getPost().getId();
    }

    @Transactional
    public Long cancel(Long requestId, Long memberId) {
        TradeRequest request = lockedRequest(requestId);
        boolean buyer = request.getBuyer().getId().equals(memberId);
        boolean seller = request.getSeller().getId().equals(memberId);
        if (!buyer && !seller) throw new PostAccessDeniedException();
        if (!ACTIVE.contains(request.getStatus())) throw new TradeConflictException("trade.error.transition");
        if (request.getStatus() == RequestStatus.REQUESTED && !buyer) throw new PostAccessDeniedException();
        if (request.getStatus() == RequestStatus.RESERVED) {
            requirePostReserved(request);
            posts.updateStatus(request.getPost().getId(), TradeStatus.SALE);
        }
        request.cancel();
        return request.getPost().getId();
    }

    @Transactional
    public Long reject(Long requestId, Long memberId) {
        TradeRequest request = lockedRequest(requestId);
        requireSeller(request, memberId);
        requireStatus(request, RequestStatus.REQUESTED);
        request.reject();
        return request.getPost().getId();
    }

    public Page<TradeRequestView> received(Long postId, Long memberId, int page) {
        TradePost post = posts.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
        if (!post.isWrittenBy(memberId)) throw new PostAccessDeniedException();
        return requests.findByPostIdOrderByIdDesc(postId, pageable(page)).map(r -> view(r, memberId));
    }

    public Page<TradeRequestView> history(Long memberId, int page) {
        return requests.findHistory(memberId, pageable(page)).map(r -> view(r, memberId));
    }

    private PageRequest pageable(int page) {
        // 웹 페이지는 1부터, Spring Data의 페이지 번호는 0부터 시작한다.
        return PageRequest.of(Math.max(1, page) - 1, 10);
    }

    private TradeRequestView view(TradeRequest r, Long memberId) {
        boolean seller = r.getSeller().getId().equals(memberId);
        boolean buyer = r.getBuyer().getId().equals(memberId);
        boolean pending = r.getStatus() == RequestStatus.REQUESTED;
        boolean reserved = r.getStatus() == RequestStatus.RESERVED;
        return new TradeRequestView(r.getId(), r.getPost().getId(), r.getTitle(), r.getPrice(),
                r.getBuyer().getName(), r.getSeller().getName(), r.getStatus(), r.getCreatedAt(),
                seller && pending && r.getPost().getStatus() == TradeStatus.SALE,
                seller && pending, (buyer && pending) || ((buyer || seller) && reserved), seller && reserved);
    }

    private TradeRequest lockedRequest(Long id) {
        Long postId = requests.findPostIdById(id).orElseThrow(() -> new PostNotFoundException(id));
        lockPost(postId);
        return requests.findById(id).orElseThrow(() -> new PostNotFoundException(id));
    }

    private TradePost lockPost(Long id) {
        // 모든 변경은 '게시글 → 신청' 순서로 잠근다. 중복 신청/예약/삭제의 경쟁을 직렬화한다.
        return posts.findByIdForUpdate(id).orElseThrow(() -> new PostNotFoundException(id));
    }
    private void requireSeller(TradeRequest r, Long id) {
        if (!r.getSeller().getId().equals(id)) throw new PostAccessDeniedException();
    }
    private void requireStatus(TradeRequest r, RequestStatus expected) {
        if (r.getStatus() != expected) throw new TradeConflictException("trade.error.transition");
    }
    private void requirePostReserved(TradeRequest r) {
        if (r.getPost().getStatus() != TradeStatus.RESERVED) throw new TradeConflictException("trade.error.transition");
    }
}
