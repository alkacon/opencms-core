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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.client.ui.groupeditor.CmsInheritanceContainerEditor;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElement.ModelGroupState;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsFieldSet;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuButton;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
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
import org.opencms.gwt.client.ui.input.form.CmsFormRow;
import org.opencms.gwt.client.ui.input.form.CmsInfoBoxFormFieldPanel;
import org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler;
import org.opencms.gwt.client.ui.resourceinfo.CmsResourceInfoView.ContextMenuHandler;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsTemplateContextInfo;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
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

/**
 * The element settings dialog.<p>
 */
public class CmsElementSettingsDialog extends CmsFormDialog {

    /** The model group options. */
    protected enum GroupOption {

        /** The copy elements option. */
        copy(Messages.get().key(Messages.GUI_MODEL_GROUP_OPTION_COPY_0)),

        /** The disabled option. */
        disabled(Messages.get().key(Messages.GUI_MODEL_GROUP_OPTION_DISABLED_0)),

        /** The reuse option. */
        reuse(Messages.get().key(Messages.GUI_MODEL_GROUP_OPTION_REUSE_0));

        /** The option label. */
        private String m_label;

        /**
         * Constructor.<p>
         *
         * @param label the label
         */
        GroupOption(String label) {
            m_label = label;
        }

        /**
         * Returns the option label.<p>
         *
         * @return the option label
         */
        protected String getLabel() {

            return m_label;
        }
    }

    /** The hidden field widget name. */
    private static final String HIDDEN_FIELD_WIDGET = "hidden";

    /** The template context changed flag. */
    private boolean m_changedContext;

    /** The current container id. */
    private String m_containerId;

    /** The template context info. */
    private CmsTemplateContextInfo m_contextInfo;

    /** The template context widget. */
    private CmsMultiCheckBox m_contextsWidget;

    /** The container page controller. */
    CmsContainerpageController m_controller;

    /** Checkbox to set the 'createNew' status. */
    private CmsCheckBox m_createNewCheckBox;

    /** The element data bean. */
    private CmsContainerElementData m_elementBean;

    /** The element panel. */
    private CmsContainerPageElementPanel m_elementWidget;

    /** The formatter select widget. */
    private CmsSelectBox m_formatterSelect;

    /** The break up model group checkbox. */
    private CmsCheckBox m_modelGroupBreakUp;

    /** Checkbox to set the 'model group' status. */
    CmsSelectBox m_modelGroupSelect;

    /** The element setting values. */
    private Map<String, String> m_settings;

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

