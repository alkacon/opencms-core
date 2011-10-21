/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.inherited;

import org.opencms.db.CmsPublishedResource;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CmsContainerConfigurationCache implements I_CmsContainerConfigurationCache {

    public static final String FILE_NAME = ".container-config";

    protected boolean m_initialized;

    private CmsObject m_cms;

    private Map<String, CmsUUID> m_needToUpdate = new HashMap<String, CmsUUID>();

    private Map<String, CmsContainerConfigurationGroup> m_pathCache = new HashMap<String, CmsContainerConfigurationGroup>();

    public CmsContainerConfigurationCache(CmsObject cms)
    throws CmsException {

        m_cms = OpenCms.initCmsObject(cms);
    }

    public CmsContainerConfiguration getContainerConfiguration(String rootPath, String name, Locale locale) {

        return null;
    }

    public void initialize() {

        try {
            List<CmsResource> configurationResources = m_cms.readResources(
                "/",
                CmsResourceFilter.DEFAULT.addRequireType(safeGetType()),
                true);
            for (CmsResource configResource : configurationResources) {
                update(configResource);
            }
        } catch (CmsException e) {

        }
    }

    public void remove(CmsPublishedResource resource) {

        remove(resource.getStructureId(), resource.getRootPath(), resource.getType());
    }

    public void remove(CmsResource resource) {

        remove(resource.getStructureId(), resource.getRootPath(), resource.getTypeId());
    }

    public void update(CmsPublishedResource resource) {

        update(resource.getStructureId(), resource.getRootPath(), resource.getType(), resource.getState());
    }

    public void update(CmsResource resource) {

        update(resource.getStructureId(), resource.getRootPath(), resource.getTypeId(), resource.getState());
    }

    protected String getBasePath(String rootPath) {

        if (rootPath.endsWith("/" + FILE_NAME)) {
            return rootPath.replaceAll("/" + FILE_NAME + "$", "/");
        } else {
            throw new IllegalArgumentException();
        }
    }

    protected boolean isContainerConfiguration(String rootPath, int type) {

        try {
            int expectedId = OpenCms.getResourceManager().getResourceType("inheritconfig").getTypeId();
            return !CmsResource.isTemporaryFileName(rootPath)
                && rootPath.endsWith("/" + FILE_NAME)
                && (type == expectedId);
        } catch (CmsLoaderException e) {
            return false;
        }
    }

    protected synchronized void remove(CmsUUID structureId, String rootPath, int type) {

        if (!isContainerConfiguration(rootPath, type)) {
            return;
        }
        String basePath = getBasePath(rootPath);
        m_pathCache.remove(basePath);
    }

    protected int safeGetType() {

        try {
            return OpenCms.getResourceManager().getResourceType("inheritconfig").getTypeId();
        } catch (CmsLoaderException e) {
            return -1;
        }
    }

    protected synchronized void update(CmsUUID structureId, String rootPath, int type, CmsResourceState state) {

        if (!isContainerConfiguration(rootPath, type)) {
            return;
        }
        String basePath = getBasePath(rootPath);
        m_pathCache.remove(basePath);
        m_needToUpdate.put(rootPath, structureId);
    }

}
