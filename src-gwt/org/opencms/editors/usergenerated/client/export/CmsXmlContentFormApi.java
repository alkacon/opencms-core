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

package org.opencms.editors.usergenerated.client.export;

import org.opencms.editors.usergenerated.client.CmsRpcCallHelper;
import org.opencms.editors.usergenerated.shared.CmsFormContent;
import org.opencms.editors.usergenerated.shared.rpc.I_CmsFormEditService;
import org.opencms.editors.usergenerated.shared.rpc.I_CmsFormEditServiceAsync;
import org.opencms.gwt.client.util.CmsDebugLog;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;
import org.timepedia.exporter.client.NoExport;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * Root access point for the Javascript form editing API exported with gwt-exporter. Can be used 
 * to acquire new form editing sessions.<p>
 */
@Export
@ExportPackage("opencms")
public class CmsXmlContentFormApi implements Exportable {

    /** Service instance. */
    @NoExport
    public static final I_CmsFormEditServiceAsync SERVICE = GWT.create(I_CmsFormEditService.class);

    /** 
     * Default wait indicator callback.<p>
     * 
     * TODO: use default OpenCms gwt wait symbol here 
     */
    private I_CmsBooleanCallback m_waitIndicatorCallback = new I_CmsBooleanCallback() {

        public void call(boolean b) {

            CmsDebugLog.consoleLog("Wait indicator " + (b ? "ON" : "OFF"));
        }
    };

    /**
     * Default constructor for gwt-exporter.<p>
     */
    public CmsXmlContentFormApi() {

        // do nothing 

    }

    static {
        String url = Window.Location.getHref();
        // cut off fragment, parameters, and trailing slash, then append service name  
        url = url.replaceAll("#.*$", "").replaceAll("\\?.*$", "").replaceAll("/$", "")
            + "/org.opencms.editors.usergenerated.CmsFormEditService.gwt";
        ((ServiceDefTarget)SERVICE).setServiceEntryPoint(url);
    }

    /**
     * Creates an RPC helper object.<p>
     * 
     * @return the RPC helper 
     */
    @NoExport
    public CmsRpcCallHelper getRpcHelper() {

        return new CmsRpcCallHelper(m_waitIndicatorCallback);
    }

    /**
     * Creates a new session for a newly created XML content.<p>
     * 
     * @param formConfigPath the form configuration path 
     * @param formElement the form element 
     * @param onSuccess the function to call in case of success 
     * @param onError the function to call in case an error occurs 
     */
    public void initFormForNewContent(
        final String formConfigPath,
        final Element formElement,
        final I_CmsClientFormSessionCallback onSuccess,
        final I_CmsStringCallback onError) {

        getRpcHelper().executeRpc(SERVICE.getNewContent(formConfigPath, new AsyncCallback<CmsFormContent>() {

            public void onFailure(Throwable caught) {

                onError.call("RPC call failed: " + caught);
            }

            public void onSuccess(CmsFormContent result) {

                CmsClientFormSession session = new CmsClientFormSession(CmsXmlContentFormApi.this, result);
                onSuccess.call(session);
            }
        }));

    }

    /** 
     * Sets the wait indicator callback.<p>
     * 
     * @param callback a callback used to switch the wait indicator off or on.<p>
     */
    public void setWaitIndicatorCallback(I_CmsBooleanCallback callback) {

        m_waitIndicatorCallback = callback;
    }
}
