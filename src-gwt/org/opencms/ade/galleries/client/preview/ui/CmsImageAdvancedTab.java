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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.client.preview.CmsImagePreviewHandler;
import org.opencms.ade.galleries.client.preview.CmsImagePreviewHandler.Attribute;
import org.opencms.ade.galleries.client.preview.CmsPreviewUtil;
import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.util.CmsJSONMap;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;

import java.util.Map;

/**
 * Advanced image tag properties tab, use in editor mode only.<p>
 *
 * @since 8.0.
 */
public class CmsImageAdvancedTab extends A_CmsPreviewDetailTab {

    /** The form. */
    private CmsImageAdvancedForm m_form;

    /** The preview handler. */
    private CmsImagePreviewHandler m_handler;

    /**
     * The constructor.<p>
     *
     * @param dialogMode the mode of the gallery
     * @param height the height of the tab
     * @param width the width of the height
     * @param handler the preview handler
     */
    public CmsImageAdvancedTab(GalleryMode dialogMode, int height, int width, CmsImagePreviewHandler handler) {

        super(dialogMode, height, width);
        m_handler = handler;
        m_form = new CmsImageAdvancedForm();
        m_main.insert(m_form, 0);
    }

    /**
     * Displays the provided image information.<p>
     *
     * @param imageInfo the image information
     */
    public void fillContent(CmsImageInfoBean imageInfo) {

        CmsJSONMap imageAttributes = CmsPreviewUtil.getImageAttributes();
        boolean inititalFill = false;
        // checking if selected image resource is the same as previewed resource
        if (imageAttributes.containsKey(Attribute.emptySelection.name())
            || (imageAttributes.containsKey(Attribute.hash.name())
                && !imageAttributes.getString(Attribute.hash.name()).equals(
                    String.valueOf(m_handler.getImageIdHash())))) {
            imageAttributes = CmsJSONMap.createJSONMap();
            inititalFill = true;
        }
        m_form.fillContent(imageInfo, imageAttributes, inititalFill);
    }

    /**
     * Adds necessary attributes to the map.<p>
     *
     * @param attributes the attribute map
     * @param callback the callback to execute
     */
    public void getImageAttributes(
        Map<String, String> attributes,
        final I_CmsSimpleCallback<Map<String, String>> callback) {

        m_form.getImageAttributes(attributes, callback);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreviewDetailTab#getHandler()
     */
    @Override
    protected CmsImagePreviewHandler getHandler() {

        return m_handler;
    }

}
