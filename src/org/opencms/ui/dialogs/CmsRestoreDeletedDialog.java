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
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.CmsVfsService;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * Dialog for restoring deleted resources in a folder.<p>
 */
public class CmsRestoreDeletedDialog extends CmsBasicDialog {

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

    /** The resource. */
    private CmsResource m_resource;

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
        m_resource = context.getResources().get(0);
        CmsObject cms = context.getCms();
        List<I_CmsHistoryResource> deletedResources = cms.readDeletedResources(
            cms.getSitePath(m_resource),
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
            historyResources = cms.readDeletedResources(cms.getSitePath(m_resource), value.booleanValue());
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
            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(deleted.getTypeId());
            String typeName = resType.getTypeName();
            CmsExplorerTypeSettings explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);
            String title = cms.getRequestContext().removeSiteRoot(deleted.getRootPath());

            String subtitle = CmsVaadinUtils.getMessageText(
                org.opencms.ui.Messages.GUI_RESTOREDELETED_DATE_VERSION_2,
                CmsVfsService.formatDateTime(cms, deleted.getDateLastModified()),
                "" + deleted.getVersion());
            String iconPath = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                deleted.isFile() ? "unknown_file" : "unknown_folder").getBigIconIfAvailable();
            if (explorerType != null) {
                iconPath = CmsWorkplace.RES_PATH_FILETYPES + explorerType.getBigIconIfAvailable();
            }
            CmsResourceInfo info = new CmsResourceInfo(title, subtitle, CmsWorkplace.getResourceUri(iconPath));
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

}
