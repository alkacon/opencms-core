/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsDbImportFiles.java,v $
 * Date   : $Date: 2000/04/03 10:48:29 $
 * Version: $Revision: 1.15 $
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
 * @version $Revision: 1.15 $ $Date: 2000/04/03 10:48:29 $
 */
class CmsDbImportFiles implements I_CmsConstants {
	
	
	/** ResourceBroker to access all methods and objects */
	private I_CmsResourceBroker m_RB = null;
	/** User to access all resourcbroker methods and objects */
	private A_CmsUser m_user = null;
	/** Project to access all resourcbroker methods and objects */
	private A_CmsProject m_project = null;
	
	/** to get the entries in the XML object */
	private Node m_firstNode = null;
	/** to navigate in the XML object	 */
	private NodeList m_sectionElements = null;
	/** to navigate in the XML object	 */
	private NodeList m_resourceElements = null;
	
	/** to update and the db and creates the folder and files - resource name */
	private String m_fName=null;
	/** to update and the db and creates the folder and files - resource typename */
	private String m_fTypename=new String();
	/** to update and the db and creates the folder and files - resource owner */
	private String m_fUser=new String();
	/** to update and the db and creates the folder and files - resource group */
	private String m_fGroup=new String();
	/** to update and the db and creates the folder and files - resource accessflags */
	private String m_fAccess=new String();
	/** to update and the db and creates the folder and files - file content */
	private String m_fContent=null;

	/** the folder in which to import the new resources	 */
	private String m_importPath=null;
	
	/** vector to return the error messages */
	private Vector m_errMsg=new Vector();
	
	
	/**
	 * Constructor, creates a new CmsDbImport object.
	 *
	 * @param eRB current ResourceBroker
	 * @param luser current m_user logged in
	 * @param lproject current m_project
	 * @param path in which folder (absolute path) to import
	 * @param fileNode contains all resources, which should be imported
 	 * @param errLog contains error messages, which occour on create files and write files
 	 * 
 	 * @exception throws IOException
 	 * @exception throws Exception
	 * 
	 */
	CmsDbImportFiles(I_CmsResourceBroker eRB, A_CmsUser luser, A_CmsProject lproject, String path, Node fileNode , Vector errLog)
		throws IOException, Exception {
		m_RB=eRB;
		m_user=luser;
		m_project=lproject;
		m_importPath=path;
		
		m_firstNode=fileNode;
		
		m_errMsg=errLog;
	}


	/**
	 * xmlImport
	 * imports the exported resources in the db
	 *
	 * @return m_errMsg vector with all error messages
	 * 
	 * @exception throws IOException
	 * @exception throws Exception
	 * 
	 */
	public Vector xmlImport()
			throws CmsException, Exception {
			
			//get all file nodes
			m_sectionElements=m_firstNode.getChildNodes();
			
			int nll=m_sectionElements.getLength();
			int i;

			for(i=0; i<nll; i++){
				if((m_sectionElements.item(i).getNodeName()).equals(C_TFILEOBJ)) {
					m_resourceElements = m_sectionElements.item(i).getChildNodes();
					readResource();
				}
			}
			return m_errMsg;
	}
	// end xmlImport
	
