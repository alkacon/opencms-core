/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/Attic/CmsMail.java,v $
* Date   : $Date: 2003/07/31 13:19:37 $
* Version: $Revision: 1.18 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.defaults;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsUser;
import com.opencms.file.I_CmsRegistry;
import com.opencms.util.CmsByteArrayDataSource;
import com.opencms.util.Utils;

import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * This class is used to send a mail using the JavaMail package.
 * Sun's <code>mail.jar</code> and <code>activation.jar</code> are required,
 * if this class should be used in any OpenCms class.
 * <P>
 * Sender and recipients addresses may be given as <code>String</code>
 * or as <code>CmsUser</code> objects.
 * Different constructors for setting additional CC and BCC addresses are provided.
 * <P>
 * Attachments can be added using the <code>addAttachment()</code> method.
 * If the current HTTP request is a <code>multipart/form-data</code> request
 * and contains uploaded files, all these files will be sent as attachments, too.
 * <P>
 * For performance reasons, this classes uses threads for sending mails.
 * Mail threads can be initialized and started by :
 * <ul>
 * <li>creating a new CmsMail object with the appropriate constructor</li>
 * <li>eventually adding one ore more attachments to this objects</li>
 * <li>calling the <code>start()</code> method on this object.
 * </ul>
 * <P>
 * Example:<br><code>
 * String from = "waruschan.babachan@framfab.de";<br>
 * String[] to = {"alexander.lucas@framfab.de"};<br>
 * String subject = "Testmail";<br>
 * String content = "Hello World!";<br>
 * &nbsp;<br>
 * CmsMail mail=new CmsMail(cms, from, to, subject, content, "text/plain");<br>
 * mail.start();<br>
 * </code>
 *
 * @author Waruschan Babachan <waruschan.babachan@framfab.de>
 * @author mla
 * @author Alexander Lucas <alexander.lucas@framfab.de>
 *
 * @version $Name:  $ $Revision: 1.18 $ $Date: 2003/07/31 13:19:37 $
 * @since OpenCms 4.1.37. Previously, this class was part of the <code>com.opencms.workplace</code> package.
 */
public class CmsMail extends Thread {

    // constants
    private String m_from = "";
    private String[] m_to = null;
    private String[] m_bcc = null;
    private String[] m_cc = null;
    private String m_mailserver = "";
    private String m_alternativeMailserver = "";
    private String m_subject = "";
    private String m_content = "";
    private String m_type = "";
    private CmsObject m_cms = null;
    private Vector m_attachContent = new Vector();
    private Vector m_attachType = new Vector();
 
    /**
     * Constructor to create a new CmsMail instance.<p>
     * 
     * @param from sender address
     * @param to receiver address
     * @param subject mail subject
     * @param content mail content body
     * @param type mail type
     * @throws CmsException if something goes wrong
     */
    public CmsMail(String from, String[] to, String subject, String content, String type) throws CmsException {
        this(null, from, to, subject, content, type);
    }
    
    /**
     * Constructor to create a new CmsMail instance.<p>
     * 
     * @param from sender address
     * @param to receiver address
     * @param subject mail subject
     * @param content mail content body
     * @param type mail type
     * @throws CmsException if something goes wrong
     */
    public CmsMail(CmsUser from, CmsUser[] to, String subject, String content, String type) throws CmsException {
        this(null, from, to, subject, content, type);
    }
    
    /**
     * Constructor to create a new CmsMail instance.<p>
     * 
     * @param from sender address
     * @param to receiver address
     * @param subject mail subject
     * @param content mail content body
     * @param type mail type
     * @throws CmsException if something goes wrong
     */
    public CmsMail(CmsUser from, CmsGroup to, String subject, String content, String type) throws CmsException {
        this(null, from, to, subject, content, type);
    }
    
    /**
     * Constructor to create a new CmsMail instance.<p>
     * 
     * @param from sender address
     * @param to receiver address
     * @param cc copy address
     * @param bcc blind copy address
     * @param subject mail subject
     * @param content mail content body
     * @param type mail type
     * @throws CmsException if something goes wrong
     */
    public CmsMail(String from, String[] to, String[] cc, String[] bcc, String subject, String content, String type) throws com.opencms.core.CmsException {
        this(null, from, to, cc, bcc, subject, content, type);
    }
    
