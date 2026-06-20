🚀 Premium Content & Point Billing Platform (Spring Blog Ver.3)본 프로젝트는 단순한 블로그 기능을 넘어, PortOne PG API를 활용한 실제 결제 프로세스와 포인트 기반의 유료 콘텐츠 소비 아키텍처를 구축한 웹 애플리케이션입니다.  1. 🛠 핵심 비즈니스 로직 (Core Logic)① 포인트 충전 및 위변조 방지 검증사전 결제 요청 (Prepare): 결제창을 띄우기 전, 서버에서 고유한 paymentId를 생성하여 발급합니다. 이는 주문 번호의 중복을 막고, 서버가 인지하지 못한 결제 요청을 차단하기 위함입니다.  서버 간 검증 (Server-to-Server): 결제 완료 후, 클라이언트가 보낸 금액 정보를 그대로 믿지 않고 포트원 API를 통해 실제 결제된 금액을 재조회하여 검증합니다.  ② 유료 콘텐츠 구매 및 차감 로직복합 권한 검증: 유료 게시글 조회 시 작성자 본인 여부, 기존 구매 이력, 보유 포인트 잔액을 순차적으로 확인합니다.  원자성 보장: PurchaseService에서 포인트 차감과 구매 내역 저장을 하나의 @Transactional로 묶어, 어느 하나라도 실패할 경우 전체 로직을 롤백하여 데이터 무결성을 유지합니다.  
2. 💡 기술적 문제 해결 (Troubleshooting)① OSIV 비활성화를 통한 커넥션 효율화Problem: OSIV(Open Session In View)가 활성화되어 있으면 API 응답이 끝날 때까지 DB 커넥션을 점유하여 대규모 트래픽 발생 시 커넥션 고갈 위험이 있었습니다.  Solution: application.yml에서 OSIV를 false로 설정하고, 필요한 데이터는 서비스 레이어에서 DTO로 변환하여 반환함으로써 트랜잭션 종료 즉시 커넥션을 반환하도록 설계했습니다.  ② N+1 문제 해결 및 Fetch Join 최적화Problem: OSIV 비활성화 후 View에서 연관된 엔티티(User, Board 등)에 접근할 때 LazyInitializationException이 발생했습니다.  Solution: JpaRepository에서 JOIN FETCH를 사용하여 연관된 데이터를 한 번의 쿼리로 조회하도록 최적화했습니다.  
3. 🏗 아키텍처 및 보안 설계 (Architecture & Security)① 계층형 아키텍처 (3-Tier)Controller - Service - Repository - Entity 구조를 철저히 분리하여 유지보수성을 높였으며, 엔티티 보호를 위해 모든 데이터 전송에 DTO를 사용했습니다.  ② 인터셉터 기반의 다단계 보안LoginInterceptor: 인증이 필요한 모든 경로(/board/, /user/ 등)를 보호합니다.  AdminInterceptor: 관리자 권한(ADMIN) 확인 로직을 분리하여 관리자 전용 대시보드의 보안을 강화했습니다.  SessionInterceptor: 화면에 필요한 공통 유저 정보를 postHandle 시점에 주입하여 컨트롤러 중복 코드를 제거했습니다.  
4. 📺 주요 화면 가이드 및 워크플로우 (UI/UX & Workflow)유료 콘텐츠 상세보기: 구매 전에는 콘텐츠가 가려지며, 보유 포인트와 대조하여 '구매하기' 버튼이 동적으로 노출됩니다.  포인트 충전 페이지: 포트원 브라우저 SDK를 활용하여 사용자에게 익숙한 PG 결제창을 제공하고, 다양한 금액 선택 버튼을 배치했습니다.  메일 인증 및 회원가입 신뢰성 확보:인증번호 발송: MailUtil로 생성한 6자리 난수를 세션에 이메일 계정별로 저장합니다.  인증 도장 부여: 인증 성공 시 세션에 verified_email이라는 '도장'을 찍어둡니다.  최종 가입 검증: 가입 요청 시 세션의 인증 정보와 요청 이메일이 일치하는지 재확인하여 이메일 위변조 가입을 원천 차단합니다. 
5. 🔒 보안 및 편의 기능 (Security & Utils)기술적인 안정성 외에도 실제 운영 환경을 고려한 보안 및 편의 기능을 배치했습니다.  비밀번호 보안: BCryptPasswordEncoder를 사용하여 사용자 비밀번호를 단방향 해시 암호화하여 저장합니다.  이메일 위변조 방지: 회원가입 시 세션에 저장된 인증 이메일과 실제 가입 요청 이메일을 대조하여 비정상적인 가입 시도를 차단합니다.  날짜 포맷팅 유틸: MyDateUtil을 사용하여 Timestamp 데이터를 yyyy-MM-dd HH:mm 형식으로 변환, 모든 뷰 레이어에서 일관된 날짜 형식을 제공합니다. 
6. 🛠 DB 스키마 설계 (Entity Relationship)데이터의 무결성과 효율적인 조회를 위해 정규화된 테이블 구조를 설계했습니다. 특히, 구매 이력과 사용자 권한은 별도 테이블로 분리하여 확장성을 확보했습니다.  테이블명역할 및 특징핵심 설정user_tb사용자 기본 정보 및 포인트 관리  email, username 유니크 제약 조건  board_tb게시글 데이터 및 유료 여부 관리  premium 컬럼(Boolean)으로 유료 글 구분  purchase_tb[핵심] 사용자별 게시글 구매 이력  uk_user_board (User+Board) 복합 유니크 제약 조건으로 중복 구매 방지  payment_tb외부 PG사(포트원)를 통한 실제 포인트 충전 내역  paymentId 유니크 설정으로 결제 데이터 정합성 보장  user_role_tb사용자의 권한(ADMIN, USER) 관리  CascadeType.ALL 설정으로 User와 생명주기 동기화  reply_tb게시글별 댓글 데이터 관리  게시글 삭제 시 관련 댓글 일괄 삭제(Modifying 쿼리)  
7. 📊 유료 콘텐츠 구매 상세 시퀀스 다이어그램단순한 성공 흐름뿐만 아니라, 코드에 구현된 방어적 로직(Exception Handling)과 데이터 정합성 보장 과정을 포함하여 설계 역량을 강조했습니다.  코드 스니펫sequenceDiagram
    autonumber
    participant Client as 사용자 (Browser)
    participant Auth as LoginInterceptor
    participant Controller as BoardController
    participant Service as PurchaseService
    participant Repo as Repository (Board/User/Purchase)
    participant DB as Database (MySQL)
    participant GlobalEx as GlobalExceptionHandler

    Note over Client, GlobalEx: [유료 콘텐츠 구매 프로세스]

    Client->>Controller: POST /board/{id}/purchase
    
    activate Controller
    Controller->>Auth: 1. 인증 확인 (Session)
    Auth-->>Controller: 인증 완료 (sessionUser)

    Controller->>Service: 2. 구매 로직 실행 (userId, boardId)
    activate Service

    Service->>Repo: 3. 게시글 존재 여부 조회 (findById)
    Repo->>DB: SELECT * FROM board_tb
    DB-->>Repo: boardEntity 반환
    alt 게시글 없음
        Service-->>GlobalEx: Exception404 ("게시글을 찾을 수 없습니다")
        GlobalEx-->>Client: alert('게시글을 찾을 수 없습니다') & history.back()
    end

    Service->>Service: 4. 본인 글 구매 여부 확인 (방어적 코드)
    alt 작성자 == 구매자
        Service-->>GlobalEx: Exception400 ("자신이 작성한 게시글은 구매할 수 없습니다")
    end

    Service->>Repo: 5. 중복 구매 확인 (existsByUserIdAndBoardId)
    Repo->>DB: SELECT COUNT(*) FROM purchase_tb
    DB-->>Repo: 결과값
    alt 이미 구매함
        Service-->>GlobalEx: Exception400 ("이미 구매한 게시글입니다")
    end

    Service->>Repo: 6. 사용자 포인트 조회
    Repo->>DB: SELECT * FROM user_tb
    DB-->>Repo: userEntity

    Service->>Service: 7. 포인트 차감 (deductPoint)
    alt 포인트 부족
        Service-->>GlobalEx: NotEnoughException ("포인트가 부족합니다")
        GlobalEx-->>Client: alert('포인트가 부족합니다') & location.href='/user/detail'
    end

    Note right of Service: [Transactional 시작]
    Service->>Repo: 8. 구매 이력 저장 (save)
    Repo->>DB: INSERT INTO purchase_tb
    Service->>Repo: 9. 유저 포인트 업데이트 (save)
    Repo->>DB: UPDATE user_tb SET point = ?
    Note right of Service: [Transactional 완료 - Commit]
    
    Service-->>Controller: 10. 업데이트된 User 객체 반환
    deactivate Service

    Controller->>Controller: 11. 세션 정보 동기화 (setAttribute)
    Controller-->>Client: 12. 리다이렉트 (redirect:/board/{id})
    deactivate Controller
