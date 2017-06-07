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

package org.opencms.workplace.commons;

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.tools.CmsToolDialog;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit new or existing user in the administration view.<p>
 *
 * @since 6.0.0
 */
public class CmsResourceInfoDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "resourceinfo";

    /** Defines which pages are valid for this dialog. */
    protected static final String[] PAGES = {"page1"};

    /** The user object that is edited on this dialog. */
    private String m_path;

    /** Stores the value of the request parameter for the user id. */
    private String m_title;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsResourceInfoDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsResourceInfoDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited user to the db.<p>
     */
    @Override
    public void actionCommit() {

        // no saving is done
        setCommitErrors(new ArrayList<Throwable>());
    }

    /**
     * Returns the file.<p>
     *
     * @return the file
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Sets the file.<p>
     *
     * @param file the file to set
     */
    public void setPath(String file) {

        m_path = file;
    }

    /**
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }

    /**
     * @see org.opencms.workplace.tools.CmsToolDialog#computeUpLevelLink()
     */
    @Override
    protected String computeUpLevelLink() {

        // if adminProject is set, go back to the project file list in the administration
        String adminProject = getJsp().getRequest().getParameter(CmsToolDialog.PARAM_ADMIN_PROJECT);
        if ((adminProject != null) && CmsUUID.isValidUUID(adminProject)) {
            String upLevelLink = OpenCms.getLinkManager().substituteLink(
                getCms(),
                "/system/workplace/views/admin/admin-main.jsp")
                + "?path=%2Fprojects%2Ffiles&action=initial&projectid="
                + adminProject;
            return upLevelLink;
        } else {
            return super.computeUpLevelLink();
        }
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     *
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     *
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());
        // create the widgets for the first dialog page
        result.append(dialogBlockStart(key(Messages.GUI_RESOURCE_INFO_0)));
        result.append(createWidgetTableStart());
        result.append(createDialogRowsHtml(0, 1));
        result.append(createWidgetTableEnd());
        result.append(dialogBlockEnd());

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defaultActionHtmlEnd()
     */
    @Override
    protected String defaultActionHtmlEnd() {

        return "";
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        // initialize the user object to use for the dialog
        initFileInfo();

        setKeyPrefix(KEY_PREFIX);

        addWidget(new CmsWidgetDialogParameter(this, "path", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "title", PAGES[0], new CmsDisplayWidget()));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes the user object.<p>
     */
    protected void initFileInfo() {

        try {
            // edit an existing user, get the user object from db
            m_path = getParamResource();
            m_title = getCms().readPropertyObject(m_path, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue("-");
        } catch (CmsException e) {
            // should never happen
        }
    }
}