/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsDbExportUsers.java,v $
 * Date   : $Date: 2000/06/05 13:37:54 $
 * Version: $Revision: 1.4 $
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

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;

/**
 * Exports Files from database into XML file
 * 
 * @author Michaela Schleich
 * @version $Revision: 1.4 $ $Date: 2000/06/05 13:37:54 $
 */

class CmsDbExportUsers implements I_CmsConstants {

	/** ResourceBroker to access all methods and objects */
	private I_CmsResourceBroker m_RB = null;
	/** User to access all resourcbroker methods and objects */
	private CmsUser m_user = null;
	/** Project to access all resourcbroker methods and objects */
	private CmsProject m_project = null;

	
	/** need to initiate an XML object */
	private Document m_docXml = null;
	/** first element of an XML object(document node) need to insert other elements*/
	private Element m_firstElement = null;
	/** new XML element which is inserted in the XML first element */
	private Element m_newElement = null;
	/** need to navigate in the XML tree */
	private Element m_grandparentElement = null;
	/** need to navigate in the XML tree */
	private Element m_sectionElement = null;
	/** need to navigate in the XML tree */
	private Element m_parentElement = null;
	/** need to at values in the XML elements */
	private Node m_newNode = null;
	
	
	/**
	 * Constructor, creates a new CmsDBExportFile object.
	 * 
	 * @param eRB current ResourceBroker
	 * @param luser current m_user logged in
	 * @param lproject current m_project
	 * @param docXML XML object
	 * 
	 * @exception throws Exception
	 * 
	 */
	CmsDbExportUsers(I_CmsResourceBroker eRB, CmsUser luser, CmsProject lproject, Document m_docXml)
		throws Exception {
		
		m_RB=eRB;
		m_user=luser;
		m_project=lproject;
		this.m_docXml=m_docXml;
	}
	
	/**
	 * initiate the file export
	 * 
	 * @return the filled XML object
	 * 
	 * @exception throws CmsException
	 * @exception throws Exception
	 */	
	public Document export()
		throws CmsException, Exception {
			
		//get the documents node, first element in the XML object
		m_firstElement = m_docXml.getDocumentElement();
		
		// add all groups and users to the XML object
		groupExport();
		userExport();

		return m_docXml;
	}
	
	/**
	 * exports all groups 
	 * 
	 * @exception throws CmsException
	 * @exception throws Exception
	 */
	private void groupExport()
		throws CmsException, Exception {
	
		String help =null;
		int gi;
		
		m_newElement= m_docXml.createElement(C_TGROUPS);
		m_firstElement.appendChild(m_newElement);
		m_sectionElement=m_newElement;
		
		Vector groups=m_RB.getGroups(m_user,m_project);
			Enumeration genum=groups.elements();
			while (genum.hasMoreElements()) {
				m_newElement= m_docXml.createElement(C_TGROUPOBJ);
				m_sectionElement.appendChild(m_newElement);
				m_parentElement=m_newElement;
				
				CmsGroup g=(CmsGroup)genum.nextElement();
				
				m_newElement= m_docXml.createElement(C_TGNAME);
				m_parentElement.appendChild(m_newElement);
				m_newNode = m_docXml.createTextNode(g.getName());
				m_newElement.appendChild(m_newNode);
				
				m_newElement= m_docXml.createElement(C_TGDESC);
				m_parentElement.appendChild(m_newElement);
				m_newNode = m_docXml.createTextNode(g.getDescription());
				m_newElement.appendChild(m_newNode);
		
				m_newElement= m_docXml.createElement(C_TGFLAG);
				m_parentElement.appendChild(m_newElement);
				m_newNode = m_docXml.createTextNode(String.valueOf(g.getFlags()));
				m_newElement.appendChild(m_newNode);
				
				
				// get name of parent group
				// if none value for XML is "none"
				gi = g.getParentId();
				if(gi!=(-1)) {
					Enumeration genum2=groups.elements();
					while (genum2.hasMoreElements()) {
						CmsGroup pg=(CmsGroup)genum2.nextElement();
						if(pg.getId()==gi) {
							help=(pg.getName());
							break;
						}
					}
				} else {
					help="";
				}
				m_newElement= m_docXml.createElement(C_TGPARENTGROUP);
				m_parentElement.appendChild(m_newElement);
				m_newNode = m_docXml.createTextNode(help);
				m_newElement.appendChild(m_newNode);
			
				// get all m_user in group
				m_newElement= m_docXml.createElement(C_TGROUPUSERS);
				m_parentElement.appendChild(m_newElement);
				m_grandparentElement=m_parentElement;
				m_parentElement=m_newElement;
				
				Vector ug=m_RB.getUsersOfGroup(m_user,m_project,g.getName());
				Enumeration ugenum=ug.elements();
				while(ugenum.hasMoreElements()) {
					CmsUser u=(CmsUser)ugenum.nextElement();
					m_newElement= m_docXml.createElement(C_TGUSER);
					m_parentElement.appendChild(m_newElement);
					m_newNode = m_docXml.createTextNode(u.getName());
					m_newElement.appendChild(m_newNode);
				}
		
			}
	}
	// end groupExport()


