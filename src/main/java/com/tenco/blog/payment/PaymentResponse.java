package com.tenco.blog.payment;


//const response = await PortOne.requestPayment({
//    // Store ID 설정
//    storeId: "store-4ff4af41-85e3-4559-8eb8-0d08a2c6ceec",
//            // 채널 키 설정
//            channelKey: "channel-key-893597d6-e62d-410f-83f9-119f530b4b11",
//            paymentId: `payment-${crypto.randomUUID()}`,
//            orderName: "나이키 와플 트레이너 2 SD",
//            totalAmount: 1000,
//            currency: "CURRENCY_KRW",
//            payMethod: "CARD",
//});

import lombok.Builder;
import lombok.Data;

public class PaymentResponse {

    @Data
    public static class PrePareDTO  {
        private String paymentId;
        private Integer amount;
        private String storeId;
        private String channelKey;


        @Builder
        public PrePareDTO (String paymentId, Integer amount, String storeId, String channelKey) {
            this.paymentId = paymentId;
            this.amount = amount;
            this.storeId = storeId;
            this.channelKey = channelKey;
        }
    }


} // end of Response
