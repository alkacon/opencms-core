/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/searchindex/CmsDeleteIndexSourceDialog.java,v $
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
import org.opencms.workplace.tools.CmsToolManager;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * A <code>CmsWidgetDialog</code> that starts a (confirmed) delete dialog for 
 * an indexsource.<p> 
 * 
 * The constraint for allowing deletion of the indexsource: It must not be referenced by 
 * any searchindex.<p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.0.0
 */
public class CmsDeleteIndexSourceDialog extends A_CmsEditIndexSourceDialog {

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */

    public CmsDeleteIndexSourceDialog(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */

    public CmsDeleteIndexSourceDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);

    }

    /**
     * Commits the edited search index to the search manager.<p>
     */
    public void actionCommit() {

        List errors = new ArrayList();

        try {

            m_searchManager.removeSearchIndexSource(m_indexsource);
            clearDialogObject();
            writeConfiguration();
            // if we go back to /searchindex/singleindex (overview) the deleted searchindex is not 
            // there any more in the CmsSearchManager and CmsOverviewSearchIndex.getUserObject will 
            // find null -> defineWidgets will provide null as bean...
            setParamCloseLink(CmsToolManager.linkForToolPath(getJsp(), "/searchindex/indexsources"));

        } catch (Throwable t) {
            errors.add(t);
        }
        // set the list of errors to display when saving failed
        setCommitErrors(errors);
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

        StringBuffer result = new StringBuffer(512);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_LIST_INDEXSOURCE_ACTION_DELETE_NAME_0)));
            result.append(createWidgetTableStart());
            result.append(key(
                Messages.GUI_LIST_INDEXSOURCE_ACTION_DELETE_CONF_1,
                new Object[] {m_indexsource.getName()}));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());

        // See CmsWidgetDialog.dialogButtonsCustom(): if no widgets are defined that are non-display-only widgets, 
        // no dialog buttons (Ok, Cancel) will be visible....
        result.append(dialogButtons(new int[] {BUTTON_OK, BUTTON_CANCEL}, new String[2]));
        return result.toString();
    }

}
