FROM images.htyoudao.com/library/jdk-17:jammy-no-monitoring-v3
LABEL maintainer="HanGZ<hangz80@126.com>"
ENV TZ=Asia/Shanghai \
    LANG=C.UTF-8
ARG APP_NAME
ENV APP_NAME $APP_NAME
WORKDIR /app
COPY ${APP_NAME}/target/${APP_NAME}.jar /app/${APP_NAME}.jar
EXPOSE 8080