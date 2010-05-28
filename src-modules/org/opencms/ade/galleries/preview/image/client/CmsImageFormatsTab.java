/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/preview/image/client/Attic/CmsImageFormatsTab.java,v $
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

import org.opencms.ade.galleries.client.preview.ui.I_CmsPreviewTab;
import org.opencms.ade.galleries.client.ui.Messages;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsSelectBox;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class CmsImageFormatsTab extends Composite implements I_CmsPreviewTab {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    /* default */interface I_CmsImageFormatsTabUiBinder extends UiBinder<Widget, CmsImageFormatsTab> {
        // GWT interface, nothing to do here
    }

    private static final int LABEL_WIDTH = 80;

    /** Text metrics key. */
    private static final String TM_PREVIEW_TAB_IMAGEFORMATS = "ImageFormatsTab";

    /** The ui-binder instance for this class. */
    private static I_CmsImageFormatsTabUiBinder uiBinder = GWT.create(I_CmsImageFormatsTabUiBinder.class);

    /** The cropping button. */
    @UiField
    CmsPushButton m_cropButton;

    /** The panel holding the content. */
    @UiField
    FlowPanel m_panel;

    /** The remove cropping button. */
    @UiField
    CmsPushButton m_removeCropButton;

    /** The select box. */
    @UiField
    CmsSelectBox m_selectBox;

    /** The select box label. */
    @UiField
    CmsLabel m_selectBoxLabel;

    /** The select button. */
    @UiField
    CmsPushButton m_selectButton;

    /** The width label. */
    @UiField
    CmsLabel m_widthLabel;

    //    /** The height label. */
    //    @UiField
    //    CmsLabel m_heightLabel;

    /** The mode of the gallery. */
    private GalleryMode m_dialogMode;

    public CmsImageFormatsTab(GalleryMode dialogMode, int height, int width, Map<String, String> formats) {

        initWidget(uiBinder.createAndBindUi(this));

        m_dialogMode = dialogMode;

        m_selectBoxLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_FORMAT_0));
        m_selectBoxLabel.truncate(TM_PREVIEW_TAB_IMAGEFORMATS, 80);

        // TODO: set the select box values
        m_selectBox.addOption("original", "original");
        m_selectBox.addOption("user", "user");

        m_cropButton.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_CROP_0));
        m_cropButton.setImageClass(I_CmsImageBundle.INSTANCE.style().croppingIcon());

        m_removeCropButton.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_REMOVECROP_0));
        m_removeCropButton.setImageClass(I_CmsImageBundle.INSTANCE.style().removeCroppingIcon());

        m_widthLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_WIDTH_0));
        m_widthLabel.truncate(TM_PREVIEW_TAB_IMAGEFORMATS, 80);
        //        m_heightLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_HEIGHT_0));

        // buttons        
        m_selectButton.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_SELECT_0));

    }

    /**
     * Returns the dialogMode.<p>
     *
     * @return the dialogMode
     */
    public GalleryMode getDialogMode() {

        return m_dialogMode;
    }

    @UiHandler("m_selectButton")
    public void onSaveClick(ClickEvent event) {

        // TODO: Auto-generated method stub

    }

    public void onSelectClick(ClickEvent event) {

        // TODO: Auto-generated method stub

    }

}
