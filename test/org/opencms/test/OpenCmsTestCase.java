/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/OpenCmsTestCase.java,v $
 * Date   : $Date: 2004/08/20 11:44:49 $
 * Version: $Revision: 1.42 $
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
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertydefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsShell;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.setup.CmsSetupDb;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsPropertyUtils;
import org.opencms.util.CmsUUID;

import java.io.File;
import java.io.FileInputStream;
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
 * @version $Revision: 1.42 $
 * 
 * @since 5.3.5
 */
public class OpenCmsTestCase extends TestCase {

    /** Key for tests on MySql database. */
    public static final String C_DB_MYSQL = "mysql";
    
    /** Key for tests on Oracle database. */
    public static final String C_DB_ORACLE = "oracle";
      
    /** The OpenCms/database configuration. */
    public static ExtendedProperties m_configuration = null;
    
    /** The name of the database. */
    public static String m_dbName;
    
    /** DB product used for the tests. */
    public static String m_dbProduct = C_DB_MYSQL;
    
    /** Name of the default tablespace (oracle only). */
    public static String m_defaultTablespace;
    
    /** Name of the index tablespace (oracle only). */
    public static String m_indexTablespace;
    
    /** The database driver. */
    public static String m_jdbcDriver;
    
    /** The database url. */
    public static String m_jdbcUrl;
    
    /** Additional database params. */
    public static String m_jdbcUrlParams;
    
    /** The internal storages. */
    public static HashMap m_resourceStorages;

    /** The name of the setup user. */
    public static String m_setupName;

    /** The password of the setup user. */
    public static String m_setupPassword;

    /** The setup jdbc url. */
    public static String m_setupUrl;

    /** Name of the temporary tablespace (oracle only). */
    public static String m_tempTablespace;

    /** The name of the user. */
    public static String m_userName;

    /** The password of the user. */
    public static String m_userPassword;

    /** The file date of the configuration files. */
    private static long[] m_dateConfigFiles;

    /** The path to the default setup data files. */
    private static String m_setupDataPath;

    /** The initialized OpenCms shell instance. */
    private static CmsShell m_shell;

    /** The path to the additional test data files. */
    private static String m_testDataPath;

    /** The current resource storage. */
    public OpenCmsTestResourceStorage m_currentResourceStrorage;

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public OpenCmsTestCase(String arg0) {
        super(arg0);
        OpenCmsTestLogAppender.setBreakOnError(false);
        if (m_resourceStorages == null) {
            m_resourceStorages = new HashMap();
        }
        
        // initialize configuration
        initConfiguration();
        
        // set "OpenCmsLog" system property to enable the logger
        System.setProperty(CmsLog.SYSPROP_LOGFILE, "opencms_test.log");
        OpenCmsTestLogAppender.setBreakOnError(true);       
    }
    
    /**
     * Removes the initialized OpenCms database and all 
     * temporary files created during the test run.<p>
     */
    public static void removeOpenCms() {
        
        // ensure logging does not throw exceptions
        OpenCmsTestLogAppender.setBreakOnError(false);

        // output a message
        m_shell.printPrompt(); 
        System.out.println("----- Test cases finished -----");        

        // exit the shell
        m_shell.exit();
        
        // remove the database
        removeDatabase();
        
        // copy the configuration files to re-create the original configuration
        String configFolder = getTestDataPath() + "WEB-INF/config." + m_dbProduct + "/";
        copyConfiguration(configFolder);

        // remove potentially created "classes, "lib" and "backup" folder
        CmsFileUtil.purgeDirectory(new File(getTestDataPath() + "WEB-INF/classes/"));        
        CmsFileUtil.purgeDirectory(new File(getTestDataPath() + "WEB-INF/lib/"));
        CmsFileUtil.purgeDirectory(new File(getTestDataPath() + "WEB-INF/config/backup/"));        
    }

    /**
     * Sets up a complete OpenCms instance with configuration from the config-ori folder, 
     * creating the usual projects, and importing a default database.<p>
     * 
     * @param importFolder the folder to import in the "real" FS
     * @param targetFolder the target folder of the import in the VFS
     * @return an initialized OpenCms context with "Admin" user in the "Offline" project with the site root set to "/" 
     */
    public static CmsObject setupOpenCms(String importFolder, String targetFolder) {
    
        return setupOpenCms(importFolder, targetFolder, getTestDataPath() + "WEB-INF/config." + m_dbProduct + "/");
    }
    
    /**
     * Sets up a complete OpenCms instance, creating the usual projects,
     * and importing a default database.<p>
     * 
     * @param importFolder the folder to import in the "real" FS
     * @param targetFolder the target folder of the import in the VFS
     * @param configFolder the folder to copy the configuration files
     * @return an initialized OpenCms context with "Admin" user in the "Offline" project with the site root set to "/" 
     */
    public static CmsObject setupOpenCms(String importFolder, String targetFolder, String configFolder) {
        
        // turn off exceptions after error logging during setup (won't work otherwise)
        OpenCmsTestLogAppender.setBreakOnError(false);
        // output a message 
        System.out.println("\n\n\n----- Starting test case: Importing OpenCms VFS data -----");
                
        // kill any old shell that might have remained from a previous test 
        if (m_shell != null) {
            try {
                m_shell.exit();
                m_shell = null;
            } catch (Throwable t) {
                // ignore
            }
        }
        
        // copy the configuration files
        copyConfiguration(configFolder);

        // create a new database first
        setupDatabase();
        
        // create a shell instance
        m_shell = new CmsShell(getTestDataPath() + "WEB-INF" + File.separator, "${user}@${project}>", null);
        
        // open the test script 
        File script;
        FileInputStream stream = null;
        CmsObject cms = null;
        
        try {
            // start the shell with the base script
            script = new File(getTestDataPath() + "scripts/script_base.txt");
            stream = new FileInputStream(script);
            m_shell.start(stream);
            
            // add the default folders by script
            script = new File(getTestDataPath() + "scripts/script_default_folders.txt");
            stream = new FileInputStream(script);        
            m_shell.start(stream); 
            
            // log in the Admin user and switch to the setup project
            cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
            cms.loginUser("Admin", "admin");
            cms.getRequestContext().setCurrentProject(cms.readProject("_setupProject"));
            
            if (importFolder != null) {
                // import the "simpletest" files
                importResources(cms, importFolder, targetFolder);
                
                // publish the current project by script
                script = new File(getTestDataPath() + "scripts/script_publish.txt");
                stream = new FileInputStream(script);        
                m_shell.start(stream);
            } else {
                // unlock project resources if not published
                cms.unlockProject(cms.readProject("_setupProject").getId());
            }
            
            // create the default projects by script
            script = new File(getTestDataPath() + "scripts/script_default_projects.txt");
            stream = new FileInputStream(script);        
            m_shell.start(stream);            
            
            // switch to the "Offline" project
            cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
            cms.getRequestContext().setSiteRoot("/sites/default/");               
            
            // output a message 
            System.out.println("----- Starting test cases -----");
        } catch (Throwable t) {
            fail("Unable to setup OpenCms\n" + CmsException.getStackTraceAsString(t));
        }
        // turn on exceptions after error logging
        OpenCmsTestLogAppender.setBreakOnError(true);
        // return the initialized cms context Object
        return cms;
    }
    
    /**
     * Check the setup DB for errors that might have occured.<p>
     * 
     * @param setupDb the setup DB object to check
     */
    protected static void checkErrors(CmsSetupDb setupDb) {
        if (! setupDb.noErrors()) {
            Vector errors = setupDb.getErrors();
            for (Iterator i = errors.iterator(); i.hasNext();) {
                String error = (String)i.next();
                System.out.println(error);
            }
            fail((String)setupDb.getErrors().get(0));
        }                
    }
    
    /**
     * Tests database creation.<p>
     * 
     * @return the setup DB object used for connection to the DB
     */    
    protected static CmsSetupDb createDatabase() {
                
        // create a setup DB object 
        CmsSetupDb setupDb = getSetupDb(true);
        
        // create the database
        setupDb.createDatabase(m_dbProduct, getReplacer());        
        return setupDb;
    }
    
