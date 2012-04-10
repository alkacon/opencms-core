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

package org.opencms.test;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.CmsDbPool;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.history.CmsHistoryFile;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsShell;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobBase;
import org.opencms.publish.CmsPublishJobInfoBean;
import org.opencms.relations.CmsRelation;
import org.opencms.report.CmsShellReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPermissionSetCustom;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.setup.CmsSetupDb;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.io.filefilter.FileFilterUtils;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.util.NodeComparator;

/** 
 * Extends the JUnit standard with methods to handle an OpenCms database
 * test instance.<p>
 * 
 * The required configuration files are located in the 
 * <code>${test.data.path}/WEB-INF</code> folder structure.<p>
 * 
 * To run this test you might have to change the database connection
 * values in the provided <code>${test.data.path}/WEB-INF/config/opencms.properties</code> file.<p>
 * 
 * @since 6.0.0
 */
public class OpenCmsTestCase extends TestCase {

    /** Class to bundle the connection information. */
    protected static class ConnectionData {

        /** The name of the database. */
        public String m_dbName;

        /** The database driver. */
        public String m_jdbcDriver;

        /** The database url. */
        public String m_jdbcUrl;

        /** Additional database parameters. */
        public String m_jdbcUrlParams;

        /** The name of the user. */
        public String m_userName;

        /** The password of the user. */
        public String m_userPassword;
    }

    /**
     * Extension of <code>NodeComparator</code> to store unequal nodes.<p>
     */
    static class InternalNodeComparator extends NodeComparator implements Serializable {

        /** UID required for safe serialization. */
        private static final long serialVersionUID = 2742216550970181832L;

        /** Unequal node1. */
        public Node m_node1;

        /** Unequal node2. */
        public Node m_node2;

        /**
         * @see org.dom4j.util.NodeComparator#compare(org.dom4j.Node, org.dom4j.Node)
         */
        @Override
        public int compare(Node n1, Node n2) {

            int result = super.compare(n1, n2);
            if ((result != 0) && (m_node1 == null)) {
                m_node1 = n1;
                m_node2 = n2;
            }
            return result;
        }
    }

    /** test article type id constant. */
    public static final int ARTICLE_TYPEID = 27;

    /** Special character constant. */
    public static final String C_AUML_LOWER = "\u00e4";

    /** Special character constant. */
    public static final String C_AUML_UPPER = "\u00c4";

    /** Special character constant. */
    public static final String C_EURO = "\u20ac";

    /** Special character constant. */
    public static final String C_OUML_LOWER = "\u00f6";

    /** Special character constant. */
    public static final String C_OUML_UPPER = "\u00d6";

    /** Special character constant. */
    public static final String C_SHARP_S = "\u00df";

    /** Special character constant. */
    public static final String C_UUML_LOWER = "\u00fc";

    /** Special character constant. */
    public static final String C_UUML_UPPER = "\u00dc";

    /** Key for tests on MySql database. */
    public static final String DB_MYSQL = "mysql";

    /** Key for tests on Oracle database. */
    public static final String DB_ORACLE = "oracle";

    /** The OpenCms/database configuration. */
    public static CmsParameterConfiguration m_configuration;

    /** Name of the default tablespace (oracle only). */
    public static String m_defaultTablespace;

    /** Name of the index tablespace (oracle only). */
    public static String m_indexTablespace;

    /** The internal storages. */
    public static HashMap<String, OpenCmsTestResourceStorage> m_resourceStorages;

    /** Name of the temporary tablespace (oracle only). */
    public static String m_tempTablespace;

    /** Additional connection data. */
    protected static ConnectionData m_additionalConnection;

    /** The user connection data. */
    protected static ConnectionData m_defaultConnection;

    /** The setup connection data. */
    protected static ConnectionData m_setupConnection;

    /** The file date of the configuration files. */
    private static long[] m_dateConfigFiles;

    /** DB product used for the tests. */
    private static String m_dbProduct = DB_MYSQL;

    /** The path to the default setup data files. */
    private static String m_setupDataPath;

    /** The initialized OpenCms shell instance. */
    private static CmsShell m_shell;

    /** The list of paths to the additional test data files. */
    private static List<String> m_testDataPath;

    /** The current resource storage. */
    public OpenCmsTestResourceStorage m_currentResourceStrorage;

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public OpenCmsTestCase(String arg0) {

        this(arg0, true);
    }

    /**
     * JUnit constructor.<p>
     * @param arg0 JUnit parameters
     * @param initialize indicates if the configuration will be initialized
     */
    public OpenCmsTestCase(String arg0, boolean initialize) {

        super(arg0);
        if (initialize) {
            OpenCmsTestLogAppender.setBreakOnError(false);
            if (m_resourceStorages == null) {
                m_resourceStorages = new HashMap<String, OpenCmsTestResourceStorage>();
            }

            // initialize configuration
            initConfiguration();

            // set "OpenCmsLog" system property to enable the logger
            OpenCmsTestLogAppender.setBreakOnError(true);
        }
    }

    /**
     * Generates a sub tree of folders with files.<p>
     * 
     * @param cms the cms context
     * @param vfsFolder name of the folder
     * @param numberOfFiles the number of files to generate
     * @param fileTypeDistribution a percentage: x% binary files and (1-x)% text files
     * 
     * @return the number of files generated
     * 
     * @throws Exception if something goes wrong
     */
    public static int generateContent(CmsObject cms, String vfsFolder, int numberOfFiles, double fileTypeDistribution)
    throws Exception {

        int maxProps = 10;
        double propertyDistribution = 0.0;
        int writtenFiles = 0;

        int numberOfBinaryFiles = (int)(numberOfFiles * fileTypeDistribution);

        // generate binary files
        writtenFiles += generateResources(
            cms,
            "org/opencms/search/pdf-test-112.pdf",
            vfsFolder,
            numberOfBinaryFiles,
            CmsResourceTypeBinary.getStaticTypeId(),
            maxProps,
            propertyDistribution);

        // generate text files
        writtenFiles += generateResources(cms, "org/opencms/search/extractors/test1.html", vfsFolder, numberOfFiles
            - numberOfBinaryFiles, CmsResourceTypePlain.getStaticTypeId(), maxProps, propertyDistribution);

        System.out.println("" + writtenFiles + " files written in Folder " + vfsFolder);

        return writtenFiles;
    }

    /**
     * Generates a sub tree of folders with files.<p>
     * 
     * @param cms the cms context
     * @param vfsFolder where to create the subtree
     * @param maxWidth an upper bound for the number of subfolder a folder should have
     * @param maxDepth an upper bound for depth of the genearted subtree
     * @param maxProps upper bound for number of properties to create for each resource
     * @param propertyDistribution a percentage: x% shared props and (1-x)% individuals props
     * @param maxNumberOfFiles upper bound for the number of files in each folder
     * @param fileTypeDistribution a percentage: x% binary files and (1-x)% text files
     * 
     * @return the number of really written files
     * 
     * @throws Exception if something goes wrong
     */
    public static int generateContent(
        CmsObject cms,
        String vfsFolder,
        int maxWidth,
        int maxDepth,
        int maxProps,
        double propertyDistribution,
        int maxNumberOfFiles,
        double fileTypeDistribution) throws Exception {

        int fileNameLength = 10;
        int propValueLength = 10;

        // end recursion
        if (maxDepth < 1) {
            return 0;
        }
        if (!vfsFolder.endsWith("/")) {
            vfsFolder += "/";
        }

        int writtenFiles = 0;

        Random rnd = new Random();
        int width = rnd.nextInt(maxWidth) + 1;
        int depth = maxDepth - rnd.nextInt(2);
        for (int i = 0; i < width; i++) {
            // generate folder
            String vfsName = vfsFolder + generateName(fileNameLength) + i;
            List<CmsProperty> props = generateProperties(cms, maxProps, propValueLength, propertyDistribution);
            cms.createResource(vfsName, CmsResourceTypeFolder.getStaticTypeId(), new byte[0], props);
            cms.unlockResource(vfsName);

            int numberOfFiles = rnd.nextInt(maxNumberOfFiles) + 1;
            // generate binary files
            int numberOfBinaryFiles = (int)(numberOfFiles * fileTypeDistribution);
            writtenFiles += generateResources(
                cms,
                "org/opencms/search/pdf-test-112.pdf",
                vfsName,
                numberOfBinaryFiles,
                CmsResourceTypeBinary.getStaticTypeId(),
                maxProps,
                propertyDistribution);

            // generate text files
            writtenFiles += generateResources(cms, "org/opencms/search/extractors/test1.html", vfsName, numberOfFiles
                - numberOfBinaryFiles, CmsResourceTypePlain.getStaticTypeId(), maxProps, propertyDistribution);

            // in depth recursion
            writtenFiles += generateContent(
                cms,
                vfsName,
                maxWidth,
                depth - 1,
                maxProps,
                propertyDistribution,
                maxNumberOfFiles,
                fileTypeDistribution);

            System.out.println("" + writtenFiles + " files written in Folder " + vfsName);
        }
        return writtenFiles;
    }

    /**
     * Generate a new random name.<p>
     * 
     * @param maxLen upper bound for the length of the name
     * 
     * @return a random name
     */
    public static String generateName(int maxLen) {

        String name = "";
        Random rnd = new Random();
        int len = rnd.nextInt(maxLen) + 1;
        for (int j = 0; j < len; j++) {
            name += (char)(rnd.nextInt(26) + 97);
        }
        return name;
    }

