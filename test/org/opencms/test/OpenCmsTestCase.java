/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/OpenCmsTestCase.java,v $
 * Date   : $Date: 2004/05/25 09:44:01 $
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
 
package org.opencms.test;
import org.opencms.db.CmsDbPool;
import org.opencms.setup.CmsSetupDb;
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
 * Extends the JUnit standard with methods to handle an OpenCms database
 * test instance.<p>
 * 
 * The required configuration files are located in the 
 * <code>../test/data/WEB-INF</code> folder structure.<p>
 * 
 * To run this test you might have to change the database connection
 * values in the provided <code>./test/data/WEB-INF/config/opencms.properties</code> file.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.3.5
 */
public class OpenCmsTestCase extends TestCase {

    /** Name of the test database instance */
    public static final String C_DATABASE_NAME = "ocjutest";
    
    /** DB product used for the tests */
    public static final String C_DB_PRODUCT = "mysql";

    /** The path to the default setup data files */
    private static String m_setupDataPath;
    
    /** The path to the additional test data files */
    private static String m_testDataPath;
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public OpenCmsTestCase(String arg0) {
        super(arg0);
    }
    
    /**
     * Returns the path to the data files used by the setup wizard.<p>
     * 
     * Whenever possible use this path to ensure that the files 
     * used for testing are actually the same as for the setup.<p>
     * 
     * @return the path to the data files used by the setup wizard
     */
    protected synchronized String getSetupDataPath() {
        
        if (m_setupDataPath == null) {
            // get URL of test input resource
            URL basePathUrl = ClassLoader.getSystemResource("./");

            // check if the db setup files are available
            File setupDataFolder = new File(basePathUrl.getFile() + "../webapp/");
            if (!setupDataFolder.exists()) {
                fail("DB setup data not available at " + setupDataFolder.getAbsolutePath());
            }
            m_setupDataPath = setupDataFolder.getAbsolutePath() + File.separator;
        }
        // return the path name
        return m_setupDataPath;
    }
        
    /**
     * Returns the path to the test data configuration files.<p>
     * 
     * Use this path in case you require input files for testing 
     * that are modified or otherwise different from the setup data.<p>
     * 
     * @return the path to the test data configuration files
     */    
    protected synchronized String getTestDataPath() {

        if (m_testDataPath == null) {
            // get URL of test input resource
            URL basePathUrl = ClassLoader.getSystemResource("./");

            // check if the db setup files are available
            File testDataFolder = new File(basePathUrl.getFile() + "../test/data/");
            if (!testDataFolder.exists()) {
                fail("DB setup data not available at " + testDataFolder.getAbsolutePath());
            }
            m_testDataPath = testDataFolder.getAbsolutePath() + File.separator;
        }
        // return the path name
        return m_testDataPath;    
    }

    /**
     * Returns an initialized DB setup object.<p>
     *  
     * @param dbProduct the name of the DB product to use, e.g. "mysql"
     * @param create if true, the DB will be initialized for creation
     * @return the initialized setup DB object
     */
    protected CmsSetupDb getSetupDb(String dbProduct, boolean create) {
        
        ExtendedProperties dbConfiguration;
        ExtendedProperties configuration;
        try {
            // load DB configuration
            String dbConfigFile = getSetupDataPath() + "setup/database/" + dbProduct + "/database.properties";
            dbConfiguration = CmsPropertyUtils.loadProperties(dbConfigFile);
                    
            // load test configuration
            String propertyFile = getTestDataPath() + "WEB-INF/config/opencms.properties";        
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
        CmsSetupDb setupDb = new CmsSetupDb(getSetupDataPath());
        
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
    protected void checkErrors(CmsSetupDb setupDb) {
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
    protected Map getReplacer() {
        Map replacer = new HashMap();
        replacer.put("${database}", C_DATABASE_NAME);
        return replacer;
    }
    
    /**
     * Tests database creation.<p>
     * 
     * @return the setup DB object used for connection to the DB
     */
    protected CmsSetupDb createDatabase() {
           
        // create a setup DB object 
        CmsSetupDb setupDb = getSetupDb(C_DB_PRODUCT, true);
        
        // create the database
        setupDb.createDatabase(C_DB_PRODUCT, getReplacer());        
        return setupDb;
    }
    
    /**
     * Tests table creation.<p>
     * 
     * @return the setup DB object used for connection to the DB
     */
    protected CmsSetupDb createTables() {

        // create a setup DB object 
        CmsSetupDb setupDb = getSetupDb(C_DB_PRODUCT, false);
        
        // create the database tables
        setupDb.createTables(C_DB_PRODUCT, getReplacer());      
        return setupDb;     
    }
    
    /**
     * Tests table removal.<p>
     * 
     * @return the setup DB object used for connection to the DB
     */
    protected CmsSetupDb dropTables() {
        
        // create a setup DB object 
        CmsSetupDb setupDb = getSetupDb(C_DB_PRODUCT, false);
        
        // create the database
        setupDb.dropTables(C_DB_PRODUCT);
        return setupDb;      
    }
 
    /**
     * Tests database removal.<p>
     * 
     * @return the setup DB object used for connection to the DB
     */
    protected CmsSetupDb dropDatabase() {

        // create a setup DB object for DB creation
        CmsSetupDb setupDb = getSetupDb(C_DB_PRODUCT, true);
        
        // drop the database
        setupDb.dropDatabase(C_DB_PRODUCT, getReplacer());
        return setupDb;       
    }
    
    /**
     * Creates a new OpenCms test database including the tables.<p>
     * 
     * Any existing instance of the test database is forcefully removed first.<p>
     */
    protected void setupDatabase() {
                
        // first kill any existing old database instance
        dropDatabase();
        
        // now setup the new instance
        CmsSetupDb setupDb;
        setupDb = createDatabase();
        checkErrors(setupDb);        
        setupDb = createTables();            
        checkErrors(setupDb);
    }
    
    /**
     * Removes the OpenCms database test instance.<p>
     */
    protected void removeDatabase() {
        
        CmsSetupDb setupDb;
        setupDb = dropTables();
        checkErrors(setupDb);
        setupDb = dropDatabase();
        checkErrors(setupDb);
    }    
}
