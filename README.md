# Campus Book Market

대학생 전공책·학습용품 중고거래 서비스입니다. 게시글 CRUD부터 회원 인증, 이미지 업로드, 거래 신청·예약·완료까지 구현한 Spring Boot 학습 프로젝트입니다.

## 기술 스택

- Java 17, Spring Boot 4.1, Gradle
- Spring MVC, Thymeleaf, Bean Validation
- Spring Data JPA, Querydsl, H2, Flyway
- JUnit, Mockito, MockMvc

## 주요 기능

| 영역 | 기능 |
|---|---|
| 회원·권한 | 회원가입, 비밀번호 해시, 세션 로그인·로그아웃, 작성자·거래 참여자 권한 검사 |
| 게시글 | 등록·조회·수정·삭제, 거래 상태 표시 |
| 검색 | 제목·판매자명 검색, 상태 필터, 최신순·가격순 정렬, 페이징 |
| 이미지 | 업로드·조회·교체·삭제, 파일 검증, 최대 5MB |
| 거래 | 신청·거절·예약·취소·완료, 개인 거래 내역 |
| 공통 | 한국어·영어 메시지, 입력 검증, 오류 화면 |

구매자가 거래를 신청하면 판매자가 예약을 확정하고 완료 처리합니다. 동일 게시글의 중복 예약은 DB 행 잠금으로 제어하고, 상태 변경은 트랜잭션으로 묶습니다. 자세한 정책은 [거래 흐름 설명](docs/trade-workflow.md)을 참고하세요.

## 실행 방법

JDK 17을 설치하고 프로젝트 루트에서 실행합니다.

```powershell
.\gradlew.bat bootRun
```

접속 주소: [http://localhost:8080](http://localhost:8080)

- 실행 시 Flyway가 DB 스키마를 생성·갱신합니다.
- DB: `./data/campus-book-market`
- 이미지: `./uploads` (`UPLOAD_DIR` 환경 변수로 변경 가능)
- 회원가입·로그인 후 게시글 등록과 거래 신청이 가능합니다.
- DB·업로드 파일은 Git에 포함하지 않으며, 이미 적용한 마이그레이션은 수정하지 않습니다.

## 프로젝트 구조

```text
src/main/java/com/skc04/campusbookmarket/
  config/     # MVC, Querydsl, 비밀번호 인코더 설정
  member/     # 회원·인증
  post/       # 게시글·검색·페이징
  trade/      # 거래 신청·상태 전환·업무 규칙
  file/       # 이미지 검증·저장
  web/        # 컨트롤러, 입력 폼, 인터셉터, 예외 처리
src/main/resources/
  db/migration/   # Flyway 마이그레이션
  templates/      # Thymeleaf 화면
  static/         # CSS 등 정적 자원
src/test/java/    # 단위·통합 테스트
```

기본 흐름은 `Controller -> Service -> Repository -> DB`이며, 결과는 Thymeleaf로 렌더링합니다. 기본 저장소는 JPA이고, JDBC 구현체는 학습·비교용으로 남겨 두었습니다.

## 테스트

```powershell
.\gradlew.bat test
```

서비스·저장소·MVC·이미지 처리와 거래 권한·롤백·동시 요청을 검증합니다. 결과는 `build/reports/tests/test/index.html`에서 확인할 수 있습니다.

## 참고

로컬 학습용 설정으로 H2 콘솔이 활성화되어 있습니다. CSRF 보호는 거래 변경 요청에 한정되며, 실서비스 배포 전에는 전체 보안 정책·운영 DB·백업 등을 보강해야 합니다. 실제 결제 기능은 포함하지 않습니다.
