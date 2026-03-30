# 🎮 GameGrid — Online Video Game Catalog

A web platform for discovering video games, reading industry news, and connecting with other gamers. Users can browse and search a game catalog, write reviews, track what they've played, and follow other players.

---

## Screenshots (in bulgarian)

*Home Page*
<img width="1005" height="498" alt="image" src="https://github.com/user-attachments/assets/f1eb971f-feca-46ae-b976-903cb8dc0f7f" />

*Profile Page*
<img width="1005" height="501" alt="image" src="https://github.com/user-attachments/assets/363dbd24-9d04-4f78-bea2-1999bd83aa02" />

*Article Creation Modal*
<img width="1005" height="570" alt="image" src="https://github.com/user-attachments/assets/7683c01a-9a1a-457b-bdfe-8defe0a56bee" />

---

## Features

**For all users:**
- Search and filter games by genre, studio, release year, price, and platform
- Rate games and write/edit reviews
- Track played games and mark favorites
- Follow other users and see their activity
- Real-time chat with other users
- Notifications for followed users' activity
- Public user profiles

**For super users (admins):**
- Add and edit games in the catalog
- Add and edit news articles

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java / Quarkus |
| Frontend | React / JavaScript |
| Primary Database | PostgreSQL (containerized) |
| Graph Database | Neo4j (containerized) — handles social features |
| Auth | Keycloak (containerized) + JWT |
| Real-time | Quarkus WebSockets + React WebSocket Client |
| Public Tunnel | Ngrok |

---

## Getting Started

> Make sure you have Docker, Java, and Node.js installed before proceeding.

**1. Clone the repository**
```bash
git clone https://github.com/your-username/gamegrid.git
cd gamegrid
```

**2. Start the containers**
```bash
docker-compose up -d
```
This starts PostgreSQL, Neo4j, and Keycloak.

**3. Run the backend**
```bash
cd backend
./mvnw quarkus:dev
```

**4. Run the frontend**
```bash
cd frontend
npm install
npm start
```

**5. (Optional) Expose publicly with Ngrok**
```bash
ngrok http 3000
```
