/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/setup/Attic/CmsSetupDbTest.java,v $
 * Date   : $Date: 2004/05/24 09:30:33 $
 * Version: $Revision: 1.1 $
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
 
package org.opencms.setup;

import org.opencms.db.CmsDbPool;
import org.opencms.util.CmsPropertyUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.commons.collections.ExtendedProperties;

/** 
 * Tests the database creation / removal used during setup.<p>
 * 
 * To run this test you might have to change the database connection
 * values in the provides <code>opencms.properties</code> file.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.3.5
 */
public class CmsSetupDbTest extends TestCase {

    /** Name of the test database instance */
    public static final String C_DATABASE_NAME = "ocjutest";
    
    /** DB product used for the tests */
    public static final String C_DB_PRODUCT = "mysql";
            
    /**
     * Returns an initialized DB setup object.<p>
     *  
     * @param dbProduct the name of the DB product to use, e.g. "mysql"
     * @param create if true, the DB will be initialized for creation
     * @return the initialized setup DB object
     */
    private CmsSetupDb getSetupDb(String dbProduct, boolean create) {
        
        // get URL of test input resource
        URL basePathUrl = ClassLoader.getSystemResource("./");
        // check if the db setup files are available
        File setupDataFolder = new File(basePathUrl.getFile() + "../webapp/setup/");
        if (! setupDataFolder.exists()) {
            fail("DB setup scripts not available at " + setupDataFolder.getAbsolutePath());
        }
        
        // set the base path
        String basePath = new File(basePathUrl.getFile() + "../webapp/").getAbsolutePath() + File.separator;
        
        ExtendedProperties dbConfiguration;
        ExtendedProperties configuration;
        try {
            // load DB configuration
            String dbConfigFile = basePath + "setup" + File.separator + "database" + File.separator + dbProduct + File.separator + "database.properties";
            dbConfiguration = CmsPropertyUtils.loadProperties(dbConfigFile);
                    
            // load test configuration
            String propertyFile = basePathUrl.getFile() + "org/opencms/setup/opencms.properties";        
            configuration = CmsPropertyUtils.loadProperties(propertyFile);
            configuration.setProperty("DATABASE_NAME", C_DATABASE_NAME);
        } catch (IOException e) {
            fail(e.toString());
            return null;
        }
        
        // get connection values from properties
        String key = "default";        
        String jdbcDriver = configuration.getString(CmsDbPool.C_KEY_DATABASE_POOL + "." + key + "." + CmsDbPool.C_KEY_JDBC_DRIVER);
        String username = configuration.getString(CmsDbPool.C_KEY_DATABASE_POOL + "." + key + "." + CmsDbPool.C_KEY_USERNAME);
        String password = configuration.getString(CmsDbPool.C_KEY_DATABASE_POOL + "." + key + "." + CmsDbPool.C_KEY_PASSWORD);        

        String jdbcUrl;
        if (create) {
            jdbcUrl = dbConfiguration.getString(dbProduct + ".constr");
        } else {
            jdbcUrl = configuration.getString(CmsDbPool.C_KEY_DATABASE_POOL + "." + key + "." + CmsDbPool.C_KEY_JDBC_URL);
        }
        
        // create setup DB instance
        CmsSetupDb setupDb = new CmsSetupDb(basePath);
        
        // connecto to the DB
        setupDb.setConnection(jdbcDriver, jdbcUrl, username, password);
                
        // check for errors 
        checkErrors(setupDb);
        
        // connect to the DB
        return setupDb;
    }
    
    /**
     * Check the setup DB for errors that might have occured.<p>
     * 
     * @param setupDb the setup DB object to check
     */
    private void checkErrors(CmsSetupDb setupDb) {
        if (! setupDb.noErrors()) {
            Vector errors = setupDb.getErrors();
            for (Iterator i = errors.iterator(); i.hasNext();) {
                String error = (String)i.next();
                System.err.println(error);
            }
            fail((String)setupDb.getErrors().get(0));
        }                
    }

    /**
     * Returns an initialized replacer map.<p>
     * 
     * @return an initialized replacer map
     */
    private Map getReplacer() {
        Map replacer = new HashMap();
        replacer.put("${database}", C_DATABASE_NAME);
        return replacer;
    }
    
    /**
     * Tests database creation.<p>
     */
    public void testCreateDatabase() {
           
        // create a setup DB object 
        CmsSetupDb setupDb = getSetupDb(C_DB_PRODUCT, true);
        
        // create the database
        setupDb.createDatabase(C_DB_PRODUCT, getReplacer());        
        
        // check for errors 
        checkErrors(setupDb);        
    }
    
    /**
     * Tests table creation.<p>
     */
    public void testCreateTables() {

        // create a setup DB object 
        CmsSetupDb setupDb = getSetupDb(C_DB_PRODUCT, false);
        
        // create the database tables
        setupDb.createTables(C_DB_PRODUCT, getReplacer());      
        
        // check for errors 
        checkErrors(setupDb);        
    }
    
    /**
     * Tests table removal.<p>
     */
    public void testDropTables() {
        
        // create a setup DB object 
        CmsSetupDb setupDb = getSetupDb(C_DB_PRODUCT, false);
        
        // create the database
        setupDb.dropTables(C_DB_PRODUCT);
        
        // check for errors 
        checkErrors(setupDb);        
    }
 
    /**
     * Tests database removal.<p>
     */
    public void testDropDatabase() {

        // create a setup DB object for DB creation
        CmsSetupDb setupDb = getSetupDb(C_DB_PRODUCT, true);
        
        // drop the database
        setupDb.dropDatabase(C_DB_PRODUCT, getReplacer());
        
        // check for errors 
        checkErrors(setupDb);        
    }
}
