/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/ui/Attic/CmsImagePreviewDialog.java,v $
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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.client.preview.CmsImagePreviewHandler;
import org.opencms.ade.galleries.client.preview.I_CmsPreviewHandler;
import org.opencms.ade.galleries.client.ui.Messages;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.CmsCoreProvider;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Provides a widget for the image preview dialog .<p>
 *  
 * @author Polina Smagina
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.
 */
public class CmsImagePreviewDialog extends A_CmsPreviewDialog<CmsImageInfoBean> {

    /** The default min height of the image. */
    public static final int IMAGE_HEIGHT_MAX = 361;

    /** The default min width of the image. */
    public static final int IMAGE_WIDTH_MAX = 640;

    /** The preview handler. */
    private CmsImagePreviewHandler m_handler;

    /** The format tab. */
    private CmsImageFormatsTab m_imageFormatTab;

    /** The infos tab. */
    private CmsImageInfosTab m_imageInfosTab;

    /** The initial fill flag. */
    private boolean m_initialFill = true;

    private Image m_previewImage;

    /** The properties tab. */
    private CmsPropertiesTab m_propertiesTab;

    /**
     * The constructor.<p>
     * 
     * @param dialogMode the dialog mode
     * @param dialogHeight the dialog height to set
     * @param dialogWidth the dialog width to set     
     */
    public CmsImagePreviewDialog(GalleryMode dialogMode, int dialogHeight, int dialogWidth) {

        super(dialogMode, dialogHeight, dialogWidth);
    }

    /**
     * Fills the content of the tabs panel.<p>
     * 
     * @param infoBean the bean containing the parameter 
     */
    @Override
    public void fillContent(CmsImageInfoBean infoBean) {

        // properties tab
        m_propertiesTab.fillProperties(infoBean.getProperties());
        if (m_initialFill) {
            m_imageFormatTab.fillContent(infoBean);
            m_initialFill = false;
        }
        //TODO: fill other tabs

        fillPreviewPanel(infoBean);
    }

    /**
     * Fills the preview panel.<p>
     * 
     * @param infoBean the image info
     */
    public void fillPreviewPanel(CmsImageInfoBean infoBean) {

        FlowPanel panel = new FlowPanel();
        panel.addStyleName(I_CmsLayoutBundle.INSTANCE.previewDialogCss().imagePanel());
        m_previewImage = new Image();

        // TODO: set the image scale parameters
        StringBuffer urlScaled = new StringBuffer(128);
        urlScaled.append(CmsCoreProvider.get().link(infoBean.getResourcePath())).append("?").append(
            m_handler.getPreviewScaleParam());
        m_previewImage.setUrl(urlScaled.toString());
        panel.add(m_previewImage);

        m_previewPanel.setWidget(panel);

    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreviewDialog#hasChanges()
     */
    @Override
    public boolean hasChanges() {

        return false;
    }

    /**
     * Initializes the preview.<p>
     * 
     * @param handler the preview handler
     */
    public void init(CmsImagePreviewHandler handler) {

        m_handler = handler;
        m_propertiesTab = new CmsPropertiesTab(m_galleryMode, m_dialogHeight, m_dialogWidth, m_handler);
        m_tabbedPanel.add(m_propertiesTab, m_propertiesTab.getTabName());

        m_imageFormatTab = new CmsImageFormatsTab(m_galleryMode, m_dialogHeight, m_dialogWidth, handler, null);
        m_tabbedPanel.add(m_imageFormatTab, Messages.get().key(Messages.GUI_PREVIEW_TAB_IMAGEFORMAT_0));

        m_imageInfosTab = new CmsImageInfosTab(m_galleryMode, m_dialogHeight, m_dialogWidth, null);
        m_tabbedPanel.add(m_imageInfosTab, Messages.get().key(Messages.GUI_PREVIEW_TAB_IMAGEFORMAT_0));
    }

    /**
     * Resets the image displayed in the preview.<p>
     * 
     * @param path the image path including scale parameter
     */
    public void resetPreviewImage(String path) {

        m_previewImage.setUrl(path);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreviewDialog#getHandler()
     */
    @Override
    protected I_CmsPreviewHandler<CmsImageInfoBean> getHandler() {

        return m_handler;
    }
}