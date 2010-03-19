/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/I_CmsGalleryProviderConstants.java,v $
 * Date   : $Date: 2010/03/19 10:11:54 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.galleries.shared;

/**
 * Constant interface for {@link org.opencms.ade.galleries.CmsGalleryProvider} and {@link org.opencms.ade.galleries.client.util.CmsGalleryProvider}.<p>
 * 
 * @author Polina Smagina 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.ade.sitemap.CmsSitemapProvider
 * @see org.opencms.ade.sitemap.client.util.CmsSitemapProvider
 */
public interface I_CmsGalleryProviderConstants {

    /** Name of the used dictionary. */
    String DICT_NAME = "org.opencms.ade.galleries.core";

    /** Path to the host page. */
    String VFS_OPEN_GALLERY_PATH = "system/modules/org.opencms.ade.galleries/testVfs.jsp";

    /** Gallery mode constants. */
    enum GalleryMode {

        /** The advanced direct edit mode. */
        ade,

        /** The FCKEditor mode. */
        editor,

        /** The sitemap editor mode. */
        sitemap,

        /** The explorer mode. */
        view,

        /** The widget mode. */
        widget;
    }

    /** Request parameter name constants. */
    public enum ReqParam {
        //TODO: clean up the enum, remove not used params

        //        /** The action of execute. */
        //        action,

        //        /** The current element. */
        //        currentelement,

        /** Generic data parameter. */
        data,

        /** The dialog mode. */
        dialogmode,

        /** The gallery path. */
        gallerypath,
        //        /** The current gallery item parameter. */
        //        galleryitem,

        //        /** Specific image data parameter. */
        //        imagedata,

        //        /** The path to the editor plugin script. */
        //        integrator,

        /** The current locale. */
        locale,

        /** The tabs configuration, which tabs should be displayed. */
        tabs,

        /** The available types for the gallery dialog. */
        types;

    }

    /** Tab ids used for tab configuration. */
    public enum GalleryTabId {

        /** The id for categories tab. */
        cms_tab_categories,

        /** The id for containerpage tab. */
        cms_tab_containerpage,

        /** The id for galleries tab. */
        cms_tab_galleries,

        /** The id for types tab. */
        cms_tab_results,

        /** The id for search tab. */
        cms_tab_search,

        /** The id for sitemap tab. */
        cms_tab_sitemap,

        /** The id for types tab. */
        cms_tab_types,

        /** The id for vfs-tree tab. */
        cms_tab_vfstree
    }
}