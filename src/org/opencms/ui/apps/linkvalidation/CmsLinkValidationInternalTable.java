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

package org.opencms.ui.apps.linkvalidation;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsInternalLinksValidator;
import org.opencms.relations.CmsRelation;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsFileExplorerConfiguration;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsResourceIcon;
import org.opencms.ui.components.CmsResourceIcon.IconMode;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Image;
import com.vaadin.ui.Table;

/**
 * Result table for broken internal relations.<p>
 */
public class CmsLinkValidationInternalTable extends Table {

    /**
     * The menu entry to switch to the explorer of concerning site.<p>
     */
    class ExplorerEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            CmsUUID uuid = new CmsUUID(data.iterator().next());
            CmsResource res;
            try {
                res = A_CmsUI.getCmsObject().readResource(uuid);
                openExplorerForParent(res.getRootPath(), res.getStructureId().getStringValue());
            } catch (CmsException e) {
                //
            }

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return Messages.get().getBundle(locale).key(Messages.GUI_EXPLORER_TITLE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            return (data != null) && (data.size() == 1)
            ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE
            : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }

    }

    /**
     * All table properties.<p>
     */
    enum TableProperty {

        /**Broken links column. */
        BrokenLinks(Messages.GUI_LINKVALIDATION_BROKENLINKS_DETAIL_LINKS_NAME_0, String.class, "", false),

        /**Date of expiration column. */
        DateExpired(Messages.GUI_LINKVALIDATION_EXPIRED_DATE_0, Date.class, null, true),

        /**Date of release column.*/
        DateReleased(Messages.GUI_LINKVALIDATION_RELEASE_DATE_0, Date.class, null, true),

        /**Icon column.*/
        Icon(null, Image.class, null, false),

        /**Last modified column. */
        LastModified(Messages.GUI_LINKVALIDATION_LASTMODIFIED_0, Date.class, null, false),

        /**Path column.*/
        Path(Messages.GUI_LINKVALIDATION_PATH_0, String.class, "", false),

        /**Size of file column. */
        Size(Messages.GUI_LINKVALIDATION_SIZE_0, String.class, "", false),

        /**Title column. */
        Title(Messages.GUI_LINKVALIDATION_TITLE_0, String.class, "", false),

        /**Type column. */
        Type(Messages.GUI_LINKVALIDATION_TYPE_0, String.class, "", false);

        /**Indicates if column is collapsable.*/
        private boolean m_collapsable;

        /**Default value for column.*/
        private Object m_defaultValue;

        /**Header Message key.*/
        private String m_headerMessage;

        /**Type of column property.*/
        private Class<?> m_type;

        /**
         * constructor.
         *
         * @param headerMessage key
         * @param type to property
         * @param defaultValue of column
         * @param collapsable should this column be collapsable?
         */
        TableProperty(String headerMessage, Class<?> type, Object defaultValue, boolean collapsable) {
            m_headerMessage = headerMessage;
            m_type = type;
            m_defaultValue = defaultValue;
            m_collapsable = collapsable;
        }

        /**
         * Returns list of all properties with non-empty header.<p>
         *
         * @return list of properties
         */
        static List<TableProperty> withHeader() {

            List<TableProperty> props = new ArrayList<TableProperty>();

            for (TableProperty prop : TableProperty.values()) {
                if (prop.m_headerMessage != null) {
                    props.add(prop);
                }
            }
            return props;
        }

        /**
         * Returns the default value of property.<p>
         *
         * @return object
         */
        Object getDefaultValue() {

            return m_defaultValue;
        }

        /**
         * Returns localized header.<p>
         *
         * @return string for header
         */
        String getLocalizedMessage() {

            if (m_headerMessage == null) {
                return "";
            }
            return CmsVaadinUtils.getMessageText(m_headerMessage);
        }

        /**
         * Returns tye of value for given property.<p>
         *
         * @return type
         */
        Class<?> getType() {

            return m_type;
        }

        /**
         * Indicates if column is collapsable.<p>
         *
         * @return boolean, true = is collapsable
         */
        boolean isCollapsable() {

            return m_collapsable;
        }

    }

    /**vaadin serial id.*/
    private static final long serialVersionUID = -5023815553518761192L;

    /**Internal link validator instance. */
    CmsInternalLinksValidator m_validator;

    /**Indexed Container.*/
    private IndexedContainer m_container;

    /** The context menu. */
    private CmsContextMenu m_menu;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**
     * Constructor for table.<p>
     */
    protected CmsLinkValidationInternalTable() {

        m_container = new IndexedContainer();

        setContainerDataSource(m_container);

        setColumnCollapsingAllowed(true);

        for (TableProperty prop : TableProperty.values()) {
            m_container.addContainerProperty(prop, prop.getType(), prop.getDefaultValue());
            setColumnHeader(prop, prop.getLocalizedMessage());
            setColumnCollapsible(prop, prop.isCollapsable());
            if (prop.isCollapsable()) {
                setColumnCollapsed(prop, prop.isCollapsable());
            }
        }

        setColumnWidth(TableProperty.Icon, 40);

        setVisibleColumns(
            TableProperty.Icon,
            TableProperty.Path,
            TableProperty.Title,
            TableProperty.Type,
            TableProperty.Size,
            TableProperty.DateReleased,
            TableProperty.DateExpired,
            TableProperty.LastModified,
            TableProperty.BrokenLinks);

        setSelectable(true);
        setMultiSelect(true);
        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);
        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 1L;

            public void itemClick(ItemClickEvent event) {

                onItemClick(event, event.getItemId(), event.getPropertyId());
            }
        });

        setCellStyleGenerator(new CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                if (TableProperty.Path.equals(propertyId)) {
                    return " " + OpenCmsTheme.HOVER_COLUMN;
                }

                return null;
            }
        });

    }

    /**
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    List<I_CmsSimpleContextMenuEntry<Set<String>>> getMenuEntries() {

        if (m_menuEntries == null) {
            m_menuEntries = new ArrayList<I_CmsSimpleContextMenuEntry<Set<String>>>();
            m_menuEntries.add(new ExplorerEntry());
        }
        return m_menuEntries;
    }

    /**
     * Handles the table item clicks, including clicks on images inside of a table item.<p>
     *
     * @param event the click event
     * @param itemId of the clicked row
     * @param propertyId column id
     */
    void onItemClick(MouseEvents.ClickEvent event, Object itemId, Object propertyId) {

        if (!event.isCtrlKey() && !event.isShiftKey()) {

            setValue(null);
            select(itemId);

            // don't interfere with multi-selection using control key
            if (event.getButton().equals(MouseButton.RIGHT) || (propertyId == TableProperty.Icon)) {

                m_menu.setEntries(
                    getMenuEntries(),
                    Collections.singleton(((CmsResource)itemId).getStructureId().getStringValue()));
                m_menu.openForTable(event, itemId, propertyId, this);
            } else if (event.getButton().equals(MouseButton.LEFT) && TableProperty.Path.equals(propertyId)) {
                openExplorerForParent(
                    ((CmsResource)itemId).getRootPath(),
                    ((CmsResource)itemId).getStructureId().toString());
            }

        }
    }

    /**
     * Opens the explorer for given path and selected resource.<p>
     *
     * @param rootPath to be opened
     * @param uuid to be selected
     */
    void openExplorerForParent(String rootPath, String uuid) {

        String parentPath = CmsResource.getParentFolder(rootPath);

        CmsAppWorkplaceUi.get().getNavigator().navigateTo(
            CmsFileExplorerConfiguration.APP_ID
                + "/"
                + A_CmsUI.getCmsObject().getRequestContext().getCurrentProject().getUuid()
                + "!!"
                + A_CmsUI.getCmsObject().getRequestContext().getSiteRoot()
                + "!!"
                + parentPath.substring(A_CmsUI.getCmsObject().getRequestContext().getSiteRoot().length())
                + "!!"
                + uuid
                + "!!");
    }

    /**
     * Updates the table for given resource paths.<p>
     *
     * @param resourcePaths to be validated
     */
    void update(List<String> resourcePaths) {

        m_container.removeAllItems();

        getValidator(resourcePaths);
        List<CmsResource> broken = m_validator.getResourcesWithBrokenLinks();
        for (CmsResource res : broken) {
            Item item = m_container.addItem(res);

            item.getItemProperty(TableProperty.Path).setValue(
                A_CmsUI.getCmsObject().getRequestContext().getSitePath(res));

            item.getItemProperty(TableProperty.Type).setValue(
                OpenCms.getResourceManager().getResourceType(res).getTypeName());

            item.getItemProperty(TableProperty.Icon).setValue(getTypeImage(res));

            if (res.getDateExpired() < CmsResource.DATE_RELEASED_DEFAULT) {
                item.getItemProperty(TableProperty.DateExpired).setValue(new Date(res.getDateExpired()));
            }
            if (res.getDateReleased() > 0) {
                item.getItemProperty(TableProperty.DateReleased).setValue(new Date(res.getDateReleased()));
            }
            item.getItemProperty(TableProperty.LastModified).setValue(new Date(res.getDateLastModified()));

            if (res.getLength() > 0) {
                item.getItemProperty(TableProperty.Size).setValue(
                    CmsFileUtil.formatFilesize(
                        res.getLength(),
                        A_CmsUI.getCmsObject().getRequestContext().getLocale()));
            }
            try {
                item.getItemProperty(TableProperty.Title).setValue(
                    A_CmsUI.getCmsObject().readPropertyObject(
                        res,
                        CmsPropertyDefinition.PROPERTY_TITLE,
                        false).getValue());
            } catch (CmsException e) {
                //
            }
            List<CmsRelation> brokenLinks = m_validator.getBrokenLinksForResource(res.getRootPath());
            if (brokenLinks != null) {
                Iterator<CmsRelation> j = brokenLinks.iterator();
                while (j.hasNext()) {
                    item.getItemProperty(TableProperty.BrokenLinks).setValue(
                        getBrokenLinkString(j.next().getTargetPath()));
                }
            }
        }
    }

    /**
     * get string to show for broken link.<p>
     *
     * 1:1 the same like old workplace app<p>
     *
     * @param rootPath to get Broken links for.
     * @return broken link string
     */
    private String getBrokenLinkString(String rootPath) {

        String ret = "";

        CmsObject rootCms;
        try {
            rootCms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());

            rootCms.getRequestContext().setSiteRoot("");

            String siteRoot = OpenCms.getSiteManager().getSiteRoot(rootPath);
            String siteName = siteRoot;
            if (siteRoot != null) {
                try {

                    siteName = rootCms.readPropertyObject(
                        siteRoot,
                        CmsPropertyDefinition.PROPERTY_TITLE,
                        false).getValue(siteRoot);
                } catch (CmsException e) {
                    siteName = siteRoot;
                }
                ret = rootPath.substring(siteRoot.length());
            } else {
                siteName = "/";
            }
            if (!A_CmsUI.getCmsObject().getRequestContext().getSiteRoot().equals(siteRoot)) {
                ret = CmsVaadinUtils.getMessageText(
                    org.opencms.workplace.commons.Messages.GUI_DELETE_SITE_RELATION_2,
                    new Object[] {siteName, rootPath});
            }
        } catch (CmsException e1) {
            //
        }
        return ret;
    }

    /**
     * Returns image for type of resource.<p>
     *
     * @param resource to get icon for
     * @return icon
     */
    private Image getTypeImage(final CmsResource resource) {

        Image ret = new Image(
            String.valueOf(System.currentTimeMillis()),
            new ExternalResource(
                CmsResourceIcon.getSitemapResourceIcon(A_CmsUI.getCmsObject(), resource, IconMode.localeCompare)));
        ret.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -5277521979482801748L;

            public void click(ClickEvent event) {

                onItemClick(event, resource, TableProperty.Icon);

            }
        });
        return ret;
    }

    /**
     * Returns the link validator class.<p>
     *
     * @param resources to be validated.
     * @return the link validator class
     */
    private CmsInternalLinksValidator getValidator(List<String> resources) {

        m_validator = new CmsInternalLinksValidator(A_CmsUI.getCmsObject(), resources);

        return m_validator;
    }
}
