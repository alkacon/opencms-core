/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.client.CmsGalleryFactory;
import org.opencms.ade.galleries.client.I_CmsGalleryHandler;
import org.opencms.ade.galleries.client.ui.CmsResultListItem;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.A_CmsToolbarMenu;
import org.opencms.gwt.client.ui.CmsListItemWidget.Background;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.I_CmsButton;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * The gallery tool-bar menu.<p>
 * 
 * @since 8.0.0
 */
public class CmsToolbarGalleryMenu extends A_CmsToolbarMenu<CmsContainerpageHandler> {

    /** The main content widget. */
    private FlowPanel m_contentPanel;

    /** The drag and drop handler for the gallery menu. */
    private CmsDNDHandler m_dragHandler;

    /** Signals if the gallery was already opened. */
    private boolean m_initialized;

    /**
     * Constructor.<p>
     * 
     * @param handler the container-page handler
     * @param dragHandler the container-page drag handler
     */
    public CmsToolbarGalleryMenu(CmsContainerpageHandler handler, CmsDNDHandler dragHandler) {

        super(I_CmsButton.ButtonData.ADD, handler);
        m_dragHandler = dragHandler;
        m_contentPanel = new FlowPanel();
        setMenuWidget(m_contentPanel);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarActivate()
     */
    public void onToolbarActivate() {

        Document.get().getBody().addClassName(I_CmsButton.ButtonData.ADD.getIconClass());
        if (!m_initialized) {
            SimplePanel tabsContainer = new SimplePanel();
            tabsContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().menuTabContainer());
            Predicate<CmsResultItemBean> resultDndFilter = Predicates.alwaysTrue();
            if (CmsContainerpageController.get().getData().getTemplateContextInfo().getCurrentContext() != null) {
                resultDndFilter = new CmsTemplateContextResultDndFilter();
            }
            final Predicate<CmsResultItemBean> finalDndFilter = resultDndFilter;
            //tabsContainer.add(CmsGalleryFactory.createDialog(m_dragHandler, m_popup, resultDndFilter));
            tabsContainer.add(CmsGalleryFactory.createDialog(new I_CmsGalleryHandler() {

                public boolean filterDnd(CmsResultItemBean resultBean) {

                    if (finalDndFilter != null) {
                        return finalDndFilter.apply(resultBean);
                    } else {
                        return true;
                    }
                }

                public I_CmsAutoHider getAutoHideParent() {

                    return getPopup();
                }

                public CmsDNDHandler getDndHandler() {

                    return getDragHandler();
                }

                public void processResultItem(CmsResultListItem item) {

                    if (item.getResult().isCopyModel()) {
                        item.getListItemWidget().setBackground(Background.YELLOW);
                    }
                }

            }));
            m_contentPanel.add(tabsContainer);
            m_initialized = true;
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarButton#onToolbarDeactivate()
     */
    public void onToolbarDeactivate() {

        Document.get().getBody().removeClassName(I_CmsButton.ButtonData.ADD.getIconClass());
    }

    /**
     * Gets the drag handler.<p>
     * 
     * @return the drag handler 
     */
    protected CmsDNDHandler getDragHandler() {

        return m_dragHandler;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsMenuButton#getPopup()
     */
    @Override
    protected CmsPopup getPopup() {

        return super.getPopup();
    }

}