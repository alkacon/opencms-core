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

package org.opencms.workplace.galleries;

import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolderExtended;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsImageLoader;
import org.opencms.loader.CmsImageScaler;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.awt.Color;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides the specific constants, members and helper methods to generate the content of the image gallery dialog
 * used in the XML content editors, WYSIWYG editors and context menu.<p>
 *
 * @since 7.5.0
 */
public class CmsAjaxImageGallery extends A_CmsAjaxGallery {

    /** Type name of the image gallery. */
    public static final String GALLERYTYPE_NAME = "imagegallery";

    /** The uri suffix for the gallery start page. */
    public static final String OPEN_URI_SUFFIX = GALLERYTYPE_NAME + "/index.jsp";

    /** Request parameter name for the format name. */
    public static final String PARAM_FORMATNAME = "formatname";

    /** Request parameter name for the format value. */
    public static final String PARAM_FORMATVALUE = "formatvalue";

    /** Request parameter name for the input field hash id. */
    public static final String PARAM_HASHID = "hashid";

    /** Request parameter name for the image height. */
    public static final String PARAM_IMGHEIGHT = "imgheight";

    /** Request parameter name for the image width. */
    public static final String PARAM_IMGWIDTH = "imgwidth";

    /** Request parameter name for the image scale parameters. */
    public static final String PARAM_SCALE = "scale";

    /** Request parameter name for the use formats flag. */
    public static final String PARAM_USEFORMATS = "useformats";

    /** Property definition name for the Copyright property. */
    public static final String PARAM_WIDGETMODE = "widgetmode";

    /** Property definition name for the Copyright property. */
    public static final String PROPERTY_COPYRIGHT = "Copyright";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsAjaxImageGallery.class);

    /** The default image scaling parameters for the gallery preview. */
    private CmsImageScaler m_defaultScaleParams;

    /** The resource type id of this gallery instance. */
    private int m_galleryTypeId;

    /**
     * Public empty constructor, required for {@link A_CmsAjaxGallery#createInstance(String, CmsJspActionElement)}.<p>
     */
    public CmsAjaxImageGallery() {

        // noop
    }

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsAjaxImageGallery(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAjaxImageGallery(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the default image scaling parameters for the gallery preview.<p>
     *
     * @return the default image scaling parameters for the gallery preview
     */
    public CmsImageScaler getDefaultScaleParams() {

        return m_defaultScaleParams;
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsAjaxGallery#getGalleryItemsTypeId()
     */
    @Override
    public int getGalleryItemsTypeId() {

        int imageId;
        try {
            imageId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeImage.getStaticTypeName()).getTypeId();
        } catch (CmsLoaderException e1) {
            // should really never happen
            LOG.warn(e1.getLocalizedMessage(), e1);
            imageId = CmsResourceTypeImage.getStaticTypeId();
        }
        return imageId;
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsAjaxGallery#getGalleryTypeId()
     */
    @Override
    public int getGalleryTypeId() {

        try {
            m_galleryTypeId = OpenCms.getResourceManager().getResourceType(GALLERYTYPE_NAME).getTypeId();
        } catch (CmsLoaderException e) {
            // resource type not found, log error
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return m_galleryTypeId;
    }

    /**
     * @see org.opencms.workplace.galleries.A_CmsAjaxGallery#getGalleryTypeName()
     */
    @Override
    public String getGalleryTypeName() {

        return GALLERYTYPE_NAME;
    }

    /**
     * Initializes the default image scaling parameters for the gallery preview.<p>
     *
     * @see org.opencms.workplace.galleries.A_CmsAjaxGallery#init()
     */
    @Override
    public void init() {

        if (CmsImageLoader.isEnabled()) {
            try {
                //reads the optional parameters for the gallery from the XML configuration
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(GALLERYTYPE_NAME);
                if (type instanceof CmsResourceTypeFolderExtended) {
                    m_galleryTypeParams = ((CmsResourceTypeFolderExtended)type).getFolderClassParams();
                } else {
                    m_galleryTypeParams = null;
                }
            } catch (CmsLoaderException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            m_defaultScaleParams = new CmsImageScaler(m_galleryTypeParams);
            if (!m_defaultScaleParams.isValid()) {
                // no valid parameters have been provided, use defaults
                m_defaultScaleParams.setType(0);
                m_defaultScaleParams.setPosition(0);
                m_defaultScaleParams.setWidth(120);
                m_defaultScaleParams.setHeight(90);
                m_defaultScaleParams.setColor(new Color(221, 221, 221));
            }
        } else {
            m_defaultScaleParams = null;
        }
    }

    /**
     * Fills the JSON object with the specific information used for image resource type.<p>
     *
     * <ul>
     * <li><code>scalepath</code>: scaling parameters.</li>
     * <li><code>width</code>: image width.</li>
     * <li><code>height</code>: image height.</li>
     * <li><code>id</code>: image ID.</li>
     * <li><code>type</code>: image type.</li>
     * <li><code>hash</code>: image structure id hash code.</li>
     * <li><code>copyright</code>: image copyright.</li>
     * </ul>
     *
     * @see org.opencms.workplace.galleries.A_CmsAjaxGallery#buildJsonItemSpecificPart(JSONObject jsonObj, CmsResource res, String sitePath)
     */
    @Override
    protected void buildJsonItemSpecificPart(JSONObject jsonObj, CmsResource res, String sitePath) {

        CmsImageScaler scaler = new CmsImageScaler(getCms(), res);
        try {
            String scaleParams = "";
            // 1: if scaling is disabled, the scale parameters might be null!
            if (getDefaultScaleParams() != null) {
                scaleParams = getDefaultScaleParams().toRequestParam();
            }
            jsonObj.put("scalepath", getJsp().link(sitePath + scaleParams));
            // 2: image width
            if (scaler.isValid()) {
                jsonObj.put("width", scaler.getWidth());
            } else {
                jsonObj.put("width", -1);
            }
            // 3: image height
            if (scaler.isValid()) {
                jsonObj.put("height", scaler.getHeight());
            } else {
                jsonObj.put("height", -1);
            }
            // 4: image ID
            jsonObj.put("id", res.getStructureId());
            // 5: image type (gif, jpg, etc.)
            String type = "";
            int dotIndex = res.getName().lastIndexOf('.');
            if (dotIndex != -1) {
                type = res.getName().substring(dotIndex + 1).toLowerCase();
            }
            jsonObj.put("type", type);
            // 6: image structure id hash code
            jsonObj.put("hash", res.getStructureId().hashCode());
            // 7: image copyright
            String copyright = getJsp().property(PROPERTY_COPYRIGHT, sitePath, "");
            jsonObj.put("copyright", CmsStringUtil.escapeJavaScript(copyright));

        } catch (JSONException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }
}