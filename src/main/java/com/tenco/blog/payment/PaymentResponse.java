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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    } // end of PrePareDTO


    @Data
    public static class CompleteDTO{
        private Integer amount;
        private Integer currentPoint;


        @Builder
        public CompleteDTO(Integer amount, Integer currentPoint) {
            this.amount = amount;
            this.currentPoint = currentPoint;
        }
    } // end of CompleteDTO

    // 포트원 단건 조회 api 응답
    @Data
    // JSON 문자열에는 값이 있고, 자바 클래스 필드에는 선언이 없다면 그냥 무시해라
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PortOnePayment{
        private String status; // READY, PAID , FAILED , CANCELLED
        private String id; // paymentId (우리 서버에서 생성한 주문 번호)
        private String pgTxId; // pg사 거래번호 (간혹 null 이 될 수 있음)
        private Amount amount;

        @Data
        public static class Amount{
            private Integer total;
            private Integer taxFree;
            private Integer vat;
        }
    }


} // end of Response
