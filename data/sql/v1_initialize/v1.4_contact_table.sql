
CREATE TABLE "nowaiting"."contact" (
  "email"                varchar(500) PRIMARY KEY,
  "subject"           varchar(500),
  "message"         varchar(1000),
  "phone"           varchar(500)
);

create table "nowaiting"."users" (
"id" identity primary key,
"email"  varchar(1000),
"nickname" varchar(1000),
"password" varchar(1000))
