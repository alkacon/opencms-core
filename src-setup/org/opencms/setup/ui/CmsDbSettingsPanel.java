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
import org.opencms.ui.CmsVaadinUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Form for database settings.
 */
public class CmsDbSettingsPanel extends VerticalLayout {

    /** Form field. */
    private CheckBox m_createDb;

    /** Form field. */
    private CheckBox m_createTables;

    /** Form field. */
    private TextField m_dbCreateConStr;

    /** Form field. */
    private PasswordField m_dbCreatePwd;

    /** Form field. */
    private TextField m_dbCreateUser;

    /** Form field. */
    private TextField m_dbName;

    /** Form field. */
    private PasswordField m_dbWorkPwd;

    /** Form field. */
    private TextField m_dbWorkUser;

    /** Form field. */
    private TextField m_defaultTablespace;

    /** Form field. */
    private CheckBox m_dropDatabase;

    /** Form field. */
    private TextField m_indexTablespace;

    /** Setup bean. */
    private CmsSetupBean m_setupBean;

    /** Form field. */
    private Panel m_setupPanel;

    private TextField m_templateDb;

    /** Form field. */
    private TextField m_temporaryTablespace;

    public CmsDbSettingsPanel(CmsSetupBean setupBean) {

        super();
        m_setupBean = setupBean;
        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);

    }

    /**
     * Gets the value of the 'create database' check box.
     *
     * @return the value of the check box
     */
    public boolean getCreateDb() {

        return m_createDb.getValue().booleanValue();
    }

    /**
     * Gets the value of the 'create tables' check box.
     *
     * @return the value of the check box
     */
    public boolean getCreateTables() {

        return m_createTables.isVisible() ? m_createTables.getValue() : m_createDb.getValue();
    }

    /**
     * Gets the value of the 'drop database' check box.
     *
     * @return the value of the check box
     */
    public boolean getDropDb() {

        return m_dropDatabase.getValue().booleanValue();
    }

    /**
     * Initializes fields with data from setup bean.
     *
     * @param the webapp name (null for root webapp)
     */
    public void initFromSetupBean(String webapp) {

        String db = m_setupBean.getDatabase();
        CmsSetupBean bean = m_setupBean;
        Map<String, String[]> params = new HashMap<>();
        m_setupPanel.setVisible(true);
        switch (db) {
            case "mysql":
            case "mssql":
                setVisible(
                    m_dbCreateUser,
                    m_dbCreatePwd,
                    m_dbWorkUser,
                    m_dbWorkPwd,
                    m_dbCreateConStr,
                    m_dbName,
                    m_createDb,
                    m_dropDatabase);
                m_dbCreateUser.setValue(bean.getDbCreateUser());
                m_dbCreatePwd.setValue(bean.getDbCreatePwd());
                m_dbWorkUser.setValue(bean.getDbWorkUser());
                m_dbWorkPwd.setValue(bean.getDbWorkPwd());
                m_dbCreateConStr.setValue(bean.getDbCreateConStr());
                m_dbName.setValue(webapp != null ? webapp : bean.getDb());
                m_createDb.setValue(true);
                m_createDb.setCaption("Create database and tables");
                m_dropDatabase.setValue(false);
                break;
            case "postgresql":
                setVisible(
                    m_dbCreateUser,
                    m_dbCreatePwd,
                    m_dbWorkUser,
                    m_dbWorkPwd,
                    m_dbCreateConStr,
                    m_dbName,
                    m_createDb,
                    m_createTables,
                    m_dropDatabase,
                    m_templateDb);
                m_dbCreateUser.setValue(bean.getDbCreateUser());
                m_dbCreatePwd.setValue(bean.getDbCreatePwd());
                m_dbWorkUser.setValue(bean.getDbWorkUser());
                m_dbWorkPwd.setValue(bean.getDbWorkPwd());
                m_dbCreateConStr.setValue(bean.getDbCreateConStr());
                m_dbName.setValue(webapp != null ? webapp : bean.getDb());
                m_createDb.setValue(true);
                m_createDb.setCaption("Create database and user");
                m_templateDb.setValue(dbProp("templateDb"));
                m_createTables.setValue(true);
                m_dropDatabase.setValue(false);
                break;
            case "hsqldb":
                setVisible(
                    m_dbCreateUser,
                    m_dbCreatePwd,
                    m_dbWorkUser,
                    m_dbWorkPwd,
                    m_dbCreateConStr,
                    m_createDb,
                    m_dropDatabase);

                m_dbCreateUser.setValue(bean.getDbCreateUser());
                m_dbCreatePwd.setValue(bean.getDbCreatePwd());
                m_dbWorkUser.setValue(bean.getDbWorkUser());
                m_dbWorkPwd.setValue(bean.getDbWorkPwd());
                m_createDb.setValue(true);
                m_createDb.setCaption("Create database and tables");
                m_dropDatabase.setValue(false);
                String origCreateConStr = bean.getDbProperty("hsqldb.constr");
                String createConStr = bean.getDbCreateConStr();
                if ((origCreateConStr != null) && origCreateConStr.equals(createConStr)) {
                    createConStr = "jdbc:hsqldb:file:"
                        + bean.getWebAppRfsPath()
                        + "WEB-INF"
                        + File.separatorChar
                        + "hsqldb"
                        + File.separatorChar
                        + "opencms;shutdown=false";
                }
                m_dbCreateConStr.setValue(createConStr);
                break;
            case "oracle":
                setVisible(
                    m_dbCreateUser,
                    m_dbCreatePwd,
                    m_dbWorkUser,
                    m_dbWorkPwd,
                    m_dbCreateConStr,
                    m_createDb,
                    m_createTables,
                    m_dropDatabase,
                    m_temporaryTablespace,
                    m_indexTablespace,
                    m_defaultTablespace);
                m_dbCreateUser.setValue(bean.getDbCreateUser());
                m_dbCreatePwd.setValue(bean.getDbCreatePwd());
                m_dbWorkUser.setValue(bean.getDbWorkUser());
                m_dbWorkPwd.setValue(bean.getDbWorkPwd());
                m_dbCreateConStr.setValue(bean.getDbCreateConStr());
                m_dbName.setValue(webapp != null ? webapp : bean.getDb());
                m_createDb.setValue(true);
                m_createDb.setCaption("Create user");
                m_createTables.setValue(true);
                m_dropDatabase.setValue(false);
                m_temporaryTablespace.setValue(dbProp("temporaryTablespace"));
                m_indexTablespace.setValue(dbProp("indexTablespace"));
                m_defaultTablespace.setValue(dbProp("defaultTablespace"));
                break;
            case "db2":
            case "as400":
                setVisible(m_dbWorkUser, m_dbWorkPwd, m_dbCreateConStr, m_dbName, m_createTables);
                m_setupPanel.setVisible(false);
                m_dbWorkUser.setValue(bean.getDbWorkUser());
                m_dbWorkPwd.setValue(bean.getDbWorkPwd());
                m_dbCreateConStr.setValue(bean.getDbCreateConStr());
                m_dbName.setValue(webapp != null ? webapp : bean.getDb());
                m_createDb.setValue(false);
                m_createTables.setValue(true);
                m_dropDatabase.setValue(true);
                break;
            default:
                break;
        }
    }

    /**
     * Saves form field data to setup bean.
     */
    public void saveToSetupBean() {

        Map<String, String[]> result = new HashMap<String, String[]>();

        CmsSetupBean bean = m_setupBean;
        if (m_dbCreateUser.isVisible()) {
            bean.setDbCreateUser(m_dbCreateUser.getValue());
        }
        if (m_dbCreatePwd.isVisible()) {
            bean.setDbCreatePwd(m_dbCreatePwd.getValue());
        }
        if (m_dbWorkUser.isVisible()) {
            bean.setDbWorkUser(m_dbWorkUser.getValue());
        }

        if (m_templateDb.isVisible()) {
            setDbProp("templateDb", m_templateDb.getValue());
        }

        if (m_dbWorkPwd.isVisible()) {
            bean.setDbWorkPwd(m_dbWorkPwd.getValue());
        }

        if (m_dbCreateConStr.isVisible()) {
            bean.setDbCreateConStr(m_dbCreateConStr.getValue());
        }

        if (m_dbName.isVisible()) {
            bean.setDb(m_dbName.getValue());
        }

        result.put("dbCreateConStr", new String[] {m_dbCreateConStr.getValue()});

        result.put("dbName", new String[] {bean.getDatabase()});
        result.put("dbProduct", new String[] {bean.getDatabase()});
        result.put("dbProvider", new String[] {"sql"});
        result.put("dbName", new String[] {m_dbName.getValue()});
        result.put("db", new String[] {bean.getDb()});
        result.put("createDb", new String[] {Boolean.toString(m_createDb.getValue().booleanValue())});
        result.put("createTables", new String[] {Boolean.toString(m_createTables.getValue().booleanValue())});
        result.put("jdbcDriver", new String[] {dbProp("driver")});
        result.put("templateDb", new String[] {dbProp("templateDb")});
        result.put("dbCreateUser", new String[] {bean.getDbCreateUser()});
        result.put("dbCreatePwd", new String[] {bean.getDbCreatePwd()});
        result.put("dbWorkUser", new String[] {bean.getDbWorkUser()});
        result.put("dbWorkPwd", new String[] {bean.getDbWorkPwd()});
        result.put("dbDefaultTablespace", new String[] {m_defaultTablespace.getValue()});
        result.put("dbTemporaryTablespace", new String[] {m_temporaryTablespace.getValue()});
        result.put("dbIndexTablespace", new String[] {m_indexTablespace.getValue()});
        // result.put("servletMapping", new String[] {getServeltMapping()});
        result.put("submit", new String[] {Boolean.TRUE.toString()});
        bean.setDbParamaters(result, bean.getDatabase(), null, null);

    }

    /**
     * Accesses a property from the DB configuration for the selected DB.
     *
     * @param name the name of the property
     * @return the value of the property
     */
    private String dbProp(String name) {

        String dbType = m_setupBean.getDatabase();
        Object prop = m_setupBean.getDatabaseProperties().get(dbType).get(dbType + "." + name);
        if (prop == null) {
            return "";
        }
        return prop.toString();
    }

    private void setDbProp(String key, String value) {

        String db = m_setupBean.getDatabase();
        m_setupBean.setDbProperty(db + "." + key, value);
    }

    /**
     * Sets the visibility of the given components to 'true' .
     *
     * @param components the components to show
     */
    private void setVisible(Component... components) {

        for (Component component : components) {
            component.setVisible(true);
        }
    }

}
