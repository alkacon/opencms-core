/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml;

import org.opencms.file.CmsResource;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.http.client.utils.URIBuilder;

/**
 * Does final postprocessing on a link by cutting off specific path suffixes (e.g. index.html).
 */
public class CmsLinkFinisher {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLinkFinisher.class);

    /** True if the link  finisher is enabled. */
    private boolean m_enabled;

    /** Default file names to remove. */
    private Set<String> m_defaultFileNames;

    /** If a link matches this, it shouldn't be finished. */
    private Pattern m_excludePattern;

    /**
     * Creates a new instance.
     *
     * @param enabled true if it should be enabled
     * @param defaultFileNames the default file names to remove
     * @param exclude regex to prevent certain links from being finished if they match
     */
    public CmsLinkFinisher(boolean enabled, Collection<String> defaultFileNames, String exclude) {

        m_enabled = enabled;
        m_defaultFileNames = Collections.unmodifiableSet(new HashSet<>(defaultFileNames));
        if (exclude != null) {
            try {
                m_excludePattern = Pattern.compile(exclude);
            } catch (PatternSyntaxException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

    }

    /**
     * Transforms the link into a finished format.
     *
     * @param link the link
     * @param full in full mode, remove all configured suffixes, otherwise just remove trailing slashes
     * @return
     */
    public String transformLink(String link, boolean full) {

        if (!m_enabled) {
            return link;
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(link)) {
            return link;
        }
        if (link.startsWith(CmsHistoryResourceHandler.HISTORY_HANDLER)) {
            return link;
        }
        if (m_excludePattern != null) {
            if (m_excludePattern.matcher(link).matches()) {
                return link;
            }
        }
        try {
            URI uri = new URI(link);
            URIBuilder builder = new URIBuilder(uri);
            String path = builder.getPath();
            path = path.replaceFirst("/$", "");
            if (full) {
                if (path.length() > 1) {
                    String name = CmsResource.getName(path);
                    if (m_defaultFileNames.contains(name)) {
                        path = path.substring(0, path.lastIndexOf('/'));
                    }
                }
            }
            // since we usually put the result of link substitution into hrefs, we need to avoid an empty path if there is no host, otherwise the href will link to the current page
            if ((uri.getAuthority() == null) && "".equals(path)) {
                path = "/";
            }
            builder.setPath(path);

            link = builder.toString();
        } catch (URISyntaxException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        return link;
    }

}
