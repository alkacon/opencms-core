/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/CmsSetup.java,v $
* Date   : $Date: 2003/05/23 16:26:46 $
* Version: $Revision: 1.31 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.boot;

import com.opencms.flex.util.CmsUUID;

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import source.org.apache.java.util.ExtendedProperties;

/**
 * Bean with get / set methods for all properties stored in the
 * 'opencms.properties' file. The path to the opencms home folder and
 * its config folder can also be stored an retrieved as well as a vector
 * containing possible error messages thrown by the setup.
 *
 * @author Magnus Meurer
 * @author Thomas Weckert (t.weckert@alkacon.com)
 */
public class CmsSetup {

	/** 
	 * Contains error messages, displayed by the setup wizard 
	 */
	private static Vector errors = new Vector();

	/** 
	 * Contains the properties from the opencms.properties file 
	 */
	private ExtendedProperties m_ExtProperties;

	/** 
	 * properties from dbsetup.properties 
	 */
	private Properties m_DbProperties;

	/** Contains the absolute path to the opencms home directory */
	private String m_basePath;

	/** 
	 * Indicates if the user has chosen standard (false) or advanced (true) setup
	 */
	private boolean m_setupType;

	/**
	 * name of the database system
	 */
	private String m_database;
	
	// name of the database
	private String m_db;
	
	/**
	 * database password used to drop and create database
	 */
	private String m_dbCreatePwd;

	/**
	 * replacer string
	 */
	private Hashtable m_replacer;

	/** 
	 * This method reads the properties from the opencms.property file
	 * and sets the CmsSetup properties with the matching values.
	 * This method should be called when the first page of the OpenCms
	 * Setup Wizard is called, so the input fields of the wizard are pre-defined
	 */
	public void initProperties(String props) {
		String path = getConfigFolder() + props;
		try {
			FileInputStream fis = new FileInputStream(new File(path));
			m_ExtProperties = new ExtendedProperties();
			m_ExtProperties.load(fis);
			fis.close();
			m_DbProperties = new Properties();
			m_DbProperties.load(getClass().getClassLoader().getResourceAsStream("com/opencms/boot/dbsetup.properties"));
		}
		catch (Exception e) {
			e.printStackTrace();
			errors.add(e.toString());
		}
	}
    
    /**
     * This method checks the validity of the given properties
     * and adds unset properties if possible
     * @return boolean true if all properties are set correctly
     */
    public boolean checkProperties() {

        // check if properties available
        if (getProperties() == null) {
            return false;
        }

        // check the ethernet address
        // in order to generate a random address, if not available                   
        if ("".equals(getEthernetAddress())) {
            setEthernetAddress(CmsUUID.getDummyEthernetAddress());
        }
        // check the maximum file size, set it to unlimited, if not valid 
        String size = getFileMaxUploadSize();
        if (size == null || "".equals(size)) {
            setFileMaxUploadSize("-1");
        }
        else {
            try {
                Integer.parseInt(size);
            } catch (Exception e) {
                setFileMaxUploadSize("-1");
            }
        }
 
        return true;
    }

	/** 
	 * This method sets the value for a given key in the extended properties.
	 * @param key The key of the property
	 * @param value The value of the property
	 */
	public void setExtProperty(String key, String value) {
		m_ExtProperties.put(key, value);
	}

	/**
	 * Returns the value for a given key from the extended properties.
	 * @return the string value for a given key
	 */
	public String getExtProperty(String key) {
		Object value = null;
            
		return ((value = m_ExtProperties.get(key)) != null) ? value.toString() : "";
	}

	/** 
	 * This method sets the value for a given key in the database properties.
	 * @param key The key of the property
	 * @param value The value of the property
	 */
	public void setDbProperty(String key, String value) {
		m_DbProperties.put(key, value);
	}

	/**
	 * Returns the value for a given key from the database properties.
	 * @return the string value for a given key
	 */
	public String getDbProperty(String key) {
        Object value = null;            
        return ((value = m_DbProperties.get(key)) != null) ? value.toString() : "";        
	}

