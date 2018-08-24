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

package org.opencms.db.timing;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default profiling handler which only has a single instance and delegates method calls to its
 * registered child handlers.<p>
 */
public class CmsDefaultProfilingHandler implements I_CmsProfilingHandler {

    /** The singleton instance. */
    public static final CmsDefaultProfilingHandler INSTANCE = new CmsDefaultProfilingHandler();

    /** The list of child handlers. */
    private CopyOnWriteArrayList<I_CmsProfilingHandler> m_handlers = new CopyOnWriteArrayList<>();

    /**
     * Hidden default constructor.<p>
     */
    protected CmsDefaultProfilingHandler() {
        // do nothing
    }

    /**
     * Adds a handler.<p>
     *
     * @param handler the handler to add
     */
    public void addHandler(I_CmsProfilingHandler handler) {

        m_handlers.add(handler);
    }

    /**
     * @see org.opencms.db.timing.I_CmsProfilingHandler#putTime(java.lang.String, long)
     */
    public void putTime(String key, long nanos) {

        for (I_CmsProfilingHandler handler : m_handlers) {
            handler.putTime(key, nanos);
        }
    }

    /**
     * Removes a handler.<p>
     *
     * @param handler the handler to remove
     */
    public void removeHandler(I_CmsProfilingHandler handler) {

        m_handlers.remove(handler);
    }

}
