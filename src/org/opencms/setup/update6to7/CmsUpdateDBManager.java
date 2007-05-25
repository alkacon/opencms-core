/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/CmsUpdateDBManager.java,v $
 * Date   : $Date: 2007/05/25 11:54:08 $
 * Version: $Revision: 1.1.2.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.update6to7;

import org.opencms.setup.CmsSetupBean;
import org.opencms.setup.update6to7.generic.CmsUpdateDBAlterTables;
import org.opencms.setup.update6to7.generic.CmsUpdateDBCmsUsers;
import org.opencms.setup.update6to7.generic.CmsUpdateDBContentTables;
import org.opencms.setup.update6to7.generic.CmsUpdateDBCreateIndexes7;
import org.opencms.setup.update6to7.generic.CmsUpdateDBDropBackupTables;
import org.opencms.setup.update6to7.generic.CmsUpdateDBDropOldIndexes;
import org.opencms.setup.update6to7.generic.CmsUpdateDBDropUnusedTables;
import org.opencms.setup.update6to7.generic.CmsUpdateDBHistoryPrincipals;
import org.opencms.setup.update6to7.generic.CmsUpdateDBHistoryTables;
import org.opencms.setup.update6to7.generic.CmsUpdateDBNewTables;
import org.opencms.setup.update6to7.generic.CmsUpdateDBProjectId;
import org.opencms.setup.update6to7.generic.CmsUpdateDBUpdateOU;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;

/**
 * This manager controls the update of the database from OpenCms 6 to OpenCms 7.<p>
 * 
 * @author metzler
 *
 */
public class CmsUpdateDBManager {

    /** The database name. */
    private String m_dbName;

    /** The pools connection data. */
    private Map m_dbPools = new HashMap();

    /** List of xml update plugins. */
    private List m_plugins;

    /**
     * Default constructor.<p>
     */
    public CmsUpdateDBManager() {

        // no-op
    }

    /**
     * Returns the configured jdbc driver for the given pool.<p>
     * 
     * @param pool the db pool to get the driver for
     * 
     * @return the driver class name
     */
    public String getDbDriver(String pool) {

        return (String)((Map)m_dbPools.get(pool)).get("driver");
    }

    /**
     * Returns the database name.<p>
     *
     * @return the database name
     */
    public String getDbName() {

        return m_dbName;
    }

    /**
     * Returns the configured jdbc url parameters for the given pool.<p>
     * 
     * @param pool the db pool to get the params for
     * 
     * @return the jdbc url parameters
     */
    public String getDbParams(String pool) {

        return (String)((Map)m_dbPools.get(pool)).get("params");
    }

    /**
     * Returns the configured jdbc connection url for the given pool.<p>
     * 
     * @param pool the db pool to get the url for
     * 
     * @return the jdbc connection url
     */
    public String getDbUrl(String pool) {

        return (String)((Map)m_dbPools.get(pool)).get("url");
    }

    /**
     * Returns the configured database user for the given pool.<p>
     * 
     * @param pool the db pool to get the user for
     * 
     * @return the database user
     */
    public String getDbUser(String pool) {

        return (String)((Map)m_dbPools.get(pool)).get("user");
    }

    /**
     * Returns all configured database pools.<p>
     * 
     * @return a list of {@link String} objects
     */
    public List getPools() {

        return new ArrayList(m_dbPools.keySet());
    }

    /**
     * Initializes the Update Manager object with the updateBean to get the database connection.<p>
     * 
     * @param updateBean the update bean with the database connection
     * 
     * @throws Exception if the setup bean is not initialized 
     */
    public void initialize(CmsSetupBean updateBean) throws Exception {

        if (updateBean.isInitialized()) {
            ExtendedProperties props = updateBean.getProperties();

            // Initialize the CmsUUID generator.
            CmsUUID.init(props.getString("server.ethernet.address"));

            m_dbName = props.getString("db.name");

            List pools = props.getList("db.pools");
            for (Iterator it = pools.iterator(); it.hasNext();) {
                String pool = (String)it.next();
                Map data = new HashMap();
                data.put("driver", props.getString("db.pool." + pool + ".jdbcDriver"));
                data.put("url", props.getString("db.pool." + pool + ".jdbcUrl"));
                data.put("params", props.getString("db.pool." + pool + ".jdbcUrl.params"));
                data.put("user", props.getString("db.pool." + pool + ".user"));
                data.put("pwd", props.getString("db.pool." + pool + ".password"));
                m_dbPools.put(pool, data);
            }
        } else {
            throw new Exception("setup bean not initialized");
        }
    }

    /**
     * Updates all database pools.<p>
     */
    public void run() {

        try {
            // add a list of plugins to execute
            // be sure to use the right order 
            m_plugins = new ArrayList();

            m_plugins.add(new CmsUpdateDBDropOldIndexes());
            m_plugins.add(new CmsUpdateDBUpdateOU());
            m_plugins.add(new CmsUpdateDBCmsUsers());
            m_plugins.add(new CmsUpdateDBProjectId());
            m_plugins.add(new CmsUpdateDBNewTables());
            m_plugins.add(new CmsUpdateDBHistoryTables());
            m_plugins.add(new CmsUpdateDBHistoryPrincipals());
            m_plugins.add(new CmsUpdateDBDropUnusedTables());
            m_plugins.add(new CmsUpdateDBContentTables());
            m_plugins.add(new CmsUpdateDBAlterTables());
            m_plugins.add(new CmsUpdateDBDropBackupTables());
            m_plugins.add(new CmsUpdateDBCreateIndexes7());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        Iterator it = getPools().iterator();
        while (it.hasNext()) {
            String dbPool = (String)it.next();
            System.out.println("Starting DB Update for pool " + dbPool + "... ");

            try {
                updateDatabase(dbPool);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            System.out.println("... DB Update finished for " + dbPool + ".");
        }
    }

    /**
     * Updates the database.<p>
     * 
     * @param pool the database pool to update
     */
    public void updateDatabase(String pool) {

        System.out.println("JDBC Driver:                " + getDbDriver(pool));
        System.out.println("JDBC Connection Url:        " + getDbUrl(pool));
        System.out.println("JDBC Connection Url Params: " + getDbParams(pool));
        System.out.println("Database User:              " + getDbUser(pool));

        Iterator it = m_plugins.iterator();
        while (it.hasNext()) {
            I_CmsUpdateDBPart updatePart = (I_CmsUpdateDBPart)it.next();
            updatePart.execute((Map)m_dbPools.get(pool));
        }
    }
}
