/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/I_CmsXmlContentHandler.java,v $
 * Date   : $Date: 2004/11/01 12:23:49 $
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

package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dom4j.Element;

/**
 * Handles special XML content livetime events, and also provides XML content editor rendering hints.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.5.4
 */
public interface I_CmsXmlContentHandler {

    /** Array of all allowed attribute mapping names. */
    String[] C_ATTRIBUTES = {
        "datereleased",
        "dateexpired"
    };
    
    /** List of all allowed attribute mapping names, for fast lookup. */
    List C_ATTRIBUTES_LIST = Collections.unmodifiableList(Arrays.asList(C_ATTRIBUTES));

    /** Prefix for attribute mappings. */
    String C_MAPTO_ATTRIBUTE = "attribute:";

    /** Prefix for property mappings. */
    String C_MAPTO_PROPERTY = "property:";

    /**
     * Analyzes the "appinfo" node in a XML content definition.<p>
     * 
     * @param appInfoElement the "appinfo" element node to analyze
     * @param contentDefinition the XML content definition that XML content handler belongs to
     * 
     * @throws CmsXmlException if something goes wrong
     */
    void analyzeAppInfo(Element appInfoElement, CmsXmlContentDefinition contentDefinition) throws CmsXmlException;

    /**
     * Freezes this XML content handler.<p>
     *
     * Will be called once after the instance class of the content handler was created and 
     * <code>{@link #analyzeAppInfo(Element, CmsXmlContentDefinition)}</code> has been called once.<p>
     */
    void freeze();

    /**
     * Resolves the element mappings of this XML content class.<p>
     * 
     * @param cms the current OpenCms user context
     * @param content the XML content to resolve the mappings for
     * @param contentDefinition the XML content definition of the XML content
     * 
     * @throws CmsException if something goes wrong
     */
    void resolveElementMappings(CmsObject cms, CmsXmlContent content, CmsXmlContentDefinition contentDefinition)
    throws CmsException;
}