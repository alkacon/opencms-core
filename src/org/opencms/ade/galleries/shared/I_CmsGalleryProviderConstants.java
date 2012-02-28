/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Shared constants interface.<p>
 * 
 * @since 8.0.0
 */
public interface I_CmsGalleryProviderConstants {

    /** Gallery mode constants. */
    enum GalleryMode implements IsSerializable {

        /** The advanced direct edit mode. */
        ade(GalleryTabId.cms_tab_types, GalleryTabId.cms_tab_galleries, GalleryTabId.cms_tab_categories,
        GalleryTabId.cms_tab_search),

        /** The FCKEditor mode. */
        editor(GalleryTabId.cms_tab_galleries, GalleryTabId.cms_tab_categories, GalleryTabId.cms_tab_search),

        /** The explorer mode. */
        view(GalleryTabId.cms_tab_types, GalleryTabId.cms_tab_galleries, GalleryTabId.cms_tab_vfstree,
        GalleryTabId.cms_tab_categories, GalleryTabId.cms_tab_search),

        /** The widget mode. */
        widget(GalleryTabId.cms_tab_galleries, GalleryTabId.cms_tab_vfstree, GalleryTabId.cms_tab_categories,
        GalleryTabId.cms_tab_search);

        /** The configuration. */
        private GalleryTabId[] m_tabs;

        /** Constructor.<p>
         *
         * @param tabs the configuration
         */
        private GalleryMode(GalleryTabId... tabs) {

            m_tabs = tabs;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public GalleryTabId[] getTabs() {

            return m_tabs;
        }

    }

    /** Tab ids used for tab configuration. */
    public enum GalleryTabId implements IsSerializable {

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

        /** The id for types tab. */
        cms_tab_types,

        /** The id for vfs-tree tab. */
        cms_tab_vfstree;
    }

    /** Image parameteres. */
    public enum ImageParams implements IsSerializable {

        /** The last modified date. */
        dateLastModified,

        /** The image file name. */
        file_name,

        /** The file size. */
        file_size,

        /** Image type. */
        file_type,

        /** The image height. */
        height,

        /** The image path. */
        path,

        /** Property title. */
        title,

        /** The image width. */
        width;

    }

    /** Request parameter name constants. */
    public enum ReqParam {

        /** The current element. */
        currentelement,

        /** Generic data parameter. */
        data,

        /** The dialog mode. */
        dialogmode,

        /** The widget field id. */
        fieldid,

        /** The gallery path. */
        gallerypath,

        /** The gallery tab id. */
        gallerytabid,

        /** The widget field id hash. */
        hashid,

        /** The current locale. */
        locale,

        /** The edited resource. */
        resource,

        /** The tabs configuration, which tabs should be displayed. */
        tabs,

        /** The available types for the gallery dialog. */
        types;
    }

    /** Sorting parameters. */
    public enum SortParams implements IsSerializable {

        /** Date last modified ascending. */
        dateLastModified_asc,

        /** Date last modified descending. */
        dateLastModified_desc,

        /** Resource path ascending sorting. */
        path_asc,

        /** Resource path descending sorting.*/
        path_desc,

        /** Title ascending sorting. */
        title_asc,

        /** Title descending sorting. */
        title_desc,

        /** Tree.*/
        tree,

        /** Resource type ascending sorting. */
        type_asc,

        /** Resource type descending sorting. */
        type_desc;
    }

    /** The request attribute name for the close link. */
    String ATTR_CLOSE_LINK = "closeLink";

    /** The id for the HTML div containing the gallery dialog. */
    String GALLERY_DIALOG_ID = "galleryDialog";

    /** The widget field id. */
    String KEY_FIELD_ID = "fieldId";

    /** The widget field id hash. */
    String KEY_HASH_ID = "hashId";

    /** The key for the flag which controls whether the select button should be shown. */
    String KEY_SHOW_SELECT = "showSelect";

    /** Path to the host page. */
    String VFS_OPEN_GALLERY_PATH = "/system/modules/org.opencms.ade.galleries/gallery.jsp";
}