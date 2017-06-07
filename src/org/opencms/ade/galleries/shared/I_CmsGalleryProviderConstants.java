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
        ade(CmsGalleryTabConfiguration.resolve(CmsGalleryTabConfiguration.TC_ADE_ADD)),

        /** The mode for showing all galleries in ADE. */
        adeView(CmsGalleryTabConfiguration.resolve(CmsGalleryTabConfiguration.TC_SELECT_DOC)),

        /** The wysiwyg editor mode. */
        editor(CmsGalleryTabConfiguration.resolve(
            CmsGalleryTabConfiguration.TC_SELECT_DOC) /* may be overwritten by configuration */),

        /** The explorer mode. */
        view(CmsGalleryTabConfiguration.resolve(CmsGalleryTabConfiguration.TC_SELECT_DOC)),

        /** The widget mode. */
        widget(CmsGalleryTabConfiguration.resolve(CmsGalleryTabConfiguration.TC_SELECT_DOC));

        /** The configuration. */
        private CmsGalleryTabConfiguration m_tabConfig;

        /** Constructor.<p>
         *
         * @param tabConfig the tab configuration
         */
        private GalleryMode(CmsGalleryTabConfiguration tabConfig) {

            m_tabConfig = tabConfig;
        }

        /**
         * Returns the configured tabs.<p>
         *
         * @return the configured tabs
         */
        public GalleryTabId[] getTabs() {

            GalleryTabId[] tabs = new GalleryTabId[m_tabConfig.getTabs().size()];
            for (int i = 0; i < tabs.length; i++) {
                tabs[i] = m_tabConfig.getTabs().get(i);
            }
            return tabs;
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

        /** The id for sitemap tab. */
        cms_tab_sitemap,

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

    /** Configuration key. */
    String CONFIG_CURRENT_ELEMENT = "currentelement";

    /** Configuration key. */
    String CONFIG_GALLERY_MODE = "gallerymode";

    /** Configuration key. */
    String CONFIG_GALLERY_NAME = "galleryname";

    /** Configuration key. */
    String CONFIG_GALLERY_PATH = "gallerypath";

    /** Configuration key. */
    String CONFIG_GALLERY_STORAGE_PREFIX = "galleryprefix";

    /** Configuration key. */
    String CONFIG_GALLERY_TYPES = "gallerytypes";

    /** Configuration key. */
    String CONFIG_IMAGE_FORMAT_NAMES = "imageformatnames";

    /** Configuration key. */
    String CONFIG_IMAGE_FORMATS = "imageformats";

    /** Configuration key. */
    String CONFIG_LOCALE = "locale";

    /** Configuration key. */
    String CONFIG_REFERENCE_PATH = "resource";

    /** Configuration key. */
    String CONFIG_RESOURCE_TYPES = "resourcetypes";

    /** Configuration key. */
    String CONFIG_SEARCH_TYPES = "searchtypes";

    /** Configuration key. */
    String CONFIG_SHOW_SITE_SELECTOR = "showsiteselector";

    /** Configuration key. */
    String CONFIG_START_FOLDER = "startfolder";

    /** Configuration key. */
    String CONFIG_START_SITE = "startsite";

    /** Configuration key. */
    String CONFIG_TAB_CONFIG = "tabconfig";

    /** The key for the tree token. */
    String CONFIG_TREE_TOKEN = "treeToken";

    /** Configuration key. */
    String CONFIG_UPLOAD_FOLDER = "uploadfolder";

    /** Configuration key. */
    String CONFIG_USE_FORMATS = "useformats";

    /** The id for the HTML div containing the gallery dialog. */
    String GALLERY_DIALOG_ID = "galleryDialog";

    /** The widget field id. */
    String KEY_FIELD_ID = "fieldId";

    /** The widget field id hash. */
    String KEY_HASH_ID = "hashId";

    /** The key for the flag which controls whether the select button should be shown. */
    String KEY_SHOW_SELECT = "showSelect";

    /** The parameter used to tell the gallery dialog to use the standard set of resource types from the VfsFileWidget. */
    String PARAM_USE_LINK_DEFAULT_TYPES = "useLinkDefaultTypes";

    /** The folder resource type name. */
    String RESOURCE_TYPE_FOLDER = "folder";

    /** Sitemap tree state session attribute name prefix. */
    String TREE_SITEMAP = "sitemap";

    /** VFS tree state session attribute name prefix. */
    String TREE_VFS = "vfs";

    /** Path to the host page. */
    String VFS_OPEN_GALLERY_PATH = "/system/workplace/commons/gallery.jsp";

    /** Option to enable/disable gallery selection. */
    String CONFIG_GALLERIES_SELECTABLE = "galleriesSelectable";

    /** Option to disable result selection if set to false. */
    String CONFIG_RESULTS_SELECTABLE = "resultsSelectable";

}