/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/comparison/CmsPointerComparisonDialog.java,v $
 * Date   : $Date: 2011/03/23 14:51:39 $
 * Version: $Revision: 1.9 $
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
 * All rights reserved.
 * 
 * This source code is the intellectual property of Alkacon Software GmbH.
 * It is PROPRIETARY and CONFIDENTIAL.
 * Use of this source code is subject to license terms.
 *
 * In order to use this source code, you need written permission from 
 * Alkacon Software GmbH. Redistribution of this source code, in modified 
 * or unmodified form, is not allowed unless written permission by 
 * Alkacon Software GmbH has been given.
 *
 * ALKACON SOFTWARE GMBH MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THIS SOURCE CODE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. ALKACON SOFTWARE GMBH SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOURCE CODE OR ITS DERIVATIVES.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 */

package org.opencms.workplace.comparison;

import org.opencms.file.CmsFile;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.commons.CmsHistoryList;

import javax.servlet.jsp.JspWriter;

/**
 * Provides a GUI for displaying two images.<p> 
 *
 * @author Jan Baudisch
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 6.0.0 
 */
public class CmsPointerComparisonDialog extends CmsDialog {

    /** Parameter value for the structure Id of the first file. */
    private String m_paramId1;

    /** Parameter value for the structure Id of the second file. */
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
    public CmsPointerComparisonDialog(CmsJspActionElement jsp) {

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
        CmsFile resource1 = CmsResourceComparisonDialog.readFile(
            getCms(),
            new CmsUUID(getParamId1()),
            getParamVersion1());
        CmsFile resource2 = CmsResourceComparisonDialog.readFile(
            getCms(),
            new CmsUUID(getParamId2()),
            getParamVersion2());
        String linkTarget1 = new String(resource1.getContents());
        String linkTarget2 = new String(resource2.getContents());
        JspWriter out = getJsp().getJspContext().getOut();
        out.println(dialogBlockStart(key(Messages.GUI_COMPARE_CONTENT_0)));
        out.println(dialogContentStart(null));
        out.println("<table cellpadding='0' cellspacing='0' border='0' class='maxwidth'><tr align='center'><th>");
        out.println(key(Messages.GUI_COMPARE_VERSION_1, new String[] {CmsHistoryList.getDisplayVersion(
            m_paramVersion1,
            getLocale())}));
        out.println("</th><th>");
        out.println(key(Messages.GUI_COMPARE_VERSION_1, new String[] {CmsHistoryList.getDisplayVersion(
            m_paramVersion2,
            getLocale())}));
        out.println("</th></tr>");
        out.print("<tr><td><a href=\"");
        out.print(linkTarget1);
        out.print("\">");
        out.print(linkTarget1);
        out.print("</a>");
        out.print("</td><td width='50%'>\n");
        out.print("<a href=\"");
        out.print(linkTarget2);
        out.print("\">");
        out.print(linkTarget2);
        out.print("</a>");
        out.println("</td></td></table>");
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