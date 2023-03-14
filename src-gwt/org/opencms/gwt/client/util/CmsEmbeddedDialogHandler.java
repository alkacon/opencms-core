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
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.contextmenu.I_CmsActionHandler;
import org.opencms.gwt.client.ui.contextmenu.I_CmsStringSelectHandler;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.I_CmsEmbeddedDialogInfo;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

/**
 * Handler for embedded VAADIN dialogs.<p>
 */
public class CmsEmbeddedDialogHandler implements I_CmsHasInit, I_CmsEmbeddedDialogHandlerJsCallbacks {

    /** The context menu handler. */
    private I_CmsActionHandler m_handler;

    /** The on close command. */
    private Command m_onCloseCommand;

    /** The principle select handler. */
    private I_CmsStringSelectHandler m_stringSelectHandler;

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

        this();
        m_handler = handler;
    }

    /**
     * Encodes a parameter value for use in a query string.
     *
     * @param str the string to encode
     * @return the encoded string
     */
    public static String encodeParam(String str) {

        return com.google.gwt.http.client.URL.encodeQueryString(str);

    }

    /**
     * Exports native JS function cmsOpenEmbeddedDialog.
     */
    public static native void exportNativeFunctions() /*-{
        $wnd.cmsOpenEmbeddedDialog = function(dialogId, callback, structureIds,
                params) {
            @org.opencms.gwt.client.util.CmsEmbeddedDialogHandler::openEmbeddedDialog(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JsArrayString;Lcom/google/gwt/core/client/JavaScriptObject;)(dialogId, callback, structureIds, params);
        };
    }-*/;

    /**
     * Called on load, exports native functions.
     */
    public static void initClass() {

        exportNativeFunctions();
    }

    /**
     * Opens the given dialog in an iframe.
     *
     * @param dialogId the action class
     * @param structureIds the structure ids for the action
     * @param finishCallback the callback to call after the dialog closes
     */
    public static void openDialog(String dialogId, List<CmsUUID> structureIds, Consumer<CmsUUID> finishCallback) {

        CmsEmbeddedDialogHandler handler = new CmsEmbeddedDialogHandler(new I_CmsActionHandler() {

            public void leavePage(String targetUri) {

                // TODO Auto-generated method stub

            }

            public void onSiteOrProjectChange(String sitePath, String serverLink) {

                // TODO Auto-generated method stub

            }

            public void refreshResource(CmsUUID structureId) {

                finishCallback.accept(structureId);
            }
        });
        handler.openDialog(dialogId, CmsGwtConstants.CONTEXT_TYPE_FILE_TABLE, structureIds, new HashMap<>());
    }

    /**
     * Opens the given dialog in an iframe.
     *
     * @param dialogId the action class
     * @param structureIds the structure ids for the action
     * @param params additional parameters to pass
     * @param finishCallback the callback to call after the dialog closes
     */
    public static void openDialog(
        String dialogId,
        List<CmsUUID> structureIds,
        Map<String, String> params,
        Consumer<CmsUUID> finishCallback) {

        CmsEmbeddedDialogHandler handler = new CmsEmbeddedDialogHandler(new I_CmsActionHandler() {

            public void leavePage(String targetUri) {

                // TODO Auto-generated method stub

            }

            public void onSiteOrProjectChange(String sitePath, String serverLink) {

                // TODO Auto-generated method stub

            }

            public void refreshResource(CmsUUID structureId) {

                finishCallback.accept(structureId);
            }
        });
        handler.openDialog(dialogId, CmsGwtConstants.CONTEXT_TYPE_FILE_TABLE, structureIds, params);
    }

    /**
     * Opens an embedded dialog.
     *
     * @param dialogId the dialog
     * @param callback the onClose callback
     * @param structureIds the structure ids
     * @param params the parameters
     */
    public static void openEmbeddedDialog(
        String dialogId,
        JavaScriptObject callback,
        JsArrayString structureIds,
        JavaScriptObject params) {

        CmsEmbeddedDialogHandler handler = new CmsEmbeddedDialogHandler();
        if (callback != null) {
            Command command = CmsJsUtil.convertCallbackToCommand(callback);
            handler.setOnCloseCommand(command);
        }
        Map<String, String> paramsMap = new HashMap<>();
        if (params != null) {
            CmsJsUtil.fillStringMapFromJsObject(params, paramsMap);
        }
        List<CmsUUID> uuids = new ArrayList<>();
        if (structureIds != null) {
            for (int i = 0; i < structureIds.length(); i++) {
                String uuidStr = structureIds.get(i);
                try {
                    uuids.add(new CmsUUID(uuidStr));
                } catch (Exception e) {
                    CmsDebugLog.consoleLog(e.getClass() + ":" + e.getLocalizedMessage());
                }
            }
        }
        handler.openDialog(dialogId, null, uuids, paramsMap);
    }

    /**
     * @see org.opencms.gwt.client.util.I_CmsEmbeddedDialogHandlerJsCallbacks#finish(java.lang.String)
     */
    @Override
    public void finish(String resources) {

        CmsEmbeddedDialogFrame.get().hide();
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
     * Called when site and or project have been changed.<p>
     *
     * @param sitePath the site path to the resource to display
     * @param serverLink the server link to the resource to display
     */
    @Override
    public void finishForProjectOrSiteChange(String sitePath, String serverLink) {

        CmsEmbeddedDialogFrame.get().hide();
        if (m_handler != null) {
            m_handler.onSiteOrProjectChange(sitePath, serverLink);
        } else {
            Window.Location.assign(serverLink);
        }
    }

    /**
     * Navigates to the given URI.<p>
     *
     * @param targetUri the target URI
     */
    @Override
    public void leavePage(String targetUri) {

        CmsEmbeddedDialogFrame.get().hide();
        if (m_handler != null) {
            m_handler.leavePage(targetUri);
        } else {
            // the timer is a workaround for weird Safari behavior, just calling Location.assign doesn't work there
            Timer timer = new Timer() {

                @Override
                public void run() {

                    Window.Location.assign(targetUri);
                }
            };
            timer.schedule(10);
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
     * @param params additional set of parameters
     */

    public void openDialog(String dialogId, String contextType, List<CmsUUID> resources, Map<String, String> params) {

        AutoBean<I_CmsEmbeddedDialogInfo> info = CmsCoreProvider.AUTO_BEAN_FACTORY.createDialogInfo();
        info.as().setDialogId(dialogId);
        info.as().setContextType(contextType);
        if (resources == null) {
            resources = new ArrayList<>();
        }
        info.as().setStructureIds(resources.stream().map(id -> "" + id).collect(Collectors.toList()));
        if (params == null) {
            params = new HashMap<>();
        }
        info.as().setParameters(params);
        String infoJson = AutoBeanCodex.encode(info).getPayload();
        CmsEmbeddedDialogFrame.get().loadDialog(infoJson, this);
    }

    /**
     * Reloads the current page.<p>
     */
    @Override
    public void reload() {

        if (m_handler != null) {
            String uri = Window.Location.getHref();
            m_handler.leavePage(uri);
        } else {
            Window.Location.reload();
        }
    }

    /**
     * Calls the principle select handler and closes the dialog frame.<p>
     *
     * @param principle the principle to select
     */
    @Override
    public void selectString(String principle) {

        CmsEmbeddedDialogFrame.get().hide();
        if (m_stringSelectHandler != null) {
            m_stringSelectHandler.selectString(principle);
        }
        if (m_onCloseCommand != null) {
            m_onCloseCommand.execute();
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
     * Sets the principle select handler.<p>
     *
     * @param selectHandler the principle select handler
     */
    public void setStringSelectHandler(I_CmsStringSelectHandler selectHandler) {

        m_stringSelectHandler = selectHandler;
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

}
