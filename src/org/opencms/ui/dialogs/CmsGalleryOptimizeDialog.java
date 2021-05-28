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
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.Page.Styles;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Class representing a dialog for optimizing image galleries.<p>
 */
public class CmsGalleryOptimizeDialog extends CmsBasicDialog {

    /**
     * Class representing an editable gallery image.<p>
     */
    private class DataItem {

        /** The data binder of this editable gallery image. */
        private Binder<DataItem> m_binder = new Binder<DataItem>();

        /** The form composite of this editable gallery image. */
        private FormComposite m_compositeForm;

        /** The image composite of this editable gallery image. */
        private ImageComposite m_compositeImage;

        /** The image delete composite of this editable gallery image. */
        private ImageDeleteComposite m_compositeImageDelete;

        /** The copyright information of this editable gallery image. */
        private String m_copyright;

        /** Date when this editable gallery image was last modified. */
        private Long m_dateLastModified;

        /** Whether this editable gallery image shall be deleted. */
        private Boolean m_deleteFlag = Boolean.valueOf(false);

        /** The description of this editable gallery image. */
        private String m_description;

        /** Whether this editable gallery image is used. */
        private Boolean m_isUsed;

        /** The file name of this editable gallery image. */
        private String m_name;

        /** The full path of this editable gallery image. */
        private String m_path;

        /** The CMS resource of this editable gallery image. */
        private CmsResource m_resource;

        /** The CMS resource utility of this editable gallery image. */
        private CmsResourceUtil m_resourceUtil;

        /** The title of this editable gallery image. */
        private String m_title;

        /**
         * Creates a new editable gallery image for a given CMS resource.<p>
         *
         * @param resource the CMS resource
         */
        public DataItem(CmsResource resource) {

            m_resource = resource;
            initData();
            initComponent();
        }

        /**
         * Returns the binder of this editable gallery image.<p>
         *
         * @return the binder
         */
        public Binder<DataItem> getBinder() {

            return m_binder;
        }

        /**
         * Returns the form composite of this editable gallery image.<p>
         *
         * @return the form composite
         */
        public FormComposite getCompositeForm() {

            return m_compositeForm;
        }

        /**
         * Returns the image composite of this editable gallery image.<p>
         *
         * @return the image composite
         */
        public ImageComposite getCompositeImage() {

            return m_compositeImage;
        }

        /**
         * Returns the image delete composite of this editable gallery image.<p>
         *
         * @return the image delete composite
         */
        public ImageDeleteComposite getCompositeImageDelete() {

            return m_compositeImageDelete;
        }

        /**
         * Returns the copyright information of this editable gallery image.<p>
         *
         * @return the copyright information
         */
        public String getCopyright() {

            return m_copyright;
        }

        /**
         * Returns the date when this editable gallery image was last modified.<p>
         *
         * @return the date
         */
        public Long getDateLastModified() {

            return m_dateLastModified;
        }

        /**
         * Returns whether this editable gallery image shall be deleted.<p>
         *
         * @return whether delete or not
         */
        public Boolean getDeleteFlag() {

            return m_deleteFlag;
        }

        /**
         * Returns the description of this editable gallery image.<p>
         *
         * @return the description
         */
        public String getDescription() {

            return m_description;
        }

        /**
         * Returns whether this editable gallery image is used.<p>
         *
         * @return whether used or not
         */
        public Boolean getIsUsed() {

            return m_isUsed;
        }

        /**
         * Returns the name of this editable gallery image.<p>
         *
         * @return the name
         */
        public String getName() {

            return m_name;
        }

        /**
         * Returns the full path of this editable gallery image.<p>
         *
         * @return the full path
         */
        public String getPath() {

            return m_path;
        }

        /**
         * Converts the form data of this editable gallery image into a list of CMS properties.<p>
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
         * Returns the CMS resource this editable gallery image was created from.<p>
         *
         * @return the CMS resource
         */
        public CmsResource getResource() {

            return m_resource;
        }

