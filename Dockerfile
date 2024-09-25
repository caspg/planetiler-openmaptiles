FROM eclipse-temurin:21.0.1_12-jdk

# Install git and clean up apt cache to reduce image size
RUN apt-get update && \
  apt-get install -y git && \
  rm -rf /var/lib/apt/lists/*

ARG JAVA_TOOL_OPTIONS "-Xmx1g"
ENV JAVA_TOOL_OPTIONS $JAVA_TOOL_OPTIONS

WORKDIR /planetiler

COPY . .

RUN ./mvnw clean package -Dmaven.test.skip
RUN cp ./target/*with-deps.jar ./planetiler.jar

ENTRYPOINT ["java", "-jar", "planetiler.jar"]
