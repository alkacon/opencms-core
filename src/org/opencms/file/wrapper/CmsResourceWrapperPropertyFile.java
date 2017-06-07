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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Adds a folder in every existing folder with the name "__properties" which
 * contains property files for every resource in the existing folder.<p>
 *
 * Empty folders don't have the property folder visible.<p>
 *
 * The names of the property files are the same as the resource they belong to
 * with the extension "properties". To keep the correct sorting the names of
 * folders gets additionaly the prefix "__" to keep them at the beginning of the
 * list.<p>
 *
 * When creating new folders, the property folder gets visible after a time period
 * of 60 seconds. For new resources the property file appears after that period too.
 * In this time period it is possible to create the property folder and the property
 * files manually. The properties in the created property files will be set at the
 * resource they belong to.<p>
 *
 * @since 6.5.6
 */
public class CmsResourceWrapperPropertyFile extends A_CmsResourceWrapper {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceWrapperPropertyFile.class);

    /** The prefix for folders to keep correct sorting. */
    private static final String FOLDER_PREFIX = "__";

    /** The name to use for the folder where all property files are listed in. */
    private static final String PROPERTY_DIR = "__properties";

    /** The time in seconds to wait till the properties (and the property folder) are visible. */
    private static final int TIME_DELAY = 60;

    /** Table with the states of the virtual files. */
    private static final List<String> TMP_FILE_TABLE = new ArrayList<String>();

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#addResourcesToFolder(CmsObject, String, CmsResourceFilter)
     */
    @Override
    public List<CmsResource> addResourcesToFolder(CmsObject cms, String resourcename, CmsResourceFilter filter)
    throws CmsException {

        String path = resourcename;
        if (!path.endsWith("/")) {
            path += "/";
        }

        if (path.endsWith(PROPERTY_DIR + "/")) {

            String parent = CmsResource.getParentFolder(path);
            List<CmsResource> ret = new ArrayList<CmsResource>();

            // Iterate through all existing resources
            List<CmsResource> resources = cms.getResourcesInFolder(parent, filter);
            Iterator<CmsResource> iter = resources.iterator();
            while (iter.hasNext()) {
                CmsResource res = iter.next();

                // check "existance" of resource
                if (existsResource(res)) {

                    // add the generated property file
                    ret.add(CmsResourceWrapperUtils.createPropertyFile(cms, res, getPropertyFileName(res)));
                }
            }

            return ret;
        } else {

            try {
                CmsResource folder = cms.readResource(resourcename);
                if (folder.isFolder()) {

                    // check if folder is empty
                    if (!cms.getResourcesInFolder(resourcename, CmsResourceFilter.DEFAULT).isEmpty()) {

                        // check "existance" of folder
                        if (existsResource(folder)) {
                            List<CmsResource> ret = new ArrayList<CmsResource>();

                            CmsWrappedResource wrap = new CmsWrappedResource(folder);
                            wrap.setRootPath(folder.getRootPath() + PROPERTY_DIR + "/");

                            ret.add(wrap.getResource());
                            return ret;
                        }
                    }
                }
            } catch (CmsVfsResourceNotFoundException ex) {
                return null;
            }
        }

        return null;
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

        CmsResource res = getResource(cms, resourcename, CmsResourceFilter.DEFAULT);
        if (res != null) {

            // cut off trailing slash
            if (resourcename.endsWith("/")) {
                resourcename = resourcename.substring(0, resourcename.length() - 1);
            }

            // check "existance" of resource
            if (existsResource(res)) {

                throw new CmsVfsResourceAlreadyExistsException(org.opencms.db.generic.Messages.get().container(
                    org.opencms.db.generic.Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                    resourcename));
            }

            // mark file as created in tmp file table
            TMP_FILE_TABLE.add(res.getRootPath());

            // lock the resource because this is the expected behavior
            cms.lockResource(cms.getRequestContext().removeSiteRoot(res.getRootPath()));

            if (resourcename.endsWith(PROPERTY_DIR)) {

                CmsWrappedResource wrap = new CmsWrappedResource(res);
                wrap.setRootPath(res.getRootPath() + PROPERTY_DIR + "/");
                wrap.setFolder(true);
                return wrap.getResource();
            } else if (resourcename.endsWith(CmsResourceWrapperUtils.EXTENSION_PROPERTIES)) {

                CmsResourceWrapperUtils.writePropertyFile(
                    cms,
                    cms.getRequestContext().removeSiteRoot(res.getRootPath()),
                    content);
                return res;
            }

        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#deleteResource(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResource.CmsResourceDeleteMode)
     */
    @Override
    public boolean deleteResource(CmsObject cms, String resourcename, CmsResourceDeleteMode siblingMode)
    throws CmsException {

        CmsResource res = getResource(cms, resourcename, CmsResourceFilter.DEFAULT);
        if (res != null) {
            TMP_FILE_TABLE.remove(res.getRootPath());
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#getLock(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    public CmsLock getLock(CmsObject cms, CmsResource resource) throws CmsException {

        CmsResource org = getResource(cms, resource.getStructureId());
        if (org != null) {
            return cms.getLock(org);
        }
        return null;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#isWrappedResource(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public boolean isWrappedResource(CmsObject cms, CmsResource res) {

        String path = res.getRootPath();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.endsWith(PROPERTY_DIR)) {
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#lockResource(org.opencms.file.CmsObject, java.lang.String, boolean)
     */
    @Override
    public boolean lockResource(CmsObject cms, String resourcename, boolean temporary) throws CmsException {

        CmsResource res = getResource(cms, resourcename, CmsResourceFilter.DEFAULT);
        if (res != null) {
            String path = cms.getRequestContext().removeSiteRoot(res.getRootPath());
            if (temporary) {
                cms.lockResourceTemporary(path);
            } else {
                cms.lockResource(path);
            }
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#readFile(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    @Override
    public CmsFile readFile(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        if (!resourcename.endsWith(PROPERTY_DIR)) {
            CmsResource res = getResource(cms, resourcename, filter);
            if (res != null) {

                // Workaround for Dreamweaver:
                // Dreamweaver copies folders through creating folder for folder and file for file.
                // So there is first a call if the property folder already exists and afterwards create
                // it. If the folder already exists, the copy action fails.
                // In the first time after creating a folder, the property dir does not exists until it is
                // created or the time expired.
                if (!existsResource(res)) {
                    return null;
                }

                return CmsResourceWrapperUtils.createPropertyFile(cms, res, getPropertyFileName(res));
            }
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#readResource(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    @Override
    public CmsResource readResource(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource res = getResource(cms, resourcename, filter);
        if (res != null) {

            // Workaround for Dreamweaver:
            // Dreamweaver copies folders through creating folder for folder and file for file.
            // So there is first a call if the property folder already exists and afterwards create
            // it. If the folder already exists, the copy action fails.
            // In the first time after creating a folder, the property dir does not exists until it is
            // created or the time expired.
            if (!existsResource(res)) {
                return null;
            }

            // cut off trailing slash
            if (resourcename.endsWith("/")) {
                resourcename = resourcename.substring(0, resourcename.length() - 1);
            }

            // create property file and return the resource for it
            if (!resourcename.endsWith(PROPERTY_DIR)) {
                return CmsResourceWrapperUtils.createPropertyFile(cms, res, getPropertyFileName(res));
            }

            // create a resource for the __property folder
            CmsWrappedResource wrap = new CmsWrappedResource(res);
            wrap.setRootPath(res.getRootPath() + PROPERTY_DIR);
            wrap.setFolder(true);
            return wrap.getResource();
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#restoreLink(org.opencms.file.CmsObject, java.lang.String)
     */
    @Override
    public String restoreLink(CmsObject cms, String uri) {

        try {
            CmsResource res = getResource(cms, uri, CmsResourceFilter.DEFAULT);
            if (res != null) {
                return res.getRootPath();
            }
        } catch (CmsException ex) {
            // noop
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#unlockResource(org.opencms.file.CmsObject, java.lang.String)
     */
    @Override
    public boolean unlockResource(CmsObject cms, String resourcename) throws CmsException {

        CmsResource res = getResource(cms, resourcename, CmsResourceFilter.DEFAULT);
        if (res != null) {
            cms.unlockResource(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#writeFile(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    @Override
    public CmsFile writeFile(CmsObject cms, CmsFile resource) throws CmsException {

        CmsResource res = getResource(cms, resource.getStructureId());
        if (res != null) {
            CmsResourceWrapperUtils.writePropertyFile(
                cms,
                cms.getRequestContext().removeSiteRoot(res.getRootPath()),
                resource.getContents());
            return resource;
        }

        return null;
    }

    /**
     * Tries to the read the resource with the given structure id using the given CmsObject and returns it, or null if the resource can not be read.<p>
     *
     * @param cms the CmsObject to use
     * @param structureId the structure id of the resource
     * @return the resource which has been read
     */
    CmsResource getResource(CmsObject cms, CmsUUID structureId) {

        try {
            CmsResource result = cms.readResource(structureId);
            return result;
        } catch (CmsVfsResourceNotFoundException e) {
            return null;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Checks if a resource exists depending on the creation date and the temp files saved.<p>
     *
     * Dreamweaver copies folders through creating folder for folder and file for file.
     * So there is first a call if the property folder already exists and afterwards create
     * it. If the folder already exists, the copy action fails.
     * In the first time after creating a folder, the property dir does not exists until it is
     * created or the time expired.<p>
     *
     * @param res the resource to check if it exists
     *
     * @return return if the folder exists otherwise false
     */
    private boolean existsResource(CmsResource res) {

        long now = new Date().getTime();
        long created = res.getDateCreated();
        long diff = (now - created) / 1000;

        if (diff <= TIME_DELAY) {

            // check tmp file table
            if (TMP_FILE_TABLE.contains(res.getRootPath())) {
                return true;
            }

            return false;
        } else {

            // remove from tmp file table
            TMP_FILE_TABLE.remove(res.getRootPath());
        }

        return true;
    }

    /**
     * Creates the full path to the property file of the given resource.<p>
     *
     * @param res the resource where to create the path for the property file for
     *
     * @return the full path to the property file of the resource
     */
    private String getPropertyFileName(CmsResource res) {

        StringBuffer ret = new StringBuffer();

        // path to the parent folder
        String parentFolder = CmsResource.getParentFolder(res.getRootPath());
        ret.append(parentFolder);

        // make sure ends with a slash
        if (!parentFolder.endsWith("/")) {
            ret.append("/");
        }

        // append name of the property folder
        ret.append(PROPERTY_DIR);
        ret.append("/");

        // if resource is a folder add the prefix "__"
        if (res.isFolder()) {
            ret.append(FOLDER_PREFIX);
        }

        // append the name of the resource
        ret.append(res.getName());

        return ret.toString();
    }

    /**
     * Reads the resource for the property file.<p>
     *
     * @param cms the initialized CmsObject
     * @param resourcename the name of the property resource
     * @param filter the filter to use
     *
     * @return the resource for the property file or null if not found
     *
     * @throws CmsException if something goes wrong
     */
    private CmsResource getResource(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        // the path without trailing slash
        String path = CmsResource.getParentFolder(resourcename);
        if (path == null) {
            return null;
        }

        // the parent path
        String parent = CmsResource.getParentFolder(path);

        // the name of the resource
        String name = CmsResource.getName(resourcename);
        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }

        // read the resource for the property dir
        if (name.equals(PROPERTY_DIR)) {

            return cms.readResource(path, filter);
        }

        if ((path.endsWith(PROPERTY_DIR + "/")) && (name.endsWith(CmsResourceWrapperUtils.EXTENSION_PROPERTIES))) {
            CmsResource res = null;

            if (name.startsWith(FOLDER_PREFIX)) {
                name = name.substring(2);
            }

            try {
                String resPath = CmsResourceWrapperUtils.removeFileExtension(
                    cms,
                    parent + name,
                    CmsResourceWrapperUtils.EXTENSION_PROPERTIES);

                res = cms.readResource(resPath, filter);
            } catch (CmsException ex) {
                // noop
            }

            return res;
        }

        return null;
    }

}
