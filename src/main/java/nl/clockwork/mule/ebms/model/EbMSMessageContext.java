package nl.clockwork.mule.ebms.model;

import nl.clockwork.mule.ebms.model.ebxml.MessageHeader;

public class EbMSMessageContext
{
	private String cpaId;
	private String fromRole;
	private String toRole;
	private String service;
	private String action;
	private String conversationId;
	private String messageId;
	private String refToMessageId;

	public EbMSMessageContext(MessageHeader messageHeader)
	{
		this(messageHeader.getCPAId(),messageHeader.getFrom().getRole(),messageHeader.getTo().getRole(),messageHeader.getService().getValue(),messageHeader.getAction(),messageHeader.getConversationId(),messageHeader.getMessageData().getMessageId(),messageHeader.getMessageData().getRefToMessageId());		
	}
	
	public EbMSMessageContext(String cpaId, String service, String action)
	{
		this(cpaId,null,null,service,action,null);
	}

	public EbMSMessageContext(String cpaId, String service, String action, String conversationId)
	{
		this(cpaId,null,null,service,action,conversationId);
	}

	public EbMSMessageContext(String cpaId, String from, String to, String service, String action, String conversationId)
	{
		this(cpaId,from,to,service,action,conversationId,null,null);
	}
	
	public EbMSMessageContext(String cpaId, String fromRole, String toRole, String service, String action, String conversationId, String messageId, String refToMessageId)
	{
		this.cpaId = cpaId;
		this.fromRole = fromRole;
		this.toRole = toRole;
		this.service = service;
		this.action = action;
		this.conversationId = conversationId;
		this.messageId = messageId;
		this.refToMessageId = refToMessageId;
	}

	public String getCpaId()
	{
		return cpaId;
	}

	public String getFromRole()
	{
		return fromRole;
	}
	
	public String getToRole()
	{
		return toRole;
	}
	
	public String getService()
	{
		return service;
	}
	
	public String getAction()
	{
		return action;
	}
	
	public String getConversationId()
	{
		return conversationId;
	}
	
	public String getMessageId()
	{
		return messageId;
	}
	
	public String getRefToMessageId()
	{
		return refToMessageId;
	}
}
