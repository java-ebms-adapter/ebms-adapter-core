ALTER TABLE ebms_message ADD message_nr INTEGER NOT NULL DEFAULT 0;

ALTER TABLE ebms_message DROP INDEX uc_ebms_message_id;

ALTER TABLE ebms_message ADD CONSTRAINT uc_ebms_message_id UNIQUE (message_id(255),message_nr);

ALTER TABLE ebms_attachment MODIFY content LONGBLOB NOT NULL;

ALTER TABLE ebms_send_event ADD error_message TEXT NULL;

ALTER TABLE ebms_send_event MODIFY status_time TIMESTAMP NOT NULL DEFAULT '0000-00-00 00:00:00';

ALTER TABLE ebms_send_event MODIFY time TIMESTAMP NOT NULL DEFAULT NOW();

ALTER TABLE ebms_send_event ADD CONSTRAINT uc_ebms_send_event UNIQUE (ebms_message_id,time);