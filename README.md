# Ancient Bowling Game API - README

## Overview

The **Ancient Bowling Game API** is a backend application designed to manage an ancient bowling game. It provides endpoints for managing players, games, playing sessions, frames, and throws. The game introduces unique rules for bowling, including a customizable number of pins, frames, and bonus throws.

## How the Game Works

1. **Game Initialization**:
    - A game starts in the `INITIALIZED` state. Only initialized games can transition to the `IN_PROGRESS` state.
    - Players can be added to a game via **playing sessions** while the game is in the `INITIALIZED` state.

2. **Playing Sessions**:
    - A playing session represents a player's participation in the game.
    - Playing sessions can only be added when the game is `INITIALIZED`.
    - Once the game is started, playing sessions progress through their lifecycle (`INITIALIZED` → `IN_PROGRESS` → `ACTIVE` → `FINISHED`). `ACTIVE` state means this is the session turn to play.

3. **Game Flow**:
    - Players take turns throwing in frames.
    - Frames and throws follow specific rules:
        - A frame starts in the `CREATED` state and progresses as throws are made.
        - Bonus throws are awarded for strikes and spares.
    - The game ends when all frames and throws are completed for all sessions.

4. **Throw Rules**:
    - Players throw balls to knock down pins.
    - **Throw types** include:
        - `REGULAR`: A standard throw.
        - `SPARE`: When all remaining pins are knocked down within the allowed throws for a frame.
        - `STRIKE`: When all pins are knocked down on the first throw.
    - Bonus throws are awarded based on strikes and spares:
        - `STRIKE`: +3 bonus throws.
        - `SPARE`: +2 bonus throws.

5. **Customization Parameters**:
    - `ancient.bowling.numberOfPins`: Total pins in a frame (default: 15).
    - `ancient.bowling.numberOfFrames`: Total frames in a game (default: 5).
    - `ancient.bowling.numberOfThrowsPerFrame`: Maximum throws per frame (default: 3).
    - `ancient.bowling.strikeBonusThrows`: Bonus throws for a strike (default: 3).
    - `ancient.bowling.spareBonusThrows`: Bonus throws for a spare (default: 2).

---

## API Documentation

### Players Controller

- **GET /players/{playerId}**  
  Retrieve player details by ID.

- **PUT /players/{playerId}**  
  Update player details by ID.

- **POST /players**  
  Create a new player.

---

### Games Controller

- **GET `/games`**  
  Retrieve a list of all games.

- **POST `/games`**  
  Create a new game.

- **POST `/games/{gameId}/start`**  
  Start a game. The game must be in the `INITIALIZED` state.

- **GET `/games/{gameId}/playing-sessions`**  
  Retrieve all playing sessions for a specific game.

- **POST `/games/{gameId}/playing-sessions`**  
  Add a playing session to a game in the `INITIALIZED` state.

- **POST `/games/{gameId}/playing-sessions/{playingSessionId}/throws`**  
  Add a throw for a specific playing session in a game.

- **GET `/games/{gameId}`**  
  Retrieve details of a specific game.

- **GET `/games/{gameId}/playing-sessions/{playingSessionId}`**  
  Retrieve details of a specific playing session in a game.

---

## Game and Session States

### Game Status Flow
**INITIALIZED → IN_PROGRESS → FINISHED**

### Playing Session Status Flow
**INITIALIZED → IN_PROGRESS → ACTIVE → FINISHED**

ACTIVE -> IN_PROGRESS: next session turn to play

### Frame Status Flow
**CREATED → COMPLETED → CLOSED**
**CREATED → EXTENDED → CLOSED** (for the last frame)

CREATED -> COMPLETED: all throws are made for the frame. It is waiting for bonus points
CREATED -> EXTENDED: the last frame is completed. It is not closed due to extended bonus throws 
---

## Example Workflow

1. **Create Players**:
    - Use the `/players` endpoint to create players.

2. **Create a Game**:
    - Use `/games` to create a new game. The game starts in the `INITIALIZED` state.

3. **Add Playing Sessions**:
    - Use `/games/{gameId}/playing-sessions` to add playing sessions to the initialized game.

4. **Start the Game**:
    - Use `/games/{gameId}/start` to transition the game to the `IN_PROGRESS` state.

5. **Play the Game**:
    - Players take turns making throws via `/games/{gameId}/playing-sessions/{playingSessionId}/throws`.
    - Frames and throws are tracked, and bonus throws are awarded as per the rules.

6. **Game Completion**:
    - When all frames are completed, the game is marked as `FINISHED`.
