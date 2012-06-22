/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db.jpa;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.CmsPersistenceUnitConfiguration;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbException;
import org.opencms.db.CmsDbPool;
import org.opencms.file.CmsProject;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;

/**
 * JPA database server implementation of the SQL manager interface.<p>
 * 
 * @since 8.0.0 
 */
public class CmsSqlManager extends org.opencms.db.CmsSqlManager {

    /** Default pool size for EntityManager instances. */
    public static final int DEFAULT_ENTITY_MANAGER_POOL_SIZE = 250;

    /** The persistence unit name in the persistence.xml file for OpenCms'es persistence classes. */
    public static final String JPA_PERSISTENCE_UNIT = "OpenCmsJPAPool";

    /** Property name for pool size configuration. */
    public static final String JPA_POOL_SIZE_PROPERTY_NAME = "opencms.jpa.EntityManagerPoolSize";

    /** The fully qualified Java class name of the JDBC driver to be used. */
    public static final String KEY_DRIVER_CLASS_NAME = "driverClassName";

    /** The initial number of connections that are created when the pool is started. */
    public static final String KEY_INITIAL_SIZE = "initialSize";

    /** 
     * The maximum number of active connections that can be allocated from 
     * this pool at the same time, or negative for no limit. 
     */
    public static final String KEY_MAX_ACTIVE = "maxActive";

    /** 
     * The maximum number of connections that can remain idle in the pool, 
     * without extra ones being released, or negative for no limit. 
     */
    public static final String KEY_MAX_IDLE = "maxIdle";

    /** 
     * The maximum number of milliseconds that the pool will wait (when there are no available connections) 
     * for a connection to be returned before throwing an exception, or <= 0 to wait indefinitely. 
     */
    public static final String KEY_MAX_WAIT = "maxWait";

    /**  
     * The minimum amount of time an object may sit idle in the pool 
     * before it is eligable for eviction by the idle object evictor (if any). 
     */
    public static final String KEY_MIN_EVICTABLE_IDLE_TIME = "minEvictableIdleTimeMillis";

    /** 
     * The minimum number of active connections that can remain idle in the pool, 
     * without extra ones being created, or 0 to create none. 
     */
    public static final String KEY_MIN_IDLE = "minIdle";

    /** The number of objects to examine during each run of the idle object evictor thread (if any). */
    public static final String KEY_NUM_TESTS_PER_EVICTION_RUN = "numTestsPerEvictionRun";

    /** The connection password to be passed to our JDBC driver to establish a connection. */
    public static final String KEY_PASS = "password";

    /** Prepared statement pooling for this pool. */
    public static final String KEY_PREP_STATEMENTS = "poolPreparedStatements";

    /** The indication of whether objects will be validated before being borrowed from the pool. */
    public static final String KEY_TEST_ON_BORROW = "testOnBorrow";

    /** The indication of whether objects will be validated by the idle object evictor (if any). */
    public static final String KEY_TEST_WHILE_IDLE = "testWhileIdle";

    /** The number of milliseconds to sleep between runs of the idle object evictor thread. */
    public static final String KEY_TIME_BETWEEN_EVICTION_RUNS = "timeBetweenEvictionRunsMillis";

    /** The connection URL to be passed to our JDBC driver to establish a connection. */
    public static final String KEY_URL = "url";

    /** The connection username to be passed to our JDBC driver to establish a connection. */
    public static final String KEY_USER = "username";

    /**  The SQL query that will be used to validate connections from this pool before returning them to the caller. */
    public static final String KEY_VALIDATION_QUERY = "validationQuery";

    /** Poll of EntityManager instances for OpenCms. */
    protected static ObjectPool m_openCmsEmPool;

    /** The value to be replaced with for online project. */
    protected static final String OFFLINE_PROJECT = "Offline";

    /** The value to be replaced with for online project. */
    protected static final String ONLINE_PROJECT = "Online";

