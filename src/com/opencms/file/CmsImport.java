/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsImport.java,v $
 * Date   : $Date: 2000/05/18 12:37:41 $
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

package com.opencms.file;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import com.opencms.core.*;
import com.opencms.template.*;
import org.w3c.dom.*;

/**
 * This class holds the functionaility to import resources from the filesystem 
 * into the cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 2000/05/18 12:37:41 $
 */
class CmsImport implements I_CmsImportExport, I_CmsConstants {
	
	/**
	 * The import-file to load resources from
	 */
	private String m_importFile;
	
	/**
	 * The import-resource (folder) to load resources from
	 */
	private File m_importResource = null;
	
	/**
	 * The import-resource (zip) to load resources from
	 */
	private ZipFile m_importZip = null;
	
	/**
	 * The import-path to write resources into the cms.
	 */
	private String m_importPath;
	
	/**
	 * The cms-object to do the operations.
	 */
	private A_CmsObject m_cms;
	
	/**
	 * The xml manifest-file.
	 */
	private Document m_docXml;
	
	/**
	 * This constructs a new CmsImport-object which imports the resources.
	 * 
	 * @param importFile the file or folder to import from.
	 * @param importPath the path to the cms to import into.
	 * @exception CmsException the CmsException is thrown if something goes wrong.
	 */
	CmsImport(String importFile, String importPath, A_CmsObject cms) 
		throws CmsException {

		m_importFile = importFile;
		m_importPath = importPath;
		m_cms = cms;

		// open the import resource
		getImportResource();
		
		// read the xml-config file
		getXmlConfigFile();
		
		// import the resources
		importResources();
	}
	
