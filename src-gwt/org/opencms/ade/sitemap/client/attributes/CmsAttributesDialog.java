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

package org.opencms.ade.sitemap.client.attributes;

import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.Messages;
import org.opencms.ade.sitemap.shared.CmsSitemapAttributeData;
import org.opencms.ade.sitemap.shared.rpc.I_CmsSitemapServiceAsync;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel;
import org.opencms.gwt.client.ui.input.form.CmsBasicFormField;
import org.opencms.gwt.client.ui.input.form.CmsDialogFormHandler;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.CmsFormDialog;
import org.opencms.gwt.client.ui.input.form.CmsFormRow;
import org.opencms.gwt.client.ui.input.form.CmsInfoBoxFormFieldPanel;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetMultiFactory;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import jsinterop.annotations.JsMethod;

/**
 * Sitemap attribute editor dialog.
 */
public class CmsAttributesDialog extends CmsFormDialog {

    /**
     * Field panel for the form fields.
     */
    public static class FieldPanel extends CmsInfoBoxFormFieldPanel {

        /**
         * Creates a new instance.
         *
         * @param info the list info bean for the sitemap configuration file
         */
        public FieldPanel(CmsListInfoBean info) {

            super(info);
        }

        /**
         * @see org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel#createRow(org.opencms.gwt.client.ui.input.I_CmsFormField)
         */
        @Override
        protected CmsFormRow createRow(I_CmsFormField field) {

            // show description on help icons instead of the title
            CmsFormRow row = createRow(
                field.getLabel(),
                A_CmsFormFieldPanel.NO_DESCRIPTION,
                (Widget)field.getWidget(),
                field.getDescription(),
                true);
            return row;
        }

    }

    /** The sitemap attribute data. */
    private CmsSitemapAttributeData m_data;

    /** The registration for the WindowClose handler registered by this dialog. */
    private HandlerRegistration m_windowCloseRegistration;

    /**
     * Creates a new instance.
     *
     * @param data the sitemap attribute data
     */
    public CmsAttributesDialog(CmsSitemapAttributeData data) {

        super(Messages.get().key(Messages.GUI_EDIT_ATTRIBUTES_0), new CmsForm(false), null);
        setAnimationEnabled(false);
        setUseAnimation(false);
        addStyleName(I_CmsLayoutBundle.INSTANCE.attributeEditorCss().attributeEditor());
        m_data = data;

        I_CmsFormSubmitHandler submitHandler = new I_CmsFormSubmitHandler() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormSubmitHandler#onSubmitForm(org.opencms.gwt.client.ui.input.form.CmsForm, java.util.Map, java.util.Set)
             */
            public void onSubmitForm(
                CmsForm formParam,
                final Map<String, String> fieldValues,
                Set<String> editedFields) {

                final I_CmsSitemapServiceAsync service = CmsSitemapView.getInstance().getController().getService();
                CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

                    @Override
                    public void execute() {

                        start(0, true);
                        CmsUUID rootId = CmsSitemapView.getInstance().getController().getData().getRoot().getId();
                        // We need to send all values to the server, not just the edited ones, because when an inherited
                        // value from a parent sitemap has changed to match the value explicitly set in a child sitemap, we want
                        // to clear the value from the child sitemap even if the user hasn't edited it just now.
                        service.saveSitemapAttributes(rootId, fieldValues, this);
                    }

                    @SuppressWarnings("synthetic-access")
                    @Override
                    protected void onResponse(Void result) {

                        stop(false);
                        sendUnlockRequest();
                    }
                };
                action.execute();
            }
        };
        CmsDialogFormHandler formHandler = new CmsDialogFormHandler();
        formHandler.setSubmitHandler(submitHandler);
        getForm().setWidget(new FieldPanel(data.getInfo()));
        getForm().setFormHandler(formHandler);
        formHandler.setDialog(this);
        for (Map.Entry<String, CmsXmlContentProperty> attrEntry : m_data.getAttributeDefinitions().entrySet()) {
            String attrName = attrEntry.getKey();
            CmsXmlContentProperty definition = attrEntry.getValue();
            CmsBasicFormField field = CmsBasicFormField.createField(
                definition,
                definition.getName(),
                new I_CmsFormWidgetMultiFactory() {

                    public I_CmsFormWidget createFormWidget(
                        String widgetName,
                        Map<String, String> widgetParams,
                        Optional<String> defaultValue) {

                        return CmsWidgetFactoryRegistry.instance().createFormWidget(
                            widgetName,
                            widgetParams,
                            defaultValue);
                    }
                },
                Collections.<String, String> emptyMap(),
                false);
            getForm().addField(field, m_data.getAttributeValues().get(attrName));
        }
        addAttachHandler(event -> handleAttach(event));
        getForm().render();
    }

    /**
     * Native method - navigator.sendBeacon().
     *
     * @param target the target URL
     * @param data the data to send to the target URL
     */
    @JsMethod(namespace = "navigator")
    private static native void sendBeacon(String target, String data);

    /**
     * @see com.google.gwt.user.client.ui.PopupPanel#setPopupPosition(int, int)
     */
    @Override
    public void setPopupPosition(int left, int top) {

        // handled by CSS
    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.CmsFormDialog#show()
     */
    @Override
    public void show() {

        super.show();
        // positioning handled by CSS
        getElement().getStyle().clearPosition();
        getElement().getStyle().clearLeft();
        getElement().getStyle().clearTop();

    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.CmsFormDialog#onClickCancel()
     */
    @Override
    protected void onClickCancel() {

        super.onClickCancel();
        sendUnlockRequest();
    }

    /**
     * Handles attach/detach events for the dialog.
     *
     * @param event the attach/detach event
     */
    private void handleAttach(AttachEvent event) {

        if (event.isAttached()) {
            m_windowCloseRegistration = Window.addWindowClosingHandler(closingEvent -> sendUnlockRequest());
        } else {
            m_windowCloseRegistration.removeHandler();
        }
    }

    /**
     * Tells the server to unlock the sitemap configuration.
     */
    private void sendUnlockRequest() {

        sendBeacon(m_data.getUnlockUrl(), "");

    }
}
