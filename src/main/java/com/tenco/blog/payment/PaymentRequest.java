package com.tenco.blog.payment;

import com.tenco.blog._core.errors.Exception400;
import lombok.Data;

public class PaymentRequest {

    // 사전 결제 요청 DTO
    @Data
    public static class PrepareDTO{
        private Integer amount; // 충전 금액

        public void validate(){
            if (amount == null || amount <= 0 ){
                throw new Exception400("최소 충전 금액은 1000P 입니다");
            }
            if (amount < 1000 ){
                throw new Exception400("최소 충전 금액은 1000P 입니다");
            }
            if (amount > 100000 ){
                throw new Exception400("최대 충전 금액은 100000P 입니다.");
            }
        }
    } // end of PrepareDTO

    @Data
    public static class CompleteDTO {
        private String paymentId;

        // 유효성 검사
        public void validate(){
            if (paymentId == null || paymentId.trim().isEmpty()){
                throw new Exception400("결제 건 식별자가 필요합니다.");
            }
        }
    } // end of CompleteDTO


}