	/**
	 * Safely inserts a backslash before each comma in a given string.
	 */
    /*
	private String escapeComma(String str) {
		StringBuffer dummy = new StringBuffer("");
		char[] chars = str.toCharArray();
		int len = chars.length;

		try {
			for (int i = 0; i < len; i++) {
				if (chars[i] == ',') {
					dummy.append('\\');
				}
				dummy.append(chars[i]);
			}
		}
		catch (Exception e) {
            System.out.println( e.toString() );
		}

		return dummy.toString();
	}
    */

	/** Sets the path to the OpenCms home directory */
	public void setBasePath(String basePath) {
		m_basePath = basePath;
		if (!m_basePath.endsWith(File.separator)) {
			// Make sure that Path always ends with a separator, not always the case in different environments
			// since getServletContext().getRealPath("/") does not end with a "/" in all servlet runtimes
			m_basePath += File.separator;
		}
	}

	/** Returns the absolute path to the OpenCms home directory */
	public String getBasePath() {
		return m_basePath.replace('\\', '/').replace('/', File.separatorChar);
	}

	/** Sets the setup type to the given value: standard (false), advanced (true) */
	public void setSetupType(boolean setupType) {
		m_setupType = setupType;
	}

	/** Returns the value of the setup type: standard (false), advanced (true) */
	public boolean getSetupType() {
		return m_setupType;
	}

	/** Gets the default pool */
	public String getPool() {
		StringTokenizer tok = new StringTokenizer(this.getExtProperty("db.pools"),",[]");
		String pool = tok.nextToken();
		return pool;
	}
	
	/** Sets the database drivers to the given value */
	public void setDatabase(String database) {

		m_database = database;
		
		String vfsDriver = this.getDbProperty(m_database + ".vfs.driver");
		String userDriver = this.getDbProperty(m_database + ".user.driver");
		String projectDriver = this.getDbProperty(m_database + ".project.driver");
		String workflowDriver = this.getDbProperty(m_database + ".workflow.driver");
        String backupDriver = this.getDbProperty(m_database + ".backup.driver");
		
		// Change/write configuration only if not available or database changed
		setExtProperty("db.name", m_database);
		if (getExtProperty("db.vfs.driver") == null || "".equals(getExtProperty("db.vfs.driver"))) {
		setExtProperty("db.vfs.driver", vfsDriver);
		}
		if (getExtProperty("db.user.driver") == null || "".equals(getExtProperty("db.user.driver"))) {
		setExtProperty("db.user.driver",userDriver);
		}	
		if (getExtProperty("db.project.driver") == null || "".equals(getExtProperty("db.project.driver"))) {
		setExtProperty("db.project.driver",projectDriver);
		}	
		if (getExtProperty("db.workflow.driver") == null || "".equals(getExtProperty("db.workflow.driver"))) {	    
			setExtProperty("db.workflow.driver",workflowDriver);
		}
        if (getExtProperty("db.backup.driver") == null || "".equals(getExtProperty("db.backup.driver"))) {	    
	        setExtProperty("db.backup.driver",backupDriver);
        }
	}

	/** Gets the database */
	public String getDatabase() {
		if (m_database == null) {
			m_database = this.getExtProperty("db.name");
		}
		if (m_database == null || "".equals(m_database)) {
			m_database = (String)this.getDatabases().firstElement();
		}
		return m_database;
	}

	/** Returns all databases found in 'dbsetup.properties' */
	public Vector getDatabases() {
		Vector values = new Vector();

		String value = this.getDbProperty("databases");
		StringTokenizer tokenizer = new StringTokenizer(value, ",");
		while (tokenizer.hasMoreTokens()) {
			values.add(tokenizer.nextToken().trim());
		}
		return values;
	}
    
    /** Returns "nice display names" for all databases found in 'dbsetup.properties' */
    public Vector getDatabaseNames() {
        Vector values = new Vector();

        String value = this.getDbProperty("databaseNames");
        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        while (tokenizer.hasMoreTokens()) {
            values.add(tokenizer.nextToken().trim());
        }
        return values;
    }    

	/** Sets the connection string to the database to the given value */
	public void setDbWorkConStr(String dbWorkConStr) {

		String driver = this.getDbProperty(m_database + ".driver");
		
		// TODO: set the driver in own methods
		setExtProperty("db.pool." + getPool() + ".jdbcDriver", driver);
		setExtProperty("db.pool." + getPool() + ".jdbcUrl", dbWorkConStr);
		
		this.setTestQuery(this.getDbTestQuery());
	}

