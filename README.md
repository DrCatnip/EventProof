# EventProof

Event Reliability Management System — a college Advanced OOP Java desktop project.
Stages 1 and 2 provide a working operations dashboard and an Advanced OOP core:
validated models, JDBC CRUD, service boundaries and category-specific reliability
strategies. Every GUI dashboard value remains static demo data.

See [Stage 2 / Review 2 guide](docs/STAGE2.md) for the rules, OOP examples,
architecture diagrams and database verification steps.

## What works

- Dark AtlantaFX PrimerDark theme with EventProof CSS and Feather icons.
- FXML main window, selected sidebar navigation, dashboard and module placeholders.
- Reliability preview (86/100, low risk), statistics, upcoming events and factor bars.
- Restrained dashboard entrance and page fade animations; navigation hover feedback.
- Independent JDBC connection infrastructure and an opt-in connection diagnostic.
- GUI starts without MySQL or database credentials.
- Abstract Event with TechnicalEvent, SportsEvent and CulturalEvent subclasses.
- Venue and Incident models, validation exceptions and database error handling.
- Three-table MySQL schema and transactional, prepared-statement JDBC CRUD.
- Reliability strategy interface, score breakdown and low/medium/high risk levels.
- In-memory console demonstration for Review 2.

Events, Venues, Incidents, Reliability, Simulation and Settings currently show
placeholders. Header notification/settings controls are reserved for future work.
CRUD and reliability calculation work in the core layer. Their GUI forms, failure
simulation and schema migrations are scheduled for later stages.

## Stack and prerequisites

Install JDK **25**, Maven **3.9+**, VS Code with **Extension Pack for Java**, and
Scene Builder (a current version supporting your FXML runtime). MySQL 8.0.16+ is
needed for JDBC CRUD and enforced schema constraints.

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

Create the database in MySQL Workbench or the MySQL CLI:

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
connect/socket timeouts. DAOs close connections, statements and result sets
using try-with-resources, commit successful operations and roll back failures.

Environment variables are read by `DatabaseConnection`; a `.env` file is **not**
automatically loaded. Do not commit real credentials. Controllers contain no
database access. After verifying your connection, create the three tables:

```powershell
mvn exec:java "-Dexec.mainClass=com.eventproof.database.DatabaseSchemaSetup"
```

This explicitly creates missing tables; it does not modify existing tables or
insert sample data. Use a fresh database if you already have an incompatible
schema. The SQL lives in `src/main/resources/com/eventproof/database/schema.sql`.

Run the live CRUD integration test against a development database:

```powershell
$env:EVENTPROOF_DB_TESTS = 'true'
mvn test "-Dtest=MySqlCrudIntegrationTest"
Remove-Item Env:EVENTPROOF_DB_TESTS
```

The test creates uniquely named rows and cleans up those rows in a finally block.
It verifies CRUD, category restoration, incident history, foreign-key restrictions
and event-to-incident delete cascading. It is skipped in the normal test suite
unless explicitly enabled. No passwords are stored in files.

## Review 2 console demo

```powershell
mvn compile exec:java "-Dexec.mainClass=com.eventproof.demo.Stage2Demo"
```

This runs without MySQL and demonstrates inheritance, polymorphism, strategy
selection, explained scores and a caught validation exception. It prints
Technical = 70 / MEDIUM, Sports = 100 / LOW, Cultural = 70 / MEDIUM for its
documented sample inputs. These are separate from the GUI's foundation fixtures.

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
├── docs/STAGE2.md
├── src/main/java/com/eventproof/
│   ├── Main.java
│   ├── controller/
│   │   ├── MainController.java
│   │   ├── DashboardController.java
│   │   └── PlaceholderController.java
│   ├── database/
│   │   ├── DatabaseConnection.java
│   │   ├── DatabaseConnectionCheck.java
│   │   ├── ConnectionProvider.java
│   │   └── DatabaseSchemaSetup.java
│   ├── demo/Stage2Demo.java
│   ├── util/
│   │   ├── Resources.java
│   │   └── ValidationUtil.java
│   ├── model/
│   │   ├── Event.java
│   │   ├── TechnicalEvent.java
│   │   ├── SportsEvent.java
│   │   ├── CulturalEvent.java
│   │   ├── EventFactory.java
│   │   ├── EventType.java
│   │   ├── Venue.java
│   │   ├── Incident.java
│   │   ├── IncidentType.java
│   │   ├── RiskLevel.java
│   │   ├── ScoreFactor.java
│   │   ├── ReliabilityAssessment.java
│   │   └── package-info.java
│   ├── service/
│   │   ├── EventService.java
│   │   ├── VenueService.java
│   │   ├── IncidentService.java
│   │   ├── ReliabilityService.java
│   │   └── package-info.java
│   ├── dao/
│   │   ├── CrudDAO.java
│   │   ├── JdbcDAO.java
│   │   ├── EventDAO.java
│   │   ├── VenueDAO.java
│   │   ├── IncidentDAO.java
│   │   └── package-info.java
│   ├── exception/
│   │   ├── InvalidEventException.java
│   │   ├── InvalidVenueException.java
│   │   ├── InvalidIncidentException.java
│   │   ├── DatabaseException.java
│   │   ├── RecordNotFoundException.java
│   │   └── package-info.java
│   └── strategy/
│       ├── ReliabilityStrategy.java
│       ├── PenaltyReliabilityStrategy.java
│       ├── TechnicalReliabilityStrategy.java
│       ├── SportsReliabilityStrategy.java
│       ├── CulturalReliabilityStrategy.java
│       └── package-info.java
├── src/main/resources/com/eventproof/
│   ├── fxml/
│   │   ├── Main.fxml
│   │   ├── Dashboard.fxml
│   │   └── Placeholder.fxml
│   ├── css/
│   │   ├── main.css
│   │   ├── components.css
│   │   └── dashboard.css
│   ├── images/.gitkeep
│   └── database/schema.sql
└── src/test/java/com/eventproof/
    ├── FoundationTest.java
    ├── model/ModelValidationTest.java
    ├── service/ReliabilityServiceTest.java
    ├── service/CrudServiceTest.java
    ├── dao/JdbcFailureTest.java
    └── dao/MySqlCrudIntegrationTest.java
```

Generated `target/` and `.maven-repository/` folders are excluded from Git.
Package documentation describes each layer's responsibility.

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
Core tests also cover validation, immutable history, strategy weights, boundary
scores, SQL parameter binding, resource cleanup, rollback and missing records.
Run the opt-in database commands above to validate your local server separately.

## Incremental roadmap

1. Stage 1: foundation and dashboard skeleton (complete).
2. Stage 2: models, inheritance, interfaces, custom exceptions, JDBC CRUD,
   services and reliability engine (implemented; live MySQL verification requires credentials).
3. Stage 3: complete Event/Venue/Incident forms and connect reliability results
   to the GUI; then failure simulation and dashboard polish.
4. Stage 4: end-to-end testing, report, presentation and viva preparation.

Planned features include event/venue management, incident tracking, reliability
scoring, risk classification and power/internet/equipment failure simulation.
Add each stage independently so the application remains runnable throughout.
