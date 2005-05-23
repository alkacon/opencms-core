/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/I_CmsXmlContentValue.java,v $
 * Date   : $Date: 2005/05/23 09:36:51 $
 * Version: $Revision: 1.11 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.xml.I_CmsXmlDocument;

import java.util.Locale;

import org.dom4j.Element;

/**
 * Provides access to the value of a specific XML content node.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.11 $
 * @since 5.5.0
 */
public interface I_CmsXmlContentValue extends I_CmsXmlSchemaType {

    /**
     * Returns the XML content instance this value belongs to.<p>
     * 
     * @return the XML content instance this value belongs to
     */
    I_CmsXmlDocument getDocument();

    /**
     * Returns the original XML element of this XML content value.<p>
     * 
     * @return the original XML element of this XML content value
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
     * Returns the path of this XML content value in the source document.<p>
     * 
     * @return the path of this XML content value in the source document
     */
    String getPath();

    /**
     * Returns the value of this XML content node as a plain text String.<p>
     * 
     * Plain text in this context means a pure textual representation
     * of the content (i.e. without html tags).
     * The plain text may be <code>null</code>, too, if there is no sound or useful
     * textual representation (i.e. color values).<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * 
     * @return the value of this XML content node as a plain text String
     */
    String getPlainText(CmsObject cms);

    /**
     * Returns the value of this XML content node as a String.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * 
     * @return the value of this XML content node as a String
     */
    String getStringValue(CmsObject cms);

    /**
     * Sets the provided String as value of this XML content node.<p>  
     * 
     * This method does provide processing of the content based on the
     * users current OpenCms context. This can be used e.g. for link 
     * extraction and replacement in the content.<p>
     * 
     * @param cms an initialized instance of a CmsObject
     * @param value the value to set 
     * 
     */
    void setStringValue(CmsObject cms, String value);
}