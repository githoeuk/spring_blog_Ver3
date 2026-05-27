package com.tenco.blog.purchase;

import com.tenco.blog.board.Board;
import com.tenco.blog.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

/**
 * 구매 내역 엔티티
 * <p>
 * User 와 Board의 구매 이력 관계를 표현함
 * <p>
 * 한 사람에  사용자는 여러 게시글을 구매할 수 있는가? -> O
 * 한 게시글은 여러 사용자에게 구매 될 수 있는가? -> O
 * <p>
 * User : Board - 다대다(Many to Many 관계로 표현이 되기 때문에
 * 중간 테이블(Purchase) 생성이 되어야 한다.
 * <p>
 * Purchase : User --> @Many To One --> join column 이름 지정
 * Purchase : Board --> @Many To One --> join column 이름 지정
 * 복합키 설정 방법도 확인 (테이블 기준으로 어노테이션 설정)
 *
 */

@Data
@NoArgsConstructor
@Entity
@Table(name = "purchase_tb",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_board", columnNames = {"user_id", "board_id"})
        })
public class Purchase {
    // 복합키 ... DB 물리적 구조 유니크 설정이 필요
    // id 추가
    // 누가 구매를 했는지 정보를 저장
    // 어떤 게시글을 구매 했는지 정보 저장
    // 게시글 구매 금액 (500 포인트 고정 예정) 지불한 포인트를 이력 관리
    // 언제 구매했는지 구매 시간 저장

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 단방향 관계 : purchase -> user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 단방향 관계 : purchase -> Board
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    // 구매시 지불한 포인트
    private Integer price;

    @CreationTimestamp
    private Timestamp createdAt;


    @Builder
    public Purchase(User user, Board board, Integer price) {
        this.user = user;
        this.board = board;
        this.price = price;
    }

} // end of PurchaseDTO
