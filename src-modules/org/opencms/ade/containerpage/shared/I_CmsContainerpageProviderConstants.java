/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/shared/Attic/I_CmsContainerpageProviderConstants.java,v $
 * Date   : $Date: 2010/04/12 15:00:37 $
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

package org.opencms.ade.containerpage.shared;

/**
 * Constant interface for {@link org.opencms.ade.containerpage.CmsContainerpageProvider} and {@link org.opencms.ade.containerpage.client.util.CmsContainerpageProvider}.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public interface I_CmsContainerpageProviderConstants {

    /** HTML class used to identify container elements. Has to be identical with {@link org.opencms.jsp.CmsJspTagContainer#CLASS_CONTAINER_ELEMENTS}. */
    String CLASS_CONTAINER_ELEMENTS = "cms_ade_element";

    /** HTML class used to identify sub container elements. Has to be identical with {@link org.opencms.jsp.CmsJspTagContainer#CLASS_SUB_CONTAINER_ELEMENTS}. */
    String CLASS_SUB_CONTAINER_ELEMENTS = "cms_ade_subcontainer";

    /** Name of the used dictionary. */
    String DICT_NAME = "org.opencms.ade.containerpage.core";

    /** Key for container data. This has to be identical with {@link org.opencms.jsp.CmsJspTagContainer#KEY_CONTAINER_DATA}. */
    String KEY_CONTAINER_DATA = "org_opencms_ade_containerpage_containers";

    /** Key for the current URI. */
    String KEY_CURRENT_CONTAINERPAGE_URI = "current_containerpage_uri";

    /** Key for the xml-content editor URI. */
    String KEY_EDITOR_URI = "editor_uri";

    /** Key for no-edit reason. */
    String KEY_NO_EDIT_REASON = "no_edit_reason";

    /** Key for request parameters. */
    String KEY_REQUEST_PARAMS = "request_params";

    /** Key for the sitemap URI. */
    String KEY_SITEMAP_URI = "sitemap_uri";

    /** Key for back-link URI. */
    String KEY_BACKLINK_URI = "backlink_uri";
}
