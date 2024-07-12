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

package org.opencms.notification;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsMessages;
import org.opencms.mail.CmsHtmlMail;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsHtml2TextConverter;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.EmailException;

import org.htmlparser.util.ParserException;

/**
 * Abstract class to create a notfication which will be send as a html mail to
 * a user in OpenCms.
 *
 * @since 6.5.3
 */
public abstract class A_CmsNotification extends CmsHtmlMail {

    /** ID of the system mail host. */
    public static final String SYSTEM_MAIL_HOST = "system";

    /** Path to optional config file containing header and footer. */
    public static final String HEADER_FOOTER_CONFIG_PATH = "notification-header-footer.html";

    /** Separator between header and footer in optional config file. */
    public static final String HEADER_FOOTER_SEPARATOR = Pattern.quote("$BODY");

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsNotification.class);

    /** The configured header. */
    protected String m_configuredHeader;

    /** The configured footer. */
    protected String m_configuredFooter;

    /** The xml-content to read subject, header and footer of the notification. */
    protected CmsXmlContent m_mailContent;

    /** The CmsObject. */
    protected CmsObject m_cms;

    /** The locale of the receiver of the content notification. */
    protected Locale m_locale;

    /** The macro resolver used. */
    protected CmsNotificationMacroResolver m_macroResolver;

    /** The receiver of the notification. */
    private CmsUser m_receiver;

    /**
     * Creates a new A_CmsNotification.<p>
     *
     * @param cms the cms object to use
     * @param receiver the receiver of the notification
     */
    public A_CmsNotification(CmsObject cms, CmsUser receiver) {

        super(SYSTEM_MAIL_HOST);
        m_cms = cms;
        m_receiver = receiver;
        m_macroResolver = new CmsNotificationMacroResolver(cms, receiver);
        m_macroResolver.setCmsObject(cms);
    }

    /**
     * Adds a new macro to the used macro resolver. Macros are used for the xml
     * content file.
     *
     * @param key The key of the macro.
     * @param value The value of the macro.
     */
    public void addMacro(String key, String value) {

        m_macroResolver.addMacro(key, value);
    }

    /**
     * Returns the CmsObject.<p>
     *
     * @return the CmsObject
     */
    public CmsObject getCmsObject() {

        return m_cms;
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the receiver.<p>
     *
     * @return the receiver
     */
    public CmsUser getReceiver() {

        return m_receiver;
    }

    /**
     * @see org.apache.commons.mail.Email#send()
     */
    @Override
    public String send() throws EmailException {

        String messageID = null;
        try {
            // check if user is valid and has a mail address specified
            if (CmsStringUtil.isEmpty(m_receiver.getEmail())) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_NOTIFICATION_NO_ADDRESS_1, m_receiver.getName()));
                return null;
            }

            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().getBundle().key(Messages.LOG_NOTIFICATION_SEND_1, m_receiver.getEmail()));
            }

            // read resource with subject, header and footer
            m_mailContent = CmsXmlContentFactory.unmarshal(m_cms, m_cms.readFile(getNotificationContent()));

            // detect locale
            List<Locale> locales = m_mailContent.getLocales();
            Locale userLocale = new CmsUserSettings(m_receiver).getLocale();
            if (locales.contains(userLocale)) {
                // mail is localized in the user locale, use that
                m_locale = userLocale;
            } else if (locales.contains(OpenCms.getWorkplaceManager().getDefaultLocale())) {
                // mail is localized in the system default locale, use that
                m_locale = OpenCms.getWorkplaceManager().getDefaultLocale();
            } else {
                // use any localization
                m_locale = locales.get(0);
            }

            String mailCharset = Messages.get().getBundle(m_locale).key(Messages.GUI_MAIL_CHARSET_0);
            if (!CmsMessages.isUnknownKey(mailCharset)) {
                setCharset(mailCharset);
            }

            // define macro resolver
            //TODO Remove when old notifications were adjusted
            m_macroResolver.addMacro("firstname", m_receiver.getFirstname());
            m_macroResolver.addMacro("lastname", m_receiver.getLastname());
            m_macroResolver.addMacro("project", m_cms.getRequestContext().getCurrentProject().getName());
            try {
                CmsResource configRes = m_cms.readResource(
                    OpenCms.getSystemInfo().getConfigFilePath(m_cms, HEADER_FOOTER_CONFIG_PATH));
                CmsFile configFile = m_cms.readFile(configRes);
                String configContent = new String(configFile.getContents(), "UTF-8");
                String[] configParts = configContent.split(HEADER_FOOTER_SEPARATOR);
                if (configParts.length == 2) {
                    m_configuredHeader = configParts[0];
                    m_configuredFooter = configParts[1];
                } else {
                    LOG.error("Invalid notification header/footer configuration: " + HEADER_FOOTER_CONFIG_PATH);
                }
            } catch (CmsVfsResourceNotFoundException e) {
                LOG.debug(e.getLocalizedMessage(), e);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }

            StringBuffer msg = new StringBuffer();

            // append html header
            appendHtmlHeader(msg);
            appendXMLContent(msg);

            // append html footer
            appenHtmlFooter(msg);

            addTo(m_receiver.getEmail(), m_receiver.getFirstname() + ' ' + m_receiver.getLastname());
            setSubject(
                CmsMacroResolver.resolveMacros(
                    m_mailContent.getStringValue(m_cms, "Subject", m_locale),
                    m_macroResolver));
            setHtmlMsg(msg.toString());
            try {
                String textMsg = CmsHtml2TextConverter.html2text(msg.toString(), mailCharset);
                setTextMsg(textMsg);
            } catch (ParserException e) {
                LOG.error("Failed to process the text version of a HTML message.", e);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }

            // send mail
            super.send();

            // get MIME message ID
            messageID = getMimeMessage().getMessageID();

        } catch (CmsException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_NOTIFICATION_SEND_ERROR_0), e);
        } catch (MessagingException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_NOTIFICATION_SEND_ERROR_0), e);
        }
        return messageID;
    }

    /**
     * Append the html-code to start a html mail message to the given buffer.<p>
     *
     * @param buffer The StringBuffer to add the html code to.
     */
    protected void appendHtmlHeader(StringBuffer buffer) {

        if (m_configuredHeader != null) {
            buffer.append(m_configuredHeader);
        } else {
            buffer.append("<html>\r\n");
            buffer.append("  <head>\r\n");
            buffer.append("    <style type=\"text/css\">\r\n");
            buffer.append(
                "      body { font-family: Verdana, Arial, Helvetica, sans-serif; background-color:white; }\r\n");
            buffer.append("      a { color:#b31b43; text-decoration:none; font-weight: bold; }\r\n");
            buffer.append("      a:hover { color:#b31b43; text-decoration:underline; font-weight: bold; }\r\n");
            buffer.append("      div.publish_link { margin: 20px 0; }\r\n");
            buffer.append("      table { white-space: nowrap; font-size: small; }\r\n");
            buffer.append("      tr.trow1 { background-color: #cdc0b0; }\r\n");
            buffer.append("      tr.trow2 { background-color: #eedfcc; }\r\n");
            buffer.append("      tr.trow3 { background-color: #ffefdb; }\r\n");
            buffer.append(
                "      th.rescol { border-width: 1px 0 2px 1px; border-style: solid; border-color: #222222; padding: 5px; }\r\n");
            buffer.append(
                "      th.titlecol { border-width: 1px 1px 2px 1px; border-style: solid; border-color: #222222; padding: 5px; }\r\n");
            buffer.append(
                "      td.rescol { border-width: 0 0 1px 1px; border-style: solid; border-color: #222222; padding: 5px; }\r\n");
            buffer.append(
                "      td.titlecol { border-width: 0 1px 1px 1px; border-style: solid; border-color: #222222; padding: 5px; }\r\n");
            buffer.append("    </style>\r\n");
            buffer.append("  </head>\r\n");
            buffer.append("  <body>\r\n");
        }
    }

    /**
     * Append XMLContent to StringBuffer.<p>
     *
     * @param msg StringBuffer
     */
    protected void appendXMLContent(StringBuffer msg) {

        // append header from xmlcontent
        msg.append(
            CmsMacroResolver.resolveMacros(m_mailContent.getStringValue(m_cms, "Header", m_locale), m_macroResolver));

        // append body
        msg.append("\n<br/><br/>\n");
        msg.append(generateHtmlMsg());
        msg.append("\n<br/><br/>\n");

        // append footer from xmlcontent
        msg.append(
            CmsMacroResolver.resolveMacros(m_mailContent.getStringValue(m_cms, "Footer", m_locale), m_macroResolver));
    }

    /**
     * Append the html-code to finish a html mail message to the given buffer.
     *
     * @param buffer The StringBuffer to add the html code to.
     */
    protected void appenHtmlFooter(StringBuffer buffer) {

        if (m_configuredFooter != null) {
            buffer.append(m_configuredFooter);
        } else {
            buffer.append("  </body>\r\n" + "</html>");
        }
    }

    /**
     * Overwrite the method to generate the message body of the notification. This
     * text is placed between the header and the footer of the defined xmlcontent
     * and the required html code is added.<p>
     *
     * @return The text to be inserted in the notification.
     */
    protected abstract String generateHtmlMsg();

    /**
     * Overwrite the method to return the path to the xmlcontent, where the subject,
     * the header and the footer are defined.<p>
     *
     * @return The path to the xmlcontent file.
     */
    protected abstract String getNotificationContent();
}
