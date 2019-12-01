--create schema nowaiting;
--
--CREATE TABLE nowaiting.restaurant (
--  id   SERIAL PRIMARY KEY,
--  name VARCHAR(100)
--);
--
--CREATE TABLE nowaiting.sucursal (
--  id                SERIAL PRIMARY KEY,
--  address_1         VARCHAR(500),
--  address_2         VARCHAR(500),
--  zip_code          VARCHAR(5),
--  state             VARCHAR(2),
--  city              VARCHAR(100),
--  country           VARCHAR(10),
--  phone_number      VARCHAR(20),
--  restaurant_id     BIGINT NOT NULL,
--  latitude          FLOAT  NOT NULL,
--  longitud          FLOAT  NOT NULL,
--  created_timestamp TIMESTAMP,
--  deleted           BOOLEAN DEFAULT FALSE,
--  FOREIGN KEY (restaurant_id) REFERENCES nowaiting.restaurant (id)
--);
--
--CREATE TABLE nowaiting.service_time (
--  id          SERIAL PRIMARY KEY,
--  sucursal_id BIGINT,
--  day         INT,
--  start_time  TIMESTAMP,
--  end_time    TIMESTAMP,
--  FOREIGN KEY (sucursal_id) REFERENCES nowaiting.sucursal (id)
--);
--
--CREATE TABLE nowaiting.rest_user (
--  id                SERIAL PRIMARY KEY,
--  sucursal_id       BIGINT,
--  first_name        VARCHAR(100),
--  last_name         VARCHAR(100),
--  phone_number      VARCHAR(20),
--  user_name         VARCHAR(50),
--  password          VARCHAR(64),
--  created_timestamp TIMESTAMP,
--  deleted           BOOLEAN DEFAULT FALSE,
--  FOREIGN KEY (sucursal_id) REFERENCES nowaiting.sucursal (id)
--);
--
--CREATE TABLE nowaiting.client (
--  id                SERIAL PRIMARY KEY,
--  phone_number      VARCHAR(20) UNIQUE,
--  first_name        VARCHAR(100),
--  last_name         VARCHAR(100),
--  email             VARCHAR(300),
--  user_name         VARCHAR(100),
--  password          VARCHAR(64),
--  created_timestamp TIMESTAMP,
--  deleted           BOOLEAN DEFAULT FALSE NOT NULL
--);
--
--CREATE TABLE nowaiting.reservation (
--  id                SERIAL PRIMARY KEY,
--  user_id           BIGINT,
--  user_type         BIGINT,
--  sucursal_id       BIGINT,
--  status            INT,
--  created_timestamp TIMESTAMP,
--  FOREIGN KEY (user_id) REFERENCES nowaiting.client (id),
--  FOREIGN KEY (sucursal_id) REFERENCES nowaiting.sucursal (id)
--);
--
--CREATE TABLE nowaiting.reservation_event (
--  id                SERIAL PRIMARY KEY,
--  user_id           BIGINT,
--  user_type         BIGINT,
--  reservation_id    BIGINT,
--  status            INT,
--  created_timestamp TIMESTAMP,
--  FOREIGN KEY (reservation_id) REFERENCES nowaiting.reservation (id)
--);


create schema "nowaiting";

CREATE TABLE "nowaiting"."restaurant" (
  "id"   identity PRIMARY KEY,
  "name" VARCHAR(100)
);

CREATE TABLE "nowaiting"."sucursal" (
  "id"                identity PRIMARY KEY,
  "address_1"         VARCHAR(500),
  "address_2"         VARCHAR(500),
  "zip_code"          VARCHAR(5),
  "state"             VARCHAR(2),
  "city"              VARCHAR(100),
  "country"           VARCHAR(10),
  "phone_number"      VARCHAR(20),
  "restaurant_id"     BIGINT NOT NULL,
  "latitude"          FLOAT  NOT NULL,
  "longitud"          FLOAT  NOT NULL,
  "created_timestamp" TIMESTAMP,
  "deleted"           BOOLEAN DEFAULT FALSE,
  FOREIGN KEY ("restaurant_id") REFERENCES "nowaiting"."restaurant" ("id")
);

CREATE TABLE "nowaiting"."service_time" (
  "id"          identity PRIMARY KEY,
  "sucursal_id" BIGINT,
  "day"         INT,
  "start_time"  TIMESTAMP,
  "end_time"    TIMESTAMP,
  FOREIGN KEY ("sucursal_id") REFERENCES "nowaiting"."sucursal" ("id")
);

CREATE TABLE "nowaiting"."rest_user" (
  "id"                identity PRIMARY KEY,
  "sucursal_id"       BIGINT,
  "first_name"        VARCHAR(100),
  "last_name"         VARCHAR(100),
  "phone_number"      VARCHAR(20),
  "user_name"         VARCHAR(50),
  "password"          VARCHAR(64),
  "created_timestamp" TIMESTAMP,
  "deleted"           BOOLEAN DEFAULT FALSE,
  FOREIGN KEY ("sucursal_id") REFERENCES "nowaiting"."sucursal" ("id")
);

CREATE TABLE "nowaiting"."client" (
  "id"                identity PRIMARY KEY,
  "phone_number"      VARCHAR(20) UNIQUE,
  "first_name"        VARCHAR(100),
  "last_name"         VARCHAR(100),
  "email"             VARCHAR(300),
  "user_name"         VARCHAR(100),
  "password"          VARCHAR(64),
  "created_timestamp" TIMESTAMP,
  "deleted"           BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE TABLE "nowaiting"."reservation" (
  "id"                identity PRIMARY KEY,
  "user_id"           BIGINT,
  "user_type"         BIGINT,
  "sucursal_id"       BIGINT,
  "status"            INT,
  "created_timestamp" TIMESTAMP,
  FOREIGN KEY ("user_id") REFERENCES "nowaiting"."client" ("id"),
  FOREIGN KEY ("sucursal_id") REFERENCES "nowaiting"."sucursal" ("id")
);

CREATE TABLE "nowaiting"."reservation_event" (
  "id"                identity PRIMARY KEY,
  "user_id"           BIGINT,
  "user_type"         BIGINT,
  "reservation_id"    BIGINT,
  "status"            INT,
  "created_timestamp" TIMESTAMP,
  FOREIGN KEY ("reservation_id") REFERENCES "nowaiting"."reservation" ("id")
);

