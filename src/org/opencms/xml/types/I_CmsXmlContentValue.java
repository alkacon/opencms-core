/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/I_CmsXmlContentValue.java,v $
 * Date   : $Date: 2004/11/30 14:23:51 $
 * Version: $Revision: 1.5 $
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
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;

import java.util.Locale;

import org.dom4j.Element;

/**
 * Provides access to the value of a specific XML content node.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.5 $
 * @since 5.5.0
 */
public interface I_CmsXmlContentValue extends I_CmsXmlSchemaType {

    /**
     * Returns the original XML element of this XML content node.<p>
     * 
     * @return the original XML element of this XML content node
     */
    Element getElement();

    /**
     * Returns the node index of this XML content value in the source XML document, 
     * starting with 0.<p>
     * 
     * This is usefull in case there are more then one elements 
     * with the same XML node name in the source XML document.<p> 
     * 
     * @return the index of this XML content node in the source document
     */
    int getIndex();

    /**
     * Returns the locale of this XML content value was generated for.<p>
     * 
     * @return the locale of this XML content value was generated for
     */
    Locale getLocale();

    /**
     * Returns the value of this XML content node as a plain text String.<p>
     * Plain text in this context means a pure textual representation
     * of the content (i.e. without html tags).
     * The plain text may be <code>null</code>, too, if there is no sound or useful
     * textual representation (i.e. color values).
     * 
     * @param cms an initialized instance of a CmsObject
     * @param document the XML document this value belongs to
     * 
     * @return the value of this XML content node as a plain text String
     * 
     * @throws CmsXmlException if something goes wrong
     */
    String getPlainText(CmsObject cms, I_CmsXmlDocument document) throws CmsXmlException;

    /**
     * Returns the value of this XML content node as a String.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param document the XML document this value belongs to
     * 
     * @return the value of this XML content node as a String
     * 
     * @throws CmsXmlException if something goes wrong
     */
    String getStringValue(CmsObject cms, I_CmsXmlDocument document) throws CmsXmlException;

    /**
     * Sets the provided String as value of this XML content node.<p>  
     * 
     * This method does does provide processing of the content based on the
     * users current OpenCms context. This can be used e.g. for link 
     * extraction and replacement in the content.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param document the XML document this value belongs to
     * @param value the value to set
     * 
     * @throws CmsXmlException if something goes wrong
     * 
     * @see #setStringValue(String)
     */
    void setStringValue(CmsObject cms, I_CmsXmlDocument document, String value) throws CmsXmlException;

    /**
     * Sets the provided String as value of this XML content node.<p>  
     * 
     * This method does not provide special processing of the content based on the
     * users current OpenCms context.<p>
     * 
     * @param value the value to set
     * 
     * @throws CmsXmlException if something goes wrong
     * 
     * @see #setStringValue(CmsObject, I_CmsXmlDocument, String)
     */
    void setStringValue(String value) throws CmsXmlException;
}