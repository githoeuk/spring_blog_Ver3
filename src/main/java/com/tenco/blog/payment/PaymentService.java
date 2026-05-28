package com.tenco.blog.payment;

import com.tenco.blog._core.errors.Exception400;
import com.tenco.blog._core.errors.Exception404;
import com.tenco.blog.user.User;
import com.tenco.blog.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;


    @Value("${portone.store-id}")
    private String storeId;
    @Value("${portone.channel-key}")
    private String channelKey;
    @Value("${portone.api-secret}")
    private String apiSecret;


    /**
     * 결제 사전 요청 생성
     * 프론트엔드가 결제창을 띄우기 전에 서버로부터 고유한결제 건 식별자를
     * 서버측에서 생성해서 발급 시켜준다
     * <p>
     * - 중복 결제 방지 (paymentId 유니크 설정함)
     * - 위변조 방지 (paymenId 서버측에서 생성해서 내려 줌)
     *
     */
    public PaymentResponse.PrePareDTO  결제요청생성(Integer userid, Integer amount) {


        // 1, 사용자 존재 유무 확인
        if (!userRepository.existsById(userid)) {
            throw new Exception404("사용자를 찾을 수 없습니다.");
        }

        // 2. paymentId 생성 및 중복 확인
        String paymentId = generatePaymentId(userid);

        // 2.1 중복 방지 확인

        // 2.2 - 중복 발생 시 - 다시 주문번호 생성 -> 다시 확인
        while (paymentRepository.existsByPaymentId(paymentId)) {
            paymentId = generatePaymentId(userid);
        }

        return new PaymentResponse.PrePareDTO (paymentId,amount,storeId,channelKey);
    }

    private String generatePaymentId(Integer userId) {
        return "point_" + userId + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }


    /*
        ( 위변조 방지 로직 )
        결제 검증 후 포인트 충전 (비즈니스 로직)
        포트원 결제 후, 프론트가 보낸 paymentId 로
        포트원 서버에 직접 조회 (Server to Server) - 한번 더 요청
     */
    @Transactional
    public PaymentResponse.CompleteDTO 결제검증후포인트충전(Integer userId, String paymentId) {

        // 1. 사전 검증 (실제 사용자가 맞는지 (userId) , paymentId DB에 이미 존재하는지 확인)
        User userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 혹시라도 중복된 paymentId가 있는지 확인 - 중복 결제 방지?
        if(paymentRepository.existsByPaymentId(paymentId)){
           throw new Exception400("이미 처리된 결과 입니다.");
        }

        // 2. 외부 통신 (Server to Server)
        PaymentResponse.PortOnePayment portOnePayment = 포트원단건결제조회(paymentId);

        // 3. 데이터 무결성 검증
        if(portOnePayment.getStatus() == null || "PAID".equals(portOnePayment.getStatus()) == false){
            throw new Exception400("결제가 완료되지 않았습니다. " + portOnePayment.getStatus());
        }

        if (portOnePayment.getAmount() == null || portOnePayment.getAmount().getTotal() == null){
            throw new Exception400("결제 금액 정보를 확인할 수 없습니다. ");
        }

        Integer amount = portOnePayment.getAmount().getTotal();
        // 4. 비즈니스 로직 처리 (결제한 금액만큼 -> 포인트 자동 충전)
        // 더티 체킹 (update)
        userEntity.chargePoint(amount);

        // UserApiController - chargePoint 삭제

        // 5. 결제 내역 저장 (insert 처리)
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .pgTxId(portOnePayment.getPgTxId())
                .user(userEntity)
                .amount(amount)
                .status("PAID")
                .build();

        paymentRepository.save(payment);

        log.info("결제 화인 후 포인트 충전 완료: userId = {} | paymentId = {} | amount = {} "
                ,userId,paymentId,amount );

        return new PaymentResponse.CompleteDTO(amount,userEntity.getPoint());
    }


//    // 1. 포트원 결제내역 단건조회 API 호출
//    const paymentResponse = await fetch(
//      `https://api.portone.io/payments/${encodeURIComponent(paymentId)}`,
//    {
//        headers: { Authorization: `PortOne ${PORTONE_API_SECRET}` },
//    }


    // 캡슐화
    private PaymentResponse.PortOnePayment 포트원단건결제조회(String paymentId){
        RestTemplate restTemplate = new RestTemplate();
        // 헤더  + 바디 조합 -- exchange() 메서드 호출 후 HTTP 요청 및 응답

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","PortOne "+apiSecret);

        // GET 요청 --> 바디 존재 x, 즉 헤더만 담아서 HTTP 요청 메세지 구축
        HttpEntity request = new HttpEntity(headers);

        // HTTP 요청 후 응답
        ResponseEntity<PaymentResponse.PortOnePayment> response = restTemplate.exchange(
                "https://api.portone.io/payments/" + paymentId, // 요청할 주소
                HttpMethod.GET,                                      // 요청 타입
                request,                                             // 전송 내용
                PaymentResponse.PortOnePayment.class                 // 응답 내용 타입
        );

        System.out.println("----------------");
        System.out.println(response.toString());

        return response.getBody();
    } // end of 포트원단건결제조회

} // end of PaymentService
