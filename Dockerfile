FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/cryptotracker.jar /cryptotracker/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/cryptotracker/app.jar"]
