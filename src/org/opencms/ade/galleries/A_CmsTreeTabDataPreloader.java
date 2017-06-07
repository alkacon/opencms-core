/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

import org.opencms.ade.galleries.shared.I_CmsGalleryTreeEntry;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSiteManagerImpl;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Abstract class which is used to generate the data for showing an already opened tree in the gallery dialog.<p>
 *
 * @param <T> the type of tree entry bean produced by this class
 */
public abstract class A_CmsTreeTabDataPreloader<T extends I_CmsGalleryTreeEntry<T>> {

    /** Keeps track of which resources are children of each other. */
    private Multimap<CmsResource, CmsResource> m_childMap = ArrayListMultimap.create();

    /** The CMS context used for file operations. */
    private CmsObject m_cms;

    /** The common root path. */
    private String m_commonRoot;

    /** The filter used for reading resources. */
    private CmsResourceFilter m_filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED;

    /** The set of resources which have already been read. */
    private Set<CmsResource> m_knownResources = new HashSet<CmsResource>();

    /** Resources whose children should be loaded. */
    private Set<CmsResource> m_mustLoadChildren = new HashSet<CmsResource>();

    /** The root resource. */
    private CmsResource m_rootResource;

    /**
     * Creates the preload data for a collection of resources which correspond to "opened" tree items.<p>
     *
     * @param cms the CMS context to use
     * @param openResources the resources which correspond to opened tree items
     * @param selectedResources resources which should be part of the tree, but not opened
     *
     * @return the root tree entry bean which was created
     *
     * @throws CmsException if something goes wrong
     */
    public T preloadData(CmsObject cms, Set<CmsResource> openResources, Set<CmsResource> selectedResources)
    throws CmsException {

        assert m_cms == null : "Instance can't be used more than once!";
        if (openResources == null) {
            openResources = Sets.newHashSet();
        }
        if (selectedResources == null) {
            selectedResources = Sets.newHashSet();
        }

        boolean ignoreOpen = false;
        if (!selectedResources.isEmpty() && !openResources.isEmpty()) {
            // if selected and opened resources are in different sites,
            // we do *not* want to start from the common root folder (usually '/'),
            // so ignore the 'open' resources.
            String siteForSelected = getCommonSite(selectedResources);
            String siteForOpen = getCommonSite(openResources);
            if (!Objects.equal(siteForSelected, siteForOpen)) {
                ignoreOpen = true;
            }
        }

        Set<CmsResource> allParamResources = Sets.newHashSet();
        if (!ignoreOpen) {
            allParamResources.addAll(openResources);
        }
        allParamResources.addAll(selectedResources);
        m_cms = OpenCms.initCmsObject(cms);
        m_cms.getRequestContext().setSiteRoot("");
        // first determine the common root of all open resources
        findRoot(allParamResources);

        m_mustLoadChildren.add(m_rootResource);
        m_mustLoadChildren.addAll(openResources);
        // now load ancestors of all open resources
        for (CmsResource resource : allParamResources) {
            loadAncestors(resource);
        }
        // ensure that all children of ancestors of open resources are loaded
        loadChildren();
        // finally create the beans for the loaded resources
        return createBeans();
    }

    /**
     * Creates a tree entry bean from a resource.<p>
     *
     * @param cms the current CMS context
     * @param resource the resource for which to create the tree entry bean
     *
     * @return the created tree entry bean
     * @throws CmsException if something goes wrong
     */
    protected abstract T createEntry(CmsObject cms, CmsResource resource) throws CmsException;

    /**
     * Finds the common root folder for a collection of resources.<p>
     *
     * @param resources the collection of resources
     *
     * @throws CmsException if something goes wrong
     */
    protected void findRoot(Collection<CmsResource> resources) throws CmsException {

        m_commonRoot = getCommonSite(resources);
        String commonPath = getCommonAncestorPath(resources);
        try {
            m_rootResource = m_cms.readResource(m_commonRoot, m_filter);
        } catch (CmsVfsResourceNotFoundException e) {
            String currentPath = commonPath;
            String lastWorkingPath = null;
            while (m_cms.existsResource(currentPath, m_filter)) {
                lastWorkingPath = currentPath;
                currentPath = CmsResource.getParentFolder(currentPath);
            }
            m_rootResource = m_cms.readResource(lastWorkingPath, m_filter);
            m_commonRoot = lastWorkingPath;
        }
        m_knownResources.add(m_rootResource);
    }

    /**
     * Gets the children of a resource.<p>
     *
     * @param resource the resource for which the children should be read
     * @return the children of the resource
     *
     * @throws CmsException if something goes wrong
     */
    protected List<CmsResource> getChildren(CmsResource resource) throws CmsException {

        return m_cms.getSubFolders(resource.getRootPath(), m_filter);
    }

