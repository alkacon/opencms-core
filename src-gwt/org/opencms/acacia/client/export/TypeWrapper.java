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

package org.opencms.acacia.client.export;

import org.opencms.acacia.shared.Type;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

/**
 * Exportable wrapper for entity types.<p>
 */
@Export
@ExportPackage(value = "acacia")
public class TypeWrapper implements Exportable {

    /** The wrapped type object. */
    private Type m_type;

    /**
     * Default constructor.
     */
    public TypeWrapper() {

    }

    /** 
     * Wrapper constructor.<p>
     * 
     * @param type the type object to wrap 
     */
    public TypeWrapper(Type type) {

        m_type = type;
    }

    /**
     * Wrapper method.<p>
     * 
     * @param attributeName parameter for the wrapped method 
     * 
     * @return the result of the wrapped method 
     */
    public int getAttributeMaxOccurrence(String attributeName) {

        return m_type.getAttributeMaxOccurrence(attributeName);
    }

    /**
     * Wrapper method.<p>
     * 
     * @param attributeName parameter for the wrapped method 
     * 
     * @return the result of the wrapped method 
     */
    public int getAttributeMinOccurrence(String attributeName) {

        return m_type.getAttributeMinOccurrence(attributeName);
    }

    /**
     * Wrapper method.<p>
     * 
     * @return the result of the wrapped method 
     */
    public String[] getAttributeNames() {

        return WrapperUtils.arrayFromStringList(m_type.getAttributeNames());
    }

    /**
     * Wrapper method.<p>
     * 
     * @param attributeName parameter for the wrapped method 
     * 
     * @return the result of the wrapped method 
     */
    public TypeWrapper getAttributeType(String attributeName) {

        return new TypeWrapper(m_type.getAttributeType(attributeName));
    }

    /**
     * Wrapper method.<p>
     * 
     * @param attributeName parameter for the wrapped method 
     * 
     * @return the result of the wrapped method 
     */
    public String getAttributeTypeName(String attributeName) {

        return m_type.getAttributeTypeName(attributeName);
    }

    /**
     * Wrapper method.<p>
     * 
     * @return the result of the wrapped method
     */
    public int getChoiceMaxOccurrence() {

        return m_type.getChoiceMaxOccurrence();
    }

    /**
     * Wrapper method.<p>
     * 
     * @return the result of the wrapped method
     */
    public String getId() {

        return m_type.getId();
    }

    /**
     * Wrapper method.<p>
     * 
     * @return the result of the wrapped method
     */
    public boolean isChoice() {

        return m_type.isChoice();
    }

    /**
     * Wrapper method.<p>
     * 
     * @return the result of the wrapped method 
     */
    public boolean isSimpleType() {

        return m_type.isSimpleType();
    }
}
