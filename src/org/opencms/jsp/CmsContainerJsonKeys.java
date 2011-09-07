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

package org.opencms.jsp;

/**
 * JSON keys used for creating Javascript objects representing containers.<p>
 * 
 * @since 8.0.0
 */
public final class CmsContainerJsonKeys {

    /** Key 'isDetailView' used within the JSON representation of a container object. */
    public static final String DETAILVIEW = "isDetailView";

    /** Key 'elements' used within the JSON representation of a container object. */
    public static final String ELEMENTS = "elements";

    /** Key 'maxElements' used within the JSON representation of a container object. */
    public static final String MAXELEMENTS = "maxElem";

    /** Key 'name' used within the JSON representation of a container object. */
    public static final String NAME = "name";

    /** Key 'type' used within the JSON representation of a container object. */
    public static final String TYPE = "type";

    /** Key 'width' used within the JSON representation of a container object. */
    public static final String WIDTH = "width";

    /**
     * Hidden constructor.<p>
     */
    private CmsContainerJsonKeys() {

        // do nothing
    }
}
