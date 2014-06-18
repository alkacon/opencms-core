/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.main.CmsLog;
import org.opencms.setup.comptest.CmsSetupTestResult;
import org.opencms.setup.comptest.CmsSetupTests;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * A bean to perform a OpenCms setup automatically.<p>
 * 
 * @since 9.0
 */
public class CmsAutoSetup {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAutoSetup.class);

    /** A constant fpr the path, where the "setup.properties" files is placed on the local file system. */
    private static final String PARAM_CONFIG_PATH = "-path";

    /** The setup bean. */
    private CmsSetupBean m_bean;

    /** The setup properties to use. */
    private CmsAutoSetupProperties m_props;

    /**
     * A bean for a automatically performed setup of OpenCms.<p>
     * 
     * @param props the properties to use
     */
    public CmsAutoSetup(CmsAutoSetupProperties props) {

        m_props = props;
        m_bean = new CmsSetupBean();
        m_bean.init(props.getSetupWebappPath(), props.getServeltMapping(), props.getSetupDefaultWebappName());
    }

    /**
     * Main program entry point when started via the command line.<p>
     *
     * @param args parameters passed to the application via the command line
     */
    public static void main(String[] args) {

        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println("Setup started at: (" + new Date(System.currentTimeMillis()) + ")");
        System.out.println("-------------------------------------------------");
        System.out.println();

        String path = null;

        if ((args[0] != null) && args[0].startsWith(PARAM_CONFIG_PATH)) {
            if ((args.length == 2) && (args[1] != null) && new File(args[1]).exists()) {
                path = args[1];
            } else {
                path = args[0].substring(PARAM_CONFIG_PATH.length()).trim();
            }
        }

        if (new File(path).exists()) {
            System.out.println("Using config file: " + path + ":");
            try {
                CmsAutoSetupProperties props = new CmsAutoSetupProperties(path);
                for (Map.Entry<String, String[]> entry : props.toParameterMap().entrySet()) {
                    System.out.println(entry.getKey() + " = " + entry.getValue()[0]);
                }
                System.out.println();
                new CmsAutoSetup(props).run();
            } catch (Exception e) {
                System.out.println("An error occurred during the setup process with the following error message:");
                System.out.println(e.getMessage());
                System.out.println("Please have a look into the opencms log file for detailed information.");
                LOG.error(e.getMessage(), e);
                System.exit(1);
            }
        } else {
            System.out.println("");
            System.err.println("Config file not found, please specify a path where to find the setup properties to use.");
            System.out.println("Usage example (Unix): setup.sh  -path /path/to/setup.properties");
            System.out.println("Usage example (Win):  setup.bat -path C:\\setup.properties");
            System.out.println("");
            System.out.println("Have a look at the package: org/opencms/setup/setup.properties.example");
            System.out.println("in order to find a sample configuration file.");
        }
        System.exit(0);
    }

    /**
     * Performs the setup.<p>
     * @throws Exception 
     */
    public void run() throws Exception {

        if (m_bean.getWizardEnabled()) {

            long timeStarted = System.currentTimeMillis();

            CmsSetupTests setupTests = new CmsSetupTests();
            setupTests.runTests(m_bean, "No server info present, because this test is running in auto mode.");
            if (setupTests.isRed()) {
                for (CmsSetupTestResult result : setupTests.getTestResults()) {
                    if (result.isRed()) {
                        throw new Exception(result.getInfo());
                    }
                }
            }
            if (CmsStringUtil.isEmpty(m_bean.getDatabaseConfigPage(m_props.getDbProduct()))) {
                throw new Exception("DB product: " + m_props.getDbProduct() + " not supported.");
            }
            System.out.println("System requirements tested successfully.");

            m_bean.setAutoMode(true);
            m_bean.setDatabase(m_props.getDbProduct());
            m_bean.setDb(m_props.getDbName());
            m_bean.setDbCreateUser(m_props.getCreateUser());
            m_bean.setDbCreatePwd(m_props.getCreatePwd() == null ? "" : m_props.getCreatePwd());
            m_bean.setDbWorkUser(m_props.getWorkerUser());
            m_bean.setDbWorkPwd(m_props.getWorkerPwd() == null ? "" : m_props.getWorkerPwd());
            m_bean.setDbCreateConStr(m_props.getConnectionUrl());
            m_bean.setDbWorkConStr(m_props.getConnectionUrl());
            m_bean.setDbParamaters(m_props.toParameterMap(), m_props.getDbProvider(), "/opencms/", null);

            m_bean.setServerName(m_props.getServerName());
            m_bean.setWorkplaceSite(m_props.getServerUrl());
            m_bean.setEthernetAddress(m_props.getEthernetAddress() == null
            ? CmsStringUtil.getEthernetAddress()
            : m_props.getEthernetAddress());

            // initialize the available modules
            m_bean.getAvailableModules();
            List<String> componentsToInstall = m_props.getInstallComponents();
            String modules = m_bean.getComponentModules(componentsToInstall);
            m_bean.setInstallModules(modules);

            if (m_bean.isInitialized()) {
                System.out.println("Setup-Bean initialized successfully.");
                CmsSetupDb db = new CmsSetupDb(m_bean.getWebAppRfsPath());
                try {
                    // try to connect as the runtime user
                    db.setConnection(
                        m_bean.getDbDriver(),
                        m_bean.getDbWorkConStr(),
                        m_bean.getDbConStrParams(),
                        m_bean.getDbWorkUser(),
                        m_bean.getDbWorkPwd());
                    if (!db.noErrors()) {
                        // try to connect as the setup user
                        db.closeConnection();
                        db.clearErrors();
                        db.setConnection(
                            m_bean.getDbDriver(),
                            m_bean.getDbCreateConStr(),
                            m_bean.getDbConStrParams(),
                            m_bean.getDbCreateUser(),
                            m_bean.getDbCreatePwd());
                    }
                    if (!db.noErrors() || !m_bean.validateJdbc()) {
                        throw new Exception("DB Connection test faild.");
                    }
                } finally {
                    db.clearErrors();
                    db.closeConnection();
                }
            }

            System.out.println("DB connection tested successfully.");

            CmsSetupDb db = null;
            boolean dbExists = false;
            if (m_bean.isInitialized()) {
                if (m_props.isCreateDb() || m_props.isCreateTables()) {
                    db = new CmsSetupDb(m_bean.getWebAppRfsPath());
                    /* check if database exists */
                    if (m_bean.getDatabase().startsWith("oracle")
                        || m_bean.getDatabase().startsWith("db2")
                        || m_bean.getDatabase().startsWith("as400")) {
                        db.setConnection(
                            m_bean.getDbDriver(),
                            m_bean.getDbWorkConStr(),
                            m_bean.getDbConStrParams(),
                            m_bean.getDbWorkUser(),
                            m_bean.getDbWorkPwd());
                    } else {
                        db.setConnection(
                            m_bean.getDbDriver(),
                            m_bean.getDbWorkConStr(),
                            m_bean.getDbConStrParams(),
                            m_bean.getDbCreateUser(),
                            m_bean.getDbCreatePwd());
                        dbExists = db.noErrors();
                        if (dbExists) {
                            db.closeConnection();
                        } else {
                            db.clearErrors();
                        }
                    }
                    if (!dbExists || m_props.isDropDb()) {
                        db.closeConnection();
                        if (!m_bean.getDatabase().startsWith("db2") && !m_bean.getDatabase().startsWith("as400")) {
                            db.setConnection(
                                m_bean.getDbDriver(),
                                m_bean.getDbCreateConStr(),
                                m_bean.getDbConStrParams(),
                                m_bean.getDbCreateUser(),
                                m_bean.getDbCreatePwd());
                        }
                    }
                }
            }
            if (!m_props.isCreateDb() && !m_props.isCreateTables() && !dbExists) {
                throw new Exception("You have not created the Alkacon OpenCms database.");
            }
            if (dbExists && m_props.isCreateTables() && !m_props.isDropDb() && (db != null)) {
                throw new Exception("You have selected to not drop existing DBs, but a DB with the given name exists.");
            }
            if (dbExists
                && m_props.isCreateDb()
                && m_props.isDropDb()
                && (db != null)
                && (!CmsSetupBean.JPA_PROVIDER.equals(m_props.getDbProvider()))) {
                // drop the DB
                db.closeConnection();
                db.setConnection(
                    m_bean.getDbDriver(),
                    m_bean.getDbWorkConStr(),
                    m_bean.getDbConStrParams(),
                    m_bean.getDbCreateUser(),
                    m_bean.getDbCreatePwd());
                db.dropDatabase(m_bean.getDatabase(), m_bean.getReplacer());
                if (!db.noErrors()) {
                    for (String error : db.getErrors()) {
                        System.err.println(error + "\n");
                        System.err.println("-------------------------------------------\n");
                    }
                    db.clearErrors();
                    throw new Exception("Error ocurred while dropping the DB!");
                }
                System.out.println("Database dropped successfully.");
            }

            if (m_props.isCreateDb() && (db != null) && (!CmsSetupBean.JPA_PROVIDER.equals(m_props.getDbProvider()))) {
                // Create Database
                db.createDatabase(m_bean.getDatabase(), m_bean.getReplacer());
                if (!db.noErrors()) {
                    for (String error : db.getErrors()) {
                        System.err.println(error + "\n");
                        System.err.println("-------------------------------------------\n");
                    }
                    db.clearErrors();
                    throw new Exception("Error ocurred while creating the DB!");
                }
                db.closeConnection();
                System.out.println("Database created successfully.");
            }

            if (m_props.isCreateTables() && (db != null)) {
                db.setConnection(
                    m_bean.getDbDriver(),
                    m_bean.getDbWorkConStr(),
                    m_bean.getDbConStrParams(),
                    m_bean.getDbWorkUser(),
                    m_bean.getDbWorkPwd());
                //Drop Tables (intentionally quiet)
                db.dropTables(m_bean.getDatabase());
                db.clearErrors();
                db.closeConnection();
                // reopen the connection in order to display errors
                db.setConnection(
                    m_bean.getDbDriver(),
                    m_bean.getDbWorkConStr(),
                    m_bean.getDbConStrParams(),
                    m_bean.getDbWorkUser(),
                    m_bean.getDbWorkPwd());
                //Create Tables
                db.createTables(m_bean.getDatabase(), m_bean.getReplacer());
                if (!db.noErrors()) {
                    for (String error : db.getErrors()) {
                        System.err.println(error + "\n");
                        System.err.println("-------------------------------------------\n");
                    }
                    db.clearErrors();
                    throw new Exception("Error ocurred while creating tables.");
                }
                db.closeConnection();
                System.out.println("Tables created successfully.");
            }
            if (db != null) {
                db.closeConnection();
            }
            System.out.println("Database setup was successful.");

            if (m_bean.prepareStep8()) {
                System.out.println("Configuration files written successfully.");
                m_bean.prepareStep8b();
            }
            while (m_bean.isImportRunning()) {
                Thread.sleep(500);
            }
            System.out.println("Modules imported successfully");

            m_bean.prepareStep10();
            System.out.println();
            System.out.println("-------------------------------------------");
            System.out.println("Setup finished successfully in: "
                + Math.round((System.currentTimeMillis() - timeStarted) / 1000)
                + " seconds.");
            System.out.println("-------------------------------------------");
        } else {
            throw new Exception("Error starting Alkacon OpenCms setup wizard.");
        }
    }
}
