/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsDbExportUser.java,v $
 * Date   : $Date: 2000/02/15 17:43:59 $
 * Version: $Revision: 1.2 $
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

package com.opencms.file;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;


public class CmsDbExportUser
{
	final static String C_FELEMENT = "USERS";
	final static String C_TUSEROBJ = "USEROBJ";
	final static String C_TLOGIN = "LOGIN";
	final static String C_TPASSWD = "PASSWD";
	final static String C_TNAME = "NAME";	
	final static String C_TFIRSTNAME = "FIRSTNAME";	
	final static String C_TDESC = "DESC";	
	final static String C_TEMAIL = "EMAIL";	
	final static String C_TDGROUP = "DEFAULTGROUP";	
	final static String C_TDISABLED = "DISABLED";	
	final static String C_TFLAG = "FLAG";
	final static String C_TUSERGROUPS = "USERGROUPS";
	final static String C_TGROUP = "GROUP";
	final static String C_TADDINFO = "ADDINFO";
	final static String C_TINFOKEY = "INFOKEY";
	final static String C_TINFOVALUE = "INFOVALUE";	
		
	//ResourceBroker for all methods and objects
	private I_CmsResourceBroker RB = null;
	//to access all objects
	private A_CmsUser user = null;
	//to access all objects
	private A_CmsProject project = null;
	
	//To convert into XML format
	private I_CmsXmlParser parser = null;
	private Document docXml = null;
	private Element firstElement = null;
	private Element newElement = null;
	private Element parentElement = null;
	private Element grandparentElement = null;
	private Node newNode = null;
	
	//For the output XML File
	private File fUser = null;
	private FileOutputStream fUserStream = null;
	private PrintWriter fUserOutput = null;
	
	/**
	 * Constructor, creates a new CmsTask object.
	 */
	CmsDbExportUser(I_CmsResourceBroker eRB, A_CmsUser luser, A_CmsProject lproject, String filename)
		throws IOException, Exception
	{
		RB=eRB;
		user=luser;
		project=lproject;
	
		init(filename);
	
	}
	

	/**
	 * Inittialisation, open all Streams etc.
	 */
	private void init(String filename) throws IOException, Exception
	{											  
		fUser = new File(filename);
		fUserStream = new FileOutputStream(fUser);
		
		parser = A_CmsXmlContent.getXmlParser();
		docXml = parser.createEmptyDocument(C_FELEMENT);	
	}

	
	
	/**
	 * Exports all Useres with additional info form database in XMLfile
	 */
	public void export()
		throws CmsException, Exception
	{
		firstElement = docXml.getDocumentElement();
		
		Vector users=RB.getUsers(user,project);
			Enumeration enum=users.elements();
			while (enum.hasMoreElements()) {
				newElement= docXml.createElement(C_TUSEROBJ);
				firstElement.appendChild(newElement);
				parentElement=newElement;
				
				A_CmsUser u=(A_CmsUser)enum.nextElement();
				
				newElement= docXml.createElement(C_TLOGIN);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(u.getName());
				newElement.appendChild(newNode);
				
				newElement= docXml.createElement(C_TPASSWD);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode("Kennwort");
				newElement.appendChild(newNode);
				
				newElement= docXml.createElement(C_TNAME);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(u.getLastname());
				newElement.appendChild(newNode);
				
				newElement= docXml.createElement(C_TFIRSTNAME);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(u.getFirstname());
				newElement.appendChild(newNode);
				
				newElement= docXml.createElement(C_TDESC);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(u.getDescription());
				newElement.appendChild(newNode);

				newElement= docXml.createElement(C_TEMAIL);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(u.getEmail());
				newElement.appendChild(newNode);
				
				A_CmsGroup g = u.getDefaultGroup();
				newElement= docXml.createElement(C_TDGROUP);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(g.getName());
				newElement.appendChild(newNode);
				
				//get all user groups
				newElement= docXml.createElement(C_TUSERGROUPS);
				parentElement.appendChild(newElement);
				grandparentElement=parentElement;
				parentElement=newElement;
				Vector ug=RB.getGroupsOfUser(user,project,u.getName());
				Enumeration ugenum=ug.elements();
				while (ugenum.hasMoreElements()){
					A_CmsGroup ng=(A_CmsGroup)ugenum.nextElement();
					newElement= docXml.createElement(C_TGROUP);
					parentElement.appendChild(newElement);
					newNode = docXml.createTextNode(ng.getName());
					newElement.appendChild(newNode);
				}
				

				String help=null;
				if(u.getDisabled()==false) {
					help = new String("false");
				} else {
					help = new String("true");	
				}
				newElement= docXml.createElement(C_TDISABLED);
				grandparentElement.appendChild(newElement);
				newNode = docXml.createTextNode(help);
				newElement.appendChild(newNode);
				
				newElement= docXml.createElement(C_TFLAG);
				grandparentElement.appendChild(newElement);
				newNode = docXml.createTextNode(String.valueOf(u.getFlags()));
				newElement.appendChild(newNode);

				
				// get additional Info
				newElement= docXml.createElement(C_TADDINFO);
				grandparentElement.appendChild(newElement);
				parentElement=newElement;
				
				Hashtable h = u.getAdditionalInfo();
				Enumeration enum2=h.keys();
				Enumeration enum3=h.elements();
				while (enum2.hasMoreElements())	{
				
					if(!((String)enum2.nextElement()).equals("USER_DEFAULTGROUP_ID")) {
							newElement= docXml.createElement(C_TINFOKEY);
							parentElement.appendChild(newElement);
							newNode = docXml.createTextNode((String)enum2.nextElement());
							newElement.appendChild(newNode);
					
							newElement= docXml.createElement(C_TINFOVALUE);
							parentElement.appendChild(newElement);
							newNode = docXml.createTextNode(enum3.nextElement().toString());
							newElement.appendChild(newNode);
						}
				}

			}
		

		parser.getXmlText(docXml, fUserStream);
		fUserStream.close();
	}
	
}
