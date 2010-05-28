/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/preview/image/client/Attic/CmsImagePreview.java,v $
 * Date   : $Date: 2010/05/28 09:31:39 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.galleries.preview.image.client;

import org.opencms.ade.galleries.client.preview.ui.A_CmsPreview;
import org.opencms.ade.galleries.client.preview.ui.CmsPropertiesTab;
import org.opencms.ade.galleries.client.ui.Messages;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsPreviewInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabLayout;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class CmsImagePreview extends A_CmsPreview {

    /** The default min height of the image. */
    private static final int HEIGHT_MIN = 361;

    /** The default min height of the image. */
    private static final int WIDTH_MIN = 640;

    public CmsImagePreview(GalleryMode dialogMode, int dialogHeight, int dialogWidth) {

        // TODO: Auto-generated constructor stub
        super(dialogMode, dialogHeight, dialogWidth);

    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreview#fillPreviewPanel(java.lang.String)
     */
    @Override
    public void fillPreviewPanel(int height, int width, String html) {

        switch (m_dialogMode) {
            case ade:

            case widget:

            case editor:

            case sitemap:

            case view:

        }

        FlowPanel panel = new FlowPanel();
        panel.addStyleName(I_CmsLayoutBundle.INSTANCE.previewDialogCss().imagePanel());
        Image image = new Image();

        // TODO: set the image scale parameters accoding to others
        String urlScaled = html.concat("?__scale=h:").concat(Integer.toString(HEIGHT_MIN)).concat(",w:").concat(
            Integer.toString(WIDTH_MIN));
        image.setUrl(urlScaled);
        panel.add(image);

        m_previewPanel.add(panel);

    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreview#fillTabs(int, int, org.opencms.ade.galleries.shared.CmsPreviewInfoBean)
     */
    @Override
    public void fillTabs(int height, int width, CmsPreviewInfoBean infoBean) {

        m_tabbedPanel = new CmsTabbedPanel<Widget>(CmsTabLayout.small, false);
        // properties tab
        CmsPropertiesTab propertiesTab = new CmsPropertiesTab(m_dialogMode, height, width, infoBean.getPropeties());
        m_tabbedPanel.add(propertiesTab, Messages.get().key(Messages.GUI_PREVIEW_TAB_PROPERTIES_0));

        // image format tab
        CmsImageFormatsTab imageFormatTab = new CmsImageFormatsTab(
            m_dialogMode,
            height,
            width,
            infoBean.getImageInfos());
        m_tabbedPanel.add(imageFormatTab, Messages.get().key(Messages.GUI_PREVIEW_TAB_IMAGEFORMAT_0));

        // image infos tab
        CmsImageInfosTab imageInfosTab = new CmsImageInfosTab(m_dialogMode, height, width, infoBean.getImageInfos());
        m_tabbedPanel.add(imageFormatTab, Messages.get().key(Messages.GUI_PREVIEW_TAB_IMAGEFORMAT_0));

        m_tabsHolder.add(m_tabbedPanel);
    }

    // TODO: should implement
    public void openPreview() {

    }
}