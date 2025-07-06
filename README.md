# Connect-Platform
Connect Platform은 회원 정보 관리, 포인트 기반 결제 기능, 전문가 매칭 등의 서비스를 제공하는 마이크로서비스 아키텍처 기반의 웹 플랫폼입니다.
## 주요 기술
- 언어 : Java 17
- 프레임워크 : Spring Framework (Spring Boot)
- 빌드 도구 : Gradle
- 배포 및 운영 :
    - 서버 : AWS EC2, Docker
    - CI/CD : GitHub Actions
    - ORM & DB :  Spring Data JPA, QueryDSL, MySQL
    - 캐시 : Redis
- 테스트 : JUnit5, Mockito
- API 문서화 : Springdoc OpenAPI (Swagger UI)

# 기술적 의사결정
- **Docker**
	- 이미지 파일 기반으로 개발 환경 통합, 경량화, 배포 프로세스 단축
- **Spring Data JPA**
	- 반복적인 CRUD 코드 제거로 생산성 향상
	- 객체 중심의 DB 접근 방식
- **QueryDSL**
	- 타입 안정성이 높은 동적 쿼리 기능
	- 쿼리 파라미터 바인딩 방식으로 SQL Injection 방지
- **Redis 기반 카운터 캐시**
	- 조회수는 읽기보다 쓰기 트래픽이 집중적으로 발생하는 케이스로, Lock 경쟁, IOPS 증가 등의 문제가 발생
	- 실시간으로 DB에 반영하는 대신, Redis에서 키 기반으로 카운트를 증가시켜 배치 또는 스케줄러를 통해 DB에 반영
		- 이를 통해 DB 부하를 줄이면서도 데이터 일관성을 일정 수준 보장
- **GitHub Actions**
	- 빌드, 테스트, 배포 프로세스를 자동화
	- GitHub과의 통합과 서버 운영 부담이 적은 선택
- **Swagger**
	- 클라이언트 및 팀 내 협업 시 빠른 API 이해를 위해 자동 문서화

# 시스템 아키텍처
![Pasted image 20250706134055](https://github.com/user-attachments/assets/b3ae2a98-a79d-4ca1-961a-09ec5c90a1b5)

# ERD
![Pasted image 20250706134920](https://github.com/user-attachments/assets/47fe979f-1262-46c4-a557-7703c1be1911)

# 결제 흐름
**Toss 결제 승인 API**
- 프론트엔드에서 Toss 결제 요청
- 결제 성공 후 받은 `paymentKey`, `orderId`, `amount`를 서버에 전송
- `payment-service`에서 Toss로 결제 승인 요청 → 성공 시 포인트 적립

**KakaoPay 결제 승인 API**
- 프론트엔드에서 KakaoPay 결제 요청
- 결제 성공 후 받은 `cid`, `tid`, `partnerOrderId`, `partenerUserId`, `pgToken`를 서버에 전송
- `payment-service`에서 KakaoPay로 결제 승인 요청 → 성공 시 포인트 적립

# 실행 방법

**Root Dir에서 아래의 명령어 실행**

로컬 빌드
- `./gradlew clean build`

Docker Desktop 전체 서비스 실행
- `docker-compose up --build`

Swagger-UI 확인
- Member-Serivce : http://localhost:8081/swagger-ui/index.html
- Payment-Serivce : http://localhost:8082/swagger-ui/index.html

# 유의 사항
- 결제 승인 API는 `payment-service/src/main/resources/application.yml` 의 Toss Secret Key, Kakao Admin Key 설정이 필요합니다.
