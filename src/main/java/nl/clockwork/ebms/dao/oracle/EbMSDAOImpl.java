/**
 * Copyright 2011 Clockwork
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.clockwork.ebms.dao.oracle;

import nl.clockwork.ebms.Constants.EbMSMessageEventType;
import nl.clockwork.ebms.Constants.EbMSMessageStatus;
import nl.clockwork.ebms.dao.AbstractEbMSDAO;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

public class EbMSDAOImpl extends AbstractEbMSDAO
{
	public EbMSDAOImpl(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, boolean identifyServer, String serverId)
	{
		super(transactionTemplate,jdbcTemplate,identifyServer,serverId);
	}

	@Override
	public String getMessageIdsQuery(String messageContextFilter, EbMSMessageStatus status, int maxNr)
	{
		return "select * from (" +
		"select message_id" +
		" from ebms_message" +
		" where message_nr = 0" +
		" and status = " + status.id() +
		messageContextFilter +
		" order by time_stamp asc)" +
		" where ROWNUM <= " + maxNr;
	}

	@Override
	protected String getMessageEventsQuery(String messageContextFilter, EbMSMessageEventType[] types, int maxNr)
	{
		return "select * from (" +
			"select ebms_message_event.message_id, ebms_message_event.event_type" +
			" from ebms_message_event, ebms_message" +
			" where ebms_message_event.processed = 0" +
			" and ebms_message_event.event_type in (" + join(types == null ? EbMSMessageEventType.values() : types,",") + ")" +
			" and ebms_message_event.message_id = ebms_message.message_id" +
			" and ebms_message.message_nr = 0" +
			messageContextFilter +
			" order by ebms_message_event.time_stamp asc)" +
			" where ROWNUM <= " + maxNr;
	}

}
