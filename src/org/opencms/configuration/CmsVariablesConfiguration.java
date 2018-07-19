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

package org.opencms.configuration;

import org.opencms.db.CmsLoginMessage;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import org.apache.commons.digester3.Digester;

import org.dom4j.Element;

/**
 * Class to read and write the OpenCms site configuration.<p>
 */
public class CmsVariablesConfiguration extends A_CmsXmlConfiguration {

    /** The node name for the login message. */
    public static final String N_LOGINMESSAGE = "loginmessage";

    /** The node name for the login message. */
    public static final String N_BEFORELOGINMESSAGE = "beforeloginmessage";

    /** The node name for the variables element. */
    public static final String N_VARIABLES = "variables";

    /** The node name for the login message enabled flag. */
    public static final String N_ENABLED = "enabled";

    /** The node name for the login message text. */
    public static final String N_MESSAGE = "message";

    /** The node name for the login message start time. */
    public static final String N_TIMESTART = "timeStart";

    /** The node name for the login message end time. */
    public static final String N_TIMEEND = "timeEnd";

    /** The node name for the login message login forbidden flag. */
    public static final String N_LOGINFORBIDDEN = "loginForbidden";

    /** The name of the DTD for this configuration. */
    public static final String CONFIGURATION_DTD_NAME = "opencms-variables.dtd";

    /** The name of the default XML file for this configuration. */
    public static final String DEFAULT_XML_FILE_NAME = "opencms-variables.xml";

    /** The configured login message. */
    private CmsLoginMessage m_loginMessage;

    /** The configured before login message. */
    private CmsLoginMessage m_beforeLoginMessage;

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester3.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {

        // add login message creation rules
        digester.addObjectCreate("*/" + N_LOGINMESSAGE, CmsLoginMessage.class);
        digester.addBeanPropertySetter("*/" + N_LOGINMESSAGE + "/" + N_ENABLED);
        digester.addBeanPropertySetter("*/" + N_LOGINMESSAGE + "/" + N_MESSAGE);
        digester.addBeanPropertySetter("*/" + N_LOGINMESSAGE + "/" + N_LOGINFORBIDDEN);
        digester.addBeanPropertySetter("*/" + N_LOGINMESSAGE + "/" + N_TIMESTART);
        digester.addBeanPropertySetter("*/" + N_LOGINMESSAGE + "/" + N_TIMEEND);
        digester.addSetNext("*/" + N_LOGINMESSAGE, "setLoginMessage");

        // add before login message creation rules
        digester.addObjectCreate("*/" + N_BEFORELOGINMESSAGE, CmsLoginMessage.class);
        digester.addBeanPropertySetter("*/" + N_BEFORELOGINMESSAGE + "/" + N_ENABLED);
        digester.addBeanPropertySetter("*/" + N_BEFORELOGINMESSAGE + "/" + N_MESSAGE);
        digester.addSetNext("*/" + N_BEFORELOGINMESSAGE, "setBeforeLoginMessage");
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {

        // create <sites> node
        Element variablesElement = parent.addElement(N_VARIABLES);
        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            // initialized OpenCms instance is available, use latest values
            m_loginMessage = OpenCms.getLoginManager().getLoginMessage();
            m_beforeLoginMessage = OpenCms.getLoginManager().getBeforeLoginMessage();
        }
        // login message
        if (m_loginMessage != null) {
            Element messageElement = variablesElement.addElement(N_LOGINMESSAGE);
            messageElement.addElement(N_ENABLED).addText(String.valueOf(m_loginMessage.isEnabled()));
            messageElement.addElement(N_MESSAGE).addCDATA(m_loginMessage.getMessage());
            messageElement.addElement(N_LOGINFORBIDDEN).addText(String.valueOf(m_loginMessage.isLoginForbidden()));
            if (m_loginMessage.getTimeStart() != CmsLoginMessage.DEFAULT_TIME_START) {
                messageElement.addElement(N_TIMESTART).addText(String.valueOf(m_loginMessage.getTimeStart()));
            }
            if (m_loginMessage.getTimeEnd() != CmsLoginMessage.DEFAULT_TIME_END) {
                messageElement.addElement(N_TIMEEND).addText(String.valueOf(m_loginMessage.getTimeEnd()));
            }
        }
        // before login message
        if (m_beforeLoginMessage != null) {
            Element messageElement = variablesElement.addElement(N_BEFORELOGINMESSAGE);
            messageElement.addElement(N_ENABLED).addText(String.valueOf(m_beforeLoginMessage.isEnabled()));
            messageElement.addElement(N_MESSAGE).addCDATA(m_beforeLoginMessage.getMessage());
        }

        return variablesElement;
    }

    /**
     * Returns the login message.<p>
     *
     * @return before login message
     */
    public CmsLoginMessage getBeforeLoginMessage() {

        return m_beforeLoginMessage;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {

        return CONFIGURATION_DTD_NAME;
    }

    /**
     * Returns the login message.<p>
     *
     * @return the login message
     */
    public CmsLoginMessage getLoginMessage() {

        return m_loginMessage;
    }

    /**
     * Adds the before login message from the configuration.<p>
     *
     * @param message the login message to add
     */
    public void setBeforeLoginMessage(CmsLoginMessage message) {

        m_beforeLoginMessage = message;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_LOGINMESSAGE_3,
                    Boolean.valueOf(message.isEnabled()),
                    Boolean.valueOf(message.isLoginForbidden()),
                    message.getMessage()));
        }
    }

    /**
     * Adds the login message from the configuration.<p>
     *
     * @param message the login message to add
     */
    public void setLoginMessage(CmsLoginMessage message) {

        m_loginMessage = message;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_LOGINMESSAGE_3,
                    Boolean.valueOf(message.isEnabled()),
                    Boolean.valueOf(message.isLoginForbidden()),
                    message.getMessage()));
        }
    }

    /**
     * @see org.opencms.configuration.A_CmsXmlConfiguration#initMembers()
     */
    @Override
    protected void initMembers() {

        setXmlFileName(DEFAULT_XML_FILE_NAME);
    }
}
