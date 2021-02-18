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

package org.opencms.gwt.client;

import org.opencms.gwt.client.util.CmsJSONMap;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Accessor for a global singleton JSON object used to pass additional context information to RPC calls.
 */
public class CmsRpcContext {

    /** The JSON map. */
    private static CmsJSONMap m_map;

    /**
     * Gets the JSON map.
     *
     * @return the JSON map
     */
    public static CmsJSONMap get() {

        if (m_map == null) {
            m_map = CmsJSONMap.createJSONMap();
            installContext(m_map);
        }
        return m_map;

    }

    /**
     * Installs the JSON object as a variable in the main window.
     *
     * @param context the context to set
     */
    private static native void installContext(JavaScriptObject context) /*-{
		$wnd.ocmsRpcContext = context;
    }-*/;

}
