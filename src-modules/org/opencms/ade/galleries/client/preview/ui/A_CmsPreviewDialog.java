/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/ui/Attic/A_CmsPreviewDialog.java,v $
 * Date   : $Date: 2011/04/20 17:54:37 $
 * Version: $Revision: 1.12 $
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
import org.opencms.ade.galleries.client.ui.Messages;
import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsTabbedPanel;
import org.opencms.gwt.client.ui.CmsTabbedPanel.CmsTabbedPanelStyle;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
 * @version $Revision: 1.12 $
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
    protected CmsPushButton m_closeButton;

    /** The dialog height. */
    protected int m_dialogHeight;

    /** The dialog width. */
    protected int m_dialogWidth;

    /** The dialog mode of the gallery. */
    protected GalleryMode m_galleryMode;

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
    private final int m_minPreviewHeight = 362;

    /**
     * The constructor.<p>
     * 
     * @param dialogMode the gallery dialog mode (view, widget, ade, editor, ...)
     * @param dialogHeight the dialog height to set
     * @param dialogWidth the dialog width to set
     */
    public A_CmsPreviewDialog(GalleryMode dialogMode, int dialogHeight, int dialogWidth) {

        initWidget(uiBinder.createAndBindUi(this));

        m_galleryMode = dialogMode;

        m_dialogHeight = dialogHeight;
        m_dialogWidth = dialogWidth;

        int previewHeight = m_minPreviewHeight;
        int detailsHeight = m_dialogHeight - previewHeight - 7;

        m_previewHolder.getElement().getStyle().setHeight(previewHeight, Unit.PX);

        m_tabsHolder.getElement().getStyle().setHeight(detailsHeight, Unit.PX);

        m_tabbedPanel = new CmsTabbedPanel<Widget>(CmsTabbedPanelStyle.classicTabs);
        m_tabsHolder.add(m_tabbedPanel);

        // close button        
        m_closeButton = new CmsPushButton();
        m_closeButton.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
        m_closeButton.addStyleName(org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.INSTANCE.previewDialogCss().previewCloseButton());
        m_closeButton.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_HIDE_0));
        m_closeButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                getHandler().closePreview();
            }
        });
        m_tabsHolder.add(m_closeButton);

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
     * Returns the preview handler.<p>
     * 
     * @return the preview handler
     */
    protected abstract I_CmsPreviewHandler<T> getHandler();
}