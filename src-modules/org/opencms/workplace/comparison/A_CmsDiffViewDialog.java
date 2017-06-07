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

import com.alkacon.diff.Diff;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * Provides a GUI for the configuration file comparison dialog.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsDiffViewDialog extends CmsDialog {

    /** Diff mode. */
    private CmsDiffViewMode m_mode;

    /**
     * Default constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    protected A_CmsDiffViewDialog(CmsJspActionElement jsp) {

        super(jsp);
        setParamStyle(STYLE_NEW);
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
        JspWriter out = getJsp().getJspContext().getOut();
        out.print("<link rel='stylesheet' type='text/css' href='");
        out.print(getStyleUri(getJsp()));
        out.println("diff.css'>");
        out.println(dialogContentStart(getParamTitle()));
        out.print("<form name='diff-form' method='post' action='");
        out.print(getDialogUri());
        out.println("'>");
        out.println(allParamsAsHidden());
        out.println("</form>");
        // icon is displayed on the right in order that the user needs not scroll to the icon for long lines
        out.println("<p>");
        out.println(getDiffOnlyButtonsHtml());
        out.println("</p>");
        out.println(dialogBlockStart(null));
        out.println("<table cellspacing='0' cellpadding='0' class='xmlTable'>\n<tr><td><pre style='overflow:auto'>");
        try {
            CmsHtmlDifferenceConfiguration conf = new CmsHtmlDifferenceConfiguration(
                getMode() == CmsDiffViewMode.ALL ? -1 : getLinesBeforeSkip(),
                getLocale());
            String diff = Diff.diffAsHtml(getOriginalSource(), getCopySource(), conf);
            if (CmsStringUtil.isNotEmpty(diff)) {
                out.println(diff);
            } else {
                // print original source, if there are no differences
                out.println(
                    wrapLinesWithUnchangedStyle(
                        CmsStringUtil.substitute(CmsStringUtil.escapeHtml(getOriginalSource()), "<br/>", "")));
            }
        } catch (Exception e) {
            out.print(e);
        }
        out.println("</pre></td></tr>\n</table>");
        out.println(dialogBlockEnd());
        out.println(dialogContentEnd());
        out.println(dialogEnd());
        out.println(bodyEnd());
        out.println(htmlEnd());
    }

    /**
     * Returns the html code for the buttons 'show only differences' and 'show everything'.<p>
     *
     * @return the html code for the buttons 'show only differences' and 'show everything'
     */
    String getDiffOnlyButtonsHtml() {

        StringBuffer result = new StringBuffer();
        if (!getOriginalSource().equals(getCopySource())) {
            String onClick1 = "javascript:document.forms['diff-form'].mode.value = '";
            String onClick2 = "javascript:document.forms['diff-form'].mode.value = '";
            onClick1 += CmsDiffViewMode.ALL;
            onClick2 += CmsDiffViewMode.DIFF_ONLY;
            onClick1 += "'; document.forms['diff-form'].submit();";
            onClick2 += "'; document.forms['diff-form'].submit();";
            result.append(
                getTwoButtonsHtml(
                    CmsDiffViewMode.DIFF_ONLY.getName().key(getLocale()),
                    CmsDiffViewMode.ALL.getName().key(getLocale()),
                    onClick1,
                    onClick2,
                    getMode() == CmsDiffViewMode.DIFF_ONLY));
        } else {
            // display all text, if there are no differences
            setMode(CmsDiffViewMode.ALL);
        }
        return result.toString();
    }

    /**
     * Returns the html for two buttons, whereby the third parameter determines which one is active.<p>
     *
     * @param label1 the label for the first button
     * @param label2 the label for the second button
     * @param firstActive a flag indicating wheter the first or second button is active
     * @param onClick1 the action to be performed if the first button is clicked
     * @param onClick2 the action to be performed if the second button is clicked
     *
     * @return the html for two buttons, whereby the third parameter determines which one is active
     */
    public String getTwoButtonsHtml(
        String label1,
        String label2,
        String onClick1,
        String onClick2,
        boolean firstActive) {

        StringBuffer result = new StringBuffer();
        if (firstActive) {
            result.append(
                A_CmsHtmlIconButton.defaultButtonHtml(
                    CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
                    "id",
                    label1,
                    null,
                    true,
                    A_CmsListDialog.ICON_DETAILS_SHOW,
                    null,
                    onClick1));
            result.append("&nbsp;&nbsp;");
            result.append(deactivatedEmphasizedButtonHtml(label2, A_CmsListDialog.ICON_DETAILS_HIDE));
        } else {

            result.append(deactivatedEmphasizedButtonHtml(label1, A_CmsListDialog.ICON_DETAILS_HIDE));
            result.append("&nbsp;&nbsp;");
            result.append(
                A_CmsHtmlIconButton.defaultButtonHtml(
                    CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
                    "id",
                    label2,
                    null,
                    true,
                    A_CmsListDialog.ICON_DETAILS_SHOW,
                    null,
                    onClick2));
        }
        result.append("&nbsp;&nbsp;");
        return result.toString();
    }

    /**
     * Returns the html code for a deactivated empfasized button.<p>
     *
     * @param name the label of the button
     * @param iconPath the path to the icon
     *
     * @return the html code for a deactivated empfasized button
     */
    public String deactivatedEmphasizedButtonHtml(String name, String iconPath) {

        StringBuffer result = new StringBuffer();
        result.append(
            "<span style='vertical-align:middle;'><img style='width:20px;height:20px;display:inline;vertical-align:middle;text-decoration:none;' src=\'");
        result.append(CmsWorkplace.getSkinUri());
        result.append(iconPath);
        result.append("\' alt=\'");
        result.append(name);
        result.append("\' title=\'");
        result.append(name);
        result.append("\'>&nbsp;<b>");
        result.append(name);
        result.append("</b></span>");
        return result.toString();
    }

    /**
     * Returns the mode.<p>
     *
     * @return the mode
     */
    public CmsDiffViewMode getMode() {

        return m_mode;
    }

    /**
     * Returns the parameter value for the Mode.<p>
     *
     * @return the parameter value for the Mode
     */
    public String getParamMode() {

        if (m_mode == null) {
            return null;
        }
        return m_mode.getMode();
    }

    /**
     * Sets the mode.<p>
     *
     * @param mode the mode to set
     */
    public void setMode(CmsDiffViewMode mode) {

        m_mode = mode;
    }

    /**
     * Sets the parameter value for the Mode.<p>
     *
     * @param mode the parameter value for the Mode to set
     */
    public void setParamMode(String mode) {

        m_mode = CmsDiffViewMode.valueOf(mode);
    }

    /**
     * Returns the text to compare as copy.<p>
     *
     * @return the text to compare as copy
     */
    protected abstract String getCopySource();

    /**
     * Returns the number of lines to show before they are skipped.<p>
     *
     * @return the number of lines to show before they are skipped
     */
    protected abstract int getLinesBeforeSkip();

    /**
     * Returns the text to compare as original.<p>
     *
     * @return the text to compare as original
     */
    protected abstract String getOriginalSource();

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamMode())) {
            // ensure a valid mode is set
            m_mode = CmsDiffViewMode.DIFF_ONLY;
        }
        // test the needed parameters
        try {
            validateParamaters();
        } catch (Exception e) {
            // close if parameters not available
            setAction(ACTION_CANCEL);
            try {
                actionCloseDialog();
            } catch (JspException e1) {
                // noop
            }
            return;
        }
    }

    /**
     * Validates the parameters.<p>
     *
     * @throws Exception if something goes wrong
     */
    protected abstract void validateParamaters() throws Exception;

    /**
     *
     * Returns a diff text wrapped with formatting style.<p>
     *
     * @param diff the text to wrap with CSS formatting
     * @return the text with formatting styles wrapped
     * @throws IOException if something goes wrong
     */
    protected String wrapLinesWithUnchangedStyle(String diff) throws IOException {

        String line;
        StringBuffer result = new StringBuffer();
        BufferedReader br = new BufferedReader(new StringReader(diff));
        while ((line = br.readLine()) != null) {
            if ("".equals(line.trim())) {
                line = "&nbsp;";
            }
            result.append("<div class=\"df-unc\"><span class=\"df-unc\">").append(line).append("</span></div>\n");
        }
        return result.toString();
    }

}