	/** Returns a connection string */
	public String getDbWorkConStr() {

		return this.getExtProperty("db.pool." + getPool() + ".jdbcUrl");
	}
	
	/** Sets the user of the database to the given value */
	public void setDbWorkUser(String dbWorkUser) {

		setExtProperty("db.pool." + getPool() + ".user", dbWorkUser);
	}

	/** Returns the user of the database from the properties */
	public String getDbWorkUser() {

		return this.getExtProperty("db.pool." + getPool() + ".user");
	}

	/** Sets the password of the database to the given value */
	public void setDbWorkPwd(String dbWorkPwd) {

		setExtProperty("db.pool." + getPool() + ".password", dbWorkPwd);
	}

	/** Returns the password of the database from the properties */
	public String getDbWorkPwd() {

		return this.getExtProperty("db.pool." + getPool() + ".password");
	}

	/** Returns the extended properties */
	public ExtendedProperties getProperties() {
		return m_ExtProperties;
	}

	/** Adds a new error message to the vector */
	public static void setErrors(String error) {
		errors.add(error);
	}

	/** Returns the error messages */
	public Vector getErrors() {
		return errors;
	}

	/** Returns the path to the opencms config folder */
	public String getConfigFolder() {
		return (m_basePath + "WEB-INF/config/").replace('\\', '/').replace('/', File.separatorChar);
	}

	/** Sets the database driver belonging to the database */
	public void setDbDriver(String driver) {
		this.setDbProperty(m_database + ".driver", driver);
	}
	
	/** Returns the database driver belonging to the database */
	public String getDbDriver() {
		return this.getDbProperty(m_database + ".driver");
	}

	/** Returns the validation query belonging to the database */
	public String getDbTestQuery() {
		return this.getDbProperty(m_database + ".testQuery");
	}

	/** Sets the validation query to the given value */
	public void setTestQuery(String query) {
		setExtProperty("db.pool." + getPool() + ".testQuery", query);
	}

	/** Returns the validation query */
	public String getTestQuery() {
		return this.getExtProperty("db.pool." + getPool() + ".testQuery");
	}
		
	/** Sets the minimum connections to the given value */
	public void setMinConn(String minConn) {
		setExtProperty("db.pool." + getPool() + ".maxIdle", minConn);
	}

	/** Returns the min. connections */
	public String getMinConn() {
		return this.getExtProperty("db.pool." + getPool() + ".maxIdle");
	}

	/** Sets the maximum connections to the given value */
	public void setMaxConn(String maxConn) {
		setExtProperty("db.pool." + getPool() + ".maxActive", maxConn);
	}

	/** Returns the max. connections */
	public String getMaxConn() {
		return this.getExtProperty("db.pool." + getPool() + ".maxActive");
	}

	/** Sets the increase rate to the given value */
	//public void setIncreaseRate(String increaseRate) {
	//	setExtProperty("pool." + getResourceBroker() + ".increaseRate", increaseRate);
	//}

	/** Returns the increase rate */
	//public String getIncreaseRate() {
	//	return this.getExtProperty("pool." + getResourceBroker() + ".increaseRate");
	//}

	/** Sets the timeout to the given value */
	public void setTimeout(String timeout) {
		setExtProperty("db.pool." + getPool() + ".maxWait", timeout);
	}

	/** Returns the timeout value */
	public String getTimeout() {
		return this.getExtProperty("db.pool." + getPool() + ".maxWait");
	}

	/** Sets the max. age to the given value */
	//public void setMaxAge(String maxAge) {
	//	setExtProperty("pool." + getResourceBroker() + ".maxage", maxAge);
	//}

	/** Returns the max. age */
	//public String getMaxAge() {
	//	return this.getExtProperty("pool." + getResourceBroker() + ".maxage");
	//}

	/** Sets the cache value for user to the given value */
	public void setCacheUser(String cacheUser) {
		setExtProperty("cache.user", cacheUser);
	}

	/** Returns the cache value for user */
	public String getCacheUser() {
		return this.getExtProperty("cache.user");
	}

	/** Sets the cache value for group to the given value */
	public void setCacheGroup(String cacheGroup) {
		setExtProperty("cache.group", cacheGroup);
	}

	/** Returns the cache value for group */
	public String getCacheGroup() {
		return this.getExtProperty("cache.group");
	}

