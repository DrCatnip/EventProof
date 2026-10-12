# EventProof

Event Reliability Management System: a Java desktop project for Advanced OOP.
Stage 3 connects the Stage 2 models, services, strategies and JDBC DAOs to a
complete JavaFX workspace.

## Working features

- Events: add, edit, delete, search, category filter, venue association and score/risk badges.
- Venues: add, edit, delete, search, capacity and power/internet backup settings.
- Incidents: add, edit, delete, search and event association.
- Reliability: derived score, risk level and four explained deductions.
- Simulation: four temporary failure scenarios, animated score, risk badge and reset.
- Dashboard: database counts, upcoming events, readiness and portfolio average.
- AtlantaFX dark theme, EventProof CSS, Ikonli icons and subtle transitions.
- FXML forms, validation messages, deletion confirmation and background JDBC tasks.

Settings remains a placeholder; header notification/settings buttons are disabled.
The application can open without credentials and displays a connection message.
It needs MySQL for real records; there is no silent in-memory production fallback.

See [Stage 3 implementation and demonstration guide](docs/STAGE3.md) for the
changed files, scoring rules and complete teacher demonstration workflow.
[Stage 2 / Review 2 guide](docs/STAGE2.md) explains the OOP core.

## Requirements

JDK **25**, Maven **3.9+**, MySQL **8.0.16+**, and an interactive Windows desktop.
VS Code's Extension Pack for Java and Scene Builder are useful for development.

| Dependency | Version |
| --- | --- |
| JavaFX controls / FXML | 27 |
| AtlantaFX base | 3.0.0 |
| Ikonli JavaFX / Feather | 12.4.0 |
| MySQL Connector/J | 9.6.0 |
| JUnit Jupiter (tests) | 5.12.2 |

Build plugins: compiler 3.14.0, Surefire 3.5.3, JavaFX 0.0.8 and exec 3.5.0.
Maven provides the JavaFX native libraries. No separate JavaFX SDK is needed.
The ignored local dependency cache is configured in `.mvn/maven.config`.
Initial dependency downloads need internet access.

## Run

Open this folder in VS Code and select JDK 25 with **Java: Configure Java Runtime**.
From the project directory:

```powershell
java -version
mvn -version
powershell -NoExit -ExecutionPolicy Bypass -File .\scripts\Launch-EventProof.ps1
```

The launcher defaults to `jdbc:mysql://localhost:3306/eventproof` and user `root`.
It asks for a masked password when `DB_PASSWORD` is absent. The password stays
in that terminal's environment and is never written to files. If you already
have credentials in a PowerShell terminal, run:

```powershell
.\scripts\Launch-EventProof.ps1
# Or:
mvn compile javafx:run
```

Set `DB_URL` and `DB_USER` in the same terminal first if using another server or
account. These variables and `DB_PASSWORD` are read by `DatabaseConnection`;
`.env` files are not loaded automatically. Do not commit credentials.

The window opens at 1280 by 850, with minimum size 1060 by 740.
Assessment pages scroll vertically; tables resize with the window.

## Sample database

One database, `eventproof`, contains `venues`, `events` and `incidents`.
To create the database/tables and seed empty tables:

```powershell
powershell -NoExit -ExecutionPolicy Bypass -File .\scripts\Setup-DemoDatabase.ps1
```

The script asks locally for the password. It inserts samples only when **all
three tables are empty**, preserving existing data and avoiding duplicates.
Samples contain 3 venues, 3 events and 4 incidents, with upcoming dates based on
the database server's current date:

| Event | Score | Risk |
| --- | --- | --- |
| Tech Fest | 90 | LOW |
| Sports Meet | 75 | MEDIUM |
| Cultural Fest | 45 | HIGH |

In MySQL Workbench:

```sql
USE eventproof;
SHOW TABLES;
SELECT * FROM venues;
SELECT * FROM events;
SELECT * FROM incidents;
```

Stage 3 introduces no tables or schema changes. Calculated scores and simulations
are not stored. Incidents are counted as **Recorded incidents**, since the
existing schema does not contain an active/resolved field.

For manual setup, the schema and seed SQL are under
`src/main/resources/com/eventproof/database/`. With DB variables configured:

```powershell
mvn compile exec:java "-Dexec.mainClass=com.eventproof.database.DemoDatabaseSetup"
mvn exec:java "-Dexec.mainClass=com.eventproof.database.DatabaseConnectionCheck"
```

## Verification

```powershell
mvn test
# Full suite including live MySQL tests, followed by application launch:
.\scripts\Launch-EventProof.ps1 -Verify
```

Normal tests cover models, strategies, SQL failure handling, simulation isolation,
FXML forms, navigation, search, validation/save dialogs, animation and resizing.
The suite requires a graphical desktop. Screenshots are saved under ignored
`target/`. JavaFX can print an unnamed-module diagnostic in the test runner.

Two database tests are skipped unless `EVENTPROOF_DB_TESTS=true`.
The launcher enables them temporarily with `-Verify`. They create unique test
rows, verify relationships, persistence and the form-to-service-to-JDBC workflow,
then delete only their own rows. Run them against a local development database.

## Architecture

```text
controller/          Navigation, page controllers and FXML form controllers
service/             CRUD services, AppContext, reliability and simulation
model/               Immutable entities, event subtypes and result records
strategy/            ReliabilityStrategy and category-specific implementations
dao/                 Prepared-statement JDBC CRUD and aggregate loading
database/            Connection configuration, schema and sample setup
exception/           Validation, missing-record and database exceptions
util/                View loading, table styling, dialogs and friendly errors
```

Controllers contain no SQL. JDBC operations run on a shared background worker;
JavaFX updates run on the application thread. Reliability retains the Stage 2
strategy engine. Simulation adds one temporary deduction without calling a DAO.

## Scene Builder and Review 2

Open FXML files under `src/main/resources/com/eventproof/fxml/`.
Import the Ikonli JavaFX, core and Feather-pack 12.4.0 jars from
`.maven-repository/org/kordamp/ikonli/` in Scene Builder's library manager.
Load `main.css`, `components.css`, `dashboard.css` and `workspace.css` in order.
Palette values live on `.app-root`; preview pages within Main.fxml.

The original OOP console demonstration still works without MySQL:

```powershell
mvn compile exec:java "-Dexec.mainClass=com.eventproof.demo.Stage2Demo"
```

Stage 4: prepare the report, screenshots, presentation, viva answers and submission
package; perform final manual usability checks and teacher demo rehearsal.
