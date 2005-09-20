/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/searchindex/CmsSearchIndexSourceAssignResourcesWidget.java,v $
 * Date   : $Date: 2005/09/20 15:39:06 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.searchindex;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialogParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * 
 * Widget dialog to add or remove resources (folders) to / from the request parameter 
 * given <code>{@link org.opencms.search.CmsSearchIndexSource}</code> ("indexsource").<p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSearchIndexSourceAssignResourcesWidget extends A_CmsEditIndexSourceDialog {

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSearchIndexSourceAssignResourcesWidget(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSearchIndexSourceAssignResourcesWidget(
        PageContext context,
        HttpServletRequest req,
        HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     * 
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     * 
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // 1st block: indexsource information
            result.append(dialogBlockStart(key(Messages.GUI_LABEL_INDEXSOURCE_BLOCK_SETTINGS_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 1));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());

            // 2nd block: configureable resources for indexsource
            result.append(dialogBlockStart(key(Messages.GUI_LIST_SEARCHRESOURCES_NAME_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(2, 2));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    protected void defineWidgets() {

        super.defineWidgets();

        // widgets to display
        // indexsource data (first block)
        addWidget(new CmsWidgetDialogParameter(m_indexsource, "name", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_indexsource, "indexerClassName", PAGES[0], new CmsDisplayWidget()));

        // resources block
        addWidget(new CmsWidgetDialogParameter(
            m_indexsource.getResourcesNames(),
            "resourcesNames",
            "/",
            PAGES[0],
            new CmsVfsFileWidget(false, ""),
            1,
            10));
    }
}