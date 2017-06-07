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

package org.opencms.ade.galleries.client.ui;

import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * A panel displaying search parameters associated with a gallery tab.<p>
 * Used in the result tab to display and remove these parameters.<p>
 *
 * @since 8.0.0
 */
public class CmsSearchParamPanel extends Composite {

    /** The ui-binder to this widget. */
    interface I_CmsSearchParamPanelUiBinder extends UiBinder<FlowPanel, CmsSearchParamPanel> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance. */
    private static I_CmsSearchParamPanelUiBinder uiBinder = GWT.create(I_CmsSearchParamPanelUiBinder.class);

    /** The parameters title. */
    private String m_title;

    /** The HTML widget to hold the parameters content. */
    @UiField
    protected HTML m_text;

    /** The button to remove the parameters. */
    @UiField
    protected CmsPushButton m_button;

    /** The result tab. */
    private A_CmsTab m_tab;

    /** The parameter key. */
    private String m_paramKey;

    /**
     * Constructor.<p>
     *
     * @param title the parameters title
     * @param tab the tab
     */
    public CmsSearchParamPanel(String title, A_CmsTab tab) {

        initWidget(uiBinder.createAndBindUi(this));
        m_button.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_button.setImageClass(I_CmsButton.DELETE_SMALL);
        m_button.setSize(Size.small);
        m_title = title;
        m_tab = tab;
    }

    /**
     * Sets the text content of the parameters panel.<p>
     *
     * @param content the content
     * @param paramKey the parameter key
     */
    public void setContent(String content, String paramKey) {

        StringBuffer sb = new StringBuffer(128);
        sb.append("<b>").append(m_title).append("</b>&nbsp;").append(content);
        m_text.setHTML(sb.toString());
        m_paramKey = paramKey;
    }

    /**
     * Calls to the result tab to remove parameters. Executed on button click.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_button")
    protected void onClick(ClickEvent event) {

        m_tab.removeParam(m_paramKey);
        m_tab = null;
        removeFromParent();
    }
}
