ARG MARCH=x86-64-v2

FROM debian:13-slim AS builder

ENV HOME=/root
ENV SDKMAN_DIR=/root/.sdkman

RUN apt-get update && apt-get install -y --no-install-recommends \
    bash curl zip unzip gcc zlib1g-dev && \
    rm -rf /var/lib/apt/lists/* && \
    curl -s "https://get.sdkman.io" | bash && \
    bash -c "source $SDKMAN_DIR/bin/sdkman-init.sh && sdk install java 25.r25-mandrel"

ENV JAVA_HOME=$SDKMAN_DIR/candidates/java/current
ENV PATH=$JAVA_HOME/bin:$PATH

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
