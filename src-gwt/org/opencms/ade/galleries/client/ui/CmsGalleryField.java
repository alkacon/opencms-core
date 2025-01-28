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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.CmsGalleryConfigurationJSO;
import org.opencms.ade.galleries.client.CmsGalleryController;
import org.opencms.ade.galleries.client.I_CmsGalleryWidgetHandler;
import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.client.preview.CmsCroppingParamBean;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsGalleryActionInfo;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync;
import org.opencms.ade.upload.client.I_CmsUploadContext;
import org.opencms.ade.upload.client.ui.CmsDialogUploadButtonHandler;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.CmsJsFunctions;
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.input.CmsSimpleTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.gwt.client.ui.input.upload.CmsFileInfo;
import org.opencms.gwt.client.ui.input.upload.CmsUploadButton;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsEmbeddedDialogHandler;
import org.opencms.gwt.shared.CmsGwtLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;

import elemental2.dom.HTMLImageElement;
import jsinterop.base.Js;

/**
 * A widget for selecting a resource from an ADE gallery dialog.<p>
 *
 * @since 8.0.0
 */
public class CmsGalleryField extends Composite
implements I_CmsFormWidget, I_CmsHasInit, HasValueChangeHandlers<String>, HasResizeHandlers, HasFocusHandlers {

    /**
     * The UI Binder interface for this widget.<p>
     */
    protected interface I_CmsGalleryFieldUiBinder extends UiBinder<HTMLPanel, CmsGalleryField> {
        // binder interface
    }

    /**
     * Handler to fire resize event on resource info widget open/close.<p>
     */
    protected class OpenCloseHandler implements CloseHandler<CmsListItemWidget>, OpenHandler<CmsListItemWidget> {

        /**
         * @see com.google.gwt.event.logical.shared.CloseHandler#onClose(com.google.gwt.event.logical.shared.CloseEvent)
         */
        @Override
        public void onClose(CloseEvent<CmsListItemWidget> event) {

            fireResize();
        }

        /**
         * @see com.google.gwt.event.logical.shared.OpenHandler#onOpen(com.google.gwt.event.logical.shared.OpenEvent)
         */
        @Override
        public void onOpen(OpenEvent<CmsListItemWidget> event) {

            fireResize();
        }
    }

    /** The widget type. */
    public static final String WIDGET_TYPE = "gallery";

    /** The ui binder for this widget. */
    private static I_CmsGalleryFieldUiBinder uibinder = GWT.create(I_CmsGalleryFieldUiBinder.class);

    /** The gallery configuration. */
    protected I_CmsGalleryConfiguration m_configuration;

    /** The scale parameters from popup. */
    protected CmsCroppingParamBean m_croppingParam;

    /** The fading element. */
    @UiField
    protected Label m_fader;

    /** The DIV carrying the input field. */
    @UiField
    protected DivElement m_fieldBox;

    /** The image preview element. */
    @UiField
    protected DivElement m_imagePreview;

    /** The main panel. */
    protected HTMLPanel m_main;

    /** The button to to open the selection. */
    @UiField
    protected CmsPushButton m_opener;

    /** The gallery pop-up. */
    protected CmsGalleryPopup m_popup;

    /** The resource info panel. */
    @UiField
    protected FlowPanel m_resourceInfoPanel;

    /** Everything to the right of the preview image. */
    @UiField
    protected HTMLPanel m_rightBlock;

    /** The special upload button. */
    @UiField(provided = true)
    protected CmsPushButton m_specialUploadButton;

    /** The textbox containing the currently selected path. */
    @UiField
    protected CmsSimpleTextBox m_textbox;

    /** The upload button. */
    @UiField(provided = true)
    protected CmsUploadButton m_uploadButton;

    /** The upload drop zone. */
    protected Element m_uploadDropZone;

    /** The upload target folder. */
    String m_uploadTarget;

    /** Flag indicating uploads are allowed. */
    private boolean m_allowUploads;

    /** The gallery service instance. */
    private I_CmsGalleryServiceAsync m_gallerySvc;

    /** The has image flag. */
    private boolean m_hasImage;

    /** The info timer instance. */
    private Timer m_infoTimer;

    /** The previous field value. */
    private String m_previousValue;

    /** The upload action. */
    private String m_uploadAction;

    /**
     * Constructs a new gallery widget.<p>
     *
     * @param configuration the gallery configuration
     * @param allowUploads states if the upload button should be enabled for this widget
     */
    public CmsGalleryField(I_CmsGalleryConfiguration configuration, boolean allowUploads) {

        CmsDialogUploadButtonHandler buttonHandler = new CmsDialogUploadButtonHandler(
            new Supplier<I_CmsUploadContext>() {

                @Override
                public I_CmsUploadContext get() {

                    return new I_CmsUploadContext() {

                        @Override
                        public void onUploadFinished(List<String> uploadedFiles) {

                            if ((uploadedFiles != null) && !uploadedFiles.isEmpty()) {
                                setValue(m_uploadTarget + uploadedFiles.iterator().next(), true);
                            }
                        }

                    };
                }
            });
        buttonHandler.setIsTargetRootPath(false);
        m_uploadButton = new CmsUploadButton(buttonHandler);
        m_uploadButton.setText(null);
        m_uploadButton.setTitle(
            Messages.get().key(Messages.GUI_GALLERY_UPLOAD_TITLE_1, configuration.getUploadFolder()));
        m_uploadButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_uploadButton.setImageClass(I_CmsButton.UPLOAD);
        m_uploadButton.setSize(Size.small);
        m_uploadButton.removeStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        m_uploadButton.getElement().setTabIndex(-1);

        m_specialUploadButton = createSpecialUploadButton();

        m_main = uibinder.createAndBindUi(this);
        initWidget(m_main);
        m_allowUploads = allowUploads;
        if (m_allowUploads) {
            m_fieldBox.addClassName(I_CmsLayoutBundle.INSTANCE.galleryFieldCss().hasUpload());
        }
        m_configuration = configuration;
        I_CmsLayoutBundle.INSTANCE.galleryFieldCss().ensureInjected();
        m_opener.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_opener.setImageClass(I_CmsButton.GALLERY);
        m_opener.setSize(Size.small);
        m_opener.setHideFromTabNav(true);
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        // registers a factory for creating new instances of this widget
        CmsWidgetFactoryRegistry.instance().registerFactory(WIDGET_TYPE, new I_CmsFormWidgetFactory() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map, com.google.common.base.Optional)
             */
            @Override
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams, Optional<String> defaultValue) {

                String config = widgetParams.get("configuration");
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(config)) {
                    config = "{}";
                }
                CmsGalleryConfigurationJSO conf = CmsGalleryConfigurationJSO.parseConfiguration(config);
                CmsGalleryField galleryField = new CmsGalleryField(conf, false);
                return galleryField;
            }
        });
    }

    /**
     * Adds a style name to the DIV carrying the input field.<p>
     *
     * @param styleName the style name to add
     */
    public void addFieldStyleName(String styleName) {

        m_fieldBox.addClassName(styleName);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return addDomHandler(handler, FocusEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasResizeHandlers#addResizeHandler(com.google.gwt.event.logical.shared.ResizeHandler)
     */
    @Override
    public HandlerRegistration addResizeHandler(ResizeHandler handler) {

        return addHandler(handler, ResizeEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    @Override
    public String getApparentValue() {

        return getFormValueAsString();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    @Override
    public FieldType getFieldType() {

        return FieldType.STRING;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    @Override
    public Object getFormValue() {

        return getFormValueAsString();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    @Override
    public String getFormValueAsString() {

        return m_textbox.getValue();
    }

    /**
     * Returns the gallery popup.<p>
     *
     * @return the gallery popup
     */
    public CmsGalleryPopup getPopup() {

        return m_popup;

    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    @Override
    public boolean isEnabled() {

        return m_textbox.isEnabled();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    @Override
    public void reset() {

        setFormValueAsString("");
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    @Override
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        // do nothing
    }

    /**
     * Sets the upload drop zone element.<p>
     *
     * @param dropZone the upload drop zone element
     */
    public void setDropZoneElement(Element dropZone) {

        if (m_allowUploads && (dropZone != null) && (m_uploadDropZone == null)) {
            m_uploadDropZone = dropZone;
            initUploadZone(m_uploadDropZone);
            m_uploadDropZone.setTitle(
                org.opencms.ade.upload.client.Messages.get().key(
                    org.opencms.ade.upload.client.Messages.GUI_UPLOAD_DRAG_AND_DROP_ENABLED_0));
            m_uploadDropZone.addClassName(I_CmsLayoutBundle.INSTANCE.galleryFieldCss().uploadDropZone());
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled) {

        m_textbox.setEnabled(enabled);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    @Override
    public void setErrorMessage(String errorMessage) {

        // do nothing
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
    @Override
    public void setFormValueAsString(String value) {

        setValue(value, false);
    }

    /**
     * Sets the gallery opener button title.<p>
     *
     * @param openerTitle the gallery opener button title
     */
    public void setGalleryOpenerTitle(String openerTitle) {

        m_opener.setTitle(openerTitle);
    }

    /**
     * Sets the has image flag.<p>
     *
     * @param hasImage the has image flag
     **/
    public void setHasImage(boolean hasImage) {

        m_hasImage = hasImage;
    }

    /**
     * Sets the name of the input field.<p>
     *
     * @param name of the input field
     * */
    public void setName(String name) {

        m_textbox.setName(name);

    }

    /**
     * Adds a widget to the main panel.<p>
     *
     * @param widget the widget to add
     */
    protected void addToMain(IsWidget widget) {

        m_rightBlock.add(widget);
    }

    /**
     * Fires the value change event if the value has changed.<p>
     *
     * @param force <code>true</code> to force firing the event in any case
     */
    protected void fireChange(boolean force) {

        String value = getFormValueAsString();
        if (force || !value.equals(m_previousValue)) {
            m_previousValue = value;
            ValueChangeEvent.fire(this, value);
        }
    }

    /**
     * Fires the resize event for this widget.<p>
     */
    protected void fireResize() {

        ResizeEvent.fire(this, getElement().getOffsetWidth(), getElement().getOffsetHeight());
    }

    /**
     * Returns the currently set resource path.<p>
     *
     * @return the currently set resource path
     */
    protected String getCurrentElement() {

        return getFormValueAsString();
    }

    /**
     * Returns the gallery service instance.<p>
     *
     * @return the gallery service instance
     */
    protected I_CmsGalleryServiceAsync getGalleryService() {

        if (m_gallerySvc == null) {
            m_gallerySvc = GWT.create(I_CmsGalleryService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.galleries.CmsGalleryService.gwt");
            ((ServiceDefTarget)m_gallerySvc).setServiceEntryPoint(serviceUrl);
        }
        return m_gallerySvc;
    }

    /**
     * Checks if drag/drop should be enabled.
     *
     * @return true if drag/drop should be enabled
     */
    protected boolean isDndEnabled() {

        return m_uploadAction == null;
    }

    /**
     * Handles the focus event on the opener.<p>
     *
     * @param event  the focus event
     */
    @UiHandler("m_textbox")
    protected void onFocusTextbox(FocusEvent event) {

        CmsDomUtil.fireFocusEvent(this);
    }

    /**
     * Internal method which opens the gallery dialog.<p>
     */
    protected void openGalleryDialog() {

        if (m_popup == null) {
            m_popup = createPopup();
            m_popup.center();
        } else {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getCurrentElement())) {
                m_popup.searchElement(getCurrentElement());
            } else {
                m_popup.center();
            }
        }
    }

    /**
     * Removes the given widget from the main panel.<p>
     *
     * @param widget the widget to remove
     *
     * @return <code>true</code> if the widget was a child of the main panel
     */
    protected boolean removeFromMain(IsWidget widget) {

        return m_main.remove(widget);
    }

    /**
     * Sets the image preview.<p>
     *
     * @param realPath the actual image path
     * @param imagePath the image path
     */
    protected void setImagePreview(String realPath, String imagePath) {

        if ((m_croppingParam == null) || !getFormValueAsString().contains(m_croppingParam.toString())) {
            m_croppingParam = CmsCroppingParamBean.parseImagePath(getFormValueAsString());
        }
        CmsCroppingParamBean restricted;

        String highRes = null;
        String normalRes = null;
        if (m_croppingParam.getScaleParam(false).isEmpty()) {
            highRes = imagePath + getScalingParams(true);
            normalRes = imagePath + getScalingParams(false);
        } else {
            restricted = m_croppingParam.getRestrictedSizeParam(140, 165);
            normalRes = imagePath + "?" + restricted.convertToScalingParam(false);
            highRes = imagePath + "?" + restricted.convertToScalingParam(true);
        }
        Element image = DOM.createImg();
        image.setAttribute("src", normalRes);
        {
            HTMLImageElement img = Js.cast(image);
            img.addEventListener("error", event -> CmsJsFunctions.INSTANCE.handleBrokenImage(img));
            if (highRes != null) {
                img.srcset = highRes + " 2x";
            }
        }
        m_imagePreview.setInnerHTML("");
        m_imagePreview.appendChild(image);
    }

    /**
     * Sets the widget value.<p>
     *
     * @param value the value to set
     * @param fireEvent if the change event should be fired
     */
    protected void setValue(String value, boolean fireEvent) {

        m_textbox.setValue(value);
        updateUploadTarget(CmsResource.getFolderPath(value));
        updateResourceInfo(value);
        m_previousValue = value;
        if (fireEvent) {
            fireChange(true);
        }
    }

    /**
     * Sets the widget value. To be called from the gallery dialog.<p>
     *
     * @param resourcePath the selected resource path
     * @param structureId the resource structure id
     * @param croppingParameter the selected cropping
     */
    protected void setValueFromGallery(
        String resourcePath,
        CmsUUID structureId,
        CmsCroppingParamBean croppingParameter) {

        m_croppingParam = croppingParameter;
        String path = resourcePath;
        // in case of an image check the cropping parameter
        if ((m_croppingParam != null) && (m_croppingParam.isCropped() || m_croppingParam.isScaled())) {
            path += "?" + m_croppingParam.toString();
        }
        setValue(path, true);
        m_popup.hide();
    }

    /**
     * Updates the upload target folder path.<p>
     *
     * @param uploadTarget the upload target folder
     */
    protected void updateUploadTarget(String uploadTarget) {

        m_uploadAction = null;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(uploadTarget)) {
            m_uploadTarget = m_configuration.getUploadFolder();
        } else {
            m_uploadTarget = uploadTarget;
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_uploadTarget)) {
            setSpecialUpload(false);
            // disable the upload button as no target folder is available
            m_uploadButton.disable(
                org.opencms.ade.upload.client.Messages.get().key(
                    org.opencms.ade.upload.client.Messages.GUI_UPLOAD_BUTTON_NO_TARGET_0));
        } else {
            CmsRpcAction<CmsGalleryActionInfo> action = new CmsRpcAction<CmsGalleryActionInfo>() {

                @Override
                public void execute() {

                    start(0, false);
                    CmsGalleryController.getGalleryService().getGalleryActionInfo(uploadTarget, this);

                }

                @SuppressWarnings("synthetic-access")
                @Override
                protected void onResponse(CmsGalleryActionInfo result) {

                    stop(false);
                    if ((result == null) || (result.getUploadAction() == null)) {
                        setSpecialUpload(false);
                        // make sure the upload button is available
                        m_uploadButton.enable();
                        ((CmsDialogUploadButtonHandler)m_uploadButton.getButtonHandler()).setTargetFolder(
                            m_uploadTarget);
                        m_uploadButton.updateFileInput();
                        m_uploadButton.setTitle(
                            Messages.get().key(Messages.GUI_GALLERY_UPLOAD_TITLE_1, m_uploadTarget));

                    } else {
                        m_uploadAction = result.getUploadAction();
                        setSpecialUpload(true);
                    }
                }
            };
            action.execute();

        }
    }

    /**
     * Clears the info timer.<p>
     */
    void clearInfoTimer() {

        m_infoTimer = null;
    }

    /**
     * Displays the resource info.<p>
     *
     * @param info the resource info
     */
    void displayResourceInfo(CmsResultItemBean info) {

        if (m_hasImage) {
            setImagePreview(info.getPath(), info.getViewLink());
            m_resourceInfoPanel.add(new CmsImageInfo(info, info.getDimension()));
        } else {
            CmsListItemWidget widget = new CmsListItemWidget(info);
            OpenCloseHandler handler = new OpenCloseHandler();
            widget.addCloseHandler(handler);
            widget.addOpenHandler(handler);
            m_resourceInfoPanel.add(widget);
            int width = m_resourceInfoPanel.getOffsetWidth();
            if (width > 0) {
                widget.truncate("STANDARD", width);
            }
        }
        fireResize();
    }

    /**
     * On text box blur.<p>
     *
     * @param event the event
     */
    @UiHandler("m_textbox")
    void onBlur(BlurEvent event) {

        setFaded((m_textbox.getValue().length() * 6.88) > m_textbox.getOffsetWidth());
        setTitle(m_textbox.getValue());
    }

    /**
     * Handles styling changes on drag out.<p>
     */
    void onDragOut() {

        m_uploadDropZone.removeClassName(I_CmsLayoutBundle.INSTANCE.galleryFieldCss().dropZoneHover());
    }

    /**
     * Handles styling changes on drag over.<p>
     */
    void onDragOver() {

        if (m_uploadTarget != null) {
            m_uploadDropZone.addClassName(I_CmsLayoutBundle.INSTANCE.galleryFieldCss().dropZoneHover());
        }
    }

    /**
     * On fader click.<p>
     *
     * @param event the event
     */
    @UiHandler("m_fader")
    void onFaiderClick(ClickEvent event) {

        m_textbox.setFocus(true);
    }

    /**
     * On opener click.<p>
     *
     * @param event the event
     */
    @UiHandler("m_opener")
    void onOpenerClick(ClickEvent event) {

        m_opener.clearHoverState();
        openGalleryDialog();
    }

    /**
     * On text box change.<p>
     *
     * @param event the event
     */
    @UiHandler("m_textbox")
    void onTextBoxChange(ValueChangeEvent<String> event) {

        fireChange(false);
        if (m_infoTimer != null) {
            m_infoTimer.cancel();
            m_infoTimer = null;
        }
        m_infoTimer = new Timer() {

            @Override
            public void run() {

                updateResourceInfo(getFormValueAsString());
                clearInfoTimer();
            }
        };
        m_infoTimer.schedule(300);
    }

    /**
     * Toggles the fading element.<p>
     *
     * @param faded <code>true</code> to show the fading element.<p>
     */
    void setFaded(boolean faded) {

        m_fader.setVisible(faded);
    }

    /**
     * Updates the resource info.<p>
     *
     * @param path the resource path
     */
    void updateResourceInfo(final String path) {

        m_resourceInfoPanel.clear();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {
            m_resourceInfoPanel.setVisible(false);
            if (m_hasImage) {
                removeStyleName(I_CmsLayoutBundle.INSTANCE.galleryFieldCss().hasImage());
                m_imagePreview.setInnerHTML("");
            }
        } else {
            m_resourceInfoPanel.getElement().getStyle().clearDisplay();
            if (m_hasImage) {
                addStyleName(I_CmsLayoutBundle.INSTANCE.galleryFieldCss().hasImage());
            }
            CmsRpcAction<CmsResultItemBean> action = new CmsRpcAction<CmsResultItemBean>() {

                @Override
                public void execute() {

                    getGalleryService().getInfoForResource(path, m_configuration.getLocale(), this);
                }

                @Override
                protected void onResponse(CmsResultItemBean result) {

                    displayResourceInfo(result);
                }
            };
            action.execute();
        }
        fireResize();
    }

    /**
     * Creates the gallery pop-up.<p>
     *
     * @return the gallery pop-up
     */
    private CmsGalleryPopup createPopup() {

        I_CmsGalleryWidgetHandler handler = new I_CmsGalleryWidgetHandler() {

            @Override
            public void setWidgetValue(
                String resourcePath,
                CmsUUID structureId,
                CmsCroppingParamBean croppingParameter) {

                setValueFromGallery(resourcePath, structureId, croppingParameter);
            }
        };
        m_configuration.setCurrentElement(getCurrentElement());
        return new CmsGalleryPopup(handler, m_configuration);
    }

    /**
     * Creates custom upload button for galleries which have an upload action configured.
     *
     * @return the special upload button
     */
    private CmsPushButton createSpecialUploadButton() {

        CmsPushButton uploadButton = new CmsPushButton(I_CmsButton.UPLOAD_SMALL);
        uploadButton.setText(null);
        //uploadButton.setTitle(Messages.get().key(Messages.GUI_GALLERY_UPLOAD_TITLE_1, gallery.getPath()));
        uploadButton.setButtonStyle(ButtonStyle.FONT_ICON, null);
        uploadButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                CmsRpcAction<CmsUUID> action = new CmsRpcAction<CmsUUID>() {

                    @Override
                    public void execute() {

                        start(0, true);
                        CmsCoreProvider.getVfsService().getStructureId(m_uploadTarget, this);
                    }

                    @SuppressWarnings("synthetic-access")
                    @Override
                    protected void onResponse(CmsUUID result) {

                        stop(false);
                        if (result == null) {
                            return;
                        }
                        List<CmsUUID> resultIds = new ArrayList<>();
                        resultIds.add(result);
                        Map<String, String> params = new HashMap<>();
                        params.put("editor", "true");
                        CmsEmbeddedDialogHandler.openDialog(
                            m_uploadAction,
                            resultIds,
                            params,
                            id -> updateValueFromId(id));
                    }

                };
                action.execute();
            }
        });
        return uploadButton;
    }

    /** 
     * Gets the default scaling parameters for the preview.
     * 
     * @param highRes true if we want the high-res version
     * @param the scaling parameters
     */
    private String getScalingParams(boolean highRes) {


        if (highRes) {
            return "?__scale=w:400,h:340,t:9,q:85";
        } else {
            return "?__scale=w:200,h:170,t:9";
        }
    }

    /**
     * Initializes the upload drop zone event handlers.<p>
     *
     * @param element the drop zone element
     */
    private native void initUploadZone(JavaScriptObject element)/*-{
        // check for file api support
        if ((typeof FileReader == 'function' || typeof FileReader == 'object')
                && (typeof FormData == 'function' || typeof FormData == 'object')) {
            var self = this;

            function isDndEnabled() {
                return self.@org.opencms.ade.galleries.client.ui.CmsGalleryField::isDndEnabled()();
            }

            function dragover(event) {
                event.stopPropagation();
                event.preventDefault();
                if (!isDndEnabled()) {
                    return;
                }
                self.@org.opencms.ade.galleries.client.ui.CmsGalleryField::onDragOver()();
            }

            function dragleave(event) {
                event.stopPropagation();
                event.preventDefault();
                if (!isDndEnabled()) {
                    return;
                }
                self.@org.opencms.ade.galleries.client.ui.CmsGalleryField::onDragOut()();
            }

            function drop(event) {
                event.preventDefault();
                if (!isDndEnabled()) {
                    return;
                }
                self.@org.opencms.ade.galleries.client.ui.CmsGalleryField::onDragOut()();
                if (self.@org.opencms.ade.galleries.client.ui.CmsGalleryField::m_uploadTarget != null) {
                    var dt = event.dataTransfer;
                    var files = dt.files;
                    self.@org.opencms.ade.galleries.client.ui.CmsGalleryField::openUploadWithFiles(Lcom/google/gwt/core/client/JavaScriptObject;)(files);
                }
            }

            element.addEventListener("dragover", dragover, false);
            element.addEventListener("dragexit", dragleave, false);
            element.addEventListener("dragleave", dragleave, false);
            element.addEventListener("dragend", dragleave, false);
            element.addEventListener("drop", drop, false);
        }
    }-*/;

    /**
     * Opens the upload dialog with the given file references to upload.<p>
     *
     * @param files the file references
     */
    private void openUploadWithFiles(JavaScriptObject files) {

        JsArray<CmsFileInfo> cmsFiles = files.cast();
        List<CmsFileInfo> fileObjects = new ArrayList<CmsFileInfo>();
        for (int i = 0; i < cmsFiles.length(); ++i) {
            fileObjects.add(cmsFiles.get(i));
        }
        ((CmsDialogUploadButtonHandler)m_uploadButton.getButtonHandler()).openDialogWithFiles(fileObjects);
    }

    /**
     * Enables the special upload button.
     *
     * @param specialUpload true if the special upload button should be enabled
     */
    private void setSpecialUpload(boolean specialUpload) {

        m_specialUploadButton.setVisible(specialUpload);
        m_uploadButton.setVisible(!specialUpload);

    }

    /**
     * Updates the value to the path for the given structure id.
     *
     * @param id a structure id
     */
    private void updateValueFromId(CmsUUID id) {

        CmsRpcAction<String> action = new CmsRpcAction<String>() {

            @Override
            public void execute() {

                start(0, false);
                CmsCoreProvider.getVfsService().getSitePath(id, this);

            }

            @Override
            protected void onResponse(String result) {

                stop(false);
                m_textbox.setValue(result, true);

            }
        };
        action.execute();

    }
}
