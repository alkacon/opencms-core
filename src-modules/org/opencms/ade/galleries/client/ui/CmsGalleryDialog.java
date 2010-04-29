/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsGalleryDialog.java,v $
 * Date   : $Date: 2010/04/29 07:37:51 $
 * Version: $Revision: 1.8 $
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.CmsCategoriesTabHandler;
import org.opencms.ade.galleries.client.CmsGalleriesTabHandler;
import org.opencms.ade.galleries.client.CmsGalleryController;
import org.opencms.ade.galleries.client.CmsResultsTabHandler;
import org.opencms.ade.galleries.client.CmsTypesTabHandler;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.gwt.client.draganddrop.I_CmsDragElement;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.draganddrop.I_CmsDragTarget;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabLayout;

import java.util.ArrayList;

import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Composite;

/**
 * Provides the method for the gallery dialog.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 8.0.
 */
public class CmsGalleryDialog extends Composite implements BeforeSelectionHandler<Integer>, SelectionHandler<Integer> {

    /** The reference to the drag handler. */
    private I_CmsDragHandler<? extends I_CmsDragElement, ? extends I_CmsDragTarget> m_dragHandler;

    /** The flag for the initails search. */
    private boolean m_isInitialSearch;

    /** The tabbed panel. */
    private CmsTabbedPanel<A_CmsTab> m_tabbedPanel;

    /**
     * The default constructor for the gallery dialog.<p> 
     */
    public CmsGalleryDialog() {

        initCss();
        m_dragHandler = null;
        m_isInitialSearch = false;
        m_tabbedPanel = new CmsTabbedPanel<A_CmsTab>(CmsTabLayout.standard, false);

        // All composites must call initWidget() in their constructors.
        initWidget(m_tabbedPanel);
    }

    // TODO: remove, if do not used any more
    /**
     * The constructor of the gallery dialog.<p>
     * 
     * @param tabsConfig the tabs config string for this gallery dialog
     * @param controller the reference to the gallery controller
     * @param handler the reference to the drag handler
     */
    public CmsGalleryDialog(
        ArrayList<String> tabsConfig,
        CmsGalleryController controller,
        I_CmsDragHandler<? extends I_CmsDragElement, ? extends I_CmsDragTarget> handler) {

        initCss();
        m_dragHandler = handler;
        m_tabbedPanel = new CmsTabbedPanel<A_CmsTab>(CmsTabLayout.standard, false);

        // All composites must call initWidget() in their constructors.
        initWidget(m_tabbedPanel);

        fillTabs(tabsConfig, controller);

    }

    /**
     * The default constructor for the gallery dialog.<p> 
     * 
     * @param handler the reference to the drag handler
     */
    public CmsGalleryDialog(I_CmsDragHandler<? extends I_CmsDragElement, ? extends I_CmsDragTarget> handler) {

        initCss();
        m_dragHandler = handler;
        m_tabbedPanel = new CmsTabbedPanel<A_CmsTab>(CmsTabLayout.standard, false);

        // All composites must call initWidget() in their constructors.
        initWidget(m_tabbedPanel);
    }

    /**
     * Ensures all style sheets are loaded.<p>
     */
    public static void initCss() {

        I_CmsLayoutBundle.INSTANCE.galleryDialogCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.listTreeCss().ensureInjected();
        org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.INSTANCE.galleryDialogCss().ensureInjected();
    }

