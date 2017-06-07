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

package org.opencms.ugc.client;

import org.opencms.ugc.client.export.CmsXmlContentUgcApi;

import org.timepedia.exporter.client.ExporterUtil;

import com.google.gwt.core.client.EntryPoint;

/**
 * Entry point for client-side form handling code for user-generated content module.<p>
 */
public class CmsUgcEntryPoint implements EntryPoint {

    /**
     * Exports the API objects as native Javascript objects.<p>
     *
     * @param api the API to expose as Javascript object
     */
    public native void installJavascriptApi(CmsXmlContentUgcApi api) /*-{
                                                                     $wnd.OpenCmsUgc = new $wnd.opencmsugc.CmsXmlContentUgcApi(api);
                                                                     }-*/;

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    public void onModuleLoad() {

        ExporterUtil.exportAll();
        CmsXmlContentUgcApi api = new CmsXmlContentUgcApi();
        installJavascriptApi(api);
        callInitFunction();
    }

    /**
     * Calls the init form method after the API has been exported.<p>
     */
    private native void callInitFunction() /*-{
                                           if ($wnd.initUgc != undefined && typeof $wnd.initUgc == 'function') {
                                           $wnd.initUgc();
                                           }
                                           }-*/;

}
