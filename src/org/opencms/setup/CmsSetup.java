/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/CmsSetup.java,v $
 * Date   : $Date: 2004/02/03 10:59:16 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.setup;

import org.opencms.util.CmsUUID;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Bean with get / set methods for all properties stored in the 'opencms.properties' file.<p> 
 * The path to the opencms home folder and
 * its config folder can also be stored an retrieved as well as a vector
 * containing possible error messages thrown by the setup.
 *
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.1 $ 
 */
public class CmsSetup {

    /**
     * The database properties
     */
    private static final String C_DB_PROPERTIES = "org/opencms/setup/dbsetup.properties";
    
    /** 
     * Contains error messages, displayed by the setup wizard 
     */
    private static Vector errors = new Vector();

    /** 
     * Contains the properties from the opencms.properties file 
     */
    private ExtendedProperties m_extProperties;

    /** 
     * Properties from dbsetup.properties 
     */
    private Properties m_dbProperties;

    /** Contains the absolute path to the opencms home directory */
    private String m_basePath;

    /**
     * Name of the database system
     */
    private String m_database;

    /**
     * Database password used to drop and create database
     */
    private String m_dbCreatePwd;

    /** 
     * This method reads the properties from the opencms.property file
     * and sets the CmsSetup properties with the matching values.
     * This method should be called when the first page of the OpenCms
     * Setup Wizard is called, so the input fields of the wizard are pre-defined
     * 
     * @param props path to the properties file
     */
    public void initProperties(String props) {
        String path = getConfigFolder() + props;
        try {
            m_extProperties = CmsSetupUtils.loadProperties(path);
            m_dbProperties = new Properties();
            m_dbProperties.load(getClass().getClassLoader().getResourceAsStream(C_DB_PROPERTIES));
        } catch (Exception e) {
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
        } else {
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
        m_extProperties.put(key, value);
    }

    /**
     * Returns the value for a given key from the extended properties.
     * 
     * @param key the property key
     * @return the string value for a given key
     */
    public String getExtProperty(String key) {
        Object value = null;

        return ((value = m_extProperties.get(key)) != null) ? value.toString() : "";
    }

    /** 
     * This method sets the value for a given key in the database properties.
     * @param key The key of the property
     * @param value The value of the property
     */
    public void setDbProperty(String key, String value) {
        m_dbProperties.put(key, value);
    }

    /**
     * Returns the value for a given key from the database properties.
     * 
     * @param key the property key
     * @return the string value for a given key
     */
    public String getDbProperty(String key) {
        Object value = null;
        return ((value = m_dbProperties.get(key)) != null) ? value.toString() : "";
    }

    /** 
     * Sets the path to the OpenCms home directory 
     *
     * @param basePath path to OpenCms home directory
     */
    public void setBasePath(String basePath) {
        m_basePath = basePath;
        if (!m_basePath.endsWith(File.separator)) {
            // Make sure that Path always ends with a separator, not always the case in different environments
            // since getServletContext().getRealPath("/") does not end with a "/" in all servlet runtimes
            m_basePath += File.separator;
        }
    }

    /** 
     * Returns the absolute path to the OpenCms home directory
     * 
     * @return the path to the OpenCms home directory 
     */
    public String getBasePath() {
        return m_basePath.replace('\\', '/').replace('/', File.separatorChar);
    }

    /** 
     * Gets the default pool
     * 
     * @return name of the default pool 
     */
    public String getPool() {
        StringTokenizer tok = new StringTokenizer(this.getExtProperty("db.pools"), ",[]");
        String pool = tok.nextToken();
        return pool;
    }

    /** 
     * Sets the database drivers to the given value 
     * 
     * @param database name of the database
     */
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
            setExtProperty("db.user.driver", userDriver);
        }
        if (getExtProperty("db.project.driver") == null || "".equals(getExtProperty("db.project.driver"))) {
            setExtProperty("db.project.driver", projectDriver);
        }
        if (getExtProperty("db.workflow.driver") == null || "".equals(getExtProperty("db.workflow.driver"))) {
            setExtProperty("db.workflow.driver", workflowDriver);
        }
        if (getExtProperty("db.backup.driver") == null || "".equals(getExtProperty("db.backup.driver"))) {
            setExtProperty("db.backup.driver", backupDriver);
        }
    }

    /** 
     * Gets the database 
     * 
     * @return name of the database
     **/
    public String getDatabase() {
        if (m_database == null) {
            m_database = this.getExtProperty("db.name");
        }
        if (m_database == null || "".equals(m_database)) {
            m_database = (String)this.getDatabases().firstElement();
        }
        return m_database;
    }

    /** 
     * Returns all databases found in 'dbsetup.properties' 
     *
     * @return List of names of possible databases 
     */
    public Vector getDatabases() {
        Vector values = new Vector();

        String value = this.getDbProperty("databases");
        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        while (tokenizer.hasMoreTokens()) {
            values.add(tokenizer.nextToken().trim());
        }
        return values;
    }

    /** 
     * Returns "nice display names" for all databases found in 'dbsetup.properties' 
     *
     * @return List of display names for possible databases 
     */
    public Vector getDatabaseNames() {
        Vector values = new Vector();

        String value = this.getDbProperty("databaseNames");
        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        while (tokenizer.hasMoreTokens()) {
            values.add(tokenizer.nextToken().trim());
        }
        return values;
    }

    /** 
     * Sets the connection string to the database to the given value 
     *
     * @param dbWorkConStr the connection string used by the OpenCms core 
     */
    public void setDbWorkConStr(String dbWorkConStr) {

        String driver = this.getDbProperty(m_database + ".driver");

        // TODO: set the driver in own methods
        setExtProperty("db.pool." + getPool() + ".jdbcDriver", driver);
        setExtProperty("db.pool." + getPool() + ".jdbcUrl", dbWorkConStr);

        this.setTestQuery(this.getDbTestQuery());
    }

    /** 
     * Returns a connection string 
     *
     * @return the connection string used by the OpenCms core  
     */
    public String getDbWorkConStr() {

        return this.getExtProperty("db.pool." + getPool() + ".jdbcUrl");
    }

    /** 
     * Sets the user of the database to the given value 
     *
     * @param dbWorkUser the database user used by the opencms core 
     */
    public void setDbWorkUser(String dbWorkUser) {

        setExtProperty("db.pool." + getPool() + ".user", dbWorkUser);
    }

    /** 
     * Returns the user of the database from the properties 
     *
     * @return the database user used by the opencms core  
     */
    public String getDbWorkUser() {

        return this.getExtProperty("db.pool." + getPool() + ".user");
    }

    /** 
     * Sets the password of the database to the given value 
     *
     * @param dbWorkPwd the password for the OpenCms database user  
     */
    public void setDbWorkPwd(String dbWorkPwd) {

        setExtProperty("db.pool." + getPool() + ".password", dbWorkPwd);
    }

    /** 
     * Returns the password of the database from the properties 
     * 
     * @return the password for the OpenCms database user 
     */
    public String getDbWorkPwd() {

        return this.getExtProperty("db.pool." + getPool() + ".password");
    }

    /** 
     * Returns the extended properties 
     *
     * @return the extended properties  
     */
    public ExtendedProperties getProperties() {
        return m_extProperties;
    }

    /** 
     * Adds a new error message to the vector 
     *
     * @param error the error message 
     */
    public static void setErrors(String error) {
        errors.add(error);
    }

    /** 
     * Returns the error messages 
     * 
     * @return a vector of error messages 
     */
    public Vector getErrors() {
        return errors;
    }

    /** 
     * Returns the path to the opencms config folder 
     *
     * @return the path to the config folder 
     */
    public String getConfigFolder() {
        return (m_basePath + "WEB-INF/config/").replace('\\', '/').replace('/', File.separatorChar);
    }

    /** 
     * Sets the database driver belonging to the database 
     * 
     * @param driver name of the opencms driver 
     */
    public void setDbDriver(String driver) {
        this.setDbProperty(m_database + ".driver", driver);
    }

    /** 
     * Returns the database driver belonging to the database
     * from the default configuration 
     *
     * @return name of the opencms driver 
     */
    public String getDbDriver() {
        return this.getDbProperty(m_database + ".driver");
    }

    /** 
     * Returns the validation query belonging to the database
     * from the default configuration 
     *
     * @return query used to validate connections 
     */
    public String getDbTestQuery() {
        return this.getDbProperty(m_database + ".testQuery");
    }

    /** 
     * Sets the validation query to the given value 
     *
     * @param query query used to validate connections   
     */
    public void setTestQuery(String query) {
        setExtProperty("db.pool." + getPool() + ".testQuery", query);
    }

    /** 
     * Returns the validation query 
     *
     * @return query used to validate connections 
     */
    public String getTestQuery() {
        return this.getExtProperty("db.pool." + getPool() + ".testQuery");
    }

    /** 
     * Sets the minimum connections to the given value 
     * 
     * @param minConn number of minimum connections
     */
    public void setMinConn(String minConn) {
        setExtProperty("db.pool." + getPool() + ".maxIdle", minConn);
    }

    /** 
     * Returns the min. connections.<p>
     * 
     * @return the min. connections
     */
    public String getMinConn() {
        return this.getExtProperty("db.pool." + getPool() + ".maxIdle");
    }

    /** 
     * Sets the maximum connections to the given value.<p>
     * 
     * @param maxConn maximum connection count
     */
    public void setMaxConn(String maxConn) {
        setExtProperty("db.pool." + getPool() + ".maxActive", maxConn);
    }

    /** 
     * Returns the max. connections.<p>
     * 
     * @return the max. connections
     */
    public String getMaxConn() {
        return this.getExtProperty("db.pool." + getPool() + ".maxActive");
    }

    /** 
     * Sets the timeout to the given value.<p>
     * 
     * @param timeout the timeout to set
     */
    public void setTimeout(String timeout) {
        setExtProperty("db.pool." + getPool() + ".maxWait", timeout);
    }

    /** 
     * Returns the timeout value.<p>
     * 
     * @return the timeout value
     */
    public String getTimeout() {
        return this.getExtProperty("db.pool." + getPool() + ".maxWait");
    }

    /** 
     * Set the mac ethernet address, required for UUID generation.<p>
     * 
     * @param ethernetAddress the mac addess to set
     */
    public void setEthernetAddress(String ethernetAddress) {
        setExtProperty("server.ethernet.address", ethernetAddress);
    }

    /** 
     * Return the mac ethernet address
     * 
     * @return the mac ethernet addess
     */
    public String getEthernetAddress() {
        return getExtProperty("server.ethernet.address");
    }

    /** 
     * Set the maximum file upload size.<p>
     * 
     * @param size the size to set
     */
    public void setFileMaxUploadSize(String size) {
        setExtProperty("workplace.file.maxuploadsize", size);
    }

    /** 
     * Returns the maximum file upload size.<p>
     * 
     * @return the maximum file upload size
     */
    public String getFileMaxUploadSize() {
        return getExtProperty("workplace.file.maxuploadsize");
    }

    /**
     * Returns the database name.<p>
     * 
     * @return the database name
     */
    public String getDb() {
        return this.getDbProperty(m_database + ".dbname");
    }

    /**
     * Sets the database name.<p>
     * 
     * @param db the database name to set
     */
    public void setDb(String db) {
        this.setDbProperty(m_database + ".dbname", db);
    }

    /**
     * Returns the database create statement.<p>
     * 
     * @return the database create statement
     */
    public String getDbCreateConStr() {
        String constr = null;
        constr = this.getDbProperty(m_database + ".constr");
        return constr;
    }

    /**
     * Sets the database create statement.<p>
     * 
     * @param dbCreateConStr the database create statement
     */
    public void setDbCreateConStr(String dbCreateConStr) {
        this.setDbProperty(m_database + ".constr", dbCreateConStr);
    }

    /**
     * Returns the database user that is used to connect to the database.<p>
     * 
     * @return the database user
     */
    public String getDbCreateUser() {
        return this.getDbProperty(m_database + ".user");
    }

    /**
     * Set the database user that is used to connect to the database.<p>
     * 
     * @param dbCreateUser the user to set
     */
    public void setDbCreateUser(String dbCreateUser) {
        this.setDbProperty(m_database + ".user", dbCreateUser);
    }

    /**
     * Returns the password used for database creation.<p>
     * 
     * @return the password used for database creation
     */
    public String getDbCreatePwd() {
        return (m_dbCreatePwd != null) ? m_dbCreatePwd : "";
    }

    /**
     * Sets the password used for the initial OpenCms database creation.<p>
     * 
     * This password will not be stored permanently, 
     * but used only in the setup wizard.<p>
     * 
     * @param dbCreatePwd the password used for the initial OpenCms database creation
     */
    public void setDbCreatePwd(String dbCreatePwd) {
        m_dbCreatePwd = dbCreatePwd;
    }

    /** 
     * Set the default tablespace when creating a new oracle user.<p>
     * 
     * @param dbDefaultTablespace the tablespace to set
     */
    public void setDbDefaultTablespace(String dbDefaultTablespace) {
        this.setDbProperty(m_database + ".defaultTablespace", dbDefaultTablespace);
    }

    /** 
     * Returns the default tablespace when creating a new oracle user.<p>
     * 
     * @return the default tablespace when creating a new oracle user
     */
    public String getDbDefaultTablespace() {
        return this.getDbProperty(m_database + ".defaultTablespace");
    }

    /** 
     * Set the temporary tablespace when creating a new oracle user.<p>
     * 
     * @param dbTemporaryTablespace the temporary tablespace when creating a new oracle user
     */
    public void setDbTemporaryTablespace(String dbTemporaryTablespace) {
        this.setDbProperty(m_database + ".temporaryTablespace", dbTemporaryTablespace);
    }

    /** 
     * Returns the temporary tablespace when creating a new oracle user.<p>
     * 
     * @return the temporary tablespace when creating a new oracle user
     */
    public String getDbTemporaryTablespace() {
        return this.getDbProperty(m_database + ".temporaryTablespace");
    }

    /** 
     * Set the index tablespace when creating a new oracle user.<p>
     * 
     * @param dbIndexTablespace the index tablespace to set
     */
    public void setDbIndexTablespace(String dbIndexTablespace) {
        this.setDbProperty(m_database + ".indexTablespace", dbIndexTablespace);
    }

    /** 
     * Returns the index tablespace when creating a new oracle user
     * 
     * @return the index tablespace
     */
    public String getDbIndexTablespace() {
        return this.getDbProperty(m_database + ".indexTablespace");
    }

    /**
     * Checks if the setup wizard is enabled.<p>
     * 
     * @return true if the setup wizard is enables, false otherwise
     */
    public boolean getWizardEnabled() {
        return "true".equals(getExtProperty("wizard.enabled"));
    }

    /**
     * Locks (i.e. disables) the setup wizard.<p>
     *
     */
    public void lockWizard() {
        setExtProperty("wizard.enabled", "false");
    }

    /**
     * Returns the database setup properties.<p>
     * 
     * @return the database setup properties
     */
    public Properties getDbSetupProps() {
        return m_dbProperties;
    }

    /**
     * Sets filename translation to enabled / disabled.<p>
     * 
     * @param value value to set (must be "true" or "false")
     */
    public void setFilenameTranslationEnabled(String value) {
        this.setExtProperty("filename.translation.enabled", value);
    }

    /** 
     * Returns "true" if filename translation is enabled.<p>
     * 
     * @return "true" if filename translation is enabled
     */
    public String getFilenameTranslationEnabled() {
        return this.getExtProperty("filename.translation.enabled");
    }

    /**
     * Sets directory translation to enabled / disabled.<p>
     * 
     * @param value value to set (must be "true" or "false")
     */    
    public void setDirectoryTranslationEnabled(String value) {
        this.setExtProperty("directory.translation.enabled", value);
    }

    /** 
     * Returns "true" if directory translation is enabled.<p>
     * 
     * @return "true" if directory translation is enabled
     */
    public String getDirectoryTranslationEnabled() {
        return this.getExtProperty("directory.translation.enabled");
    }

    /**
     * Sets the directory default index files.<p>
     * 
     * This must be a comma separated list of files.<p>
     *
     * @param value the value to set
     */
    public void setDirectoryIndexFiles(String value) {
        this.setExtProperty("directory.default.files", value);
    }

    /**
     * Returns the directory default index files as a comma separated list.<p>
     * 
     * @return the directory default index files as a comma separated list
     */
    public String getDirectoryIndexFiles() {
        Object value = null;
        value = m_extProperties.get("directory.default.files");

        if (value == null) {
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
     * 
     * @param value1 the first value 
     * @param value2 the secound value
     * @return "checked" if both values are equal, the empty String "" otherwise
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

    /**
     * Sets the webapp name
     * 
     * @param value the new webapp name
     */
    public void setAppName(String value) {
        this.setExtProperty("app.name", value);
    }

    /**
     * Returns the webapp name
     * 
     * @return the webapp name
     */
    public String getAppName() {
        return this.getExtProperty("app.name");
    }

    /** Replacer Hashtable */
    private Hashtable m_replacer;
    
    /**
     * Returns the replacer.<p>
     * 
     * @return the replacer
     */
    public Hashtable getReplacer() {
        return m_replacer;
    }
    
    /**
     * Sets the replacer.<p>
     * 
     * @param value the replacer to set
     */
    public void setReplacer(Hashtable value) {
        m_replacer = value;
    }
}