/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeXmlContent.java,v $
 * Date   : $Date: 2004/08/03 07:19:04 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsXmlContentLoader;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContent;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Resource type descriptor for the type "xmlcontent".<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 5.5
 */
public class CmsResourceTypeXmlContent extends A_CmsResourceType {

    /** Configuration key for the resource type id. */
    public static final String C_CONFIGURATION_RESOURCE_TYPE_ID = "resource.type.id";

    /** Configuration key for the resource type name. */
    public static final String C_CONFIGURATION_RESOURCE_TYPE_NAME = "resource.type.name";

    /** The type id of this resource. */
    private int m_resourceType;

    /** The name of this resource. */
    private String m_resourceTypeName;

    /**
     * @see org.opencms.file.types.A_CmsResourceType#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        if (C_CONFIGURATION_RESOURCE_TYPE_ID.equalsIgnoreCase(paramName)) {
            m_resourceType = Integer.valueOf(paramValue).intValue();
        } else if (C_CONFIGURATION_RESOURCE_TYPE_NAME.equalsIgnoreCase(paramName)) {
            m_resourceTypeName = paramValue.trim();
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getCachePropertyDefault()
     */
    public String getCachePropertyDefault() {

        return "element;locale;";
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#getConfiguration()
     */
    public ExtendedProperties getConfiguration() {

        ExtendedProperties result = new ExtendedProperties();
        result.put(C_CONFIGURATION_RESOURCE_TYPE_ID, new Integer(m_resourceType));
        result.put(C_CONFIGURATION_RESOURCE_TYPE_NAME, m_resourceTypeName);
        return result;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {

        return CmsXmlContentLoader.C_RESOURCE_LOADER_ID;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getTypeId()
     */
    public int getTypeId() {

        return m_resourceType;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#getTypeName()
     */
    public String getTypeName() {

        return m_resourceTypeName;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration()
     */
    public void initConfiguration() throws CmsConfigurationException {

        // configuration must be complete for this resource type
        if ((m_resourceTypeName == null) || (m_resourceType <= 0)) {
            throw new CmsConfigurationException("Not all required configuration parameters available for resource type");
        }
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#isDirectEditable()
     */
    public boolean isDirectEditable() {

        return true;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#writeFile(org.opencms.file.CmsObject, CmsDriverManager, CmsFile)
     */
    public CmsFile writeFile(CmsObject cms, CmsDriverManager driverManager, CmsFile resource) throws CmsException {

        // check if the user has write access and if resource is locked
        // done here so that all the XML operations are not performed if permissions not granted
        driverManager.checkPermissions(cms.getRequestContext(), resource, I_CmsConstants.C_WRITE_ACCESS, true, CmsResourceFilter.ALL);
        // read the xml page, use the encoding set in the property       
        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, resource, false);
        // validate the xml structure before writing the file         
        // an exception will be thrown if the structure is invalid
        xmlContent.validateXmlStructure(new CmsXmlEntityResolver(cms));
        // correct the HTML structure 
        resource = xmlContent.correctXmlStructure(cms);        
        // resolve the file mappings
        xmlContent.resolveElementMappings(cms);        
        // now write the file
        return super.writeFile(cms, driverManager, resource);
    }
}