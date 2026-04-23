# Smart Campus Sensor & Room Management API

> **5COSC022W Client-Server Architectures — Coursework 2025/26**  
> **Informatics Institute of Technologies (IIT)**
> **Student:** W. S. Nuwanka Fernando
> **IIT ID:** 20232498
> **UoW ID:** w2120488

---

## Table of Contents

1. [API Design Overview](#api-design-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Build & Run Instructions](#build--run-instructions)
5. [Sample curl Commands](#sample-curl-commands)
6. [Conceptual Report — Question Answers](#conceptual-report--question-answers)

---

## API Design Overview

This project is a fully RESTful API built with **JAX-RS (Jersey 2.32)** for managing the university's Smart Campus infrastructure. It provides endpoints to manage physical **Rooms**, IoT **Sensors** deployed within those rooms, and historical **Sensor Readings**.

### Base URL

```
http://localhost:8080/SmartCampusAPI/api/v1
```

### Key Design Principles

- Versioned entry point via `@ApplicationPath("/api/v1")`
- Fully in-memory data store using `ArrayList` and `HashMap` — no database
- Sub-resource locator pattern for nested sensor readings
- Custom exception hierarchy with dedicated `ExceptionMapper` implementations
- Request/response logging via a single JAX-RS filter (`@Provider`)
- HATEOAS-style discovery endpoint at `GET /api/v1`

### Endpoints Summary

| Method   | Path                                  | Description                                 | Success Code |
| -------- | ------------------------------------- | ------------------------------------------- | :----------: |
| `GET`    | `/api/v1`                             | Discovery — API metadata and resource links |     200      |
| `GET`    | `/api/v1/rooms`                       | List all rooms                              |     200      |
| `POST`   | `/api/v1/rooms`                       | Create a new room                           |     201      |
| `GET`    | `/api/v1/rooms/{roomId}`              | Get a specific room by ID                   |     200      |
| `DELETE` | `/api/v1/rooms/{roomId}`              | Delete a room (409 if sensors exist)        |     200      |
| `GET`    | `/api/v1/sensors`                     | List sensors (supports `?type=` filter)     |     200      |
| `POST`   | `/api/v1/sensors`                     | Register a sensor (422 if roomId invalid)   |     201      |
| `GET`    | `/api/v1/sensors/{sensorId}/readings` | Get reading history for a sensor            |     200      |
| `POST`   | `/api/v1/sensors/{sensorId}/readings` | Add a reading (403 if MAINTENANCE)          |     201      |

### HTTP Status Codes

| Status                     | When Used                                         |
| -------------------------- | ------------------------------------------------- |
| 200 OK                     | Successful GET or DELETE                          |
| 201 Created                | Successful POST                                   |
| 400 Bad Request            | Missing or malformed required fields              |
| 403 Forbidden              | POST reading to a MAINTENANCE sensor              |
| 404 Not Found              | Unknown room or sensor ID                         |
| 409 Conflict               | DELETE room that still has active sensors         |
| 415 Unsupported Media Type | Wrong `Content-Type` (handled by JAX-RS)          |
| 422 Unprocessable Entity   | POST sensor with a non-existent `roomId`          |
| 500 Internal Server Error  | Any unhandled exception (caught by global mapper) |

---

## Technology Stack

| Component      | Technology                                       |
| -------------- | ------------------------------------------------ |
| Language       | Java 8+                                          |
| REST Framework | JAX-RS 2.1 (Jersey 2.32)                         |
| JSON Binding   | Jackson 2.10.1                                   |
| Build Tool     | Apache Maven                                     |
| Server         | Apache Tomcat 9                                  |
| Data Storage   | In-memory (`ArrayList`, `HashMap`) — no database |

---

## Project Structure

```
SmartCampusAPI/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── SmartCampusApplication.java           # @ApplicationPath("/api/v1") — JAX-RS bootstrap
    ├── dao/
    │   ├── BaseDAO.java                      # Generic DAO wrapping any List<T extends BaseModel>
    │   └── MockDatabase.java                 # Static in-memory store (with seed data)
    ├── models/
    │   ├── BaseModel.java                    # Interface: getId() / setId()
    │   ├── Room.java                         # id, name, capacity, sensorIds
    │   ├── Sensor.java                       # id, type, status, currentValue, roomId
    │   └── SensorReading.java                # id (UUID), timestamp (epoch ms), value
    ├── resources/
    │   ├── DiscoveryResource.java            # GET /api/v1 — HATEOAS discovery
    │   ├── SensorRoom.java                   # GET|POST /rooms, GET|DELETE /rooms/{id}
    │   ├── SensorResource.java               # GET|POST /sensors, sub-resource locator
    │   └── SensorReadingResource.java        # GET|POST /sensors/{id}/readings
    ├── exceptions/
    │   ├── RoomNotEmptyException.java        # Thrown when DELETE room has active sensors
    │   ├── LinkedResourceNotFoundException.java # Thrown when POST sensor has invalid roomId
    │   └── SensorUnavailableException.java   # Thrown when POST reading to MAINTENANCE sensor
    ├── mappers/
    │   ├── RoomNotEmptyExceptionMapper.java              # → HTTP 409 Conflict
    │   ├── LinkedResourceNotFoundExceptionMapper.java    # → HTTP 422 Unprocessable Entity
    │   ├── SensorUnavailableExceptionMapper.java         # → HTTP 403 Forbidden
    │   └── GlobalExceptionMapper.java                    # → HTTP 500 (catch-all Throwable)
    └── filters/
        └── LoggingFilter.java                # Logs method+URI (request) & status (response)
```

---

## Build & Run Instructions

### Prerequisites

- **Java 8 or higher** — verify with `java -version`
- **Maven 3.6+** — verify with `mvn -version`
- **Apache Tomcat 9** — [download here](https://tomcat.apache.org/download-90.cgi)

### Step 1 — Clone the Repository

```bash
git clone https://github.com/NuwankaFernando/CSA-CW-20232498-w2120488.git
cd CSA-CW-20232498-w2120488
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

# Linux / macOS
/path/to/tomcat/bin/startup.sh

# Windows
/path/to/tomcat/bin/startup.bat
```

**Option B — Tomcat Manager UI:**

1. Open `http://localhost:8080/manager/html`
2. Scroll to **"WAR file to deploy"**
3. Select `target/SmartCampusAPI-1.war` and click **Deploy**

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

> **Note:** The context path `/SmartCampusAPI` matches the WAR filename. Adjust if you rename the WAR.

---

## Sample curl Commands

All commands assume the server is running at `http://localhost:8080/SmartCampusAPI`.

### 1. Discovery Endpoint — `GET /api/v1`

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1 \
  -H "Accept: application/json"
```

Expected Response (200 OK)

```json
{
  "api": "Smart Campus Sensor & Room Management API",
  "version": "1.0",
  "contact": "suvin.20232498@iit.ac.lk",
  "description": "RESTful API for managing campus rooms and IoT sensors.",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

### 2. Get All Rooms — `GET /api/v1/rooms`

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Accept: application/json"
```

Expected Response (200 OK)

```json
[
  {
    "id": "LEC-301",
    "name": "Lecture Room 301",
    "capacity": 200,
    "sensorIds": ["Temp-001", "Temp-002"]
  },
  {
    "id": "LEC-302",
    "name": "Lecture Room 301",
    "capacity": 30,
    "sensorIds": []
  }
]
```

### 3. Create a New Room — `POST /api/v1/rooms`

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "LEC-304","name": "Lecture Room 304","capacity": 200}'
```

Expected Response (201 Created)

```json
{
  "id": "LEC-304",
  "name": "Lecture Room 304",
  "capacity": 200,
  "sensorIds": []
}
```

### 4. Get a Specific Room — `GET /api/v1/rooms/{roomId}`

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms/LEC-301 \
  -H "Accept: application/json"
```

Expected Response (200 OK)

```json
{
  "id": "LEC-301",
  "name": "Lecture Room 301",
  "capacity": 200,
  "sensorIds": ["Temp-001", "Temp-002"]
}
```

### 5. Delete a Room with No Sensors — `DELETE /api/v1/rooms/{roomId}`

```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LEC-304 \
  -H "Accept: application/json"
```

Expected Response (200 OK)

```json
{
  "message": "Room with id LEC-304 deleted successfully",
  "method": "OK",
  "status": 200
}
```

### 6. Attempt to Delete a Room with Active Sensors — expect `404 Not Found`

```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LEC-304 \
  -H "Accept: application/json"
```

Expected Response (404 Not Found)

```json
{
  "error": "Not Found",
  "message": "Room 'LEC-304' not found.",
  "status": 404
}
```

### 7. Get All Sensors — `GET /api/v1/sensors`

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Accept: application/json"
```

Expected Response (200 OK)

```json
[
  {
    "id": "Temp-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 30.0,
    "roomId": "LEC-301"
  },
  {
    "id": "Temp-002",
    "type": "CO2",
    "status": "MAINTENANCE",
    "currentValue": 30.0,
    "roomId": "LEC-301"
  },
  {
    "id": "Temp-003",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 30.0,
    "roomId": "LEC-301"
  }
]
```

### 8. Filter Sensors by Type — `GET /api/v1/sensors?type=Temperature`

```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=Temperature" \
  -H "Accept: application/json"
```

Expected Response (200 OK)

```json
[
  {
    "id": "Temp-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 30.0,
    "roomId": "LEC-301"
  },
  {
    "id": "Temp-003",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 30.0,
    "roomId": "LEC-301"
  }
]
```

### 9. Register a New Sensor — `POST /api/v1/sensors`

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "Temp-004","type": "CO2","status": "ACTIVE","currentValue": 10.0,"roomId": "LEC-301"}'
```

Expected Response (201 Created)

```json
{
  "id": "Temp-004",
  "type": "CO2",
  "status": "ACTIVE",
  "currentValue": 10.0,
  "roomId": "LEC-301"
}
```

### 10. Register a Sensor with Invalid roomId — expect `422 Unprocessable Entity`

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "Temp-004","type": "CO2","status": "ACTIVE","currentValue": 10.0,"roomId": "BAD-301"}'
```

Expected Response (409 Conflict)

```json
{
  "error": "Conflict",
  "message": "A sensor with ID 'Temp-004' already exists.",
  "status": 409
}
```

### 11. Get Sensor Readings — `GET /api/v1/sensors/{sensorId}/readings`

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/Temp-001/readings \
  -H "Accept: application/json"
```

Expected Response (200 OK)

```json
{
  "sensorId": "Temp-001",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 30.0,
  "roomId": "LEC-301",
  "readings": [
    {
      "id": "490c1005-67df-4263-9468-9b2808120c88",
      "timestamp": 1776918597842,
      "value": 10.0
    },
    {
      "id": "26a61c2d-d801-4c26-81a9-8783e8eba6c9",
      "timestamp": 1776918597843,
      "value": 20.0
    }
  ]
}
```

### 12. Post a New Sensor Reading — `POST /api/v1/sensors/{sensorId}/readings`

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/Temp-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":26.5}'
```

Expected Response (201 Created)

```json
{
  "id": "663d08e5-d609-4d69-abe3-a4e04ed3bc9a",
  "timestamp": 1776919489750,
  "value": 26.5
}
```

### 13. Post a Reading to a MAINTENANCE Sensor — expect `403 Forbidden`

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/Temp-002/readings \
  -H "Content-Type: application/json" \
  -d '{"value":15.0}'
```

Expected Response (403 Forbidden)

```json
{
  "error": "Forbidden",
  "message": "Sensor 'Temp-002' is under MAINTENANCE.",
  "status": 403
}
```

---

## Conceptual Report — Question Answers

---

### Part 1 — Service Architecture & Setup

#### 1.1 Project & Application Configuration

**Question:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures to prevent data loss or race conditions.

**Answer:**

JAX-RS default is a per-request lifecycle, meaning that a new instance of each resource class is created with every incoming HTTP request and immediately garbage collected. This implies that any instance level fields on a resource class are cleared with each request and cannot maintain any shared state between calls.

SensorRoomResource, SensorResource, and SensorReadingResource are all per request resources in this project. A shared in memory store is a static MockDatabase class to store data safely that is persistent across requests. The lists and maps that are declared using static keyword in MockDatabase.java reside at the class level, thus persisting across request instances.

These shared lists are then wrapped by each resource class with a generic BaseDAO T injected at construction time new BaseDAO<>(MockDatabase.ROOMS). Since Java does not copy lists, but passes references, all instances of resources are based on the same underlying data.

There is a possible race condition since ArrayList and HashMap are not thread safe. In case two requests would call ROOMS.add() at the same time, internal structural changes might corrupt the list. If two requests were to call ROOMS.add() simultaneously, internal structural modifications could corrupt the list. This would be solved in a production environment by replacing ArrayList with CopyOnWriteArrayList and HashMap with ConcurrentHashMap which offer thread-safe concurrent access with no explicit synchronisation blocks.

---

#### 1.2 The Discovery Endpoint

**Question:** Why is the provision of "Hypermedia" (HATEOAS) considered a hallmark of advanced RESTful design? How does this approach benefit client developers compared to static documentation?

**Answer:**

Level 3 of the Richardson REST Maturity Model, the highest level, is HATEOAS (Hypermedia as the Engine of Application State). It implies that API responses contain navigational links to related resources, instead of compelling clients to hard-code URLs or using external documentation solely. In this implementation, the discovery endpoint at GET /api/v1 reports a resources map, which advertises the available collections.

This has several tangible benefits to client developers. First, the client does not have to know or create URLs in advance, it just follows the links given. Second, in case the server subsequently modifies a URL path, clients which navigate using links and not hard coded strings will still work without alteration. Third, the API is self-documenting and can be explored dynamically, eliminating the possibility of clients being out of step with the server. In comparison, static documentation (e.g. a PDF spec) can get out-of-date as soon as the API evolves, and clients become invalid. HATEOAS puts the live API as the authoritative source of navigation truth.

---

### Part 2 — Room Management

#### 2.1 Room Resource Implementation

**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

**Answer:**

This implementation will send back complete Room objects in response to GET /api/v1/rooms, which contains the id, name, capacity, and the list of sensorIds of the room. This is the right option for most of the applications and the tradeoffs are as follows.

Only IDs returned generate a very small initial payload, which conserves bandwidth. But then the client will have to make one more GET /api/v1/rooms/{id} request to present any meaningful information on each individual room. It is the famous N+1 problem where N rooms there are N+1 round trips, which grows exponentially in the overall latency and server load at scale.

Repatriating complete objects is cheaper in terms of payload size per response but provides the client with everything they require in a single round trip. In the case of the common campus management dashboard that requires showing a table of rooms with names and capacities this is much more efficient.

---

#### 2.2 Room Deletion & Safety Logic

**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:**

Idempotency of REST is that repeated application of the same operation to the server yields the same state as when it is applied once. In the HTTP specification, DELETE is considered idempotent. The behavior of repeated DELETE requests with the same room ID in this implementation is:

- **First call (room exists, no active sensors):** Room is deleted out of MockDatabase.ROOMS through roomDAO.delete(room) and a 200 OK is returned with a confirmation message.
- **Following calls (room no longer exists):** roomDAO.getById(roomId) returns null, and the method throws back 404 Not Found.

All calls leave the server in the same state, and the room is not in the data store, thus meeting the idempotency guarantee at the resource state level. The difference in status codes (200 followed by 404) is normal and natural, it is only necessary that the ultimate status be the same, not that all responses to be the same. Also, the safety condition which prevents deletion of rooms containing running sensors (throwing RoomNotEmptyException: HTTP 409) is verified prior to deletion, such that the data store is never left in an orphaned state.

---

### Part 3 — Sensor Operations & Linking

#### 3.1 Sensor Resource & Integrity

**Question:** We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as `text/plain` or `application/xml`. How does JAX-RS handle this mismatch?

**Answer:**

The @Consumes(MediaType) .The annotation of the POST /api/v1/sensors method of SensorResource.java is (APPLICATION_JSON) which tells JAX-RS to accept only requests with a Content-Type header of application/json. When a client submits a request with a Content-Type: text/plain or Content-Type: application/xml, JAX-RS rejects it prior to invocation of the resource method. The content negotiation layer of the framework matches the received Content-Type to the list of accepted media types, and, not finding a match, sends an automatic response of HTTP 415 Unsupported Media Type. This is the reason why no custom exception handling code is needed. It is completely controlled by JAX-RS runtime. This is a major advantage of declarative media-type annotations, the resource method body is never entered with an invalid or unforeseen input, and offers a clean, safe contract between client and server. Only application/json payloads are matched by Jackson message body reader, which guarantees type-safe deserialisation of the Sensor POJO. But in the present program, a generic error handler that catches all the unexpected exceptions does override this behavior, and the program will send an HTTP 500 Internal Server Error rather than the desired 415 Unsupported Media Type response.

---

#### 3.2 Filtered Retrieval & Search

**Question:** You implemented this filtering using `@QueryParam`. Contrast this with an alternative design where the type is part of the URL path (e.g., `/api/v1/sensors/type/CO2`). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer:**

The path-based design is semantically unsound in REST design since URL path segments are supposed to designate a specific resource or sub resource. /sensors/type/CO2 suggests that there is a resource literally at that path with a specific, uniquely addressable entity. This is not a discrete named resource; we are asking for a filtered view of the sensors collection.

The semantically sound approach of the query parameter is that query parameters are optional, non-identifying modifiers used on a resource collection. It also has graceful scaling; one can combine multiple filters in a natural way (?type=CO2&status=ACTIVE) without specifying a new pattern of URLs to each combination. It also has good support of HTTP catching infrastructure and makes it clear to API consumers that the response is a dynamic filtered view, not a fixed sub resource. Path parameters, on the contrary, are intended to be used to identify resources (/sensors/Temp-001), rather than to filter.

---

### Part 4 — Deep Nesting with Sub-Resources

#### 4.1 The Sub-Resource Locator Pattern

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?

**Answer:**

The fundamental advantage is Single Responsibility: a single logical resource is dealt with by each class. SensorResource handles sensor CRUD; SensorReadingResource handles historical readings of a sensor in a sensor context. Each of the classes does not know the inner logic of the other.

In the absence of this pattern, one controller class would have to deal with GET /sensors, POST /sensors, GET /sensors/{id}, GET /sensors/{id}/readings, POST /sensors/{id}/readings and possibly additional nested routes. This is a God class as the API expands, which is hard to read, test and maintain.

Context injections are also supported by the locator pattern, the sensorId path parameter is checked in the locator method (404 returned in case the sensor does not exist) and then cleanly injected into the SensorReadingResource constructor. This implies that the sub resource never has to re-authenticate the parent; it can assume that it has a valid sensorId. The outcome is a more isolated separation of concerns, more testable in isolation, and a codebase that can scale horizontally with the addition of new nested resources.

---

### Part 5 — Error Handling, Exception Mapping & Logging

#### 5.1 Dependency Validation (422 Unprocessable Entity)

**Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:**

When a client POSTs a new sensor with a `roomId` that does not exist, this implementation throws `LinkedResourceNotFoundException`, which is mapped to **HTTP 422 Unprocessable Entity**.

In the event that a client POSTs a new sensor with a roomId that is non-existent, this implementation will raise LinkedResourceNotFoundException which is handled by LinkedResourceNotFoundExceptionMapper, which will map to HTTP 422 Unprocessable Entity.

The HTTP 404 Not Found is a response that indicates that the URL itself was not found and the endpoint is not present on the server. The endpoint POST /api/v1/sensors in this case is fully legitimate and valid. The 404 response would be a false alarm to the client that the API path is not correct.

The semantics of the HTTP 422 Unprocessable Entity are correct since, the request is syntactically correct and correctly decoded as JSON, the URL and the HTTP method are correct, but the semantics of the payload does not pass a business logic validation rule namely, the roomId field refers to a resource that is not present in the system. The server knows what the request is but is unable to execute it because of some logical constraint in the data. This informs the client on what to correct the payload content, not the URL.

---

#### 5.2 The Global Safety Net (500)

**Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

**Answer:**

The GlobalExceptionMapper handles all unchecked Throwables and responds with a safe, generic 500 Internal Server Error JSON response, so no raw stack trace will ever be sent to the client. The actual exception is only recorded on the server side to developers. The following security risks would be created by exposing stack traces:

- **Technology fingerprinting:** A stack trace shows the precise library names and versions (e.g., jersey-server-2.32, jackson-databind-2.10.1). The attacker can cross-check them with publicly available CVE databases to determine the known vulnerabilities in those specific versions and develop specific exploits.

- **Internal path disclosure:** Stack traces contain complete qualified file paths on the server (e.g, /home/deploy/SmartCampusAPI/src/...), which exposes the directory layout and deployment setup of the server, which can be further used in other attacks like path traversal.

- **Business logic exposure:** Class names, method names, and the complete chain of calls can be used to show the internal architecture and logic flow of the application and allow an attacker to reverse-engineer the system and find vulnerabilities or unguarded code paths.

- **Exception type leakage:** A NullpointerException on a given line number informs an attacker that a given input resulted in a null dereference, which they can use repeatedly or to explore the input validation limits of the API.

---

#### 5.3 API Request & Response Logging Filters

**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting `Logger.info()` statements inside every single resource method?

**Answer:**

It would be a breach of the Don't Repeat Yourself (DRY) principle and Single Responsibility Principle to manually insert Logger.info() calls in each resource method. The individual resources would have to be modified separately to add, modify, or delete logging of a maintenance burden that increases with the addition of each new endpoint. It is also prone to errors a developer that adds a new endpoint may forget to add the logging call, and this will introduce blind spots in observability.

The filter approach considers logging as a real cross-cutting issue, which is applicable to all endpoints. Switching logging approach (e.g. replacing logging with SLF4J or adding correlation IDs) only needs a change in a single location. This renders the system more maintainable, regular, and in line with industry standard AOP (Aspect Oriented Programming) design principles.

---
