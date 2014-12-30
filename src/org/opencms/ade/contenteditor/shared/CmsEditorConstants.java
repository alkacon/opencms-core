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

package org.opencms.ade.contenteditor.shared;

/**
 * Constants used for content editor  integration.<p>
 */
public final class CmsEditorConstants {

    /** Javascript attribute name. */
    public static final String ATTR_CONTEXT_ID = "contextId";

    /** Javascript attribute name. */
    public static final String ATTR_MODE = "mode";

    /** Javascript attribute name. */
    public static final String ATTR_POST_CREATE_HANDLER = "postCreateHandler";

    /** Javascript function name. */
    public static final String FUNCTION_CREATE_NEW = "cmsCreateAndEditNewElement";

    /** Copy mode value. */
    public static final String MODE_COPY = "copy";

    /** Request parameter name. */
    public static final String PARAM_MODE = "mode";

    /** Request parameter name. */
    public static final String PARAM_POST_CREATE_HANDLER = "postCreateHandler";

    /** Attribute used for storing the element view of a collector list element. */
    public static final String ATTR_ELEMENT_VIEW = "elementView";

    /** Constant for the "reuse" mode when dropping clipboard elements. */
    public static final String MODE_REUSE = "reuse";

    /**
     * Hiding the constructor.<p>
     */
    private CmsEditorConstants() {

    }

}
