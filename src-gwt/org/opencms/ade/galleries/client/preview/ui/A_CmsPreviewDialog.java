/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.ade.galleries.client.preview.CmsPreviewUtil;
import org.opencms.ade.galleries.client.preview.I_CmsPreviewHandler;
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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
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

    /** The button panel. */
    @UiField
    protected FlowPanel m_buttonBar;

    /** The select button. */
    @UiField
    protected CmsPushButton m_closePreview;

    /** The dialog height. */
    protected int m_dialogHeight;

    /** The dialog width. */
    protected int m_dialogWidth;

    /** The dialog mode of the gallery. */
    protected GalleryMode m_galleryMode;

    /** The parent panel of the preview dialog. */
    @UiField
    protected FlowPanel m_parentPanel;

    /** The preview height. */
    protected int m_previewHeight;

    /** The preview placeholder panel. */
    @UiField
    protected FlowPanel m_previewHolder;

    /** The preview panel of preview dialog. */
    @UiField
    protected SimplePanel m_previewPanel;

    /** The select button. */
    @UiField
    protected CmsPushButton m_selectButton;

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
        m_previewHeight = m_minPreviewHeight;
        int detailsHeight = m_dialogHeight - m_previewHeight - 7;
        m_previewHolder.getElement().getStyle().setHeight(m_previewHeight, Unit.PX);
        m_tabsHolder.getElement().getStyle().setHeight(detailsHeight, Unit.PX);
        m_tabbedPanel = new CmsTabbedPanel<Widget>(CmsTabbedPanelStyle.classicTabs);
        m_tabsHolder.add(m_tabbedPanel);
        m_selectButton.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_SELECT_0));
        m_selectButton.setVisible(false);
        m_closePreview.setText(Messages.get().key(Messages.GUI_PREVIEW_CLOSE_BUTTON_0));

        // buttons        
        switch (m_galleryMode) {
            case editor:
                m_selectButton.setVisible(CmsPreviewUtil.shouldShowSelectButton());
                m_closePreview.setText(Messages.get().key(Messages.GUI_PREVIEW_CLOSE_GALLERY_BUTTON_0));
                m_buttonBar.getElement().getStyle().setBottom(94, Unit.PX);
                m_buttonBar.getElement().getStyle().setRight(1, Unit.PX);
                break;
            case widget:
                m_selectButton.setVisible(true);
                m_closePreview.setText(Messages.get().key(Messages.GUI_PREVIEW_CLOSE_GALLERY_BUTTON_0));
                break;
            case ade:
            case view:
            default:
                break;
        }
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
        confirmDialog.center();
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
     * Will be triggered, when the select button is clicked.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_closePreview")
    public void onCloseClick(ClickEvent event) {

        saveChanges(null);
        getHandler().closePreview();
    }

    /**
     * Will be triggered, when the select button is clicked.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_selectButton")
    public void onSelectClick(ClickEvent event) {

        if (m_galleryMode == GalleryMode.editor) {
            // note: the select button isn't necessarily visible in editor mode (depending on the WYSIWYG editor), but 
            // if it is, we want it to save the data and close the gallery dialog 
            if (getHandler().setDataInEditor()) {
                // do this after a delay, so we don't get ugly Javascript errors when the iframe is closed.
                Timer timer = new Timer() {

                    @Override
                    public void run() {

                        CmsPreviewUtil.closeDialog();
                    }

                };
                timer.schedule(1);
            }
        } else {
            saveChanges(null);
            getHandler().selectResource();
        }
    }

    /**
     * Removes the preview.<p>
     */
    public void removePreview() {

        removeFromParent();
    }

    /**
     * Saves the changes for this dialog.<p>
     * 
     * @param afterSaveCommand the command to execute after saving the changes 
     */
    public abstract void saveChanges(Command afterSaveCommand);

    /**
     * Returns the preview handler.<p>
     * 
     * @return the preview handler
     */
    protected abstract I_CmsPreviewHandler<T> getHandler();
}