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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.property.CmsActiveFieldData;
import org.opencms.gwt.client.property.CmsPropertySubmitHandler;
import org.opencms.gwt.client.property.CmsSimplePropertyEditorHandler;
import org.opencms.gwt.client.property.CmsVfsModePropertyEditor;
import org.opencms.gwt.client.property.I_CmsPropertySaver;
import org.opencms.gwt.client.property.definition.CmsPropertyDefinitionButton;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.form.CmsDialogFormHandler;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.I_CmsFormHandler;
import org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The class for the "edit properties" context menu entries.<p>
 *
 * @since 8.0.0
 */
public final class CmsEditProperties implements I_CmsHasContextMenuCommand {

    /**
     * Interface used to access the next/previous file for which to edit properties.
     */
    public static interface I_MultiFileNavigation {

        /**
         * Requests the next / previous file id.<p>
         *
         * @param offset should be 1 for the next file, or -1 for the previous file
         * @param callback the callback to call with the id
         */
        void requestNextFile(int offset, AsyncCallback<CmsUUID> callback);
    }

    /**
     * Helper class which encapsulates the differences between the contexts where the property edit dialog is opened.<p>
     */
    public static class PropertyEditingContext {

        /** The cancel handler. */
        protected Runnable m_cancelHandler;

        /** The dialog instance. */
        protected CmsFormDialog m_formDialog;

        /** The form handler. */
        protected I_CmsFormHandler m_formHandler;

        /** Enable/disable property definition button. */
        private boolean m_allowCreateProperties = true;

        /** Flag to control whether the file name field should be focused after opening the property dialog. */
        private boolean m_focusNameField;

        /** The file navigation. */
        private I_MultiFileNavigation m_multiFileNavigation;

        /** The property saver. */
        private I_CmsPropertySaver m_propertySaver;

        /**
         * Returns true if the property definition button should be enabled.<p>
         *
         * @return true if the user should be able to define new properties
         */
        public boolean allowCreateProperties() {

            return m_allowCreateProperties;
        }

        /**
         * Creates the property definition button.<p>
         *
         * @return the property definition button
         */
        public CmsPropertyDefinitionButton createPropertyDefinitionButton() {

            return new CmsPropertyDefinitionButton() {

                /**
                 * @see org.opencms.gwt.client.property.definition.CmsPropertyDefinitionButton#onBeforeEditPropertyDefinition()
                 */
                @Override
                public void onBeforeEditPropertyDefinition() {

                    m_formDialog.hide();
                }
            };

        }

        /**
         * Gets the form dialog.<p>
         *
         * @return the form dialog
         */
        public CmsFormDialog getDialog() {

            return m_formDialog;
        }

        /**
         * Gets the property saver.<p>
         *
         * @return the property saver
         */
        public I_CmsPropertySaver getPropertySaver() {

            return m_propertySaver;
        }

        /**
         * Initializes the close handler of the dialog.<p>
         */
        public void initCloseHandler() {

            if (m_cancelHandler != null) {
                m_formDialog.addCloseHandler(new CloseHandler<PopupPanel>() {

                    public void onClose(CloseEvent<PopupPanel> event) {

                        if (!m_formHandler.isSubmitting()) {
                            m_cancelHandler.run();
                        }
                    }
                });
            }
        }

        /**
         * Return true if  the file name field should be focused after opening the dialog.<p>
         *
         * @return true if the file name field should be focused
         */
        public boolean isFocusNameField() {

            return m_focusNameField;
        }

        /**
         * Enables / disables the 'define property' functionality.<p>
         *
         * @param allowCreateProperties true if the user should be able to create new properties
         */
        public void setAllowCreateProperties(boolean allowCreateProperties) {

            m_allowCreateProperties = allowCreateProperties;
        }

        /**
         * Sets the cancel handler.<p>
         *
         * @param cancelHandler the cancel handler
         */
        public void setCancelHandler(Runnable cancelHandler) {

            m_cancelHandler = cancelHandler;
        }

