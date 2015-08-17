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

package org.opencms.acacia.shared;

import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The entity validation result containing all errors and warnings for a set of entities.<p>
 */
public class CmsValidationResult implements IsSerializable {

    /** The error messages by entity and attribute. */
    private Map<String, Map<String[], String>> m_errors;

    /** The warning messages by entity and attribute. */
    private Map<String, Map<String[], String>> m_warnings;

    /**
     * Constructor.<p>
     *
     * @param errors the error messages by entity and attribute
     * @param warnings the warning messages by entity and attribute
     */
    public CmsValidationResult(Map<String, Map<String[], String>> errors, Map<String, Map<String[], String>> warnings) {

        m_errors = errors;
        m_warnings = warnings;
    }

    /**
     * Constructor. For serialization only.<p>
     */
    protected CmsValidationResult() {

        // nothing to do
    }

    /**
     * Returns all error messages by entity id and attribute.<p>
     *
     * @return the error messages by entity id and attribute
     */
    public Map<String, Map<String[], String>> getErrors() {

        return m_errors;
    }

    /**
     * Returns the error messages for the given entity.<p>
     *
     * @param entityId the entity id
     *
     * @return the error messages for the given entity
     */
    public Map<String[], String> getErrors(String entityId) {

        return m_errors != null ? m_errors.get(entityId) : null;
    }

    /**
     * Returns all warning messages by entity id and attribute.<p>
     *
     * @return the warning messages by entity id and attribute
     */
    public Map<String, Map<String[], String>> getWarnings() {

        return m_warnings;
    }

    /**
     * Returns the warning messages for the given entity.<p>
     *
     * @param entityId the entity id
     *
     * @return the warning messages for the given entity
     */
    public Map<String[], String> getWarnings(String entityId) {

        return m_warnings != null ? m_warnings.get(entityId) : null;
    }

    /**
     * Returns if there are any errors.<p>
     *
     * @return <code>true</code> if there are any errors
     */
    public boolean hasErrors() {

        return (m_errors != null) && !m_errors.isEmpty();
    }

    /**
     * Returns if the entity of the given id has errors.<p>
     *
     * @param entityId the entity id
     *
     * @return <code>true</code> if the entity of the given id has errors
     */
    public boolean hasErrors(String entityId) {

        return (m_errors != null) && (m_errors.get(entityId) != null);
    }

    /**
     * Returns if there are any warnings.<p>
     *
     * @return <code>true</code> if there are any warnings
     */
    public boolean hasWarnings() {

        return (m_warnings != null) && !m_warnings.isEmpty();
    }

    /**
     * Returns if the entity of the given id has warnings.<p>
     *
     * @param entityId the entity id
     *
     * @return <code>true</code> if the entity of the given id has warnings
     */
    public boolean hasWarnings(String entityId) {

        return (m_warnings != null) && (m_warnings.get(entityId) != null);
    }

}
