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

package org.opencms.ade.sitemap.client.control;

import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.sitemap.client.CmsSitemapTreeItem;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.client.edit.CmsLocaleComparePropertyHandler;
import org.opencms.ade.sitemap.client.edit.CmsNavModePropertyEditor;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.CmsDetailPageTable;
import org.opencms.ade.sitemap.shared.CmsGalleryFolderEntry;
import org.opencms.ade.sitemap.shared.CmsGalleryType;
import org.opencms.ade.sitemap.shared.CmsLocaleComparePropertyData;
import org.opencms.ade.sitemap.shared.CmsModelInfo;
import org.opencms.ade.sitemap.shared.CmsModelPageEntry;
import org.opencms.ade.sitemap.shared.CmsNewResourceInfo;
import org.opencms.ade.sitemap.shared.CmsSitemapCategoryData;
import org.opencms.ade.sitemap.shared.CmsSitemapChange;
import org.opencms.ade.sitemap.shared.CmsSitemapChange.ChangeType;
import org.opencms.ade.sitemap.shared.CmsSitemapClipboardData;
import org.opencms.ade.sitemap.shared.CmsSitemapData;
import org.opencms.ade.sitemap.shared.CmsSitemapData.EditorMode;
import org.opencms.ade.sitemap.shared.I_CmsSitemapController;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.property.A_CmsPropertyEditor;
import org.opencms.gwt.client.property.CmsPropertySubmitHandler;
import org.opencms.gwt.client.property.CmsReloadMode;
import org.opencms.gwt.client.property.definition.CmsPropertyDefinitionButton;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsDeleteWarningDialog;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.input.form.CmsDialogFormHandler;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler;
import org.opencms.gwt.client.ui.tree.CmsLazyTreeItem.LoadState;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.gwt.shared.rpc.I_CmsVfsServiceAsync;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * Sitemap editor controller.<p>
 *
 * @since 8.0.0
 */
public class CmsSitemapController implements I_CmsSitemapController {

    /** The name to use for new entries. */
    public static final String NEW_ENTRY_NAME = "page";

    /** A map of *all* detail page info beans, indexed by page id. */
    protected Map<CmsUUID, CmsDetailPageInfo> m_allDetailPageInfos = new HashMap<CmsUUID, CmsDetailPageInfo>();

    /** The sitemap data. */
    protected CmsSitemapData m_data;

    /** The detail page table. */
    protected CmsDetailPageTable m_detailPageTable;

    /** The event bus. */
    protected SimpleEventBus m_eventBus;

    /** The category data. */
    CmsSitemapCategoryData m_categoryData;

    /** The entry data model. */
    private Map<CmsUUID, CmsClientSitemapEntry> m_entriesById;

    /** The sitemap entries by path. */
    private Map<String, CmsClientSitemapEntry> m_entriesByPath;

    /** The gallery type names by id. */
    private Map<Integer, CmsGalleryType> m_galleryTypes;

    /** The set of names of hidden properties. */
    private Set<String> m_hiddenProperties;

    /** The map of property maps by structure id. */
    private Map<CmsUUID, Map<String, CmsClientProperty>> m_propertyMaps = Maps.newHashMap();

    /** The list of property update handlers. */
    private List<I_CmsPropertyUpdateHandler> m_propertyUpdateHandlers = new ArrayList<I_CmsPropertyUpdateHandler>();

    /** The sitemap service instance. */
    private I_CmsSitemapServiceAsync m_service;

    /** The vfs service. */
    private I_CmsVfsServiceAsync m_vfsService;

    /**
     * Constructor.<p>
     */
    public CmsSitemapController() {

        m_entriesById = new HashMap<CmsUUID, CmsClientSitemapEntry>();
        m_entriesByPath = new HashMap<String, CmsClientSitemapEntry>();
        m_galleryTypes = new HashMap<Integer, CmsGalleryType>();
        try {
            m_data = (CmsSitemapData)CmsRpcPrefetcher.getSerializedObjectFromDictionary(
                getService(),
                CmsSitemapData.DICT_NAME);
        } catch (SerializationException e) {
            CmsErrorDialog.handleException(
                new Exception(
                    "Deserialization of sitemap data failed. This may be caused by expired java-script resources, please clear your browser cache and try again.",
                    e));
        }

        m_hiddenProperties = new HashSet<String>();
        if (m_data != null) {
            m_detailPageTable = m_data.getDetailPageTable();
            m_data.getRoot().initializeAll(this);
            m_eventBus = new SimpleEventBus();
            initDetailPageInfos();
        }
    }

    /**
     * Opens the property editor for locale compare mode.<p>
     *
     * @param data the data used by the property editor
     * @param ownId the id
     * @param defaultFileId the default file id
     * @param noEditReason the reason the properties can't be edited
     */
    public static void editPropertiesForLocaleCompareMode(
        final CmsLocaleComparePropertyData data,
        final CmsUUID ownId,
        final CmsUUID defaultFileId,
        final String noEditReason) {

        final CmsUUID infoId;
        infoId = defaultFileId;
        Set<CmsUUID> idsForPropertyConfig = new HashSet<CmsUUID>();
        idsForPropertyConfig.add(defaultFileId);
        idsForPropertyConfig.add(ownId);

        final List<CmsUUID> propertyConfigIds = new ArrayList<CmsUUID>(idsForPropertyConfig);

        CmsRpcAction<CmsListInfoBean> action = new CmsRpcAction<CmsListInfoBean>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().getPageInfo(infoId, this);
            }

