/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/I_CmsLinkSubstitutionHandler.java,v $
 * Date   : $Date: 2007/08/29 13:30:25 $
 * Version: $Revision: 1.1 $
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
 * @version $Revision: 1.1 $ 
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
    String substituteLink(CmsObject cms, String link, String siteRoot, boolean forceSecure);
}