    /**
     * Generates random properties.<p>
     * 
     * @param cms the cms context
     * @param maxProps upper bound for number of properties to create for each resource
     * @param propValueLength upper bound for the number of char for the values
     * @param propertyDistribution a percentage: x% shared props and (1-x)% individuals props
     * 
     * @return a list of <code>{@link CmsProperty}</code> objects
     * 
     * @throws CmsException if something goes wrong
     */
    public static List<CmsProperty> generateProperties(
        CmsObject cms,
        int maxProps,
        int propValueLength,
        double propertyDistribution) throws CmsException {

        List<CmsPropertyDefinition> propList = cms.readAllPropertyDefinitions();

        List<CmsProperty> props = new ArrayList<CmsProperty>();
        if (maxProps > propList.size()) {
            maxProps = propList.size();
        }
        Random rnd = new Random();
        int propN = rnd.nextInt(maxProps) + 1;
        for (int j = 0; j < propN; j++) {
            CmsPropertyDefinition propDef = propList.get((int)(Math.random() * propList.size()));
            propList.remove(propDef);
            if (Math.random() < propertyDistribution) {
                // only resource prop
                props.add(new CmsProperty(propDef.getName(), null, generateName(propValueLength)));
            } else {
                // resource and structure props
                props.add(new CmsProperty(
                    propDef.getName(),
                    generateName(propValueLength),
                    generateName(propValueLength)));
            }
        }

        return props;
    }

    /**
     * Generates n new resources in a given folder.<p>
     * 
     * @param cms the cms context
     * @param rfsName the rfs file for the content
     * @param vfsFolder the folder to create the resources in
     * @param n number of resources to generate
     * @param type the type of the resource
     * @param maxProps upper bound for number of properties to create for each resource
     * @param propertyDistribution a percentage: x% shared props and (1-x)% individuals props
     * 
     * @return the number of really written files
     * 
     * @throws Exception if something goes wrong
     */
    public static int generateResources(
        CmsObject cms,
        String rfsName,
        String vfsFolder,
        int n,
        int type,
        int maxProps,
        double propertyDistribution) throws Exception {

        int fileNameLength = 10;
        int propValueLength = 10;

        if (!vfsFolder.endsWith("/")) {
            vfsFolder += "/";
        }
        int writtenFiles = 0;
        System.out.println("Importing Files");
        for (int i = 0; i < n; i++) {
            String vfsName = vfsFolder + generateName(fileNameLength) + i;
            if (rfsName.lastIndexOf('.') > 0) {
                vfsName += rfsName.substring(rfsName.lastIndexOf('.'));
            }
            List<CmsProperty> props = generateProperties(cms, maxProps, propValueLength, propertyDistribution);
            try {
                OpenCmsTestCase.importTestResource(cms, rfsName, vfsName, type, props);
                writtenFiles++;
            } catch (Exception e) {
                System.out.println("error! " + e.getMessage());
            }
        }
        return writtenFiles;
    }