            @Override
            protected void onResponse(CmsListInfoBean infoResult) {

                stop(false);
                final CmsLocaleComparePropertyHandler handler = new CmsLocaleComparePropertyHandler(data);
                handler.setPageInfo(infoResult);

                CmsRpcAction<Map<CmsUUID, Map<String, CmsXmlContentProperty>>> propertyAction = new CmsRpcAction<Map<CmsUUID, Map<String, CmsXmlContentProperty>>>() {

                    @Override
                    public void execute() {

                        start(0, true);
                        CmsCoreProvider.getVfsService().getDefaultProperties(propertyConfigIds, this);
                    }

                    @Override
                    protected void onResponse(Map<CmsUUID, Map<String, CmsXmlContentProperty>> propertyResult) {

                        stop(false);
                        Map<String, CmsXmlContentProperty> propConfig = new LinkedHashMap<String, CmsXmlContentProperty>();
                        for (Map<String, CmsXmlContentProperty> defaultProps : propertyResult.values()) {
                            propConfig.putAll(defaultProps);
                        }
                        propConfig.putAll(CmsSitemapView.getInstance().getController().getData().getProperties());
                        A_CmsPropertyEditor editor = new CmsNavModePropertyEditor(propConfig, handler);
                        editor.setPropertyNames(
                            CmsSitemapView.getInstance().getController().getData().getAllPropertyNames());
                        final CmsFormDialog dialog = new CmsFormDialog(handler.getDialogTitle(), editor.getForm());
                        CmsPropertyDefinitionButton defButton = new CmsPropertyDefinitionButton() {

                            /**
                             * @see org.opencms.gwt.client.property.definition.CmsPropertyDefinitionButton#onBeforeEditPropertyDefinition()
                             */
                            @Override
                            public void onBeforeEditPropertyDefinition() {

                                dialog.hide();
                            }

                        };
                        defButton.getElement().getStyle().setFloat(Style.Float.LEFT);
                        defButton.installOnDialog(dialog);
                        CmsDialogFormHandler formHandler = new CmsDialogFormHandler();
                        formHandler.setDialog(dialog);
                        I_CmsFormSubmitHandler submitHandler = new CmsPropertySubmitHandler(handler);
                        formHandler.setSubmitHandler(submitHandler);
                        dialog.setFormHandler(formHandler);
                        editor.initializeWidgets(dialog);
                        dialog.centerHorizontally(50);
                        dialog.catchNotifications();
                        if (noEditReason != null) {
                            editor.disableInput(noEditReason, false);
                            dialog.getOkButton().disable(noEditReason);
                        }

                    }
                };
                propertyAction.execute();
            }

        };
        action.execute();
    }

    /**
     * Helper method for looking up a value in a map which may be null.<p>
     *
     * @param <A> the key type
     * @param <B> the value type
     * @param map the map (which may be null)
     * @param key the map key
     *
     * @return the value of the map at the given key, or null if the map is null
     */
    public static <A, B> B safeLookup(Map<A, B> map, A key) {

        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    /**
     * Adds a new change event handler.<p>
     *
     * @param handler the handler to add
     *
     * @return the handler registration
     */
    public HandlerRegistration addChangeHandler(I_CmsSitemapChangeHandler handler) {

        return m_eventBus.addHandlerToSource(CmsSitemapChangeEvent.getType(), this, handler);
    }

    /**
     * Adds a new detail page information bean.<p>
     *
     * @param info the detail page information bean to add
     */
    public void addDetailPageInfo(CmsDetailPageInfo info) {

        m_detailPageTable.add(info);
        m_allDetailPageInfos.put(info.getId(), info);
    }

    /**
     * Adds a new load event handler.<p>
     *
     * @param handler the handler to add
     *
     * @return the handler registration
     */
    public HandlerRegistration addLoadHandler(I_CmsSitemapLoadHandler handler) {

        return m_eventBus.addHandlerToSource(CmsSitemapLoadEvent.getType(), this, handler);
    }

    /**
     * Adds a handler for property changes caused by user edits.<p>
     *
     * @param handler a new handler for property updates caused by the user
     */
    public void addPropertyUpdateHandler(I_CmsPropertyUpdateHandler handler) {

        m_propertyUpdateHandlers.add(handler);

    }

    /**
     * Adds the entry to the navigation.<p>
     *
     * @param entry the entry
     */
    public void addToNavigation(CmsClientSitemapEntry entry) {

        entry.setInNavigation(true);
        CmsSitemapChange change = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.modify);
        CmsPropertyModification mod = new CmsPropertyModification(
            entry.getId(),
            CmsClientProperty.PROPERTY_NAVTEXT,
            entry.getTitle(),
            true);
        change.setPropertyChanges(Collections.singletonList(mod));
        change.setPosition(entry.getPosition());
        commitChange(change, null);
    }

    /**
     * Makes the given sitemap entry the default detail page for its detail page type.<p>
     *
     * @param entry an entry representing a detail page
     */
    public void bump(CmsClientSitemapEntry entry) {

        CmsDetailPageTable table = getDetailPageTable().copy();
        table.makeDefault(entry.getId());
        CmsSitemapChange change = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.bumpDetailPage);
        change.setDetailPageInfos(table.toList());
        commitChange(change, null);
    }

    /**
     * Changes the given category.<p>
     *
     * @param id the category id
     * @param title the new category title
     * @param name the new category name
     */
    public void changeCategory(final CmsUUID id, final String title, final String name) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(200, true);
                getService().changeCategory(getEntryPoint(), id, title, name, this);
            }

            @Override
            protected void onResponse(Void result) {

                stop(false);
                loadCategories(true, null);
            }

        };
        action.execute();
    }

    /**
     * Clears the deleted clip-board list and commits the change.<p>
     */
    public void clearDeletedList() {

        CmsSitemapClipboardData clipboardData = getData().getClipboardData().copy();
        clipboardData.getDeletions().clear();
        CmsSitemapChange change = new CmsSitemapChange(null, null, ChangeType.clipboardOnly);
        change.setClipBoardData(clipboardData);
        commitChange(change, null);
    }

    /**
     * Clears the modified clip-board list and commits the change.<p>
     */
    public void clearModifiedList() {

        CmsSitemapClipboardData clipboardData = getData().getClipboardData().copy();
        clipboardData.getModifications().clear();
        CmsSitemapChange change = new CmsSitemapChange(null, null, ChangeType.clipboardOnly);
        change.setClipBoardData(clipboardData);
        commitChange(change, null);
    }

    /**
     * Registers a new sitemap entry.<p>
     *
     * @param newEntry the new entry
     * @param parentId the parent entry id
     * @param resourceTypeId the resource type id
     * @param copyResourceId the copy resource id
     * @param parameter an additional parameter which may contain more information needed to create the new resource
     * @param isNewSitemap a flag controlling whether a new sitemap should be created
     */
    public void create(
        CmsClientSitemapEntry newEntry,
        CmsUUID parentId,
        int resourceTypeId,
        CmsUUID copyResourceId,
        String parameter,
        boolean isNewSitemap) {

        assert (getEntry(newEntry.getSitePath()) == null);

        CmsSitemapChange change = new CmsSitemapChange(null, newEntry.getSitePath(), ChangeType.create);
        change.setDefaultFileId(newEntry.getDefaultFileId());
        change.setParentId(parentId);
        change.setName(newEntry.getName());
        change.setPosition(newEntry.getPosition());
        change.setOwnInternalProperties(newEntry.getOwnProperties());
        change.setDefaultFileInternalProperties(newEntry.getDefaultFileProperties());
        change.setTitle(newEntry.getTitle());
        change.setCreateParameter(parameter);
        change.setNewResourceTypeId(resourceTypeId);
        if (isNewSitemap) {
            change.setCreateSitemapFolderType(newEntry.getResourceTypeName());
        }
        change.setNewCopyResourceId(copyResourceId);
        if (isDetailPage(newEntry)) {
            CmsDetailPageTable table = getDetailPageTable().copy();
            if (!table.contains(newEntry.getId())) {
                CmsDetailPageInfo info = new CmsDetailPageInfo(
                    newEntry.getId(),
                    newEntry.getSitePath(),
                    newEntry.getDetailpageTypeName(),
                    /* qualifier = */null,
                    newEntry.getVfsModeIcon());
                table.add(info);
            }
            change.setDetailPageInfos(table.toList());
        }
        CmsSitemapClipboardData data = getData().getClipboardData().copy();
        data.addModified(newEntry);
        change.setClipBoardData(data);
        commitChange(change, null);
    }

    /**
     * Creates a new category.<p>
     *
     * @param id the parent category id, or the null uuid for a new top-level category
     * @param title the title of the category
     * @param name the name of the category
     */
    public void createCategory(final CmsUUID id, final String title, final String name) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(200, true);
                getService().createCategory(getEntryPoint(), id, title, name, this);
            }

            @Override
            protected void onResponse(Void result) {

                stop(false);
                loadCategories(true, id);

            }
        };
        action.execute();
    }

    /**
     * Creates a new gallery folder of the given type.<p>
     *
     * @param parentId the parent folder id
     * @param galleryTypeId the folder type id
     * @param title the folder title
     */
    public void createNewGallery(final CmsUUID parentId, final int galleryTypeId, final String title) {

        final String parentFolder = parentId != null
        ? getEntryById(parentId).getSitePath()
        : CmsStringUtil.joinPaths(m_data.getRoot().getSitePath(), m_data.getDefaultGalleryFolder());

        CmsRpcAction<CmsGalleryFolderEntry> action = new CmsRpcAction<CmsGalleryFolderEntry>() {

            @Override
            public void execute() {

                getService().createNewGalleryFolder(parentFolder, title, galleryTypeId, this);
            }

            @Override
            protected void onResponse(CmsGalleryFolderEntry result) {

                CmsSitemapView.getInstance().displayNewGallery(result);

            }
        };
        action.execute();
    }

    /**
     * Creates a new model page.<p>
     *
     * @param title the title of the model page
     * @param description the description of the model page
     * @param copyId the structure id of the resource which should be used as a copy model for the new page
     * @param isModelGroup in case of a model group page
     */
    public void createNewModelPage(
        final String title,
        final String description,
        final CmsUUID copyId,
        final boolean isModelGroup) {

        CmsRpcAction<CmsModelPageEntry> action = new CmsRpcAction<CmsModelPageEntry>() {

            @Override
            public void execute() {

                start(200, true);

                getService().createNewModelPage(getEntryPoint(), title, description, copyId, isModelGroup, this);

            }

            @Override
            protected void onResponse(final CmsModelPageEntry result) {

                stop(false);
                if (isModelGroup) {
                    CmsSitemapView.getInstance().displayNewModelPage(result, true);
                } else {
                    loadNewElementInfo(new AsyncCallback<Void>() {

                        public void onFailure(Throwable caught) {

                            // nothing to do

                        }

                        public void onSuccess(Void v) {

                            CmsSitemapView.getInstance().displayNewModelPage(result, false);
                        }
                    });
                }

            }

        };
        action.execute();
    }

    /**
     * Creates a sitemap folder.<p>
     *
     * @param newEntry the new entry
     * @param parentId the entry parent id
     * @param sitemapType the resource type for the subsitemap folder
     */
    public void createSitemapSubEntry(final CmsClientSitemapEntry newEntry, CmsUUID parentId, String sitemapType) {

        CmsUUID structureId = m_data.getDefaultNewElementInfo().getCopyResourceId();
        newEntry.setResourceTypeName(sitemapType);
        create(newEntry, parentId, m_data.getDefaultNewElementInfo().getId(), structureId, null, true);
    }

    /**
     * Creates a new sub-entry which is a subsitemap.<p>
     *
     * @param parent the parent entry
     * @param sitemapFolderType the sitemap folder type
     */
    public void createSitemapSubEntry(final CmsClientSitemapEntry parent, final String sitemapFolderType) {

        CmsSitemapTreeItem item = CmsSitemapTreeItem.getItemById(parent.getId());
        AsyncCallback<CmsClientSitemapEntry> callback = new AsyncCallback<CmsClientSitemapEntry>() {

            public void onFailure(Throwable caught) {

                // do nothing
            }

            public void onSuccess(CmsClientSitemapEntry result) {

                makeNewEntry(parent, new I_CmsSimpleCallback<CmsClientSitemapEntry>() {

                    public void execute(CmsClientSitemapEntry newEntry) {

                        newEntry.setResourceTypeName(sitemapFolderType);
                        createSitemapSubEntry(newEntry, parent.getId(), sitemapFolderType);
                    }
                });
            }
        };
        if (item.getLoadState().equals(LoadState.UNLOADED)) {
            getChildren(parent.getId(), true, callback);
        } else {
            callback.onSuccess(parent);
        }
    }

    /**
     * Creates a new sub-entry of an existing sitemap entry.<p>
     *
     * @param parent the entry to which a new sub-entry should be added
     */
    public void createSubEntry(final CmsClientSitemapEntry parent) {

        createSubEntry(parent, null);
    }

    /**
     * Creates a new sub-entry of an existing sitemap entry.<p>
     *
     * @param parent the entry to which a new sub-entry should be added
     * @param structureId the structure id of the model page (if null, uses default model page)
     */
    public void createSubEntry(final CmsClientSitemapEntry parent, final CmsUUID structureId) {

        CmsSitemapTreeItem item = CmsSitemapTreeItem.getItemById(parent.getId());
        AsyncCallback<CmsClientSitemapEntry> callback = new AsyncCallback<CmsClientSitemapEntry>() {

            public void onFailure(Throwable caught) {

                // nothing to do
            }

            public void onSuccess(CmsClientSitemapEntry result) {

                makeNewEntry(parent, new I_CmsSimpleCallback<CmsClientSitemapEntry>() {

                    public void execute(CmsClientSitemapEntry newEntry) {

                        createSubEntry(newEntry, parent.getId(), structureId);
                    }
                });

            }

        };

        if (item.getLoadState().equals(LoadState.UNLOADED)) {
            getChildren(parent.getId(), true, callback);
        } else {
            callback.onSuccess(parent);
        }
    }

    /**
     * Registers a new sitemap entry.<p>
     *
     * @param newEntry the new entry
     * @param parentId the parent entry id
     * @param structureId the structure id of the model page (if null, uses default model page)
     */
    public void createSubEntry(final CmsClientSitemapEntry newEntry, CmsUUID parentId, CmsUUID structureId) {

        if (structureId == null) {
            structureId = m_data.getDefaultNewElementInfo().getCopyResourceId();
        }
        create(newEntry, parentId, m_data.getDefaultNewElementInfo().getId(), structureId, null, false);
    }

    /**
     * Creates a sub-sitemap from the subtree of the current sitemap starting at the given entry.<p>
     *
     * @param entryId the id of the entry
     */
    public void createSubSitemap(final CmsUUID entryId) {

        CmsRpcAction<CmsSitemapChange> subSitemapAction = new CmsRpcAction<CmsSitemapChange>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, true);
                getService().createSubSitemap(entryId, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsSitemapChange result) {

                stop(false);
                applyChange(result);
            }
        };
        subSitemapAction.execute();
    }

    /**
     * Deletes the given entry and all its descendants.<p>
     *
     * @param sitePath the site path of the entry to delete
     */
    public void delete(String sitePath) {

        CmsClientSitemapEntry entry = getEntry(sitePath);
        CmsClientSitemapEntry parent = getEntry(CmsResource.getParentFolder(entry.getSitePath()));
        CmsSitemapChange change = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.delete);
        change.setParentId(parent.getId());
        change.setDefaultFileId(entry.getDefaultFileId());
        CmsSitemapClipboardData data = CmsSitemapView.getInstance().getController().getData().getClipboardData().copy();
        if (!entry.isNew()) {
            data.addDeleted(entry);
            removeDeletedFromModified(entry, data);
        }
        change.setClipBoardData(data);
        CmsDetailPageTable detailPageTable = CmsSitemapView.getInstance().getController().getData().getDetailPageTable();
        CmsUUID id = entry.getId();
        if (detailPageTable.contains(id)) {
            CmsDetailPageTable copyTable = detailPageTable.copy();
            copyTable.remove(id);
            change.setDetailPageInfos(copyTable.toList());
        }
        commitChange(change, null);
    }

    /**
     * Deletes a category.<p>
     *
     * @param id the id of the category
     */
    public void deleteCategory(final CmsUUID id) {

        CmsDeleteWarningDialog deleteWarningDialog = new CmsDeleteWarningDialog(id) {

            @Override
            protected void onAfterDeletion() {

                CmsSitemapView.getInstance().getController().loadCategories(true, null);
            }
        };
        deleteWarningDialog.loadAndShow(null);
    }

    /**
     * Disables the given model page entry within the configuration.<p>
     *
     * @param id the entry id
     * @param disable <code>true</code> to disable the entry
     */
    public void disableModelPage(final CmsUUID id, final boolean disable) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(200, true);
                getService().disableModelPage(getEntryPoint(), id, disable, this);

            }

            @Override
            protected void onResponse(Void result) {

                stop(false);
                CmsSitemapView.getInstance().updateModelPageDisabledState(id, disable);
            }
        };
        action.execute();

    }

    /**
     * Edits the given sitemap entry.<p>
     *
     * @param entry the sitemap entry to update
     * @param propertyChanges the property changes
     * @param reloadStatus a value indicating which entries need to be reloaded after the change
     */
    public void edit(
        CmsClientSitemapEntry entry,
        List<CmsPropertyModification> propertyChanges,
        final CmsReloadMode reloadStatus) {

        CmsSitemapChange change = getChangeForEdit(entry, propertyChanges);
        final String updateTarget = ((reloadStatus == CmsReloadMode.reloadParent))
        ? getParentEntry(entry).getSitePath()
        : entry.getSitePath();
        Command callback = new Command() {

            public void execute() {

                if ((reloadStatus == CmsReloadMode.reloadParent) || (reloadStatus == CmsReloadMode.reloadEntry)) {
                    updateEntry(updateTarget);
                }
            }
        };
        if (change != null) {
            commitChange(change, callback);
        }
    }

    /**
     * Edits an entry and changes its URL name.<p>
     *
     * @param entry the entry which is being edited
     * @param newUrlName the new URL name of the entry
     * @param propertyChanges the property changes
     * @param keepNewStatus <code>true</code> if the entry should keep it's new status
     * @param reloadStatus a value indicating which entries need to be reloaded after the change
     */
    public void editAndChangeName(
        final CmsClientSitemapEntry entry,
        String newUrlName,
        List<CmsPropertyModification> propertyChanges,
        final boolean keepNewStatus,
        final CmsReloadMode reloadStatus) {

        CmsSitemapChange change = getChangeForEdit(entry, propertyChanges);
        change.setName(newUrlName);
        final CmsUUID entryId = entry.getId();
        final boolean newStatus = keepNewStatus && entry.isNew();

        final CmsUUID updateTarget = ((reloadStatus == CmsReloadMode.reloadParent))
        ? getParentEntry(entry).getId()
        : entryId;
        Command callback = new Command() {

            public void execute() {

                if ((reloadStatus == CmsReloadMode.reloadParent) || (reloadStatus == CmsReloadMode.reloadEntry)) {
                    updateEntry(updateTarget);
                }
                getEntryById(entryId).setNew(newStatus);
            }

        };

        commitChange(change, callback);
    }

    /**
     * Ensure the uniqueness of a given URL-name within the children of the given parent site-map entry.<p>
     *
     * @param parent the parent entry
     * @param newName the proposed name
     * @param callback the callback to execute
     */
    public void ensureUniqueName(CmsClientSitemapEntry parent, String newName, I_CmsSimpleCallback<String> callback) {

        ensureUniqueName(parent.getSitePath(), newName, callback);
    }

    /**
     * Ensure the uniqueness of a given URL-name within the children of the given parent folder.<p>
     *
     * @param parentFolder the parent folder
     * @param newName the proposed name
     * @param callback the callback to execute
     */
    public void ensureUniqueName(
        final String parentFolder,
        String newName,
        final I_CmsSimpleCallback<String> callback) {

        // using lower case folder names
        final String lowerCaseName = newName.toLowerCase();
        CmsRpcAction<String> action = new CmsRpcAction<String>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, false);
                CmsCoreProvider.getService().getUniqueFileName(parentFolder, lowerCaseName, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(String result) {

                stop(false);
                callback.execute(result);
            }

        };
        action.execute();
    }

    /**
     * Applies the given property modification.<p>
     *
     * @param propMod the property modification to apply
     */
    public void executePropertyModification(CmsPropertyModification propMod) {

        CmsClientSitemapEntry entry = getEntryById(propMod.getId());
        if (entry != null) {
            Map<String, CmsClientProperty> props = getPropertiesForId(propMod.getId());
            if (props != null) {
                propMod.updatePropertyInMap(props);
                entry.setOwnProperties(props);
            }
        }
    }

    /**
     * Gets the category data.<p>
     *
     * @return the category data
     */
    public CmsSitemapCategoryData getCategoryData() {

        return m_categoryData;
    }

    /**
     * Retrieves the child entries of the given node from the server.<p>
     *
     * @param entryId the entry id
     * @param setOpen if the entry should be opened
     *
     * @param callback the callback to execute after the children have been loaded
     */
    public void getChildren(
        final CmsUUID entryId,
        final boolean setOpen,
        final AsyncCallback<CmsClientSitemapEntry> callback) {

        getChildren(entryId, setOpen, false, callback);

    }

    /**
     * Retrieves the child entries of the given node from the server.<p>
     *
     * @param entryId the entry id
     * @param setOpen if the entry should be opened
     * @param continueIfParentNotLoaded if false, and the entry identified by the id has not been loaded, stop; else store the entry in memory
     *
     * @param callback the callback to execute after the children have been loaded
     */
    public void getChildren(
        final CmsUUID entryId,
        final boolean setOpen,
        final boolean continueIfParentNotLoaded,
        final AsyncCallback<CmsClientSitemapEntry> callback) {

        CmsRpcAction<CmsClientSitemapEntry> getChildrenAction = new CmsRpcAction<CmsClientSitemapEntry>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                // Make the call to the sitemap service
                start(500, false);
                // loading grand children as well
                getService().getChildren(getEntryPoint(), entryId, 2, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsClientSitemapEntry result) {

                CmsClientSitemapEntry target = getEntryById(entryId);
                if ((target == null) && !continueIfParentNotLoaded) {
                    // this might happen after an automated deletion
                    stop(false);
                    return;
                }
                CmsSitemapTreeItem item = null;
                if (target != null) {
                    target.setSubEntries(result.getSubEntries(), CmsSitemapController.this);
                    item = CmsSitemapTreeItem.getItemById(target.getId());
                    target.update(result);
                } else {
                    target = result;
                }

                target.initializeAll(CmsSitemapController.this);
                if (item != null) {

                    item.updateEntry(target);
                }
                m_eventBus.fireEventFromSource(new CmsSitemapLoadEvent(target, setOpen), CmsSitemapController.this);
                stop(false);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        };
        getChildrenAction.execute();
    }

    /**
    * Returns the sitemap data.<p>
    *
    * @return the sitemap data
    */
    public CmsSitemapData getData() {

        return m_data;
    }

    /**
     * Returns the detail page info for a given entry id.<p>
     *
     * @param id a sitemap entry id
     *
     * @return the detail page info for that id
     */
    public CmsDetailPageInfo getDetailPageInfo(CmsUUID id) {

        return m_allDetailPageInfos.get(id);
    }

    /**
     * Returns the detail page table.<p>
     *
     * @return the detail page table
     */
    public CmsDetailPageTable getDetailPageTable() {

        return m_detailPageTable;
    }

    /**
     * Gets the effective value of a property value for a sitemap entry.<p>
     *
     * @param entry the sitemap entry
     * @param name the name of the property
     *
     * @return the effective value
     */
    public String getEffectiveProperty(CmsClientSitemapEntry entry, String name) {

        CmsClientProperty prop = getEffectivePropertyObject(entry, name);
        if (prop == null) {
            return null;
        }
        return prop.getEffectiveValue();
    }

    /**
     * Gets the value of a property which is effective at a given sitemap entry.<p>
     *
     * @param entry the sitemap entry
     * @param name the name of the property
     * @return the effective property value
     */
    public CmsClientProperty getEffectivePropertyObject(CmsClientSitemapEntry entry, String name) {

        Map<String, CmsClientProperty> dfProps = entry.getDefaultFileProperties();
        CmsClientProperty result = safeLookup(dfProps, name);
        if (!CmsClientProperty.isPropertyEmpty(result)) {
            return result.withOrigin(entry.getSitePath());
        }
        result = safeLookup(entry.getOwnProperties(), name);
        if (!CmsClientProperty.isPropertyEmpty(result)) {
            return result.withOrigin(entry.getSitePath());
        }
        return getInheritedPropertyObject(entry, name);
    }

    /**
     * Returns all entries with an id from a given list.<p>
     *
     * @param ids a list of sitemap entry ids
     *
     * @return all entries whose id is contained in the id list
     */
    public Map<CmsUUID, CmsClientSitemapEntry> getEntriesById(Collection<CmsUUID> ids) {

        // TODO: use some map of id -> entry instead
        List<CmsClientSitemapEntry> entriesToProcess = new ArrayList<CmsClientSitemapEntry>();
        Map<CmsUUID, CmsClientSitemapEntry> result = new HashMap<CmsUUID, CmsClientSitemapEntry>();

        entriesToProcess.add(m_data.getRoot());
        while (!entriesToProcess.isEmpty()) {
            CmsClientSitemapEntry entry = entriesToProcess.remove(entriesToProcess.size() - 1);
            if (ids.contains(entry.getId())) {
                result.put(entry.getId(), entry);
                if (result.size() == ids.size()) {
                    return result;
                }
            }
            entriesToProcess.addAll(entry.getSubEntries());
        }
        return result;
    }

    /**
     * Returns the tree entry with the given path.<p>
     *
     * @param entryPath the path to look for
     *
     * @return the tree entry with the given path, or <code>null</code> if not found
     */
    public CmsClientSitemapEntry getEntry(String entryPath) {

        return m_entriesByPath.get(entryPath);
    }

    /**
     * Finds an entry by id.<p>
     *
     * @param id the id of the entry to find
     *
     * @return the found entry, or null if the entry wasn't found
     */
    public CmsClientSitemapEntry getEntryById(CmsUUID id) {

        return m_entriesById.get(id);
    }

    /**
     * Returns the gallery type with the given id.<p>
     *
     * @param typeId the type id
     *
     * @return the gallery type
     */
    public CmsGalleryType getGalleryType(Integer typeId) {

        return m_galleryTypes.get(typeId);
    }

    /**
     * Gets the value for a property which a sitemap entry would inherit if it didn't have its own properties.<p>
     *
     * @param entry the sitemap entry
     * @param name the property name
     * @return the inherited property value
     */
    public String getInheritedProperty(CmsClientSitemapEntry entry, String name) {

        CmsClientProperty prop = getInheritedPropertyObject(entry, name);
        if (prop == null) {
            return null;
        }
        return prop.getEffectiveValue();
    }

    /**
     * Gets the property object which would be inherited by a sitemap entry.<p>
     *
     * @param entry the sitemap entry
     * @param name the name of the property
     * @return the property object which would be inherited
     */
    public CmsClientProperty getInheritedPropertyObject(CmsClientSitemapEntry entry, String name) {

        CmsClientSitemapEntry currentEntry = entry;
        while (currentEntry != null) {
            currentEntry = getParentEntry(currentEntry);
            if (currentEntry != null) {
                CmsClientProperty folderProp = currentEntry.getOwnProperties().get(name);
                if (!CmsClientProperty.isPropertyEmpty(folderProp)) {
                    return folderProp.withOrigin(currentEntry.getSitePath());
                }
            }
        }
        CmsClientProperty parentProp = getParentProperties().get(name);
        if (!CmsClientProperty.isPropertyEmpty(parentProp)) {
            String origin = parentProp.getOrigin();
            String siteRoot = CmsCoreProvider.get().getSiteRoot();
            if (origin.startsWith(siteRoot)) {
                origin = origin.substring(siteRoot.length());
            }
            return parentProp.withOrigin(origin);
        }
        return null;

    }

    /**
     * Returns a list of all descendant sitemap entries of a given path which have already been loaded on the client.<p>
     *
     * @param path the path for which the descendants should be collected
     *
     * @return the list of descendant sitemap entries
     */
    public List<CmsClientSitemapEntry> getLoadedDescendants(String path) {

        LinkedList<CmsClientSitemapEntry> remainingEntries = new LinkedList<CmsClientSitemapEntry>();
        List<CmsClientSitemapEntry> result = new ArrayList<CmsClientSitemapEntry>();
        CmsClientSitemapEntry entry = getEntry(path);
        remainingEntries.add(entry);
        while (remainingEntries.size() > 0) {
            CmsClientSitemapEntry currentEntry = remainingEntries.removeFirst();
            result.add(currentEntry);
            for (CmsClientSitemapEntry subEntry : currentEntry.getSubEntries()) {
                remainingEntries.add(subEntry);
            }
        }

        return result;

    }

    /**
     * Returns the no edit reason or <code>null</code> if editing is allowed.<p>
     *
     * @param entry the entry to get the no edit reason for
     *
     * @return the no edit reason
     */
    public String getNoEditReason(CmsClientSitemapEntry entry) {

        return getNoEditReason(entry, true);
    }

    /**
     * Returns the no edit reason or <code>null</code> if editing is allowed.<p>
     *
     * @param entry the entry to get the no edit reason for
     * @param checkChildLocks true if locks of children should be checked
     *
     * @return the no edit reason
     */
    public String getNoEditReason(CmsClientSitemapEntry entry, boolean checkChildLocks) {

        String reason = entry.getNoEditReason();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(reason)) {
            reason = null;
            if ((entry.getLock() != null)
                && (entry.getLock().getLockOwner() != null)
                && !entry.getLock().isOwnedByUser()) {
                reason = Messages.get().key(Messages.GUI_DISABLED_LOCKED_BY_1, entry.getLock().getLockOwner());

            }
            if (checkChildLocks && entry.hasBlockingLockedChildren()) {
                reason = Messages.get().key(Messages.GUI_DISABLED_BLOCKING_LOCKED_CHILDREN_0);
            }
        }
        return reason;
    }

    /**
     * Returns the parent entry of a sitemap entry, or null if it is the root entry.<p>
     *
     * @param entry a sitemap entry
     *
     * @return the parent entry or null
     */
    public CmsClientSitemapEntry getParentEntry(CmsClientSitemapEntry entry) {

        String path = entry.getSitePath();
        String parentPath = CmsResource.getParentFolder(path);
        if (parentPath == null) {
            return null;
        }
        return getEntry(parentPath);
    }

    /**
     * Gets the properties for a given structure id.<p>
     *
     * @param id the structure id of a sitemap entry
     *
     * @return the properties for that structure id
     */
    public Map<String, CmsClientProperty> getPropertiesForId(CmsUUID id) {

        return m_propertyMaps.get(id);
    }

    /**
     * Returns the sitemap service instance.<p>
     *
     * @return the sitemap service instance
     */
    public I_CmsSitemapServiceAsync getService() {

        if (m_service == null) {
            m_service = GWT.create(I_CmsSitemapService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.sitemap.CmsVfsSitemapService.gwt");
            ((ServiceDefTarget)m_service).setServiceEntryPoint(serviceUrl);
        }
        return m_service;
    }

    /**
     * Leaves the current sitemap to open the parent sitemap.<p>
     */
    public void gotoParentSitemap() {

        openSiteMap(getData().getParentSitemap());
    }

    /**
     * Hides the entry within the site navigation.<p>
     *
     * Hidden entries will still be visible in the navigation mode of the sitemap editor.
     * They will also have a NavText and a NavPos. Only when using the NavBuilder get navigation for folder method,
     * they will not be included.<p>
     *
     * @param entryId the entry id
     */
    public void hideInNavigation(CmsUUID entryId) {

        CmsClientSitemapEntry entry = getEntryById(entryId);
        CmsSitemapChange change = getChangeForEdit(
            entry,
            Collections.singletonList(
                new CmsPropertyModification(
                    entryId.toString()
                        + "/"
                        + CmsClientProperty.PROPERTY_NAVINFO
                        + "/"
                        + CmsClientProperty.PATH_STRUCTURE_VALUE,
                    CmsClientSitemapEntry.HIDDEN_NAVIGATION_ENTRY)));
        commitChange(change, null);
    }

    /**
     * Checks whether this entry belongs to a detail page.<p>
     *
     * @param entry the entry to check
     *
     * @return true if this entry belongs to a detail page
     */
    public boolean isDetailPage(CmsClientSitemapEntry entry) {

        return (entry.getDetailpageTypeName() != null) || isDetailPage(entry.getId());
    }

    /**
     * Returns true if the id is the id of a detail page.<p>
     *
     * @param id the sitemap entry id
     * @return true if the id is the id of a detail page entry
     */
    public boolean isDetailPage(CmsUUID id) {

        return m_allDetailPageInfos.containsKey(id);
    }

    /**
     * Checks if the current sitemap is editable.<p>
     *
     * @return <code>true</code> if the current sitemap is editable
     */
    public boolean isEditable() {

        return CmsStringUtil.isEmptyOrWhitespaceOnly(m_data.getNoEditReason());
    }

    /**
     * Checks whether a string is the name of a hidden property.<p>
     *
     * A hidden property is a property which should not appear in the property editor
     * because it requires special treatment.<p>
     *
     * @param propertyName the property name which should be checked
     *
     * @return true if the argument is the name of a hidden property
     */
    public boolean isHiddenProperty(String propertyName) {

        if (propertyName.equals("secure") && !m_data.isSecure()) {
            // "secure" property should not be editable in a site for which no secure server is configured
            return true;
        }

        return m_hiddenProperties.contains(propertyName);
    }

    /**
     * Returns true if the locale comparison mode is enabled.<p>
     *
     * @return true if the locale comparison mode is enabled
     */
    public boolean isLocaleComparisonEnabled() {

        return m_data.isLocaleComparisonEnabled();
    }

    /**
     * Checks if the given site path is the sitemap root.<p>
     *
     * @param sitePath the site path to check
     *
     * @return <code>true</code> if the given site path is the sitemap root
     */
    public boolean isRoot(String sitePath) {

        return m_data.getRoot().getSitePath().equals(sitePath);
    }

    /**
     * Ask to save the page before leaving, if necessary.<p>
     *
     * @param target the leaving target
     */
    public void leaveEditor(String target) {

        CmsUUID baseId = getData().getRoot().getId();
        CmsRpcAction<String> action = new CmsRpcAction<String>() {

            @Override
            public void execute() {

                start(0, false);
                getService().getResourceLink(baseId, target, this);
            }

            @Override
            protected void onResponse(String link) {

                Window.Location.assign(link);
            }

        };
        action.execute();
    }

    /**
     * Loads and displays the category data.<p>
     *
     * @param openLocalCategories true if the local category tree should be opened
     * @param openItemId the id of the item to open
     */
    public void loadCategories(final boolean openLocalCategories, final CmsUUID openItemId) {

        CmsRpcAction<CmsSitemapCategoryData> action = new CmsRpcAction<CmsSitemapCategoryData>() {

            @Override
            public void execute() {

                start(200, false);
                getService().getCategoryData(getEntryPoint(), this);
            }

            @Override
            protected void onResponse(CmsSitemapCategoryData result) {

                stop(false);
                m_categoryData = result;
                CmsSitemapView.getInstance().displayCategoryData(result, openLocalCategories, openItemId);
            }
        };
        action.execute();
    }

    /**
     * Loads all available galleries for the current sub site.<p>
     */
    public void loadGalleries() {

        CmsRpcAction<Map<CmsGalleryType, List<CmsGalleryFolderEntry>>> action = new CmsRpcAction<Map<CmsGalleryType, List<CmsGalleryFolderEntry>>>() {

            @Override
            public void execute() {

                start(500, false);
                getService().getGalleryData(m_data.getRoot().getSitePath(), this);
            }

            @Override
            protected void onResponse(Map<CmsGalleryType, List<CmsGalleryFolderEntry>> result) {

                storeGalleryTypes(result.keySet());
                CmsSitemapView.getInstance().displayGalleries(result);
                stop(false);
            }
        };
        action.execute();
    }

    /**
     * Loads the model pages.<p>
     */
    public void loadModelPages() {

        CmsRpcAction<CmsModelInfo> action = new CmsRpcAction<CmsModelInfo>() {

            @Override
            public void execute() {

                start(500, false);
                getService().getModelInfos(m_data.getRoot().getId(), this);

            }

            @Override
            protected void onResponse(CmsModelInfo result) {

                stop(false);
                CmsSitemapView.getInstance().displayModelPages(result);
            }
        };
        action.execute();
    }

    /**
     * Loads the new element info.<p>
     *
     * @param callback the callback to call when done
     */
    public void loadNewElementInfo(final AsyncCallback<Void> callback) {

        CmsRpcAction<List<CmsNewResourceInfo>> newResourceInfoAction = new CmsRpcAction<List<CmsNewResourceInfo>>() {

            @Override
            public void execute() {

                start(200, true);

                getService().getNewElementInfo(m_data.getRoot().getSitePath(), this);
            }

            @Override
            protected void onResponse(List<CmsNewResourceInfo> result) {

                stop(false);

                m_data.setNewElementInfos(result);
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

        };
        newResourceInfoAction.execute();
    }

    /**
     * Loads all entries on the given path.<p>
     *
     * @param sitePath the site path
     */
    public void loadPath(final String sitePath) {

        loadPath(sitePath, null);
    }

    /**
     * Loads the sitemap entry for the given site path.<p>
     *
     * @param sitePath the site path
     * @param callback the callback
     */
    public void loadPath(final String sitePath, final AsyncCallback<CmsClientSitemapEntry> callback) {

        loadPath(sitePath, false, callback);
    }

    /**
     * Loads all entries on the given path.<p>
     *
     * @param sitePath the site path
     * @param continueIfNotLoaded parameter passed to getChildren
     * @param callback the callback to execute when done
     */
    public void loadPath(
        final String sitePath,
        final boolean continueIfNotLoaded,
        final AsyncCallback<CmsClientSitemapEntry> callback) {

        if (getEntry(sitePath) != null) {
            CmsClientSitemapEntry entry = getEntry(sitePath);
            getChildren(entry.getId(), CmsSitemapTreeItem.getItemById(entry.getId()).isOpen(), callback);
        } else {
            String parentPath = CmsResource.getParentFolder(sitePath);
            CmsUUID idToLoad = null;
            CmsClientSitemapEntry entry = getEntry(parentPath);
            while (entry == null) {
                parentPath = CmsResource.getParentFolder(parentPath);
                if (parentPath == null) {
                    break;
                } else {
                    entry = getEntry(parentPath);
                }
            }
            if (entry != null) {
                idToLoad = entry.getId();
            } else {
                idToLoad = m_data.getSiteRootId();
            }
            CmsSitemapTreeItem treeItem = CmsSitemapTreeItem.getItemById(idToLoad);
            boolean open = true;
            if (treeItem != null) {
                open = treeItem.isOpen();
            }
            getChildren(idToLoad, open, continueIfNotLoaded, new AsyncCallback<CmsClientSitemapEntry>() {

                public void onFailure(Throwable caught) {

                    // nothing to do
                }

                public void onSuccess(CmsClientSitemapEntry result) {

                    // check if target entry is loaded
                    CmsClientSitemapEntry target = getEntry(sitePath);
                    if (target == null) {
                        loadPath(sitePath, continueIfNotLoaded, callback);
                    } else {
                        if (callback != null) {
                            callback.onSuccess(target);
                        }
                    }
                }
            });
        }
    }

    /**
    * Merges a subsitemap at the given id back into this sitemap.<p>
    *
    * @param entryId the id of the sub sitemap entry
    */
    public void mergeSubSitemap(final CmsUUID entryId) {

        CmsRpcAction<CmsSitemapChange> mergeAction = new CmsRpcAction<CmsSitemapChange>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, true);
                getService().mergeSubSitemap(getEntryPoint(), entryId, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsSitemapChange result) {

                stop(false);
                applyChange(result);

            }
        };
        mergeAction.execute();

    }

    /**
     * Moves the given sitemap entry with all its descendants to the new position.<p>
     *
     * @param entry the sitemap entry to move
     * @param toPath the destination path
     * @param position the new position between its siblings
     */
    public void move(CmsClientSitemapEntry entry, String toPath, int position) {

        // check for valid data
        if (!isValidEntryAndPath(entry, toPath)) {
            // invalid data, do nothing
            CmsDebugLog.getInstance().printLine("invalid data, doing nothing");
            return;
        }
        // check for relevance
        if (isChangedPosition(entry, toPath, position)) {
            // only register real changes
            CmsSitemapChange change = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.modify);
            change.setDefaultFileId(entry.getDefaultFileId());
            if (!toPath.equals(entry.getSitePath())) {
                change.setParentId(getEntry(CmsResource.getParentFolder(toPath)).getId());
                change.setName(CmsResource.getName(toPath));
            }
            if (CmsSitemapView.getInstance().isNavigationMode()) {
                change.setPosition(position);
            }
            change.setLeafType(entry.isLeafType());
            CmsSitemapClipboardData data = getData().getClipboardData().copy();
            data.addModified(entry);
            change.setClipBoardData(data);
            commitChange(change, null);
        }
    }

    /**
     * Opens the property dialog for the locale comparison mode.<p>
     *
     * @param structureId the structure id of the sitemap entry to edit
     * @param rootId the structure id of the resource comparing to the tree root in sitemap compare mode
     */
    public void openPropertyDialogForVaadin(final CmsUUID structureId, final CmsUUID rootId) {

        CmsRpcAction<CmsLocaleComparePropertyData> action = new CmsRpcAction<CmsLocaleComparePropertyData>() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void execute() {

                start(0, false);
                m_service.loadPropertyDataForLocaleCompareView(structureId, rootId, this);
            }

            @Override
            protected void onResponse(CmsLocaleComparePropertyData result) {

                stop(false);
                CmsSitemapController.editPropertiesForLocaleCompareMode(
                    result,
                    structureId,
                    result.getDefaultFileId(),
                    null);
            }
        };
        action.execute();

    }

    /**
     * Opens the site-map specified.<p>
     *
     * @param sitePath the site path to the site-map folder
     */
    public void openSiteMap(String sitePath) {

        openSiteMap(sitePath, false);
    }

    /**
     * Opens the site-map specified.<p>
     *
     * @param sitePath the site path to the site-map folder
     * @param siteChange in case the site was changed
     */
    public void openSiteMap(String sitePath, boolean siteChange) {

        String uri = CmsCoreProvider.get().link(
            CmsCoreProvider.get().getUri()) + "?" + CmsCoreData.PARAM_PATH + "=" + sitePath;
        if (!siteChange) {
            uri += "&" + CmsCoreData.PARAM_RETURNCODE + "=" + getData().getReturnCode();
        }
        Window.Location.replace(uri);
    }

    /**
     * Recomputes properties for all sitemap entries.<p>
     */
    public void recomputeProperties() {

        CmsClientSitemapEntry root = getData().getRoot();
        recomputeProperties(root);
    }

    /**
     * Refreshes the root entry.<p>
     *
     * @param callback the callback to call after the entry has been refreshed
     */
    public void refreshRoot(AsyncCallback<Void> callback) {

        updateEntry(getData().getRoot().getId(), callback);
    }

    /**
     * @see org.opencms.ade.sitemap.shared.I_CmsSitemapController#registerEntry(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry)
     */
    public void registerEntry(CmsClientSitemapEntry entry) {

        if (m_entriesById.containsKey(entry.getId())) {
            CmsClientSitemapEntry oldEntry = m_entriesById.get(entry.getId());
            oldEntry.update(entry);
            if (oldEntry != m_entriesByPath.get(oldEntry.getSitePath())) {
                m_entriesByPath.put(oldEntry.getSitePath(), oldEntry);
            }
        } else {
            m_entriesById.put(entry.getId(), entry);
            m_entriesByPath.put(entry.getSitePath(), entry);
        }

    }

    /**
     * @see org.opencms.ade.sitemap.shared.I_CmsSitemapController#registerPathChange(org.opencms.ade.sitemap.shared.CmsClientSitemapEntry, java.lang.String)
     */
    public void registerPathChange(CmsClientSitemapEntry entry, String oldPath) {

        m_entriesById.put(entry.getId(), entry);
        m_entriesByPath.remove(oldPath);
        m_entriesByPath.put(entry.getSitePath(), entry);
    }

    /**
     * Removes the entry with the given site-path from navigation.<p>
     *
     * @param entryId the entry id
     */
    public void removeFromNavigation(CmsUUID entryId) {

        CmsClientSitemapEntry entry = getEntryById(entryId);
        CmsClientSitemapEntry parent = getEntry(CmsResource.getParentFolder(entry.getSitePath()));
        CmsSitemapChange change = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.remove);
        change.setParentId(parent.getId());
        change.setDefaultFileId(entry.getDefaultFileId());
        CmsSitemapClipboardData data = CmsSitemapView.getInstance().getController().getData().getClipboardData().copy();
        data.addModified(entry);
        change.setClipBoardData(data);
        //TODO: handle detail page delete

        commitChange(change, null);
    }

    /**
     * Removes a model page from the sitemap's configuration.<p>
     *
     * @param id the structure id of the model page
     *
     * @param asyncCallback the callback to call when done
     */
    public void removeModelPage(final CmsUUID id, final AsyncCallback<Void> asyncCallback) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(200, true);
                getService().removeModelPage(getEntryPoint(), id, this);

            }

            @Override
            protected void onResponse(Void result) {

                stop(false);
                loadNewElementInfo(null);
                asyncCallback.onSuccess(null);

            }
        };
        action.execute();

    }

    /**
     * @see org.opencms.ade.sitemap.shared.I_CmsSitemapController#replaceProperties(org.opencms.util.CmsUUID, java.util.Map)
     */
    public Map<String, CmsClientProperty> replaceProperties(CmsUUID id, Map<String, CmsClientProperty> properties) {

        if ((id == null) || (properties == null)) {
            return null;
        }
        Map<String, CmsClientProperty> props = m_propertyMaps.get(id);
        if (props == null) {
            props = properties;
            m_propertyMaps.put(id, props);
        } else {
            props.clear();
            props.putAll(properties);
        }
        return props;

    }

    /**
     * Sets the editor mode in the user session.<p>
     *
     * @param editorMode the editor mode
     */
    public void setEditorModeInSession(final EditorMode editorMode) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                getService().setEditorMode(editorMode, this);
            }

            @Override
            protected void onResponse(Void result) {

                // nothing to do

            }
        };
        action.execute();
    }

    /**
     * Shows a formerly hidden entry in the navigation.<p>
     *
     * @see #hideInNavigation(CmsUUID)
     *
     * @param entryId the entry id
     */
    public void showInNavigation(CmsUUID entryId) {

        CmsClientSitemapEntry entry = getEntryById(entryId);
        CmsSitemapChange change = getChangeForEdit(
            entry,
            Collections.singletonList(
                new CmsPropertyModification(
                    entryId.toString()
                        + "/"
                        + CmsClientProperty.PROPERTY_NAVINFO
                        + "/"
                        + CmsClientProperty.PATH_STRUCTURE_VALUE,
                    "")));
        commitChange(change, null);
    }

    /**
     * Undeletes the resource with the given structure id.<p>
     *
     * @param entryId the entry id
     * @param sitePath the site-path
     */
    public void undelete(CmsUUID entryId, String sitePath) {

        CmsSitemapChange change = new CmsSitemapChange(entryId, sitePath, ChangeType.undelete);
        CmsSitemapClipboardData data = CmsSitemapView.getInstance().getController().getData().getClipboardData().copy();
        data.getDeletions().remove(entryId);
        change.setClipBoardData(data);
        commitChange(change, null);
    }

    /**
     * Updates the given entry.<p>
     *
     * @param entryId the entry id
      */
    public void updateEntry(CmsUUID entryId) {

        getChildren(entryId, CmsSitemapTreeItem.getItemById(entryId).isOpen(), null);
    }

    /**
     * Updates the given entry.<p>
     *
     * @param entryId the entry id
     * @param callback the callback to call after the entry has been updated
      */
    public void updateEntry(CmsUUID entryId, final AsyncCallback<Void> callback) {

        getChildren(
            entryId,
            CmsSitemapTreeItem.getItemById(entryId).isOpen(),
            new AsyncCallback<CmsClientSitemapEntry>() {

                public void onFailure(Throwable caught) {

                    // nothing to do

                }

                public void onSuccess(CmsClientSitemapEntry result) {

                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                }
            });
    }

    /**
    * Updates the given entry.<p>
    *
    * @param sitePath the entry sitepath
     */
    public void updateEntry(String sitePath) {

        CmsClientSitemapEntry entry = getEntry(sitePath);
        if ((entry != null) && (CmsSitemapTreeItem.getItemById(entry.getId()) != null)) {
            getChildren(entry.getId(), CmsSitemapTreeItem.getItemById(entry.getId()).isOpen(), null);
        }
    }

    /**
     * Updates the given entry only, not evaluating any child changes.<p>
     *
     * @param entryId the entry id
     */
    public void updateSingleEntry(final CmsUUID entryId) {

        CmsRpcAction<CmsClientSitemapEntry> getChildrenAction = new CmsRpcAction<CmsClientSitemapEntry>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                getService().getChildren(getEntryPoint(), entryId, 0, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(CmsClientSitemapEntry result) {

                CmsClientSitemapEntry target = getEntryById(entryId);
                if (target == null) {
                    // this might happen after an automated deletion
                    stop(false);
                    return;
                }
                target.update(result);
                CmsSitemapTreeItem item = CmsSitemapTreeItem.getItemById(target.getId());
                item.updateEntry(target);
            }
        };
        getChildrenAction.execute();
    }

    /**
     * Fires a sitemap change event.<p>
     *
     * @param change the change event to fire
     */
    protected void applyChange(CmsSitemapChange change) {

        // update the clip board data
        if (change.getClipBoardData() != null) {
            getData().getClipboardData().setDeletions(change.getClipBoardData().getDeletions());
            getData().getClipboardData().setModifications(change.getClipBoardData().getModifications());
        }
        switch (change.getChangeType()) {
            case bumpDetailPage:
                CmsDetailPageTable detailPageTable = getData().getDetailPageTable();
                if (detailPageTable.contains(change.getEntryId())) {
                    detailPageTable.makeDefault(change.getEntryId());
                }
                break;
            case clipboardOnly:
                // nothing to do
                break;
            case remove:
                CmsClientSitemapEntry entry = getEntryById(change.getEntryId());
                entry.setInNavigation(false);
                CmsPropertyModification propMod = new CmsPropertyModification(
                    entry.getId(),
                    CmsClientProperty.PROPERTY_NAVTEXT,
                    null,
                    true);
                executePropertyModification(propMod);
                propMod = new CmsPropertyModification(entry.getId(), CmsClientProperty.PROPERTY_NAVTEXT, null, true);
                executePropertyModification(propMod);
                entry.normalizeProperties();
                break;
            case undelete:
                updateEntry(change.getParentId());
                break;
            case create:
                CmsClientSitemapEntry newEntry = change.getUpdatedEntry();
                getEntryById(change.getParentId()).insertSubEntry(newEntry, change.getPosition(), this);
                newEntry.initializeAll(this);
                if (isDetailPage(newEntry)) {
                    CmsDetailPageInfo info = getDetailPageInfo(newEntry.getId());
                    if (info == null) {
                        info = new CmsDetailPageInfo(
                            newEntry.getId(),
                            newEntry.getSitePath(),
                            newEntry.getDetailpageTypeName(),
                            /* qualifier = */null,
                            newEntry.getVfsModeIcon());
                    }
                    addDetailPageInfo(info);
                }
                break;
            case delete:
                removeEntry(change.getEntryId(), change.getParentId());
                break;
            case modify:
                if (change.hasNewParent() || change.hasChangedPosition()) {
                    CmsClientSitemapEntry moved = getEntryById(change.getEntryId());
                    String oldSitepath = moved.getSitePath();
                    CmsClientSitemapEntry sourceParent = getEntry(CmsResource.getParentFolder(oldSitepath));
                    sourceParent.removeSubEntry(moved.getId());
                    CmsClientSitemapEntry destParent = change.hasNewParent()
                    ? getEntryById(change.getParentId())
                    : sourceParent;
                    if (change.getPosition() < destParent.getSubEntries().size()) {
                        destParent.insertSubEntry(moved, change.getPosition(), this);
                    } else {
                        // inserting as last entry of the parent list
                        destParent.addSubEntry(moved, this);
                    }
                    if (change.hasNewParent()) {
                        cleanupOldPaths(oldSitepath, destParent.getSitePath());
                    }
                }
                if (change.hasChangedName()) {
                    CmsClientSitemapEntry changed = getEntryById(change.getEntryId());
                    String oldSitepath = changed.getSitePath();
                    String parentPath = CmsResource.getParentFolder(oldSitepath);
                    String newSitepath = CmsStringUtil.joinPaths(parentPath, change.getName());
                    changed.updateSitePath(newSitepath, this);
                    cleanupOldPaths(oldSitepath, newSitepath);
                }
                if (change.hasChangedProperties()) {
                    for (CmsPropertyModification modification : change.getPropertyChanges()) {
                        executePropertyModification(modification);
                    }
                    getEntryById(change.getEntryId()).normalizeProperties();
                }
                if (change.getUpdatedEntry() != null) {
                    CmsClientSitemapEntry oldEntry = getEntryById(change.getEntryId());
                    CmsClientSitemapEntry parent = getEntry(CmsResource.getParentFolder(oldEntry.getSitePath()));
                    removeEntry(change.getEntryId(), parent.getId());
                    parent.insertSubEntry(change.getUpdatedEntry(), oldEntry.getPosition(), this);
                    change.getUpdatedEntry().initializeAll(this);
                }
                break;
            default:
        }
        if (change.getChangeType() != ChangeType.delete) {
            recomputeProperties();
        }
        m_eventBus.fireEventFromSource(new CmsSitemapChangeEvent(change), this);
    }

    /**
    * Adds a change to the queue.<p>
    *
    * @param change the change to commit
    * @param callback the callback to execute after the change has been applied
    */
    protected void commitChange(final CmsSitemapChange change, final Command callback) {

        if (change != null) {
            // save the sitemap
            CmsRpcAction<CmsSitemapChange> saveAction = new CmsRpcAction<CmsSitemapChange>() {

                /**
                * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
                */
                @Override
                public void execute() {

                    start(0, true);
                    getService().save(getEntryPoint(), change, this);

                }

                @Override
                public void onFailure(Throwable t) {
                    // An error after a drag/drop operation can cause click events to get sent to the sitemap entry's move handle
                    // instead of the error dialog's buttons. We use releaseCapture() to fix this.
                    DOM.releaseCapture(DOM.getCaptureElement());
                    super.onFailure(t);
                }

                /**
                * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
                */
                @Override
                public void onResponse(CmsSitemapChange result) {

                    stop(false);

                    applyChange(result);
                    if (callback != null) {
                        callback.execute();
                    }
                }
            };
            saveAction.execute();
        }
    }

    /**
     * Creates a change object for an edit operation.<p>
     *
     * @param entry the edited sitemap entry
     * @param propertyChanges the list of property changes
     *
     * @return the change object
     */
    protected CmsSitemapChange getChangeForEdit(
        CmsClientSitemapEntry entry,
        List<CmsPropertyModification> propertyChanges) {

        CmsSitemapChange change = new CmsSitemapChange(entry.getId(), entry.getSitePath(), ChangeType.modify);
        change.setDefaultFileId(entry.getDefaultFileId());
        change.setLeafType(entry.isLeafType());
        List<CmsPropertyModification> propertyChangeData = new ArrayList<CmsPropertyModification>();
        for (CmsPropertyModification propChange : propertyChanges) {
            propertyChangeData.add(propChange);
        }
        change.setPropertyChanges(propertyChangeData);

        CmsSitemapClipboardData data = getData().getClipboardData().copy();
        data.addModified(entry);
        change.setClipBoardData(data);
        return change;
    }

    /**
     * Returns the URI of the current sitemap.<p>
     *
     * @return the URI of the current sitemap
     */
    protected String getEntryPoint() {

        return m_data.getRoot().getSitePath();

    }

    /**
     * Helper method for getting the full path of a sitemap entry whose URL name is being edited.<p>
     *
     * @param entry the sitemap entry
     * @param newUrlName the new url name of the sitemap entry
     *
     * @return the new full site path of the sitemap entry
     */
    protected String getPath(CmsClientSitemapEntry entry, String newUrlName) {

        if (newUrlName.equals("")) {
            return entry.getSitePath();
        }
        return CmsResource.getParentFolder(entry.getSitePath()) + newUrlName + "/";
    }

    /**
     * Returns the sitemap service instance.<p>
     *
     * @return the sitemap service instance
     */
    protected I_CmsVfsServiceAsync getVfsService() {

        if (m_vfsService == null) {
            m_vfsService = CmsCoreProvider.getVfsService();
        }
        return m_vfsService;
    }

    /**
     * Initializes the detail page information.<p>
     */
    protected void initDetailPageInfos() {

        for (CmsUUID id : m_data.getDetailPageTable().getAllIds()) {
            CmsDetailPageInfo info = m_data.getDetailPageTable().get(id);
            m_allDetailPageInfos.put(id, info);
        }
    }

    /**
     * Creates a new client sitemap entry bean to use for the RPC call which actually creates the entry on the server side.<p>
     *
     * @param parent the parent entry
     * @param callback the callback to execute
     */
    protected void makeNewEntry(
        final CmsClientSitemapEntry parent,
        final I_CmsSimpleCallback<CmsClientSitemapEntry> callback) {

        ensureUniqueName(parent, NEW_ENTRY_NAME, new I_CmsSimpleCallback<String>() {

            public void execute(String urlName) {

                CmsClientSitemapEntry newEntry = new CmsClientSitemapEntry();
                //newEntry.setTitle(urlName);
                newEntry.setName(urlName);
                String sitePath = parent.getSitePath() + urlName + "/";
                newEntry.setSitePath(sitePath);
                newEntry.setVfsPath(null);
                newEntry.setPosition(0);
                newEntry.setNew(true);
                newEntry.setInNavigation(true);
                newEntry.setResourceTypeName("folder");
                newEntry.getOwnProperties().put(
                    CmsClientProperty.PROPERTY_TITLE,
                    new CmsClientProperty(CmsClientProperty.PROPERTY_TITLE, NEW_ENTRY_NAME, NEW_ENTRY_NAME));
                callback.execute(newEntry);
            }
        });

    }

    /**
     * Recomputes the properties for a client sitemap entry.<p>
     *
     * @param entry the entry for whose descendants the properties should be recomputed
     */
    protected void recomputeProperties(CmsClientSitemapEntry entry) {

        for (I_CmsPropertyUpdateHandler handler : m_propertyUpdateHandlers) {
            handler.handlePropertyUpdate(entry);
        }
        for (CmsClientSitemapEntry child : entry.getSubEntries()) {
            recomputeProperties(child);
        }
    }

    /**
     * Store the gallery type information.<p>
     *
     * @param galleryTypes the gallery types
     */
    void storeGalleryTypes(Collection<CmsGalleryType> galleryTypes) {

        m_galleryTypes.clear();
        for (CmsGalleryType type : galleryTypes) {
            m_galleryTypes.put(Integer.valueOf(type.getTypeId()), type);
        }
    }

    /**
     * Cleans up wrong path references.<p>
     *
     * @param oldSitepath the old sitepath
     * @param newSitepath the new sitepath
     */
    private void cleanupOldPaths(String oldSitepath, String newSitepath) {

        // use a separate list to avoid concurrent changes
        List<CmsClientSitemapEntry> entries = new ArrayList<CmsClientSitemapEntry>(m_entriesById.values());
        for (CmsClientSitemapEntry entry : entries) {
            if (entry.getSitePath().startsWith(oldSitepath)) {
                String currentPath = entry.getSitePath();
                String partAfterOldSitePath = currentPath.substring(oldSitepath.length());

                String updatedSitePath = "".equals(partAfterOldSitePath) ? newSitepath : CmsStringUtil.joinPaths(
                    newSitepath,
                    partAfterOldSitePath);
                entry.updateSitePath(updatedSitePath, this);
            }
        }
    }

    /**
     * Returns the properties above the sitemap root.<p>
     *
     * @return the map of properties of the root's parent
     */
    private Map<String, CmsClientProperty> getParentProperties() {

        return m_data.getParentProperties();
    }

    /**
     * Checks if the given path and position indicate a changed position for the entry.<p>
     *
     * @param entry the sitemap entry to move
     * @param toPath the destination path
     * @param position the new position between its siblings
     *
     * @return <code>true</code> if this is a position change
     */
    private boolean isChangedPosition(CmsClientSitemapEntry entry, String toPath, int position) {

        return (!entry.getSitePath().equals(toPath) || (entry.getPosition() != position));
    }

    /**
     * Validates the entry and the given path.<p>
     *
     * @param entry the entry
     * @param toPath the path
     *
     * @return <code>true</code> if entry and path are valid
     */
    private boolean isValidEntryAndPath(CmsClientSitemapEntry entry, String toPath) {

        return ((toPath != null)
            && (CmsResource.getParentFolder(toPath) != null)
            && (entry != null)
            && (getEntry(CmsResource.getParentFolder(toPath)) != null)
            && (getEntry(entry.getSitePath()) != null));
    }

    /**
     * Removes all children of the given entry recursively from the data model.<p>
     *
     * @param entry the entry
     */
    private void removeAllChildren(CmsClientSitemapEntry entry) {

        for (CmsClientSitemapEntry child : entry.getSubEntries()) {
            removeAllChildren(child);
            m_entriesById.remove(child.getId());
            m_entriesByPath.remove(child.getSitePath());
            // apply to detailpage table
            CmsDetailPageTable detailPageTable = getData().getDetailPageTable();
            if (detailPageTable.contains(child.getId())) {
                detailPageTable.remove(child.getId());
            }
        }
        entry.getSubEntries().clear();
    }

    /**
     * Removes the given entry and it's descendants from the modified list.<p>
     *
     * @param entry the entry
     * @param data the clip board data
     */
    private void removeDeletedFromModified(CmsClientSitemapEntry entry, CmsSitemapClipboardData data) {

        data.removeModified(entry);
        for (CmsClientSitemapEntry child : entry.getSubEntries()) {
            removeDeletedFromModified(child, data);
        }
    }

    /**
     * Removes the given entry from the data model.<p>
     *
     * @param entryId the id of the entry to remove
     * @param parentId the parent entry id
     */
    private void removeEntry(CmsUUID entryId, CmsUUID parentId) {

        CmsClientSitemapEntry entry = getEntryById(entryId);
        CmsClientSitemapEntry deleteParent = getEntryById(parentId);
        removeAllChildren(entry);
        deleteParent.removeSubEntry(entry.getPosition());
        m_entriesById.remove(entryId);
        m_entriesByPath.remove(entry.getSitePath());
        // apply to detailpage table
        CmsDetailPageTable detailPageTable = getData().getDetailPageTable();
        if (detailPageTable.contains(entryId)) {
            detailPageTable.remove(entryId);
        }
    }
}
