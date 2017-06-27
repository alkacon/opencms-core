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

package org.opencms.ui.apps.sessions;

import org.opencms.db.CmsLoginMessage;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.sessions.CmsSessionsApp.MessageValidator;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsDateField;

import java.util.Date;

import org.apache.commons.logging.Log;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;

/**
 * Class for the Edit Login View.<p>
 */
public class CmsEditLoginView extends CmsBasicDialog {

    /**
     * Validator for date fields.<p>
     */
    class DateValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 3831671614324671946L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if ((getEnd() == 0) | (getStart() == 0)) {
                return;
            }
            if (getEnd() < getStart()) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_LOGINMESSAGE_VAL_DATE_0));
            }
        }

    }

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsEditLoginView.class.getName());

    /**vaadin serial ok.*/
    private static final long serialVersionUID = -1053691437033852491L;

    /**vaadin component.*/
    private Button m_cancel;

    /**vaadin component.*/
    private CheckBox m_enabled;

    /**date field.*/
    private CmsDateField m_endTime;

    /**vaadin component.*/
    private CheckBox m_logout;

    /**vaadin component.*/
    private TextArea m_message;

    /**vaadin component.*/
    private Button m_ok;

    /**date field.*/
    private CmsDateField m_startTime;

    /**
     * Public constructor.<p>
     *
     * @param window to be closed
     */
    public CmsEditLoginView(final Window window) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        iniUI(OpenCms.getLoginManager().getLoginMessage());

        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 4425001638229366505L;

            public void buttonClick(ClickEvent event) {

                window.close();
            }

        });

        m_ok.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 5512397920545155478L;

            public void buttonClick(ClickEvent event) {

                addValidator();
                if (isFormValid()) {
                    submit();
                    window.close();
                }

            }
        });
        m_ok.setEnabled(false);

        ValueChangeListener vChange = new ValueChangeListener() {

            private static final long serialVersionUID = -4211327550420490163L;

            public void valueChange(ValueChangeEvent event) {

                enableOk();

            }

        };
        m_enabled.addValueChangeListener(vChange);
        m_endTime.addValueChangeListener(vChange);
        m_startTime.addValueChangeListener(vChange);
        m_message.addValueChangeListener(vChange);
        m_logout.addValueChangeListener(vChange);
    }

    /**
     * Adds validators to formular.<p>
     */
    protected void addValidator() {

        m_startTime.removeAllValidators();
        m_endTime.removeAllValidators();
        m_message.removeAllValidators();

        m_startTime.addValidator(new DateValidator());
        m_endTime.addValidator(new DateValidator());

        if (m_enabled.getValue().booleanValue()) {
            m_message.addValidator(new MessageValidator());
        }
    }

    /**
     * Enables the ok button.<p>
     */
    protected void enableOk() {

        m_ok.setEnabled(true);
    }

    /**
     * Gets end time from formular.<p>
     *
     * @return time as long
     */
    protected long getEnd() {

        if (m_endTime.getValue() == null) {
            return 0;
        }
        return m_endTime.getValue().getTime();
    }

    /**
     * Gets start time from formular.<p>
     *
     * @return time as long
     */
    protected long getStart() {

        if (m_startTime.getValue() == null) {
            return 0;
        }
        return m_startTime.getValue().getTime();
    }

    /**
     * Checks if formular is valid.<p>
     *
     * @return true if all fields are ok
     */
    protected boolean isFormValid() {

        return m_startTime.isValid() & m_endTime.isValid() & m_message.isValid();
    }

    /**
     * Saves the settings.<p>
     */
    protected void submit() {

        CmsLoginMessage loginMessage = new CmsLoginMessage();
        loginMessage.setEnabled(m_enabled.getValue().booleanValue());
        loginMessage.setLoginForbidden(m_logout.getValue().booleanValue());
        loginMessage.setMessage(m_message.getValue());
        if (m_startTime.getValue() != null) {
            loginMessage.setTimeStart(m_startTime.getValue().getTime());
        }
        if (m_endTime.getValue() != null) {
            loginMessage.setTimeEnd(m_endTime.getValue().getTime());
        }
        try {
            OpenCms.getLoginManager().setLoginMessage(A_CmsUI.getCmsObject(), loginMessage);
            m_ok.setEnabled(false);
        } catch (CmsRoleViolationException e) {
            LOG.error("Unable to save Login Message", e);
        }
    }

    /**
     * Fills the form with settings from CmsLoginMessage.<p>
     *
     * @param message to be displayed
     */
    private void iniUI(CmsLoginMessage message) {

        if (message == null) {
            message = new CmsLoginMessage();
        }
        m_enabled.setValue(new Boolean(message.isActive()));
        m_logout.setValue(new Boolean(message.isLoginCurrentlyForbidden()));
        m_message.setValue(message.getMessage());
        if (message.getTimeEnd() != CmsLoginMessage.DEFAULT_TIME_END) {
            m_endTime.setValue(new Date(message.getTimeEnd()));
        }
        if (message.getTimeStart() != CmsLoginMessage.DEFAULT_TIME_START) {
            m_startTime.setValue(new Date(message.getTimeStart()));
        }
    }
}
