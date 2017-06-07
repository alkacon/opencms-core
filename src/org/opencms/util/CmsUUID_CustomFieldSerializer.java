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

package org.opencms.util;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * GWT serializer for {@link CmsUUID}.<p>
 *
 * @since 8.0.0
 */
public final class CmsUUID_CustomFieldSerializer {

    /**
     * Hide constructor.<p>
     */
    private CmsUUID_CustomFieldSerializer() {

        // prevent instantiation
    }

    /**
     * Deserializes additional fields.<p>
     *
     * @param streamReader the reader
     * @param instance the instance
     */
    public static void deserialize(SerializationStreamReader streamReader, CmsUUID instance) {

        // No fields
    }

    /**
     * Creates a new instance.<p>
     *
     * @param streamReader the reader
     *
     * @return a new instance
     *
     * @throws SerializationException if something goes wrong
     */
    public static CmsUUID instantiate(SerializationStreamReader streamReader) throws SerializationException {

        return CmsUUID.valueOf(streamReader.readString());
    }

    /**
     * Serializes the given instance.<p>
     *
     * @param streamWriter the writer
     * @param instance the instance to serialize
     *
     * @throws SerializationException if something goes wrong
     */
    public static void serialize(SerializationStreamWriter streamWriter, CmsUUID instance)
    throws SerializationException {

        streamWriter.writeString(instance.toString());
    }
}