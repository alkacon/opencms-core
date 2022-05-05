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

package org.opencms.acacia.client.widgets.code;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsScriptCallbackHelper;
import org.opencms.gwt.client.util.CmsStylesheetLoader;

import java.util.Arrays;
import java.util.Random;

/**
 * Helper class that asynchronously loads all the necessary scripts and stylesheets for the CodeMirror editor widget, and
 * executes a callback at the end.
 *
 * <p>Scripts are loaded (unless they've already been loaded before) with the async=false option, which causes them to be executed in order, and a special dummy
 * script is loaded at the end to execute the callback.
 */
public class CmsCodeMirrorScriptLoader {

    /** Random number generator for generating the random parameters for the dummy script. */
    private static Random random = new Random();

    /** The base URI.*/
    private String m_baseUri;

    /**
     * Creates a new instance.
     */
    public CmsCodeMirrorScriptLoader() {

        m_baseUri = CmsCoreProvider.get().getWorkplaceResourcesPrefix() + "/editors/codemirror";

    }

    /**
     * Asynchronously loads all scripts / stylesheets and calls the given callback at the end.
     *
     * @param callback the callback to execute after script loading
     */
    public void load(Runnable callback) {

        CmsStylesheetLoader cssLoader = new CmsStylesheetLoader(
            Arrays.asList(
                m_baseUri + "/dist/lib/codemirror.css",
                m_baseUri + "/dist/theme/eclipse.css",
                m_baseUri + "/dist/addon/dialog/dialog.css",
                m_baseUri + "/dist/addon/hint/show-hint.css",
                m_baseUri + "/codemirror-ocms.css"),

            () -> {
                load("/js/lang-en.js");
                load("/dist/lib/codemirror.js");
                load("/dist/addon/dialog/dialog.js");
                load("/dist/addon/search/searchcursor.js");
                load("/dist/addon/search/search.js");
                load("/dist/addon/search/jump-to-line.js");
                load("/dist/addon/edit/closebrackets.js");
                load("/dist/addon/edit/closetag.js");
                load("/dist/addon/edit/matchbrackets.js");
                load("/dist/addon/edit/trailingspace.js");
                load("/dist/addon/hint/show-hint.js");
                load("/dist/addon/hint/html-hint.js");
                load("/dist/addon/hint/javascript-hint.js");
                load("/dist/addon/hint/xml-hint.js");
                load("/dist/addon/fold/foldcode.js");
                load("/dist/addon/fold/brace-fold.js");
                load("/dist/addon/fold/xml-fold.js");
                load("/dist/addon/comment/comment.js");
                load("/dist/addon/selection/active-line.js");
                load("/dist/mode/css/css.js");
                load("/dist/mode/xml/xml.js");
                load("/dist/mode/clike/clike.js");
                load("/dist/mode/javascript/javascript.js");
                load("/dist/mode/htmlmixed/htmlmixed.js");
                load("/js/htmlembedded_modified.js");
                CmsScriptCallbackHelper helper = new CmsScriptCallbackHelper() {

                    @Override
                    public void run() {

                        callback.run();
                    }
                };
                // use a random number to avoid caching and ensure that the onload handler
                // is always executed
                CmsDomUtil.ensureJavaScriptIncluded(
                    m_baseUri + "/dummy.js?r=" + random.nextInt(100000000),
                    false,
                    helper.createCallback());

            });
        cssLoader.loadWithTimeout(10000);
    }

    /**
     * Loads the script from the given path (which is treated as relative to the CodeMirror base URI.
     *
     * @param path the path to load the script from
     */
    public void load(String path) {

        // ensure scripts are executed in insertion order with async=false
        CmsDomUtil.ensureJavaScriptIncluded(m_baseUri + path, /*async=*/false);
    }

}
