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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuEntry;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class which implements the common part of all toolbar handler functionality.<p>
 *
 * @since 8.0.0
 */
public abstract class A_CmsToolbarHandler implements I_CmsToolbarHandler {

    /**
     * Transforms a list of context menu entry beans to a list of context menu entries.<p>
     *
     * @param menuBeans the list of context menu entry beans
     * @param structureId the id of the resource for which to transform the context menu entries
     *
     * @return a list of context menu entries
     */
    public List<I_CmsContextMenuEntry> transformEntries(
        List<CmsContextMenuEntryBean> menuBeans,
        final CmsUUID structureId) {

        List<I_CmsContextMenuEntry> menuEntries = new ArrayList<I_CmsContextMenuEntry>();
        for (CmsContextMenuEntryBean bean : menuBeans) {
            I_CmsContextMenuEntry entry = transformSingleEntry(structureId, bean);
            if (entry != null) {
                menuEntries.add(entry);
            }
        }
        return menuEntries;
    }

    /**
     * Creates a single context menu entry from a context menu entry bean.<p>
     *
     * @param structureId the structure id of the resource
     * @param bean the context menu entry bean
     *
     * @return the created context menu entry
     */
    protected I_CmsContextMenuEntry transformSingleEntry(final CmsUUID structureId, CmsContextMenuEntryBean bean) {

        String name = bean.getName();
        I_CmsContextMenuCommand command = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
            command = getContextMenuCommands().get(name);
        }
        if ((command == null) && !bean.hasSubMenu()) {
            return null;
        }
        CmsContextMenuEntry entry = new CmsContextMenuEntry(this, structureId, command);
        entry.setBean(bean);
        if (bean.hasSubMenu()) {
            entry.setSubMenu(transformEntries(bean.getSubMenu(), structureId));
            if (entry.getSubMenu().isEmpty()) {
                return null;
            }
        }
        return entry;
    }

}
