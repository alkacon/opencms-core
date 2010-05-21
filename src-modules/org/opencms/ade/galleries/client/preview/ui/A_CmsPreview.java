/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/ui/Attic/A_CmsPreview.java,v $
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

import org.opencms.ade.galleries.shared.CmsPreviewInfoBean;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a widget sceleton for the preview.<p>
 *  
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.
 */
public abstract class A_CmsPreview extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    /* default */interface I_CmsPreviewUiBinder extends UiBinder<Widget, A_CmsPreview> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsPreviewUiBinder uiBinder = GWT.create(I_CmsPreviewUiBinder.class);

    /** The close button of the preview dialog. */
    @UiField
    protected CmsPushButton m_closeButton;

    /** The parent panel of the preview dialog. */
    @UiField
    protected FlowPanel m_parentPanel;

    /** The preview placeholder panel. */
    @UiField
    protected FlowPanel m_previewHolder;

    /** The preview panel of preview dialog. */
    @UiField
    protected FlowPanel m_previewPanel;

    // TODO: set the right generic type
    /** The tabbed panel of the preview dialog. */
    protected CmsTabbedPanel<Widget> m_tabbedPanel;

    /** The tabs placeholder panel. */
    @UiField
    protected FlowPanel m_tabsHolder;

    /** The min height for the preview panel. */
    private final int m_minPreviewHeight = 364;

    /**
     * The contructor.<p>
     * 
     * @param dialogHeight the dialog height to set
     * @param dialogWidth the dialog width to set
     */
    // TODO: this contrustor should be called only once, when the gallery dialod is opened and set to invisible
    public A_CmsPreview(int dialogHeight, int dialogWidth) {

        initWidget(uiBinder.createAndBindUi(this));

        // height of the preview dialog
        m_parentPanel.setHeight(Integer.toString(dialogHeight));
        m_parentPanel.setWidth(Integer.toString(dialogWidth - 2));

        int previewHeght = m_minPreviewHeight;
        int detailsHeight = dialogHeight - previewHeght;

        // height of the preview and tabs part
        previewHeght = previewHeght
            - CmsDomUtil.getCurrentStyleInt(m_previewHolder.getElement(), Style.marginBottom)
            - CmsDomUtil.getCurrentStyleInt(m_previewHolder.getElement(), Style.marginTop)
            - 1;
        // FF: -1;
        // IE7 -2;
        m_previewHolder.setHeight(Integer.toString(previewHeght));

        detailsHeight = detailsHeight
            - CmsDomUtil.getCurrentStyleInt(m_tabsHolder.getElement(), Style.marginBottom)
            - CmsDomUtil.getCurrentStyleInt(m_tabsHolder.getElement(), Style.marginTop)
            - 2;
        m_tabsHolder.setHeight(Integer.toString(detailsHeight));
        m_tabsHolder.setWidth(Integer.toString(dialogWidth - 4));

        // close button        
        m_closeButton.setUiIcon(I_CmsButton.UiIcon.closethick);
        m_closeButton.setShowBorder(false);
    }

    /**
     * Fills the content of the preview panel.<p>
     * 
     * @param html the content html
     */
    public abstract void fillPreviewPanel(String html);

    /**
     * Fills the content of the tabs panel.<p>
     * 
     * @param height the tab height 
     * @param width the tab width
     * @param infoBean the bean containing the parameter 
     */
    public abstract void fillPropertiesTab(int height, int width, CmsPreviewInfoBean infoBean);

    /**
     * Will be triggered, when the close button of the preview dialog is clicked.<p>
     *
     * @param event the click event
     */
    // TODO: this function should call the preview handler
    @UiHandler("m_closeButton")
    void onCloseClick(ClickEvent event) {

        //TODO: this functions should be called from the preview controller handler
        m_parentPanel.setVisible(false);
        m_tabsHolder.clear();
        // TODO: clear the content of the preview panel
        // TODO: clear the content of the tabs (remove all tabs, so different tabs are opened dependend on resource type)
    }
}