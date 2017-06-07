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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.ui.I_CmsTruncable;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;

/**
 * This class represents a single select option in the selector of the select box.
 */
public class CmsLabelSelectCell extends A_CmsSelectCell implements I_CmsTruncable {

    /** The value of the select option. */
    protected String m_value;

    /** The label of which this select cell consists. */
    private CmsLabel m_label = new CmsLabel();

    /** The label width last used in a truncate()-call. */
    private int m_labelWidth;

    /** The opener text. */
    private String m_openerText;

    /** The text of the select option. */
    private String m_text;

    /** The text metrics key last used in a truncate()-call. */
    private String m_textMetricsKey;

    /**
     * Creates a new select cell.<p>
     *
     * @param value the value of the select option
     * @param text the text to display for the select option
     */
    public CmsLabelSelectCell(String value, String text) {

        this(value, text, null);
    }

    /**
     * Creates a new select cell.<p>
     *
     * @param value the value of the select option
     * @param text the text to display for the select option
     * @param title the title to display on mouseover
     */
    public CmsLabelSelectCell(String value, String text, String title) {

        super();
        m_value = value;
        m_text = text;
        m_openerText = text;
        initWidget(m_label);
        addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().selectBoxCell());
        m_label.setText(m_text);
        m_label.setTitle(title != null ? title : m_text);
    }

    /**
     * Gets the opener text.<p>
     *
     * @return the opener text
     */
    public String getOpenerText() {

        return m_openerText;
    }

    /**
     * Returns the text as which the select option should be displayed to the user.<p>
     *
     * @return the text of the select option
     */
    public String getText() {

        return m_text;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectCell#getValue()
     */
    @Override
    public String getValue() {

        return m_value;
    }

    /**
     * Sets the opener text.<p>
     *
     * @param openerText the new opener text
     */
    public void setOpenerText(String openerText) {

        m_openerText = openerText;
    }

    /**
     * Sets the text of the label.<p>
     *
     * @param text the new text
     */
    public void setText(String text) {

        m_label.setText(text);
        m_label.setTitle(text);
        m_text = text;
        if (m_textMetricsKey != null) {
            truncate(m_textMetricsKey, m_labelWidth);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int labelWidth) {

        m_textMetricsKey = textMetricsKey;
        m_labelWidth = labelWidth;
        m_label.truncate(textMetricsKey, labelWidth);
    }

}
