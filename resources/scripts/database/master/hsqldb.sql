CREATE TABLE cpa
(
	cpa_id						VARCHAR(128)		NOT NULL UNIQUE,
	cpa								CLOB						NOT NULL
);

CREATE TABLE ebms_message
(
	id								INTEGER					GENERATED BY DEFAULT AS IDENTITY (START WITH 1) PRIMARY KEY,
--	parent_id					INTEGER					NULL FOREIGN KEY REFERENCES ebms_message(id),
	time_stamp				TIMESTAMP				NOT NULL,
	cpa_id						VARCHAR(256)		NOT NULL,
	conversation_id		VARCHAR(256)		NOT NULL,
	sequence_nr				INTEGER					NULL,
	message_id				VARCHAR(256)		NOT NULL,
	ref_to_message_id	VARCHAR(256)		NULL,
	from_role					VARCHAR(256)		NULL,
	to_role						VARCHAR(256)		NULL,
	service_type			VARCHAR(256)		NULL,
	service						VARCHAR(256)		NOT NULL,
	action						VARCHAR(256)		NOT NULL,
	signature					CLOB						NULL,
	message_header		CLOB						NOT NULL,
	sync_reply				CLOB						NULL,
	message_order			CLOB						NULL,
	ack_requested			CLOB						NULL,
	content						CLOB						NULL,
	status						INTEGER					NULL,
	status_time				TIMESTAMP				NULL
);

ALTER TABLE ebms_message ADD CONSTRAINT uc_ebms_message_id UNIQUE (message_id);

CREATE TABLE ebms_attachment
(
	ebms_message_id		INTEGER					NOT NULL,
	name							VARCHAR(256)		NOT NULL,
	content_id 				VARCHAR(256) 		NOT NULL,
	content_type			VARCHAR(255)		NOT NULL,
	content						BLOB						NOT NULL,
	FOREIGN KEY (ebms_message_id) REFERENCES ebms_message(id)
);

CREATE TABLE ebms_send_event
(
	ebms_message_id		INTEGER					NOT NULL,
	time							TIMESTAMP				DEFAULT NOW() NOT NULL,
	status						INTEGER					DEFAULT 0 NOT NULL,
	status_time				TIMESTAMP				DEFAULT NOW() NOT NULL,
--	http_url					INTEGER					NULL,
--	http_status_code	VARCHAR(2048)		NULL,
	FOREIGN KEY (ebms_message_id) REFERENCES ebms_message(id),
	UNIQUE (ebms_message_id,time)
);
