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

package org.opencms.ui.dialogs.permissions;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.user.CmsAccountsApp;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.commons.Messages;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.data.util.filter.Or;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Table for the ACE Entries.<p>
 */
public class CmsPermissionViewTable extends Table {

    /**
     * Column with FavIcon.<p>
     */
    class ViewColumn implements Table.ColumnGenerator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = -3772456970393398685L;

        /**
         * @see com.vaadin.ui.Table.ColumnGenerator#generateCell(com.vaadin.ui.Table, java.lang.Object, java.lang.Object)
         */
        public Object generateCell(Table source, Object itemId, Object columnId) {

            return m_dialog.buildPermissionEntryForm((CmsAccessControlEntry)itemId, m_editable, false, null);
        }
    }

    /**vaadin serial id. */
    private static final long serialVersionUID = -2759899760528588890L;

    /**View column.*/
    private static final String PROP_VIEW = "view";

    /**Name column (hidden, only used for filter). */
    private static final String PROP_NAME = "name";

    /**editable. */
    boolean m_editable;

    /**Data container. */
    IndexedContainer m_container;

    /**Calling dialog. */
    CmsPermissionDialog m_dialog;

    /**
     * public constructor.<p>
     *
     * @param cms CmsObject
     * @param entries to be shown
     * @param editable boolean
     * @param showRes with resources?
     * @param parents parents
     * @param dialog calling dialog
     */
    public CmsPermissionViewTable(
        CmsObject cms,
        List<CmsAccessControlEntry> entries,
        boolean editable,
        boolean showRes,
        Map<CmsUUID, String> parents,
        CmsPermissionDialog dialog) {

        m_editable = editable;
        m_dialog = dialog;
        setHeight("450px");
        setWidth("100%");
        setPageLength(4);
        setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
        m_container = new IndexedContainer();
        m_container.addContainerProperty(PROP_VIEW, VerticalLayout.class, null);
        m_container.addContainerProperty(PROP_NAME, String.class, "");
        setCellStyleGenerator(new CellStyleGenerator() {

            private static final long serialVersionUID = 3943163625035784161L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                return " " + OpenCmsTheme.TABLE_CONST_COLOR;
            }

        });

        Iterator<CmsAccessControlEntry> i = entries.iterator();
        boolean hasEntries = i.hasNext();

        if (hasEntries) {
            // list all entries
            while (i.hasNext()) {
                CmsAccessControlEntry curEntry = i.next();

                CmsPermissionView view = m_dialog.buildPermissionEntryForm(
                    curEntry,
                    m_editable,
                    false,
                    showRes ? curEntry.getResource() : null);
                Item item = m_container.addItem(view);
                item.getItemProperty(PROP_VIEW).setValue(
                    getLayoutFromEntry(cms, curEntry, view, showRes ? parents.get(curEntry.getResource()) : null));
                item.getItemProperty(PROP_NAME).setValue(view.getPrincipalName());
            }
        }

        setContainerDataSource(m_container);
        setVisibleColumns(PROP_VIEW);
    }

    /**
     * Filter the table according to string.<p>
     *
     * @param search string
     */
    public void filterTable(String search) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search)) {
            m_container.addContainerFilter(new Or(new SimpleStringFilter(PROP_NAME, search, true, false)));
        }
    }

    /**
     * Makes item for table.<p>
     *
     * @param cms CmsObject
     * @param entry ACE
     * @param view permission table
     * @param resPath parentResource (or null)
     * @return VerticalLayout
     */
    private VerticalLayout getLayoutFromEntry(
        CmsObject cms,
        CmsAccessControlEntry entry,
        final CmsPermissionView view,
        String resPath) {

        VerticalLayout res = new VerticalLayout();
        res.setSpacing(false);
        I_CmsPrincipal principal = null;
        try {
            principal = CmsPrincipal.readPrincipalIncludingHistory(cms, entry.getPrincipal());

        } catch (CmsException e) {
            principal = new CmsGroup(entry.getPrincipal(), null, "", "", 0);

        }
        if (principal != null) {
            CmsResourceInfo info = CmsAccountsApp.getPrincipalInfo(principal);
            if (view.isEditable()) {
                CssLayout cssl = new CssLayout();
                Button removeButton = new Button(FontOpenCms.TRASH_SMALL);
                removeButton.addStyleName("borderless o-toolbar-button o-resourceinfo-toolbar o-toolbar-icon-visible");
                removeButton.addClickListener(new ClickListener() {

                    private static final long serialVersionUID = -6112693137800596485L;

                    public void buttonClick(ClickEvent event) {

                        view.deletePermissionSet();

                    }

                });
                cssl.addComponent(removeButton);
                info.setButtonWidget(cssl);
            }
            res.addComponent(info);
            if (resPath != null) {
                Label resLabel = new Label(
                    CmsVaadinUtils.getMessageText(Messages.GUI_PERMISSION_INHERITED_FROM_1, resPath));
                resLabel.addStyleName("o-report");
                res.addComponent(resLabel);
            }
        }
        res.addComponent(view);
        return res;
    }
}
