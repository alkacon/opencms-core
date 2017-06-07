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

package org.opencms.workplace.list;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsDialog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Generates a CSV file for a given list.<p>
 *
 * @since 6.0.0
 */
public class CmsListCsvExportDialog extends CmsDialog {

    /** List class parameter name constant. */
    public static final String PARAM_LISTCLASS = "listclass";

    /** List class parameter value. */
    private String m_paramListclass;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsListCsvExportDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Generates the CSV file for the given list.<p>
     *
     * @return CSV file
     *
     * @throws ClassNotFoundException if the list dialog class is not found
     */
    public String generateCsv() throws ClassNotFoundException {

        CmsHtmlList list = A_CmsListDialog.getListObject(Class.forName(getParamListclass()), getSettings());
        return list.listCsv();
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsListCsvExportDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the value for the List class parameter.<p>
     *
     * @return the value for the List class parameter
     */
    public String getParamListclass() {

        return m_paramListclass;
    }

    /**
     * Sets the value for the List class parameter.<p>
     *
     * @param listclass the value for the List class parameter to set
     */
    public void setParamListclass(String listclass) {

        m_paramListclass = listclass;
    }

}