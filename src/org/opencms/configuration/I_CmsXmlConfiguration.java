/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/I_CmsXmlConfiguration.java,v $
 * Date   : $Date: 2004/03/08 07:29:48 $
 * Version: $Revision: 1.4 $
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

package org.opencms.configuration;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;

import org.dom4j.Element;

/**
 * Each configurable element in OpenCms must implement this interface.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public interface I_CmsXmlConfiguration extends ObjectCreationFactory, I_CmsConfigurationParameterHandler {
    
    /** The "class" attribute */
    String A_CLASS = "class";
    
    /** The "destination" attribute */
    String A_DESTINATION = "destination";
    
    /** The "enabled" attibute */
    String A_ENABLED = "enabled";

    /** The "from" attribute */
    String A_FROM = "from";

    /** The "key" attribute */
    String A_KEY = "key";

    /** The "name" attribute */
    String A_NAME = "name";

    /** The "order" attribute */
    String A_ORDER = "order";

    /** The "to" attribute */
    String A_TO = "to";    
    
    /** The "type" attribute */
    String A_TYPE = "type";
    
    /** The "uri" attribute */
    String A_URI = "uri";
    
    /** Individual export point node */
    String N_EXPORTPOINT = "exportpoint";
    
    /** Export points master node */
    String N_EXPORTPOINTS = "exportpoints";
    
    /** The "param" node name for generic parameters */
    String N_PARAM = "param";   
        
    /** An individual resource node */
    String N_RESOURCE = "resource";
    
    /** Individual view node */
    String N_VIEW = "view";
    
    /** Workplace views master node */
    String N_VIEWS = "views";
    
    /**
     * Digests an XML node and creates an instance of this configurable class.<p>
     * 
     * @param digester the digester to use
     */
    void addXmlDigesterRules(Digester digester);

    /**
     * Generates the XML element for this configurable class.<p> 
     * 
     * @param parent the parent element in the XML tree
     * @return the XML element for this configurable class
     */
    Element generateXml(Element parent);
    
    /**
     * Usually called after the digester factory object creation method is called.<p>  
     * 
     * @see ObjectCreationFactory#createObject(org.xml.sax.Attributes)
     */
    void initialize();
}
