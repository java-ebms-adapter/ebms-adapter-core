ALTER TABLE cpa DROP COLUMN url;

CREATE TABLE url
(
	source						VARCHAR(256)		NOT NULL UNIQUE,
	destination				VARCHAR(256)		NOT NULL
);

CREATE TABLE ebms_message_event
(
	message_id				VARCHAR(256)		NOT NULL UNIQUE,
	message_nr				SMALLINT				DEFAULT 0 NOT NULL,
	event_type				SMALLINT				NOT NULL,
	time_stamp				TIMESTAMP				NOT NULL,
	processed					SMALLINT				DEFAULT 0 NOT NULL,
	FOREIGN KEY (message_id,message_nr) REFERENCES ebms_message (message_id,message_nr)
);

CREATE INDEX i_ebms_message_event ON ebms_message_event (time_stamp);
