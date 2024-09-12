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

package org.opencms.setup.updater.dialogs;

import org.opencms.setup.CmsUpdateBean;
import org.opencms.setup.CmsUpdateUI;
import org.opencms.setup.db.CmsUpdateDBManager;
import org.opencms.setup.ui.CmsSetupErrorDialog;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;

import java.util.Iterator;

import com.vaadin.ui.Panel;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * DB settings dialog.<p>
 */
public class CmsUpdateStep02DBDialog extends A_CmsUpdateDialog {

    /**vaadin serial id. */
    private static final long serialVersionUID = 1L;

    /**vaadin component. */
    VerticalLayout m_contentLayout;

    /**vaadin component. */
    Label m_icon;

    /**DB Manager. */
    CmsUpdateDBManager m_dbBean;

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#init(org.opencms.setup.CmsUpdateUI)
     */
    @Override
    public boolean init(CmsUpdateUI ui) {

        CmsVaadinUtils.readAndLocalizeDesign(this, null, null);
        super.init(ui, true, true);
        setCaption("OpenCms Update-Wizard - Database upgrade");
        CmsUpdateBean bean = ui.getUpdateBean();
        bean.updateDBDriverProperties();
        m_dbBean = new CmsUpdateDBManager();
        try {
            m_dbBean.initialize(bean);
        } catch (Exception e) {
            //
        }

        try {
            bean.setDetectedVersion(m_dbBean.getDetectedVersion());
            if (m_dbBean.needUpdate()) {
                m_icon.setContentMode(ContentMode.HTML);
                m_icon.setValue(FontOpenCms.WARNING.getHtml());
                m_contentLayout.addComponent(getDisplayContent(m_dbBean));
                return true;
            } else {
                ui.displayDialog(new CmsUpdateStep04SettingsDialog());
                return false;
            }
        } catch (NullPointerException en) {
            CmsSetupErrorDialog.showErrorDialog(
                "Database error",
                "Your database version is not compatible with this version of OpenCms.");
            return false;
        }
    }

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#getNextDialog()
     */
    @Override
    A_CmsUpdateDialog getNextDialog() {

        if (!m_dbBean.needUpdate()) {
            return new CmsUpdateStep04SettingsDialog();
        } else {
            return new CmsUpdateStep03DBThreadDialog();
        }
    }

    /**
     * @see org.opencms.setup.updater.dialogs.A_CmsUpdateDialog#getPreviousDialog()
     */
    @Override
    A_CmsUpdateDialog getPreviousDialog() {

        return new CmsUpdateStep01LicenseDialog();
    }

    /**
     * Get panel for given db pool.<p>
     *
     * @param dbBean to get info from
     * @param pool to show panel for
     * @return Panel
     */
    private Panel getDBPoolPanel(CmsUpdateDBManager dbBean, String pool) {

        Panel res = new Panel();
        res.setCaption(pool);
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        String widthString = "300px";
        layout.addComponent(getTableLikeLabel("JDBC Driver", dbBean.getDbDriver(pool), widthString));
        layout.addComponent(getTableLikeLabel("JDBC Connection Url", dbBean.getDbUrl(pool), widthString));
        layout.addComponent(getTableLikeLabel("JDBC Connection Url Params", dbBean.getDbParams(pool), widthString));
        layout.addComponent(getTableLikeLabel("Database User", dbBean.getDbUser(pool), widthString));
        res.setContent(layout);
        return res;
    }

    /**
     * Gets the content.<p>
     *
     * @param dbBean to create content for
     * @return VerticalLayout
     */
    private VerticalLayout getDisplayContent(CmsUpdateDBManager dbBean) {

        VerticalLayout res = new VerticalLayout();
        res.setSpacing(true);
        Label label = new Label();
        label.setContentMode(ContentMode.HTML);
        String html = "";
        html += "<p>Detected Database is: " + dbBean.getDbName() + "</p>";
        html += "<p>Following db pool(s) will be upgraded:</p>";
        label.setValue(html);
        res.addComponent(label);
        Iterator<String> it = dbBean.getPools().iterator();
        while (it.hasNext()) {
            String pool = it.next();
            res.addComponent(getDBPoolPanel(dbBean, pool));
        }

        return res;
    }

    /**
     * Get table like labels.<p>
     *
     * @param key first Col
     * @param value second Col
     * @param keyWidth width of first column
     * @return Label
     */
    private Label getTableLikeLabel(String key, String value, String keyWidth) {

        Label res = new Label();
        res.setContentMode(ContentMode.HTML);
        String html = "<div style='display:flex'><div style='width:"
            + keyWidth
            + "'>"
            + key
            + "</div><div>"
            + value
            + "</div></div>";
        res.setValue(html);
        return res;
    }
}
