#java环境
FROM jdk8
ADD application.jar /application/
WORKDIR /application
CMD ["java","-jar application.jar"]
ENTRYPOINT ["java","-version"]
VOLUME ["/application/log"]