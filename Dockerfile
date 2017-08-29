FROM maven:alpine
COPY ./xds-on-fhir /app
WORKDIR /app
RUN mvn clean install
CMD sh -c "java -jar target/xds-on-fhir-*.jar"
EXPOSE 8000
