# SwiftGoal (Formerly JAVA FINAL)

This is the final project for my sophomore year Java programming fundamentals course. It is an intelligent sports news aggregation and analysis platform.

## Quick Start

**Environment:**
*   Java 17
*   Maven 3.x
*   A running SQL database (MySQL/PostgreSQL)

### Configuration

1.  **Database Setup**: Create a new database in your SQL server (e.g., `swiftgoal_db`).
2.  **Application Properties**: Open `src/main/resources/application.properties` and configure the following:
    *   `spring.datasource.url`: Update the URL to point to your database.
    *   `spring.datasource.username` and `spring.datasource.password`: Enter your database credentials.
    *   `app.deepseek.api-key`: Provide your valid DeepSeek API key.

**Example `application.properties`:**
```properties
# For MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/swiftgoal_db?createDatabaseIfNotExist=true
# For PostgreSQL
# spring.datasource.url=jdbc:postgresql://localhost:5432/swiftgoal_db

spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

# DeepSeek API Key
app.deepseek.api-key=YOUR_API_KEY
```

### Running the Application

1.  **Build the project** using Maven in the root directory:
    ```bash
    mvn clean package
    ```

2.  **Run the application**:
    ```bash
    java -jar target/ai-0.0.1-SNAPSHOT.jar
    ```
    Alternatively, you can run the `main` method in `com.sportslens.ai.SportsLensAiApplication` directly from your IDE.

Once the application starts, you can access it at `http://localhost:8080`.

