/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsAdvancedLinkSubstitutionHandler.java,v $
 * Date   : $Date: 2011/03/23 14:52:50 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.staticexport;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSiteMatcher;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Advanced link substitution behavior.<p>
 * You can define additional paths that are always used as external links, even if
 * they point to the same configured site than the OpenCms itself.
 *
 * @author  Michael Emmerich
 *
 * @version $Revision: 1.5 $ 
 * 
 * @since 7.5.0
 * 
 * @see CmsLinkManager#substituteLink(org.opencms.file.CmsObject, String, String, boolean) 
 *      for the method where this handler is used.
 */
public class CmsAdvancedLinkSubstitutionHandler extends CmsDefaultLinkSubstitutionHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAdvancedLinkSubstitutionHandler.class);

    /** Filename of the link exclude definition file. */
    private static final String LINK_EXCLUDE_DEFINIFITON_FILE = "/system/shared/linkexcludes";

    /** XPath for link exclude in definition file. */
    private static final String XPATH_LINK = "link";

    /**
     * Reads the link exclude definition file and extracts all excluded links stored in it.<p>
     * 
     * @param cms the current CmsObject
     * @return list of Strings, containing link exclude paths
     */
    private List readLinkExcludes(CmsObject cms) {

        List linkExcludes = new ArrayList();

        try {
            // get the link exclude file
            CmsResource res = cms.readResource(LINK_EXCLUDE_DEFINIFITON_FILE);
            CmsFile file = cms.readFile(res);
            CmsXmlContent linkExcludeDefinitions = CmsXmlContentFactory.unmarshal(cms, file);

            // get number of excludes
            int count = linkExcludeDefinitions.getIndexCount(XPATH_LINK, Locale.ENGLISH);

            for (int i = 1; i <= count; i++) {

                String exclude = linkExcludeDefinitions.getStringValue(cms, XPATH_LINK + "[" + i + "]", Locale.ENGLISH);
                linkExcludes.add(exclude);

            }

        } catch (CmsException e) {
            LOG.error(e);
        }

        return linkExcludes;
    }

    /**
     * @see org.opencms.staticexport.I_CmsLinkSubstitutionHandler#getRootPath(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    @Override
    public String getRootPath(CmsObject cms, String targetUri, String basePath) {

        if (cms == null) {
            // required by unit test cases
            return targetUri;
        }

        URI uri;
        String path;
        String fragment;
        String query;
        String suffix;

        // malformed uri
        try {
            uri = new URI(targetUri);
            path = uri.getPath();

            fragment = uri.getFragment();
            if (fragment != null) {
                fragment = "#" + fragment;
            } else {
                fragment = "";
            }

            query = uri.getQuery();
            if (query != null) {
                query = "?" + query;
            } else {
                query = "";
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_MALFORMED_URI_1, targetUri), e);
            }
            return null;
        }

        // concatenate fragment and query 
        suffix = fragment.concat(query);

        // opaque URI
        if (uri.isOpaque()) {
            return null;
        }

        // get the list of link excludes form the cache if possible
        CmsVfsMemoryObjectCache cache = CmsVfsMemoryObjectCache.getVfsMemoryObjectCache();
        List excludes = (List)cache.getCachedObject(cms, LINK_EXCLUDE_DEFINIFITON_FILE);
        if (excludes == null) {
            // nothing found in cache, so read definition file and store the result in cache
            excludes = readLinkExcludes(cms);
            cache.putCachedObject(cms, LINK_EXCLUDE_DEFINIFITON_FILE, excludes);
        }
        // now check if the current link start with one of the exclude links
        for (int i = 0; i < excludes.size(); i++) {
            if (path.startsWith((String)excludes.get(i))) {
                return null;
            }
        }

        // absolute URI (i.e. URI has a scheme component like http:// ...)
        if (uri.isAbsolute()) {
            CmsSiteMatcher matcher = new CmsSiteMatcher(targetUri);
            if (OpenCms.getSiteManager().isMatching(matcher)) {
                if (path.startsWith(OpenCms.getSystemInfo().getOpenCmsContext())) {
                    path = path.substring(OpenCms.getSystemInfo().getOpenCmsContext().length());
                }
                if (OpenCms.getSiteManager().isWorkplaceRequest(matcher)) {
                    // workplace URL, use current site root
                    // this is required since the workplace site does not have a site root to set 
                    return cms.getRequestContext().addSiteRoot(path + suffix);
                } else {
                    // add the site root of the matching site
                    return cms.getRequestContext().addSiteRoot(
                        OpenCms.getSiteManager().matchSite(matcher).getSiteRoot(),
                        path + suffix);
                }
            } else {
                return null;
            }
        }

        // relative URI (i.e. no scheme component, but filename can still start with "/") 
        String context = OpenCms.getSystemInfo().getOpenCmsContext();
        if ((context != null) && path.startsWith(context)) {
            // URI is starting with opencms context
            String siteRoot = null;
            if (basePath != null) {
                siteRoot = OpenCms.getSiteManager().getSiteRoot(basePath);
            }

            // cut context from path
            path = path.substring(context.length());

            if (siteRoot != null) {
                // special case: relative path contains a site root, i.e. we are in the root site                
                if (!path.startsWith(siteRoot)) {
                    // path does not already start with the site root, we have to add this path as site prefix
                    return cms.getRequestContext().addSiteRoot(siteRoot, path + suffix);
                } else {
                    // since path already contains the site root, we just leave it unchanged
                    return path + suffix;
                }
            } else {
                // site root is added with standard mechanism
                return cms.getRequestContext().addSiteRoot(path + suffix);
            }
        }

        // URI with relative path is relative to the given relativePath if available and in a site, 
        // otherwise invalid
        if (CmsStringUtil.isNotEmpty(path) && (path.charAt(0) != '/')) {
            if (basePath != null) {
                String absolutePath;
                int pos = path.indexOf("../../galleries/pics/");
                if (pos >= 0) {
                    // HACK: mixed up editor path to system gallery image folder
                    return CmsWorkplace.VFS_PATH_SYSTEM + path.substring(pos + 6) + suffix;
                }
                absolutePath = CmsLinkManager.getAbsoluteUri(path, cms.getRequestContext().addSiteRoot(basePath));
                if (OpenCms.getSiteManager().getSiteRoot(absolutePath) != null) {
                    return absolutePath + suffix;
                }
                // HACK: some editor components (e.g. HtmlArea) mix up the editor URL with the current request URL 
                absolutePath = CmsLinkManager.getAbsoluteUri(path, cms.getRequestContext().getSiteRoot()
                    + CmsWorkplace.VFS_PATH_EDITORS);
                if (OpenCms.getSiteManager().getSiteRoot(absolutePath) != null) {
                    return absolutePath + suffix;
                }
                // HACK: same as above, but XmlContent editor has one path element more
                absolutePath = CmsLinkManager.getAbsoluteUri(path, cms.getRequestContext().getSiteRoot()
                    + CmsWorkplace.VFS_PATH_EDITORS
                    + "xmlcontent/");
                if (OpenCms.getSiteManager().getSiteRoot(absolutePath) != null) {
                    return absolutePath + suffix;
                }
            }

            return null;
        }

        // relative URI (= VFS path relative to currently selected site root)
        if (CmsStringUtil.isNotEmpty(path)) {
            return cms.getRequestContext().addSiteRoot(path) + suffix;
        }

        // URI without path (typically local link)
        return suffix;
    }
}