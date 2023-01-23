FROM openjdk:11-jdk

ARG JAR_FILE=build/libs/data-server-0.3.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

CMD java -jar /app.jar