/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/I_CmsXmlConfiguration.java,v $
 * Date   : $Date: 2004/03/18 15:03:17 $
 * Version: $Revision: 1.8 $
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

import org.dom4j.Element;

/**
 * Each configurable element in OpenCms must implement this interface.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public interface I_CmsXmlConfiguration extends I_CmsConfigurationParameterHandler {
    
    /** The "class" attribute */
    String A_CLASS = "class";

    /** The "count" attribute */
    String A_COUNT = "count";
    
    /** The "destination" attribute */
    String A_DESTINATION = "destination";
    
    /** The "enabled" attibute */
    String A_ENABLED = "enabled";

    /** The "from" attribute */
    String A_FROM = "from";
    
    /** The "icon" attribute */
    String A_ICON = "icon";

    /** The "key" attribute */
    String A_KEY = "key";

    /** The "name" attribute */
    String A_NAME = "name";

    /** The "order" attribute */
    String A_ORDER = "order";
    
    /** The "password" attribute */
    String A_PASSWORD = "password";
    
    /** The "protocol" attribute */
    String A_PROTOCOL = "protocol";
    
    /** The "to" attribute */
    String A_TO = "to";    
    
    /** The "type" attribute */
    String A_TYPE = "type";
    
    /** The "uri" attribute */
    String A_URI = "uri";
    
    /** The "user" attribute */
    String A_USER = "user";
    
    /** Individual export point node */
    String N_EXPORTPOINT = "exportpoint";
    
    /** Export points master node */
    String N_EXPORTPOINTS = "exportpoints";
    
    /** The "param" node name for generic parameters */
    String N_PARAM = "param";   
    
    /** An individual property node */
    String N_PROPERTY = "property";
        
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
     * Returns the name of the DTD file for this XML configuration,
     * e.g. <code>opencms-configuration.dtd</code>.<p>
     * 
     * @return the name of the DTD file for this XML configuration
     * @see #getDtdSystemLocation()
     * @see #getDtdUrlPrefix()
     */
    String getDtdFilename();
    
    /**
     * Returns the internal system location of the DTD file for this XML configuration,
     * e.g. <code>org/opencms/configuration/</code>.<p>
     * 
     * If this is not <code>null</code>, then the DTD is not read through the
     * web URL, but an internal name resolution is added that resolves the 
     * system id of the DTD to 
     * <code>{@link #getDtdSystemLocation()} + {@link #getDtdUrlPrefix()}</code>.<p>
     * 
     * @return the system location of the DTD file for this XML configuration
     * @see #getDtdUrlPrefix()
     * @see #getDtdFilename()
     */
    String getDtdSystemLocation();
    
    /**
     * Returns the external system id prefix of the DTD file for this XML configuration,
     * e.g. <code>http://www.opencms.org/dtd/6.0/</code>.<p>
     * 
     * The full system id for the DTD is calculated lile this:
     * <code>{@link #getDtdSystemLocation()} + {@link #getDtdUrlPrefix()}</code>.<p>
     *  
     * @return the system id prefix of the DTD file for this XML configuration
     * @see #getDtdSystemLocation()
     * @see #getDtdFilename()
     */
    String getDtdUrlPrefix();
    
    /**
     * Returns the name of the XML input file for this configuration,
     * e.g. <code>opencms.xml</code>.<p>
     * 
     * @return the name of the XML input file for this configuration
     */
    String getXmlFileName();
}
