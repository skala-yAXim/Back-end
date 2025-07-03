# 베이스 이미지로 OpenJDK 17 버전의 JRE 이미지 사용
FROM openjdk:17-jdk-slim
WORKDIR /app

# 외부에서 컨테이너의 8088 포트에 접근할 수 있도록 설정
EXPOSE 8088

# 애플리케이션의 jar 파일을 컨테이너에 추가
COPY ./yaxim/build/libs/yaxim-0.0.1-SNAPSHOT.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
