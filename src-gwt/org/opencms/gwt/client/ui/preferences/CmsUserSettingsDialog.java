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

package org.opencms.gwt.client.ui.preferences;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsScrollPanel;
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
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsDomUtil.Style;
import org.opencms.gwt.shared.CmsUserSettingsBean;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Element;

/**
 * Dialog used for changing the user settings.<p>
 */
public class CmsUserSettingsDialog extends CmsFormDialog implements I_CmsFormSubmitHandler {

    /** The action to execute after the user has changed their preferences. */
    Runnable m_finishAction;

    /** The panel used to edit the preferences. */
    CmsUserSettingsFormFieldPanel m_panel;

    /** The old tab index. */
    private int m_oldTabIndex;

    /**
     * Creates a new widget instance.<p>
     *
     * @param userSettings the current user settings
     * @param finishAction the action to execute when the user has edited the user settings
     */
    public CmsUserSettingsDialog(CmsUserSettingsBean userSettings, Runnable finishAction) {

        super("User settings", new CmsForm(false), -1);
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
            String initialValue = userSettings.getValue(key);
            if (initialValue == null) {
                initialValue = settingDef.getDefault();
            }

            getForm().addField(field, initialValue);
        }
        CmsDialogFormHandler handler = new CmsDialogFormHandler();
        handler.setDialog(this);
        handler.setSubmitHandler(this);
        getForm().setFormHandler(handler);
        getForm().render();
        getElement().addClassName(I_CmsLayoutBundle.INSTANCE.dialogCss().hideCaption());
        setMainContent(m_panel);
        setModal(true);
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
                dlg.centerHorizontally(50);
                dlg.initWidth();
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

            @Override
            protected void onResponse(Void result) {

                stop(false);
                if (m_finishAction != null) {
                    m_finishAction.run();
                }
            }
        };
        action.execute();
    }

    /**
     * Initializes the width of the dialog contents.<p>
     */
    protected void initWidth() {

        m_panel.truncate("user_settings", getWidth() - 12);

    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    protected void onLoad() {

        super.onLoad();
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

            public boolean execute() {

                if (!CmsUserSettingsDialog.this.isAttached() || !CmsUserSettingsDialog.this.isVisible()) {
                    return false;
                }
                updateHeight();
                return true;
            }

        }, 200);
    }

    /**
     * Updates the panel height depending on the content of the current tab.<p>
     */
    protected void updateHeight() {

        CmsPopup dialog = this;
        int tabIndex = m_panel.getTabPanel().getSelectedIndex();
        boolean changedTab = tabIndex != m_oldTabIndex;
        m_oldTabIndex = tabIndex;
        CmsScrollPanel tabWidget = m_panel.getTabPanel().getWidget(tabIndex);
        Element innerElement = tabWidget.getWidget().getElement();
        int contentHeight = CmsDomUtil.getCurrentStyleInt(innerElement, Style.height);
        int spaceLeft = dialog.getAvailableHeight(0);
        int newHeight = Math.min(spaceLeft, contentHeight + 47);
        boolean changedHeight = m_panel.getTabPanel().getOffsetHeight() != newHeight;
        if (changedHeight || changedTab) {
            m_panel.getTabPanel().setHeight(newHeight + "px");
            int selectedIndex = m_panel.getTabPanel().getSelectedIndex();
            CmsScrollPanel widget = m_panel.getTabPanel().getWidget(selectedIndex);
            widget.setHeight((newHeight - 34) + "px");
            widget.onResizeDescendant();
        }
    }
}
