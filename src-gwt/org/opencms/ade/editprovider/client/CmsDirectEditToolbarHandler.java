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

package org.opencms.ade.editprovider.client;

import org.opencms.ade.publish.client.CmsPublishDialog;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.A_CmsToolbarHandler;
import org.opencms.gwt.client.ui.CmsToolbarContextButton;
import org.opencms.gwt.client.ui.I_CmsToolbarButton;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommandInitializer;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuEntry;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsCoreData.AdeContext;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * A toolbar handler for the Toolbar direct edit provider.<p>
 *
 * @since 8.0.0
 */
public class CmsDirectEditToolbarHandler extends A_CmsToolbarHandler {

    /** The currently active button. */
    private I_CmsToolbarButton m_activeButton;

    /** The context menu button. */
    private CmsToolbarContextButton m_contextButton;

    /** The context menu commands. */
    private Map<String, I_CmsContextMenuCommand> m_contextMenuCommands;

    /** The editor handler. */
    private I_CmsContentEditorHandler m_editorHandler;

    /** The entry point. */
    private CmsDirectEditEntryPoint m_entryPoint;

    /**
     * Constructor.<p>
     *
     * @param entryPoint the entry point
     */
    public CmsDirectEditToolbarHandler(CmsDirectEditEntryPoint entryPoint) {

        m_entryPoint = entryPoint;
        m_editorHandler = new I_CmsContentEditorHandler() {

            /**
             * @see org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler#onClose(java.lang.String, org.opencms.util.CmsUUID, boolean)
             */
            public void onClose(String sitePath, CmsUUID structureId, boolean isNew) {

                Window.Location.reload();
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
     * De-activates the current button.<p>
     */
    public void deactivateCurrentButton() {

        if (m_activeButton != null) {
            m_activeButton.setActive(false);
            m_activeButton = null;
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#ensureLockOnResource(org.opencms.util.CmsUUID)
     */
    public boolean ensureLockOnResource(CmsUUID structureId) {

        return CmsCoreProvider.get().lock(structureId);
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
        }
        return m_contextMenuCommands;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getContextType()
     */
    public String getContextType() {

        return CmsGwtConstants.CONTEXT_TYPE_APP_TOOLBAR;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getEditorHandler()
     */
    public I_CmsContentEditorHandler getEditorHandler() {

        return m_editorHandler;
    }

    /**
     * Inserts the context menu.<p>
     *
     * @param menuBeans the menu beans from the server
     * @param structureId the structure id of the resource at which the workplace should be opened
     */
    public void insertContextMenu(List<CmsContextMenuEntryBean> menuBeans, CmsUUID structureId) {

        List<I_CmsContextMenuEntry> menuEntries = transformEntries(menuBeans, structureId);
        m_contextButton.showMenu(menuEntries);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#leavePage(java.lang.String)
     */
    public void leavePage(String targetUri) {

        Window.Location.replace(targetUri);
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsToolbarHandler#loadContextMenu(org.opencms.util.CmsUUID, org.opencms.gwt.shared.CmsCoreData.AdeContext)
     */
    public void loadContextMenu(final CmsUUID structureId, final AdeContext context) {

        /** The RPC menu action for the container page dialog. */
        CmsRpcAction<List<CmsContextMenuEntryBean>> menuAction = new CmsRpcAction<List<CmsContextMenuEntryBean>>() {

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
            */
            @Override
            public void execute() {

                CmsCoreProvider.getService().getContextMenuEntries(structureId, context, this);
            }

            /**
            * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
            */
            @Override
            public void onResponse(List<CmsContextMenuEntryBean> menuBeans) {

                insertContextMenu(menuBeans, structureId);
            }
        };
        menuAction.execute();

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
     * Sets the context menu button.<p>
     *
     * @param button the context menu button
     */
    public void setContextMenuButton(CmsToolbarContextButton button) {

        m_contextButton = button;
    }

    /**
     * Opens the publish dialog.<p>
     */
    public void showPublishDialog() {

        CmsPublishDialog.showPublishDialog(new HashMap<String, String>(), new CloseHandler<PopupPanel>() {

            /**
             * @see com.google.gwt.event.logical.shared.CloseHandler#onClose(com.google.gwt.event.logical.shared.CloseEvent)
             */
            public void onClose(CloseEvent<PopupPanel> event) {

                deactivateCurrentButton();

            }
        }, new Runnable() {

            public void run() {

                showPublishDialog();
            }

        }, null);
    }

    /**
     * Toggles the visibility of the toolbar.<p>
     *
     * @param show <code>true</code> to show the toolbar
     */
    public void showToolbar(boolean show) {

        m_entryPoint.toggleToolbar(show);
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#unlockResource(org.opencms.util.CmsUUID)
     */
    public void unlockResource(CmsUUID structureId) {

        CmsCoreProvider.get().unlock(structureId);
    }

}
