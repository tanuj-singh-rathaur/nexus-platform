FROM  amazoncorretto:21-alpine

WORKDIR /app

COPY target/identity-service-0.0.1-SNAPSHOT.jar app.jar

CMD [ "java", "-jar", "app.jar" ]