	/** Sets the cache value for usergroups to the given value */
	public void setCacheUserGroups(String cacheUserGroups) {
		setExtProperty("cache.usergroups", cacheUserGroups);
	}

	/** Returns the cache value for usergroups */
	public String getCacheUserGroups() {
		return this.getExtProperty("cache.usergroups");
	}

	/** Sets the cache value for project to the given value */
	public void setCacheProject(String cacheProject) {
		setExtProperty("cache.project", cacheProject);
	}

	/** Returns the cache value for project */
	public String getCacheProject() {
		return this.getExtProperty("cache.project");
	}

	/** Sets the cache value for online project to the given value */
	public void setCacheOnlineProject(String cacheOnlineProject) {
		setExtProperty("cache.onlineproject", cacheOnlineProject);
	}

	/** Returns the cache value for online project */
	public String getCacheOnlineProject() {
		return this.getExtProperty("cache.onlineproject");
	}

	/** Sets the cache value for resource to the given value */
	public void setCacheResource(String cacheResource) {
		setExtProperty("cache.resource", cacheResource);
	}

	/** Returns the cache value for resource */
	public String getCacheResource() {
		return this.getExtProperty("cache.resource");
	}

	/** Sets the cache value for subres to the given value */
	public void setCacheSubres(String cacheSubres) {
		setExtProperty("cache.subres", cacheSubres);
	}

	/** Returns the cache value for subres */
	public String getCacheSubres() {
		return this.getExtProperty("cache.subres");
	}

	/** Sets the cache value for property to the given value */
	public void setCacheProperty(String cacheProperty) {
		setExtProperty("cache.property", cacheProperty);
	}

	/** Returns the cache value for property */
	public String getCacheProperty() {
		return this.getExtProperty("cache.property");
	}

	/** Sets the cache value for property def. to the given value */
	public void setCachePropertyDef(String cachePropertyDef) {
		setExtProperty("cache.propertydef", cachePropertyDef);
	}

	/** Returns the cache value for property def. */
	public String getCachePropertyDef() {
		return this.getExtProperty("cache.propertydef");
	}

	/** Sets the cache value for property def. vector to the given value */
	public void setCachePropertyDefVector(String cachePropertyDefVector) {
		setExtProperty("cache.propertydefvector", cachePropertyDefVector);
	}

	/** Returns the cache value for property def. vector */
	public String getCachePropertyDefVector() {
		return this.getExtProperty("cache.propertydefvector");
	}

	/** Sets the value for session failover to the given value */
	public void setSessionFailover(String sessionFailover) {
		setExtProperty("sessionfailover.enabled", sessionFailover);
	}

	/** Returns the value for session failover */
	public String getSessionFailover() {
		return this.getExtProperty("sessionfailover.enabled");
	}

	/** Sets the value for deleting published project parameters to the given value */
	public void setHistoryEnabled(String historyEnabled) {
		setExtProperty("history.enabled", historyEnabled);
	}

	/** Returns the value for deleting published project parameters */
	public String getHistoryEnabled() {
		return this.getExtProperty("history.enabled");
	}

	/** Sets the value for http streaming to the given value */
	public void setHttpStreaming(String httpStreaming) {
		setExtProperty("httpstreaming.enabled", httpStreaming);
	}

	/** Returns the value for http streaming */
	public String getHttpStreaming() {
		return this.getExtProperty("httpstreaming.enabled");
	}

	/** Sets the value for exportpoint nr to the given value */
	public void setExportPoint(String exportPoint, int nr) {
		setExtProperty("exportpoint." + nr, exportPoint);
	}

	/** Returns the value for exportpoint nr */
	public String getExportPoint(int nr) {
		return this.getExtProperty("exportpoint." + nr);
	}

	/** Sets the value for exportpoint path nr to the given value */
	public void setExportPointPath(String exportPointPath, int nr) {
		setExtProperty("exportpoint.path." + nr, exportPointPath);
	}

	/** Returns the value for exportpoint path nr */
	public String getExportPointPath(int nr) {
		return this.getExtProperty("exportpoint.path." + nr);
	}

	/** Sets the value for redirect nr to the given value */
	public void setRedirect(String redirect, int nr) {
		setExtProperty("redirect." + nr, redirect);
	}

