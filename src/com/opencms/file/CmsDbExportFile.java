/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsDbExportFile.java,v $
 * Date   : $Date: 2000/02/15 17:43:59 $
 * Version: $Revision: 1.3 $
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
 * @version $Revision: 1.3 $ $Date: 2000/02/15 17:43:59 $
 */

class CmsDbExportFile implements I_CmsConstants {

	/** ResourceBroker to access all methods and objects */
	private I_CmsResourceBroker RB = null;
	/** User to access all resourcbroker methods and objects */
	private A_CmsUser user = null;
	/** Project to access all resourcbroker methods and objects */
	private A_CmsProject project = null;

	/** need to convert into XML format */
	private I_CmsXmlParser parser = null;
	/** need to initiate an XML object */
	private Document docXml = null;
	/** first element of an XML object(document node) need to insert other elements*/
	private Element firstElement = null;
	/** new XML element which is inserted in the XML first element */
	private Element newElement = null;
	/** need to navigate in the XML tree */
	private Element grandparentElement = null;
	/** need to navigate in the XML tree */
	private Element sectionElement = null;
	/** need to navigate in the XML tree */
	private Element parentElement = null;
	/** need to at values in the XML elements */
	private Node newNode = null;
	
	/** which folder to export absolute path*/
	private String resourcepath = null;
	/** which name to write in the XML file only relativ path */
	private String startFolder;
	// private String date;
	
	
	/**
	 * Constructor, creates a new CmsDBExportFile object.
	 * 
	 * @param eRB current ResourceBroker
	 * @param luser current user logged in
	 * @param lproject current project
	 * @param docXML XML object
	 * @param lpath, which resource (folder, files and subfolder) are to export,
	 *                   String with full path eg "/workplace/system/pic/"
	 * 
	 * @exception throws Exception
	 * 
	 */
	
	CmsDbExportFile(I_CmsResourceBroker eRB, A_CmsUser luser, A_CmsProject lproject, Document docXml, String lpath)
		throws Exception {
		RB=eRB;
		user=luser;
		project=lproject;
		this.docXml=docXml;
		
		resourcepath=lpath;
		if (resourcepath.equals("/")==false) {
			CmsFolder folder = RB.readFolder(user, project, resourcepath, "");
			startFolder=folder.getName();
		} else {
			startFolder="/";
		}
				 
	}
	

