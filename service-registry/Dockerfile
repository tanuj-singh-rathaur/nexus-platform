FROM  amazoncorretto:21-alpine

RUN apk add --no-cache curl

WORKDIR /app

COPY target/service-registry-0.0.1-SNAPSHOT.jar app.jar

CMD [ "java", "-jar", "app.jar" ]
