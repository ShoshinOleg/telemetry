FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle :buildFatJar --no-daemon
RUN ls /home/gradle/src
#RUN gradle build

FROM openjdk:11
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/ktor-docker-sample.jar
COPY --from=build /home/gradle/src/build/otel/*.jar /app/opentelemetry-javaagent.jar
#ENTRYPOINT ["java","-jar","/app/ktor-docker-sample.jar"]
ENTRYPOINT java -jar -javaagent:/app/opentelemetry-javaagent.jar /app/ktor-docker-sample.jar