        boolean isEditableModelGroup = CmsCoreProvider.get().getUserInfo().isDeveloper()
            && m_controller.getData().isModelGroup()
            && ((m_controller.getModelGroupElementId() == null)
                || CmsContainerpageController.getServerId(elementBean.getClientId()).equals(
                    m_controller.getModelGroupElementId()));
        boolean isDeveloper = CmsCoreProvider.get().getUserInfo().isDeveloper();
        if (m_contextInfo.shouldShowElementTemplateContextSelection()
            || isDeveloper
            || m_elementBean.hasAlternativeFormatters(m_containerId)) {
            CmsFieldsetFormFieldPanel fieldSetPanel = new CmsFieldsetFormFieldPanel(
                infoBean,
                Messages.get().key(Messages.GUI_SETTINGS_LEGEND_0));
            formFieldPanel = fieldSetPanel;
            if (m_elementBean.hasAlternativeFormatters(m_containerId)) {
                CmsFieldSet formatterFieldset = new CmsFieldSet();
                // insert as first field-set after the element info box
                fieldSetPanel.getMainPanel().insert(formatterFieldset, 1);
                formatterFieldset.setLegend(Messages.get().key(Messages.GUI_FORMATTERS_LEGEND_0));
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
            if (isDeveloper || m_controller.getData().isModelPage() || isEditableModelGroup) {
                CmsFieldSet modelGroupFieldSet = new CmsFieldSet();
                modelGroupFieldSet.setLegend(Messages.get().key(Messages.GUI_CREATE_NEW_LEGEND_0

                ));
                modelGroupFieldSet.getElement().getStyle().setMarginTop(10, Unit.PX);

                if (isEditableModelGroup && !elementWidget.hasModelGroupParent()) {
                    addModelGroupSettings(elementBean, elementWidget, modelGroupFieldSet);
                } else if (!elementWidget.isModelGroup()) {
                    addCreateNewCheckbox(elementBean, modelGroupFieldSet);
                }
                if (modelGroupFieldSet.getWidgetCount() > 0) {
                    fieldSetPanel.getMainPanel().insert(modelGroupFieldSet, 1);
                }

            } else if (elementWidget.isModelGroup()) {
                CmsFieldSet modelGroupFieldSet = new CmsFieldSet();
                modelGroupFieldSet.setLegend(Messages.get().key(Messages.GUI_CREATE_NEW_LEGEND_0

                ));
                modelGroupFieldSet.getElement().getStyle().setMarginTop(10, Unit.PX);
                m_modelGroupBreakUp = new CmsCheckBox(Messages.get().key(Messages.GUI_MODEL_GROUP_BREAK_UP_0));
                m_modelGroupBreakUp.setDisplayInline(false);
                m_modelGroupBreakUp.getElement().getStyle().setMarginTop(7, Style.Unit.PX);
                modelGroupFieldSet.add(m_modelGroupBreakUp);
                fieldSetPanel.getMainPanel().insert(modelGroupFieldSet, 1);
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
                m_contextsWidget = new CmsMultiCheckBox(
                    CmsStringUtil.splitAsMap(m_contextInfo.getSettingDefinition().getWidgetConfiguration(), "|", ":"));
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
        String id = CmsContainerpageController.getServerId(elementBean.getClientId());
        if (CmsUUID.isValidUUID(id) && !(new CmsUUID(id).isNullUUID())) {
            CmsContextMenuButton menuButton = new CmsContextMenuButton(new CmsUUID(id), new ContextMenuHandler());
            menuButton.addStyleName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
            formFieldPanel.getInfoWidget().addButton(menuButton);
        }
        getForm().setWidget(formFieldPanel);
        formFieldPanel.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().formGradientBackground());
        I_CmsFormSubmitHandler submitHandler = new I_CmsFormSubmitHandler() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler#onSubmitForm(org.opencms.gwt.client.ui.input.form.CmsForm, java.util.Map, java.util.Set)
             */
            public void onSubmitForm(
                CmsForm formParam,
                final Map<String, String> fieldValues,
                Set<String> editedFields) {

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
            CmsXmlContentProperty propDef = settingsConfig.get(fieldId);
            // skip hidden fields
            if (!HIDDEN_FIELD_WIDGET.equals(propDef.getWidget())) {
                String initialValue = m_settings.get(fieldId);
                if (initialValue == null) {

                    initialValue = propDef.getDefault();
                }
                getForm().addField(field, initialValue);
            }
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
    @SuppressWarnings("incomplete-switch")
    void submitForm(CmsForm formParam, final Map<String, String> fieldValues, Set<String> editedFields) {

        String modelGroupId = null;

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
        if (m_createNewCheckBox != null) {
            m_elementWidget.setCreateNew(m_createNewCheckBox.isChecked());
            fieldValues.put(CmsContainerElement.CREATE_AS_NEW, Boolean.toString(m_createNewCheckBox.isChecked()));
        }
        if (m_modelGroupSelect != null) {
            GroupOption group = GroupOption.valueOf(m_modelGroupSelect.getFormValueAsString());
            switch (group) {
                case disabled:
                    fieldValues.put(CmsContainerElement.MODEL_GROUP_STATE, ModelGroupState.noGroup.name());
                    fieldValues.put(CmsContainerElement.USE_AS_COPY_MODEL, Boolean.toString(false));
                    break;
                case copy:
                    fieldValues.put(CmsContainerElement.MODEL_GROUP_STATE, ModelGroupState.isModelGroup.name());
                    fieldValues.put(CmsContainerElement.USE_AS_COPY_MODEL, Boolean.toString(true));
                    break;
                case reuse:
                    fieldValues.put(CmsContainerElement.MODEL_GROUP_STATE, ModelGroupState.isModelGroup.name());
                    fieldValues.put(CmsContainerElement.USE_AS_COPY_MODEL, Boolean.toString(false));
                    break;
            }
            if (group != GroupOption.disabled) {
                modelGroupId = CmsContainerpageController.getServerId(m_elementBean.getClientId());
            }
        }

        if ((m_modelGroupBreakUp != null) && m_modelGroupBreakUp.isChecked()) {
            fieldValues.put(CmsContainerElement.MODEL_GROUP_STATE, ModelGroupState.noGroup.name());
        }

        final Map<String, String> filteredFieldValues = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if ((value != null) && (value.length() > 0)) {
                filteredFieldValues.put(key, value);
            }
        }
        final String changeModelGroupId = modelGroupId;
        m_controller.reloadElementWithSettings(
            m_elementWidget,
            m_elementBean.getClientId(),
            filteredFieldValues,
            new I_CmsSimpleCallback<CmsContainerPageElementPanel>() {

                public void execute(CmsContainerPageElementPanel result) {

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
                    if (m_modelGroupSelect != null) {
                        m_controller.setModelGroupElementId(changeModelGroupId);
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
     * Adds the create new checkbox to the given field set.<p>
     *
     * @param elementBean the element bean
     * @param fieldSet the field set
     */
    private void addCreateNewCheckbox(CmsContainerElementData elementBean, CmsFieldSet fieldSet) {

        m_createNewCheckBox = new CmsCheckBox(Messages.get().key(Messages.GUI_CREATE_NEW_LABEL_0));
        m_createNewCheckBox.setDisplayInline(false);
        m_createNewCheckBox.getElement().getStyle().setMarginTop(7, Style.Unit.PX);
        m_createNewCheckBox.setChecked(elementBean.isCreateNew());
        fieldSet.add(m_createNewCheckBox);
    }

    /**
     * Adds the model group settings fields to the given field set.<p>
     *
     * @param elementBean the element bean
     * @param elementWidget the element widget
     * @param fieldSet the field set
     */
    private void addModelGroupSettings(
        CmsContainerElementData elementBean,
        CmsContainerPageElementPanel elementWidget,
        CmsFieldSet fieldSet) {

        Map<String, String> groupOptions = new LinkedHashMap<String, String>();
        groupOptions.put(GroupOption.disabled.name(), GroupOption.disabled.getLabel());
        groupOptions.put(GroupOption.copy.name(), GroupOption.copy.getLabel());
        groupOptions.put(GroupOption.reuse.name(), GroupOption.reuse.getLabel());
        m_modelGroupSelect = new CmsSelectBox(groupOptions);
        if (elementWidget.isModelGroup()) {
            if (Boolean.valueOf(elementBean.getSettings().get(CmsContainerElement.USE_AS_COPY_MODEL)).booleanValue()) {
                m_modelGroupSelect.selectValue(GroupOption.copy.name());
            } else {
                m_modelGroupSelect.selectValue(GroupOption.reuse.name());
            }
        }
        CmsFormRow selectRow = new CmsFormRow();
        selectRow.getLabel().setText(Messages.get().key(Messages.GUI_USE_AS_MODEL_GROUP_LABEL_0));
        selectRow.getWidgetContainer().add(m_modelGroupSelect);
        fieldSet.add(selectRow);
    }

    /**
     * Ensures the CSS snippet with the given ID is present.<p>
     *
     * @param formatterId the ID
     * @param cssContent the CSS snippet
     */
    private native void ensureInlineCss(String formatterId, String cssContent)/*-{
		var styles = $wnd.document.styleSheets;
		for (var i = 0; i < styles.length; i++) {
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