    /**
     * Fill the tabs with the content provided from the info bean. <p>
     * 
     * @param tabsConfig the tabs config string for this gallery dialog
     * @param controller the reference to the gallery controller
     */
    public void fillTabs(ArrayList<String> tabsConfig, CmsGalleryController controller) {

        if (tabsConfig.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_types.name())) {
            CmsTypesTab tabContent = new CmsTypesTab(m_dragHandler);
            tabContent.setTabHandler(new CmsTypesTabHandler(controller));
            m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_TYPES_0));
        }

        if (tabsConfig.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_galleries.name())) {

            CmsGalleriesTab tabContent = new CmsGalleriesTab();
            tabContent.setTabHandler(new CmsGalleriesTabHandler(controller));
            m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_GALLERIES_0));

        }

        if (tabsConfig.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_categories.name())) {

            CmsCategoriesTab tabContent = new CmsCategoriesTab();
            tabContent.setTabHandler(new CmsCategoriesTabHandler(controller));
            m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_CATEGORIES_0));

        }

        if (tabsConfig.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_search.name())) {
            // TODO: fill with content
            m_tabbedPanel.add(new CmsSearchTab(), Messages.get().key(Messages.GUI_TAB_TITLE_SEARCH_0));
        }

        if (tabsConfig.contains(I_CmsGalleryProviderConstants.GalleryTabId.cms_tab_sitemap.name())) {
            // implement  
        }
        // add left margin to the result button
        CmsResultsTab resultTab = new CmsResultsTab(m_dragHandler);
        resultTab.setHandler(new CmsResultsTabHandler(controller));
        m_tabbedPanel.addWithLeftMargin(resultTab, Messages.get().key(Messages.GUI_TAB_TITLE_RESULTS_0));

        m_tabbedPanel.addBeforeSelectionHandler(this);
        m_tabbedPanel.addSelectionHandler(this);
    }

    /**
     * Returns the categories tab widget.<p>
     * 
     * @return the categories widget
     */
    public CmsCategoriesTab getCategoriesTab() {

        // TODO: better way to get the last index
        return (CmsCategoriesTab)m_tabbedPanel.getWidget(2);
    }

    /**
     * Returns the galleries tab widget.<p>
     * 
     * @return the galleries widget
     */
    public CmsGalleriesTab getGalleriesTab() {

        // TODO: better way to get the last index
        return (CmsGalleriesTab)m_tabbedPanel.getWidget(1);
    }

    /**
     * Returns the results tab widget.<p>
     * 
     * @return the results widget
     */
    public CmsResultsTab getResultsTab() {

        // TODO: better way to get the last index
        return (CmsResultsTab)m_tabbedPanel.getWidget(4);
    }

    /**
     * Returns the types tab widget.<p>
     * 
     * @return the types widget
     */
    public CmsTypesTab getTypesTab() {

        // TODO: better way to get the last index
        return (CmsTypesTab)m_tabbedPanel.getWidget(0);
    }

    /**
     * @see com.google.gwt.event.logical.shared.BeforeSelectionHandler#onBeforeSelection(com.google.gwt.event.logical.shared.BeforeSelectionEvent)
     */
    public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {

        // event.cancel(), if the tab selection should be canceled, the tab will not be selected 
        // Integer index = event.getItem(); the index of the selected tab               
    }

    /**
     * @see com.google.gwt.event.logical.shared.SelectionHandler#onSelection(com.google.gwt.event.logical.shared.SelectionEvent)
     */
    public void onSelection(SelectionEvent<Integer> event) {

        int selectedIndex = m_tabbedPanel.getSelectedIndex();

        A_CmsTab tabWidget = m_tabbedPanel.getWidget(selectedIndex);
        // delegate to the specific tab handler on selection
        if (tabWidget instanceof CmsTypesTab) {
            tabWidget.onSelection();
        } else if (tabWidget instanceof CmsGalleriesTab) {
            tabWidget.onSelection();
        } else if (tabWidget instanceof CmsCategoriesTab) {
            tabWidget.onSelection();
        } else if ((tabWidget instanceof CmsResultsTab) && m_isInitialSearch) {
            m_isInitialSearch = false;
        } else if ((tabWidget instanceof CmsResultsTab) && !m_isInitialSearch) {
            tabWidget.onSelection();
        }
    }

    /**
     * Selects a tab.<p>
     * 
     * @param tabIndex the tab index to beselected
     * @param isInitial flag for initial search
     */
    public void selectTab(int tabIndex, boolean isInitial) {

        m_isInitialSearch = isInitial;
        m_tabbedPanel.selectTab(tabIndex);
    }
}