	/** Returns the value for redirect nr */
	public String getRedirect(int nr) {
		return this.getExtProperty("redirect." + nr);
	}

	/** Sets the value for redirect location nr to the given value */
	public void setRedirectLocation(String redirectLocation, int nr) {
		setExtProperty("redirectlocation." + nr, redirectLocation);
	}

	/** Returns the value for redirect location nr */
	public String getRedirectLocation(int nr) {
		return this.getExtProperty("redirectlocation." + nr);
	}

	/** Sets the value for opencms logging to the given value */
	public void setLogging(String logging) {
		setExtProperty("log", logging);
	}

	/** Returns the value for opencms logging */
	public String getLogging() {
		return this.getExtProperty("log");
	}

	/** Sets the value for the log file to the given value */
	public void setLogFile(String logFile) {
		setExtProperty("log.file", logFile);
	}

	/** Returns the value for the log file */
	public String getLogFile() {
		return this.getExtProperty("log.file");
	}

	/** Enables/Disables timestamps in the opencms logfile */
	public void setLogTimestamp(String logTimestamp) {
		setExtProperty("log.timestamp", logTimestamp);
	}

	/** Indicates if timestamps are displayed in the opencms logfile */
	public String getLogTimestamp() {
		return this.getExtProperty("log.timestamp");
	}

	/** Enables/Disables memory state in the log messages */
	public void setLogMemory(String logMemory) {
		setExtProperty("log.memory", logMemory);
	}

	/** Indicates if memory state is displayed in the log messages */
	public String getLogMemory() {
		return this.getExtProperty("log.memory");
	}

	/** Sets the value for the log date format to the given value */
	public void setLogDateFormat(String logDateFormat) {
		setExtProperty("log.dateFormat", logDateFormat);
	}

	/** Returns the value for the log date format */
	public String getLogDateFormat() {
		return this.getExtProperty("log.dateFormat");
	}

	/** Sets the value for the log queue maxage to the given value */
	public void setLogQueueMaxAge(String logQueueMaxAge) {
		setExtProperty("log.queue.maxage", logQueueMaxAge);
	}

	/** Returns the value for the log queue maxage */
	public String getLogQueueMaxAge() {
		return this.getExtProperty("log.queue.maxage");
	}

	/** Sets the value for the log queue maxsize to the given value */
	public void setLogQueueMaxSize(String logQueueMaxSize) {
		setExtProperty("log.queue.maxsize", logQueueMaxSize);
	}

	/** Returns the value for the log queue maxsize */
	public String getLogQueueMaxSize() {
		return this.getExtProperty("log.queue.maxsize");
	}

	/** Enables/Disables channel names in the log messages */
	public void setLoggingChannelName(String loggingChannelName) {
		setExtProperty("log.channel", loggingChannelName);
	}

	/** Indicates if channel names are displayed in the log messages */
	public String getLoggingChannelName() {
		return this.getExtProperty("log.channel");
	}

	/** Enables/Disables channel opencms_init in the log messages */
	public void setLoggingChannelOpencms_init(String loggingChannelOpencms_init) {
		setExtProperty("log.channel.opencms_init", loggingChannelOpencms_init);
	}

	/** Indicates if channel opencms_init is enabled in the log messages */
	public String getLoggingChannelOpencms_init() {
		return this.getExtProperty("log.channel.opencms_init");
	}

	/** Enables/Disables channel opencms_debug in the log messages */
	public void setLoggingChannelOpencms_debug(String loggingChannelOpencms_debug) {
		setExtProperty("log.channel.opencms_debug", loggingChannelOpencms_debug);
	}

	/** Indicates if channel opencms_debug is enabled in the log messages */
	public String getLoggingChannelOpencms_debug() {
		return this.getExtProperty("log.channel.opencms_debug");
	}

	/** Enables/Disables channel opencms_cache in the log messages */
	public void setLoggingChannelOpencms_cache(String loggingChannelOpencms_cache) {
		setExtProperty("log.channel.opencms_cache", loggingChannelOpencms_cache);
	}

	/** Indicates if channel opencms_cache is enabled in the log messages */
	public String getLoggingChannelOpencms_cache() {
		return this.getExtProperty("log.channel.opencms_cache");
	}

