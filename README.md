# Echoes of Memory

## Description
Echoes of Memory is a web-based memory game designed to enhance memory skills. The game involves flipping over pairs of cards, with the objective to find all matching pairs. Users need to register with their email, username, and password before playing.

The game offers:
- **Single-player mode:** 6 difficulty levels with different time limits.
- **Multiplayer mode:** 3 difficulty levels, where players take turns and the one with more matched pairs wins.

Players can also view their game statistics and rankings.

## Application Structure

The Echoes of Memory application is structured using the Model-View-Controller (MVC) architecture with an additional service layer. Below is an overview of each layer:

### View Layer
- **Implemented in:** `memo-react-app`
- **Responsibility:** Handles the user interface and interactions.

### Model Layer
- **Implemented in:** `memo-java-spring-app`
- **Entities**
  - `MemoUser`: Represents a user.
  - `MemoSingleGame`: Represents a single-player game session.
  - `MemoMultiGame`: Represents a multiplayer game session.
- **Repositories:**
  - `MemoUserRepository`
  - `MemoSingleGameRepository`
  - `MemoMultiGameRepository`
- **Game Logic:**
  - `MemoGame` (abstract)
  - `SinglePlayer`
  - `MultiPlayer`

### Service Layer
- **Implemented in:** `memo-java-spring-app`
- **Location:** `src/main/java/com/memo/game/service`
- **Key Services:**
  - `UserService`: Manages user operations.
  - `SinglePlayerService`: Handles single-player game operations.
  - `MultiPlayerService`: Manages multiplayer game operations.
  - `SinglePlayerStatService`: Provides statistics for single-player games.
  - `MultiPlayerStatService`: Provides rankings for multiplayer games.
  - `TokenService`: Manages JWT token operations.
  - `TokenBlackListService`: Manages blacklisted tokens.

### Controller Layer
- **Implemented in:** `memo-java-spring-app`
- **Location:** `src/main/java/com/memo/game/controller`
- **Key Controllers:**
  - `AuthController`: Handles authentication and user management.
  - `SinglePlayerController`: Manages single-player game operations.
  - `MultiPlayerController`: Manages multiplayer game interactions.
  - `StatisticsController`: Provides game statistics.
  - `MessageController`: Handles WebSocket communication for real-time interactions.

## API Endpoints

### AuthController
- **POST** `/api/register`: Register a new user with email, username, and password.
- **POST** `/api/signIn`: Sign in with email or username and password.
- **POST** `/api/getUserInfo`: Retrieve user information using JWT token.

### SinglePlayerController
- **POST** `/api/singlePlayer/startSinglePlayer`: Start a new single-player game with specified pairs and initial time.
- **POST** `/api/singlePlayer/getRemainingTime/{sessionId}`: Get remaining time for a game session.
- **POST** `/api/singlePlayer/getCard/{sessionId}`: Reveal a card at a given index.
- **POST** `/api/singlePlayer/leaveGame/{sessionId}`: Leave a specified game session.
- **POST** `/api/singlePlayer/isPlayValid/{sessionId}`: Check if a play is valid.

### StatisticsController
- **POST** `/api/singlePlayerStatistics/all`: Retrieve paginated single-player session statistics.
- **POST** `/api/singlePlayerStatistics/summarized`: Get summarized statistics of single-player games.
- **POST** `/api/multiPlayerStatistics`: Get rankings for multiplayer sessions.

### MessageController
- **WebSocket Routes:**
  - **`/game.join`**: Handle joining a multiplayer game.
  - **`/game.leave`**: Handle leaving a multiplayer game.
  - **`/game.move`**: Perform a move in a multiplayer game.

## System Requirements
The application can run on a local server (personal computer) or a web server.

### Backend Requirements:
- Spring Boot 3.2.2
- Java 17 or newer
- Required libraries:
  - Spring Boot Starter Data JPA
  - Spring Boot Starter Web
  - Spring Boot Starter Test
  - PostgreSQL JDBC Driver
  - jBCrypt
  - Java JWT
  - Spring Boot Starter WebSocket

### Database:
- PostgreSQL 16.0 or newer

Recommended: At least 1 GB of RAM and 1 GB of free disk space. Compatible with Windows, Linux, or Mac operating systems for local installation.

## Installation

### Local Installation

#### Required Software Installation

1. **Java Development Kit (JDK)**
   - Download from Oracle JDK or AdoptOpenJDK and follow the installation instructions. Ensure Java is added to the system environment variables. Verify the installation with:
   ```powershell
   java -version
   ```

2. **PostgreSQL Database**
   - Download from the PostgreSQL website and follow the installation instructions. Verify the installation with:
   ```powershell
   psql -version
   ```

#### PostgreSQL Database Setup

1. Open the PostgreSQL application (e.g., DbBeaver) and start the PostgreSQL service. Create a database and user with:
    ```sql
    CREATE DATABASE yourdatabase;
    CREATE USER yourusername WITH ENCRYPTED PASSWORD 'yourpassword';
    GRANT ALL PRIVILEGES ON DATABASE yourdatabase TO yourusername;
    ```
#### Backend Setup (Spring Boot)

1. Install dependencies with:
      ```powershell
      gradlew.bat build
      ```

2. Configure application.properties or application.yml:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/yourdatabase
    spring.datasource.username=yourusername
    spring.datasource.password=yourpassword
    ```
## Run the application:
  ```powershell
  gradlew.bat bootRun
  ```
    
- For the first run, use:
    ```powershell
    gradlew.bat bootRun --args="init-db"
    ```
