/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client.toolbar;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.I_CmsDisableable;
import org.opencms.gwt.client.ui.A_CmsToolbarHandler;
import org.opencms.gwt.client.ui.I_CmsToolbarButton;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommandInitializer;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

/**
 * The toolbar handler used for the sitemap toolbar context menu.<p>
 */
public class CmsSitemapToolbarHandler extends A_CmsToolbarHandler {

    /** The currently active button. */
    private I_CmsToolbarButton m_activeButton;

    /** The available context menu commands. */
    private Map<String, I_CmsContextMenuCommand> m_contextMenuCommands;

    /** The context menu entries. */
    private List<I_CmsContextMenuEntry> m_contextMenuEntries;

    /** The content editor handler. */
    private I_CmsContentEditorHandler m_editorHandler;

    /**
     * Constructor.<p>
     *
     * @param menuBeans the context menu entry beans
     */
    public CmsSitemapToolbarHandler(List<CmsContextMenuEntryBean> menuBeans) {

        m_contextMenuEntries = transformEntries(menuBeans, null);
        m_editorHandler = new I_CmsContentEditorHandler() {

            public void onClose(String sitePath, CmsUUID structureId, boolean isNew) {

                CmsSitemapView.getInstance().getController().updateEntry(sitePath);
            }
        };
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarHandler#activateSelection()
     */
    public void activateSelection() {

        // do nothing
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarHandler#deactivateCurrentButton()
     */
    public void deactivateCurrentButton() {

        if (m_activeButton != null) {
            m_activeButton.setActive(false);
            m_activeButton = null;
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#ensureLockOnResource(org.opencms.util.CmsUUID, org.opencms.gwt.client.util.I_CmsSimpleCallback)
     */
    public void ensureLockOnResource(CmsUUID structureId, I_CmsSimpleCallback<Boolean> callback) {

        CmsCoreProvider.get().lock(structureId, callback);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarHandler#getActiveButton()
     */
    public I_CmsToolbarButton getActiveButton() {

        return m_activeButton;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getContextMenuCommands()
     */
    public Map<String, I_CmsContextMenuCommand> getContextMenuCommands() {

        if (m_contextMenuCommands == null) {
            I_CmsContextMenuCommandInitializer initializer = GWT.create(I_CmsContextMenuCommandInitializer.class);
            m_contextMenuCommands = initializer.initCommands();
            List<String> toRemove = new ArrayList<String>();
            for (Map.Entry<String, I_CmsContextMenuCommand> entry : m_contextMenuCommands.entrySet()) {
                I_CmsContextMenuCommand command = entry.getValue();
                if ((command != null)
                    && (command instanceof I_CmsDisableable)
                    && ((I_CmsDisableable)command).isDisabled()) {
                    toRemove.add(entry.getKey());
                }
            }
            for (String removeKey : toRemove) {
                m_contextMenuCommands.remove(removeKey);
            }
        }
        return m_contextMenuCommands;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getContextType()
     */
    public String getContextType() {

        return CmsGwtConstants.CONTEXT_TYPE_SITEMAP_TOOLBAR;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getEditorHandler()
     */
    public I_CmsContentEditorHandler getEditorHandler() {

        return m_editorHandler;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#leavePage(java.lang.String)
     */
    public void leavePage(String targetUri) {

        Window.Location.assign(targetUri);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarHandler#loadContextMenu(org.opencms.util.CmsUUID, org.opencms.gwt.shared.CmsCoreData.AdeContext)
     */
    public void loadContextMenu(CmsUUID structureId, AdeContext context) {

        CmsSitemapView.getInstance().getToolbar().getContextMenuButton().showMenu(m_contextMenuEntries);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#onSiteOrProjectChange(java.lang.String, java.lang.String)
     */
    public void onSiteOrProjectChange(String sitePath, String serverLink) {

        CmsSitemapView.getInstance().getController().openSiteMap(sitePath, true);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#refreshResource(org.opencms.util.CmsUUID)
     */
    public void refreshResource(CmsUUID structureId) {

        Window.Location.reload();
    }

    /**
     * Sets the currently active tool-bar button.<p>
     *
     * @param button the button
     */
    public void setActiveButton(I_CmsToolbarButton button) {

        m_activeButton = button;
    }

    /**
     * @see org.opencms.gwt.client.ui.A_CmsToolbarHandler#transformEntries(java.util.List, org.opencms.util.CmsUUID)
     */
    @Override
    public List<I_CmsContextMenuEntry> transformEntries(List<CmsContextMenuEntryBean> menuBeans, CmsUUID structureId) {

        List<I_CmsContextMenuEntry> result = super.transformEntries(menuBeans, structureId);
        return result;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#unlockResource(org.opencms.util.CmsUUID)
     */
    public void unlockResource(CmsUUID structureId) {

        CmsCoreProvider.get().unlock(structureId);
    }

}
