/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.properties.client;

import org.opencms.ade.properties.shared.I_CmsAdePropertiesConstants;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.property.CmsPropertySubmitHandler;
import org.opencms.gwt.client.property.CmsSimplePropertyEditorHandler;
import org.opencms.gwt.client.property.CmsVfsModePropertyEditor;
import org.opencms.gwt.client.property.definition.CmsPropertyDefinitionButton;
import org.opencms.gwt.client.property.definition.CmsPropertyDefinitionDialog;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.form.CmsDialogFormHandler;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Entry point class for the standalone ADE properties dialog.<p>
 */
public class CmsPropertiesEntryPoint extends A_CmsEntryPoint {

    /**
     * Property editor handler for the standalone dialog.<p>
     */
    static class PropertyEditorHandler extends CmsSimplePropertyEditorHandler {

        /**
         * Default constructor.<p> 
         */
        public PropertyEditorHandler() {

            super(null);
        }

        /**
         * Override: Always return false, since we want to be free to choose any JSP.
         * 
         * @see org.opencms.gwt.client.property.CmsSimplePropertyEditorHandler#useAdeTemplates()
         */
        @Override
        public boolean useAdeTemplates() {

            return false;
        }
    }

    /** The link to open after editing the properties / property definition is finished. */
    protected String m_closeLink;

    /** Flag which indicates that the property definition dialog needs to be opened. */
    protected boolean m_needsPropertyDefinitionDialog;

    /**
     * @see org.opencms.gwt.client.A_CmsEntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        m_closeLink = CmsCoreProvider.getMetaElementContent(I_CmsAdePropertiesConstants.META_BACKLINK);
        String resource = CmsCoreProvider.getMetaElementContent(I_CmsAdePropertiesConstants.META_RESOURCE);
        editProperties(new CmsUUID(resource));
    }

    /**
     * Starts the property editor for the resource with the given structure id.<p>
     * 
     * @param structureId the structure id of a resource
     */
    protected void editProperties(final CmsUUID structureId) {

        CmsRpcAction<CmsPropertiesBean> action = new CmsRpcAction<CmsPropertiesBean>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().loadPropertyData(structureId, this);
            }

            @Override
            protected void onResponse(CmsPropertiesBean result) {

                I_CmsLayoutBundle.INSTANCE.propertiesCss().ensureInjected();
                CmsSimplePropertyEditorHandler handler = new PropertyEditorHandler();
                handler.setPropertiesBean(result);
                CmsVfsModePropertyEditor editor = new CmsVfsModePropertyEditor(result.getPropertyDefinitions(), handler);
                editor.setReadOnly(result.isReadOnly());
                editor.setShowResourceProperties(!handler.isFolder());
                stop(false);
                final CmsFormDialog dialog = new CmsFormDialog(handler.getDialogTitle(), editor.getForm());
                CmsPropertyDefinitionButton button = new CmsPropertyDefinitionButton() {

                    /**
                     * @see org.opencms.gwt.client.property.definition.CmsPropertyDefinitionButton#onBeforeEditPropertyDefinition()
                     */
                    @Override
                    public void onBeforeEditPropertyDefinition() {

                        m_needsPropertyDefinitionDialog = true;
                        dialog.hide();
                    }

                    @Override
                    public void onClosePropertyDefinitionDialog() {

                        closeDelayed();
                    }
                };
                button.installOnDialog(dialog);
                CmsDialogFormHandler formHandler = new CmsDialogFormHandler();
                formHandler.setDialog(dialog);
                I_CmsFormSubmitHandler submitHandler = new CmsPropertySubmitHandler(handler);
                formHandler.setSubmitHandler(submitHandler);
                editor.getForm().setFormHandler(formHandler);
                editor.initializeWidgets(dialog);
                dialog.centerHorizontally(50);
                dialog.addCloseHandler(new CloseHandler<PopupPanel>() {

                    public void onClose(CloseEvent<PopupPanel> event) {

                        onClosePropertyDialog();
                    }

                });
                dialog.catchNotifications();
            }
        };
        action.execute();
    }

    /**
     * Opens the dialog for creating new property definitions.<p>
     */
    protected void editPropertyDefinition() {

        CmsRpcAction<ArrayList<String>> action = new CmsRpcAction<ArrayList<String>>() {

            @Override
            public void execute() {

                start(200, true);
                CmsCoreProvider.getVfsService().getDefinedProperties(this);
            }

            @Override
            protected void onResponse(ArrayList<String> result) {

                stop(false);
                CmsPropertyDefinitionDialog dialog = new CmsPropertyDefinitionDialog(result);
                dialog.center();
                dialog.addCloseHandler(new CloseHandler<PopupPanel>() {

                    public void onClose(CloseEvent<PopupPanel> event) {

                        onClosePropertyDefinitionDialog();
                    }

                });
            }

        };
        action.execute();
    }

    /**
     * This method is called after the property definition dialog is closed.<p>
     */
    protected void onClosePropertyDefinitionDialog() {

        closeDelayed();
    }

    /**
     * This method is called after the property dialog is closed.<p>
     */
    protected void onClosePropertyDialog() {

        if (!m_needsPropertyDefinitionDialog) {
            closeDelayed();
        }
    }

    /**
     * Returns to the close link after a short delay.<p>
     */
    void closeDelayed() {

        Scheduler.RepeatingCommand command = new Scheduler.RepeatingCommand() {

            public boolean execute() {

                if (CmsErrorDialog.isShowingErrorDialogs()) {
                    return true;
                } else {
                    Window.Location.assign(m_closeLink);
                    return false;
                }

            }
        };
        Scheduler.get().scheduleFixedDelay(command, 300);
    }

}
