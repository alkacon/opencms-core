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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.client.preview.CmsBinaryPreviewHandler;
import org.opencms.ade.galleries.client.preview.I_CmsPreviewHandler;
import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;

/**
 * Provides a widget for the binary preview dialog .<p>
 *
 * @since 8.0.
 */
public class CmsBinaryPreviewDialog extends A_CmsPreviewDialog<CmsResourceInfoBean> {

    /** The preview handler. */
    private CmsBinaryPreviewHandler m_handler;

    /** The properties tab. */
    private CmsPropertiesTab m_propertiesTab;

    /**
     * The constructor.<p>
     *
     * @param dialogMode the dialog mode
     * @param dialogHeight the dialog height to set
     * @param dialogWidth the dialog width to set
     * @param disableSelection true if selection from the preview should be disabled
     */
    public CmsBinaryPreviewDialog(GalleryMode dialogMode, int dialogHeight, int dialogWidth, boolean disableSelection) {

        super(dialogMode, dialogHeight, dialogWidth, disableSelection);
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreviewDialog#fillContent(org.opencms.ade.galleries.shared.CmsResourceInfoBean)
     */
    @Override
    public void fillContent(CmsResourceInfoBean infoBean) {

        fillPreviewPanel(infoBean);
        m_propertiesTab.fillProperties(infoBean.getProperties(), infoBean.getNoEditReason());
    }

    /**
     * Fills the content of the preview panel part.<p>
     *
     * @param infoBean the resource info
     */
    public void fillPreviewPanel(CmsResourceInfoBean infoBean) {

        m_previewPanel.setWidget(new CmsBinaryPreviewContent(infoBean, m_handler));
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
        final CmsPropertiesTab propTab = new CmsPropertiesTab(m_galleryMode, m_dialogHeight, m_dialogWidth, m_handler);
        m_selectButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                saveChanges(null);

            }
        });
        m_propertiesTab = propTab;
        m_tabbedPanel.add(m_propertiesTab, Messages.get().key(Messages.GUI_PREVIEW_TAB_PROPERTIES_0));
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreviewDialog#saveChanges(com.google.gwt.user.client.Command)
     */
    @Override
    public void saveChanges(Command afterSaveCommand) {

        if (hasChanges()) {
            m_propertiesTab.saveProperties(afterSaveCommand);
        }
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.ui.A_CmsPreviewDialog#getHandler()
     */
    @Override
    protected I_CmsPreviewHandler<CmsResourceInfoBean> getHandler() {

        return m_handler;
    }
}