	/**
	 * readResource
	 * get one resource of the XML object
	 *
	 */
	private void readResource() {
	
		Hashtable h_fProperty=new Hashtable();
		byte[] fContent=new byte[1];
	
		int nll=m_resourceElements.getLength();
		int i;
		for(i=0; i<nll; i++) {
			String help=m_resourceElements.item(i).getNodeName();
			
				if(help.equals(C_TFNAME)){
					m_fName=m_resourceElements.item(i).getFirstChild().getNodeValue();
				}
				if(help.equals(C_TFTYPENAME)){
					m_fTypename=m_resourceElements.item(i).getFirstChild().getNodeValue();
				}
				if(help.equals(C_TFUSER)){
					m_fUser=m_resourceElements.item(i).getFirstChild().getNodeValue();
				}
				if(help.equals(C_TFGROUP)){
					m_fGroup=m_resourceElements.item(i).getFirstChild().getNodeValue();
				}
				if(help.equals(C_TFACCESS)){
					m_fAccess=m_resourceElements.item(i).getFirstChild().getNodeValue();
				}
				
				// Propertydefinitions
				
				if( (help.equals(C_TFPROPERTYINFO)) && (m_resourceElements.item(i).hasChildNodes()) ){
					h_fProperty=writePropertyDef(m_resourceElements.item(i).getChildNodes(), m_fTypename);
				}
				
				// if resource is a file
				if( (help.equals(C_FCONTENT)) && ( !(m_fTypename.equals(C_TYPE_FOLDER_NAME)) ) && (m_resourceElements.item(i).hasChildNodes()) ){
					m_fContent=m_resourceElements.item(i).getFirstChild().getNodeValue();
					fContent=readContent(m_fContent);
				}
		}
		//end for
		
		if( m_fTypename.equals(C_TYPE_FOLDER_NAME) ) {
			if( (!(m_fName.equals("/"))) ) {
				m_fName=m_fName.substring(1,(m_fName.length()-1));
			}else {
				m_fName=m_fName.substring(0,(m_fName.length()-1));
			}
		}
		
		
		if(m_fTypename.equals(C_TYPE_FOLDER_NAME)){
			try {
				if( !(m_fName.equals("")) ) {
                    System.out.print("Importing: " + m_fName);
				    System.out.flush();
					CmsFolder newFolder = m_RB.createFolder(m_user,m_project,m_importPath, m_fName, h_fProperty);
					m_RB.lockResource(m_user, m_project,newFolder.getAbsolutePath(),true);
					m_RB.chown(m_user, m_project, newFolder.getAbsolutePath(), m_fUser);
					m_RB.chgrp(m_user, m_project, newFolder.getAbsolutePath(), m_fGroup);
					m_RB.chmod(m_user, m_project, newFolder.getAbsolutePath(), Integer.parseInt(m_fAccess));
					m_RB.unlockResource(m_user,m_project,newFolder.getAbsolutePath());
         		    System.out.println(" ok");
				}
			} catch (CmsException e) {
                System.out.println(" error: " + e.getMessage());
				m_errMsg.addElement(e.getMessage());
			}
			

		} else {
			String picimportPath=null;
			if(m_importPath.equals("/")) {
				picimportPath= m_fName.substring(0,m_fName.lastIndexOf("/")+1);
			}else {
				picimportPath= m_importPath+m_fName.substring(1,m_fName.lastIndexOf("/")+1);
			}
			m_fName=m_fName.substring((m_fName.lastIndexOf("/")+1),m_fName.length());
			try {
				System.out.print("Importing: " + m_fName);
				System.out.flush();
				CmsFile newFile = m_RB.createFile(m_user, m_project, picimportPath ,m_fName, fContent, m_fTypename, h_fProperty);
				m_RB.lockResource(m_user,m_project,newFile.getAbsolutePath(), true);
				m_RB.chown(m_user, m_project, newFile.getAbsolutePath(), m_fUser);
				m_RB.chgrp(m_user, m_project, newFile.getAbsolutePath(), m_fGroup);
				m_RB.chmod(m_user, m_project, newFile.getAbsolutePath(), Integer.parseInt(m_fAccess) );
				m_RB.unlockResource(m_user,m_project,newFile.getAbsolutePath());
       			System.out.println(" ok");
			} catch (CmsException e) {
				System.out.println(" error: " + e.getMessage());
				m_errMsg.addElement(e.getMessage());	
			}
		}		
	}
	//end readResource
	
	
	/**
	 * writePropertyDef
	 * get Propertydef and Propertyinfo
	 * the Propertydef must first be updated, before the resource can be written
	 *	
	 * @param property list with all Properties
	 * 
	 * @return a hashtable with Propertydefinition for given resource
	 */
	private Hashtable writePropertyDef(NodeList property, String s_rtype) {
	
		Hashtable h_property= new Hashtable();
		String s_key=new String();
		String s_type=new String();
		String s_value=new String();
		String help =null;
		
        
		int nll=property.getLength();
		int i;
		for(i=0; i<nll; i++) {
			help=property.item(i).getNodeName();
			
			if(help.equals(C_TFPROPERTYNAME)) {
				s_key=property.item(i).getFirstChild().getNodeValue();
			}
			if(help.equals(C_TFPROPERTYTYPE)) {
				s_type=property.item(i).getFirstChild().getNodeValue();
			}
			if(help.equals(C_TFPROPERTYVALUE)) {
				Node firstChild = property.item(i).getFirstChild();
                if(firstChild != null) {
                    s_value=firstChild.getNodeValue();
				    h_property.put(s_key,s_value);
				    try {
    					m_RB.createPropertydefinition(m_user,m_project, s_key, s_rtype, 0);
				    } catch (CmsException e) {
    					m_errMsg.addElement(e.getMessage());
                    }
                }
			}
			
		}
		return h_property;
	}
	//end writePropertyDef()
	
	
	/**
	 * readContent
	 * 
	 * if resource is a file then get file content
	 * get hexcontent and write binary
	 * 
	 * @param s_content the content string from the XML object
	 * 
	 */
	private byte[] readContent(String s_content) {
		
		Vector v_erg= new Vector();
		int sl=(s_content.length())-1;
		int i, code, erg;
		
		for(i=0; i<sl; i+=2) {
			char sh1=s_content.charAt(i);
			
			// is it a valig hex-char?
			while( ! isValidHexChar(sh1) ) {
				// no, get the next one!
				i++;
				sh1=s_content.charAt(i);
			}

			char sh0=s_content.charAt(i+1);
			
			// is it a valig hex-char?
			while( ! isValidHexChar(sh0) ) {
				// no, get the next one!
				i++;
				sh0=s_content.charAt(i+1);
			}
			
			code= decodeHex(sh1);
			erg=code*16;
			code= decodeHex(sh0);
			erg= (erg+code)-128;
			v_erg.addElement(Integer.toString(erg));
		}

		sl=v_erg.size();
		
		byte[] fContent=new byte[sl];
		
		for(i=0; i<sl; i++) {
			fContent[i]=(byte)Integer.parseInt( v_erg.elementAt(i).toString());	
		}
		return fContent;
		
	}
	// end readContent()
	
	
	
