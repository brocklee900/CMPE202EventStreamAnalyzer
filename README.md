# Event Stream Analyzer

Command-line tool that reads IoT / smart-building event records (`key=value` lines),
classifies them (sensor / device / access), and writes aggregated results to
`sensors.json`, `devices.json`, and `access.json`.

## Build
Set your name once in `pom.xml` (`<submission.name>FirstName_LastName</submission.name>`), then:

    mvn clean package        # runs tests, produces target/FirstName_LastName_EventStreamAnalyzer.jar

Or override it without editing the pom:

    mvn clean package -Dsubmission.name=Jane_Doe

The main class is `com.example.analyzer.EventStreamAnalyzer`; the jar's manifest points to it,
so it runs with `java -jar` alone.

## Run
    java -jar target/FirstName_LastName_EventStreamAnalyzer.jar --file events.txt --outdir results/

| Argument | Required | Description |
|---|---|---|
| `--file <path>` | yes | input file |
| `--outdir <dir>` | no | output directory (default: current directory) |

Errors (missing `--file`, file not found, ...) print a message to stderr and exit with code 1
(code 2 for an I/O error while reading or writing).

## Calculation conventions
- Percentiles: nearest-rank, value at index `ceil(p/100 * N) - 1` of the sorted list.
- Median: average of the two middle values for even N.
- Standard deviation: population (divide by N).
- Decimals rounded to 1 place (half-up); any value that is a whole number after rounding is written
  without a decimal point (`45`, not `45.0`). The assignment text requires this, although its example
  JSON shows some whole numbers as `21.0` / `5.0`; this implementation follows the text.
- Note: the assignment's sample `access.json` (8 records, `90_percentile: 45`, `max: 60`) cannot occur under
  nearest-rank, since for N = 8 the 90th percentile is rank 8, i.e. the maximum. It is treated as illustrative.

## Output
- `sensors.json`: per sensor `count`, `minimum`, `median`, `average`, `maximum`, `std_dev`.
- `devices.json`: `by_severity` (all four levels, zeros included) and `by_device` with `total` and
  `most_frequent_severity` (ties go to the higher severity: CRITICAL > ERROR > WARN > INFO).
- `access.json`: per door `decision_times` (`min`, `50_percentile`, `90_percentile`, `99_percentile`, `max`),
  `results` (`GRANTED`, `DENIED`, zeros included) and `flagged_users` (3+ denials on that door, sorted).
- A file with no records of its type contains `{}`.

## Classification rules
Lines are passed through a chain of handlers in this order: sensor reading, device event, access record.
The first handler whose required keys are present and valid claims the line; lines no handler claims are skipped.
- Sensor reading: non-blank `sensor`, finite decimal `reading` (e.g. `21.5`, `-3`, `1e3`; not `abc`, `NaN`, `0x10`).
- Device event: non-blank `device`, `severity` exactly one of `CRITICAL`, `ERROR`, `WARN`, `INFO`.
- Access record: non-blank `user` and `door`, `result` exactly `GRANTED` or `DENIED`, `duration_ms` a non-negative integer.
- Enum values are case-sensitive. Extra keys are ignored.

## Status
- [x] CLI argument parsing + error handling
- [x] Line parser (key=value, quoted values)
- [x] Classification (Chain of Responsibility: sensor -> device -> access)
- [x] JSON output writer
- [x] Sensor / Device / Access aggregators (Template Method base: `RecordAggregator`)
- [ ] Design PDF in docs/
