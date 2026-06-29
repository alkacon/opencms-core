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

package org.opencms.widgets;

import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;

/**
 * ADE link gallery widget implementations.<p>
 */
public class CmsLinkGalleryWidget extends A_CmsAdeGalleryWidget {

    /** The gallery name. */
    private static final String GALLERY_NAME = "link";

    /**
     * Constructor.<p>
     */
    public CmsLinkGalleryWidget() {

        this("");
    }

    /**
     * Creates a new gallery widget with the given configuration.<p>
     *
     * @param configuration the configuration to use
     */
    protected CmsLinkGalleryWidget(String configuration) {

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

        return CmsGwtConstants.WIDGET_LINKGALLERY;

    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    public I_CmsWidget newInstance() {

        return new CmsLinkGalleryWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.A_CmsAdeGalleryWidget#getAdditionalGalleryInfo(org.opencms.file.CmsObject, java.lang.String, org.opencms.i18n.CmsMessages, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    protected JSONObject getAdditionalGalleryInfo(
        CmsObject cms,
        String resource,
        CmsMessages messages,
        I_CmsWidgetParameter param)
    throws JSONException {

        JSONObject result = new JSONObject();
        result.put(I_CmsGalleryProviderConstants.CONFIG_TAB_CONFIG, "selectDoc");
        return result;
    }

    /**
     * @see org.opencms.widgets.A_CmsAdeGalleryWidget#getGalleryStoragePrefix()
     */
    @Override
    protected String getGalleryStoragePrefix() {

        return "link";
    }

    /**
     * @see org.opencms.widgets.A_CmsAdeGalleryWidget#getGalleryTypes()
     */
    @Override
    protected String getGalleryTypes() {

        return CmsResourceTypePointer.getStaticTypeName();
    }
}