        /**
         * Sets the form dialog.<p>
         *
         * @param formDialog the form dialog
         */
        public void setDialog(CmsFormDialog formDialog) {

            m_formDialog = formDialog;
        }

        /**
         * Enables / disables focusing on the name field.
         *
         * @param focusNameField true if the file name field should be focused after opening the dialog
         *
         * */
        public void setFocusNameField(boolean focusNameField) {

            m_focusNameField = focusNameField;
        }

        /**
         * Sets the form handler.<p>
         *
         * @param formHandler the form handler
         */
        public void setFormHandler(I_CmsFormHandler formHandler) {

            m_formHandler = formHandler;
        }

        /**
         * Sets the file navigation object.<p>
         *
         * @param nav the file navigation object
         */
        public void setMultiFileNavigation(I_MultiFileNavigation nav) {

            m_multiFileNavigation = nav;
        }

        /**
         * Sets the property saver.<p>
         *
         * @param saver the property saver
         */
        public void setPropertySaver(I_CmsPropertySaver saver) {

            m_propertySaver = saver;
        }

        /**
         * Gets the file navigation object.<p>
         *
         * @return the file navigation object
         */
        private I_MultiFileNavigation getMultiFileNavigation() {

            return m_multiFileNavigation;

        }

    }

    /**
     * Helper class for editing properties in the workplace.<p>
     */
    public static class WorkplacePropertyEditorContext implements I_CmsFormHandler {

        /** The cancel handler. */
        private Runnable m_cancelHandler;

        /** The context menu handler. */
        private I_CmsContextMenuHandler m_contextMenuHandler;

        /** The dialog. */
        private PropertiesFormDialog m_dialog;

        /** The edit context. */
        private PropertyEditingContext m_editContext;

        /** True if name should be edited. */
        private boolean m_editName;

        /** The property editor. */
        private CmsVfsModePropertyEditor m_editor;

        /** True if ADE template selection should be enabled. */
        private boolean m_enableAdeTemplateSelect;

        /** The editor handler. */
        private PropertyEditorHandler m_handler;

        /** True if last button was prev/next. */
        private boolean m_isPrevNext;

        /** The active field data. */
        private CmsActiveFieldData m_prevFieldData;

        /** Structure id of the current resource. */
        private CmsUUID m_structureId;

        /** The submit handler.*/
        private I_CmsFormSubmitHandler m_submitHandler;

        /** True if form is currently being submitted. */
        private boolean m_submitting;

