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

package org.opencms.file.wrapper;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.jlan.CmsJlanDiskInterface;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Resource wrapper used to import/export modules by copying them to/from virtual folders.<p>
 */
public class CmsResourceWrapperModules extends A_CmsResourceWrapper {

    /** The logger instance to use for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceWrapperModules.class);

    /** The base folder under which the virtual resources from this resource wrapper are available. */
    public static final String BASE_PATH = "/modules";

    /** The virtual folder which can be used to import modules. */
    public static final String IMPORT_PATH = BASE_PATH + "/import";

    /** The virtual folder which can be used to export modules. */
    public static final String EXPORT_PATH = BASE_PATH + "/export";

    /** The virtual folder which can be used to provide logs for module operations. */
    public static final String LOG_PATH = BASE_PATH + "/log";

    /** List of virtual folders made available by this resource wrapper. */
    public static final List<String> FOLDERS = Collections.unmodifiableList(
        Arrays.asList(BASE_PATH, IMPORT_PATH, EXPORT_PATH, LOG_PATH));

    /** Cache for imported module files. */
    private Map<String, CmsFile> m_importDataCache = new ConcurrentHashMap<String, CmsFile>();

    /**
     * Map containing the last update time for a given import folder path.<p>
     *
     * Why do we need this if we just want to write files in the import folder and not read them?
     * The reason is that when using this wrapper with the JLAN CIFS connector, some clients check
     * on the status of the import file before they write any data to it, and fail mysteriously if it isn't found,
     * so we have to pretend that the file actually exists after creating it.
     **/
    ConcurrentHashMap<String, Long> m_importFileUpdateCache = new ConcurrentHashMap<String, Long>();

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#addResourcesToFolder(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    @Override
    public List<CmsResource> addResourcesToFolder(CmsObject cms, String resourcename, CmsResourceFilter filter)
    throws CmsException {

        String resourceNameWithTrailingSlash = CmsStringUtil.joinPaths(resourcename, "/");
        if (matchPath("/", resourceNameWithTrailingSlash)) {
            return getVirtualResourcesForRoot(cms);
        } else if (matchPath(BASE_PATH, resourceNameWithTrailingSlash)) {
            return getVirtualResourcesForBasePath(cms);
        } else if (matchPath(EXPORT_PATH, resourceNameWithTrailingSlash)) {
            return getVirtualResourcesForExport(cms);
        } else if (matchPath(IMPORT_PATH, resourceNameWithTrailingSlash)) {
            return getVirtualResourcesForImport(cms);
        } else if (matchPath(LOG_PATH, resourceNameWithTrailingSlash)) {
            return getVirtualLogResources(cms);

        }

        return Collections.emptyList();
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#createResource(org.opencms.file.CmsObject, java.lang.String, int, byte[], java.util.List)
     */
    @Override
    public CmsResource createResource(
        CmsObject cms,
        String resourcename,
        int type,
        byte[] content,
        List<CmsProperty> properties) throws CmsException, CmsIllegalArgumentException {

        if (matchParentPath(IMPORT_PATH, resourcename)) {
            CmsResource res = createFakeBinaryFile(resourcename, 0);
            CmsFile file = new CmsFile(res);
            file.setContents(content);
            OpenCms.getModuleManager().getImportExportRepository().importModule(
                CmsResource.getName(resourcename),
                content);
            m_importFileUpdateCache.put(resourcename, Long.valueOf(System.currentTimeMillis()));
            return file;
        } else {
            return super.createResource(cms, resourcename, type, content, properties);
        }
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#deleteResource(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResource.CmsResourceDeleteMode)
     */
    @Override
    public boolean deleteResource(CmsObject cms, String resourcename, CmsResource.CmsResourceDeleteMode siblingMode)
    throws CmsException {

        if (matchParentPath(EXPORT_PATH, resourcename)) {
            String fileName = CmsResource.getName(resourcename);
            boolean result = OpenCms.getModuleManager().getImportExportRepository().deleteModule(fileName);
            return result;
        } else {
            return false;
        }
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#getLock(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    public CmsLock getLock(CmsObject cms, CmsResource resource) throws CmsException {

        if (isFakePath(resource.getRootPath())) {
            return CmsLock.getNullLock();
        } else {
            return super.getLock(cms, resource);
        }
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#isWrappedResource(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public boolean isWrappedResource(CmsObject cms, CmsResource res) {

        return CmsStringUtil.isPrefixPath(BASE_PATH, res.getRootPath());
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#lockResource(org.opencms.file.CmsObject, java.lang.String, boolean)
     */
    @Override
    public boolean lockResource(CmsObject cms, String resourcename, boolean temporary) {

        return isFakePath(resourcename);
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#readFile(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    @Override
    public CmsFile readFile(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        // this method isn't actually called when using the JLAN repository, because readResource already returns a CmsFile when needed
        cms.getRequestContext().removeAttribute(CmsJlanDiskInterface.NO_FILESIZE_REQUIRED);

        CmsResource res = readResource(cms, resourcename, filter);
        return (CmsFile)res;

    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#readResource(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    @Override
    public CmsResource readResource(CmsObject cms, String resourcepath, CmsResourceFilter filter) throws CmsException {

        if (resourcepath.endsWith("desktop.ini")) {
            return null;
        }

        for (String folder : FOLDERS) {
            if (matchPath(resourcepath, folder)) {
                return createFakeFolder(folder);
            }
        }

        if (matchParentPath(IMPORT_PATH, resourcepath)) {
            if (hasImportFile(resourcepath)) {
                CmsFile importData = m_importDataCache.get(resourcepath);
                if (importData != null) {
                    return importData;
                }
                return new CmsFile(createFakeBinaryFile(resourcepath));
            }
        }

        if (matchParentPath(EXPORT_PATH, resourcepath)) {
            CmsFile resultFile = new CmsFile(createFakeBinaryFile(resourcepath));
            if (cms.getRequestContext().getAttribute(CmsJlanDiskInterface.NO_FILESIZE_REQUIRED) == null) {
                // we *do* require the file size, so we need to get the module data
                LOG.info("Getting data for " + resourcepath);
                byte[] data = OpenCms.getModuleManager().getImportExportRepository().getExportedModuleData(
                    CmsResource.getName(resourcepath),
                    cms.getRequestContext().getCurrentProject());
                resultFile.setContents(data);
            }
            return resultFile;
        }

        if (matchParentPath(LOG_PATH, resourcepath)) {
            CmsFile resultFile = new CmsFile(createFakeBinaryFile(resourcepath));
            // if (cms.getRequestContext().getAttribute(CmsJlanDiskInterface.NO_FILESIZE_REQUIRED) == null) {
            String moduleName = CmsResource.getName(resourcepath).replaceFirst("\\.log$", "");
            try {
                byte[] data = OpenCms.getModuleManager().getImportExportRepository().getModuleLog().readLog(moduleName);
                resultFile.setContents(data);
                return resultFile;
            } catch (IOException e) {
                throw new CmsVfsResourceNotFoundException(
                    org.opencms.db.Messages.get().container(org.opencms.db.Messages.ERR_READ_RESOURCE_1, resourcepath),
                    e);
            }

        }
        return super.readResource(cms, resourcepath, filter);
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#unlockResource(org.opencms.file.CmsObject, java.lang.String)
     */
    @Override
    public boolean unlockResource(CmsObject cms, String resourcename) {

        return isFakePath(resourcename);
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#writeFile(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    @Override
    public CmsFile writeFile(CmsObject cms, CmsFile resource) throws CmsException {

        if (matchParentPath(IMPORT_PATH, resource.getRootPath())) {
            OpenCms.getModuleManager().getImportExportRepository().importModule(
                CmsResource.getName(resource.getRootPath()),
                resource.getContents());
            m_importFileUpdateCache.put(resource.getRootPath(), Long.valueOf(System.currentTimeMillis()));
            m_importDataCache.put(resource.getRootPath(), resource);
            return resource;
        } else {
            return super.writeFile(cms, resource);
        }
    }

    /**
     * Creates a fake CmsResource of type 'binary'.<p>
     *
     * @param rootPath  the root path
     *
     * @return the fake resource
     *
     * @throws CmsLoaderException if the binary type is missing
     */
    protected CmsResource createFakeBinaryFile(String rootPath) throws CmsLoaderException {

        return createFakeBinaryFile(rootPath, 0);
    }

    /**
     * Creates a fake CmsResource of type 'binary'.<p>
     *
     * @param rootPath  the root path
     * @param dateLastModified the last modification date to use
     *
     * @return the fake resource
     *
     * @throws CmsLoaderException if the binary type is missing
     */
    protected CmsResource createFakeBinaryFile(String rootPath, long dateLastModified) throws CmsLoaderException {

        CmsUUID structureId = CmsUUID.getConstantUUID("s-" + rootPath);
        CmsUUID resourceId = CmsUUID.getConstantUUID("r-" + rootPath);
        @SuppressWarnings("deprecation")
        int type = OpenCms.getResourceManager().getResourceType(CmsResourceTypeBinary.getStaticTypeName()).getTypeId();
        boolean isFolder = false;
        int flags = 0;
        CmsUUID projectId = CmsProject.ONLINE_PROJECT_ID;
        CmsResourceState state = CmsResource.STATE_UNCHANGED;
        long dateCreated = 0;
        long dateReleased = 1;
        long dateContent = 1;
        int version = 0;

        CmsUUID userCreated = CmsUUID.getNullUUID();
        CmsUUID userLastModified = CmsUUID.getNullUUID();
        long dateExpired = Integer.MAX_VALUE;
        int linkCount = 0;
        int size = 1;

        CmsResource resource = new CmsResource(
            structureId,
            resourceId,
            rootPath,
            type,
            isFolder,
            flags,
            projectId,
            state,
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            linkCount,
            size,
            dateContent,
            version);
        return resource;
    }

    /**
     * Creates a fake CmsResource of type 'folder'.<p>
     *
     * @param rootPath the root path
     *
     * @return the fake resource
     *
     * @throws CmsLoaderException if the 'folder' type can not be found
     */
    protected CmsResource createFakeFolder(String rootPath) throws CmsLoaderException {

        if (rootPath.endsWith("/")) {
            rootPath = CmsFileUtil.removeTrailingSeparator(rootPath);
        }

        CmsUUID structureId = CmsUUID.getConstantUUID("s-" + rootPath);
        CmsUUID resourceId = CmsUUID.getConstantUUID("r-" + rootPath);
        @SuppressWarnings("deprecation")
        int type = OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()).getTypeId();
        boolean isFolder = true;
        int flags = 0;
        CmsUUID projectId = CmsProject.ONLINE_PROJECT_ID;
        CmsResourceState state = CmsResource.STATE_UNCHANGED;
        long dateCreated = 0;
        long dateLastModified = 0;
        long dateReleased = 1;
        long dateContent = 1;
        int version = 0;
        CmsUUID userCreated = CmsUUID.getNullUUID();
        CmsUUID userLastModified = CmsUUID.getNullUUID();
        long dateExpired = Integer.MAX_VALUE;
        int linkCount = 0;
        int size = -1;
        CmsResource resource = new CmsResource(
            structureId,
            resourceId,
            rootPath,
            type,
            isFolder,
            flags,
            projectId,
            state,
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            linkCount,
            size,
            dateContent,
            version);
        return resource;
    }

    /**
     * Gets the virtual resources in the log folder.<p>
     *
     * @param cms the CMS context
     * @return the list of virtual log resources
     *
     * @throws CmsException if something goes wrong
     */
    private List<CmsResource> getVirtualLogResources(CmsObject cms) throws CmsException {

        List<CmsResource> virtualResources = Lists.newArrayList();
        for (CmsModule module : OpenCms.getModuleManager().getAllInstalledModules()) {
            String path = CmsStringUtil.joinPaths(LOG_PATH, module.getName() + ".log");
            CmsResource res = createFakeBinaryFile(path);
            virtualResources.add(res);
        }
        return virtualResources;
    }

    /**
     * Gets the virtual resources for the base folder.<p>
     *
     * @param cms the current CMS context
     * @return the virtual resources for the base folder
     *
     * @throws CmsException if something goes wrong
     */
    private List<CmsResource> getVirtualResourcesForBasePath(CmsObject cms) throws CmsException {

        return Arrays.asList(createFakeFolder(IMPORT_PATH), createFakeFolder(EXPORT_PATH), createFakeFolder(LOG_PATH));
    }

    /**
     * Gets the virtual resources for the export folder.<p>
     *
     * @param cms the CMS context
     * @return the list of resources for the export folder
     *
     * @throws CmsException if something goes wrong
     */
    private List<CmsResource> getVirtualResourcesForExport(CmsObject cms) throws CmsException {

        List<CmsResource> virtualResources = Lists.newArrayList();
        for (String name : OpenCms.getModuleManager().getImportExportRepository().getModuleFileNames()) {
            String path = CmsStringUtil.joinPaths(EXPORT_PATH, name);
            CmsResource res = createFakeBinaryFile(path);
            virtualResources.add(res);
        }
        return virtualResources;

    }

    /**
     * Gets the virtual resources for the import folder.<p>
     *
     * @param cms the CMS context
     *
     * @return the virtual resources for the import folder
     */
    private List<CmsResource> getVirtualResourcesForImport(CmsObject cms) {

        List<CmsResource> result = Lists.newArrayList();
        return result;
    }

    /**
     * Gets the virtual resources to add to the root folder.<p>
     *
     * @param cms the CMS context to use
     * @return the virtual resources for the root folder
     *
     * @throws CmsException if something goes wrong
     */
    private List<CmsResource> getVirtualResourcesForRoot(CmsObject cms) throws CmsException {

        CmsResource resource = createFakeFolder(BASE_PATH);
        return Arrays.asList(resource);
    }

    /**
     * Checks if the the import file is available.<p>
     *
     * @param resourcepath the resource path
     *
     * @return true if the import file is available
     */
    private boolean hasImportFile(String resourcepath) {

        Long value = m_importFileUpdateCache.get(resourcepath);
        if (value == null) {
            return false;
        }
        long age = System.currentTimeMillis() - value.longValue();
        return age < 5000;
    }

    /**
     * Returns true if the given path is a fake path handled by this resource wrapper.<p>
     *
     * @param resourcename the path
     *
     * @return true if the path is a fake path handled by this resource wrapper
     */
    private boolean isFakePath(String resourcename) {

        for (String folder : FOLDERS) {
            if (matchPath(folder, resourcename) || matchParentPath(folder, resourcename)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a given path is a direct descendant of another path.<p>
     *
     * @param expectedParent the expected parent folder
     * @param path a path
     * @return true if the path is a direct child of expectedParent
     */
    private boolean matchParentPath(String expectedParent, String path) {

        String parent = CmsResource.getParentFolder(path);
        if (parent == null) {
            return false;
        }
        return matchPath(expectedParent, parent);
    }

    /**
     * Checks if a path matches another part.<p>
     *
     * This is basically an equality test, but ignores the presence/absence of trailing slashes.
     *
     * @param expected the expected path
     * @param actual the actual path
     * @return true if the actual path matches the expected path
     */
    private boolean matchPath(String expected, String actual) {

        return CmsStringUtil.joinPaths(actual, "/").equals(CmsStringUtil.joinPaths(expected, "/"));
    }
}
