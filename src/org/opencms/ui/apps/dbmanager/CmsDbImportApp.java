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

package org.opencms.ui.apps.dbmanager;

import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsAttributeAwareApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicReportPage;
import org.opencms.util.CmsStringUtil;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.vaadin.ui.Component;

/**
 * Class for the database import app.<p>
 */
public class CmsDbImportApp extends A_CmsAttributeAwareApp implements I_CmsReportApp {

    /**
     * Enumeration to distinguist between http- and server import.
     */
    protected enum Mode {
        /**Import per HTTP.*/
        HTTP,
        /**Import from Server.*/
        SERVER;
    }

    /**Path to the report from import(HTTP). */
    public static final String PATH_REPORT_HTTP = "reportHTTP";

    /**Path to the report from import(server).*/
    public static final String PATH_REPORT_SERVER = "reportserver";

    /**Map to store labels with theire threads for showing the reports.*/
    private IdentityHashMap<A_CmsReportThread, String> m_labels = new IdentityHashMap<A_CmsReportThread, String>();

    /**Map to link threads to the current states. */
    private Map<String, A_CmsReportThread> m_reports = Maps.newHashMap();

    /**Import mode.*/
    private Mode m_mode;

    /**
     * Public constructor.<p>
     *
     * @param mode for import
     */
    public CmsDbImportApp(Mode mode) {
        m_mode = mode;
    }

    /**
     * @see org.opencms.ui.apps.dbmanager.I_CmsReportApp#goToMainView()
     */
    public void goToMainView() {

        openSubView("", true);
    }

    /**
     * @see org.opencms.ui.apps.dbmanager.I_CmsReportApp#openReport(java.lang.String, org.opencms.report.A_CmsReportThread, java.lang.String)
     */
    public void openReport(String path, A_CmsReportThread thread, String title) {

        m_reports.put(path, thread);
        m_labels.put(thread, title);
        openSubView(path, true);
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)
            | state.startsWith(PATH_REPORT_HTTP)
            | state.startsWith(PATH_REPORT_SERVER)) {
            if (m_mode.equals(Mode.HTTP)) {
                crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_IMPORTHTTP_ADMIN_TOOL_NAME_0));
            } else {
                crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_IMPORTSERVER_ADMIN_TOOL_NAME_0));
            }
            return crumbs;
        }
        return new LinkedHashMap<String, String>();
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(state)) {
            if (m_mode.equals(Mode.HTTP)) {
                return new CmsDbImportHTTP(this);
            } else {
                return new CmsDbImportServer(this);
            }
        }
        if (state.startsWith(PATH_REPORT_SERVER) | state.startsWith(PATH_REPORT_HTTP)) {
            CmsBasicReportPage reportForm = new CmsBasicReportPage(
                m_labels.get(m_reports.get(state)),
                m_reports.get(state),
                new Runnable() {

                    public void run() {

                        openSubView("", true);
                    }
                });
            reportForm.setHeight("100%");
            return reportForm;
        }
        return null;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }
}
