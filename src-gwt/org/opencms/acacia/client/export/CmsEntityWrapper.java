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

import org.opencms.acacia.shared.CmsEntity;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

/**
 * Exportable wrapper for entity instances.<p>
 */
@Export
@ExportPackage(value = "acacia")
public class CmsEntityWrapper implements Exportable {

    /** The entity wrapped by this wrapper. */
    private CmsEntity m_entity;

    /**
     * Default constructor.<p>
     */
    public CmsEntityWrapper() {

    }

    /**
     * Wrapper constructor.<p>
     * 
     * @param entity the entity to be wrapped 
     */
    public CmsEntityWrapper(CmsEntity entity) {

        m_entity = entity;
    }

    /**
     * Wrapper method.<p>
     * 
     * @param attributeName argument for the wrapped method 
     * @param value argument for the wrapped method 
     */
    public void addAttributeValueEntity(String attributeName, CmsEntityWrapper value) {

        m_entity.addAttributeValue(attributeName, value.getEntity());
    }

    /**
     * Wrapper method.<p>
     * 
     * @param attributeName argument for the wrapped method 
     * @param value argument for the wrapped method 
     */
    public void addAttributeValueString(String attributeName, String value) {

        m_entity.addAttributeValue(attributeName, value);
    }

    /**
     * Wrapper method.<p>
     * 
     * @param attributeName parameter for the wrapped method 
     * @return the result of the wrapped method 
     */
    public CmsEntityAttributeWrapper getAttribute(String attributeName) {

        return new CmsEntityAttributeWrapper(m_entity.getAttribute(attributeName));
    }

    /**
     * Wrapper method.<p>
     * 
     * @return the result of the wrapped method 
     */
    public CmsEntityAttributeWrapper[] getAttributes() {

        return CmsWrapperUtils.arrayFromEntityAttributeList(m_entity.getAttributes());
    }

    /** 
     * Gets the wrapped entity.<p>
     * 
     * @return the wrapped entity  
     */
    public CmsEntity getEntity() {

        return m_entity;
    }

    /**
     * Wrapper method.<p>
     * 
     * @return the result of the wrapped method 
     */
    public String getId() {

        return m_entity.getId();
    }

    /**
     * Wrapper method.<p>
     * 
     * @return the result of the wrapped method 
     */
    public String getTypeName() {

        return m_entity.getTypeName();
    }

    /**
     * Wrapper method.<p>
     * 
     * @param attributeName parameter for the wrapped method 
     * @return the result of the wrapped method 
     */
    public boolean hasAttribute(String attributeName) {

        return m_entity.hasAttribute(attributeName);
    }

    /**
     * Wrapper method.<p>
     * 
     * @param attributeName parameter for the wrapped method 
     * @param value parameter for the wrapped method 
     * @param index parameter for the wrapped method 
     */
    public void insertAttributeValueEntity(String attributeName, CmsEntityWrapper value, int index) {

        m_entity.insertAttributeValue(attributeName, value.getEntity(), index);
    }

    /**
     * Wrapper method.<p>
     * 
     * @param attributeName parameter for the wrapped method 
     * @param value parameter for the wrapped method 
     * @param index parameter for the wrapped method 
     */
    public void insertAttributeValueString(String attributeName, String value, int index) {

        m_entity.insertAttributeValue(attributeName, value, index);
    }

    /**
     * Wrapper method.<p>
     * 
     * @param attributeName parameter for the wrapped method 
     */
    public void removeAttribute(String attributeName) {

        m_entity.removeAttribute(attributeName);
    }

    /**
     * Wrapper method.<p>
     * 
     * @param attributeName parameter for the wrapped method 
     */
    public void removeAttributeSilent(String attributeName) {

        m_entity.removeAttributeSilent(attributeName);
    }

    /**
     * Wrapper method.<p>
     * 
     * @param attributeName parameter for the wrapped method 
     * @param index parameter for the wrapped method 
     */
    public void removeAttributeValue(String attributeName, int index) {

        m_entity.removeAttributeValue(attributeName, index);
    }

    /**
     * Wrapper method.<p>
     * 
     * @param attributeName parameter for the wrapped method 
     * @param value parameter for the wrapped method 
     */
    public void setAttributeValueEntity(String attributeName, CmsEntityWrapper value) {

        m_entity.setAttributeValue(attributeName, value.getEntity());
    }

    /**
    public void setAttributeValueEntity(String attributeName, CmsEntityWrapper value, int index) {

        m_entity.setAttributeValue(attributeName, value.getEntity(), index);
    }

    public void setAttributeValueString(String attributeName, String value) {

        m_entity.setAttributeValue(attributeName, value);
    }

    /**
     * Wrapper method.<p>
     * 
     * @param attributeName parameter for the wrapped method
     * @param value parameter for the wrapped method
     * @param index parameter for the wrapped method
     */
    public void setAttributeValueString(String attributeName, String value, int index) {

        m_entity.setAttributeValue(attributeName, value, index);
    }

    /**
     * Sets the wrapped entity.<p>
     * 
     * @param entity the entity to wrap 
     */
    public void setEntity(CmsEntity entity) {

        m_entity = entity;
    }

    /**
     * Wrapper method.<p>
     * 
     * @return the result of the wrapped method 
     */
    public String toJSON() {

        return m_entity.toJSON();
    }
}
