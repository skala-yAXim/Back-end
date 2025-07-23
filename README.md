# 개인 업무 관리 AI 서비스 - Backend API
Spring Boot 기반의 개인 업무 관리 AI 서비스 백엔드 API 서버입니다. Microsoft 365 연동, JWT 인증, 파일 업로드, 실시간 데이터 처리를 지원합니다.

## 🛠️ 기술 스택
- Runtime: Java 17
- Framework: Spring Boot 3.5.0
- Database: PostgreSQL + Redis
- Authentication: JWT + OAuth2 + Microsoft Azure AD
- File Storage: AWS S3
- Documentation: Swagger (OpenAPI 3.0)
- Query: Spring Data JPA + QueryDSL
- Build Tool: Gradle

## ⚙️ 서비스 아키텍처

<img width="865" height="483" alt="image" src="https://github.com/user-attachments/assets/cadabe2e-c2b1-4efb-9db3-ec502a065d90" />

## 🚀 빠른 시작
### 사전 요구사항
- Java 17 이상
- PostgreSQL 14+
- Redis 6+
- AWS S3 버킷
- Microsoft Azure AD 앱 등록

### 로컬 실행
```sh
# 저장소 클론
git clone [repository-url]
cd Back-end/yaxim/src/main/resources

# 환경변수 설정
cp application.yml.example application-dev.yml
# application.yml 파일 수정

# 데이터베이스 설정
# PostgreSQL과 Redis 실행 확인

# 프로젝트 빌드 및 실행
./gradlew bootRun
```

## 📡 API 문서
### Swagger UI
서버 실행 후 http://localhost:8088/swagger-ui/index.html 접속

## 🔐 인증 및 보안
### JWT 인증 플로우
1. 사용자 로그인 (/oauth2/authorization/azure)
2. JWT 토큰 발급 (24시간 유효)
3. 쿠키 발급 (자동으로 헤더에 토큰 포함) 
4. 토큰 만료 시 /auth/reissue로 갱신

### Microsoft 365 연동
- Azure AD 앱 등록 필요
- OAuth2 Authorization Code Flow 사용
- Microsoft Graph API 연동으로 Teams, OneDrive 데이터 접근

## 도커 이미지 빌드 및 k8s 배포
```sh
./docker-build&push.sh # 도커 이미지 빌드 및 배포

cd k8s/
kubectl -f apply deploy.yaml ingress.yaml service.yaml
```

## 📚 프로젝트 구조
```sh
src/
├── main/
│   ├── java/com/workspace/
│   │   ├── config/          # 설정 클래스
│   │   ├── controller/      # REST 컨트롤러
│   │   ├── service/         # 비즈니스 로직
│   │   ├── repository/      # 데이터 액세스
│   │   ├── entity/          # JPA 엔티티
│   │   ├── dto/             # 데이터 전송 객체
│   │   ├── security/        # 보안 설정
│   │   └── exception/       # 예외 처리
│   └── resources/
│       ├── application.yml  # 설정 파일
│       └── db/migration/    # Flyway 마이그레이션
└── test/
    └── java/com/workspace/  # 테스트 코드
```
