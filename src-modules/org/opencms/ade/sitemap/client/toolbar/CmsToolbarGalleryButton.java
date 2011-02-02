/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/toolbar/Attic/CmsToolbarGalleryButton.java,v $
 * Date   : $Date: 2011/02/02 07:37:52 $
 * Version: $Revision: 1.6 $
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

import org.opencms.ade.galleries.client.CmsGalleryFactory;
import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.ui.CmsSpecialTab;
import org.opencms.ade.sitemap.client.ui.CmsSpecialTabHandler;
import org.opencms.ade.sitemap.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsMenuButton;
import org.opencms.gwt.client.ui.I_CmsButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The sitemap toolbar gallery add button.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 8.0.0
 */
public class CmsToolbarGalleryButton extends CmsMenuButton implements I_CmsToolbarActivatable {

    /** The main content widget. */
    private FlowPanel m_contentPanel;

    /**
     * The constructor.<p>
     * 
     * @param toolbar the toolbar
     * @param controller the sitemap controller
     */
    public CmsToolbarGalleryButton(final CmsSitemapToolbar toolbar, final CmsSitemapController controller) {

        super(I_CmsButton.ButtonData.ADD.getTitle(), I_CmsButton.ButtonData.ADD.getIconClass());
        setToolbarMode(true);
        setOpenRight(true);
        if (!controller.isEditable()) {
            setEnabled(false);
        }
        addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                handleClick(toolbar);
            }
        });

    }

    /**
     * @see org.opencms.ade.sitemap.client.toolbar.I_CmsToolbarActivatable#onActivation(com.google.gwt.user.client.ui.Widget)
     */
    public void onActivation(Widget widget) {

        closeMenu();
    }

    /**
     * Handles the button click.<p>
     * 
     * @param toolbar the toolbar
     */
    protected void handleClick(CmsSitemapToolbar toolbar) {

        if (!isOpen()) {
            toolbar.onButtonActivation(CmsToolbarGalleryButton.this);
            if (m_contentPanel == null) {

                SimplePanel tabsContainer = new SimplePanel();
                tabsContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.clipboardCss().menuTabContainer());
                CmsGalleryDialog gallery = CmsGalleryFactory.createDialog(
                    CmsSitemapView.getInstance().getTree().getDnDHandler(),
                    m_content);
                boolean canEditDetailPages = CmsSitemapView.getInstance().getController().getData().canEditDetailPages();
                CmsSpecialTab special = new CmsSpecialTab(
                    new CmsSpecialTabHandler(gallery.getController()),
                    CmsSitemapView.getInstance().getTree().getDnDHandler(),
                    canEditDetailPages);

                String tabTitle = Messages.get().key(Messages.GUI_SPECIAL_TAB_TITLE_0);
                gallery.addTab(special, tabTitle);
                tabsContainer.add(gallery);

                m_contentPanel = new FlowPanel();
                m_contentPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.clipboardCss().menuContent());
                m_contentPanel.add(tabsContainer);
                setMenuWidget(m_contentPanel);
            }

            openMenu();
        } else {
            closeMenu();
        }
    }
}
