FROM openjdk:21-jdk-oracle

ARG JAR_FILE=build/libs/data-server-0.4-plain.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

CMD java -jar /app.jar