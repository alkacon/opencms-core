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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.history.CmsHistoryProject;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.CmsResourceTypeUnknownFile;
import org.opencms.file.types.CmsResourceTypeUnknownFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.CmsVfsService;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;

/**
 * Dialog for restoring deleted resources in a folder.<p>
 */
public class CmsRestoreDeletedDialog extends CmsBasicDialog {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRestoreDeletedDialog.class);

    /** Property for storing selection status. */
    private static final String PROP_SELECTED = "selected";

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The cancel button. */
    private Button m_cancelButton;

    /** The box containing the widgets representing the deleted resources. */
    private AbstractOrderedLayout m_deletedResourceContainer;

    /** The dialog context. */
    private I_CmsDialogContext m_dialogContext;

    /** Checkbox for including subfolders. */
    private CheckBox m_includeSubfoldersField;

    /** The OK button. */
    private Button m_okButton;

    /** Check box to select all resources. */
    private CheckBox m_selectAllField;

    /** Data model for check boxes / selection. */
    private IndexedContainer m_selectionContainer;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     * @throws CmsException if something goes wrong
     */
    public CmsRestoreDeletedDialog(I_CmsDialogContext context)
    throws CmsException {

        m_dialogContext = context;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        CmsObject cms = context.getCms();
        List<I_CmsHistoryResource> deletedResources = readDeletedResources(
            m_includeSubfoldersField.getValue().booleanValue());
        initDeletedResources(cms, deletedResources);
        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                cancel();
            }
        });

        m_includeSubfoldersField.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                onSubFolderChange((Boolean)event.getProperty().getValue());
            }
        });

        m_selectAllField.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                onSelectAllChange((Boolean)(event.getProperty().getValue()));
            }
        });
        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                submit();
            }
        });

        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                CmsRestoreDeletedDialog.this.cancel();
            }

            @Override
            protected void ok() {

                submit();
            }
        });
    }

    /**
     * Gets the ids of the selected resources.<p>
     *
     * @return the ids of the selected resources
     */
    public List<CmsUUID> getSelectedIds() {

        List<?> itemIds = m_selectionContainer.getItemIds();
        List<CmsUUID> result = Lists.newArrayList();
        for (Object itemId : itemIds) {
            CmsUUID structureId = (CmsUUID)itemId;
            Boolean value = (Boolean)(m_selectionContainer.getItem(itemId).getItemProperty(PROP_SELECTED).getValue());
            if (value.booleanValue()) {
                result.add(structureId);
            }
        }
        return result;
    }

    /**
     * Cancels the dialog.<p>
     */
    void cancel() {

        m_dialogContext.finish(new ArrayList<CmsUUID>());
    }

    /**
     * Called on select all change.<p>
     *
     * @param value the new value
     */
    void onSelectAllChange(Boolean value) {

        for (Object id : m_selectionContainer.getItemIds()) {
            m_selectionContainer.getItem(id).getItemProperty(PROP_SELECTED).setValue(value);
        }
    }

    /**
     * Called on include sub folders change.<p>
     *
     * @param value the new value
     */
    void onSubFolderChange(Boolean value) {

        List<I_CmsHistoryResource> historyResources;
        try {
            CmsObject cms = m_dialogContext.getCms();
            historyResources = readDeletedResources(value.booleanValue());
            initDeletedResources(cms, historyResources);
        } catch (CmsException e) {
            m_dialogContext.error(e);
        }
    }

    /**
     * Submits the dialog.<p>
     */
    void submit() {

        List<CmsUUID> selectedIds = getSelectedIds();
        List<CmsUUID> updated = Lists.newArrayList();
        CmsObject cms = m_dialogContext.getCms();
        try {
            for (CmsUUID selectedId : selectedIds) {
                cms.restoreDeletedResource(selectedId);
                updated.add(selectedId);
            }
            m_dialogContext.finish(updated);
        } catch (CmsException e) {
            m_dialogContext.error(e);
        }
    }

    /**
     * Fills the list of resources to select from.<p>
     *
     * @param cms the cms context
     * @param deletedResources the deleted resources
     *
     * @throws CmsException if something goes wrong
     */
    private void initDeletedResources(CmsObject cms, List<I_CmsHistoryResource> deletedResources) throws CmsException {

        Collections.sort(deletedResources, new Comparator<I_CmsHistoryResource>() {

            public int compare(I_CmsHistoryResource first, I_CmsHistoryResource second) {

                return first.getRootPath().compareTo(second.getRootPath());
            }
        });
        m_deletedResourceContainer.removeAllComponents();
        m_selectionContainer = new IndexedContainer();
        m_selectionContainer.addContainerProperty(PROP_SELECTED, Boolean.class, Boolean.FALSE);
        m_okButton.setEnabled(!deletedResources.isEmpty());
        if (deletedResources.isEmpty()) {
            m_deletedResourceContainer.addComponent(
                new Label(CmsVaadinUtils.getMessageText(org.opencms.workplace.list.Messages.GUI_LIST_EMPTY_0)));

        }
        for (I_CmsHistoryResource deleted : deletedResources) {
            CmsExplorerTypeSettings explorerType;
            try {
                I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(deleted.getTypeId());
                String typeName = resType.getTypeName();
                explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);
            } catch (CmsLoaderException e) {
                explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                    deleted.isFile()
                    ? CmsResourceTypeUnknownFile.getStaticTypeName()
                    : CmsResourceTypeUnknownFolder.getStaticTypeName());
            }
            String title = cms.getRequestContext().removeSiteRoot(deleted.getRootPath());

            long deletionDate = 0;
            try {
                CmsHistoryProject hp = cms.readHistoryProject(deleted.getPublishTag());
                if (hp != null) {
                    deletionDate = hp.getPublishingDate();
                }
            } catch (CmsException e) {
                LOG.debug(
                    "Failed to retrieve deletion date for deleted resource "
                        + deleted.getRootPath()
                        + ". Last modification date will be shown.");
            }
            String subtitle = CmsVaadinUtils.getMessageText(
                org.opencms.ui.Messages.GUI_RESTOREDELETED_DATE_VERSION_2,
                CmsVfsService.formatDateTime(cms, deletionDate == 0 ? deleted.getDateLastModified() : deletionDate),
                "" + deleted.getVersion());
            if (explorerType == null) {
                explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                    deleted.isFile()
                    ? CmsResourceTypeUnknownFile.RESOURCE_TYPE_NAME
                    : CmsResourceTypeUnknownFolder.RESOURCE_TYPE_NAME);
            }
            CmsResourceInfo info = new CmsResourceInfo(
                title,
                subtitle,
                CmsResourceUtil.getBigIconResource(explorerType, deleted.getName()));
            info.setWidth("100%");
            HorizontalLayout hl = new HorizontalLayout();
            hl.setWidth("100%");
            CheckBox checkbox = new CheckBox();
            hl.addComponent(checkbox);
            hl.addComponent(info);
            hl.setExpandRatio(info, 1);
            hl.setComponentAlignment(checkbox, Alignment.MIDDLE_LEFT);
            m_selectionContainer.addItem(deleted.getStructureId());
            checkbox.setPropertyDataSource(
                m_selectionContainer.getItem(deleted.getStructureId()).getItemProperty(PROP_SELECTED));
            m_deletedResourceContainer.addComponent(hl);
        }
    }

    /**
     * Reads the deleted resources in the folders selected for the dialog.
     *
     * @param includeSubFolders true if deleted resources in subfolders should be included
     * @return the list of deleted resources
     *
     * @throws CmsException if something goes wrong
     */
    private List<I_CmsHistoryResource> readDeletedResources(boolean includeSubFolders) throws CmsException {

        CmsObject cms = m_dialogContext.getCms();
        List<I_CmsHistoryResource> result = new ArrayList<>();
        for (CmsResource res : m_dialogContext.getResources()) {
            result.addAll(cms.readDeletedResources(cms.getSitePath(res), includeSubFolders));
        }
        return result;
    }

}
