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

import org.opencms.setup.comptest.CmsSetupTestResult;
import org.opencms.setup.comptest.CmsSetupTests;
import org.opencms.util.CmsStringUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * A bean to perform a OpenCms setup automatically.<p>
 * 
 * @since 9.0
 */
public class CmsAutoSetup {

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
        m_bean.init(props.getSetupWebappPath(), null, props.getSetupDefaultWebappName());
    }

    /**
     * Main program entry point when started via the command line.<p>
     *
     * @param args parameters passed to the application via the command line
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {

        String path = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith(PARAM_CONFIG_PATH)) {
                path = arg.substring(PARAM_CONFIG_PATH.length());
            }
        }
        new CmsAutoSetup(new CmsAutoSetupProperties(path)).run();
        System.exit(0);
    }

    /**
     * Performs the setup.<p>
     * @throws Exception 
     */
    public void run() throws Exception {

        if (m_bean.getWizardEnabled()) {
            CmsSetupTests setupTests = new CmsSetupTests();
            setupTests.runTests(m_bean, "no server info present, because this test is running in auto mode.");
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

            m_bean.setDatabase(m_props.getDbProduct());
            m_bean.setDb(m_props.getDbName());
            m_bean.setDbCreateUser(m_props.getCreateUser());
            m_bean.setDbCreatePwd(m_props.getCreatePwd() == null ? "" : m_props.getCreatePwd());
            m_bean.setDbWorkUser(m_props.getWorkerUser());
            m_bean.setDbWorkPwd(m_props.getWorkerPwd() == null ? "" : m_props.getWorkerPwd());
            m_bean.setDbCreateConStr(m_props.getConnectionUrl());
            m_bean.setDbWorkConStr(m_props.getConnectionUrl());
            m_bean.setDbParamaters(m_props.toParameterMap(), m_props.getDbProduct(), "/opencms/", null);

            boolean enableContinue;
            String chkVars;
            if (m_bean.isInitialized()) {
                CmsSetupDb db = new CmsSetupDb(m_bean.getWebAppRfsPath());
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
                List<String> conErrors = new ArrayList<String>(db.getErrors());
                db.clearErrors();
                enableContinue = conErrors.isEmpty();
                chkVars = db.checkVariables(m_bean.getDatabase());
                db.closeConnection();
                if (enableContinue && db.noErrors() && m_bean.validateJdbc()) {
                    // go on
                    System.out.println("DB setup was successful");
                    if (chkVars != null) {
                        // System.out.println(chkVars);
                    }
                } else {
                    throw new Exception("DB Connection test faild.");
                }
            }

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
            if (dbExists && m_props.isCreateDb() && m_props.isDropDb() && (db != null)) {
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
                System.out.println("Database dropped successful!");
            }

            if (m_props.isCreateDb() && (db != null)) {
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
                System.out.println("Database created successful!");
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
                    throw new Exception("Error ocurred while creating tables!");
                }
                db.closeConnection();
                System.out.println("Tables created successful!");
            }

            if (db != null) {
                db.closeConnection();
            }

            m_bean.setServerName(m_props.getServerName());
            m_bean.setWorkplaceSite(m_props.getServerUrl());
            m_bean.setEthernetAddress(getMacAdress());

            // initialize the available modules
            m_bean.getAvailableModules();
            List<String> componentsToInstall = m_props.getInstallComponents();

            String modules = m_bean.getComponentModules(componentsToInstall);
            System.out.println(modules);

            m_bean.setInstallModules(modules);

            if (m_bean.prepareStep8()) {
                m_bean.prepareStep8b();
            }

            while (m_bean.isImportRunning()) {
                Thread.sleep(500);
            }

            m_bean.prepareStep10();

            System.out.println();
            System.out.println("##################");
            System.out.println("# Setup finished #");
            System.out.println("##################");

        } else {
            throw new Exception("Error starting Alkacon OpenCms setup wizard.");
        }
    }

    /**
     * Returns the MAC address.<p>
     * 
     * @return the MAC address
     */
    private String getMacAdress() {

        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", new Byte(mac[i]), (i < (mac.length - 1)) ? ":" : ""));
            }
            return sb.toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }
}
