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

package org.opencms.module;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.importexport.CmsImportVersion10;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsPath;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Module data read from a module zip file.<p>
 */
public class CmsModuleImportData {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleImportData.class);

    /** The CMS context. */
    private CmsObject m_cms;

    /** The map of conflicting ids (keys are structure ids from the manifest, values are structure ids from VFS). */
    private Map<CmsUUID, CmsUUID> m_conflictingIds = new HashMap<>();

    /** The module metadata. */
    private CmsModule m_module;

    /** Online CMS object. */
    private CmsObject m_onlineCms;

    /** The list of resource data for each entry in the manifest. */
    private List<CmsResourceImportData> m_resources = new ArrayList<>();

    /**
     * Adds the information for a single resource.<p>
     *
     * @param resourceData the information for a single resource
     */
    public void addResource(CmsResourceImportData resourceData) {

        m_resources.add(resourceData);
    }

    /**
     * Checks if the installed module is updatable with the version from the import zip file.<p>
     *
     * @param cms the current CMS context
     *
     * @return true if the module is updatable
     */
    public boolean checkUpdatable(CmsObject cms) {

        CmsModule newModule = getModule();
        LOG.info("Checking if module " + newModule.getName() + " is updateable");
        String exportVersion = newModule.getExportVersion();
        CmsModule installedModule = OpenCms.getModuleManager().getModule(getModule().getName());
        if (!CmsModuleUpdater.checkCompatibleModuleResources(installedModule, newModule)) {
            LOG.info("Module is not updateable because of incompatible module resources. ");
            return false;
        }

        if ((exportVersion == null) || !exportVersion.equals("" + CmsImportVersion10.IMPORT_VERSION10)) {
            LOG.info("Module is not updateable because the export version is not 10.");
            return false;
        }

        try {
            Map<CmsUUID, CmsResourceImportData> importResourcesById = new HashMap<>();
            Map<CmsPath, CmsResourceImportData> importResourcesByPath = new HashMap<>();
            Map<String, CmsUUID> structureIdsByParentIdAndName = new HashMap<>();
            Map<CmsUUID, Map<String, CmsUUID>> parentFolderMaps = new HashMap<>();
            for (CmsResourceImportData resData : getResourceData()) {
                if (resData.hasStructureId()) {
                    importResourcesById.put(resData.getResource().getStructureId(), resData);
                }
                importResourcesByPath.put(new CmsPath(resData.getPath()), resData);
            }
            for (CmsResourceImportData resData : getResourceData()) {
                String parentFolderStr = CmsResource.getParentFolder(resData.getPath());
                if (parentFolderStr != null) {
                    CmsResourceImportData parent = importResourcesByPath.get(new CmsPath(parentFolderStr));
                    if ((parent != null) && parent.hasStructureId()) {
                        parentFolderMaps.put(parent.getResource().getStructureId(), new HashMap<>());
                        String key = parent.getResource().getStructureId() + "/" + resData.getResource().getName();
                        if (resData.hasStructureId()) {
                            structureIdsByParentIdAndName.put(key, resData.getResource().getStructureId());
                        }
                    }
                }
            }

            cms = getCms();
            CmsObject onlineCms = OpenCms.initCmsObject(cms);
            m_onlineCms = onlineCms;
            CmsProject online = cms.readProject(CmsProject.ONLINE_PROJECT_NAME);
            onlineCms.getRequestContext().setCurrentProject(online);

            // Reject imports which have an entry with (parentId=A,name=B,structureId=C1) if there is a VFS entry with (parentId=A,name=B,structureId=C2), unless
            // C1 does not occur in the VFS and C2 does not occur in the import.
            for (CmsUUID parentId : parentFolderMaps.keySet()) {
                for (CmsObject cmsToRead : Arrays.asList(cms, onlineCms)) {
                    try {
                        CmsResource parent = cmsToRead.readResource(parentId, CmsResourceFilter.IGNORE_EXPIRATION);
                        List<CmsResource> children = cmsToRead.readResources(
                            parent,
                            CmsResourceFilter.IGNORE_EXPIRATION,
                            false);
                        for (CmsResource child : children) {
                            String key = parent.getStructureId() + "/" + child.getName();
                            CmsUUID importStructureId = structureIdsByParentIdAndName.get(key);
                            if ((importStructureId != null) && !importStructureId.equals(child.getStructureId())) {
                                CmsResourceImportData importRes = importResourcesById.get(importStructureId);
                                if (child.isFile()
                                    && importRes.getResource().isFile()
                                    && !containsStructureId(child.getStructureId())
                                    && !vfsResourceWithStructureId(importStructureId)) {
                                    m_conflictingIds.put(importStructureId, child.getStructureId());
                                    importRes.setSkipResourceIdCheck(true);
                                    LOG.info(
                                        "Permitting conflicting id in module update for resource "
                                            + importRes.getPath()
                                            + " because the id from the module is not present in the VFS and vice versa.");
                                } else {
                                    LOG.info("Different structure id for same directory entry.");
                                    return false;
                                }
                            }

                        }

                    } catch (CmsException e) {
                        LOG.info(e.getLocalizedMessage(), e);

                    }
                }
            }

            for (CmsResourceImportData resData : getResourceData()) {
                String importPath = CmsModuleUpdater.normalizePath(resData.getResource().getRootPath());
                if (resData.hasStructureId()) {
                    CmsUUID importId = resData.getResource().getStructureId();
                    for (CmsObject cmsToRead : Arrays.asList(cms, onlineCms)) {
                        try {
                            CmsResource resourceFromVfs = cmsToRead.readResource(importPath, CmsResourceFilter.ALL);
                            if (!resourceFromVfs.getStructureId().equals(importId)) {

                                if (resData.getResource().isFile()
                                    && resourceFromVfs.isFile()
                                    && !containsStructureId(resourceFromVfs.getStructureId())
                                    && !vfsResourceWithStructureId(importId)) {

                                    LOG.info(
                                        "Permitting conflicting id in module update for resource "
                                            + resData.getPath()
                                            + " because the id from the module is not present in the VFS and vice versa.");
                                    m_conflictingIds.put(importId, resourceFromVfs.getStructureId());

                                    // If we have different structure ids, but they don't conflict with anything else in the manifest or VFS,
                                    // we don't compare resource ids. First, because having different resource ids is normal in this scenario, second
                                    // because the resource in the VFS is deleted anyway during the module update.
                                    resData.setSkipResourceIdCheck(true);

                                } else {

                                    LOG.info(
                                        "Module is not updateable because the same path is mapped to different structure ids in the import and in the VFS: "
                                            + importPath);
                                    return false;
                                }
                            }
                            if (!resData.isSkipResourceIdCheck()
                                && resData.getResource().isFile()
                                && !(resData.getResource().getResourceId().equals(resourceFromVfs.getResourceId()))) {
                                LOG.info(
                                    "Module is not updateable because of a resource id conflict for "
                                        + resData.getResource().getRootPath());
                                return false;
                            }
                        } catch (CmsVfsResourceNotFoundException e) {
                            // ignore
                        }
                    }

                    try {
                        CmsResource vfsResource = cms.readResource(importId, CmsResourceFilter.ALL);
                        if (vfsResource.isFolder() != resData.getResource().isFolder()) {
                            LOG.info(
                                "Module is not updateable because the same id belongs to a file in the import and a folder in the VFS, or vice versa");
                            return false;
                        }
                    } catch (CmsVfsResourceNotFoundException e) {
                        // ignore
                    }
                } else {
                    CmsModule module = getModule();
                    boolean included = false;
                    boolean excluded = false;
                    for (String res : module.getResources()) {
                        if (CmsStringUtil.isPrefixPath(res, resData.getPath())) {
                            included = true;
                            break;
                        }
                    }
                    for (String res : module.getExcludeResources()) {
                        if (CmsStringUtil.isPrefixPath(res, resData.getPath())) {
                            excluded = true;
                            break;
                        }
                    }
                    if (included && !excluded) {
                        LOG.info(
                            "Module is not updateable because one of the resource entries included in the module resources has no structure id in the manifest.");
                        return false;
                    }
                }

            }
            return true;
        } catch (CmsException e) {
            LOG.info("Module is not updateable because of error: " + e.getLocalizedMessage(), e);
            return false;
        }
    }

    /**
     * Gets the CMS object.<p>
     *
     * @return the CMS object
     */
    public CmsObject getCms() {

        return m_cms;
    }

    /**
     * Gets the map of conflicting ids.<p>
     *
     * The keys are structure ids from the manifest, the values are structure ids from the VFS.
     *
     * @return the conflicting id map
     */
    public Map<CmsUUID, CmsUUID> getConflictingIds() {

        return m_conflictingIds;
    }

    /**
     * Gets the module metadata from the import zip.<p>
     *
     * @return the module metadata
     */
    public CmsModule getModule() {

        return m_module;
    }

    /**
     * Gets the list of resource data objects for the manifest entries.<p>
     *
     * @return the resource data objects
     */
    public List<CmsResourceImportData> getResourceData() {

        return Collections.unmodifiableList(m_resources);
    }

    /**
     * Sets the CMS object.<p>
     *
     * @param cms the CMS object to set
     */
    public void setCms(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * Sets the module metadata.<p>
     *
     * @param module the module metadata
     */
    public void setModule(CmsModule module) {

        m_module = module;
    }

    /**
     * Check if the module data contains a given structure id.<p>
     *
     * @param structureId a structure id
     * @return true if the module contains the given structure id
     *
     */
    private boolean containsStructureId(CmsUUID structureId) {

        for (CmsResourceImportData res : m_resources) {
            if (res.getResource().getStructureId().equals(structureId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a resource  with a given structure id exists in the VFS, either online or offline.<p>
     *
     * @param importId the structure id to check
     *
     * @return true if the VFS contains a resource with the given id
     */
    private boolean vfsResourceWithStructureId(CmsUUID importId) {

        return m_cms.existsResource(importId, CmsResourceFilter.ALL)
            || m_onlineCms.existsResource(importId, CmsResourceFilter.ALL);
    }

}
