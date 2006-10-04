/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/relations/CmsRelationDeleteValidator.java,v $
 * Date   : $Date: 2006/10/04 16:01:51 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.relations;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.site.CmsSiteManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Util class to find broken links in a bundle of resources.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.5.3
 */
public class CmsRelationDeleteValidator {

    /**
     * Entry information bean.<p> 
     * 
     * @author Michael Moossen 
     * 
     * @version $Revision: 1.1.2.1 $ 
     * 
     * @since 6.5.3 
     */
    public class InfoEntry {

        /** The original entry name. */
        protected String m_entryName;
        /** If the entry is in other site. */
        protected boolean m_inOtherSite;
        /** If the entry is a sibling. */
        protected boolean m_isSibling;
        /** The resource name. */
        protected String m_resourceName;
        /** The site name. */
        protected String m_siteName;
        /** The site root. */
        protected String m_siteRoot;

        /**
         * Returns all the relations for this entry.<p>
         * 
         * @return a list of {@link CmsRelation} objects
         */
        public List getRelations() {

            return (List)m_brokenRelations.get(m_entryName);
        }

        /**
         * Returns the resource name.<p>
         *
         * @return the resource name
         */
        public String getResourceName() {

            return m_resourceName;
        }

        /**
         * Returns the site name.<p>
         *
         * @return the site name
         */
        public String getSiteName() {

            return m_siteName;
        }

        /**
         * Returns the site root.<p>
         *
         * @return the site root
         */
        public String getSiteRoot() {

            return m_siteRoot;
        }

        /**
         * Returns <code>true</code> if the entry is in other site.<p>
         *
         * @return <code>true</code> if the entry is in other site
         */
        public boolean isInOtherSite() {

            return m_inOtherSite;
        }

        /**
         * Returns <code>true</code> if the entry is a sibling.<p>
         *
         * @return <code>true</code> if the entry is a sibling
         */
        public boolean isSibling() {

            return m_isSibling;
        }
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRelationDeleteValidator.class);

    /** Prefix for mapping sibling's relations. */
    private static final String SIBLING_KEY_PREFIX = "_sibling_prefix_";

    /** The internal computed broken relations map. */
    protected Map m_brokenRelations;

    /** the cms context object. */
    private CmsObject m_cms;

    /**
     * Creates a new helper object.<p>
     * 
     * @param cms the cms object
     * @param resourceNames a list of resource names to be deleted
     * @param includeSiblings if the siblings should also be deleted
     */
    public CmsRelationDeleteValidator(CmsObject cms, List resourceNames, boolean includeSiblings) {

        m_cms = cms;
        m_brokenRelations = getBrokenRelations(resourceNames, includeSiblings);
    }

