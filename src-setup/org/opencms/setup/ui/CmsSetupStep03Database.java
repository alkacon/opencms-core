/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.setup.ui;

import org.opencms.setup.CmsSetupBean;
import org.opencms.setup.CmsSetupDb;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;

/**
 * Setup step: Databbase settings.
 */
public class CmsSetupStep03Database extends A_CmsSetupStep {

    /**
     * Exception class for use during DB setup, where we get lists of strings as errors from CmsSetupDb instead of the original exceptions.
     */
    class DBException extends RuntimeException {

        /** The list of original errors. */
        private List<String> m_errors;

        /** The error message. */
        private String m_message;

        /**
         * Creates a new instance.<p>
         *
         * @param message the error message
         * @param errors the list of original errors
         */
        public DBException(String message, List<String> errors) {

            m_message = message;
            m_errors = new ArrayList<>(errors);

        }

        /**
         * Gets original errors, separated by newlines.
         *
         * @return the original errors
         */
        public String getDetails() {

            return CmsStringUtil.listAsString(m_errors, "\n");
        }

        /**
         * @see java.lang.Throwable#getMessage()
         */
        @Override
        public String getMessage() {

            return m_message;
        }
    }

    /** Back button. */
    private Button m_backButton;

    /** DB selector. */
    private ComboBox m_dbSelect;

    /** Forward button. */
    private Button m_forwardButton;

    /** Main layout. */
    private VerticalLayout m_mainLayout;

    /** Array for storing the DB settings panel (need to wrap it in array because it's not part of the declarative layout). */
    private CmsDbSettingsPanel[] m_panel = {null};

    /** Setup bean. */
    private CmsSetupBean m_setupBean;

