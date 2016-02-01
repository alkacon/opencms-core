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

import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.client.ui.contextmenu.CmsEditProperties;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand;
import org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler;
import org.opencms.ui.components.extensions.CmsPropertyDialogExtension;
import org.opencms.ui.shared.rpc.I_CmsPropertyClientRpc;
import org.opencms.ui.shared.rpc.I_CmsPropertyServerRpc;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Connector for the  property dialog extension.
 */
@Connect(CmsPropertyDialogExtension.class)
public class CmsPropertyDialogExtensionConnector extends AbstractExtensionConnector
implements I_CmsPropertyClientRpc, CmsEditProperties.I_MultiFileNavigation {

    /**
     * Context menu handler.<p>
     */
    public class ContextMenuHandler implements I_CmsContextMenuHandler {

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

    /** Changed ids. */
    protected List<String> m_changed = Lists.newArrayList();

    /** Stored callback. */
    private AsyncCallback<CmsUUID> m_currentCallback;

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsPropertyClientRpc#editProperties(java.lang.String, boolean, boolean)
     */
    public void editProperties(String editStructureId, boolean editName, boolean disablePrevNext) {

        CmsEditProperties.PropertyEditingContext context = new CmsEditProperties.PropertyEditingContext();
        if (!disablePrevNext) {
            context.setMultiFileNavigation(this);
        }
        CmsEditProperties.editPropertiesWithFileNavigation(
            new CmsUUID(editStructureId),
            new ContextMenuHandler(),
            editName,
            new Runnable() {

                public void run() {

                    // nop
                }
            },
            false,
            context,
            null);

    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.CmsEditProperties.I_MultiFileNavigation#requestNextFile(int, com.google.gwt.user.client.rpc.AsyncCallback)
     */
    public void requestNextFile(int offset, AsyncCallback<CmsUUID> callback) {

        m_currentCallback = callback;
        getRpcProxy(I_CmsPropertyServerRpc.class).requestNextFile(offset);

    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsPropertyClientRpc#sendNextId(java.lang.String)
     */
    public void sendNextId(String id) {

        if (m_currentCallback != null) {
            m_currentCallback.onSuccess(new CmsUUID(id));
            m_currentCallback = null;
        }
    }

    /**
     * Disposes of the extension on the server side and notifies the server of which resources have been changed.<p>
     *
     * @param delayMillis the time to wait on the server before refreshing the view
     */
    protected void close(long delayMillis) {

        getRpcProxy(I_CmsPropertyServerRpc.class).onClose(delayMillis);
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        registerRpc(I_CmsPropertyClientRpc.class, this);
    }

}
