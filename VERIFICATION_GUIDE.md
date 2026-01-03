# Verification Guide

This guide provides step-by-step instructions to verify the application against the specific requirements.

> [!TIP]
> All scenarios below can also be tested interactively using the **Swagger UI** at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html).


## Prerequisites
1. Ensure the application is running:
   ```bash
   mvn spring-boot:run
   ```
2. The application uses an in-memory database. Restarting the app resets all data.
3. Pre-defined users loaded on startup:
    - **Authorized** (Can start sessions): `alice`, `charlie`
    - **Unauthorized** (Cannot start sessions): `bob`, `dave`

---

## Scenario 1: Global Session & Random Selection
**Requirements Covered:**
- Req 1: User input restaurant.
- Req 2, 4: Random selection.
- Req 3: Multiple users submit.
- Req 4: No submissions after selection.

### 1. Submit restaurants (Alice & Bob)
Users submit their choices to the default Global Session (ID: 0).

```bash
# Alice submits "Pasta Place"
curl -X POST "http://localhost:8080/restaurant/submit" \
  -H "Content-Type: application/json" \
  -H "X-Username: alice" \
  -d "{\"name\": \"Pasta Place\"}"

# Bob submits "Burger Joint"
curl -X POST "http://localhost:8080/restaurant/submit" \
  -H "Content-Type: application/json" \
  -H "X-Username: bob" \
  -d "{\"name\": \"Burger Joint\"}"
```
**Expected:** `201 Created` for both.

### 2. Get Random Choice
Any user requests a random choice.
> [!NOTE]
> **Global Session Exception**: For the **Global Session (0)**, ANY user can request the random restaurant. For custom sessions, ONLY the session creator can do this.


```bash
curl -X GET "http://localhost:8080/restaurant/random" \
  -H "X-Username: bob"
```
**Expected:** Returns one of the submitted restaurants (e.g., `{"name": "Pasta Place", ...}`).

### 3. Verify Session Closure
Try to submit another restaurant after selection.

```bash
curl -X POST "http://localhost:8080/restaurant/submit" \
  -H "Content-Type: application/json" \
  -H "X-Username: alice" \
  -d "{\"name\": \"Late Soup\"}"
```
**Expected:** `400 Bad Request` with message indicating session is closed.

---

## Scenario 2: Custom Sessions & Permissions
**Requirements Covered:**
- Req 5: Multiple sessions, invites.
- Req 6: Only pre-defined users can initiate.

### 1. Unauthorized Creation Attempt (req 6)
`bob` (unauthorized) tries to submit to a NEW session (ID: 100). Submitting to a non-existent session ID initiates it.

```bash
curl -X POST "http://localhost:8080/restaurant/submit" \
  -H "Content-Type: application/json" \
  -H "X-Username: bob" \
  -d "{\"sessionId\": 999, \"sessionName\": \"Bob's Pizza\", \"name\": \"Bob's Pizza\"}"
```
**Expected:** `400 Bad Request` (Bob cannot initiate sessions).

### 2. Authorized Creation (req 5, 6)
`alice` (authorized) initiates a session. She attempts to submit to a non-existent ID (e.g., 999), which triggers the creation of a **NEW** session.

> [!IMPORTANT]
> The `sessionId` passed (999) is ignored for the *new* session's ID. The database auto-generates a new ID (e.g., `1`).
> **Check the JSON response** to get the actual `id` of the created session.

```bash
curl -X POST "http://localhost:8080/restaurant/submit" \
  -H "Content-Type: application/json" \
  -H "X-Username: alice" \
  -d "{\"sessionId\": 999, \"sessionName\": \"Alice's Session\", \"name\": \"Alice's Salad\"}"
```
**Expected:** `201 Created`. Response body will contain the new session structure (e.g., `session: { "id": 1, ... }`).

### 3. Invite Guests (req 5)
Alice invites `dave` to the session.
> **Note**: Replace `1` in the URL/body below with the **actual Session ID** from the previous step.

```bash
curl -X POST "http://localhost:8080/session/invite" \
  -H "Content-Type: application/json" \
  -H "X-Username: alice" \
  -d "{\"sessionId\": 1, \"usernames\": [\"dave\"]}"
```
**Expected:** `200 OK`.

### 4. Invited Guest Submission (req 5)
`dave` submits to the session.

```bash
curl -X POST "http://localhost:8080/restaurant/submit" \
  -H "Content-Type: application/json" \
  -H "X-Username: dave" \
  -d "{\"sessionId\": 1, \"name\": \"Dave's Tacos\"}"
```
**Expected:** `201 Created`.

### 5. Uninvited Guest Rejection (req 5)
`charlie` (authorized, but not invited to *this* session) tries to submit to the session.

```bash
curl -X POST "http://localhost:8080/restaurant/submit" \
  -H "Content-Type: application/json" \
  -H "X-Username: charlie" \
  -d "{\"sessionId\": 1, \"name\": \"Charlie's Steak\"}"
```
**Expected:** `400 Bad Request`.


---

## Resetting
To re-run scenarios, you can restart the application or use the reset endpoint on the session to reopen the session for submissions:

```bash
curl -X PATCH "http://localhost:8080/session/{sessionId}/reset"
```
