/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/Attic/CmsXmlContentProperty_CustomFieldSerializer.java,v $
 * Date   : $Date: 2010/05/05 15:18:22 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
 * * This library is free software; you can redistribute it and/or
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

package org.opencms.xml.content;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * The GWT field serializer for the {@link CmsXmlContentProperty} class.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public final class CmsXmlContentProperty_CustomFieldSerializer {

    /**
     * Hide the default constructor.<p>
     */
    private CmsXmlContentProperty_CustomFieldSerializer() {

        // do nothing
    }

    /**
     * Deserializes a {@link CmsXmlContentProperty} object.
     * 
     * This method actually does nothing since the instantiate() method already does all the work.
     * 
     * @param streamReader 
     * @param property
     * @throws SerializationException
     */
    @SuppressWarnings("unused")
    public static void deserialize(SerializationStreamReader streamReader, CmsXmlContentProperty property)
    throws SerializationException {

        // do nothing; instantiate() already does all the work

    }

    /**
     * Instantiates a {@link CmsXmlContentProperty} object from a GWT serialization stream.<p>
     * 
     * @param streamReader the serialization stream reader
     * 
     * @return the newly instantiated CmsXmlContentProperty 
     * 
     * @throws SerializationException if the serialization fails 
     */
    public static CmsXmlContentProperty instantiate(SerializationStreamReader streamReader)
    throws SerializationException {

        String name = streamReader.readString();
        String type = streamReader.readString();
        String widget = streamReader.readString();
        String widgetConfig = streamReader.readString();
        String ruleRegex = streamReader.readString();
        String ruleType = streamReader.readString();
        String default1 = streamReader.readString();
        String niceName = streamReader.readString();
        String description = streamReader.readString();
        String error = streamReader.readString();
        return new CmsXmlContentProperty(
            name,
            type,
            widget,
            widgetConfig,
            ruleRegex,
            ruleType,
            default1,
            niceName,
            description,
            error);
    }

    /**
     * Serializes a {@link CmsXmlContentProperty} instance to a GWT serialization stream.<p>
     * 
     * @param streamWriter the stream writer for the GWT serialization 
     * @param prop the object to serialize
     * 
     * @throws SerializationException if the serialization goes wrong 
     */
    public static void serialize(SerializationStreamWriter streamWriter, CmsXmlContentProperty prop)
    throws SerializationException {

        String[] fields = new String[] {
            prop.getPropertyName(),
            prop.getPropertyType(),
            prop.getWidget(),
            prop.getWidgetConfiguration(),
            prop.getRuleRegex(),
            prop.getRuleType(),
            prop.getDefault(),
            prop.getNiceName(),
            prop.getDescription(),
            prop.getError()};
        for (String field : fields) {
            streamWriter.writeString(field);
        }
    }

}
