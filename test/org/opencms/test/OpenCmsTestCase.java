/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/OpenCmsTestCase.java,v $
 * Date   : $Date: 2004/05/26 08:01:45 $
 * Version: $Revision: 1.2 $
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
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsShell;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.setup.CmsSetupDb;
import org.opencms.staticexport.CmsStaticExportManager;
import org.opencms.util.CmsPropertyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
 * @version $Revision: 1.2 $
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
    
    /**
     * Imports a resource into the Cms.<p>
     * 
     * @param cms an initialized CmsObject
     * @param importFile the name (absolute Path) of the import resource (zip or folder)
     * @param targetPath the name (absolute Path) of the target folder in the VFS
     * @throws CmsException if something goes wrong
     */
    public void importResources(CmsObject cms, String importFile, String targetPath) throws CmsException {
        OpenCms.getImportExportManager().importData(cms, getTestDataPath() + File.separator + "imports" + File.separator + importFile, targetPath, new CmsShellReport());
    }    
    
    /** The initialized OpenCms shell instance */
    private CmsShell m_shell;    
    
    /**
     * Sets up a complete OpenCms instance, creating the usual projects,
     * and importing a default database.<p>
     * 
     * @param importFolder the folder to import in the "real" FS
     * @param targetFolder the target folder of the import in the VFS
     * @return an initialized OpenCms context with "Admin" user in the "Offline" project with the site root set to "/" 
     * @throws FileNotFoundException in case of file access errors
     * @throws CmsException in case of OpenCms access errors
     */
    protected CmsObject setupOpenCms(String importFolder, String targetFolder) throws FileNotFoundException, CmsException {
        // create a new database first
        setupDatabase();
        
        // kill any old shell that might have remained from a previous test 
        if (m_shell != null) {
            try {
                m_shell.exit();
                m_shell = null;
            } catch (Throwable t) {
                // ignore
            }
        }
        
        // create a shell instance
        m_shell = new CmsShell(
            getTestDataPath() + "WEB-INF" + File.separator,
            "${user}@${project}>", 
            null);
        
        // open the test script 
        File script;
        FileInputStream stream = null;
        
        // start the shell with the base script
        script = new File(getTestDataPath() + "scripts/script_base.txt");
        stream = new FileInputStream(script);
        m_shell.start(stream);
        
        // add the default folders by script
        script = new File(getTestDataPath() + "scripts/script_default_folders.txt");
        stream = new FileInputStream(script);        
        m_shell.start(stream); 
        
        // log in the Admin user and switch to the setup project
        CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("_setupProject"));
        
        // import the "simpletest" files
        importResources(cms, importFolder, targetFolder);  
        
        // publish the current project by script
        script = new File(getTestDataPath() + "scripts/script_publish.txt");
        stream = new FileInputStream(script);        
        m_shell.start(stream);      
        
        // switch to the "Offline" project
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.getRequestContext().setSiteRoot("/sites/default/");               
                
        // return the initialized cms context Object
        return cms;
    }
    
    /**
     * Removes the initialized OpenCms database and all 
     * temporary files created during the test run.<p>
     */
    protected void removeOpenCms() {
        
        // exit the shell
        m_shell.exit();
        
        // remove the database
        removeDatabase();

        // get the name of the folder for the backup configuration files
        File configBackupDir = new File(getTestDataPath() + "WEB-INF/config/backup/");
        
        // remove the backup configuration files
        CmsStaticExportManager.purgeDirectory(configBackupDir);        
    }
}