    /**
     * Constructor to create a new CmsMail instance.<p>
     * 
     * @param from sender address
     * @param to receiver address
     * @param bcc blind copy address
     * @param subject mail subject
     * @param content mail content body
     * @param type mail type
     * @throws CmsException if something goes wrong
     */
    public CmsMail(String from, String[] to, String[] bcc, String subject, String content, String type) throws CmsException {
        this(null, from, to, bcc, subject, content, type);
    }

    /**
     * Create a new email object with a <code>CmsUser</code> as sender and an array of
     * <code>CmsUser</code>'s as recipient(s). Email addresses will be taken from
     * the CmsUser properties.
     *
     * @param cms Cms object
     * @param from User object that contains the address of sender
     * @param to User object that contains the address of recipient
     * @param subject Subject of email
     * @param content Content of email
     * @param type ContentType of email
     * @throws CmsException if something goes wrong
     */
    public CmsMail(CmsObject cms, CmsUser from, CmsUser[] to, String subject, String content, String type) throws CmsException {

        super("OpenCms: Sending mail from " + from.getName());
        
        // Get Registry
        I_CmsRegistry reg = A_OpenCms.getRegistry();

        // check sender email address
        String fromAddress = from.getEmail();
        if (fromAddress == null || fromAddress.equals("")) {
            fromAddress = reg.getSystemValue("defaultmailsender");
        }
        if (fromAddress == null || fromAddress.equals("")) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address.", CmsException.C_BAD_NAME);
        }
        if (fromAddress.indexOf("@") == -1 || fromAddress.indexOf(".") == -1) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address: " + fromAddress, CmsException.C_BAD_NAME);
        }

        // check recipient email address
        Vector v = new Vector(to.length);
        for (int i = 0; i < to.length; i++) {
            if (to[i].getEmail() == null) {
                continue;
            }
            if (to[i].getEmail().equals("")) {
                continue;
            }
            if (to[i].getEmail().indexOf("@") == -1 || to[i].getEmail().indexOf(".") == -1) {
                throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email, Invalid recipient email address: " + to[i].getEmail(), CmsException.C_BAD_NAME);
            }
            v.addElement(to[i].getEmail());
        }
        String users[] = new String[v.size()];
        for (int i = 0; i < v.size(); i++) {
            users[i] = (String)v.elementAt(i);
        }
        if (users.length == 0) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown recipient email address.", CmsException.C_BAD_NAME);
        }
        m_to = users;
        m_from = fromAddress;
        m_subject = (subject == null ? "" : subject);
        m_content = (content == null ? "" : content);
        m_mailserver = reg.getSystemValue("smtpserver");
        m_alternativeMailserver = reg.getSystemValue("smtpserver2");
        m_type = type;
        m_cms = cms;
    }

    /**
     * Create a new email object with a <code>CmsUser</code> as sender and a
     * <code>CmsGroup</code> as recipient(s). The sender's address will be taken from
     * the CmsUser properties, the recipient's addresses from all CmsUsers
     * belonging to the given group.<p>
     *
     * @param cms Cms object
     * @param from User object that contains the address of sender
     * @param to Group object that contains the address of recipient
     * @param subject Subject of email
     * @param content Content of email
     * @param type ContentType of email
     * @throws CmsException if something goes wrong
     */
    public CmsMail(CmsObject cms, CmsUser from, CmsGroup to, String subject, String content, String type) throws CmsException {

        // Get Registry
        I_CmsRegistry reg = A_OpenCms.getRegistry();

        // check sender email address
        String fromAddress = from.getEmail();
        if (fromAddress == null || fromAddress.equals("")) {
            fromAddress = reg.getSystemValue("defaultmailsender");
        }
        if (fromAddress == null || fromAddress.equals("")) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address.", CmsException.C_BAD_NAME);
        }
        if (fromAddress.indexOf("@") == -1 || fromAddress.indexOf(".") == -1) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address: " + fromAddress, CmsException.C_BAD_NAME);
        }

        // check recipient email address
        Vector vu = cms.getUsersOfGroup(to.getName());
        Vector v = new Vector(vu.size());
        for (int i = 0; i < vu.size(); i++) {
            String address = ((CmsUser)vu.elementAt(i)).getEmail();
            if (address == null) {
                continue;
            }
            if (address.equals("")) {
                continue;
            }
            if (address.indexOf("@") == -1 || address.indexOf(".") == -1) {
                throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email, Invalid recipient email address: " + address, CmsException.C_BAD_NAME);
            }
            v.addElement(address);
        }
        String users[] = new String[v.size()];
        for (int i = 0; i < v.size(); i++) {
            users[i] = (String)v.elementAt(i);
        }
        if (users.length == 0) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown recipient email address.", CmsException.C_BAD_NAME);
        }
        m_to = users;
        m_from = fromAddress;
        m_subject = (subject == null ? "" : subject);
        m_content = (content == null ? "" : content);
        m_mailserver = reg.getSystemValue("smtpserver");
        m_alternativeMailserver = reg.getSystemValue("smtpserver2");
        m_type = type;
        m_cms = cms;
    }

    /**
     * Create a new email object with given FROM, TO, CC and BCC addresses.<p>
     *
     * @see #CmsMail(CmsObject,String, String[], String[], String, String, String)
     *
     * @param cms Cms object
     * @param from Address of sender
     * @param to Address of recipient
     * @param cc Address of copy recipient
     * @param bcc Address of blank copy recipient
     * @param subject Subject of email
     * @param content Content of email
     * @param type ContentType of email
     * @throws CmsException if something goes wrong
     */
    public CmsMail(CmsObject cms, String from, String[] to, String[] cc, String[] bcc, String subject, String content, String type) throws CmsException {
        this(cms, from, to, bcc, subject, content, type);
        Vector v = new Vector();
        for (int i = 0; i < cc.length; i++) {
            if (cc[i] == null) {
                continue;
            }
            if (cc[i].equals("")) {
                continue;
            }
            if (cc[i].indexOf("@") == -1 || cc[i].indexOf(".") == -1) {
                continue;
            }
            v.addElement(cc[i]);
        }
        String users[] = new String[v.size()];
        for (int i = 0; i < v.size(); i++) {
            users[i] = (String)v.elementAt(i);
        }
        if (users.length == 0) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown recipient email address.", CmsException.C_BAD_NAME);
        }
        m_cc = users;
    }

    /**
     * Create a new email object with given FROM, TO and BCC addresses.<p>
     *
     * @see #CmsMail(CmsObject,String, String[], String, String, String)
     *
     * @param cms Cms object
     * @param from Address of sender
     * @param to Address of recipient
     * @param bcc Address of blank copy recipient
     * @param subject Subject of email
     * @param content Content of email
     * @param type ContentType of email
     * @throws CmsException if something goes wrong
     */
    public CmsMail(CmsObject cms, String from, String[] to, String[] bcc, String subject, String content, String type) throws CmsException {
        this(cms, from, to, subject, content, type);
        Vector v = new Vector();
        for (int i = 0; i < bcc.length; i++) {
            if (bcc[i] == null) {
                continue;
            }
            if (bcc[i].equals("")) {
                continue;
            }
            if (bcc[i].indexOf("@") == -1 || bcc[i].indexOf(".") == -1) {
                continue;
            }
            v.addElement(bcc[i]);
        }
        String users[] = new String[v.size()];
        for (int i = 0; i < v.size(); i++) {
            users[i] = (String)v.elementAt(i);
        }
        if (users.length == 0) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown recipient email address.", CmsException.C_BAD_NAME);
        }
        m_bcc = users;
    }

    /**
     * Create a new email object with given FROM, TO and BCC addresses.<p>
     *
     * @see #CmsMail(CmsObject,String, String[], String, String, String)
     *
     * @param cms Cms object
     * @param from Address of sender
     * @param to Address of recipient
     * @param cc Address of copy recipient
     * @param isBcc dedined wheather the type of cc is a bcc or not
     * @param subject Subject of email
     * @param content Content of email
     * @param type ContentType of email
     * @throws CmsException if something goes wrong
     */
    public CmsMail(CmsObject cms, String from, String[] to, String[] cc, boolean isBcc, String subject, String content, String type) throws CmsException {
        this(cms, from, to, subject, content, type);
        Vector v = new Vector();
        for (int i = 0; i < cc.length; i++) {
            if (cc[i] == null) {
                continue;
            }
            if (cc[i].equals("")) {
                continue;
            }
            if (cc[i].indexOf("@") == -1 || cc[i].indexOf(".") == -1) {
                continue;
            }
            v.addElement(cc[i]);
        }
        String users[] = new String[v.size()];
        for (int i = 0; i < v.size(); i++) {
            users[i] = (String)v.elementAt(i);
        }
        if (users.length == 0) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown recipient email address.", CmsException.C_BAD_NAME);
        }
        if (isBcc)
            m_bcc = users;
        else
            m_cc=users;
    }

    /**
     * Create a new email object with given FROM and TO addresses.
     *
     * @param cms Cms object
     * @param from Address of sender
     * @param to Array with address(es) of recipient(s)
     * @param subject Subject of email
     * @param content Content of email
     * @param type ContentType of email
     * @throws CmsException if something goes wrong
     */
    public CmsMail(CmsObject cms, String from, String[] to, String subject, String content, String type) throws CmsException {

        // check sender email address
        if (from == null) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address.", CmsException.C_BAD_NAME);
        }
        if (from.equals("")) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address.", CmsException.C_BAD_NAME);
        }
        if (from.indexOf("@") == -1 || from.indexOf(".") == -1) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address: " + from, CmsException.C_BAD_NAME);
        }

        // check recipient email address
        Vector v = new Vector(to.length);
        for (int i = 0; i < to.length; i++) {
            if (to[i] == null) {
                continue;
            }
            if (to[i].equals("")) {
                continue;
            }
            if (to[i].indexOf("@") == -1 || to[i].indexOf(".") == -1) {
                throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email, Invalid recipient email address: " + to[i], CmsException.C_BAD_NAME);
            }
            v.addElement(to[i]);
        }
        String users[] = new String[v.size()];
        for (int i = 0; i < v.size(); i++) {
            users[i] = (String)v.elementAt(i);
        }
        if (users.length == 0) {
            throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown recipient email address.", CmsException.C_BAD_NAME);
        }
        m_from = from;
        m_to = users;
        m_subject = (subject == null ? "" : subject);
        m_content = (content == null ? "" : content);
        I_CmsRegistry reg = A_OpenCms.getRegistry();
        m_mailserver = reg.getSystemValue("smtpserver");
        m_alternativeMailserver = reg.getSystemValue("smtpserver2");
        m_type = type;
        m_cms = cms;
    }

    /**
     * Add a new attachment of the given content type to this <code>CmsMail</code>
     * object. The attachment will get a random name.
     *
     * @param content Content of the attachment.
     * @param type Content type of the attachment.
     */
    public void addAttachment(String content, String type) {
        m_attachContent.addElement(content);
        m_attachType.addElement(type);
    }

    /**
     * Internal method for building a new <code>MimeMessage</code> object
     * with the given content, attachments, SMTP host properties, FROM, TO,
     * CC and BCC addresses.<p>
     * 
     * Will be called from the thread connecting to the SMTP server
     * and sending the mail
     * @param smtpHost Name of the SMTP host that should be connected
     * @return <code>Message</code> object that can be used as argument for the <code>Transport</code> class
     * @throws Exception no exceptions occuring while building the mail will be caught
     */
    private Message buildMessage(String smtpHost) throws Exception {

        // Default encoding for new mail message
        String mail_encoding = "ISO-8859-1";
        
        // First check the smtpHost parameter
        if (smtpHost == null || "".equals(smtpHost)) {
            throw new CmsException("No SMTP server given.");
        }

        // create some properties and get the default Session
        Properties props = System.getProperties();
        props.put("mail.smtp.host", smtpHost);
        Session session = Session.getDefaultInstance(props, null);

        // Build a new message object
        MimeMessage msg = new MimeMessage(session);

        // Check and set all addresses.
        InternetAddress[] to = new InternetAddress[m_to.length];
        for (int i = 0; i < m_to.length; i++) {
            to[i] = new InternetAddress(m_to[i]);
        }
        msg.setFrom(new InternetAddress(m_from));
        msg.setRecipients(Message.RecipientType.TO, to);
        InternetAddress[] cc = null;
        if (m_cc != null) {
            cc = new InternetAddress[m_cc.length];
            for (int i = 0; i < m_cc.length; i++) {
                cc[i] = new InternetAddress(m_cc[i]);
            }
            msg.setRecipients(Message.RecipientType.CC, cc);
        }
        InternetAddress[] bcc = null;
        if (m_bcc != null) {
            bcc = new InternetAddress[m_bcc.length];
            for (int i = 0; i < m_bcc.length; i++) {
                bcc[i] = new InternetAddress(m_bcc[i]);
            }
            msg.setRecipients(Message.RecipientType.BCC, bcc);
        }

        // Set subject
        msg.setSubject(m_subject, mail_encoding);

        // Set content and attachments
        Vector v = new Vector();
        if (m_cms != null) {
            Enumeration enum = m_cms.getRequestContext().getRequest().getFileNames();
            while (enum.hasMoreElements()) {
                v.addElement(enum.nextElement());
            }
        }
        int size = v.size();
        int numAttach = m_attachContent.size();
        if (size != 0 || numAttach != 0) {

            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();
            Multipart mp = new MimeMultipart();
            if (m_type.equals("text/html")) {
                mbp1.setDataHandler(new DataHandler(new CmsByteArrayDataSource(m_content, m_type, mail_encoding)));
            } else {
                mbp1.setText(m_content, mail_encoding);
            }
            mp.addBodyPart(mbp1);

            // Check, if there are any attachments
            for (int i = 0; i < numAttach; i++) {

                // create another message part
                // attach the file to the message
                MimeBodyPart mbpAttach = new MimeBodyPart();
                if ("text/html".equals(m_attachType.elementAt(i))) {
                    mbpAttach.setDataHandler(new DataHandler(new CmsByteArrayDataSource((String)m_attachContent.elementAt(i), "text/html", mail_encoding)));
                } else {
                    mbpAttach.setText((String)m_attachContent.elementAt(i), mail_encoding);
                }
                mp.addBodyPart(mbpAttach);
            }
            for (int i = 0; i < size; i++) {
                String filename = (String)v.elementAt(i);
                if (!"unknown".equalsIgnoreCase(filename)) {
                    MimetypesFileTypeMap mimeTypeMap = new MimetypesFileTypeMap();
                    String mimeType = mimeTypeMap.getContentType(filename);
                    MimeBodyPart mbp = new MimeBodyPart();
                    mbp.setDataHandler(new DataHandler(new CmsByteArrayDataSource(m_cms.getRequestContext().getRequest().getFile(filename), mimeType)));
                    mbp.setFileName(filename);
                    mp.addBodyPart(mbp);
                }
            }
            msg.setContent(mp);
        } else {
            if (m_type.equals("text/html")) {
                msg.setDataHandler(new DataHandler(new CmsByteArrayDataSource(m_content, m_type, mail_encoding)));
            } else {
                msg.setContent(m_content, m_type);
            }
        }
        msg.setSentDate(new Date());
        return msg;
    }

    /**
     * Helper method for printing nice classnames in error messages
     * @return class name in [ClassName] format
     */
    protected String getClassName() {
        String name = getClass().getName();
        return "[" + name.substring(name.lastIndexOf(".") + 1) + "] ";
    }

    /**
     * Try sending the mail.
     * This can take a few seconds to several minutes. We don't want
     * the user to wait for the response of the current HTTP request.
     * Therefore we are running this in a new thread. The user will
     * get his response immediately, then.
     */
    public void run() {

        // Build the message
        Message msg = null;
        try {
            msg = buildMessage(m_mailserver);
        } catch (Exception e) {
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, getClassName() + "Error while building Email object: " + Utils.getStackTrace(e));
            }

            // Do not continue here. We don't have a Message object we can send.
            return;
        }

        // Now the message is ready.
        // Try to send it...
        try {
            Transport.send(msg);
        } catch (Exception e) {

            // Emergency! An error occured while connecting to the SMTP server
            // We cannot inform the user at this point since this code runs
            // in a thread and the initiating request is completed for a long time.
            // Get nested Exception used for pretty printed error message in logfile
            for (; e instanceof MessagingException; e = ((MessagingException)e).getNextException()) {
            }

            // First print out an error message...
            if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, getClassName() + "Error while transmitting mail to SMTP server: " + e);
            }

            // ... and now try an alternative server (if given)
            if (m_alternativeMailserver != null && !"".equals(m_alternativeMailserver)) {
                if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, getClassName() + "Trying alternative server...");
                }
                try {
                    msg = buildMessage(m_alternativeMailserver);
                    Transport.send(msg);
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, getClassName() + "...OK. Mail sent.");
                    }
                } catch (Exception e2) {

                    // Get nested Exception used for pretty printed error message in logfile
                    for (; e2 instanceof MessagingException; e2 = ((MessagingException)e2).getNextException()) {
                    }
                    if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                        A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, getClassName() + "PANIC! Could not send Email. Even alternative server failed! " + e2);
                    }
                }
            } else {
                if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, getClassName() + "PANIC! No alternative SMTP server given! Could not send Email!");
                }
            }
        }
    }
}
