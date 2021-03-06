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
package nl.clockwork.ebms.validation;

import java.util.Date;

import org.oasis_open.committees.ebxml_cppa.schema.cpp_cpa_2_0.SyncReplyModeType;

import nl.clockwork.ebms.common.CPAManager;
import nl.clockwork.ebms.dao.EbMSDAO;
import nl.clockwork.ebms.encryption.EbMSMessageDecrypter;
import nl.clockwork.ebms.model.CacheablePartyId;
import nl.clockwork.ebms.model.EbMSMessage;
import nl.clockwork.ebms.util.CPAUtils;

public class EbMSMessageValidator
{
	protected EbMSDAO ebMSDAO;
	protected CPAManager cpaManager;
	protected CPAValidator cpaValidator;
	protected MessageHeaderValidator messageHeaderValidator;
	protected ManifestValidator manifestValidator;
	protected SignatureValidator signatureValidator;
	protected EbMSMessageDecrypter messageDecrypter;
	protected ClientCertificateValidator clientCertificateValidator;

	public void validateMessage(EbMSMessage message, Date timestamp) throws ValidatorException
	{
		if (isDuplicateMessage(message))
			throw new DuplicateMessageException();
		cpaValidator.validate(message);
		messageHeaderValidator.validate(message,timestamp);
		clientCertificateValidator.validate(message);
		signatureValidator.validate(message);
		manifestValidator.validate(message);
		messageDecrypter.decrypt(message);
		signatureValidator.validateSignature(message);
	}

	public void validateMessageError(EbMSMessage requestMessage, EbMSMessage responseMessage, Date timestamp) throws ValidatorException
	{
		if (isDuplicateMessage(responseMessage))
			throw new DuplicateMessageException();
		messageHeaderValidator.validate(requestMessage,responseMessage);
		messageHeaderValidator.validate(responseMessage,timestamp);
		clientCertificateValidator.validate(responseMessage);
	}

	public void validateAcknowledgment(EbMSMessage requestMessage, EbMSMessage responseMessage, Date timestamp) throws ValidatorException
	{
		if (isDuplicateMessage(responseMessage))
			throw new DuplicateMessageException();
		messageHeaderValidator.validate(requestMessage,responseMessage);
		messageHeaderValidator.validate(responseMessage,timestamp);
		clientCertificateValidator.validate(responseMessage);
		signatureValidator.validate(requestMessage,responseMessage);
	}

	public void validateStatusRequest(EbMSMessage message, Date timestamp) throws ValidatorException
	{
		messageHeaderValidator.validate(message,timestamp);
		clientCertificateValidator.validate(message);
	}

	public void validateStatusResponse(EbMSMessage message, Date timestamp) throws ValidatorException
	{
		messageHeaderValidator.validate(message,timestamp);
		clientCertificateValidator.validate(message);
	}

	public void validatePing(EbMSMessage message, Date timestamp) throws ValidatorException
	{
		messageHeaderValidator.validate(message,timestamp);
		clientCertificateValidator.validate(message);
	}

	public void validatePong(EbMSMessage message, Date timestamp) throws ValidatorException
	{
		messageHeaderValidator.validate(message,timestamp);
		clientCertificateValidator.validate(message);
	}

	public boolean isSyncReply(EbMSMessage message)
	{
		try
		{
			//return message.getSyncReply() != null;
			SyncReplyModeType syncReply = cpaManager.getSyncReply(message.getMessageHeader().getCPAId(),new CacheablePartyId(message.getMessageHeader().getFrom().getPartyId()),message.getMessageHeader().getFrom().getRole(),CPAUtils.toString(message.getMessageHeader().getService()),message.getMessageHeader().getAction());
			return syncReply != null && !syncReply.equals(SyncReplyModeType.NONE);
		}
		catch (Exception e)
		{
			return message.getSyncReply() != null;
		}
	}

	public boolean isDuplicateMessage(EbMSMessage message)
	{
		return /*message.getMessageHeader().getDuplicateElimination()!= null && */ebMSDAO.existsMessage(message.getMessageHeader().getMessageData().getMessageId());
	}
	
	public void setEbMSDAO(EbMSDAO ebMSDAO)
	{
		this.ebMSDAO = ebMSDAO;
	}

	public void setCpaManager(CPAManager cpaManager)
	{
		this.cpaManager = cpaManager;
	}

	public void setCpaValidator(CPAValidator cpaValidator)
	{
		this.cpaValidator = cpaValidator;
	}

	public void setMessageHeaderValidator(MessageHeaderValidator messageHeaderValidator)
	{
		this.messageHeaderValidator = messageHeaderValidator;
	}

	public void setManifestValidator(ManifestValidator manifestValidator)
	{
		this.manifestValidator = manifestValidator;
	}

	public void setSignatureValidator(SignatureValidator signatureValidator)
	{
		this.signatureValidator = signatureValidator;
	}

	public void setMessageDecrypter(EbMSMessageDecrypter messageDecrypter)
	{
		this.messageDecrypter = messageDecrypter;
	}
	public void setClientCertificateValidator(ClientCertificateValidator clientCertificateValidator)
	{
		this.clientCertificateValidator = clientCertificateValidator;
	}
}
