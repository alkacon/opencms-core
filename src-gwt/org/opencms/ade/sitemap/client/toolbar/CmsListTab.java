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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapLayoutBundle;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.I_CmsTruncable;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Tab widget to display a CmsList.<p>
 */
public class CmsListTab extends Composite implements I_CmsTruncable {

    /** Text metrics key for truncation. */
    public static final String TM_LITST_MENU = "TM_LITST_MENU";

    /** The optional clear list button. */
    private CmsPushButton m_clearButton;

    /** The list. */
    private CmsList<? extends I_CmsListItem> m_list;

    /** The main panel. */
    private FlowPanel m_panel;

    /** The scroll panel. */
    private CmsScrollPanel m_scrollPanel;

    /**
     * Constructor.<p>
     *
     * @param list the list
     */
    public CmsListTab(CmsList<? extends I_CmsListItem> list) {

        m_list = list;
        m_panel = new FlowPanel();
        initWidget(m_panel);
        setStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().tabPanel());
        m_scrollPanel = GWT.create(CmsScrollPanel.class);
        m_scrollPanel.addStyleName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().buttonCornerAll());
        m_scrollPanel.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.clipboardCss().clipboardList());
        m_panel.add(m_scrollPanel);
        m_scrollPanel.add(m_list);
    }

    /**
     * Adds a clear list button to the tab.<p>
     *
     * @param clickHandler the button click handler
     */
    public void addClearListButton(ClickHandler clickHandler) {

        m_clearButton = new CmsPushButton();
        m_clearButton.setText(Messages.get().key(Messages.GUI_CLIPBOARD_CLEAR_LIST_0));
        m_clearButton.setTitle(Messages.get().key(Messages.GUI_CLIPBOARD_CLEAR_LIST_0));
        m_clearButton.addClickHandler(clickHandler);
        m_clearButton.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.clipboardCss().listClearButton());
        m_panel.add(m_clearButton);
    }

    /**
     * Returns the required height.<p>
     *
     * @return the height
     */
    public int getRequiredHeight() {

        return m_list.getElement().getClientHeight() + 12;
    }

    /**
     * Returns the scroll panel.<p>
     *
     * @return the scroll panel
     */
    public CmsScrollPanel getScrollPanel() {

        return m_scrollPanel;
    }

    /**
     * Sets the clear list button enabled.<p>
     *
     * @param enabled <code>true</code> to enable the button
     */
    public void setClearButtonEnabled(boolean enabled) {

        if (enabled) {
            m_clearButton.enable();
        } else {
            m_clearButton.disable(Messages.get().key(Messages.GUI_DISABLE_CLEAR_LIST_0));
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int clientWidth) {

        m_list.truncate(TM_LITST_MENU, clientWidth);
    }
}
