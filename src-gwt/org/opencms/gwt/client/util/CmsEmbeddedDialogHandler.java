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

package org.opencms.gwt.client.util;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsIFrame;
import org.opencms.gwt.client.ui.contextmenu.I_CmsActionHandler;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Handler for embedded VAADIN dialogs.<p>
 */
public class CmsEmbeddedDialogHandler {

    /** The iframe element. */
    private CmsIFrame m_frame;

    /** The context menu handler. */
    private I_CmsActionHandler m_handler;

    /** The on close command. */
    private Command m_onCloseCommand;

    /**
     * Constructor.<p>
     */
    public CmsEmbeddedDialogHandler() {
        // nothing to do
    }

    /**
     * Constructor.<p>
     *
     * @param handler the context handler
     */
    public CmsEmbeddedDialogHandler(I_CmsActionHandler handler) {
        m_handler = handler;
    }

    /**
     * Called on dialog close.<p>
     *
     * @param resources the resource ids to update as a ';' separated string.<p>
     */
    public void finish(String resources) {

        if (m_frame != null) {
            m_frame.removeFromParent();
            m_frame = null;
        }
        if (m_handler != null) {
            List<CmsUUID> resourceIds = parseResources(resources);
            if (!resourceIds.isEmpty()) {
                m_handler.refreshResource(resourceIds.get(0));
            }
        }
        if (m_onCloseCommand != null) {
            m_onCloseCommand.execute();
        }
    }

    /**
     * Returns if the dialog iframe is attached.<p>
     *
     * @return <code>true</code> if the dialog iframe is attached
     */
    public boolean hasDialogFrame() {

        return m_frame != null;
    }

    /**
     * Navigates to the given URI.<p>
     *
     * @param targetUri the target URI
     */
    public void leavePage(String targetUri) {

        if (m_frame != null) {
            m_frame.removeFromParent();
            m_frame = null;
        }
        if (m_handler != null) {
            m_handler.leavePage(targetUri);
        } else {
            Window.Location.assign(targetUri);
        }
    }

    /**
     * Called when site and or project have been changed.<p>
     *
     * @param sitePath the site path to the resource to display
     * @param serverLink the server link to the resource to display
     */
    public void onSiteOrProjectChange(String sitePath, String serverLink) {

        if (m_frame != null) {
            m_frame.removeFromParent();
            m_frame = null;
        }
        if (m_handler != null) {
            m_handler.onSiteOrProjectChange(sitePath, serverLink);
        } else {
            Window.Location.assign(serverLink);
        }
    }

    /**
     * Opens the dialog with the given id.<p>
     *
     * @param dialogId the dialog id
     * @param contextType the context type, used to check the action visibility
     * @param resources the resource to handle
     */
    public void openDialog(String dialogId, String contextType, List<CmsUUID> resources) {

        openDialog(dialogId, contextType, resources, null);
    }

    /**
     * Opens the dialog with the given id.<p>
     *
     * @param dialogId the dialog id
     * @param contextType the context type, used to check the action visibility
     * @param resources the resource to handle
     * @param rawParams additional set of parameters to append to the query string (will not be escaped, therefore 'raw')
     */

    public void openDialog(
        String dialogId,
        String contextType,
        List<CmsUUID> resources,
        Map<String, String> rawParams) {

        String resourceIds = "";
        if (resources != null) {
            for (CmsUUID id : resources) {
                resourceIds += id.toString() + ";";
            }
        }
        String url = CmsCoreProvider.get().getEmbeddedDialogsUrl()
            + dialogId
            + "?resources="
            + resourceIds
            + "&contextType="
            + contextType;

        if ((rawParams != null) && !rawParams.isEmpty()) {
            List<String> params = new ArrayList<String>();
            for (Map.Entry<String, String> entry : rawParams.entrySet()) {
                params.add(entry.getKey() + "=" + entry.getValue());
            }
            url = url + "&" + CmsStringUtil.listAsString(params, "&");
        }
        m_frame = new CmsIFrame("embeddedDialogFrame", url);
        m_frame.setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().embeddedDialogFrame());
        RootPanel.get().add(m_frame);
        initIFrame();
    }

    /**
     * Reloads the current page.<p>
     */
    public void reload() {

        if (m_handler != null) {
            String uri = Window.Location.getHref();
            m_handler.leavePage(uri);
        } else {
            Window.Location.reload();
        }
    }

    /**
     * Sets the on close command.<p>
     *
     * @param onCloseCommand the on close command
     */
    public void setOnCloseCommand(Command onCloseCommand) {

        m_onCloseCommand = onCloseCommand;
    }

    /**
     * Parses the resources string.<p>
     *
     * @param resources the resources
     *
     * @return the list of resource ids
     */
    protected List<CmsUUID> parseResources(String resources) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(resources)) {
            return Collections.emptyList();
        } else {
            List<CmsUUID> result = new ArrayList<CmsUUID>();
            String[] resArray = resources.trim().split(";");
            for (int i = 0; i < resArray.length; i++) {
                result.add(new CmsUUID(resArray[i]));
            }
            return result;
        }
    }

    /**
     * Initializes the iFrame element.<p>
     */
    private native void initIFrame()/*-{
		var self = this;
		$wnd.frames.embeddedDialogFrame.connector = {
			reload : function() {
				self.@org.opencms.gwt.client.util.CmsEmbeddedDialogHandler::reload()();
			},
			finish : function(resources) {
				self.@org.opencms.gwt.client.util.CmsEmbeddedDialogHandler::finish(Ljava/lang/String;)(resources);
			},
			finishForProjectOrSiteChange : function(sitePath, serverLink) {
				self.@org.opencms.gwt.client.util.CmsEmbeddedDialogHandler::onSiteOrProjectChange(Ljava/lang/String;Ljava/lang/String;)(sitePath,serverLink)
			},
			leavePage : function(targetUri) {
				self.@org.opencms.gwt.client.util.CmsEmbeddedDialogHandler::leavePage(Ljava/lang/String;)(targetUri);
			}
		};
    }-*/;
}
