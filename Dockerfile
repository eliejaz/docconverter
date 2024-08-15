# Stage 1: Build the application
FROM jelastic/maven:3.9.5-openjdk-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests
# Change the working directory and extract JAR layers
WORKDIR /opt/app
# Split fat jar
RUN java -Djarmode=layertools -jar /build/target/*.jar extract

# Stage 2: Run the application
FROM ubuntu:20.04
ENV DEBIAN_FRONTEND=noninteractive
# Install Java, Python, and required packages
RUN apt-get update && \
    apt-get install -y openjdk-21-jdk python3 python3-pip && \
    python3 -m pip install --upgrade pip && \
    pip3 install pdf2docx && \
    ln -s /usr/bin/python3 /usr/bin/python
	
ENV PATH="/usr/bin:${PATH}"

# Create a user and group for the application
RUN groupadd -r javauser && useradd --no-log-init -r -g javauser javauser
USER javauser

# Copy extracted JAR layers from the builder stage
WORKDIR /opt/app
COPY --from=builder --chown=javauser:javauser /opt/app/dependencies/ ./
RUN true
COPY --from=builder --chown=javauser:javauser /opt/app/snapshot-dependencies/ ./
RUN true
COPY --from=builder --chown=javauser:javauser /opt/app/spring-boot-loader/ ./
RUN true
COPY --from=builder --chown=javauser:javauser /opt/app/application/ ./
RUN true

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]