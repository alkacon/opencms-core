/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/ui/Attic/A_CmsPreviewDialog.java,v $
 * Date   : $Date: 2010/07/19 07:45:28 $
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

import org.opencms.ade.galleries.client.preview.I_CmsPreviewHandler;
import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabLayout;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a widget skeleton for the preview dialog.<p>
 * 
 * This widget contains a panel with the resource preview and
 * a set of tabs with resource information under the preview panel.<p>
 * 
 * @param <T> the resource info bean type
 *  
 * @author Polina Smagina
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 8.0.
 */
public abstract class A_CmsPreviewDialog<T extends CmsResourceInfoBean> extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsPreviewDialogUiBinder extends UiBinder<Widget, A_CmsPreviewDialog<?>> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsPreviewDialogUiBinder uiBinder = GWT.create(I_CmsPreviewDialogUiBinder.class);

    /** The close button of the preview dialog. */
    @UiField
    protected CmsPushButton m_closeButton;

    /** The dialog height. */
    protected int m_dialogHeight;

    /** The dialog width. */
    protected int m_dialogWidth;

    /** The dialog mode of the gallery. */
    protected GalleryMode m_galleryMode;

    /** The preview handler. */
    protected I_CmsPreviewHandler<T> m_handler;

    /** The parent panel of the preview dialog. */
    @UiField
    protected FlowPanel m_parentPanel;

    /** The preview placeholder panel. */
    @UiField
    protected FlowPanel m_previewHolder;

    /** The preview panel of preview dialog. */
    @UiField
    protected SimplePanel m_previewPanel;

    /** The tabbed panel of the preview dialog. */
    protected CmsTabbedPanel<Widget> m_tabbedPanel;

    /** The tabs placeholder panel. */
    @UiField
    protected FlowPanel m_tabsHolder;

    /** The min height for the preview panel. */
    private final int m_minPreviewHeight = 364;

    /**
     * The constructor.<p>
     * 
     * @param dialogMode the gallery dialog mode (view, widget, ade, editor, ...)
     * @param dialogHeight the dialog height to set
     * @param dialogWidth the dialog width to set
     */
    public A_CmsPreviewDialog(GalleryMode dialogMode, int dialogHeight, int dialogWidth) {

        // TODO: to remove, if a better way is found, so the css is only loaded once
        CmsGalleryDialog.initCss();

        initWidget(uiBinder.createAndBindUi(this));

        m_galleryMode = dialogMode;

        m_dialogHeight = dialogHeight;
        m_dialogWidth = dialogWidth - 2;

        // height of the preview dialog
        m_parentPanel.getElement().getStyle().setHeight(m_dialogHeight, Unit.PX);
        m_parentPanel.getElement().getStyle().setWidth((m_dialogWidth), Unit.PX);

        int previewHeight = m_minPreviewHeight;
        int detailsHeight = m_dialogHeight - previewHeight;

        // height of the preview and tabs part
        previewHeight = previewHeight
            - CmsDomUtil.getCurrentStyleInt(m_previewHolder.getElement(), Style.marginBottom)
            - CmsDomUtil.getCurrentStyleInt(m_previewHolder.getElement(), Style.marginTop)
            - 1;
        // FF: -1;
        // IE7 -2;
        m_previewHolder.getElement().getStyle().setHeight(previewHeight, Unit.PX);

        detailsHeight = detailsHeight
            - CmsDomUtil.getCurrentStyleInt(m_tabsHolder.getElement(), Style.marginBottom)
            - CmsDomUtil.getCurrentStyleInt(m_tabsHolder.getElement(), Style.marginTop)
            - 2;
        m_tabsHolder.getElement().getStyle().setHeight(detailsHeight, Unit.PX);
        // m_tabsHolder.getElement().getStyle().setWidth((m_dialogWidth - 2), Unit.PX);

        m_tabbedPanel = new CmsTabbedPanel<Widget>(CmsTabLayout.small, false);
        m_tabsHolder.add(m_tabbedPanel);

        // close button        
        m_closeButton.setUiIcon(I_CmsButton.UiIcon.closethick);
        m_closeButton.setShowBorder(false);
    }

    /**
     * Displays a confirm save changes dialog with the given message.
     * May insert individual message before the given one for further information.<p> 
     * Will call the appropriate command after saving/cancel.<p>
     * 
     * @param message the message to display
     * @param onConfirm the command executed after saving
     * @param onCancel the command executed on cancel
     */
    public void confirmSaveChanges(String message, final Command onConfirm, final Command onCancel) {

        CmsConfirmDialog confirmDialog = new CmsConfirmDialog("Confirm", message);
        confirmDialog.setHandler(new I_CmsConfirmDialogHandler() {

            /**
             * @see org.opencms.gwt.client.ui.I_CmsCloseDialogHandler#onClose()
             */
            public void onClose() {

                if (onCancel != null) {
                    onCancel.execute();
                }
            }

            /**
             * @see org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler#onOk()
             */
            public void onOk() {

                if (onConfirm != null) {
                    onConfirm.execute();
                }
            }
        });
    }

    /**
     * Fills the content of the tabs panel.<p>
     * 
     * @param resourceInfo the bean containing the parameter 
     */
    public abstract void fillContent(T resourceInfo);

    /**
     * Returns the gallery mode.<p>
     * 
     * @return the gallery mode
     */
    public GalleryMode getGalleryMode() {

        return m_galleryMode;
    }

    /**
     * Returns if there are any changes that need saving, before the preview may be closed.<p>
     * 
     * @return <code>true</code> if changed
     */
    public abstract boolean hasChanges();

    /**
     * Removes the preview.<p>
     */
    public void removePreview() {

        removeFromParent();
    }

    /**
     * Will be triggered, when the close button of the preview dialog is clicked.<p>
     * 
     * The preview dialog is set invisible and removed from parent.
     *
     * @param event the click event
     */
    @UiHandler("m_closeButton")
    protected void onCloseClick(ClickEvent event) {

        m_handler.closePreview();
    }
}