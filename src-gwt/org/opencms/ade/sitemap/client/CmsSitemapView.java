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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.client.control.CmsSitemapChangeEvent;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.client.control.CmsSitemapDNDController;
import org.opencms.ade.sitemap.client.control.CmsSitemapLoadEvent;
import org.opencms.ade.sitemap.client.control.I_CmsSitemapChangeHandler;
import org.opencms.ade.sitemap.client.control.I_CmsSitemapLoadHandler;
import org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry;
import org.opencms.ade.sitemap.client.hoverbar.CmsChangeCategoryMenuEntry;
import org.opencms.ade.sitemap.client.hoverbar.CmsCreateCategoryMenuEntry;
import org.opencms.ade.sitemap.client.hoverbar.CmsCreateCategoryMenuEntry.CmsCategoryTitleAndName;
import org.opencms.ade.sitemap.client.hoverbar.CmsDeleteCategoryMenuEntry;
import org.opencms.ade.sitemap.client.hoverbar.CmsHoverbarCreateGalleryButton;
import org.opencms.ade.sitemap.client.hoverbar.CmsHoverbarCreateModelPageButton;
import org.opencms.ade.sitemap.client.hoverbar.CmsSitemapHoverbar;
import org.opencms.ade.sitemap.client.hoverbar.I_CmsContextMenuItemProvider;
import org.opencms.ade.sitemap.client.toolbar.CmsSitemapToolbar;
import org.opencms.ade.sitemap.client.ui.CmsStatusIconUpdateHandler;
import org.opencms.ade.sitemap.client.ui.css.I_CmsImageBundle;
import org.opencms.ade.sitemap.client.ui.css.I_CmsSitemapLayoutBundle;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsDetailPageTable;
import org.opencms.ade.sitemap.shared.CmsGalleryFolderEntry;
import org.opencms.ade.sitemap.shared.CmsGalleryType;
import org.opencms.ade.sitemap.shared.CmsModelInfo;
import org.opencms.ade.sitemap.shared.CmsModelPageEntry;
import org.opencms.ade.sitemap.shared.CmsSitemapCategoryData;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.CmsSitemapData.EditorMode;
import org.opencms.ade.sitemap.shared.CmsSitemapInfo;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.CmsBroadcastTimer;
import org.opencms.gwt.client.dnd.CmsDNDHandler;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.CmsInfoHeader;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsListItemWidget.Background;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsListItem;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.tree.CmsLazyTree;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem;
import org.opencms.gwt.client.ui.tree.CmsTree;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsJsUtil;
import org.opencms.gwt.client.util.CmsScriptCallbackHelper;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.gwt.shared.CmsCategoryBean;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sitemap editor.<p>
 *
 * @since 8.0.0
 */
