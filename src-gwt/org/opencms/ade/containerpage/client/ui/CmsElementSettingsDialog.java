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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.client.ui.groupeditor.CmsInheritanceContainerEditor;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsFieldSet;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsMultiCheckBox;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsDialogFormHandler;
import org.opencms.gwt.client.ui.input.form.CmsFieldsetFormFieldPanel;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.CmsInfoBoxFormFieldPanel;
import org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The element settings dialog.<p>
 */
public class CmsElementSettingsDialog extends CmsFormDialog {

    /** The template context changed flag. */
    private boolean m_changedContext;

    /** The current container id. */
    private String m_containerId;

    /** The template context info. */
    private CmsTemplateContextInfo m_contextInfo;

    /** The template context widget. */
    private CmsMultiCheckBox m_contextsWidget;

    /** The container page controller. */
    private CmsContainerpageController m_controller;

    /** The element data bean. */
    private CmsContainerElementData m_elementBean;

    /** The element panel. */
    private CmsContainerPageElementPanel m_elementWidget;

    /** The formatter select widget. */
    private CmsSelectBox m_formatterSelect;

    /** The element setting values. */
    private Map<String, String> m_settings;

    /** Checkbox to set the 'createNew' status. */
    private CmsCheckBox m_createNewCheckBox;

