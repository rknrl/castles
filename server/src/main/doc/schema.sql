CREATE DATABASE castles;

USE castles;

CREATE TABLE account_state (
  id    VARBINARY(128) NOT NULL,
  state VARBINARY(256) NOT NULL,
  PRIMARY KEY (id)
)
  ENGINE = InnoDb;

CREATE TABLE ratings (
  id         VARBINARY(128) NOT NULL,
  weekNumber INT,
  rating     DOUBLE,
  PRIMARY KEY (id, weekNumber)
)
  ENGINE = InnoDb;

CREATE INDEX rating ON ratings (rating);

CREATE TABLE user_info (
  id       VARBINARY(128)  NOT NULL,
  userInfo VARBINARY(1024) NOT NULL,
  PRIMARY KEY (id)
)
  ENGINE = InnoDb;

CREATE TABLE tutor_state (
  id    VARBINARY(128) NOT NULL,
  state VARBINARY(32)  NOT NULL,
  PRIMARY KEY (id)
)
  ENGINE = InnoDb;