        /**
         * Returns the CMS resource utility class for this editable gallery image.<p>
         *
         * @return the CMS resource utility class
         */
        public CmsResourceUtil getResourceUtil() {

            return m_resourceUtil;
        }

        /**
         * Returns the title of this editable gallery image.<p>
         *
         * @return the title
         */
        public String getTitle() {

            return m_title;
        }

        /**
         * Returns whether this editable gallery image has value changes compared
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
         * Sets the copyright information of this editable gallery image.<p>
         *
         * @param copyright the copyright information
         */
        public void setCopyright(String copyright) {

            m_copyright = copyright;
        }

        /**
         * Sets the flag that states whether this editable gallery image shall be deleted.<p>
         *
         * @param deleteFlag the flag
         */
        public void setDeleteFlag(Boolean deleteFlag) {

            m_deleteFlag = deleteFlag;
        }

        /**
         * Sets the description of this editable gallery image.<p>
         *
         * @param description the description
         */
        public void setDescription(String description) {

            m_description = description;
        }

        /**
         * Sets the CMS resource if this editable gallery image and re-initializes all data and components.<p>
         *
         * @param resource the CMS resource
         */
        public void setResource(CmsResource resource) {

            m_resource = resource;
            initData();
            initComponent();
        }

        /**
         * Sets the title of this editable gallery image.<p>
         *
         * @param title the title
         */
        public void setTitle(String title) {

            m_title = title;
        }

        /**
         * Initializes all UI components of this editable gallery image and initializes all data fields.<p>
         */
        private void initComponent() {

            m_compositeImage = new ImageComposite(this);
            m_compositeImageDelete = new ImageDeleteComposite(this);
            m_compositeForm = new FormComposite(this);
            m_binder.readBean(this);
        }

        /**
         * Initializes all data of this editable gallery image.<p>
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
     * Class representing the data list header view for sorting and paging the data item list.
     */
    private class DataListHeaderComposite extends HorizontalLayout {

        /** The default serial version UID. */
        private static final long serialVersionUID = 1L;

        /** Page info label. */
        private Label m_labelPageInfo;

        /** The select box for page selection. */
        private NativeSelect<Integer> m_selectPage;

        /** The select box for sort order selection. */
        private NativeSelect<String> m_selectSortOrder;

        /**
         * Creates a new data list header composite.
         */
        public DataListHeaderComposite() {

            setHeightUndefined();
            setWidthFull();
            m_selectSortOrder = createSelectSortOrder();
            m_selectPage = createSelectPage();
            m_labelPageInfo = createLabelPageInfo();
            addComponent(m_selectSortOrder);
            addComponent(m_selectPage);
            addComponent(m_labelPageInfo);
            setComponentAlignment(m_selectSortOrder, Alignment.MIDDLE_LEFT);
            setComponentAlignment(m_selectPage, Alignment.MIDDLE_CENTER);
            setComponentAlignment(m_labelPageInfo, Alignment.MIDDLE_RIGHT);
        }

        /**
         * Refreshes this component.<p>
         */
        public void refresh() {

            removeComponent(m_selectPage);
            removeComponent(m_labelPageInfo);
            m_selectPage = createSelectPage();
            m_labelPageInfo = createLabelPageInfo();
            addComponent(m_selectPage);
            addComponent(m_labelPageInfo);
            setComponentAlignment(m_selectSortOrder, Alignment.MIDDLE_LEFT);
            setComponentAlignment(m_selectPage, Alignment.MIDDLE_CENTER);
            setComponentAlignment(m_labelPageInfo, Alignment.MIDDLE_RIGHT);
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
                    String.valueOf(getSize()));
            } else {
                text = CmsVaadinUtils.getMessageText(
                    Messages.GUI_GALLERY_OPTIMIZE_LABEL_PAGE_INFO_1,
                    String.valueOf(getSize()));
            }
            Label label = new Label(text);
            label.setWidthUndefined();
            return label;
        }

