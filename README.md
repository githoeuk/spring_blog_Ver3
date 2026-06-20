
-----

# 🚀 유료 콘텐츠 블로그 & 포인트 결제 시스템 (Spring Blog Ver.3)

본 프로젝트는 단순한 게시판을 넘어, **포트원(PortOne) API를 활용한 실제 결제 프로세스**와 **포인트 기반의 유료 콘텐츠 소비 모델**을 구축한 웹 애플리케이션입니다.

<br>

## 1\. 🛠 핵심 비즈니스 로직 상세 (Core Logic)

### ① 전략적 결제 시스템 (Point & Payment)

단순 충전 기능을 넘어 **위변조 방지 및 데이터 정합성**을 최우선으로 설계했습니다.

  * **사전 검증 시스템**: 결제 요청 전 서버에서 고유 `paymentId`를 생성하여 발급하고, 결제 완료 후 포트원 서버와의 **Server-to-Server 조회**를 통해 실제 결제 금액과 요청 금액의 일치 여부를 재검증합니다.
  * **트랜잭션 원자성**: 포인트 충전 및 유료 게시글 구매 시, **포인트 차감과 내역 저장**을 하나의 트랜잭션으로 묶어 데이터 불일치를 원천 차단했습니다.

### ② 유료 콘텐츠 접근 제어 (Premium Content Control)

콘텐츠의 가치를 보호하기 위한 단계별 인가 로직을 구현했습니다.

  * **다중 조건 검증**: 유료 게시글 상세 조회 시 `게시글의 유무`, `작성자 본인 여부`, `기존 구매 이력`을 순차적으로 확인합니다.
  * **방어적 코드 설계**: 작성자가 본인의 유료 글을 구매하려는 시도를 차단하고, 중복 결제를 방지하는 예외 처리 로직을 서비스 레이어에 견고하게 배치했습니다.

### ③ 메일 인증 기반 신뢰 시스템 (Mail Verification)

  * **JavaMailSender**와 세션을 활용하여, 인증된 이메일 주소로만 회원가입이 가능하도록 구현했습니다.
  * 회원가입 완료 시점에 세션에 저장된 '인증 도장'을 확인하여 메일 정보의 위변조를 방지합니다.

<br>

## 2\. 💡 기술적 문제 해결 (Troubleshooting)

### N+1 문제 해결 및 쿼리 최적화

  * **Issue**: 게시글 목록 및 댓글 조회 시 연관된 `User` 엔티티를 참조할 때 발생하는 N+1 성능 저하 확인.
  * **Solution**: `JPQL`의 `JOIN FETCH`를 활용하여 연관된 데이터를 한 번의 쿼리로 가져오도록 최적화했습니다.
  * **Code Example**:

<!-- end list -->

``` java
// BoardRepository.java
@Query("SELECT b FROM Board b JOIN FETCH b.user WHERE b.id = :id")
Optional<Board> findByIdJoinUser(@Param("id") Integer id);

```

### OSIV 비활성화에 따른 데이터 로딩 전략

  * **Issue**: `open-in-view: false` 설정으로 인해 컨트롤러 및 뷰 계층에서 지연 로딩(Lazy Loading) 예외 발생.
  * **Solution**: 서비스 레이어에서 필요한 모든 데이터를 포함하는 \*\*응답 전용 DTO(BoardResponse.DetailDTO)\*\*를 설계하여 트랜잭션 종료 전 데이터를 완전히 로딩 후 반환하도록 구조를 개선했습니다.

<br>

## 3\. 🏗 아키텍처 및 보안 설계 (Architecture & Security)

  * **계층형 아키텍처**: Controller - Service - Repository - Entity의 역할을 명확히 분리하여 유지보수성을 극대화했습니다.
  * **Spring Security & Interceptor**:
      * `BCryptPasswordEncoder`를 통한 비밀번호 암호화 저장.
      * `LoginInterceptor` 및 `AdminInterceptor`를 통한 계층적 권한 관리.
  * **전역 예외 처리**: `@ControllerAdvice`를 활용하여 비즈니스 예외(400, 401, 403, 404 등)를 통합 관리하고, 사용자에게 명확한 안내 팝업을 제공하도록 설계했습니다.

<br>

## 4\. 📺 주요 화면 가이드 (UI/UX)

  * **유료 콘텐츠**: 구매 전에는 콘텐츠가 가려지며 '구매하기' 버튼이 활성화됩니다.
  * **포인트 충전**: 사용자 친화적인 금액 선택 버튼과 포트원 통합 결제창을 제공합니다.

-----

