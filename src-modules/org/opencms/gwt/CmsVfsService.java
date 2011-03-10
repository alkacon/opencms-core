/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/Attic/CmsVfsService.java,v $
 * Date   : $Date: 2011/03/10 07:48:54 $
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

package org.opencms.gwt;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.shared.CmsBrokenLinkBean;
import org.opencms.gwt.shared.CmsVfsEntryBean;
import org.opencms.gwt.shared.rpc.I_CmsVfsService;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.map.MultiValueMap;

/**
 * A service class for reading the VFS tree.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsVfsService extends CmsGwtService implements I_CmsVfsService {

    /** Serialization id. */
    private static final long serialVersionUID = -383483666952834348L;

    /**
     * @see org.opencms.gwt.shared.rpc.I_CmsVfsService#getBrokenLinks(java.lang.String)
     */
    public List<CmsBrokenLinkBean> getBrokenLinks(String sitePath) throws CmsRpcException {

        List<CmsBrokenLinkBean> result = null;

        try {
            ensureSession();
            CmsObject cms = getCmsObject();
            List<CmsResource> descendands = new ArrayList<CmsResource>();
            HashSet<CmsUUID> deleteIds = new HashSet<CmsUUID>();
            CmsResource entryResource = cms.readResource(sitePath);

            descendands.add(entryResource);
            if (entryResource.isFolder()) {
                descendands.addAll(cms.readResources(sitePath, CmsResourceFilter.IGNORE_EXPIRATION));
            }
            for (CmsResource deleteRes : descendands) {
                deleteIds.add(deleteRes.getStructureId());
            }
            MultiValueMap linkMap = MultiValueMap.decorate(
                new HashMap<Object, Object>(),
                FactoryUtils.instantiateFactory(HashSet.class));
            for (CmsResource resource : descendands) {
                List<CmsResource> linkSources = getLinkSources(cms, resource, deleteIds);
                for (CmsResource source : linkSources) {
                    linkMap.put(resource, source);
                }
            }

            result = getBrokenLinkBeans(linkMap);
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

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
     * Creates a "broken link" bean based on a resource.<p>
     * 
     * @param resource the resource 
     * 
     * @return the "broken link" bean with the data from the resource 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected CmsBrokenLinkBean createSitemapBrokenLinkBean(CmsResource resource) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsProperty titleProp = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, true);
        String defaultTitle = "";
        String title = titleProp.getValue(defaultTitle);
        String path = cms.getSitePath(resource);
        String subtitle = path;
        return new CmsBrokenLinkBean(title, subtitle);
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

    /**
     * Helper method for converting a map which maps resources to resources to a list of "broken link" beans,
     * which have beans representing the source of the corresponding link as children.<p>  
     * 
     * @param linkMap a multimap from resource to resources  
     * 
     * @return a list of beans representing links which will be broken 
     * 
     * @throws CmsException if something goes wrong 
     */
    @SuppressWarnings("unchecked")
    private List<CmsBrokenLinkBean> getBrokenLinkBeans(MultiValueMap linkMap) throws CmsException {

        List<CmsBrokenLinkBean> result = new ArrayList<CmsBrokenLinkBean>();
        for (CmsResource entry : (Set<CmsResource>)linkMap.keySet()) {
            CmsBrokenLinkBean parentBean = createSitemapBrokenLinkBean(entry);
            result.add(parentBean);
            Collection<CmsResource> values = linkMap.getCollection(entry);
            for (CmsResource resource : values) {
                CmsBrokenLinkBean childBean = createSitemapBrokenLinkBean(resource);
                parentBean.addChild(childBean);
            }
        }
        return result;
    }

    /**
     * Gets the resources which link to a given structure id.<p>
     * 
     * @param cms the current CMS context 
     * @param resource the relation target resource
     * @param deleteIds set of resources to delete
     *  
     * @return the list of resources which link to the given id
     *  
     * @throws CmsException
     */
    private List<CmsResource> getLinkSources(CmsObject cms, CmsResource resource, HashSet<CmsUUID> deleteIds)
    throws CmsException {

        List<CmsRelation> relations = cms.getRelationsForResource(resource, CmsRelationFilter.SOURCES);
        List<CmsResource> result = new ArrayList<CmsResource>();
        for (CmsRelation relation : relations) {
            // only add related resources that are not going to be deleted
            if (!deleteIds.contains(relation.getSourceId())) {
                result.add(relation.getSource(cms, CmsResourceFilter.IGNORE_EXPIRATION));
            }
        }
        return result;
    }

}