	/**
	 * exports all users with additional info
	 * 
	 * @exception throws CmsException
	 * @exception throws Exception
	 * 
	 */
	private void userExport()
		throws CmsException, Exception {
	
		m_newElement= m_docXml.createElement(C_TUSERS);
		m_firstElement.appendChild(m_newElement);
		m_sectionElement=m_newElement;
		
		Vector users=m_RB.getUsers(m_user,m_project);
			Enumeration enum=users.elements();
			while (enum.hasMoreElements()) {
				m_newElement= m_docXml.createElement(C_TUSEROBJ);
				m_sectionElement.appendChild(m_newElement);
				m_parentElement=m_newElement;
				
				CmsUser u=(CmsUser)enum.nextElement();
				
				m_newElement= m_docXml.createElement(C_TULOGIN);
				m_parentElement.appendChild(m_newElement);
				m_newNode = m_docXml.createTextNode(u.getName());
				m_newElement.appendChild(m_newNode);
				
				m_newElement= m_docXml.createElement(C_TUPASSWD);
				m_parentElement.appendChild(m_newElement);
				m_newNode = m_docXml.createTextNode("Kennwort");
				m_newElement.appendChild(m_newNode);
				
				m_newElement= m_docXml.createElement(C_TUNAME);
				m_parentElement.appendChild(m_newElement);
				m_newNode = m_docXml.createTextNode(u.getLastname());
				m_newElement.appendChild(m_newNode);
				
				m_newElement= m_docXml.createElement(C_TUFIRSTNAME);
				m_parentElement.appendChild(m_newElement);
				m_newNode = m_docXml.createTextNode(u.getFirstname());
				m_newElement.appendChild(m_newNode);
				
				m_newElement= m_docXml.createElement(C_TUDESC);
				m_parentElement.appendChild(m_newElement);
				m_newNode = m_docXml.createTextNode(u.getDescription());
				m_newElement.appendChild(m_newNode);

				m_newElement= m_docXml.createElement(C_TUEMAIL);
				m_parentElement.appendChild(m_newElement);
				m_newNode = m_docXml.createTextNode(u.getEmail());
				m_newElement.appendChild(m_newNode);
				
				CmsGroup g = u.getDefaultGroup();
				m_newElement= m_docXml.createElement(C_TUDGROUP);
				m_parentElement.appendChild(m_newElement);
				m_newNode = m_docXml.createTextNode(g.getName());
				m_newElement.appendChild(m_newNode);
				
				//get all m_user groups
				m_newElement= m_docXml.createElement(C_TUSERGROUPS);
				m_parentElement.appendChild(m_newElement);
				m_grandparentElement=m_parentElement;
				m_parentElement=m_newElement;
				Vector ug=m_RB.getGroupsOfUser(m_user,m_project,u.getName());
				Enumeration ugenum=ug.elements();
				while (ugenum.hasMoreElements()){
					CmsGroup ng=(CmsGroup)ugenum.nextElement();
					m_newElement= m_docXml.createElement(C_TUGROUP);
					m_parentElement.appendChild(m_newElement);
					m_newNode = m_docXml.createTextNode(ng.getName());
					m_newElement.appendChild(m_newNode);
				}
				

				String help=null;
				if(u.getDisabled()==false) {
					help = new String("false");
				} else {
					help = new String("true");	
				}
				m_newElement= m_docXml.createElement(C_TUDISABLED);
				m_grandparentElement.appendChild(m_newElement);
				m_newNode = m_docXml.createTextNode(help);
				m_newElement.appendChild(m_newNode);
				
				m_newElement= m_docXml.createElement(C_TUFLAG);
				m_grandparentElement.appendChild(m_newElement);
				m_newNode = m_docXml.createTextNode(String.valueOf(u.getFlags()));
				m_newElement.appendChild(m_newNode);

				
				// get additional Info
				m_newElement= m_docXml.createElement(C_TUADDINFO);
				m_grandparentElement.appendChild(m_newElement);
				m_parentElement=m_newElement;
				
				Hashtable h = u.getAdditionalInfo();
				Enumeration enum2=h.keys();
				Enumeration enum3=h.elements();
				while (enum2.hasMoreElements())	{
				String keyhelp=(String)enum2.nextElement();
								
					if(!(keyhelp.equals(C_ADDITIONAL_INFO_DEFAULTGROUP_ID))) {
							m_newElement= m_docXml.createElement(C_TUINFOKEY);
							m_parentElement.appendChild(m_newElement);
							m_newNode = m_docXml.createTextNode(keyhelp);
							m_newElement.appendChild(m_newNode);
					
							m_newElement= m_docXml.createElement(C_TUINFOVALUE);
							m_parentElement.appendChild(m_newElement);
							m_newNode = m_docXml.createTextNode(enum3.nextElement().toString());
							m_newElement.appendChild(m_newNode);
						}
				}

			}
	
	}
	// end userExport()

}
		
