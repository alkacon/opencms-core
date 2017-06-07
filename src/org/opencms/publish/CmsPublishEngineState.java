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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.publish;

import org.opencms.util.A_CmsModeStringEnumeration;

/**
 * Class defining the publish engine states.<p>
 *
 * @since 6.9.1
 */
public final class CmsPublishEngineState extends A_CmsModeStringEnumeration {

    /** State indicating that engine is not acceptiong publish jobs. */
    public static final CmsPublishEngineState ENGINE_DISABLED = new CmsPublishEngineState("disabled");

    /** State indicating that engine is acceptiong and processing publish jobs. */
    public static final CmsPublishEngineState ENGINE_STARTED = new CmsPublishEngineState("started");

    /** State indicating that engine is accepting but not processing publish jobs. */
    public static final CmsPublishEngineState ENGINE_STOPPED = new CmsPublishEngineState("stopped");

    /** The serial version id. */
    private static final long serialVersionUID = 4860148946570407490L;

    /**
     * Private constructor.<p>
     *
     * @param state the state description
     */
    private CmsPublishEngineState(String state) {

        super(state);
    }
}
