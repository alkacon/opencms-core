/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/I_CmsLinkSubstitutionHandler.java,v $
 * Date   : $Date: 2007/09/10 13:16:55 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * This handler is plugged into 
 * {@link CmsLinkManager#substituteLink(org.opencms.file.CmsObject, String, String, boolean)},
 * which is the central method to calculate links for the use on web pages. 
 * This method is also used by the <code>&lt;cms:link /&gt;</code> tag.<p> 
 *
 * Using this handler, you can completely customize the behavior of the link substitution.<p>
 * 
 * The default implementation of this interface is {@link CmsDefaultLinkSubstitutionHandler}.<p>
 *
 * @author  Alexander Kandzior 
 *
 * @version $Revision: 1.2 $ 
 * 
 * @since 7.0.2
 * 
 * @see CmsLinkManager#substituteLink(org.opencms.file.CmsObject, String, String, boolean) 
 *      for the method where this handler is used
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
     * OpenCms user context <code>cms</code> is nornally used to make the relative <code>link</code> absolute.<p>
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
     */
    String getLink(CmsObject cms, String link, String siteRoot, boolean forceSecure);

    /**
     * Returns the resource root path for the given target URI in the OpenCms VFS, or <code>null</code> in
     * case the target URI points to an external site.<p>
     * 
     * If the target URI contains no site information, but starts with the opencms context, the context is removed:<pre>
     * /opencms/opencms/system/further_path -> /system/further_path</pre><p>
     * 
     * If the target URI contains no site information, the path will be prefixed with the current site
     * from the provided OpenCms user context:<pre>
     * /folder/page.html -> /sites/mysite/folder/page.html</pre><p>
     *  
     * If the path of the target URI is relative, i.e. does not start with "/", 
     * the path will be prefixed with the current site and the given relative path,
     * then normalized.
     * If no relative path is given, <code>null</code> is returned.
     * If the normalized path is outsite a site, null is returned.<pre>
     * page.html -> /sites/mysite/page.html
     * ../page.html -> /sites/mysite/page.html
     * ../../page.html -> null</pre><p>
     * 
     * If the target URI contains a scheme/server name that denotes an opencms site, 
     * it is replaced by the appropriate site path:<pre>
     * http://www.mysite.de/folder/page.html -> /sites/mysite/folder/page.html</pre><p>
     * 
     * If the target URI contains a scheme/server name that does not match with any site, 
     * or if the URI is opaque or invalid,
     * <code>null</code> is returned:<pre>
     * http://www.elsewhere.com/page.html -> null
     * mailto:someone@elsewhere.com -> null</pre><p>
     * 
     * @param cms the current users OpenCms context
     * @param targetUri the target URI
     * @param basePath path to use as base site for the target URI (can be <code>null</code>)
     * 
     * @return the resource root path for the given target URI in the OpenCms VFS, or <code>null</code> in
     *      case the target URI points to an external site
     */
    String getRootPath(CmsObject cms, String targetUri, String basePath);
}