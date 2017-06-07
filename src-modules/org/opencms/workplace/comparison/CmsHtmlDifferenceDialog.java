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

import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsHtml2TextConverter;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

/**
 * Provides a GUI for the file comparison dialog.<p>
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
    @Override
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

        String onClic1 = "javascript:document.forms['diff-form'].textmode.value = '";
        onClic1 += MODE_TEXT;
        onClic1 += "'; document.forms['diff-form'].submit();";
        String onClic2 = "javascript:document.forms['diff-form'].textmode.value = '";
        onClic2 += MODE_HTML;
        onClic2 += "'; document.forms['diff-form'].submit();";
        out.println(
            getTwoButtonsHtml(
                Messages.get().container(Messages.GUI_DIFF_MODE_TEXT_0).key(getLocale()),
                Messages.get().container(Messages.GUI_DIFF_MODE_HTML_0).key(getLocale()),
                onClic1,
                onClic2,
                MODE_HTML.equals(getParamTextmode())));

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
                    out.println(
                        Messages.get().container(Messages.GUI_COMPARE_IDENTICAL_TEXT_DIFFERENT_HTML_0).key(
                            getLocale()));
                } else if (getMode() == CmsDiffViewMode.ALL) {
                    // print original source, if there are no differences
                    out.println(wrapLinesWithUnchangedStyle(
                        CmsStringUtil.substitute(CmsStringUtil.escapeHtml(originalSource), "<br/>", "")));
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
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
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
    @Override
    protected void validateParamaters() {

        // noop
    }
}