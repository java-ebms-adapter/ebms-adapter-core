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
package nl.clockwork.ebms.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import nl.clockwork.ebms.Constants;
import nl.clockwork.ebms.common.util.DOMUtils;
import nl.clockwork.ebms.model.EbMSDocument;
import nl.clockwork.ebms.processor.EbMSMessageProcessor;
import nl.clockwork.ebms.processor.EbMSProcessorException;
import nl.clockwork.ebms.util.EbMSMessageUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

public abstract class EbMSInputStreamHandler
{
  protected transient Log logger = LogFactory.getLog(nl.clockwork.ebms.server.EbMSInputStreamHandler.class);
	private EbMSMessageProcessor messageProcessor;

	public EbMSInputStreamHandler(EbMSMessageProcessor messageProcessor)
	{
		this.messageProcessor = messageProcessor;
	}

	public void handle(InputStream request) throws EbMSProcessorException
	{
	  try
		{
	  	String soapAction = getRequestHeader("SOAPAction");
	  	if (!Constants.EBMS_SOAP_ACTION.equals(soapAction))
	  	{
				if (logger.isInfoEnabled())
					logger.info("<<<<\n" + getRequestHeaders() + "\n" + IOUtils.toString(request));
				throw new EbMSProcessorException("Unable to process message! SOAPAction=" + soapAction);
	  	}
//	  	if (logger.isDebugEnabled())
//	  		request = new LoggingInputStream(request);
	  	if (logger.isDebugEnabled())
	  	{
	  		request = new BufferedInputStream(request);
	  		request.mark(Integer.MAX_VALUE);
				logger.info("<<<<\n" + getRequestHeaders() + "\n" + IOUtils.toString(request));
	  		request.reset();
	  	}
			EbMSMessageReader messageReader = new EbMSMessageReader(getRequestHeader("Content-ID"),getRequestHeader("Content-Type"));
			EbMSDocument in = messageReader.read(request);
			if (logger.isInfoEnabled() && !logger.isDebugEnabled())
				logger.info("<<<<\n" + DOMUtils.toString(in.getMessage()));
			EbMSDocument out = messageProcessor.processRequest(in);
			if (out == null)
			{
				logger.info(">>>>\nstatusCode: " + Constants.SC_NOCONTENT);
				writeResponseStatus(Constants.SC_NOCONTENT);
			}
			else
			{
				if (logger.isInfoEnabled())
					logger.info(">>>>\nstatusCode: " + Constants.SC_OK + "\nContent-Type: text/xml\nSOAPAction: " + Constants.EBMS_SOAP_ACTION + "\n" + DOMUtils.toString(out.getMessage()));
				writeResponseStatus(Constants.SC_OK);
				writeResponseHeader("Content-Type","text/xml");
				writeResponseHeader("SOAPAction",Constants.EBMS_SOAP_ACTION);
				OutputStream response = getOutputStream();
				DOMUtils.write(out.getMessage(),response);
			}
		}
		catch (Exception e)
		{
			try
			{
				Document soapFault = EbMSMessageUtils.createSOAPFault(e);
				if (logger.isInfoEnabled())
				{
					logger.info(">>>>\nstatusCode: " + Constants.SC_INTERNAL_SERVER_ERROR + "\nContent-Type: text/xml\n" + DOMUtils.toString(soapFault));
					logger.info("",e);
				}
				writeResponseStatus(Constants.SC_INTERNAL_SERVER_ERROR);
				writeResponseHeader("Content-Type","text/xml");
				OutputStream response = getOutputStream();
				DOMUtils.write(soapFault,response);
			}
			catch (Exception e1)
			{
				throw new EbMSProcessorException(e1);
			}
		}
	}

	private String getRequestHeaders()
	{
		List<String> requestHeaderNames = getRequestHeaderNames();
		StringBuffer requestHeaders = new StringBuffer();
		for (String headerName : requestHeaderNames)
		{
			List<String> headers = getRequestHeaders(headerName);
			for (String header : headers)
				requestHeaders = requestHeaders.append(headerName).append(": ").append(header).append("\n");
		}
		return requestHeaders.toString();
	}
	
	public abstract List<String> getRequestHeaderNames();
	
	public abstract List<String> getRequestHeaders(String headerName);

	public abstract String getRequestHeader(String headerName);
	
	public abstract void writeResponseStatus(int statusCode);
	
	public abstract void writeResponseHeader(String name, String value);

	public abstract OutputStream getOutputStream() throws IOException;
	
}
