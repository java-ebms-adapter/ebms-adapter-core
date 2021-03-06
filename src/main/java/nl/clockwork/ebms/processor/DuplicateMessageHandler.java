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
package nl.clockwork.ebms.processor;

import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.util.Date;

import nl.clockwork.ebms.Constants;
import nl.clockwork.ebms.Constants.EbMSAction;
import nl.clockwork.ebms.common.CPAManager;
import nl.clockwork.ebms.common.EbMSMessageFactory;
import nl.clockwork.ebms.dao.DAOTransactionCallback;
import nl.clockwork.ebms.dao.EbMSDAO;
import nl.clockwork.ebms.job.EventManager;
import nl.clockwork.ebms.model.CacheablePartyId;
import nl.clockwork.ebms.model.EbMSDocument;
import nl.clockwork.ebms.model.EbMSMessage;
import nl.clockwork.ebms.model.EbMSMessageContext;
import nl.clockwork.ebms.util.CPAUtils;
import nl.clockwork.ebms.validation.EbMSMessageValidator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.DeliveryChannel;
import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.MessageHeader;
import org.oasis_open.committees.ebxml_msg.schema.msg_header_2_0.Service;

public class DuplicateMessageHandler
{
  protected transient Log logger = LogFactory.getLog(getClass());
  protected EbMSDAO ebMSDAO;
  protected CPAManager cpaManager;
  protected EbMSMessageFactory ebMSMessageFactory;
	protected EventManager eventManager;
	protected EbMSMessageValidator messageValidator;
	protected boolean storeDuplicateMessage;
	protected boolean storeDuplicateMessageAttachments;
	protected Service mshMessageService;

	public DuplicateMessageHandler()
	{
		mshMessageService = new Service();
		mshMessageService.setValue(Constants.EBMS_SERVICE_URI);
	}

	public EbMSDocument handleMessage(final Date timestamp, final EbMSMessage message) throws EbMSProcessingException
	{
		final MessageHeader messageHeader = message.getMessageHeader();
		if (isIdenticalMessage(message))
		{
			logger.warn("Message " + message.getMessageHeader().getMessageData().getMessageId() + " is duplicate!");
			if (messageValidator.isSyncReply(message))
			{
				if (storeDuplicateMessage)
				{
					if (!storeDuplicateMessageAttachments)
						message.getAttachments().clear();
					ebMSDAO.insertDuplicateMessage(timestamp,message);
				}
				EbMSDocument result = ebMSDAO.getEbMSDocumentByRefToMessageId(messageHeader.getCPAId(),messageHeader.getMessageData().getMessageId(),mshMessageService,EbMSAction.MESSAGE_ERROR.action(),EbMSAction.ACKNOWLEDGMENT.action());
				if (result == null)
					logger.warn("No response found for duplicate message " + message.getMessageHeader().getMessageData().getMessageId() + "!");
				return result;
			}
			else
			{
				ebMSDAO.executeTransaction(
					new DAOTransactionCallback()
					{
						@Override
						public void doInTransaction()
						{
							try
							{
								if (storeDuplicateMessage)
								{
									if (!storeDuplicateMessageAttachments)
										message.getAttachments().clear();
									ebMSDAO.insertDuplicateMessage(timestamp,message);
								}
								EbMSMessageContext messageContext = ebMSDAO.getMessageContextByRefToMessageId(messageHeader.getCPAId(),messageHeader.getMessageData().getMessageId(),mshMessageService,EbMSAction.MESSAGE_ERROR.action(),EbMSAction.ACKNOWLEDGMENT.action());
								if (messageContext != null)
								{
									String clientAlias = cpaManager.getClientAlias(messageHeader.getCPAId(),new CacheablePartyId(messageHeader.getFrom().getPartyId()),messageHeader.getFrom().getRole(),CPAUtils.toString(messageHeader.getService()),messageHeader.getAction());
									DeliveryChannel deliveryChannel = cpaManager.getReceiveDeliveryChannel(messageHeader.getCPAId(),new CacheablePartyId(messageHeader.getFrom().getPartyId()),messageHeader.getFrom().getRole(),CPAUtils.toString(CPAUtils.createEbMSMessageService()),null);
									eventManager.createEvent(messageHeader.getCPAId(),clientAlias,deliveryChannel,messageContext.getMessageId(),messageHeader.getMessageData().getTimeToLive(),messageContext.getTimestamp(),false);
								}
								else
									logger.warn("No response found for duplicate message " + messageHeader.getMessageData().getMessageId() + "!");
							}
							catch (CertificateException | KeyStoreException e)
							{
								throw new RuntimeException(e);
							}
						}
					}
				);
				return null;
			}
		}
		else
			throw new EbMSProcessingException("MessageId " + messageHeader.getMessageData().getMessageId() + " already used!");
	}

	public void handleMessageError(final Date timestamp, final EbMSMessage responseMessage) throws EbMSProcessingException
	{
		if (isIdenticalMessage(responseMessage))
		{
			logger.warn("MessageError " + responseMessage.getMessageHeader().getMessageData().getMessageId() + " is duplicate!");
			if (storeDuplicateMessage)
				ebMSDAO.insertDuplicateMessage(timestamp,responseMessage);
		}
		else
			throw new EbMSProcessingException("MessageId " + responseMessage.getMessageHeader().getMessageData().getMessageId() + " already used!");
	}
	
	public void handleAcknowledgment(final Date timestamp, final EbMSMessage responseMessage) throws EbMSProcessingException
	{
		if (isIdenticalMessage(responseMessage))
		{
			logger.warn("Acknowledgment " + responseMessage.getMessageHeader().getMessageData().getMessageId() + " is duplicate!");
			if (storeDuplicateMessage)
				ebMSDAO.insertDuplicateMessage(timestamp,responseMessage);
		}
		else
			throw new EbMSProcessingException("MessageId " + responseMessage.getMessageHeader().getMessageData().getMessageId() + " already used!");
	}
	
	private boolean isIdenticalMessage(EbMSMessage message)
	{
		return ebMSDAO.existsIdenticalMessage(message);
	}

	public void setEbMSDAO(EbMSDAO ebMSDAO)
	{
		this.ebMSDAO = ebMSDAO;
	}

	public void setCpaManager(CPAManager cpaManager)
	{
		this.cpaManager = cpaManager;
	}

	public void setEbMSMessageFactory(EbMSMessageFactory ebMSMessageFactory)
	{
		this.ebMSMessageFactory = ebMSMessageFactory;
	}

	public void setEventManager(EventManager eventManager)
	{
		this.eventManager = eventManager;
	}

	public void setMessageValidator(EbMSMessageValidator messageValidator)
	{
		this.messageValidator = messageValidator;
	}

	public void setStoreDuplicateMessage(boolean storeDuplicateMessage)
	{
		this.storeDuplicateMessage = storeDuplicateMessage;
	}

	public void setStoreDuplicateMessageAttachments(boolean storeDuplicateMessageAttachments)
	{
		this.storeDuplicateMessageAttachments = storeDuplicateMessageAttachments;
	}
}
