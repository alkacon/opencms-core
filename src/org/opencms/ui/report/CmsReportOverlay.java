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

package org.opencms.ui.report;

import org.opencms.main.CmsLog;
import org.opencms.report.A_CmsReportThread;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;

import com.google.gwt.event.shared.HandlerRegistration;
import com.vaadin.server.Page;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.JavaScript;

/**
 * Report overlay, shows the wait spinner first and the report window later in case off longer running threads.<p>
 */
public class CmsReportOverlay extends CustomLayout {

    /** The serial version id. */
    private static final long serialVersionUID = 12519444651216053L;

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsReportOverlay.class);

    /** The delay in ms before the report window is shown. */
    private static final int REPORT_VIEW_DELAY = 5000;

    /** The report widget. */
    CmsReportWidget m_report;

    /**
     * Constructor.<p>
     *
     * @param thread the report thread
     */
    public CmsReportOverlay(A_CmsReportThread thread) {
        setId(RandomStringUtils.randomAlphabetic(8));

        try {
            @SuppressWarnings("resource")
            InputStream layoutStream = CmsVaadinUtils.readCustomLayout(getClass(), "reportoverlay.html");
            initTemplateContentsFromInputStream(layoutStream);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        m_report = new CmsReportWidget(thread);
        m_report.setWidth("100%");
        m_report.setHeight((Page.getCurrent().getBrowserWindowHeight() - 200) + "px");
        CmsBasicDialog dialogContent = new CmsBasicDialog();
        dialogContent.setContent(m_report);
        addComponent(dialogContent, "content");

        m_report.addReportFinishedHandler(new Runnable() {

            public void run() {

                setVisible(false);
            }
        });
        // add a timeout to show the report window
        JavaScript.eval(
            "setTimeout(function(){ "
                + "var el= document.getElementById('"
                + getId()
                + "'); if (el!=null) el.classList.add('o-report-show'); },"
                + REPORT_VIEW_DELAY
                + ")");
    }

    /**
     * Adds an action that should be executed if the report is finished.<p>
     *
     * Note that this action will only be called if the report is finished while the report widget is actually
     * displayed. For example, if the user closes the browser window before the report is finished, this will not be executed.
     *
     * @param handler the handler
     * @return the handler registration
     */
    public HandlerRegistration addReportFinishedHandler(final Runnable handler) {

        return m_report.addReportFinishedHandler(handler);
    }

    /**
     * Sets the window title.<p>
     *
     * @param title the new window title
     */
    public void setTitle(String title) {

        /* HACK: Using a Label destroys the layout for some reason, so we resort to setting the caption directly in the
         element via an explicit JavaScript call. */
        JavaScript.eval(
            "document.querySelector('#"
                + getId()
                + " .fakewindowheader').innerHTML = '"
                + StringEscapeUtils.escapeJavaScript(title)
                + "'");
    }
}