        /**
         * Creates a new select box for page select.<p>
         *
         * @return the page select box
         */
        @SuppressWarnings("synthetic-access")
        private NativeSelect<Integer> createSelectPage() {

            NativeSelect<Integer> selectPage = new NativeSelect<Integer>();
            selectPage.setWidthUndefined();
            int numPages = getNumPages();
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
         * Creates a new select box for sort order select.<p>
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
                m_messageSortUnusedFirst);
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
         * Returns the number of available pages.<p>
         *
         * @return the number of available pages
         */
        private int getNumPages() {

            return (int)Math.ceil((double)getSize() / PageHandler.LIMIT);
        }

        /**
         * Utility function that returns the size of the data list.
         *
         * @return the data list size
         */
        @SuppressWarnings("synthetic-access")
        private int getSize() {

            return m_provider.getItems().size();
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
     * Class representing a form composite to edit gallery image data.<p>
     */
    private class FormComposite extends AbsoluteLayout {

        /** The default serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The data item of this form composite. */
        private DataItem m_dataItem;

        /**
         * Creates a new form composite for a given data item.<p>
         *
         * @param dataItem the data item
         */
        public FormComposite(DataItem dataItem) {

            m_dataItem = dataItem;
            setSizeFull();
            setStyleName("o-gallery-composite-form");
            addComponent(createDisplayResourceInfo(), "top: 0px;");
            addComponent(createDisplayResourceDate(), "top: 42px;");
            FormLayout formLayout = new FormLayout();
            formLayout.setMargin(false);
            formLayout.setSpacing(false);
            formLayout.addStyleName("o-gallery-formlayout");
            formLayout.addComponent(createFieldTitle());
            formLayout.addComponent(createFieldCopyright());
            formLayout.addComponent(createFieldDescription());
            addComponent(formLayout, "bottom: 0px;");
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
         * Returns a component to display resource information.<p>
         *
         * @return the component
         */
        private CmsResourceInfo createDisplayResourceInfo() {

            CmsResourceInfo resourceInfo = new CmsResourceInfo(m_dataItem.getResource());
            resourceInfo.setTopLineText(m_dataItem.getName());
            return resourceInfo;
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
     * Class representing an image composite offering an image preview.<p>
     */
    private class ImageComposite extends HorizontalLayout {

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

        /** The data item of this image composite. */
        private DataItem m_dataItem;

        /** The main panel of this image composite. */
        private AbsoluteLayout m_panel;

        /**
         * Creates a new image composite for a given data item.<p>
         *
         * @param dataItem the data item
         */
        public ImageComposite(DataItem dataItem) {

            m_dataItem = dataItem;
            setSizeUndefined();
            setMargin(true);
            m_panel = new AbsoluteLayout();
            m_panel.setWidth(PANEL_WIDTH);
            m_panel.setHeight(PANEL_HEIGHT);
            m_panel.addStyleName("v-panel");
            m_panel.addComponent(createClickableVaadinImage(), "left: 2px; top: 2px;");
            addComponent(m_panel);
        }

        /**
         * Utility function to create a clickable Vaadin image.<p>
         *
         * @return the clickable Vaadin image
         */
        private Link createClickableVaadinImage() {

            CmsResource resource = m_dataItem.getResource();
            ExternalResource externalResource = new ExternalResource(getScaleUri(resource));
            Link link = new Link(null, new ExternalResource(getPermanentUri(resource)));
            link.setWidth(PANEL_WIDTH);
            link.setHeight(PANEL_HEIGHT);
            link.setIcon(externalResource);
            link.setTargetName("_blank");
            link.setStyleName("o-gallery-optimize-image-preview");
            return link;
        }

        /**
         * Utility function to create a permanent URI for a preview image.<p>
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
     * Class representing a image delete composite with a check box to mark an image as deleted.<p>
     */
    private class ImageDeleteComposite extends VerticalLayout {

        /** The default serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The component width. */
        private static final String WIDTH = "206px";

        /** The data item of this image composite. */
        private DataItem m_dataItem;

        /**
         * Creates a new image delete composite for a given data item.<p>
         *
         * @param dataItem the data item
         */
        public ImageDeleteComposite(DataItem dataItem) {

            m_dataItem = dataItem;
            setMargin(new MarginInfo(true, true, true, false));
            setSpacing(false);
            setWidth(WIDTH);
            setHeightFull();
            Label dimension = createDisplayDimension();
            Label fileSize = createDisplayFileSize();
            addComponent(dimension);
            addComponent(fileSize);
            if (!m_dataItem.getIsUsed().booleanValue()) {
                CssLayout layout = new CssLayout();
                Label labelInUse = createDisplayInUseInfo();
                CheckBox fieldDeleteFlag = createFieldDeleteFlag();
                layout.addComponent(labelInUse);
                layout.addComponent(fieldDeleteFlag);
                addComponent(layout);
                setExpandRatio(layout, 1.0f);
                setComponentAlignment(layout, Alignment.BOTTOM_LEFT);
            } else {
                setExpandRatio(fileSize, 1.0f);
            }
        }

        /**
         * Creates a component displaying the dimension of this editable gallery image.<p>
         *
         * @return the display component
         */
        private Label createDisplayDimension() {

            return new Label(getFormattedDimension());
        }

        /**
         * Creates a component displaying the size of this editable gallery image.<p>
         *
         * @return the display component
         */
        private Label createDisplayFileSize() {

            return new Label(getFormattedFileSize());
        }

        /**
         * Creates a component displaying whether this editable gallery image is used.<p>
         *
         * @return the display component
         */
        private Label createDisplayInUseInfo() {

            String notInUse = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_LABEL_NOT_IN_USE_0);
            return new Label(notInUse);
        }

        /**
         * Creates a check box to mark an image as deleted.<p>
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
         * Returns the dimension of this editable gallery image in a formatted way.<p>
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
         * Returns the size of this editable gallery image in a formatted way.<p>
         *
         * @return the formatted file size
         */
        private String getFormattedFileSize() {

            return (m_dataItem.getResource().getLength() / 1024) + " kb";
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
            if (lastItem > getSizeItem()) {
                lastItem = getSizeItem();
            }
            return lastItem;
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

            return m_provider.getItems().size();
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

        /**
         * Create a new provider for a given data item list.
         *
         * @param dataItemList the data item list
         */
        public Provider(List<DataItem> dataItemList) {

            super(dataItemList);
        }

        /**
         * Fetches one page of the sorted in-memory data item list.<p>
         *
         * @param pageHandler the page handler containing offset and limit information
         * @return the sorted data item page
         */
        public List<DataItem> fetch(PageHandler pageHandler) {

            Query<DataItem, SerializablePredicate<DataItem>> query = new Query<DataItem, SerializablePredicate<DataItem>>(
                pageHandler.getOffset(),
                pageHandler.getLimit(),
                Collections.emptyList(),
                getSortComparator(),
                null);
            return super.fetch(query).collect(Collectors.toList());
        }

        /**
         * @see com.vaadin.data.provider.ListDataProvider#getItems()
         */
        @Override
        public List<DataItem> getItems() {

            return (List<DataItem>)super.getItems();
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

    /** The UI composite representing the gallery image list header view. */
    private Object m_compositeDataListHeader;

    /** The UI component representing the gallery image list header view. */
    private VerticalLayout m_dataListHeaderView;

    /** The UI component representing the gallery image list view. */
    private GridLayout m_dataListView;

    /** The lock action record for the gallery image folder. */
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

    /** The page handler. */
    private PageHandler m_pageHandler = new PageHandler();

    /** The data provider. */
    private Provider m_provider;

    /** The save handler. */
    private SaveHandler m_saveHandler = new SaveHandler();

    /**
     * Creates a new instance of the gallery optimize dialog.<p>
     *
     * @param context the dialog context
     */
    public CmsGalleryOptimizeDialog(I_CmsDialogContext context) {

        m_context = context;
        initMessages();
        initDialog();
        initStyles();
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
    void handleDataItemDelete(List<DataItem> dataItemList) {

        m_provider.getItems().removeAll(dataItemList);
        ((DataListHeaderComposite)m_compositeDataListHeader).refresh();
    }

    /**
     * Refreshes the UI state after a list of data items has been successfully updated.<p>
     *
     * @param dataItemList the list of updated data items
     * @throws CmsException thrown if reading a persisted CMS resource fails
     */
    void handleDataItemUpdate(List<DataItem> dataItemList) throws CmsException {

        for (DataItem dataItem : dataItemList) {
            CmsResource reload = getCms().readResource(dataItem.getResource().getStructureId());
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

                    saveAndDelete();
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
            saveAndDelete();
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

        CmsResource resource = m_context.getResources().get(0);
        HorizontalLayout layout1 = new HorizontalLayout();
        layout1.setWidthFull();
        layout1.addStyleNames("v-panel", "o-error-dialog", "o-gallery-in-use");
        HorizontalLayout layout2 = new HorizontalLayout();
        layout2.setWidthUndefined();
        Label icon = new Label(FontOpenCms.WARNING.getHtml());
        icon.setContentMode(ContentMode.HTML);
        icon.setWidthUndefined();
        icon.setStyleName("o-warning-icon");
        String galleryTitle = getGalleryTitle(resource);
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
     * Whether one of the editable gallery images has been modified by the user.<p>
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
     * Loads the gallery image list.<p>
     */
    private void dataListLoad() {

        List<DataItem> dataList = new ArrayList<DataItem>();
        CmsResource root = m_context.getResources().get(0);
        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            m_context.getResources().get(0);
            CmsResourceFilter resourceFilter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(
                new CmsResourceTypeImage());
            List<CmsResource> resources = cms.readResources(cms.getSitePath(root), resourceFilter);
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

        m_dataListHeaderView.setHeightUndefined();
        m_dataListHeaderView.setWidthFull();
        m_dataListHeaderView.setMargin(false);
        if (isReadOnly()) {
            m_dataListHeaderView.addComponent(createDisplayInOnlineProject());
        } else {
            try {
                CmsResource resource = m_context.getResources().get(0);
                List<CmsRelation> relations = getCms().getRelationsForResource(resource, CmsRelationFilter.SOURCES);
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
     * Displays the UI component representing the scrollable gallery image list.<p>
     *
     * @param scrollToTop whether to scroll to top after displaying gallery image list
     */
    private void displayDataListView(boolean scrollToTop) {

        m_dataListView.setHeightUndefined();
        m_dataListView.setWidthFull();
        m_dataListView.setMargin(false);
        m_dataListView.setSpacing(false);
        m_dataListView.removeAllComponents();
        List<DataItem> dataItemList = m_provider.fetch(m_pageHandler);
        m_dataListView.setColumns(3);
        m_dataListView.setRows(dataItemList.size() + 1);
        m_dataListView.setColumnExpandRatio(2, 1.0f);
        int i = 1;
        Label dummy = new Label(" ");
        dummy.setId("scrollToTop");
        dummy.setHeight("0px");
        m_dataListView.addComponent(dummy, 0, 0);
        for (DataItem dataItem : dataItemList) {
            dataItem.getCompositeImage().removeStyleName("o-gallery-optimize-row-odd");
            dataItem.getCompositeImageDelete().removeStyleName("o-gallery-optimize-row-odd");
            dataItem.getCompositeForm().removeStyleName("o-gallery-optimize-row-odd");
            if ((i % 2) == 0) {
                dataItem.getCompositeImage().addStyleName("o-gallery-optimize-row-odd");
                dataItem.getCompositeImageDelete().addStyleName("o-gallery-optimize-row-odd");
                dataItem.getCompositeForm().addStyleName("o-gallery-optimize-row-odd");
            }
            m_dataListView.addComponent(dataItem.getCompositeImage(), 0, i);
            m_dataListView.addComponent(dataItem.getCompositeImageDelete(), 1, i);
            m_dataListView.addComponent(dataItem.getCompositeForm(), 2, i);
            i++;
        }
        if (scrollToTop) {
            A_CmsUI.get().getPage().getJavaScript().execute(
                "document.getElementById('scrollToTop').scrollIntoView(true);");
        }
    }

    /**
     * Sorts the gallery image list according to a given sort order and re-renders the
     * gallery image list view.
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
        } else {
            m_provider.setSortComparator(defaultSortOrder);
        }
        setSessionSortOrder(sortOrder);
        displayDataListView(true);
    }

    /**
     * Returns the gallery title for a given gallery folder resource.<p>
     *
     * @param resource the gallery folder resource
     * @return the title
     * @throws CmsException the CMS exception
     */
    private String getGalleryTitle(CmsResource resource) throws CmsException {

        String galleryTitle = getCms().readPropertyObject(
            resource,
            CmsPropertyDefinition.PROPERTY_TITLE,
            false).getValue();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(galleryTitle)) {
            galleryTitle = resource.getName();
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
        displayResourceInfo(m_context.getResources(), "");
        Button buttonCancel = createButtonCancel();
        buttonCancel.addClickListener(event -> {
            CmsGalleryOptimizeDialog.this.handleDialogCancel();
        });
        addButton(buttonCancel, false);
        m_buttonSave.setEnabled(!isReadOnly());
        m_buttonSaveAndExit.setEnabled(!isReadOnly());
    }

    /**
     * Initializes the events of this dialog.
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
     * Locks the gallery folder.
     */
    private void initLock() {

        try {
            m_lockActionRecord = CmsLockUtil.ensureLock(getCms(), m_context.getResources().get(0));
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Initializes the localized messages of this dialog.
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
    }

    /**
     * Initializes some styles for this dialog.<p>
     */
    private void initStyles() {

        Styles styles = Page.getCurrent().getStyles();
        styles.add(".o-gallery-optimize-row-odd { background-color: #E0E0E2; }");
        styles.add(
            ".o-gallery-optimize-image-preview {"
                + "background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAI0AAABkCAMAAACMy0PdAAAAS1BMVEXm5ua5ubn09PTw8PDy8vLp6ens7Oz29vbu7u7IyMjNzc2+vr7ExMTBwcHKysrPz8/R0dHc3NzY2NjT09Ph4eHl5eXj4+Pe3t7V1dW2TkV8AAADjklEQVRo3u2X7XajIBBAyXZxC/ItoO//pGuSSawIo+eIrT3HO3+iglyHAVpycXFxcXFxcXFxcXGxD3t7w8iP4yYbQX4Sb0fMZCNtjkC+BdfetqAJcKzWJIPSvi30mD7uPDkEGExM5P0cAbq3oDA2HGEjPZnwMmcjCNAntlINvq4NJ1/RtxyvIYPMPhbahkNs+LJqBGPDy4YlD7FE1bcRxvqQ7k04yu+x0eWZ4j1JCHp9HcodNslp8GUqWE8yeLPqs2uFczfxZaIGUsAPuE+7w6aA7NFzFoNVt5FJKcaQHLQI3W4bXCYq2Y43Tb8pO/1+G/yFw2uR+WknKBIq2aQF7JmFhS1eFToEuKNqLymyUoZW3lqfzgwnQPtNNj0Y3MeTkAs1uUa8dGRdGzU7ztXi9G49mhxV18bOD8huUbYaO+5vrqqNeM6NS+x8m262Pm8Tq9poWEXpzHSL5AikiGvZ2EWNygB+yfe7420gE2ZZmC7dkEK2f1UbWNJsdg/SlW46Mld2VW1EZiCX/gHPptpKMQfYhDb9fyrqpTLL9PdVbVhqAzvO0CaNCosqHJobWEF+NjIv25Aj6kYku4vK7bfycBsZ04PAL4/ICNtz/mPq7zduNite5BJgMzac4NBikPa2DJiFKOH6bhfMbdaGETBe9h97UyQw2hzwdQYu1X09yXYGlA3P9I4EhSJkbUSkd6J4Xvbjb5s0gZfKTG+KgiauzeLoA/e80oRS4mYNDH3g26wNQQNBZmHQp4PLAL9fGHiu5RJBUXCbPA4eGxjBw9Dzp72QS3R9GxbhuRNvAaKT8aLIfsgOG1FAU8Bz8cBQSh4/uX29UIscfs2mKQYVJRx9NBjpDXtYEEqUUIG+6EQWOnZEAs9NEfvle7x1zvX3yaFvnMhDGzw3DYJgpWC2wYhM5Ps1OATLHENwtCnOsGMFeEPRQHPDMEws9PLIJzQ4uA2KcrnO1CpWxO+1wX08navQyBkC2WOj1tGD9ZHQu1TwhnOF0lSywemaqBWY7LL5bMrBN6Kp39auaT6xwHPDNxJ7volhPTcIhm+i7/XGhp8rEPThoDfgeqO3Qddt6kC968wan+s2H1XiSRN6h9rgb6mWG3B6QqMdugxjCxzycRgN8W4mY1e7VLHBpYIFqfiBg9hUZpy99aFGmzNBPv6eKE6Wm79n4rL5PTZ/ThTkz5m4bH6Pzb8Txcly8+9MXDZl/gM9XrODdIfTogAAAABJRU5ErkJggg==) -0px -0px  no-repeat;\n"
                + "background-position: center;\n"
                + "width: "
                + IMAGE_WIDTH
                + "px;\n"
                + "height: "
                + IMAGE_HEIGHT
                + "px;\n"
                + "}");
        styles.add(
            ".o-gallery-formlayout .v-formlayout-contentcell, .o-gallery-formlayout .v-formlayout-captioncell {"
                + " padding-top: 4px;\n"
                + " }");
        styles.add(".o-gallery-composite-form { padding: 8px; }");
        styles.add(
            "div.v-horizontallayout-o-gallery-in-use {\n"
                + " color: white !important;\n"
                + " background-color: #b31b34 !important;\n"
                + " background-image: linear-gradient(to bottom,#b31b34 0%, #b31b34 100%) !important;\n"
                + "}");
    }

    /**
     * Persists all data changes that have not been saved yet. Refreshes the UI.
     * Informs the user about failed updates and failed deletes.
     */
    private void saveAndDelete() {

        StringBuilder errorMessageList = new StringBuilder();
        List<DataItem> saved = new ArrayList<DataItem>();
        for (DataItem dataItem : m_saveHandler.getChangedCurrent()) {
            if (dataItem.hasChanges()) {
                CmsResource resource = dataItem.getResource();
                try {
                    getCms().writePropertyObjects(resource, dataItem.getPropertyList());
                    getCms().writeResource(resource);
                    saved.add(dataItem);
                } catch (CmsException e) {
                    errorMessageList.append("<div>" + e.getLocalizedMessage() + "</div>");
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            }
        }
        try {
            handleDataItemUpdate(saved);
        } catch (CmsException e) {
            errorMessageList.append("<div>" + e.getLocalizedMessage() + "</div>");
            LOG.warn(e.getLocalizedMessage(), e);
        }
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
        handleDataItemDelete(deleted);
        if (m_saveHandler.hasDeletedCurrent()) {
            displayDataListView(true);
        }
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
     * Saves the selected sort order in the user session.<p>
     *
     * @param sortOrder the sort order
     */
    private void setSessionSortOrder(String sortOrder) {

        WrappedSession wrappedSession = VaadinService.getCurrentRequest().getWrappedSession();
        wrappedSession.setAttribute(GALLERY_OPTIMIZE_ATTR_SORT_ORDER, sortOrder);
    }

    /**
     * Unlocks the gallery folder.
     */
    private void unlock() {

        if (m_lockActionRecord != null) {
            CmsResource resource = m_context.getResources().get(0);
            try {
                getCms().unlockResource(resource);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
    }

}
