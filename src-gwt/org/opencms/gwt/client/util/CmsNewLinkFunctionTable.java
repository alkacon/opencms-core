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

import org.opencms.ade.contenteditor.shared.CmsEditorConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of the actions which should be executed for each collector context id.<p>
 */
public class CmsNewLinkFunctionTable {

    /** The global instance for this class. */
    public static final CmsNewLinkFunctionTable INSTANCE = new CmsNewLinkFunctionTable();

    /**
     * The map from collector context ids to the corresponding actions.<p>
     */
    private Map<String, Runnable> m_createFunctions = new HashMap<String, Runnable>();

    /**
     * Creates a new instance.<p>
     */
    public CmsNewLinkFunctionTable() {

        installFunction(CmsEditorConstants.FUNCTION_CREATE_NEW);
    }

    /**
     * Static method for used to create a new element based on a collector's context id and open the editor for id.<p>
     *
     * @param contextId the collector context id
     */
    public static void createAndEditNewElementStatic(String contextId) {

        INSTANCE.createAndEditNewElement(contextId);
    }

    /**
     * Helper method for logging.<p>
     *
     * @param s the string to log
     */
    private static native void log(String s) /*-{
                                             if ($wnd.console && $wnd.console.log) {
                                             $wnd.console.log(s);
                                             }
                                             }-*/;

    /**
     * Triggers creation and editing of a new element for the given collector, identified by its context id.<p>
     *
     * @param contextId the context id of the collector
     */
    public void createAndEditNewElement(String contextId) {

        Runnable action = m_createFunctions.get(contextId);
        if (action != null) {
            action.run();
        } else {
            log("Could not execute create action for context id '" + contextId + "'");
        }
    }

    /**
     * Installs a Javascript function which can be used to create and edit a new element given the collector context id.<p>
     *
     * @param functionName the name that should be used for the function
     */
    public native void installFunction(String functionName) /*-{
                                                            $wnd[functionName] = function(s) {
                                                            @org.opencms.gwt.client.util.CmsNewLinkFunctionTable::createAndEditNewElementStatic(Ljava/lang/String;)(s);
                                                            }
                                                            }-*/;

    /**
     * Sets the action which should be executed if cmsCreateAndEditNewElement is called with the given collector context id.<p>
     *
     * @param contextId the collector context id
     * @param action the action to execute
     */
    public void setHandler(String contextId, Runnable action) {

        if (contextId != null) {
            m_createFunctions.put(contextId, action);
        }
    }

}
