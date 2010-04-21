/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/Attic/CmsSitemapService.java,v $
 * Date   : $Date: 2010/04/21 14:29:20 $
 * Version: $Revision: 1.6 $
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

package org.opencms.ade.sitemap;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.ade.sitemap.shared.I_CmsSitemapChange;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.shared.rpc.CmsRpcException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.sitemap.CmsSitemapEntry;
import org.opencms.xml.sitemap.CmsXmlSitemap;
import org.opencms.xml.sitemap.CmsXmlSitemapFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Handles all RPC services related to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.CmsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService
 * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync
 */
public class CmsSitemapService extends CmsGwtService implements I_CmsSitemapService {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapService.class);

    /** Serialization uid. */
    private static final long serialVersionUID = -7136544324371767330L;

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#createSubsitemap(java.lang.String, java.lang.String)
     */
    public String createSubsitemap(String sitemapUri, String path) {

        // TODO: Auto-generated method stub
        // TODO: problem with locales, 
        // - if it applies to only one locale, what happens if the later language changes to the subsitemap?
        // - if it applies to all locales, how to keep the entry point consistent?
        // anyhow, should not be keep only one language variation with language root folders? 
        return null;
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getChildren(java.lang.String)
     */
    public List<CmsClientSitemapEntry> getChildren(String root) throws CmsRpcException {

        try {
            CmsSitemapEntry entry = OpenCms.getSitemapManager().getEntryForUri(getCmsObject(), root);
            List<CmsSitemapEntry> subEntries = entry.getSubEntries();
            int size = subEntries.size();
            List<CmsClientSitemapEntry> children = new ArrayList<CmsClientSitemapEntry>(size);
            for (int i = 0; i < size; i++) {
                CmsSitemapEntry child = subEntries.get(i);
                children.add(toClientEntry(child, i));
            }
            return children;
        } catch (Throwable e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getEntry(String)
     */
    public CmsClientSitemapEntry getEntry(String root) throws CmsRpcException {

        try {
            return toClientEntry(OpenCms.getSitemapManager().getEntryForUri(getCmsObject(), root), -1);
        } catch (Throwable e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#getRoots(java.lang.String)
     */
    public List<CmsClientSitemapEntry> getRoots(String sitemapUri) throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            CmsXmlSitemap xml = CmsXmlSitemapFactory.unmarshal(cms, cms.readResource(sitemapUri));
            String sitePath = cms.getRequestContext().removeSiteRoot(
                xml.getSitemap(cms, Locale.ENGLISH).getEntryPoint());
            boolean isSubsitemap = false;
            List<CmsClientSitemapEntry> roots = null;
            if (isSubsitemap) {
                roots = getChildren(sitePath);
            } else {
                roots = new ArrayList<CmsClientSitemapEntry>();
                CmsClientSitemapEntry entry = getEntry(sitePath);
                String name = CmsResource.getName(sitePath);
                if (name.endsWith("/")) {
                    name = name.substring(0, name.length() - 1);
                }
                entry.setName(name);
                roots.add(entry);
            }
            for (CmsClientSitemapEntry root : roots) {
                root.setChildren(getChildren(root.getSitePath()));
            }
            return roots;
        } catch (Throwable e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(e.getLocalizedMessage());
        }
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#mergeSubsitemap(java.lang.String, java.lang.String)
     */
    public void mergeSubsitemap(String sitemapUri, String path) {

        // TODO: 
    }

    /**
     * @see org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapService#save(java.lang.String, List)
     */
    public void save(String sitemapUri, List<I_CmsSitemapChange> changes) {

        // TODO:
    }

    /**
     * Converts a site entry bean into a JSON object.<p>
     * 
     * @param entry the entry to convert
     * @param position the relative position between its siblings
     * 
     * @return the JSON representation, can be <code>null</code> in case of not enough permissions
     * 
     * @throws CmsException should never happen 
     */
    protected CmsClientSitemapEntry toClientEntry(CmsSitemapEntry entry, int position) throws CmsException {

        CmsClientSitemapEntry clientEntry = new CmsClientSitemapEntry();
        clientEntry.setId(entry.getId());
        clientEntry.setName(entry.getName());
        clientEntry.setTitle(entry.getTitle());
        String vfsPath = "---";
        if (getCmsObject().existsResource(entry.getResourceId())) {
            vfsPath = getCmsObject().getSitePath(getCmsObject().readResource(entry.getResourceId()));
        }
        clientEntry.setVfsPath(vfsPath);
        clientEntry.setProperties(new HashMap<String, String>(entry.getProperties()));
        clientEntry.setSitePath(entry.getSitePath(getCmsObject()));
        clientEntry.setPosition(position);
        return clientEntry;
    }
}
