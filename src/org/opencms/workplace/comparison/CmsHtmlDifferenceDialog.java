/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/comparison/CmsHtmlDifferenceDialog.java,v $
 * Date   : $Date: 2006/03/22 16:12:26 $
 * Version: $Revision: 1.1.2.5 $
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

import com.alkacon.diff.Diff;

import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsHtml2TextConverter;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Provides a GUI for the file comparison dialog.<p> 
 *
 * @author Jan Baudisch  
 * 
 * @version $Revision: 1.1.2.5 $ 
 * 
 * @since 6.0.0 
 */
public class CmsHtmlDifferenceDialog extends CmsDifferenceDialog {

    /** constant indicating that the html is to be compared.<p> */
    public static final String MODE_HTML = "html";

    /** constant indicating that a textual representation of the html is to be compared.<p> */
    public static final String MODE_TEXT = "text";

    /** request parameter for the textmode.<p> */
    public static final String PARAM_TEXTMODE = "textmode";

    /**
     * Default constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsHtmlDifferenceDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsHtmlDifferenceDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
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
        out.println("<p>");
        out.println(getDiffOnlyButtonsHtml());
        
        String onClic = "javascript:document.forms['diff-form'].textmode.value = '";
        String iconPath = null;
        CmsMessageContainer iconName = null;
        boolean htmlMode = MODE_HTML.equals(getParamTextmode());
        if (htmlMode) {
            iconPath = A_CmsListDialog.ICON_DETAILS_SHOW;
        } else {
            iconPath = A_CmsListDialog.ICON_DETAILS_HIDE;
        }        
        onClic += MODE_TEXT;        
        iconName = Messages.get().container(Messages.GUI_DIFF_MODE_TEXT_0);
        onClic += "'; document.forms['diff-form'].submit();";
        out.println(A_CmsHtmlIconButton.defaultButtonHtml(
            getJsp(),
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            "id",
            iconName.key(getLocale()),
            null,
            htmlMode, // activate Button, if in html mode
            iconPath,
            null,
            onClic));
        out.println("&nbsp;&nbsp;");

        onClic = "javascript:document.forms['diff-form'].textmode.value = '";
        if (!htmlMode) {
            iconPath = A_CmsListDialog.ICON_DETAILS_SHOW;
        } else {
            iconPath = A_CmsListDialog.ICON_DETAILS_HIDE;
        } 
        onClic += MODE_HTML;
        iconName = Messages.get().container(Messages.GUI_DIFF_MODE_HTML_0);
        onClic += "'; document.forms['diff-form'].submit();";
        out.println(A_CmsHtmlIconButton.defaultButtonHtml(
            getJsp(),
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            "id",
            iconName.key(getLocale()),
            null,
            !htmlMode, // activate Button, if in text mode
            iconPath,
            null,
            onClic));
        out.println("&nbsp;&nbsp;");
        out.println("</p>");
        out.println(dialogBlockStart(null));
        out.println("<table cellspacing='0' cellpadding='0' class='xmlTable'>\n<tr><td><pre style='overflow:auto'>");
        try {
            CmsHtmlDifferenceConfiguration conf = new CmsHtmlDifferenceConfiguration(
                getMode() == CmsDiffViewMode.ALL ? -1 : getLinesBeforeSkip(),
                getLocale());
            String originalSource = getOriginalSource();
            String copySource = getCopySource();
            if (MODE_TEXT.equals(getParamTextmode())) {
                originalSource = CmsHtml2TextConverter.html2text(originalSource, CmsEncoder.ENCODING_ISO_8859_1);
                copySource = CmsHtml2TextConverter.html2text(copySource, CmsEncoder.ENCODING_ISO_8859_1);
            }
            String diff = Diff.diffAsHtml(originalSource, copySource, conf);
            if (CmsStringUtil.isNotEmpty(diff)) {
                out.println(diff);
            } else {
                String htmlDiff = Diff.diffAsHtml(getOriginalSource(), getCopySource(), conf);
                if (CmsStringUtil.isNotEmpty(htmlDiff)) {
                    // extracted text is equal, but html differs
                    out.println(Messages.get().key(Messages.GUI_COMPARE_IDENTICAL_TEXT_DIFFERENT_HTML_0));
                } else if (getMode() == CmsDiffViewMode.ALL) { 
                    // print original source, if there are no differences
                    out.println(wrapLinesWithUnchangedStyle(CmsStringUtil.substitute(CmsStringUtil.escapeHtml(originalSource), "<br/>", ""))); 
                }
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
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        addMessages(org.opencms.workplace.commons.Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamTextmode())) {
            // ensure a valid mode is set
            setParamTextmode(MODE_TEXT);
        }
    }

    /**
     * 
     * @see org.opencms.workplace.comparison.A_CmsDiffViewDialog#validateParamaters()
     */
    protected void validateParamaters() {

        // noop
    }
}