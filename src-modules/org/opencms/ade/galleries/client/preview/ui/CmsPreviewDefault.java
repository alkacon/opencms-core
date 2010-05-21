/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/ui/Attic/CmsPreviewDefault.java,v $
 * Date   : $Date: 2010/05/21 14:27:39 $
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

import org.opencms.ade.galleries.client.ui.Messages;
import org.opencms.ade.galleries.shared.CmsPreviewInfoBean;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabLayout;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a widget for the default preview dialog .<p>
 *  
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.
 */
public class CmsPreviewDefault extends A_CmsPreview {

    /**
     * The constructor.<p>
     * 
     * @param dialogHeight the dialog height to set
     * @param dialogWidth the dialog width to set     
     */
    public CmsPreviewDefault(int dialogHeight, int dialogWidth) {

        super(dialogHeight, dialogWidth);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreview#fillPreviewPanel(String)
     */
    @Override
    public void fillPreviewPanel(String html) {

        m_previewPanel.add(new HTML(html));
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreview#fillPreviewPanel(String)
     */
    @Override
    public void fillPropertiesTab(int height, int width, CmsPreviewInfoBean infoBean) {

        m_tabbedPanel = new CmsTabbedPanel<Widget>(CmsTabLayout.small, false);
        CmsPropertiesTab tab = new CmsPropertiesTab(height, width, infoBean.getPropeties());
        m_tabbedPanel.add(tab, Messages.get().key(Messages.GUI_PREVIEW_TAB_PROPERTIES_0));

        m_tabsHolder.add(m_tabbedPanel);
    }
}