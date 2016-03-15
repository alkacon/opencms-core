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
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.form.CmsDialogFormHandler;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.CmsForm.I_FieldChangeHandler;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;

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
        protected CmsDialogFormHandler m_formHandler;

        /** The file navigation. */
        private I_MultiFileNavigation m_multiFileNavigation;

        /** The property saver. */
        private I_CmsPropertySaver m_propertySaver;

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
         * Sets the form handler.<p>
         *
         * @param formHandler the form handler
         */
        public void setFormHandler(CmsDialogFormHandler formHandler) {

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

        CmsRpcAction<CmsPropertiesBean> action = new CmsRpcAction<CmsPropertiesBean>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().loadPropertyData(structureId, this);
            }

            @Override
            protected void onResponse(CmsPropertiesBean result) {

                final PropertyEditorHandler handler = new PropertyEditorHandler(null);

                handler.setEnableAdeTemplateSelect(enableAdeTemplateSelect);
                editContext.setCancelHandler(cancelHandler);

                handler.setPropertiesBean(result);
                handler.setEditableName(editName);
                final CmsVfsModePropertyEditor editor = new CmsVfsModePropertyEditor(
                    result.getPropertyDefinitions(),
                    handler);

                editor.setShowResourceProperties(!handler.isFolder());
                editor.setReadOnly(result.isReadOnly());
                stop(false);
                final PropertiesFormDialog dialog = new PropertiesFormDialog(
                    handler.getDialogTitle(),
                    editor.getForm());
                editContext.setDialog(dialog);
                @SuppressWarnings("synthetic-access")
                final I_MultiFileNavigation fileNavigation = editContext.getMultiFileNavigation();
                final boolean[] isPrevNext = new boolean[] {false};
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

                        public void onSuccess(CmsUUID nextId) {

                            dialog.hide();
                            CmsActiveFieldData fieldData = editor.getActiveFieldData();
                            editPropertiesWithFileNavigation(
                                nextId,
                                contextMenuHandler,
                                editName,
                                cancelHandler,
                                enableAdeTemplateSelect,
                                editContext,
                                fieldData);
                        }
                    };
                    prevButton.addClickHandler(new ClickHandler() {

                        public void onClick(ClickEvent event) {

                            isPrevNext[0] = true;
                            dialog.getForm().validateAndSubmit();
                            handler.setNextAction(new Runnable() {

                                public void run() {

                                    fileNavigation.requestNextFile(-1, loadHandler);
                                }
                            });
                        }
                    });
                    nextButton.addClickHandler(new ClickHandler() {

                        public void onClick(ClickEvent event) {

                            isPrevNext[0] = true;
                            dialog.getForm().validateAndSubmit();
                            handler.setNextAction(new Runnable() {

                                public void run() {

                                    fileNavigation.requestNextFile(1, loadHandler);
                                }
                            });

                        }
                    });
                    additionalLeftButtons.add(prevButton);
                    additionalLeftButtons.add(nextButton);
                }

                CmsPropertyDefinitionButton defButton = editContext.createPropertyDefinitionButton();
                FlowPanel leftButtonBox = new FlowPanel();
                String boxStyle = org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.dialogCss().leftButtonBox();
                leftButtonBox.addStyleName(boxStyle);
                leftButtonBox.getElement().getStyle().setFloat(Float.LEFT);
                for (CmsPushButton additionalButton : additionalLeftButtons) {
                    leftButtonBox.add(additionalButton);
                }
                if (CmsCoreProvider.get().getUserInfo().isDeveloper()) {
                    defButton.setDialog(dialog);
                    leftButtonBox.add(defButton);
                }
                dialog.addButton(leftButtonBox);
                final CmsDialogFormHandler formHandler = new CmsDialogFormHandler();
                editContext.setFormHandler(formHandler);

                dialog.addCloseHandler(new CloseHandler<PopupPanel>() {

                    public void onClose(CloseEvent<PopupPanel> event) {

                        if (!isPrevNext[0]) {

                            contextMenuHandler.refreshResource(CmsUUID.getNullUUID());
                        }
                    }
                });
                formHandler.setDialog(dialog);
                I_CmsFormSubmitHandler submitHandler = new CmsPropertySubmitHandler(handler);
                formHandler.setSubmitHandler(submitHandler);
                editor.getForm().setFormHandler(formHandler);
                editor.getForm().setFieldChangeHandler(new I_FieldChangeHandler() {

                    public void onFieldChange(I_CmsFormField field, String newValue) {

                        editor.handleFieldChange(field);
                    }
                });
                editor.restoreActiveFieldData(prevFieldData);
                editor.initializeWidgets(dialog);
                dialog.centerHorizontally(50);
                dialog.catchNotifications();
            }
        };
        action.execute();
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

        PropertyEditorHandler handler = new PropertyEditorHandler(contextMenuHandler);
        handler.setPropertySaver(editContext.getPropertySaver());
        handler.setEnableAdeTemplateSelect(enableAdeTemplateSelect);
        editContext.setCancelHandler(cancelHandler);

        handler.setPropertiesBean(result);
        handler.setEditableName(editName);
        CmsVfsModePropertyEditor editor = new CmsVfsModePropertyEditor(result.getPropertyDefinitions(), handler);

        editor.setShowResourceProperties(!handler.isFolder());
        editor.setReadOnly(result.isReadOnly());

        final CmsFormDialog dialog = new PropertiesFormDialog(handler.getDialogTitle(), editor.getForm());
        editContext.setDialog(dialog);

        CmsPropertyDefinitionButton defButton = editContext.createPropertyDefinitionButton();

        defButton.installOnDialog(dialog);
        defButton.getElement().getStyle().setFloat(Float.LEFT);
        final CmsDialogFormHandler formHandler = new CmsDialogFormHandler();
        editContext.setFormHandler(formHandler);
        editContext.initCloseHandler();
        formHandler.setDialog(dialog);
        I_CmsFormSubmitHandler submitHandler = new CmsPropertySubmitHandler(handler);
        formHandler.setSubmitHandler(submitHandler);
        editor.getForm().setFormHandler(formHandler);
        editor.initializeWidgets(dialog);
        dialog.centerHorizontally(50);
        dialog.catchNotifications();
    }

}
