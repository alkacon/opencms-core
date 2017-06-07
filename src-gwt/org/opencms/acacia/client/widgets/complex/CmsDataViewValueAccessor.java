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

package org.opencms.acacia.client.widgets.complex;

import org.opencms.acacia.client.CmsAttributeHandler;
import org.opencms.acacia.client.I_CmsAttributeHandler;
import org.opencms.acacia.client.css.I_CmsLayoutBundle;
import org.opencms.acacia.client.entity.CmsEntityBackend;
import org.opencms.acacia.shared.CmsEntity;
import org.opencms.acacia.shared.CmsEntityAttribute;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsDataViewConstants;

import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Class responsible for reading data view values from the editor, and writing them back to the editor.<p>
 */
public class CmsDataViewValueAccessor {

    /** The id of this accessor. */
    private String m_id = "" + Math.random();

    /** Entity which the data view widget is currently displaying. */
    private CmsEntity m_entity;

    /** The attribute handler. */
    private I_CmsAttributeHandler m_handler;

    /** The value index. */
    private int m_index;

    /** The widget instance. */
    private Widget m_widget;

    /**
     * Creates a new instance.<p>
     *
     * @param entity the entity
     * @param handler the attribute handler
     * @param index the value index
     */
    public CmsDataViewValueAccessor(CmsEntity entity, I_CmsAttributeHandler handler, int index) {
        m_entity = entity;
        m_handler = handler;
        m_index = index;
    }

    /**
     * Gets the id of this accessor.<p>
     *
     * @return the id
     */
    public String getId() {

        return m_id;
    }

    /**
     * Gets the current value in the editor.<p>
     *
     * @return the value in the editor
     */
    public CmsDataViewValue getValue() {

        return new CmsDataViewValue(
            getString(CmsDataViewConstants.VALUE_ID),
            getString(CmsDataViewConstants.VALUE_TITLE),
            getString(CmsDataViewConstants.VALUE_DESCRIPTION),
            getString(CmsDataViewConstants.VALUE_DATA));

    }

    /**
     * Replaces the value in the editor with a list of other values.<p>
     *
     * @param replacementValues the list of replacement values
     */
    public void replaceValue(List<CmsDataViewValue> replacementValues) {

        CmsAttributeHandler handler = (CmsAttributeHandler)m_handler;
        Element parent = CmsDomUtil.getAncestor(
            m_widget.getElement(),
            I_CmsLayoutBundle.INSTANCE.form().attributeValue()).getParentElement();
        NodeList<Node> siblings = parent.getChildNodes();
        for (int j = 0; j < siblings.getLength(); j++) {
            Node node = siblings.getItem(j);
            if (node instanceof Element) {
                Element elem = (Element)node;
                if (elem.isOrHasChild(m_widget.getElement())) {
                    m_index = j;
                    break;
                }
            }
        }
        Panel container = handler.removeAttributeValueAndReturnPrevParent(m_index, true);
        int i = m_index;
        for (CmsDataViewValue value : replacementValues) {
            CmsEntity entity = CmsEntityBackend.getInstance().createEntity(null, m_entity.getTypeName());

            writeValueToEntity(value, entity);

            // handler.addNewAttributeValue(entity);
            handler.insertNewAttributeValue(entity, i, container);
            i += 1;
        }

        handler.updateButtonVisisbility();
    }

    /**
     * Sets the widget instance.<p>
     *
     * @param cmsDataViewClientWidget the widget instance
     */
    public void setWidget(CmsDataViewClientWidget cmsDataViewClientWidget) {

        m_widget = cmsDataViewClientWidget;
    }

    /**
     * Gets a string-valued attribute from this accessor's entity.<p>
     *
     * @param valueKey the key
     * @return the attribute value
     */
    private String getString(String valueKey) {

        String realKey = m_entity.getTypeName() + "/" + valueKey;
        CmsEntityAttribute attr = m_entity.getAttribute(realKey);
        if (attr == null) {
            return "";
        } else {
            return attr.getSimpleValue();
        }
    }

    /**
     * Writes the value to the given entity.<p>
     *
     * @param val the value
     * @param entity the entity
     */
    private void writeValueToEntity(CmsDataViewValue val, CmsEntity entity) {

        String prefix = entity.getTypeName() + "/";
        entity.setAttributeValue(prefix + CmsDataViewConstants.VALUE_ID, val.getId());
        entity.setAttributeValue(prefix + CmsDataViewConstants.VALUE_TITLE, val.getTitle());
        entity.setAttributeValue(prefix + CmsDataViewConstants.VALUE_DESCRIPTION, val.getDescription());
        entity.setAttributeValue(prefix + CmsDataViewConstants.VALUE_DATA, val.getData());
    }

}
