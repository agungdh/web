ARG MARCH=x86-64-v2

FROM debian:13-slim AS builder

ENV MANDREL_VERSION=25.0.3.0-Final
ENV JAVA_HOME=/opt/mandrel
ENV PATH=$JAVA_HOME/bin:$PATH

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl gcc zlib1g-dev && \
    rm -rf /var/lib/apt/lists/* && \
    curl -sL "https://github.com/graalvm/mandrel/releases/download/mandrel-${MANDREL_VERSION}/mandrel-java25-linux-amd64-${MANDREL_VERSION}.tar.gz" | \
    tar -xzf - -C /opt && \
    mv /opt/mandrel-java25-* /opt/mandrel

COPY . /src
WORKDIR /src
RUN ./mvnw package -Dnative -DskipTests \
    -Dquarkus.native.additional-build-args="-march=${MARCH}"

FROM gcr.io/distroless/base-debian13
WORKDIR /app
COPY --from=builder /src/target/*-runner /app/application
EXPOSE 8080
USER nonroot
ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
