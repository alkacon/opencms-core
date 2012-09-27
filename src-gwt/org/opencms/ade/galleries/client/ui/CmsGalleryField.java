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

import org.opencms.ade.galleries.client.I_CmsGalleryWidgetHandler;
import org.opencms.ade.galleries.client.preview.CmsCroppingParamBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
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

    /** Flag indicating files should be selectable, VFS widget only. */
    private boolean m_isIncludeFiles;

    /** Flag indicating site selector should be visible, VFS widget only. */
    private boolean m_isShowSiteSelector;

    /** Flag indicating wither the field is used as a file widget. */
    private boolean m_isVfsWidget;

    /** The reference path, for example the site path of the edited resource. */
    private String m_referencePath;

    /** The start site. */
    private String m_startSite;

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
        var config = @org.opencms.gwt.client.util.CmsDomUtil::parseJSON(Ljava/lang/String;)(configuration);
        var types = null;
        var gallerypath = null;
        var gallerytypes = null;
        var referencepath = null;
        var useformats = false;
        var imageformats = null;
        var imageformatnames = null;
        if (config.types)
            types = config.types;
        if (config.gallerypath)
            gallerypath = config.gallerypath;
        if (config.gallerytypes)
            gallerytypes = config.gallerytypes;
        if (config.resource)
            referencepath = config.resource;
        if (config.useFormats)
            useformats = config.useFormats;
        if (config.imageFormats)
            imageformats = config.imageFormats.toString();
        if (config.imageFormatNames)
            imageformatnames = config.imageFormatNames.toString();
        this.@org.opencms.ade.galleries.client.ui.CmsGalleryField::setConfiguration(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;Ljava/lang/String;)(referencepath,gallerypath,types,gallerytypes,useformats,imageformats,imageformatnames);
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
     * Sets the configuration of the gallery field.<p>
     * 
     * @param referencePath the reference path, for example the resource being edited
     * @param galleryPath the startup gallery
     * @param resourceTypes the resource types (comma separated list)
     * @param galleryTypes the gallery types (comma separated list)
     * @param useFormats the use image formats flag
     * @param imageFormats the image formats (comma separated list)
     * @param imageFormatNames the image format names (comma separated list)
     */
    public void setConfiguration(
        String referencePath,
        String galleryPath,
        String resourceTypes,
        String galleryTypes,
        boolean useFormats,
        String imageFormats,
        String imageFormatNames) {

        m_referencePath = referencePath;
        m_galleryPath = galleryPath;
        m_types = resourceTypes;
        m_galleryTypes = galleryTypes;
        m_useFormats = useFormats;
        m_imageFormats = imageFormats;
        m_imageFormatNames = imageFormatNames;
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
     * Sets the name of the input field.<p>
     * 
     * @param name of the input field
     * */
    public void setName(String name) {

        m_textbox.setName(name);

    }

    /**
     * Sets the widget configuration when used as VFS file widget.<p>
     * 
     * @param includeFiles <code>true</code> if files should be selectable
     * @param showSiteSelector <code>true</code> if the site selector should be visible
     * @param startSite the start site
     * @param referencePath the reference path
     * @param types the resource types (comma separated list)
     */
    public void setVfsConfiguration(
        boolean includeFiles,
        boolean showSiteSelector,
        String startSite,
        String referencePath,
        String types) {

        m_isVfsWidget = true;
        m_isIncludeFiles = includeFiles;
        m_isShowSiteSelector = showSiteSelector;
        m_startSite = startSite;
        m_referencePath = referencePath;
        m_types = types;
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
     * On text box change.<p>
     * @param event the event
     */
    @UiHandler("m_textbox")
    void onValueChange(ValueChangeEvent<String> event) {

        ValueChangeEvent.fire(this, getFormValueAsString());
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
        if (m_isVfsWidget) {

            GalleryTabId[] tabIds = null;
            if (m_isIncludeFiles) {
                tabIds = new GalleryTabId[] {
                    GalleryTabId.cms_tab_types,
                    GalleryTabId.cms_tab_vfstree,
                    GalleryTabId.cms_tab_sitemap,
                    GalleryTabId.cms_tab_categories,
                    GalleryTabId.cms_tab_search,
                    GalleryTabId.cms_tab_results};
            } else {
                tabIds = new GalleryTabId[] {GalleryTabId.cms_tab_vfstree};
            }
            return new CmsGalleryPopup(
                handler,
                m_referencePath,
                getFormValueAsString(),
                m_types,
                m_isShowSiteSelector,
                m_startSite,
                tabIds);
        } else {
            return new CmsGalleryPopup(
                handler,
                m_referencePath,
                m_galleryPath,
                getFormValueAsString(),
                m_types,
                m_galleryTypes,
                m_useFormats,
                m_imageFormats,
                m_imageFormatNames);
        }
    }

}