	/**
	 * Gets the import resource and stores it in object-member.
	 */
	private void getImportResource() 
		throws CmsException {
		try {
			// get the import resource
			m_importResource = new File(m_importFile);
			
			// if it is a file it must be a zip-file
			if(m_importResource.isFile()) {
				m_importZip = new ZipFile(m_importResource);
			}
		} catch(Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
	}
	
	/**
	 * Gets the xml-config file from the import resource and stroes it in object-member.
	 */
	private void getXmlConfigFile() 
		throws CmsException {
		
		try {
			
			BufferedReader xmlReader = getFileReader(C_XMLFILENAME);
		
			m_docXml = A_CmsXmlContent.getXmlParser().parse(xmlReader);
			
			xmlReader.close();

		} catch(Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
	}
	
	/**
	 * Imports the resources and writes them to the cms.
	 */
	private void importResources() 
		throws CmsException {
		
		NodeList fileNodes, propertyNodes;
		Element currentElement, currentProperty;
		String source, destination, type, user, group, access;
		Hashtable properties;
		
		// first lock the resource to import
		m_cms.lockResource(m_importPath);
		
		try {
			// get all file-nodes
			fileNodes = m_docXml.getElementsByTagName(C_TAG_FILE);
			
			// walk through all files in manifest
			for(int i = 0; i < fileNodes.getLength(); i++) {
				currentElement = (Element) fileNodes.item(i);
				
				// get all informations for a file-import
				source = getTextNodeValue(currentElement, C_TAG_SOURCE);
				destination = getTextNodeValue(currentElement, C_TAG_DESTINATION);
				type = getTextNodeValue(currentElement, C_TAG_TYPE);
				user = getTextNodeValue(currentElement, C_TAG_USER);
				group = getTextNodeValue(currentElement, C_TAG_GROUP);
				access = getTextNodeValue(currentElement, C_TAG_ACCESS);
				
				// get all properties for this file
				propertyNodes = currentElement.getElementsByTagName(C_TAG_PROPERTY);
				// clear all stores for propertyinformations
				properties = new Hashtable();
				// walk through all properties
				for(int j = 0; j < propertyNodes.getLength(); j++) {
					currentProperty = (Element) propertyNodes.item(j);
					// get all information for this property
					String name = getTextNodeValue(currentProperty, C_TAG_NAME);
					String propertyType = getTextNodeValue(currentProperty, C_TAG_TYPE);
					String value = getTextNodeValue(currentProperty, C_TAG_VALUE);
					// store these informations
					properties.put(name, value);
					createPropertydefinition(name, propertyType, type);
				}
				
				// import the specified file
				importFile(source, destination, type, user, group, access, properties);
			}
			
		} catch(Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}		
		// all is done, unlock the resource
		m_cms.unlockResource(m_importPath);
	}

	/**
	 * Imports a file into the cms.
	 * @param source the path to the source-file
	 * @param destination the path to the destination-file in the cms
	 * @param type the resource-type of the file
	 * @param user the owner of the file
	 * @param group the group of the file
	 * @param access the access-flags of the file
	 * @param properties a hashtable with properties for this resource
	 */
	private void importFile(String source, String destination, String type, String user, String group, String access, Hashtable properties) {
		// print out the information for shell-users
		System.out.print("Importing ");
		System.out.print(source + ", ");	
		System.out.print(destination + ", ");	
		System.out.print(type + ", ");	
		System.out.print(user + ", ");	
		System.out.print(group + ", ");	
		System.out.print(access + "... ");
		
		try {			
			String path = m_importPath + destination.substring(0,destination.lastIndexOf("/")+1);
			String name = destination.substring((destination.lastIndexOf("/")+1),destination.length());
			String fullname;
			
			if(source == null) {
				// this is a directory
				fullname = m_cms.createFolder(path, name, properties).getAbsolutePath();
			} else {
				// this is a file
				// first delete the file, so it can be overwritten
				try {
					m_cms.deleteFile(path + name);
				} catch(CmsException exc) {
					// ignore the exception, the file dosen't exist
				}
				// now create the file
				fullname = m_cms.createFile(path, name, getFileBytes(source), type, properties).getAbsolutePath();
			}
			// lock the new resource
			m_cms.lockResource(fullname);
			m_cms.chmod(fullname, Integer.parseInt(access));
			m_cms.chgrp(fullname, group);
			m_cms.chown(fullname, user);
			System.out.println("OK");
		} catch(Exception exc) {
			// an error while importing the file
			System.out.println("Error");
			exc.printStackTrace();
		}		
	}	
	
	/**
	 * Creates missing property definitions if needed.
	 * 
	 * @param name the name of the property.
	 * @param propertyType the type of the property.
	 * @param resourceType the type of the resource.
	 * 
	 * @exception throws CmsException if something goes wrong.
	 */
	private void createPropertydefinition(String name, String propertyType, String resourceType) 
		throws CmsException {
		
		// does the propertydefinition exists already?
		try {
			m_cms.readPropertydefinition(name, resourceType);
		} catch(CmsException exc) {
			// no: create it
			m_cms.createPropertydefinition(name, resourceType, Integer.parseInt(propertyType));
		}
	}
	
	/**
	 * Returns the text for this node.
	 * 
	 * @param elem the parent-element.
	 * @param tag the tagname to get the value from.
	 * @return the value of the tag.
	 */
	private String getTextNodeValue(Element elem, String tag) {
		try {
			return elem.getElementsByTagName(tag).item(0).getFirstChild().getNodeValue();
		} catch(Exception exc) {
			// ignore the exception and return null
			return null;
		}
	}

	/**
	 * Returns a buffered reader for this resource using the importFile as root.
	 * 
	 * @param filename The name of the file to read.
	 * @return BufferedReader The filereader for this file.
	 */
	private BufferedReader getFileReader(String filename) 
		throws Exception{
		// is this a zip-file?
		if(m_importZip != null) {
			// yes
			ZipEntry entry = m_importZip.getEntry(filename);
			InputStream stream = m_importZip.getInputStream(entry);
			return new BufferedReader( new InputStreamReader(stream));
		} else {
			// no - use directory
			File xmlFile = new File(m_importResource, filename);
			return new BufferedReader(new FileReader(xmlFile));
		}
	}

	/**
	 * Returns a byte-array containing the content of the file.
	 * 
	 * @param filename The name of the file to read.
	 * @return bytes[] The content of the file.
	 */
	private byte[] getFileBytes(String filename) 
		throws Exception{
		// is this a zip-file?
		if(m_importZip != null) {
			// yes
			ZipEntry entry = m_importZip.getEntry(filename);
			InputStream stream = m_importZip.getInputStream(entry);
			
			int charsRead = 0;
			int size = new Long(entry.getSize()).intValue();
			byte[] buffer = new byte[size];
			while(charsRead < size) {
				charsRead += stream.read(buffer, charsRead, size - charsRead);
			}
			stream.close();
			return buffer;			
		} else {
			// no - use directory
			File file = new File(m_importResource, filename);
			FileInputStream fileStream = new FileInputStream(file);

			int charsRead = 0;
			int size = new Long(file.length()).intValue();
			byte[] buffer = new byte[size];
			while(charsRead < size) {
				charsRead += fileStream.read(buffer, charsRead, size - charsRead);
			}
			fileStream.close();
			return buffer;
		}
	}
}