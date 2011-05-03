/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/Attic/CmsBinaryPreviewHandler.java,v $
 * Date   : $Date: 2011/05/03 10:48:59 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.ade.galleries.client.preview.ui.CmsBinaryPreviewDialog;
import org.opencms.ade.galleries.shared.CmsResourceInfoBean;

/**
 * Binary preview dialog controller handler.<p>
 * 
 * Delegates the actions of the preview controller to the preview dialog.
 * 
 * @author Polina Smagina
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0
 */
public class CmsBinaryPreviewHandler extends A_CmsPreviewHandler<CmsResourceInfoBean> {

    /** The dialog. */
    private CmsBinaryPreviewDialog m_previewDialog;

    /** The controller. */
    private CmsBinaryPreviewController m_controller;

    /**
     * Constructor.<p>
     * 
     * @param previewDialog the reference to the preview dialog 
     * @param resourcePreview the resource preview instance
     * @param previewParentId the preview parent element id
     */
    public CmsBinaryPreviewHandler(
        CmsBinaryPreviewDialog previewDialog,
        I_CmsResourcePreview resourcePreview,
        String previewParentId) {

        super(resourcePreview, previewParentId);
        m_previewDialog = previewDialog;
        m_previewDialog.init(this);
    }

    /**
     * Initializes the preview handler.<p>
     * 
     * @param controller the preview controller
     */
    public void init(CmsBinaryPreviewController controller) {

        m_controller = controller;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.A_CmsPreviewHandler#getController()
     */
    @Override
    public A_CmsPreviewController<CmsResourceInfoBean> getController() {

        return m_controller;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.A_CmsPreviewHandler#getDialog()
     */
    @Override
    public A_CmsPreviewDialog<CmsResourceInfoBean> getDialog() {

        return m_previewDialog;
    }

}
