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

package org.opencms.gwt.client.ui.replace;

import org.opencms.gwt.client.ui.CmsLoadingAnimation;
import org.opencms.gwt.client.ui.FontOpenCms;
import org.opencms.gwt.client.ui.css.I_CmsConstantsBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * The replace dialog content widget.<p>
 */
public class CmsReplaceContentWidget extends Composite {

    /** The UiBinder interface for this widget. */
    interface I_CmsReplaceContentWidgetUiBinder extends UiBinder<FlowPanel, CmsReplaceContentWidget> {
        // nothing to do
    }

    /** The UiBinder for this widget. */
    private static I_CmsReplaceContentWidgetUiBinder uiBinder = GWT.create(I_CmsReplaceContentWidgetUiBinder.class);

    /** The dialog content container. */
    @UiField
    protected FlowPanel m_container;

    /** The dialog info widget. */
    @UiField
    protected HTML m_dialogInfo;

    /** The loading panel. */
    private FlowPanel m_loadingPanel;

    /** The main panel. */
    private FlowPanel m_mainPanel;

    /** The replace info widget. */
    private Widget m_replaceInfo;

    /**
     * Constructor.<p>
     */
    public CmsReplaceContentWidget() {

        m_mainPanel = uiBinder.createAndBindUi(this);
        initWidget(m_mainPanel);
    }

    /**
     * Sets the dialog info message.<p>
     *
     * @param msg the message to display
     * @param warning signals whether the message should be a warning or nor
     */
    public void displayDialogInfo(String msg, boolean warning) {

        StringBuffer buffer = new StringBuffer(64);
        if (!warning) {
            buffer.append("<p class=\"");
            buffer.append(I_CmsLayoutBundle.INSTANCE.uploadButton().dialogMessage());
            buffer.append("\">");
            buffer.append(msg);
            buffer.append("</p>");
        } else {
            buffer.append(FontOpenCms.WARNING.getHtml(32, I_CmsConstantsBundle.INSTANCE.css().colorWarning()));
            buffer.append("<p class=\"");
            buffer.append(I_CmsLayoutBundle.INSTANCE.uploadButton().warningMessage());
            buffer.append("\">");
            buffer.append(msg);
            buffer.append("</p>");
        }
        m_dialogInfo.setHTML(buffer.toString());
    }

    /**
     * Removes the loading animation.<p>
     */
    public void removeLoadingAnimation() {

        if (m_loadingPanel != null) {
            m_loadingPanel.removeFromParent();
            m_loadingPanel = null;
        }
    }

    /**
     * Sets the container widget content.<p>
     *
     * @param widget the container content
     */
    public void setContainerWidget(Widget widget) {

        m_container.clear();
        m_container.add(widget);
    }

    /**
     * Sets the replace info widget.<p>
     *
     * @param replaceInfo the replace info widget
     */
    public void setReplaceInfo(Widget replaceInfo) {

        if (m_replaceInfo != null) {
            m_replaceInfo.removeFromParent();
        }
        m_replaceInfo = replaceInfo;
        m_mainPanel.insert(m_replaceInfo, 0);
    }

    /**
     * Creates the loading animation HTML and adds is to the content wrapper.<p>
     *
     * @param msg the message to display below the animation
     */
    public void showLoadingAnimation(String msg) {

        removeLoadingAnimation();
        m_loadingPanel = new FlowPanel();
        m_loadingPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadButton().loadingPanel());
        m_loadingPanel.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());

        CmsLoadingAnimation animationDiv = new CmsLoadingAnimation();
        animationDiv.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadButton().loadingAnimation());
        m_loadingPanel.add(animationDiv);

        HTML messageDiv = new HTML();
        messageDiv.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadButton().loadingText());
        messageDiv.setHTML(msg);
        m_loadingPanel.add(messageDiv);

        m_container.add(m_loadingPanel);
    }
}
