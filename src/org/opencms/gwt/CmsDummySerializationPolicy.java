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

package org.opencms.gwt;

import com.google.gwt.user.server.rpc.SerializationPolicy;

/**
 * A serialization policy which allows serialization and deserialization of all classes.<p>
 *
 * @since 8.0.0
 */
public class CmsDummySerializationPolicy extends SerializationPolicy {

    /**
     * @see com.google.gwt.user.server.rpc.SerializationPolicy#shouldDeserializeFields(java.lang.Class)
     */
    @Override
    public boolean shouldDeserializeFields(Class<?> clazz) {

        if ((clazz == null) || (clazz == Object.class)) {
            return false;
        }

        return true;
    }

    /**
     * @see com.google.gwt.user.server.rpc.SerializationPolicy#shouldSerializeFields(java.lang.Class)
     */
    @Override
    public boolean shouldSerializeFields(Class<?> clazz) {

        if ((clazz == null) || (clazz == Object.class)) {
            return false;
        }

        return true;
    }

    /**
     * @see com.google.gwt.user.server.rpc.SerializationPolicy#validateDeserialize(java.lang.Class)
     */
    @Override
    public void validateDeserialize(Class<?> clazz) {

        // do nothing

    }

    /**
     * @see com.google.gwt.user.server.rpc.SerializationPolicy#validateSerialize(java.lang.Class)
     */
    @Override
    public void validateSerialize(Class<?> clazz) {

        // do nothing

    }

}
