CREATE DATABASE talachitas_sms;

CREATE TABLE talachitas_sms.message_type (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(500),
description text);

CREATE TABLE talachitas_sms.messages(
id BIGINT AUTO_INCREMENT PRIMARY KEY,
message_id VARCHAR(500) UNIQUE,
phone_number VARCHAR(500),
create_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
message_type_id BIGINT NOT NULL,
network VARCHAR(500),
from_phone_number VARCHAR(500),
FOREIGN KEY fk_me_me_type (message_type_id) REFERENCES talachitas_sms.message_type(id) );

INSERT
  INTO
  talachitas_sms.message_type (name,
  description)
VALUES ('Welcome Message',
'Welcome to talachitas.com. Thank you for signing up');

INSERT
  INTO
  talachitas_sms.message_type (name,
  description)
VALUES("table reservation",
'Your table reservation has been made. Will let you know once is ready');

INSERT
  INTO
  talachitas_sms.message_type (name,
  description)
VALUES ('reservation confirmation',
'Table confirmation, please reply YES/NO. There will be 5 min tolerance');

CREATE INDEX idx_message_id ON
talachitas_sms.messages (message_id);