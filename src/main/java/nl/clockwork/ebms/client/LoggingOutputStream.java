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
package nl.clockwork.ebms.client;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class LoggingOutputStream extends FilterOutputStream
{
  protected transient Log logger = LogFactory.getLog(getClass());
	private String charset;
	private StringBuffer sb = new StringBuffer();

	public LoggingOutputStream(OutputStream out)
	{
		this(out,"UTF-8");
	}

	public LoggingOutputStream(OutputStream out, String charset)
	{
		super(out);
		this.charset = charset;
	}

	@Override
	public void write(int b) throws IOException
	{
		if (logger.isDebugEnabled())
			sb.append(b);
		out.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		if (logger.isDebugEnabled())
			sb.append(b);
		out.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		if (logger.isDebugEnabled())
			sb.append(new String(b,off,len,charset));
		out.write(b,off,len);
	}

	@Override
	public void close() throws IOException
	{
		logger.debug(">>>>\n" + sb.toString());
		super.close();
	}
}