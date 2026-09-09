# Campus Book Market

대학생 전공책·학습용품 거래를 위한 Spring MVC 학습 프로젝트입니다. 게시글 CRUD에서 시작해 회원 로그인, 작성자 권한, 검색·페이징, 이미지 업로드, 구매 신청·예약·완료와 거래 내역까지 확장합니다.

## 기술

- Java 17, Gradle Wrapper, Spring Boot 4.1
- Spring MVC, Thymeleaf, Bean Validation, 한국어/영어 메시지
- Spring Data JPA, Querydsl, H2, Flyway
- 세션 로그인, 비밀번호 해시, 인터셉터 기반 접근 제어
- JUnit, Mockito, MockMvc 및 실제 H2/JPA 통합 테스트

## 실행

JDK 17을 설치하고 프로젝트 최상위 폴더에서 실행합니다. IDE의 Gradle 프로젝트도 같은 폴더를 엽니다.

```powershell
.\gradlew.bat clean test
.\gradlew.bat bootRun
```

브라우저에서 `http://localhost:8080`으로 접속합니다. 최초 실행 시 Flyway가 V1~V7을 적용하고 Hibernate가 매핑을 검증합니다. 기존 DB를 사용하면 아직 적용되지 않은 마이그레이션만 실행합니다. 이미 적용한 SQL 파일은 수정하지 않습니다.

- DB: `./data/campus-book-market`
- 이미지: `./uploads` (`UPLOAD_DIR` 환경 변수로 변경 가능)
- 회원가입 후 로그인해야 게시글 등록과 거래 신청이 가능합니다.
- `data`, `uploads`, 환경 변수 파일은 Git에 포함하지 않습니다. DB 변경 전에는 앱을 중지하고 별도로 백업하세요.

## 거래 기능

구매자가 신청하면 판매자가 한 명을 예약 확정하고, 거래가 끝나면 완료 처리합니다. 구매자와 판매자는 상단의 **내 거래**에서 진행 상태와 완료 기록을 확인합니다. 동일 게시글의 동시 예약은 DB 행 잠금으로 직렬화합니다.

새 파일별 역할, 요청 흐름, 연관관계, 트랜잭션, 상태 전환, 수정·삭제 정책과 테스트 방법은 [거래 흐름 설명](docs/trade-workflow.md)을 참고하세요.

## 학습과 운영의 구분

현재 설정은 **로컬 학습용**입니다. H2 콘솔이 켜져 있고 앱 전체에 Spring Security를 적용한 것은 아닙니다. 거래 변경 URL에는 CSRF 토큰 검사를 추가했습니다. 실서비스 배포 전에는 운영 DB, HTTPS, 전체 보안 정책, 백업, 파일·DB 실패 복구와 모니터링을 별도로 설계해야 합니다.

JDBC 저장소는 기술 비교용으로 남겨 두었으며 애플리케이션의 기본 게시글 저장소는 JPA 구현체입니다. 저장소가 공개되어 있어도 실행 중인 웹 서비스가 자동으로 배포되는 것은 아닙니다.
