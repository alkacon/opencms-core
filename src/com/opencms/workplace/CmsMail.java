/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsMail.java,v $
 * Date   : $Date: 2000/04/13 16:20:10 $
 * Version: $Revision: 1.1 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.workplace;

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
 * This class is used to send a mail, it uses Threads to send it.
 *
 * @author $Author: w.babachan $
 * @version $Name:  $ $Revision: 1.1 $ $Date: 2000/04/13 16:20:10 $
 * @see java.lang.Thread
 */
public class CmsMail extends Thread implements I_CmsLogChannels {
			
	// constants	
	private final String c_FROM;
	private final String[] c_TO;
	private final String c_MAILSERVER;
	private final String c_SUBJECT;
	private final String c_CONTENT;	
	
	/**
	 * Constructor, that creates an Email Object.
	 * 
	 * @param cms Cms object.
	 * @param from Address of sender.
	 * @param to Address of recipient.
	 * @param subject Subject of email.
	 * @param content Content of email.
	 */	
	public CmsMail(A_CmsObject cms,String from, String[] to, String subject, String content)
		throws CmsException{
		// check sender email address
		if (from==null) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address.", CmsException.C_BAD_NAME);
		}
		if (from.equals("")) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address.", CmsException.C_BAD_NAME);
		}
		if (from.indexOf("@")==-1 || from.indexOf(".")==-1) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address: " + from, CmsException.C_BAD_NAME);			
		}
		// check recipient email address
		Vector v=new Vector(to.length);
		for(int i=0;i<to.length;i++) {
			if (to[i]==null) {
				continue;
			}
			if (to[i].equals("")){
				continue;
			}
			if (to[i].indexOf("@")==-1 || to[i].indexOf(".")==-1) {
				throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email, Invalid recipient email address: " + to[i],CmsException.C_BAD_NAME);
			}
			v.addElement(to[i]);
		}
		String users[]=new String[v.size()];
		for(int i=0;i<v.size();i++) {
			users[i]=(String)v.elementAt(i);
		}
		if (users.length==0){
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown recipient email address.", CmsException.C_BAD_NAME);
		}
		c_FROM=from;
		c_TO=users;
		c_SUBJECT=(subject==null?"":subject);
		c_CONTENT=(content==null?"":content);
		CmsXmlWpConfigFile conf=new CmsXmlWpConfigFile(cms);		
		c_MAILSERVER=conf.getMailServer();
	}
	
	/**
	 * Constructor, that creates an Email Object.
	 * 
	 * @param cms Cms object
	 * @param from User object that contains the address of sender.
	 * @param to User object that contains the address of recipient.
	 * @param subject Subject of email.
	 * @param content Content of email.
	 */	
	public CmsMail(A_CmsObject cms,A_CmsUser from, A_CmsUser[] to, String subject, String content)
		throws CmsException{
		// check sender email address
		if (from.getEmail()==null) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address.", CmsException.C_BAD_NAME);
		}
		if (from.getEmail().equals("")) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address.", CmsException.C_BAD_NAME);
		}
		if (from.getEmail().indexOf("@")==-1 || from.getEmail().indexOf(".")==-1) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address: " + from.getEmail(), CmsException.C_BAD_NAME);
		}
		// check recipient email address
		Vector v=new Vector(to.length);
		for(int i=0;i<to.length;i++) {
			if (to[i].getEmail()==null) {
				continue;
			}
			if (to[i].getEmail().equals("")){
				continue;
			}
			if (to[i].getEmail().indexOf("@")==-1 || to[i].getEmail().indexOf(".")==-1) {
				throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email, Invalid recipient email address: " + to[i].getEmail(),CmsException.C_BAD_NAME);
			}
			v.addElement(to[i].getEmail());
		}
		String users[]=new String[v.size()];
		for(int i=0;i<v.size();i++) {
			users[i]=(String)v.elementAt(i);
		}
		if (users.length==0){
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown recipient email address.", CmsException.C_BAD_NAME);
		}
		c_TO=users;
		c_FROM=from.getEmail();
		c_SUBJECT=(subject==null?"":subject);
		c_CONTENT=(content==null?"":content);
		CmsXmlWpConfigFile conf=new CmsXmlWpConfigFile(cms);		
		c_MAILSERVER=conf.getMailServer();
		
	}
	
	
	/**
	 * Constructor, that creates an Email Object.
	 * 
	 * @param cms Cms object.
	 * @param from User object that contains the address of sender.
	 * @param to Group object that contains the address of recipient.
	 * @param subject Subject of email.
	 * @param content Content of email.	
	 */	
	public CmsMail(A_CmsObject cms,A_CmsUser from, A_CmsGroup to, String subject, String content)
		throws CmsException{
		// check sender email address
		if (from.getEmail()==null) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address.", CmsException.C_BAD_NAME);
		}
		if (from.getEmail().equals("")) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address.", CmsException.C_BAD_NAME);
		}
		if (from.getEmail().indexOf("@")==-1 || from.getEmail().indexOf(".")==-1) {
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown sender email address: " + from.getEmail(), CmsException.C_BAD_NAME);
		}
		// check recipient email address
		Vector vu=cms.getUsersOfGroup(to.getName());
		Vector v=new Vector(vu.size());
		for(int i=0;i<vu.size();i++) {
			String address=((A_CmsUser)vu.elementAt(i)).getEmail();
			if (address==null) {
				continue;
			}
			if (address.equals("")) {
				continue;
			}
			if (address.indexOf("@")==-1 || address.indexOf(".")==-1) {
				throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email, Invalid recipient email address: " + address,CmsException.C_BAD_NAME);
			}
			v.addElement(address);
		}
		String users[]=new String[v.size()];
		for(int i=0;i<v.size();i++) {
			users[i]=(String)v.elementAt(i);
		}
		if (users.length==0){
			throw new CmsException("[" + this.getClass().getName() + "] " + "Error in sending email,Unknown recipient email address.", CmsException.C_BAD_NAME);
		}		
		c_TO=users;
		c_FROM=from.getEmail();
		c_SUBJECT=(subject==null?"":subject);
		c_CONTENT=(content==null?"":content);
		CmsXmlWpConfigFile conf=new CmsXmlWpConfigFile(cms);		
		c_MAILSERVER=conf.getMailServer();
	}
	
	
	/**
	 * This method starts sending an email.
	 */
	public void run() {
		// Send the mail
		// create some properties and get the default Session
		
		Properties props=new Properties();		
		props.put("mail.smtp.host",c_MAILSERVER);		
		Session session=Session.getDefaultInstance(props,null);	
		MimeMessage msg=new MimeMessage(session);
		try {
			InternetAddress[] to=new InternetAddress[c_TO.length];
			for(int i=0;i<c_TO.length;i++) {
				to[i]=new InternetAddress(c_TO[i]);
			}
			msg.setFrom(new InternetAddress(c_FROM));
			msg.setRecipients(Message.RecipientType.TO,to);
			msg.setSubject(c_SUBJECT,"ISO-8859-1");
			msg.setContent(c_CONTENT,"text/plain");
			msg.setSentDate(new Date());
			Transport.send(msg);			
		} catch(Exception e) {
			if(A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_DEBUG, "Error in sending email:"+ e.getMessage());
            }
			
		}
		
	}
}