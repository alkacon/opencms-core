/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/A_CmsXmlValueTextBase.java,v $
 * Date   : $Date: 2004/11/30 16:04:21 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.xml.types;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.I_CmsXmlDocument;

import java.util.Locale;

import org.dom4j.Element;

/**
 * Base class for XML content value implementations that require only a simple XML plain text node.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 */
public abstract class A_CmsXmlValueTextBase extends A_CmsXmlContentValue {

    /** The String value of the element node. */
    protected String m_stringValue;
    
    /**
     * Default constructor for a xml content type 
     * that initializes some internal values.<p> 
     */
    protected A_CmsXmlValueTextBase() {

        super();
    }
    
    /**
     * Initializes the required members for this XML content value.<p>
     * 
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     */
    protected A_CmsXmlValueTextBase(Element element, Locale locale) {

        super(element, locale);
        m_stringValue = element.getText();
    }

    /**
     * Initializes the schema type descriptor values for this type descriptor.<p>
     * 
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurences of this type according to the XML schema
     * @param maxOccurs maximum number of occurences of this type according to the XML schema
     */
    protected A_CmsXmlValueTextBase(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }
    
    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#setStringValue(java.lang.String)
     */
    public void setStringValue(String value) {

        m_element.clearContent();
        if (CmsStringUtil.isNotEmpty(value)) {
            m_element.addText(value);
        }  
        m_stringValue = value;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlContentValue#getStringValue(CmsObject, I_CmsXmlDocument)
     */
    public String getStringValue(CmsObject cms, I_CmsXmlDocument document) {

        return m_stringValue;
    }    
}
