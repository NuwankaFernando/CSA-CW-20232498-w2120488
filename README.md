# Smart Campus Sensor & Room Management API

> **5COSC022W Client-Server Architectures — Coursework 2025/26**  
> University of Westminster (IIT) | Student: Nuwanka Fernando | ID: 20232498

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [API Design Overview](#api-design-overview)
5. [Build & Run Instructions](#build--run-instructions)
6. [Sample curl Commands](#sample-curl-commands)
7. [Conceptual Report — Question Answers](#conceptual-report--question-answers)

---

## Project Overview

This project is a fully RESTful API built with **JAX-RS (Jersey 2.32)** for managing the university's Smart Campus infrastructure. It provides endpoints to manage physical **Rooms**, IoT **Sensors** deployed within those rooms, and historical **Sensor Readings**.

Key characteristics:
- Versioned API entry point at `/api/v1`
- Fully in-memory data store using `ArrayList` and `HashMap` (no database)
- Custom exception hierarchy with JAX-RS `ExceptionMapper` implementations
- Request/response logging via JAX-RS filter
- Sub-resource locator pattern for nested sensor readings
- HATEOAS-style discovery endpoint

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 8+ |
| REST Framework | JAX-RS 2.1 (Jersey 2.32) |
| JSON | Jackson 2.10.1 |
| Build Tool | Maven |
| Server | Apache Tomcat 9 (GlassFish/Payara compatible) |
| Data Storage | In-memory (`ArrayList`, `HashMap`) |

---

## Project Structure

```
SmartCampusAPI/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── SmartCampusApplication.java          # @ApplicationPath("/api/v1") — JAX-RS entry point
    ├── dao/
    │   ├── BaseDAO.java                     # Generic DAO wrapping any List<T extends BaseModel>
    │   └── MockDatabase.java                # Static in-memory data store (seed data included)
    ├── models/
    │   ├── BaseModel.java                   # Interface: getId() / setId()
    │   ├── Room.java                        # id, name, capacity, sensorIds
    │   ├── Sensor.java                      # id, type, status, currentValue, roomId
    │   └── SensorReading.java               # id (UUID), timestamp (epoch ms), value
    ├── resources/
    │   ├── DiscoveryResource.java           # GET /api/v1  — HATEOAS discovery
    │   ├── SensorRoomResource.java          # GET|POST /rooms, GET|DELETE /rooms/{id}
    │   ├── SensorResource.java              # GET|POST /sensors, sub-resource locator
    │   └── SensorReadingResource.java       # GET|POST /sensors/{id}/readings
    ├── exceptions/
    │   ├── RoomNotEmptyException.java       # Thrown on DELETE room with active sensors
    │   ├── LinkedResourceNotFoundException  # Thrown on POST sensor with invalid roomId
    │   └── SensorUnavailableException.java  # Thrown on POST reading to MAINTENANCE sensor
    ├── mappers/
    │   ├── RoomNotEmptyExceptionMapper      # → HTTP 409 Conflict
    │   ├── LinkedResourceNotFoundExceptionMapper # → HTTP 422 Unprocessable Entity
    │   ├── SensorUnavailableExceptionMapper # → HTTP 403 Forbidden
    │   └── GlobalExceptionMapper.java       # → HTTP 500 (catch-all Throwable)
    └── filters/
        └── LoggingFilter.java               # Logs method+URI (request) & status (response)
```

---

## API Design Overview

### Base URL
```
http://localhost:8080/SmartCampusAPI/api/v1
```

### Endpoints Summary

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1` | Discovery — returns API metadata and resource links |
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room by ID |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (blocked if active sensors exist → 409) |
| GET | `/api/v1/sensors` | List all sensors (supports `?type=` filter) |
| POST | `/api/v1/sensors` | Register a new sensor (validates roomId → 422 if invalid) |
| GET | `/api/v1/sensors/{sensorId}` | (via locator) — returns 404 if not found |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get reading history for a sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a reading (403 if sensor is MAINTENANCE) |

### HTTP Status Codes Used

| Status | Meaning | When Used |
|--------|---------|-----------|
| 200 OK | Success | GET, DELETE success |
| 201 Created | Resource created | POST success |
| 400 Bad Request | Malformed request | Missing required fields |
| 403 Forbidden | Not allowed | POST reading to MAINTENANCE sensor |
| 404 Not Found | Resource missing | Unknown room/sensor ID |
| 409 Conflict | Business rule violation | DELETE room with active sensors |
| 415 Unsupported Media Type | Wrong Content-Type | Automatic by JAX-RS |
| 422 Unprocessable Entity | Invalid reference | POST sensor with non-existent roomId |
| 500 Internal Server Error | Unexpected error | Any unhandled exception |

---

## Build & Run Instructions

### Prerequisites

- **Java 8 or higher** — verify with `java -version`
- **Maven 3.6+** — verify with `mvn -version`
- **Apache Tomcat 9** — [download here](https://tomcat.apache.org/download-90.cgi)

### Step 1 — Clone the Repository

```bash
git clone https://github.com/<your-username>/SmartCampusAPI.git
cd SmartCampusAPI
```

### Step 2 — Build the WAR File

```bash
mvn clean package
```

This produces `target/SmartCampusAPI-1.war`.

### Step 3 — Deploy to Tomcat

**Option A — Copy WAR manually:**
```bash
cp target/SmartCampusAPI-1.war /path/to/tomcat/webapps/SmartCampusAPI.war
/path/to/tomcat/bin/startup.sh   # Linux/macOS
/path/to/tomcat/bin/startup.bat  # Windows
```

**Option B — Deploy via Tomcat Manager:**
1. Open `http://localhost:8080/manager/html`
2. Scroll to "WAR file to deploy"
3. Select `target/SmartCampusAPI-1.war` and click Deploy

### Step 4 — Verify the Server is Running

```bash
curl http://localhost:8080/SmartCampusAPI/api/v1
```

Expected response:
```json
{
  "api": "Smart Campus Sensor & Room Management API",
  "version": "1.0",
  "contact": "suvin.20232498@iit.ac.lk",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

> **Note:** The context path is `/SmartCampusAPI` (matching the WAR filename). Adjust if you rename the WAR.

---

## Sample curl Commands

All commands assume the server is running at `http://localhost:8080/SmartCampusAPI`.

### 1. Discovery Endpoint
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1 \
  -H "Accept: application/json"
```

### 2. Get All Rooms
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Accept: application/json"
```

### 3. Create a New Room
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"ENG-201","name":"Engineering Lab","capacity":40}'
```

### 4. Get a Specific Room
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms/LEC-301 \
  -H "Accept: application/json"
```

### 5. Attempt to Delete a Room with Active Sensors (expect 409)
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LEC-301 \
  -H "Accept: application/json"
```

### 6. Get All Sensors
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Accept: application/json"
```

### 7. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=Temperature" \
  -H "Accept: application/json"
```

### 8. Register a New Sensor
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-002","type":"CO2","status":"ACTIVE","currentValue":410.0,"roomId":"LEC-302"}'
```

### 9. Register a Sensor with Invalid roomId (expect 422)
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-999","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"GHOST-999"}'
```

### 10. Get Sensor Readings
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/Temp-001/readings \
  -H "Accept: application/json"
```

### 11. Post a New Sensor Reading
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/Temp-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":26.5}'
```

### 12. Post a Reading to a MAINTENANCE Sensor (expect 403)
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/Temp-002/readings \
  -H "Content-Type: application/json" \
  -d '{"value":15.0}'
```

---

## Conceptual Report — Question Answers

---

### Part 1 — Service Architecture & Setup

#### 1.1 Project & Application Configuration

**Question:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? How does this impact in-memory data management?

By default, JAX-RS uses a **per-request lifecycle** — a brand new instance of each resource class is created for every incoming HTTP request and discarded afterwards. This means that instance-level fields on a resource class cannot hold shared state between requests.

In this project, `SensorRoomResource`, `SensorResource`, and `SensorReadingResource` are all per-request resources. To safely store data that persists across requests, a **static `MockDatabase` class** is used as the shared in-memory store. The lists and maps declared with the `static` keyword live at the class level and survive across request instances:

```java
public class MockDatabase {
    public static List<Room> ROOMS = new ArrayList<>();
    public static final List<Sensor> SENSORS = new ArrayList<>();
    public static final Map<String, List<SensorReading>> READINGS = new HashMap<>();
}
```

Each resource class wraps these shared lists through a generic `BaseDAO<T>` injected at construction time (e.g., `new BaseDAO<>(MockDatabase.ROOMS)`), so all resource instances operate on the same underlying data.

A potential race condition exists because `ArrayList` and `HashMap` are not thread-safe. In a production environment this would be addressed by replacing them with `CopyOnWriteArrayList` and `ConcurrentHashMap`, which provide thread-safe concurrent access without explicit synchronisation blocks.

---

#### 1.2 The Discovery Endpoint

**Question:** Why is Hypermedia (HATEOAS) considered a hallmark of advanced RESTful design? How does it benefit client developers compared to static documentation?

HATEOAS (Hypermedia as the Engine of Application State) is Level 3 of the Richardson REST Maturity Model — the highest level. It means API responses embed navigational links to related resources rather than forcing clients to hard-code URLs.

The discovery endpoint at `GET /api/v1` returns a `resources` map advertising available collections:

```json
{
  "api": "Smart Campus...",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

This benefits client developers in several ways: the client does not need to know URLs ahead of time and simply follows the links provided. If the server changes a URL path, clients that navigate via links rather than hard-coded strings will continue to work without modification. The API is self-documenting and explorable at runtime, keeping the live API as the authoritative source of navigation truth — unlike static documentation which can become stale the moment the API changes.

---

### Part 2 — Room Management

#### 2.1 Room Resource Implementation

**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

This implementation returns **full Room objects** from `GET /api/v1/rooms`, including id, name, capacity, and sensorIds.

**Returning only IDs** produces a small initial payload but forces the client to issue one additional `GET /api/v1/rooms/{id}` request per room to display meaningful information. This is the well-known **N+1 problem** — with N rooms there are N+1 round trips, dramatically increasing total latency and server load at scale.

**Returning full objects** is more expensive in payload size per response but delivers everything the client needs in a single round trip, which is far more efficient for a management dashboard. A thoughtful middle ground for very large datasets would be server-side **pagination** (e.g., `?page=1&size=20`) combined with optional sparse fieldsets (e.g., `?fields=id,name`).

---

#### 2.2 Room Deletion & Safety Logic

**Question:** Is the DELETE operation idempotent in your implementation? Justify with what happens on repeated DELETE requests.

In REST, idempotency means applying the same operation multiple times produces the same server state as applying it once. DELETE is defined as idempotent in RFC 9110.

In this implementation:
- **First DELETE (room exists, no active sensors):** The room is removed via `roomDAO.delete(room)` and `200 OK` is returned.
- **Subsequent DELETEs (room no longer exists):** `roomDAO.getById(roomId)` returns null and `404 Not Found` is returned.

The **server state** is identical after all calls — the room is absent from the store — satisfying idempotency at the resource-state level. The differing status codes (200 then 404) are acceptable and common; HTTP idempotency refers to state, not response codes. The safety constraint that blocks deletion of rooms with active sensors (throwing `RoomNotEmptyException` → 409) is checked first, ensuring the data store is never left in an orphaned state.

---

### Part 3 — Sensor Operations & Linking

#### 3.1 Sensor Resource & Integrity

**Question:** What are the technical consequences if a client sends data in a format other than `application/json` to a `@Consumes(MediaType.APPLICATION_JSON)` endpoint?

The `@Consumes(MediaType.APPLICATION_JSON)` annotation on `POST /api/v1/sensors` in `SensorResource.java` instructs JAX-RS to only accept requests whose `Content-Type` header is `application/json`.

If a client sends `Content-Type: text/plain` or `Content-Type: application/xml`, JAX-RS rejects it **before the resource method is ever invoked**. The framework's content-negotiation layer compares the incoming `Content-Type` against the set of accepted media types and, finding no match, automatically returns **HTTP 415 Unsupported Media Type**. No custom exception-handling code is required — it is entirely managed by the JAX-RS runtime, providing a clean, safe contract between client and server.

---

#### 3.2 Filtered Retrieval & Search

**Question:** Contrast `@QueryParam` filtering (e.g., `?type=CO2`) with path-based filtering (e.g., `/sensors/type/CO2`). Why is `@QueryParam` superior?

The path-based approach `/sensors/type/CO2` is semantically incorrect in REST design because URL path segments are intended to identify a specific, uniquely-addressable resource. `type/CO2` implies a discrete entity at that path, not a filtered view.

The `@QueryParam` approach is semantically correct because query parameters represent optional, non-identifying modifiers applied to a resource collection. It also scales elegantly — multiple filters combine naturally (e.g., `?type=CO2&status=ACTIVE`) without defining a new URL pattern for every combination. It is well-supported by HTTP caching infrastructure and clearly signals to consumers that the result is a dynamic filtered view, not a fixed sub-resource. Path parameters are reserved for resource identity (e.g., `/sensors/Temp-001`), not filtering.

---

### Part 4 — Deep Nesting with Sub-Resources

#### 4.1 The Sub-Resource Locator Pattern

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern versus defining all nested paths in one large controller.

In this project, `SensorResource` uses a sub-resource locator to delegate all reading-related requests to a dedicated `SensorReadingResource` class:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource getReadingResource(
        @PathParam("sensorId") String sensorId) {
    Sensor sensor = sensorDAO.getById(sensorId);
    if (sensor == null) { throw new WebApplicationException(404); }
    return new SensorReadingResource(sensorId);
}
```

The key benefits are:

**Single Responsibility:** Each class handles exactly one logical resource. `SensorResource` manages sensor CRUD; `SensorReadingResource` manages readings. Neither is aware of the other's internal logic.

**Reduced complexity:** A single "God controller" handling all nested endpoints would become unmanageable. Sub-resources allow the codebase to scale horizontally as the API grows.

**Context injection:** The `sensorId` is validated in the locator (returning 404 if absent) and passed cleanly into the `SensorReadingResource` constructor, so the sub-resource never needs to re-validate the parent.

**Testability:** Each class can be unit-tested in complete isolation.

---

### Part 5 — Error Handling, Exception Mapping & Logging

#### 5.1 Dependency Validation (422 Unprocessable Entity)

**Question:** Why is HTTP 422 more semantically accurate than 404 when a JSON payload references a non-existent resource?

`HTTP 404 Not Found` means the **URL itself** was not found — the endpoint does not exist. In this scenario, `POST /api/v1/sensors` exists and is perfectly valid. Returning 404 would mislead the client into thinking the API path is wrong.

`HTTP 422 Unprocessable Entity` is correct because the request is syntactically well-formed JSON that was successfully parsed, the URL and HTTP method are correct, but the **semantic content** of the payload fails a business-logic validation rule — the `roomId` field references a resource that does not exist. This tells the client precisely what to fix: the payload content, not the URL. RFC 4918 defines 422 as: "The server understands the content type of the request entity, and the syntax is correct, but it was unable to process the contained instructions."

---

#### 5.2 The Global Safety Net (500)

**Question:** From a cybersecurity standpoint, what risks arise from exposing Java stack traces to API consumers?

The `GlobalExceptionMapper` catches all unhandled `Throwable` instances and returns a generic `500 Internal Server Error`, logging the real exception server-side only. Exposing stack traces creates the following attack vectors:

1. **Technology fingerprinting:** Stack traces reveal exact library names and versions (e.g., `jersey-server-2.32`, `jackson-databind-2.10.1`). Attackers can look up known CVEs for those specific versions and craft targeted exploits.

2. **Internal path disclosure:** File paths in stack traces (e.g., `/home/deploy/SmartCampusAPI/src/...`) reveal the server's directory structure, aiding path traversal and other attacks.

3. **Business logic exposure:** Class names, method names, and the full call chain enable an attacker to reverse-engineer the application's internal architecture and identify weak points.

4. **Exception type leakage:** A `NullPointerException` at a specific line tells an attacker that a particular input caused a null dereference, which they can exploit to probe input validation boundaries.

---

#### 5.3 API Request & Response Logging Filters

**Question:** Why is it better to use JAX-RS filters for logging rather than inserting `Logger.info()` in every resource method?

`LoggingFilter` implements both `ContainerRequestFilter` and `ContainerResponseFilter` and is registered via `@Provider`, intercepting every request and response automatically without changes to any resource class.

Inserting `Logger.info()` calls manually into every resource method violates the **DRY (Don't Repeat Yourself)** and **Single Responsibility** principles. Each class would need to be individually modified to change logging behaviour — a maintenance burden that grows with every new endpoint. A developer adding a new endpoint might forget to add logging, creating invisible blind spots in observability.

The filter approach treats logging as a true **cross-cutting concern**. Changing the logging strategy (e.g., switching to SLF4J, adding correlation IDs) requires a change in only one place. This is aligned with industry-standard **AOP (Aspect-Oriented Programming)** design principles and is the accepted best practice for cross-cutting concerns in JAX-RS applications.

---

*Report prepared by Nuwanka Fernando | Student ID: 20232498 | 5COSC022W 2025/26*
