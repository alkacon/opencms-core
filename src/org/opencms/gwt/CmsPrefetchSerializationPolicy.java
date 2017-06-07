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

package org.opencms.gwt;

import java.util.Set;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.impl.LegacySerializationPolicy;

/**
 * A GWT serialization policy for pre-fetching.<p>
 *
 * @since 8.0.0
 */
public final class CmsPrefetchSerializationPolicy extends SerializationPolicy {

    /** The singleton instance of this serialization policy. */
    private static CmsPrefetchSerializationPolicy m_instance;

    /** An instance of the legacy serialization policy. */
    private LegacySerializationPolicy m_legacyPolicy = LegacySerializationPolicy.getInstance();

    /**
     * Hidden default constructor.<p>
     */
    private CmsPrefetchSerializationPolicy() {

        // do nothing
    }

    /**
     * Returns the singleton instance of this class.<p>
     *
     * @return the singleton instance of this class
     */
    public static CmsPrefetchSerializationPolicy instance() {

        if (m_instance == null) {
            m_instance = new CmsPrefetchSerializationPolicy();
        }
        return m_instance;
    }

    /**
     * @see com.google.gwt.user.server.rpc.SerializationPolicy#getClientFieldNamesForEnhancedClass(java.lang.Class)
     */
    @Override
    public Set<String> getClientFieldNamesForEnhancedClass(Class<?> clazz) {

        return null;
    }

    /**
     * @see com.google.gwt.user.server.rpc.SerializationPolicy#shouldDeserializeFields(java.lang.Class)
     */
    @Override
    public boolean shouldDeserializeFields(Class<?> clazz) {

        return m_legacyPolicy.shouldDeserializeFields(clazz);
    }

    /**
     * @see com.google.gwt.user.server.rpc.SerializationPolicy#shouldSerializeFields(java.lang.Class)
     */
    @Override
    public boolean shouldSerializeFields(Class<?> clazz) {

        return m_legacyPolicy.shouldDeserializeFields(clazz);
    }

    /**
     * @see com.google.gwt.user.server.rpc.SerializationPolicy#validateDeserialize(java.lang.Class)
     */
    @Override
    public void validateDeserialize(Class<?> clazz) {

        // all are valid
    }

    /**
     * @see com.google.gwt.user.server.rpc.SerializationPolicy#validateSerialize(java.lang.Class)
     */
    @Override
    public void validateSerialize(Class<?> clazz) {

        // all are valid
    }
}