        /**
         * Creates a new instance.<p>
         *
         * @param structureId the structure id of the resource
         * @param contextMenuHandler the context menu handler
         * @param editName true if name should be editable
         * @param cancelHandler the cancel handler
         * @param enableAdeTemplateSelect true if ADE template selection should be enabled
         * @param editContext the edit context
         * @param prevFieldData the previous field data
         */
        public WorkplacePropertyEditorContext(
            CmsUUID structureId,
            I_CmsContextMenuHandler contextMenuHandler,
            boolean editName,
            Runnable cancelHandler,
            boolean enableAdeTemplateSelect,
            PropertyEditingContext editContext,
            CmsActiveFieldData prevFieldData) {
            m_structureId = structureId;
            m_contextMenuHandler = contextMenuHandler;
            m_editName = editName;
            m_cancelHandler = cancelHandler;
            m_enableAdeTemplateSelect = enableAdeTemplateSelect;
            m_editContext = editContext;
            m_prevFieldData = prevFieldData;

            m_dialog = new PropertiesFormDialog("XXX", null);

            m_editContext.setDialog(m_dialog);
            m_dialog.catchNotifications();
            @SuppressWarnings("synthetic-access")
            final I_MultiFileNavigation fileNavigation = m_editContext.getMultiFileNavigation();
            List<CmsPushButton> additionalLeftButtons = Lists.newArrayList();
            if (fileNavigation != null) {
                CmsPushButton prevButton = new CmsPushButton();
                prevButton.setText("<<");
                String prevText = org.opencms.gwt.client.Messages.get().key(
                    org.opencms.gwt.client.Messages.GUI_BUTTON_PREV_RESOURCE_0);
                prevButton.setTitle(prevText);
                String nextText = org.opencms.gwt.client.Messages.get().key(
                    org.opencms.gwt.client.Messages.GUI_BUTTON_NEXT_RESOURCE_0);
                CmsPushButton nextButton = new CmsPushButton();
                nextButton.setText(">>");
                nextButton.setTitle(nextText);
                for (CmsPushButton button : new CmsPushButton[] {prevButton, nextButton}) {
                    button.setButtonStyle(ButtonStyle.TEXT, ButtonColor.BLUE);
                    // button.getElement().getStyle().setFloat(Float.LEFT);
                }

                final AsyncCallback<CmsUUID> loadHandler = new AsyncCallback<CmsUUID>() {

                    public void onFailure(Throwable caught) {

                        CmsDebugLog.consoleLog("" + caught);
                    }

                    @SuppressWarnings("synthetic-access")
                    public void onSuccess(CmsUUID nextId) {

                        CmsActiveFieldData fieldData = getActiveFieldData();
                        m_prevFieldData = fieldData;
                        m_structureId = nextId;
                        editProperties();
                    }
                };
                prevButton.addClickHandler(new ClickHandler() {

                    @SuppressWarnings("synthetic-access")
                    public void onClick(ClickEvent event) {

                        m_isPrevNext = true;
                        m_dialog.getForm().validateAndSubmit();
                        m_handler.setNextAction(new Runnable() {

                            public void run() {

                                fileNavigation.requestNextFile(-1, loadHandler);
                            }
                        });
                    }
                });
                nextButton.addClickHandler(new ClickHandler() {

                    @SuppressWarnings("synthetic-access")
                    public void onClick(ClickEvent event) {

                        m_isPrevNext = true;
                        m_dialog.getForm().validateAndSubmit();
                        m_handler.setNextAction(new Runnable() {

                            public void run() {

                                fileNavigation.requestNextFile(1, loadHandler);
                            }
                        });

                    }
                });
                additionalLeftButtons.add(prevButton);
                additionalLeftButtons.add(nextButton);
            }

            CmsPropertyDefinitionButton defButton = m_editContext.createPropertyDefinitionButton();
            FlowPanel leftButtonBox = new FlowPanel();
            String boxStyle = org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.dialogCss().leftButtonBox();
            leftButtonBox.addStyleName(boxStyle);
            leftButtonBox.getElement().getStyle().setFloat(Float.LEFT);
            for (CmsPushButton additionalButton : additionalLeftButtons) {
                leftButtonBox.add(additionalButton);
            }
            if (CmsCoreProvider.get().getUserInfo().isDeveloper()) {
                defButton.setDialog(m_dialog);
                leftButtonBox.add(defButton);
            }
            m_dialog.addButton(leftButtonBox);

            m_dialog.addCloseHandler(new CloseHandler<PopupPanel>() {

                @SuppressWarnings("synthetic-access")
                public void onClose(CloseEvent<PopupPanel> event) {

                    m_contextMenuHandler.refreshResource(CmsUUID.getNullUUID());
                }
            });

        }

        /**
         * Edits properties for current resource.<p>
         */
        public void editProperties() {

            CmsRpcAction<CmsPropertiesBean> action = new CmsRpcAction<CmsPropertiesBean>() {

                @SuppressWarnings("synthetic-access")
                @Override
                public void execute() {

                    start(0, true);
                    CmsCoreProvider.getVfsService().loadPropertyData(m_structureId, this);
                }

                @SuppressWarnings("synthetic-access")
                @Override
                protected void onResponse(CmsPropertiesBean result) {

                    stop(false);
                    updateData(result);
                }

            };
            action.execute();

        }

        /**
         * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#isSubmitting()
         */
        public boolean isSubmitting() {

            return m_submitting;
        }

