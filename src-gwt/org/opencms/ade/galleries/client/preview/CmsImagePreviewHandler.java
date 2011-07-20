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

package org.opencms.ade.galleries.client.preview;

import org.opencms.ade.galleries.client.preview.ui.A_CmsPreviewDialog;
import org.opencms.ade.galleries.client.preview.ui.CmsImagePreviewDialog;
import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.gwt.client.CmsCoreProvider;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * Image preview dialog controller handler.<p>
 * 
 * Delegates the actions of the preview controller to the preview dialog.
 * 
 * @since 8.0.0
 */
public class CmsImagePreviewHandler extends A_CmsPreviewHandler<CmsImageInfoBean>
implements ValueChangeHandler<CmsCroppingParamBean> {

    /** Enumeration of image tag attribute names. */
    public enum Attribute {
        /** Image align attribute. */
        align,
        /** Image alt attribute. */
        alt,
        /** Image class attribute. */
        clazz,
        /** Image copyright info. */
        copyright,
        /** Image direction attribute. */
        dir,
        /** The image hash. */
        hash,
        /** Image height attribute. */
        height,
        /** Image hspace attribute. */
        hspace,
        /** Image id attribute. */
        id,
        /** Image copyright flag. */
        insertCopyright,
        /** Image link original flag. */
        insertLinkOrig,
        /** Image spacing flag. */
        insertSpacing,
        /** Image subtitle flag. */
        insertSubtitle,
        /** Image language attribute. */
        lang,
        /** Image link path. */
        linkPath,
        /** Image link target. */
        linkTarget,
        /** Image longDesc attribute. */
        longDesc,
        /** Image style attribute. */
        style,
        /** Image title attribute. */
        title,
        /** Image vspace attribute. */
        vspace,
        /** Image width attribute. */
        width
    }

    /** The controller. */
    private CmsImagePreviewController m_controller;

    /** The cropping parameter. */
    private CmsCroppingParamBean m_croppingParam;

    /** The image format handler. */
    private CmsImageFormatHandler m_formatHandler;

    /** The dialog. */
    private CmsImagePreviewDialog m_previewDialog;

    /**
     * Constructor.<p>
     * 
     * @param previewDialog the reference to the preview dialog 
     * @param resourcePreview the resource preview instance
     * @param previewParentId the preview parent element id
     */
    public CmsImagePreviewHandler(
        CmsImagePreviewDialog previewDialog,
        I_CmsResourcePreview resourcePreview,
        String previewParentId) {

        super(resourcePreview, previewParentId);
        m_previewDialog = previewDialog;
        m_previewDialog.init(this);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.A_CmsPreviewHandler#getController()
     */
    @Override
    public A_CmsPreviewController<CmsImageInfoBean> getController() {

        return m_controller;
    }

    /**
     * Returns the image cropping parameter bean.<p>
     * 
     * @return the image cropping parameter bean
     */
    public CmsCroppingParamBean getCroppingParam() {

        return m_croppingParam;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.A_CmsPreviewHandler#getDialog()
     */
    @Override
    public A_CmsPreviewDialog<CmsImageInfoBean> getDialog() {

        return m_previewDialog;
    }

    /**
     * Returns the name of the currently selected image format.<p>
     * 
     * @return the format name
     */
    public String getFormatName() {

        String result = "";
        if ((m_formatHandler != null) && (m_formatHandler.getCurrentFormat() != null)) {
            result = m_formatHandler.getCurrentFormat().getName();
        }
        return result;
    }

    /**
     * Returns image tag attributes to set for editor plugins.<p>
     * 
     * @return the attribute map
     */
    public Map<String, String> getImageAttributes() {

        Map<String, String> result = new HashMap<String, String>();
        result.put(Attribute.hash.name(), String.valueOf(getImageIdHash()));
        m_previewDialog.getImageAttributes(result);
        m_formatHandler.getImageAttributes(result);
        return result;
    }

    /**
     * Returns the structure id hash of the previewed image.<p>
     * 
     * @return the structure id hash
     */
    public int getImageIdHash() {

        return m_resourceInfo.getHash();
    }

    /**
     * Returns the cropping parameter.<p>
     * 
     * @return the cropping parameter
     */
    public String getPreviewScaleParam() {

        if (m_croppingParam != null) {
            return m_croppingParam.getRestrictedSizeScaleParam(
                CmsImagePreviewDialog.IMAGE_HEIGHT_MAX,
                CmsImagePreviewDialog.IMAGE_WIDTH_MAX);
        }
        CmsCroppingParamBean restricted = new CmsCroppingParamBean();
        restricted.setTargetHeight(CmsImagePreviewDialog.IMAGE_HEIGHT_MAX);
        restricted.setTargetWidth(CmsImagePreviewDialog.IMAGE_WIDTH_MAX);
        return restricted.toString();
    }

    /**
     * Initializes the preview handler.<p>
     * 
     * @param controller the preview controller
     */
    public void init(CmsImagePreviewController controller) {

        m_controller = controller;
    }

    /**
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
     */
    public void onValueChange(ValueChangeEvent<CmsCroppingParamBean> event) {

        m_croppingParam = event.getValue();
        m_previewDialog.resetPreviewImage(CmsCoreProvider.get().link(m_controller.getResourcePath())
            + "?"
            + getPreviewScaleParam());
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.A_CmsPreviewHandler#removePreview()
     */
    @Override
    public void removePreview() {

        super.removePreview();
        m_controller = null;
        m_croppingParam = null;
        m_formatHandler = null;
        m_previewDialog = null;
    }

    /**
     * Sets the image format handler.<p>
     * 
     * @param formatHandler the format handler
     */
    public void setFormatHandler(CmsImageFormatHandler formatHandler) {

        m_formatHandler = formatHandler;
        m_croppingParam = m_formatHandler.getCroppingParam();
        m_formatHandler.addValueChangeHandler(this);
    }

}
