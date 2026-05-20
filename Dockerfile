ARG MARCH=x86-64-v2

FROM debian:13 AS builder

ENV MANDREL_VERSION=25.0.3.0-Final
ENV MAVEN_VERSION=3.9.15

ENV JAVA_HOME=/opt/mandrel
ENV MAVEN_HOME=/opt/maven
ENV PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH
ENV MAVEN_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl ca-certificates gcc zlib1g-dev binutils unzip && \
    rm -rf /var/lib/apt/lists/* && \
    curl -sL --retry 5 --retry-delay 15 \
      -o /tmp/mandrel.tar.gz \
      "https://github.com/graalvm/mandrel/releases/download/mandrel-${MANDREL_VERSION}/mandrel-java25-linux-amd64-${MANDREL_VERSION}.tar.gz" && \
    tar -xzf /tmp/mandrel.tar.gz -C /opt && \
    mv /opt/mandrel-* /opt/mandrel && \
    rm /tmp/mandrel.tar.gz && \
    curl -sL --retry 3 -o /tmp/maven.zip \
      "https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.zip" && \
    unzip -q /tmp/maven.zip -d /opt && \
    mv /opt/apache-maven-* /opt/maven && \
    rm /tmp/maven.zip

COPY . /src
WORKDIR /src
RUN mvn package -B -Dnative -DskipTests \
    -Dquarkus.native.additional-build-args="-march=${MARCH}" \
    -Dquarkus.native.native-image-xmx=4g

FROM gcr.io/distroless/base-debian12
WORKDIR /app
COPY --from=builder /src/target/*-runner /app/application
EXPOSE 8080
USER nonroot
ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
