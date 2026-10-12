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

package org.opencms.file.quota;

import org.opencms.util.CmsFileUtil;

/**
 * A budget scope with its individual setting and applicable default, independent of the configuration format.<p>
 *
 * A scope is a website root or a subsite marked with {@link CmsQuotaConfig#ATTR_UPLOAD_QUOTA_SCOPE}.
 * Ordinary subsites share their enclosing scope's budget and do not have separate definitions.
 * The default may already include inheritance from enclosing websites or subsites.<p>
 */
public class CmsQuotaDefinition {

    /** One decimal GB, matching budget input and storage size display. */
    public static final long BYTES_PER_GB = 1000L * 1000 * 1000;

    /** Explicitly unlimited. Null, in contrast, means inherit. */
    public static final Long UNLIMITED = Long.valueOf(0);

    /** A setting could not be read; must not silently fall back to an inherited value. */
    public static final Long UNAVAILABLE = Long.valueOf(-1);

    /** Normalized scope root path. */
    private final String m_rootPath;

    /** Applicable default for this scope and subordinate budget scopes; may already be inherited. */
    private final Long m_defaultQuota;

    /** Explicit quota for this scope only. */
    private final Long m_individualQuota;

    /**
     * Constructor.<p>
     *
     * @param rootPath the absolute VFS root path of the scope
     * @param defaultQuota applicable default for this scope and subordinate budget scopes, in bytes; null to inherit
     * @param individualQuota quota for this scope only, in bytes; null to use the default
     */
    public CmsQuotaDefinition(String rootPath, Long defaultQuota, Long individualQuota) {

        if ((rootPath == null) || !rootPath.startsWith("/")) {
            throw new IllegalArgumentException("A quota scope requires a VFS root path");
        }
        m_rootPath = CmsFileUtil.addTrailingSeparator(rootPath);
        m_defaultQuota = defaultQuota;
        m_individualQuota = individualQuota;
    }

    /**
     * Gets the applicable default for this scope and subordinate budget scopes.<p>
     *
     * @return the default, null to inherit, 0 for unlimited, or -1 if unavailable
     */
    public Long getDefaultQuota() {

        return m_defaultQuota;
    }

    /**
     * Gets the individual quota for this scope.<p>
     *
     * @return the individual limit, null for default, 0 for unlimited, or -1 if unavailable
     */
    public Long getIndividualQuota() {

        return m_individualQuota;
    }

    /**
     * Gets the VFS root path of the quota scope.<p>
     *
     * @return the scope's root path, with trailing slash
     */
    public String getRootPath() {

        return m_rootPath;
    }
}
