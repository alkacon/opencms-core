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

package org.opencms.ui.client;

import org.opencms.gwt.client.ui.CmsInfoHeader;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsJsUtil;
import org.opencms.gwt.client.util.CmsScriptCallbackHelper;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.ui.shared.rpc.I_CmsSitemapClientRpc;
import org.opencms.ui.shared.rpc.I_CmsSitemapServerRpc;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Connector class for the Vaadin extension used to embed Vaadin dialogs in the sitemap editor.<p>
 */
@Connect(org.opencms.ui.sitemap.CmsSitemapExtension.class)
public class CmsSitemapExtensionConnector extends AbstractExtensionConnector implements I_CmsSitemapClientRpc {

    /** Counter used to generate unique ids for RPC calls. */
    public static int CALL_COUNTER = 1;

    /** The singleton instance of this class. */
    static CmsSitemapExtensionConnector INSTANCE;

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Map of callbacks for RPC responses. */
    private Map<String, AsyncCallback<String>> m_callbacks = Maps.newHashMap();

    /**
     * Creates a new instance.<p>
     */
    public CmsSitemapExtensionConnector() {
        super();
        registerRpc(I_CmsSitemapClientRpc.class, this);
        INSTANCE = this;
    }

    /**
     * Gets the static instance of this class.<p>
     *
     * @return the instance
     */
    public static CmsSitemapExtensionConnector get() {

        return INSTANCE;
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsSitemapClientRpc#finishPageCopyDialog(java.lang.String, java.lang.String)
     */
    public void finishPageCopyDialog(String callId, String response) {

        m_callbacks.get(callId).onSuccess(response);
        m_callbacks.remove(callId);
    }

    /**
     * Installs the native JavaScript functions used to access Vaadin functionality from the sitemap editor's GWT module.<p>
     */
    public native void installNativeFunctions() /*-{
                                                var self = this;
                                                $wnd.cmsOpenPageCopyDialog = function(id, callback) {
                                                self.@org.opencms.ui.client.CmsSitemapExtensionConnector::openPageCopyDialog(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(id , callback);
                                                }
                                                }-*/;

    /**
     * Opens the page copy dialog.<p>
     *
     * @param structureId the structure id of the resource for which to open the dialog
     * @param callback the callback to call with the result when the dialog has finished
     */
    public void openPageCopyDialog(String structureId, AsyncCallback<String> callback) {

        String callId = "" + CALL_COUNTER++;
        m_callbacks.put(callId, callback);
        getRpcProxy(I_CmsSitemapServerRpc.class).openPageCopyDialog(callId, "" + structureId);

    }

    /**
     * Opens the page copy dialog.<p>
     *
     * @param id the structure id of the resource for which to open the dialog
     * @param callback the native callback to call with the result when the dialog has finished
     */
    public void openPageCopyDialog(String id, JavaScriptObject callback) {

        openPageCopyDialog(id, CmsJsUtil.wrapCallback(callback));
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsSitemapClientRpc#openPropertyDialog(java.lang.String, java.lang.String)
     */
    public void openPropertyDialog(String structureIdStr, String rootIdStr) {

        CmsJsUtil.callNamedFunctionWithString2(
            CmsGwtConstants.LOCALECOMPARE_EDIT_PROPERTIES,
            structureIdStr,
            rootIdStr);
    }

    /**
     * @see org.opencms.ui.shared.rpc.I_CmsSitemapClientRpc#showInfoHeader(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void showInfoHeader(String title, String description, String path, String locale, String typeIcon) {

        CmsInfoHeader header = new CmsInfoHeader(title, description, path, locale, typeIcon);
        RootPanel panel = RootPanel.get(CmsGwtConstants.ID_LOCALE_HEADER_CONTAINER);
        if (panel == null) {
            CmsDebugLog.consoleLog("no element #locale-header-container found");
        } else {
            if (panel.getWidgetCount() > 0) {
                panel.remove(0);
            }
            panel.add(header);
        }
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        installNativeFunctions();
        CmsScriptCallbackHelper callbackHelper = new CmsScriptCallbackHelper() {

            @Override
            @SuppressWarnings("synthetic-access")
            public void run() {

                JsArrayString arguments = m_arguments.cast();
                getRpcProxy(I_CmsSitemapServerRpc.class).showLocaleComparison(arguments.get(0));

            }
        };
        callbackHelper.installCallbackOnWindow(CmsGwtConstants.CALLBACK_REFRESH_LOCALE_COMPARISON);

        CmsScriptCallbackHelper propertySaveHelper = new CmsScriptCallbackHelper() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void run() {

                JsArrayString arguments = m_arguments.cast();
                String id = arguments.get(0);
                getRpcProxy(I_CmsSitemapServerRpc.class).handleChangedProperties(id);
            }

        };
        propertySaveHelper.installCallbackOnWindow(CmsGwtConstants.CALLBACK_HANDLE_CHANGED_PROPERTIES);

    }

}
