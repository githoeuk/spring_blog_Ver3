
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