        /**
         * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#onSubmitValidationResult(org.opencms.gwt.client.ui.input.form.CmsForm, boolean)
         */
        public void onSubmitValidationResult(CmsForm form, boolean ok) {

            if (ok) {
                m_submitting = true;
                if (!m_isPrevNext) {
                    m_dialog.hide();
                }
                m_isPrevNext = false;
                form.handleSubmit(m_submitHandler);
            } else {
                m_dialog.setOkButtonEnabled(form.noFieldsInvalid());
            }
        }

        /**
         * @see org.opencms.gwt.client.ui.input.form.I_CmsFormHandler#onValidationResult(org.opencms.gwt.client.ui.input.form.CmsForm, boolean)
         */
        public void onValidationResult(CmsForm form, boolean ok) {

            m_dialog.setOkButtonEnabled(ok);
        }

        /**
         * Gets the active field data.<p>
         *
         * @return the active field data
         */
        CmsActiveFieldData getActiveFieldData() {

            if (m_editor != null) {
                return m_editor.getActiveFieldData();
            } else {
                return null;
            }
        }

        /**
         * Updates the property dialog with the next resource.<p>
         *
         * @param result the data for the next resource
         */
        private void updateData(CmsPropertiesBean result) {

            final PropertyEditorHandler handler = new PropertyEditorHandler(null);
            m_handler = handler;
            handler.setEnableAdeTemplateSelect(m_enableAdeTemplateSelect);
            m_editContext.setCancelHandler(m_cancelHandler);

            handler.setPropertiesBean(result);
            handler.setEditableName(m_editName);
            final CmsVfsModePropertyEditor editor = new CmsVfsModePropertyEditor(
                result.getPropertyDefinitions(),
                handler);
            m_editor = editor;
            editor.setShowResourceProperties(!handler.isFolder());
            editor.setReadOnly(result.isReadOnly());

            m_editContext.setFormHandler(this);

            m_submitHandler = new CmsPropertySubmitHandler(handler);
            editor.getForm().setFormHandler(this);
            try {
                CmsVfsModePropertyEditor.disableResize(true);
                editor.restoreActiveFieldData(m_prevFieldData);
                editor.initializeWidgets(m_dialog);
                m_dialog.setForm(editor.getForm());
                m_dialog.centerHorizontally(50);
            } finally {
                CmsVfsModePropertyEditor.disableResize(false);
            }

        }

    }

    /**
     * Property editor handler which uses a text box for the template selection.<p>
     */
    protected static class PropertyEditorHandler extends CmsSimplePropertyEditorHandler {

        /** Enables the ADE template select box for pages. */
        private boolean m_enableAdeTemplateSelect;

        /** The stored callback. */
        private Runnable m_nextAction;

        /**
         * Creates a new instance.<p>
         *
         * @param handler the handler
         */
        public PropertyEditorHandler(I_CmsContextMenuHandler handler) {
            super(handler);
        }

        /**
         * Executes and clears the stored callback.<p>
         */
        public void runAction() {

            if (m_nextAction != null) {
                m_nextAction.run();
                m_nextAction = null;
            }
        }

        /**
         * Enables or disables the ADE template select box for pages.<p>
         *
         * @param enableAdeTemplateSelect true if ADE template select box for pages should be enabled
         */
        public void setEnableAdeTemplateSelect(boolean enableAdeTemplateSelect) {

            m_enableAdeTemplateSelect = enableAdeTemplateSelect;
        }

        /**
         * Stores an action to execute after successful submits.<p>
         *
         * @param runnable the callback
         */
        public void setNextAction(Runnable runnable) {

            m_nextAction = runnable;

        }

        /**
         * @see org.opencms.gwt.client.property.CmsSimplePropertyEditorHandler#useAdeTemplates()
         */
        @Override
        public boolean useAdeTemplates() {

            return m_enableAdeTemplateSelect;
        }

        /**
         * @see org.opencms.gwt.client.property.CmsSimplePropertyEditorHandler#onSubmitSuccess()
         */
        @Override
        protected void onSubmitSuccess() {

            super.onSubmitSuccess();
            runAction();
        }

    }

