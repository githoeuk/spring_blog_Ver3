🚀 Premium Content & Point Billing Platform (Spring Blog Ver.3)
본 프로젝트는 단순한 블로그 기능을 넘어, PortOne PG API를 활용한 실제 결제 프로세스와 포인트 기반의 유료 콘텐츠 소비 아키텍처를 구축한 웹 애플리케이션입니다.<br>
1. 🛠 핵심 비즈니스 로직 상세 (Core Logic)
① 포인트 충전 및 위변조 방지 검증
사전 결제 요청 (Prepare): 결제창을 띄우기 전, 서버에서 고유한 paymentId를 생성하여 발급합니다. 이는 주문 번호의 중복을 막고, 서버가 인지하지 못한 결제 요청을 차단하기 위함입니다.
서버 간 검증 (Server-to-Server): 결제 완료 후, 클라이언트가 보낸 금액 정보를 그대로 믿지 않고 포트원 API를 통해 실제 결제된 금액을 재조회하여 검증합니다.
② 유료 콘텐츠 구매 및 차감 로직
복합 권한 검증: 유료 게시글 조회 시 작성자 본인 여부, 기존 구매 이력, 보유 포인트 잔액을 순차적으로 확인합니다.
원자성 보장: PurchaseService에서 포인트 차감과 구매 내역 저장을 하나의 @Transactional로 묶어, 어느 하나라도 실패할 경우 전체 로직을 롤백하여 데이터 무결성을 유지합니다.
<br>
2. 💡 기술적 문제 해결 (Troubleshooting)
① OSIV 비활성화를 통한 커넥션 효율화
Problem: OSIV(Open Session In View)가 활성화되어 있으면 API 응답이 끝날 때까지 DB 커넥션을 점유하여 대규모 트래픽 발생 시 커넥션 고갈 위험이 있었습니다.
Solution: application.yml에서 OSIV를 false로 설정하고, 필요한 데이터는 서비스 레이어에서 DTO로 변환하여 반환함으로써 트랜잭션 종료 즉시 커넥션을 반환하도록 설계했습니다.
② N+1 문제 해결 및 Fetch Join 최적화
Problem: OSIV 비활성화 후 View에서 연관된 엔티티(User, Board 등)에 접근할 때 LazyInitializationException이 발생했습니다.
Solution: JpaRepository에서 JOIN FETCH를 사용하여 연관된 데이터를 한 번의 쿼리로 조회하도록 최적화했습니다.
<br>
3. 🏗 아키텍처 및 보안 설계 (Architecture & Security)
① 계층형 아키텍처 (3-Tier)
Controller - Service - Repository - Entity 구조를 철저히 분리하여 유지보수성을 높였으며, 엔티티 보호를 위해 모든 데이터 전송에 DTO를 사용했습니다.
② 인터셉터 기반의 다단계 보안
LoginInterceptor: 인증이 필요한 모든 경로(/board/**, /user/** 등)를 보호합니다.
AdminInterceptor: 관리자 권한(ADMIN) 확인 로직을 분리하여 관리자 전용 대시보드의 보안을 강화했습니다.
SessionInterceptor: 화면에 필요한 공통 유저 정보를 postHandle 시점에 주입하여 컨트롤러 중복 코드를 제거했습니다.
<br>
4. 📺 주요 화면 가이드 (UI/UX)
유료 콘텐츠 상세보기: 구매 전에는 콘텐츠가 가려지며, 보유 포인트와 대조하여 '구매하기' 버튼이 동적으로 노출됩니다.
포인트 충전 페이지: 포트원 브라우저 SDK를 활용하여 사용자에게 익숙한 PG 결제창을 제공하고, 다양한 금액 선택 버튼을 배치했습니다.
마이 프로필: 현재 보유 포인트와 프로필 이미지 업로드 상태를 한눈에 확인할 수 있습니다.
<br>
5. 📊 핵심 워크플로우 분석 (Deep Dive)
메일 인증 및 회원가입 신뢰성 확보
인증번호 발송: MailUtil로 생성한 6자리 난수를 세션에 이메일 계정별로 저장합니다.
인증 도장 부여: 인증 성공 시 세션에 verified_email이라는 '도장'을 찍어둡니다.
최종 가입 검증: 가입 요청 시 세션의 인증 정보와 요청 이메일이 일치하는지 재확인하여 이메일 위변조 가입을 원천 차단합니다.
<br>
6. 🛠 DB 스키마 설계 (Entity Relationship)
Purchase (구매 내역): User와 Board 사이의 다대다(M:N) 관계를 해소하기 위한 연결 엔티티로, 유니크 제약 조건을 통해 중복 구매를 방지합니다.
Payment (결제 내역): 외부 결제 고유 번호(paymentId)를 관리하여 실제 자산(포인트) 변동 이력을 추적합니다.
Board (게시글): premium 필드(Boolean)를 통해 콘텐츠의 유/무료 여부를 구분합니다.
<br>
7. 🔒 보안 및 편의 기능 (Security & Utils)
암호화: BCryptPasswordEncoder를 사용하여 사용자 비밀번호를 단방향 해시 암호화하여 저장합니다.
글로벌 예외 처리: @ControllerAdvice를 활용해 포인트 부족(NotEnoughException), 권한 없음(Exception403) 등 비즈니스 예외를 통합 관리하고 사용자에게 알림창으로 피드백을 전달합니다.
날짜 변환 유틸: MyDateUtil을 통해 Timestamp를 가독성 좋은 yyyy-MM-dd HH:mm 형식으로 일관되게 포맷팅합니다.
