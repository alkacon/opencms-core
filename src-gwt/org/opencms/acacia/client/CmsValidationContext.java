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

package org.opencms.acacia.client;

import java.util.HashSet;
import java.util.Set;

/**
 * The validation context. Keeps track of valid and invalid entity id's.<p>
 */
public class CmsValidationContext {

    /** The invalid entity id's. */
    private Set<String> m_invalidEntityIds;

    /** The valid entity id's. */
    private Set<String> m_validEntityIds;

    /**
     * Constructor.<p>
     */
    public CmsValidationContext() {

        m_invalidEntityIds = new HashSet<String>();
        m_validEntityIds = new HashSet<String>();
    }

    /**
     * Adds an invalid entity id.<p>
     * 
     * @param entityId the entity id
     */
    public void addInvalidEntity(String entityId) {

        m_validEntityIds.remove(entityId);
        m_invalidEntityIds.add(entityId);
    }

    /**
     * Adds a valid entity id.<p>
     * 
     * @param entityId the entity id
     */
    public void addValidEntity(String entityId) {

        m_invalidEntityIds.remove(entityId);
        m_validEntityIds.add(entityId);
    }

    /**
     * Returns the invalid entity id's.<p>
     * 
     * @return the invalid entity id's
     */
    public Set<String> getInvalidEntityIds() {

        return m_invalidEntityIds;
    }

    /**
     * Returns the valid entity id's.<p>
     * 
     * @return the valid entity id's
     */
    public Set<String> getValidEntityIds() {

        return m_validEntityIds;
    }

    /**
     * Returns if there are any invalid entities.<p>
     * 
     * @return <code>true</code>  if there are any invalid entities
     */
    public boolean hasValidationErrors() {

        return !m_invalidEntityIds.isEmpty();
    }

    /**
     * Removes the given entity id, use when validating the entity is no longer required.<p>
     * 
     * @param entityId the entity id
     */
    public void removeEntityId(String entityId) {

        m_invalidEntityIds.remove(entityId);
        m_validEntityIds.remove(entityId);
    }
}
