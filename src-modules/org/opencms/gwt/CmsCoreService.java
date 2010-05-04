/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/Attic/CmsCoreService.java,v $
 * Date   : $Date: 2010/05/04 09:40:41 $
 * Version: $Revision: 1.7 $
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
import org.opencms.flex.CmsFlexController;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.gwt.shared.rpc.I_CmsCoreService;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides general core services.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreService
 * @see org.opencms.gwt.shared.rpc.I_CmsCoreServiceAsync
 */
public class CmsCoreService extends CmsGwtService implements I_CmsCoreService {

    /** Serialization uid. */
    private static final long serialVersionUID = 5915848952948986278L;

    /**
     * Returns a new configured service instance.<p>
     * 
     * @param request the current request
     * 
     * @return a new service instance
     */
    public static CmsCoreService newInstance(HttpServletRequest request) {

        CmsCoreService srv = new CmsCoreService();
        srv.setCms(CmsFlexController.getCmsObject(request));
        srv.setRequest(request);
        return srv;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#getCategories(java.lang.String, boolean, java.util.List)
     */
    public CmsCategoryTreeEntry getCategories(String fromPath, boolean includeSubCats, List<String> refPaths)
    throws CmsRpcException {

        CmsObject cms = getCmsObject();
        CmsCategoryService catService = CmsCategoryService.getInstance();

        List<String> repositories = new ArrayList<String>();
        if ((refPaths != null) && !refPaths.isEmpty()) {
            for (String refPath : refPaths) {
                repositories.addAll(catService.getCategoryRepositories(getCmsObject(), refPath));
            }
        } else {
            repositories.add(CmsCategoryService.CENTRALIZED_REPOSITORY);
        }

        CmsCategoryTreeEntry result = null;
        try {
            result = new CmsCategoryTreeEntry(fromPath);
            // get the categories
            List<CmsCategory> categories = catService.readCategoriesForRepositories(
                cms,
                fromPath,
                includeSubCats,
                repositories);
            // convert them to a tree structure
            CmsCategoryTreeEntry parent = result;
            for (CmsCategory category : categories) {
                CmsCategoryTreeEntry current = new CmsCategoryTreeEntry(category);
                String parentPath = CmsResource.getParentFolder(current.getPath());
                if (!parentPath.equals(parent.getPath())) {
                    parent = findCategory(result, parentPath);
                }
                parent.addChild(current);
            }
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#lock(java.lang.String)
     */
    public String lock(String uri) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            cms.lockResource(uri);
        } catch (CmsException e) {
            return e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#lockTemp(java.lang.String)
     */
    public String lockTemp(String uri) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            cms.lockResourceTemporary(uri);
        } catch (CmsException e) {
            return e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#prefetch()
     */
    public CmsCoreData prefetch() {

        CmsObject cms = getCmsObject();
        CmsCoreData data = new CmsCoreData(
            OpenCms.getSystemInfo().getOpenCmsContext(),
            cms.getRequestContext().getSiteRoot(),
            cms.getRequestContext().getLocale().toString(),
            OpenCms.getWorkplaceManager().getWorkplaceLocale(cms).toString(),
            cms.getRequestContext().getUri());
        return data;
    }

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsCoreService#unlock(java.lang.String)
     */
    public String unlock(String uri) throws CmsRpcException {

        CmsObject cms = getCmsObject();
        try {
            cms.unlockResource(uri);
        } catch (CmsException e) {
            return e.getLocalizedMessage(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
        } catch (Throwable e) {
            error(e);
        }
        return null;
    }

    /**
     * FInds a category in the given tree.<p>
     * 
     * @param tree the the tree to search in
     * @param path the path to search for
     * 
     * @return the category with the given path or <code>null</code> if not found
     */
    private CmsCategoryTreeEntry findCategory(CmsCategoryTreeEntry tree, String path) {

        // we assume that the category to find is descendant of tree
        CmsCategoryTreeEntry parent = tree;
        if (path.equals(parent.getPath())) {
            return parent;
        }
        boolean found = true;
        while (found) {
            List<CmsCategoryTreeEntry> children = parent.getChildren();
            if (children == null) {
                return null;
            }
            // since the categories are sorted it is faster to go backwards
            found = false;
            for (int i = children.size() - 1; i >= 0; i--) {
                CmsCategoryTreeEntry child = children.get(i);
                if (path.equals(child.getPath())) {
                    return child;
                }
                if (path.startsWith(child.getPath())) {
                    parent = child;
                    found = true;
                    break;
                }
            }
        }
        return null;
    }
}
