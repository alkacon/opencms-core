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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;

/**
 * Base class for context sensitive custom beans that supports creation via {@link CmsJspStandardContextBean#getBean(String className)}.
 *
 * It is the suitable base class for custom beans that need access to the OpenCms context.
 *
 * @since 11.0
 */
public abstract class A_CmsJspCustomContextBean {

    /** The standard context. */
    private CmsJspStandardContextBean m_context;
    /** The cms object from the standard context. */
    private CmsObject m_cms;

    /**
     * Returns the cms object for the current context.
     * @return the cms object for the current context.
     */
    public CmsObject getCmsObject() {

        return m_cms;
    }

    /**
     * Set the context for the bean.
     * @param context the context to set.
     */
    public void setContext(CmsJspStandardContextBean context) {

        m_context = context;
        m_cms = m_context.getVfs().getCmsObject();
    }

    /**
     * Returns the standard context.
     * @return the standard context.
     */
    protected CmsJspStandardContextBean getStandardContextBean() {

        return m_context;
    }

    /**
     * Convert the input to a CmsResource using the current context.
     *
     * @see CmsJspElFunctions#convertResource(CmsObject, Object)
     *
     * @param input the object to convert to a resource, e.g., a path, a structure id, an access wrapper ...
     * @return the resource for the input.
     * @throws CmsException if resource conversion fails.
     */
    protected CmsResource toResource(Object input) throws CmsException {

        return CmsJspElFunctions.convertRawResource(getCmsObject(), input);
    }

    /**
     * Converts the input (typically a specification of an XML file) to an XML document.
     *
     * If the input already is an XML document, it is returned.
     * Otherwise the method assumes the input specifies an XML file
     * and tries to determine and unmarshal that file.nd unmarshal that file
     *
     * To determine the file {@link #toResource(Object)} is used.
     *
     * @param input the object to be converted to an XML document.
     * @return the XML document specified by the input.
     * @throws CmsException if converting the input to a XML document fails.
     */
    protected I_CmsXmlDocument toXml(Object input) throws CmsException {

        if (input instanceof CmsJspContentAccessBean) {
            return ((CmsJspContentAccessBean)input).getRawContent();
        }
        if (input instanceof I_CmsXmlDocument) {
            return (I_CmsXmlDocument)input;
        } else {
            CmsResource res = toResource(input);
            return CmsXmlContentFactory.unmarshal(getCmsObject(), getCmsObject().readFile(res));
        }
    }
}