    /**
     * Creates a new instance.
     *
     * @param context the setup context
     */
    public CmsSetupStep03Database(I_SetupUiContext context) {

        super(context);
        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
        m_setupBean = context.getSetupBean();
        Map<String, Properties> propsForDbs = m_setupBean.getDatabaseProperties();
        List<String> dbList = new ArrayList<>();

        for (Map.Entry<String, Properties> entry : propsForDbs.entrySet()) {
            dbList.add(entry.getKey());
        }
        m_dbSelect.setItems(dbList);
        m_dbSelect.setItemCaptionGenerator(db -> propsForDbs.get(db).get(db + ".name").toString());
        String path = context.getSetupBean().getServletConfig().getServletContext().getContextPath();
        int lastSlash = path.lastIndexOf("/");
        String webapp = null;
        if (lastSlash != -1) {
            String lastSegment = path.substring(lastSlash + 1);
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(lastSegment)) {
                webapp = lastSegment;
            }
        }
        final String fWebapp = webapp;
        m_dbSelect.addValueChangeListener(evt -> {
            String value = (String)(evt.getValue());
            updateDb(value, fWebapp);
        });
        m_dbSelect.setNewItemProvider(null);
        m_dbSelect.setEmptySelectionAllowed(false);
        m_dbSelect.setValue("mysql");
        m_forwardButton.addClickListener(evt -> forward());
        m_backButton.addClickListener(evt -> m_context.stepBack());
    }

    /**
     * Creates DB and tables when necessary.<p>
     *
     * @throws Exception in case creating DB or tables fails
     */
    public void setupDb(boolean createDb, boolean createTables, boolean dropDb) throws Exception {

        boolean dbExists = false;
        if (m_setupBean.isInitialized()) {
            System.out.println("Setup-Bean initialized successfully.");
            CmsSetupDb db = new CmsSetupDb(m_setupBean.getWebAppRfsPath());
            try {
                // try to connect as the runtime user
                System.out.println("Check runtime connection....");
                db.setConnection(
                    m_setupBean.getDbDriver(),
                    m_setupBean.getDbWorkConStr(),
                    m_setupBean.getDbConStrParams(),
                    m_setupBean.getDbWorkUser(),
                    m_setupBean.getDbWorkPwd(),
                    false);
                System.out.println("Check runtime connection - COMPLETED");
                if (!db.noErrors()) {
                    System.out.println("Check setup connection....");
                    // try to connect as the setup user
                    db.closeConnection();
                    db.clearErrors();
                    db.setConnection(
                        m_setupBean.getDbDriver(),
                        m_setupBean.getDbCreateConStr(),
                        m_setupBean.getDbConStrParams(),
                        m_setupBean.getDbCreateUser(),
                        m_setupBean.getDbCreatePwd());
                    System.out.println("Check setup connection - COMPLETED");
                } else {
                    dbExists = true;
                }
                if (!db.noErrors()) {
                    throw new DBException("DB connection test failed.", db.getErrors());
                }
            } finally {
                db.clearErrors();
                db.closeConnection();
            }
        }

        System.out.println("DB connection tested successfully.");

        CmsSetupDb db = null;
        if (m_setupBean.isInitialized()) {
            if (createDb || createTables) {
                db = new CmsSetupDb(m_setupBean.getWebAppRfsPath());
                // check if database exists
                if (m_setupBean.getDatabase().startsWith("oracle")
                    || m_setupBean.getDatabase().startsWith("db2")
                    || m_setupBean.getDatabase().startsWith("as400")) {
                    setWorkConnection(db);
                } else {
                    db.setConnection(
                        m_setupBean.getDbDriver(),
                        m_setupBean.getDbWorkConStr(),
                        m_setupBean.getDbConStrParams(),
                        m_setupBean.getDbCreateUser(),
                        m_setupBean.getDbCreatePwd(),
                        false);
                    dbExists = db.noErrors();
                    if (dbExists) {
                        db.closeConnection();
                    } else {
                        db.clearErrors();
                    }
                }
                if (!dbExists || dropDb) {
                    db.closeConnection();
                    if (!m_setupBean.getDatabase().startsWith("db2")
                        && !m_setupBean.getDatabase().startsWith("as400")) {
                        db.setConnection(
                            m_setupBean.getDbDriver(),
                            m_setupBean.getDbCreateConStr(),
                            m_setupBean.getDbConStrParams(),
                            m_setupBean.getDbCreateUser(),
                            m_setupBean.getDbCreatePwd());
                    }
                }
            }
        }
        if (!createDb && !createTables && !dbExists) {
            throw new Exception("You have not created the Alkacon OpenCms database.");
        }
        if (dbExists && createTables && !dropDb && (db != null)) {
            throw new Exception("You have selected to not drop existing DBs, but a DB with the given name exists.");
        }
        if (dbExists && createDb && dropDb && (db != null)) {
            // drop the DB
            db.closeConnection();
            db.setConnection(
                m_setupBean.getDbDriver(),
                m_setupBean.getDbCreateConStr(),
                m_setupBean.getDbConStrParams(),
                m_setupBean.getDbCreateUser(),
                m_setupBean.getDbCreatePwd());
            db.dropDatabase(m_setupBean.getDatabase(), m_setupBean.getReplacer());
            if (!db.noErrors()) {
                List<String> errors = new ArrayList<>(db.getErrors());
                db.clearErrors();
                throw new DBException("Error occurred while dropping the DB!", errors);
            }
            System.out.println("Database dropped successfully.");
        }

        if (createDb && (db != null)) {
            // Create Database
            db.createDatabase(m_setupBean.getDatabase(), m_setupBean.getReplacer());
            if (!db.noErrors()) {
                DBException ex = new DBException("Error occurred while creating the DB!", db.getErrors());
                db.clearErrors();
                throw ex;
            }
            db.closeConnection();
            System.out.println("Database created successfully.");
        }

        if (createTables && (db != null)) {
            setWorkConnection(db);
            //Drop Tables (intentionally quiet)
            db.dropTables(m_setupBean.getDatabase());
            db.clearErrors();
            db.closeConnection();
            // reopen the connection in order to display errors
            setWorkConnection(db);
            //Create Tables
            db.createTables(m_setupBean.getDatabase(), m_setupBean.getReplacer());
            if (!db.noErrors()) {
                DBException ex = new DBException("Error occurred while creating the DB!", db.getErrors());
                db.clearErrors();
                throw ex;
            }
            db.closeConnection();
            System.out.println("Tables created successfully.");
        }
        if (db != null) {
            db.closeConnection();
        }
        System.out.println("Database setup was successful.");
        m_context.stepForward();
    }

    /**
     * Set work connection.
     *
     * @param db the db setup bean
     */
    public void setWorkConnection(CmsSetupDb db) {

        db.setConnection(
            m_setupBean.getDbDriver(),
            m_setupBean.getDbWorkConStr(),
            m_setupBean.getDbConStrParams(),
            m_setupBean.getDbWorkUser(),
            m_setupBean.getDbWorkPwd());
    }

    /**
     * Proceed to next step.
     */
    private void forward() {

        try {
            CmsDbSettingsPanel panel = m_panel[0];
            panel.saveToSetupBean();

            boolean createDb = panel.getCreateDb();
            boolean dropDb = panel.getDropDb();
            boolean createTables = panel.getCreateTables();
            setupDb(createDb, createTables, dropDb);
        } catch (DBException e) {
            CmsSetupErrorDialog.showErrorDialog(e.getMessage(), e.getDetails());
        } catch (Exception e) {
            CmsSetupErrorDialog.showErrorDialog(e);
        }

    }

    /**
    * Switches DB type.
    *
    * @param dbName the database type
    * @param webapp the webapp name
    */
    private void updateDb(String dbName, String webapp) {

        m_mainLayout.removeAllComponents();
        m_setupBean.setDatabase(dbName);
        CmsDbSettingsPanel panel = new CmsDbSettingsPanel(m_setupBean);
        m_panel[0] = panel;
        panel.initFromSetupBean(webapp);
        m_mainLayout.addComponent(panel);
    }

}