    /**
     * Returns the information bean for the given entry.<p> 
     * 
     * @param resourceName the entry name
     * 
     * @return the information bean for the given entry
     */
    public InfoEntry getInfoEntry(String resourceName) {

        InfoEntry entry = new InfoEntry();
        String resName = resourceName;
        String siteRoot = m_cms.getRequestContext().getSiteRoot();
        if (resourceName.lastIndexOf(SIBLING_KEY_PREFIX) > 0) {
            resName = resourceName.substring(resourceName.lastIndexOf(SIBLING_KEY_PREFIX) + SIBLING_KEY_PREFIX.length());
            entry.m_isSibling = true;
            if (resName.startsWith(m_cms.getRequestContext().getSiteRoot())) {
                resName = m_cms.getRequestContext().removeSiteRoot(resName);
            } else {
                entry.m_inOtherSite = true;
                siteRoot = CmsSiteManager.getSiteRoot(resName);
                String siteName = siteRoot;
                if (siteRoot != null) {
                    try {
                        m_cms.getRequestContext().saveSiteRoot();
                        m_cms.getRequestContext().setSiteRoot("/");
                        siteName = m_cms.readPropertyObject(siteRoot, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue(
                            siteRoot);
                    } catch (CmsException e) {
                        siteName = siteRoot;
                    } finally {
                        m_cms.getRequestContext().restoreSiteRoot();
                    }
                    resName = resName.substring(siteRoot.length());
                } else {
                    siteName = "/";
                }
                entry.m_siteName = siteName;
            }
        }
        entry.m_siteRoot = siteRoot;
        entry.m_resourceName = resName;
        entry.m_entryName = resourceName;
        return entry;
    }

    /**
     * If no relation would be broken deleting the given resources.<p>
     * 
     * @return <code>true</code> if no relation would be broken deleting the given resources
     */
    public boolean isEmpty() {

        return m_brokenRelations.isEmpty();
    }

    /**
     * @see java.util.Map#keySet()
     * 
     * @return the broken relations key set
     */
    public Set keySet() {

        return m_brokenRelations.keySet();
    }

    /**
     * @see java.util.Map#values()
     * 
     * @return the broken relations value set
     */
    public Collection values() {

        return m_brokenRelations.values();
    }

    /**
     * Returns a map of where each entry has as key a name of a resource to be deleted,
     * and value a list of relations that would be broken.<p> 
     * 
     * The keys for non-siblings have following format:
     * <code>file root path</code>.<p>
     *  
     * The keys for siblings have following format:
     * <code>original file root path + PREFIX_SIBLING + sibling root path</code>.<p>
     * 
     * The values are {@link CmsRelation} objects.<p>
     * 
     * @param cms the cms object
     * @param resourceNames a list of resource names to be deleted
     * @param includeSiblings if the siblings should also be deleted
     * 
     * @return a map of broken relations
     */
    private Map getBrokenRelations(List resourceNames, boolean includeSiblings) {

        Map brokenRelations = new HashMap();
        Set resources = new HashSet();
        // expand the folders to single resources
        String site = m_cms.getRequestContext().getSiteRoot();
        try {
            m_cms.getRequestContext().saveSiteRoot();
            m_cms.getRequestContext().setSiteRoot("/");
            List resourceList = new ArrayList();
            Iterator itResources = resourceNames.iterator();
            while (itResources.hasNext()) {
                // get the root path
                String resName = m_cms.getRequestContext().addSiteRoot(site, (String)itResources.next());
                try {
                    CmsResource resource = m_cms.readResource(resName);
                    resourceList.add(resource);
                    if (resource.isFolder()) {
                        resourceList.addAll(m_cms.readResources(resName, CmsResourceFilter.ALL, true));
                    }
                } catch (CmsException e) {
                    // should never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e);
                    }
                }
            }
            
            // collect the root paths
            itResources = resourceList.iterator();
            while (itResources.hasNext()) {
                CmsResource resource = (CmsResource)itResources.next();
                resources.add(resource.getRootPath());
            }
            
            if (Boolean.valueOf(includeSiblings).booleanValue()) {
                // expand the siblings
                itResources = new ArrayList(resourceList).iterator();
                while (itResources.hasNext()) {
                    CmsResource resource = (CmsResource)itResources.next();
                    try {
                        if (!resource.isFolder() && resource.getSiblingCount() > 1) {
                            Iterator itSiblings = m_cms.readSiblings(resource.getRootPath(), CmsResourceFilter.IGNORE_EXPIRATION).iterator();
                            while (itSiblings.hasNext()) {
                                CmsResource sibling = (CmsResource)itSiblings.next();
                                if (!resources.contains(sibling.getRootPath())) {
                                    String siblingKey = resource.getRootPath() + SIBLING_KEY_PREFIX + sibling.getRootPath();
                                    resources.add(sibling.getRootPath());
                                    // be careful mixing string and resources in one list
                                    resourceList.add(siblingKey);
                                }
                            }
                        }
                    } catch (CmsException e) {
                        // should never happen
                        if (LOG.isErrorEnabled()) {
                            LOG.error(e);
                        }
                    }
                }
            }

            // check every resource
            itResources = resourceList.iterator();
            while (itResources.hasNext()) {
                String resName = null;
                Object item = itResources.next();
                if (item instanceof CmsResource) {
                    CmsResource resource = (CmsResource)item;
                    resName = resource.getRootPath();
                } else {
                    resName = (String)item;
                }
                String resourceName = resName;
                if (resourceName.lastIndexOf(SIBLING_KEY_PREFIX) > 0) {
                    resourceName = resName.substring(resName.lastIndexOf(SIBLING_KEY_PREFIX)
                        + SIBLING_KEY_PREFIX.length());
                }
                try {
                    Iterator it = m_cms.getRelationsForResource(resourceName, CmsRelationFilter.SOURCES).iterator();
                    while (it.hasNext()) {
                        CmsRelation relation = (CmsRelation)it.next();
                        String relationName = relation.getSourcePath();
                        // add only if the source is not to be deleted too
                        if (!resources.contains(relationName)) {
                            List broken = (List)brokenRelations.get(resName);
                            if (broken == null) {
                                broken = new ArrayList();
                                brokenRelations.put(resName, broken);
                            }
                            broken.add(relation);
                        }
                    }
                } catch (CmsException e) {
                    // should never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e);
                    }
                }
            }
        } finally {
            m_cms.getRequestContext().restoreSiteRoot();
        }
        return brokenRelations;
    }
}
