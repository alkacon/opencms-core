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
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.input.form.CmsDialogFormHandler;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.util.CmsUUID;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Entry point class for the standalone ADE properties dialog.<p>
 */
public class CmsPropertiesEntryPoint extends A_CmsEntryPoint {

    /**
     * Starts the property editor for the resource with the given structure id.<p>
     * 
     * @param structureId the structure id of a resource
     * @param closeLink the link which should be opened when the property dialog is closed 
     */
    protected static void editProperties(final CmsUUID structureId, final String closeLink) {

        CmsRpcAction<CmsPropertiesBean> action = new CmsRpcAction<CmsPropertiesBean>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().loadPropertyData(structureId, this);
            }

            @Override
            protected void onResponse(CmsPropertiesBean result) {

                CmsSimplePropertyEditorHandler handler = new CmsSimplePropertyEditorHandler(null);
                handler.setPropertiesBean(result);
                CmsVfsModePropertyEditor editor = new CmsVfsModePropertyEditor(result.getPropertyDefinitions(), handler);
                editor.setReadOnly(result.isReadOnly());
                editor.setShowResourceProperties(!handler.isFolder());
                stop(false);
                CmsFormDialog dialog = new CmsFormDialog(handler.getDialogTitle(), editor.getForm());
                CmsDialogFormHandler formHandler = new CmsDialogFormHandler();
                formHandler.setDialog(dialog);
                I_CmsFormSubmitHandler submitHandler = new CmsPropertySubmitHandler(handler);
                formHandler.setSubmitHandler(submitHandler);
                editor.getForm().setFormHandler(formHandler);
                editor.initializeWidgets(dialog);
                dialog.centerHorizontally(50);
                dialog.catchNotifications();
                dialog.addCloseHandler(new CloseHandler<PopupPanel>() {

                    public void onClose(CloseEvent<PopupPanel> event) {

                        Timer timer = new Timer() {

                            @Override
                            public void run() {

                                Window.Location.assign(closeLink);
                            }
                        };
                        timer.schedule(300);
                    }
                });
            }
        };
        action.execute();
    }

    /**
     * @see org.opencms.gwt.client.A_CmsEntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        String resource = CmsCoreProvider.getMetaElementContent(I_CmsAdePropertiesConstants.META_RESOURCE);
        String closeLink = CmsCoreProvider.getMetaElementContent(I_CmsAdePropertiesConstants.META_BACKLINK);
        editProperties(new CmsUUID(resource), closeLink);
    }

}
