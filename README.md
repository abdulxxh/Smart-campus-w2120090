# Smart Campus API

This project provides a robust, scalable backend for the University's "Smart Campus" initiative, managing Rooms and Sensors, and tracking real-time/historical telemetry reading data. 
Built in Java with JAX-RS (Jakarta RESTful Web Services) utilizing the Jersey framework and Grizzly HTTP server, focusing on thread-safe architectures and robust exception mapping.

## Setup and Launch Instructions

### Prerequisites
- [Java Development Kit (JDK) 11](https://adoptium.net/) or higher
- [Apache Maven](https://maven.apache.org/)

### Building the Project
From the root directory where `pom.xml` belongs, run:
```bash
mvn clean install
```

### Launching the Server
Start the embedded Grizzly server by executing:
```bash
mvn exec:java
```
The application will bootstrap, and the main Rest API endpoint will be exposed on `localhost`:
`http://localhost:8080/api/v1`

---

## Example Interactions (cURL Commands)

### 1. Discovery API Metadata
```bash
curl -X GET http://localhost:8080/api/v1
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{"name": "Computing Lab", "capacity": 30}' \
-v
```

### 3. Create a Sensor (Must specify valid roomId from Step 2)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{"type": "CO2", "roomId": "RM-123456789", "currentValue": 0}' \
-v
```
*(Replace `RM-123456789` with the ID obtained from Step 2)*

### 4. Search Sensors by Type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 5. Add a Sensor Reading (using Sub-Resource Locator)
```bash
curl -X POST http://localhost:8080/api/v1/sensors/SENS-987654321/readings \
-H "Content-Type: application/json" \
-d '{"value": 415.5}' \
-v
```
*(Replace `SENS-987654321` with the Sensor ID obtained from Step 3)*

### 6. Delete a Room (Conflict Error check if room has sensors)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/RM-123456789 -v
```

---

## Coursework Report Answers

### 1. Service Architecture & Setup: JAX-RS Resource Lifecycle and Thread Safety
**Question:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

**Answer:** By default, JAX-RS Resource classes are instantiated strictly per-request. A new Resource instance is dynamically created to handle each incoming HTTP request, and it is subsequently destroyed. Consequently, instance variables inside these controllers cannot persist data across multiple requests. To guarantee data persists globally while the application runs, we must decouple the data layer (using static fields, a Singleton repository, or an external database). In a multi-threaded service handling concurrent HTTP requests using a global static container, race conditions are a major risk. To prevent data corruption or `ConcurrentModificationException`s, synchronization is mandatory. Thus, this architectural pattern dictates the adoption of thread-safe collections, such as `ConcurrentHashMap`, guaranteeing atomic operations while scaling effectively under heavy load.

### 2. The Discovery Endpoint: Benefits of HATEOAS
**Question:** Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer:** HATEOAS (Hypermedia As The Engine Of Application State) allows consumers to dynamically navigate the API purely through hypermedia links embedded within payloads rather than hard-coding static URI structures logic on the client-side. This decoupling mechanism benefits client developers immensely; if a route hierarchy changes on the server, the consumer code (if programmed correctly) seamlessly adapts as it continuously discovers the correct URIs at runtime. By serving as an organic, self-documenting routing table, HATEOAS vastly improves backward compatibility and simplifies the client's architecture compared to consulting out-of-date static documentation files.

### 3. Room Resource Implementation: Validating Return Types
**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

**Answer:** Returning strictly identifiers minimizes JSON payload sizes greatly, vastly mitigating network bandwidth saturation when retrieving massive collections (thousands of Rooms). However, this creates an "N+1" problem: an application requiring detailed properties to render a UI would be forced to fire hundreds of separate GET requests per single identifier, heavily penalizing total latency and draining server connections. Conversely, returning entire serialized Room objects in a single list demands massive bandwidth and server-side memory serialization but eliminates subsequent client-side fetches. A superior hybrid compromise usually involves returning partial views (summaries containing ID and Name mappings) and exposing an endpoint to lazily fetch exhaustive properties individually as needed.

### 4. Room Deletion & Safety Logic: Deletion Idempotency
**Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer:** Yes, the DELETE endpoint is functionally idempotent. An API operation is classified as idempotent if processing it once produces the exact equivalent server-side state as processing it countless identical times safely. If a consumer sends a valid DELETE requesting to remove Room `LIB-100`, the server eliminates it from the `ConcurrentHashMap` and replies with `204 No Content`. If the very identical DELETE action broadcasts repeatedly, the server will check for `LIB-100`, fail to find it, and gracefully return a `404 Not Found`. Throughout these secondary identical requests, the fundamental underlying server state—meaning the Room `LIB-100` remains erased—maintains complete consistency. 

### 5. Sensor Resource & Integrity: @Consumes Mechanics
**Question:** We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

**Answer:** Utilizing `@Consumes(MediaType.APPLICATION_JSON)` formally commands the JAX-RS configuration ruleset to only pass HTTP pipelines structured rigorously as a valid JSON payload stream. Should an erroneous client attempt pushing a mismatched data format (e.g., `text/plain` or `application/xml`) into the POST parameter mapping, the underlying JAX-RS framework intervenes utilizing Content-Negotiation based heavily upon standard `Content-Type` headers. Since the server immediately identifies an incompatible, un-consumable payload stream prior to hitting internal business logic paths, the Servlet Container halts the request efficiently, declining deserialization, and automatically issuing a standardized `415 Unsupported Media Type` failure response safely. 

### 6. Filtered Retrieval & Search: Path vs Query Parameter
**Question:** You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer:** URL Path Parameters structurally characterize unique base collections natively or designate specifically precise, nested individual entities (`/sensors/123`). Alternatively, Query Parameters are conceptually designed to augment functionality, acting strictly as dynamic filters, sort mechanisms, or state modifiers over a static aggregate base resource. Utilizing queries (`/sensors?type=CO2`) preserves `/sensors` logically mapping correctly as our primary plural REST resource context object. Conversely, baking filters into Paths (`/sensors/type/CO2`) causes rigid router sprawl, exponentially complicating URL matching expressions while preventing flexible, combined compound filtering (`?type=CO2&status=OFFLINE`) without absurd endpoint over-engineering. 

### 7. Deep Nesting with Sub-Resources: Sub-Resource Locator Pattern
**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

**Answer:** Building monolithic root controllers that exhaustively define hundreds of nested, deeply tiered sub-paths entirely inside of a single class actively violates the Single Responsibility Principle, forcing extreme structural fragility globally. The explicit Sub-Resource Locator Pattern rectifies this scaling crisis seamlessly by deferring route processing contexts locally. The main `SensorResource` exclusively interprets broad `/sensors` level transactions natively; upon sensing an inbound `/readings` URI fragment, it intelligently instantiates and returns a dedicated, nested `SensorReadingResource`. This architectural delegation significantly simplifies codebase testing mechanisms, enforces highly modular contexts, isolates feature dependencies perfectly, and produces readable, lightweight domain class files easily navigated by developers.

### 8. Dependency Validation: Understanding Contextual HTTP 422
**Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer:** A conventional `404 Not Found` implies primarily that the base routing URI mapping essentially does not exist internally (e.g., `/api/wrong-route`); thus, providing it implies an environmental routing failure. In this exact payload scenario, the URI target endpoint (`POST /sensors`) functioned phenomenally and dynamically parsed out the valid JSON successfully natively. The distinct breakdown lies structurally inside the payload's semantic integrity: the foreign `roomId` reference provided references an unregistered parent identifier inside our framework. Returning a `422 Unprocessable Entity` relays with immaculate precision that the server natively comprehended the perfectly structured metadata completely, but mathematically could not process the localized business instructions derived from those exact fields securely. 

### 9. The Global Safety Net: Stack Trace Leakage Cybersecurity
**Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

**Answer:** Broadcasting comprehensive, un-obfuscated raw Java runtime stack traces externally operates functionally as critical backend Information Disclosure to bad actors. Savvy assailants comb these detailed server dumps to fingerprint exact hardware architectures globally. Stack traces effortlessly relinquish root folder directory footprints natively, internal framework class definitions cleanly, precise 3rd-party library names + specific software version configurations freely (which are instantly pivotable cross-referenced natively against active CVE vulnerability exploit databases efficiently). Furthermore, traces outline SQL connection pathways perfectly, giving an attacker an immaculate structural roadmap precisely tailored to bypass blind probing entirely and launch pinpoint exploitation campaigns seamlessly. 

### 10. API Request & Response Logging: JAX-RS Filter Value Context
**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

**Answer:** Executing cross-cutting functional requirements natively through globally configured JAX-RS container proxy interceptor pipelines enforces optimal component decoupling natively. Manually pasting `Logger.info()` logic uniformly directly into hundreds of diverse Resource endpoint blocks massively corrupts domain purity naturally, violating the foundational "Don't Repeat Yourself" (DRY) principles extensively. This boilerplate method causes intense scaling regressions natively—developers often structurally forget incorporating statements on new deployments naturally. Operating standardized `ContainerRequestFilter` logic centralizes the auditing footprint holistically; the container reliably wraps requests universally identically cleanly, significantly lightening the class files naturally, guaranteeing error-free runtime oversight.
