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

import org.opencms.acacia.shared.I_EntityAttribute;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

/**
 * Exportable wrapper class for an entity attribute.<p> 
 */
@Export
@ExportPackage("acacia")
public class EntityAttributeWrapper implements Exportable {

    /** The wrapped attribute. */
    private I_EntityAttribute m_attribute;

    /** 
     * Default constructor.<p>
     */
    public EntityAttributeWrapper() {

    }

    /** 
     * Constructor.<p>
     * 
     * @param attribute the attribute to wrap
     */
    public EntityAttributeWrapper(I_EntityAttribute attribute) {

        m_attribute = attribute;
    }

    /**
     * Wrapper method.<p>
     *  
     * @return the result of the wrapped method  
     */
    public String getAttributeName() {

        return m_attribute.getAttributeName();
    }

    /**
     * Wrapper method.<p>
     *  
     * @return the result of the wrapped method  
     */
    public EntityWrapper getComplexValue() {

        return new EntityWrapper(m_attribute.getComplexValue());
    }

    /**
     * Wrapper method.<p>
     *  
     * @return the result of the wrapped method  
     */
    public EntityWrapper[] getComplexValues() {

        return WrapperUtils.arrayFromEntityList(m_attribute.getComplexValues());
    }

    /**
     * Wrapper method.<p>
     * 
     * @return the result of the wrapped method 
     */
    public String getSimpleValue() {

        return m_attribute.getSimpleValue();
    }

    /**
     * Wrapper method.<p>
     * 
     * @return the result of the wrapped method 
     */
    public String[] getSimpleValues() {

        return WrapperUtils.arrayFromStringList(m_attribute.getSimpleValues());
    }

    /**
     * Wrapper method.<p>
     * 
     * @return the result of the wrapped method 
     */
    public int getValueCount() {

        return m_attribute.getValueCount();
    }

    /**
     * Wrapper method.<p>
     * 
     * @return the result of the wrapped method
     */
    public boolean isComplexValue() {

        return m_attribute.isComplexValue();
    }

    /**
     * Wrapper method.<p>
     * 
     * @return the result of the wrapped method 
     */
    public boolean isSimpleValue() {

        return m_attribute.isSimpleValue();
    }

    /**
     * Wrapper method.<p>
     * 
     * @return the result of the wrapped method 
     */
    public boolean isSingleValue() {

        return m_attribute.isSingleValue();
    }

    /**
     * Sets the wrapped attribute.<p>
     * 
     * @param attribute the attribute to wrap 
     */
    public void setAttribute(I_EntityAttribute attribute) {

        m_attribute = attribute;
    }
}
