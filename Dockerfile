FROM openjdk:17-jdk-slim

ENV MAVEN_VERSION=3.3.9
#ENV APP_HOME=/usr/app
ENV MAVEN_HOME=/opt/apache-maven-$MAVEN_VERSION
ENV PATH=$MAVEN_HOME/bin:$PATH

RUN echo "Replace Maven installation..."
RUN rm -Rf /opt/maven 
RUN wget -q https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.tar.gz
RUN tar xvzf apache-maven-$MAVEN_VERSION-bin.tar.gz -C /opt
RUN rm apache-maven-$MAVEN_VERSION-bin.tar.gz

EXPOSE 8080
ADD target/maybeach-integration.jar app.jar
#ADD map /opt/map

ENTRYPOINT ["java","-jar","/app.jar"]
