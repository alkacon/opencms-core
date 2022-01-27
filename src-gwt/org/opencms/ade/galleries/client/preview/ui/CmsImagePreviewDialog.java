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
import org.opencms.ade.galleries.client.preview.CmsImagePreviewHandler;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;

import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides a widget for the image preview dialog .<p>
 *
 * @since 8.0.
 */
public class CmsImagePreviewDialog extends A_CmsPreviewDialog<CmsImageInfoBean> {

    /** The default min height of the image. */
    public static final int IMAGE_HEIGHT_MAX = 361;

    /** The default min width of the image. */
    public static final int IMAGE_WIDTH_MAX = 640;

    /** Style name for distinguishing whether the properties tab is currently active or not. */
    public static final String STYLE_PROPERTIES_TAB_ACTIVE = "propertiesTabActive";

    /** The preview handler. */
    private CmsImagePreviewHandler m_handler;

    /** The advanced image tab. */
    private CmsImageAdvancedTab m_imageAdvancedTab;

    /** The formats tab. */
    private CmsImageEditorTab m_imageEditorFormatsTab;

    /** The format tab. */
    private CmsImageFormatsTab m_imageFormatTab;

    /** The initial fill flag. */
    private boolean m_initialFill = true;

    /** The preview image. */
    private Image m_previewImage;

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
    public CmsImagePreviewDialog(GalleryMode dialogMode, int dialogHeight, int dialogWidth, boolean disableSelection) {

        super(dialogMode, dialogHeight, dialogWidth, disableSelection);
        // set the line-height to the height of the preview panel to be able to center the image vertically
        m_previewHolder.getElement().getStyle().setProperty("lineHeight", m_previewHeight - 2, Unit.PX);
    }

    /**
     * Fills the content of the tabs panel.<p>
     *
     * @param infoBean the bean containing the parameter
     */
    @Override
    public void fillContent(CmsImageInfoBean infoBean) {

        // properties tab
        m_propertiesTab.fillContent(infoBean);
        if (m_initialFill) {
            if (getGalleryMode() == GalleryMode.widget) {
                m_imageFormatTab.fillContent(infoBean);
            }
            if (getGalleryMode() == GalleryMode.editor) {
                m_imageFormatTab.fillContent(infoBean);
                m_imageEditorFormatsTab.fillContent(infoBean);
                m_imageAdvancedTab.fillContent(infoBean);
            }
            m_initialFill = false;
        }
        fillPreviewPanel(infoBean);
    }

    /**
     * Fills the preview panel.<p>
     *
     * @param infoBean the image info
     */
    public void fillPreviewPanel(CmsImageInfoBean infoBean) {

        FlowPanel panel = new FlowPanel();
        panel.addStyleName(I_CmsLayoutBundle.INSTANCE.previewDialogCss().imagePanel());
        m_previewImage = new Image();
        if (CmsClientStringUtil.checkIsPathOrLinkToSvg(infoBean.getResourcePath())) {
            m_previewImage.getElement().getStyle().setWidth(100, Unit.PCT);
            m_previewImage.getElement().getStyle().setHeight(100, Unit.PCT);
            m_previewImage.getElement().getStyle().setProperty("objectFit", "contain");
        }
        StringBuffer urlScaled = new StringBuffer(128);
        String src = infoBean.getViewLink() != null
        ? infoBean.getViewLink()
        : CmsCoreProvider.get().link(infoBean.getResourcePath());
        urlScaled.append(src);
        m_previewPanel.setWidget(panel); // Need to already attach it here so we can measure the dimensions
        m_handler.setImageContainerSize(panel.getOffsetWidth(), panel.getOffsetHeight());
        String scalingParams = m_handler.getPreviewScaleParam(infoBean.getHeight(), infoBean.getWidth());
        urlScaled.append("?").append(scalingParams);
        // add time stamp to override image caching
        urlScaled.append("&time=").append(System.currentTimeMillis());
        m_previewImage.setUrl(urlScaled.toString());
        getHandler().getFocalPointController().updateImage(panel, m_previewImage);
        panel.add(m_previewImage);
    }

