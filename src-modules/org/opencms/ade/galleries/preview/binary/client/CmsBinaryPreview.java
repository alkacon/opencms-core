/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/preview/binary/client/Attic/CmsBinaryPreview.java,v $
 * Date   : $Date: 2010/06/07 08:07:40 $
 * Version: $Revision: 1.2 $
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

package org.opencms.ade.galleries.preview.binary.client;

import org.opencms.ade.galleries.client.preview.CmsPropertiesTabHandler;
import org.opencms.ade.galleries.client.preview.I_CmsPreviewController;
import org.opencms.ade.galleries.client.preview.ui.A_CmsPreview;
import org.opencms.ade.galleries.client.preview.ui.CmsPropertiesTab;
import org.opencms.ade.galleries.shared.CmsPreviewInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabLayout;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a widget for the binary preview dialog .<p>
 *  
 * @author Polina Smagina
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.
 */
public class CmsBinaryPreview extends A_CmsPreview {

    /** The properties tab. */
    private CmsPropertiesTab m_propertiesTab;

    /**
     * The constructor.<p>
     * 
     * @param dialogMode the dialog mode
     * @param dialogHeight the dialog height to set
     * @param dialogWidth the dialog width to set     
     */
    public CmsBinaryPreview(GalleryMode dialogMode, int dialogHeight, int dialogWidth) {

        super(dialogMode, dialogHeight, dialogWidth);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreview#fillPreviewPanel(int, int, String)
     */
    @Override
    public void fillPreviewPanel(int height, int width, String html) {

        m_previewPanel.add(new HTML(html));
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreview#fillTabs(int, int, CmsPreviewInfoBean, I_CmsPreviewController)
     */
    @Override
    public void fillTabs(int height, int width, CmsPreviewInfoBean infoBean, I_CmsPreviewController controller) {

        m_tabbedPanel = new CmsTabbedPanel<Widget>(CmsTabLayout.small, false);
        m_propertiesTab = new CmsPropertiesTab(
            m_galleryMode,
            height,
            width,
            infoBean.getPropeties(),
            new CmsPropertiesTabHandler(controller));
        m_tabbedPanel.add(m_propertiesTab, m_propertiesTab.getTabName());

        m_tabsHolder.add(m_tabbedPanel);
    }

    /**
     * Returns if the properties tab has unsaved changes.<p>
     * 
     * @return <code>true</code> if the properties tab has unsaved changes
     */
    public boolean hasChangedProperties() {

        return m_propertiesTab.isChanged();
    }
}