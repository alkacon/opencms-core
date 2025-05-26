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

package org.opencms.ui.util;

import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.main.CmsPermalinkResourceHandler;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsResourceUtil;

import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;

/**
 * Creates clickable previews for images and other files.
 */
public final class CmsGalleryFilePreview {

    /** The height of the preview images. */
    public static final int IMAGE_HEIGHT = 170;

    /** The width of the preview images. */
    public static final int IMAGE_WIDTH = 200;

    /**
     * Creates a clickable file preview.<p>
     * @param resource the resource to create the preview for
     * @param resourceUtil the resource util
     *
     * @return the clickable file preview
     */
    public static Component createClickableFile(CmsResource resource, CmsResourceUtil resourceUtil) {

        Component preview = isTypeImage(resource)
        ? createClickableImage(resource)
        : createClickableOther(resource, resourceUtil);
        preview.setWidth(IMAGE_WIDTH + "px");
        preview.setHeight(IMAGE_HEIGHT + "px");
        return preview;
    }

    /**
     * Gets the scaling query string for the preview.
     * @param highres if true, generates high-resolution scaling query string
     * @return the scaling parameters
     */
    public static String getScaleQueryString(boolean highres) {

        return "?__scale=" + getScaleParameter(highres);
    }

    /**
     * Utility function to create a clickable image.<p>
     * @param resource the resource
     *
     * @return the clickable image
     */
    private static Label createClickableImage(CmsResource resource) {

        String image = "<img width=\""
            + IMAGE_WIDTH
            + "px\" height=\""
            + IMAGE_HEIGHT
            + "px\" src=\""
            + CmsGalleryFilePreview.getScaleUri(resource, false)
            + "\""
            + " srcset=\""
            + CmsGalleryFilePreview.getScaleUri(resource, true)
            + " 2x"
            + "\" "
            + " onerror='cmsJsFunctions.handleBrokenImage(this)' "
            + " >";
        String a = "<a target=\"_blank\" href=\""
            + CmsGalleryFilePreview.getPermanentUri(resource)
            + "\">"
            + image
            + "</a>";
        String div = "<div class=\""
            + OpenCmsTheme.GALLERY_PREVIEW_IMAGE
            + "\" style=\"width:"
            + IMAGE_WIDTH
            + "px;height:"
            + IMAGE_HEIGHT
            + "px;\">"
            + a
            + "</div>";
        Label label = new Label(div);
        label.setContentMode(ContentMode.HTML);
        return label;
    }

    /**
     * Utility function to create a clickable preview for files that are not images.
     * @param resource the resource
     * @param resourceUtil the resource util
     *
     * @return the clickable preview
     */
    private static Link createClickableOther(CmsResource resource, CmsResourceUtil resourceUtil) {

        CmsCssIcon cssIcon = (CmsCssIcon)resourceUtil.getSmallIconResource();
        String caption = "<div style=\"width:"
            + IMAGE_WIDTH
            + "px;height:"
            + IMAGE_HEIGHT
            + "px;display: flex; justify-content: center; align-items: center;\"><span class=\""
            + cssIcon.getStyleName()
            + "\" style=\"transform: scale(4);\"></span></div>";
        Link link = new Link(caption, new ExternalResource(CmsGalleryFilePreview.getPermanentUri(resource)));
        link.setCaptionAsHtml(true);
        link.setTargetName("_blank");
        return link;
    }

    /**
     * Creates a permanent URI for a file preview.<p>
     *
     * @param resource the CMS resource
     * @return the permanent URI
     */
    private static String getPermanentUri(CmsResource resource) {

        String structureId = resource.getStructureId().toString();
        String extension = CmsResource.getExtension(resource.getRootPath());
        String suffix = (extension != null) ? "." + extension : "";
        String permalink = CmsStringUtil.joinPaths(
            OpenCms.getSystemInfo().getOpenCmsContext(),
            CmsPermalinkResourceHandler.PERMALINK_HANDLER,
            structureId) + suffix;
        return permalink;
    }

    /**
     * Gets the scaling parameters for the preview.
     *
     * @param highres if true, generates high-resolution scaling parameters
     * @return the scaling parameters
     */
    private static String getScaleParameter(boolean highres) {

        int m = highres ? 2 : 1;
        String suffix = highres ? ",q:85" : "";
        return "t:9,w:" + (m * IMAGE_WIDTH) + ",h:" + (m * IMAGE_HEIGHT) + suffix;

    }

    /**
     * Creates a permanent URI for a scaled preview image.<p>
     *
     * @param resource the CMS resource
     * @param highres if true, generate high resolution scaling uri
     * @return the scale URI
     */
    private static String getScaleUri(CmsResource resource, boolean highres) {

        String paramTimestamp = "&timestamp=" + System.currentTimeMillis();
        return getPermanentUri(resource) + getScaleQueryString(highres) + paramTimestamp;
    }

    /**
     * Returns whether the actual resource is an image.
     * @param resource the resource
     * @return whether the actual resource is an image
     */
    private static boolean isTypeImage(CmsResource resource) {

        return OpenCms.getResourceManager().getResourceType(resource) instanceof CmsResourceTypeImage;
    }
}
