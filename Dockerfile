FROM hirokimatsumoto/alpine-openjdk-11

MAINTAINER PS

ADD build/libs/*.jar app.jar

#ENTRYPOINT ["java","-jar","/app.jar"]
CMD ["java","-jar","/app.jar", "--spring.profiles.active=prod"]