	/**
	 * initiate the file export
	 * 
	 * @return the filled XML object
	 * 
	 * @exception throws CmsException
	 * @exception throws Exception
	 * 
	 */	
	public Document export()
		throws CmsException, Exception {
			
		// get the documents node, first element in the XML object
		firstElement = docXml.getDocumentElement();
		newElement= docXml.createElement(C_TFILES);
		firstElement.appendChild(newElement);
		firstElement=newElement;
		
		// add all fils to the XML object
		fileExport(resourcepath, startFolder);
					
		return docXml;
	}
	// end export()
	
		
	/**
	 * exports all files
	 * 
	 * @param resourcepath which folder is to export absolute path
	 * @param parentName which folder is to export the relative name
	 * 
	 * @exception throws CmsException
	 * @exception throws Exception
	 * 
	 */
	private void fileExport(String resourcepath, String parentName)
		throws CmsException, Exception {
	
		newElement= docXml.createElement(C_TFILEOBJ);
		firstElement.appendChild(newElement);
		sectionElement=newElement;
		
		if(parentName.equals("/")==false) {
				parentName = parentName+"/";
			}
		
		generateFolderEntry(resourcepath, sectionElement, parentName);
		
		Vector filesinfolder = RB.getFilesInFolder(user, project, resourcepath);
		Enumeration fifenum=filesinfolder.elements();
		while (fifenum.hasMoreElements()) {
			CmsFile fif=(CmsFile)fifenum.nextElement();
			newElement= docXml.createElement(C_TFILEOBJ);
			firstElement.appendChild(newElement);
			sectionElement=newElement;
			generateFileEntry(fif, sectionElement, parentName);
		}
		
		Vector subfolders = RB.getSubFolders(user, project, resourcepath);
		Enumeration sfenum=subfolders.elements();
		while (sfenum.hasMoreElements()) {
			CmsFolder sf=(CmsFolder)sfenum.nextElement();
			fileExport(sf.getAbsolutePath(), parentName+sf.getName());
		}
		
	}
	// end fileExport()
	

	
	/**
	 * generates the folder entry for the XML object
	 * 
	 * @param path which folder is to export absolute path
	 * @param parent an element of the XML object
	 * @param parentName which folder is to export the relative name
	 * 
	 * @exception throws CmsException
	 * 
	 */
	private void generateFolderEntry(String path, Element parent, String parentName)
		throws CmsException {
		
		CmsFolder folder = RB.readFolder(user, project, path, "");
			
			newElement= docXml.createElement(C_TFNAME);
			parent.appendChild(newElement);
			newNode = docXml.createTextNode(parentName);
			newElement.appendChild(newNode);
			
			newElement= docXml.createElement(C_TFTYPE);
			parent.appendChild(newElement);
			newNode = docXml.createTextNode(String.valueOf(folder.getType()));
			newElement.appendChild(newNode);
			
			A_CmsResourceType typeHelp=RB.getResourceType(user, project, folder.getType());
			newElement= docXml.createElement(C_TFTYPENAME);
			parent.appendChild(newElement);
			newNode = docXml.createTextNode(typeHelp.getResourceName());
			newElement.appendChild(newNode);
		
			/** read and write metainfo */
			newElement=docXml.createElement(C_TFMETAINFO);
			parent.appendChild(newElement);
			generateMetaInfo(folder.getAbsolutePath(), newElement, typeHelp.getResourceName());
			
	}
	// end generateFolderEntry()
	
	
	/**
	 * generates the file entry for the XML object
	 * 
	 * @param fif which file is to export absolute path
	 * @param parent an element of the XML object
	 * @param parentName which folder is to export the relative name
	 * 
	 * @exception throws CmsException
	 * @exception throws Exception
	 * 
	 */
	private void generateFileEntry(CmsFile fif, Element parent, String parentName)
		throws CmsException, NumberFormatException {
		
			newElement= docXml.createElement(C_TFNAME);
			parent.appendChild(newElement);
			newNode = docXml.createTextNode(parentName+fif.getName());
			newElement.appendChild(newNode);

			newElement= docXml.createElement(C_TFTYPE);
			parent.appendChild(newElement);
			newNode = docXml.createTextNode(String.valueOf(fif.getType()));
			newElement.appendChild(newNode);
			
			A_CmsResourceType typeHelp=RB.getResourceType(user, project, fif.getType());
			newElement= docXml.createElement(C_TFTYPENAME);
			parent.appendChild(newElement);
			newNode = docXml.createTextNode(typeHelp.getResourceName());
			newElement.appendChild(newNode);
			
			// read and write metainfo
			newElement=docXml.createElement(C_TFMETAINFO);
			parent.appendChild(newElement);
			generateMetaInfo(fif.getAbsolutePath(), newElement, typeHelp.getResourceName());
			
			// reads and writes the file content
			CmsFile file= RB.readFile(user, project, fif.getAbsolutePath());
			byte[] fcontent = file.getContents();
			int value;
			int i=0;
			int l=fcontent.length;
			StringBuffer content=new StringBuffer();
			
			for(i=0; i<l; i++) {
				value=Integer.parseInt(String.valueOf(fcontent[i]));
				value+=128;
				String shelp = Integer.toHexString(value);
				if(shelp.length()==1) {
					shelp="0"+shelp;
				}
				
				content.append(shelp);
			}
			
			newElement=docXml.createElement(C_FCONTENT);
			parent.appendChild(newElement);
			newNode=docXml.createTextNode(new String(content));
			newElement.appendChild(newNode);
			
	}
	// end generateFileEntry
	
	
	/**
	 * method to get group name for folder or file
	 * 
	 * @param group id DB id for the group
	 * @returns string with group name
	 */
	private String getGroupName(int groupId)
		throws CmsException {
		
			Vector groups=RB.getGroups(user,project);
				if(groupId!=(-1)) {
					Enumeration genum=groups.elements();
					while (genum.hasMoreElements()) {
						A_CmsGroup pg=(A_CmsGroup)genum.nextElement();
						if(pg.getId()==groupId) {
							return pg.getName();
						}
					}
				} 
				return "none";
	}

