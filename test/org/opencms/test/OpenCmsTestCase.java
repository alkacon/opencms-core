/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/OpenCmsTestCase.java,v $
 * Date   : $Date: 2004/05/26 17:04:08 $
 * Version: $Revision: 1.8 $
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
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.lock.CmsLock;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
 * @version $Revision: 1.8 $
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
        
    /** The internal resource storage */
    private OpenCmsTestResourceStorage m_resourceStrorage;
    
    /** The initialized OpenCms shell instance */
    private CmsShell m_shell;    
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public OpenCmsTestCase(String arg0) {
        super(arg0);
    }
    
    /**
     * Compares the current date last modified of a resource with a given date.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param dateLastModified the last modification date
     */
    protected void assertDateLastModified(CmsObject cms, String resourceName, long dateLastModified) {
        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readFileHeader(resourceName, CmsResourceFilter.ALL);
            
            if (res.getDateLastModified() != dateLastModified) {
                fail("[DateLastModified "+dateLastModified + " <-> "+res.getDateLastModified() +"]");
            }
            
        } catch (CmsException e) {
            fail("cannot read resource "+resourceName+" "+CmsException.getStackTraceAsString(e));     
        }
    }
    
    /**
     * Tests if the the current date last modified of a resource is later then a given date.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param dateLastModified the last modification date
     */
    protected void assertDateLastModifiedAfter(CmsObject cms, String resourceName, long dateLastModified) {
        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readFileHeader(resourceName, CmsResourceFilter.ALL);
            
            if (res.getDateLastModified() < dateLastModified) {
                fail("[DateLastModified "+dateLastModified + " > "+res.getDateLastModified() +"]");
            }
            
        } catch (CmsException e) {
            fail("cannot read resource "+resourceName+" "+CmsException.getStackTraceAsString(e));     
        }
    }
    
    
    /**
     * Compares a resource to its stored version containing the state before a CmsObject
     * method was called.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param filter the filter continaing the flags defining which attributes to compare
     */
    protected void assertFilter(CmsObject cms, String resourceName, OpenCmsTestResourceFilter filter) {

        try {
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_resourceStrorage.get(resourceName);
        
            // get the actual resource from the vfs
            CmsResource res = cms.readFileHeader(resourceName, CmsResourceFilter.ALL);
            
            String noMatches = "";
            
            // compare the content Id if nescessary
            if (filter.testContentId()) {
               if (!storedResource.getFileId().equals(res.getFileId())) {
                noMatches += "[ContentId "+storedResource.getFileId()+ " <-> "+res.getFileId()+"]";
               }
            }
            // compare the contents if nescessary
            if (filter.testContents()) {
                byte[] contents;
                // we only have to do this when compareing files
                if (res.isFile()) {
                    contents = cms.readFile(resourceName, CmsResourceFilter.ALL).getContents();               
                    if (!new String(storedResource.getContents()).equals(new String(contents))) {
                        noMatches += "[Content does not match]";
                    }
                    contents = null;
                }
            }
            // compare the date creted if nescessary
            if (filter.testDateCreated()) {
               if (storedResource.getDateCreated() != res.getDateCreated() ) {
                noMatches += "[DateCreated "+storedResource.getDateCreated() + " <-> "+res.getDateCreated() +"]";
               }
            }
            // compare the date expired if nescessary
            if (filter.testDateExpired()) {
               if (storedResource.getDateExpired() != res.getDateExpired() ) {
                noMatches += "[DateExpired "+storedResource.getDateExpired() + " <-> "+res.getDateExpired() +"]";
               }
            }
            // compare the date last modified if nescessary
            if (filter.testDateLastModified()) {
               if (storedResource.getDateLastModified() != res.getDateLastModified() ) {
                noMatches += "[DateLastModified "+storedResource.getDateLastModified() + " <-> "+res.getDateLastModified() +"]";
               }
            }
            // compare the date last released if nescessary
            if (filter.testDateReleased()) {
               if (storedResource.getDateReleased() != res.getDateReleased() ) {
                noMatches += "[DateReleased "+storedResource.getDateReleased() + " <-> "+res.getDateReleased() +"]";
               }
            }
            // compare the flags if nescessary
            if (filter.testFlags()) {
               if (storedResource.getFlags() != res.getFlags() ) {
                noMatches += "[Flags "+storedResource.getFlags() + " <-> "+res.getFlags() +"]";
               }
            }
            // compare the length if nescessary
            if (filter.testLength()) {
               if (storedResource.getLength() != res.getLength() ) {
                noMatches += "[Length "+storedResource.getLength() + " <-> "+res.getLength() +"]";
               }
            }
            // compare the link count if nescessary
            if (filter.testLinkCount()) {
               if (storedResource.getLinkCount() != res.getLinkCount() ) {
                noMatches += "[LinkCount "+storedResource.getLinkCount() + " <-> "+res.getLinkCount() +"]";
               }
            }
            // compare the loader id if nescessary
            if (filter.testLoaderId()) {
               if (storedResource.getLoaderId() != res.getLoaderId() ) {
                noMatches += "[LoaderId "+storedResource.getLoaderId() + " <-> "+res.getLoaderId() +"]";
               }
            }
            // compare the lockstate if nescessary
            if (filter.testLock()) {
               CmsLock resLock = cms.getLock(res);
               if (!storedResource.getLock().equals(resLock)) {
                noMatches += "[Lockstate "+storedResource.getLock() + " <-> "+resLock +"]";
               }
            }
            // compare the name if nescessary
            if (filter.testName()) {
               if (!storedResource.getName().equals(res.getName()) ) {
                noMatches += "[Name "+storedResource.getName() + " <-> "+res.getName() +"]";
               }
            }
            // compare the parent id if nescessary
            if (filter.testParentId()) {
               if (!storedResource.getParentStructureId().equals(res.getParentStructureId()) ) {
                noMatches += "[ParentId "+storedResource.getParentStructureId() + " <-> "+res.getParentStructureId() +"]";
               }
            }
            // compare the project last modified if nescessary
            if (filter.testProjectLastModified()) {
               if (storedResource.getProjectLastModified() != res.getProjectLastModified() ) {
                noMatches += "[ProjectLastModified "+storedResource.getProjectLastModified() + " <-> "+res.getProjectLastModified() +"]";
               }
            }
            // compare the properties if nescessary
            if (filter.testProperties()) {
                noMatches += compareProperties(cms, resourceName, storedResource, null);                
            }  
            // compare the resource id if nescessary
            if (filter.testResourceId()) {
               if (!storedResource.getResourceId().equals(res.getResourceId()) ) {
                noMatches += "[ResourceId "+storedResource.getResourceId() + " <-> "+res.getResourceId() +"]";
               }
            }
            // compare the state if nescessary
            if (filter.testState()) {
               if (storedResource.getState() != res.getState() ) {
                noMatches += "[State "+storedResource.getState() + " <-> "+res.getState() +"]";
               }
            }
            // compare the structure id if nescessary
            if (filter.testStructureId()) {
               if (!storedResource.getStructureId().equals(res.getStructureId()) ) {
                noMatches += "[StructureId "+storedResource.getStructureId() + " <-> "+res.getStructureId() +"]";
               }
            }
            // compare the touched flag if nescessary
            if (filter.testTouched()) {
               if (storedResource.isTouched() != res.isTouched() ) {
                noMatches += "[Touched "+storedResource.isTouched() + " <-> "+res.isTouched() +"]";
               }
            }
            // compare the type if nescessary
            if (filter.testType()) {
               if (storedResource.getType() != res.getType() ) {
                noMatches += "[Type "+storedResource.getType() + " <-> "+res.getType() +"]";
               }
            }
            // compare the user created if nescessary
            if (filter.testUserCreated()) {
               if (!storedResource.getUserCreated().equals(res.getUserCreated()) ) {
                noMatches += "[UserCreated "+storedResource.getUserCreated() + " <-> "+res.getUserCreated() +"]";
               }
            }
            // compare the user created if nescessary
            if (filter.testUserLastModified()) {
               if (!storedResource.getUserLastModified().equals(res.getUserLastModified()) ) {
                noMatches += "[UserLastModified "+storedResource.getUserLastModified() + " <-> "+res.getUserLastModified() +"]";
               }
            }
            
            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing resource "+resourceName+" with stored values: "+noMatches);
            }
            
        } catch (CmsException e) {
            fail("cannot read resource "+resourceName+" "+CmsException.getStackTraceAsString(e));     
        }
    }
    
    
    /**
     * Compares the current project of a resource with a given CmsProject.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param project the project
     */
    protected void assertProject(CmsObject cms, String resourceName, CmsProject project) {
        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readFileHeader(resourceName, CmsResourceFilter.ALL);
            
            if (res.getProjectLastModified() != project.getId()) {
                fail("[ProjectLastModified "+project.getId() + " <-> "+res.getProjectLastModified() +"]");
            }
            
        } catch (CmsException e) {
            fail("cannot read resource "+resourceName+" "+CmsException.getStackTraceAsString(e));     
        }
    }
    
     /**
     * Compares the current properties of a resource with the stored values and a given, changed property.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param property the changed property
     */
    protected void assertPropertyChanged(CmsObject cms, String resourceName, CmsProperty property) {
        try {
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_resourceStrorage.get(resourceName);
            
            // create the exclude list
            List excludeList = new ArrayList();
            excludeList.add(property);            
            
            String noMatches = compareProperties(cms, resourceName, storedResource, excludeList);   
            
            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing resource "+resourceName+" with stored values: "+noMatches);
            }   
            
            // finally test if the values of the changed properties are correct.
            CmsProperty resourceProperty = cms.readPropertyObject(resourceName, property.getKey(), false);
            if (!resourceProperty.isIdentical(property)) {
                fail("properties are not identical :"+property+" <-> "+ resourceProperty);              
            }
            
            
        } catch (CmsException e) {
            fail("cannot read resource "+resourceName+" "+CmsException.getStackTraceAsString(e));     
        }
    }
     
     /**
     * Compares the current properties of a resource with the stored values and a list of changed property.<p>
     * 
     * @param cms an initialized CmsObject
     * @param resourceName the name of the resource to compare
     * @param excludeList a list of CmsProperties to exclude
     */
    protected void assertPropertyChanged(CmsObject cms, String resourceName, List excludeList) {
        try {
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_resourceStrorage.get(resourceName);    
            
            String noMatches = compareProperties(cms, resourceName, storedResource, excludeList);   
            
            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing resource "+resourceName+" with stored values: "+noMatches);
            }   
            
        } catch (CmsException e) {
            fail("cannot read resource "+resourceName+" "+CmsException.getStackTraceAsString(e));     
        }
    } 
     
     
    /**
     * Compares the current properties of a resource with the stored values.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     */
    protected void assertPropertyEqual(CmsObject cms, String resourceName) {
        try {
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_resourceStrorage.get(resourceName);
            String noMatches = compareProperties(cms, resourceName, storedResource, null);   
            
            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing resource "+resourceName+" with stored values: "+noMatches);
            }   
            
        } catch (CmsException e) {
            fail("cannot read resource "+resourceName+" "+CmsException.getStackTraceAsString(e));     
        }
    }
    
    /**
     * Compares the current state of a resource with a given state.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param state the state
     */
    protected void assertState(CmsObject cms, String resourceName, int state) {
        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readFileHeader(resourceName, CmsResourceFilter.ALL);
            
            if (res.getState() != state) {
                fail("[State "+state + " <-> "+res.getState() +"]");
            }
            
        } catch (CmsException e) {
            fail("cannot read resource "+resourceName+" "+CmsException.getStackTraceAsString(e));     
        }
    }
    
    /**
     * Compares the current user last modified of a resource with a given user.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param user the last modification user
     */
    protected void assertUserLastModified(CmsObject cms, String resourceName, CmsUser user) {
        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readFileHeader(resourceName, CmsResourceFilter.ALL);
            
            if (!res.getUserLastModified().equals(user.getId())) {
                fail("[UserLastModified ("+user.getName()+") "+user.getId() + " <-> "+res.getUserLastModified() +"]");
            }
            
        } catch (CmsException e) {
            fail("cannot read resource "+resourceName+" "+CmsException.getStackTraceAsString(e));     
        }
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
     * Imports a resource into the Cms.<p>
     * 
     * @param cms an initialized CmsObject
     * @param importFile the name (absolute Path) of the import resource (zip or folder)
     * @param targetPath the name (absolute Path) of the target folder in the VFS
     * @throws CmsException if something goes wrong
     */
    protected void importResources(CmsObject cms, String importFile, String targetPath) throws CmsException {
        OpenCms.getImportExportManager().importData(cms, getTestDataPath() + File.separator + "imports" + File.separator + importFile, targetPath, new CmsShellReport());
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
    
    /**
     * Sets the mapping for resourcenames.<p>
     *
     * @param source the source resource name
     * @param target the target resource name
     */
    protected void setMapping(String source, String target) {        
        m_resourceStrorage.setMapping(source, target);
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

        // init the storage
        m_resourceStrorage = new OpenCmsTestResourceStorage(cms);
        
        // return the initialized cms context Object
        return cms;
    }
    
    /**
     * Stores the state (e.g. attributes, properties, content, lock state and ACL) of 
     * a resource in the internal resource storage.<p>
     * 
     * If the resourceName is the name of a folder in the vfs, all subresoruces are stored as well.
     *   
     * @param cms an initialized CmsObject
     * @param resourceName the name of the resource in the vfs
     */
    protected void storeResources(CmsObject cms, String resourceName) {
        try {
            CmsResource resource = cms.readFileHeader(resourceName, CmsResourceFilter.ALL);
            // test if the name belongs to a file or folder
            if (resource.isFile()) {
                m_resourceStrorage.add(resourceName, resource);
            } else {
                // TODO: add handling for subtrees here
            }
            } catch (CmsException e) {
                fail("cannot read resource "+resourceName+" "+CmsException.getStackTraceAsString(e));                
            }
    }
    
    
    
    /**
     * Compares two lists of CmsProperty objects and creates a list of all properties which are
     * not included in a seperate exclude list.
     * @param cms the CmsObject
     * @param resourceName the name of the resource the properties belong to
     * @param storedResource the stored resource corresponding to the resourcename
     * @param excludeList the list of properies to exclude in the test or null
     * @return list of CmsProperty objects 
     * @throws CmsException if something goes wrong
     */
    private String compareProperties(CmsObject cms, String resourceName, OpenCmsTestResourceStorageEntry storedResource, List excludeList) 
        throws CmsException {
            String noMatches = "";
            List storedProperties = storedResource.getProperties();
            List properties = cms.readPropertyObjects(resourceName, false);
            List unmatchedProperties;
            unmatchedProperties = OpenCmsTestResourceFilter.compareProperties(storedProperties, properties, excludeList);
            if (unmatchedProperties.size() >0 ) {
                noMatches += "[Properies missing "+unmatchedProperties.toString()+"]";   
            }
            unmatchedProperties = OpenCmsTestResourceFilter.compareProperties(properties, storedProperties, excludeList);
            if (unmatchedProperties.size() >0 ) {
                noMatches += "[Properies additional "+unmatchedProperties.toString()+"]";   
            } 
            return noMatches;
    }
    
    
}
