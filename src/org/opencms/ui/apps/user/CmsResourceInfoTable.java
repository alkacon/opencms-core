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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.ui.Table;

/**
 * Table to show resources with CmsResourceInfo elements.<p>
 */
public class CmsResourceInfoTable extends Table {

    /**vaadin serial id. */
    private static final long serialVersionUID = 5999721724863889097L;

    /**Table column. */
    private static String PROP_ELEMENT = "element";

    /**Indexed container. */
    IndexedContainer m_container;

    /**
     * Public constructor.<p>
     *
     * @param cms CmsObject
     * @param userIDs user id
     * @param groupIDs group id
     */
    public CmsResourceInfoTable(CmsObject cms, Set<CmsUUID> userIDs, Set<CmsUUID> groupIDs) {
        List<CmsUser> user = new ArrayList<CmsUser>();
        try {
            for (CmsUUID group : groupIDs) {
                user.addAll(cms.getUsersOfGroup(cms.readGroup(group).getName()));
            }
            Set<CmsUUID> principalIDs = new HashSet<CmsUUID>();
            principalIDs.addAll(userIDs);
            principalIDs.addAll(groupIDs);
            Set<CmsResource> resources = new HashSet<CmsResource>();
            for (CmsUUID id : principalIDs) {
                resources.addAll(cms.getResourcesForPrincipal(id, null, false));
            }

            init(resources, user);
        } catch (CmsException e) {
            //
        }
    }

    /**
     * public constructor.<p>
     *
     * @param resources to be shown
     * @param user list of user
     */
    public CmsResourceInfoTable(Set<CmsResource> resources, List<CmsUser> user) {
        addStyleName("o-no-padding");
        m_container = new IndexedContainer();
        m_container.addContainerProperty(PROP_ELEMENT, CmsResourceInfo.class, null);

        for (CmsResource res : resources) {
            Item item = m_container.addItem(res);
            item.getItemProperty(PROP_ELEMENT).setValue(new CmsResourceInfo(res));
        }
        if (user != null) {
            for (CmsUser us : user) {
                Item item = m_container.addItem(us);
                item.getItemProperty(PROP_ELEMENT).setValue(
                    new CmsResourceInfo(
                        us.getSimpleName(),
                        us.getDescription(),
                        new CmsCssIcon(OpenCmsTheme.ICON_USER)));
            }
        }
        setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
        setContainerDataSource(m_container);
        setVisibleColumns(PROP_ELEMENT);

    }

    /**
     * Initializes the table.<p>
     *
     * @param resources List of resources
     * @param user List user
     */
    private void init(Set<CmsResource> resources, List<CmsUser> user) {

        addStyleName("o-no-padding");
        m_container = new IndexedContainer();
        m_container.addContainerProperty(PROP_ELEMENT, CmsResourceInfo.class, null);

        for (CmsResource res : resources) {
            Item item = m_container.addItem(res);
            if (item != null) { //Item is null if resource is dependency of multiple groups/user to delete
                item.getItemProperty(PROP_ELEMENT).setValue(new CmsResourceInfo(res));
            }
        }
        if (user != null) {
            for (CmsUser us : user) {
                Item item = m_container.addItem(us);
                if (item != null) { //Item is null if User is dependency of multiple groups to delete
                    item.getItemProperty(PROP_ELEMENT).setValue(
                        new CmsResourceInfo(
                            us.getSimpleName(),
                            us.getDescription(),
                            new CmsCssIcon(OpenCmsTheme.ICON_USER)));
                }
            }
        }
        setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
        setContainerDataSource(m_container);
        setVisibleColumns(PROP_ELEMENT);

    }
}