    /**
     * Tests table creation.<p>
     * 
     * @return the setup DB object used for connection to the DB
     */
    protected static CmsSetupDb createTables() {

        // create a setup DB object 
        CmsSetupDb setupDb = getSetupDb(false);
        
        // create the database tables
        setupDb.createTables(m_dbProduct, getReplacer());      
        return setupDb;     
    }
 
    /**
     * Tests database removal.<p>
     * 
     * @return the setup DB object used for connection to the DB
     */
    protected static CmsSetupDb dropDatabase() {

        // create a setup DB object for DB creation
        CmsSetupDb setupDb = getSetupDb(true);
        
        // drop the database
        setupDb.dropDatabase(m_dbProduct, getReplacer());
        return setupDb;       
    }
    
    /**
     * Tests table removal.<p>
     * 
     * @return the setup DB object used for connection to the DB
     */
    protected static CmsSetupDb dropTables() {
        
        // create a setup DB object 
        CmsSetupDb setupDb = getSetupDb(false);
        
        // create the database
        setupDb.dropTables(m_dbProduct);
        return setupDb;      
    }

    /**
     * Returns an initialized replacer map.<p>
     * 
     * @return an initialized replacer map
     */
    protected static Map getReplacer() {
        
        Map replacer = new HashMap();
        replacer.put("${database}", m_dbName);
        replacer.put("${user}", m_userName);
        replacer.put("${password}", m_userPassword);
        replacer.put("${defaultTablespace}", m_defaultTablespace);
        replacer.put("${indexTablespace}", m_indexTablespace);
        replacer.put("${temporaryTablespace}", m_tempTablespace);
            
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
    protected static synchronized String getSetupDataPath() {
        
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
     * @param create if true, the DB will be initialized for creation
     * @return the initialized setup DB object
     */
    protected static CmsSetupDb getSetupDb(boolean create) {
                 
        // create setup DB instance
        CmsSetupDb setupDb = new CmsSetupDb(getSetupDataPath());
        
        // connecto to the DB
        if (create) {
            setupDb.setConnection(m_jdbcDriver, m_setupUrl, m_jdbcUrlParams, m_setupName, m_setupPassword);
        } else {
            setupDb.setConnection(m_jdbcDriver, m_jdbcUrl, m_jdbcUrlParams, m_userName, m_userPassword);
        }
        
        // check for errors 
        if (!C_DB_ORACLE.equals(m_dbProduct)) {
            checkErrors(setupDb);
        }
        
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
    protected static synchronized String getTestDataPath() {

        if (m_testDataPath == null) {
            // get URL of test input resource
            URL basePathUrl = ClassLoader.getSystemResource("./");

            // check if the db setup files are available
            File testDataFolder = new File(basePathUrl.getFile() + "../test/data/");
            if (!testDataFolder.exists()) {
                fail("DB setup data not available at " + testDataFolder.getAbsolutePath());
            }
            m_testDataPath = CmsFileUtil.normalizePath(testDataFolder.getAbsolutePath() + File.separator);
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
    protected static void importResources(CmsObject cms, String importFile, String targetPath) throws CmsException {

        OpenCms.getImportExportManager().importData(
            cms,
            getTestDataPath() + File.separator + "imports" + File.separator + importFile,
            targetPath,
            new CmsShellReport());
    }    
    
    /**
     * Removes the OpenCms database test instance.<p>
     */
    protected static void removeDatabase() {
        
        CmsSetupDb setupDb;
        setupDb = dropTables();
        checkErrors(setupDb);
        setupDb.closeConnection();
        
        setupDb = dropDatabase();
        if (!"oracle".equals(m_dbProduct)) {
            checkErrors(setupDb);
        }       
        setupDb.closeConnection();
    }    
    
    /**
     * Creates a new OpenCms test database including the tables.<p>
     * 
     * Any existing instance of the test database is forcefully removed first.<p>
     */
    protected static void setupDatabase() {
        CmsSetupDb setupDb;
    
        // first kill any existing old database instance
        if (C_DB_ORACLE.equals(m_dbProduct)) {
            setupDb = dropTables();
        } else {
            setupDb = dropDatabase();
        }
        setupDb.closeConnection();
        
        // now setup the new instance
        setupDb = createDatabase();
        if (!"oracle".equals(m_dbProduct)) {
            checkErrors(setupDb);
        }
        setupDb.closeConnection();
        

        setupDb = createTables();            
        checkErrors(setupDb);
        setupDb.closeConnection();
    }
    
    
    
    /**
     * Compares two lists of CmsProperty objects and creates a list of all properties which are
     * not included in a seperate exclude list.
     * @param cms the CmsObject
     * @param resourceName the name of the resource the properties belong to
     * @param storedResource the stored resource corresponding to the resourcename
     * @param excludeList the list of properies to exclude in the test or null
     * @return string of non matching properties
     * @throws CmsException if something goes wrong
     */
    private static String compareProperties(
        CmsObject cms,
        String resourceName,
        OpenCmsTestResourceStorageEntry storedResource,
        List excludeList) throws CmsException {

            String noMatches = "";
            List storedProperties = storedResource.getProperties();
            List properties = cms.readPropertyObjects(resourceName, false);
            List unmatchedProperties;
            unmatchedProperties = OpenCmsTestResourceFilter.compareProperties(storedProperties, properties, excludeList);
            if (unmatchedProperties.size() >0) {
                noMatches += "[Properies missing "+unmatchedProperties.toString()+"]\n";   
            }
            unmatchedProperties = OpenCmsTestResourceFilter.compareProperties(properties, storedProperties, excludeList);
            if (unmatchedProperties.size() >0) {
                noMatches += "[Properies additional "+unmatchedProperties.toString()+"]\n";   
            } 
            return noMatches;
    }    
    
    /**
     * Copies the configuration files from the "config-ori" folder to the 
     * "config" folder.<p>
     */
    private static void copyConfiguration(String newConfig) {
        
        File configDir = new File(getTestDataPath() + "WEB-INF/config/");
        File configOriDir = new File(newConfig);
        
        if (configOriDir.exists()) {
            File[] oriFiles = configOriDir.listFiles();
            boolean initConfigDates = false;
            if (m_dateConfigFiles == null) {
                m_dateConfigFiles = new long[oriFiles.length];
                initConfigDates = true;
            }
            for (int i=0; i<oriFiles.length; i++) {
                File source = oriFiles[i];
                if (source.isFile()) {
                    // only copy files
                    String sourceName = source.getAbsolutePath();
                    File target = new File(configDir, source.getName());
                    if (initConfigDates) {
                        m_dateConfigFiles[i] = target.lastModified();
                    }
                    String targetName = target.getAbsolutePath();
                    try {
                        CmsFileUtil.copy(sourceName, targetName);
                        target.setLastModified(m_dateConfigFiles[i]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
    }
    
    /**
     * Compares an access control entry of a resource with a given access control entry.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param ace the access control entry to compare or null if to compare with the stored values
     */
    public void assertAce(CmsObject cms, String resourceName, CmsAccessControlEntry ace) {
        try {
                              
            // create the exclude list
            List excludeList = new ArrayList();
            if (ace != null) {
                excludeList.add(ace);
            }  
            
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_currentResourceStrorage.get(resourceName);
                           
            String noMatches = compareAccessEntries(cms, resourceName, storedResource, excludeList);
     
            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing ace of resource " + resourceName + " with stored values: " + noMatches);
            }  
            
            if (ace != null) {
                Vector resAces = cms.getAccessControlEntries(resourceName);
                boolean notFound= true;
                Iterator i = resAces.iterator();
                while (i.hasNext()) {
                    CmsAccessControlEntry resAce = (CmsAccessControlEntry)i.next();
                    if (resAce.getPrincipal().equals(ace.getPrincipal()) 
                            && (resAce.getResource().equals(ace.getResource()))) {
                        notFound = false;
                        if (!resAce.equals(ace)) {
                            fail("[ACE " + ace + " != " + resAce + "]");
                        }
                    }                    
                }            
                if (notFound) {
                    fail("[ACE not found" + ace + "]");   
                }   
            }
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " "+CmsException.getStackTraceAsString(e));     
        }
    }
        
    /**
     * Compares an access control list of a resource with a given access control permission.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param principal the principal of the permission set or null if to compare with the stored values
     * @param permission the permission set to compare
     */
    public void assertAcl(CmsObject cms, String resourceName, CmsUUID principal, CmsPermissionSet permission) {
        try {
                                
            // create the exclude list
            List excludeList = new ArrayList();
            if (permission != null) {
                excludeList.add(principal);
            } 
            
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_currentResourceStrorage.get(resourceName);
              
            String noMatches = compareAccessLists(cms, resourceName, storedResource, excludeList);
     
            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing permission sets of resource "
                    + resourceName
                    + " with stored values: "
                    + noMatches);
            }  
            
            if (permission != null) {
                CmsAccessControlList resAcls = cms.getAccessControlList(resourceName);
                
                Map permissionMap = resAcls.getPermissionMap();
                CmsPermissionSet resPermission = (CmsPermissionSet)permissionMap.get(principal);
                if (resPermission != null) {
                    if (!resPermission.equals(permission)) {
                        fail("[Permission set not equal " + principal + ":"+ permission + " != " + resPermission + "]");   
                    }
                } else {
                    fail("[Permission set not found " + principal + ":"+ permission +"]");   
                }   
            }
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " "+CmsException.getStackTraceAsString(e));     
        }
    }

    /**
     * Compares an access control list of a resource with a given access control permission.<p>
     * 
     * @param cms the CmsObject
     * @param modifiedResource the name of the which had its permissions changed
     * @param resourceName the name of the resource to compare
     * @param principal the principal of the permission set or null if to compare with the stored values
     * @param permission the permission set to compare
     */
    public void assertAcl(
        CmsObject cms,
        String modifiedResource,
        String resourceName,
        CmsUUID principal,
        CmsPermissionSet permission) {

       //TODO: This method does not work correctly so far, it must be completed!
        
        try {                       
            // create the exclude list
            List excludeList = new ArrayList();
            if (permission != null) {
                excludeList.add(principal);
            }    
                 
            //TODO: This is the code to recalculate the pemrission set if necessary. Its not completed yet!
            
            Map parents = getParents(cms, resourceName);
            Vector aceList = cms.getAccessControlEntries(resourceName);
            Iterator i = aceList.iterator();
            while (i.hasNext()) {
                CmsAccessControlEntry ace = (CmsAccessControlEntry)i.next();
                if (ace.getPrincipal().equals(principal)) {
                    String parent = (String) parents.get(ace.getResource());
                    if ((!parent.equals(modifiedResource)) && (parent.length() > modifiedResource.length())) {
                        permission.setPermissions(ace.getAllowedPermissions(), ace.getDeniedPermissions());
                    }
                }
            }
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_currentResourceStrorage.get(resourceName);
            
            String noMatches = compareAccessLists(cms, resourceName, storedResource, excludeList);
     
            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing permission sets of resource "
                    + resourceName
                    + " with stored values: "
                    + noMatches);
            }  
            
            if (permission != null) {
                CmsAccessControlList resAcls = cms.getAccessControlList(resourceName);
                
                Map permissionMap = resAcls.getPermissionMap();
                CmsPermissionSet resPermission = (CmsPermissionSet)permissionMap.get(principal);
                if (resPermission != null) {
                    if (!resPermission.equals(permission)) {
                        fail("[Permission set not equal " + principal + ":"+ permission + " != " + resPermission + "]");   
                    }
                } else {
                    fail("[Permission set not found " + principal + ":"+ permission +"]");   
                }   
            }
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " "+CmsException.getStackTraceAsString(e));     
        }
    }       
    
    /**
     * Compares the current content of a (file) resource with a given content.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param content the content to compare
     */
    public void assertContent(CmsObject cms, String resourceName, byte[] content) {
        try {
            // get the actual resource from the vfs
            CmsFile file = cms.readFile(resourceName, CmsResourceFilter.ALL);

            byte[] fileContent = file.getContents();
            if (fileContent.length != file.getLength()) {
                fail("[Content length stored " + file.getContents().length + " != " + file.getLength() + "]");
            }
            if (fileContent.length != content.length) {
                fail("[Content length compared " + file.getContents().length + " != " + content.length + "]");
            }
            for (int i=0; i<content.length; i++) {
                if (fileContent[i] != content[i]) {
                    fail("[Content compare failed at index " + i + "]");
                }
            }            
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " "+CmsException.getStackTraceAsString(e));     
        }
    }        
    
    /**
     * Compares the current date created of a resource with a given date.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param dateCreated the creation date
     */
    public void assertDateCreated(CmsObject cms, String resourceName, long dateCreated) {
        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);
            
            if (res.getDateCreated() != dateCreated) {
                fail("[DateCreated "
                    + dateCreated
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(dateCreated)
                    + " != "
                    + res.getDateCreated()
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(res.getDateCreated())
                    + "]");
                
            }
            
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " "+CmsException.getStackTraceAsString(e));     
        }
    }    
    
    /**
     * Tests if the the creation date of a resource is later then a given date.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param dateCreated the creation date
     */
    public void assertDateCreatedAfter(CmsObject cms, String resourceName, long dateCreated) {
        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);
            
            if (res.getDateCreated() < dateCreated) {
                fail("[DateCreated "
                    + dateCreated
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(dateCreated)
                    + " > "
                    + res.getDateCreated()
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(res.getDateCreated())
                    + "]");
            }
            
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName+" " + CmsException.getStackTraceAsString(e));     
        }
    }    
    
    /**
     * Compares the current date last modified of a resource with a given date.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param dateLastModified the last modification date
     */
    public void assertDateLastModified(CmsObject cms, String resourceName, long dateLastModified) {
        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);
            
            if (res.getDateLastModified() != dateLastModified) {
                fail("[DateLastModified "
                    + dateLastModified
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(dateLastModified)
                    + " != "
                    + res.getDateLastModified()
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(res.getDateLastModified())
                    + "]");
            }
            
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " "+CmsException.getStackTraceAsString(e));     
        }
    }
    
    /**
     * Tests if the the current date last modified of a resource is later then a given date.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param dateLastModified the last modification date
     */
    public void assertDateLastModifiedAfter(CmsObject cms, String resourceName, long dateLastModified) {
        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);
            
            if (res.getDateLastModified() < dateLastModified) {
                fail("[DateLastModified "
                    + dateLastModified
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(dateLastModified)
                    + " > "
                    + res.getDateLastModified()
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(res.getDateLastModified())
                    + "]");
            }
            
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName+" " + CmsException.getStackTraceAsString(e));     
        }
    }

    /**
     * Tests if the given exceptions are equal (or both null).<p>
     * 
     * @param e1 first exception to compare
     * @param e2 second exception to compare
     */
    public void assertEquals(CmsException e1, CmsException e2) {
        if (e1 == null && e2 == null) {
            return;
        } 
        
        if ((e1 == null && e2 != null) || (e1 != null && e2 == null)) {
            fail ("Exceptions not equal (not both null)");
        }
        
        if (!(e1.getClass().equals(e2.getClass()) || !(e1.getType() == e2.getType()))) {
            fail("Exception " + e1.toString() + " does not equal " + e2.toString());
        }
    }
    
    /**
     * Compares a stored Cms resource with another Cms resource instance using a specified filter.<p>
     * 
     * @param cms the current user's Cms object
     * @param storedResource a stored Cms resource representing the state before an operation
     * @param res a Cms resource representing the state after an operation
     * @param filter a filter to compare both resources
     */
    public void assertFilter(
        CmsObject cms,
        OpenCmsTestResourceStorageEntry storedResource,
        CmsResource res,
        OpenCmsTestResourceFilter filter) {

        String noMatches = null;
        String resourceName = null;

        try {
            noMatches = "";
            resourceName = cms.getRequestContext().removeSiteRoot(res.getRootPath());

            // compare the contents if necessary
            if (filter.testContents()) {
                byte[] contents;
                // we only have to do this when comparing files
                if (res.isFile()) {
                    contents = cms.readFile(resourceName, CmsResourceFilter.ALL).getContents();
                    if (!new String(storedResource.getContents()).equals(new String(contents))) {
                        noMatches += "[Content does not match]\n";
                    }
                    contents = null;
                }
            }
            // compare the date created if necessary
            if (filter.testDateCreated()) {
                if (storedResource.getDateCreated() != res.getDateCreated()) {
                    noMatches += "[DateCreated "
                        + storedResource.getDateCreated() 
                        + " i.e. "
                        + CmsDateUtil.getHeaderDate(storedResource.getDateCreated())
                        + " != "
                        + res.getDateCreated()
                        + " i.e. "
                        + CmsDateUtil.getHeaderDate(res.getDateCreated())
                        + "]\n";
                }
            }
            // compare the date expired if necessary
            if (filter.testDateExpired()) {
                if (storedResource.getDateExpired() != res.getDateExpired()) {
                    noMatches += "[DateExpired "                        
                        + storedResource.getDateExpired() 
                        + " i.e. "
                        + CmsDateUtil.getHeaderDate(storedResource.getDateExpired())
                        + " != "
                        + res.getDateExpired()
                        + " i.e. "
                        + CmsDateUtil.getHeaderDate(res.getDateExpired())
                        + "]\n";
                }
            }
            // compare the date last modified if necessary
            if (filter.testDateLastModified()) {
                if (storedResource.getDateLastModified() != res.getDateLastModified()) {
                    noMatches += "[DateLastModified "
                        + storedResource.getDateLastModified() 
                        + " i.e. "
                        + CmsDateUtil.getHeaderDate(storedResource.getDateLastModified())
                        + " != "
                        + res.getDateLastModified()
                        + " i.e. "
                        + CmsDateUtil.getHeaderDate(res.getDateLastModified())
                        + "]\n";
                }
            }
            // compare the date last released if necessary
            if (filter.testDateReleased()) {
                if (storedResource.getDateReleased() != res.getDateReleased()) {
                    noMatches += "[DateReleased "
                        + storedResource.getDateReleased() 
                        + " i.e. "
                        + CmsDateUtil.getHeaderDate(storedResource.getDateReleased())
                        + " != "
                        + res.getDateReleased()
                        + " i.e. "
                        + CmsDateUtil.getHeaderDate(res.getDateReleased())
                        + "]\n";
                }
            }
            // compare the flags if necessary
            if (filter.testFlags()) {
                if (storedResource.getFlags() != res.getFlags()) {
                    noMatches += "[Flags " + storedResource.getFlags() + " != " + res.getFlags() + "]\n";
                }
            }
            // compare the length if necessary
            if (filter.testLength()) {
                if (storedResource.getLength() != res.getLength()) {
                    noMatches += "[Length " + storedResource.getLength() + " != " + res.getLength() + "]\n";
                }
            }
            // compare the sibling count if necessary
            if (filter.testSiblingCount()) {
                if (storedResource.getSiblingCount() != res.getSiblingCount()) {
                    noMatches += "[SiblingCount "
                        + storedResource.getSiblingCount()
                        + " != "
                        + res.getSiblingCount()
                        + "]\n";
                }
            }
            // compare the lockstate if necessary
            if (filter.testLock()) {
                CmsLock resLock = cms.getLock(res);
                if (!storedResource.getLock().equals(resLock)) {
                    noMatches += "[Lockstate " + storedResource.getLock() + " != " + resLock + "]\n";
                }
            }
            // compare the name if necessary
            if (filter.testName()) {
                if (!storedResource.getName().equals(res.getName())) {
                    noMatches += "[Name " + storedResource.getName() + " != " + res.getName() + "]\n";
                }
            }
            // compare the project last modified if necessary
            if (filter.testProjectLastModified()) {
                if (storedResource.getProjectLastModified() != res.getProjectLastModified()) {
                    noMatches += "[ProjectLastModified "
                        + storedResource.getProjectLastModified()
                        + " != "
                        + res.getProjectLastModified()
                        + "]\n";
                }
            }
            // compare the properties if necessary
            if (filter.testProperties()) {
                noMatches += compareProperties(cms, resourceName, storedResource, null);
            }
            // compare the acl if necessary
            if (filter.testAcl()) {
                // compare the ACLs
                noMatches += compareAccessLists(cms, resourceName, storedResource, null);
            }
            // compare the ace if necessary
            if (filter.testAce()) {
                // compate the ACEs
                noMatches += compareAccessEntries(cms, resourceName, storedResource, null);
            }            
            // compare the resource id if necessary
            if (filter.testResourceId()) {
                if (!storedResource.getResourceId().equals(res.getResourceId())) {
                    noMatches += "[ResourceId " + storedResource.getResourceId() + " != " + res.getResourceId() + "]\n";
                }
            }
            // compare the state if necessary
            if (filter.testState()) {
                if (storedResource.getState() != res.getState()) {
                    noMatches += "[State " + storedResource.getState() + " != " + res.getState() + "]\n";
                }
            }
            // compare the structure id if necessary
            if (filter.testStructureId()) {
                if (!storedResource.getStructureId().equals(res.getStructureId())) {
                    noMatches += "[StructureId "
                        + storedResource.getStructureId()
                        + " != "
                        + res.getStructureId()
                        + "]\n";
                }
            }
            // compare the touched flag if necessary
            if (filter.testTouched()) {
                if (storedResource.isTouched() != res.isTouched()) {
                    noMatches += "[Touched " + storedResource.isTouched() + " != " + res.isTouched() + "]\n";
                }
            }
            // compare the type if necessary
            if (filter.testType()) {
                if (storedResource.getType() != res.getTypeId()) {
                    noMatches += "[Type " + storedResource.getType() + " != " + res.getTypeId() + "]\n";
                }
            }
            // compare the user created if necessary
            if (filter.testUserCreated()) {
                if (!storedResource.getUserCreated().equals(res.getUserCreated())) {
                    noMatches += "[UserCreated "
                        + storedResource.getUserCreated()
                        + " != "
                        + res.getUserCreated()
                        + "]\n";
                }
            }
            // compare the user created if necessary
            if (filter.testUserLastModified()) {
                if (!storedResource.getUserLastModified().equals(res.getUserLastModified())) {
                    noMatches += "[UserLastModified "
                        + storedResource.getUserLastModified()
                        + " != "
                        + res.getUserLastModified()
                        + "]\n";
                }
            }

            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing resource " + resourceName + " with stored values:\n" + noMatches);
            }
        } catch (CmsException e) {
            fail("cannot assert filter " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }
    }
    
    /**
     * Compares a resource to its stored version containing the state before a CmsObject
     * method was called.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param filter the filter contianing the flags defining which attributes to compare
     */
    public void assertFilter(CmsObject cms, String resourceName, OpenCmsTestResourceFilter filter) {

        try {
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_currentResourceStrorage.get(resourceName);

            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);

            // compare the current resource with the stored resource
            assertFilter(cms, storedResource, res, filter);
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Compares a given resource to its stored version containing the state before a CmsObject
     * method was called.<p>
     * 
     * @param cms the CmsObject
     * @param resource the resource to compare
     * @param filter the filter contianing the flags defining which attributes to compare
     */
    public void assertFilter(CmsObject cms, CmsResource resource, OpenCmsTestResourceFilter filter) {

        try {
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_currentResourceStrorage.get(resource.getRootPath());

            // compare the current resource with the stored resource
            assertFilter(cms, storedResource, resource, filter);
        } catch (CmsException e) {
            fail("cannot read resource " + resource.getRootPath() + " " + CmsException.getStackTraceAsString(e));
        }
    }
    
    /**
     * Compares a resource to another given resource using a specified filter.<p>
     * 
     * @param cms the current user's Cms object
     * @param resourceName1 resource #1
     * @param resourceName2 resource #2
     * @param filter the filter contianing the flags defining which attributes to compare
     */
    public void assertFilter(CmsObject cms, String resourceName1, String resourceName2, OpenCmsTestResourceFilter filter) {
        try {
            CmsResource res1 = cms.readResource(resourceName1, CmsResourceFilter.ALL);
            CmsResource res2 = cms.readResource(resourceName2, CmsResourceFilter.ALL);
            
            // a dummy storage entry gets created here to share existing code
            OpenCmsTestResourceStorageEntry dummy = new OpenCmsTestResourceStorageEntry(cms, resourceName2, res2);

            assertFilter(cms, dummy, res1, filter);
        } catch (CmsException e) {
            fail("cannot read either resource "
                + resourceName1
                + " or resource "
                + resourceName2
                + " "
                + CmsException.getStackTraceAsString(e));
        }        
    }
    
    /**
     * Ensures that the given resource is a folder.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to check for a folder
     */
    public void assertIsFolder(CmsObject cms, String resourceName) {
        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);

            if (!res.isFolder()) {
                fail("[Not a folder: " + resourceName +  "]");
            }    
            if (res.getLength() != -1) {
                fail("[Folder length not -1: " + resourceName +  "]");
            }                      
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));     
        }
    }    
    
    /**
     * Validates if a specified resource is somehow locked to the current user.<p>
     * 
     * @param cms the current user's Cms object
     * @param resourceName the name of the resource to validate
     */
    public void assertLock(CmsObject cms, String resourceName) {
        try {
            // get the actual resource from the VFS
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);
            CmsLock lock = cms.getLock(res);
            
            if (lock.isNullLock() || !lock.getUserId().equals(cms.getRequestContext().currentUser().getId())) {
                fail("[Lock "
                    + resourceName
                    + " requires must be locked to user "
                    + cms.getRequestContext().currentUser().getId()
                    + "]");
            }
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }        
    }
    
    /**
     * Validates if a specified resource has a lock of a given type for the current user.<p>
     * 
     * @param cms the current user's Cms object
     * @param resourceName the name of the resource to validate
     * @param lockType the type of the lock
     * @see CmsLock#C_TYPE_EXCLUSIVE
     * @see CmsLock#C_TYPE_INHERITED
     * @see CmsLock#C_TYPE_SHARED_EXCLUSIVE
     * @see CmsLock#C_TYPE_SHARED_INHERITED
     * @see CmsLock#C_TYPE_UNLOCKED
     */
    public void assertLock(CmsObject cms, String resourceName, int lockType) {
        try {
            // get the actual resource from the VFS
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);
            CmsLock lock = cms.getLock(res);
            
            if (lockType == CmsLock.C_TYPE_UNLOCKED) {
                if (!lock.isNullLock()) {
                    fail("[Lock " + resourceName + " must be unlocked]");
                }
            } else if (lock.isNullLock()
                || lock.getType() != lockType
                || !lock.getUserId().equals(cms.getRequestContext().currentUser().getId())) {
                fail("[Lock "
                    + resourceName
                    + " requires a lock of type "
                    + lockType
                    + " for user "
                    + cms.getRequestContext().currentUser().getId()
                    + " but has a lock of type "
                    + lock.getType()
                    + " for user "
                    + lock.getUserId()
                    + "]");
            }
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }        
    }
    
    /**
     * Validates the project status of a resource,
     * i.e. if a resource has a "red flag" or not.<p>
     * 
     * @param cms the current user's Cms object
     * @param resourceName the name of the resource to validate
     * @param shouldHaveRedFlag true, if the resource should currently have a red flag
     */
    public void assertModifiedInCurrentProject(CmsObject cms, String resourceName, boolean shouldHaveRedFlag) {

        boolean hasRedFlag = false;

        try {
            // get the actual resource from the VFS
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);

            // the current resource has a red flag if it's state is changed/new/deleted
            hasRedFlag = (res.getState() != I_CmsConstants.C_STATE_UNCHANGED);
            // and if it was modified in the current project
            hasRedFlag &= (res.getProjectLastModified() == cms.getRequestContext().currentProject().getId());
            // and if it was modified by the current user
            hasRedFlag &= (res.getUserLastModified().equals(cms.getRequestContext().currentUser().getId()));

            if (shouldHaveRedFlag && !hasRedFlag) {
                // it should have a red flag, but it hasn't
                fail("[HasRedFlag " +  resourceName + " must have a red flag]");
            } else if (hasRedFlag && !shouldHaveRedFlag) {
                // it has a red flag, but it shouldn't
                fail("[HasRedFlag " +  resourceName + " must not have a red flag]");
            }
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }
    }
    
    /**
     * Compares the current project of a resource with a given CmsProject.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param project the project
     */
    public void assertProject(CmsObject cms, String resourceName, CmsProject project) {
        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);
            
            if (res.getProjectLastModified() != project.getId()) {
                fail("[ProjectLastModified " + project.getId() + " != " + res.getProjectLastModified() + "]");
            }
            
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));     
        }
    }
  
    
     /**
     * Compares the current properties of a resource with the stored values and a given, changed property.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param property the changed property
     */
    public void assertPropertyChanged(CmsObject cms, String resourceName, CmsProperty property) {
        try {
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_currentResourceStrorage.get(resourceName);
            
            // create the exclude list
            List excludeList = new ArrayList();
            excludeList.add(property);            
            
            String noMatches = compareProperties(cms, resourceName, storedResource, excludeList);   
            
            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing resource " + resourceName + " with stored values: " + noMatches);
            }   
            
            // test if the property was already in the stored result
            List storedProperties =  storedResource.getProperties();
            if (!storedProperties.contains(property)) {
                 fail("property not found in stored value: " + property);     
            }
            
            // test if the values of the changed propertiy is correct.
            CmsProperty resourceProperty = cms.readPropertyObject(resourceName, property.getKey(), false);
            if (!resourceProperty.isIdentical(property)) {
                fail("property is not identical :" + property + " != " + resourceProperty);              
            }  
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " "+CmsException.getStackTraceAsString(e));     
        }
    }
    
    
     /**
     * Compares the current properties of a resource with the stored values and a list of changed property.<p>
     * 
     * @param cms an initialized CmsObject
     * @param resourceName the name of the resource to compare
     * @param excludeList a list of CmsProperties to exclude
     */
    public void assertPropertyChanged(CmsObject cms, String resourceName, List excludeList) {
        
        try {
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_currentResourceStrorage.get(resourceName);    
            
            String noMatches = compareProperties(cms, resourceName, storedResource, excludeList);   
            
            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing resource "+resourceName+" with stored values: "+noMatches);
            }   
  
            // test if the values of the changed properties are correct and if the properties
            // were already in the stored result
            
            String propertyNoMatches = "";
            String storedNotFound = "";
            Iterator i = excludeList.iterator();
            List storedProperties =  storedResource.getProperties();
            while (i.hasNext()) {
                CmsProperty property = (CmsProperty)i.next();
                CmsProperty resourceProperty = cms.readPropertyObject(resourceName, property.getKey(), false);
                // test if the property has the same value
                if (!resourceProperty.isIdentical(property)) {
                    propertyNoMatches +=  "[" + property + " != " + resourceProperty + "]";          
                }
                // test if the property was already in the stored object
                if (!storedProperties.contains(property)) {
                    storedNotFound +=  "[" + property + "]"; 
                }
            }                        
            // now see if we have collected any property no-matches
            if (propertyNoMatches.length() > 0) {
                fail("error comparing properties for resource " + resourceName + ": " + propertyNoMatches);
            }
            // now see if we have collected any property not found in the stored original
            if (storedNotFound.length() > 0) {
                fail("properties not found in stored value: " + storedNotFound);
            }          
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));     
        }
    } 
   
    /**
     * Tests if a properetydefintion does exist.<p>
     * 
     * @param cms the CmsObject
     * @param propertyDefinition the propertsdefinition
     */
    public void assertPropertydefinitionExist(CmsObject cms,  CmsPropertydefinition propertyDefinition) {
        try {
            CmsPropertydefinition prop = cms.readPropertydefinition(propertyDefinition.getName());
            if (prop != null) {
                if ((prop.getType() != propertyDefinition.getType())
                    && (!prop.getName().equals(propertyDefinition.getName()))) {
                    fail("propertsdefinitions do not match: " + prop + " != " + propertyDefinition);
                }                
            } else {
                fail("cannot read propertydefitnion" + propertyDefinition);  
            }
        } catch (CmsException e) {
            fail("cannot read propertydefitnion" + propertyDefinition + " "+CmsException.getStackTraceAsString(e));     
        }
    }

    /**
    * Tests the list the propertydefinitions are identical to a given list except one exlclude propertydefintion.<p>
    * 
    * @param cms the CmsObject
    * @param propertyDefintions the list of propertydefintions 
    * @param exclude the exclude propertydefinition
    */
   public void assertPropertydefinitions(CmsObject cms,  List propertyDefintions, CmsPropertydefinition exclude) {
       try {
           String noMatches = "";
           List allPropertydefintions = cms.readAllPropertydefinitions();
           noMatches += comparePropertydefintions(propertyDefintions, allPropertydefintions, exclude);
           noMatches += comparePropertydefintions(allPropertydefintions, propertyDefintions, exclude);
           if (noMatches.length()>0) {
               fail("missig propertydefintions: "+noMatches);
           }
       } catch (CmsException e) {
           fail("cannot read propertydefitnions "+CmsException.getStackTraceAsString(e));     
       }
   }
    
    
    /**
     * Compares the current properties of a resource with the stored values.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     */
    public void assertPropertyEqual(CmsObject cms, String resourceName) {
        try {
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_currentResourceStrorage.get(resourceName);
            String noMatches = compareProperties(cms, resourceName, storedResource, null);   
            
            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing resource " + resourceName + " with stored values: " + noMatches);
            }   
            
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));     
        }
    }
     
    /**
     * Compares the current properties of a resource with the stored values and a given, new property.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param property the changed property
     */
    public void assertPropertyNew(CmsObject cms, String resourceName, CmsProperty property) {
        try {
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_currentResourceStrorage.get(resourceName);

            // create the exclude list
            List excludeList = new ArrayList();
            excludeList.add(property);            
            
            String noMatches = compareProperties(cms, resourceName, storedResource, excludeList);   
            
            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing resource " + resourceName + " with stored values: " + noMatches);
            }   
            
            // test if the property was already in the stored result
            List storedProperties =  storedResource.getProperties();
            if (storedProperties.contains(property)) {
                 fail("property already found in stored value: " + property);     
            }
            
            // test if the values of the changed propertiy is correct.
            CmsProperty resourceProperty = cms.readPropertyObject(resourceName, property.getKey(), false);
            if (!resourceProperty.isIdentical(property)) {
                fail("property is not identical :" + property + " != " + resourceProperty);              
            }  
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " "+CmsException.getStackTraceAsString(e));     
        }
    }
    
    
    /**
     * Compares the current properties of a resource with the stored values and a list of new property.<p>
     * 
     * @param cms an initialized CmsObject
     * @param resourceName the name of the resource to compare
     * @param excludeList a list of CmsProperties to exclude
     */
    public void assertPropertyNew(CmsObject cms, String resourceName, List excludeList) {
        
        try {
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_currentResourceStrorage.get(resourceName);    
                       
            String noMatches = compareProperties(cms, resourceName, storedResource, excludeList);   
            
            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing resource "+resourceName+" with stored values: "+noMatches);
            }   
  
            // test if the values of the changed properties are correct and if the properties
            // were already in the stored result
            
            String propertyNoMatches = "";
            String storedFound = "";
            Iterator i = excludeList.iterator();
            List storedProperties =  storedResource.getProperties();
            while (i.hasNext()) {
                CmsProperty property = (CmsProperty)i.next();
                CmsProperty resourceProperty = cms.readPropertyObject(resourceName, property.getKey(), false);
                // test if the property has the same value
                if (!resourceProperty.isIdentical(property)) {
                    propertyNoMatches +=  "[" + property + " != " + resourceProperty + "]";          
                }
                // test if the property was already in the stored object
                if (storedProperties.contains(property)) {
                    storedFound +=  "[" + property + "]"; 
                }
            }                        
            // now see if we have collected any property no-matches
            if (propertyNoMatches.length() > 0) {
                fail("error comparing properties for resource " + resourceName + ": " + propertyNoMatches);
            }
            // now see if we have collected any property not found in the stored original
            if (storedFound.length() > 0) {
                fail("properties already found in stored value: " + storedFound);
            }          
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));     
        }
    } 
     
    /**
     * Compares the current properties of a resource with the stored values and a given, deleted property.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param property the deleted property
     */
    public void assertPropertyRemoved(CmsObject cms, String resourceName, CmsProperty property) {
        try {

            // create the exclude list
            List excludeList = new ArrayList();
            excludeList.add(property);            
     
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_currentResourceStrorage.get(resourceName);
                 
            String noMatches = compareProperties(cms, resourceName, storedResource, excludeList);   
            
            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing resource "+resourceName+" with stored values: "+noMatches);
            }   
            
            // test if the property was already in the stored result
            List storedProperties =  storedResource.getProperties();
            if (!storedProperties.contains(property)) {
                 fail("property not found in stored value: "+property);     
            }
            
            // test if the values of the changed propertiy is correct.
            CmsProperty resourceProperty = cms.readPropertyObject(resourceName, property.getKey(), false);
            if (resourceProperty != CmsProperty.getNullProperty()) {
                fail("property is not removed :"+property+" != "+ resourceProperty);              
            }  
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " "+CmsException.getStackTraceAsString(e));     
        }
    }  
    
    
    /**
     * Compares the current properties of a resource with the stored values and a list of deleted properties.<p>
     * 
     * @param cms an initialized CmsObject
     * @param resourceName the name of the resource to compare
     * @param excludeList a list of CmsProperties to exclude
     */
    public void assertPropertyRemoved(CmsObject cms, String resourceName, List excludeList) {
        
        try {
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_currentResourceStrorage.get(resourceName);    
                       
            String noMatches = compareProperties(cms, resourceName, storedResource, excludeList);   
            
            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing resource "+resourceName+" with stored values: "+noMatches);
            }   
  
            // test if the values of the changed properties are correct and if the properties
            // were already in the stored result
            
            String propertyNotDeleted = "";
            String storedNotFound = "";
            Iterator i = excludeList.iterator();
            List storedProperties =  storedResource.getProperties();
            List resourceProperties = cms.readPropertyObjects(resourceName, false);
            
            while (i.hasNext()) {
                CmsProperty property = (CmsProperty)i.next();
                 // test if the property has the same value
                if (resourceProperties.contains(property)) {
                    CmsProperty resourceProperty = cms.readPropertyObject(resourceName, property.getKey(), false);
                    propertyNotDeleted +=  "[" + property + " != " + resourceProperty +"]";          
                }
                // test if the property was already in the stored object
                if (!storedProperties.contains(property)) {
                    storedNotFound +=  "[" + property + "]"; 
                }
            }                        
            // now see if we have collected any property no-matches
            if (propertyNotDeleted.length() > 0) {
                fail("properties not deleted for "+resourceName+": "+propertyNotDeleted);
            }
            // now see if we have collected any property not found in the stored original
            if (storedNotFound.length() > 0) {
                fail("properties not found in stored value: "+storedNotFound);
            }          
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));     
        }
    } 
    
    /**
     * Ensures that the given resource is of a certain type.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to check
     * @param resourceType the resource type to check for
     */
    public void assertResourceType(CmsObject cms, String resourceName, int resourceType) {
        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);

            if (res.getTypeId() != resourceType) {
                fail("[ResourceType " + res.getTypeId() + " != " + resourceType +  "]");
            }                  
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));     
        }
    }       
    
    /**
     * Validates if the current sibling count of a resource matches the given number.<p>
     * 
     * @param cms the current user's Cms object
     * @param resourceName the name of the resource to compare
     * @param count the number of additional siblings
     */
    public void assertSiblingCount(CmsObject cms, String resourceName, int count) {
        try {
            // get the current resource from the VFS
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);
            if (res.getSiblingCount() != count) {
                fail("[SiblingCount " + res.getSiblingCount() + " != " + count + "]");
            }
            
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));     
        }        
    }
    
    /**
     * Validates if the current sibling count of a resource has been incremented
     * compared to it's previous sibling count.<p>
     * 
     * @param cms the current user's Cms object
     * @param resourceName the name of the resource to compare
     * @param increment the number of additional siblings compared to the original state 
     */
    public void assertSiblingCountIncremented(CmsObject cms, String resourceName, int increment) {
        try {
            // get the current resource from the VFS
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);   
            
            // get the previous resource from resource storage
            OpenCmsTestResourceStorageEntry entry = m_currentResourceStrorage.get(resourceName);
            
            if (res.getSiblingCount() != (entry.getSiblingCount()+increment)) {
                fail("[SiblingCount "
                    + res.getSiblingCount()
                    + " != "
                    + entry.getSiblingCount()
                    + "+"
                    + increment
                    + "]");
            }
            
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));     
        }        
    }
    
    /**
     * Compares the current state of a resource with a given state.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param state the state
     */
    public void assertState(CmsObject cms, String resourceName, int state) {
        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);
            
            if (res.getState() != state) {
                fail("[State " + state + " != " + res.getState() + "]");
            }
            
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));     
        }
    }
    
    /**
     * Compares the user who created a resource with a given user.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param user the last modification user
     */
    public void assertUserCreated(CmsObject cms, String resourceName, CmsUser user) {
        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);
            
            if (!res.getUserCreated().equals(user.getId())) {
                fail("[UserLastCreated (" + user.getName() + ") " + user.getId() + " != " + res.getUserCreated() + "]");
            }
            
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));     
        }
    }    
    
    /**
     * Compares the current user last modified of a resource with a given user.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param user the last modification user
     */
    public void assertUserLastModified(CmsObject cms, String resourceName, CmsUser user) {
        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);
            
            if (!res.getUserLastModified().equals(user.getId())) {
                fail("[UserLastModified ("
                    + user.getName()
                    + ") "
                    + user.getId()
                    + " != "
                    + res.getUserLastModified()
                    + "]");
            }
            
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));     
        }
    }
    
    
    
    /**
     * Creates a new storage object.<p>
     * @param name the name of the storage
     */
    public void createStorage(String name) {
        OpenCmsTestResourceStorage storage = new OpenCmsTestResourceStorage(name);
        m_resourceStorages.put(name, storage);
    }
    
    /**
     * Returns the name of the database product.<p>
     * 
     * @return returns either oracle or mysql
     */
    public String getDatabaseProduct() {

        return m_dbProduct;
    }

    /**
     * Gets an precalculate resource state from the storage.<p>
     * 
     * @param resourceName the name of the resource to get  the state
     * @return precalculated resource state
     * @throws CmsException in case something goes wrong
     */
    public int getPreCalculatedState(String resourceName) throws CmsException {
         return m_currentResourceStrorage.getPreCalculatedState(resourceName);
    }
    
    
    /**
     * Gets a list of all subresources in of a folder.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the folder to get the subtree from
     * @return list of CmsResource objects
     * @throws CmsException if something goes wrong
     */
    public List getSubtree(CmsObject cms, String resourceName) throws CmsException {

        return cms.getResourcesInTimeRange(
            resourceName,
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT);
    }

    /**
     * Resets the mapping for resourcenames.<p>
     */
    public void resetMapping() {        
        m_currentResourceStrorage.resetMapping();
    }
    
    /**
     * Sets the mapping for resourcenames.<p>
     *
     * @param source the source resource name
     * @param target the target resource name
     */
    public void setMapping(String source, String target) {        
        m_currentResourceStrorage.setMapping(source, target);
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
    public void storeResources(CmsObject cms, String resourceName) {
        storeResources(cms, resourceName, true);
    }
    
    
    /**
     * Stores the state (e.g. attributes, properties, content, lock state and ACL) of 
     * a resource in the internal resource storage.<p>
     * 
     * If the resourceName is the name of a folder in the vfs and storeSubresources is true, 
     * all subresoruces are stored as well.
     * 
     * @param cms an initialized CmsObject
     * @param resourceName the name of the resource in the vfs
     * @param storeSubresources indicates to store subresources of folders
     */
    public void storeResources(CmsObject cms, String resourceName, boolean storeSubresources) {
        
        String resName = "";
        
        try {            
            CmsResource resource = cms.readResource(resourceName, CmsResourceFilter.ALL);
            // test if the name belongs to a file or folder
            if (resource.isFile()) {
                m_currentResourceStrorage.add(cms, resourceName, resource);
            } else {
                // this is a folder, so first add the folder itself to the storeage
                m_currentResourceStrorage.add(cms, 
                    resourceName + (resourceName.charAt(resourceName.length()-1) != '/' ? "/" : ""), 
                    resource);
                
                if (!storeSubresources) {
                    return;
                }
                
                // now get all subresources and add them as well
                List resources = getSubtree(cms, resourceName);
                Iterator i = resources.iterator();
                while (i.hasNext()) {
                    CmsResource res = (CmsResource) i.next();
                    resName = cms.getSitePath(res);
                    m_currentResourceStrorage.add(cms, resName, res);
                }
            }
            } catch (CmsException e) {
            fail("cannot read resource "
                + resourceName
                + " or "
                + resName
                + " "
                + CmsException.getStackTraceAsString(e));
            }
    }
    
    /**
     * Switches the internal resource storage.<p>
     * @param name the name of the storage
     * @throws CmsException if the storage was not found
     */
    public void switchStorage(String name) throws CmsException {
        OpenCmsTestResourceStorage storage = (OpenCmsTestResourceStorage)m_resourceStorages.get(name);
        if (storage != null) {
            m_currentResourceStrorage = storage;
        } else {
            throw new CmsException("Resource storage "+name+" not found", CmsException.C_UNKNOWN_EXCEPTION);
        }
        
    }
    
    /**
     * Writes a message to the current output stream.<p>
     * 
     * @param message the message to write
     */
    protected void echo(String message) {
        try {
            m_shell.printPrompt();
            System.out.println(message);
        } catch (Throwable t) {
            throw new RuntimeException(t);
    }   
    }   
    
    /**
     * Returns an initialized CmsObject with admin user permissions,
     * running in the "/sites/default" site root.<p>
     * 
     * @return an initialized CmsObject with admin user permissions
     * @throws CmsException in case of OpenCms access errors
     */
    protected CmsObject getCmsObject() throws CmsException {
                
        // log in the Admin user and switch to the setup project
        CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
        cms.loginUser("Admin", "admin");
        // switch to the "Offline" project
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.getRequestContext().setSiteRoot("/sites/default/");
        
        // init the storage
        createStorage(OpenCmsTestResourceStorage.DEFAULT_STORAGE);
        switchStorage(OpenCmsTestResourceStorage.DEFAULT_STORAGE);
        
        // return the initialized cms context Object
        return cms;        
    }
    
    /**
     * Removes and deletes a storage object.<p>
     * @param name the name of the storage
     */
    protected void removeStorage(String name) {

        OpenCmsTestResourceStorage storage = (OpenCmsTestResourceStorage)m_resourceStorages.get(name);
        if (storage != null) {
            m_resourceStorages.remove(name);
            storage = null;
        }
    }

    /**
     * Restarts the cms.<p>
     */
    protected void restart() {

        OpenCmsTestLogAppender.setBreakOnError(false);
        
        // output a message 
        System.out.println("\n\n\n----- Restarting shell -----");
        
        m_shell.exit();
        
        m_shell = new CmsShell(getTestDataPath() + "WEB-INF" + File.separator, "${user}@${project}>", null);

        OpenCmsTestLogAppender.setBreakOnError(true);
    }
    
    /**
     * Compares two vectors of access entries and creates a list of all access control entries which are
     * not matching and are not included in a seperate exclude list.
     * @param cms the CmsObject
     * @param resourceName the name of the resource the properties belong to
     * @param storedResource the stored resource corresponding to the resourcename
     * @param excludeList the list of ccess entries to exclude in the test or null   
     * @return string of non matching access entries
     * @throws CmsException if something goes wrong
     */
    private String compareAccessEntries(
        CmsObject cms,
        String resourceName,
        OpenCmsTestResourceStorageEntry storedResource,
        List excludeList) throws CmsException {

            String noMatches = "";
            Vector resAce = cms.getAccessControlEntries(resourceName);
            Vector storedAce = storedResource.getAccessControlEntries();
            List unmatchedAce;
            unmatchedAce = compareAce(resAce, storedAce, excludeList);
            if (unmatchedAce.size() > 0) {
                noMatches += "[ACE missing "+unmatchedAce.toString()+"]\n";   
            }    
            unmatchedAce = compareAce(storedAce, resAce, excludeList);
            if (unmatchedAce.size() > 0) {
                noMatches += "[ACE missing "+unmatchedAce.toString()+"]\n";   
            }  
            return noMatches;
    } 
    
    
    /**
     * Compares two access lists and creates a list of permission sets which are
     * not matching and are not included in a seperate exclude list.
     * @param cms the CmsObject
     * @param resourceName the name of the resource the properties belong to
     * @param storedResource the stored resource corresponding to the resourcename
     * @param excludeList the list of permission sets to exclude in the test or null
     * @return string of non matching access list entries
     * @throws CmsException if something goes wrong
     */
    private String compareAccessLists(
        CmsObject cms,
        String resourceName,
        OpenCmsTestResourceStorageEntry storedResource,
        List excludeList) throws CmsException {

            String noMatches = "";
            CmsAccessControlList resList = cms.getAccessControlList(resourceName);
            CmsAccessControlList storedList = storedResource.getAccessControlList();
            List unmatchedList;
            unmatchedList = compareList(resList, storedList, excludeList);
            if (unmatchedList.size() > 0) {
                noMatches += "[ACL differences "+unmatchedList.toString()+"]\n";   
            }    
            unmatchedList = compareList(storedList, resList, excludeList);
            if (unmatchedList.size() > 0) {
                noMatches += "[ACL differences "+unmatchedList.toString()+"]\n";   
            }  
            return noMatches;
    } 
    
    
    /**
     * Compares two vectors of access control entires.<p>
     * 
     * @param source the source vector to compare
     * @param target  the destination vector to compare
     * @param exclude the exclude list
     * @return list of non matching access control entires 
     */
    private List compareAce(Vector source, Vector target, List exclude) {
        List result = new ArrayList();
        Iterator i = source.iterator();
        while (i.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)i.next();            
            if (!target.contains(ace)) {
                result.add(ace);
            }
        }
        // finally match the result list with the exclude list
        if (exclude != null) {
            Iterator l = exclude.iterator();
            while (l.hasNext()) {
                CmsAccessControlEntry excludeAce = (CmsAccessControlEntry) l.next();   
                if (result.contains(excludeAce)) {
                    result.remove(excludeAce);
                }
            }
        }      
        return result;
    }   
    
    /**
     * Compares two lists of permission sets.<p>
     * @param source the source list to compare
     * @param target  the destination list to compare
     * @param exclude the exclude list
     * @return list of non matching permission sets
     */
    private List compareList(CmsAccessControlList source, CmsAccessControlList target, List exclude) {
        HashMap result = new HashMap();
        
        HashMap destinationMap = target.getPermissionMap(); 
        HashMap sourceMap = source.getPermissionMap(); 
        
        Iterator i = sourceMap.keySet().iterator();
        while (i.hasNext()) {
            CmsUUID key = (CmsUUID)i.next();
            CmsPermissionSet value = (CmsPermissionSet)sourceMap.get(key);
            if (destinationMap.containsKey(key)) {
                CmsPermissionSet destValue =   (CmsPermissionSet)destinationMap.get(key);  
                if (!destValue.equals(value)) {
                    result.put(key, key+" " + value + " != " + destValue);   
                }
            } else {
                result.put(key, "missing "+key);
            }
        }
        
        // finally match the result list with the exclude list
        if (exclude != null) {
            Iterator l = exclude.iterator();
            while (l.hasNext()) {
                CmsUUID excludeUUID = (CmsUUID) l.next();   
                if (result.containsKey(excludeUUID)) {
                    result.remove(excludeUUID);
                }
            }
        }         
        return new ArrayList(result.values()); 
    }
    
   
    /**
     * Compares two lists of propertydefintions excluding an exclude propertydefintion. 
     * @param source the source list of propertydefintions
     * @param target the target list of propertydefintions
     * @param exclude the exclude propertydefintion
     * @return String of missing propertydefinitions
     */
    private String comparePropertydefintions(List source, List target, CmsPropertydefinition exclude) {

        String noMatches = "";
        Iterator i = source.iterator();
        while (i.hasNext()) {
            CmsPropertydefinition prop = (CmsPropertydefinition)i.next();
            if ((!target.contains(prop)) 
                && (!prop.getName().equals(exclude.getName()))
                && (prop.getType() != exclude.getType())) {
                    noMatches += "[" + prop + "]";
            }
        }
        return noMatches;
    }
    
    
    /**
     * Creates a map of all parent resources of a OpenCms resource.<p>
     * The resource UUID is used as key, the full resource path is used as the value.
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to get the parent map from
     * @return HashMap of parent resources
     */
    private Map getParents(CmsObject cms, String resourceName) {
        HashMap parents = new HashMap();
        List parentResources = new ArrayList();
        try {
            // get all parent folders of the current file
            parentResources = cms.readPath(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsException e) {
            // ignore
        }
        Iterator k = parentResources.iterator();
        while (k.hasNext()) {
            // add the current folder to the map
            CmsResource curRes = (CmsResource)k.next();
            parents.put(curRes.getResourceId(), curRes.getRootPath());
        }  
        return parents;
    }

    /**
     * Initializies the OpenCms/database configuration 
     * by reading the appropriate values from opencms.properties.<p>
     */
    private void initConfiguration() {

        if (m_configuration == null) {
            try {
                String propertyFile = getTestDataPath() + "../test.properties";
                m_configuration = CmsPropertyUtils.loadProperties(propertyFile);
                m_dbProduct = m_configuration.getString("db.product");
            } catch (IOException e) {
                fail(e.toString());
                return;
}
            try {
                String propertyFile = getTestDataPath() + "WEB-INF/config." + m_dbProduct + "/opencms.properties";
                m_configuration = CmsPropertyUtils.loadProperties(propertyFile);
            } catch (IOException e) {
                fail(e.toString());
                return;
            }

            m_setupUrl = m_configuration.getString(CmsDbPool.C_KEY_DATABASE + "setup." + "jdbcUrl");
            m_setupName = m_configuration.getString(CmsDbPool.C_KEY_DATABASE + "setup." + "user");
            m_setupPassword = m_configuration.getString(CmsDbPool.C_KEY_DATABASE + "setup." + "password");

            String key = "default";
            m_dbName = m_configuration.getString(CmsDbPool.C_KEY_DATABASE_POOL + "." + key + "." + "dbName");
            m_userName = m_configuration.getString(CmsDbPool.C_KEY_DATABASE_POOL
                + "."
                + key
                + "."
                + CmsDbPool.C_KEY_USERNAME);
            m_userPassword = m_configuration.getString(CmsDbPool.C_KEY_DATABASE_POOL
                + "."
                + key
                + "."
                + CmsDbPool.C_KEY_PASSWORD);

            m_jdbcDriver = m_configuration.getString(CmsDbPool.C_KEY_DATABASE_POOL
                + "."
                + key
                + "."
                + CmsDbPool.C_KEY_JDBC_DRIVER);
            m_jdbcUrl = m_configuration.getString(CmsDbPool.C_KEY_DATABASE_POOL
                + "."
                + key
                + "."
                + CmsDbPool.C_KEY_JDBC_URL);
            m_jdbcUrlParams = m_configuration.getString(CmsDbPool.C_KEY_DATABASE_POOL
                + "."
                + key
                + "."
                + CmsDbPool.C_KEY_JDBC_URL_PARAMS);

            m_defaultTablespace = m_configuration.getString("db.oracle.defaultTablespace");
            m_indexTablespace = m_configuration.getString("db.oracle.indexTablespace");
            m_tempTablespace = m_configuration.getString("db.oracle.temporaryTablespace");

            System.out.println("----- Starting tests on database " + m_dbProduct + " (" + m_setupUrl + ") " + "-----");
        }
    }
}