	/** Enables/Disables channel opencms_info in the log messages */
	public void setLoggingChannelOpencms_info(String loggingChannelOpencms_info) {
		setExtProperty("log.channel.opencms_info", loggingChannelOpencms_info);
	}

	/** Indicates if channel opencms_info is enabled in the log messages */
	public String getLoggingChannelOpencms_info() {
		return this.getExtProperty("log.channel.opencms_info");
	}

	/** Enables/Disables channel opencms_pool in the log messages */
	public void setLoggingChannelOpencms_pool(String loggingChannelOpencms_pool) {
		setExtProperty("log.channel.opencms_pool", loggingChannelOpencms_pool);
	}

	/** Indicates if channel opencms_pool is enabled in the log messages */
	public String getLoggingChannelOpencms_pool() {
		return this.getExtProperty("log.channel.opencms_pool");
	}

	/** Enables/Disables channel opencms_streaming in the log messages */
	public void setLoggingChannelOpencms_streaming(String loggingChannelOpencms_streaming) {
		setExtProperty("log.channel.opencms_streaming", loggingChannelOpencms_streaming);
	}

	/** Indicates if channel opencms_streaming is enabled in the log messages */
	public String getLoggingChannelOpencms_streaming() {
		return this.getExtProperty("log.channel.opencms_streaming");
	}

	/** Enables/Disables channel opencms_critical in the log messages */
	public void setLoggingChannelOpencms_critical(String loggingChannelOpencms_critical) {
		setExtProperty("log.channel.opencms_critical", loggingChannelOpencms_critical);
	}

	/** Indicates if channel opencms_critical is enabled in the log messages */
	public String getLoggingChannelOpencms_critical() {
		return this.getExtProperty("log.channel.opencms_critical");
	}

	/** Enables/Disables channel opencms_elementcache in the log messages */
	public void setLoggingChannelOpencms_elementcache(String loggingChannelOpencms_elementcache) {
		setExtProperty("log.channel.opencms_elementcache", loggingChannelOpencms_elementcache);
	}

	/** Indicates if channel opencms_elementcache is enabled in the log messages */
	public String getLoggingChannelOpencms_elementcache() {
		return this.getExtProperty("log.channel.opencms_elementcache");
	}

	/** Enables/Disables channel modules_debug in the log messages */
	public void setLoggingChannelModules_debug(String loggingChannelModules_debug) {
		setExtProperty("log.channel.modules_debug", loggingChannelModules_debug);
	}

	/** Indicates if channel modules_debug is enabled in the log messages */
	public String getLoggingChannelModules_debug() {
		return this.getExtProperty("log.channel.modules_debug");
	}

	/** Enables/Disables channel modules_info in the log messages */
	public void setLoggingChannelModules_info(String loggingChannelModules_info) {
		setExtProperty("log.channel.modules_info", loggingChannelModules_info);
	}

	/** Indicates if channel modules_info is enabled in the log messages */
	public String getLoggingChannelModules_info() {
		return this.getExtProperty("log.channel.modules_info");
	}

	/** Enables/Disables channel modules_critical in the log messages */
	public void setLoggingChannelModules_critical(String loggingChannelModules_critical) {
		setExtProperty("log.channel.modules_critical", loggingChannelModules_critical);
	}

	/** Indicates if channel modules_critical is enabled in the log messages */
    public String getLoggingChannelModules_critical() {
        return this.getExtProperty("log.channel.modules_critical");
    } 
    
    public String getLoggingFlexCache() {
        return this.getExtProperty("log.channel.flex_cache");
    }
    
    public void setLoggingFlexCache(String value) {
        this.setExtProperty("log.channel.flex_cache", value);
    }
    
    public String getLoggingFlexLoader() {
        return this.getExtProperty("log.channel.flex_loader");
    }
    
    public void setLoggingFlexLoader(String value) {
        this.setExtProperty("log.channel.flex_loader", value);
    }           

	public void setElementCache(String elementCache) {
		setExtProperty("elementcache.enabled", elementCache);
	}

	public String getElementCache() {
		return this.getExtProperty("elementcache.enabled");
	}

	public void setElementCacheURI(String elementCacheURI) {
		setExtProperty("elementcache.uri", elementCacheURI);
	}

	public String getElementCacheURI() {
		return this.getExtProperty("elementcache.uri");
	}

