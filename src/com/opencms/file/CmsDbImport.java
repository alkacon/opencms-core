/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsDbImport.java,v $
 * Date   : $Date: 2000/02/16 18:06:27 $
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
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.opencms.core.*;
import com.opencms.template.*;

/**
 * imports an generated (with db export) XML file
 * 
 * @author Michaela Schleich
 * @version $Revision: 1.4 $ $Date: 2000/02/16 18:06:27 $
 */
class CmsDbImport implements I_CmsConstants, I_CmsDbImport {
	
	/** ResourceBroker to access all methods and objects */
	private I_CmsResourceBroker m_RB = null;
	/** User to access all resourcbroker methods and objects */
	private A_CmsUser m_user = null;
	/** Project to access all resourcbroker methods and objects */
	private A_CmsProject m_project = null;
	
	/** to get the the XML object from the inputfile */
	private I_CmsXmlParser m_parser = null;
	/** to create the XML object */
	private Document m_docXml = null;
	/** to get the entries in the XML object */
	private Element m_firstElement = null;
	/** to get navigate in the XML object */
	private NodeList m_sectionElements = null;
	
	//to update db
	/** the path to import the resources */
	private String m_importpath=null;
	/** to get the group name and store in the db*/
	private String m_gName= null;
	/** to get the group description and store in the db*/
	private String m_gDesc= null;
	/** to get the group flag and store in the db*/
	private String m_gFlag= null;
	/** to get the parent group name and store in the db*/
	private String m_gParent= null;
	
	/** to get the user큦 login and store in the db*/
	private String m_uLogin = null;
	/** to store the user큦 password in the db default "Kennwort" */
	private String m_uPasswd = null;
	/** to get the user큦 lastname and store in the db*/
	private String m_uName = new String();
	/** to get the user큦 firstname and store in the db*/
	private String m_uFirstname = new String();
	/** to get the user큦 email and store in the db*/
	private String m_uEmail = new String();
	/** to get the user큦 description and store in the db*/
	private String m_uDesc = null;
	/** to get the user큦 default gorup and store in the db*/
	private String m_uDGroup = null;
	/** is the user disabled or not and store the info in the db*/
	private String m_uDis = null;
	/** to get the user큦 flag and store in the db*/
	private String m_uFlag = null;
	/** to get the user큦 group(s) and store in the db*/
	private String m_uGroup = null;

	/** to store the exception msg  */
	private Vector m_errMsg=new Vector();
	/** to signalise if files imported or not  */
	private int m_files_exported = C_NO_FILES_IMPORTED;
	
	/** for the input XML file */
	private File m_fXml = null;
	/** for the input file reader */
	private FileReader m_fXmlReader = null;
	/** for the input stream */
	private BufferedReader m_fXmlStream = null;
	
	
	/**
	 * Constructor, creates a new CmsDbImport object.
	 *
	 * @param eRB current ResourceBroker
	 * @param luser current user logged in
	 * @param lproject current project
	 * @param path in which folder (absolute path) to import
	 * @param filename which XML file should be imported
	 * 
	 * @exception throws IOException
	 * @exception throws Exception
	 * 
	 */
	CmsDbImport(I_CmsResourceBroker eRB, A_CmsUser luser, A_CmsProject lproject, String filename, String path)
		throws IOException, Exception {
		
		m_RB=eRB;
		m_user=luser;
		m_project=lproject;
		m_importpath=path;
	
		init(filename);
	}

	/**
	 * Inittialisation
	 * 
	 * open the XML file
	 * reads the XML file into a XML object
	 * 
	 * @exception throws IOException
	 * @exception throws Exception
	 * 
	 */
	private void init(String filename)
		throws IOException, Exception {											  
		
		// creats an output stream		
		m_fXml = new File(filename);
		m_fXmlReader= new FileReader(m_fXml);
		m_fXmlStream = new BufferedReader(m_fXmlReader);
		
		// creats a new XML object		
		m_parser = A_CmsXmlContent.getXmlParser();
		m_docXml = m_parser.parse(m_fXmlStream);
	}