    /**
     * Gets the common site root of a set of resources.<p>
     * @param resourceSet the resources
     *
     * @return the common site root (may also be the shared folder or root folder)
     */
    protected String getCommonSite(Collection<CmsResource> resourceSet) {

        String commonPath = getCommonAncestorPath(resourceSet);
        String result = null;
        CmsSiteManagerImpl siteManager = OpenCms.getSiteManager();
        if (siteManager.startsWithShared(commonPath)) {
            result = siteManager.getSharedFolder();
        } else {
            String siteRoot = siteManager.getSiteRoot(CmsStringUtil.joinPaths(commonPath, "/"));
            if (siteRoot == null) {
                result = "/";
            } else {
                result = siteRoot;
            }
        }
        return result;

    }

    /**
     * Creates the beans for the loaded resources, and returns the root bean.<p>
     *
     * @return the root bean
     * @throws CmsException if something goes wrong
     */
    private T createBeans() throws CmsException {

        // create the beans for the resources
        Map<CmsResource, T> beans = new HashMap<CmsResource, T>();
        for (CmsResource resource : m_knownResources) {
            T bean = createEntry(m_cms, resource);
            if (bean != null) {
                beans.put(resource, bean);
            }
        }

        // attach beans for child resources to the beans for their parents
        for (Map.Entry<CmsResource, T> entry : beans.entrySet()) {
            CmsResource key = entry.getKey();
            T bean = entry.getValue();
            for (CmsResource child : m_childMap.get(key)) {
                T childEntry = beans.get(child);
                if (childEntry != null) {
                    bean.addChild(childEntry);
                }
            }
        }
        return beans.get(m_rootResource);
    }

    /**
     * Gets the common ancestor path of a collection of resources.<p>
     *
     * @param resources the resources for which to get the ancestor path
     *
     * @return the common ancestor path for the resources
     */
    private String getCommonAncestorPath(Collection<CmsResource> resources) {

        if (resources.isEmpty()) {
            return "/";
        }
        String commonPath = null;
        for (CmsResource resource : resources) {
            commonPath = getCommonAncestorPath(commonPath, resource.getRootPath());
        }
        return commonPath;
    }

    /**
     * Gets the common ancestor of two paths.<p>
     *
     * @param rootPath1 the first path
     * @param rootPath2 the second path
     *
     * @return the common ancestor path
     */
    private String getCommonAncestorPath(String rootPath1, String rootPath2) {

        if (rootPath1 == null) {
            return rootPath2;
        }
        if (rootPath2 == null) {
            return rootPath1;
        }
        rootPath1 = CmsStringUtil.joinPaths("/", rootPath1, "/");
        rootPath2 = CmsStringUtil.joinPaths("/", rootPath2, "/");
        int minLength = Math.min(rootPath1.length(), rootPath2.length());
        int i;
        for (i = 0; i < minLength; i++) {
            char char1 = rootPath1.charAt(i);
            char char2 = rootPath2.charAt(i);
            if (char1 != char2) {
                break;
            }
        }
        String result = rootPath1.substring(0, i);
        if ("/".equals(result)) {
            return result;
        }
        int slashIndex = result.lastIndexOf('/');
        result = result.substring(0, slashIndex);
        return result;
    }

    /**
     * Loads the ancestors of a resource.<p>
     *
     * @param resource the resource for which to load the ancestors
     *
     * @throws CmsException if something goes wrong
     */
    private void loadAncestors(CmsResource resource) throws CmsException {

        CmsResource currentResource = resource;
        while ((currentResource != null) && m_cms.existsResource(currentResource.getStructureId(), m_filter)) {
            if (!m_knownResources.add(currentResource)) {
                break;
            }

            if (CmsStringUtil.comparePaths(currentResource.getRootPath(), m_commonRoot)) {
                break;
            }
            CmsResource parent = m_cms.readParentFolder(currentResource.getStructureId());
            if (parent != null) {
                m_mustLoadChildren.add(parent);

            }
            currentResource = parent;
        }
    }

    /**
     * Loads the children of the already loaded resources.<p>
     *
     * @throws CmsException if something goes wrong
     */
    private void loadChildren() throws CmsException {

        for (CmsResource resource : new ArrayList<CmsResource>(m_knownResources)) {
            if (resource.isFolder()) {
                if (!m_mustLoadChildren.contains(resource)) {
                    continue;
                }
                List<CmsResource> children = getChildren(resource);
                for (CmsResource child : children) {
                    m_knownResources.add(child);
                    m_childMap.put(resource, child);
                }
            }
        }
    }

}