    /**
     * Generates a wrapper for a test class which handles setting up the OpenCms instance.<p>
     * 
     * @param testClass the test class to wrap 
     * @param importFolder the RFS folder with the test data to import 
     * @param targetFolder the VFS target folder for the test data
     *  
     * @return the wrapped test 
     */
    public static Test generateSetupTestWrapper(
        Class<? extends Test> testClass,
        final String importFolder,
        final String targetFolder) {

        try {
            TestSuite suite = new TestSuite();
            suite.setName(testClass.getName());
            Constructor<? extends Test> constructor = testClass.getConstructor(String.class);
            for (Method method : testClass.getMethods()) {
                String methodName = method.getName();
                if (methodName.startsWith("test") && (method.getParameterTypes().length == 0)) {
                    Test test = constructor.newInstance(method.getName());
                    suite.addTest(test);
                }
            }
            TestSetup wrapper = new TestSetup(suite) {

                /**
                 * @see junit.extensions.TestSetup#setUp()
                 */
                @Override
                protected void setUp() {

                    setupOpenCms(importFolder, targetFolder);
                }

                /**
                 * @see junit.extensions.TestSetup#tearDown()
                 */
                @Override
                protected void tearDown() {

                    removeOpenCms();
                }
            };
            return wrapper;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates n new users for a given group.<p>
     * 
     * @param cms the cms context
     * @param groupName the group name, group will be creating if group does not exists
     * @param n number of users to generate
     * 
     * @throws CmsException if something goes wrong
     */
    public static void generateUsers(CmsObject cms, String groupName, int n) throws CmsException {

        CmsGroup group = null;
        try {
            group = cms.readGroup(groupName);
        } catch (Exception e) {
            // ignore
        }
        if (group == null) {
            cms.createGroup(groupName, groupName, 0, null);
        }
        for (int i = 0; i < n; i++) {
            String name = generateName(10) + i;
            cms.createUser(name, "pwd" + i, "test user " + i, null);
            cms.addUserToGroup(name, groupName);
        }
    }

    /**
     * Returns the currently used database/configuration.<p>
     * 
     * @return he currently used database/configuration
     */
    public static String getDbProduct() {

        return m_dbProduct;
    }

    /**
     * Does a database import from the given RFS folder to the given VFS folder.<p>
     * 
     * @param importFolder the RFS folder to import from
     * @param targetFolder the VFS folder to import into
     */
    public static void importData(String importFolder, String targetFolder) {

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

        // create a shell instance
        m_shell = new CmsShell(getTestDataPath("WEB-INF" + File.separator), null, null, "${user}@${project}>", null);

        // open the test script 
        File script;
        FileInputStream stream = null;
        CmsObject cms = null;

        try {
            // start the shell with the base script
            script = new File(getTestDataPath("scripts/script_import.txt"));
            stream = new FileInputStream(script);
            m_shell.start(stream);

            // log in the Admin user and switch to the setup project
            cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
            cms.loginUser("Admin", "admin");
            cms.getRequestContext().setCurrentProject(cms.readProject("tempFileProject"));

            if (importFolder != null) {
                // import the "simpletest" files
                importResources(cms, importFolder, targetFolder);
            }

            // publish the current project by script
            script = new File(getTestDataPath("scripts/script_import_publish.txt"));
            stream = new FileInputStream(script);
            m_shell.start(stream);
            OpenCms.getPublishManager().waitWhileRunning();

            // switch to the "Offline" project
            cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
            cms.getRequestContext().setSiteRoot("/sites/default/");

            // output a message 
            System.out.println("----- Starting test cases -----");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            fail("Unable to setup OpenCms\n" + CmsException.getStackTraceAsString(t));
        }
        // turn on exceptions after error logging
        OpenCmsTestLogAppender.setBreakOnError(true);
    }

    /**
     * Initializes the path to the test data configuration files
     * using the default path.<p>
     */
    public static synchronized void initTestDataPath() {

        if (m_testDataPath == null) {
            m_testDataPath = new ArrayList<String>(4);

            // test wether we are instantiated within the 
            // AllTest suite and therefore the OpenCmsTestProperties are 
            // already set up:
            try {
                OpenCmsTestProperties.getInstance();
            } catch (RuntimeException rte) {
                OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
            }
            // set data path 
            addTestDataPath(OpenCmsTestProperties.getInstance().getTestDataPath());
        }
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

        try {
            // sleep 0.5 seconds - sometimes other Threads need to finish before the next test case can start
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // ignore
        }

        // remove the database
        removeDatabase();

        String path;

        // copy the configuration files to re-create the original configuration
        String configFolder = getTestDataPath("WEB-INF" + File.separator + "config." + m_dbProduct + File.separator);
        copyConfiguration(configFolder);

        // remove potentially created "classes, "lib", "backup" etc. folder
        path = getTestDataPath("WEB-INF/classes/");
        if (path != null) {
            CmsFileUtil.purgeDirectory(new File(path));
        }
        path = getTestDataPath("WEB-INF/logs/publish");
        if (path != null) {
            CmsFileUtil.purgeDirectory(new File(path));
        }
        path = getTestDataPath("WEB-INF/lib/");
        if (path != null) {
            CmsFileUtil.purgeDirectory(new File(path));
        }
        path = getTestDataPath("WEB-INF/" + CmsSystemInfo.FOLDER_CONFIG_DEFAULT + "backup/");
        if (path != null) {
            CmsFileUtil.purgeDirectory(new File(path));
        }
        path = getTestDataPath("WEB-INF/index/");
        if ((path != null) && !m_configuration.containsKey("test.keep.searchIndex")) {
            CmsFileUtil.purgeDirectory(new File(path));
        }
        path = getTestDataPath("export/");
        if (path != null) {
            CmsFileUtil.purgeDirectory(new File(path));
        }
    }

    /**
     * Restarts the OpenCms shell.<p>
     */
    public static void restartOpenCms() {

        // turn off exceptions after error logging during setup (won't work otherwise)
        OpenCmsTestLogAppender.setBreakOnError(false);
        // output a message 
        System.out.println("\n\n\n----- Restarting OpenCms -----");

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
        m_shell = new CmsShell(getTestDataPath("WEB-INF" + File.separator), null, null, "${user}@${project}>", null);

        // turn on exceptions after error logging
        OpenCmsTestLogAppender.setBreakOnError(true);
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

        return setupOpenCms(importFolder, targetFolder, getTestDataPath("WEB-INF/config." + m_dbProduct + "/"), true);
    }

    /**
     * Sets up a complete OpenCms instance with configuration from the config-ori folder, 
     * creating the usual projects, and importing a default database.<p>
     * 
     * @param importFolder the folder to import in the "real" FS
     * @param targetFolder the target folder of the import in the VFS
     * @param publish flag to signalize if the publish script should be called
     * @return an initialized OpenCms context with "Admin" user in the "Offline" project with the site root set to "/" 
     */
    public static CmsObject setupOpenCms(String importFolder, String targetFolder, boolean publish) {

        return setupOpenCms(importFolder, targetFolder, getTestDataPath("WEB-INF/config." + m_dbProduct + "/"), publish);
    }

    /**
     * Sets up a complete OpenCms instance with configuration from the config-ori folder, 
     * creating the usual projects, and importing a default database.<p>
     * 
     * @param importFolder the folder to import in the "real" FS
     * @param targetFolder the target folder of the import in the VFS
     * @param specialConfigFolder the folder that contains the special configuration files for this setup
     * @return an initialized OpenCms context with "Admin" user in the "Offline" project with the site root set to "/" 
     */
    public static CmsObject setupOpenCms(String importFolder, String targetFolder, String specialConfigFolder) {

        return setupOpenCms(
            importFolder,
            targetFolder,
            getTestDataPath("WEB-INF/config." + m_dbProduct + "/"),
            getTestDataPath(specialConfigFolder),
            true);
    }

    /**
     * Sets up a complete OpenCms instance, creating the usual projects,
     * and importing a default database.<p>
     * 
     * @param importFolder the folder to import in the "real" FS
     * @param targetFolder the target folder of the import in the VFS
     * @param configFolder the folder to copy the configuration files
     * @param publish publish only if set
     * 
     * @return an initialized OpenCms context with "Admin" user in the "Offline" project with the site root set to "/" 
     */
    public static CmsObject setupOpenCms(String importFolder, String targetFolder, String configFolder, boolean publish) {

        return setupOpenCms(importFolder, targetFolder, configFolder, null, publish);
    }

    /**
     * Sets up a complete OpenCms instance, creating the usual projects,
     * and importing a default database.<p>
     * 
     * @param importFolder the folder to import in the "real" FS
     * @param targetFolder the target folder of the import in the VFS
     * @param configFolder the folder to copy the standard configuration files from
     * @param specialConfigFolder the folder that contains the special configuration fiiles for this setup

     * @param publish publish only if set
     * 
     * @return an initialized OpenCms context with "Admin" user in the "Offline" project with the site root set to "/" 
     */
    public static CmsObject setupOpenCms(
        String importFolder,
        String targetFolder,
        String configFolder,
        String specialConfigFolder,
        boolean publish) {

        // intialize a new resource storage
        m_resourceStorages = new HashMap<String, OpenCmsTestResourceStorage>();

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

        // create the OpenCms "config" folder
        File configFile = new File(m_testDataPath.get(0)
            + "WEB-INF"
            + File.separator
            + CmsSystemInfo.FOLDER_CONFIG_DEFAULT);
        if (!configFile.exists()) {
            configFile.mkdir();
        }

        // copy the configuration files from the base folder
        copyConfiguration(getTestDataPath("WEB-INF/base/"));

        // copy the special configuration files from the database folder
        copyConfiguration(configFolder);

        // copy the configuration files from the special individual folder if required
        if (specialConfigFolder != null) {
            copyConfiguration(specialConfigFolder);
        }

        // create a new database first
        setupDatabase();

        // create a shell instance
        m_shell = new CmsShell(getTestDataPath("WEB-INF" + File.separator), null, null, "${user}@${project}>", null);

        // open the test script 
        File script;
        FileInputStream stream = null;
        CmsObject cms = null;

        try {
            // start the shell with the base script
            script = new File(getTestDataPath("scripts/script_base.txt"));
            stream = new FileInputStream(script);
            m_shell.start(stream);

            // add the default folders by script
            script = new File(getTestDataPath("scripts/script_default_folders.txt"));
            stream = new FileInputStream(script);
            m_shell.start(stream);

            // log in the Admin user and switch to the setup project
            cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
            cms.loginUser("Admin", "admin");
            cms.getRequestContext().setCurrentProject(cms.readProject("_setupProject"));

            if (importFolder != null) {
                // import the "simpletest" files
                importResources(cms, importFolder, targetFolder);
            }

            // create the default projects by script
            script = new File(getTestDataPath("scripts/script_default_projects.txt"));
            stream = new FileInputStream(script);
            m_shell.start(stream);

            if (publish) {
                // publish the current project by script
                script = new File(getTestDataPath("scripts/script_publish.txt"));
                stream = new FileInputStream(script);
                m_shell.start(stream);
                OpenCms.getPublishManager().waitWhileRunning();
            } else {
                cms.unlockProject(cms.readProject("_setupProject").getUuid());
            }

            // switch to the "Offline" project
            cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
            cms.getRequestContext().setSiteRoot("/sites/default/");

            // output a message 
            System.out.println("----- Starting test cases -----");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            fail("Unable to setup OpenCms\n" + CmsException.getStackTraceAsString(t));
        }
        // turn on exceptions after error logging
        OpenCmsTestLogAppender.setBreakOnError(true);
        // return the initialized cms context Object
        return cms;
    }

    /**
     * Adds an additional path to the list of test data configuration files.<p>
     * 
     * @param dataPath the path to add
     */
    protected static synchronized void addTestDataPath(String dataPath) {

        // check if the db data folder is available
        File testDataFolder = new File(dataPath);
        if (!testDataFolder.exists()) {
            fail("DB setup data not available at " + testDataFolder.getAbsolutePath());
        }
        String path = CmsFileUtil.normalizePath(testDataFolder.getAbsolutePath() + File.separator);
        if (!m_testDataPath.contains(path)) {
            m_testDataPath.add(path);
        }
    }

    /**
     * Check the setup DB for errors that might have occurred.<p>
     * 
     * @param setupDb the setup DB object to check
     */
    protected static void checkErrors(CmsSetupDb setupDb) {

        if (!setupDb.noErrors()) {
            List<String> errors = setupDb.getErrors();
            for (Iterator<String> i = errors.iterator(); i.hasNext();) {
                String error = i.next();
                System.out.println(error);
            }
            fail(setupDb.getErrors().get(0));
        }
    }

    /**
     * Returns an initialized replacer map.<p>
     * 
     * @param connectionData the connection data to derive the replacer information
     * 
     * @return an initialized replacer map
     */
    protected static Map<String, String> getReplacer(ConnectionData connectionData) {

        Map<String, String> replacer = new HashMap<String, String>();
        replacer.put("${database}", connectionData.m_dbName);
        replacer.put("${user}", connectionData.m_userName);
        replacer.put("${password}", connectionData.m_userPassword);
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
            // check if the db setup files are available
            File setupDataFolder = new File(OpenCmsTestProperties.getInstance().getTestWebappPath());
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
     * @param connection the connection data
     * 
     * @return the initialized setup DB object
     */
    protected static CmsSetupDb getSetupDb(ConnectionData connection) {

        // create setup DB instance
        CmsSetupDb setupDb = new CmsSetupDb(getSetupDataPath());

        // connect to the DB
        setupDb.setConnection(
            connection.m_jdbcDriver,
            connection.m_jdbcUrl,
            connection.m_jdbcUrlParams,
            connection.m_userName,
            connection.m_userPassword);

        // check for errors 
        if (!DB_ORACLE.equals(m_dbProduct)) {
            checkErrors(setupDb);
        }

        return setupDb;
    }

    /**
     * Returns the path to a file in the test data configuration, 
     * or <code>null</code> if the given file can not be found.<p>
     * 
     * This methods searches the given file in all configured test data paths.
     * It returns the file found first.<p>
     * 
     * @param filename the file name to look up
     * @return the path to a file in the test data configuration
     */
    protected static String getTestDataPath(String filename) {

        for (int i = 0; i < m_testDataPath.size(); i++) {

            String path = m_testDataPath.get(i);
            File file = new File(path + filename);
            if (file.exists()) {
                if (file.isDirectory()) {
                    return CmsFileUtil.normalizePath(file.getAbsolutePath() + File.separator);
                } else {
                    return CmsFileUtil.normalizePath(file.getAbsolutePath());
                }
            }
        }

        return null;
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
            new CmsShellReport(cms.getRequestContext().getLocale()),
            new CmsImportParameters(
                getTestDataPath(File.separator + "imports" + File.separator + importFile),
                targetPath,
                true));
    }

    /**
     * Imports a resource from the RFS test directories to the VFS.<p> 
     * 
     * The imported resource will be automatically unlocked.<p>
     * 
     * @param cms the current users OpenCms context
     * @param rfsPath the RTF path of the resource to import, must be a path accessibly by the current class loader
     * @param vfsPath the VFS path for the imported resource
     * @param type the type for the imported resource
     * @param properties the properties for the imported resource
     * @return the imported resource
     * 
     * @throws Exception if the import fails
     */
    protected static CmsResource importTestResource(
        CmsObject cms,
        String rfsPath,
        String vfsPath,
        int type,
        List<CmsProperty> properties) throws Exception {

        byte[] content = CmsFileUtil.readFile(rfsPath);
        CmsResource result = cms.createResource(vfsPath, type, content, properties);
        cms.unlockResource(vfsPath);
        return result;
    }

    /**
     * Removes the OpenCms database test instance.<p>
     */
    protected static void removeDatabase() {

        if (m_defaultConnection != null) {
            removeDatabase(m_setupConnection, m_defaultConnection, false);
        }
        if (m_additionalConnection != null) {
            removeDatabase(m_setupConnection, m_additionalConnection, false);
        }
    }

    /**
     * Removes the OpenCms database test instance.<p>
     * 
     * @param setupConnection the setup connection
     * @param defaultConnection the default connection
     * @param handleErrors flag to indicate if errors should be handled/checked
     */
    protected static void removeDatabase(
        ConnectionData setupConnection,
        ConnectionData defaultConnection,
        boolean handleErrors) {

        CmsSetupDb setupDb = null;
        boolean noErrors = true;

        try {
            setupDb = getSetupDb(defaultConnection);
            setupDb.dropTables(m_dbProduct, getReplacer(defaultConnection), handleErrors);
            noErrors = setupDb.noErrors();
        } catch (Exception e) {
            noErrors = false;
        } finally {
            if (setupDb != null) {
                setupDb.closeConnection();
            }
        }

        if (!handleErrors || noErrors) {
            try {
                setupDb = getSetupDb(setupConnection);
                setupDb.dropDatabase(m_dbProduct, getReplacer(defaultConnection), handleErrors);
                setupDb.closeConnection();
            } catch (Exception e) {
                noErrors = false;
            } finally {
                if (setupDb != null) {
                    setupDb.closeConnection();
                }
            }
        }

        if (handleErrors) {
            checkErrors(setupDb);
        }
    }

    /**
     * Creates a new OpenCms test database including the tables.<p>
     * 
     * Any existing instance of the test database is forcefully removed first.<p>
     */
    protected static void setupDatabase() {

        if (m_defaultConnection != null) {
            setupDatabase(m_setupConnection, m_defaultConnection, true);
        }
        if (m_additionalConnection != null) {
            setupDatabase(m_setupConnection, m_additionalConnection, true);
        }
    }

    /**
     * Creates a new OpenCms test database including the tables.<p>
     * 
     * @param setupConnection the setup connection
     * @param defaultConnection the default connection
     * @param handleErrors flag to indicate if errors should be handled/checked
     */
    protected static void setupDatabase(
        ConnectionData setupConnection,
        ConnectionData defaultConnection,
        boolean handleErrors) {

        CmsSetupDb setupDb = null;
        boolean noErrors = true;

        try {
            setupDb = getSetupDb(setupConnection);
            setupDb.createDatabase(m_dbProduct, getReplacer(defaultConnection), handleErrors);
            noErrors = setupDb.noErrors();
            setupDb.closeConnection();
        } catch (Exception e) {
            noErrors = false;
        } finally {
            if (setupDb != null) {
                setupDb.closeConnection();
            }
        }

        if (!handleErrors || noErrors) {
            try {
                setupDb = getSetupDb(defaultConnection);
                setupDb.createTables(m_dbProduct, getReplacer(defaultConnection), handleErrors);
                noErrors = setupDb.noErrors();
                setupDb.closeConnection();
            } catch (Exception e) {
                noErrors = false;
            } finally {
                if (setupDb != null) {
                    setupDb.closeConnection();
                }
            }
        }

        if (noErrors) {
            return;
        } else if (handleErrors) {
            removeDatabase(setupConnection, defaultConnection, false);
            setupDatabase(setupConnection, defaultConnection, false);
        } else {
            checkErrors(setupDb);
        }
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
        List<CmsProperty> excludeList) throws CmsException {

        String noMatches = "";
        List<CmsProperty> storedProperties = storedResource.getProperties();
        List<CmsProperty> properties = cms.readPropertyObjects(resourceName, false);
        List<CmsProperty> unmatchedProperties;
        unmatchedProperties = OpenCmsTestResourceFilter.compareProperties(storedProperties, properties, excludeList);
        if (unmatchedProperties.size() > 0) {
            noMatches += "[Properies missing " + unmatchedProperties.toString() + "]\n";
        }
        unmatchedProperties = OpenCmsTestResourceFilter.compareProperties(properties, storedProperties, excludeList);
        if (unmatchedProperties.size() > 0) {
            noMatches += "[Properies additional " + unmatchedProperties.toString() + "]\n";
        }
        return noMatches;
    }

    /**
     * Copies the configuration files from the given folder to the "config" folder.
     * 
     * @param newConfig the folder with the configuration files to copy
     */
    private static void copyConfiguration(String newConfig) {

        File configDir = new File(getTestDataPath("WEB-INF" + File.separatorChar + CmsSystemInfo.FOLDER_CONFIG_DEFAULT));
        File configOriDir = new File(newConfig);

        FileFilter filter = FileFilterUtils.orFileFilter(
            FileFilterUtils.suffixFileFilter(".xml"),
            FileFilterUtils.suffixFileFilter(".properties"));
        if (configOriDir.exists()) {
            File[] oriFiles = configOriDir.listFiles(filter);
            boolean initConfigDates = false;
            if (m_dateConfigFiles == null) {
                m_dateConfigFiles = new long[oriFiles.length];
                initConfigDates = true;
            }
            for (int i = 0; i < oriFiles.length; i++) {
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
            List<CmsAccessControlEntry> excludeList = new ArrayList<CmsAccessControlEntry>();
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
                List<CmsAccessControlEntry> resAces = cms.getAccessControlEntries(resourceName);
                boolean notFound = true;
                Iterator<CmsAccessControlEntry> i = resAces.iterator();
                while (i.hasNext()) {
                    CmsAccessControlEntry resAce = i.next();
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
        } catch (Exception e) {
            e.printStackTrace();
            fail("cannot read resource " + resourceName + " " + e.getMessage());
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
            List<CmsUUID> excludeList = new ArrayList<CmsUUID>();
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

                Map<CmsUUID, CmsPermissionSetCustom> permissionMap = resAcls.getPermissionMap();
                CmsPermissionSet resPermission = permissionMap.get(principal);
                if (resPermission != null) {
                    if (!resPermission.equals(permission)) {
                        fail("[Permission set not equal " + principal + ":" + permission + " != " + resPermission + "]");
                    }
                } else {
                    fail("[Permission set not found " + principal + ":" + permission + "]");
                }
            }
        } catch (Exception e) {
            fail("cannot read resource " + resourceName + " " + e.getMessage());
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

        try {
            // create the exclude list
            List<CmsUUID> excludeList = new ArrayList<CmsUUID>();
            if (permission != null) {
                excludeList.add(principal);
            }

            // TODO: This is the code to recalculate the permission set if necessary. Its not completed yet!

            Map<CmsUUID, String> parents = getParents(cms, resourceName);
            List<CmsAccessControlEntry> aceList = cms.getAccessControlEntries(resourceName);
            Iterator<CmsAccessControlEntry> i = aceList.iterator();
            while (i.hasNext()) {
                CmsAccessControlEntry ace = i.next();
                if (ace.getPrincipal().equals(principal)) {
                    String parent = parents.get(ace.getResource());
                    if ((!parent.equals(modifiedResource)) && (parent.length() > modifiedResource.length())) {
                        permission = new CmsPermissionSet(ace.getAllowedPermissions(), ace.getDeniedPermissions());
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

                Map<CmsUUID, CmsPermissionSetCustom> permissionMap = resAcls.getPermissionMap();
                CmsPermissionSet resPermission = permissionMap.get(principal);
                if (resPermission != null) {
                    if (!resPermission.equals(permission)) {
                        fail("[Permission set not equal " + principal + ":" + permission + " != " + resPermission + "]");
                    }
                } else {
                    fail("[Permission set not found " + principal + ":" + permission + "]");
                }
            }
        } catch (Exception e) {
            fail("cannot read resource " + resourceName + " " + e.getMessage());
        }
    }

    /**
     * Tests if a pattern can be found in a content string.<p>
     * Fails if the pattern is not found.
     * 
     * @param content the content string
     * @param pattern the pattern to search for
     */
    public void assertContains(String content, String pattern) {

        if (content.toLowerCase().indexOf(pattern.toLowerCase()) == -1) {
            fail("pattern '" + pattern + "' not found in content");
        }
    }

    /**
     * Tests if a pattern cannot  be found in a content string.<p>
     * Fails if the pattern is found.
     * 
     * @param content the content string
     * @param pattern the pattern to search for
     */
    public void assertContainsNot(String content, String pattern) {

        if (content.toLowerCase().indexOf(pattern.toLowerCase()) != -1) {
            fail("pattern '" + pattern + "' found in content");
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
            for (int i = 0; i < content.length; i++) {
                if (fileContent[i] != content[i]) {
                    fail("[Content compare failed at index " + i + "]");
                }
            }
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Tests if the current content date of a resource is equals to the given date.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param dateContent the content date
     */
    public void assertDateContent(CmsObject cms, String resourceName, long dateContent) {

        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);

            if (res.getDateContent() != dateContent) {
                fail("[DateContent "
                    + dateContent
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(dateContent)
                    + " != "
                    + res.getDateContent()
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(res.getDateContent())
                    + "]");
            }

        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Tests if the the current date content of a resource is later than the given date.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param dateContent the content date
     */
    public void assertDateContentAfter(CmsObject cms, String resourceName, long dateContent) {

        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);

            if (res.getDateContent() < dateContent) {
                fail("[DateContent "
                    + dateContent
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(dateContent)
                    + " > "
                    + res.getDateContent()
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(res.getDateContent())
                    + "]");
            }

        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
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
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
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
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Compares the current expiration date of a resource with a given date.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param dateExpired the expiration date
     */
    public void assertDateExpired(CmsObject cms, String resourceName, long dateExpired) {

        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);

            if (res.getDateExpired() != dateExpired) {
                fail("[DateExpired "
                    + dateExpired
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(dateExpired)
                    + " != "
                    + res.getDateExpired()
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(res.getDateExpired())
                    + "]");

            }

        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
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
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Tests if the the current date last modified of a resource is later than the given date.<p>
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
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Compares the current release date of a resource with a given date.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param dateReleased the release date
     */
    public void assertDateReleased(CmsObject cms, String resourceName, long dateReleased) {

        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);

            if (res.getDateReleased() != dateReleased) {
                fail("[DateReleased "
                    + dateReleased
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(dateReleased)
                    + " != "
                    + res.getDateReleased()
                    + " i.e. "
                    + CmsDateUtil.getHeaderDate(res.getDateReleased())
                    + "]");

            }

        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Tests if the given exceptions are equal (or both null).<p>
     * 
     * @param e1 first exception to compare
     * @param e2 second exception to compare
     */
    public void assertEquals(CmsException e1, CmsException e2) {

        if ((e1 == null) && (e2 == null)) {
            return;
        }

        if (((e1 == null) && (e2 != null)) || ((e1 != null) && (e2 == null))) {
            fail("Exceptions not equal (not both null)");
        }
        if ((e1 != null) && (e2 != null)) {
            if (!(e1.getClass().equals(e2.getClass()))) {
                fail("Exception " + e1.toString() + " does not equal " + e2.toString());
            }

            if (!(e1.getMessageContainer().getKey().equals(e2.getMessageContainer().getKey()))) {
                fail("Exception " + e1.toString() + " does not equal " + e2.toString());
            }
        }
    }

    /**
     * Tests if the given jobs are internally equal.<p>
     * (May have different wrapper classes)
     * 
     * @param j1 first job to compare
     * @param j2 second job to compare
     * @param comparePublishLists if the publish lists should be compared, too
     * @param compareTime if the timestamps should be compared, too
     */
    public void assertEquals(
        CmsPublishJobBase j1,
        CmsPublishJobBase j2,
        boolean comparePublishLists,
        boolean compareTime) {

        CmsPublishJobInfoBean job1 = new OpenCmsTestPublishJobBase(j1).getInfoBean();
        CmsPublishJobInfoBean job2 = new OpenCmsTestPublishJobBase(j2).getInfoBean();

        if (!(job1.getPublishHistoryId().equals(job2.getPublishHistoryId())
            && job1.getProjectName().equals(job2.getProjectName())
            && job1.getUserId().equals(job2.getUserId())
            && job1.getLocale().equals(job2.getLocale())
            && (job1.getFlags() == job2.getFlags()) && (job1.getSize() == job2.getSize()))) {

            fail("Publish jobs are not equal");
        }

        if (compareTime) {
            if (!((job1.getEnqueueTime() == job2.getEnqueueTime()) && (job1.getStartTime() == job2.getStartTime()) && (job1.getFinishTime() == job2.getFinishTime()))) {

                fail("Publish jobs do not have the same timestamps");
            }
        }

        if (comparePublishLists) {
            if (!job1.getPublishList().toString().equals(job2.getPublishList().toString())) {
                fail("Publish jobs do not have the same publish list");
            }
        }
    }

    /**
     * Tests if the given xml document objects are equals (or both null).<p>
     * 
     * @param d1 first document to compare
     * @param d2 second document to compare
     */
    public void assertEquals(Document d1, Document d2) {

        if ((d1 == null) && (d2 == null)) {
            return;
        }

        if (((d1 == null) && (d2 != null)) || ((d1 != null) && (d2 == null))) {
            fail("Documents not equal (not both null)");
        }

        if ((d1 != null) && (d2 != null)) {
            InternalNodeComparator comparator = new InternalNodeComparator();
            if (comparator.compare((Node)d1, (Node)d2) != 0) {
                fail("Comparison of documents failed: "
                    + "name = "
                    + d1.getName()
                    + ", "
                    + "path = "
                    + comparator.m_node1.getUniquePath()
                    + "\nNode 1:"
                    + comparator.m_node1.asXML()
                    + "\nNode 2:"
                    + comparator.m_node2.asXML());
            }
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
            OpenCmsTestResourceStorageEntry storedResource = m_currentResourceStrorage.get(cms.getSitePath(resource));

            // compare the current resource with the stored resource
            assertFilter(cms, storedResource, resource, filter);
        } catch (Exception e) {
            fail("cannot read resource " + cms.getSitePath(resource) + " " + e.getMessage());
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
            // compare the date content if necessary
            if (filter.testDateContent()) {
                if (storedResource.getDateContent() != res.getDateContent()) {
                    noMatches += "[DateContent "
                        + storedResource.getDateContent()
                        + " i.e. "
                        + CmsDateUtil.getHeaderDate(storedResource.getDateContent())
                        + " != "
                        + res.getDateContent()
                        + " i.e. "
                        + CmsDateUtil.getHeaderDate(res.getDateContent())
                        + "]\n";
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
            if (filter.testDateCreatedSec()) {
                if ((storedResource.getDateCreated() / 1000) != (res.getDateCreated() / 1000)) {
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
            if (filter.testDateLastModifiedSec()) {
                if ((storedResource.getDateLastModified() / 1000) != (res.getDateLastModified() / 1000)) {
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
                if (filter.testName()) {
                    if (!storedResource.getLock().equals(resLock)) {
                        noMatches += "[Lockstate " + storedResource.getLock() + " != " + resLock + "]\n";
                    }
                } else {
                    CmsLock other = storedResource.getLock();
                    if (!other.getUserId().equals(resLock.getUserId())
                        || !other.getProjectId().equals(resLock.getProjectId())
                        || !other.getType().equals(resLock.getType())) {
                        noMatches += "[Lockstate " + storedResource.getLock() + " != " + resLock + "]\n";
                    }
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
                if (!storedResource.getProjectLastModified().equals(res.getProjectLastModified())) {
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
                if (!storedResource.getState().equals(res.getState())) {
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
                    noMatches += createUserFailMessage(
                        cms,
                        "UserCreated",
                        storedResource.getUserLastModified(),
                        res.getUserLastModified());
                    noMatches += "\n";
                }
            }
            // compare the user created if necessary
            if (filter.testUserLastModified()) {
                if (!storedResource.getUserLastModified().equals(res.getUserLastModified())) {
                    noMatches += createUserFailMessage(
                        cms,
                        "UserLastModified",
                        storedResource.getUserLastModified(),
                        res.getUserLastModified());
                    noMatches += "\n";
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
     * 
     * @throws CmsException if something goes wrong 
     */
    public void assertFilter(CmsObject cms, String resourceName, OpenCmsTestResourceFilter filter) throws CmsException {

        // get the stored resource
        OpenCmsTestResourceStorageEntry storedResource = null;

        try {
            storedResource = m_currentResourceStrorage.get(resourceName);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // get the actual resource from the vfs
        CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);

        // compare the current resource with the stored resource
        assertFilter(cms, storedResource, res, filter);
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
     * Tests whether a resource has currently a specified flag set.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param flag a flag to check
     */
    public void assertFlags(CmsObject cms, String resourceName, int flag) {

        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);

            // test if the specified flag is set
            if (!((res.getFlags() & flag) > 0)) {
                fail("[Flags (" + res.getFlags() + ") do not contain flag (" + flag + ")");
            }
        } catch (CmsException e) {
            fail("Error reading resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Checks if the given resource has the correct history count, also
     * check if all entries in the history can be read.<p>
     * 
     * @param cms the current user OpenCms context
     * @param resourcename the name of the resource to check the history for
     * @param versionCount the expected version number of the resource
     *  
     * @throws Exception if the test fails
     */
    public void assertHistory(CmsObject cms, String resourcename, int versionCount) throws Exception {

        CmsResource res = cms.readResource(resourcename, CmsResourceFilter.ALL);

        // assert we have the right version number
        assertEquals(versionCount, res.getVersion());

        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            // no additional test possible for the online project
            return;
        }

        // read all available versions
        List<I_CmsHistoryResource> versions = cms.readAllAvailableVersions(resourcename);

        // new files have no historical entry despite the version number may be greater than 1 for siblings
        if (res.getState().isNew()) {
            assertTrue(versions.isEmpty());
            return;
        }

        // if the resource has not been published yet, the available versions will be one less
        boolean unchanged = res.getState().isUnchanged();
        // the list is sorted descending, ie. last version is first in list
        int count = versionCount - (unchanged ? 0 : 1);

        Iterator<I_CmsHistoryResource> i = versions.iterator();
        while (i.hasNext()) {
            // walk through the list and read all version files
            CmsResource hRes = (CmsResource)i.next();
            if (hRes instanceof CmsHistoryFile) {
                CmsFile hFile = cms.readFile(hRes);
                assertEquals(count, hFile.getVersion());
            } else {
                assertEquals(count, hRes.getVersion());
            }
            count--;
        }
        // finally assert the list size if equal to the history version 
        assertEquals(versionCount - (unchanged ? 0 : 1), versions.size());
    }

    /**
     * Checks if the given resource has the correct history count, also
     * check if all entries in the history can be read.<p>
     * 
     * Use this method only for resources that has been restored.<p>
     * 
     * @param cms the current user OpenCms context
     * @param resourcename the name of the resource to check the history for
     * @param versionCount the expected version number of the resource
     *  
     * @throws Exception if the test fails
     */
    public void assertHistoryForRestored(CmsObject cms, String resourcename, int versionCount) throws Exception {

        CmsResource res = cms.readResource(resourcename, CmsResourceFilter.ALL);

        // assert we have the right version number
        assertEquals(versionCount, res.getVersion());

        if (cms.getRequestContext().getCurrentProject().isOnlineProject()) {
            // no additional test possible for the online project
            return;
        }

        // read all available versions
        List<I_CmsHistoryResource> versions = cms.readAllAvailableVersions(resourcename);

        // if the resource has not been published yet, the available versions will be one less
        boolean unchanged = res.getState().isUnchanged();
        // the list is sorted descending, ie. last version is first in list
        int count = versionCount - (unchanged ? 0 : 1);

        Iterator<I_CmsHistoryResource> i = versions.iterator();
        while (i.hasNext()) {
            // walk through the list and read all version files
            CmsResource hRes = (CmsResource)i.next();
            CmsFile hFile = cms.readFile(hRes);
            assertEquals(count, hFile.getVersion());
            count--;
        }
        // finally assert the list size if equal to the history version 
        assertEquals(versionCount - (unchanged ? 0 : 1), versions.size());
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
                fail("[Not a folder: " + resourceName + "]");
            }
            if (res.getLength() != -1) {
                fail("[Folder length not -1: " + resourceName + "]");
            }
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Tests if the specified object is identical with another object.<p> 
     * 
     * @param o1 an object
     * @param o2 another object
     */
    public void assertIsIdentical(Object o1, Object o2) {

        if (o1 != o2) {
            fail("Object " + o1.toString() + " is not identical to " + o2.toString());
        }
    }

    /**
     * Tests if the specified object is not identical with another object.<p> 
     * 
     * @param o1 an object
     * @param o2 another object
     */
    public void assertIsNotIdentical(Object o1, Object o2) {

        if (o1 == o2) {
            fail("Object " + o1.toString() + " is identical to " + o2.toString());
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

            if (lock.isNullLock() || !lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
                fail("[Lock "
                    + resourceName
                    + " requires must be locked to user "
                    + cms.getRequestContext().getCurrentUser().getId()
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
     * 
     * @see CmsLockType
     */
    public void assertLock(CmsObject cms, String resourceName, CmsLockType lockType) {

        assertLock(cms, resourceName, lockType, cms.getRequestContext().getCurrentUser());
    }

    /**
     * Validates if a specified resource has a lock of a given type and is locked for a principal.<p>
     * 
     * @param cms the current user's Cms object
     * @param resourceName the name of the resource to validate
     * @param lockType the type of the lock
     * @param user the user to check the lock with
     * 
     * @see CmsLockType
     */
    public void assertLock(CmsObject cms, String resourceName, CmsLockType lockType, CmsUser user) {

        try {
            // get the actual resource from the VFS
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);
            CmsLock lock = cms.getLock(res);

            if (lockType.isUnlocked()) {
                if (!lock.isNullLock()) {
                    fail("[Lock " + resourceName + " must be unlocked]");
                }
            } else if (lock.isNullLock() || (lock.getType() != lockType) || !lock.isOwnedBy(user)) {
                fail("[Lock "
                    + resourceName
                    + " requires a lock of type "
                    + lockType
                    + " for user "
                    + user.getId()
                    + " ("
                    + user.getName()
                    + ") but has a lock of type "
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
            hasRedFlag = !res.getState().isUnchanged();
            // and if it was modified in the current project
            hasRedFlag &= (res.getProjectLastModified().equals(cms.getRequestContext().getCurrentProject().getUuid()));
            // and if it was modified by the current user
            hasRedFlag &= (res.getUserLastModified().equals(cms.getRequestContext().getCurrentUser().getId()));

            if (shouldHaveRedFlag && !hasRedFlag) {
                // it should have a red flag, but it hasn't
                fail("[HasRedFlag " + resourceName + " must have a red flag]");
            } else if (hasRedFlag && !shouldHaveRedFlag) {
                // it has a red flag, but it shouldn't
                fail("[HasRedFlag " + resourceName + " must not have a red flag]");
            }
        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Asserts the given permission string with the access control entry for the given resource and principal.<p>
     * 
     * @param cms the cms object
     * @param resourceName the resource name
     * @param principal the principal
     * @param permissionString the permission string to compare
     * 
     * @throws CmsException if something goes wrong
     */
    public void assertPermissionString(
        CmsObject cms,
        String resourceName,
        I_CmsPrincipal principal,
        String permissionString) throws CmsException {

        Iterator<CmsAccessControlEntry> it = cms.getAccessControlEntries(resourceName).iterator();
        while (it.hasNext()) {
            CmsAccessControlEntry ace = it.next();
            if (ace.getPrincipal().equals(principal.getId())) {
                assertEquals(permissionString, ace.getPermissions().getPermissionString()
                    + ace.getInheritingString()
                    + ace.getResponsibleString());
                return;
            }
        }
        if (permissionString != null) {
            fail("Ace not found");
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

            if (!res.getProjectLastModified().equals(project.getUuid())) {
                fail("[ProjectLastModified " + project.getUuid() + " != " + res.getProjectLastModified() + "]");
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
            List<CmsProperty> excludeList = new ArrayList<CmsProperty>();
            excludeList.add(property);

            String noMatches = compareProperties(cms, resourceName, storedResource, excludeList);

            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing resource " + resourceName + " with stored values: " + noMatches);
            }

            // test if the property was already in the stored result
            List<CmsProperty> storedProperties = storedResource.getProperties();
            if (!storedProperties.contains(property)) {
                fail("property not found in stored value: " + property);
            }

            // test if the values of the changed propertiy is correct.
            CmsProperty resourceProperty = cms.readPropertyObject(resourceName, property.getName(), false);
            if (!resourceProperty.isIdentical(property)) {
                fail("property is not identical :" + property + " != " + resourceProperty);
            }
        } catch (Exception e) {
            fail("cannot read resource " + resourceName + " " + e.getMessage());
        }
    }

    /**
     * Compares the current properties of a resource with the stored values and a list of changed property.<p>
     * 
     * @param cms an initialized CmsObject
     * @param resourceName the name of the resource to compare
     * @param excludeList a list of CmsProperties to exclude
     */
    public void assertPropertyChanged(CmsObject cms, String resourceName, List<CmsProperty> excludeList) {

        try {
            // get the stored resource
            OpenCmsTestResourceStorageEntry storedResource = m_currentResourceStrorage.get(resourceName);

            String noMatches = compareProperties(cms, resourceName, storedResource, excludeList);

            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing resource " + resourceName + " with stored values: " + noMatches);
            }

            // test if the values of the changed properties are correct and if the properties
            // were already in the stored result

            String propertyNoMatches = "";
            String storedNotFound = "";
            Iterator<CmsProperty> i = excludeList.iterator();
            List<CmsProperty> storedProperties = storedResource.getProperties();
            while (i.hasNext()) {
                CmsProperty property = i.next();
                CmsProperty resourceProperty = cms.readPropertyObject(resourceName, property.getName(), false);
                // test if the property has the same value
                if (!resourceProperty.isIdentical(property)) {
                    propertyNoMatches += "[" + property + " != " + resourceProperty + "]";
                }
                // test if the property was already in the stored object
                if (!storedProperties.contains(property)) {
                    storedNotFound += "[" + property + "]";
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
        } catch (Exception e) {
            fail("cannot read resource " + resourceName + " " + e.getMessage());
        }
    }

    /**
     * Tests if a properetydefintion does exist.<p>
     * 
     * @param cms the CmsObject
     * @param propertyDefinition the propertsdefinition
     */
    public void assertPropertydefinitionExist(CmsObject cms, CmsPropertyDefinition propertyDefinition) {

        try {
            CmsPropertyDefinition prop = cms.readPropertyDefinition(propertyDefinition.getName());
            if (prop != null) {
                if (!prop.getName().equals(propertyDefinition.getName())) {
                    fail("propertsdefinitions do not match: " + prop + " != " + propertyDefinition);
                }
            } else {
                fail("cannot read propertydefitnion" + propertyDefinition);
            }
        } catch (CmsException e) {
            fail("cannot read propertydefitnion" + propertyDefinition + " " + CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Tests the list the propertydefinitions are identical to a given list except one exlclude propertydefintion.<p>
     * 
     * @param cms the CmsObject
     * @param propertyDefintions the list of propertydefintions 
     * @param exclude the exclude propertydefinition
     */
    public void assertPropertydefinitions(
        CmsObject cms,
        List<CmsPropertyDefinition> propertyDefintions,
        CmsPropertyDefinition exclude) {

        try {
            String noMatches = "";
            List<CmsPropertyDefinition> allPropertydefintions = cms.readAllPropertyDefinitions();
            noMatches += comparePropertydefintions(propertyDefintions, allPropertydefintions, exclude);
            noMatches += comparePropertydefintions(allPropertydefintions, propertyDefintions, exclude);
            if (noMatches.length() > 0) {
                fail("missig propertydefintions: " + noMatches);
            }
        } catch (CmsException e) {
            fail("cannot read propertydefitnions " + CmsException.getStackTraceAsString(e));
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

        } catch (Exception e) {
            fail("cannot read resource " + resourceName + " " + e.getMessage());
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
            List<CmsProperty> excludeList = new ArrayList<CmsProperty>();
            excludeList.add(property);

            String noMatches = compareProperties(cms, resourceName, storedResource, excludeList);

            // now see if we have collected any no-matches
            if (noMatches.length() > 0) {
                fail("error comparing resource " + resourceName + " with stored values: " + noMatches);
            }

            // test if the property was already in the stored result
            List storedProperties = storedResource.getProperties();
            if (storedProperties.contains(property)) {
                fail("property already found in stored value: " + property);
            }

            // test if the values of the changed propertiy is correct.
            CmsProperty resourceProperty = cms.readPropertyObject(resourceName, property.getName(), false);
            if (!resourceProperty.isIdentical(property)) {
                fail("property is not identical :" + property + " != " + resourceProperty);
            }
        } catch (Exception e) {
            fail("cannot read resource " + resourceName + " " + e.getMessage());
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
                fail("error comparing resource " + resourceName + " with stored values: " + noMatches);
            }

            // test if the values of the changed properties are correct and if the properties
            // were already in the stored result

            String propertyNoMatches = "";
            String storedFound = "";
            Iterator i = excludeList.iterator();
            List storedProperties = storedResource.getProperties();
            while (i.hasNext()) {
                CmsProperty property = (CmsProperty)i.next();
                CmsProperty resourceProperty = cms.readPropertyObject(resourceName, property.getName(), false);
                // test if the property has the same value
                if (!resourceProperty.isIdentical(property)) {
                    propertyNoMatches += "[" + property + " != " + resourceProperty + "]";
                }
                // test if the property was already in the stored object
                if (storedProperties.contains(property)) {
                    storedFound += "[" + property + "]";
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
        } catch (Exception e) {
            fail("cannot read resource " + resourceName + " " + e.getMessage());
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
                fail("error comparing resource " + resourceName + " with stored values: " + noMatches);
            }

            // test if the property was already in the stored result
            List storedProperties = storedResource.getProperties();
            if (!storedProperties.contains(property)) {
                fail("property not found in stored value: " + property);
            }

            // test if the values of the changed propertiy is correct.
            CmsProperty resourceProperty = cms.readPropertyObject(resourceName, property.getName(), false);
            if (resourceProperty != CmsProperty.getNullProperty()) {
                fail("property is not removed :" + property + " != " + resourceProperty);
            }
        } catch (Exception e) {
            fail("cannot read resource " + resourceName + " " + e.getMessage());
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
                fail("error comparing resource " + resourceName + " with stored values: " + noMatches);
            }

            // test if the values of the changed properties are correct and if the properties
            // were already in the stored result

            String propertyNotDeleted = "";
            String storedNotFound = "";
            Iterator i = excludeList.iterator();
            List storedProperties = storedResource.getProperties();
            List resourceProperties = cms.readPropertyObjects(resourceName, false);

            while (i.hasNext()) {
                CmsProperty property = (CmsProperty)i.next();
                // test if the property has the same value
                if (resourceProperties.contains(property)) {
                    CmsProperty resourceProperty = cms.readPropertyObject(resourceName, property.getName(), false);
                    propertyNotDeleted += "[" + property + " != " + resourceProperty + "]";
                }
                // test if the property was already in the stored object
                if (!storedProperties.contains(property)) {
                    storedNotFound += "[" + property + "]";
                }
            }
            // now see if we have collected any property no-matches
            if (propertyNotDeleted.length() > 0) {
                fail("properties not deleted for " + resourceName + ": " + propertyNotDeleted);
            }
            // now see if we have collected any property not found in the stored original
            if (storedNotFound.length() > 0) {
                fail("properties not found in stored value: " + storedNotFound);
            }
        } catch (Exception e) {
            fail("cannot read resource " + resourceName + " " + e.getMessage());
        }
    }

    /**
     * Asserts the equality of the two given relations.<p>
     * 
     * @param expected the expected relation
     * @param actual the actual result
     */
    public void assertRelation(CmsRelation expected, CmsRelation actual) {

        assertEquals(expected.getSourceId(), actual.getSourceId());
        assertEquals(expected.getSourcePath(), actual.getSourcePath());
        assertEquals(expected.getTargetId(), actual.getTargetId());
        assertEquals(expected.getTargetPath(), actual.getTargetPath());
        assertEquals(expected.getType(), actual.getType());
    }

    /**
     * Compares the current resource id of a resource with a given id.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param resourceId the id
     */
    public void assertResourceId(CmsObject cms, String resourceName, CmsUUID resourceId) {

        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);

            if (!res.getResourceId().equals(resourceId)) {
                fail("[ResourceId] " + resourceId + " != " + res.getResourceId() + "]");
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
                fail("[ResourceType " + res.getTypeId() + " != " + resourceType + "]");
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

            if (res.getSiblingCount() != (entry.getSiblingCount() + increment)) {
                fail("[SiblingCount "
                    + res.getSiblingCount()
                    + " != "
                    + entry.getSiblingCount()
                    + "+"
                    + increment
                    + "]");
            }

        } catch (Exception e) {
            fail("cannot read resource " + resourceName + " " + e.getMessage());
        }
    }

    /**
     * Compares the current state of a resource with a given state.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param state the state
     */
    public void assertState(CmsObject cms, String resourceName, CmsResourceState state) {

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
     * Compares the current structure id of a resource with a given id.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param structureId the id
     */
    public void assertStructureId(CmsObject cms, String resourceName, CmsUUID structureId) {

        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);

            if (!res.getStructureId().equals(structureId)) {
                fail("[StructureId] " + structureId + " != " + res.getStructureId() + "]");
            }

        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Compares the current type of a resource with a given type.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param type the type
     */
    public void assertType(CmsObject cms, String resourceName, int type) {

        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);

            if (res.getTypeId() != type) {
                fail("[State " + type + " != " + res.getTypeId() + "]");
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
                fail(createUserFailMessage(cms, "UserCreated", user.getId(), res.getUserLastModified()));
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
                fail(createUserFailMessage(cms, "UserLastModified", user.getId(), res.getUserLastModified()));
            }

        } catch (CmsException e) {
            fail("cannot read resource " + resourceName + " " + CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Tests if the current version of a resource is equals to the given version number.<p>
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to compare
     * @param version the version number to check
     */
    public void assertVersion(CmsObject cms, String resourceName, int version) {

        try {
            // get the actual resource from the vfs
            CmsResource res = cms.readResource(resourceName, CmsResourceFilter.ALL);

            assertEquals("Version", version, res.getVersion());
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
     * Should return the additional connection name.<p>
     * 
     * @return the name of the additional connection
     */
    public String getConnectionName() {

        return "additional";
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
     * @throws Exception in case something goes wrong
     */
    public CmsResourceState getPreCalculatedState(String resourceName) throws Exception {

        return m_currentResourceStrorage.getPreCalculatedState(resourceName);
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
                // this is a folder, so first add the folder itself to the storage
                m_currentResourceStrorage.add(cms, resourceName
                    + (resourceName.charAt(resourceName.length() - 1) != '/' ? "/" : ""), resource);

                if (!storeSubresources) {
                    return;
                }

                // now get all subresources and add them as well
                List resources = cms.readResources(resourceName, CmsResourceFilter.ALL);
                Iterator i = resources.iterator();
                while (i.hasNext()) {
                    CmsResource res = (CmsResource)i.next();
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

        OpenCmsTestResourceStorage storage = m_resourceStorages.get(name);
        if (storage != null) {
            m_currentResourceStrorage = storage;
        } else {
            throw new CmsException(Messages.get().container(Messages.ERR_RESOURCE_STORAGE_NOT_FOUND_0));
        }
    }

    /**
     * Deletes the given file from the rfs.<p>
     * 
     * @param absolutePath the absolute path of the file
     */
    protected void deleteFile(String absolutePath) {

        try {
            // sleep 0.5 seconds - sometimes deletion does not work if not waiting
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // ignore
        }
        File file = new File(absolutePath);
        if (file.exists()) {
            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
    }

    /**
     * Writes a message to the current output stream.<p>
     * 
     * @param message the message to write
     */
    protected void echo(String message) {

        try {
            System.out.println();
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

        OpenCmsTestResourceStorage storage = m_resourceStorages.get(name);
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

        m_shell = new CmsShell(getTestDataPath("WEB-INF" + File.separator), null, null, "${user}@${project}>", null);

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
        List resAce = cms.getAccessControlEntries(resourceName);
        List storedAce = storedResource.getAccessControlEntries();
        List unmatchedAce;
        unmatchedAce = compareAce(resAce, storedAce, excludeList);
        if (unmatchedAce.size() > 0) {
            noMatches += "[ACE missing " + unmatchedAce.toString() + "]\n";
        }
        unmatchedAce = compareAce(storedAce, resAce, excludeList);
        if (unmatchedAce.size() > 0) {
            noMatches += "[ACE missing " + unmatchedAce.toString() + "]\n";
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
            noMatches += "[ACL differences " + unmatchedList.toString() + "]\n";
        }
        unmatchedList = compareList(storedList, resList, excludeList);
        if (unmatchedList.size() > 0) {
            noMatches += "[ACL differences " + unmatchedList.toString() + "]\n";
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
    private List compareAce(List source, List target, List exclude) {

        boolean isOverwriteAll = false;
        Iterator itTargets = target.iterator();
        while (itTargets.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)itTargets.next();
            if (ace.isOverwriteAll()) {
                isOverwriteAll = true;
            }
        }
        List result = new ArrayList();
        Iterator i = source.iterator();
        while (i.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)i.next();
            // here would be best to check the path of the overwrite all entry
            // but since we have just the resource id, instead of the structure id
            // we are not able to do that here :(
            if (!target.contains(ace) && !isOverwriteAll) {
                result.add(ace);
            }
        }
        // finally match the result list with the exclude list
        if (exclude != null) {
            Iterator l = exclude.iterator();
            while (l.hasNext()) {
                CmsAccessControlEntry excludeAce = (CmsAccessControlEntry)l.next();
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

        boolean isOverwriteAll = false;
        Iterator itTargets = target.getPermissionMap().keySet().iterator();
        while (itTargets.hasNext()) {
            CmsUUID principalId = (CmsUUID)itTargets.next();
            if (principalId.equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID)) {
                isOverwriteAll = true;
            }
        }

        HashMap result = new HashMap();

        Map destinationMap = target.getPermissionMap();
        Map sourceMap = source.getPermissionMap();

        Iterator i = sourceMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry)i.next();
            CmsUUID key = (CmsUUID)entry.getKey();
            CmsPermissionSet value = (CmsPermissionSet)entry.getValue();
            if (destinationMap.containsKey(key)) {
                CmsPermissionSet destValue = (CmsPermissionSet)destinationMap.get(key);
                if (!destValue.equals(value)) {
                    result.put(key, key + " " + value + " != " + destValue);
                }
            } else if (!isOverwriteAll) {
                // here would be best to check the path of the overwrite all entry
                // but since we have just the resource id, instead of the structure id
                // we are not able to do that here :(
                result.put(key, "missing " + key);
            }
        }

        // finally match the result list with the exclude list
        if (exclude != null) {
            Iterator l = exclude.iterator();
            while (l.hasNext()) {
                CmsUUID excludeUUID = (CmsUUID)l.next();
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
    private String comparePropertydefintions(List source, List target, CmsPropertyDefinition exclude) {

        String noMatches = "";
        Iterator i = source.iterator();
        while (i.hasNext()) {
            CmsPropertyDefinition prop = (CmsPropertyDefinition)i.next();
            if ((!target.contains(prop)) && (!prop.getName().equals(exclude.getName()))) {
                noMatches += "[" + prop + "]";
            }
        }
        return noMatches;
    }

    /**
     * Creates a user compare fail message.<p>
     * 
     * @param cms the current OpenCms user context
     * @param message the message to show
     * @param user1 the id of the first (expected) user
     * @param user2 the id of the second (found) user
     * @return a user compare fail message
     * 
     * @throws CmsException if one of the users can't be read
     */
    private String createUserFailMessage(CmsObject cms, String message, CmsUUID user1, CmsUUID user2)
    throws CmsException {

        StringBuffer result = new StringBuffer();
        result.append("[");
        result.append(message);
        result.append(" (");
        result.append(cms.readUser(user1).getName());
        result.append(") ");
        result.append(user1);
        result.append(" != (");
        result.append(cms.readUser(user2).getName());
        result.append(") ");
        result.append(user1);
        result.append("]");
        return result.toString();
    }

    /**
     * Creates a map of all parent resources of a OpenCms resource.<p>
     * The resource UUID is used as key, the full resource path is used as the value.
     * 
     * @param cms the CmsObject
     * @param resourceName the name of the resource to get the parent map from
     * @return HashMap of parent resources
     */
    private Map<CmsUUID, String> getParents(CmsObject cms, String resourceName) {

        HashMap<CmsUUID, String> parents = new HashMap<CmsUUID, String>();
        List<CmsResource> parentResources = new ArrayList<CmsResource>();
        try {
            // get all parent folders of the current file
            parentResources = cms.readPath(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (CmsException e) {
            // ignore
        }
        Iterator<CmsResource> k = parentResources.iterator();
        while (k.hasNext()) {
            // add the current folder to the map
            CmsResource curRes = k.next();
            parents.put(curRes.getResourceId(), curRes.getRootPath());
        }
        return parents;
    }

    /**
     * Initializes the OpenCms/database configuration 
     * by reading the appropriate values from opencms.properties.<p>
     */
    private void initConfiguration() {

        if (m_configuration == null) {
            initTestDataPath();
            m_configuration = OpenCmsTestProperties.getInstance().getConfiguration();
            m_dbProduct = OpenCmsTestProperties.getInstance().getDbProduct();
            int index = 0;
            boolean cont;
            do {
                cont = false;
                if (m_configuration.containsKey(OpenCmsTestProperties.PROP_TEST_DATA_PATH + "." + index)) {
                    addTestDataPath(m_configuration.get(OpenCmsTestProperties.PROP_TEST_DATA_PATH + "." + index));
                    cont = true;
                    index++;
                }
            } while (cont);

            try {
                String propertyFile = getTestDataPath("WEB-INF/config." + m_dbProduct + "/opencms.properties");
                m_configuration = new CmsParameterConfiguration(propertyFile);
            } catch (IOException e) {
                fail(e.toString());
                return;
            }

            String key = "setup";
            m_setupConnection = new ConnectionData();
            m_setupConnection.m_dbName = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL + "." + key + "." + "dbName");
            m_setupConnection.m_jdbcUrl = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL + "." + key + "." + "jdbcUrl");
            m_setupConnection.m_userName = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL + "." + key + "." + "user");
            m_setupConnection.m_userPassword = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL
                + "."
                + key
                + "."
                + "password");
            m_setupConnection.m_jdbcDriver = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL
                + "."
                + key
                + "."
                + CmsDbPool.KEY_JDBC_DRIVER);
            m_setupConnection.m_jdbcUrl = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL
                + "."
                + key
                + "."
                + CmsDbPool.KEY_JDBC_URL);
            m_setupConnection.m_jdbcUrlParams = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL
                + "."
                + key
                + "."
                + CmsDbPool.KEY_JDBC_URL_PARAMS);

            key = "default";
            m_defaultConnection = new ConnectionData();
            m_defaultConnection.m_dbName = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL + "." + key + "." + "dbName");
            m_defaultConnection.m_userName = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL
                + "."
                + key
                + "."
                + CmsDbPool.KEY_USERNAME);
            m_defaultConnection.m_userPassword = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL
                + "."
                + key
                + "."
                + CmsDbPool.KEY_PASSWORD);
            m_defaultConnection.m_jdbcDriver = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL
                + "."
                + key
                + "."
                + CmsDbPool.KEY_JDBC_DRIVER);
            m_defaultConnection.m_jdbcUrl = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL
                + "."
                + key
                + "."
                + CmsDbPool.KEY_JDBC_URL);
            m_defaultConnection.m_jdbcUrlParams = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL
                + "."
                + key
                + "."
                + CmsDbPool.KEY_JDBC_URL_PARAMS);

            key = getConnectionName();
            if (m_configuration.get(CmsDbPool.KEY_DATABASE_POOL + "." + key + "." + "dbName") != null) {
                m_additionalConnection = new ConnectionData();
                m_additionalConnection.m_dbName = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL
                    + "."
                    + key
                    + "."
                    + "dbName");
                m_additionalConnection.m_userName = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL
                    + "."
                    + key
                    + "."
                    + CmsDbPool.KEY_USERNAME);
                m_additionalConnection.m_userPassword = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL
                    + "."
                    + key
                    + "."
                    + CmsDbPool.KEY_PASSWORD);
                m_additionalConnection.m_jdbcDriver = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL
                    + "."
                    + key
                    + "."
                    + CmsDbPool.KEY_JDBC_DRIVER);
                m_additionalConnection.m_jdbcUrl = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL
                    + "."
                    + key
                    + "."
                    + CmsDbPool.KEY_JDBC_URL);
                m_additionalConnection.m_jdbcUrlParams = m_configuration.get(CmsDbPool.KEY_DATABASE_POOL
                    + "."
                    + key
                    + "."
                    + CmsDbPool.KEY_JDBC_URL_PARAMS);
            }

            m_defaultTablespace = m_configuration.get("db.oracle.defaultTablespace");
            m_indexTablespace = m_configuration.get("db.oracle.indexTablespace");
            m_tempTablespace = m_configuration.get("db.oracle.temporaryTablespace");

            System.out.println("----- Starting tests on database "
                + m_dbProduct
                + " ("
                + m_setupConnection.m_jdbcUrl
                + ") "
                + "-----");
        }
    }

}
