/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/wrapper/CmsResourceWrapperPropertyFile.java,v $
 * Date   : $Date: 2007/03/01 16:58:53 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.file.wrapper;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generates a property file for a resource.<p>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 6.2.4
 */
public class CmsResourceWrapperPropertyFile extends A_CmsResourceWrapper {

    /** The prefix for folders to keep correct sorting. */
    private static final String FOLDER_PREFIX = "__";

    /** The name to use for the folder where all property files are listed in. */
    private static final String PROPERTY_DIR = "__properties";

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#deleteResource(org.opencms.file.CmsObject, java.lang.String, int)
     */
    public boolean deleteResource(CmsObject cms, String resourcename, int siblingMode)
    throws CmsException {

        CmsResource res = getResource(cms, resourcename, CmsResourceFilter.DEFAULT);
        if (res != null) {
            return true;
        }

        return false;
    }
    
    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#getLock(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public CmsLock getLock(CmsObject cms, CmsResource resource) throws CmsException {

        //CmsResource org = cms.readResource(resource.getStructureId());
        CmsResource org = getResource(cms, resource.getRootPath(), CmsResourceFilter.DEFAULT);
        if (org != null) {

            return cms.getLock(org);
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#getResourcesInFolder(CmsObject, String, CmsResourceFilter)
     */
    public List getResourcesInFolder(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        String path = resourcename;
        if (!path.endsWith("/")) {
            path += "/";
        }

        if (path.endsWith(PROPERTY_DIR + "/")) {

            String parent = CmsResource.getParentFolder(path);
            List ret = new ArrayList();

            // Iterate through all existing resources
            List resources = cms.getResourcesInFolder(parent, filter);
            Iterator iter = resources.iterator();
            while (iter.hasNext()) {
                CmsResource res = (CmsResource)iter.next();

                // add the generated property file
                ret.add(CmsWrappedResource.createPropertyFile(cms, res, getPropertyFileName(res)));
            }

            return ret;
        } else {

            try {
                CmsResource folder = cms.readResource(resourcename);
                if (folder.isFolder()) {

                    // check if folder is empty
                    if (!cms.getResourcesInFolder(resourcename, CmsResourceFilter.DEFAULT).isEmpty()) {
                        List ret = new ArrayList();

                        CmsWrappedResource wrap = new CmsWrappedResource(folder);
                        wrap.setRootPath(folder.getRootPath() + PROPERTY_DIR + "/");

                        ret.add(wrap.getResource());
                        return ret;
                    }
                }
            } catch (CmsVfsResourceNotFoundException ex) {
                return null;
            }
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
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#lockResource(org.opencms.file.CmsObject, java.lang.String)
     */
    public boolean lockResource(CmsObject cms, String resourcename) throws CmsException {

        CmsResource res = getResource(cms, resourcename, CmsResourceFilter.DEFAULT);
        if (res != null) {
            cms.lockResource(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#readFile(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    public CmsFile readFile(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        if (!resourcename.endsWith(PROPERTY_DIR)) {
            CmsResource res = getResource(cms, resourcename, filter);
            if (res != null) {
                return CmsWrappedResource.createPropertyFile(cms, res, getPropertyFileName(res));
            }
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#readResource(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    public CmsResource readResource(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource res = getResource(cms, resourcename, filter);
        if (res != null) {

            // cut off trailing slash
            if (resourcename.endsWith("/")) {
                resourcename = resourcename.substring(0, resourcename.length() - 1);
            }

            if (!resourcename.endsWith(PROPERTY_DIR)) {
                return CmsWrappedResource.createPropertyFile(cms, res, getPropertyFileName(res));
            }

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
    public CmsFile writeFile(CmsObject cms, CmsFile resource) throws CmsException {

        //CmsResource res = cms.readResource(resource.getStructureId());
        CmsResource res = getResource(
            cms,
            cms.getRequestContext().removeSiteRoot(resource.getRootPath()),
            CmsResourceFilter.ALL);
        if (res != null) {
            CmsWrappedResource.writePropertyFile(
                cms,
                cms.getRequestContext().removeSiteRoot(res.getRootPath()),
                resource.getContents());
            return resource;
        }

        return null;
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

        if (name.endsWith(CmsWrappedResource.EXTENSION_PROPERTIES)) {
            CmsResource res = null;

            if (name.startsWith(FOLDER_PREFIX)) {
                name = name.substring(2);
            }

            try {
                String resPath = CmsWrappedResource.removeFileExtension(
                    cms,
                    parent + name,
                    CmsWrappedResource.EXTENSION_PROPERTIES);

                res = cms.readResource(resPath, filter);
            } catch (CmsException ex) {
                // noop
            }

            return res;
        }

        return null;
    }

}
