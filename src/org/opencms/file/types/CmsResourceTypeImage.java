/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeImage.java,v $
 * Date   : $Date: 2005/10/12 14:38:21 $
 * Version: $Revision: 1.12.2.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsException;
import org.opencms.loader.CmsDumpLoader;
import org.opencms.loader.CmsImageLoader;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsSecurityException;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Resource type descriptor for the type "image".<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.12.2.3 $ 
 * 
 * @since 6.0.0 
 */
public class CmsResourceTypeImage extends A_CmsResourceType {

    /** The log object for this class. */
    public static final Log LOG = CmsLog.getLog(CmsResourceTypeImage.class);

    /** Indicates that the static configuration of the resource type has been frozen. */
    private static boolean m_staticFrozen;

    /** The static resource loader id of this resource type. */
    private static int m_staticLoaderId;

    /** The static type id of this resource type. */
    private static int m_staticTypeId;

    /** The type id of this resource type. */
    private static final int RESOURCE_TYPE_ID = 3;

    /** The name of this resource type. */
    private static final String RESOURCE_TYPE_NAME = "image";

    /**
     * Default constructor, used to initialize member variables.<p>
     */
    public CmsResourceTypeImage() {

        super();
        m_typeId = RESOURCE_TYPE_ID;
        m_typeName = RESOURCE_TYPE_NAME;
    }

    /**
     * Returns the static type id of this (default) resource type.<p>
     * 
     * @return the static type id of this (default) resource type
     */
    public static int getStaticTypeId() {

        return m_staticTypeId;
    }

    /**
     * Returns the static type name of this (default) resource type.<p>
     * 
     * @return the static type name of this (default) resource type
     */
    public static String getStaticTypeName() {

        return RESOURCE_TYPE_NAME;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#createResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, java.lang.String, byte[], java.util.List)
     */
    public CmsResource createResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        byte[] content,
        List properties) throws CmsException {

        if (CmsImageLoader.isEnabled()) {
            properties = getImageProperties(content, properties, cms.getRequestContext().addSiteRoot(resourcename));
        }
        return super.createResource(cms, securityManager, resourcename, content, properties);
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    public int getLoaderId() {

        return m_staticLoaderId;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#importResource(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, java.lang.String, org.opencms.file.CmsResource, byte[], java.util.List)
     */
    public CmsResource importResource(
        CmsObject cms,
        CmsSecurityManager securityManager,
        String resourcename,
        CmsResource resource,
        byte[] content,
        List properties) throws CmsException {

        if (CmsImageLoader.isEnabled()) {
            if (content != null) {
                // siblings have null content in import
                properties = getImageProperties(content, properties, resource.getRootPath());
            }
        }
        return super.importResource(cms, securityManager, resourcename, resource, content, properties);
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String, String)
     */
    public void initConfiguration(String name, String id, String className) throws CmsConfigurationException {

        if ((OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) && m_staticFrozen) {
            // configuration already frozen
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_CONFIG_FROZEN_3,
                this.getClass().getName(),
                getStaticTypeName(),
                new Integer(getStaticTypeId())));
        }

        if (!RESOURCE_TYPE_NAME.equals(name)) {
            // default resource type MUST have default name
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_INVALID_RESTYPE_CONFIG_NAME_3,
                this.getClass().getName(),
                RESOURCE_TYPE_NAME,
                name));
        }

        // freeze the configuration
        m_staticFrozen = true;

        super.initConfiguration(RESOURCE_TYPE_NAME, id, className);
        // set static members with values from the configuration        
        m_staticTypeId = m_typeId;
        m_staticLoaderId = CmsImageLoader.isEnabled() ? CmsImageLoader.RESOURCE_LOADER_ID_IMAGE_LOADER
        : CmsDumpLoader.RESOURCE_LOADER_ID;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#writeFile(org.opencms.file.CmsObject, org.opencms.db.CmsSecurityManager, org.opencms.file.CmsFile)
     */
    public CmsFile writeFile(CmsObject cms, CmsSecurityManager securityManager, CmsFile resource)
    throws CmsException, CmsVfsException, CmsSecurityException {

        if (CmsImageLoader.isEnabled()) {
            // check if the user has write access and if resource is locked
            // done here so that not image operations are performed in case no write access is granted
            securityManager.checkPermissions(
                cms.getRequestContext(),
                resource,
                CmsPermissionSet.ACCESS_WRITE,
                true,
                CmsResourceFilter.ALL);

            List properties = getImageProperties(resource.getContents(), null, resource.getRootPath());
            if (properties != null) {
                writePropertyObjects(cms, securityManager, resource, properties);
            }
        }
        return super.writeFile(cms, securityManager, resource);
    }

    /**
     * Calculate the image dimensions from the given image and update the given list of properties 
     * with a value for <code>{@link CmsPropertyDefinition#PROPERTY_IMAGE_SIZE}</code> that 
     * contains the calculated image dimensions.<p> 
     * 
     * @param content the image to calculate the dimensions for
     * @param properties the list of properties to update 
     * @param rootPath the root path if the resource (for error logging)
     * 
     * @return the updated property list with a value for 
     *      <code>{@link CmsPropertyDefinition#PROPERTY_IMAGE_SIZE}</code> that contains the calculated image dimensions
     */
    protected List getImageProperties(byte[] content, List properties, String rootPath) {

        CmsImageScaler scaler = new CmsImageScaler(content, rootPath);
        if (!scaler.isValid()) {
            // error calculating image dimensions
            return properties;
        }

        CmsProperty p = new CmsProperty(CmsPropertyDefinition.PROPERTY_IMAGE_SIZE, null, scaler.toString());
        // create the new property list if required (don't modify the original List)
        List result = new ArrayList();
        if ((properties != null) && (properties.size() > 0)) {
            result.addAll(properties);
            result.remove(p);
        }
        // add the updated property
        result.add(p);
        return result;
    }
}