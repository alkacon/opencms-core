/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/Attic/CmsImagePreviewHandler.java,v $
 * Date   : $Date: 2010/07/26 06:40:50 $
 * Version: $Revision: 1.3 $
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

package org.opencms.ade.galleries.client.preview;

import org.opencms.ade.galleries.client.preview.ui.A_CmsPreviewDialog;
import org.opencms.ade.galleries.client.preview.ui.CmsImagePreviewDialog;
import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.gwt.client.CmsCoreProvider;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * Image preview dialog controller handler.<p>
 * 
 * Delegates the actions of the preview controller to the preview dialog.
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 8.0.0
 */
public class CmsImagePreviewHandler extends A_CmsPreviewHandler<CmsImageInfoBean>
implements ValueChangeHandler<CmsCroppingParamBean> {

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
     */
    public CmsImagePreviewHandler(CmsImagePreviewDialog previewDialog, I_CmsResourcePreview resourcePreview) {

        super(resourcePreview);
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
     * @see org.opencms.ade.galleries.client.preview.A_CmsPreviewHandler#getDialog()
     */
    @Override
    public A_CmsPreviewDialog<CmsImageInfoBean> getDialog() {

        return m_previewDialog;
    }

    /**
     * Returns the cropping parameter.<p>
     * 
     * @return the cropping parameter
     */
    public String getPreviewScaleParam() {

        return m_croppingParam != null ? m_croppingParam.getRestrictedSizeScaleParam(
            CmsImagePreviewDialog.IMAGE_HEIGHT_MAX,
            CmsImagePreviewDialog.IMAGE_WIDTH_MAX) : "";
    }

    /**
     * Returns the image scaling parameter.<p>
     * 
     * @return the image scaling parameter
     */
    public String getScaleParam() {

        return m_croppingParam != null ? m_croppingParam.toString() : "";
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
