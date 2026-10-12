# Stage 3: complete EventProof desktop workflow

Stage 3 builds on the existing Stage 2 event hierarchy, immutable models,
ReliabilityStrategy implementations, services, custom exceptions and JDBC DAOs.
The production application uses the existing MySQL database for all CRUD.

## Implemented modules

| Module | Behavior |
| --- | --- |
| Events | Add/edit/delete, search name/venue/ID, type filter, select and assess |
| Venues | Add/edit/delete, name search, capacity and backup settings |
| Incidents | Add/edit/delete, search, event association and full description |
| Reliability | Select event, derive score/risk, explain each deduction |
| Simulation | Select event/scenario, temporary result, animated score, reset |
| Dashboard | Counts, upcoming events, current scores, average and selected-event assessment |

All pages refresh on navigation and offer an explicit Refresh button.
CRUD dialogs stay open on validation or database errors and disable Save while
the operation is running. Delete requires confirmation. A referenced venue
cannot be deleted until its events are removed or reassigned. Deleting an event
cascades to its incidents; the confirmation explains this.

## Files added and modified

Paths below are relative to `src/main/java/com/eventproof/` unless noted.

| Area | Files |
| --- | --- |
| Updated application/navigation | `Main.java`, `controller/MainController.java`, `controller/DashboardController.java` |
| New page controllers | `controller/EventController.java`, `VenueController.java`, `IncidentController.java`, `ReliabilityController.java`, `SimulationController.java` |
| Reusable UI controllers | `controller/WorkspaceController.java`, `AssessmentController.java`, `FactorController.java` |
| Form controllers | `controller/form/FormController.java`, `EventFormController.java`, `VenueFormController.java`, `IncidentFormController.java` |
| New services | `service/AppContext.java`, `service/SimulationService.java` |
| New result models | `model/WorkspaceSnapshot.java`, `FailureScenario.java`, `SimulationResult.java` |
| New utilities | `util/Views.java`, `CrudDialogs.java`, `DisplayNames.java`, `TableSupport.java`, `UiErrors.java` |
| Sample setup support | `database/DemoDatabaseSetup.java`, updated `database/DatabaseConnection.java` |
| Updated FXML | `Main.fxml`, `Dashboard.fxml` |
| New FXML | `Events.fxml`, `Venues.fxml`, `Incidents.fxml`, `Reliability.fxml`, `Simulation.fxml`, `EventForm.fxml`, `VenueForm.fxml`, `IncidentForm.fxml`, `AssessmentPane.fxml`, `FactorRow.fxml` |
| New stylesheet | `src/main/resources/com/eventproof/css/workspace.css` |
| New sample SQL | `src/main/resources/com/eventproof/database/sample-data.sql` |
| Local tools | `scripts/Setup-DemoDatabase.ps1`, `scripts/Launch-EventProof.ps1` |
| Documentation | Updated `README.md`; new `docs/STAGE3.md` |
| Tests | Updated `FoundationTest`; new `FxTestSupport`, `TestWorkspace`, `ModuleViewTest`, `Stage3WorkflowIntegrationTest`, `SimulationServiceTest`, `DemoDatabaseSetupTest` |

FXML files live under `src/main/resources/com/eventproof/fxml/`.
Tests live under `src/test/java/com/eventproof/` with service/database subpackages.
Existing DAO, CRUD service, validation and scoring behavior is reused.

## Architecture and threading

```text
FXML → Controller → CRUD Service → DAO → JDBC → MySQL
                  → ReliabilityService → ReliabilityStrategy
                  → SimulationService → ReliabilityService
```

AppContext provides shared services and one daemon worker queue.
JavaFX Tasks run database reads/writes off the UI thread and deliver results
on the UI thread. Page controllers handle controls and navigation; shared
WorkspaceController handles loading/status/error feedback. UI helpers manage
consistent table cells, view loading and CRUD dialogs.

The Event abstract class and Technical/Sports/Cultural subclasses remain the
core inheritance example. JDBC restores the correct concrete subclass through
EventFactory. Venue/Incident validation uses existing custom exceptions.
ReliabilityService selects a category strategy through the existing interface.
SimulationService uses composition and immutable results; it has no database
dependency.

## Reliability rules retained from Stage 2

Start at 100 and subtract:

- Attendance above capacity: 25 points.
- Attendance at least 90% of capacity, without exceeding it: 5 points.
- Missing backup power: Technical 20, Sports 10, Cultural 20.
- Missing backup internet: Technical 20, Sports 0, Cultural 5.
- Incident history: 5 per recorded incident, capped at 30.

