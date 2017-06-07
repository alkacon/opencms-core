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

package org.opencms.workplace.tools.link;

import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog in the administration view, to edit the resources to check the internal links for.<p>
 *
 * @since 6.5.3
 */
public class CmsInternalLinkValidationDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "internallinks";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Auxiliary Property for the VFS resources. */
    private List m_resources;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsInternalLinkValidationDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsInternalLinkValidationDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited project to the db.<p>
     */
    @Override
    public void actionCommit() {

        List errors = new ArrayList();

        try {
            setDialogObject(m_resources);
            // refresh the list
            Map objects = (Map)getSettings().getListObject();
            if (objects != null) {
                objects.remove(CmsInternalLinkValidationList.class.getName());
            }
            // forward to the list
            getToolManager().jspForwardTool(this, "/linkvalidation/internallinks/list", null);
        } catch (Throwable t) {
            errors.add(t);
        }
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * Returns the list of VFS resources.<p>
     *
     * @return the list of VFS resources
     */
    public List getResources() {

        if ((m_resources == null) || m_resources.isEmpty()) {
            m_resources = new ArrayList();
            m_resources.add("/");
        }
        return m_resources;
    }

    /**
     * Sets the resources list.<p>
     *
     * @param value the resources to set
     */
    public void setResources(List value) {

        if (value == null) {
            m_resources = new ArrayList();
            return;
        }
        m_resources = CmsFileUtil.removeRedundancies(value);
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_INTERNALLINK_EDITOR_LABEL_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 0));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        // initialize the project object to use for the dialog
        initSessionObject();

        setKeyPrefix(KEY_PREFIX);

        // widgets to display
        addWidget(new CmsWidgetDialogParameter(
            this,
            "resources",
            "/",
            PAGES[0],
            new CmsVfsFileWidget(false, null),
            1,
            CmsWidgetDialogParameter.MAX_OCCURENCES));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * Initializes the session object to work with depending on the dialog state and request parameters.<p>
     */
    protected void initSessionObject() {

        try {
            if (!CmsStringUtil.isEmpty(getParamAction()) && !CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
                m_resources = (List)getDialogObject();
                // test
                m_resources.size();
            }
        } catch (Exception e) {
            // ignore
        }
        if (m_resources == null) {
            // create a new project object
            m_resources = new ArrayList();
            try {
                Iterator it = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                    getCms(),
                    getCms().getRequestContext().getOuFqn()).iterator();
                while (it.hasNext()) {
                    CmsResource res = (CmsResource)it.next();
                    m_resources.add(getCms().getSitePath(res));
                }
            } catch (CmsException e1) {
                // should never happen
            }
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the project (may be changed because of the widget values)
        setDialogObject(m_resources);
    }
}