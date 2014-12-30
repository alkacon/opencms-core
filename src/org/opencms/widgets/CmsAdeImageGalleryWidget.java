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

package org.opencms.widgets;

import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

/**
 * ADE image gallery widget implementations.<p>
 * 
 * @since 8.0.0 
 */
public class CmsAdeImageGalleryWidget extends A_CmsAdeGalleryWidget {

    /** The gallery name. */
    private static final String GALLERY_NAME = "image";

    /** The widget configuration. */
    private CmsVfsImageWidgetConfiguration m_widgetConfiguration;

    /**
     * Constructor.<p>
     */
    public CmsAdeImageGalleryWidget() {

        this("");
    }

    /**
     * Creates a new gallery widget with the given configuration.<p>
     * 
     * @param configuration the configuration to use
     */
    protected CmsAdeImageGalleryWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.A_CmsAdeGalleryWidget#getGalleryName()
     */
    @Override
    public String getGalleryName() {

        return GALLERY_NAME;
    }

    /**
     * @see org.opencms.widgets.I_CmsADEWidget#getWidgetName()
     */
    @Override
    public String getWidgetName() {

        return CmsAdeImageGalleryWidget.class.getName();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsAdeImageGalleryWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.A_CmsAdeGalleryWidget#getAdditionalGalleryInfo(org.opencms.file.CmsObject, java.lang.String, org.opencms.i18n.CmsMessages, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    protected JSONObject getAdditionalGalleryInfo(
        CmsObject cms,
        String resource,
        CmsMessages messages,
        I_CmsWidgetParameter param) throws JSONException {

        CmsVfsImageWidgetConfiguration config = getWidgetConfiguration(cms, messages, param);
        JSONObject result = new JSONObject();
        result.put(I_CmsGalleryProviderConstants.CONFIG_USE_FORMATS, config.isShowFormat());
        result.put(I_CmsGalleryProviderConstants.CONFIG_IMAGE_FORMATS, new JSONArray(config.getFormatValues()));
        String temp = config.getSelectFormatString();
        String[] formatNames = new String[0];
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(temp)) {
            formatNames = config.getSelectFormatString().split("\\|");
        }
        result.put(I_CmsGalleryProviderConstants.CONFIG_IMAGE_FORMAT_NAMES, new JSONArray(formatNames));
        result.put(I_CmsGalleryProviderConstants.CONFIG_TAB_CONFIG, "selectDoc");
        String uploadFolder = OpenCms.getWorkplaceManager().getRepositoryFolderHandler().getRepositoryFolder(
            cms,
            resource,
            GALLERY_NAME + "gallery");
        if (uploadFolder != null) {
            result.put(I_CmsGalleryProviderConstants.CONFIG_UPLOAD_FOLDER, uploadFolder);
        }
        return result;
    }

    /**
     * @see org.opencms.widgets.A_CmsAdeGalleryWidget#getGalleryStoragePrefix()
     */
    @Override
    protected String getGalleryStoragePrefix() {

        return "image";
    }

    /**
     * @see org.opencms.widgets.A_CmsAdeGalleryWidget#getGalleryTypes()
     */
    @Override
    protected String getGalleryTypes() {

        return CmsResourceTypeImage.getStaticTypeName();
    }

    /**
     * @see org.opencms.widgets.A_CmsAdeGalleryWidget#getOpenPreviewCall(org.opencms.widgets.I_CmsWidgetDialog, java.lang.String)
     */
    @Override
    protected String getOpenPreviewCall(I_CmsWidgetDialog widgetDialog, String id) {

        // using the 'cmsOpenImagePreview' function instead of 'cmsOpenPreview'
        StringBuffer sb = new StringBuffer(64);
        // using the 'cmsOpenImagePreview' function instead of 'cmsOpenPreview'
        sb.append("javascript:cmsOpenImagePreview('").append(
            widgetDialog.getMessages().key(Messages.GUI_BUTTON_PREVIEW_0));
        sb.append("', '").append(OpenCms.getSystemInfo().getOpenCmsContext());
        sb.append("', '").append(id);
        sb.append("'); return false;");
        return sb.toString();
    }

    /**
     * @see org.opencms.widgets.A_CmsAdeGalleryWidget#getWidgetConfiguration(org.opencms.file.CmsObject, org.opencms.i18n.CmsMessages, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    protected CmsVfsImageWidgetConfiguration getWidgetConfiguration(
        CmsObject cms,
        CmsMessages messages,
        I_CmsWidgetParameter param) {

        if (m_widgetConfiguration == null) {
            m_widgetConfiguration = new CmsVfsImageWidgetConfiguration(cms, messages, param, getConfiguration());
        }
        return m_widgetConfiguration;
    }
}
