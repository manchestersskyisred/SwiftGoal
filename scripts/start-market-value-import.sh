#!/bin/bash

# Navigate to the project root directory
cd "$(dirname "$0")/.."

echo "Building the project with Maven..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "Maven build failed. Aborting."
    exit 1
fi

echo "Starting the Market Value Import process..."
java -Dspring.profiles.active=market-value-import -jar target/*.jar 