FROM mayan31370/openjdk-alpine-with-chinese-timezone:8-jre
EXPOSE 8080
ENV SERVER_USERNAME user
ENV SERVER_PASSWORD password
ENTRYPOINT ["java","-XX:+UnlockExperimentalVMOptions","-XX:+UseCGroupMemoryLimitForHeap","-jar","-Dserver.port=8080","/app.jar"]
ADD target/*.jar app.jar
