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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.util.CmsUUID;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.Window;

/**
 * Context menu handler to be used within overlay dialogs.<p>
 */
public class CmsDialogContextMenuHandler extends CmsContextMenuHandler {

    /** Set of context menu actions which we do not want to appear in the context menu for the relation source items. */
    protected static Set<String> m_filteredActions;

    static {
        m_filteredActions = new HashSet<String>();
        m_filteredActions.add(CmsGwtConstants.TEMPLATECONTEXT_MENU_PLACEHOLDER);
        m_filteredActions.add(CmsGwtConstants.ACTION_EDITSMALLELEMENTS);
        m_filteredActions.add(CmsGwtConstants.ACTION_SELECTELEMENTVIEW);
        m_filteredActions.add(CmsGwtConstants.ACTION_SHOWLOCALE);
        m_filteredActions.add(CmsGwtConstants.ACTION_VIEW_ONLINE);

        for (Class<?> cls : new Class[] {
            CmsEditUserSettings.class,
            CmsAbout.class,
            CmsOpenSeoDialog.class,
            CmsLogout.class}) {
            m_filteredActions.add(cls.getName());
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.CmsContextMenuHandler#refreshResource(org.opencms.util.CmsUUID)
     */
    @Override
    public void refreshResource(CmsUUID structureId) {

        Window.Location.reload();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.CmsContextMenuHandler#transformSingleEntry(org.opencms.gwt.shared.CmsContextMenuEntryBean, org.opencms.util.CmsUUID)
     */
    @Override
    protected I_CmsContextMenuEntry transformSingleEntry(CmsContextMenuEntryBean entryBean, CmsUUID structureId) {

        if (m_filteredActions.contains(entryBean.getName())) {
            return null;
        } else {
            return super.transformSingleEntry(entryBean, structureId);
        }
    }

}