    /** A pattern being replaced in JPQL queries to generate JPQL queries to access online/offline tables. */
    protected static final String QUERY_PROJECT_SEARCH_PATTERN = "${PROJECT}";

    /** String which indicates project parameter in the queries. */
    protected static final String QUERY_PROJECT_STRING = "PROJECT";

    /** Contains JPQL placeholder for query parameters. Currently it's question mark.*/
    private static final String JPQL_PARAMETER_PLACEHOLDER = "?";

    /** Number of characters for JPQL parameter's placeholder. */
    private static final int JPQL_PARAMETER_PLACEHOLDER_LENGTH = JPQL_PARAMETER_PLACEHOLDER.length();

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSqlManager.class);

    /** The hashtable with all factories. You may have additional factories for OpenCms modules. */
    private static Hashtable<String, EntityManagerFactory> m_factoryTable = new Hashtable<String, EntityManagerFactory>();

    /** Contains the state of initialization of the static part of the class. */
    private static boolean m_isInitialized;

    /** EntityManager factory for OpenCms application. */
    private static EntityManagerFactory m_persistenceFactory;

    /** Contains information about uncleared EntityManager instances. */
    private static Hashtable<EntityManager, StackTraceElement[]> m_trackOn = new Hashtable<EntityManager, StackTraceElement[]>();

    /** The filename/path of the JPQL query properties. */
    private static final String QUERY_PROPERTIES = "org/opencms/db/jpa/query.properties";

    /** A map to cache queries with replaced search patterns. */
    protected Hashtable<String, String> m_cachedQueries;

    /** A map holding all JPQL queries. */
    protected Hashtable<String, String> m_queries;

    /** Queries with parameters. */
    protected Hashtable<String, String> m_queriesWithParameters;

    /**
     * The constructor.<p>
     * 
     * @throws CmsDbException if the manager is not initialized yet 
     */
    public CmsSqlManager()
    throws CmsDbException {

        if (!m_isInitialized) {
            throw new CmsDbException(Messages.get().container(Messages.ERR_SQLMANAGER_NOT_INITIALIZED));
        }
        m_cachedQueries = new Hashtable<String, String>();
        m_queries = new Hashtable<String, String>();
        m_queriesWithParameters = new Hashtable<String, String>();
        loadQueryProperties(QUERY_PROPERTIES);
    }

    /**
     * Create EntityManager instance for given unit name. If factory
     * for this unit is not already created it creates one.<p>
     * 
     * @param unitName - the unit name in the persistence.xml file
     * @return EntityManager instance for given unit name
     */
    public static EntityManager createEntityManager(String unitName) {

        EntityManager em = null;
        EntityManagerFactory factory = getFactory(unitName);

        if (factory != null) {
            em = factory.createEntityManager();
        }
        return em;
    }

    /**
     * Close all instances of EntityManagerFactory.
     */
    public static synchronized void destroy() {

        if (CmsLog.INIT.isDebugEnabled()) {
            trackOn();
        }

        try {
            m_openCmsEmPool.close();
        } catch (Exception e) {
            // do nothing
        }
        if (m_factoryTable != null) {
            Set<String> s = m_factoryTable.keySet();
            EntityManagerFactory emf;
            for (String f : s) {
                emf = m_factoryTable.get(f);
                if (emf != null) {
                    emf.close();
                    m_factoryTable.remove(f);
                }
            }
        }
        m_isInitialized = false;
    }

    /**
     * Creates EntityManager from OpenCms's factory.<p>
     * 
     * @return EntityManager created from OpenCms's factory
     */
    public static EntityManager getEntityManager() {

        EntityManager em = null;
        try {
            em = (EntityManager)m_openCmsEmPool.borrowObject();
            if (CmsLog.INIT.isDebugEnabled()) {
                m_trackOn.put(em, Thread.currentThread().getStackTrace());
            }
        } catch (Exception e) {
            LOG.error(e);
        }

        return em;
    }

    /**
     * Returns EntityManagerFactory for given unit name. If the factory does not already exists it creates one.<p>
     * 
     * @param unitName - the unit name in the persistence.xml file
     * 
     * @return EntityManagerFactory for given unit name
     */
    public static EntityManagerFactory getFactory(String unitName) {

        EntityManagerFactory factory = m_factoryTable.get(unitName);
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory(unitName);
            m_factoryTable.put(unitName, factory);
        }
        return factory;
    }

    /**
     * Creates a new instance of a SQL manager.<p>
     * 
     * @param classname the classname of the SQL manager
     * 
     * @return a new instance of the SQL manager
     */
    public static CmsSqlManager getInstance(String classname) {

        CmsSqlManager sqlManager;

        try {
            sqlManager = new CmsSqlManager();
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_SQL_MANAGER_INIT_FAILED_1, classname), t);
            sqlManager = null;
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DRIVER_SQL_MANAGER_1, classname));
        }

        return sqlManager;

    }

    /**
     * Initialize the static part of the class.<p>
     * 
     * @param config the combined configuration of "opencms.properties" and the "persistence.xml"
     */
    public static void init(CmsParameterConfiguration config) {

        if (!m_isInitialized) {
            m_isInitialized = true;

            String connProps = buildConnectionPropertiesValue(config, CmsDbPool.OPENCMS_DEFAULT_POOL_NAME);
            Properties systemProps = System.getProperties();
            systemProps.setProperty(CmsPersistenceUnitConfiguration.ATTR_CONNECTION_PROPERTIES, connProps);

            m_persistenceFactory = Persistence.createEntityManagerFactory(JPA_PERSISTENCE_UNIT, systemProps);

            m_factoryTable.put(JPA_PERSISTENCE_UNIT, m_persistenceFactory);
            CmsPoolEntityManagerFactory entityMan = new CmsPoolEntityManagerFactory(m_persistenceFactory);
            int entityManagerPoolSize = config.getInteger(CmsDbPool.KEY_DATABASE_POOL
                + "."
                + CmsDbPool.OPENCMS_DEFAULT_POOL_NAME
                + "."
                + CmsDbPool.KEY_ENTITY_MANAGER_POOL_SIZE, DEFAULT_ENTITY_MANAGER_POOL_SIZE);
            m_openCmsEmPool = new StackObjectPool(entityMan, entityManagerPoolSize, 0);
        }
    }

    /**
     * Returns EntityManager instance from OpenCms, back to pool.<p>
     * 
     * @param em - instance which returns back to pool of OpenCmsJPAPool persistence context.
     */
    public static void returnEntityManager(EntityManager em) {

        try {
            m_openCmsEmPool.returnObject(em);
            if (CmsLog.INIT.isDebugEnabled()) {
                m_trackOn.remove(em);
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    /**
     * Builds the connection property value for JPA.<p>
     * 
     * @param config the opencms properties
     * @param key the pool name
     * 
     * @return the connection properties value 
     */
    private static String buildConnectionPropertiesValue(CmsParameterConfiguration config, String key) {

        StringBuffer propValue = new StringBuffer();

        // read the values of the pool configuration specified by the given key
        String jdbcDriver = config.get(CmsDbPool.KEY_DATABASE_POOL + '.' + key + '.' + CmsDbPool.KEY_JDBC_DRIVER);
        String jdbcUrl = config.get(CmsDbPool.KEY_DATABASE_POOL + '.' + key + '.' + CmsDbPool.KEY_JDBC_URL);
        String jdbcUrlParams = config.get(CmsDbPool.KEY_DATABASE_POOL
            + '.'
            + key
            + '.'
            + CmsDbPool.KEY_JDBC_URL_PARAMS);
        int maxActive = config.getInteger(CmsDbPool.KEY_DATABASE_POOL + '.' + key + '.' + CmsDbPool.KEY_MAX_ACTIVE, 10);
        int maxWait = config.getInteger(CmsDbPool.KEY_DATABASE_POOL + '.' + key + '.' + CmsDbPool.KEY_MAX_WAIT, 2000);
        int maxIdle = config.getInteger(CmsDbPool.KEY_DATABASE_POOL + '.' + key + '.' + CmsDbPool.KEY_MAX_IDLE, 5);
        int minEvictableIdleTime = config.getInteger(CmsDbPool.KEY_DATABASE_POOL
            + '.'
            + key
            + '.'
            + CmsDbPool.KEY_MIN_EVICTABLE_IDLE_TIME, 1800000);
        int minIdle = config.getInteger(CmsDbPool.KEY_DATABASE_POOL + '.' + key + '.' + CmsDbPool.KEY_MIN_IDLE, 0);
        int numTestsPerEvictionRun = config.getInteger(CmsDbPool.KEY_DATABASE_POOL
            + '.'
            + key
            + '.'
            + CmsDbPool.KEY_NUM_TESTS_PER_EVICTION_RUN, 3);
        int timeBetweenEvictionRuns = config.getInteger(CmsDbPool.KEY_DATABASE_POOL
            + '.'
            + key
            + '.'
            + CmsDbPool.KEY_TIME_BETWEEN_EVICTION_RUNS, 3600000);

        String username = config.getString(CmsDbPool.KEY_DATABASE_POOL + '.' + key + '.' + CmsDbPool.KEY_USERNAME, "");
        String password = config.getString(CmsDbPool.KEY_DATABASE_POOL + '.' + key + '.' + CmsDbPool.KEY_PASSWORD, "");

        boolean testOnBorrow = Boolean.valueOf(
            config.getString(CmsDbPool.KEY_DATABASE_POOL + '.' + key + '.' + CmsDbPool.KEY_TEST_ON_BORROW, "false").trim()).booleanValue();
        boolean testWhileIdle = Boolean.valueOf(
            config.getString(CmsDbPool.KEY_DATABASE_POOL + '.' + key + '.' + CmsDbPool.KEY_TEST_WHILE_IDLE, "false").trim()).booleanValue();

        String testQuery = config.get(CmsDbPool.KEY_DATABASE_POOL + '.' + key + '.' + CmsDbPool.KEY_TEST_QUERY);
        if ("".equals(testQuery)) {
            testQuery = null;
        }

        int initialSize = config.getInteger(CmsDbPool.KEY_DATABASE_POOL + '.' + key + '.' + KEY_INITIAL_SIZE, 20);

        boolean poolPreparedStatements = config.getBoolean(CmsDbPool.KEY_DATABASE_POOL
            + '.'
            + key
            + '.'
            + KEY_PREP_STATEMENTS, true);

        propValue.append(KEY_DRIVER_CLASS_NAME);
        propValue.append("=");
        propValue.append(jdbcDriver);
        propValue.append(", ");

        propValue.append(KEY_URL);
        propValue.append("=");
        propValue.append(jdbcUrl);
        propValue.append(StringUtils.defaultString(jdbcUrlParams));
        propValue.append(", ");

        propValue.append(KEY_USER);
        propValue.append("=");
        propValue.append(username);
        propValue.append(", ");

        propValue.append(KEY_PASS);
        propValue.append("=");
        propValue.append(StringUtils.defaultString(password));
        propValue.append(", ");

        propValue.append(KEY_MAX_ACTIVE);
        propValue.append("=");
        propValue.append(maxActive);
        propValue.append(", ");

        propValue.append(KEY_MAX_IDLE);
        propValue.append("=");
        propValue.append(maxIdle);
        propValue.append(", ");

        propValue.append(KEY_MAX_WAIT);
        propValue.append("=");
        propValue.append(maxWait);
        propValue.append(", ");

        propValue.append(KEY_MIN_IDLE);
        propValue.append("=");
        propValue.append(minIdle);
        propValue.append(", ");

        if (testQuery != null) {

            propValue.append(KEY_VALIDATION_QUERY);
            propValue.append("=");
            propValue.append(StringUtils.defaultString(testQuery));
            propValue.append(", ");

            propValue.append(KEY_TEST_ON_BORROW);
            propValue.append("=");
            propValue.append(testOnBorrow);
            propValue.append(", ");

            propValue.append(KEY_TEST_WHILE_IDLE);
            propValue.append("=");
            propValue.append(testWhileIdle);
            propValue.append(", ");

            propValue.append(KEY_TIME_BETWEEN_EVICTION_RUNS);
            propValue.append("=");
            propValue.append(timeBetweenEvictionRuns);
            propValue.append(", ");

            propValue.append(KEY_NUM_TESTS_PER_EVICTION_RUN);
            propValue.append("=");
            propValue.append(numTestsPerEvictionRun);
            propValue.append(", ");

            propValue.append(KEY_MIN_EVICTABLE_IDLE_TIME);
            propValue.append("=");
            propValue.append(minEvictableIdleTime);
            propValue.append(", ");
        }

        propValue.append(KEY_INITIAL_SIZE);
        propValue.append("=");
        propValue.append(initialSize);
        propValue.append(", ");

        propValue.append(KEY_PREP_STATEMENTS);
        propValue.append("=");
        propValue.append(poolPreparedStatements);

        return propValue.toString();
    }

    /**
     * Replaces the project search pattern in JPQL queries by the pattern _ONLINE_ or _OFFLINE_ depending on the 
     * specified project ID.<p> 
     * 
     * @param projectId the ID of the current project
     * @param query the JPQL query
     * @return String the JPQL query with the table key search pattern replaced
     */
    private static String replaceProjectPattern(CmsUUID projectId, String query) {

        // make the statement project dependent
        String replacePattern = ((projectId == null) || projectId.equals(CmsProject.ONLINE_PROJECT_ID))
        ? ONLINE_PROJECT
        : OFFLINE_PROJECT;
        return CmsStringUtil.substitute(query, QUERY_PROJECT_SEARCH_PATTERN, replacePattern);
    }

    /**
     * Write information about uncleared EntityManager instances in the log.<p>
     */
    private static void trackOn() {

        LOG.debug("#################### Start Tracking on EM instances ");
        LOG.debug(" there is " + m_trackOn.keySet().size() + " instances uncleared");
        Set<EntityManager> set = m_trackOn.keySet();
        int i = 0;
        for (EntityManager em : set) {
            i++;
            LOG.debug("--- " + i + " instance tracelog --- ");
            StackTraceElement[] el = m_trackOn.get(em);
            for (int b = 0; b < el.length; b++) {
                LOG.debug(el[b].toString());
            }
        }
        LOG.debug("#################### Stop Tracking on EM instances ");

    }

    /**
     * Returns a Query for a EntityManagerContext specified by the key of a SQL query
     * and the project-ID.<p>
     * 
     * @param dbc the the db context
     * @param projectId the ID of the specified CmsProject
     * @param queryKey the key of the SQL query
     * 
     * @return Query a new Query containing the pre-compiled SQL statement  
     */
    public Query createNativeQuery(CmsDbContext dbc, CmsUUID projectId, String queryKey) {

        org.opencms.db.jpa.CmsDbContext jpaDbc = (org.opencms.db.jpa.CmsDbContext)dbc;
        jpaDbc.getEntityManager().flush();
        String rawSql = readQuery(projectId, queryKey);

        return jpaDbc.getEntityManager().createNativeQuery(prepareQueryParameters(rawSql).toUpperCase());
    }

    /**
     * Returns a Query for a JDBC connection specified by the key of a JPQL query
     * and the CmsProject.<p>
     * 
     * @param dbc the db context
     * @param project the specified CmsProject
     * @param queryKey the key of the JPQL query
     * 
     * @return Query a new Query containing the pre-compiled JPQL statement  
     */
    public Query createQuery(CmsDbContext dbc, CmsProject project, String queryKey) {

        return createQuery(dbc, project.getUuid(), queryKey);
    }

    /**
     * Returns a Query for a EntityManagerContext specified by the key of a JPQL query
     * and the project-ID.<p>
     * 
     * @param dbc the dbc context
     * @param projectId the ID of the specified CmsProject
     * @param queryKey the key of the JPQL query
     * 
     * @return Query a new Query containing the pre-compiled JPQL statement  
     * 
     */
    public Query createQuery(CmsDbContext dbc, CmsUUID projectId, String queryKey) {

        org.opencms.db.jpa.CmsDbContext jpaDbc = (org.opencms.db.jpa.CmsDbContext)dbc;
        jpaDbc.getEntityManager().flush();
        String rawJpql = readQuery(projectId, queryKey);
        return jpaDbc.getEntityManager().createQuery(prepareQueryParameters(rawJpql));
    }

    /**
     * Returns a Query for a EntityManagerContext specified by the key of a JPQL query.<p>
     * 
     * @param dbc the db context
     * @param queryKey the key of the JPQL query
     * @return Query a new Query containing the pre-compiled JPQL statement 
     */
    public Query createQuery(CmsDbContext dbc, String queryKey) {

        org.opencms.db.jpa.CmsDbContext jpaDbc = (org.opencms.db.jpa.CmsDbContext)dbc;
        jpaDbc.getEntityManager().flush();
        String rawJpql = readQuery(CmsUUID.getNullUUID(), queryKey);
        return jpaDbc.getEntityManager().createQuery(prepareQueryParameters(rawJpql));
    }

    /**
     * Returns a Query for a JDBC connection specified by the JPQL query.<p>
     * 
     * @param dbc the db context object 
     * @param query the JPQL query
     * @return Query a new Query containing the pre-compiled JPQL statement  
     */
    public Query createQueryFromJPQL(CmsDbContext dbc, String query) {

        org.opencms.db.jpa.CmsDbContext jpaDbc = (org.opencms.db.jpa.CmsDbContext)dbc;
        jpaDbc.getEntityManager().flush();
        return jpaDbc.getEntityManager().createQuery(prepareQueryParameters(query));
    }

    /**
     * Returns a Query for a JDBC connection specified by the JPQL query.<p>
     * 
     * @param dbc the db context object 
     * @param query the JPQL query
     * @param params the parameters to insert into the query
     *  
     * @return Query a new Query containing the pre-compiled JPQL statement  
     */
    public Query createQueryWithParametersFromJPQL(CmsDbContext dbc, String query, List<Object> params) {

        org.opencms.db.jpa.CmsDbContext jpaDbc = (org.opencms.db.jpa.CmsDbContext)dbc;
        jpaDbc.getEntityManager().flush();
        query = CmsStringUtil.substitute(query, "\t", " ");
        query = CmsStringUtil.substitute(query, "\n", " ");
        String realQuery = prepareQueryParameters(query, false);
        Query queryObj = jpaDbc.getEntityManager().createQuery(realQuery);
        int index = 1;
        for (Object param : params) {
            if ((param instanceof String) || (param instanceof Integer) || (param instanceof Long)) {
                queryObj.setParameter(index, param);
            } else {
                throw new IllegalArgumentException();
            }
            index += 1;
        }
        return queryObj;
    }

    /**
     * Finds an object in the db and returns it.<p>
     * 
     * @param <T> the class to be returned
     * @param dbc the current dbc
     * @param cls the class information of the object to be returned 
     * @param o the object to search for
     * 
     * @return returns the found object 
     */
    public <T> T find(org.opencms.db.CmsDbContext dbc, Class<T> cls, Object o) {

        org.opencms.db.jpa.CmsDbContext jpaDbc = (org.opencms.db.jpa.CmsDbContext)dbc;
        return jpaDbc.getEntityManager().find(cls, o);
    }

    /**
     * Returns the entity manager from the current dbc.<p>
     * 
     * @param dbc the current dbc
     * 
     * @return the according entity manager
     */
    public EntityManager getEntityManager(org.opencms.db.CmsDbContext dbc) {

        org.opencms.db.jpa.CmsDbContext jpaDbc = (org.opencms.db.jpa.CmsDbContext)dbc;
        return jpaDbc.getEntityManager();
    }

    /**
     * Persists an object.<p>
     * 
     * @param dbc the current dbc
     * @param o the object to persist
     */
    public void persist(org.opencms.db.CmsDbContext dbc, Object o) {

        org.opencms.db.jpa.CmsDbContext jpaDbc = (org.opencms.db.jpa.CmsDbContext)dbc;
        jpaDbc.getEntityManager().persist(o);
    }

    /**
     * Searches for the JPQL query with the specified key and CmsProject.<p>
     * 
     * @param project the specified CmsProject
     * @param queryKey the key of the JPQL query
     * @return the the JPQL or JPQL query in this property list with the specified key
     */
    public String readQuery(CmsProject project, String queryKey) {

        return readQuery(project.getUuid(), queryKey);
    }

    /**
     * Searches for the JPQL query with the specified key and project-ID.<p>
     * 
     * For projectIds &ne; 0, the pattern {@link #QUERY_PROJECT_SEARCH_PATTERN} in table names of queries is 
     * replaced with "Online" or "Offline" to choose the right database 
     * tables for JPQL queries that are project dependent!
     * 
     * @param projectId the ID of the specified CmsProject
     * @param queryKey the key of the JPQL query
     * @return the the JPQL query in this property list with the specified key
     */
    public String readQuery(CmsUUID projectId, String queryKey) {

        String key;
        if ((projectId != null) && !projectId.isNullUUID()) {
            // id 0 is special, please see below
            StringBuffer buffer = new StringBuffer(128);
            buffer.append(queryKey);
            if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
                buffer.append(ONLINE_PROJECT);
            } else {
                buffer.append(OFFLINE_PROJECT);
            }
            key = buffer.toString();
        } else {
            key = queryKey;
        }

        // look up the query in the cache
        String query = m_cachedQueries.get(key);

        if (query == null) {
            // the query has not been cached yet
            // get the JPQL statement from the properties hash
            query = readQuery(queryKey);

            if (query == null) {
                throw new CmsRuntimeException(Messages.get().container(Messages.ERR_QUERY_NOT_FOUND_1, queryKey));
            }

            // replace control chars.
            query = CmsStringUtil.substitute(query, "\t", " ");
            query = CmsStringUtil.substitute(query, "\n", " ");

            if ((projectId != null) && !projectId.isNullUUID()) {
                // a project ID = 0 is an internal indicator that a project-independent 
                // query was requested - further regex operations are not required then
                query = CmsSqlManager.replaceProjectPattern(projectId, query);
            }
            // to minimize costs, all statements with replaced expressions are cached in a map
            m_cachedQueries.put(key, query);
        }

        return query;
    }

    /**
     * Searches for the JPQL query with the specified key.<p>
     * 
     * @param queryKey the JPQL query key
     * @return the the JPQL query in this property list with the specified key
     */
    public String readQuery(String queryKey) {

        String value = m_queries.get(queryKey);
        if ((value == null) && (!QUERY_PROJECT_STRING.equalsIgnoreCase(queryKey))) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_QUERY_NOT_FOUND_1, queryKey));
            }
        }
        return value;
    }

    /**
     * Removes an object from the db.<p>
     * 
     * @param dbc the current dbc
     * @param o the object to remove
     */
    public void remove(org.opencms.db.CmsDbContext dbc, Object o) {

        org.opencms.db.jpa.CmsDbContext jpaDbc = (org.opencms.db.jpa.CmsDbContext)dbc;
        jpaDbc.getEntityManager().remove(o);
    }

    /**
     * Replaces null or empty Strings with a String with one space character <code>" "</code>.<p>
     * 
     * @param value the string to validate
     * @return the validate string or a String with one space character if the validated string is null or empty
     */
    public String validateEmpty(String value) {

        if (CmsStringUtil.isNotEmpty(value)) {
            return value;
        }

        return " ";
    }

    /**
     * Loads a Java properties hash containing JPQL queries.<p>
     * 
     * @param propertyFilename the package/filename of the properties hash
     */
    protected void loadQueryProperties(String propertyFilename) {

        Properties properties = new Properties();

        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(propertyFilename));
            m_queries.putAll(CmsCollectionsGenericWrapper.<String, String> map(properties));
            replaceQuerySearchPatterns();
        } catch (Throwable t) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.LOG_LOAD_QUERY_PROP_FILE_FAILED_1, propertyFilename),
                    t);
            }

            properties = null;
        }
    }

    /**
     * Replaces patterns ${XXX} by another property value, if XXX is a property key with a value.<p>
     */
    protected void replaceQuerySearchPatterns() {

        String currentKey = null;
        String currentValue = null;
        int startIndex = 0;
        int endIndex = 0;
        int lastIndex = 0;

        Iterator<String> allKeys = m_queries.keySet().iterator();
        while (allKeys.hasNext()) {
            currentKey = allKeys.next();
            currentValue = m_queries.get(currentKey);
            startIndex = 0;
            endIndex = 0;
            lastIndex = 0;

            while ((startIndex = currentValue.indexOf("${", lastIndex)) != -1) {
                endIndex = currentValue.indexOf('}', startIndex);
                if ((endIndex != -1) && !currentValue.startsWith(QUERY_PROJECT_SEARCH_PATTERN, startIndex - 1)) {

                    String replaceKey = currentValue.substring(startIndex + 2, endIndex);
                    String searchPattern = currentValue.substring(startIndex, endIndex + 1);
                    String replacePattern = this.readQuery(replaceKey);

                    if (replacePattern != null) {
                        currentValue = CmsStringUtil.substitute(currentValue, searchPattern, replacePattern);
                    }
                }

                lastIndex = endIndex + 2;
            }
            m_queries.put(currentKey, currentValue);
        }
    }

    /**
     * Set numbers for parameters of giving JPQL query.<p>
     * 
     * @param query - the query
     * 
     * @return query with numbered parameter's placeholders
     */
    private String prepareQueryParameters(String query) {

        return prepareQueryParameters(query, true);
    }

    /**
     * Set numbers for parameters of giving JPQL query.<p>
     * 
     * @param query - the query
     * @param cache if true, the query will be cached 
     * 
     * @return query with numbered parameter's placeholders
     */
    private String prepareQueryParameters(String query, boolean cache) {

        String jpqlQuery = m_queriesWithParameters.get(query);
        if (jpqlQuery != null) {
            return jpqlQuery;
        }

        StringBuilder builder = new StringBuilder(query);
        int startPosition = 0;
        int currPosition = 0;
        int counter = 0;

        while ((currPosition = builder.indexOf(JPQL_PARAMETER_PLACEHOLDER, startPosition)) != -1) {
            builder.insert(currPosition + JPQL_PARAMETER_PLACEHOLDER_LENGTH, ++counter);
            startPosition = currPosition + JPQL_PARAMETER_PLACEHOLDER_LENGTH + (counter < 10 ? 1 : 2); // assumes we have not more than 99 parameters per query :)
        }

        jpqlQuery = builder.toString();
        if (cache) {
            m_queriesWithParameters.put(query, jpqlQuery);
        }
        return jpqlQuery;
    }
}
