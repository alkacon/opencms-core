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

import org.opencms.ade.galleries.client.preview.CmsCroppingParamBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextArea;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.uibinder.client.UiHandler;

/**
 * A widget for selecting a resource from an ADE gallery dialog.<p>
 *
 * @since 8.0.0
 */
public class CmsImageGalleryField extends CmsGalleryField {

    /** Parameter to split or generate the value string. */
    private static final String PARAMETER_DESC = "description=";

    /** Parameter to split or generate the value string. */
    private static final String PARAMETER_FORMAT = "format=";

    /** Parameter to split or generate the value string. */
    private static final String PARAMETER_SCALE = "scale=";

    /** The text area. */
    protected CmsTextArea m_descriptionArea;

    /** The select box. */
    protected CmsSelectBox m_formatSelection;

    /** Map of values for the Formats selection box. */
    Map<String, String> m_formats = new LinkedHashMap<String, String>();

    /** The description value. */
    private String m_description;

    /** The scale values. */
    private String m_scaleValue;

    /** The selected format. */
    private String m_selectedFormat;

    /**
     * Constructs a new gallery widget.<p>
     *
     * @param configuration the gallery configuration
     * @param allowUploads states if the upload button should be enabled for this widget
     */
    public CmsImageGalleryField(I_CmsGalleryConfiguration configuration, boolean allowUploads) {

        super(configuration, allowUploads);
        setHasImage(true);
        m_descriptionArea = new CmsTextArea();

        m_descriptionArea.addStyleName(
            org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.INSTANCE.galleryFieldCss().descriptionField());
        m_descriptionArea.getTextArea().setStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().textAreaBox());
        m_descriptionArea.getTextAreaContainer().addStyleName(
            I_CmsLayoutBundle.INSTANCE.globalWidgetCss().textAreaBoxPanel());
        m_descriptionArea.setRows(3);
        m_descriptionArea.getTextAreaContainer().onResizeDescendant();
        m_formatSelection = new CmsSelectBox();
        m_formatSelection.addStyleName(
            org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.INSTANCE.galleryFieldCss().formats());
        m_formatSelection.getOpener().addStyleName(I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxSelected());
        m_formatSelection.getSelectorPopup().addStyleName(
            I_CmsLayoutBundle.INSTANCE.globalWidgetCss().selectBoxPopup());
        ValueChangeHandler<String> changeHandler = new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                fireChange(false);
            }
        };
        addToMain(m_formatSelection);
        addToMain(m_descriptionArea);
        m_descriptionArea.addValueChangeHandler(changeHandler);
        m_formatSelection.addValueChangeHandler(changeHandler);
        generatesFormatSelection();
        m_resourceInfoPanel.setVisible(false);
        addStyleName(org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle.INSTANCE.galleryFieldCss().hasImage());
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        // do nothing
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormWidget#getFormValueAsString()
     */
    @Override
    public String getFormValueAsString() {

        String result = m_textbox.getValue().trim();
        // only append the other field values if a link is set
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result)) {
            result += "?__" + PARAMETER_SCALE + m_scaleValue;
            if (m_configuration.isUseFormats()) {
                result += "&" + PARAMETER_FORMAT + m_formatSelection.getFormValueAsString();
                m_selectedFormat = m_formatSelection.getFormValueAsString();
            }
            result += "&" + PARAMETER_DESC + URL.encode(m_descriptionArea.getFormValueAsString());
            m_description = m_descriptionArea.getFormValueAsString();
        }
        return result;
    }

    /**
     * On select box value change.<p>
     *
     * @param event the event
     */
    @UiHandler("m_formatSelection")
    public void onSelectBoxChange(ValueChangeEvent<String> event) {

        fireChange(false);
    }

    /**
     * On textarea box value change.<p>
     *
     * @param event the event
     */
    @UiHandler("m_descriptionArea")
    public void onTextAreaBoxChange(ValueChangeEvent<String> event) {

        fireChange(false);
    }

    /**
     * On textarea box resize.<p>
     *
     * @param event the event
     */
    @UiHandler("m_descriptionArea")
    public void onTextAreaBoxResize(ResizeEvent event) {

        ResizeEvent.fire(this, event.getWidth(), event.getHeight());
    }

    /**
     * Sets the name of the input field.<p>
     *
     * @param name of the input field
     * */
    @Override
    public void setName(String name) {

        m_textbox.setName(name);
        m_descriptionArea.setName(name + "_TextArea");

    }

    /**
     * Sets the widget value.<p>
     *
     * @param value the value to set
     * @param fireEvent if the change event should be fired
     */
    @Override
    public void setValue(String value, boolean fireEvent) {

        value = parseValue(value);
        m_textbox.setValue(value);
        if (fireEvent) {
            fireChange(true);
        }
    }

    /**
     * Handles the focus event on the opener.<p>
     *
     * @param event the focus event
     */
    @UiHandler("m_descriptionArea")
    protected void onFocusDescription(FocusEvent event) {

        CmsDomUtil.fireFocusEvent(this);
    }

    /**
     * Handles the focus event on the opener.<p>
     *
     * @param event the focus event
     */
    @UiHandler("m_formatSelection")
    protected void onFocusSelect(FocusEvent event) {

        CmsDomUtil.fireFocusEvent(this);
    }

    /**
     * @see org.opencms.ade.galleries.client.ui.CmsGalleryField#setValueFromGallery(java.lang.String, org.opencms.util.CmsUUID, org.opencms.ade.galleries.client.preview.CmsCroppingParamBean)
     */
    @Override
    protected void setValueFromGallery(
        String resourcePath,
        CmsUUID structureId,
        CmsCroppingParamBean croppingParameter) {

        m_croppingParam = new CmsCroppingParamBean(croppingParameter);
        String path = resourcePath + "?";
        path += croppingParameter.toString();
        path += "&" + PARAMETER_FORMAT + croppingParameter.getFormatName();
        setValue(path, true);
        m_popup.hide();
    }

    /**
     * Generates the format select box.<p>
     **/
    private void generatesFormatSelection() {

        if (m_configuration.isUseFormats()) {
            String[] formats = m_configuration.getImageFormatNames().split(",");
            for (int i = 0; i < formats.length; i++) {
                m_formats.put(formats[i].split(":")[0], formats[i].split(":")[1]);
            }
            m_formatSelection.setItems(m_formats);
        } else {
            m_formatSelection.removeFromParent();
            m_descriptionArea.setRowsGallery(4);
        }
    }

    /**
     * Parses the value and all its informations.<p>
     * First part is the URL of the image. The second one describes the scale of this image.<p>
     * The last one sets the selected format.<p>
     *
     * @param value that should be parsed
     *
     * @return the URL of the image without any parameters
     */

    private String parseValue(String value) {

        m_croppingParam = CmsCroppingParamBean.parseImagePath(value);
        String path = "";
        String params = "";
        if (value.indexOf("?") > -1) {
            path = value.substring(0, value.indexOf("?"));
            params = value.substring(value.indexOf("?"));
        } else {
            path = value;
        }
        int indexofscale = params.indexOf(PARAMETER_SCALE);
        if (indexofscale > -1) {
            String scal = "";
            int hasmoreValues = params.lastIndexOf("&");
            if (hasmoreValues > indexofscale) {
                scal = params.substring(indexofscale, params.indexOf("&")).replace(PARAMETER_SCALE, "");
            } else {
                scal = params.substring(indexofscale).replace(PARAMETER_SCALE, "");
            }
            if (!scal.equals(m_scaleValue)) {
                m_scaleValue = scal;
            }
            params = params.replace(PARAMETER_SCALE + m_scaleValue, "");

        }
        int indexofformat = params.indexOf(PARAMETER_FORMAT);
        if (indexofformat > -1) {
            int hasmoreValues = params.lastIndexOf("&");
            if (hasmoreValues > indexofformat) {
                m_selectedFormat = params.substring(indexofformat, hasmoreValues).replace(PARAMETER_FORMAT, "");
            } else {
                m_selectedFormat = params.substring(indexofformat).replace(PARAMETER_FORMAT, "");
            }
            params = params.replace(PARAMETER_FORMAT + m_selectedFormat, "");
            m_formatSelection.selectValue(m_selectedFormat);
        }
        int indexofdescritption = params.indexOf(PARAMETER_DESC);
        if (indexofdescritption > -1) {
            int hasmoreValues = params.lastIndexOf("&");
            if (hasmoreValues > indexofdescritption) {
                m_description = params.substring(indexofdescritption, hasmoreValues).replace(PARAMETER_DESC, "");
            } else {
                m_description = params.substring(indexofdescritption).replace(PARAMETER_DESC, "");
            }
            params = params.replace(PARAMETER_DESC + m_description, "");
            m_description = URL.decode(m_description);
            m_descriptionArea.setFormValueAsString(m_description);
        }
        updateUploadTarget(CmsResource.getFolderPath(path));
        if (!path.isEmpty()) {
            String imageLink = CmsCoreProvider.get().link(path);
            setImagePreview(imageLink);

        } else {
            m_imagePreview.setInnerHTML("");
        }
        return path;

    }
}
