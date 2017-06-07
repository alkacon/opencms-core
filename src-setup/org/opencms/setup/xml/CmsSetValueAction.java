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

package org.opencms.setup.xml;

import org.dom4j.Document;

import com.google.common.base.Objects;

/**
 * Simple XML update action which justs sets a single value.<p>
 *
 * @since 8.0.0
 */
public class CmsSetValueAction extends CmsXmlUpdateAction {

    /** The value to set. */
    private String m_value;

    /**
     * Creates a new instance.<p>
     *
     * @param value the value which should be set by the action
     */
    public CmsSetValueAction(String value) {

        m_value = value;
    }

    /**
     * @see org.opencms.setup.xml.CmsXmlUpdateAction#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

        org.dom4j.Node node = doc.selectSingleNode(xpath);
        if ((node != null) && Objects.equal(node.getText(), m_value)) {
            return false;
        }
        CmsSetupXmlHelper.setValue(doc, xpath, m_value);
        return true;
    }
}
