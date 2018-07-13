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

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Timer;

/**
 * Helper class to append a stylesheet link to the head of the current page and run a callback when everything has been loaded.
 */
public class CmsStylesheetLoader {

    /** The callback that should be called when everything has been loaded. */
    private Runnable m_allLoadedCallback;

    /** Indicates whether the all-loaded callback has already been called. */
    private boolean m_callbackCalled;

    /** The callback for the onload handlers of the stylesheets. */
    private JavaScriptObject m_jsCallback;

    /** The number of stylesheets that have been already loaded. */
    private int m_loadCounter;

    /** The list of stylesheets to load. */
    private List<String> m_stylesheets;

    /**
     * Creates a new instance.<p>
     *
     * @param stylesheets the list of stylesheets to load
     * @param allLoadedCallback the callback to call when everything has been loaded
     */
    public CmsStylesheetLoader(List<String> stylesheets, Runnable allLoadedCallback) {

        m_stylesheets = stylesheets;
        m_loadCounter = 0;
        m_allLoadedCallback = allLoadedCallback;

        m_jsCallback = createJsCallback();
    }

    /**
     * Checks the window.document for given style-sheet.<p>
     *
     * @param styleSheetLink the style-sheet link
     * @return true if the stylesheet is already present
     */
    private static native boolean checkStylesheet(String styleSheetLink)/*-{
        var styles = $wnd.document.styleSheets;
        for (var i = 0; i < styles.length; i++) {
            if (styles[i].href != null
                    && styles[i].href.indexOf(styleSheetLink) >= 0) {
                // style-sheet is present
                return true;
            }
        }
        return false;
    }-*/;

    /**
     * Starts the loading process and creates a timer that sets of the callback after a given tiime if it hasn't already been triggered.
     *
     * @param timeout number of milliseconds after which the callback should be called if it hasn't already been
     */
    public void loadWithTimeout(int timeout) {

        for (String stylesheet : m_stylesheets) {
            boolean alreadyLoaded = checkStylesheet(stylesheet);
            if (alreadyLoaded) {
                m_loadCounter += 1;
            } else {
                appendStylesheet(stylesheet, m_jsCallback);
            }
        }
        checkAllLoaded();
        if (timeout > 0) {
            Timer timer = new Timer() {

                @SuppressWarnings("synthetic-access")
                @Override
                public void run() {

                    callCallback();
                }
            };

            timer.schedule(timeout);
        }
    }

    /**
     * Appends a stylesheet to the page head.<p>
     *
     * @param stylesheet the stylesheet link
     * @param callback the load handler for the stylesheet link element
     */
    private native void appendStylesheet(String stylesheet, JavaScriptObject callback) /*-{
        var headID = $wnd.document.getElementsByTagName("head")[0];
        var cssNode = $wnd.document.createElement('link');
        cssNode.type = 'text/css';
        cssNode.rel = 'stylesheet';
        cssNode.href = stylesheet;
        headID.appendChild(cssNode, callback);
        cssNode.addEventListener("load", callback);
    }-*/;

    /**
     * Calls the callback if it hasn't already been triggered.<p>
     */
    private void callCallback() {

        if (!m_callbackCalled) {
            m_callbackCalled = true;
            m_allLoadedCallback.run();
        }
    }

    /**
     * Checks if all stylesheets have been loaded.<p>
     */
    private void checkAllLoaded() {

        if (m_loadCounter == m_stylesheets.size()) {
            callCallback();
        }
    }

    /**
     * Creates the callback for the onload handlers of the link elements.<p>
     *
     * @return the callback
     */
    private native JavaScriptObject createJsCallback() /*-{
        var self = this;
        return function() {
            self.@org.opencms.gwt.client.util.CmsStylesheetLoader::onLoadCallback()();
        }
    }-*/;

    /**
     * The method which should be called by the onload handlers of the link elements.<p>
     */
    private void onLoadCallback() {

        m_loadCounter += 1;
        checkAllLoaded();
    }

}
