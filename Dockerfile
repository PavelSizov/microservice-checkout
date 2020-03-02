FROM openjdk:8-jre-alpine
EXPOSE 8081
COPY /target/checkout-0.0.1-SNAPSHOT.jar checkout.jar
ENTRYPOINT ["java", "-jar", "checkout.jar"]