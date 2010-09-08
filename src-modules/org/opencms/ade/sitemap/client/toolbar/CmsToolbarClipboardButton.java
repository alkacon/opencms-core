/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/toolbar/Attic/CmsToolbarClipboardButton.java,v $
 * Date   : $Date: 2010/09/08 08:34:01 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsMenuButton;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsListItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sitemap toolbar clipboard button.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarClipboardButton extends CmsMenuButton implements I_CmsToolbarActivatable {

    /** The content panel. */
    protected FlowPanel m_content;

    /**
     * Constructor.<p>
     * 
     * @param toolbar the toolbar instance
     * @param controller the sitemap controller 
     */
    public CmsToolbarClipboardButton(final CmsSitemapToolbar toolbar, final CmsSitemapController controller) {

        super(I_CmsButton.ButtonData.CLIPBOARD.getTitle(), I_CmsButton.ButtonData.CLIPBOARD.getIconClass());
        if (!controller.isEditable()) {
            setEnabled(false);
            // TODO: the CmsMenuButon should also implement this method!
            // disable(controller.getData().getNoEditReason());
        }

        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                if (!isOpen()) {
                    toolbar.onButtonActivation(CmsToolbarClipboardButton.this);
                    if (m_content == null) {
                        // lazy initialization
                        CmsToolbarClipboardView view = new CmsToolbarClipboardView(
                            CmsToolbarClipboardButton.this,
                            controller);
                        CmsTabbedPanel<FlowPanel> tabs = new CmsTabbedPanel<FlowPanel>();
                        tabs.add(
                            createTab(Messages.get().key(Messages.GUI_CLIPBOARD_MODIFIED_DESC_0), view.getModified()),
                            Messages.get().key(Messages.GUI_CLIPBOARD_MODIFIED_TITLE_0));
                        tabs.add(
                            createTab(Messages.get().key(Messages.GUI_CLIPBOARD_DELETED_DESC_0), view.getDeleted()),
                            Messages.get().key(Messages.GUI_CLIPBOARD_DELETED_TITLE_0));

                        SimplePanel tabsContainer = new SimplePanel();
                        tabsContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.clipboardCss().menuTabContainer());
                        tabsContainer.add(tabs);

                        m_content = new FlowPanel();
                        m_content.setStyleName(I_CmsLayoutBundle.INSTANCE.clipboardCss().menuContent());
                        m_content.add(tabsContainer);
                        setMenuWidget(m_content);
                    }

                    openMenu();
                } else {
                    closeMenu();
                }
            }
        });
    }

    /**
     * Creates a new tab.<p>
     * 
     * @param description the description 
     * @param list list of items
     * 
     * @return the new created tab
     */
    public FlowPanel createTab(String description, CmsList<? extends I_CmsListItem> list) {

        FlowPanel tab = new FlowPanel();
        tab.setStyleName(I_CmsLayoutBundle.INSTANCE.clipboardCss().clipboardTabPanel());
        Label descriptionLabel = new Label(description);
        descriptionLabel.setStyleName(I_CmsLayoutBundle.INSTANCE.clipboardCss().description());
        descriptionLabel.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().textBig());
        tab.add(descriptionLabel);
        list.setStyleName(I_CmsLayoutBundle.INSTANCE.clipboardCss().itemList());
        list.addStyleName(I_CmsLayoutBundle.INSTANCE.clipboardCss().clipboardList());
        list.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        tab.add(list);
        return tab;
    }

    /**
     * @see org.opencms.ade.sitemap.client.toolbar.I_CmsToolbarActivatable#onActivation(com.google.gwt.user.client.ui.Widget)
     */
    public void onActivation(Widget widget) {

        closeMenu();
    }
}
