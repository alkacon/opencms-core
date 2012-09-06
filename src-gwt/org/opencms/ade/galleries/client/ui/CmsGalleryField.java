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

import org.opencms.ade.galleries.client.CmsGalleryFactory;
import org.opencms.ade.galleries.client.I_CmsGalleryWidgetHandler;
import org.opencms.ade.galleries.client.preview.CmsCroppingParamBean;
import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsAutoHider;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A widget for selecting a resource from an ADE gallery dialog.<p>
 * 
 * @since 8.0.0
 */
public class CmsGalleryField extends Composite implements I_CmsFormWidget, I_CmsHasInit, HasValueChangeHandlers<String> {

    /**
     * The UI Binder interface for this widget.<p>
     */
    protected interface I_CmsGalleryFieldUiBinder extends UiBinder<Panel, CmsGalleryField> {
        // binder interface
    }

    /** The widget type. */
    public static final String WIDGET_TYPE = "gallery";

    /** The ui binder for this widget. */
    private static I_CmsGalleryFieldUiBinder uibinder = GWT.create(I_CmsGalleryFieldUiBinder.class);

    /** The fading element. */
    @UiField
    protected Label m_fader;

    /** The button to to open the selection. */
    @UiField
    protected CmsPushButton m_opener;

    /** The gallery pop-up. */
    protected CmsGalleryPopup m_popup;

    /** The textbox containing the currently selected path. */
    @UiField
    protected TextBox m_textbox;

    /** The start gallery path. */
    private String m_galleryPath;

    /** The gallery types. */
    private String m_galleryTypes;

    /** The image format names. */
    private String m_imageFormatNames;

    /** The image formats. */
    private String m_imageFormats;

    /** The reference path, for example the site path of the edited resource. */
    private String m_referencePath;

    /** The resource types. */
    private String m_types;

    /** The use formats flag. */
    private boolean m_useFormats;

    /** 
     * Constructs a new gallery widget.<p>
     */
    public CmsGalleryField() {

        initWidget(uibinder.createAndBindUi(this));
        m_opener.setButtonStyle(ButtonStyle.TRANSPARENT, null);
        m_opener.setImageClass(I_CmsImageBundle.INSTANCE.style().popupIcon());
    }

    /** 
     * Constructs a new gallery widget.<p>
     * 
     * @param iconImage the icon image class 
     */
    public CmsGalleryField(String iconImage) {

        this();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(iconImage)) {
            m_opener.setImageClass(I_CmsImageBundle.INSTANCE.style().popupIcon());
        }
    }

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

                CmsGalleryField galleryField = new CmsGalleryField(null);
                galleryField.parseConfiguration(widgetParams.get("configuration"));
                return galleryField;
            }
        });
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
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
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#isEnabled()
     */
    public boolean isEnabled() {

        return m_textbox.isEnabled();
    }

    /**
     * Parses the widget configuration.<p>
     * 
     * @param configuration the widget configuration as a JSON string
     */
    public native void parseConfiguration(String configuration)/*-{
        var config;
        if (typeof $wnd.JSON != 'undefined') {
            config = $wnd.JSON.parse(configuration);
        } else {
            config = eval("(" + configuration + ")");
        }
        if (config.types)
            this.@org.opencms.ade.galleries.client.ui.CmsGalleryField::setTypes(Ljava/lang/String;)(config.types);
        if (config.gallerypath)
            this.@org.opencms.ade.galleries.client.ui.CmsGalleryField::setGalleryPath(Ljava/lang/String;)(config.gallerypath);
        if (config.gallerytypes)
            this.@org.opencms.ade.galleries.client.ui.CmsGalleryField::setGalleryTypes(Ljava/lang/String;)(config.gallerytypes);
        if (config.referencepath)
            this.@org.opencms.ade.galleries.client.ui.CmsGalleryField::setReferencePath(Ljava/lang/String;)(config.referencepath);
        if (config.useFormats)
            this.@org.opencms.ade.galleries.client.ui.CmsGalleryField::setUseFormats(Z)(config.useFormats);
        if (config.imageFormats)
            this.@org.opencms.ade.galleries.client.ui.CmsGalleryField::setImageFormats(Ljava/lang/String;)(config.imageFormats.toString());
        if (config.imageFormatNames)
            this.@org.opencms.ade.galleries.client.ui.CmsGalleryField::setImageFormatNames(Ljava/lang/String;)(config.imageFormatNames.toString());
    }-*/;

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
     * Sets the galleryPath.<p>
     *
     * @param galleryPath the galleryPath to set
     */
    public void setGalleryPath(String galleryPath) {

        m_galleryPath = galleryPath;
    }

    /**
     * Sets the galleryTypes.<p>
     *
     * @param galleryTypes the galleryTypes to set
     */
    public void setGalleryTypes(String galleryTypes) {

        m_galleryTypes = galleryTypes;
    }

    /**
     * Sets the image format names.<p>
     *
     * @param imageFormatNames the image format names to set
     */
    public void setImageFormatNames(String imageFormatNames) {

        m_imageFormatNames = imageFormatNames;
    }

    /**
     * Sets the image formats.<p>
     *
     * @param imageFormats the image formats to set
     */
    public void setImageFormats(String imageFormats) {

        m_imageFormats = imageFormats;
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
     * Sets the referencePath.<p>
     *
     * @param referencePath the referencePath to set
     */
    public void setReferencePath(String referencePath) {

        m_referencePath = referencePath;
    }

    /**
     * Sets the types.<p>
     *
     * @param types the types to set
     */
    public void setTypes(String types) {

        m_types = types;
    }

    /**
     * Sets the use image formats flag.<p>
     *
     * @param useFormats the use image formats flag to set
     */
    public void setUseFormats(boolean useFormats) {

        m_useFormats = useFormats;
    }

    /**
     * Internal method which opens the gallery dialog.<p>
     */
    protected void openGalleryDialog() {

        if (m_popup == null) {
            m_popup = CmsGalleryFactory.createGalleryPopup(new I_CmsGalleryWidgetHandler() {

                public void setWidgetValue(
                    String resourcePath,
                    CmsUUID structureId,
                    CmsCroppingParamBean croppingParameter) {

                    String path = resourcePath;
                    // in case of an image check the cropping parameter
                    if ((croppingParameter != null) && (croppingParameter.isCropped() || croppingParameter.isScaled())) {
                        path += "?" + croppingParameter.toString();
                    }
                    setValue(path, true);
                    m_popup.hide();
                }
            },
                m_referencePath,
                m_galleryPath,
                getFormValueAsString(),
                m_types,
                m_galleryTypes,
                m_useFormats,
                m_imageFormats,
                m_imageFormatNames);
            m_popup.center();
        } else {
            m_popup.searchElement(getFormValueAsString());
        }
    }

    /**
     * Sets the widget value.<p>
     * 
     * @param value the value to set
     * @param fireEvent if the change event should be fired
     */
    protected void setValue(String value, boolean fireEvent) {

        m_textbox.setValue(value);
        if (fireEvent) {
            ValueChangeEvent.fire(this, getFormValueAsString());
        }
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
     * On text box value change.<p>
     * 
     * @param event the even
     */
    @UiHandler("m_textbox")
    void onTextboxChange(ValueChangeEvent<String> event) {

        ValueChangeEvent.fire(CmsGalleryField.this, getFormValueAsString());
    }

    /**
     * Toggles the fading element.<p>
     * 
     * @param faded <code>true</code> to show the fading element.<p>
     */
    void setFaded(boolean faded) {

        m_fader.setVisible(faded);
    }

}
