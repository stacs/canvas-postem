FROM amazoncorretto:25-al2023-headless

ENV LANG=en_US.UTF-8 \
    LANGUAGE=en_US:en \
    LC_ALL=en_US.UTF-8

RUN dnf upgrade -y \
 && dnf install -y shadow-utils glibc-langpack-en fontconfig dejavu-sans-fonts \
 && dnf clean all && rm -rf /var/cache/dnf

# Create an unprivileged user (don't run as root)
RUN groupadd --system app && useradd --system --gid app --no-create-home app

WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY --chown=app:app ${JAR_FILE} app.jar

USER app
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
