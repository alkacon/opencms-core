/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/ui/Attic/CmsImageFormatsTab.java,v $
 * Date   : $Date: 2010/07/26 06:40:50 $
 * Version: $Revision: 1.5 $
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

import org.opencms.ade.galleries.client.preview.CmsImageFormatHandler;
import org.opencms.ade.galleries.client.preview.CmsImagePreviewHandler;
import org.opencms.ade.galleries.client.preview.I_CmsPreviewHandler;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.CmsCoreProvider;

import java.util.Map;

/**
 * The widget to display the format information of the selected image.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.
 */
public class CmsImageFormatsTab extends A_CmsPreviewDetailTab {

    /** The mode of the gallery. */
    private CmsImagePreviewHandler m_handler;

    /**
     * The constructor.<p>
     * 
     * @param dialogMode the mode of the gallery
     * @param height the height of the tab
     * @param width the width of the height
     * @param handler the preview handler
     * @param formats the map with format values for the select box
     */
    public CmsImageFormatsTab(
        GalleryMode dialogMode,
        int height,
        int width,
        CmsImagePreviewHandler handler,
        Map<String, String> formats) {

        super(dialogMode, height, width);
        m_content.removeStyleName(I_CmsLayoutBundle.INSTANCE.previewDialogCss().clearFix());
        m_content.addStyleName(I_CmsLayoutBundle.INSTANCE.previewDialogCss().formatsPanel());
        m_handler = handler;

    }

    /**
     * Displays the provided image information.<p>
     * 
     * @param imageInfo the image information
     */
    public void fillContent(CmsImageInfoBean imageInfo) {

        CmsCroppingDialog croppingDialog = new CmsCroppingDialog(
            CmsCoreProvider.get().link(imageInfo.getResourcePath()));
        m_handler.getDialog().m_parentPanel.add(croppingDialog);
        CmsImageFormatHandler formatHandler = new CmsImageFormatHandler(
            getDialogMode(),
            imageInfo.getSelectedPath(),
            imageInfo.getHeight(),
            imageInfo.getWidth());
        CmsImageFormatsForm formatsForm = new CmsImageFormatsForm(formatHandler);
        formatHandler.init(formatsForm, croppingDialog);
        m_handler.setFormatHandler(formatHandler);
        m_content.clear();
        m_content.add(formatsForm);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreviewDetailTab#getHandler()
     */
    @Override
    protected I_CmsPreviewHandler<?> getHandler() {

        return m_handler;
    }

}