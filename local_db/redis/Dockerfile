FROM redis:7.2-alpine

# Redis 포트 노출
EXPOSE 6379

# 데이터 디렉토리 볼륨 설정
VOLUME ["/data"]

# Redis 서버를 AOF(Append Only File) 모드로 실행
CMD ["redis-server", "--appendonly", "yes"]