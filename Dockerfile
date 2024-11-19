FROM openjdk:21
WORKDIR target/
COPY target/ancient-bowling-api-1.0.0-SNAPSHOT.jar ancient-bowling-api.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ancient-bowling-api.jar"]