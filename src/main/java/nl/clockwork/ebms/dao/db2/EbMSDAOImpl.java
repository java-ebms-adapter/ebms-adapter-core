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
package nl.clockwork.ebms.dao.db2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.xml.transform.TransformerException;

import nl.clockwork.ebms.Constants.EbMSMessageEventType;
import nl.clockwork.ebms.Constants.EbMSMessageStatus;
import nl.clockwork.ebms.common.util.DOMUtils;
import nl.clockwork.ebms.dao.DAOException;
import nl.clockwork.ebms.model.EbMSMessage;
import nl.clockwork.ebms.util.EbMSMessageUtils;

import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.MessageHeader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class EbMSDAOImpl extends nl.clockwork.ebms.dao.postgresql.EbMSDAOImpl
{
	public EbMSDAOImpl(TransactionTemplate transactionTemplate, JdbcTemplate jdbcTemplate, boolean identifyServer, String serverId)
	{
		super(transactionTemplate,jdbcTemplate,identifyServer,serverId);
	}

	@Override
	public String getMessageIdsQuery(String messageContextFilter, EbMSMessageStatus status, int maxNr)
	{
		return "select message_id" +
		" from ebms_message" +
		" where message_nr = 0" + 
		" and status = " + status.id() +
		messageContextFilter +
		" order by time_stamp asc" +
		" fetch first " + maxNr + " rows only";
	}

	@Override
	public void insertMessage(final Date timestamp, final Date persistTime, final EbMSMessage message, final EbMSMessageStatus status) throws DAOException
	{
		try
		{
			transactionTemplate.execute(
				new TransactionCallbackWithoutResult()
				{
					@Override
					public void doInTransactionWithoutResult(TransactionStatus arg0)
					{
						try
						{
							Key key = (Key)jdbcTemplate.query(
								new PreparedStatementCreator()
								{
									@Override
									public PreparedStatement createPreparedStatement(Connection connection) throws SQLException
									{
										try
										{
											PreparedStatement ps = connection.prepareStatement
											(
												"select message_id, message_nr from final table(" +
												"insert into ebms_message (" +
													"time_stamp," +
													"cpa_id," +
													"conversation_id," +
													"message_id," +
													"ref_to_message_id," +
													"time_to_live," +
													"from_party_id," +
													"from_role," +
													"to_party_id," +
													"to_role," +
													"service," +
													"action," +
													"content," +
													"status," +
													"status_time," +
													"persist_time" +
												") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?))"
											);
											ps.setTimestamp(1,new Timestamp(timestamp.getTime()));
											MessageHeader messageHeader = message.getMessageHeader();
											ps.setString(2,messageHeader.getCPAId());
											ps.setString(3,messageHeader.getConversationId());
											ps.setString(4,messageHeader.getMessageData().getMessageId());
											ps.setString(5,messageHeader.getMessageData().getRefToMessageId());
											ps.setTimestamp(6,messageHeader.getMessageData().getTimeToLive() == null ? null : new Timestamp(messageHeader.getMessageData().getTimeToLive().getTime()));
											ps.setString(7,EbMSMessageUtils.toString(messageHeader.getFrom().getPartyId().get(0)));
											ps.setString(8,messageHeader.getFrom().getRole());
											ps.setString(9,EbMSMessageUtils.toString(messageHeader.getTo().getPartyId().get(0)));
											ps.setString(10,messageHeader.getTo().getRole());
											ps.setString(11,EbMSMessageUtils.toString(messageHeader.getService()));
											ps.setString(12,messageHeader.getAction());
											ps.setString(13,DOMUtils.toString(message.getMessage(),"UTF-8"));
											if (status == null)
											{
												ps.setNull(14,java.sql.Types.INTEGER);
												ps.setNull(15,java.sql.Types.TIMESTAMP);
											}
											else
											{
												ps.setInt(14,status.id());
												ps.setTimestamp(15,new Timestamp(timestamp.getTime()));
											}
											if (persistTime == null)
												ps.setNull(16,java.sql.Types.DATE);
											else
												ps.setTimestamp(16,new Timestamp(persistTime.getTime()));
											return ps;
										}
										catch (TransformerException e)
										{
											throw new SQLException(e);
										}
									}
								},
								new KeyExtractor()
							);
							insertAttachments(key,message.getAttachments());
						}
						catch (IOException e)
						{
							throw new DAOException(e);
						}
					}
				}
			);
		}
		catch (DataAccessException | TransactionException e)
		{
			throw new DAOException(e);
		}
	}
	
	@Override
	public void insertDuplicateMessage(final Date timestamp, final EbMSMessage message) throws DAOException
	{
		try
		{
			transactionTemplate.execute(
				new TransactionCallbackWithoutResult()
				{
					@Override
					public void doInTransactionWithoutResult(TransactionStatus arg0)
					{
						try
						{
							Key key = (Key)jdbcTemplate.query(
								new PreparedStatementCreator()
								{
									@Override
									public PreparedStatement createPreparedStatement(Connection connection) throws SQLException
									{
										try
										{
											PreparedStatement ps = connection.prepareStatement
											(
													"select message_id, message_nr from final table(" +
													"insert into ebms_message (" +
													"time_stamp," +
													"cpa_id," +
													"conversation_id," +
													"message_id," +
													"message_nr," +
													"ref_to_message_id," +
													"time_to_live," +
													"from_party_id," +
													"from_role," +
													"to_party_id," +
													"to_role," +
													"service," +
													"action," +
													"content" +
												") values (?,?,?,?,(select max(message_nr) + 1 from ebms_message where message_id = ?),?,?,?,?,?,?,?,?,?))"
											);
											ps.setTimestamp(1,new Timestamp(timestamp.getTime()));
											MessageHeader messageHeader = message.getMessageHeader();
											ps.setString(2,messageHeader.getCPAId());
											ps.setString(3,messageHeader.getConversationId());
											ps.setString(4,messageHeader.getMessageData().getMessageId());
											ps.setString(5,messageHeader.getMessageData().getMessageId());
											ps.setString(6,messageHeader.getMessageData().getRefToMessageId());
											ps.setTimestamp(7,messageHeader.getMessageData().getTimeToLive() == null ? null : new Timestamp(messageHeader.getMessageData().getTimeToLive().getTime()));
											ps.setString(8,EbMSMessageUtils.toString(messageHeader.getFrom().getPartyId().get(0)));
											ps.setString(9,messageHeader.getFrom().getRole());
											ps.setString(10,EbMSMessageUtils.toString(messageHeader.getTo().getPartyId().get(0)));
											ps.setString(11,messageHeader.getTo().getRole());
											ps.setString(12,EbMSMessageUtils.toString(messageHeader.getService()));
											ps.setString(13,messageHeader.getAction());
											ps.setString(14,DOMUtils.toString(message.getMessage(),"UTF-8"));
											return ps;
										}
										catch (TransformerException e)
										{
											throw new SQLException(e);
										}
									}
								},
								new KeyExtractor()
							);
							insertAttachments(key,message.getAttachments());
						}
						catch (IOException e)
						{
							throw new DAOException(e);
						}
					}
				}
			);
		}
		catch (DataAccessException e)
		{
			throw new DAOException(e);
		}
		catch (TransactionException e)
		{
			throw new DAOException(e);
		}
	}

	@Override
	protected String getMessageEventsQuery(String messageContextFilter, EbMSMessageEventType[] types, int maxNr)
	{
		return "select ebms_message_event.message_id, ebms_message_event.event_type" +
			" from ebms_message_event, ebms_message" +
			" where ebms_message_event.processed = 0" +
			" and ebms_message_event.event_type in (" + join(types == null ? EbMSMessageEventType.values() : types,",") + ")" +
			" and ebms_message_event.message_id = ebms_message.message_id" +
			" and ebms_message.message_nr = 0" +
			messageContextFilter +
			" order by ebms_message_event.time_stamp asc" +
			" fetch first " + maxNr + " rows only";
	}

}
