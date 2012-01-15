/*******************************************************************************
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
 ******************************************************************************/
package nl.clockwork.mule.ebms;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HSQLDatabaseProvider
{
	protected transient Log logger = LogFactory.getLog(getClass());
	private DataSource dataSource;
	private String createTablesScriptFile;
	private String createConstraintsScriptFile;
	private boolean execute;

	public HSQLDatabaseProvider(DataSource dataSource, String createTablesScriptFile, String createConstraintsScriptFile)
	{
		this(dataSource,createTablesScriptFile,createConstraintsScriptFile,true);
	}
	
	public HSQLDatabaseProvider(DataSource dataSource, String createTablesScriptFile, String createConstraintsScriptFile, boolean execute)
	{
		this.dataSource = dataSource;
		this.createTablesScriptFile = createTablesScriptFile;
		this.createConstraintsScriptFile = createConstraintsScriptFile;
		this.execute = execute;
	}

	public void init() throws SQLException, IOException
	{
		if (execute)
		{
			Connection c = dataSource.getConnection();
			try
			{
				Statement s = c.createStatement();
				String sql = IOUtils.toString(HSQLDatabaseProvider.class.getResourceAsStream(createTablesScriptFile));
				s.executeUpdate(sql);
				sql = IOUtils.toString(HSQLDatabaseProvider.class.getResourceAsStream(createConstraintsScriptFile));
				if (!StringUtils.isBlank(sql))
					s.executeUpdate(sql);
				s.close();
			}
			catch (SQLException e)
			{
				logger.warn("",e);
			}
			finally
			{
				c.close();
			}
		}
	}
	
	public void close() throws SQLException
	{
		if (execute)
		{
			Connection c = dataSource.getConnection();
			try
			{
				Statement s = c.createStatement();
				s.executeUpdate("shutdown");
				s.close();
			}
			finally
			{
				c.close();
			}
		}
	}
}
