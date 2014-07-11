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

package org.opencms.gwt.client.ui.preferences;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsDialogFormHandler;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetMultiFactory;
import org.opencms.gwt.shared.CmsUserSettingsBean;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Dialog used for changing the user settings.<p>
 */
public class CmsUserSettingsDialog extends CmsFormDialog implements I_CmsFormSubmitHandler {

    /** The action to execute after the user has changed their preferences. */
    private Runnable m_finishAction;

    /** The panel used to edit the preferences. */
    CmsUserSettingsFormFieldPanel m_panel;

    /** 
     * Creates a new widget instance.<p>
     * 
     * @param userSettings the current user settings 
     * @param finishAction the action to execute when the user has edited the user settings 
     */
    public CmsUserSettingsDialog(CmsUserSettingsBean userSettings, Runnable finishAction) {

        super("User settings", new CmsForm(false), 1000);
        m_finishAction = finishAction;
        m_panel = new CmsUserSettingsFormFieldPanel(userSettings);

        getForm().setWidget(m_panel);
        for (Map.Entry<String, CmsXmlContentProperty> entry : userSettings.getConfiguration().entrySet()) {
            String key = entry.getKey();
            CmsXmlContentProperty settingDef = entry.getValue();
            I_CmsFormWidgetMultiFactory factory = new I_CmsFormWidgetMultiFactory() {

                public I_CmsFormWidget createFormWidget(String widgetKey, Map<String, String> widgetParams) {

                    if (CmsSelectBox.WIDGET_TYPE.equals(widgetKey)) {
                        widgetKey = CmsSelectBox.WIDGET_TYPE_NOTNULL;
                    }
                    return CmsWidgetFactoryRegistry.instance().createFormWidget(widgetKey, widgetParams);
                }
            };
            I_CmsFormField field = CmsBasicFormField.createField(
                settingDef,
                settingDef.getName(),
                factory,
                Collections.<String, String> emptyMap(),
                false);
            if (userSettings.isBasic(settingDef.getName())) {
                field.getLayoutData().put("basic", "true");
            }
            getForm().addField(field, userSettings.getValue(key));
        }
        CmsDialogFormHandler handler = new CmsDialogFormHandler();
        handler.setDialog(this);
        handler.setSubmitHandler(this);
        getForm().setFormHandler(handler);
        getForm().render();
        m_panel.truncate("asdfasdfmjsdb", 1000);
        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().hideCaption());
        setMainContent(m_panel);
        setModal(true);
        setHeight(600);
        setGlassEnabled(true);
        removePadding();
    }

    /**
     * Loads the user settings dialog.<p>
     * 
     * @param finishAction  the action to execute after the user has changed his preferences 
     */
    public static void loadAndShow(final Runnable finishAction) {

        CmsRpcAction<CmsUserSettingsBean> action = new CmsRpcAction<CmsUserSettingsBean>() {

            @Override
            public void execute() {

                start(200, false);
                CmsCoreProvider.getService().loadUserSettings(this);

            }

            @Override
            protected void onResponse(CmsUserSettingsBean result) {

                stop(false);
                CmsUserSettingsDialog dlg = new CmsUserSettingsDialog(result, finishAction);
                dlg.centerHorizontally(90);
            }
        };

        action.execute();

    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler#onSubmitForm(org.opencms.gwt.client.ui.input.form.CmsForm, java.util.Map, java.util.Set)
     */
    public void onSubmitForm(CmsForm form, final Map<String, String> fieldValues, final Set<String> editedFields) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(200, false);
                CmsCoreProvider.getService().saveUserSettings(fieldValues, editedFields, this);

            }

            @SuppressWarnings("synthetic-access")
            @Override
            protected void onResponse(Void result) {

                stop(false);
                m_finishAction.run();
            }
        };
        action.execute();
    }

}
