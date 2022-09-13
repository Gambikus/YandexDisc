FROM openjdk:latest
EXPOSE 80
COPY target/newDisc-0.0.1-SNAPSHOT.jar /
CMD ["java","-jar","newDisc-0.0.1-SNAPSHOT.jar"]