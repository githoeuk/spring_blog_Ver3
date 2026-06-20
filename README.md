🚀 성능 최적화 및 안정성을 고려한 유료 콘텐츠 블로그 플랫폼 (Spring Blog Ver.3)
본 프로젝트는 기본적인 블로그 기능에 포트원(PortOne) API 기반의 결제 시스템과 유료 콘텐츠 소비 모델을 결합한 웹 애플리케이션입니다. 1인 풀사이클 개발로 진행되었으며, 실무적인 비즈니스 로직 구현과 백엔드 성능 최적화에 집중했습니다.<br>
1. 🛠 핵심 비즈니스 로직 (Core Business Logic)
① 전략적 결제 및 포인트 시스템 (Point & Payment)
단순한 포인트 충전을 넘어 결제의 신뢰성을 보장하기 위한 2단계 검증 시스템을 구축했습니다.
결제 사전 요청 (Prepare): 위변조 방지를 위해 서버에서 고유한 paymentId를 생성하여 발급하며, 중복 결제를 원천 차단합니다.
결제 검증 및 충전 (Complete): 포트원 서버와의 Server-to-Server 단건 조회를 통해 실제 결제 금액과 요청 금액의 일치 여부를 재검증한 후 포인트를 지급합니다.
데이터 정합성: 포인트 충전 내역 저장과 유저 포인트 업데이트를 하나의 **트랜잭션(@Transactional)**으로 관리하여 데이터 불일치를 방지합니다.
② 유료 콘텐츠 접근 제어 (Premium Content Access)
콘텐츠의 가치를 보호하기 위해 정교한 권한 검증 로직을 구현했습니다.
다중 조건 검증: 유료 게시글 상세 조회 시 작성자 본인 여부, 기존 구매 이력, 보유 포인트 잔액을 순차적으로 검증합니다.
방어적 설계: 본인이 작성한 유료 글은 구매할 수 없도록 제한하며, 중복 구매 시도를 비즈니스 예외로 처리합니다.
실시간 세션 동기화: 구매 완료 후 차감된 포인트를 즉시 세션에 반영하여 사용자 경험(UX)을 개선했습니다.
③ 신뢰 기반 메일 인증 시스템
JavaMailSender와 세션을 활용하여 인증된 이메일 주소로만 가입이 가능하게 설계했습니다.
인증 성공 시 세션에 **'인증 도장(verified_email)'**을 찍어 가입 단계에서의 정보 위변조를 방지합니다.
<br>
2. 💡 트러블 슈팅 및 성능 최적화 (Technical Challenges)
① OSIV 비활성화 및 성능 병목 해결
Issue: OSIV(Open Session In View) 활성화 시 API 응답 완료까지 DB 커넥션을 점유하여 트래픽 증가 시 커넥션 고갈 위험 발생.
Solution: application.yml에서 OSIV를 false로 설정하여 트랜잭션 종료 즉시 커넥션을 반환하도록 개선.
② N+1 문제 해결 및 Fetch Join 도입
Issue: OSIV 비활성화 이후 View 레이어에서 연관 엔티티 접근 시 LazyInitializationException 발생 및 반복적인 쿼리 실행(N+1).
Solution: JpaRepository에서 JOIN FETCH를 사용하여 연관된 User 및 Board 데이터를 한 번의 쿼리로 조회하도록 최적화했습니다.
③ AOP 기반 전역 예외 처리 및 보안 공통화
Solution: @ControllerAdvice를 사용하여 모든 비즈니스 예외를 통합 관리하고, 사용자에게 가독성 있는 피드백을 제공합니다.
Interceptor 설계: LoginInterceptor와 AdminInterceptor를 계층적으로 배치하여 인증과 권한 관리(RBAC)를 비즈니스 로직과 완벽히 분리했습니다.
<br>
3. 📊 시퀀스 다이어그램 (Workflow)
포인트 충전 및 검증 프로세스
sequenceDiagram
    participant User as 사용자 (Client)
    participant Server as 백엔드 서버
    participant PortOne as 포트원 API
    participant DB as Database

    User->>Server: 1. 결제 준비 요청 (/api/payment/prepare)
    Server->>DB: 2. 중복 검증 및 PaymentId 발급
    Server-->>User: 3. 결제 식별자 전달
    User->>PortOne: 4. 결제 실행 (PG창)
    PortOne-->>User: 5. 결제 성공 응답 (imp_uid)
    User->>Server: 6. 결제 완료 요청 (/api/payment/complete)
    Server->>PortOne: 7. Server-to-Server 단건 조회 (검증)
    Server->>DB: 8. 포인트 충전 및 내역 저장 (Transaction)
    Server-->>User: 9. 최종 성공 및 포인트 갱신
<br>
4. 🏗 Tech Stack
Backend: Java 21, Spring Boot 3.5.x, Spring Data JPA
Database: MySQL, H2 (Test)
Security: Spring Security (Crypto), Interceptor
Template Engine: Mustache
API: PortOne API, JavaMailSender
