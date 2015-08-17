/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.shared;

/**
 * Constants for parameters, JSON keys, etc. which are used by both the client and server side code of the alias editor.<p>
 */
public interface I_CmsAliasConstants {

    /** JSON field key. */
    String JSON_LINE = "line";

    /** JSON field key. */
    String JSON_MESSAGE = "message";

    /** JSON field key. */
    String JSON_RESULT = "result";

    /** JSON field key. */
    String JSON_STATUS = "status";

    /** Request parameter name. */
    String PARAM_IMPORTFILE = "importfile";

    /** Request parameter name. */
    String PARAM_SEPARATOR = "separator";

    /** Request parameter name. */
    String PARAM_SITEROOT = "siteroot";

}
