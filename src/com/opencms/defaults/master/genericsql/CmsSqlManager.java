/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/master/genericsql/Attic/CmsSqlManager.java,v $
 * Date   : $Date: 2003/09/17 14:30:14 $
 * Version: $Revision: 1.13 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package com.opencms.defaults.master.genericsql;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringSubstitution;

import com.opencms.file.CmsObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

/**
 * A helper object to manage SQL queries. First, it loads key/value encoded SQL queries from a Java
 * properties hash. Second, it has a set of methods to return JDBC connections and statements
 * from different connection pools in the Cms dependent on the CmsProject/project-ID.
 * 
 * <p>
 * 
 * Things to know:
 * <ul>
 * <li>"name" parameters (e.g. "attributeName") identify an attribute in a table</li>
 * <li>"key" parameters (e.g. "queryKey") identify a key in query.properties to receive a SQL or attribute name</li>
 * </ul>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.13 $ $Date: 2003/09/17 14:30:14 $
 */
public class CmsSqlManager extends org.opencms.db.generic.CmsSqlManager {
    
    /** the query properties for the cos */
    private static Properties m_queries;
    
    /**
     * CmsSqlManager constructor
     */
    public CmsSqlManager(String dbPoolUrl, Class currentClass) {
        super(dbPoolUrl, false);
        
        // collect all query.properties in all packages of superclasses
        m_queries = new Properties();
        loadQueries(currentClass);
        combineQueries();   
    }
    
    /**
     * Loads recursively all query.properties from all packages of the
     * superclasses. This method calls recursively itself with the superclass
     * (if exists) as parameter.<p>
     *
     * @param the current Class of the dbaccess module.
     */
    private void loadQueries(Class currentClass) {
        // creates the queryFilenam from the packagename and
        // filename query.properties
        String className = currentClass.getName();
        String queryFilename = className.substring(0, className.lastIndexOf('.'));
        queryFilename = queryFilename.replace('.','/') + "/query.properties";
        // gets the superclass and calls this method recursively
        Class superClass = currentClass.getSuperclass();
        if(superClass != java.lang.Object.class) {
            loadQueries(superClass);
        }
        try {
            // load the queries. Entries of the most recent class will overwrite
            // entries of superclasses.
            m_queries.load(getClass().getClassLoader().getResourceAsStream(queryFilename));
        } catch(Exception exc) {
            // no query.properties found - write to logstream.
            if(OpenCms.getLog(CmsLog.CHANNEL_MODULE_MASTER).isWarnEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_MODULE_MASTER).warn("[CmsProjectDriver] Couldn't load " + queryFilename + " errormessage: " + exc.getMessage());
            }
        }
    }

    /**
     * Combines the queries in the properties to complete queries. Therefore a
     * replacement is needed: The following Strings will be replaced
     * automatically by the corresponding property-entrys:
     * ${property_key}<p>
     */
    private void combineQueries() {
        Enumeration keys = m_queries.keys();
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            // replace while there has been replacements performed
            while(replace(key));
        }
    }

    /**
     * Computes one run of the replacement for one query.
     * Stores the new value into m_queries.<p>
     * @param key the key for the query to compute.
     * @return true if in this run replacements are done.
     */
    private boolean replace(String key) {
        boolean retValue = false;
        String value = m_queries.getProperty(key);
        String newValue = new String();
        int index = 0;
        int lastIndex = 0;
        // run as long as there are "${" strings found
        while(index != -1) {
            index = value.indexOf("${", lastIndex);
            if(index != -1) {
                retValue = true;
                int nextIndex = value.indexOf('}', index);
                if(nextIndex != -1) {
                    // get the replacer key
                    String replacer = value.substring(index+2, nextIndex);
                    // copy the first part of the query
                    newValue += value.substring(lastIndex, index);
                    // copy the replcement-value
                    newValue += m_queries.getProperty(replacer, "");
                    // set up lastindex
                    lastIndex = nextIndex+1;
                } else {
                    // no key found, just copy the query-part
                    newValue += value.substring(lastIndex, index+2);
                    // set up lastindex
                    lastIndex = index+2;
                }
            } else {
                // end of the string, copy the tail into new value
                newValue += value.substring(lastIndex);
            }
        }
        // put back the new query to the queries
        m_queries.put(key, newValue);
        // returns true, if replacements were made in this run
        return retValue;
    }
    
    /**
     * Creates a new connection and prepares a statement.<p>
     * 
     * @param cms the CmsObject to get access to cms resources.
     * @param conn the Connection to use.
     * @param queryKey the key for the query to use.
     */
    public PreparedStatement sqlPrepare(CmsObject cms, Connection conn, String queryKey) throws SQLException {
        return this.sqlPrepare(cms, conn, queryKey, null);
    }

    /**
     * Replaces in a SQL statement $XXX tokens by strings and returns a prepared statement.<p>
     * 
     * @param cms the current user's CmsObject instance
     * @param conn the JDBC connection
     * @param queryKey the name of the SQL statement (in query.properties)
     * @param optionalSqlTokens a HashMap with optional SQL tokens to be replaced in the SQL statement
     * @return a new PreparedStatement
     * @throws SQLException
     */
    public PreparedStatement sqlPrepare(CmsObject cms, Connection conn, String queryKey, HashMap optionalSqlTokens) throws SQLException {
        String statement = null;
        String moduleMaster = "CMS_MODULE_MASTER";
        String channelRel = "CMS_MODULE_CHANNEL_REL";
        String media = "CMS_MODULE_MEDIA";

        // get the string of the SQL statement
        statement = m_queries.getProperty(queryKey, "");

        // choose the right tables depending on the online/offline project
        if (cms.getRequestContext().currentProject().isOnlineProject()) {
            moduleMaster = "CMS_MODULE_ONLINE_MASTER";
            channelRel = "CMS_MODULE_ONLINE_CHANNEL_REL";
            media = "CMS_MODULE_ONLINE_MEDIA";
        }

        // replace in the SQL statement the table names
        statement = CmsStringSubstitution.substitute(statement, "$CMS_MODULE_MASTER", moduleMaster);
        statement = CmsStringSubstitution.substitute(statement, "$CMS_MODULE_CHANNEL_REL", channelRel);
        statement = CmsStringSubstitution.substitute(statement, "$CMS_MODULE_MEDIA", media);

        // replace in the SQL statement further optional SQL tokens
        if (optionalSqlTokens != null) {
            Iterator optionalSqlKeys = optionalSqlTokens.keySet().iterator();
            while (optionalSqlKeys.hasNext()) {
                String currentKey = (String) optionalSqlKeys.next();
                String currentValue = (String) optionalSqlTokens.get(currentKey);
                statement = CmsStringSubstitution.substitute(statement, currentKey, currentValue);
            }
        }
        
        return conn.prepareStatement(statement);
    }
    
    /**
     * Searches for the SQL query with the specified key.
     * 
     * @param queryKey the SQL query key
     * @return String the SQL query in this property list with the specified key
     */
    public String get(String queryKey) {              
        String value = null;
        if ((value = m_queries.getProperty(queryKey)) == null) {
            if (OpenCms.getLog(CmsLog.CHANNEL_MODULE_MASTER).isErrorEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_MODULE_MASTER).error("Query '" + queryKey + "' not found");
            }
        }
        return value;
    }
    
}
