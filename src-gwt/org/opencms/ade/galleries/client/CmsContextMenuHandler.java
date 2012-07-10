/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.galleries.client;

import org.opencms.ade.galleries.client.ui.CmsContextMenuButton;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuEntry;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommandInitializer;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;

/**
 * The context menu handler for the search result tab.<p>
 */
public class CmsContextMenuHandler implements I_CmsContextMenuHandler {

    /** The available context menu commands. */
    private static Map<String, I_CmsContextMenuCommand> m_contextMenuCommands;

    /** The result tab handler. */
    CmsResultsTabHandler m_resultTabHandler;

    /**
     * Constructor.<p>
     * 
     * @param resultTabHandler the result tab handler
     */
    public CmsContextMenuHandler(CmsResultsTabHandler resultTabHandler) {

        m_resultTabHandler = resultTabHandler;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#ensureLockOnResource(org.opencms.util.CmsUUID)
     */
    public boolean ensureLockOnResource(CmsUUID structureId) {

        return CmsCoreProvider.get().lock(structureId);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#loadContextMenu(org.opencms.util.CmsUUID, org.opencms.gwt.shared.CmsCoreData.AdeContext)
     */
    public void loadContextMenu(final CmsUUID structureId, final AdeContext context) {

        throw new UnsupportedOperationException(
            "Not supported. Use 'loadContextMenu(final CmsUUID structureId, final AdeContext context, final CmsContextMenuButton menuButton)' instead.");
    }

    /**
     * Loads the context menu.<p>
     * 
     * @param structureId the resource structure id
     * @param context the context
     * @param menuButton the menu button
     */
    public void loadContextMenu(
        final CmsUUID structureId,
        final AdeContext context,
        final CmsContextMenuButton menuButton) {

        CmsRpcAction<List<CmsContextMenuEntryBean>> action = new CmsRpcAction<List<CmsContextMenuEntryBean>>() {

            @Override
            public void execute() {

                CmsCoreProvider.getService().getContextMenuEntries(structureId, context, this);
            }

            @Override
            protected void onResponse(List<CmsContextMenuEntryBean> result) {

                menuButton.showMenu(transformEntries(result, structureId));
            }
        };
        action.execute();
    }

    /**
     * Transforms a list of context menu entry beans to a list of context menu entries.<p>
     * 
     * @param menuBeans the list of context menu entry beans
     * @param structureId the id of the resource for which to transform the context menu entries 
     * 
     * @return a list of context menu entries 
     */
    protected List<I_CmsContextMenuEntry> transformEntries(
        List<CmsContextMenuEntryBean> menuBeans,
        final CmsUUID structureId) {

        List<I_CmsContextMenuEntry> menuEntries = new ArrayList<I_CmsContextMenuEntry>();
        for (CmsContextMenuEntryBean bean : menuBeans) {
            String name = bean.getName();
            I_CmsContextMenuCommand command = null;
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(name)) {
                command = getContextMenuCommands().get(name);
            }
            CmsContextMenuEntry entry = new CmsContextMenuEntry(this, structureId, command);
            entry.setBean(bean);
            if (bean.hasSubMenu()) {
                entry.setSubMenu(transformEntries(bean.getSubMenu(), structureId));
            }
            menuEntries.add(entry);
        }
        return menuEntries;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getContextMenuCommands()
     */
    public Map<String, I_CmsContextMenuCommand> getContextMenuCommands() {

        if (m_contextMenuCommands == null) {
            I_CmsContextMenuCommandInitializer initializer = GWT.create(I_CmsContextMenuCommandInitializer.class);
            m_contextMenuCommands = initializer.initCommands();
        }
        return m_contextMenuCommands;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#refreshResource(org.opencms.util.CmsUUID)
     */
    public void refreshResource(CmsUUID structureId) {

        m_resultTabHandler.updateIndex();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#unlockResource(org.opencms.util.CmsUUID)
     */
    public void unlockResource(CmsUUID structureId) {

        CmsCoreProvider.get().unlock(structureId);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#leavePage(java.lang.String)
     */
    public void leavePage(String targetUri) {

        // not supported within galleries
    }
}
