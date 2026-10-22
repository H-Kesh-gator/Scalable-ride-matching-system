# Rideflow: interactive Java ride-booking demo

This UI extension builds on the group project at https://github.com/HritikBudhwar/Scalable-ride-matching-system. Original source and Git history are preserved. The original `com.platform` backend contains unfinished controllers, service interfaces, and persistence mappings. This extension uses an isolated `com.ridedemo` Spring Boot application so the demo is runnable without presenting those unfinished features as implemented.

## Run locally

Install JDK 17 and Maven 3.6+ (the project still targets Java 8 bytecode).

```sh
mvn -Pdemo spring-boot:run -Dspring-boot.run.profiles=demo
```

Open http://localhost:8080. The demo profile binds to localhost. No Node, API keys, database setup, or external map service is required. Stop with Ctrl+C.

## Try it

1. Enter your name and select different pickup and drop-off locations.
2. Choose Economy, Comfort, or Premium and inspect the server-calculated fare.
3. Select **Find my ride**. The API assigns the nearest available sample driver in that category.
4. Start and complete the ride, or cancel before it starts. The driver becomes available again.
5. Book three simultaneous rides in one category to see the no-driver error (two drivers per category).
6. Refresh the page: rides remain until the Java server restarts.

## Implementation

- Responsive, keyboard-accessible HTML/CSS/JavaScript UI served by Spring Boot.
- JSON REST API for locations, drivers, fare estimates, booking, and trip transitions.
- Haversine straight-line distance for nearest-driver selection and transparent demo pricing: INR 40 base + INR 12/18/24 per kilometre by category.
- Synchronized allocation prevents double assignment within one JVM. Matching scans six sample drivers; no distributed scaling or load-test claim is made.
- Server-side validation, unavailable-driver errors, illegal-transition rejection, and frontend loading/error states.
- Integration and concurrency tests: `mvn test`.

## Demo boundaries

Sample drivers and fixed Bengaluru locations; schematic map, not navigation. In-memory state shared by all local demo visitors and reset on restart. No authentication, real dispatch, live GPS, durable database, payment processing, traffic routing, or multi-node coordination. Existing `/api/bookings`, `/api/users`, `/api/trips`, and payment/admin scaffolds are outside this demo; the working UI uses `/api/demo` only. Do not expose this demo as a production service.

## API

| Method | Route | Purpose |
| --- | --- | --- |
| GET | `/api/demo/places` | Supported locations (zero-based IDs) |
| GET | `/api/demo/drivers` | Sample driver availability |
| POST | `/api/demo/quote` | Server-calculated fare and distance |
| GET / POST | `/api/demo/rides` | List rides / allocate a driver |
| POST | `/api/demo/rides/{id}/start` | Accepted to in progress |
| POST | `/api/demo/rides/{id}/complete` | In progress to completed |
| POST | `/api/demo/rides/{id}/cancel` | Accepted to cancelled |

Quote and booking body: `{"rider":"Demo rider","pickup":0,"dropoff":1,"category":"ECONOMY"}`.

## Attribution and resume use

This is a group project with an additional UI/demo implementation. Retain the original contributors and describe your own contribution accurately. Review and understand the added code before discussing it in interviews. No claims of production deployment, real users, measured scalability, or sole authorship are implied.
