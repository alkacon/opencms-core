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

package org.opencms.relations;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;

import java.util.List;

/**
 * This interface serves to retrieve all links from a given file by parsing.<p>
 *
 * Relation validation for a file is only enabled if the file's resource type
 * implements this interface. so, files with resource types that do not implement
 * this interface don't get validated for broken links, during publsihing, for
 * instance.<p>
 *
 * This interface is used to build the internal relation information, but it is
 * not directly used to validate the relations.<p>
 *
 * @since 6.3.0
 */
public interface I_CmsLinkParseable {

    /**
     * Returns a list of all links from the specified file.<p>
     *
     * Implementations of this method must return an empty list, or better
     * {@link java.util.Collections#EMPTY_LIST}, if no link is found at all.<p>
     *
     * Implementations of this method should return the list of links including internal
     * (OpenCms VFS) and external links (http, https, mailto, ftp, etc.).<p>
     *
     * @param cms the current user's context
     * @param file the file to be parsed
     *
     * @return a list of {@link CmsLink} objects with the URIs of all linked resources
     */
    List<CmsLink> parseLinks(CmsObject cms, CmsFile file);
}