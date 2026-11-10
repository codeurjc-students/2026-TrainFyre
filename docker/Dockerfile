ARG BUILD_IMAGE=maven:3.10-eclipse-temurin-27
ARG RUNTIME_IMAGE=eclipse-temurin:27-jre

FROM ${BUILD_IMAGE} AS build

WORKDIR /build

ARG AOT_PROFILES=dev

COPY pom.xml .
COPY .mvn/ .mvn/

RUN --mount=type=bind,source=src/main,target=/build/src/main,readonly \
    --mount=type=cache,id=trainfyre-maven,target=/root/.m2,sharing=locked \
    SPRING_PROFILES_ACTIVE="${AOT_PROFILES}" \
    mvn -B -ntp \
        -Pnative \
        -Dmaven.test.skip=true \
        -Djacoco.skip=true \
        package \
    && test -d target/spring-aot/main/classes \
    && cp target/TrainFyre-0.0.1-SNAPSHOT.jar application.jar \
    && java -Djarmode=tools \
        -jar application.jar \
        extract --layers --destination /layers \
    && test -s /layers/application/application.jar \
    && chmod -R a=rX /layers

FROM ${RUNTIME_IMAGE} AS runtime

WORKDIR /app

COPY --from=build --chown=0:0 /layers/dependencies/ ./
COPY --from=build --chown=0:0 /layers/spring-boot-loader/ ./
COPY --from=build --chown=0:0 /layers/snapshot-dependencies/ ./
COPY --from=build --chown=0:0 /layers/application/ ./

USER 10001:10001

EXPOSE 443

ENTRYPOINT ["java", "-Dspring.aot.enabled=true", "-jar", "/app/application.jar"]
