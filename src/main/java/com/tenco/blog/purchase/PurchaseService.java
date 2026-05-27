package com.tenco.blog.purchase;


import com.tenco.blog._core.errors.Exception400;
import com.tenco.blog._core.errors.Exception404;
import com.tenco.blog.board.Board;
import com.tenco.blog.board.BoardRepository;
import com.tenco.blog.user.User;
import com.tenco.blog.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // 유료 게시글 기본 가격 설정
    private static final Integer PREMIUM_BOARD_PRICE = 500;

    /**
     * 유로 게시글 구매 로직
     * <p>
     * 트랜잭션처리! : 포인트 차감과 구매 내역(저장)을 하나의 트랜잭셔으로 처리
     *
     *
     */


    // 유료 게시글 구매
    @Transactional
    public User 구매하기(Integer userId, Integer boardID) {

        // 1. 게시글 조회
        Board boardEntity = boardRepository.findById(boardID)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));
        // 2. 유료 게시글 여부 확인
        // board <-- 컬럼 축가 예정 (premium) , 유료인지 무료 확인

        // 3. 작성자가 자신의 게시글을 구매 시도 방지 - 방어적 코드
        if (boardEntity.getUser() != null && boardEntity.getUser().getId().equals(userId)) {
            throw new Exception400("자신이 작성한 게시글은 구매할 수 없습니다.");
        }

        // 4. 이미 구매한 게시글인지 확인
        if (purchaseRepository.existsByUserIdAndBoardId(userId, boardID)) {
            throw new Exception400("이미 구매한 게시글입니다.");
        }

        // 5. 사용자 조회 (포인트 차감처리를 위함)
        User userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));

        // 6. 포인트 차감 (500포인트)
        userEntity.deductPoint(PREMIUM_BOARD_PRICE);

        // 구매 내역 저장
        Purchase purchase = Purchase.builder()
                .user(userEntity)
                .board(boardEntity)
                .build();

        // 구매 이력 저장
        purchaseRepository.save(purchase);

        // 사용자 포인트 차감된 상태 update 처리
        //  Controller단에서 차감된 포인트 상태를 세션 동기화 (반환) --?
        return userRepository.save(userEntity);
    } // end of 구매하기

    // 상세보기 진입 시 (유료 게시글이라면)
    // 구매 여부 확인
    public boolean 구매여부확인(Integer userId, Integer boardId) {
        if (userId == null) {
            return false;
        }

        return purchaseRepository.existsByUserIdAndBoardId(userId, boardId);
    }
}
