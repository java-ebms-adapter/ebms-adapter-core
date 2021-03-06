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
package nl.clockwork.ebms.client.apache;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import nl.clockwork.ebms.ssl.SSLFactoryManager;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.springframework.beans.factory.FactoryBean;

public class SSLConnectionSocketFactoryFactory implements FactoryBean<SSLConnectionSocketFactory>
{
	private SSLFactoryManager sslFactoryManager;
	private String[] enabledProtocols = new String[]{};
	private String[] enabledCipherSuites = new String[]{};
	private boolean verifyHostnames;

	public SSLConnectionSocketFactoryFactory()
	{
	}

	public SSLConnectionSocketFactoryFactory(SSLFactoryManager sslFactoryManager, String[] enabledProtocols, String[] enabledCipherSuites, boolean verifyHostnames)
	{
		this.sslFactoryManager = sslFactoryManager;
		this.enabledProtocols = enabledProtocols;
		this.enabledCipherSuites = enabledCipherSuites;
		this.verifyHostnames = verifyHostnames;
	}

	@Override
	public SSLConnectionSocketFactory getObject() throws Exception
	{
		return new SSLConnectionSocketFactory(sslFactoryManager.getSslSocketFactory(),enabledProtocols.length == 0 ? null : enabledProtocols,enabledCipherSuites.length == 0 ? null : enabledCipherSuites,getHostnameVerifier());
	}

	private HostnameVerifier getHostnameVerifier()
	{
		return verifyHostnames ? HttpsURLConnection.getDefaultHostnameVerifier() : new HostnameVerifier()
		{
			@Override
			public boolean verify(String hostname, SSLSession sslSession)
			{
				return true;
			}
		};
	}
	
	@Override
	public Class<?> getObjectType()
	{
		return SSLConnectionSocketFactory.class;
	}

	@Override
	public boolean isSingleton()
	{
		return true;
	}

	public void setSslFactoryManager(SSLFactoryManager sslFactoryManager)
	{
		this.sslFactoryManager = sslFactoryManager;
	}

	public void setEnabledProtocols(String[] enabledProtocols)
	{
		this.enabledProtocols = enabledProtocols;
	}

	public void setEnabledCipherSuites(String[] enabledCipherSuites)
	{
		this.enabledCipherSuites = enabledCipherSuites;
	}

	public void setVerifyHostnames(boolean verifyHostnames)
	{
		this.verifyHostnames = verifyHostnames;
	}
}
