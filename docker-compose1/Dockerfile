#java环境
FROM openjdk:8-alpine
ADD application.jar /application/
WORKDIR /application
#CMD ["-jar","/application/application.jar"]
ENTRYPOINT ["java","-jar","/application/application.jar","&"]
#VOLUME ["/application"]
EXPOSE 8080