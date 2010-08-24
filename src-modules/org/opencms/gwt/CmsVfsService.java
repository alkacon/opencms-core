/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/Attic/CmsVfsService.java,v $
 * Date   : $Date: 2010/08/24 15:15:14 $
 * Version: $Revision: 1.1 $
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

package org.opencms.gwt;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.shared.CmsVfsEntryBean;
import org.opencms.gwt.shared.rpc.I_CmsVfsService;
import org.opencms.main.CmsException;

import java.util.ArrayList;
import java.util.List;

/**
 * A service class for reading the VFS tree.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsVfsService extends CmsGwtService implements I_CmsVfsService {

    /** Serialization id. */
    private static final long serialVersionUID = -383483666952834348L;

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getChildren(java.lang.String)
     */
    public List<CmsVfsEntryBean> getChildren(String path) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            List<CmsResource> resources = new ArrayList<CmsResource>();
            resources.addAll(cms.getResourcesInFolder(path, CmsResourceFilter.DEFAULT));
            List<CmsVfsEntryBean> result = makeEntryBeans(resources, false);
            return result;
        } catch (CmsException e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getRootEntries()
     */
    public List<CmsVfsEntryBean> getRootEntries() throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            List<CmsResource> roots = new ArrayList<CmsResource>();
            roots.add(cms.readResource("/"));
            return makeEntryBeans(roots, true);
        } catch (CmsException e) {
            error(e);
        }
        return null;
    }

    /**
     * Helper method for creating a VFS entry bean from a resource.<p>
     *  
     * @param resource the resource whose data should be stored in the bean 
     * @param root true if the resource is a root resource
     *  
     * @return the data bean representing the resource 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected CmsVfsEntryBean makeEntryBean(CmsResource resource, boolean root) throws CmsException {

        CmsObject cms = getCmsObject();
        boolean isFolder = resource.isFolder();
        String name = root ? "/" : resource.getName();
        String path = cms.getSitePath(resource);
        boolean hasChildren = false;
        if (isFolder) {
            List<CmsResource> children = cms.getResourcesInFolder(
                cms.getRequestContext().getSitePath(resource),
                CmsResourceFilter.DEFAULT);
            if (!children.isEmpty()) {
                hasChildren = true;
            }
        }
        return new CmsVfsEntryBean(path, name, isFolder, hasChildren);
    }

    /**
     * Helper method for creating a list of VFS entry beans from a list of the corresponding resources.<p>
     * 
     * @param resources the list of resources which should be converted to entry beans 
     * @param root true if the resources in the list are root resources
     *  
     * @return the list of VFS entry beans for the resources 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected List<CmsVfsEntryBean> makeEntryBeans(List<CmsResource> resources, boolean root) throws CmsException {

        List<CmsVfsEntryBean> result = new ArrayList<CmsVfsEntryBean>();
        for (CmsResource res : resources) {
            result.add(makeEntryBean(res, root));
        }
        return result;
    }

}