	/**
	 * method to get user name for folder or file
	 * 
	 * @param user id DB id for the group
	 * 
	 * @returns string with user name
	 * 
	 * @exception throws CmsException
	 * 
	 */
	private String getUserName(int userId)
		throws CmsException {
		
			Vector users=RB.getUsers(user,project);
				if(userId!=(-1)) {
					Enumeration uenum=users.elements();
					while (uenum.hasMoreElements()) {
						A_CmsUser pg=(A_CmsUser)uenum.nextElement();
						if(pg.getId()==userId) {
							return pg.getName();
						}
					}
				} 
				return "none";
	}

	/**
	 * method to format long in to String with datevalue
	 * 
	 * @param user id DB id for the group
	 * 
	 * @returns string with formated date "fullyear-month-day hour:minutes:seconds"
	 */
	private String LongToDateString(long ld) {

		Date dd = new Date(ld);
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(dd);
		int y, m, d, h, min, sec;
		String sy, sm, sd, sh, smin, ssec;
		String sdate;
		
		// format date
		y=gc.get(Calendar.YEAR);
		m=gc.get(Calendar.MONTH)+1;
		d=gc.get(Calendar.DAY_OF_MONTH);
		sy=String.valueOf(y);
		if (m<10) {
			sm='0'+String.valueOf(m);
		} else {
			sm=String.valueOf(m);
		}
		if (d<10) {
			sd='0'+String.valueOf(d);
		} else {
			sd=String.valueOf(d);
		}
		
		// format time
		h=gc.get(Calendar.HOUR_OF_DAY);
		min=gc.get(Calendar.MINUTE);
		sec=gc.get(Calendar.SECOND);
		
		if(h<10) {
			sh='0'+String.valueOf(h);
		} else {
			sh=String.valueOf(h);
		}
		if(min<10) {
			smin='0'+String.valueOf(min);
		} else {
			smin=String.valueOf(min);
		}
		if(sec<10) {
			ssec='0'+String.valueOf(sec);
		} else {
			ssec=String.valueOf(sec);
		}
		
		sdate=sy+'-'+sm+'-'+sd+' '+sh+':'+smin+':'+ssec;
		return sdate;
	}
	// end LongtoDateString

	/**
	 * method to read and write metainfo to XML object
	 * 
	 * @param user id DB id for the group
	 *
	 * @returns string with formated date "fullyear-month-day hour:minutes:seconds"
	 * 
	 */
	private void generateMetaInfo(String path, Element metaparent, String rtype)
		throws CmsException {
		
		
		Hashtable metadef=RB.readAllMetainformations(user, project, path);
		Enumeration metadefenum= metadef.elements();
		Enumeration metadefkey= metadef.keys();
		while (metadefenum.hasMoreElements()) {
			
			String metainfo= (String)metadefenum.nextElement();
			String metakey= (String)metadefkey.nextElement();
			
			newElement= docXml.createElement(C_TFMETANAME);
			metaparent.appendChild(newElement);
			newNode = docXml.createTextNode(metakey);
			newElement.appendChild(newNode);
			
			A_CmsMetadefinition mtype= RB.readMetadefinition(user, project, metakey, rtype);
			
			newElement= docXml.createElement(C_TFMETATYPE);
			metaparent.appendChild(newElement);
			newNode = docXml.createTextNode(String.valueOf(mtype.getType()));
			newElement.appendChild(newNode);
			
			newElement= docXml.createElement(C_TFMETAVALUE);
			metaparent.appendChild(newElement);
			newNode = docXml.createTextNode(metainfo);
			newElement.appendChild(newNode);
		}
		
	}

}



