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

package org.opencms.ui.client;

import org.opencms.ade.publish.client.CmsPublishDialog;
import org.opencms.ade.publish.shared.CmsPublishData;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishService;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishServiceAsync;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.client.ui.contextmenu.CmsAbout;
import org.opencms.gwt.client.ui.contextmenu.CmsEditProperties;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler;
import org.opencms.gwt.client.ui.preferences.CmsUserSettingsDialog;
import org.opencms.ui.components.extensions.CmsGwtDialogExtension;
import org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc;
import org.opencms.ui.shared.components.I_CmsGwtDialogServerRpc;
import org.opencms.util.CmsUUID;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.ui.PopupPanel;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Client side part of CmsGwtDialogExtension.<p>
 */
@Connect(CmsGwtDialogExtension.class)
public class CmsGwtDialogExtensionConnector extends AbstractExtensionConnector implements I_CmsGwtDialogClientRpc {

    /**
     * Context menu handler.<p>
     */
    protected class ContextMenuHandler implements I_CmsContextMenuHandler {

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#ensureLockOnResource(org.opencms.util.CmsUUID)
         */
        public boolean ensureLockOnResource(CmsUUID structureId) {

            notImplemented();
            return false;
        }

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getContextMenuCommands()
         */
        public Map<String, I_CmsContextMenuCommand> getContextMenuCommands() {

            notImplemented();
            return null;
        }

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#getEditorHandler()
         */
        public I_CmsContentEditorHandler getEditorHandler() {

            notImplemented();
            return null;
        }

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#leavePage(java.lang.String)
         */
        public void leavePage(String targetUri) {

            notImplemented();
        }

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#refreshResource(org.opencms.util.CmsUUID)
         */
        public void refreshResource(CmsUUID structureId) {

            List<String> changed = Lists.newArrayList();
            changed.add("" + structureId);
            m_changed = changed;
            close(0);
        }

        /**
         * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler#unlockResource(org.opencms.util.CmsUUID)
         */
        public void unlockResource(CmsUUID structureId) {

            notImplemented();
        }

        /**
         * Throws an illegal state exception for not implemented methods.<p>
         */
        private void notImplemented() {

            throw new IllegalStateException("Not implemented");
        }
    }

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** List of structure ids of changed resources. */
    protected List<String> m_changed = Lists.newArrayList();

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc#editProperties(java.lang.String)
     */
    public void editProperties(String editStructureId) {

        CmsEditProperties.editProperties(new CmsUUID(editStructureId), new ContextMenuHandler(), new Runnable() {

            public void run() {

                // Handle cancel
                close(0);
            }
        });
    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc#openPublishDialog(java.lang.String)
     */
    public void openPublishDialog(String serializedPublishData) {

        try {
            I_CmsPublishServiceAsync publishService = GWT.create(I_CmsPublishService.class);
            CmsPublishData initData = (CmsPublishData)CmsRpcPrefetcher.getSerializedObjectFromString(
                publishService,
                serializedPublishData);
            Runnable menuRefreshAction = new Runnable() {

                public void run() {

                    Window.Location.reload();
                }
            };

            CloseHandler<PopupPanel> closeHandler = new CloseHandler<PopupPanel>() {

                public void onClose(CloseEvent<PopupPanel> event) {

                    CmsPublishDialog dialog = (CmsPublishDialog)(event.getTarget());
                    long delay = 0;
                    if (dialog.hasFailed() || dialog.hasSucceeded()) {
                        m_changed = Arrays.asList("" + CmsUUID.getNullUUID());
                        delay = 700;
                    } else {
                        m_changed = Lists.newArrayList();
                    }
                    close(delay);
                }
            };

            CmsPublishDialog.showPublishDialog(initData, closeHandler, menuRefreshAction, null);
        } catch (SerializationException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc#showAbout()
     */
    public void showAbout() {

        CmsAbout.showAbout();
    }

    /**
     * @see org.opencms.ui.shared.components.I_CmsGwtDialogClientRpc#showUserPreferences()
     */
    public void showUserPreferences() {

        CmsUserSettingsDialog.loadAndShow(new Runnable() {

            public void run() {

                // Handle cancel
                close(0);
            }
        });
    }

    /**
     * Disposes of the extension on the server side and notifies the server of which resources have been changed.<p>
     *
     * @param delayMillis the time to wait on the server before refreshing the view
     */
    protected void close(long delayMillis) {

        getRpcProxy(I_CmsGwtDialogServerRpc.class).onClose(m_changed, delayMillis);
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        registerRpc(I_CmsGwtDialogClientRpc.class, this);
    }

}
