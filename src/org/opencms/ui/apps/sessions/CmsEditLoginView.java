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

import org.opencms.configuration.CmsVariablesConfiguration;
import org.opencms.db.CmsLoginMessage;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsDateField;
import org.opencms.ui.components.CmsRichTextArea;

import java.util.Date;

import org.apache.commons.logging.Log;

import com.vaadin.data.Binder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;

/**
 * Class for the Edit Login View.<p>
 */
public class CmsEditLoginView extends CmsBasicDialog {

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsEditLoginView.class.getName());

    /**vaadin serial ok.*/
    private static final long serialVersionUID = -1053691437033852491L;

    /**vaadin component.*/
    private Button m_cancel;

    /**vaadin component.*/
    private Button m_deleteBefore;

    /**vaadin component.*/
    private Button m_deleteAfter;

    /**vaadin component.*/
    private CheckBox m_enabledAfter;

    /**vaadin component.*/
    private CheckBox m_enabledBefore;

    /**date field.*/
    private CmsDateField m_endTimeAfter;

    /** The form field binder. */
    private Binder<CmsLoginMessage> m_formBinderAfter;

    /** The form field binder. */
    private Binder<CmsLoginMessage> m_formBinderBefore;

    /**Vaadin component. */
    private TabSheet m_tab;

    /**vaadin component.*/
    private CheckBox m_logoutAfter;

    /**vaadin component.*/
    protected CmsRichTextArea m_messageAfter;

    /**vaadin component.*/
    protected CmsRichTextArea m_messageBefore;

    /**vaadin component.*/
    private Button m_ok;

    /**date field.*/
    private CmsDateField m_startTimeAfter;

    /**
     * Public constructor.<p>
     *
     * @param window to be closed
     */
    public CmsEditLoginView(final Window window) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        setHeight("100%");
        bindFields();

        CmsLoginMessage message = OpenCms.getLoginManager().getLoginMessage();
        if (message == null) {
            message = new CmsLoginMessage();
        }

        m_formBinderAfter.readBean(message);
        if (!message.isEnabled()) {
            m_endTimeAfter.setValue(null);
            m_startTimeAfter.setValue(null);
        }
        CmsLoginMessage messageBefore = OpenCms.getLoginManager().getBeforeLoginMessage();
        if (messageBefore == null) {
            messageBefore = new CmsLoginMessage();
        }
        m_formBinderBefore.readBean(messageBefore);

        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 4425001638229366505L;

            public void buttonClick(ClickEvent event) {

                window.close();
            }

        });

        m_ok.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 5512397920545155478L;

            public void buttonClick(ClickEvent event) {

                if (isFormValid()) {
                    submit();
                    window.close();
                }

            }
        });
        m_deleteBefore.addClickListener(event -> m_messageBefore.clear());
        m_deleteAfter.addClickListener(event -> m_messageAfter.clear());
    }

    /**
     * Gets end time from formular.<p>
     *
     * @return time as long
     */
    protected long getEnd() {

        if (m_endTimeAfter.getValue() == null) {
            return 0;
        }
        return m_endTimeAfter.getDate().getTime();
    }

    /**
     * Gets start time from formular.<p>
     *
     * @return time as long
     */
    protected long getStart() {

        if (m_startTimeAfter.getValue() == null) {
            return 0;
        }
        return m_startTimeAfter.getDate().getTime();
    }

    /**
     * Checks if formular is valid.<p>
     *
     * @return true if all fields are ok
     */
    protected boolean isFormValid() {

        // return m_startTimeAfter.isValid() & m_endTimeAfter.isValid() & m_messageAfter.isValid();
        return !m_formBinderAfter.validate().hasErrors();
    }

    /**
     * Saves the settings.<p>
     */
    protected void submit() {

        CmsLoginMessage loginMessage = new CmsLoginMessage();
        CmsLoginMessage beforeLoginMessage = new CmsLoginMessage();
        try {
            m_formBinderAfter.writeBean(loginMessage);
            m_formBinderBefore.writeBean(beforeLoginMessage);
            OpenCms.getLoginManager().setLoginMessage(A_CmsUI.getCmsObject(), loginMessage);
            OpenCms.getLoginManager().setBeforeLoginMessage(A_CmsUI.getCmsObject(), beforeLoginMessage);
            // update the system configuration
            OpenCms.writeConfiguration(CmsVariablesConfiguration.class);
        } catch (Exception e) {
            LOG.error("Unable to save Login Message", e);
        }
    }

    /**
     * Checks whether the entered start end end times are valid.<p>
     *
     * @return <code>true</code> in case the times are valid
     */
    boolean hasValidTimes() {

        if ((getEnd() > 0L) && (getEnd() < System.currentTimeMillis())) {
            return false;
        }

        return ((getEnd() == 0) | (getStart() == 0)) || (getEnd() >= getStart());
    }

    /**
     * Binds the form fields.<p>
     */
    private void bindFields() {

        m_formBinderAfter = new Binder<>();
        m_formBinderAfter.bind(m_messageAfter, CmsLoginMessage::getMessage, CmsLoginMessage::setMessage);

        m_formBinderAfter.bind(
            m_enabledAfter,
            loginMessage -> Boolean.valueOf(loginMessage.isEnabled()),
            (loginMessage, enabled) -> loginMessage.setEnabled(enabled.booleanValue()));

        m_formBinderAfter.bind(
            m_logoutAfter,
            loginMessage -> Boolean.valueOf(loginMessage.isLoginForbidden()),
            (loginMessage, forbidden) -> loginMessage.setLoginForbidden(forbidden.booleanValue()));

        m_formBinderAfter.forField(m_endTimeAfter).withValidator(
            endTime -> hasValidTimes(),
            CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_LOGINMESSAGE_VAL_DATE_0)).bind(
                loginMessage -> loginMessage.getTimeEnd() != CmsLoginMessage.DEFAULT_TIME_END
                ? CmsDateField.dateToLocalDateTime(new Date(loginMessage.getTimeEnd()))
                : null,
                (loginMessage, endTime) -> loginMessage.setTimeEnd(
                    endTime != null
                    ? CmsDateField.localDateTimeToDate(endTime).getTime()
                    : CmsLoginMessage.DEFAULT_TIME_END));
        m_formBinderAfter.forField(m_startTimeAfter).withValidator(
            startTime -> hasValidTimes(),
            CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_LOGINMESSAGE_VAL_DATE_0)).bind(
                loginMessage -> loginMessage.getTimeStart() != CmsLoginMessage.DEFAULT_TIME_START
                ? CmsDateField.dateToLocalDateTime(new Date(loginMessage.getTimeStart()))
                : null,
                (loginMessage, startTime) -> loginMessage.setTimeStart(
                    startTime != null
                    ? CmsDateField.localDateTimeToDate(startTime).getTime()
                    : CmsLoginMessage.DEFAULT_TIME_START));
        m_formBinderBefore = new Binder<>();
        m_formBinderBefore.bind(m_messageBefore, CmsLoginMessage::getMessage, CmsLoginMessage::setMessage);

        m_formBinderBefore.bind(
            m_enabledBefore,
            loginMessage -> Boolean.valueOf(loginMessage.isEnabled()),
            (loginMessage, enabled) -> loginMessage.setEnabled(enabled.booleanValue()));
    }
}
