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
import org.opencms.widgets.CmsHttpUploadWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to define an extended HTML import in the new Dialog for the current user.<p>
 *
 * If the advanced button is pressed or the validation is false, then the {@link #MODE_ADVANCED} mode
 * of {@link CmsHtmlImportDialog} is shown. <p>
 *
 * The following files use this class:
 * <ul>
 * <li><code>/commons/newresource_uploadHtml.jsp</code> (Contains only a redirect to the uploadhtml.jsp)</li>
 * <li><code>/explorer/uploadhtml/uploadhtml.jsp</code></li>
 * </ul>
 * <p>
 *
 * @see CmsHtmlImportDialog <p>
 *
 */
public class CmsNewResourceExtendedHtmlImport extends CmsHtmlImportDialog {

    /** the action parameter for the advanced button. */
    public static final String ACTION_IMPORT = "dialogimport";

    /** marker of using the advanced button. */
    private boolean m_advanced;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsNewResourceExtendedHtmlImport(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNewResourceExtendedHtmlImport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#dialogButtonsCustom()
     */
    @Override
    public String dialogButtonsCustom() {

        if (m_advanced) {
            return super.dialogButtonsCustom();
        }
        boolean onlyDisplay = true;
        Iterator it = getWidgets().iterator();
        while (it.hasNext()) {
            CmsWidgetDialogParameter wdp = (CmsWidgetDialogParameter)it.next();
            if (!(wdp.getWidget() instanceof CmsDisplayWidget)) {
                onlyDisplay = false;
                break;
            }
        }
        if (!onlyDisplay && !ACTION_IMPORT.equals(getParamAction())) {
            // this is a single page dialog, create common buttons
            return dialogButtons(
                new int[] {BUTTON_OK, BUTTON_CANCEL, BUTTON_ADVANCED},
                new String[] {"", "", " onclick=\"submitAction('" + ACTION_IMPORT + "', form);\""});
        }
        // this is a display only dialog
        return "";
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        if (m_advanced) {
            return super.createDialogHtml(dialog);
        }

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {

            result.append(createWidgetBlockStart(key(Messages.GUI_HTMLIMPORT_BLOCK_LABEL_FOLDER_0)));
            result.append(createDialogRowsHtml(0, 0));
            result.append(createWidgetBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();

    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        m_advanced = !CmsStringUtil.isEmpty(getParamAction());
        if (m_advanced) {
            super.defineWidgets();
        } else {

            initHtmlImportObject();
            setKeyPrefix(KEY_PREFIX);
            addWidget(getDialogParameter("httpDir", new CmsHttpUploadWidget()));
        }
        // set the current directory as the destination directory
        m_htmlimport.setDestinationDir(getSettings().getExplorerResource());
        // it can only be imported with HTTP upload
        m_htmlimport.setInputDir("");

    }

}