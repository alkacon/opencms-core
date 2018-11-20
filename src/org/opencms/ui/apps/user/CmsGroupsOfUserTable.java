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

package org.opencms.ui.apps.user;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.ui.apps.user.CmsGroupTable.TableProperty;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.ui.Table;

/**
 * Table showing all Groups of a user.<p>
 */
public class CmsGroupsOfUserTable extends Table {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The app instance. */
    private CmsAccountsApp m_app;

    /**Data container. */
    private IndexedContainer m_container;

    /**
     * Init method.<p>
     *
     * @param app the app instance
     * @param cms CmsObject
     * @param user CmsUser
     * @param groups list of groups
     */
    public void init(CmsAccountsApp app, CmsObject cms, CmsUser user, List<CmsGroup> groups) {

        m_app = app;
        if (m_container == null) {
            m_container = new IndexedContainer();

            for (TableProperty prop : TableProperty.values()) {
                m_container.addContainerProperty(prop, prop.getType(), prop.getDefault());
                setColumnHeader(prop, prop.getName());
            }
            m_app.addGroupContainerProperties(m_container);
            setContainerDataSource(m_container);
            setItemIconPropertyId(TableProperty.Icon);
            setRowHeaderMode(RowHeaderMode.ICON_ONLY);

            setColumnWidth(null, 40);
            setSelectable(false);
            setMultiSelect(false);
            setVisibleColumns(TableProperty.Name, TableProperty.OU);

        }
        m_container.removeAllItems();

        for (CmsGroup group : groups) {
            Item item = m_container.addItem(group);
            m_app.fillGroupItem(item, group, new ArrayList<CmsGroup>());
        }

    }

}
