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

import org.opencms.ade.galleries.client.CmsCategoriesTabHandler;
import org.opencms.ade.galleries.client.CmsGalleriesTabHandler;
import org.opencms.ade.galleries.client.CmsGalleryController;
import org.opencms.ade.galleries.client.CmsResultsTabHandler;
import org.opencms.ade.galleries.client.CmsSearchTabHandler;
import org.opencms.ade.galleries.client.CmsSitemapTabHandler;
import org.opencms.ade.galleries.client.CmsTypesTabHandler;
import org.opencms.ade.galleries.client.CmsVfsTabHandler;
import org.opencms.ade.galleries.client.I_CmsGalleryHandler;
import org.opencms.ade.galleries.client.I_CmsGalleryWidgetHandler;
import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGalleryFolderBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsNotificationWidget;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabbedPanelStyle;
import org.opencms.gwt.client.ui.CmsToolbarPopup;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.I_CmsNotificationWidget;
import org.opencms.gwt.client.ui.I_CmsTruncable;
import org.opencms.gwt.client.util.CmsStyleVariable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Provides the method for the gallery dialog.<p>
 *
 * @since 8.0.
 */
public class CmsGalleryDialog extends Composite
implements BeforeSelectionHandler<Integer>, SelectionHandler<Integer>, I_CmsTruncable {

    /** The initial dialog width. */
    public static final int DEFAULT_DIALOG_HEIGHT = 486;

    /** The initial dialog width. */
    public static final int DEFAULT_DIALOG_WIDTH = 800;

    /** Text metrics key. */
    private static final String TM_GALLERY_DIALOG = "GalleryDialog";

    /** The parent panel for the gallery dialog. */
    protected FlowPanel m_parentPanel;

    /** The tabbed panel. */
    protected CmsTabbedPanel<A_CmsTab> m_tabbedPanel;

    /** The auto-hide parent to this dialog if present. */
    private I_CmsAutoHider m_autoHideParent;

    /** The categories tab. */
    private CmsCategoriesTab m_categoriesTab;

    /** The gallery controller. */
    private CmsGalleryController m_controller;

    /** The HTML id of the dialog element. */
    private String m_dialogElementId;

    /** The drag and drop handler. */
    private CmsDNDHandler m_dndHandler;

    /** The galleries tab. */
    private CmsGalleriesTab m_galleriesTab;

    /** The gallery handler. */
    private I_CmsGalleryHandler m_galleryHandler;

    /** The dialog height. */
    private int m_height;

    /** The image format names. */
    private String m_imageFormatNames;

    /** The image formats. */
    private String m_imageFormats;

    /** The flag for the initails search. */
    private boolean m_isInitialSearch;

    /** The command which should be executed when this widget is attached to the DOM. */
    private Command m_onAttachCommand;

    /** Flag which indicates that the formats from this object should have priority. */
    private boolean m_overrideFormats;

    /** The preview visible style. */
    private CmsStyleVariable m_previewVisibility;

    /** Flag indicating if the resource preview is visible. */
    private boolean m_previewVisible;

    /** The results tab. */
    private CmsResultsTab m_resultsTab;

    /** The Full-text search tab. */
    private CmsSearchTab m_searchTab;

    /** The sitemap tab. */
    private CmsSitemapTab m_sitemapTab;

    /** The types tab. */
    private CmsTypesTab m_typesTab;

    /** The use formats flag. */
    private boolean m_useFormats;

    /** The VFS folder tab. */
    private CmsVfsTab m_vfsTab;

    /** The widget handler. */
    private I_CmsGalleryWidgetHandler m_widgetHandler;

    /** The dialog width. */
    private int m_width;

    /**
     * The constructor.<p>
     *
     * @param galleryHandler the gallery handler
     */
    public CmsGalleryDialog(I_CmsGalleryHandler galleryHandler) {

        this(galleryHandler, CmsTabbedPanelStyle.buttonTabs);
    }

    /**
     * The default constructor for the gallery dialog.<p>
     *
     * @param galleryHandler the gallery handler
     * @param style the style for the panel
     */
    public CmsGalleryDialog(I_CmsGalleryHandler galleryHandler, CmsTabbedPanelStyle style) {

        initCss();
        m_height = DEFAULT_DIALOG_HEIGHT;
        m_width = DEFAULT_DIALOG_WIDTH;
        m_isInitialSearch = false;
        // parent widget
        m_parentPanel = new FlowPanel();
        m_parentPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().parentPanel());
        m_previewVisibility = new CmsStyleVariable(m_parentPanel);
        m_previewVisibility.setValue(I_CmsLayoutBundle.INSTANCE.previewDialogCss().hidePreview());
        m_dialogElementId = HTMLPanel.createUniqueId();
        m_parentPanel.getElement().setId(m_dialogElementId);
        // set the default height of the dialog
        m_parentPanel.getElement().getStyle().setHeight((m_height), Unit.PX);
        // tabs
        m_tabbedPanel = new CmsTabbedPanel<A_CmsTab>(style);
        // add tabs to parent widget
        m_parentPanel.add(m_tabbedPanel);
        //        m_showPreview = new CmsPushButton();
        //        m_showPreview.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_SHOW_0));
        //        m_showPreview.addStyleName(I_CmsLayoutBundle.INSTANCE.galleryDialogCss().showPreview());
        //        m_showPreview.addClickHandler(new ClickHandler() {
        //
        //            public void onClick(ClickEvent event) {
        //
        //                setPreviewVisible(true);
        //            }
        //        });
        //        m_showPreview.setVisible(false);
        //        m_parentPanel.add(m_showPreview);
        // All composites must call initWidget() in their constructors.
        initWidget(m_parentPanel);
        ensureNotifications();
        m_dndHandler = galleryHandler.getDndHandler();
        m_autoHideParent = galleryHandler.getAutoHideParent();
        m_galleryHandler = galleryHandler;
    }

    /**
     * Disables the search tab.<p>
     */
    public void disableSearchTab() {

        m_tabbedPanel.disableTab(m_resultsTab, Messages.get().key(Messages.GUI_GALLERY_NO_PARAMS_0));
    }

    /**
     * Enables the search tab.<p>
     */
    public void enableSearchTab() {

        m_tabbedPanel.enableTab(m_resultsTab);
    }

    /**
     * Displays the search result in the result tab.<p>
     *
     * @param searchObj the search object
     */
    public void fillResultTab(CmsGallerySearchBean searchObj) {

        if (m_resultsTab == null) {
            return;
        }
        List<CmsSearchParamPanel> paramPanels = null;
        if (!searchObj.isEmpty()) {
            enableSearchTab();
            paramPanels = new ArrayList<CmsSearchParamPanel>();
            Iterator<A_CmsTab> it = m_tabbedPanel.iterator();
            while (it.hasNext()) {
                A_CmsTab tab = it.next();
                paramPanels.addAll(tab.getParamPanels(searchObj));
            }
            m_resultsTab.fillContent(searchObj, paramPanels);
        }
    }

    /**
     * Fill the tabs with the content provided from the info bean. <p>
     *
     * @param controller the reference to the gallery controller
     */
    public void fillTabs(CmsGalleryController controller) {

        m_controller = controller;
        GalleryTabId[] tabIds = m_controller.getTabIds();

        int i;
        for (i = 0; i < tabIds.length; i++) {
            switch (tabIds[i]) {
                case cms_tab_types:
                    m_typesTab = new CmsTypesTab(
                        new CmsTypesTabHandler(controller),
                        m_dndHandler,
                        m_galleryHandler.getAdditionalTypeTabControl());
                    m_typesTab.setTabTextAccessor(getTabTextAccessor(i));
                    m_tabbedPanel.add(m_typesTab, Messages.get().key(Messages.GUI_TAB_TITLE_TYPES_0));
                    break;
                case cms_tab_galleries:
                    List<CmsGalleryFolderBean> availableGalleries = controller.getAvailableGalleries();
                    if ((availableGalleries != null) && availableGalleries.isEmpty()) {
                        continue;
                    }
                    m_galleriesTab = new CmsGalleriesTab(new CmsGalleriesTabHandler(controller));
                    m_galleriesTab.setTabTextAccessor(getTabTextAccessor(i));
                    m_tabbedPanel.add(m_galleriesTab, Messages.get().key(Messages.GUI_TAB_TITLE_GALLERIES_0));
                    break;
                case cms_tab_categories:
                    m_categoriesTab = new CmsCategoriesTab(new CmsCategoriesTabHandler(controller));
                    m_categoriesTab.setTabTextAccessor(getTabTextAccessor(i));
                    m_tabbedPanel.add(m_categoriesTab, Messages.get().key(Messages.GUI_TAB_TITLE_CATEGORIES_0));
                    break;
                case cms_tab_search:
                    m_searchTab = new CmsSearchTab(
                        new CmsSearchTabHandler(controller),
                        m_autoHideParent,
                        m_controller.getStartLocale(),
                        m_controller.getAvailableLocales(),
                        m_controller.getSearchScope(),
                        m_controller.getDefaultScope(),
                        m_controller.getShowExpiredDefault());
                    m_searchTab.enableExpiredResourcesSearch(true);
                    m_searchTab.setTabTextAccessor(getTabTextAccessor(i));
                    m_tabbedPanel.add(m_searchTab, Messages.get().key(Messages.GUI_TAB_TITLE_SEARCH_0));
                    break;
                case cms_tab_vfstree:
                    CmsVfsTabHandler vfsTabHandler = new CmsVfsTabHandler(controller);
                    m_vfsTab = new CmsVfsTab(vfsTabHandler, controller.isIncludeFiles());
                    vfsTabHandler.setTab(m_vfsTab);
                    m_vfsTab.setTabTextAccessor(getTabTextAccessor(i));
                    m_tabbedPanel.add(m_vfsTab, Messages.get().key(Messages.GUI_TAB_TITLE_VFS_0));
                    break;
                case cms_tab_sitemap:
                    CmsSitemapTabHandler sitemapTabHandler = new CmsSitemapTabHandler(controller);
                    m_sitemapTab = new CmsSitemapTab(sitemapTabHandler);
                    m_sitemapTab.setTabTextAccessor(getTabTextAccessor(i));
                    m_tabbedPanel.add(m_sitemapTab, "Sitemap");
                    break;
                case cms_tab_results:
                    m_resultsTab = new CmsResultsTab(
                        new CmsResultsTabHandler(controller),
                        m_dndHandler,
                        m_galleryHandler);
                    m_resultsTab.setTabTextAccessor(getTabTextAccessor(i));
                    m_tabbedPanel.addWithLeftMargin(m_resultsTab, Messages.get().key(Messages.GUI_TAB_TITLE_RESULTS_0));
                    disableSearchTab();
                    break;
                default:
                    break;
            }
        }
        m_tabbedPanel.addBeforeSelectionHandler(this);
        m_tabbedPanel.addSelectionHandler(this);
        truncateTabs();
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
     * Returns the gallery controller.<p>
     *
     * @return the gallery controller
     */
    public CmsGalleryController getController() {

        return m_controller;
    }

    /**
     * Returns the HTML id of the dialog element.<p>
     *
     * @return the HTML id of the dialog element
     */
    public String getDialogId() {

        return m_dialogElementId;
    }

    /**
     * Returns the drag and drop handler.<p>
     *
     * @return the drag and drop handler
     */
    public CmsDNDHandler getDndHandler() {

        return m_dndHandler;
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
     * Returns the image format names.<p>
     *
     * @return the image format names
     */
    public String getImageFormatNames() {

        return m_imageFormatNames;
    }

    /**
     * Returns the image formats.<p>
     *
     * @return the image formats
     */
    public String getImageFormats() {

        return m_imageFormats;
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
     * Returns the searchTab.<p>
     *
     * @return the searchTab
     */
    public CmsSearchTab getSearchTab() {

        return m_searchTab;
    }

    /**
     * Returns the sitemap tab.<p>
     *
     * @return the sitemap tab
     */
    public CmsSitemapTab getSitemapTab() {

        return m_sitemapTab;
    }

    /**
     * Gets the tab with a given tab id, or null if the dialog has no such tab.<p>
     *
     * @param tabId the tab id to look for
     *
     * @return the tab with the given tab id, or null
     */
    public A_CmsTab getTab(GalleryTabId tabId) {

        for (A_CmsTab tab : m_tabbedPanel) {
            if (tabId == GalleryTabId.valueOf(tab.getTabId())) {
                return tab;
            }
        }
        return null;
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
     * Returns the VFS tab widget.<p>
     *
     * @return the VFS tab widget
     */
    public CmsVfsTab getVfsTab() {

        return m_vfsTab;
    }

    /**
     * Returns the widget handler.<p>
     *
     * @return the widget handler
     */
    public I_CmsGalleryWidgetHandler getWidgetHandler() {

        return m_widgetHandler;
    }

    /**
     * Hides or shows the show-preview-button.<p>
     *
     * @param hide <code>true</code> to hide the button
     */
    public void hideShowPreviewButton(boolean hide) {

        //  m_showPreview.setVisible(!hide);
    }

    /**
     * Returns if the gallery is used as a native widget.<p>
     *
     * @return <code>true</code> if the gallery is used as a native widget
     */
    public boolean isNativeWidget() {

        return m_widgetHandler != null;
    }

    /**
     * Returns true if the formats from this dialog object should be prioritized by the format handler.<p>
     *
     * @return the value of the 'override formats' flag
     */
    public boolean isOverrideFormats() {

        return m_overrideFormats;
    }

    /**
     * Returns the use formats flag.<p>
     *
     * @return the use formats flag
     */
    public boolean isUseFormats() {

        return m_useFormats;
    }

    /**
     * @see com.google.gwt.event.logical.shared.BeforeSelectionHandler#onBeforeSelection(com.google.gwt.event.logical.shared.BeforeSelectionEvent)
     */
    public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {

        int selectedIndex = m_tabbedPanel.getSelectedIndex();
        int newIndex = event.getItem().intValue();
        if (m_tabbedPanel.isDisabledTab(newIndex)) {
            event.cancel();
            return;
        }
        if (selectedIndex != newIndex) {
            m_tabbedPanel.getWidget(selectedIndex).onDeselection();
        }
    }

    /**
     * @see com.google.gwt.event.logical.shared.SelectionHandler#onSelection(com.google.gwt.event.logical.shared.SelectionEvent)
     */
    public void onSelection(SelectionEvent<Integer> event) {

        int selectedIndex = m_tabbedPanel.getSelectedIndex();

        final A_CmsTab tabWidget = m_tabbedPanel.getWidget(selectedIndex);
        if ((tabWidget instanceof CmsResultsTab) && m_isInitialSearch) {
            // no search here
            m_isInitialSearch = false;
        } else {
            tabWidget.onSelection();
        }
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                updateSizeForTab(tabWidget);
            }
        });

    }

    /**
     * Selects a tab by the given id.<p>
     *
     * @param tabId the tab id
     * @param fireEvent <code>true</code> to fire the tab event
     */
    public void selectTab(GalleryTabId tabId, boolean fireEvent) {

        A_CmsTab tab = getTab(tabId);
        if (tab != null) {
            m_tabbedPanel.selectTab(tab, fireEvent);
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

        if (height > 50) {
            m_height = height;
            m_parentPanel.setHeight(m_height + "px");
        }
        m_width = width;
        m_parentPanel.setWidth(m_width + "px");
        truncateTabs();
    }

    /**
     * Sets the image format names.<p>
     *
     * @param imageFormatNames the image format names to set
     */
    public void setImageFormatNames(String imageFormatNames) {

        m_imageFormatNames = imageFormatNames;
    }

    /**
     * Sets the image formats.<p>
     *
     * @param imageFormats the image formats to set
     */
    public void setImageFormats(String imageFormats) {

        m_imageFormats = imageFormats;
    }

    /**
     * Sets the on attach command.<p>
     *
     * @param onAttachCommand the on attach command to set
     */
    public void setOnAttachCommand(Command onAttachCommand) {

        m_onAttachCommand = onAttachCommand;
    }

    /**
     * Sets the 'override formats' flag, which tells the format handler to prioritize the formats from the gallery dialog object.<p>
     *
     * @param overrideFormats the new value for the 'override formats' flag
     */
    public void setOverrideFormats(boolean overrideFormats) {

        m_overrideFormats = overrideFormats;
    }

    /**
     * Sets the preview visibility.<p>
     *
     * @param visible the preview visibility
     */
    public void setPreviewVisible(boolean visible) {

        m_previewVisible = visible;
        if (m_previewVisible) {
            useMaxDimensions();
            m_previewVisibility.setValue(I_CmsLayoutBundle.INSTANCE.previewDialogCss().previewVisible());
            if (m_galleryHandler instanceof CmsGalleryPopup) {
                ((CmsGalleryPopup)m_galleryHandler).center();
            }
        } else {
            m_previewVisibility.setValue(I_CmsLayoutBundle.INSTANCE.previewDialogCss().hidePreview());
            updateSizes();
        }
    }

    /**
     * Sets the use formats flag.<p>
     *
     * @param useFormats the use formats flag to set
     */
    public void setUseFormats(boolean useFormats) {

        m_useFormats = useFormats;
    }

    /**
     * Sets the widget handler.<p>
     *
     * @param widgetHandler the widget handler
     */
    public void setWidgetHandler(I_CmsGalleryWidgetHandler widgetHandler) {

        m_widgetHandler = widgetHandler;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int clientWidth) {

        m_width = clientWidth;
        m_parentPanel.setWidth(m_width + "px");
        truncateTabs();
    }

    /**
     * Updates the gallery data.<p>
     *
     * @param data the gallery data
     */
    public void updateGalleryData(CmsGalleryDataBean data) {

        m_controller.updateGalleryData(data);
    }

    /**
     * Updates the dialog size according to the requirements of the selected tab.<p>
     *
     * @param tab the selected tab
     */
    public void updateSizeForTab(A_CmsTab tab) {

        if (tab == m_resultsTab) {
            m_resultsTab.updateListSize();
        }
        if (!m_previewVisible) {
            int height = tab.getRequiredHeight() + 42;
            int availableHeight = CmsToolbarPopup.getAvailableHeight();
            setDialogSize(m_width, height < availableHeight ? height : availableHeight);
            tab.onResize();
        }
    }

    /**
     * Updates variable ui-element dimensions, execute after dialog has been attached and it's content is displayed.<p>
     */
    public void updateSizes() {

        int tabIndex = m_tabbedPanel.getSelectedIndex();
        if (tabIndex >= 0) {
            updateSizeForTab(m_tabbedPanel.getWidget(tabIndex));
        }
    }

    /**
     * Sets the dialog to use the maximum available space.<p>
     */
    public void useMaxDimensions() {

        int availableHeight = CmsToolbarPopup.getAvailableHeight();
        int availableWidth = CmsToolbarPopup.getAvailableWidth();
        setDialogSize(availableWidth, availableHeight);
    }

    /**
     * Make sure a notification widget is installed.<p>
     */
    protected void ensureNotifications() {

        I_CmsNotificationWidget oldWidget = CmsNotification.get().getWidget();
        if (oldWidget == null) {
            CmsNotificationWidget newWidget = new CmsNotificationWidget();
            CmsNotification.get().setWidget(newWidget);
            RootPanel.get().add(newWidget);
        }
    }

    /**
     * Creates a tab text accessor for a given text.<p>
     *
     * @param pos the index of the tab
     *
     * @return the tab text accessor for the tab at index pos
     */
    protected HasText getTabTextAccessor(final int pos) {

        HasText tabText = new HasText() {

            /**
             * @see com.google.gwt.user.client.ui.HasText#getText()
             */
            public String getText() {

                return m_tabbedPanel.getTabText(pos);

            }

            /**
             * @see com.google.gwt.user.client.ui.HasText#setText(java.lang.String)
             */
            public void setText(String text) {

                m_tabbedPanel.setTabText(pos, text);
            }
        };
        return tabText;
    }

    /**
     * @see com.google.gwt.user.client.ui.Composite#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
        if (m_onAttachCommand != null) {
            m_onAttachCommand.execute();
            m_onAttachCommand = null;
        }
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                updateSizes();
            }
        });
    }

    /**
     * Ensures all style sheets are loaded.<p>
     */
    private void initCss() {

        I_CmsLayoutBundle.INSTANCE.galleryDialogCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.galleryResultItemCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.listTreeCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.previewDialogCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.croppingDialogCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.imageEditorFormCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.imageAdvancedFormCss().ensureInjected();
    }

    /**
     * Truncates the gallery tabs to the available width.<p>
     */
    private void truncateTabs() {

        for (int i = 0; i < m_tabbedPanel.getTabCount(); i++) {
            if (m_tabbedPanel.getWidget(i) instanceof I_CmsTruncable) {
                I_CmsTruncable tab = (I_CmsTruncable)m_tabbedPanel.getWidget(i);
                tab.truncate(TM_GALLERY_DIALOG, m_width - 50);
            }
        }
    }
}
