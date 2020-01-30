--CREATE DATABASE talachitas_sms;

CREATE TABLE talachitas_sms.message_status(
id BIGINT NOT NULL,
name VARCHAR(500),
description TEXT,
keyword VARCHAR(500) NOT NULL,
PRIMARY KEY pkey_stat (id,
keyword) );

CREATE TABLE talachitas_sms.message_type (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(500),
description TEXT,
keyword VARCHAR(500),
status BIGINT,
FOREIGN KEY fk_ty_sta (status, keyword) REFERENCES talachitas_sms.message_status(id, keyword) );

CREATE TABLE talachitas_sms.messages(
id BIGINT AUTO_INCREMENT PRIMARY KEY,
message_id VARCHAR(500) UNIQUE,
phone_number VARCHAR(500),
create_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
message_type_id BIGINT NOT NULL,
network VARCHAR(500),
from_phone_number VARCHAR(500),
keyword VARCHAR(500),
status BIGINT,
FOREIGN KEY fk_me_me_type (message_type_id) REFERENCES talachitas_sms.message_type(id),
FOREIGN KEY fk_me_sta(status) REFERENCES talachitas_sms.message_status(id) );

INSERT
  INTO
  talachitas_sms.message_status ( id,
  name,
  description,
  keyword)
VALUES (1,
'WELCOME',
'This is welcome sms subscription to talachitas.com',
'TAL');

INSERT
  INTO
  talachitas_sms.message_status ( id,
  name,
  description,
  keyword)
VALUES (100,
'CANCEL',
'This is to cancel sms subscription to talachitas.com',
'TAL');

INSERT
  INTO
  talachitas_sms.message_status ( id,
  name,
  description,
  keyword)
VALUES (1,
'RESERVED',
'This is to reserved table first message',
'TABLE');

INSERT
  INTO
  talachitas_sms.message_status ( id,
  name,
  description,
  keyword)
VALUES (2,
'CONFIRMED',
'This is the confirmation sms for reservation',
'TABLE');

INSERT
  INTO
  talachitas_sms.message_status ( id,
  name,
  description,
  keyword)
VALUES (3,
'YES',
'This is the acceptance of the table',
'TABLE');

INSERT
  INTO
  talachitas_sms.message_status ( id,
  name,
  description,
  keyword)
VALUES (4,
'NO',
'This is the rejection of the table',
'TABLE');

INSERT
  INTO
  talachitas_sms.message_status ( id,
  name,
  description,
  keyword)
VALUES (100,
'CANCEL',
'This is to cancel sms reservation',
'TABLE');


-- these are the messages types
INSERT
  INTO
  talachitas_sms.message_type (name,
  description,
  keyword,
  status)
VALUES ('Welcome Message',
'Welcome to talachitas.com. Thank you for signing up. Txt TAL STOP to cancel.',
'TAL',
1);

INSERT
  INTO
  talachitas_sms.message_type (name,
  description,
  keyword,
  status)
VALUES('table reservation',
'Your table reservation has been made. Will let you know once is ready. txt TABLE CANCEL to cancel reservation.',
'TABLE',
1);

INSERT
  INTO
  talachitas_sms.message_type (name,
  description,
  keyword,
  status)
VALUES ('reservation confirmation',
'Table confirmation, please reply TABLE YES/NO. There will be 5 min tolerance',
'TABLE',
2);

INSERT
  INTO
  talachitas_sms.message_type (name,
  description,
  keyword,
  status)
VALUES ('reservation acceptance',
'You have confirmed the reservation. We will wait for you.',
'TABLE',
3);

INSERT
  INTO
  talachitas_sms.message_type (name,
  description,
  keyword,
  status)
VALUES ('reservation rejection',
'You have cancelled the reservation. We hope you come back.',
'TABLE',
4);

CREATE INDEX idx_message_id ON
talachitas_sms.messages (message_id);
