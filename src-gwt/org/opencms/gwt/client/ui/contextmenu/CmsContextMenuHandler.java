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

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsGwtConstants;
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

    /** the content editor handler. */
    private I_CmsContentEditorHandler m_editorHandler;

    /**
     * Constructor.<p>
     */
    public CmsContextMenuHandler() {

    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#ensureLockOnResource(org.opencms.util.CmsUUID)
     */
    public boolean ensureLockOnResource(CmsUUID structureId) {

        return CmsCoreProvider.get().lock(structureId);
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
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getContextType()
     */
    public String getContextType() {

        return CmsGwtConstants.CONTEXT_TYPE_FILE_TABLE;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getEditorHandler()
     */
    public I_CmsContentEditorHandler getEditorHandler() {

        if (m_editorHandler == null) {
            m_editorHandler = new I_CmsContentEditorHandler() {

                public void onClose(String sitePath, CmsUUID structureId, boolean isNew) {

                    // do nothing
                }
            };
        }
        return m_editorHandler;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#leavePage(java.lang.String)
     */
    public void leavePage(String targetUri) {

        // not supported within galleries
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
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#onSiteOrProjectChange(java.lang.String, java.lang.String)
     */
    public void onSiteOrProjectChange(String sitePath, String serverLink) {

        leavePage(serverLink);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#refreshResource(org.opencms.util.CmsUUID)
     */
    public void refreshResource(CmsUUID structureId) {

        // do nothing
    }

    /**
     * Sets the editor handler.<p>
     *
     * @param editorHandler the editor handler
     */
    public void setEditorHandler(I_CmsContentEditorHandler editorHandler) {

        m_editorHandler = editorHandler;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#unlockResource(org.opencms.util.CmsUUID)
     */
    public void unlockResource(CmsUUID structureId) {

        CmsCoreProvider.get().unlock(structureId);
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
            I_CmsContextMenuEntry entry = transformSingleEntry(bean, structureId);
            if (entry != null) {
                menuEntries.add(entry);
            }
        }
        return menuEntries;
    }

    /**
     * Creates a single context menu entry from a context menu entry bean.<p>
     *
     * @param bean the menu entry bean
     * @param structureId the structure id
     *
     * @return the context menu for the given entry
     */
    protected I_CmsContextMenuEntry transformSingleEntry(CmsContextMenuEntryBean bean, CmsUUID structureId) {

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
        return entry;
    }
}
