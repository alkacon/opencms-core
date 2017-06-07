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

package org.opencms.ui.dialogs.history.diff;

import com.alkacon.diff.Diff;

import org.opencms.main.CmsLog;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.util.CmsHtml2TextConverter;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.comparison.CmsDiffViewMode;
import org.opencms.workplace.comparison.CmsHtmlDifferenceConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.logging.Log;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Widget used to display a colorized diff view for two texts.<p>
 */
public class CmsTextDiffPanel extends VerticalLayout {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsTextDiffPanel.class);

    /** Serial version  id. */
    private static final long serialVersionUID = 1L;

    /** Label containing the actual diff. */
    private Label m_diffHtml;

    /** The current diff mode. */
    private String m_diffMode = "diff";

    /** Selects between different diff modes. */
    private OptionGroup m_diffModeSelect;

    /** First text used for comparison. */
    private String m_text1;

    /** Second text used for comparison. */
    private String m_text2;

    /** The current text mode. */
    private String m_textMode = "html";

    /** Selects between different text modes. */
    private OptionGroup m_textOrHtmlSelect;

    /**
     * Creates a new instance.<p>
     *
     * @param text1 the first text
     * @param text2 the second text
     *
     * @param selectTextOrHtml true if the option to select between comparison as text and comparison as HTML should be shown
     * @param selectDiffMode true if the option to select between showing only the changed lines or all the lines should be displayed
     */
    public CmsTextDiffPanel(String text1, String text2, boolean selectTextOrHtml, boolean selectDiffMode) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_text1 = text1;
        m_text2 = text2;
        m_textOrHtmlSelect.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        m_textOrHtmlSelect.setValue("html");
        m_textOrHtmlSelect.setVisible(selectTextOrHtml);
        m_diffModeSelect.setValue("diff");
        m_diffModeSelect.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        m_diffModeSelect.setVisible(selectDiffMode);

        m_textOrHtmlSelect.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void valueChange(ValueChangeEvent event) {

                m_textMode = event.getProperty().getValue().toString();
                update();
            }
        });

        m_diffModeSelect.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void valueChange(ValueChangeEvent event) {

                m_diffMode = event.getProperty().getValue().toString();
                update();
            }
        });

        update();
    }

    /**
     * Gets the diff HTML based on the selected display options.<p>
     *
     * @return the diff HTML to display
     *
     * @throws Exception if something goes wrong
     */
    public String getDiffHtml() throws Exception {

        CmsDiffViewMode mode = "all".equals(m_diffMode) ? CmsDiffViewMode.ALL : CmsDiffViewMode.DIFF_ONLY;
        String text1 = m_text1;
        String text2 = m_text2;
        if ("text".equals(m_textMode)) {
            text1 = CmsHtml2TextConverter.html2text(text1, "UTF-8");
            text2 = CmsHtml2TextConverter.html2text(text2, "UTF-8");
        }
        CmsHtmlDifferenceConfiguration conf = new CmsHtmlDifferenceConfiguration(
            mode == CmsDiffViewMode.ALL ? -1 : 2,
            A_CmsUI.get().getLocale());
        String diff = Diff.diffAsHtml(text1, text2, conf);
        String html = null;
        if (CmsStringUtil.isNotEmpty(diff)) {
            html = diff;
        } else {
            html = wrapLinesWithUnchangedStyle(
                CmsStringUtil.substitute(CmsStringUtil.escapeHtml(m_text1), "<br/>", ""));
        }
        return html;

    }

    /**
     * Updates the displayed diff based on the selected display options.<p>
     */
    public void update() {

        try {
            String diffHtml = "<pre>" + getDiffHtml() + "</pre>";
            m_diffHtml.setContentMode(ContentMode.HTML);
            m_diffHtml.setValue(diffHtml);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

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