	/**
	 * xmlImport
	 * initialize the database import
	 * 
	 * @return a vector with error messages
	 * 
	 * @exception throws IOException
	 * @exception throws Exception
	 * 
	 */	
	public Vector xmlImport()
			throws CmsException, Exception {
			
			//get the documents node, first element in the XML object
			m_firstElement = m_docXml.getDocumentElement();
			m_sectionElements=m_firstElement.getChildNodes();
			
			m_files_exported = C_NO_FILES_IMPORTED;
			
			int nll=m_sectionElements.getLength();
			int i, htype;
			for(i=0; i<nll; i++){
				if((m_sectionElements.item(i).getNodeName()).equals(C_TGROUPS)) {
					//read all groups from the XML object
					groupImport(m_sectionElements.item(i));
				}
				//read all users from the XML object und update user and users and groupusers
				if((m_sectionElements.item(i).getNodeName()).equals(C_TUSERS)) {
					userImport(m_sectionElements.item(i));
				}
				if((m_sectionElements.item(i).getNodeName()).equals(C_TFILES)) {
					CmsDbImportFiles cmsImport= new CmsDbImportFiles(m_RB, m_user, m_project, m_importpath, m_sectionElements.item(i), m_errMsg);
					m_errMsg=cmsImport.xmlImport();
					m_files_exported = C_FILES_IMPORTED;
				}
			}
				
			//colse the input file
			m_fXmlStream.close();
			return m_errMsg;
		}
	
	
	/**
	 * xmlImport
	 * to signalize if files are importetd or not
	 * 
	 * @return an int:
	 *		C_NO_FILES_IMPORTED if no resources are imported;
	 *		C_FILES_IMPORTED if resources are imported;
	 * 
	 */
	public int getFilesImported() {
		return m_files_exported;	
	}
	
		
	/**
	 * imports groups 
	 * 
	 * @param parentNode the first Node, which contains all groups
	 * 
	 * @exception throws IOException
	 * @exception throws Exception
	 * 
	 */
	private void groupImport(Node parentNode)
		throws CmsException, Exception {
	
		NodeList parentElements=parentNode.getChildNodes();
			
		int nll=parentElements.getLength();
		int i;
		for(i=0; i<nll; i++) {
			//read all groups from the XML object
			if((parentElements.item(i).getNodeName()).equals(C_TGROUPOBJ)) {
				updateDbGroups(parentElements.item(i));
				}
		}
	}
	
	/**
	 * imports users
	 * 
	 * @param parentNode the first Node, which contains all groups
	 * 
	 * @exception throws IOException
	 * @exception throws Exception
	 * 
	 */
	private void userImport(Node parentNode)
		throws CmsException, Exception {
	
		NodeList parentElements=parentNode.getChildNodes();
			
		int nll=parentElements.getLength();
		int i;
		for(i=0; i<nll; i++) {
			//read all users from the XML object
			if((parentElements.item(i).getNodeName()).equals(C_TUSEROBJ)) {
				updateDbUsers(parentElements.item(i));
				}
		}
	}
		
