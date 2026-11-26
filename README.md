# Event Stream Analyzer

Command-line tool that reads IoT / smart-building event records (`key=value` lines),
classifies them (sensor / device / access), and writes aggregated results to
`sensors.json`, `devices.json`, and `access.json`.

## Build
    mvn clean package        # runs tests, produces target/analyzer.jar

## Run
    java -jar target/analyzer.jar --file events.txt --outdir results/

| Argument | Required | Description |
|---|---|---|
| `--file <path>` | yes | input file |
| `--outdir <dir>` | no | output directory (default: current directory) |

Errors (missing `--file`, file not found, ...) print a message to stderr and exit with code 1.

## Calculation conventions
- Percentiles: nearest-rank, value at index `ceil(p/100 * N) - 1` of the sorted list.
- Median: average of the two middle values for even N.
- Standard deviation: population (divide by N).
- Decimals rounded to 1 place; integral values written without a decimal point.

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
- [ ] Sensor / Device / Access aggregators (output files are written once these are registered)
- [ ] Design PDF in docs/
