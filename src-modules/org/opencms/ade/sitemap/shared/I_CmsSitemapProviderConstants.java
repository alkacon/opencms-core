/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/I_CmsSitemapProviderConstants.java,v $
 * Date   : $Date: 2010/04/19 06:39:10 $
 * Version: $Revision: 1.2 $
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

package org.opencms.ade.sitemap.shared;

/**
 * Constant interface for {@link org.opencms.ade.sitemap.CmsSitemapProvider} and {@link org.opencms.ade.sitemap.client.CmsSitemapProvider}.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.CmsSitemapProvider
 * @see org.opencms.ade.sitemap.client.CmsSitemapProvider
 */
public interface I_CmsSitemapProviderConstants {

    /** Name of the used dictionary. */
    String DICT_NAME = "org.opencms.ade.sitemap.core";

    /** The text explaining why you can not edit the sitemap, ie. <code>"Not enough permissions"</code>. */
    String KEY_EDIT = "edit";

    /** Flag to indicate if to display the toolbar, ie. <code>true</code>. */
    String KEY_TOOLBAR = "toolbar";

    /** The type id of the cnt page, ie. <code>13</code>. */
    String KEY_TYPE_CNTPAGE = "cntpage";

    /** The current sitemap URI, ie. <code>/a/b/c.xml</code>. */
    String KEY_URI_SITEMAP = "sitemap";
}