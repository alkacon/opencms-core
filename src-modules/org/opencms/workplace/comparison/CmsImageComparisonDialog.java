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

package org.opencms.workplace.comparison;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;

import javax.servlet.jsp.JspWriter;

/**
 * Provides a GUI for displaying two images.<p>
 *
 * @since 6.0.0
 */
public class CmsImageComparisonDialog extends CmsDialog {

    /** Parameter value for the structure id of the first file. */
    private String m_paramId1;

    /** Parameter value for the structure id of the second file. */
    private String m_paramId2;

    /** Parameter value for the version of the first file. */
    private String m_paramVersion1;

    /** Parameter value for the version of the second file. */
    private String m_paramVersion2;

    /**
     * Default constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsImageComparisonDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Performs the dialog actions depending on the initialized action and displays the dialog form.<p>
     *
     * @throws Exception if writing to the JSP out fails
     */
    public void displayDialog() throws Exception {

        if (getAction() == ACTION_CANCEL) {
            actionCloseDialog();
        }
        String link1 = CmsHistoryListUtil.getHistoryLink(getCms(), new CmsUUID(m_paramId1), m_paramVersion1);
        String link2 = CmsHistoryListUtil.getHistoryLink(getCms(), new CmsUUID(m_paramId2), m_paramVersion2);
        JspWriter out = getJsp().getJspContext().getOut();
        out.println(dialogBlockStart(key(Messages.GUI_COMPARE_CONTENT_0)));
        out.println(dialogContentStart(null));
        out.println("<table cellpadding='0' cellspacing='0' border='0' class='maxwidth'><tr align='center'><th>");
        out.println(
            key(
                Messages.GUI_COMPARE_VERSION_1,
                new String[] {CmsHistoryListUtil.getDisplayVersion(m_paramVersion1, getLocale())}));
        out.println("</th><th>");
        out.println(
            key(
                Messages.GUI_COMPARE_VERSION_1,
                new String[] {CmsHistoryListUtil.getDisplayVersion(m_paramVersion2, getLocale())}));
        out.println("</th></tr>");
        out.println("<tr><td>&nbsp;</td><td>&nbsp;</td></tr>");
        out.println("\t<tr align='center'>\n");
        out.println("\t\t<td width='50%'><img src='");
        out.print(getJsp().link(link1));
        out.print("' alt='");
        out.print(
            key(
                Messages.GUI_COMPARE_VERSION_1,
                new String[] {CmsHistoryListUtil.getDisplayVersion(m_paramVersion1, getLocale())}));
        out.print("'/></td>\n");
        out.println("\t\t<td width='50%'><img src='");
        out.print(getJsp().link(link2));
        out.print("' alt='");
        out.print(
            key(
                Messages.GUI_COMPARE_VERSION_1,
                new String[] {CmsHistoryListUtil.getDisplayVersion(m_paramVersion2, getLocale())}));
        out.print("'/></td>\n");
        out.println("\t</tr>");
        out.println("</table>");
        out.println(dialogBlockEnd());
        out.println(dialogContentEnd());
        out.println(dialogEnd());
        out.println(bodyEnd());
        out.println(htmlEnd());
    }

    /**
     * Returns the paramId1.<p>
     *
     * @return the paramId1
     */
    public String getParamId1() {

        return m_paramId1;
    }

    /**
     * Returns the paramId2.<p>
     *
     * @return the paramId2
     */
    public String getParamId2() {

        return m_paramId2;
    }

    /**
     * Returns the paramVersion1.<p>
     *
     * @return the paramVersion1
     */
    public String getParamVersion1() {

        return m_paramVersion1;
    }

    /**
     * Returns the paramVersion2.<p>
     *
     * @return the paramVersion2
     */
    public String getParamVersion2() {

        return m_paramVersion2;
    }

    /**
     * Sets the paramId1.<p>
     *
     * @param paramId1 the paramId1 to set
     */
    public void setParamId1(String paramId1) {

        m_paramId1 = paramId1;
    }

    /**
     * Sets the paramId2.<p>
     *
     * @param paramId2 the paramId2 to set
     */
    public void setParamId2(String paramId2) {

        m_paramId2 = paramId2;
    }

    /**
     * Sets the paramVersion1.<p>
     *
     * @param paramVersion1 the paramVersion1 to set
     */
    public void setParamVersion1(String paramVersion1) {

        m_paramVersion1 = paramVersion1;
    }

    /**
     * Sets the paramVersion2.<p>
     *
     * @param paramVersion2 the paramVersion2 to set
     */
    public void setParamVersion2(String paramVersion2) {

        m_paramVersion2 = paramVersion2;
    }
}