public final class CmsSitemapView extends A_CmsEntryPoint
implements I_CmsSitemapChangeHandler, I_CmsSitemapLoadHandler {

    /**
     * The sitemap tree open handler.<p>
     */
    protected class TreeOpenHandler implements I_CmsLazyOpenHandler<CmsSitemapTreeItem> {

        /** Flag indicating the tree is initializing. */
        private boolean m_initializing;

        /**
         * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#load(org.opencms.gwt.client.ui.tree.CmsLazyTreeItem, java.lang.Runnable)
         */
        public void load(final CmsSitemapTreeItem target, Runnable loadCallback) {

            // not used
        }

        /**
         * @see org.opencms.gwt.client.ui.tree.I_CmsLazyOpenHandler#onOpen(com.google.gwt.event.logical.shared.OpenEvent)
         */
        public void onOpen(OpenEvent<CmsSitemapTreeItem> event) {

            CmsSitemapTreeItem target = event.getTarget();
            if ((target.getLoadState() == CmsLazyTreeItem.LoadState.UNLOADED)) {
                target.onStartLoading();
                target.setOpen(false);
                getController().getChildren(target.getEntryId(), true, null);
            } else if (!m_initializing
                && ((target.getChildren().getWidgetCount() > 0)
                    && (((CmsSitemapTreeItem)target.getChild(
                        0)).getLoadState() == CmsLazyTreeItem.LoadState.UNLOADED))) {
                            // load grand children in advance
                            getController().getChildren(target.getEntryId(), false, null);
                        }
        }

        /**
         * Sets the initializing flag.<p>
         *
         * @param initializing the initializing flag
         */
        protected void setInitializing(boolean initializing) {

            m_initializing = initializing;
        }
    }

    /** The download gallery type name. */
    public static final String DOWNLOAD_GALLERY_TYPE = "downloadgallery";

    /** The image gallery type name. */
    public static final String IMAGE_GALLERY_TYPE = "imagegallery";

    /** The singleton instance. */
    private static CmsSitemapView m_instance;

    /** Text metrics key. */
    private static final String TM_SITEMAP = "Sitemap";

    /** The displayed sitemap tree. */
    protected CmsLazyTree<CmsSitemapTreeItem> m_tree;

    /** The tree shown in category mode. */
    private CmsTree<CmsTreeItem> m_categoryTree;

    /** The controller. */
    private CmsSitemapController m_controller;

    /** The current sitemap editor mode. */
    private EditorMode m_editorMode;

    /** The gallery tree widget. */
    private CmsTree<CmsGalleryTreeItem> m_galleryTree;

    /** The gallery folder items by id. */
    private Map<CmsUUID, CmsGalleryTreeItem> m_galleryTreeItems;

    /** The gallery type items by type name. */
    private Map<String, CmsGalleryTreeItem> m_galleryTypeItems;

    /** The header. */
    private CmsInfoHeader m_header;

    /** The header container. */
    private FlowPanel m_headerContainer;

    /** Style variable which keeps track of whether we are in VFS mode or navigation mode. */
    private CmsStyleVariable m_inNavigationStyle;

    /** The locale comparison view. */
    private FlowPanel m_localeComparison = new FlowPanel();

    /** The model group pages root entry. */
    private CmsModelPageTreeItem m_modelGroupRoot;

    /** Model page data beans for the model page mode. */
    private Map<CmsUUID, CmsModelPageEntry> m_modelPageData = new HashMap<CmsUUID, CmsModelPageEntry>();

    /** Root tree item for the model page mode. */
    private CmsModelPageTreeItem m_modelPageRoot;

    /** Tree for the model page editor mode. */
    private CmsTree<CmsModelPageTreeItem> m_modelPageTree;

    /** The tree items for the model page mode, indexed by structure id. */
    private Map<CmsUUID, CmsModelPageTreeItem> m_modelPageTreeItems = new HashMap<CmsUUID, CmsModelPageTreeItem>();

    /** Flag to indicate that the tree needs to be refreshed. */
    private boolean m_needTreeRefresh;

    /** Label to display to no galleries in this sub site message. */
    private Label m_noGalleriesLabel;

    /** The tree open handler. */
    private TreeOpenHandler m_openHandler;

    /** The page. */
    private FlowPanel m_page;

    /** The parent model page root. */
    private CmsModelPageTreeItem m_parentModelPageRoot;

    /** The parent model page entries. */
    private Map<CmsUUID, CmsModelPageTreeItem> m_parentModelPageTreeItems = new HashMap<CmsUUID, CmsModelPageTreeItem>();

    /** The sitemap tree root item. */
    private CmsSitemapTreeItem m_rootItem;

    /** The sitemap toolbar. */
    private CmsSitemapToolbar m_toolbar;

    /** The registered tree items. */
    private Map<CmsUUID, CmsSitemapTreeItem> m_treeItems;

    /**
     * Returns the instance.<p>
     *
     * @return the instance
     */
    public static CmsSitemapView getInstance() {

        return m_instance;
    }

    /**
     * Creates a new tree item from the given sitemap entry.<p>
     *
     * @param entry the sitemap entry
     *
     * @return the new created (still orphan) tree item
     */
    public CmsSitemapTreeItem create(CmsClientSitemapEntry entry) {

        CmsSitemapTreeItem treeItem = new CmsSitemapTreeItem(entry);
        CmsSitemapHoverbar.installOn(m_controller, treeItem, entry.getId());
        // highlight the open path
        if (isLastPage(entry)) {
            treeItem.setBackgroundColor(Background.YELLOW);
        }
        m_treeItems.put(entry.getId(), treeItem);
        return treeItem;
    }

    /**
     * Creates a sitemap tree item from a client sitemap entry.<p>
     *
     * @param entry the entry from which the sitemap tree item should be created
     *
     * @return the new sitemap tree item
     */
    public CmsSitemapTreeItem createSitemapItem(CmsClientSitemapEntry entry) {

        CmsSitemapTreeItem result = create(entry);
        result.clearChildren();
        for (CmsClientSitemapEntry child : entry.getSubEntries()) {
            CmsSitemapTreeItem childItem = createSitemapItem(child);
            if (!entry.isSubSitemapType()) {
                result.addChild(childItem);
            }
        }
        if (entry.getChildrenLoadedInitially()) {
            result.onFinishLoading();
        }
        return result;
    }

    /**
     * Displays the category data.<p>
     *
     * @param categoryData the category data
     * @param openLocalCategories true if the local category tree should be opened
     * @param openItemId the id of the item to open
     */
    public void displayCategoryData(
        CmsSitemapCategoryData categoryData,
        final boolean openLocalCategories,
        CmsUUID openItemId) {

        final Set<CmsUUID> openIds = new HashSet<CmsUUID>();
        if (openLocalCategories) {
            if (openItemId != null) {
                openIds.add(openItemId);
            }
            for (Widget item : m_categoryTree) {
                if (item instanceof CmsTreeItem) {
                    ((CmsTreeItem)item).visit(new Function<CmsTreeItem, Boolean>() {

                        public Boolean apply(CmsTreeItem input) {

                            if (input.isOpen() && (input instanceof CmsCategoryTreeItem)) {
                                openIds.add(((CmsCategoryTreeItem)input).getStructureId());
                            }
                            return null;
                        }
                    });
                }
            }
        }

        m_categoryTree.clear();

        Multimap<Boolean, CmsCategoryTreeEntry> entries = categoryData.getEntriesIndexedByLocality();
        String localLabel = Messages.get().key(Messages.GUI_CATEGORIES_LOCAL_0);
        String nonlocalLabel = Messages.get().key(Messages.GUI_CATEGORIES_NONLOCAL_0);

        String localSubtitle = Messages.get().key(Messages.GUI_CATEGORIES_LOCAL_SUBTITLE_0);
        String nonlocalSubtitle = Messages.get().key(Messages.GUI_CATEGORIES_NONLOCAL_SUBTITLE_0);

        CmsListInfoBean localRootInfo = new CmsListInfoBean(localLabel, localSubtitle, null);
        localRootInfo.setResourceType("category");
        localRootInfo.setBigIconClasses(CmsCategoryBean.BIG_ICON_CLASSES);
        final CmsTreeItem localRoot = new CmsTreeItem(true, new CmsListItemWidget(localRootInfo));

        CmsListInfoBean nonlocalRootInfo = new CmsListInfoBean(nonlocalLabel, nonlocalSubtitle, null);
        nonlocalRootInfo.setResourceType("category");
        nonlocalRootInfo.setBigIconClasses(CmsCategoryBean.BIG_ICON_CLASSES);
        final CmsTreeItem nonlocalRoot = new CmsTreeItem(true, new CmsListItemWidget(nonlocalRootInfo));

        m_categoryTree.add(nonlocalRoot);
        m_categoryTree.add(localRoot);

        for (Boolean isLocal : Arrays.asList(Boolean.FALSE, Boolean.TRUE)) {
            for (CmsCategoryTreeEntry entry : entries.get(isLocal)) {
                CmsTreeItem treeItem = createCategoryTreeItem(entry);
                (isLocal.booleanValue() ? localRoot : nonlocalRoot).addChild(treeItem);
            }
        }

        for (CmsTreeItem root : Arrays.asList(localRoot, nonlocalRoot)) {
            final CmsTreeItem finalRoot = root;

            root.visit(new Function<CmsTreeItem, Boolean>() {

                @SuppressWarnings("synthetic-access")
                public Boolean apply(CmsTreeItem input) {

                    CmsUUID id = null;
                    if (input == localRoot) {
                        id = CmsUUID.getNullUUID();
                    } else if ((input instanceof CmsCategoryTreeItem) && (localRoot == finalRoot)) {
                        id = ((CmsCategoryTreeItem)input).getStructureId();

                    }

                    if ((id != null) && m_controller.isEditable()) {
                        final CmsSitemapHoverbar hoverbar = CmsSitemapHoverbar.installOn(
                            m_controller,
                            input,
                            id,
                            false,
                            !(id.isNullUUID()),
                            new I_CmsContextMenuItemProvider() {

                                public List<A_CmsSitemapMenuEntry> createContextMenu(CmsSitemapHoverbar hoverbar2) {

                                    List<A_CmsSitemapMenuEntry> result = Lists.newArrayList();

                                    result.add(new CmsChangeCategoryMenuEntry(hoverbar2));
                                    result.add(new CmsDeleteCategoryMenuEntry(hoverbar2));
                                    return result;
                                }
                            });
                        if (input == localRoot) {
                            hoverbar.setAlwaysVisible();
                        }
                        CmsPushButton newButton = new CmsPushButton();
                        newButton.setTitle(Messages.get().key(Messages.GUI_SITEMAP_CONTEXT_MENU_CREATE_CATEGORY_0));
                        newButton.setImageClass(I_CmsButton.ADD_SMALL);
                        newButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
                        hoverbar.add(newButton);
                        newButton.addClickHandler(new ClickHandler() {

                            /**
                             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
                             */
                            public void onClick(ClickEvent event) {

                                hoverbar.hide();
                                final CmsSitemapController controller = hoverbar.getController();
                                final CmsUUID hoverbarId = hoverbar.getId();
                                CmsCreateCategoryMenuEntry.askForNewCategoryInfo(
                                    hoverbarId,
                                    new AsyncCallback<CmsCategoryTitleAndName>() {

                                        public void onFailure(Throwable caught) {

                                            // do nothing
                                        }

                                        public void onSuccess(CmsCategoryTitleAndName result) {

                                            controller.createCategory(hoverbarId, result.getTitle(), result.getName());
                                        }
                                    });
                            }
                        });
                    }
                    if (((finalRoot == localRoot) && openLocalCategories && openIds.contains(id))
                        || (input == nonlocalRoot)
                        || (input == localRoot)) {
                        input.setOpen(true);
                    }
                    return null;
                }
            });
        }
    }

    /**
     * Displays the gallery view.<p>
     *
     * @param galleries the gallery data
     */
    public void displayGalleries(Map<CmsGalleryType, List<CmsGalleryFolderEntry>> galleries) {

        m_galleryTree.clear();
        m_galleryTreeItems.clear();
        m_galleryTypeItems.clear();
        CmsUUID galleriesFolderId = null;
        if (getRootItem().getChild(m_controller.getData().getDefaultGalleryFolder()) != null) {
            galleriesFolderId = ((CmsSitemapTreeItem)getRootItem().getChild(
                m_controller.getData().getDefaultGalleryFolder())).getEntryId();
        }
        List<CmsGalleryType> types = new ArrayList<CmsGalleryType>(galleries.keySet());
        Collections.sort(types, new Comparator<CmsGalleryType>() {

            public int compare(CmsGalleryType o1, CmsGalleryType o2) {

                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        m_toolbar.setGalleryTypes(types);
        boolean hasGalleries = false;
        for (CmsGalleryType type : types) {
            CmsGalleryTreeItem typeItem = new CmsGalleryTreeItem(type);
            if (m_controller.isEditable() && m_controller.getData().isGalleryManager()) {
                CmsHoverbarCreateGalleryButton createButton = new CmsHoverbarCreateGalleryButton(
                    type.getTypeId(),
                    galleriesFolderId);
                CmsSitemapHoverbar hoverbar = CmsSitemapHoverbar.installOn(
                    m_controller,
                    typeItem,
                    Collections.<Widget> singleton(createButton));
                createButton.setHoverbar(hoverbar);
                hoverbar.setAlwaysVisible();
            }
            m_galleryTypeItems.put(type.getResourceType(), typeItem);
            addGalleryEntries(typeItem, galleries.get(type));
            typeItem.setOpen(true);
            hasGalleries = true;
            m_galleryTree.addItem(typeItem);
        }
        // position image and download galleries at the top
        if (m_galleryTypeItems.containsKey(DOWNLOAD_GALLERY_TYPE)) {
            m_galleryTree.insertItem(m_galleryTypeItems.get(DOWNLOAD_GALLERY_TYPE), 0);
        }
        if (m_galleryTypeItems.containsKey(IMAGE_GALLERY_TYPE)) {
            m_galleryTree.insertItem(m_galleryTypeItems.get(IMAGE_GALLERY_TYPE), 0);
        }
        m_galleryTree.truncate(TM_SITEMAP, 920);
        if (hasGalleries) {
            m_noGalleriesLabel.getElement().getStyle().setDisplay(Display.NONE);
        } else {
            m_noGalleriesLabel.getElement().getStyle().clearDisplay();
        }
    }

    /**
     * Displays the model page data in a tree.<p>
     *
     * @param modelPageData the model page data
     */
    public void displayModelPages(CmsModelInfo modelPageData) {

        m_modelPageTree.clear();
        m_modelPageTreeItems.clear();
        m_parentModelPageTreeItems.clear();

        if ((modelPageData.getParentModelPages() != null) && !modelPageData.getParentModelPages().isEmpty()) {
            m_parentModelPageRoot = CmsModelPageTreeItem.createRootItem(
                false,
                Messages.get().key(Messages.GUI_PARENT_MODEL_PAGE_TREE_ROOT_TITLE_0),
                "");

            for (CmsModelPageEntry parentModel : modelPageData.getParentModelPages()) {
                if (parentModel.isDisabled()) {
                    continue;
                }
                CmsModelPageTreeItem treeItem = new CmsModelPageTreeItem(parentModel, false, true);
                if (m_controller.isEditable()) {
                    CmsSitemapHoverbar.installOn(
                        m_controller,
                        treeItem,
                        parentModel.getStructureId(),
                        parentModel.getSitePath(),
                        parentModel.getSitePath() != null);
                }
                m_parentModelPageTreeItems.put(parentModel.getStructureId(), treeItem);
                m_parentModelPageRoot.addChild(treeItem);
                m_modelPageData.put(parentModel.getStructureId(), parentModel);
            }
            if (m_parentModelPageRoot.getChildren().getWidgetCount() > 0) {
                m_modelPageTree.add(m_parentModelPageRoot);
            } else {
                m_parentModelPageRoot = null;
            }
        } else {
            m_parentModelPageRoot = null;
        }
        m_modelPageRoot = CmsModelPageTreeItem.createRootItem(
            false,
            Messages.get().key(Messages.GUI_MODEL_PAGE_TREE_ROOT_TITLE_0),
            Messages.get().key(Messages.GUI_MODEL_PAGE_TREE_ROOT_SUBTITLE_0));
        if (m_controller.isEditable()) {
            CmsHoverbarCreateModelPageButton createButton = new CmsHoverbarCreateModelPageButton(false);
            CmsSitemapHoverbar hoverbar = CmsSitemapHoverbar.installOn(
                m_controller,
                m_modelPageRoot,
                Collections.<Widget> singleton(createButton));
            hoverbar.setAlwaysVisible();
            createButton.setHoverbar(hoverbar);
        }
        m_modelPageTree.add(m_modelPageRoot);
        for (CmsModelPageEntry entry : modelPageData.getModelPages()) {
            if (m_parentModelPageTreeItems.containsKey(entry.getStructureId())) {
                CmsModelPageTreeItem item = m_parentModelPageTreeItems.get(entry.getStructureId());
                item.setDisabled(entry.isDisabled());
            } else {
                m_modelPageData.put(entry.getStructureId(), entry);
                CmsModelPageTreeItem treeItem = new CmsModelPageTreeItem(entry, false, false);
                if (m_controller.isEditable()) {
                    CmsSitemapHoverbar.installOn(
                        m_controller,
                        treeItem,
                        entry.getStructureId(),
                        entry.getSitePath(),
                        entry.getSitePath() != null);
                }
                m_modelPageTreeItems.put(entry.getStructureId(), treeItem);
                m_modelPageRoot.addChild(treeItem);
            }
        }
        m_modelPageRoot.setOpen(true);
        m_modelGroupRoot = CmsModelPageTreeItem.createRootItem(
            true,
            Messages.get().key(Messages.GUI_MODEL_GROUP_PAGE_TREE_ROOT_TITLE_0),
            Messages.get().key(Messages.GUI_MODEL_GROUP_PAGE_TREE_ROOT_SUBTITLE_0));
        if (m_controller.isEditable()) {
            CmsHoverbarCreateModelPageButton createModelGroupButton = new CmsHoverbarCreateModelPageButton(true);
            CmsSitemapHoverbar modelGroupHoverbar = CmsSitemapHoverbar.installOn(
                m_controller,
                m_modelGroupRoot,
                Collections.<Widget> singleton(createModelGroupButton));
            modelGroupHoverbar.setAlwaysVisible();
            createModelGroupButton.setHoverbar(modelGroupHoverbar);
        }
        m_modelPageTree.add(m_modelGroupRoot);
        for (CmsModelPageEntry entry : modelPageData.getModelGroups()) {
            CmsModelPageTreeItem treeItem = new CmsModelPageTreeItem(entry, true, false);
            if (m_controller.isEditable()) {
                CmsSitemapHoverbar.installOn(
                    m_controller,
                    treeItem,
                    entry.getStructureId(),
                    entry.getSitePath(),
                    entry.getSitePath() != null);
            }
            m_modelPageTreeItems.put(entry.getStructureId(), treeItem);
            m_modelGroupRoot.addChild(treeItem);
        }
        m_modelGroupRoot.setOpen(true);
        m_modelPageTree.truncate(TM_SITEMAP, 920);
    }

    /**
     * Displays a newly created gallery folder.<p>
     *
     * @param galleryFolder the gallery folder
     */
    public void displayNewGallery(CmsGalleryFolderEntry galleryFolder) {

        String parent = CmsResource.getParentFolder(galleryFolder.getSitePath());
        CmsSitemapTreeItem parentItem = getTreeItem(parent);
        if (parentItem != null) {
            CmsUUID parentId = parentItem.getEntryId();
            m_controller.updateEntry(parentId);
        } else {
            m_controller.loadPath(parent);
        }
        CmsGalleryTreeItem typeItem = m_galleryTypeItems.get(galleryFolder.getResourceType());
        CmsGalleryTreeItem folderItem = createGalleryFolderItem(galleryFolder);
        if ((parentItem != null) && m_galleryTreeItems.containsKey(parentItem.getEntryId())) {
            CmsGalleryTreeItem galleryParent = m_galleryTreeItems.get(parentItem.getEntryId());
            galleryParent.addChild(folderItem);
            galleryParent.setOpen(true);
        } else {
            typeItem.addChild(folderItem);
        }
        m_galleryTreeItems.put(galleryFolder.getStructureId(), folderItem);
        typeItem.setOpen(true);
        // in case the type item had been hidden
        typeItem.getElement().getStyle().clearDisplay();
        m_noGalleriesLabel.getElement().getStyle().setDisplay(Display.NONE);
    }

    /**
     * Adds a new model page to the model page view.<p>
     *
     * @param modelPageData the data for the new model page
     * @param isModelGroup in case of a model group page
     */
    public void displayNewModelPage(CmsModelPageEntry modelPageData, boolean isModelGroup) {

        CmsModelPageTreeItem treeItem = new CmsModelPageTreeItem(modelPageData, isModelGroup, false);
        CmsSitemapHoverbar.installOn(
            m_controller,
            treeItem,
            modelPageData.getStructureId(),
            modelPageData.getSitePath(),
            modelPageData.getSitePath() != null);
        m_modelPageTreeItems.put(modelPageData.getStructureId(), treeItem);
        if (isModelGroup) {
            m_modelGroupRoot.addChild(treeItem);
        } else {
            m_modelPageRoot.addChild(treeItem);
        }
    }

    /**
     * Ensures the given item is visible in the viewport.<p>
     *
     * @param item the item to see
     */
    public void ensureVisible(CmsSitemapTreeItem item) {

        // open the tree
        CmsTreeItem ti = item.getParentItem();
        while (ti != null) {
            ti.setOpen(true);
            ti = ti.getParentItem();
        }
        // scroll
        CmsDomUtil.ensureVisible(RootPanel.getBodyElement(), item.getElement(), 200);
    }

    /**
     * Returns the controller.<p>
     *
     * @return the controller
     */
    public CmsSitemapController getController() {

        return m_controller;
    }

    /**
     * Returns the editor mode.<p>
     *
     * @return the editor mode
     */
    public EditorMode getEditorMode() {

        return m_editorMode;
    }

    /**
     * Returns the icon class for the given entry depending on the editor mode.<p>
     *
     * @param entry the entry to get the icon for
     *
     * @return the icon CSS class
     */
    public String getIconForEntry(CmsClientSitemapEntry entry) {

        String result;
        if (isNavigationMode()) {
            if (m_controller.isDetailPage(entry.getId())) {
                result = m_controller.getDetailPageInfo(entry.getId()).getIconClasses();
            } else {
                result = entry.getNavModeIcon();
            }
        } else {
            result = entry.getVfsModeIcon();
        }
        return result;
    }

    /**
     * Gets the model page bean with the given structure id.<p>
     *
     * @param id a structure id
     *
     * @return the model page bean with the given id
     */
    public CmsModelPageEntry getModelPageEntry(CmsUUID id) {

        return m_modelPageData.get(id);
    }

    /**
     * Gets the list of descendants of a path and splits it into two lists, one containing the sitemap entries whose children have
     * already been loaded, and those whose children haven't been loaded.<p>
     *
     * @param path the path for which the open and closed descendants should be returned
     *
     * @return a pair whose first and second components are lists of open and closed descendant entries of the path, respectively
     */
    public CmsPair<List<CmsClientSitemapEntry>, List<CmsClientSitemapEntry>> getOpenAndClosedDescendants(String path) {

        List<CmsClientSitemapEntry> descendants = m_controller.getLoadedDescendants(path);
        List<CmsClientSitemapEntry> openDescendants = new ArrayList<CmsClientSitemapEntry>();
        List<CmsClientSitemapEntry> closedDescendants = new ArrayList<CmsClientSitemapEntry>();
        for (CmsClientSitemapEntry entry : descendants) {
            CmsSitemapTreeItem treeItem = getTreeItem(entry.getSitePath());
            List<CmsClientSitemapEntry> listToAddTo = treeItem.isLoaded() ? openDescendants : closedDescendants;
            listToAddTo.add(entry);
        }
        return new CmsPair<List<CmsClientSitemapEntry>, List<CmsClientSitemapEntry>>(
            openDescendants,
            closedDescendants);

    }

    /**
     * Gets the sitemap toolbar.<p>
     *
     * @return the sitemap toolbar
     */
    public CmsSitemapToolbar getToolbar() {

        return m_toolbar;
    }

    /**
     * Returns the tree.<p>
     *
     * @return the tree
     */
    public CmsLazyTree<CmsSitemapTreeItem> getTree() {

        return m_tree;
    }

    /**
     * Returns the tree entry with the given path.<p>
     *
     * @param entryId the id of the sitemap entry
     *
     * @return the tree entry with the given path, or <code>null</code> if not found
     */
    public CmsSitemapTreeItem getTreeItem(CmsUUID entryId) {

        return m_treeItems.get(entryId);
    }

    /**
     * Returns the tree entry with the given path.<p>
     *
     * @param path the path to look for
     *
     * @return the tree entry with the given path, or <code>null</code> if not found
     */
    public CmsSitemapTreeItem getTreeItem(String path) {

        CmsSitemapData data = m_controller.getData();
        CmsClientSitemapEntry root = data.getRoot();
        String rootSitePath = root.getSitePath();
        String remainingPath = path.substring(rootSitePath.length());

        CmsSitemapTreeItem result = getRootItem();

        String[] names = CmsStringUtil.splitAsArray(remainingPath, "/");
        for (String name : names) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
                continue;
            }
            result = (CmsSitemapTreeItem)result.getChild(name);
            if (result == null) {
                return null;
            }
        }
        return result;

    }

    /**
     * Highlights the sitemap entry with the given path.<p>
     *
     * @param sitePath the sitemap path of the entry to highlight
     */
    public void highlightPath(String sitePath) {

        openItemsOnPath(sitePath);
        CmsSitemapTreeItem item = getTreeItem(sitePath);
        if (item != null) {
            item.highlightTemporarily(
                1500,
                isLastPage(item.getSitemapEntry()) ? Background.YELLOW : Background.DEFAULT);
        }
    }

    /**
     * Returns the disabled state of the given model page entry.<p>
     *
     * @param id the entry id
     *
     * @return the disabled state
     */
    public boolean isDisabledModelPageEntry(CmsUUID id) {

        boolean result = false;
        if (m_modelPageTreeItems.containsKey(id)) {
            result = m_modelPageTreeItems.get(id).isDisabled();
        } else if (m_parentModelPageTreeItems.containsKey(id)) {
            result = m_parentModelPageTreeItems.get(id).isDisabled();
        }
        return result;
    }

    /**
     * Returns if the current sitemap editor mode is galleries.<p>
     *
     * @return <code>true</code> if the current sitemap editor mode is galleries
     */
    public boolean isGalleryMode() {

        return EditorMode.galleries == m_editorMode;
    }

    /**
     * Returns if the given entry is a model group page.<p>
     *
     * @param entryId the entry id
     *
     * @return <code>true</code> if the given entry is a model group page
     */
    public boolean isModelGroupEntry(CmsUUID entryId) {

        return m_modelPageTreeItems.containsKey(entryId) && m_modelPageTreeItems.get(entryId).isModelGroup();
    }

    /**
     * Returns if the given entry is a template model page.<p>
     *
     * @param entryId the entry id
     *
     * @return <code>true</code> if the given entry is a template model page
     */
    public boolean isModelPageEntry(CmsUUID entryId) {

        return m_modelPageData.containsKey(entryId);
    }

    /**
     * Checks if the sitemap editor is in 'model page mode'.<p>
     *
     * @return true if we are in model page view
     */
    public boolean isModelPageMode() {

        return EditorMode.modelpages == m_editorMode;
    }

    /**
     * Returns if the current sitemap editor mode is navigation.<p>
     *
     * @return <code>true</code> if the current sitemap editor mode is navigation
     */
    public boolean isNavigationMode() {

        return EditorMode.navigation == m_editorMode;
    }

    /**
     * Returns if the given entry is a template model page inherited from the parent configuration.<p>
     *
     * @param entryId the entry id
     *
     * @return <code>true</code> if the given entry is a template model page
     */
    public boolean isParentModelPageEntry(CmsUUID entryId) {

        return m_parentModelPageTreeItems.containsKey(entryId);
    }

    /**
     * Returns true if we are not currently displaying the navigation or VFS tree.<p>
     *
     * @return true if we are not currently in navigation or VFS mode
     */
    public boolean isSpecialMode() {

        return isGalleryMode() || (EditorMode.modelpages == m_editorMode);
    }

    /**
     * Performs necessary async actions before actually setting the mode.<p>
     *
     * @param mode the mode
     */
    public void onBeforeSetEditorMode(final EditorMode mode) {

        EditorMode oldMode = m_editorMode;
        if ((oldMode == EditorMode.categories) && ((mode == EditorMode.vfs) || (mode == EditorMode.navigation))) {
            final CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

                @Override
                public void execute() {

                    start(200, true);
                    getController().refreshRoot(this);
                }

                @Override
                protected void onResponse(Void result) {

                    stop(false);
                    setEditorMode(mode);
                }
            };
            action.execute();
        } else {
            setEditorMode(mode);
        }

    }

    /**
     * @see org.opencms.ade.sitemap.client.control.I_CmsSitemapChangeHandler#onChange(org.opencms.ade.sitemap.client.control.CmsSitemapChangeEvent)
     */
    public void onChange(CmsSitemapChangeEvent changeEvent) {

        CmsSitemapChange change = changeEvent.getChange();
        switch (change.getChangeType()) {
            case undelete:
                return;
            case delete:
                CmsSitemapTreeItem item = getTreeItem(change.getEntryId());
                item.getParentItem().removeChild(item);
                break;
            case create:
                CmsClientSitemapEntry newEntry = m_controller.getEntryById(change.getEntryId());
                CmsSitemapTreeItem newItem = createSitemapItem(newEntry);
                getTreeItem(change.getParentId()).insertChild(newItem, newEntry.getPosition());
                break;
            case bumpDetailPage:
                updateDetailPageView(m_controller.getEntryById(change.getEntryId()));
                updateAll(m_controller.getEntryById(change.getEntryId()));
                break;
            case modify:
                if (change.hasChangedPosition() || change.hasNewParent()) {
                    CmsClientSitemapEntry entry = m_controller.getEntryById(change.getEntryId());
                    CmsSitemapTreeItem moveEntry = getTreeItem(change.getEntryId());
                    CmsSitemapTreeItem sourceParent = (CmsSitemapTreeItem)moveEntry.getParentItem();
                    getTree().setAnimationEnabled(false);
                    sourceParent.removeChild(moveEntry);
                    CmsSitemapTreeItem destParent = change.hasNewParent()
                    ? getTreeItem(change.getParentId())
                    : sourceParent;
                    if (entry.getPosition() < destParent.getChildCount()) {
                        destParent.insertChild(moveEntry, entry.getPosition());
                    } else {
                        destParent.addChild(moveEntry);
                    }
                    updateAll(entry);
                    ensureVisible(moveEntry);
                    getTree().setAnimationEnabled(true);
                    break;
                }
                //$FALL-THROUGH$
            case remove:
                updateAll(m_controller.getEntryById(change.getEntryId()));
                break;
            default:
        }
        if (m_editorMode == EditorMode.galleries) {
            applyChangeToGalleryTree(changeEvent);
        }
        if (m_editorMode == EditorMode.modelpages) {
            applyChangeToModelPages(changeEvent);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.control.I_CmsSitemapLoadHandler#onLoad(org.opencms.ade.sitemap.client.control.CmsSitemapLoadEvent)
     */
    public void onLoad(CmsSitemapLoadEvent event) {

        CmsSitemapTreeItem target = getTreeItem(event.getEntry().getId());
        if (target != null) {
            if (event.getEntry().isSubSitemapType()) {
                return;
            }
            target.getTree().setAnimationEnabled(false);
            target.clearChildren();
            for (CmsClientSitemapEntry child : event.getEntry().getSubEntries()) {
                CmsSitemapTreeItem childItem = createSitemapItem(child);
                target.addChild(childItem);
            }
            target.onFinishLoading();
            target.getTree().setAnimationEnabled(true);
            if (event.isSetOpen()) {
                target.setOpen(true);
            }
        }
        m_controller.recomputeProperties();
    }

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        CmsBroadcastTimer.start();
        m_instance = this;

        initVaadin();
        RootPanel rootPanel = RootPanel.get();

        m_editorMode = EditorMode.navigation;
        // init
        I_CmsSitemapLayoutBundle.INSTANCE.sitemapCss().ensureInjected();
        I_CmsSitemapLayoutBundle.INSTANCE.clipboardCss().ensureInjected();
        I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().ensureInjected();
        I_CmsSitemapLayoutBundle.INSTANCE.propertiesCss().ensureInjected();
        I_CmsImageBundle.INSTANCE.buttonCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.attributeEditorCss().ensureInjected();

        rootPanel.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapCss().root());
        m_treeItems = new HashMap<CmsUUID, CmsSitemapTreeItem>();
        // controller
        m_controller = new CmsSitemapController();

        if (m_controller.getData() == null) {
            CmsErrorDialog dialog = new CmsErrorDialog(Messages.get().key(Messages.GUI_ERROR_ON_SITEMAP_LOAD_0), null);
            dialog.center();
            return;
        }
        m_controller.addChangeHandler(this);
        m_controller.addLoadHandler(this);

        // toolbar
        m_toolbar = new CmsSitemapToolbar(m_controller);
        m_toolbar.setAppTitle(Messages.get().key(Messages.GUI_SITEMAP_0));
        rootPanel.add(m_toolbar);
        CmsSitemapInfo info = m_controller.getData().getSitemapInfo();
        // header
        CmsInfoHeader header = new CmsInfoHeader(
            info.getTitle(),
            info.getDescription(),
            info.getSiteHost(),
            info.getSiteLocale(),
            m_controller.getData().getRoot().getVfsModeIcon());

        FlowPanel headerContainer = new FlowPanel();
        m_headerContainer = headerContainer;
        m_header = header;
        FlowPanel localeHeaderContainer = new FlowPanel();
        headerContainer.add(m_header);
        headerContainer.add(localeHeaderContainer);
        localeHeaderContainer.getElement().setId(CmsGwtConstants.ID_LOCALE_HEADER_CONTAINER);
        headerContainer.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapCss().headerContainer());

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_controller.getData().getParentSitemap())) {
            CmsPushButton goToParentButton = new CmsPushButton();
            goToParentButton.setImageClass(I_CmsButton.EDIT_UP_SMALL);
            goToParentButton.setTitle(Messages.get().key(Messages.GUI_HOVERBAR_PARENT_0));
            goToParentButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
            goToParentButton.addClickHandler(new ClickHandler() {

                /**
                 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
                 */
                public void onClick(ClickEvent event) {

                    getController().gotoParentSitemap();
                }
            });
            header.addButtonTopRight(goToParentButton);
        }

        rootPanel.add(headerContainer);
        final FlowPanel page = new FlowPanel();
        m_page = page;
        page.setStyleName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapCss().page());
        page.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.generalCss().cornerAll());
        rootPanel.add(page);
        // initial content
        final Label loadingLabel = new Label(
            org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_LOADING_0));
        page.add(loadingLabel);

        // initialize the tree
        m_openHandler = new TreeOpenHandler();
        m_tree = new CmsLazyTree<CmsSitemapTreeItem>(m_openHandler);

        m_inNavigationStyle = new CmsStyleVariable(m_tree);
        m_inNavigationStyle.setValue(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().navMode());
        if (m_controller.isEditable()) {
            // enable drag'n drop
            CmsDNDHandler dndHandler = new CmsDNDHandler(new CmsSitemapDNDController(m_controller, m_toolbar));
            dndHandler.addTarget(m_tree);
            m_tree.setDNDHandler(dndHandler);
            m_tree.setDropEnabled(true);
            m_tree.setDNDTakeAll(true);
        }
        m_tree.truncate(TM_SITEMAP, 920);
        m_tree.setAnimationEnabled(true);
        page.add(m_tree);

        m_galleryTree = new CmsTree<CmsGalleryTreeItem>();
        m_modelPageTree = new CmsTree<CmsModelPageTreeItem>();
        m_galleryTree.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().galleriesMode());
        m_modelPageTree.addStyleName(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().modelPageMode());

        m_categoryTree = new CmsTree<CmsTreeItem>();

        m_galleryTreeItems = new HashMap<CmsUUID, CmsGalleryTreeItem>();
        m_galleryTypeItems = new HashMap<String, CmsGalleryTreeItem>();
        page.add(m_galleryTree);
        page.add(m_modelPageTree);
        page.add(m_categoryTree);
        page.add(m_localeComparison);
        m_localeComparison.setVisible(false);
        m_localeComparison.getElement().setId(CmsGwtConstants.ID_LOCALE_COMPARISON);
        m_noGalleriesLabel = new Label();
        m_noGalleriesLabel.setStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().textMedium());
        m_noGalleriesLabel.getElement().setInnerHTML(Messages.get().key(Messages.GUI_NO_GALLERIES_AVAILABLE_0));
        m_noGalleriesLabel.getElement().getStyle().setDisplay(Display.NONE);
        page.add(m_noGalleriesLabel);

        CmsClientSitemapEntry root = m_controller.getData().getRoot();
        m_rootItem = createSitemapItem(root);
        m_rootItem.onFinishLoading();
        m_rootItem.setOpen(true);
        m_tree.addItem(m_rootItem);
        // draw tree items
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                initiateTreeItems(page, loadingLabel);
            }
        });
        setEditorMode(m_controller.getData().getEditorMode());
        CmsScriptCallbackHelper localeComparePropertyEditorCallback = new CmsScriptCallbackHelper() {

            @Override
            public void run() {

                JsArrayString args = m_arguments.cast();
                String id = args.get(0);
                String rootId = args.get(1);
                CmsSitemapView.getInstance().getController().openPropertyDialogForVaadin(
                    new CmsUUID(id),
                    new CmsUUID(rootId));
            }
        };
        localeComparePropertyEditorCallback.installCallbackOnWindow(CmsGwtConstants.LOCALECOMPARE_EDIT_PROPERTIES);

    }

    /**
     * Removes deleted entry widget reference.<p>
     *
     * @param entry the entry being deleted
     */
    public void removeDeleted(CmsClientSitemapEntry entry) {

        for (CmsClientSitemapEntry child : entry.getSubEntries()) {
            removeDeleted(child);
        }
        m_treeItems.remove(entry.getId());
    }

    /**
     * Sets the editor mode.<p>
     *
     * @param editorMode the editor mode to set
     */
    public void setEditorMode(EditorMode editorMode) {

        if (editorMode != m_editorMode) {
            EditorMode oldEditorMode = m_editorMode;
            m_editorMode = editorMode;
            m_controller.setEditorModeInSession(m_editorMode);
            if (oldEditorMode == EditorMode.compareLocales) {
                // reload sitemap editor because we may need to move to a different site
                reloadForLocaleCompareRoot(m_editorMode);
                return;
            }
            switch (m_editorMode) {
                case galleries:
                    m_tree.getElement().getStyle().setDisplay(Display.NONE);
                    setGalleriesVisible(true);
                    m_toolbar.setNewGalleryEnabled(false, "");
                    setCategoriesVisible(false);
                    setModelPagesVisible(false);
                    setHeaderVisible(true);
                    m_localeComparison.setVisible(false);
                    m_toolbar.setClipboardEnabled(false, Messages.get().key(Messages.GUI_TOOLBAR_CLIPBOARD_DISABLE_0));
                    getController().loadGalleries();
                    break;
                case navigation:
                    m_tree.getElement().getStyle().clearDisplay();
                    setGalleriesVisible(false);
                    setModelPagesVisible(false);
                    setCategoriesVisible(false);
                    setHeaderVisible(true);
                    m_localeComparison.setVisible(false);
                    m_toolbar.setNewEnabled(true, null);

                    m_inNavigationStyle.setValue(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().navMode());
                    break;
                case vfs:
                    m_tree.getElement().getStyle().clearDisplay();
                    setGalleriesVisible(false);
                    setCategoriesVisible(false);
                    setHeaderVisible(true);
                    m_localeComparison.setVisible(false);
                    m_toolbar.setNewEnabled(false, Messages.get().key(Messages.GUI_TOOLBAR_NEW_DISABLE_0));
                    m_toolbar.setClipboardEnabled(true, null);
                    m_inNavigationStyle.setValue(I_CmsSitemapLayoutBundle.INSTANCE.sitemapItemCss().vfsMode());
                    setModelPagesVisible(false);
                    break;
                case modelpages:
                    m_tree.getElement().getStyle().setDisplay(Display.NONE);
                    setGalleriesVisible(false);
                    setCategoriesVisible(false);
                    setModelPagesVisible(true);
                    setHeaderVisible(true);
                    m_localeComparison.setVisible(false);
                    m_toolbar.setNewEnabled(false, Messages.get().key(Messages.GUI_TOOLBAR_NEW_DISABLE_0));
                    m_toolbar.setClipboardEnabled(false, Messages.get().key(Messages.GUI_TOOLBAR_CLIPBOARD_DISABLE_0));
                    getController().loadModelPages();

                    break;
                case categories:
                    m_tree.getElement().getStyle().setDisplay(Display.NONE);
                    setGalleriesVisible(false);
                    setModelPagesVisible(false);
                    setCategoriesVisible(true);
                    setHeaderVisible(true);
                    m_localeComparison.setVisible(false);
                    m_toolbar.setNewEnabled(false, Messages.get().key(Messages.GUI_TOOLBAR_NEW_DISABLE_0));
                    m_toolbar.setClipboardEnabled(false, Messages.get().key(Messages.GUI_TOOLBAR_CLIPBOARD_DISABLE_0));
                    getController().loadCategories(false, null);
                    break;
                case compareLocales:
                    m_tree.getElement().getStyle().setDisplay(Display.NONE);
                    setGalleriesVisible(false);
                    setModelPagesVisible(false);
                    setCategoriesVisible(false);
                    setHeaderVisible(false);
                    m_toolbar.setNewEnabled(false, Messages.get().key(Messages.GUI_TOOLBAR_NEW_DISABLE_0));
                    m_toolbar.setClipboardEnabled(false, Messages.get().key(Messages.GUI_TOOLBAR_CLIPBOARD_DISABLE_0));
                    m_localeComparison.setVisible(true);
                    m_controller.getData().getRoot().getId();
                    CmsJsUtil.callNamedFunctionWithString(
                        CmsGwtConstants.CALLBACK_REFRESH_LOCALE_COMPARISON,
                        "" + m_controller.getData().getRoot().getId());
                    break;

                default:

            }
            // check if the tree has been drawn yet
            getRootItem().updateEditorMode();
            m_toolbar.setMode(editorMode);
        }
    }

    /**
     * Sets the visibility of the normal siteamp header (the header can be hidden so that the Vaadin code can display its own header).<p>
     *
     * @param visible true if the normal header should be visible
     */
    public void setHeaderVisible(boolean visible) {

        String style = I_CmsSitemapLayoutBundle.INSTANCE.sitemapCss().headerContainerVaadinMode();
        if (visible) {
            m_headerContainer.removeStyleName(style);
        } else {
            m_headerContainer.addStyleName(style);
        }
    }

    /**
     * Updates the detail page view for a given changed entry.<p>
     *
     * @param entry the entry which was changed
     */
    public void updateDetailPageView(CmsClientSitemapEntry entry) {

        CmsDetailPageTable detailPageTable = m_controller.getDetailPageTable();
        List<CmsUUID> idsToUpdate = new ArrayList<CmsUUID>();
        if (m_controller.isDetailPage(entry)) {
            idsToUpdate.add(entry.getId());
            idsToUpdate.addAll(detailPageTable.getAllIds());
        }
        updateEntriesById(idsToUpdate);
    }

    /**
     * Updates the entries whose id is in the given list of ids.<p>
     *
     * @param ids a list of sitemap entry ids
     */
    public void updateEntriesById(Collection<CmsUUID> ids) {

        Map<CmsUUID, CmsClientSitemapEntry> entries = m_controller.getEntriesById(ids);
        for (CmsClientSitemapEntry entry : entries.values()) {
            CmsSitemapTreeItem item = CmsSitemapTreeItem.getItemById(entry.getId());
            item.updateEntry(entry);
        }
    }

    /**
     * Updates the disabled state for the given model page.<p>
     *
     * @param entryId the model page id
     * @param disabled the disabled state
     */
    public void updateModelPageDisabledState(CmsUUID entryId, boolean disabled) {

        if (m_modelPageTreeItems.containsKey(entryId)) {
            m_modelPageTreeItems.get(entryId).setDisabled(disabled);
        } else if (m_parentModelPageTreeItems.containsKey(entryId)) {
            m_parentModelPageTreeItems.get(entryId).setDisabled(disabled);
        }
    }

    /**
     * Makes sure corresponding sitemap entries are loaded when the gallery tree is opened.<p>
     *
     * @param parent the parent gallery tree item
     */
    protected void ensureEntriesLoaded(CmsGalleryTreeItem parent) {

        CmsList<? extends I_CmsListItem> children = parent.getChildren();
        Set<String> parentPaths = new HashSet<String>();
        for (Widget listItem : children) {
            CmsGalleryTreeItem treeItem = (CmsGalleryTreeItem)listItem;
            CmsUUID entryId = treeItem.getEntryId();
            if (m_controller.getEntryById(entryId) == null) {
                parentPaths.add(CmsResource.getParentFolder(treeItem.getSitePath()));
            }
        }
        for (String parentPath : parentPaths) {
            m_controller.loadPath(parentPath);
        }
    }

    /**
     * Gets the sitemap tree item widget which represents the root of the current sitemap.<p>
     *
     * @return the root sitemap tree item widget
     */
    protected CmsSitemapTreeItem getRootItem() {

        return m_rootItem;
    }

    /**
     * Shows/hides the category view.<p>
     *
     * @param visible true if the categories should be made visible
     */
    protected void setCategoriesVisible(boolean visible) {

        setElementVisible(m_categoryTree, visible);

    }

    /**
     * Shows or hides the element for a widget.<p>
     *
     * @param widget the widget to show or hide
     * @param visible true if the widget should be shown
     */
    protected void setElementVisible(Widget widget, boolean visible) {

        if (visible) {
            widget.getElement().getStyle().clearDisplay();
        } else {
            widget.getElement().getStyle().setDisplay(Display.NONE);
        }

    }

    /**
     * Shows or hides the tree for the gallery mode.<p>
     *
     * @param visible true if the tree should be shown
     */
    protected void setGalleriesVisible(boolean visible) {

        if (visible) {
            m_noGalleriesLabel.getElement().getStyle().clearDisplay();
            m_galleryTree.getElement().getStyle().clearDisplay();
        } else {
            m_galleryTree.getElement().getStyle().setDisplay(Display.NONE);
            m_noGalleriesLabel.getElement().getStyle().setDisplay(Display.NONE);

        }
    }

    /**
     * Shows or hides the tree for the model page mode.<p>
     *
     * @param visible true if the model pages should be shown
     */
    protected void setModelPagesVisible(boolean visible) {

        setElementVisible(m_modelPageTree, visible);
    }

    /**
     * Recursively creates the widget tree for a given category bean.<p>
     *
     * @param entry the category bean
     *
     * @return the widget for that category bean, with widgets for its descendants attached
     */
    CmsCategoryTreeItem createCategoryTreeItem(CmsCategoryTreeEntry entry) {

        CmsCategoryTreeItem result = new CmsCategoryTreeItem(entry);
        for (CmsCategoryTreeEntry child : entry.getChildren()) {
            result.addChild(createCategoryTreeItem(child));
        }
        return result;
    }

    /**
     * Builds the tree items initially.<p>
     *
     * @param page the page
     * @param loadingLabel the loading label, will be removed when finished
     */
    void initiateTreeItems(FlowPanel page, Label loadingLabel) {

        CmsSitemapTreeItem rootItem = getRootItem();
        m_controller.addPropertyUpdateHandler(new CmsStatusIconUpdateHandler());
        m_controller.recomputeProperties();
        rootItem.updateSitePath();
        // check if editable
        if (!m_controller.isEditable()) {
            // notify user
            CmsNotification.get().sendSticky(
                CmsNotification.Type.WARNING,
                Messages.get().key(Messages.GUI_NO_EDIT_NOTIFICATION_1, m_controller.getData().getNoEditReason()));
        }
        String openPath = m_controller.getData().getOpenPath();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(openPath)) {
            m_openHandler.setInitializing(true);
            openItemsOnPath(openPath);
            m_openHandler.setInitializing(false);
        }
        page.remove(loadingLabel);
    }

    /**
     * Adds the gallery tree items to the parent.<p>
     *
     * @param parent the parent item
     * @param galleries the gallery folder entries
     */
    private void addGalleryEntries(CmsGalleryTreeItem parent, List<CmsGalleryFolderEntry> galleries) {

        for (CmsGalleryFolderEntry galleryFolder : galleries) {
            CmsGalleryTreeItem folderItem = createGalleryFolderItem(galleryFolder);
            parent.addChild(folderItem);
            m_galleryTreeItems.put(galleryFolder.getStructureId(), folderItem);
            addGalleryEntries(folderItem, galleryFolder.getSubGalleries());
        }
    }

    /**
     * Applies the given change to the gallery view.<p>
     *
     * @param changeEvent the change event
     */
    private void applyChangeToGalleryTree(CmsSitemapChangeEvent changeEvent) {

        CmsSitemapChange change = changeEvent.getChange();
        switch (change.getChangeType()) {
            case delete:
                CmsGalleryTreeItem deleteItem = m_galleryTreeItems.get(change.getEntryId());
                if (deleteItem != null) {
                    deleteItem.removeFromParent();
                }

                break;

            case undelete:
            case create:
                String typeName = m_controller.getGalleryType(
                    Integer.valueOf(change.getNewResourceTypeId())).getResourceType();
                if (typeName != null) {
                    CmsGalleryFolderEntry galleryFolder = new CmsGalleryFolderEntry();
                    galleryFolder.setSitePath(change.getSitePath());
                    galleryFolder.setResourceType(typeName);
                    galleryFolder.setStructureId(change.getEntryId());
                    galleryFolder.setOwnProperties(change.getOwnProperties());
                    galleryFolder.setIconClasses(
                        m_controller.getGalleryType(Integer.valueOf(change.getNewResourceTypeId())).getBigIconClasses());
                    CmsGalleryTreeItem folderItem = new CmsGalleryTreeItem(galleryFolder);
                    CmsSitemapHoverbar.installOn(m_controller, folderItem, galleryFolder.getStructureId());
                    m_galleryTypeItems.get(typeName).addChild(folderItem);
                    m_galleryTreeItems.put(galleryFolder.getStructureId(), folderItem);
                }
                break;

            case modify:
                CmsGalleryTreeItem changeItem = m_galleryTreeItems.get(change.getEntryId());
                if (changeItem != null) {
                    CmsListItemWidget widget = changeItem.getListItemWidget();
                    for (CmsPropertyModification mod : change.getPropertyChanges()) {
                        if (mod.getName().equals(CmsClientProperty.PROPERTY_TITLE)) {
                            widget.setTitleLabel(mod.getValue());
                        }
                    }
                    String oldPath = changeItem.getSitePath();
                    if ((change.getName() != null) && !oldPath.endsWith("/" + change.getName())) {
                        String newPath = CmsResource.getParentFolder(oldPath) + change.getName() + "/";
                        changeItem.updateSitePath(newPath);
                    }

                }
                break;
            case bumpDetailPage:
            case clipboardOnly:
            case remove:
            default:
                // nothing to do
        }
    }

    /**
     * Applies sitemap changes to the model page mode.<p>
     *
     * @param changeEvent the change event to apply to the model page mode
     */
    private void applyChangeToModelPages(CmsSitemapChangeEvent changeEvent) {

        CmsSitemapChange change = changeEvent.getChange();
        switch (change.getChangeType()) {
            case delete:

                CmsModelPageTreeItem deleteItem = m_modelPageTreeItems.get(change.getEntryId());
                if (deleteItem != null) {
                    deleteItem.removeFromParent();
                }

                break;

            case undelete:
            case create:
                String typeName = m_controller.getGalleryType(
                    Integer.valueOf(change.getNewResourceTypeId())).getResourceType();
                if (typeName != null) {
                    CmsModelPageEntry modelPage = new CmsModelPageEntry();
                    modelPage.setSitePath(change.getSitePath());
                    modelPage.setResourceType(typeName);
                    modelPage.setStructureId(change.getEntryId());
                    modelPage.setOwnProperties(change.getOwnProperties());
                    CmsModelPageTreeItem folderItem = new CmsModelPageTreeItem(modelPage, false, false);
                    CmsSitemapHoverbar.installOn(m_controller, folderItem, modelPage.getStructureId());
                    m_modelPageRoot.addChild(folderItem);
                    m_modelPageTreeItems.put(modelPage.getStructureId(), folderItem);
                }
                break;

            case modify:
                CmsModelPageTreeItem changeItem = m_modelPageTreeItems.get(change.getEntryId());
                if (changeItem != null) {
                    CmsListItemWidget widget = changeItem.getListItemWidget();
                    for (CmsPropertyModification mod : change.getPropertyChanges()) {
                        if (mod.getName().equals(CmsClientProperty.PROPERTY_TITLE)) {
                            widget.setTitleLabel(mod.getValue());
                        }
                    }
                    String oldPath = changeItem.getSitePath();
                    if ((change.getName() != null) && !oldPath.endsWith("/" + change.getName())) {
                        String newPath = CmsResource.getParentFolder(oldPath) + change.getName() + "/";
                        changeItem.updateSitePath(newPath);
                    }

                }
                break;
            case bumpDetailPage:
            case clipboardOnly:
            case remove:
            default:
                // nothing to do
        }
    }

    /**
     * Create a gallery folder tree item.<p>
     *
     * @param galleryFolder the gallery folder
     *
     * @return the tree item
     */
    private CmsGalleryTreeItem createGalleryFolderItem(CmsGalleryFolderEntry galleryFolder) {

        CmsGalleryTreeItem folderItem = new CmsGalleryTreeItem(galleryFolder);

        CmsSitemapHoverbar.installOn(
            m_controller,
            folderItem,
            galleryFolder.getStructureId(),
            galleryFolder.getSitePath(),
            true);
        return folderItem;
    }

    /**
     * Helper method to get all sitemap tree items from the root to a given path.<p>
     *
     * For example, if the root item has the site path '/root/', and the value of path is
     * '/root/a/b/', the sitemap tree items corresponding to '/root/', '/root/a/' and '/root/a/b'
     * will be returned (in that order).<p>
     *
     * @param path the path for which the sitemap tree items should be returned
     *
     * @return the sitemap tree items on the path
     */
    private List<CmsSitemapTreeItem> getItemsOnPath(String path) {

        List<CmsSitemapTreeItem> result = new ArrayList<CmsSitemapTreeItem>();

        CmsSitemapData data = m_controller.getData();
        CmsClientSitemapEntry root = data.getRoot();
        String rootSitePath = root.getSitePath();
        String remainingPath = path.substring(rootSitePath.length());

        CmsSitemapTreeItem currentItem = getRootItem();
        result.add(currentItem);

        String[] names = CmsStringUtil.splitAsArray(remainingPath, "/");
        for (String name : names) {
            if (currentItem == null) {
                break;
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
                continue;
            }
            currentItem = (CmsSitemapTreeItem)currentItem.getChild(name);
            if (currentItem != null) {
                result.add(currentItem);
            }
        }
        return result;
    }

    /**
     * Initializes the Vaadin part of the sitemap editor.<p>
     */
    private native void initVaadin() /*-{
        $wnd.initVaadin();
    }-*/;

    /**
     * Checks if the given entry represents the last opened page.<p>
     *
     * @param entry the entry to check
     *
     * @return <code>true</code> if the given entry is the last opened page
     */
    private boolean isLastPage(CmsClientSitemapEntry entry) {

        return ((entry.isInNavigation() && (entry.getId().toString().equals(m_controller.getData().getReturnCode())))
            || ((entry.getDefaultFileId() != null)
                && entry.getDefaultFileId().toString().equals(m_controller.getData().getReturnCode())));
    }

    /**
     * Opens all sitemap tree items on a path, except the last one.<p>
     *
     * @param path the path for which all intermediate sitemap items should be opened
     */
    private void openItemsOnPath(String path) {

        List<CmsSitemapTreeItem> itemsOnPath = getItemsOnPath(path);
        for (CmsSitemapTreeItem item : itemsOnPath) {
            item.setOpen(true);
        }
    }

    /**
     * Reloads the sitemap editor for the currently selected locale when leaving locale compare mode.<p>
     *
     * @param editorMode the new editor mode
     */
    private void reloadForLocaleCompareRoot(final EditorMode editorMode) {

        final String localeRootIdStr = CmsJsUtil.getAttributeString(
            CmsJsUtil.getWindow(),
            CmsGwtConstants.VAR_LOCALE_ROOT);
        CmsRpcAction<String> action = new CmsRpcAction<String>() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void execute() {

                start(0, false);
                m_controller.getService().prepareReloadSitemap(new CmsUUID(localeRootIdStr), editorMode, this);
            }

            @Override
            protected void onResponse(String result) {

                stop(false);
                if (result != null) {
                    Window.Location.assign(result);
                } else {
                    Window.Location.reload();
                }
            }
        };
        action.execute();

    }

    /**
     * Updates the entry and it's children's view.<p>
     *
     * @param entry the entry to update
     */
    private void updateAll(CmsClientSitemapEntry entry) {

        CmsSitemapTreeItem item = getTreeItem(entry.getId());
        if (item != null) {
            item.updateEntry(entry);
            item.updateSitePath(entry.getSitePath());
            for (CmsClientSitemapEntry child : entry.getSubEntries()) {
                updateAll(child);
            }
        }
    }
}
