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

package org.opencms.ui.sitemap;

import org.opencms.ade.sitemap.CmsVfsSitemapService;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.CmsRpcException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Loads node data for the sitemap tree.<p>
 */
public class CmsSitemapTreeDataProvider {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapTreeDataProvider.class);

    /** The CMS context. */
    private CmsObject m_cms;

    /** Cached set of ids of folders without child folders, used for rendering the tree. */
    private Set<CmsUUID> m_foldersWithNoChildFolders;

    /** The locale context. */
    private I_CmsLocaleCompareContext m_localeContext;

    /** The resource at the root of the tree. */
    private CmsResource m_root;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @param root the root resource of the tree
     * @param context the locale context
     */
    public CmsSitemapTreeDataProvider(CmsObject cms, CmsResource root, I_CmsLocaleCompareContext context) {
        m_root = root;
        m_localeContext = context;
        try {
            m_cms = OpenCms.initCmsObject(cms);
            List<CmsResource> folders = m_cms.readResources(
                root,
                CmsResourceFilter.IGNORE_EXPIRATION.addRequireFolder(),
                true);
            Collections.sort(folders, new Comparator<CmsResource>() {

                public int compare(CmsResource arg0, CmsResource arg1) {

                    return arg0.getRootPath().compareTo(arg1.getRootPath());
                }
            });
            CmsResource lastFolder = null;
            Set<CmsUUID> foldersWithNoChildFolders = Sets.newHashSet();

            folders.add(null); // add null as a dummy value so that in the loop below, lastFolder takes all real folders as values
            for (CmsResource folder : folders) {
                if ((lastFolder != null)
                    && ((folder == null)
                        || !(CmsStringUtil.isPrefixPath(lastFolder.getRootPath(), folder.getRootPath())))) {
                    foldersWithNoChildFolders.add(lastFolder.getStructureId());

                }
                lastFolder = folder;
            }
            m_foldersWithNoChildFolders = foldersWithNoChildFolders;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Gets the node data for children of a given node.<p>
     *
     * @param nodeData the node whose children to get
     * @return the children
     */
    public List<CmsSitemapTreeNodeData> getChildren(CmsSitemapTreeNodeData nodeData) {

        CmsClientSitemapEntry entry = nodeData.getClientEntry();

        List<CmsSitemapTreeNodeData> result = Lists.newArrayList();

        try {
            CmsVfsSitemapService svc = getSitemapService();
            CmsClientSitemapEntry ent = svc.getChildren(m_root.getRootPath(), entry.getId(), 1);
            for (CmsClientSitemapEntry subEnt : ent.getSubEntries()) {
                if (subEnt.isInNavigation()
                    && ((subEnt.getDefaultFileId() != null) || subEnt.isNavigationLevelType())) {
                    try {
                        CmsUUID idToRead = subEnt.getId();
                        if (subEnt.getDefaultFileId() != null) {
                            idToRead = subEnt.getDefaultFileId();
                        }
                        Locale l1 = OpenCms.getLocaleManager().getDefaultLocale(
                            svc.getCmsObject(),
                            svc.getCmsObject().readResource(idToRead));
                        Locale l2 = OpenCms.getLocaleManager().getDefaultLocale(
                            svc.getCmsObject(),
                            svc.getCmsObject().readResource(ent.getId(), CmsResourceFilter.IGNORE_EXPIRATION));
                        if (!l1.equals(l2)) {
                            continue;
                        }
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }

                    CmsSitemapTreeNodeData data = new CmsSitemapTreeNodeData(
                        m_localeContext.getRootLocale(),
                        m_localeContext.getComparisonLocale());
                    if (m_foldersWithNoChildFolders.contains(subEnt.getId())) {
                        data.setHasNoChildren(true);
                    }
                    data.setClientEntry(subEnt);
                    try {
                        data.initialize(m_cms);
                        result.add(data);
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage());
                    }
                }
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } catch (CmsRpcException e) {
            LOG.error(e.getLocalizedMessage(), e);

        }
        return result;
    }

    /**
     * Gets the tree data for a resource.<p>
     *
     * @param resource a resource
     * @return the data for the resource
     */
    public CmsSitemapTreeNodeData getData(CmsResource resource) {

        try {
            CmsVfsSitemapService svc = new CmsVfsSitemapService();
            CmsObject cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            cms.getRequestContext().setSiteRoot("");
            svc.setCms(cms);

            CmsClientSitemapEntry ent = svc.getChildren(resource.getRootPath(), resource.getStructureId(), 0);
            CmsSitemapTreeNodeData data = new CmsSitemapTreeNodeData(
                m_localeContext.getRootLocale(),
                m_localeContext.getComparisonLocale());
            data.setClientEntry(ent);
            data.initialize(cms);
            return data;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }

    }

    /**
     * Gets the node data for the tree's root.<p>
     *
     * @return the node data
     */
    public CmsSitemapTreeNodeData getRoot() {

        return getData(m_root);

    }

    /**
     * Gets the sitemap service.<p>
     *
     * @return the sitemap service
     *
     * @throws CmsException if something goes wrong
     */
    public CmsVfsSitemapService getSitemapService() throws CmsException {

        CmsVfsSitemapService svc = new CmsVfsSitemapService();
        CmsObject cms = OpenCms.initCmsObject(m_cms);
        cms.getRequestContext().setSiteRoot("");
        svc.setCms(cms);
        return svc;
    }

}
