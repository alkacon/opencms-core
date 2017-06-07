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

import org.opencms.file.CmsObject;

/**
 * Allows to configure customized link substitution behavior.<p>
 *
 * Using this handler, you can completely customize the behavior of the link substitution.<p>
 *
 * This handler is plugged into
 * {@link CmsLinkManager#substituteLink(org.opencms.file.CmsObject, String, String, boolean)},
 * which is the central method to calculate links from VFS paths,
 * used by the <code>&lt;cms:link /&gt;</code> tag and the rest of the OpenCms core.<p>
 *
 * Moreover, this handler is plugged into
 * {@link CmsLinkManager#getRootPath(CmsObject, String, String)},
 * which is the reverse method to calculate a VFS root path from a link.<p>
 *
 * For any implementation of this interface you must ensure the following:
 * <pre>
 *     // path: String that represents a valid VFS resource root path
 *     // cms:  a valid OpenCms user context
 *     String httpLink = OpenCms.getLinkManager().substituteLinkForRootPath(cms, path);
 *     String vfsPath = OpenCms.getLinkManager().getRootPath(cms, httpLink);
 *     path.equals(vfsPath); // this must be true!
 * </pre>
 *
 * The default implementation of this interface is {@link CmsDefaultLinkSubstitutionHandler}.<p>
 *
 * @since 7.0.2
 *
 * @see CmsLinkManager#substituteLink(org.opencms.file.CmsObject, String, String, boolean)
 *      for the method where this handler is used to create a link from a VFS root path
 * @see CmsLinkManager#getRootPath(CmsObject, String, String)
 *      for the method where this handler is used to create VFS root path from a link
 *
 * @see CmsDefaultLinkSubstitutionHandler
 *      for the default implementation of this interface
 */
public interface I_CmsLinkSubstitutionHandler {

    /**
     * Returns a link <i>from</i> the URI stored in the provided OpenCms user context
     * <i>to</i> the VFS resource indicated by the given <code>link</code> and <code>siteRoot</code>,
     * for use on web pages.<p>
     *
     * The result should be an absolute link that contains the configured context path and
     * servlet name, and in the case of the "online" project it will also be rewritten according to
     * to the configured static export settings.<p>
     *
     * In case <code>link</code> is a relative URI, the current URI contained in the provided
     * OpenCms user context <code>cms</code> is normally used to make the relative <code>link</code> absolute.<p>
     *
     * The provided <code>siteRoot</code> is assumed to be the "home" of the link.
     * In case the current site of the given OpenCms user context <code>cms</code> is different from the
     * provided <code>siteRoot</code>, the full server prefix is appended to the result link.<p>
     *
     * A server prefix is also added if
     * <ul>
     *   <li>the link is contained in a normal document and the link references a secure document</li>
     *   <li>the link is contained in a secure document and the link references a normal document</li>
     * </ul>
     *
     * Please note the above text describes the default behavior as implemented by
     * {@link CmsDefaultLinkSubstitutionHandler}, which can be fully customized using this handler interface.<p>
     *
     * @param cms the current OpenCms user context
     * @param link the link to process which is assumed to point to a VFS resource, with optional parameters
     * @param siteRoot the site root of the <code>link</code>
     * @param forceSecure if <code>true</code> generates always an absolute URL (with protocol and server name) for secure links
     *
     * @return a link <i>from</i> the URI stored in the provided OpenCms user context
     *      <i>to</i> the VFS resource indicated by the given <code>link</code> and <code>siteRoot</code>
     *
     * @see #getRootPath(CmsObject, String, String) for the reverse function, which creates a VFS
     */
    String getLink(CmsObject cms, String link, String siteRoot, boolean forceSecure);

    /**
     * Returns a link <i>from</i> the URI stored in the provided OpenCms user context
     * <i>to</i> the VFS resource indicated by the given <code>link</code> and <code>siteRoot</code>,
     * for use on web pages.<p>
     *
     * The result should be an absolute link that contains the configured context path and
     * servlet name, and in the case of the "online" project it will also be rewritten according to
     * to the configured static export settings.<p>
     *
     * In case <code>link</code> is a relative URI, the current URI contained in the provided
     * OpenCms user context <code>cms</code> is normally used to make the relative <code>link</code> absolute.<p>
     *
     * The provided <code>siteRoot</code> is assumed to be the "home" of the link.
     * In case the current site of the given OpenCms user context <code>cms</code> is different from the
     * provided <code>siteRoot</code>, the full server prefix is appended to the result link.<p>
     *
     * A server prefix is also added if
     * <ul>
     *   <li>the link is contained in a normal document and the link references a secure document</li>
     *   <li>the link is contained in a secure document and the link references a normal document</li>
     * </ul>
     *
     * Please note the above text describes the default behavior as implemented by
     * {@link CmsDefaultLinkSubstitutionHandler}, which can be fully customized using this handler interface.<p>
     *
     * @param cms the current OpenCms user context
     * @param link the link to process which is assumed to point to a VFS resource, with optional parameters
     * @param siteRoot the site root of the <code>link</code>
     * @param targetDetailPage the target detail page, in case of linking to a specific detail page
     * @param forceSecure if <code>true</code> generates always an absolute URL (with protocol and server name) for secure links
     *
     * @return a link <i>from</i> the URI stored in the provided OpenCms user context
     *      <i>to</i> the VFS resource indicated by the given <code>link</code> and <code>siteRoot</code>
     *
     * @see #getRootPath(CmsObject, String, String) for the reverse function, which creates a VFS
     */
    String getLink(CmsObject cms, String link, String siteRoot, String targetDetailPage, boolean forceSecure);

    /**
     * Returns the resource root path in the OpenCms VFS for the given target URI link, or <code>null</code> in
     * case the link points to an external site.<p>
     *
     * The default implementation applies the following transformations to the link:<ul>
     * <li>In case the link starts with a VFS prefix (for example <code>/opencms/opencms</code>,
     *      this prefix is removed from the result
     * <li>In case the link is not a root path, the current site root is appended to the result.<p>
     * <li>In case the link is relative, it will be made absolute using the given absolute <code>basePath</code>
     *      as starting point.<p>
     * <li>In case the link contains a server schema (for example <code>http://www.mysite.de/</code>),
     *      which points to a configured site in OpenCms, the server schema is replaced with
     *      the root path of the site.<p>
     * <li>In case the link points to an external site, or in case it is not a valid URI,
     *      then <code>null</code> is returned.<p>
     * </ul>
     *
     * Please note the above text describes the default behavior as implemented by
     * {@link CmsDefaultLinkSubstitutionHandler}, which can be fully customized using this handler interface.<p>
     *
     * @param cms the current users OpenCms context
     * @param targetUri the target URI link
     * @param basePath path to use as base in case the target URI is relative (can be <code>null</code>)
     *
     * @return the resource root path in the OpenCms VFS for the given target URI link, or <code>null</code> in
     *      case the link points to an external site
     *
     * @see #getLink(CmsObject, String, String, boolean) for the reverse function, which creates a link
     *      form a VFS resource path
     */
    String getRootPath(CmsObject cms, String targetUri, String basePath);
}