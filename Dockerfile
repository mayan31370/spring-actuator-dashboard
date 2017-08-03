FROM registry.cn-hangzhou.aliyuncs.com/counect_data/openjdk-8-jre-alpine-fixed-timezone
EXPOSE 8080
ENV ACTUATOR_USERNAME username
ENV ACTUATOR_PASSWORD password
ENV ACTUATOR_APPS Group1:App1,App2|Group2:App3
ENTRYPOINT ["java","-jar","-Dserver.port=8080","/app.jar"]
ADD target/*.jar app.jar