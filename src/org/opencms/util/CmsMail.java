/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/CmsMail.java,v $
 * Date   : $Date: 2004/07/18 16:34:33 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.util;

import org.opencms.configuration.CmsMailHost;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Provides convenient access to mail functionality using the JavaMail package.<p>
 * 
 * Sun's <code>mail.jar</code> and <code>activation.jar</code> are required,
 * if this class should be used in any OpenCms class.<p>
 * 
 * Sender and recipients addresses may be given as <code>String</code>
 * or as <code>CmsUser</code> objects.
 * Different constructors for setting additional CC and BCC addresses are provided.<p>
 * 
 * Attachments can be added using the <code>addAttachment()</code> method.
 * 
 * For performance reasons, this classes uses threads for sending mails.
 * Mail threads can be initialized and started by :
 * <ul>
 * <li>creating a new CmsMail object with the appropriate constructor</li>
 * <li>eventually adding one ore more attachments to this objects</li>
 * <li>calling the <code>start()</code> method on this object.
 * </ul><p>
 * 
 * Example:<pre>
 * String from = "sender@test.org";
 * String[] to = {"reciever@test.org"};
 * String subject = "Testmail";
 * String content = "Hello World!";
 * 
 * CmsMail mail=new CmsMail(cms, from, to, subject, content, "text/plain");
 * mail.start();
 * </pre>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.10 $
 */
public class CmsMail extends Thread {

    /** 
     * This class implements a DataSource from an InputStream, a byte array or a 
     * String and is used to create a mail message with the CmsMail object.<p>
     * 
     * See also <a href="http://java.sun.com/products/javamail/index.html">http://java.sun.com/products/javamail/index.html</a>.<p>
     */
    private class CmsMailByteArrayDataSource implements DataSource {
        
        private byte[] m_data; // data
        private String m_datatype; // content-type
        

        /**
         * Constructor to create a DataSource from a byte array.<p>
         * 
         * @param data the data
         * @param type the type of the data
         */
        public CmsMailByteArrayDataSource(byte[] data, String type) {
            m_data = data;
            m_datatype = type;
        }
        
        /**
         * Constructor to create a DataSource from an input stream.<p>
         * 
         * @param is the data as input stream
         * @param type the type of the data
         */
        public CmsMailByteArrayDataSource(InputStream is, String type) {
            m_datatype = type;
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                int ch;
                while ((ch = is.read()) != -1) {
                    os.write(ch);
                }
                m_data = os.toByteArray();
            } catch (IOException e) {
                if (OpenCms.getLog(this).isWarnEnabled()) {
                    OpenCms.getLog(this).warn("Error writing to mail data source", e);
                }
            }
        }

        /**
         * Constructor to create a DataSource from a String.<p>
         * 
         * @param data the data as string
         * @param type the type of the data
         * @param encoding the encoding
         */
        public CmsMailByteArrayDataSource(String data, String type, String encoding) {
            try {
                m_data = data.getBytes(encoding);
            } catch (UnsupportedEncodingException e) {
                m_data = data.getBytes();
            }
            m_datatype = type;
        }
        
        /**
         * @see javax.activation.DataSource#getContentType()
         */
        public String getContentType() {
            return m_datatype;
        }

        /**
         * @see javax.activation.DataSource#getInputStream()
         */
        public InputStream getInputStream() throws IOException {
            if (m_data == null) {
                throw new IOException("no data for CmsMailByteArrayDataSource");
            }
            return new ByteArrayInputStream(m_data);
        }
        
        /**
         * @see javax.activation.DataSource#getName()
         */
        public String getName() {
            return "CmsMailByteArrayDataSource";
        }
        
