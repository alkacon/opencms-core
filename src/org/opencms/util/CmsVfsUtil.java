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

package org.opencms.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Provides Vfs utility functions.<p>
 *
 * @since 11.0.0
 */
public final class CmsVfsUtil {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsUtil.class);

    /**
     * Hides the public constructor.<p>
     */
    private CmsVfsUtil() {

        // empty
    }

    /**
     * Creates a folder and its parent folders if they don't exist.<p>
     *
     * @param cms the CMS context to use
     * @param rootPath the folder root path
     *
     * @throws CmsException if something goes wrong
     */
    public static void createFolder(CmsObject cms, String rootPath) throws CmsException {

        CmsObject rootCms = OpenCms.initCmsObject(cms);
        rootCms.getRequestContext().setSiteRoot("");
        List<String> parents = new ArrayList<String>();
        String currentPath = rootPath;
        while (currentPath != null) {
            if (rootCms.existsResource(currentPath)) {
                break;
            }
            parents.add(currentPath);
            currentPath = CmsResource.getParentFolder(currentPath);
        }
        parents = Lists.reverse(parents);
        for (String parent : parents) {
            try {
                rootCms.createResource(
                    parent,
                    OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()));
                try {
                    rootCms.unlockResource(parent);
                } catch (CmsException e) {
                    // may happen if parent folder is locked also
                    if (LOG.isInfoEnabled()) {
                        LOG.info(e.getLocalizedMessage(), e);
                    }
                }
            } catch (CmsVfsResourceAlreadyExistsException e) {
                // nop
            }
        }
    }

    /**
     * Checks if the provided resource is a default file.
     * @param cms the context
     * @param resource the resource to check
     * @return true, if the file is an default file. False if the file is no default file or an exception occurred.
     */
    public static boolean isDefaultFile(CmsObject cms, CmsResource resource) {

        if ((null == resource) || resource.isFolder()) {
            return false;
        }
        try {
            CmsResource defaultFile = cms.readDefaultFile(
                CmsResource.getFolderPath(cms.getSitePath(resource)),
                CmsResourceFilter.ALL);
            return (null != defaultFile) && defaultFile.getRootPath().equals(resource.getRootPath());
        } catch (Throwable t) {
            String message = "Failed to check if \""
                + resource.getRootPath()
                + "\" is a default file. Assuming it is not.";
            if (LOG.isDebugEnabled()) {
                LOG.debug(message, t);
            } else {
                LOG.error(message);
            }
        }
        return false;
    }

    /**
     * Reads the property value for the provided name and resource.
     * The locale specific properties are read first, with fallback to the default property.
     *
     * In case the resource is a default file and the property is not found on the file itself,
     * the property is read (again locale specific) on the folder as fallback.
     *
     * @param cms the context
     * @param resource the resource to read the property from
     * @param propertyName the name of the property to read
     * @param locale the locale to read the property in.
     * @return the property value or null, if the property is not set at all or a exception occurred.
     */
    public static String readPropertyValueWithFolderFallbackForDefaultFiles(
        CmsObject cms,

        CmsResource resource,
        String propertyName,
        Locale locale) {

        try {
            List<CmsProperty> resourcePropsList = cms.readPropertyObjects(resource, false);
            Map<String, CmsProperty> resourceProps = CmsProperty.getPropertyMap(resourcePropsList);
            String value = CmsProperty.getLocaleSpecificPropertyValue(resourceProps, propertyName, locale);
            if ((value == null) && isDefaultFile(cms, resource)) {
                try {
                    List<CmsProperty> folderPropsList = cms.readPropertyObjects(
                        CmsResource.getFolderPath(cms.getSitePath(resource)),
                        false);
                    Map<String, CmsProperty> folderProps = CmsProperty.getPropertyMap(folderPropsList);
                    value = CmsProperty.getLocaleSpecificPropertyValue(folderProps, propertyName, locale);
                } catch (Throwable e) {
                    String message = "Failed to read folder property \""
                        + propertyName
                        + "\" for resource \""
                        + resource.getRootPath()
                        + "\".";
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(message, e);
                    } else {
                        LOG.error(message);
                    }
                }
            }
            return value;
        } catch (Throwable e) {
            String message = "Failed to read property \""
                + propertyName
                + "\" for resource \""
                + (null == resource ? "null" : resource.getRootPath())
                + "\".";
            if (LOG.isDebugEnabled()) {
                LOG.debug(message, e);
            } else {
                LOG.error(message);
            }
            return null;
        }
    }

}
