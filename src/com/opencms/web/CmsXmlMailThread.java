/**
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/web/Attic/CmsXmlMailThread.java,v $ 
 * Author : $Author: w.babachan $
 * Date   : $Date: 2000/02/20 20:43:11 $
 * Version: $Revision: 1.2 $
 * Release: $Name:  $
 *
 * Copyright (c) 2000 Mindfact interaktive medien ag.   All Rights Reserved.
 *
 * THIS SOFTWARE IS NEITHER FREEWARE NOR PUBLIC DOMAIN!
 *
 * To use this software you must purchease a licencse from Mindfact.
 * In order to use this source code, you need written permission from Mindfact.
 * Redistribution of this source code, in modified or unmodified form,
 * is not allowed.
 *
 * MINDAFCT MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THIS SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. MINDFACT SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */

package com.opencms.web;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import javax.mail.*;
import javax.activation.*;
import javax.mail.internet.*;	

import java.util.*;
import java.io.*;

/**
 * This class is used to send a mail, it used the Threads to send it.
 * 
 * @author $Author: w.babachan $
 * @version $Name:  $ $Revision: 1.2 $ $Date: 2000/02/20 20:43:11 $
 * @see java.lang.Thread
 */
public class CmsXmlMailThread extends Thread {
	
	// Hashtable keys
	private static final String C_HASH_CERTIFICATES="certificates";
	private static final String C_HASH_FROM="from";
	private static final String C_HASH_AN="an";
	private static final String C_HASH_CC="cc";
	private static final String C_HASH_BCC="bcc";
	private static final String C_HASH_HOST="host";
	private static final String C_HASH_SUBJECT="subject";
	private static final String C_HASH_CONTENT="content";
	
	// static constants
	private static final String C_PATH="/tmp/";
	
	// constants
	private final String c_CERTIFICATES;
	private final String c_FROM;
	private final String c_AN;
	private final String c_CC;
	private final String c_BCC;
	private final String c_HOST;
	private final String c_SUBJECT;
	private final String c_CONTENT;
	
	
	public CmsXmlMailThread(Hashtable mail) {
		c_CERTIFICATES=(String)mail.get(C_HASH_CERTIFICATES);
		c_FROM=(String)mail.get(C_HASH_FROM);
		c_AN=(String)mail.get(C_HASH_AN);
		c_CC=(String)mail.get(C_HASH_CC);
		c_BCC=(String)mail.get(C_HASH_BCC);
		c_HOST=(String)mail.get(C_HASH_HOST);
		c_SUBJECT=(String)mail.get(C_HASH_SUBJECT);
		c_CONTENT=(String)mail.get(C_HASH_CONTENT);		
	}
	
	public void run() {
		// Send the mail
		// create some properties and get the default Session
		Properties props=new Properties();		
		props.put("mail.smtp.host",c_HOST);		
		Session session=Session.getDefaultInstance(props,null);	
		try {
			MimeMessage msg=new MimeMessage(session);
			InternetAddress[] to ={new InternetAddress(c_AN)};
			msg.setFrom(new InternetAddress(c_FROM));
			msg.setRecipients(Message.RecipientType.TO,to);
			if (c_CC != null) {
				msg.setRecipients(Message.RecipientType.CC,InternetAddress.parse(c_CC, false));
			}
			if (c_BCC != null) {
				msg.setRecipients(Message.RecipientType.BCC,InternetAddress.parse(c_BCC, false));
			}
			msg.setSubject(c_SUBJECT,"ISO-8859-1");
			if (!c_CERTIFICATES.equals("")) {
				// create and fill the first message part
				MimeBodyPart mbp1=new MimeBodyPart();
				mbp1.setText(c_CONTENT,"ISO-8859-1");
			
				// create the second message part
				MimeBodyPart mbp2=new MimeBodyPart();
		
				// attach the file to the message				
				FileDataSource fds=new FileDataSource(C_PATH+c_CERTIFICATES);
				mbp2.setDataHandler(new DataHandler(fds));
		
				mbp2.setFileName(c_CERTIFICATES);				
				
				Multipart mp=new MimeMultipart();
				mp.addBodyPart(mbp1);
				mp.addBodyPart(mbp2);
												
				msg.setContent(mp);
			} else {
				msg.setContent(c_CONTENT,"text/plain");
			}
			msg.setSentDate(new Date());
			Transport.send(msg);
			
		}catch (Exception e) {			
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL,e.getMessage());
			}
		}
	}
}