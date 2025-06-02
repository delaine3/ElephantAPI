FROM public.ecr.aws/amazoncorretto/amazoncorretto:21-al2-jdk
WORKDIR /app
COPY build/libs/Demo-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
