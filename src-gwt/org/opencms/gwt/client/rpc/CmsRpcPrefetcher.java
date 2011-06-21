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

package org.opencms.gwt.client.rpc;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamFactory;

/**
 * Utility class for deserializing prefetched RPC data.<p>
 * 
 * @since 8.0
 */
public final class CmsRpcPrefetcher {

    /**
     * Hidden constructor.<p>
     */
    private CmsRpcPrefetcher() {

        // empty
    }

    /**
     * Deserializes the prefetched RPC data with the given name.<p>
     * 
     * @param asyncService the RPC service instance
     * @param name the global variable name
     * 
     * @return the prefetched RPC data
     */
    public static Object getSerializedObject(Object asyncService, String name) {

        try {
            SerializationStreamFactory ssf = (SerializationStreamFactory)asyncService;
            return ssf.createStreamReader(getString(name)).readObject();
        } catch (SerializationException e) {
            // should never happen
            CmsLog.log(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Retrieves the given global variable as a string.<p>
     * 
     * @param name the name of the variable to retrieve
     * 
     * @return the variable's value
     */
    private static native String getString(String name) /*-{
        return $wnd[name];
    }-*/;
}