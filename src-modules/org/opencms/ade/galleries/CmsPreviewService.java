/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/Attic/CmsPreviewService.java,v $
 * Date   : $Date: 2010/06/10 08:45:04 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.galleries;

import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
import org.opencms.ade.galleries.shared.rpc.I_CmsPreviewService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.sitemap.CmsSitemapEntry;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Handles all RPC services related to the gallery preview dialog.<p>
 * 
 * @author Polina Smagina
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public class CmsPreviewService extends CmsGwtService implements I_CmsPreviewService {

    /** Serialization uid. */
    private static final long serialVersionUID = -8175522641937277445L;

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsPreviewService#getResourceInfo(java.lang.String)
     */
    public CmsResourceInfoBean getResourceInfo(String resourcePath) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsResourceInfoBean resInfo = new CmsResourceInfoBean();
        try {
            CmsSitemapEntry sitemapEntry = OpenCms.getSitemapManager().getEntryForUri(cms, resourcePath);
            CmsResource resource = cms.readResource(sitemapEntry.getResourceId());
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource.getTypeId());

            resInfo.setTitle(resource.getName());
            resInfo.setDescription(CmsWorkplaceMessages.getResourceTypeName(
                OpenCms.getWorkplaceManager().getWorkplaceLocale(cms),
                type.getTypeName()));
            resInfo.setResourcePath(resourcePath);
            resInfo.setSize(resource.getLength() / 1024 + " kb");

            // reading default explorer-type properties
            CmsExplorerTypeSettings setting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type.getTypeName());
            List<String> properties = setting.getProperties();
            String reference = setting.getReference();

            // looking up properties from referenced explorer types if properties list is empty

            while ((properties.size() == 0) && !CmsStringUtil.isEmptyOrWhitespaceOnly(reference)) {
                setting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(reference);
                properties = setting.getProperties();
                reference = setting.getReference();
            }
            Map<String, String> props = new LinkedHashMap<String, String>();
            Iterator<String> propIt = properties.iterator();
            while (propIt.hasNext()) {
                String propertyName = propIt.next();
                CmsProperty property = getCmsObject().readPropertyObject(resource, propertyName, false);
                if (!property.isNullProperty()) {
                    props.put(property.getName(), property.getValue());
                } else {
                    props.put(propertyName, null);
                }
            }
            resInfo.setProperties(props);
        } catch (CmsException e) {
            error(e);
        }
        return resInfo;
    }

    /**
     * @see org.opencms.ade.galleries.shared.rpc.I_CmsPreviewService#updateProperties(java.lang.String, java.util.Map)
     */
    public CmsResourceInfoBean updateProperties(String resourcePath, Map<String, String> properties)
    throws CmsRpcException {

        CmsResource resource;
        CmsObject cms = getCmsObject();
        try {
            resource = cms.readResource(resourcePath);

            if (properties != null) {
                for (Entry<String, String> entry : properties.entrySet()) {
                    String propertyName = entry.getKey();
                    String propertyValue = entry.getValue();
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(propertyValue)) {
                        propertyValue = "";
                    }
                    try {
                        CmsProperty currentProperty = cms.readPropertyObject(resource, propertyName, false);
                        // detect if property is a null property or not
                        if (currentProperty.isNullProperty()) {
                            // create new property object and set key and value
                            currentProperty = new CmsProperty();
                            currentProperty.setName(propertyName);
                            if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                                // set structure value
                                currentProperty.setStructureValue(propertyValue);
                                currentProperty.setResourceValue(null);
                            } else {
                                // set resource value
                                currentProperty.setStructureValue(null);
                                currentProperty.setResourceValue(propertyValue);
                            }
                        } else if (currentProperty.getStructureValue() != null) {
                            // structure value has to be updated
                            currentProperty.setStructureValue(propertyValue);
                            currentProperty.setResourceValue(null);
                        } else {
                            // resource value has to be updated
                            currentProperty.setStructureValue(null);
                            currentProperty.setResourceValue(propertyValue);
                        }
                        CmsLock lock = getCmsObject().getLock(resource);
                        if (lock.isUnlocked()) {
                            // lock resource before operation
                            cms.lockResource(resourcePath);
                        }
                        // write the property to the resource
                        cms.writePropertyObject(resourcePath, currentProperty);
                        // unlock the resource
                        cms.unlockResource(resourcePath);
                    } catch (CmsException e) {
                        // writing the property failed, log error
                        log(e.getLocalizedMessage());
                    }
                }
            }
        } catch (CmsException e1) {
            error(e1);
        }
        return getResourceInfo(resourcePath);
    }

}