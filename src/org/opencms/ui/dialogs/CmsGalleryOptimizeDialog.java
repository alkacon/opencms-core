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

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsPermalinkResourceHandler;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.components.CmsGwtContextMenuButton;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.shared.rpc.I_CmsGwtContextMenuServerRpc;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.SerializableComparator;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WrappedSession;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ItemCaptionGenerator;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Class representing a dialog for optimizing galleries.<p>
 */
public class CmsGalleryOptimizeDialog extends CmsBasicDialog {

    /**
     * The context used for child dialogs.<p>
     */
    public class ContextMenu implements I_CmsGwtContextMenuServerRpc {

        /** Default serial version uid. */
        private static final long serialVersionUID = 1L;

        /**
         * The data item handled by this dialog context.
         */
        private final DataItem m_dataItem;

        /**
         * Creates a new instance.
        
         * @param dataItem the data item
         */
        public ContextMenu(DataItem dataItem) {

            m_dataItem = dataItem;
        }

        /**
         * @see org.opencms.ui.shared.rpc.I_CmsGwtContextMenuServerRpc#refresh(java.lang.String)
         */
        public void refresh(String uuid) {

            if (uuid != null) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    CmsResource resource = getCms().readResource(new CmsUUID(uuid), CmsResourceFilter.ONLY_VISIBLE);
                    boolean deleted = resource.getState() == CmsResourceState.STATE_DELETED;
                    if (deleted) {
                        CmsGalleryOptimizeDialog.this.handleDataListDelete(Arrays.asList(m_dataItem));
                    } else {
                        CmsGalleryOptimizeDialog.this.handleDataListUpdate(Arrays.asList(m_dataItem));
                    }
                    String message = CmsVaadinUtils.getMessageText(
                        Messages.GUI_GALLERY_OPTIMIZE_LABEL_SUCCESSFULLY_SAVED_0);
                    Notification notification = new Notification(message, "", Notification.Type.HUMANIZED_MESSAGE);
                    notification.setPosition(Position.TOP_CENTER);
                    notification.show(Page.getCurrent());
                    CmsAppWorkplaceUi.get().enableGlobalShortcuts();
                } catch (CmsException | InterruptedException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    Notification notification = new Notification(
                        "",
                        e.getLocalizedMessage(),
                        Notification.Type.ERROR_MESSAGE);
                    notification.setHtmlContentAllowed(true);
                    notification.setPosition(Position.TOP_CENTER);
                    notification.show(Page.getCurrent());
                }
            }
        }
    }

    /**
     * Class representing an editable gallery item.<p>
     */
    private class DataItem {

        /** The data binder of this editable gallery item. */
        private Binder<DataItem> m_binder = new Binder<DataItem>();

        /** The form composite of this editable gallery item. */
        private FormComposite m_compositeForm;

        /** The file composite of this editable gallery item. */
        private FileComposite m_compositeFile;

        /** The file delete composite of this editable gallery item. */
        private FileDeleteComposite m_compositeFileDelete;

        /** The copyright information of this editable gallery item. */
        private String m_copyright;

        /** Date when this editable gallery item was last modified. */
        private Long m_dateLastModified;

        /** Whether this editable gallery item shall be deleted. */
        private Boolean m_deleteFlag = Boolean.valueOf(false);

        /** The description of this editable gallery item. */
        private String m_description;

        /** Whether this editable gallery item is used. */
        private Boolean m_isUsed;

        /** The file name of this editable gallery item. */
        private String m_name;

        /** The full path of this editable gallery item. */
        private String m_path;

        /** The CMS resource of this editable gallery item. */
        private CmsResource m_resource;

        /** The CMS resource utility of this editable gallery item. */
        private CmsResourceUtil m_resourceUtil;

        /** The title of this editable gallery item. */
        private String m_title;

        /**
         * Creates a new editable gallery item for a given CMS resource.<p>
         *
         * @param resource the CMS resource
         */
        public DataItem(CmsResource resource) {

            m_resource = resource;
            initData();
            initComponent();
        }

        /**
         * Returns the binder of this editable gallery item.<p>
         *
         * @return the binder
         */
        public Binder<DataItem> getBinder() {

            return m_binder;
        }

        /**
         * Returns the file composite of this editable gallery item.<p>
         *
         * @return the file composite
         */
        public FileComposite getCompositeFile() {

            return m_compositeFile;
        }

        /**
         * Returns the file delete composite of this editable gallery item.<p>
         *
         * @return the file delete composite
         */
        public FileDeleteComposite getCompositeFileDelete() {

            return m_compositeFileDelete;
        }

        /**
         * Returns the form composite of this editable gallery item.<p>
         *
         * @return the form composite
         */
        public FormComposite getCompositeForm() {

            return m_compositeForm;
        }

        /**
         * Returns the copyright information of this editable gallery item.<p>
         *
         * @return the copyright information
         */
        public String getCopyright() {

            return m_copyright;
        }

        /**
         * Returns the date when this editable gallery item was last modified.<p>
         *
         * @return the date
         */
        public Long getDateLastModified() {

            return m_dateLastModified;
        }

        /**
         * Returns whether this editable gallery item shall be deleted.<p>
         *
         * @return whether delete or not
         */
        public Boolean getDeleteFlag() {

            return m_deleteFlag;
        }

        /**
         * Returns the description of this editable gallery item.<p>
         *
         * @return the description
         */
        public String getDescription() {

            return m_description;
        }

        /**
         * Returns the filter text.<p>
         *
         * @return the filter text
         */
        public String getFilterText() {

            return (m_name + " " + m_title + " " + m_copyright + " " + m_description).toLowerCase();
        }

        /**
         * Returns whether this editable gallery item is used.<p>
         *
         * @return whether used or not
         */
        public Boolean getIsUsed() {

            return m_isUsed;
        }

        /**
         * Returns the name of this editable gallery item.<p>
         *
         * @return the name
         */
        public String getName() {

            return m_name;
        }

        /**
         * Returns whether this data item has no copyright information.<p>
         *
         * @return whether this data item has no copyright information
         */
        public Boolean getNoCopyright() {

            return Boolean.valueOf(CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_copyright));
        }

        /**
         * Returns the full path of this editable gallery item.<p>
         *
         * @return the full path
         */
        public String getPath() {

            return m_path;
        }

        /**
         * Converts the form data of this editable gallery item into a list of CMS properties.<p>
         *
         * @return the CMS property list
         */
        public List<CmsProperty> getPropertyList() {

            List<CmsProperty> propertyList = new ArrayList<CmsProperty>();
            propertyList.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, m_title, null));
            propertyList.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_COPYRIGHT, m_copyright, null));
            propertyList.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, m_description, null));
            return propertyList;
        }

        /**
         * Returns the CMS resource this editable gallery item was created from.<p>
         *
         * @return the CMS resource
         */
        public CmsResource getResource() {

            return m_resource;
        }

        /**
         * Returns the CMS resource utility class for this editable gallery item.<p>
         *
         * @return the CMS resource utility class
         */
        public CmsResourceUtil getResourceUtil() {

            return m_resourceUtil;
        }

        /**
         * Returns the title of this editable gallery item.<p>
         *
         * @return the title
         */
        public String getTitle() {

            return m_title;
        }

        /**
         * Returns whether this editable gallery item has value changes compared
         * to the property values actually persisted.<p>
         *
         * @return whether changes or not
         */
        public boolean hasChanges() {

            boolean hasChanges = false;
            try {
                if (!hasChanges) {
                    hasChanges = !m_title.equals(readPropertyTitle());
                }
                if (!hasChanges) {
                    hasChanges = !m_copyright.equals(readPropertyCopyright());
                }
                if (!hasChanges) {
                    hasChanges = !m_description.equals(readPropertyDescription());
                }
            } catch (CmsException e) {
                hasChanges = true;
                LOG.warn(e.getLocalizedMessage(), e);
            }
            return hasChanges;
        }

        /**
         * Returns whether this editable gallery item is renamed compared to
         * the resource actually persisted.<p>
         *
         * @return whether renamed or not
         */
        public boolean isRenamed() {

            boolean isRenamed = false;
            try {
                isRenamed = !m_name.equals(readName());
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
            return isRenamed;
        }

        /**
         * Returns whether this editable gallery item is an image.<p>
         *
         * @return whether an image or not
         */
        public boolean isTypeImage() {

            return OpenCms.getResourceManager().getResourceType(m_resource) instanceof CmsResourceTypeImage;
        }

        /**
         * Sets the copyright information of this editable gallery item.<p>
         *
         * @param copyright the copyright information
         */
        public void setCopyright(String copyright) {

            m_copyright = copyright;
        }

        /**
         * Sets the flag that states whether this editable gallery item shall be deleted.<p>
         *
         * @param deleteFlag the flag
         */
        public void setDeleteFlag(Boolean deleteFlag) {

            m_deleteFlag = deleteFlag;
        }

        /**
         * Sets the description of this editable gallery item.<p>
         *
         * @param description the description
         */
        public void setDescription(String description) {

            m_description = description;
        }

        /**
         * Sets the name of this editable gallery item.<p>
         *
         * @param name the name
         */
        public void setName(String name) {

            m_name = name;
        }

        /**
         * Sets the CMS resource of this editable gallery item and re-initializes all data and components.<p>
         *
         * @param resource the CMS resource
         */
        public void setResource(CmsResource resource) {

            m_resource = resource;
            initData();
            initComponent();
        }

        /**
         * Sets the title of this editable gallery item.<p>
         *
         * @param title the title
         */
        public void setTitle(String title) {

            m_title = title;
        }

        /**
         * Initializes all UI components of this editable gallery item and initializes all data fields.<p>
         */
        private void initComponent() {

            m_compositeFile = new FileComposite(this);
            m_compositeFileDelete = new FileDeleteComposite(this);
            m_compositeForm = new FormComposite(this);
            m_binder.readBean(this);
        }

        /**
         * Initializes all data of this editable gallery item.<p>
         */
        private void initData() {

            m_resourceUtil = new CmsResourceUtil(A_CmsUI.getCmsObject(), m_resource);
            try {
                List<CmsRelation> relations = getCms().getRelationsForResource(m_resource, CmsRelationFilter.SOURCES);
                m_name = m_resource.getName();
                m_path = m_resource.getRootPath();
                m_title = readPropertyTitle();
                m_copyright = readPropertyCopyright();
                m_description = readPropertyDescription();
                m_dateLastModified = Long.valueOf(m_resource.getDateLastModified());
                m_isUsed = Boolean.valueOf(!((relations == null) || relations.isEmpty()));
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        /**
         * Reads the persisted resource name.<p>
         *
         * @return the resource name
         * @throws CmsException thrown if reading the resource fails
         */
        private String readName() throws CmsException {

            CmsResourceFilter resourceFilter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireFile();
            return getCms().readResource(m_resource.getStructureId(), resourceFilter).getName();
        }

        /**
         * Reads the persisted copyright property value.<p>
         *
         * @return the copyright property value
         * @throws CmsException thrown if the property read fails
         */
        private String readPropertyCopyright() throws CmsException {

            String value = getCms().readPropertyObject(
                m_resource,
                CmsPropertyDefinition.PROPERTY_COPYRIGHT,
                false).getValue();
            return value == null ? "" : value;
        }

        /**
         * Reads the persisted description property value.<p>
         *
         * @return the description property value
         * @throws CmsException thrown if the property read fails
         */
        private String readPropertyDescription() throws CmsException {

            String value = getCms().readPropertyObject(
                m_resource,
                CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                false).getValue();
            return value == null ? "" : value;
        }

        /**
         * Reads the persisted title property value.<p>
         *
         * @return the title property value
         * @throws CmsException thrown if the property read fails
         */
        private String readPropertyTitle() throws CmsException {

            String value = getCms().readPropertyObject(
                m_resource,
                CmsPropertyDefinition.PROPERTY_TITLE,
                false).getValue();
            return value == null ? "" : value;
        }
    }

    /**
     * Class representing the data list header view with components for
     * sorting, paging, and filtering the gallery item list.<p>
     */
    private class DataListHeaderComposite extends AbsoluteLayout {

        /** The default serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The page info label. */
        private Label m_labelPageInfo;

        /** The select box for page selection. */
        private NativeSelect<Integer> m_selectPage;

        /** The select box for sort order selection. */
        private NativeSelect<String> m_selectSortOrder;

        /** The text field for filtering. */
        private TextField m_textFieldFilter;

        /**
         * Creates a new data list header composite.<p>
         */
        public DataListHeaderComposite() {

            setHeight("34px");
            setWidthFull();
            m_selectSortOrder = createSelectSortOrder();
            m_textFieldFilter = createTextFieldFilter();
            addComponent(m_selectSortOrder, "left: 2px; top: 2px;");
            addComponent(m_textFieldFilter, "right: 2px; top: 2px;");
            refresh();
        }

        /**
         * Refreshes this component.<p>
         */
        public void refresh() {

            NativeSelect<Integer> selectPage = createSelectPage();
            Label pageInfo = createLabelPageInfo();
            if (m_selectPage == null) {
                m_selectPage = selectPage;
                addComponent(m_selectPage, "left: 436px; top: 2px;");
            } else {
                replaceComponent(m_selectPage, selectPage);
                m_selectPage = selectPage;
            }
            if (m_labelPageInfo == null) {
                m_labelPageInfo = pageInfo;
                addComponent(m_labelPageInfo, "right: 228px; top: 6px;");
            } else {
                replaceComponent(m_labelPageInfo, pageInfo);
                m_labelPageInfo = pageInfo;
            }
        }

        /**
         * Programmatically selects a page according to a given index.<p>
         *
         * @param index the page index
         */
        public void selectPage(int index) {

            m_selectPage.setValue(null);
            handlePageChange(index, false);
        }

        /**
         * Creates a page info label.<p>
         *
         * @return the page info label
         */
        @SuppressWarnings("synthetic-access")
        private Label createLabelPageInfo() {

            String text = "";
            if (m_pageHandler.hasPages()) {
                text = CmsVaadinUtils.getMessageText(
                    Messages.GUI_GALLERY_OPTIMIZE_LABEL_PAGE_INFO_3,
                    String.valueOf(m_pageHandler.getNumFirstItem()),
                    String.valueOf(m_pageHandler.getNumLastItem()),
                    String.valueOf(m_pageHandler.getSizeItem()));
            } else if (m_pageHandler.getSizeItem() == 1) {
                text = CmsVaadinUtils.getMessageText(
                    Messages.GUI_GALLERY_OPTIMIZE_LABEL_PAGE_INFO_ONE_0,
                    String.valueOf(m_pageHandler.getSizeItem()));
            } else {
                text = CmsVaadinUtils.getMessageText(
                    Messages.GUI_GALLERY_OPTIMIZE_LABEL_PAGE_INFO_1,
                    String.valueOf(m_pageHandler.getSizeItem()));
            }
            Label label = new Label(text);
            label.setWidthUndefined();
            return label;
        }

        /**
         * Creates a select box for page select.<p>
         *
         * @return the page select box
         */
        @SuppressWarnings("synthetic-access")
        private NativeSelect<Integer> createSelectPage() {

            NativeSelect<Integer> selectPage = new NativeSelect<Integer>();
            selectPage.setWidthUndefined();
            int numPages = m_pageHandler.getNumPages();
            selectPage.setItemCaptionGenerator(new ItemCaptionGenerator<Integer>() {

                private static final long serialVersionUID = 1L;

                public String apply(Integer item) {

                    return CmsVaadinUtils.getMessageText(
                        Messages.GUI_GALLERY_OPTIMIZE_SELECTED_PAGE_2,
                        String.valueOf(item.intValue() + 1),
                        String.valueOf(numPages));
                }

            });
            Integer firstItem = Integer.valueOf(0);
            List<Integer> items = new ArrayList<Integer>();
            for (int i = 0; i < numPages; i++) {
                if (i > 0) {
                    items.add(Integer.valueOf(i));
                }
            }
            selectPage.setEmptySelectionCaption(selectPage.getItemCaptionGenerator().apply(firstItem));
            selectPage.setItems(items);
            selectPage.addValueChangeListener(event -> {
                if (event.isUserOriginated()) {
                    int index = event.getValue() != null ? event.getValue().intValue() : 0;
                    handlePageChange(index, true);
                }
            });
            selectPage.setVisible(m_pageHandler.hasPages());
            return selectPage;
        }

        /**
         * Creates a select box for sort order select.<p>
         *
         * @return the sort order select box
         */
        @SuppressWarnings("synthetic-access")
        private NativeSelect<String> createSelectSortOrder() {

            NativeSelect<String> selectSortOrder = new NativeSelect<String>();
            selectSortOrder.setWidthUndefined();
            selectSortOrder.setEmptySelectionCaption(m_messageSortTitleAscending);
            selectSortOrder.setItems(
                m_messageSortTitleDescending,
                m_messageSortDateLastModifiedAscending,
                m_messageSortDateLastModifiedDescending,
                m_messageSortPathAscending,
                m_messageSortPathDescending,
                m_messageSortUnusedFirst,
                m_messageSortNoCopyrightFirst);
            selectSortOrder.addValueChangeListener(event -> {
                if (event.isUserOriginated()) {
                    selectPage(0);
                    m_provider.refreshAll();
                    displayDataListViewSorted(event.getValue());
                }
            });
            selectSortOrder.setValue(getSessionSortOrder());
            return selectSortOrder;
        }

        /**
         * Creates a text field for filtering.<p>
         *
         * @return the filter text field
         */
        private TextField createTextFieldFilter() {

            TextField textField = new TextField();
            textField.setPlaceholder(CmsVaadinUtils.getMessageText(org.opencms.ui.apps.Messages.GUI_EXPLORER_FILTER_0));
            textField.setWidth("200px");
            textField.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
            textField.setIcon(FontOpenCms.FILTER);
            textField.addValueChangeListener(event -> {
                handleFilterChange(event.getValue());
            });
            return textField;
        }

        /**
         * Filter change event handler. Updates the page info label and the gallery list.<p>
         *
         * @param query the filter query string
         */
        @SuppressWarnings("synthetic-access")
        private void handleFilterChange(String query) {

            String clean = query.trim();
            m_filterHandler.setQuery(clean);
            m_pageHandler.setCurrentPage(0);
            refresh();
            displayDataListView(true);
        }

        /**
         * Page change event handler. Updates the page info label.<p>
         *
         * @param index the index of the page to select
         * @param display whether to re-render the data item list
         */
        @SuppressWarnings("synthetic-access")
        private void handlePageChange(int index, boolean display) {

            m_pageHandler.setCurrentPage(index);
            Label label = createLabelPageInfo();
            replaceComponent(m_labelPageInfo, label);
            m_labelPageInfo = label;
            if (display) {
                displayDataListView(true);
            }
        }
    }

    /**
     * Class representing a file composite offering a file preview.<p>
     */
    private class FileComposite extends HorizontalLayout {

        /** The panel height. */
        private static final String PANEL_HEIGHT = "176px";

        /** The panel width. */
        private static final String PANEL_WIDTH = "206px";

        /** Image scale parameters for preview images as used by the image scaler. */
        private static final String SCALE_PARAMETERS = "t:1,c:ffffff,w:" + IMAGE_WIDTH + ",h:" + IMAGE_HEIGHT;

        /** Request query string to load a scaled preview image. */
        private static final String SCALE_QUERY_STRING = "?__scale=" + SCALE_PARAMETERS;

        /** The default serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The data item of this file composite. */
        private DataItem m_dataItem;

        /** The main panel of this file composite. */
        private AbsoluteLayout m_panel;

        /**
         * Creates a new file composite for a given data item.<p>
         *
         * @param dataItem the data item
         */
        public FileComposite(DataItem dataItem) {

            m_dataItem = dataItem;
            setSizeUndefined();
            setMargin(true);
            m_panel = new AbsoluteLayout();
            m_panel.setWidth(PANEL_WIDTH);
            m_panel.setHeight(PANEL_HEIGHT);
            m_panel.addStyleName("v-panel");
            Component link = createClickableFile();
            m_panel.addComponent(link, "left: 2px; top: 2px;");
            addComponent(m_panel);
        }

        /**
         * Creates a clickable file preview.<p>
         *
         * @return the clickable file preview
         */
        private Component createClickableFile() {

            Component link = m_dataItem.isTypeImage() ? createClickableImage() : createClickableOther();
            link.setWidth(IMAGE_WIDTH + "px");
            link.setHeight(IMAGE_HEIGHT + "px");
            return link;
        }

        /**
         * Utility function to create a clickable image.<p>
         *
         * @return the clickable image
         */
        private Label createClickableImage() {

            CmsResource resource = m_dataItem.getResource();
            String image = "<img width=\""
                + IMAGE_WIDTH
                + "px\" height=\""
                + IMAGE_HEIGHT
                + "px\" src=\""
                + getScaleUri(resource)
                + "\" style=\"background: white;\">";
            String a = "<a target=\"_blank\" href=\"" + getPermanentUri(resource) + "\">" + image + "</a>";
            String div = "<div class=\""
                + OpenCmsTheme.GALLERY_PREVIEW_IMAGE
                + "\" style=\"width:"
                + IMAGE_WIDTH
                + "px;height:"
                + IMAGE_HEIGHT
                + "px;\">"
                + a
                + "</div>";
            Label label = new Label(div);
            label.setContentMode(ContentMode.HTML);
            return label;
        }

        /**
         * Utility function to create a clickable preview for files that are not images.
         *
         * @return the clickable preview
         */
        private Link createClickableOther() {

            CmsResource resource = m_dataItem.getResource();
            CmsCssIcon cssIcon = (CmsCssIcon)m_dataItem.getResourceUtil().getSmallIconResource();
            String caption = "<div style=\"width:"
                + IMAGE_WIDTH
                + "px;height:"
                + IMAGE_HEIGHT
                + "px;display: flex; justify-content: center; align-items: center;\"><span class=\""
                + cssIcon.getStyleName()
                + "\" style=\"transform: scale(4);\"></span></div>";
            Link link = new Link(caption, new ExternalResource(getPermanentUri(resource)));
            link.setCaptionAsHtml(true);
            link.setTargetName("_blank");
            return link;
        }

        /**
         * Utility function to create a permanent URI for a file preview.<p>
         *
         * @param resource the CMS resource
         * @return the permanent URI
         */
        private String getPermanentUri(CmsResource resource) {

            String structureId = resource.getStructureId().toString();
            String permalink = CmsStringUtil.joinPaths(
                OpenCms.getSystemInfo().getOpenCmsContext(),
                CmsPermalinkResourceHandler.PERMALINK_HANDLER,
                structureId);
            return permalink;
        }

        /**
         * Utility function to create a permanent URI for a scaled preview image.<p>
         *
         * @param resource the CMS resource
         * @return the scale URI
         */
        private String getScaleUri(CmsResource resource) {

            String paramTimestamp = "&timestamp=" + System.currentTimeMillis();
            return getPermanentUri(resource) + SCALE_QUERY_STRING + paramTimestamp;
        }
    }

    /**
     * Class representing a file delete composite with a check box to mark a file as deleted.<p>
     */
    private class FileDeleteComposite extends VerticalLayout {

        /** The default serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The component width. */
        private static final String WIDTH = "206px";

        /** The data item of this file delete composite. */
        private DataItem m_dataItem;

        /**
         * Creates a new file delete composite for a given data item.<p>
         *
         * @param dataItem the data item
         */
        public FileDeleteComposite(DataItem dataItem) {

            m_dataItem = dataItem;
            setMargin(new MarginInfo(true, true, true, false));
            setSpacing(false);
            setWidth(WIDTH);
            setHeightFull();
            Label fileSize = createDisplayFileSize();
            addComponent(fileSize);
            Label dimension = createDisplayDimension();
            if (m_dataItem.isTypeImage()) {
                addComponent(dimension);
            }
            if (!m_dataItem.getIsUsed().booleanValue()) {
                CssLayout layout = new CssLayout();
                Label labelInUse = createDisplayInUseInfo();
                CheckBox fieldDeleteFlag = createFieldDeleteFlag();
                layout.addComponent(labelInUse);
                layout.addComponent(fieldDeleteFlag);
                addComponent(layout);
                setExpandRatio(layout, 1.0f);
                setComponentAlignment(layout, Alignment.BOTTOM_LEFT);
            } else if (m_dataItem.isTypeImage()) {
                setExpandRatio(dimension, 1.0f);
            } else {
                setExpandRatio(fileSize, 1.0f);
            }
        }

        /**
         * Creates a component displaying the dimension of this editable gallery item.<p>
         *
         * @return the display component
         */
        private Label createDisplayDimension() {

            return new Label(getFormattedDimension());
        }

        /**
         * Creates a component displaying the size of this editable gallery item.<p>
         *
         * @return the display component
         */
        private Label createDisplayFileSize() {

            return new Label(getFormattedFileSize());
        }

        /**
         * Creates a component displaying whether this editable gallery item is used.<p>
         *
         * @return the display component
         */
        private Label createDisplayInUseInfo() {

            String notInUse = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_LABEL_NOT_IN_USE_0);
            return new Label(notInUse);
        }

        /**
         * Creates a check box to mark an item as deleted.<p>
         *
         * @return the check box
         */
        private CheckBox createFieldDeleteFlag() {

            String caption = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_INPUT_DELETE_UNUSED_0);
            CheckBox field = new CheckBox(caption);
            field.setCaptionAsHtml(true);
            field.setWidthFull();
            field.setEnabled(!CmsGalleryOptimizeDialog.this.isReadOnly());
            m_dataItem.getBinder().bind(field, DataItem::getDeleteFlag, DataItem::setDeleteFlag);
            return field;
        }

        /**
         * Returns the dimension of this file in the case the file is an image.<p>
         *
         * @return the formatted dimension
         */
        private String getFormattedDimension() {

            String imageSize = null;
            try {
                imageSize = getCms().readPropertyObject(
                    m_dataItem.getResource(),
                    CmsPropertyDefinition.PROPERTY_IMAGE_SIZE,
                    false).getValue();
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
            String dimension = "? x ?";
            if (imageSize != null) {
                String[] tokens = imageSize.split(",");
                if ((tokens.length == 2) && (tokens[0].length() > 2) && (tokens[1].length() > 2)) {
                    dimension = tokens[0].substring(2) + " x " + tokens[1].substring(2);
                }
            }
            return dimension;
        }

        /**
         * Returns the size of this editable gallery item in a formatted way.<p>
         *
         * @return the formatted file size
         */
        private String getFormattedFileSize() {

            return (m_dataItem.getResource().getLength() / 1024) + " kb";
        }
    }

    /**
     * Filter handler. Keeps track of the query string with which the user
     * currently filters the gallery.
     */
    private class FilterHandler {

        /** The filter query string. */
        private String m_query;

        /**
         * Creates a new filter handler.<p>
         */
        public FilterHandler() {

        }

        /**
         * Returns the filter query string.<p>
         *
         * @return the filter query string
         */
        public String getQuery() {

            return m_query != null ? m_query.toLowerCase() : null;
        }

        /**
         * Sets the filter query string.<p>
         *
         * @param query the filter query string
         */
        public void setQuery(String query) {

            m_query = query;
        }
    }

    /**
     * Class representing a form composite to edit gallery item data.<p>
     */
    private class FormComposite extends VerticalLayout {

        /** The default serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The data item of this form composite. */
        DataItem m_dataItem;

        /**
         * Creates a new form composite for a given data item.<p>
         *
         * @param dataItem the data item
         */
        public FormComposite(DataItem dataItem) {

            m_dataItem = dataItem;
            setSizeFull();
            setMargin(true);
            setSpacing(false);
            addComponent(createCompositeResourceInfo());
            addComponent(createDisplayResourceDate());
            FormLayout formLayout = new FormLayout();
            formLayout.setMargin(false);
            formLayout.setSpacing(false);
            formLayout.addStyleName(OpenCmsTheme.GALLERY_FORM);
            formLayout.addComponent(createFieldTitle());
            formLayout.addComponent(createFieldCopyright());
            formLayout.addComponent(createFieldDescription());
            addComponent(formLayout);
            setComponentAlignment(formLayout, Alignment.BOTTOM_LEFT);
            setExpandRatio(formLayout, 1.0f);
        }

        /**
         * Returns a composite to display and edit resource information.<p>
         *
         * @return the component
         */
        private CmsResourceInfo createCompositeResourceInfo() {

            CmsResourceInfo resourceInfo = new CmsResourceInfo(m_dataItem.getResource());
            resourceInfo.setTopLineText(m_dataItem.getName());
            resourceInfo.decorateTopInput();
            TextField field = resourceInfo.getTopInput();
            m_dataItem.getBinder().bind(field, DataItem::getName, DataItem::setName);
            CmsGwtContextMenuButton contextMenu = new CmsGwtContextMenuButton(
                m_dataItem.getResource().getStructureId(),
                new ContextMenu(m_dataItem));
            contextMenu.addStyleName("o-gwt-contextmenu-button-margin");
            resourceInfo.setButtonWidget(contextMenu);
            return resourceInfo;
        }

        /**
         * Returns a component to display resource date information.<p>
         *
         * @return the component
         */
        private Label createDisplayResourceDate() {

            String lastModified = formatDateTime(m_dataItem.getDateLastModified().longValue());
            String lastModifiedBy = m_dataItem.getResourceUtil().getUserLastModified();
            String message = CmsVaadinUtils.getMessageText(
                Messages.GUI_GALLERY_OPTIMIZE_LASTMODIFIED_BY_2,
                lastModified,
                lastModifiedBy);
            Label label = new Label(message);
            label.addStyleNames(ValoTheme.LABEL_LIGHT, ValoTheme.LABEL_TINY);
            return label;
        }

        /**
         * Creates the copyright form field.<p>
         *
         * @return the copyright form field.
         */
        private TextField createFieldCopyright() {

            String caption = CmsVaadinUtils.getMessageText(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_COPYRIGHT_0);
            TextField field = new TextField(caption);
            field.setWidthFull();
            field.setEnabled(!CmsGalleryOptimizeDialog.this.isReadOnly());
            m_dataItem.getBinder().bind(field, DataItem::getCopyright, DataItem::setCopyright);
            return field;
        }

        /**
         * Creates the description form field.<p>
         *
         * @return the description form field.
         */
        private TextField createFieldDescription() {

            String caption = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_INPUT_DESCRIPTION_0);
            TextField field = new TextField(caption);
            field.setWidthFull();
            field.setEnabled(!CmsGalleryOptimizeDialog.this.isReadOnly());
            m_dataItem.getBinder().bind(field, DataItem::getDescription, DataItem::setDescription);
            return field;
        }

        /**
         * Creates the title form field.<p>
         *
         * @return the title form field.
         */
        private TextField createFieldTitle() {

            String caption = CmsVaadinUtils.getMessageText(org.opencms.workplace.explorer.Messages.GUI_INPUT_TITLE_0);
            TextField field = new TextField(caption);
            field.setWidthFull();
            field.setEnabled(!CmsGalleryOptimizeDialog.this.isReadOnly());
            m_dataItem.getBinder().bind(field, DataItem::getTitle, DataItem::setTitle);
            return field;
        }

        /**
         * Utility function for date formatting.<p>
         *
         * @param date the date to format
         * @return the formatted date
         */
        private String formatDateTime(long date) {

            return CmsDateUtil.getDateTime(
                new Date(date),
                DateFormat.SHORT,
                OpenCms.getWorkplaceManager().getWorkplaceLocale(getCms()));
        }
    }

    /**
     * Page handler. Keeps track of the page currently selected by the user.
     */
    private class PageHandler {

        /** The maximum number of items per page. */
        public static final int LIMIT = 50;

        /** The index of the page currently selected by the user. */
        private int m_currentPage = 0;

        /**
         * Creates a new page handler.<p>
         */
        public PageHandler() {}

        /**
         * Returns the maximum number of items per page.<p>
         *
         * @return the maximum number of items per page
         */
        public int getLimit() {

            return LIMIT;
        }

        /**
         * Returns the number of the first item on the page currently selected.
         * The first item on the first page has number 1.<p>
         *
         * @return the number of the first item currently selected
         */
        public int getNumFirstItem() {

            return (LIMIT * m_currentPage) + 1;
        }

        /**
         * Returns the number of the last item on the page currently selected.
         * The number of the last item on the last page is equal to the total number of items.
         *
         * @return the number of the last item currently selected
         */
        public int getNumLastItem() {

            int lastItem = ((LIMIT * m_currentPage) + LIMIT);
            int sizeItem = getSizeItem();
            if (lastItem > sizeItem) {
                lastItem = sizeItem;
            }
            return lastItem;
        }

        /**
         * Returns the number of available pages.<p>
         *
         * @return the number of available pages
         */
        public int getNumPages() {

            return (int)Math.ceil((double)getSizeItem() / PageHandler.LIMIT);
        }

        /**
         * Returns the index of the first item on the page currently selected.
         * The index of the first item on the first page is 0.<p>
         *
         * @return the index of the first item ob the page currently selected
         */
        public int getOffset() {

            return LIMIT * m_currentPage;
        }

        /**
         * Returns the total number of items.<p>
         *
         * @return the total number of items
         */
        @SuppressWarnings("synthetic-access")
        public int getSizeItem() {

            return m_provider.size(m_filterHandler);
        }

        /**
         * Returns whether the current data list has pages.
         *
         * @return whether has pages
         */
        public boolean hasPages() {

            return getSizeItem() > LIMIT;
        }

        /**
         * Sets the current page index to a given value.
         * The index of the first page is 0.<p>
         *
         * @param index the page index to set
         */
        public void setCurrentPage(int index) {

            m_currentPage = index;
        }
    }

    /**
     * Class representing a data provider for sorting and paging the in-memory data list.
     */
    private class Provider extends ListDataProvider<DataItem> {

        /** The default serial version UID. */
        private static final long serialVersionUID = 1L;

        /** Comparator. */
        final SerializableComparator<DataItem> SORT_DATE_ASCENDING = Comparator.comparing(
            DataItem::getDateLastModified)::compare;

        /** Comparator. */
        final SerializableComparator<DataItem> SORT_DATE_DESCENDING = Comparator.comparing(
            DataItem::getDateLastModified).reversed()::compare;

        /** Comparator. */
        final SerializableComparator<DataItem> SORT_PATH_ASCENDING = Comparator.comparing(
            DataItem::getPath,
            String.CASE_INSENSITIVE_ORDER)::compare;

        /** Comparator. */
        final SerializableComparator<DataItem> SORT_PATH_DESCENDING = Comparator.comparing(
            DataItem::getPath,
            String.CASE_INSENSITIVE_ORDER).reversed()::compare;

        /** Comparator. */
        final SerializableComparator<DataItem> SORT_TITLE_ASCENDING = Comparator.comparing(
            DataItem::getTitle,
            String.CASE_INSENSITIVE_ORDER)::compare;

        /** Comparator. */
        final SerializableComparator<DataItem> SORT_TITLE_DESCENDING = Comparator.comparing(
            DataItem::getTitle,
            String.CASE_INSENSITIVE_ORDER).reversed()::compare;

        /** Comparator. */
        final SerializableComparator<DataItem> SORT_UNUSED_FIRST = Comparator.comparing(DataItem::getIsUsed)::compare;

        /** Comparator. */
        final SerializableComparator<DataItem> SORT_NOCOPYRIGHT_FIRST = Comparator.comparing(
            DataItem::getNoCopyright)::compare;

        /**
         * Create a new provider for a given data item list.
         *
         * @param dataItemList the data item list
         */
        public Provider(List<DataItem> dataItemList) {

            super(dataItemList);
        }

        /**
         * Fetches one page of the sorted, filtered in-memory data item list.<p>
         *
         * @param pageHandler the page handler containing offset and limit information
         * @param filterHandler the filter handler containing the actual filter query string
         * @return the sorted data item page
         */
        public List<DataItem> fetch(PageHandler pageHandler, FilterHandler filterHandler) {

            SerializablePredicate<DataItem> filter = null;
            Query<DataItem, SerializablePredicate<DataItem>> filterQuery = composeFilterQuery(filterHandler);
            if (filterQuery != null) {
                filter = filterQuery.getFilter().orElse(null);
            }
            Query<DataItem, SerializablePredicate<DataItem>> query = new Query<DataItem, SerializablePredicate<DataItem>>(
                pageHandler.getOffset(),
                pageHandler.getLimit(),
                Collections.emptyList(),
                getSortComparator(),
                filter);
            return super.fetch(query).collect(Collectors.toList());
        }

        /**
         * @see com.vaadin.data.provider.ListDataProvider#getItems()
         */
        @Override
        public List<DataItem> getItems() {

            return (List<DataItem>)super.getItems();
        }

        /**
         * Returns the size of the list respecting the current filter.
         *
         * @param filterHandler the filter handler
         * @return the size
         */
        public int size(FilterHandler filterHandler) {

            Query<DataItem, SerializablePredicate<DataItem>> filterQuery = composeFilterQuery(filterHandler);
            return filterQuery == null ? getItems().size() : super.size(filterQuery);
        }

        /**
         * Composes a provider query for a given filter handler.<p>
         *
         * @param filterHandler the given filter handler
         * @return the provider query
         */
        private Query<DataItem, SerializablePredicate<DataItem>> composeFilterQuery(FilterHandler filterHandler) {

            Query<DataItem, SerializablePredicate<DataItem>> filterQuery = null;
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(filterHandler.getQuery())) {
                filterQuery = new Query<DataItem, SerializablePredicate<DataItem>>(
                    dataItem -> dataItem.getFilterText().contains(filterHandler.getQuery()));
            }
            return filterQuery;
        }
    }

    /**
     * Utility class to handle dialog save actions. Keeps track of the changes the
     * user has made since opening the dialog on the one hand and the changes since
     * the last save action on the other.
     */
    private class SaveHandler {

        /** Data items modified or marked deleted since opening the dialog. */
        Set<DataItem> m_changed = new HashSet<DataItem>();

        /** Data items modified or marked deleted since the last save action. */
        Set<DataItem> m_changedCurrent = new HashSet<DataItem>();

        /** IDs of the resources modified or marked as deleted since opening the dialog. */
        Set<CmsUUID> m_changedIds = new HashSet<CmsUUID>();

        /** Resource IDs modified or marked as deleted since the last save action. */
        Set<CmsUUID> m_changedIdsCurrent = new HashSet<CmsUUID>();

        /** Data items marked deleted since opening the dialog. */
        Set<DataItem> m_deleted = new HashSet<DataItem>();

        /** Data items marked deleted since the last save action. */
        Set<DataItem> m_deletedCurrent = new HashSet<DataItem>();

        /** Resources deleted since the last save action. */
        Set<CmsResource> m_deletedCurrentResource = new HashSet<CmsResource>();

        /** Whether the user has cancelled the last save action. */
        boolean m_flagCancelSave = false;

        /**
         * Creates a new save handler.<p>
         */
        public SaveHandler() {}

        /**
         * Marks the last save action as cancelled.<p>
         *
         * @param flagCancelSave Whether to mark as cancelled
         */
        public void setFlagCancelSave(boolean flagCancelSave) {

            m_flagCancelSave = flagCancelSave;
        }

        /**
         * Returns the data items modified or marked deleted since the last save action.<p>
         *
         * @return the data items
         */
        Set<DataItem> getChangedCurrent() {

            return m_changedCurrent;
        }

        /**
         * Returns the IDs of the resources modified or marked as deleted since opening the dialog.
         *
         * @return the resource IDs
         */
        List<CmsUUID> getChangedIds() {

            return new ArrayList<CmsUUID>(m_changedIds);
        }

        /**
         * Returns the data items marked deleted since the last save action.<p>
         *
         * @return the data items
         */
        Set<DataItem> getDeletedCurrent() {

            return m_deletedCurrent;
        }

        /**
         * Returns the resources marked deleted since the last save action.<p>
         *
         * @return the resources
         */
        List<CmsResource> getDeletedCurrentResource() {

            return new ArrayList<CmsResource>(m_deletedCurrentResource);
        }

        /**
         * Whether data items have been marked as deleted since the last save action.<p>
         *
         * @return whether data items have been marked as deleted or not
         */
        boolean hasDeletedCurrent() {

            return m_deletedCurrent.size() > 0;
        }

        /**
         * Handles a save action for a given list of data items.<p>
         *
         * @param dataList the data item list
         */
        void save(List<DataItem> dataList) {

            if (!m_flagCancelSave) {
                flush();
            }
            for (DataItem dataItem : dataList) {
                boolean dataItemHasChanges = dataItem.getBinder().hasChanges();
                if (dataItemHasChanges) {
                    try {
                        dataItem.getBinder().writeBean(dataItem);
                        m_changedCurrent.add(dataItem);
                        if (dataItem.getDeleteFlag().booleanValue()) {
                            m_deletedCurrent.add(dataItem);
                            m_deletedCurrentResource.add(dataItem.getResource());
                        } else if ((dataItem.getDeleteFlag().booleanValue() == false)
                            && m_deletedCurrent.contains(dataItem)) {
                                m_deletedCurrent.remove(dataItem);
                                m_deletedCurrentResource.remove(dataItem.getResource());
                            }
                    } catch (ValidationException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                    }
                }
            }
        }

        /**
         * Secures and resets the current changes.
         */
        private void flush() {

            m_deleted.addAll(m_deletedCurrent);
            m_changed.addAll(m_changedCurrent);
            m_changedIds.addAll(m_changedIdsCurrent);
            m_deletedCurrent.clear();
            m_changedCurrent.clear();
            m_changedIdsCurrent.clear();
            m_deletedCurrentResource.clear();
        }
    }

    /** The sort order session attribute. */
    static final String GALLERY_OPTIMIZE_ATTR_SORT_ORDER = "GALLERY_OPTIMIZE_ATTR_SORT_ORDER";

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsGalleryOptimizeDialog.class);

    /** The height of the preview images. */
    private static final String IMAGE_HEIGHT = "170";

    /** The width of the preview images. */
    private static final String IMAGE_WIDTH = "200";

    /** The default serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The save button. */
    private Button m_buttonSave;

    /** The save and exit button. */
    private Button m_buttonSaveAndExit;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The UI composite representing the gallery item list header view. */
    private Object m_compositeDataListHeader;

    /** The UI component representing the gallery item list header view. */
    private VerticalLayout m_dataListHeaderView;

    /** The UI component representing the scrollable wrapper around the gallery item list view. */
    private Panel m_dataListViewScrollable;

    /** The UI component representing the gallery item list view. */
    private GridLayout m_dataListView;

    /** The gallery */
    private CmsResource m_gallery;

    /** The lock action record for the gallery folder. */
    private CmsLockActionRecord m_lockActionRecord;

    /** Localized message. */
    private String m_messageSortDateLastModifiedAscending;

    /** Localized message. */
    private String m_messageSortDateLastModifiedDescending;

    /** Localized message. */
    private String m_messageSortPathAscending;

    /** Localized message. */
    private String m_messageSortPathDescending;

    /** Localized message. */
    private String m_messageSortTitleAscending;

    /** Localized message. */
    private String m_messageSortTitleDescending;

    /** Localized message. */
    private String m_messageSortUnusedFirst;

    /** Localized message. */
    private String m_messageSortNoCopyrightFirst;

    /** The filter handler. */
    private FilterHandler m_filterHandler = new FilterHandler();

    /** The page handler. */
    private PageHandler m_pageHandler = new PageHandler();

    /** The data provider. */
    private Provider m_provider;

    /** The save handler. */
    private SaveHandler m_saveHandler = new SaveHandler();

    /**
     * Creates a new instance of a gallery optimize dialog.<p>
     *
     * @param context the dialog context
     * @param gallery the gallery folder to optimize
     */
    public CmsGalleryOptimizeDialog(I_CmsDialogContext context, CmsResource gallery) {

        m_context = context;
        m_gallery = gallery;
        initMessages();
        initDialog();
        initLock();
        initEvents();
        dataListLoad();
        displayDataListHeaderView();
        displayDataListViewSorted(getSessionSortOrder());
    }

    /**
     * Returns the CMS object of this dialog.<p>
     *
     * @return the CMS object
     */
    public CmsObject getCms() {

        return m_context.getCms();
    }

    /**
     * Whether this dialog was opened from a read-only context.
     *
     * @see com.vaadin.ui.AbstractComponent#isReadOnly()
     */
    @Override
    protected boolean isReadOnly() {

        return m_context.getCms().getRequestContext().getCurrentProject().isOnlineProject();
    }

    /**
     * Finishes this dialog.<p>
     *
     * @param changedIds list of IDs of the changed resources
     */
    void finishDialog(List<CmsUUID> changedIds) {

        m_context.finish(changedIds);
    }

    /**
     * Refreshes the UI state after a list of data items has been successfully deleted.<p>
     *
     * @param dataItemList the list of deleted data items
     */
    void handleDataListDelete(List<DataItem> dataItemList) {

        m_provider.getItems().removeAll(dataItemList);
        ((DataListHeaderComposite)m_compositeDataListHeader).refresh();
        displayDataListView(false);
    }

    /**
     * Refreshes the UI state after a list of data items has been successfully updated.<p>
     *
     * @param dataItemList the list of updated data items
     * @throws CmsException thrown if reading a persisted CMS resource fails
     */
    void handleDataListUpdate(List<DataItem> dataItemList) throws CmsException {

        for (DataItem dataItem : dataItemList) {
            CmsResourceFilter resourceFilter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireFile();
            CmsResource reload = getCms().readResource(dataItem.getResource().getStructureId(), resourceFilter);
            dataItem.setResource(reload);
        }
        displayDataListView(false);
    }

    /**
     * Event handler handling the dialog attach event.
     */
    void handleDialogAttach() {

        Window window = CmsVaadinUtils.getWindow(CmsGalleryOptimizeDialog.this);
        window.removeAllCloseShortcuts(); // this is because Vaadin by default adds an ESC shortcut to every window
        // this is because the grid view unintentionally catches the focus whenever the height of the grid view
        // gets larger / smaller than it's containing layout, i.e., whenever the scroll-bar appears or disappears
        Page.getCurrent().getStyles().add(".o-gallery-grid-force-scroll { min-height: 1000px; }");
        m_dataListView.addStyleName("o-gallery-grid-force-scroll");
    }

    /**
     * Event handler that discards all data edited and closes the
     * dialog. Asks the user for confirmation beforehand.<p>
     */
    void handleDialogCancel() {

        if (dataListHasChanges()) {
            String title = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_CONFIRM_CANCEL_TITLE_0);
            String message = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_CONFIRM_CANCEL_0);
            CmsConfirmationDialog.show(title, message, new Runnable() {

                @Override
                public void run() {

                    finishDialog(new ArrayList<CmsUUID>());
                }
            });
        } else {
            finishDialog(new ArrayList<CmsUUID>());
        }
    }

    /**
     * Event handler that handles the dialog detach event.
     */
    void handleDialogDetach() {

        unlock();
    }

    /**
     * Event handler that saves all changes and optionally closes the dialog. If there are data items
     * marked as deleted the user is asked for confirmation beforehand.
     *
     * @param exit whether to exit the dialog
     */
    void handleDialogSave(boolean exit) {

        m_saveHandler.save(m_provider.getItems());
        if (m_saveHandler.hasDeletedCurrent()) {
            String title = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_CONFIRM_DELETE_TITLE_0);
            String message = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_CONFIRM_DELETE_0);
            CmsConfirmationDialog confirmationDialog = CmsConfirmationDialog.show(title, message, new Runnable() {

                @SuppressWarnings("synthetic-access")
                @Override
                public void run() {

                    persist();
                    m_saveHandler.setFlagCancelSave(false);
                    if (exit) {
                        finishDialog(m_saveHandler.getChangedIds());
                    }
                }
            }, new Runnable() {

                @SuppressWarnings("synthetic-access")
                @Override
                public void run() {

                    m_saveHandler.setFlagCancelSave(true);
                }

            });
            confirmationDialog.displayResourceInfo(
                m_saveHandler.getDeletedCurrentResource(),
                org.opencms.ui.Messages.GUI_SELECTED_0);
        } else {
            persist();
            if (exit) {
                finishDialog(m_saveHandler.getChangedIds());
            }
        }
    }

    /**
     * For a given gallery folder resource, creates a panel with information whether
     * this gallery is in use.<p>
     *
     * @return the gallery in use panel
     * @throws CmsException the CMS exception
     */
    private HorizontalLayout createDisplayGalleryInUse() throws CmsException {

        HorizontalLayout layout1 = new HorizontalLayout();
        layout1.setWidthFull();
        layout1.addStyleNames("v-panel", "o-error-dialog", OpenCmsTheme.GALLERY_ALERT_IN_USE);
        HorizontalLayout layout2 = new HorizontalLayout();
        layout2.setWidthUndefined();
        Label icon = new Label(FontOpenCms.WARNING.getHtml());
        icon.setContentMode(ContentMode.HTML);
        icon.setWidthUndefined();
        icon.setStyleName("o-warning-icon");
        String galleryTitle = getGalleryTitle();
        Label message = new Label(CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_DIRECTLY_USED_1, galleryTitle));
        message.setContentMode(ContentMode.HTML);
        message.setWidthUndefined();
        layout2.addComponent(icon);
        layout2.addComponent(message);
        layout2.setComponentAlignment(message, Alignment.MIDDLE_LEFT);
        layout1.addComponent(layout2);
        layout1.setComponentAlignment(layout2, Alignment.MIDDLE_CENTER);
        return layout1;
    }

    /**
     * Creates a component showing a warning if this dialog was opened from the online project context.<p>
     *
     * @return the component
     */
    private HorizontalLayout createDisplayInOnlineProject() {

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.addStyleNames("v-panel", "o-error-dialog");
        Label icon = new Label(FontOpenCms.WARNING.getHtml());
        icon.setContentMode(ContentMode.HTML);
        icon.setWidthUndefined();
        icon.setStyleName("o-warning-icon");
        Label message = new Label(
            CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_LABEL_IN_ONLINE_PROJECT_0));
        message.setContentMode(ContentMode.HTML);
        message.setWidthUndefined();
        layout.addComponent(icon);
        layout.addComponent(message);
        layout.setComponentAlignment(message, Alignment.MIDDLE_LEFT);
        layout.setExpandRatio(message, 1.0f);
        return layout;
    }

    /**
     * Whether one of the editable gallery items has been modified by the user.<p>
     *
     * @return whether has changes
     */
    private boolean dataListHasChanges() {

        boolean hasChanges = false;
        for (DataItem dataItem : m_provider.getItems()) {
            if (dataItem.getBinder().hasChanges()) {
                hasChanges = true;
            }
        }
        return hasChanges;
    }

    /**
     * Loads the gallery item list.<p>
     */
    private void dataListLoad() {

        List<DataItem> dataList = new ArrayList<DataItem>();
        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            CmsResourceFilter resourceFilter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireFile();
            List<CmsResource> resources = cms.readResources(cms.getSitePath(m_gallery), resourceFilter);
            for (CmsResource resource : resources) {
                DataItem dataItem = new DataItem(resource);
                dataList.add(dataItem);
            }
        } catch (CmsException exception) {
            m_context.error(exception);
        }
        m_provider = new Provider(dataList);
    }

    /**
     * Displays the UI component representing the dialog header view.<p>
     */
    private void displayDataListHeaderView() {

        if (isReadOnly()) {
            m_dataListHeaderView.addComponent(createDisplayInOnlineProject());
        } else {
            try {
                List<CmsRelation> relations = getCms().getRelationsForResource(m_gallery, CmsRelationFilter.SOURCES);
                if ((relations != null) && !relations.isEmpty()) {
                    m_dataListHeaderView.addComponent(createDisplayGalleryInUse());
                }
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        m_compositeDataListHeader = new DataListHeaderComposite();
        m_dataListHeaderView.addComponent((Component)m_compositeDataListHeader);
    }

    /**
     * Displays the UI component representing the scrollable gallery item list.<p>
     *
     * @param scrollToTop whether to scroll to top after displaying the gallery item list
     */
    private void displayDataListView(boolean scrollToTop) {

        m_dataListView.removeAllComponents();
        List<DataItem> dataItemList = m_provider.fetch(m_pageHandler, m_filterHandler);
        m_dataListView.setColumns(3);
        m_dataListView.setRows(dataItemList.size() + 2);
        m_dataListView.setColumnExpandRatio(2, 1.0f);
        int i = 1;
        Label dummy = new Label(" ");
        dummy.setId("scrollToTop");
        dummy.setHeight("0px");
        m_dataListView.addComponent(dummy, 0, 0);
        for (DataItem dataItem : dataItemList) {
            dataItem.getCompositeFile().removeStyleName(OpenCmsTheme.GALLERY_GRID_ROW_ODD);
            dataItem.getCompositeFileDelete().removeStyleName(OpenCmsTheme.GALLERY_GRID_ROW_ODD);
            dataItem.getCompositeForm().removeStyleName(OpenCmsTheme.GALLERY_GRID_ROW_ODD);
            if ((i % 2) == 0) {
                dataItem.getCompositeFile().addStyleName(OpenCmsTheme.GALLERY_GRID_ROW_ODD);
                dataItem.getCompositeFileDelete().addStyleName(OpenCmsTheme.GALLERY_GRID_ROW_ODD);
                dataItem.getCompositeForm().addStyleName(OpenCmsTheme.GALLERY_GRID_ROW_ODD);
            }
            m_dataListView.addComponent(dataItem.getCompositeFile(), 0, i);
            m_dataListView.addComponent(dataItem.getCompositeFileDelete(), 1, i);
            m_dataListView.addComponent(dataItem.getCompositeForm(), 2, i);
            i++;
        }
        if (scrollToTop) {
            m_dataListViewScrollable.setScrollTop(0);
        }
    }

    /**
     * Sorts the gallery item list according to a given sort order and re-renders the
     * gallery item list view.<p>
     *
     * @param sortOrder the sort order
     */
    private void displayDataListViewSorted(String sortOrder) {

        SerializableComparator<DataItem> defaultSortOrder = m_provider.SORT_PATH_ASCENDING;
        if (sortOrder == null) {
            m_provider.setSortComparator(m_provider.SORT_TITLE_ASCENDING);
        } else if (sortOrder == m_messageSortTitleAscending) {
            m_provider.setSortComparator(m_provider.SORT_TITLE_ASCENDING);
        } else if (sortOrder == m_messageSortTitleDescending) {
            m_provider.setSortComparator(m_provider.SORT_TITLE_DESCENDING);
        } else if (sortOrder == m_messageSortDateLastModifiedAscending) {
            m_provider.setSortComparator(m_provider.SORT_DATE_ASCENDING);
        } else if (sortOrder == m_messageSortDateLastModifiedDescending) {
            m_provider.setSortComparator(m_provider.SORT_DATE_DESCENDING);
        } else if (sortOrder == m_messageSortPathAscending) {
            m_provider.setSortComparator(m_provider.SORT_PATH_ASCENDING);
        } else if (sortOrder == m_messageSortPathDescending) {
            m_provider.setSortComparator(m_provider.SORT_PATH_DESCENDING);
        } else if (sortOrder == m_messageSortUnusedFirst) {
            m_provider.setSortComparator(m_provider.SORT_UNUSED_FIRST);
        } else if (sortOrder == m_messageSortNoCopyrightFirst) {
            m_provider.setSortComparator(m_provider.SORT_NOCOPYRIGHT_FIRST);
        } else {
            m_provider.setSortComparator(defaultSortOrder);
        }
        setSessionSortOrder(sortOrder);
        displayDataListView(true);
    }

    /**
     * Returns the gallery title for a given gallery folder resource.<p>
     *
     * @return the title
     * @throws CmsException the CMS exception
     */
    private String getGalleryTitle() throws CmsException {

        String galleryTitle = getCms().readPropertyObject(
            m_gallery,
            CmsPropertyDefinition.PROPERTY_TITLE,
            false).getValue();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(galleryTitle)) {
            galleryTitle = m_gallery.getName();
        }
        return galleryTitle;
    }

    /**
     * Returns the current sort order saved in the user session with lazy initialization.<p>
     *
     * @return the sort order
     */
    private String getSessionSortOrder() {

        WrappedSession wrappedSession = VaadinService.getCurrentRequest().getWrappedSession();
        String currentSortOrder = (String)wrappedSession.getAttribute(GALLERY_OPTIMIZE_ATTR_SORT_ORDER);
        if (currentSortOrder == null) {
            wrappedSession.setAttribute(GALLERY_OPTIMIZE_ATTR_SORT_ORDER, m_messageSortPathAscending);
        }
        return (String)wrappedSession.getAttribute(GALLERY_OPTIMIZE_ATTR_SORT_ORDER);
    }

    /**
     * Initializes this dialog.<p>
     */
    private void initDialog() {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        displayResourceInfo(m_gallery);
        Button buttonCancel = createButtonCancel();
        buttonCancel.addClickListener(event -> {
            CmsGalleryOptimizeDialog.this.handleDialogCancel();
        });
        addButton(buttonCancel, false);
        m_buttonSave.setEnabled(!isReadOnly());
        m_buttonSaveAndExit.setEnabled(!isReadOnly());
    }

    /**
     * Initializes the events of this dialog.<p>
     */
    private void initEvents() {

        m_buttonSave.addClickListener(event -> {
            CmsGalleryOptimizeDialog.this.handleDialogSave(false);
        });
        m_buttonSaveAndExit.addClickListener(event -> {
            CmsGalleryOptimizeDialog.this.handleDialogSave(true);
        });
        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                CmsGalleryOptimizeDialog.this.handleDialogCancel();
            }

            @Override
            protected void ok() {

                CmsGalleryOptimizeDialog.this.handleDialogSave(true);
            }
        });
        addAttachListener(event -> {
            CmsGalleryOptimizeDialog.this.handleDialogAttach();
        });
        addDetachListener(event -> {
            CmsGalleryOptimizeDialog.this.handleDialogDetach();
        });
    }

    /**
     * Locks the gallery folder.<p>
     */
    private void initLock() {

        try {
            m_lockActionRecord = CmsLockUtil.ensureLock(getCms(), m_gallery);
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Initializes the localized messages of this dialog.<p>
     */
    private void initMessages() {

        m_messageSortDateLastModifiedAscending = CmsVaadinUtils.getMessageText(
            Messages.GUI_GALLERY_OPTIMIZE_SORT_DATE_MODIFIED_ASCENDING_0);
        m_messageSortDateLastModifiedDescending = CmsVaadinUtils.getMessageText(
            Messages.GUI_GALLERY_OPTIMIZE_SORT_DATE_MODIFIED_DESCENDING_0);
        m_messageSortPathAscending = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_SORT_PATH_ASCENDING_0);
        m_messageSortPathDescending = CmsVaadinUtils.getMessageText(
            Messages.GUI_GALLERY_OPTIMIZE_SORT_PATH_DESCENDING_0);
        m_messageSortTitleAscending = CmsVaadinUtils.getMessageText(
            Messages.GUI_GALLERY_OPTIMIZE_SORT_TITLE_ASCENDING_0);
        m_messageSortTitleDescending = CmsVaadinUtils.getMessageText(
            Messages.GUI_GALLERY_OPTIMIZE_SORT_TITLE_DESCENDING_0);
        m_messageSortUnusedFirst = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_SORT_UNUSED_FIRST_0);
        m_messageSortNoCopyrightFirst = CmsVaadinUtils.getMessageText(
            Messages.GUI_GALLERY_OPTIMIZE_SORT_NOCOPYRIGHT_FIRST_0);
    }

    /**
     * Persists all data changes that have not been saved yet. Refreshes the UI.
     * Informs the user about failed updates, failed renames and failed deletes.<p>
     */
    private void persist() {

        StringBuilder errorMessageList = new StringBuilder();
        persistUpdateAndRename(errorMessageList);
        persistDelete(errorMessageList);
        if (errorMessageList.length() == 0) {
            String message = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_LABEL_SUCCESSFULLY_SAVED_0);
            Notification notification = new Notification(message, "", Notification.Type.HUMANIZED_MESSAGE);
            notification.setPosition(Position.TOP_CENTER);
            notification.show(Page.getCurrent());
        } else {
            Notification notification = new Notification(
                "",
                errorMessageList.toString(),
                Notification.Type.ERROR_MESSAGE);
            notification.setHtmlContentAllowed(true);
            notification.setPosition(Position.TOP_CENTER);
            notification.show(Page.getCurrent());
        }
    }

    /**
     * Persists all deleted gallery items.<p>
     *
     * @param errorMessageList string builder to append error messages
     */
    private void persistDelete(StringBuilder errorMessageList) {

        List<DataItem> deleted = new ArrayList<DataItem>();
        for (DataItem dataItem : m_saveHandler.getDeletedCurrent()) {
            CmsResource resource = dataItem.getResource();
            try {
                getCms().deleteResource(getCms().getSitePath(resource), CmsResource.DELETE_PRESERVE_SIBLINGS);
                deleted.add(dataItem);
            } catch (CmsException e) {
                errorMessageList.append("<div>" + e.getLocalizedMessage() + "</div>");
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        handleDataListDelete(deleted);
        if (m_saveHandler.hasDeletedCurrent()) {
            displayDataListView(true);
        }
    }

    /**
     * Persists all updated and renamed gallery items.<p>
     *
     * @param errorMessageList string builder to append error messages
     */
    private void persistUpdateAndRename(StringBuilder errorMessageList) {

        List<DataItem> updated = new ArrayList<DataItem>();
        for (DataItem dataItem : m_saveHandler.getChangedCurrent()) {
            CmsResource resource = dataItem.getResource();
            if (dataItem.hasChanges()) {
                try {
                    getCms().writePropertyObjects(resource, dataItem.getPropertyList());
                    getCms().writeResource(resource);
                    updated.add(dataItem);
                } catch (CmsException e) {
                    errorMessageList.append("<div>" + e.getLocalizedMessage() + "</div>");
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            }
            if (dataItem.isRenamed()) {
                String source = getCms().getSitePath(resource);
                String destination = CmsStringUtil.joinPaths(CmsResource.getParentFolder(source), dataItem.getName());
                try {
                    getCms().renameResource(source, destination);
                    if (!updated.contains(dataItem)) {
                        updated.add(dataItem);
                    }
                } catch (CmsException e) {
                    errorMessageList.append("<div>" + e.getLocalizedMessage() + "</div>");
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            }
        }
        try {
            handleDataListUpdate(updated);
        } catch (CmsException e) {
            errorMessageList.append("<div>" + e.getLocalizedMessage() + "</div>");
            LOG.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Saves the selected sort order in the user session.<p>
     *
     * @param sortOrder the sort order
     */
    private void setSessionSortOrder(String sortOrder) {

        WrappedSession wrappedSession = VaadinService.getCurrentRequest().getWrappedSession();
        wrappedSession.setAttribute(GALLERY_OPTIMIZE_ATTR_SORT_ORDER, sortOrder);
    }

    /**
     * Unlocks the gallery folder.<p>
     */
    private void unlock() {

        if (m_lockActionRecord != null) {
            try {
                getCms().unlockResource(m_gallery);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
    }

}
