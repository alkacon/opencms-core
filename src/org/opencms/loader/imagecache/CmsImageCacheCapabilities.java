/*
 * This library is part of OpenCms -
 * The Open Source Content Management System
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.loader.imagecache;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Describes the maintenance operations supported by an image cache backend.<p>
 */
public final class CmsImageCacheCapabilities {

    /** Individual backend capabilities. */
    public enum Capability {

        /** All cache entries can be listed. */
        LIST_ENTRIES,

        /** Listed entries contain a last-modified timestamp. */
        ENTRY_TIMESTAMPS,

        /** Individual entries can be deleted. */
        DELETE_ENTRIES,

        /** The complete cache can be cleared. */
        CLEAR,

        /** Individual entries can be renewed without regenerating their content. */
        RENEW_ENTRIES
    }

    /** The supported capabilities. */
    private final Set<Capability> m_capabilities;

    /**
     * Creates a capability set.<p>
     *
     * @param capabilities the supported capabilities
     */
    private CmsImageCacheCapabilities(EnumSet<Capability> capabilities) {

        m_capabilities = Collections.unmodifiableSet(EnumSet.copyOf(capabilities));
    }

    /**
     * Creates a capability set.<p>
     *
     * @param capabilities the supported capabilities
     * @return the capability set
     */
    public static CmsImageCacheCapabilities of(Capability... capabilities) {

        EnumSet<Capability> values = EnumSet.noneOf(Capability.class);
        Collections.addAll(values, capabilities);
        return new CmsImageCacheCapabilities(values);
    }

    /**
     * Returns the supported capabilities.<p>
     *
     * @return the supported capabilities
     */
    public Set<Capability> asSet() {

        return m_capabilities;
    }

    /**
     * Checks whether a capability is supported.<p>
     *
     * @param capability the capability
     * @return whether the capability is supported
     */
    public boolean supports(Capability capability) {

        return m_capabilities.contains(capability);
    }
}
