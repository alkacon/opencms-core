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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.util;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsMultiException;
import org.opencms.main.I_CmsThrowable;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;

import java.util.Locale;
import java.util.Properties;

/**
 * Class to display the error dialog.<p>
 *
 * @since 6.0.0
 */
public class CmsErrorBean {

    /** Name of the property file containing HTML fragments for setup wizard and error dialog.<p> */
    public static final String ERRORPAGE = "org/opencms/jsp/util/errorpage.properties";

    /** The html for the buttons. */
    private String m_buttons;

    /** The current CmsObject.<p> */
    private CmsObject m_cms;

    /** The optional error message. */
    private String m_errorMessage;

    /** The html code for the hidden parameters. */
    private String m_hiddenParams;

    /** The locale for the errorpage. */
    private Locale m_locale;

    /** Messages container. */
    private CmsMessages m_messages;

    /** The html code for the buttons. */
    private String m_paramAction;

    /** The exception that was caught.<p> */
    private Throwable m_throwable;

    /** The title for the error page. */
    private String m_title;

    /**
     * Constructs a new error bean.<p>
     *
     * @param cms the current CmsObject
     * @param throwable the exception that was caught
     */
    public CmsErrorBean(CmsObject cms, Throwable throwable) {

        m_cms = cms;
        // get the settings for system users to display errors in correct language
        CmsUser user = cms.getRequestContext().getCurrentUser();
        CmsUserSettings settings = new CmsUserSettings(user);
        m_locale = settings.getLocale();
        m_throwable = throwable;
        m_messages = Messages.get().getBundle(m_locale);
    }

    /**
     * Returns the html code for the buttons, when the errorpage is included from outside the workplace.<p>
     *
     * @return the default html for the buttons
     */
    public String getDefaultButtonsHtml() {

        StringBuffer result = new StringBuffer();
        String closeLabel = m_messages.key(Messages.GUI_CLOSE_0, new Object[] {});
        String detailsLabel = m_messages.key(Messages.GUI_DETAILS_0, new Object[] {});
        result.append("<div class=\"dialogbuttons\" unselectable=\"on\">");
        result.append("<input name=\"close\" type=\"button\" value=\"").append(closeLabel).append(
            "\" onclick=\"closeDialog();\" class=\"dialogbutton\">");
        result.append("<input name=\"details\" type=\"button\" value=\"").append(detailsLabel).append(
            "\" class=\"dialogbutton\" onclick=\"toggleElement('errordetails');\">");
        result.append("</div>");
        return result.toString();
    }

    /**
     * Returns the error message to be displayed.<p>
     *
     * @return the error message to be displayed
     */
    public String getErrorMessage() {

        StringBuffer result = new StringBuffer(512);

        String reason = m_messages.key(Messages.GUI_REASON_0, new Object[] {});

        if (CmsStringUtil.isNotEmpty(m_errorMessage)) {
            result.append(m_errorMessage);
            result.append("\n").append(reason).append(": ");
        }

        // if a localized message is already set as a parameter, append it.
        result.append(getMessage(m_throwable));
        // recursively append all error reasons to the message
        for (Throwable cause = m_throwable.getCause(); cause != null; cause = cause.getCause()) {
            result.append("\n").append(reason).append(": ");
            result.append(getMessage(cause));
        }
        return result.toString();
    }

    /**
     * Returns the localized Message, if the argument is a CmsException, or
     * the message otherwise.<p>
     *
     * @param t the Throwable to get the message from
     *
     * @return returns the localized Message, if the argument is a CmsException, or
     * the message otherwise
     */
    public String getMessage(Throwable t) {

        if ((t instanceof I_CmsThrowable) && (((I_CmsThrowable)t).getMessageContainer() != null)) {
            StringBuffer result = new StringBuffer(256);
            if (m_throwable instanceof CmsMultiException) {
                CmsMultiException exc = (CmsMultiException)m_throwable;
                String message = exc.getMessage(m_locale);
                if (CmsStringUtil.isNotEmpty(message)) {
                    result.append(message);
                    result.append('\n');
                }

            }

            I_CmsThrowable cmsThrowable = (I_CmsThrowable)t;
            result.append(cmsThrowable.getLocalizedMessage(m_locale));
            return result.toString();
        } else {
            String message = t.getMessage();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(message)) {
                // no error message found (e.g. for NPE), provide default message text
                message = m_messages.key(Messages.GUI_ERROR_UNKNOWN_0);
            }
            return message;
        }
    }

    /**
     * Sets the buttons.<p>
     *
     * @param buttons the buttons to set
     */
    public void setButtons(String buttons) {

        m_buttons = buttons;
    }

    /**
     * Sets the error message which can be displayed if no exception is there.<p>
     *
     * @param errorMessage the error message to set
     */
    public void setErrorMessage(String errorMessage) {

        m_errorMessage = errorMessage;
    }

    /**
     * Sets the hiddenParams.<p>
     *
     * @param hiddenParams the hiddenParams to set
     */
    public void setHiddenParams(String hiddenParams) {

        m_hiddenParams = hiddenParams;
    }

    /**
     * Sets the action parameter.<p>
     *
     * @param paramAction the action parameter to set
     */
    public void setParamAction(String paramAction) {

        m_paramAction = paramAction;
    }

    /**
     * Sets the title of the error page.<p>
     *
     * @param title of the error page
     */
    public void setTitle(String title) {

        m_title = title;
    }

    /**
     * Returns the html code for a errorpage.<p>
     *
     * @return the html for the errorpage
     */
    public String toHtml() {

        CmsMacroResolver resolver = new CmsMacroResolver();
        if (CmsStringUtil.isEmpty(m_title)) {
            m_title = m_messages.key(Messages.GUI_ERROR_0, new Object[] {});
        }
        resolver.addMacro("title", m_title);
        resolver.addMacro("label_error", m_messages.key(Messages.GUI_ERROR_0, new Object[] {}));
        resolver.addMacro("errorstack", CmsException.getFormattedErrorstack(m_throwable));
        resolver.addMacro("message", CmsStringUtil.escapeHtml(getErrorMessage()));
        resolver.addMacro(
            "styleuri",
            OpenCms.getLinkManager().substituteLink(m_cms, "/system/workplace/commons/style/workplace.css"));
        if (CmsStringUtil.isEmpty(m_buttons)) {
            resolver.addMacro("buttons", getDefaultButtonsHtml());
        } else {
            resolver.addMacro("buttons", m_buttons);
            resolver.addMacro("paramaction", m_paramAction);
        }

        if (CmsStringUtil.isNotEmpty(m_hiddenParams)) {
            resolver.addMacro("hiddenparams", m_hiddenParams);
        }
        resolver.addMacro(
            "erroricon",
            OpenCms.getLinkManager().substituteLink(m_cms, "/system/workplace/resources/commons/error.png"));
        Properties errorpage = new Properties();
        try {
            errorpage.load(CmsErrorBean.class.getClassLoader().getResourceAsStream(ERRORPAGE));
        } catch (Throwable th) {
            CmsLog.INIT.error(
                org.opencms.main.Messages.get().getBundle().key(
                    org.opencms.main.Messages.INIT_ERR_LOAD_HTML_PROPERTY_FILE_1,
                    ERRORPAGE),
                th);
        }
        return resolver.resolveMacros(errorpage.getProperty("ERRORPAGE"));
    }
}