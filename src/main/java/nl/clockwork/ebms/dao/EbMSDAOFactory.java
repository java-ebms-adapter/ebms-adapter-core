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
package nl.clockwork.ebms.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

public class EbMSDAOFactory extends AbstractDAOFactory<EbMSDAO>
{
	protected TransactionTemplate transactionTemplate;
	protected JdbcTemplate jdbcTemplate;
	private boolean identifyServer;
	private String serverId;

	@Override
	public Class<EbMSDAO> getObjectType()
	{
		return nl.clockwork.ebms.dao.EbMSDAO.class;
	}

	@Override
	public EbMSDAO createHSqlDbDAO()
	{
		return new nl.clockwork.ebms.dao.hsqldb.EbMSDAOImpl(transactionTemplate,jdbcTemplate,identifyServer,serverId);
	}

	@Override
	public EbMSDAO createMySqlDAO()
	{
		return new nl.clockwork.ebms.dao.mysql.EbMSDAOImpl(transactionTemplate,jdbcTemplate,identifyServer,serverId);
	}

	@Override
	public EbMSDAO createPostgresDAO()
	{
		return new nl.clockwork.ebms.dao.postgresql.EbMSDAOImpl(transactionTemplate,jdbcTemplate,identifyServer,serverId);
	}

	@Override
	public EbMSDAO createOracleDAO()
	{
		return new nl.clockwork.ebms.dao.oracle.EbMSDAOImpl(transactionTemplate,jdbcTemplate,identifyServer,serverId);
	}

	@Override
	public EbMSDAO createMsSqlDAO()
	{
		return new nl.clockwork.ebms.dao.mssql.EbMSDAOImpl(transactionTemplate,jdbcTemplate,identifyServer,serverId);
	}

	@Override
	public EbMSDAO createDB2DAO()
	{
		return new nl.clockwork.ebms.dao.db2.EbMSDAOImpl(transactionTemplate,jdbcTemplate,identifyServer,serverId);
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate)
	{
		this.transactionTemplate = transactionTemplate;
	}
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate)
	{
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setIdentifyServer(boolean identifyServer)
	{
		this.identifyServer = identifyServer;
	}

	public void setServerId(String serverId)
	{
		this.serverId = serverId;
	}
}