GitHub 포트폴리오의 전문성을 한 단계 더 높여줄 **시퀀스 다이어그램**과 **핵심 서비스 로직의 상세 분석** 섹션을 추가해 드립니다. 이 내용은 기술 면접에서 "복잡한 비즈니스 로직을 어떻게 설계하고 검증했는가?"라는 질문에 대한 강력한 답변이 될 것입니다.

-----

### 5\. 📊 핵심 워크플로우 분석 (Deep Dive)

#### ① 포인트 충전 시퀀스 (PortOne API 연동)

사용자가 포인트를 충전할 때, 서버와 클라이언트, 그리고 외부 결제 API 간의 정합성을 보장하는 프로세스입니다.

``` mermaid
sequenceDiagram
    participant User as 사용자 (Client)
    participant Server as 블로그 서버
    participant PortOne as 포트원 API
    participant DB as Database

    User->>Server: 1. 결제 준비 요청 (/api/payment/prepare)
    Server->>Server: 2. 고유 PaymentId 생성 및 중복 검증
    Server-->>User: 3. PaymentId & 결제 정보 반환
    User->>PortOne: 4. 결제 요청 (SDK 호출)
    PortOne-->>User: 5. 결제 완료 응답 (imp_uid 등)
    User->>Server: 6. 결제 검증 및 충전 요청 (/api/payment/complete)
    Server->>PortOne: 7. Server-to-Server 단건 조회 (위변조 확인)
    Server->>DB: 8. 결제 상태 저장 & 유저 포인트 업데이트
    Server-->>User: 9. 최종 성공 응답 및 세션 동기화

```

#### ② 유료 콘텐츠 구매 로직 상세

`PurchaseService`에서 처리되는 유료 게시글 구매 로직은 **방어적 프로그래밍**의 정수를 보여줍니다.

| 단계              | 검증 내용                | 구현 방식 (Source Code 기반)                                     |
| :-------------- | :------------------- | :--------------------------------------------------------- |
| **1. 존재 확인**    | 게시글 및 사용자 존재 여부      | `findById().orElseThrow()`를 통한 예외 처리                       |
| **2. 본인 구매 방지** | 작성자가 자신의 글을 구매하는지 확인 | `boardEntity.getUser().getId().equals(userId)` 체크          |
| **3. 중복 구매 차단** | 이미 구매한 이력이 있는지 확인    | `purchaseRepository.existsByUserIdAndBoardId()` 호출         |
| **4. 잔액 검증**    | 보유 포인트가 충분한지 확인      | `userEntity.deductPoint(PREMIUM_BOARD_PRICE)` 내에서 차감 로직 수행 |
| **5. 원자성 보장**   | 구매 이력 저장 및 포인트 업데이트  | `@Transactional`을 통해 전체 프로세스를 하나의 작업 단위로 묶음                |

-----

### 6\. 🛠 DB 스키마 설계 (Entity Relationship)

다대다(M:N) 관계를 해소하기 위해 중간 테이블인 `Purchase` 엔티티를 설계하고 **복합 제약 조건**을 적용했습니다.

  * **Purchase Entity**: `User`와 `Board` 사이의 매핑 테이블로, 특정 사용자가 특정 게시글을 구매했음을 기록합니다.
  * **Unique Constraint**: `uk_user_board` 설정을 통해 DB 수준에서 동일 사용자의 동일 게시글 중복 구매를 원천 차단했습니다.
  * **Payment Entity**: 포인트 충전 내역을 기록하며, 포트원 고유 식별자인 `paymentId`를 유니크 컬럼으로 관리하여 결제 데이터의 신뢰성을 확보했습니다.

-----

### 7\. 🔒 보안 및 편의 기능 (Security & Utils)

  * **세션 동기화 처리**: 결제나 구매로 인해 포인트 변동이 발생할 경우, DB 반영과 동시에 `HttpSession` 정보를 갱신하여 클라이언트가 즉각적으로 변화된 포인트를 인지하도록 구현했습니다.
  * **이메일 인증 도장 시스템**: `MailService`에서 발급한 인증번호가 확인되면 세션에 `verified_email`이라는 "인증 도장"을 저장합니다. 이후 회원가입 로직에서 이 도장을 대조하여 이메일 위변조를 방지합니다.
  * **날짜 포맷 유틸리티**: `MyDateUtil`을 통해 `Timestamp` 객체를 `yyyy-MM-dd HH:mm` 형식의 가독성 좋은 문자열로 일관되게 변환하여 뷰 레이어의 복잡도를 낮추었습니다.

-----