Clamp the result to zero. Risk is LOW at 80–100, MEDIUM at 60–79 and HIGH at 0–59.
Each assessment contains its factors and explanations. The GUI never hardcodes
an event's score. Factor bars visualize 100 minus each deduction; they are not
independent probabilities.

This is a rule-based readiness score for demonstration, not a statistical
prediction of actual event reliability.

## Temporary simulation rules

Each simulation starts from the current baseline and adds one scenario deduction:

| Scenario | Additional points deducted |
| --- | --- |
| Power failure | 15 with backup power, otherwise 30 |
| Internet failure | 8 with backup internet; otherwise Technical 20, Sports 5, Cultural 12 |
| Equipment failure | 20 |
| Overcrowding | 25 |

The total score clamps at zero. Repeated simulations do not accumulate.
Changing the event/scenario or clicking Reset restores the baseline.
No event, venue or incident is saved by simulation.
A 650 ms Timeline animates the score/ring and a 220 ms fade animates the risk badge.
Navigation uses a subtle 180 ms fade.

## Database and launch

One `eventproof` database with existing `venues`, `events`, `incidents` tables.
There are **no Stage 3 schema changes** and no stored score or simulation columns.
The setup script creates missing infrastructure explicitly and seeds only an
entirely empty database. Existing rows are preserved.

From the project directory:

```powershell
.\scripts\Launch-EventProof.ps1
# Live database tests first, then launch:
.\scripts\Launch-EventProof.ps1 -Verify
```

The masked password prompt configures `DB_PASSWORD` only in the current process
environment. Defaults are localhost:3306/eventproof and root.
Use the same terminal for subsequent Maven commands. Never put real credentials
in tracked files. Windows PowerShell stderr diagnostics are logged without
mistaking them for a failed command; Maven's exit code determines success.

## Teacher demonstration: complete workflow

1. Launch EventProof; show the real dashboard counts. In Workbench, run
   `USE eventproof; SHOW TABLES;` and open the three tables.
2. **Venues → Add venue**: name `Review Hall`, capacity `100`, enable both backups.
   Save. Show the new venue in the table and Workbench.
3. **Events → Add event**: name `Review Tech Day`, type Technical, future date,
   attendance `50`, venue Review Hall. Save. Score is **100 / LOW**.
4. **Incidents → Add incident**: select Review Tech Day, Equipment Failure,
   description `Projector failed during rehearsal`, today's date. Save.
5. **Events**: search Review Tech Day, select it, click View reliability.
   Score is now **95 / LOW**, with a 5-point incident deduction.
6. **Simulation**: select the same event and Power failure. Simulate.
   The score animates **95 → 80 / LOW**, because the venue has backup power.
   Equipment failure instead gives **75 / MEDIUM**.
7. Click Reset; score returns to **95**. In Workbench, verify no simulation record
   was inserted and the original event/incident values remain unchanged.
8. **Events → Edit** the attendance to `120`; the readiness becomes **70 / MEDIUM**.
   Show the 25-point overcapacity deduction and 5-point incident deduction.
9. Return to **Dashboard**: new counts and upcoming event reflect the database.
   Restart the app to demonstrate persistence.
10. Test invalid venue capacity `0` or blank event name: the dialog stays open
    with a clear message. Test deleting a venue still used by an event.
11. Clean up the demonstration event (its incidents cascade), then its venue.

For the seeded data, initial scores are Tech Fest 90/LOW, Sports Meet 75/MEDIUM
and Cultural Fest 45/HIGH. These values are derived from the sample records.

## Verification and limits

Module tests load real FXML views/forms as each module is added.
FoundationTest exercises navigation, filtering, animation/reset, resizable
layouts and actual dialog validation/asynchronous save.
Simulation tests verify all four scenarios, clamping, no accumulation and
immutable baseline data. Existing tests cover scoring boundaries, SQL failures,
rollback, resource cleanup and model validation.

The opt-in MySQL tests verify CRUD, references, cascading, subtype hydration and
the form → service → JDBC workflow with incident-driven score changes.
They create uniquely named records and remove only those records in finally.

Settings and header notifications remain intentionally unfinished.
The dashboard labels its count Recorded incidents because the schema has no
resolution state. Refresh is explicit/on navigation; there is no live polling.
Long assessment pages scroll at smaller window sizes.
The model supports incident history, rather than a separate resolved/active status.

## Stage 4 recommendations

- Rehearse the complete workflow and verify it with the teacher's expected inputs.
- Capture screenshots from the real sample database and prepare the final report.
- Include class/architecture diagrams, strategy rules, test results and schema.
- Prepare the presentation and viva answers for each OOP concept.
- Check keyboard navigation, longer text, empty database and unavailable MySQL.
- Prepare a clean source ZIP and installation/run instructions for submission.
