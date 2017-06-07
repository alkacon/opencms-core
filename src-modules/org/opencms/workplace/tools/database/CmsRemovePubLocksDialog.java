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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.database;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to remove old publish locks.<p>
 *
 * @since 7.0.2
 */
public class CmsRemovePubLocksDialog extends CmsWidgetDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "dbpublocks";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The notice text. */
    private String m_notice;

    /** the resources to unlock. */
    private List m_resources;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsRemovePubLocksDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRemovePubLocksDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.tools.modules.CmsModulesEditBase#actionCommit()
     */
    @Override
    public void actionCommit() {

        try {
            // forward to the report page
            Map params = new HashMap();
            params.put(CmsRemovePubLocksReport.PARAM_RESOURCES, CmsStringUtil.collectionAsString(m_resources, ","));
            params.put(PARAM_STYLE, CmsToolDialog.STYLE_NEW);
            getToolManager().jspForwardPage(this, "/system/workplace/admin/database/publishlocksreport.jsp", params);
        } catch (Exception e) {
            addCommitError(e);
        }
    }

    /**
     * Returns the notice text.<p>
     *
     * @return the notice text
     */
    public String getNotice() {

        if (m_notice == null) {
            m_notice = key(Messages.GUI_DB_PUBLOCKS_NOTICE_0);
        }
        return m_notice;
    }

    /**
     * Returns the resources.<p>
     *
     * @return the resources
     */
    public List getResources() {

        return m_resources;
    }

    /**
     * Sets the notice text.<p>
     *
     * @param notice the notice text to set
     */
    public void setNotice(String notice) {

        m_notice = notice;
    }

    /**
     * Sets the resources.<p>
     *
     * @param resources the resources to set
     */
    public void setResources(List resources) {

        m_resources = resources;
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     *
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            result.append(dialogBlockStart(null));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 1));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }
        // close table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        initObject();
        addWidget(new CmsWidgetDialogParameter(this, "notice", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "resources", PAGES[0], new CmsVfsFileWidget()));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes the session object.<p>
     */
    protected void initObject() {

        Object o;

        if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
            // this is the initial dialog call
            o = new ArrayList();
            ((List)o).add("/");
        } else {
            // this is not the initial call, get module from session
            o = getDialogObject();
        }

        if (!(o instanceof List)) {
            m_resources = new ArrayList();
            m_resources.add("/");
        } else {
            // reuse object stored in session
            m_resources = (List)o;
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the module (may be changed because of the widget values)
        setDialogObject(m_resources);
    }
}