    /**
     * Property dialog subclass which keeps track of which way the dialog is exited.<p>
     */
    static class PropertiesFormDialog extends CmsFormDialog {

        /** The content panel. */
        private FlowPanel m_content = new FlowPanel();

        /** True if the dialog should be truly exited. */
        private boolean m_maybeExit;

        /**
         * Creates a new instance.<p>
         *
         * @param title the title
         * @param form the form
         */
        public PropertiesFormDialog(String title, CmsForm form) {
            super(title, form);
            setMainContent(m_content);
        }

        /**
         * Return true if OK or Cancel was clicked previously.<p>
         *
         * @return true if OK or Cancel was clicked previously
         */
        public boolean maybeExit() {

            return m_maybeExit;
        }

        /**
         * @see org.opencms.gwt.client.ui.input.form.CmsFormDialog#onClickCancel()
         */
        @Override
        public void onClickCancel() {

            m_maybeExit = true;
            super.onClickCancel();
        }

        /**
         * Sets the form.<p>
         *
         * @param form the form
         */
        public void setForm(CmsForm form) {

            m_form = form;
        }

        /**
         * @see org.opencms.gwt.client.ui.input.form.CmsFormDialog#initContent()
         */
        @Override
        protected void initContent() {

            int prevHeight = m_content.getOffsetHeight();
            if (m_content.getWidgetCount() > 0) {
                int childPrevHeight = m_content.getWidget(0).getOffsetHeight();
                if (childPrevHeight > 0) {
                    prevHeight = childPrevHeight;
                }
            }
            final String parentStyle = I_CmsLayoutBundle.INSTANCE.propertiesCss().propertyParentLoading();
            if (prevHeight > 0) {
                m_content.getElement().getStyle().setProperty("minHeight", "" + prevHeight + "px");
            }
            m_content.addStyleName(parentStyle);
            m_content.clear();
            final Widget formWidget = m_form.getWidget();
            m_content.add(formWidget);

            Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

                @SuppressWarnings("synthetic-access")
                public boolean execute() {

                    if (!formWidget.isAttached()) {
                        m_content.removeStyleName(parentStyle);
                        return false;
                    }
                    if (formWidget.getOffsetHeight() > 100) {
                        m_content.getElement().getStyle().clearProperty("minHeight");
                        m_content.removeStyleName(parentStyle);
                        return false;
                    }
                    return true;
                }
            }, 100);
        }

        /**
         * @see org.opencms.gwt.client.ui.input.form.CmsFormDialog#onClickOk()
         */
        @Override
        protected void onClickOk() {

            m_maybeExit = true;
            super.onClickOk();
        }
    }

    /**
     * Hidden utility class constructor.<p>
     */
    private CmsEditProperties() {

        // nothing to do
    }

    /**
     * Starts the property editor for the resource with the given structure id.<p>
     *
     * @param structureId the structure id of a resource
     * @param contextMenuHandler the context menu handler
     * @param editName if true, provides a field for changing the file name
     * @param cancelHandler callback which is executed if the user cancels the property dialog
     * @param enableAdeTemplateSelect enables/disables special template selector
     * @param editContext the editing context
     */
    public static void editProperties(
        final CmsUUID structureId,
        final I_CmsContextMenuHandler contextMenuHandler,
        final boolean editName,
        final Runnable cancelHandler,
        final boolean enableAdeTemplateSelect,
        final PropertyEditingContext editContext) {

        CmsRpcAction<CmsPropertiesBean> action = new CmsRpcAction<CmsPropertiesBean>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().loadPropertyData(structureId, this);
            }

            @Override
            protected void onResponse(CmsPropertiesBean result) {

                stop(false);
                openPropertyDialog(
                    result,
                    contextMenuHandler,
                    editName,
                    cancelHandler,
                    enableAdeTemplateSelect,
                    editContext);
            }

        };
        action.execute();
    }

    /**
     * Starts the property editor for the resource with the given structure id.<p>
     *
     * @param structureId the structure id of a resource
     * @param contextMenuHandler the context menu handler
     * @param editName if true, provides a field for changing the file name
     * @param cancelHandler callback which is executed if the user cancels the property dialog
     * @param enableAdeTemplateSelect enables/disables special template selector
     * @param editContext the editing context
     * @param prevFieldData the previous active field data (may be null)
     */
    public static void editPropertiesWithFileNavigation(
        final CmsUUID structureId,
        final I_CmsContextMenuHandler contextMenuHandler,
        final boolean editName,
        final Runnable cancelHandler,
        final boolean enableAdeTemplateSelect,
        final PropertyEditingContext editContext,
        final CmsActiveFieldData prevFieldData) {

        new WorkplacePropertyEditorContext(
            structureId,
            contextMenuHandler,
            editName,
            cancelHandler,
            enableAdeTemplateSelect,
            editContext,
            prevFieldData).editProperties();
    }

    /**
     * Returns the context menu command according to
     * {@link org.opencms.gwt.client.ui.contextmenu.I_CmsHasContextMenuCommand}.<p>
     *
     * @return the context menu command
     */
    public static I_CmsContextMenuCommand getContextMenuCommand() {

        return new I_CmsContextMenuCommand() {

            public void execute(CmsUUID structureId, I_CmsContextMenuHandler handler, CmsContextMenuEntryBean bean) {

                editProperties(structureId, handler, false, null, true, new PropertyEditingContext());
            }

            public A_CmsContextMenuItem getItemWidget(
                CmsUUID structureId,
                I_CmsContextMenuHandler handler,
                CmsContextMenuEntryBean bean) {

                return null;
            }

            public boolean hasItemWidget() {

                return false;
            }
        };
    }

    /**
     * Opens the property dialog and populates it with the data from a given CmsPropertiesBean.<p>
     *
     * @param result the property data
     * @param contextMenuHandler the context menu handler
     * @param editName true if the name should be editable
     * @param cancelHandler the cancel handler
     * @param enableAdeTemplateSelect true if template selection should be enabled
     * @param editContext the edit context
     */
    public static void openPropertyDialog(
        CmsPropertiesBean result,
        final I_CmsContextMenuHandler contextMenuHandler,
        final boolean editName,
        final Runnable cancelHandler,
        final boolean enableAdeTemplateSelect,
        final PropertyEditingContext editContext) {

        final PropertyEditorHandler handler = new PropertyEditorHandler(contextMenuHandler);
        handler.setPropertySaver(editContext.getPropertySaver());
        handler.setEnableAdeTemplateSelect(enableAdeTemplateSelect);
        editContext.setCancelHandler(cancelHandler);

        handler.setPropertiesBean(result);
        handler.setEditableName(editName);
        final CmsVfsModePropertyEditor editor = new CmsVfsModePropertyEditor(result.getPropertyDefinitions(), handler);

        editor.setShowResourceProperties(!handler.isFolder());
        editor.setReadOnly(result.isReadOnly());

        final CmsFormDialog dialog = new PropertiesFormDialog(handler.getDialogTitle(), editor.getForm());
        editContext.setDialog(dialog);

        if (editContext.allowCreateProperties()) {
            CmsPropertyDefinitionButton defButton = editContext.createPropertyDefinitionButton();
            defButton.installOnDialog(dialog);
            defButton.getElement().getStyle().setFloat(Float.LEFT);
        }
        final CmsDialogFormHandler formHandler = new CmsDialogFormHandler();
        editContext.setFormHandler(formHandler);
        editContext.initCloseHandler();
        formHandler.setDialog(dialog);
        I_CmsFormSubmitHandler submitHandler = new CmsPropertySubmitHandler(handler);
        formHandler.setSubmitHandler(submitHandler);
        editor.getForm().setFormHandler(formHandler);

        editor.initializeWidgets(dialog);

        dialog.centerHorizontally(50);
        if (editContext.isFocusNameField()) {
            editor.focusNameField();
        }

        dialog.catchNotifications();
    }

}