        /**
         * @see javax.activation.DataSource#getOutputStream()
         */
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("not implemented in CmsMailByteArrayDataSource");
        }
    }    

    /**
     * Authentication object used when mail is send using SMTP password authentication. 
     */
    private class CmsMailPasswordAuth extends Authenticator {
        
        /** The password authentication. */
        private PasswordAuthentication m_auth;

        /**
         * Initializes a new  password authentication class instance.<p>
         * 
         * @param userName the name of the user
         * @param password the password of the user
         */
        public CmsMailPasswordAuth(String userName, String password) {
            m_auth = new PasswordAuthentication(userName, password);
        }

        /**
         * Returns the password authentication class instance.<p>
         * 
         * @return the password authentication class instance
         */
        public PasswordAuthentication getPasswordAuthentication() {
            return m_auth;
        }
    }    
    
    private Vector m_attachContent = new Vector();
    private Vector m_attachType = new Vector();
    private String[] m_bcc;
    private String[] m_cc;
    private CmsObject m_cms;
    private String m_content = "";    
    private String m_from = "";
    private String m_subject = "";
    private String[] m_to;
    private String m_type = "";

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

        // check sender email address
        String fromAddress = from.getEmail();
        if (fromAddress == null || fromAddress.equals("")) {
            fromAddress = OpenCms.getSystemInfo().getMailSettings().getMailFromDefault();
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
        m_type = type;
        m_cms = cms;
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
        
        // check sender email address
        String fromAddress = from.getEmail();
        if (fromAddress == null || fromAddress.equals("")) {
            fromAddress = OpenCms.getSystemInfo().getMailSettings().getMailFromDefault();
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
        m_type = type;
        m_cms = cms;
        if (m_cms == null) {
            OpenCms.getLog(this).debug("No CmsObject available for mail");
        }
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
        m_type = type;
        m_cms = cms;
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
        if (isBcc) {
            m_bcc = users;
        } else {
            m_cc=users;
        }
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
    public CmsMail(String from, String[] to, String subject, String content, String type) throws CmsException {
        this(null, from, to, subject, content, type);
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
    public CmsMail(String from, String[] to, String[] cc, String[] bcc, String subject, String content, String type) throws org.opencms.main.CmsException {
        this(null, from, to, cc, bcc, subject, content, type);
    }
    
    /**
     * Check a given email address for conforms with
     * RFC822 rules, see <a href="http://www.rfc-editor.org/rfc.html">http://www.rfc-editor.org/rfc.html</a>.<p>
     * 
     * @param address Email address to be checked
     * @return <code>true</code> if the address is syntactically correct, <code>false</code> otherwise
    */
    public static boolean checkEmail(String address) {
        boolean result = true;
        try {
            new javax.mail.internet.InternetAddress(address);
        } catch (javax.mail.internet.AddressException e) {
            result = false;
        }
        return result;
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
     * Try sending the mail.
     * This can take a few seconds to several minutes. We don't want
     * the user to wait for the response of the current HTTP request.
     * Therefore we are running this in a new thread. The user will
     * get his response immediately, then.
     */
    public void run() {

        // Build the message
        Message msg = null;
        Iterator i = OpenCms.getSystemInfo().getMailSettings().getMailHosts().iterator();
        boolean success = false;
        while (i.hasNext()) {
            CmsMailHost host = (CmsMailHost)i.next();
            try {
                msg = buildMessage(host);                
                Transport.send(msg);
                success = true;       
                if (OpenCms.getLog(this).isInfoEnabled()) {                  
                    OpenCms.getLog(this).info("Success sending mail from '" + m_from + "' to '" + getRecieverNames() + "' with subject '" + m_subject + "' using host " + host);
                }                      
            } catch (Exception e) {
                // get root exception for the log file
                while (e instanceof MessagingException) {
                    e = ((MessagingException)e).getNextException();
                }
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error while transmitting mail using host " + host, e);
                }                
            }     
            if (success) {
                break;
            }
        }
        if ((! success) && OpenCms.getLog(this).isErrorEnabled()) {
            OpenCms.getLog(this).error("Could not send mail from '" + m_from + "' to '" + getRecieverNames() + "' with subject '" + m_subject + "' using any configred host");

        }          
    }

    /**
     * Helper method for printing nice classnames in error messages.<p>
     * 
     * @return class name in [ClassName] format
     */
    protected String getClassName() {
        String name = getClass().getName();
        return "[" + name.substring(name.lastIndexOf(".") + 1) + "] ";
    }

    /**
     * Internal method for building a new <code>MimeMessage</code> object
     * with the given content, attachments, SMTP host properties, FROM, TO,
     * CC and BCC addresses.<p>
     * 
     * Will be called from the thread connecting to the SMTP server
     * and sending the mail.<p>
     * 
     * @param host configured mail host that should be connected
     * @return object that can be used as argument for the <code>Transport</code> class
     * @throws Exception no exceptions occuring while building the mail will be caught
     */
    private Message buildMessage(CmsMailHost host) throws Exception {

        // Default encoding for new mail message
        // TODO: This should NOT be hardcoded here!
        String mail_encoding = "ISO-8859-1";
        
        // get mail properties
        Properties mailProps = new java.util.Properties();
        mailProps.put("mail.smtp.host", host.getHostname());
        mailProps.put("mail.transport.protocol", host.getProtocol());
        CmsMailPasswordAuth auth = null;
        if (host.isAuthenticating()) {
            mailProps.put("mail.smtp.auth", "true");
            mailProps.put("mail.smtp.user", host.getUsername());
            auth = new CmsMailPasswordAuth(host.getUsername(), host.getPassword());
        }
        Session mailSession = Session.getInstance(mailProps, auth);                
        // build a new message object
        MimeMessage msg = new MimeMessage(mailSession);

        // check and set all addresses.
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
        
        if (m_type.equals("text/html")) {
            msg.setDataHandler(new DataHandler(new CmsMailByteArrayDataSource(m_content, m_type, mail_encoding)));
        } else {
            msg.setContent(m_content, m_type);
        }
            
        msg.setSentDate(new Date());
        return msg;
    }
    
    /**
     * Resolves the array of recievers to a String that
     * contains the comma separated names.<p>
     * 
     * @return the array of recievers resolved to a String 
     */
    private String getRecieverNames() {
        // must resolve array of recievers first
        String recievers = "";
        for (int j=0; j < m_to.length; j++) {
            recievers += m_to[j];
            if (j < (m_to.length - 1)) {
                recievers += ", ";
            }
        }        
        return recievers;
    }
}
