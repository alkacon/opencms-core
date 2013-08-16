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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.client.CmsGalleryConfigurationJSO;
import org.opencms.ade.galleries.client.I_CmsGalleryWidgetHandler;
import org.opencms.ade.galleries.client.preview.CmsCroppingParamBean;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsResultItemBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryService;
import org.opencms.ade.galleries.shared.rpc.I_CmsGalleryServiceAsync;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.input.CmsSimpleTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
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
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

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
    protected interface I_CmsGalleryFieldUiBinder extends UiBinder<Panel, CmsGalleryField> {
        // binder interface
    }

    /** Timer to update the resource info box. */
    class InfoTimer extends Timer {

        /** The resource path. */
        private String m_path;

        /**
         * Constructor.<p>
         * 
         * @param path the resource path
         */
        InfoTimer(String path) {

            m_path = path;
        }

        /**
         * @see com.google.gwt.user.client.Timer#run()
         */
        @Override
        public void run() {

            updateResourceInfo(m_path);
            clearInfoTimer();
        }
    }

    /**
     * Handler to fire resize event on resource info widget open/close.<p>
     */
    protected class OpenCloseHandler implements CloseHandler<CmsListItemWidget>, OpenHandler<CmsListItemWidget> {

        /**
         * @see com.google.gwt.event.logical.shared.CloseHandler#onClose(com.google.gwt.event.logical.shared.CloseEvent)
         */
        public void onClose(CloseEvent<CmsListItemWidget> event) {

            fireResize();
        }

        /**
         * @see com.google.gwt.event.logical.shared.OpenHandler#onOpen(com.google.gwt.event.logical.shared.OpenEvent)
         */
        public void onOpen(OpenEvent<CmsListItemWidget> event) {

            fireResize();
        }
    }

    /** The widget type. */
    public static final String WIDGET_TYPE = "gallery";

    /** The ui binder for this widget. */
    private static I_CmsGalleryFieldUiBinder uibinder = GWT.create(I_CmsGalleryFieldUiBinder.class);

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        // registers a factory for creating new instances of this widget
        CmsWidgetFactoryRegistry.instance().registerFactory(WIDGET_TYPE, new I_CmsFormWidgetFactory() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map)
             */
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams) {

                CmsGalleryConfigurationJSO conf = CmsGalleryConfigurationJSO.parseConfiguration(widgetParams.get("configuration"));
                CmsGalleryField galleryField = new CmsGalleryField(conf);
                return galleryField;
            }
        });
    }

    /** The fading element. */
    @UiField
    protected Label m_fader;

    /** The DIV carrying the input field. */
    @UiField
    protected DivElement m_fieldBox;

    /** The image preview element. */
    @UiField
    protected DivElement m_imagePreview;

    /** The button to to open the selection. */
    @UiField
    protected CmsPushButton m_opener;

    /** The gallery pop-up. */
    protected CmsGalleryPopup m_popup;

    /** The resource info panel. */
    @UiField
    protected FlowPanel m_resourceInfoPanel;

    /** The textbox containing the currently selected path. */
    @UiField
    protected CmsSimpleTextBox m_textbox;

    /** The gallery configuration. */
    I_CmsGalleryConfiguration m_configuration;

    /** The gallery service instance. */
    private I_CmsGalleryServiceAsync m_gallerySvc;

    /** The has image flag. */
    private boolean m_hasImage;

    /** The info timer instance. */
    private InfoTimer m_infoTimer;

    /** The previous field value. */
    private String m_previousValue;

    /** 
     * Constructs a new gallery widget.<p>
     * 
     * @param configuration the gallery configuration 
     */
    public CmsGalleryField(I_CmsGalleryConfiguration configuration) {

        initWidget(uibinder.createAndBindUi(this));
        m_configuration = configuration;
        I_CmsLayoutBundle.INSTANCE.galleryFieldCss().ensureInjected();
        m_opener.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        m_opener.setImageClass(I_CmsImageBundle.INSTANCE.style().popupIcon());
    }

    /** 
     * Constructs a new gallery widget.<p>
     * 
     * @param configuration the gallery configuration
     * @param iconImage the icon image class 
     */
    public CmsGalleryField(I_CmsGalleryConfiguration configuration, String iconImage) {

        this(configuration);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(iconImage)) {
            m_opener.setImageClass(I_CmsImageBundle.INSTANCE.style().popupIcon());
        }
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
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return m_textbox.addFocusHandler(handler);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasResizeHandlers#addResizeHandler(com.google.gwt.event.logical.shared.ResizeHandler)
     */
    public HandlerRegistration addResizeHandler(ResizeHandler handler) {

        return addHandler(handler, ResizeEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Clears the info timer.<p>
     */
    void clearInfoTimer() {

        m_infoTimer = null;
    }

    /**
     * Creates the gallery pop-up.<p>
     * 
     * @return the gallery pop-up
     */
    private CmsGalleryPopup createPopup() {

        I_CmsGalleryWidgetHandler handler = new I_CmsGalleryWidgetHandler() {

            public void setWidgetValue(String resourcePath, CmsUUID structureId, CmsCroppingParamBean croppingParameter) {

                String path = resourcePath;
                // in case of an image check the cropping parameter
                if ((croppingParameter != null) && (croppingParameter.isCropped() || croppingParameter.isScaled())) {
                    path += "?" + croppingParameter.toString();
                }
                setValue(path, true);
                m_popup.hide();
            }
        };
        m_configuration.setCurrentElement(getFormValueAsString());
        return new CmsGalleryPopup(handler, m_configuration);
    }

    /**
     * Displays the resource info.<p>
     * 
     * @param info the resource info
     */
    void displayResourceInfo(CmsResultItemBean info) {

        if (m_hasImage) {
            CmsCroppingParamBean cropping = CmsCroppingParamBean.parseImagePath(getFormValueAsString());
            String imagePath = info.getViewLink();
            String dimension = info.getDimension();
            int marginTop = 0;
            if (cropping.isCropped()) {
                dimension = cropping.getTargetWidth() + " x " + cropping.getTargetHeight();
                String[] dimensions = dimension.split("x");
                cropping.setOrgWidth(CmsClientStringUtil.parseInt(dimensions[0].trim()));
                cropping.setOrgHeight(CmsClientStringUtil.parseInt(dimensions[1].trim()));
                CmsCroppingParamBean restricted = cropping.getRestrictedSizeParam(110, 165);
                imagePath += "?" + restricted;
                marginTop = (110 - restricted.getTargetHeight()) / 2;
            } else {
                imagePath += "?__scale=w:165,h:110,t:1,c:white,r:2";
            }
            Element image = DOM.createImg();
            image.setAttribute("src", imagePath);
            image.getStyle().setMarginTop(marginTop, Unit.PX);
            m_imagePreview.setInnerHTML("");
            m_imagePreview.appendChild(image);
            m_resourceInfoPanel.add(new CmsImageInfo(info, dimension));
        } else {
            CmsListItemWidget widget = new CmsListItemWidget(info);
            OpenCloseHandler handler = new OpenCloseHandler();
            widget.addCloseHandler(handler);
            widget.addOpenHandler(handler);
            widget.setIcon(CmsIconUtil.getResourceIconClasses(info.getType(), info.getPath(), false));
            m_resourceInfoPanel.add(widget);
            int width = m_resourceInfoPanel.getOffsetWidth();
            if (width > 0) {
                widget.truncate("STANDARD", width);
            }
        }
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
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getApparentValue()
     */
    public String getApparentValue() {

        return getFormValueAsString();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFieldType()
     */
    public FieldType getFieldType() {

        return FieldType.STRING;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValue()
     */
    public Object getFormValue() {

        return m_textbox.getValue();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    public String getFormValueAsString() {

        return m_textbox.getValue();
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
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return m_textbox.isEnabled();
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

        CmsDomUtil.ensureMouseOut(m_opener);
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
        m_infoTimer = new InfoTimer(event.getValue());
        m_infoTimer.schedule(300);
    }

    /**
     * Internal method which opens the gallery dialog.<p>
     */
    protected void openGalleryDialog() {

        if (m_popup == null) {
            m_popup = createPopup();
            m_popup.center();
        } else {
            m_popup.searchElement(getFormValueAsString());
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#reset()
     */
    public void reset() {

        m_textbox.setValue("");
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setAutoHideParent(org.opencms.gwt.client.ui.I_CmsAutoHider)
     */
    public void setAutoHideParent(I_CmsAutoHider autoHideParent) {

        // do nothing 
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {

        m_textbox.setEnabled(enabled);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String errorMessage) {

        // do nothing 
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
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#setFormValueAsString(java.lang.String)
     */
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
     * Sets the widget value.<p>
     * 
     * @param value the value to set
     * @param fireEvent if the change event should be fired
     */
    protected void setValue(String value, boolean fireEvent) {

        m_textbox.setValue(value);
        updateResourceInfo(value);
        m_previousValue = value;
        if (fireEvent) {
            fireChange(true);
        }
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
    }
}