    /**
     * Constructor.<p>
     * 
     * @param controller the container page controller
     * @param elementWidget the element panel
     * @param elementBean the element data bean
     */
    public CmsElementSettingsDialog(
        CmsContainerpageController controller,
        CmsContainerPageElementPanel elementWidget,
        CmsContainerElementData elementBean) {

        super(Messages.get().key(Messages.GUI_PROPERTY_DIALOG_TITLE_0), new CmsForm(false), 700);
        m_elementWidget = elementWidget;
        m_controller = controller;
        m_elementBean = elementBean;
        m_contextInfo = m_controller.getData().getTemplateContextInfo();
        m_containerId = m_elementWidget.getParentTarget().getContainerId();
        CmsListInfoBean infoBean = new CmsListInfoBean();
        infoBean.setTitle(elementBean.getTitle());
        infoBean.setSubTitle(elementBean.getSitePath());
        infoBean.setResourceType(elementBean.getResourceType());
        m_settings = elementBean.getSettings();
        A_CmsFormFieldPanel formFieldPanel = null;
        if (m_contextInfo.shouldShowElementTemplateContextSelection()
            || m_elementBean.hasAlternativeFormatters(m_containerId)) {
            CmsFieldsetFormFieldPanel fieldSetPanel = new CmsFieldsetFormFieldPanel(
                infoBean,
                org.opencms.ade.containerpage.client.Messages.get().key(
                    org.opencms.ade.containerpage.client.Messages.GUI_SETTINGS_LEGEND_0));
            formFieldPanel = fieldSetPanel;
            if (m_elementBean.hasAlternativeFormatters(m_containerId)) {
                CmsFieldSet formatterFieldset = new CmsFieldSet();
                // insert as first field-set after the element info box
                fieldSetPanel.getMainPanel().insert(formatterFieldset, 1);
                formatterFieldset.setLegend(org.opencms.ade.containerpage.client.Messages.get().key(
                    org.opencms.ade.containerpage.client.Messages.GUI_FORMATTERS_LEGEND_0));
                formatterFieldset.getElement().getStyle().setMarginTop(10, Style.Unit.PX);
                LinkedHashMap<String, String> formatters = new LinkedHashMap<String, String>();
                m_formatterSelect = new CmsSelectBox();
                for (CmsFormatterConfig formatter : m_elementBean.getFormatters().get(m_containerId).values()) {
                    formatters.put(formatter.getId(), formatter.getLabel());
                    m_formatterSelect.setTitle(formatter.getId(), formatter.getJspRootPath());
                }
                m_formatterSelect.setItems(formatters);
                m_formatterSelect.selectValue(m_elementBean.getFormatterConfig(m_containerId).getId());
                m_formatterSelect.addValueChangeHandler(new ValueChangeHandler<String>() {

                    public void onValueChange(ValueChangeEvent<String> event) {

                        onFormatterChange(event.getValue());
                    }
                });
                formatterFieldset.add(m_formatterSelect);
            }
            if (CmsCoreProvider.get().getUserInfo().isDeveloper()
                && CmsContainerpageController.get().getData().isModelPage()) {
                CmsFieldSet createNewFieldSet = new CmsFieldSet();
                createNewFieldSet.setLegend(org.opencms.ade.containerpage.client.Messages.get().key(
                    org.opencms.ade.containerpage.client.Messages.GUI_CREATE_NEW_LEGEND_0

                ));
                createNewFieldSet.getElement().getStyle().setMarginTop(10, Unit.PX);
                m_createNewCheckBox = new CmsCheckBox(org.opencms.ade.containerpage.client.Messages.get().key(
                    org.opencms.ade.containerpage.client.Messages.GUI_CREATE_NEW_LABEL_0));
                createNewFieldSet.add(m_createNewCheckBox);
                fieldSetPanel.getMainPanel().insert(createNewFieldSet, 1);
                m_createNewCheckBox.setChecked(elementWidget.isCreateNew());
            } else {
                m_createNewCheckBox = null;
            }

            if (m_contextInfo.shouldShowElementTemplateContextSelection()) {
                String templateContexts = m_settings.get(CmsTemplateContextInfo.SETTING);
                if (templateContexts == null) {
                    templateContexts = CmsStringUtil.listAsString(
                        new ArrayList<String>(
                            CmsContainerpageController.get().getData().getTemplateContextInfo().getContextLabels().keySet()),
                        "|");
                } else if (templateContexts.equals(CmsTemplateContextInfo.EMPTY_VALUE)) {
                    // translate "none" to an empty selection
                    templateContexts = "";
                }
                m_settings.put(CmsTemplateContextInfo.SETTING, templateContexts);

                CmsFieldSet contextsFieldset = new CmsFieldSet();
                contextsFieldset.setLegend(m_contextInfo.getSettingDefinition().getNiceName());
                contextsFieldset.getElement().getStyle().setMarginTop(10, Style.Unit.PX);
                m_contextsWidget = new CmsMultiCheckBox(CmsStringUtil.splitAsMap(
                    m_contextInfo.getSettingDefinition().getWidgetConfiguration(),
                    "|",
                    ":"));
                for (CmsCheckBox checkbox : m_contextsWidget.getCheckboxes()) {
                    Style checkboxStyle = checkbox.getElement().getStyle();
                    checkbox.getButton().getElement().getStyle().setFontWeight(Style.FontWeight.NORMAL);
                    checkboxStyle.setMarginTop(7, Style.Unit.PX);
                }
                m_contextsWidget.setFormValueAsString(m_settings.get(CmsTemplateContextInfo.SETTING));
                m_contextsWidget.addValueChangeHandler(new ValueChangeHandler<String>() {

                    public void onValueChange(ValueChangeEvent<String> event) {

                        setTemplateContextChanged(true);
                    }
                });
                contextsFieldset.add(m_contextsWidget);
                fieldSetPanel.getMainPanel().add(contextsFieldset);
            }
            if (m_elementBean.getSettingConfig(m_containerId).isEmpty()) {
                // hide the settings field set, if there are no settings to edit
                fieldSetPanel.getFieldSet().setVisible(false);
            }
        } else {
            formFieldPanel = new CmsInfoBoxFormFieldPanel(infoBean);
        }
        getForm().setWidget(formFieldPanel);
        formFieldPanel.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().formGradientBackground());
        I_CmsFormSubmitHandler submitHandler = new I_CmsFormSubmitHandler() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler#onSubmitForm(org.opencms.gwt.client.ui.input.form.CmsForm, java.util.Map, java.util.Set)
             */
            public void onSubmitForm(CmsForm formParam, final Map<String, String> fieldValues, Set<String> editedFields) {

                submitForm(formParam, fieldValues, editedFields);
            }
        };
        CmsDialogFormHandler formHandler = new CmsDialogFormHandler();
        formHandler.setSubmitHandler(submitHandler);
        getForm().setFormHandler(formHandler);
        formHandler.setDialog(this);
        renderSettingsForm(m_elementBean.getSettingConfig(m_containerId));
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.CmsFormDialog#show()
     */
    @Override
    public void show() {

        super.show();
        if (getWidth() > 0) {
            getForm().getWidget().truncate("settings_truncation", getWidth() - 22);
        }
    }

    /**
     * Returns if the template context has been changed.<p>
     * 
     * @return <code>true</code> if the template context has been changed
     */
    boolean isTemplateContextChanged() {

        return m_changedContext;
    }

    /**
     * Handles the formatter selection changes.<p>
     * 
     * @param formatterId the formatter id
     */
    void onFormatterChange(String formatterId) {

        CmsFormatterConfig config = m_elementBean.getFormatters().get(m_containerId).get(formatterId);
        renderSettingsForm(config.getSettingConfig());
    }

    /**
     * Renders the settings form.<p>
     * 
     * @param settingsConfig the settings configuration
     */
    void renderSettingsForm(Map<String, CmsXmlContentProperty> settingsConfig) {

        getForm().removeGroup("");
        Map<String, I_CmsFormField> formFields = CmsBasicFormField.createFields(settingsConfig.values());
        for (I_CmsFormField field : formFields.values()) {
            String fieldId = field.getId();
            String initialValue = m_settings.get(fieldId);
            if (initialValue == null) {
                CmsXmlContentProperty propDef = settingsConfig.get(fieldId);
                initialValue = propDef.getDefault();
            }
            getForm().addField(field, initialValue);
        }
        getForm().render();
        A_CmsFormFieldPanel formWidget = getForm().getWidget();
        if (formWidget instanceof CmsFieldsetFormFieldPanel) {
            ((CmsFieldsetFormFieldPanel)formWidget).getFieldSet().setVisible(!settingsConfig.isEmpty());
        }
        if (getWidth() > 0) {
            getForm().getWidget().truncate("settings_truncation", getWidth() - 12);
        }
    }

    /**
     * Sets the template context changed flag.<p>
     * @param changed the template context changed flag
     */
    void setTemplateContextChanged(boolean changed) {

        m_changedContext = changed;
    }

    /**
     * Submits the settings form.<p>
     * 
     * @param formParam the form
     * @param fieldValues the field values
     * @param editedFields the changed fields
     */
    void submitForm(CmsForm formParam, final Map<String, String> fieldValues, Set<String> editedFields) {

        if (CmsInheritanceContainerEditor.getInstance() != null) {
            CmsInheritanceContainerEditor.getInstance().onSettingsEdited();
        }
        if (m_contextsWidget != null) {
            String newTemplateContexts = m_contextsWidget.getFormValueAsString();
            if ((newTemplateContexts == null) || "".equals(newTemplateContexts)) {
                newTemplateContexts = CmsTemplateContextInfo.EMPTY_VALUE;
                // translate an empty selection to "none" 
            }
            fieldValues.put(CmsTemplateContextInfo.SETTING, newTemplateContexts);
        }
        final boolean hasFormatterChanges;
        if (m_formatterSelect != null) {
            fieldValues.put(
                CmsFormatterConfig.getSettingsKeyForContainer(m_containerId),
                m_formatterSelect.getFormValueAsString());
            hasFormatterChanges = true;
        } else {
            hasFormatterChanges = false;
        }
        final Map<String, String> filteredFieldValues = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if ((value != null) && (value.length() > 0)) {
                filteredFieldValues.put(key, value);
            }
        }
        if (m_createNewCheckBox != null) {
            m_elementWidget.setIsNew(m_createNewCheckBox.isChecked());
        }
        m_controller.reloadElementWithSettings(
            m_elementWidget,
            m_elementBean.getClientId(),
            filteredFieldValues,
            new AsyncCallback<CmsContainerPageElementPanel>() {

                public void onFailure(Throwable caught) {

                    // will not be executed
                }

                public void onSuccess(CmsContainerPageElementPanel result) {

                    if (isTemplateContextChanged()) {
                        // if the context multiselect box isn't displayed, of course it can't change values,
                        // and this code won't be executed.
                        CmsContainerpageController.get().handleChangeTemplateContext(
                            result,
                            filteredFieldValues.get(CmsTemplateContextInfo.SETTING));
                    }
                    if (hasFormatterChanges) {
                        updateCss();
                    }
                    if (result.getElement().getInnerHTML().contains(CmsGwtConstants.FORMATTER_RELOAD_MARKER)
                        && !CmsContainerpageController.get().isGroupcontainerEditing()) {
                        CmsContainerpageController.get().reloadPage();
                    }
                }

            });
    }

    /**
     * Updates the CSS resources for the selected formatter.<p>
     */
    void updateCss() {

        String formatterId = m_formatterSelect.getFormValueAsString();
        CmsFormatterConfig formatter = m_elementBean.getFormatters().get(m_containerId).get(formatterId);
        Set<String> cssResources = formatter.getCssResources();
        for (String cssResource : cssResources) {
            CmsDomUtil.ensureStyleSheetIncluded(cssResource);
        }
        if (formatter.hasInlineCss()) {
            ensureInlineCss(formatterId, formatter.getInlineCss());
        }
    }

    /**
     * Ensures the CSS snippet with the given ID is present.<p>
     * 
     * @param formatterId the ID
     * @param cssContent the CSS snippet
     */
    private native void ensureInlineCss(String formatterId, String cssContent)/*-{
                                                                              var styles = $wnd.document.styleSheets;
                                                                              for ( var i = 0; i < styles.length; i++) {
                                                                              // IE uses the owningElement property
                                                                              var styleNode = styles[i].owningElement ? styles[i].owningElement
                                                                              : styles[i].ownerNode;
                                                                              if (styleNode != null && styleNode.rel == formatterId) {
                                                                              // inline css is present
                                                                              return;
                                                                              }
                                                                              }
                                                                              // include inline css into head
                                                                              var headID = $wnd.document.getElementsByTagName("head")[0];
                                                                              var cssNode = $wnd.document.createElement('style');
                                                                              cssNode.type = 'text/css';
                                                                              cssNode.rel = formatterId;
                                                                              if (cssNode.styleSheet) {
                                                                              // in case of IE
                                                                              cssNode.styleSheet.cssText = cssContent;
                                                                              } else {
                                                                              // otherwise
                                                                              cssNode.appendChild(document.createTextNode(cssContent));
                                                                              }
                                                                              headID.appendChild(cssNode);
                                                                              }-*/;
}
