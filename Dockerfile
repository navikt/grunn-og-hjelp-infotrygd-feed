FROM gcr.io/distroless/java25-debian13:nonroot
ENV TZ="Europe/Oslo"
COPY target/grunn-og-hjelp-infotrygd-feed.jar /app/app.jar
ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75"
CMD ["-jar", "/app/app.jar"]
