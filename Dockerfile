## Stage 1: Build native binary dengan Mandrel
FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-25 AS build

WORKDIR /build

# Cache dependencies dulu sebelum copy source
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B -q

# Copy source dan build native
COPY src ./src
ARG NATIVE_IMAGE_EXTRA_ARGS
RUN ./mvnw package -Pnative -DskipTests -B \
    -Dquarkus.native.native-image-xmx=6g \
    -Dquarkus.native.additional-build-args="${NATIVE_IMAGE_EXTRA_ARGS}"

## Stage 2: Runtime dengan distroless Debian 13
FROM gcr.io/distroless/base-debian13:nonroot

WORKDIR /app

COPY --from=build /build/target/*-runner /app/application

EXPOSE 8080

ENTRYPOINT ["/app/application", "-Dquarkus.http.host=0.0.0.0"]