	public void setElementCacheElements(String elementCacheElements) {
		setExtProperty("elementcache.elements", elementCacheElements);
	}

	public String getElementCacheElements() {
		return this.getExtProperty("elementcache.elements");
	}

	public void setElementCacheVariants(String elementCacheVariants) {
		setExtProperty("elementcache.variants", elementCacheVariants);
	}

	public String getElementCacheVariants() {
		return this.getExtProperty("elementcache.variants");
	}

    /** Set the fictional mac ethernet address */
    public void setEthernetAddress(String ethernetAddress) {
        setExtProperty("server.ethernet.address", ethernetAddress);
    }
    
    /** Get the fictional mac ethernet address */
    public String getEthernetAddress() {
        return getExtProperty("server.ethernet.address");
    }
    
    /** Set the maximum file upload size */
    public void setFileMaxUploadSize(String size) {
        setExtProperty("workplace.file.maxuploadsize", size);
    }

    /** Get the maximum file upload size */
    public String getFileMaxUploadSize() {
        return getExtProperty("workplace.file.maxuploadsize");
    }

	public String getDb() {
		return this.getDbProperty(m_database + ".dbname");
	}

	public void setDb(String db) {
		this.setDbProperty(m_database + ".dbname", db);
	}
	
	public String getDbCreateConStr() {
		//String constr = this.getDbWorkConStr();
		String constr = null;
		if (constr == null || "".equals(constr)) {
			constr = this.getDbProperty(m_database + ".constr");
		}
		return constr;
	}

	public void setDbCreateConStr(String dbCreateConStr) {
		this.setDbProperty(m_database + ".constr", dbCreateConStr);
	}

	public String getDbCreateUser() {
		return this.getDbProperty(m_database + ".user");
	}

	public void setDbCreateUser(String dbCreateUser) {
		this.setDbProperty(m_database + ".user", dbCreateUser);
	}

	public String getDbCreatePwd() {
		return (m_dbCreatePwd != null) ? m_dbCreatePwd : "";
	}

	public void setDbCreatePwd(String dbCreatePwd) {
		m_dbCreatePwd = dbCreatePwd;
	}

    /** Set the default tablespace when creating a new oracle user */
    public void setDbDefaultTablespace(String dbDefaultTablespace) {
        this.setDbProperty(m_database + ".defaultTablespace", dbDefaultTablespace);
    }

    /** Get the default tablespace when creating a new oracle user */
    public String getDbDefaultTablespace() {
        return this.getDbProperty(m_database + ".defaultTablespace");
    }

    /** Set the temporary tablespace when creating a new oracle user */
    public void setDbTemporaryTablespace(String dbTemporaryTablespace) {
        this.setDbProperty(m_database + ".temporaryTablespace", dbTemporaryTablespace);
    }

    /** Get the temporary tablespace when creating a new oracle user */
    public String getDbTemporaryTablespace() {
        return this.getDbProperty(m_database + ".temporaryTablespace");
    }
       
	public boolean getWizardEnabled() {
		String dummy = this.getExtProperty("wizard.enabled");
		return "true".equals(dummy);
	}

	public void lockWizard() {
		setExtProperty("wizard.enabled", "false");
	}

	public Properties getDbSetupProps() {
		return m_DbProperties;
	}

	public Hashtable getReplacer() {
		return m_replacer;
	}

	public void setReplacer(Hashtable replacer) {
		m_replacer = replacer;
	}

	public String getStaticExport() {
		return this.getExtProperty("staticexport.enabled");
	}

	public void setStaticExport(String staticExport) {
		setExtProperty("staticexport.enabled", staticExport);
	}

	public String getStaticExportPath() {
		return this.getExtProperty("staticexport.path");
	}

	public void setStaticExportPath(String staticExportPath) {
		setExtProperty("staticexport.path", staticExportPath);
	}

	public String getUrlPrefixExport() {
		return this.getExtProperty("url_prefix_export");
	}

	public void setUrlPrefixExport(String urlPrefixExport) {
		setExtProperty("url_prefix_export", urlPrefixExport);
	}

	public String getUrlPrefixHttp() {
		return this.getExtProperty("url_prefix_http");
	}

	public void setUrlPrefixHttp(String urlPrefixHttp) {
		setExtProperty("url_prefix_http", urlPrefixHttp);
	}