	/**
	 * decodeHex
	 * 
	 * @param hex char with hexadecimal sign
	 * 
	 * @return code integer value for hex sign
	 * 
	 */
	private int decodeHex (char hex) {
		int code;
		switch(hex){
				 case 'a':
				 case 'A': {
							   code=10;
							   break;
						   }
				 case 'b':
				 case 'B': {
							   code=11;
						       break;
					       }
			     case 'c':
			     case 'C': {
								code=12;
						       break;
					       }
				 case 'd':
				 case 'D': {
							   code=13;
							   break;
						   }
				 case 'e':
				 case 'E': {
							   code=14;
							   break;
						   }
				 case 'f':
				 case 'F': {
							   code=15;
							   break;
						   }
		default: {
					 code= Integer.parseInt(String.valueOf(hex));
					 break;
				 }
			 }
		return code;
	}
	// end decodeHex
	
	/**
	 * Checks, if this is a valid hex-char
	 * @param c The char to check.
	 * @return whether it is a valid char, or not.
	 */
	private boolean isValidHexChar(char c) {
		if( ( (c >= '0') && ( c <= '9') ) || 
			( (c >= 'a') && ( c <= 'f') ) || 
			( (c >= 'A') && ( c <= 'F') ) ) {
			return true;
		} else {
			return false;
		}
	}			
}
 //end class