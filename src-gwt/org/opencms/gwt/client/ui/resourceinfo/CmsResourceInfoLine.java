/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.resourceinfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A single information item for the resource information dialog.<p>
 */
public class CmsResourceInfoLine extends Composite implements HasText {

    /**
     * The uibinder interface for this widget.<p>
     */
    interface I_CmsResourceInfoLineUiBinder extends UiBinder<Widget, CmsResourceInfoLine> {
        // empty
    }

    /** The uibinder instance for this widget. */
    private static I_CmsResourceInfoLineUiBinder uiBinder = GWT.create(I_CmsResourceInfoLineUiBinder.class);

    /** The field content. */
    @UiField
    protected HTML m_content = new HTML();

    /** The label for the field. */
    @UiField
    protected InlineLabel m_label = new InlineLabel();

    /** Flag which controls whether HTML should be use for the info value. */
    private boolean m_useHtml;

    /**
     * Creates a new widget instance.<p>
     */
    public CmsResourceInfoLine() {

        initWidget(uiBinder.createAndBindUi(this));
        setVisible(false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#getText()
     */
    public String getText() {

        return m_content.getText();
    }

    /**
     * Sets the label text.<p>
     *
     * @param label the label text
     */
    public void setLabel(String label) {

        m_label.setText(label);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#setText(java.lang.String)
     */
    public void setText(String text) {

        if (text == null) {
            setVisible(false);
        } else {
            if (m_useHtml) {
                m_content.setHTML(text);
            } else {
                m_content.setText(text);
            }
            setVisible(true);
        }
    }

    /**
     * Enables or disables the use of HTML in the content field.<p>
     *
     * @param useHtml if true, enables HTML usage
     */
    public void setUseHtml(boolean useHtml) {

        m_useHtml = useHtml;
    }

}
