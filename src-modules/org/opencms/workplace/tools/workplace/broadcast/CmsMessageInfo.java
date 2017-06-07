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

package org.opencms.workplace.tools.workplace.broadcast;

import org.opencms.file.CmsObject;
import org.opencms.mail.CmsSimpleMail;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Bean class for message information.<p>
 *
 * @since 6.0.0
 */
public class CmsMessageInfo {

    /** Cc header string. */
    private String m_cc = "";

    /** From header string. */
    private String m_from = "";

    /** Message string. */
    private String m_msg = "";

    /** Subject header string. */
    private String m_subject = "";

    /** To header string. */
    private String m_to = "";

    /**
     * Default Constructor.<p>
     */
    public CmsMessageInfo() {

        // noop
    }

    /**
     * Returns the cc string.<p>
     *
     * @return the cc string
     */
    public String getCc() {

        return m_cc;
    }

    /**
     * Returns the from string.<p>
     *
     * @return the from string
     */
    public String getFrom() {

        return m_from;
    }

    /**
     * Returns the message string.<p>
     *
     * @return the message string
     */
    public String getMsg() {

        return m_msg;
    }

    /**
     * Returns the subject string.<p>
     *
     * @return the subject string
     */
    public String getSubject() {

        return m_subject;
    }

    /**
     * Returns the to string.<p>
     *
     * @return the to string
     */
    public String getTo() {

        return m_to;
    }

    /**
     * Sends the given message to the given addresses.<p>
     *
     * @param cms the cms context
     *
     * @throws Exception if something goes wrong
     */
    public void sendEmail(CmsObject cms) throws Exception {

        // create a plain text email
        CmsSimpleMail theMail = new CmsSimpleMail();
        theMail.setCharset(cms.getRequestContext().getEncoding());
        theMail.setFrom(cms.getRequestContext().getCurrentUser().getEmail(), getFrom());
        theMail.setTo(createInternetAddresses(getTo()));
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getCc())) {
            theMail.setCc(createInternetAddresses(getCc()));
        }
        theMail.setSubject("[" + OpenCms.getSystemInfo().getServerName() + "] " + getSubject());
        theMail.setMsg(getMsg());
        // send the mail
        theMail.send();
    }

    /**
     * Sets the cc string.<p>
     *
     * @param cc the cc string
     */
    public void setCc(String cc) {

        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(cc)) {
            m_cc = cc;
        }
    }

    /**
     * Sets the from string.<p>
     *
     * @param from the from string
     */
    public void setFrom(String from) {

        checkString(from);
        m_from = from;
    }

    /**
     * Sets the message string.<p>
     *
     * @param msg the message string
     */
    public void setMsg(String msg) {

        checkString(msg);
        m_msg = msg;
    }

    /**
     * Sets the subject string.<p>
     *
     * @param subject the subject string
     */
    public void setSubject(String subject) {

        checkString(subject);
        m_subject = subject;
    }

    /**
     * Sets the to string.<p>
     *
     * This has to be a ';' separated string of email-addresses.<p>
     *
     *
     * @param to the to string
     */
    public void setTo(String to) {

        m_to = to;
    }

    /**
     * Throws a runtime exception if the string is null, empty or contains JavaScript.<p>
     *
     * @param string the string to check
     */
    private void checkString(String string) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(string)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_EMPTY_STRING_0));
        }
    }

    /**
     * Creates a list of internet addresses (email) from a semicolon separated String.<p>
     *
     * @param mailAddresses a semicolon separated String with email addresses
     * @return list of internet addresses (email)
     * @throws AddressException if an email address is not correct
     */
    private List<InternetAddress> createInternetAddresses(String mailAddresses) throws AddressException {

        if (CmsStringUtil.isNotEmpty(mailAddresses)) {
            // at least one email address is present, generate list
            StringTokenizer T = new StringTokenizer(mailAddresses, ";");
            List<InternetAddress> addresses = new ArrayList<InternetAddress>(T.countTokens());
            while (T.hasMoreTokens()) {
                InternetAddress address = new InternetAddress(T.nextToken().trim());
                addresses.add(address);
            }
            return addresses;
        } else {
            // no address given, return empty list
            return Collections.emptyList();
        }
    }

}
