CREATE TABLE Season (
  idSeason INTEGER PRIMARY KEY AUTOINCREMENT,
  name VARCHAR(255) NULL
);

CREATE TABLE Team (
  idTeam INTEGER PRIMARY KEY AUTOINCREMENT,
  Season_idSeason INTEGER UNSIGNED NOT NULL,
  name VARCHAR(20) NULL,
  FOREIGN KEY(Season_idSeason)
    REFERENCES Season(idSeason)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

CREATE INDEX Team_FKIndex1 on Team(Season_idSeason);

CREATE TABLE Game (
  idGame INTEGER PRIMARY KEY AUTOINCREMENT,
  Season_idSeason INTEGER UNSIGNED NOT NULL,
  Team_idTeam INTEGER UNSIGNED NOT NULL,
  Team_Season_idSeason INTEGER UNSIGNED NOT NULL,
  date DATE NOT NULL,
  time TIME NULL,
  locationName VARCHAR(45) NULL,
  locationState TEXT NULL,
  locationCity VARCHAR(20) NULL,
  locationStreet VARCHAR(45) NULL,
  locationStreetNo VARCHAR(20) NULL,
  FOREIGN KEY(Season_idSeason)
    REFERENCES Season(idSeason)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION,
  FOREIGN KEY(Team_idTeam, Team_Season_idSeason)
    REFERENCES Team(idTeam, Season_idSeason)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

CREATE INDEX Game_FKIndex1 on Game(Season_idSeason);
CREATE INDEX Game_FKIndex2 on Game(Team_idTeam, Team_Season_idSeason);

CREATE TABLE Player (
  idPlayer INTEGER PRIMARY KEY AUTOINCREMENT,
  Team_idTeam INTEGER UNSIGNED NOT NULL,
  Team_Season_idSeason INTEGER UNSIGNED NOT NULL,
  name VARCHAR(255) NULL,
  jerseyNumber SMALLINT UNSIGNED NULL,
  photo BLOB NULL,
  isCoach BOOL NOT NULL,
  phoneNumber VARCHAR NULL,
  emailAddress VARCHAR(45) NULL,
  parentsNames VARCHAR(255) NULL,
  FOREIGN KEY(Team_idTeam, Team_Season_idSeason)
    REFERENCES Team(idTeam, Season_idSeason)
      ON DELETE CASCADE
      ON UPDATE NO ACTION,
  FOREIGN KEY(Team_idTeam, Team_Season_idSeason)
    REFERENCES Team(idTeam, Season_idSeason)
      ON DELETE CASCADE
      ON UPDATE NO ACTION
);

CREATE INDEX Player_FKIndex1 on Player(Team_idTeam, Team_Season_idSeason);
CREATE INDEX Player_FKIndex2 on Player(Team_idTeam, Team_Season_idSeason);

CREATE TABLE GamePhoto (
  idGamePhoto INTEGER PRIMARY KEY AUTOINCREMENT,
  Game_Team_Season_idSeason INTEGER UNSIGNED NOT NULL,
  Game_Team_idTeam INTEGER UNSIGNED NOT NULL,
  Game_Season_idSeason INTEGER UNSIGNED NOT NULL,
  Game_idGame INTEGER UNSIGNED NOT NULL,
  photo BLOB NULL,
  FOREIGN KEY(Game_idGame, Game_Season_idSeason, Game_Team_idTeam, Game_Team_Season_idSeason)
    REFERENCES Game(idGame, Season_idSeason, Team_idTeam, Team_Season_idSeason)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

CREATE INDEX GamePhoto_FKIndex1 on GamePhoto(Game_idGame, Game_Season_idSeason, Game_Team_idTeam, Game_Team_Season_idSeason);

CREATE TABLE PlayerStats (
  Game_Team_idTeam INTEGER UNSIGNED NOT NULL,
  Game_Season_idSeason INTEGER UNSIGNED NOT NULL,
  Game_idGame INTEGER UNSIGNED NOT NULL,
  Player_Team_Season_idSeason INTEGER UNSIGNED NOT NULL,
  Player_Team_idTeam INTEGER UNSIGNED NOT NULL,
  Player_idPlayer INTEGER UNSIGNED NOT NULL,
  Game_Team_Season_idSeason INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY(Game_Team_idTeam, Game_Season_idSeason, Game_idGame, Player_Team_Season_idSeason, Player_Team_idTeam, Player_idPlayer, Game_Team_Season_idSeason),
  FOREIGN KEY(Game_idGame, Game_Season_idSeason, Game_Team_idTeam, Game_Team_Season_idSeason)
    REFERENCES Game(idGame, Season_idSeason, Team_idTeam, Team_Season_idSeason)
      ON DELETE CASCADE
      ON UPDATE NO ACTION,
  FOREIGN KEY(Player_idPlayer, Player_Team_idTeam, Player_Team_Season_idSeason)
    REFERENCES Player(idPlayer, Team_idTeam, Team_Season_idSeason)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
);

CREATE INDEX PlayerStats_FKIndex1 on PlayerStats(Game_idGame, Game_Season_idSeason, Game_Team_idTeam, Game_Team_Season_idSeason);
CREATE INDEX PlayerStats_FKIndex2 on PlayerStats(Player_idPlayer, Player_Team_idTeam, Player_Team_Season_idSeason);



