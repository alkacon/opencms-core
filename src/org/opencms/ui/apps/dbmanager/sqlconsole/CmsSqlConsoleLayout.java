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

package org.opencms.ui.apps.dbmanager.sqlconsole;

import org.opencms.db.CmsDbPoolV11;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsStringBufferReport;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsErrorDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Widget for the SQL console.<p>
 */
public class CmsSqlConsoleLayout extends VerticalLayout {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The button to execute the SQL. */
    protected Button m_ok;

    /** Combo box for selecting the pool. */
    protected ComboBox<String> m_pool;

    /** Text area containing the SQL statements. */
    protected TextArea m_script;

    /** The executor. */
    private CmsSqlConsoleExecutor m_console;

    /**
     * Creates a new instance.<p>
     *
     * @param console the SQL executor
     */
    public CmsSqlConsoleLayout(CmsSqlConsoleExecutor console) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_console = console;
        m_ok.addClickListener(evt -> runQuery());
        m_pool.setItems(OpenCms.getDbPoolNames());
        m_pool.setValue(CmsDbPoolV11.getDefaultDbPoolName());
        m_pool.setEmptySelectionAllowed(false);
    }

    /**
     * Runs the currently entered query and displays the results.
     */
    protected void runQuery() {

        String pool = m_pool.getValue();
        String stmt = m_script.getValue();
        if (stmt.trim().isEmpty()) {
            return;
        }
        CmsStringBufferReport report = new CmsStringBufferReport(Locale.ENGLISH);
        List<Throwable> errors = new ArrayList<>();
        CmsSqlConsoleResults result = m_console.execute(stmt, pool, report, errors);
        if (errors.size() > 0) {
            CmsErrorDialog.showErrorDialog(report.toString() +  errors.get(0).getMessage(), errors.get(0));
        } else {
            Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SQLCONSOLE_QUERY_RESULTS_0));
            window.setContent(new CmsSqlConsoleResultsForm(result, report.toString()));
            A_CmsUI.get().addWindow(window);
            window.center();
        }

    }

}
