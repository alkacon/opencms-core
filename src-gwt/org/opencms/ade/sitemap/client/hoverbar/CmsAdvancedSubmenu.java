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

package org.opencms.ade.sitemap.client.hoverbar;

import org.opencms.ade.sitemap.client.Messages;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Submenu for advanced options.<p>
 */
public class CmsAdvancedSubmenu extends A_CmsSitemapMenuEntry {

    /** The list of sub-entries. */
    private List<A_CmsSitemapMenuEntry> m_subEntries;

    /**
     * Creates a new instance.<p>
     *
     * @param hoverbar the hoverbar
     * @param subEntries the nested menu entries
     */
    public CmsAdvancedSubmenu(CmsSitemapHoverbar hoverbar, List<A_CmsSitemapMenuEntry> subEntries) {

        super(hoverbar);
        m_subEntries = subEntries;
        setActive(true);
        setLabel(Messages.get().key(Messages.GUI_HOVERBAR_ADVANCED_0));
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry#execute()
     */
    public void execute() {

        // TODO Auto-generated method stub

    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#getSubMenu()
     */
    @Override
    public List<I_CmsContextMenuEntry> getSubMenu() {

        return new ArrayList<I_CmsContextMenuEntry>(m_subEntries);
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#hasSubMenu()
     */
    @Override
    public boolean hasSubMenu() {

        return true;
    }

    /**
     * @see org.opencms.ade.sitemap.client.hoverbar.A_CmsSitemapMenuEntry#onShow()
     */
    @Override
    public void onShow() {

        for (I_CmsContextMenuEntry entry : m_subEntries) {
            if (entry.isVisible()) {
                setVisible(true);
                return;
            }
        }
        setVisible(false);
    }

}