	/**
	* updateDbGroups
	* writes the groups into the db
	* 
	* @param parentNode contains one group
	* 
	* @exception throws Exception
	* 
	*/
		private void updateDbGroups(Node parentNode)
			throws Exception {
	
			NodeList parentElements=parentNode.getChildNodes();
			
			int nll=parentElements.getLength();
			int i, htype;
			
			for(i=0; i<nll; i++) {
				if((parentElements.item(i).getNodeName()).equals(C_TGNAME) && (parentElements.item(i).hasChildNodes())) {
					m_gName=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TGPARENTGROUP)&&(parentElements.item(i).hasChildNodes())) {
					m_gParent=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TGDESC) && (parentElements.item(i).hasChildNodes())) {
					m_gDesc=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TGFLAG) && (parentElements.item(i).hasChildNodes())) {
					m_gFlag=parentElements.item(i).getFirstChild().getNodeValue();
				}
			}
			// end for
			try {
				A_CmsGroup group = m_RB.addGroup(m_user, m_project, m_gName, m_gDesc, Integer.parseInt(m_gFlag), m_gParent);
			}catch (CmsException e){
					m_errMsg.addElement(e.getMessage());			
			}
		}
		// end updateDbGroups
		
		
		/**
		 * updateDbUsers
		 * writes the user into the db
		 * 
		 * @param parentNode contains one user
		 * 
		 * @exception throws Exception
		 * 
		 */
		private void updateDbUsers(Node parentNode)
			throws Exception {
	
			NodeList parentElements=parentNode.getChildNodes();
			
			Hashtable h_addInfo = new Hashtable();
			int nll=parentElements.getLength();
			int i, htype;
			for(i=0; i<nll; i++) {
				if((parentElements.item(i).getNodeName()).equals(C_TULOGIN) && (parentElements.item(i).hasChildNodes())) {
					m_uLogin=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUPASSWD)&&(parentElements.item(i).hasChildNodes())) {
					m_uPasswd=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUNAME) && (parentElements.item(i).hasChildNodes())) {
					m_uName=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUFIRSTNAME) && (parentElements.item(i).hasChildNodes())) {
					m_uFirstname=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUDESC) && (parentElements.item(i).hasChildNodes())) {
					m_uDesc=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUEMAIL) && (parentElements.item(i).hasChildNodes())) {
					m_uEmail=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUDGROUP) && (parentElements.item(i).hasChildNodes())) {
					m_uDGroup=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUDISABLED) && (parentElements.item(i).hasChildNodes())) {
					m_uDis=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUFLAG) && (parentElements.item(i).hasChildNodes())) {
					m_uFlag=parentElements.item(i).getFirstChild().getNodeValue();
				}
				//additional Info
				if((parentElements.item(i).getNodeName()).equals(C_TUNAME) && (parentElements.item(i).hasChildNodes())) {
					m_uName=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUFIRSTNAME) && (parentElements.item(i).hasChildNodes())) {
					m_uFirstname=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUEMAIL) && (parentElements.item(i).hasChildNodes())) {
					m_uEmail=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUADDINFO) && (parentElements.item(i).hasChildNodes())) {
					NodeList childElements=parentElements.item(i).getChildNodes();
					int cnll=childElements.getLength();
					int j=0;
					String key =null;
					for(j=0; j<cnll; j++) {
						if((childElements.item(j).getNodeName()).equals(C_TUINFOKEY) && (childElements.item(j).hasChildNodes())) {
							key=childElements.item(j).getFirstChild().getNodeValue();
						}
						if((childElements.item(j).getNodeName()).equals(C_TUINFOVALUE) && (childElements.item(j).hasChildNodes())) {
							String value=childElements.item(j).getFirstChild().getNodeValue();
							h_addInfo.put(key, value);
						}
					}
				}
			}
			// end for
			try {
				A_CmsUser newUser = m_RB.addUser(m_user, m_project, m_uLogin, m_uPasswd, m_uDGroup, m_uDesc, h_addInfo, Integer.parseInt(m_uFlag));
				newUser.setEmail(m_uEmail);
				newUser.setFirstname(m_uFirstname);
				newUser.setLastname(m_uName);
				m_RB.writeUser(m_user, m_project, newUser);
			}catch (CmsException e){
				m_errMsg.addElement(e.getMessage());			
			}
			
			// add user to group
			for(i=0;i<nll; i++) {
				if((parentElements.item(i).getNodeName()).equals(C_TUSERGROUPS) && (parentElements.item(i).hasChildNodes())) {
					
					NodeList childElements=parentElements.item(i).getChildNodes();
					int cnll=childElements.getLength();
					int j=0;
					for(j=0; j<cnll; j++) {
						if((childElements.item(j).getNodeName()).equals(C_TUGROUP) && (childElements.item(j).hasChildNodes())) {
							try {
								m_uGroup=(childElements.item(j).getFirstChild().getNodeValue());
								m_RB.addUserToGroup(m_user, m_project, m_uLogin, m_uGroup);
							}catch (CmsException e){
								m_errMsg.addElement(e.getMessage());
							}
						}
					}
				}
			
			
		}
		//end add user to group
	}
	// end updateDbUsers
		
}