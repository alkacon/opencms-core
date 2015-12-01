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
import org.opencms.gwt.client.property.CmsPropertySubmitHandler;
import org.opencms.gwt.client.property.CmsSimplePropertyEditorHandler;
import org.opencms.gwt.client.property.CmsVfsModePropertyEditor;
import org.opencms.gwt.client.property.definition.CmsPropertyDefinitionButton;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.input.form.CmsDialogFormHandler;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.util.CmsUUID;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The class for the "edit properties" context menu entries.<p>
 *
 * @since 8.0.0
 */
public final class CmsEditProperties implements I_CmsHasContextMenuCommand {

    /**
     * Helper class which encapsulates the differences between the contexts where the property edit dialog is opened.<p>
     */
    public static class PropertyEditingContext {

        /** The dialog instance. */
        protected CmsFormDialog m_formDialog;

        /** The form handler. */
        protected CmsDialogFormHandler m_formHandler;

        /** The cancel handler. */
        protected Runnable m_cancelHandler;

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

    }

    /**
     * Property editor handler which uses a text box for the template selection.<p>
     */
    protected static class PropertyEditorHandler extends CmsSimplePropertyEditorHandler {

        /** Enables the ADE template select box for pages. */
        private boolean m_enableAdeTemplateSelect;

        /**
         * Creates a new instance.<p>
         *
         * @param handler the handler
         */
        public PropertyEditorHandler(I_CmsContextMenuHandler handler) {
            super(handler);
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
         * @see org.opencms.gwt.client.property.CmsSimplePropertyEditorHandler#useAdeTemplates()
         */
        @Override
        public boolean useAdeTemplates() {

            return m_enableAdeTemplateSelect;
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
     * @param editContext the editing context
     */
    public static void editProperties(
        final CmsUUID structureId,
        final I_CmsContextMenuHandler contextMenuHandler,
        final boolean editName,
        final Runnable cancelHandler,
        final boolean enableAdeTemplateSelect,
        final PropertyEditingContext editContext) {

        CmsDebugLog.consoleLog("enableAdeTemplateSelect = " + enableAdeTemplateSelect);

        CmsRpcAction<CmsPropertiesBean> action = new CmsRpcAction<CmsPropertiesBean>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().loadPropertyData(structureId, this);
            }

            @Override
            protected void onResponse(CmsPropertiesBean result) {

                PropertyEditorHandler handler = new PropertyEditorHandler(contextMenuHandler);
                handler.setEnableAdeTemplateSelect(enableAdeTemplateSelect);
                editContext.setCancelHandler(cancelHandler);

                handler.setPropertiesBean(result);
                handler.setEditableName(editName);
                CmsVfsModePropertyEditor editor = new CmsVfsModePropertyEditor(
                    result.getPropertyDefinitions(),
                    handler);

                editor.setShowResourceProperties(!handler.isFolder());
                editor.setReadOnly(result.isReadOnly());
                stop(false);
                final CmsFormDialog dialog = new CmsFormDialog(handler.getDialogTitle(), editor.getForm());
                editContext.setDialog(dialog);

                CmsPropertyDefinitionButton defButton = editContext.createPropertyDefinitionButton();
                defButton.installOnDialog(dialog);
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

}