	public String getUrlPrefixHttps() {
		return this.getExtProperty("url_prefix_https");
	}

	public void setUrlPrefixHttps(String urlPrefixHttps) {
		setExtProperty("url_prefix_https", urlPrefixHttps);
	}

	public String getUrlPrefixServername() {
		return this.getExtProperty("url_prefix_servername");
	}

	public void setUrlPrefixServername(String urlPrefixServername) {
		setExtProperty("url_prefix_servername", urlPrefixServername);
	}  

	public void setFlexCacheEnabled(String value) {
		this.setExtProperty("flex.cache.enabled", value);
	}

	public String getFlexCacheEnabled() {
		return this.getExtProperty("flex.cache.enabled");
	}

	public void setCacheOfflineEnabled(String value) {
		this.setExtProperty("flex.cache.offline", value);
	}

	public String getCacheOfflineEnabled() {
		return this.getExtProperty("flex.cache.offline");
	}

	public void setForceGc(String value) {
		this.setExtProperty("flex.cache.forceGC", value);
	}

	public String getForceGc() {
		return this.getExtProperty("flex.cache.forceGC");
	}

	public void setFilenameTranslationEnabled(String value) {
		this.setExtProperty("filename.translation.enabled", value);
	}

	public String getFilenameTranslationEnabled() {
		return this.getExtProperty("filename.translation.enabled");
	}

	public void setDirectoryTranslationEnabled(String value) {
		this.setExtProperty("directory.translation.enabled", value);
	}

	public String getDirectoryTranslationEnabled() {
		return this.getExtProperty("directory.translation.enabled");
	}

	public void setMaxCacheBytes(String value) {
		this.setExtProperty("flex.cache.maxCacheBytes", value);
	}

	public String getMaxCacheBytes() {
		return this.getExtProperty("flex.cache.maxCacheBytes");
	}

	public void setAvgCacheBytes(String value) {
		this.setExtProperty("flex.cache.avgCacheBytes", value);
	}

	public String getAvgCacheBytes() {
		return this.getExtProperty("flex.cache.avgCacheBytes");
	}

	public void setMaxEntryBytes(String value) {
		this.setExtProperty("flex.cache.maxEntryBytes", value);
	}

	public String getMaxEntryBytes() {
		return this.getExtProperty("flex.cache.maxEntryBytes");
	}

	public void setMaxEntries(String value) {
		this.setExtProperty("flex.cache.maxEntries", value);
	}

	public String getMaxEntries() {
		return this.getExtProperty("flex.cache.maxEntries");
	}

	public void setMaxKeys(String value) {
		this.setExtProperty("flex.cache.maxKeys", value);
	}

	public String getMaxKeys() {
		return this.getExtProperty("flex.cache.maxKeys");
	}

	public void setDirectoryIndexFiles(String value) {
		this.setExtProperty("directory.default.files", value);
	}

	public String getDirectoryIndexFiles() {        
        Object value = null;            
        value = m_ExtProperties.get("directory.default.files");
        
        if (value==null) {
            // could be null...
            return "";
        }        
        
        if (value instanceof String) {
            // ...a string...
            return value.toString();
        }
        
        // ...or a vector!
        Enumeration allIndexFiles = ((Vector)value).elements();
        String indexFiles = "";
        
        while (allIndexFiles.hasMoreElements()) {
            indexFiles += (String)allIndexFiles.nextElement();
            
            if (allIndexFiles.hasMoreElements()) {
                indexFiles += ",";
            }
        }
        
        return indexFiles;
	}
    
    /**
     * Over simplistic helper to compare two strings to check radio buttons.
     */
    public String isChecked(String value1, String value2) {
        if (value1 == null || value2 == null) {
            return "";
        }

        if (value1.trim().equalsIgnoreCase(value2.trim())) {
            return "checked";
        }

        return "";
    }    

    /**
     * Returns the defaultContentEncoding.
     * @return String
     */
    public String getDefaultContentEncoding() {
        return this.getExtProperty("defaultContentEncoding");
    }

    /**
     * Sets the defaultContentEncoding.
     * @param defaultContentEncoding The defaultContentEncoding to set
     */
    public void setDefaultContentEncoding(String defaultContentEncoding) {
        this.setExtProperty("defaultContentEncoding", defaultContentEncoding);
    }

}