/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsGalleryDialog.java,v $
 * Date   : $Date: 2010/05/27 10:28:29 $
 * Version: $Revision: 1.20 $
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
import org.opencms.ade.galleries.client.CmsSearchTabHandler;
import org.opencms.ade.galleries.client.CmsTypesTabHandler;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.gwt.client.draganddrop.I_CmsDragHandler;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabLayout;

import java.util.Iterator;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Provides the method for the gallery dialog.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.20 $
 * 
 * @since 8.0.
 */
public class CmsGalleryDialog extends Composite
implements BeforeSelectionHandler<Integer>, SelectionHandler<Integer>, ResizeHandler, HasResizeHandlers {

    /** The initial dialog width. */
    public static final int DIALOG_HIGHT = 486;

    /** The initial dialog width. */
    public static final int DIALOG_WIDTH = 600;

    /** The categories tab. */
    private CmsCategoriesTab m_categoriesTab;

    /** The HTML id of the dialog element. */
    private String m_dialogElementId;

    /** The reference to the drag handler. */
    private I_CmsDragHandler<?, ?> m_dragHandler;

    /** The galleries tab. */
    private CmsGalleriesTab m_galleriesTab;

    /** The flag for the initails search. */
    private boolean m_isInitialSearch;

    /** The parent panel for the gallery dialog. */
    private FlowPanel m_parentPanel;

    /** The results tab. */
    private CmsResultsTab m_resultsTab;

    /** The tabbed panel. */
    private CmsTabbedPanel<A_CmsTab> m_tabbedPanel;

    /** The types tab. */
    private CmsTypesTab m_typesTab;

    /**
     * The default constructor for the gallery dialog.<p> 
     */
    public CmsGalleryDialog() {

        initCss();

        m_dragHandler = null;
        m_isInitialSearch = false;
        // parent widget
        m_parentPanel = new FlowPanel();
        m_parentPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().parentPanel());
        m_dialogElementId = HTMLPanel.createUniqueId();
        m_parentPanel.getElement().setId(m_dialogElementId);
        // set the default hight of the dialog
        m_parentPanel.getElement().getStyle().setHeight((DIALOG_HIGHT - 2), Unit.PX);
        // tabs
        m_tabbedPanel = new CmsTabbedPanel<A_CmsTab>(CmsTabLayout.standard, false);
        // add tabs to parent widget        
        m_parentPanel.add(m_tabbedPanel);

        // All composites must call initWidget() in their constructors.
        initWidget(m_parentPanel);
        addResizeHandler(this);
    }

    /**
     * The default constructor for the gallery dialog.<p> 
     * 
     * @param handler the reference to the drag handler
     */
    public CmsGalleryDialog(I_CmsDragHandler<?, ?> handler) {

        this();
        m_dragHandler = handler;
    }

    /**
     * Ensures all style sheets are loaded.<p>
     */
    public static void initCss() {

        I_CmsLayoutBundle.INSTANCE.galleryDialogCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.listTreeCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.previewDialogCss().ensureInjected();
        org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.generalCss().ensureInjected();
        org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.INSTANCE.galleryDialogCss().ensureInjected();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasResizeHandlers#addResizeHandler(com.google.gwt.event.logical.shared.ResizeHandler)
     */
    public HandlerRegistration addResizeHandler(ResizeHandler handler) {

        return addHandler(handler, ResizeEvent.getType());
    }

    /**
     * Fill the tabs with the content provided from the info bean. <p>
     * 
     * @param tabIds the tabs to show 
     * @param controller the reference to the gallery controller
     */
    public void fillTabs(GalleryTabId[] tabIds, CmsGalleryController controller) {

        for (int i = 0; i < tabIds.length; i++) {
            //TODO: add missing cases
            switch (tabIds[i]) {
                case cms_tab_types:
                    m_typesTab = new CmsTypesTab(new CmsTypesTabHandler(controller), m_dragHandler);
                    m_tabbedPanel.add(m_typesTab, Messages.get().key(Messages.GUI_TAB_TITLE_TYPES_0));
                    break;
                case cms_tab_galleries:
                    m_galleriesTab = new CmsGalleriesTab(new CmsGalleriesTabHandler(controller));
                    m_tabbedPanel.add(m_galleriesTab, Messages.get().key(Messages.GUI_TAB_TITLE_GALLERIES_0));
                    break;
                case cms_tab_categories:
                    m_categoriesTab = new CmsCategoriesTab(new CmsCategoriesTabHandler(controller));
                    m_tabbedPanel.add(m_categoriesTab, Messages.get().key(Messages.GUI_TAB_TITLE_CATEGORIES_0));
                    break;
                case cms_tab_search:
                    CmsSearchTab tabContent = new CmsSearchTab(new CmsSearchTabHandler(controller));
                    m_tabbedPanel.add(tabContent, Messages.get().key(Messages.GUI_TAB_TITLE_SEARCH_0));
                    break;
                case cms_tab_sitemap:
                    //TODO: add sitemap tree tab
                default:
                    break;
            }
        }
        m_resultsTab = new CmsResultsTab(new CmsResultsTabHandler(controller), m_dragHandler);
        m_tabbedPanel.addWithLeftMargin(m_resultsTab, Messages.get().key(Messages.GUI_TAB_TITLE_RESULTS_0));
        m_tabbedPanel.addBeforeSelectionHandler(this);
        m_tabbedPanel.addSelectionHandler(this);
    }

    /**
     * Returns the categories tab widget.<p>
     * 
     * @return the categories widget
     */
    public CmsCategoriesTab getCategoriesTab() {

        return m_categoriesTab;
    }

    /**
     * Returns the dialog element id.<p>
     *
     * @return the dialog element id
     */
    public String getDialogElementId() {

        return m_dialogElementId;
    }

    /**
     * Returns the galleries tab widget.<p>
     * 
     * @return the galleries widget
     */
    public CmsGalleriesTab getGalleriesTab() {

        return m_galleriesTab;
    }

    /**
     * Returns the parent panel of the dialog.<p>
     *
     * @return the parent
     */
    public FlowPanel getParentPanel() {

        return m_parentPanel;
    }

    /**
     * Returns the results tab widget.<p>
     * 
     * @return the results widget
     */
    public CmsResultsTab getResultsTab() {

        return m_resultsTab;
    }

    /**
     * Returns the types tab widget.<p>
     * 
     * @return the types widget
     */
    public CmsTypesTab getTypesTab() {

        return m_typesTab;
    }

    /**
     * @see com.google.gwt.event.logical.shared.BeforeSelectionHandler#onBeforeSelection(com.google.gwt.event.logical.shared.BeforeSelectionEvent)
     */
    public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {

        // event.cancel(), if the tab selection should be canceled, the tab will not be selected 
        // Integer index = event.getItem(); the index of the selected tab               
    }

    /**
     * 
     * @see com.google.gwt.event.logical.shared.ResizeHandler#onResize(com.google.gwt.event.logical.shared.ResizeEvent)
     */
    public void onResize(ResizeEvent event) {

        // TODO: implement
        int newHeight = event.getHeight();
        int newWidth = event.getWidth();

    }

    /**
     * @see com.google.gwt.event.logical.shared.SelectionHandler#onSelection(com.google.gwt.event.logical.shared.SelectionEvent)
     */
    public void onSelection(SelectionEvent<Integer> event) {

        int selectedIndex = m_tabbedPanel.getSelectedIndex();

        A_CmsTab tabWidget = m_tabbedPanel.getWidget(selectedIndex);
        if ((tabWidget instanceof CmsResultsTab) && m_isInitialSearch) {
            // no search here
            m_isInitialSearch = false;
        } else {
            tabWidget.onSelection();
        }
    }

    /**
     * Selects a tab by the given id.<p>
     * 
     * @param tabId the tab id
     */
    public void selectTab(GalleryTabId tabId) {

        Iterator<A_CmsTab> it = m_tabbedPanel.iterator();
        while (it.hasNext()) {
            A_CmsTab tab = it.next();
            if (tabId == tab.getTabId()) {
                m_tabbedPanel.selectTab(tab);
                break;
            }
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

    /**
     * Sets the size of the gallery parent panel and triggers the event to the tab.<p>
     * 
     * @param width the new width 
     * @param height the new height
     */
    public void setDialogSize(int width, int height) {

        if (height > DIALOG_HIGHT) {
            m_parentPanel.setHeight(Integer.toString(height - 2));
            m_parentPanel.setWidth(Integer.toString(width - 2));
            ResizeEvent.fire(this, width, height);
        }
    }
}