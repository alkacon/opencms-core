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

package org.opencms.mail;

import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import javax.mail.Address;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.EmailException;

import com.dumbster.smtp.SimpleSmtpServer;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the cms mail functionality.<p>
 */
public class TestCmsMail extends OpenCmsTestCase {

    /**
     * Port the SMTP server will listen to. Must be greater than 1000, depending
     * on the underlying OS.
     */
    private static final int SMTP_PORT = 2525;

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsMail(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsMail.class.getName());

        suite.addTest(new TestCmsMail("testCmsSendSimpleMail"));
        suite.addTest(new TestCmsMail("testCmsSendHtmlMail"));
        suite.addTest(new TestCmsMail("testCmsInvalidMailAddress"));
        TestSetup wrapper = new TestSetup(suite) {

            // SMTP Server running locally (c.f. library dumbster-1.6.jar)
            SimpleSmtpServer m_server;

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
                // start SMTP server
                m_server = SimpleSmtpServer.start(SMTP_PORT);

            }

            @Override
            protected void tearDown() {

                removeOpenCms();
                // stop SMTP server
                m_server.stop();
            }
        };

        return wrapper;
    }

    /**
     * Tests sending mails to invalid email address.<p>
     */
    public void testCmsInvalidMailAddress() {

        echo("Trying to send an HTML mail to invalid mail address ...");

        String invalidMail = "abc@blockmail.de";
        CmsHtmlMail mail = new CmsHtmlMail();
        StringBuilder sb = new StringBuilder("<html><body>");
        sb.append("<h1>Test mail containing HTML</h1>");
        sb.append("<p>This is only a test mail for sending HTML mails.</p>");
        sb.append(
            "<p><a href=\"http://www.opencms.org/\"><img src=\"http://www.opencms.org/export/system/modules/org.opencms.website.template/resources/img/logo/logo_opencms.gif\" border=\"0\"></a></p>");
        sb.append("<p><a href=\"http://www.opencms.org/\">www.opencms.org</a>");
        sb.append("</body></html>");

        // EmailException will be caught here to test functionality required by
        // CmsMessageInfo.java
        try {
            mail.setHtmlMsg(sb.toString());
            mail.addTo(invalidMail);
            mail.setSubject("OpenCms TestCase HTML Mail");
            mail.setSmtpPort(SMTP_PORT);
            mail.send();
        } catch (EmailException e) {
            // Check if root cause was SendFailedException due to rejected mail by SMTP server
            assertTrue(e.getCause() instanceof SendFailedException);
            SendFailedException sfe = (SendFailedException)e.getCause();
            Address[] invalidAddresses = sfe.getInvalidAddresses();
            InternetAddress invalidAddress = (InternetAddress)invalidAddresses[0];
            echo("Invalid address was: " + invalidAddress.getAddress());
            assertEquals(invalidMail, invalidAddress.getAddress());
        }
    }

    /**
     * Tests sending plain text mails.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCmsSendHtmlMail() throws Throwable {

        echo("Trying to send an HTML mail ...");

        CmsHtmlMail mail = new CmsHtmlMail();
        StringBuilder sb = new StringBuilder("<html><body>");
        sb.append("<h1>Test mail containing HTML</h1>");
        sb.append("<p>This is only a test mail for sending HTML mails.</p>");
        sb.append(
            "<p><a href=\"http://www.opencms.org/\"><img src=\"http://www.opencms.org/export/system/modules/org.opencms.website.template/resources/img/logo/logo_opencms.gif\" border=\"0\"></a></p>");
        sb.append("<p><a href=\"http://www.opencms.org/\">www.opencms.org</a>");
        sb.append("</body></html>");
        mail.setHtmlMsg(sb.toString());
        mail.addTo(OpenCms.getSystemInfo().getMailSettings().getMailFromDefault());
        mail.setSubject("OpenCms TestCase HTML Mail");
        // SMTP port must be set manually
        mail.setSmtpPort(SMTP_PORT);
        String messageID = mail.send();
        assertNotNull(messageID);

        echo("HTML mail was sent successfully.");
    }

    /**
     * Tests sending plain text mails.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCmsSendSimpleMail() throws Throwable {

        echo("Trying to send a plain text mail ...");

        CmsSimpleMail mail = new CmsSimpleMail();
        mail.setMsg("This is only a test mail for sending plain text mails.");
        mail.addTo(OpenCms.getSystemInfo().getMailSettings().getMailFromDefault());
        mail.setSubject("OpenCms TestCase Plain Text Mail");
        mail.setSmtpPort(SMTP_PORT);
        String messageID = mail.send();
        assertNotNull(messageID);

        echo("Plain text mail was sent successfully.");
    }
}
