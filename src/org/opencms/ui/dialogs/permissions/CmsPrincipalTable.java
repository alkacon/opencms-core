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

import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsStringUtil;

import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.data.util.filter.Or;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.ui.Table;

/**
 * Table to selecet principals used in the CmsPrincipalSelect vaadin element.<p>
 */
public class CmsPrincipalTable extends Table {

    /**vaadin serial id. */
    private static final long serialVersionUID = -6291109098461446195L;

    /**Caption property. */
    private static String PROP_CAPTION;

    /**Icon property. */
    private static String PROP_ICON;

    /**Ou property. */
    private static String PROP_OU;

    /**Description property. */
    private static String PROP_DESCRIPTION;

    /**Indexed Contaier. */
    private IndexedContainer m_container;

    /**
     * public constructor.<p>
     *
     * @param dialog which holds the table
     * @param container indexedcontainer
     * @param iconProp icon property
     * @param captionProp caption property
     * @param descProp description property
     * @param ouProp Ou
     */
    public CmsPrincipalTable(
        final CmsPrincipalSelectDialog dialog,
        IndexedContainer container,
        String iconProp,
        String captionProp,
        String descProp,
        String ouProp) {
        setHeight("500px");
        m_container = container;
        PROP_CAPTION = captionProp;
        PROP_DESCRIPTION = descProp;
        PROP_ICON = iconProp;
        PROP_OU = ouProp;
        setContainerDataSource(m_container);
        setItemIconPropertyId(iconProp);
        setColumnWidth(null, 40);
        setColumnWidth(ouProp, 300);
        setSelectable(true);

        setWidth("100%");
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        setVisibleColumns(PROP_CAPTION, PROP_DESCRIPTION, PROP_OU);
        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = -4593704069277393664L;

            public void itemClick(ItemClickEvent event) {

                dialog.select((I_CmsPrincipal)event.getItemId());
            }

        });
    }

    /**
     * Updates container.<p>
     *
     * @param data to be updated
     */
    public void updateContainer(IndexedContainer data) {

        m_container.removeAllItems();
        m_container = data;
        setContainerDataSource(m_container);
        setItemIconPropertyId(PROP_ICON);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);

        setVisibleColumns(PROP_CAPTION, PROP_DESCRIPTION, PROP_OU);
        setColumnWidth(null, 40);
        this.refreshRowCache();

    }

    /**
     * Filter for table.<p>
     * @param search text
     */
    protected void filterTable(String search) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search)) {
            m_container.addContainerFilter(
                new Or(
                    new SimpleStringFilter(PROP_CAPTION, search, true, false),
                    new SimpleStringFilter(PROP_DESCRIPTION, search, true, false)));
        }
    }
}
