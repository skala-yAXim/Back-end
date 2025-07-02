FROM postgres:15

# 빌드 시점 변수 받기
ARG POSTGRES_DB
ARG POSTGRES_USER  
ARG POSTGRES_PASSWORD

# 런타임 환경변수로 등록
ENV POSTGRES_DB=${POSTGRES_DB}
ENV POSTGRES_USER=${POSTGRES_USER}
ENV POSTGRES_PASSWORD=${POSTGRES_PASSWORD}

# PostgreSQL 설정 파일 복사 (선택사항)
# COPY postgresql.conf /etc/postgresql/postgresql.conf
# COPY pg_hba.conf /etc/postgresql/pg_hba.conf

# 초기화 스크립트 복사 (선택사항)
# COPY init.sql /docker-entrypoint-initdb.d/

# 데이터 디렉토리 볼륨 설정
VOLUME ["/var/lib/postgresql/data"]

# PostgreSQL 포트 노출
EXPOSE 5432

# 컨테이너 시작 시 실행할 명령어 (기본값 사용)
# CMD ["postgres"]
