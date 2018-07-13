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

import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.util.CmsDebugLog;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.vaadin.client.ResourceLoader;
import com.vaadin.client.ResourceLoader.ResourceLoadEvent;
import com.vaadin.client.ResourceLoader.ResourceLoadListener;
import com.vaadin.client.WidgetUtil;

/**
 * Entry point class for the OpenCms standard widgetset.<p>
 */
public class CmsWidgetSetEntryPoint extends A_CmsEntryPoint {

    /**
     * Loads JavaScript resources into the window context.<p>
     *
     * @param dependencies the dependencies to load
     * @param callback the callback to execute once the resources are loaded
     */
    public static void loadScriptDependencies(final JsArrayString dependencies, final JavaScriptObject callback) {

        if (dependencies.length() == 0) {
            return;
        }
        final Set<String> absoluteUris = new HashSet<String>();
        for (int i = 0; i < dependencies.length(); i++) {
            absoluteUris.add(WidgetUtil.getAbsoluteUrl(dependencies.get(i)));
        }
        // Listener that loads the next when one is completed
        ResourceLoadListener resourceLoadListener = new ResourceLoadListener() {

            @Override
            public void onError(ResourceLoadEvent event) {

                CmsDebugLog.consoleLog(event.getResourceUrl() + " could not be loaded.");
                // The show must go on
                absoluteUris.remove(event.getResourceUrl());
                if (absoluteUris.isEmpty()) {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        public void execute() {

                            callNativeFunction(callback);
                        }
                    });
                }

            }

            @Override
            public void onLoad(ResourceLoadEvent event) {

                absoluteUris.remove(event.getResourceUrl());
                if (absoluteUris.isEmpty()) {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                        public void execute() {

                            callNativeFunction(callback);
                        }
                    });

                }
            }
        };

        ResourceLoader loader = ResourceLoader.get();
        for (int i = 0; i < dependencies.length(); i++) {
            String preloadUrl = dependencies.get(i);
            loader.loadScript(preloadUrl, resourceLoadListener);
        }
    }

    /**
     * Calls the native function.<p>
     *
     * @param callback the function to call
     */
    static native void callNativeFunction(JavaScriptObject callback)/*-{
        callback.call();
    }-*/;

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        exportUtitlityFunctions();
        try {
            String tinyMCE = CmsCoreProvider.get().getTinymce().getLink();
            if (tinyMCE == null) {
                CmsDebugLog.consoleLog("tinyMCE link is null");
            } else {
                ResourceLoader.get().loadScript(tinyMCE, new ResourceLoadListener() {

                    public void onError(ResourceLoadEvent event) {

                        CmsDebugLog.consoleLog("error loading TinyMCE");
                    }

                    public void onLoad(ResourceLoadEvent event) {
                        // ignore

                    }
                });
            }
        } catch (Exception e) {
            //
        }
    }

    /**
     * Exports utility methods to the window context.<p>
     */
    private native void exportUtitlityFunctions()/*-{
        $wnd.cmsLoadScripts = function(scriptURIs, callback) {
            @org.opencms.ui.client.CmsWidgetSetEntryPoint::loadScriptDependencies(Lcom/google/gwt/core/client/JsArrayString;Lcom/google/gwt/core/client/JavaScriptObject;)(scriptURIs, callback);
        }
        $wnd.cmsSafeLoadCSS = function(cssURIs, callback) {
            @org.opencms.gwt.client.util.CmsDomUtil::safeLoadStylesheets([Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(cssURIs, callback);
        }
        $wnd.cmsLoadCSS = function(cssURIs) {
            for (i = 0; i < cssURIs.length; i++) {
                @org.opencms.gwt.client.util.CmsDomUtil::ensureStyleSheetIncluded(Ljava/lang/String;)(cssURIs[i]);
            }
        }
    }-*/;

}
