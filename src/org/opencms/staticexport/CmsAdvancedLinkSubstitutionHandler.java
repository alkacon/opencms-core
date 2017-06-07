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

package org.opencms.staticexport;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
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
 * @since 7.5.0
 *
 * @see CmsLinkManager#substituteLink(org.opencms.file.CmsObject, String, String, boolean)
 *      for the method where this handler is used.
 */
public class CmsAdvancedLinkSubstitutionHandler extends CmsDefaultLinkSubstitutionHandler {

    /** Filename of the link exclude definition file. */
    private static final String LINK_EXCLUDE_DEFINIFITON_FILE = "/system/shared/linkexcludes";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAdvancedLinkSubstitutionHandler.class);

    /** XPath for link exclude in definition file. */
    private static final String XPATH_LINK = "link";

    /**
     * @see org.opencms.staticexport.CmsDefaultLinkSubstitutionHandler#getLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public String getLink(CmsObject cms, String link, String siteRoot, boolean forceSecure) {

        if (isExcluded(cms, link)) {
            return link;
        }
        return super.getLink(cms, link, siteRoot, forceSecure);
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

        // check for malformed URI
        try {
            uri = new URI(targetUri);
            path = uri.getPath();
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_MALFORMED_URI_1, targetUri), e);
            }
            return null;
        }
        // opaque URI
        if (uri.isOpaque()) {
            return null;
        }

        // in case the target is the workplace UI
        if (CmsLinkManager.isWorkplaceUri(uri)) {
            return null;
        }
        if (isExcluded(cms, path)) {
            return null;
        }

        return super.getRootPath(cms, targetUri, basePath);
    }

    /**
     * Returns if the given path starts with an exclude prefix.<p>
     *
     * @param cms the cms context
     * @param path the path to check
     *
     * @return <code>true</code> if the given path starts with an exclude prefix
     */
    protected boolean isExcluded(CmsObject cms, String path) {

        List<String> excludes = getExcludes(cms);
        // now check if the current link start with one of the exclude links
        for (int i = 0; i < excludes.size(); i++) {
            if (path.startsWith(excludes.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the exclude prefix list.<p>
     *
     * @param cms the cms context
     *
     * @return the exclude prefix list
     */
    private List<String> getExcludes(CmsObject cms) {

        // get the list of link excludes form the cache if possible
        CmsVfsMemoryObjectCache cache = CmsVfsMemoryObjectCache.getVfsMemoryObjectCache();
        @SuppressWarnings("unchecked")
        List<String> excludes = (List<String>)cache.getCachedObject(cms, LINK_EXCLUDE_DEFINIFITON_FILE);
        if (excludes == null) {
            // nothing found in cache, so read definition file and store the result in cache
            excludes = readLinkExcludes(cms);
            cache.putCachedObject(cms, LINK_EXCLUDE_DEFINIFITON_FILE, excludes);
        }
        return excludes;
    }

    /**
     * Reads the link exclude definition file and extracts all excluded links stored in it.<p>
     *
     * @param cms the current CmsObject
     * @return list of Strings, containing link exclude paths
     */
    private List<String> readLinkExcludes(CmsObject cms) {

        List<String> linkExcludes = new ArrayList<String>();

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
}