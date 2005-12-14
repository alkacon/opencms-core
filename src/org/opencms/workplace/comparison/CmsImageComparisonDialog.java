/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/comparison/CmsImageComparisonDialog.java,v $
 * Date   : $Date: 2005/12/14 09:52:45 $
 * Version: $Revision: 1.1.2.1 $
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.commons.CmsHistoryList;

import javax.servlet.jsp.JspWriter;

/**
 * Provides a GUI for displaying two images.<p> 
 *
 * @author Jan Baudisch
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsImageComparisonDialog extends CmsDialog {

    private String m_paramPath1;

    private String m_paramPath2;

    private String m_paramTagId1;

    private String m_paramTagId2;

    private String m_paramVersion1;

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
        String link1 = "";
        String link2 = "";
        if ("-1".equals(m_paramTagId1)) {
            // offline version
            link1 = getParamResource();
        } else {
            link1 = CmsHistoryList.getBackupLink(m_paramPath1, m_paramTagId1);
        }
        if ("-1".equals(m_paramTagId2)) {
            // offline version
            link2 = getParamResource();
        } else {
            link2 = CmsHistoryList.getBackupLink(m_paramPath2, m_paramTagId2);
        }
        JspWriter out = getJsp().getJspContext().getOut();
        out.println(dialogBlockStart(key(Messages.GUI_COMPARE_CONTENT_0)));
        out.println(dialogContentStart(null));
        out.println("<table>\n");
        out.println("\t<tr>\n");
        out.println("\t\t<td><img src='");
        out.print(getJsp().link(link1));
        out.print("' alt='");
        out.print(key(Messages.GUI_COMPARE_VERSION_1, new String[] {m_paramVersion1}));
        out.print("'/></td>\n");
        out.println("\t\t<td><img src='");
        out.print(getJsp().link(link2));
        out.print("' alt='");
        out.print(key(Messages.GUI_COMPARE_VERSION_1, new String[] {m_paramVersion2}));
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
     * Returns the paramPath1.<p>
     *
     * @return the paramPath1
     */
    public String getParamPath1() {

        return m_paramPath1;
    }

    /**
     * Returns the paramPath2.<p>
     *
     * @return the paramPath2
     */
    public String getParamPath2() {

        return m_paramPath2;
    }

    /**
     * Returns the paramTagId1.<p>
     *
     * @return the paramTagId1
     */
    public String getParamTagId1() {

        return m_paramTagId1;
    }

    /**
     * Returns the paramTagId2.<p>
     *
     * @return the paramTagId2
     */
    public String getParamTagId2() {

        return m_paramTagId2;
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
     * Sets the paramPath1.<p>
     *
     * @param paramPath1 the paramPath1 to set
     */
    public void setParamPath1(String paramPath1) {

        m_paramPath1 = paramPath1;
    }

    /**
     * Sets the paramPath2.<p>
     *
     * @param paramPath2 the paramPath2 to set
     */
    public void setParamPath2(String paramPath2) {

        m_paramPath2 = paramPath2;
    }

    /**
     * Sets the paramTagId1.<p>
     *
     * @param paramTagId1 the paramTagId1 to set
     */
    public void setParamTagId1(String paramTagId1) {

        m_paramTagId1 = paramTagId1;
    }

    /**
     * Sets the paramTagId2.<p>
     *
     * @param paramTagId2 the paramTagId2 to set
     */
    public void setParamTagId2(String paramTagId2) {

        m_paramTagId2 = paramTagId2;
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