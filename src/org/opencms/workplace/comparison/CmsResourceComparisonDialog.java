/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/comparison/CmsResourceComparisonDialog.java,v $
 * Date   : $Date: 2005/12/02 16:22:41 $
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

package org.opencms.workplace.comparison;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.list.CmsMultiListDialog;
import org.opencms.workplace.list.CmsThreeListsDialog;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Helper class for managing three lists on the same dialog.<p>
 * 
 * @author Jan Baudisch
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsResourceComparisonDialog {

    
    private CmsJspActionElement m_jsp;

    /**
     * Creates a new resource comparison dialog.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsResourceComparisonDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        m_jsp = new CmsJspActionElement(context, req, res);

    }

    /**
     * Display method for two list dialogs.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void displayDialog() throws Exception {

        CmsFileInfoDialog fileInfo = new CmsFileInfoDialog(m_jsp) {
            protected String defaultActionHtmlEnd() {
                return "";
            }
        };
        if (fileInfo.isForwarded()) {
            return;
        } 
        fileInfo.displayDialog(true);
        fileInfo.writeDialog();
        // TODO: own parameter
        boolean plainTextComparison = m_jsp.getRequest().getParameter("resource").endsWith(".txt");

        CmsPropertyComparisonList propertyDiff = new CmsPropertyComparisonList(m_jsp);
        CmsAttributeComparisonList attributeDiff = new CmsAttributeComparisonList(m_jsp);
        
        if (plainTextComparison) {
            List lists = new ArrayList();
            lists.add(propertyDiff);
            lists.add(attributeDiff);
            CmsMultiListDialog twoLists = new CmsMultiListDialog(lists) {
                public String defaultActionHtmlEnd() {
                    return "";
                }
            };
            twoLists.displayDialog();
            CmsPlainTextDifferenceDialog contentDiff = new CmsPlainTextDifferenceDialog(m_jsp);
            contentDiff.displayDialog();
            
        } else {

            // initialize list dialogs
            CmsElementComparisonList contentDiff = new CmsElementComparisonList(m_jsp);
            CmsThreeListsDialog wpThreeLists = new CmsThreeListsDialog(attributeDiff, contentDiff, propertyDiff) {
                protected String defaultActionHtmlStart() {
                    return getActiveWp().getList().listJs(getActiveWp().getLocale())
                        + getActiveWp().dialogContentStart(getActiveWp().getParamTitle());
                }
            };
            // perform the active list actions
            wpThreeLists.displayDialog(true);
            // write the content of list dialog
            wpThreeLists.writeDialog();
        }

    }
}
