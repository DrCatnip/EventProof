# EventProof

Event Reliability Management System — a college Advanced OOP Java desktop project.
This first stage provides a working operations dashboard and the architecture for
incremental development. Every dashboard value is static demo data.

## What works

- Dark AtlantaFX PrimerDark theme with EventProof CSS and Feather icons.
- FXML main window, selected sidebar navigation, dashboard and module placeholders.
- Reliability preview (86/100, low risk), statistics, upcoming events and factor bars.
- Restrained dashboard entrance and page fade animations; navigation hover feedback.
- Independent JDBC connection infrastructure and an opt-in connection diagnostic.
- GUI starts without MySQL or database credentials.

Events, Venues, Incidents, Reliability, Simulation and Settings currently show
placeholders. Header notification/settings controls are reserved for future work.
No CRUD, schema migrations, reliability calculation, simulation or login is implemented.

## Stack and prerequisites

Install JDK **25**, Maven **3.9+**, VS Code with **Extension Pack for Java**, and
Scene Builder (a current version supporting your FXML runtime). MySQL 8.0+ is only
needed for the optional database check.

Pinned dependencies:

| Dependency | Version |
| --- | --- |
| JavaFX controls / FXML | 27 |
| AtlantaFX base | 3.0.0 |
| Ikonli JavaFX / Feather pack | 12.4.0 |
| MySQL Connector/J | 9.6.0 |
| JUnit Jupiter (tests only) | 5.12.2 |

JavaFX 27 requires JDK 25 or later ([official release notes](https://docs.oracle.com/en/java/java-components/javafx/27/release-notes/)).
AtlantaFX 3.0.0 targets Java 25 and JavaFX 27 ([release notes](https://github.com/mkpaz/atlantafx/releases/tag/v3.0.0)).
Maven resolves the JavaFX platform native libraries; a separate JavaFX SDK is
unnecessary for running this project.

Build plugins: compiler 3.14.0, Surefire 3.5.3, JavaFX 0.0.8, exec 3.5.0.
The project uses the classpath (no module-info.java) to keep FXML and Scene Builder
integration straightforward. Maven's JavaFX launcher manages the JavaFX module path.

## Run in VS Code

1. Open this folder in VS Code; allow Maven to import the project.
2. Select JDK 25 with **Java: Configure Java Runtime** and ensure JAVA_HOME points
   to the same JDK used by Maven.
3. In the integrated PowerShell terminal:

   ```powershell
   java -version
   mvn -version
   mvn clean compile
   mvn javafx:run
   ```

The application opens at 1280 × 850 with a minimum size of 1060 × 740. The dashboard
scrolls vertically on smaller windows. Close its window to stop the process.

Maven downloads dependencies into the ignored `.maven-repository/` directory
configured in `.mvn/maven.config`. This keeps builds working in a restricted
workspace. Remove that configuration to use your normal user Maven cache.
Initial builds require internet access.

## MySQL setup and credentials

Create only the empty database for now in MySQL Workbench or the MySQL CLI:

```sql
CREATE DATABASE IF NOT EXISTS eventproof
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Use your local MySQL account with access to this database. In the same PowerShell
terminal where you run the connection check, set these variables:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/eventproof'
$env:DB_USER = 'your_local_mysql_user'
$env:DB_PASSWORD = 'your_local_mysql_password'
mvn exec:java "-Dexec.mainClass=com.eventproof.database.DatabaseConnectionCheck"
```

Expected success output: `MySQL connection verified. Database: eventproof`.
The diagnostic exits with a Maven error on missing configuration, rejected
credentials, unreachable server or failed validation. Connections use five-second
connect/socket timeouts and are closed using try-with-resources. Each future DAO
should similarly close every connection, statement and result set.

Environment variables are read by `DatabaseConnection`; a `.env` file is **not**
automatically loaded. Do not commit real credentials. No tables or DAOs are needed
at this stage, and controllers contain no database access.

## Scene Builder

Open `src/main/resources/com/eventproof/fxml/Main.fxml` or `Dashboard.fxml`.
All layouts use standard JavaFX controls plus Ikonli FontIcon.
In **Library → JAR/FXML Manager**, import these downloaded jars:

- `.maven-repository/org/kordamp/ikonli/ikonli-javafx/12.4.0/ikonli-javafx-12.4.0.jar`
- `.maven-repository/org/kordamp/ikonli/ikonli-core/12.4.0/ikonli-core-12.4.0.jar`
- `.maven-repository/org/kordamp/ikonli/ikonli-feather-pack/12.4.0/ikonli-feather-pack-12.4.0.jar`

For the closest preview, load the three CSS files in the preview stylesheet
settings in order: main.css, components.css, dashboard.css. The app applies the
AtlantaFX base theme at launch; Scene Builder previews may differ unless its
AtlantaFX theme support is configured. CSS palette values are defined on
`.app-root` in main.css; preview Dashboard.fxml within Main.fxml for these values.
No custom controller factory or custom layout component is required.

## Complete source structure

```text
EventProof/
├── .gitignore
├── .mvn/maven.config
├── .vscode/extensions.json
├── pom.xml
├── README.md
├── src/main/java/com/eventproof/
│   ├── Main.java
│   ├── controller/
│   │   ├── MainController.java
│   │   ├── DashboardController.java
│   │   └── PlaceholderController.java
│   ├── database/
│   │   ├── DatabaseConnection.java
│   │   └── DatabaseConnectionCheck.java
│   ├── util/Resources.java
│   ├── model/package-info.java
│   ├── service/package-info.java
│   ├── dao/package-info.java
│   ├── exception/package-info.java
│   └── strategy/package-info.java
├── src/main/resources/com/eventproof/
│   ├── fxml/
│   │   ├── Main.fxml
│   │   ├── Dashboard.fxml
│   │   └── Placeholder.fxml
│   ├── css/
│   │   ├── main.css
│   │   ├── components.css
│   │   └── dashboard.css
│   └── images/.gitkeep
└── src/test/java/com/eventproof/FoundationTest.java
```

Generated `target/` and `.maven-repository/` folders are excluded from Git.
The empty architecture packages document intended responsibilities without
introducing speculative implementations.

## Verification

```powershell
mvn test
mvn javafx:run
```

The GUI smoke test starts JavaFX, loads FXML and CSS, shows and closes a real
window, visits every navigation item and checks repeat selections and return to
Dashboard. It requires an interactive graphical desktop. The database
configuration test checks the helpful missing-URL error; it does not connect to
MySQL. It also saves a rendered preview to `target/dashboard-preview.png`.
The test runner may print JavaFX's unnamed-module configuration warning because
tests use the classpath; the application launcher uses the JavaFX module path.
Run the opt-in diagnostic above to validate your local server separately.

## Incremental roadmap

1. Foundation and dashboard skeleton (this stage).
2. Event model, validation, MySQL events table and JDBC EventDAO; replace the
   Events placeholder with a small create/list workflow.
3. Venue model and event-to-venue assignment.
4. Incident recording and resolution.
5. Reliability service with interchangeable strategy implementations.
6. Failure simulation and live visual score updates.
7. Final validation, presentation polish and packaging.

Planned features include event/venue management, incident tracking, reliability
scoring, risk classification and power/internet/equipment failure simulation.
Add each stage independently so the application remains runnable throughout.