    /**
     * Returns the dialog width.<p>
     *
     * @return the dialog width
     */
    public int getDialogWidth() {

        return m_dialogWidth;
    }

    /**
     * Adds necessary attributes to the map.<p>
     *
     * @param attributes the attribute map
     * @param callback the callback to execute
     */
    public void getImageAttributes(Map<String, String> attributes, I_CmsSimpleCallback<Map<String, String>> callback) {

        if (getGalleryMode() == GalleryMode.editor) {
            m_imageEditorFormatsTab.getImageAttributes(attributes);
            m_imageAdvancedTab.getImageAttributes(attributes, callback);
        } else {
            callback.execute(attributes);
        }
    }

    /**
     * Returns the preview height.<p>
     *
     * @return the preview height
     */
    public int getPreviewHeight() {

        return m_previewHeight;
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
    public void init(CmsImagePreviewHandler handler) {

        m_handler = handler;
        m_propertiesTab = new CmsPropertiesTab(m_galleryMode, m_dialogHeight, m_dialogWidth, m_handler);
        m_tabbedPanel.add(m_propertiesTab, Messages.get().key(Messages.GUI_PREVIEW_TAB_PROPERTIES_0));
        if ((m_galleryMode == GalleryMode.editor) || (m_galleryMode == GalleryMode.widget)) {
            m_imageFormatTab = new CmsImageFormatsTab(m_galleryMode, m_dialogHeight, m_dialogWidth, handler, null);
            m_tabbedPanel.add(m_imageFormatTab, Messages.get().key(Messages.GUI_PREVIEW_TAB_IMAGEFORMAT_0));
        }
        if (getGalleryMode() == GalleryMode.editor) {
            m_imageEditorFormatsTab = new CmsImageEditorTab(m_galleryMode, m_dialogHeight, m_dialogWidth, handler);

            String hideFormatsParam = Window.Location.getParameter("hideformats");
            boolean hideFormats = "true".equals(hideFormatsParam);
            if (!hideFormats) {
                m_tabbedPanel.add(m_imageEditorFormatsTab, Messages.get().key(Messages.GUI_PREVIEW_TAB_IMAGEOPTIONS_0));
            }

            m_imageAdvancedTab = new CmsImageAdvancedTab(m_galleryMode, m_dialogHeight, m_dialogWidth, handler);
            if (!hideFormats) {
                m_tabbedPanel.add(m_imageAdvancedTab, Messages.get().key(Messages.GUI_PREVIEW_TAB_IMAGEADVANCED_0));
            }
        }
        m_tabbedPanel.addSelectionHandler(new SelectionHandler<Integer>() {

            public void onSelection(SelectionEvent<Integer> event) {

                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    @SuppressWarnings("synthetic-access")
                    public void execute() {

                        updateClassForTab(event.getSelectedItem().intValue());
                    }
                });

            }
        });
    }

    /**
     * Resets the image displayed in the preview.<p>
     *
     * @param path the image path including scale parameter
     */
    public void resetPreviewImage(String path) {

        m_previewImage.setUrl(path);
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
    protected CmsImagePreviewHandler getHandler() {

        return m_handler;
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        updateClassForTab(m_tabbedPanel.getSelectedIndex());

    }

    /**
     * Updates the CSS classes of the preview dialog depending on selected tab.<p>
     *
     * @param index the tab index
     */
    private void updateClassForTab(int index) {

        Widget widget = m_tabbedPanel.getWidget(index);

        String propertyTabStyle = STYLE_PROPERTIES_TAB_ACTIVE;
        if (widget == m_propertiesTab) {
            CmsImagePreviewDialog.this.addStyleName(propertyTabStyle);
        } else {
            CmsImagePreviewDialog.this.removeStyleName(propertyTabStyle);
        }
    }
}