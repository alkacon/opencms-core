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

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * GWT serializer for {@link CmsRpcException}.<p>
 *
 * This deliberately serializes only the exception data maintained by
 * {@link CmsRpcException}. In particular, it prevents GWT from reflectively
 * accessing fields declared by {@link Throwable}, which is not permitted by
 * the Java module system.<p>
 */
public final class CmsRpcException_CustomFieldSerializer extends CustomFieldSerializer<CmsRpcException> {

    /**
     * Deserializes the exception data.<p>
     *
     * @param streamReader the reader
     * @param instance the instance
     *
     * @throws SerializationException if something goes wrong
     */
    public static void deserialize(SerializationStreamReader streamReader, CmsRpcException instance)
    throws SerializationException {

        instance.setOriginalCauseMessage(streamReader.readString());
        instance.setOriginalClassName(streamReader.readString());
        instance.setOriginalMessage(streamReader.readString());
        instance.setOriginalStackTrace((StackTraceElement[])streamReader.readObject());
    }

    /**
     * Serializes the exception data.<p>
     *
     * @param streamWriter the writer
     * @param instance the instance
     *
     * @throws SerializationException if something goes wrong
     */
    public static void serialize(SerializationStreamWriter streamWriter, CmsRpcException instance)
    throws SerializationException {

        streamWriter.writeString(instance.getOriginalCauseMessage());
        streamWriter.writeString(instance.getOriginalClassName());
        streamWriter.writeString(instance.getOriginalMessage());
        streamWriter.writeObject(instance.getOriginalStackTrace());
    }

    /**
     * @see com.google.gwt.user.client.rpc.CustomFieldSerializer#deserializeInstance(com.google.gwt.user.client.rpc.SerializationStreamReader, java.lang.Object)
     */
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, CmsRpcException instance)
    throws SerializationException {

        deserialize(streamReader, instance);
    }

    /**
     * @see com.google.gwt.user.client.rpc.CustomFieldSerializer#serializeInstance(com.google.gwt.user.client.rpc.SerializationStreamWriter, java.lang.Object)
     */
    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, CmsRpcException instance)
    throws SerializationException {

        serialize(streamWriter, instance);
    }
}
