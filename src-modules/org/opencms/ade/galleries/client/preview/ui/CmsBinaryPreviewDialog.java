/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/ui/Attic/CmsBinaryPreviewDialog.java,v $
 * Date   : $Date: 2010/07/08 06:49:42 $
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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.client.preview.CmsBinaryPreviewHandler;
import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;

/**
 * Provides a widget for the binary preview dialog .<p>
 *  
 * @author Polina Smagina
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.
 */
public class CmsBinaryPreviewDialog extends A_CmsPreviewDialog<CmsResourceInfoBean> {

    /** The properties tab. */
    private CmsPropertiesTab m_propertiesTab;

    /**
     * The constructor.<p>
     * 
     * @param dialogMode the dialog mode
     * @param dialogHeight the dialog height to set
     * @param dialogWidth the dialog width to set     
     */
    public CmsBinaryPreviewDialog(GalleryMode dialogMode, int dialogHeight, int dialogWidth) {

        super(dialogMode, dialogHeight, dialogWidth);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreviewDialog#fillContent(org.opencms.ade.galleries.shared.CmsResourceInfoBean)
     */
    @Override
    public void fillContent(CmsResourceInfoBean infoBean) {

        //TODO: use proper preview content
        fillPreviewPanel(infoBean);
        m_propertiesTab.fillProperties(infoBean.getProperties());
    }

    /**
     * Fills the content of the preview panel part.<p>
     * 
     * @param infoBean the resource info
     */
    public void fillPreviewPanel(CmsResourceInfoBean infoBean) {

        m_previewPanel.setWidget(new CmsBinaryPreviewContent(infoBean));
    }

    /**
     * Returns if the properties tab has unsaved changes.<p>
     * 
     * @return <code>true</code> if the properties tab has unsaved changes
     */
    public boolean hasChangedProperties() {

        return m_propertiesTab.isChanged();
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreviewDialog#hasChanges()
     */
    @Override
    public boolean hasChanges() {

        return m_propertiesTab.isChanged();
    }

    /**
     * Initializes the preview.<p>
     * 
     * @param handler the preview handler
     */
    public void init(CmsBinaryPreviewHandler handler) {

        m_handler = handler;
        m_propertiesTab = new CmsPropertiesTab(m_galleryMode, m_dialogHeight, m_dialogWidth, m_handler);
        m_tabbedPanel.add(m_propertiesTab, m_propertiesTab.getTabName());
    }
}