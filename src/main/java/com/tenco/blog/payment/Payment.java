package com.tenco.blog.payment;


import com.tenco.blog.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

/**
 *  결제 내역 엔티티(포트원 PG 연동)
 *  목적 : 포인트 충전 시 발생할 실제 결제 1건을 기록
 *  - paymentId : 우리 서버가 발급한 결제 건 식별자 (PG 전달 값)
 *  - pgTxId : PG사 거래 번호 (포트원 단건 조회에서 응답 받을 예정, 추정용)
 *  - amount : 결제 금액 (천원,오천원...)
 *  - status : PAID (결제 완료)
 */
@Data
@NoArgsConstructor
@Table(name = "payment_tb")
@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 우리 서버가 발급한 결제에 대한 식별자 값 - 유니크 중복 방지 필수!
    @Column(unique = true, nullable = false)
    private String paymentId;

    // PG사 거래 번호
    // 참고 : PG사에서 간혹 null값이 들어 올 수 있음 -> null 허용
    private String pgTxId;

    // 결제한 사용자 정보
    // Payment : User - User가 Payment를 여러번 사용 가능
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    // 가격
    @Column(nullable = false)
    private Integer amount;

    // 결제 상태
    // PAID - 결제 완료 / FAILED - 결제 실패/ CANCELLED - 전액 취소 (환불)
    @Column(nullable = false)
    private String status;

    // 결제 시간
    @CreationTimestamp
    private Timestamp createdAt;


    @Builder
    public Payment( String paymentId, String pgTxId, User user,
                    Integer amount, String status, Timestamp createdAt) {

        this.paymentId = paymentId;
        this.pgTxId = pgTxId;
        this.user = user;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }


}
