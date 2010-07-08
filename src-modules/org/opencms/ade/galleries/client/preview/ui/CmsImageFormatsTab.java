/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/ui/Attic/CmsImageFormatsTab.java,v $
 * Date   : $Date: 2010/07/08 06:49:42 $
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

import org.opencms.ade.galleries.client.ui.Messages;
import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextBox;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The widget to display the format information of the selected image.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.
 */
public class CmsImageFormatsTab extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    /* default */interface I_CmsImageFormatsTabUiBinder extends UiBinder<Widget, CmsImageFormatsTab> {
        // GWT interface, nothing to do here
    }

    /** The label width. */
    private static final int LABEL_WIDTH = 80;

    /** Text metrics key. */
    private static final String TM_PREVIEW_TAB_IMAGEFORMATS = "ImageFormatsTab";

    /** The ui-binder instance for this class. */
    private static I_CmsImageFormatsTabUiBinder uiBinder = GWT.create(I_CmsImageFormatsTabUiBinder.class);

    /** The cropping button. */
    @UiField
    protected CmsPushButton m_cropButton;

    /** The height label. */
    @UiField
    protected CmsLabel m_heightLabel;

    /** The panel holding the content. */
    @UiField
    protected FlowPanel m_panel;

    /** The remove cropping button. */
    @UiField
    protected CmsPushButton m_removeCropButton;

    /** The select box. */
    @UiField
    protected CmsSelectBox m_selectBox;

    /** The select box label. */
    @UiField
    protected CmsLabel m_selectBoxLabel;

    /** The select button. */
    @UiField
    protected CmsPushButton m_selectButton;

    /** The width label. */
    @UiField
    protected CmsLabel m_widthLabel;

    /** The width text box. */
    @UiField
    protected CmsTextBox m_widthBox;

    /** The height text box. */
    @UiField
    protected CmsTextBox m_heightBox;

    /** The mode of the gallery. */
    private GalleryMode m_dialogMode;

    /**
     * The constructor.<p>
     * 
     * @param dialogMode the mode of the gallery
     * @param height the height of the tab
     * @param width the width of the height
     * @param formats the map with format values for the select box
     */
    public CmsImageFormatsTab(GalleryMode dialogMode, int height, int width, Map<String, String> formats) {

        initWidget(uiBinder.createAndBindUi(this));

        m_dialogMode = dialogMode;

        m_selectBoxLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_FORMAT_0));
        m_selectBoxLabel.truncate(TM_PREVIEW_TAB_IMAGEFORMATS, LABEL_WIDTH);

        // TODO: set the select box values
        m_selectBox.addOption("original", "original");
        m_selectBox.addOption("user", "user");

        // set localized values of the labels
        m_cropButton.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_CROP_0));
        m_cropButton.setImageClass(I_CmsImageBundle.INSTANCE.style().croppingIcon());

        m_removeCropButton.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_REMOVECROP_0));
        m_removeCropButton.setImageClass(I_CmsImageBundle.INSTANCE.style().removeCroppingIcon());

        m_widthLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_WIDTH_0));
        m_widthLabel.truncate(TM_PREVIEW_TAB_IMAGEFORMATS, LABEL_WIDTH);
        m_heightLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_HEIGHT_0));
        m_heightLabel.truncate(TM_PREVIEW_TAB_IMAGEFORMATS, LABEL_WIDTH);

        // buttons        
        m_selectButton.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_SELECT_0));

    }

    /**
     * Displays the provided image information.<p>
     * 
     * @param imageInfo the image information
     */
    public void fillContent(CmsImageInfoBean imageInfo) {

        m_widthBox.setFormValueAsString("" + imageInfo.getWidth());
        m_heightBox.setFormValueAsString("" + imageInfo.getHeight());
    }

    /**
     * Returns the gallery dialog mode.<p>
     *
     * @return the gallery dialog mode
     */
    public GalleryMode getDialogMode() {

        return m_dialogMode;
    }

    /**
     * Will be triggered when the select button is clicked.<p>
     * 
     * @param event the clicked event
     */
    @UiHandler("m_selectButton")
    public void onSelectClick(ClickEvent event) {

        // TODO:implement

    }
}