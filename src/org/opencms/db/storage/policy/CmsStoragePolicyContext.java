/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db.storage.policy;

import org.opencms.file.CmsResource;

/**
 * Context for evaluating the storage policy for binary content.
 */
public class CmsStoragePolicyContext {

    /** The binary content. */
    private final byte[] m_content;

    /** The resource. */
    private final CmsResource m_resource;

    /**
     * Creates a new storage policy context without a resource.
     *
     * @param content the binary content
     */
    public CmsStoragePolicyContext(byte[] content) {

        this(content, null);
    }

    /**
     * Creates a new storage policy context.
     *
     * @param content the binary content
     * @param resource the resource
     */
    public CmsStoragePolicyContext(byte[] content, CmsResource resource) {

        m_content = content;
        m_resource = resource;
    }

    /**
     * Returns the binary content.
     *
     * @return the binary content
     */
    public byte[] getContent() {

        return m_content;
    }

    /**
     * Returns the resource.
     *
     * @return the resource
     */
    public CmsResource getResource() {

        return m_resource;
    }

}
