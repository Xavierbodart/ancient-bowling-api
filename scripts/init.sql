CREATE TABLE players
(
    id                INT PRIMARY KEY AUTO_INCREMENT,
    name              VARCHAR(255)                                                    NOT NULL,
    birthday          DATE,
    creation_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    modification_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE games
(
    id                        INT PRIMARY KEY AUTO_INCREMENT,
    name                      VARCHAR(255)                                                    NOT NULL,
    status                    VARCHAR(50)                                                     NOT NULL,
    active_playing_session_id INT,
    winning_playing_session_id INT,
    creation_date             TIMESTAMP DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    modification_date         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE playing_sessions
(
    id                INT PRIMARY KEY AUTO_INCREMENT,
    game_id           INT                                                             NOT NULL,
    player_id         INT                                                             NOT NULL,
    status            VARCHAR(50)                                                     NOT NULL,
    score             int       default 0                                             NOT NULL,
    creation_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    modification_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (game_id) REFERENCES games (id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players (id) ON DELETE CASCADE
);

ALTER TABLE games
    ADD CONSTRAINT FOREIGN KEY (active_playing_session_id) REFERENCES playing_sessions (id) ON DELETE NO ACTION;
ALTER TABLE games
    ADD CONSTRAINT FOREIGN KEY (winning_playing_session_id) REFERENCES playing_sessions (id) ON DELETE NO ACTION;

CREATE TABLE frames
(
    id                     INT PRIMARY KEY AUTO_INCREMENT,
    playing_sessions_id    INT                                                             NOT NULL,
    status                 VARCHAR(50)                                                     NOT NULL,
    frame_number           INT                                                             NOT NULL,
    score                  INT       DEFAULT 0                                             NOT NULL,
    remaining_bonus_throws INT,
    bonus_points           INT       DEFAULT 0                                             NOT NULL,
    remaining_pins         INT                                                             NOT NULL,
    creation_date          TIMESTAMP DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    modification_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (playing_sessions_id) REFERENCES playing_sessions (id) ON DELETE CASCADE
);

CREATE TABLE throws
(
    id                INT PRIMARY KEY AUTO_INCREMENT,
    frame_id          INT,
    type              VARCHAR(50),
    pins_knocked      INT,
    creation_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP                             NOT NULL,
    modification_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (frame_id) REFERENCES frames (id) ON DELETE CASCADE
);

INSERT INTO players (name, birthday) VALUES
('Player 1', '1990-05-15'),
('Player 2', '1985-10-20'),
('Player 3', '2000-07-01'),
('Player 4', '1995-02-14'),
('Player 5', '1988-12-25');

-- Insert games
INSERT INTO games (name, status) VALUES
('Game 1', 'INITIALIZED'),
('Game 2', 'INITIALIZED'),
('Game 3', 'INITIALIZED');