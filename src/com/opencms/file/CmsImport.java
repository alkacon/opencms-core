package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsImport.java,v $
 * Date   : $Date: 2000/10/24 10:38:47 $
 * Version: $Revision: 1.27 $
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

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.lang.reflect.*;
import java.security.*;
import com.opencms.core.*;
import com.opencms.template.*;
import org.w3c.dom.*;
import source.org.apache.java.util.*;

/**
 * This class holds the functionaility to import resources from the filesystem 
 * into the cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.27 $ $Date: 2000/10/24 10:38:47 $
 */
public class CmsImport implements I_CmsConstants {

	/**
	 * The algorithm for the message digest
	 */
	public static final String C_IMPORT_DIGEST="MD5";
	
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
	private CmsObject m_cms;
	
	/**
	 * The xml manifest-file.
	 */
	private Document m_docXml;

	/**
	 * Digest for taking a fingerprint of the files
	 */
	private MessageDigest m_digest = null;

	/**
	 * This constructs a new CmsImport-object which imports the resources.
	 * 
	 * @param importFile the file or folder to import from.
	 * @param importPath the path to the cms to import into.
	 * @exception CmsException the CmsException is thrown if something goes wrong.
	 */
	public CmsImport(String importFile, String importPath, CmsObject cms) 
		throws CmsException {

		m_importFile = importFile;
		m_importPath = importPath;
		m_cms = cms;
 
		// create the digest
		createDigest();
		
		// open the import resource
		getImportResource();

		// read the xml-config file
		getXmlConfigFile(); 
	}
/**
 * Read infos from the properties and create a MessageDigest
 * Creation date: (29.08.00 15:45:35)
 */
private void createDigest() throws CmsException {
	Configurations config = m_cms.getConfigurations();
	 
	String digest = C_IMPORT_DIGEST;
	// create the digest
	try {
		m_digest = MessageDigest.getInstance(digest); 
	} catch (NoSuchAlgorithmException e) {
		throw new CmsException("Could'nt create MessageDigest with algorithm " + digest);
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
 * Returns a list of files which are both in the import and in the virtual file system
 * Creation date: (24.08.00 16:18:23)
 * @return java.util.Vector of Strings, complete path of the files
 */
public Vector getConflictingFilenames() throws CmsException {
	NodeList fileNodes;
	Element currentElement, currentProperty;
	String source, destination, path;
	Vector conflictNames = new Vector();
	try {
		// get all file-nodes
		fileNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_FILE);

		// walk through all files in manifest
		for (int i = 0; i < fileNodes.getLength(); i++) {
			currentElement = (Element) fileNodes.item(i);
			source = getTextNodeValue(currentElement, C_EXPORT_TAG_SOURCE);
			destination = getTextNodeValue(currentElement, C_EXPORT_TAG_DESTINATION);
			path = m_importPath + destination;
			if (source != null) {
				// only consider files
				boolean exists = true; 
				try {
					CmsResource res=m_cms.readFileHeader(m_importPath + destination); 
					if (res.getState()==C_STATE_DELETED) {
						exists=false;
					}
				} catch (CmsException e) { 
					exists = false;
				}
				if (exists) {
					conflictNames.addElement(m_importPath + destination);
				}
			}
		} 
	} catch (Exception exc) {
		throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
	}
	try {
		m_importZip.close();
	} catch(IOException exc) {
		throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
	}		
	return conflictNames;
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
 * This method returns the resource-names that are needed to create a project for this import.
 * It calls the method getConflictingFileNames if needed, to calculate these resources.
 */	
public Vector getResourcesForProject() throws CmsException {
	NodeList fileNodes;
	Element currentElement, currentProperty;
	String source, destination, path;
	Vector resources = new Vector();
	try {
		// get all file-nodes
		fileNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_FILE);

		// walk through all files in manifest
		for (int i = 0; i < fileNodes.getLength(); i++) {
			currentElement = (Element) fileNodes.item(i);
			source = getTextNodeValue(currentElement, C_EXPORT_TAG_SOURCE);
			destination = getTextNodeValue(currentElement, C_EXPORT_TAG_DESTINATION);
			path = m_importPath + destination;

			// get the resources for a project
			try {
				String resource = destination.substring(0, destination.indexOf("/",1) + 1);
				resource = m_importPath + resource;
				if((!resources.contains(resource)) && (!resource.equals(m_importPath))) {
					// add the resource, if it dosen't already exist
					resources.addElement(resource);
				}
			} catch(StringIndexOutOfBoundsException exc) {
				// this is a resource in root-folder: ignore the excpetion
			}
		} 
	} catch (Exception exc) {
		throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
	}
	try {
		m_importZip.close();
	} catch(IOException exc) {
		throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
	}	
	return resources;
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
	 * Gets the xml-config file from the import resource and stores it in object-member.
	 * Checks whether the import is from a module file
	 */
	private void getXmlConfigFile() 
		throws CmsException {
		
		try {
		 	BufferedReader xmlReader = getFileReader(C_EXPORT_XMLFILENAME);
  			m_docXml = A_CmsXmlContent.getXmlParser().parse(xmlReader);
 			xmlReader.close();
		 } catch(Exception exc) {
	   
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		} 
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
 * @param writtenFilenames filenames of the files and folder which have actually been successfully written
 *       not used when null
 * @param fileCodes code of the written files (for the registry) 
 *       not used when null
 */
private void importFile(String source, String destination, String type, String user, String group, String access, Hashtable properties, Vector writtenFilenames, Vector fileCodes) {
	// print out the information for shell-users
	System.out.print("Importing ");
	System.out.print(source + ", ");
	System.out.print(destination + ", ");
	System.out.print(type + ", ");
	System.out.print(user + ", ");
	System.out.print(group + ", ");
	System.out.print(access + "... ");
	boolean success = false;
	byte[] content = null;
	String fullname = null;
	try {
		String path = m_importPath + destination.substring(0, destination.lastIndexOf("/") + 1);
		String name = destination.substring((destination.lastIndexOf("/") + 1), destination.length());
		int state = C_STATE_NEW;
		if (source == null) {
			// this is a directory
			try {
				CmsFolder cmsfolder = m_cms.createFolder(path, name, properties);
				fullname = cmsfolder.getAbsolutePath();
				m_cms.lockResource(path + name + "/", true);
				success = true;
				state = C_STATE_NEW;
			} catch (CmsException e) {
				// an exception is thrown if the folder already exists
				state = C_STATE_CHANGED;
			}
		} else {
			// this is a file
			// first delete the file, so it can be overwritten
			try {
				m_cms.lockResource(path + name, true);
				m_cms.deleteFile(path + name);
				state = C_STATE_CHANGED;
			} catch (CmsException exc) {
				state = C_STATE_NEW;
				// ignore the exception, the file dosen't exist
			}
			// now create the file
			content = getFileBytes(source);
			fullname = m_cms.createFile(path, name, content, type, properties).getAbsolutePath();
			m_cms.lockResource(path + name, true);
			success = true;
		}
		if (fullname != null) {
			m_cms.chmod(fullname, Integer.parseInt(access));
			m_cms.chgrp(fullname, group);
			m_cms.chown(fullname, user);
			// for debugging: check whether the kernel and this method determine the same status
			int kernelstate = m_cms.readFileHeader(fullname).getState();
			if ((state != kernelstate) && A_OpenCms.isLogging()) {
				A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_DEBUG, "CmsImport: different statuses, kernel sais " + kernelstate + " import sais " + state);
			}
			m_cms.chstate(fullname,state);
		}
		System.out.println("OK");
	} catch (Exception exc) {
		// an error while importing the file
		success = false;
		System.out.println("Error");
		exc.printStackTrace();
	}
	byte[] digestContent = {0};
	if (content != null) {
		digestContent = m_digest.digest(content);
	}
	if (success) {
		if (writtenFilenames != null) {
			writtenFilenames.addElement(fullname);
		}
		if (fileCodes != null) {
			fileCodes.addElement(new String(digestContent));
		}
	}
}
/**
 * Imports the resources and writes them to the cms even if there already exist conflicting files
 */
public void importResources() throws CmsException {
	importResources(null, null, null, null, null);
}
/**
 * Imports the resources and writes them to the cms.
 * param excludeList filenames of files and folders which should not be (over) written in the virtual file system
 * param writtenFilenames filenames of the files and folder which have actually been successfully written
 *       not used when null
 * param fileCodes code of the written files (for the registry) 
 *       not used when null
 * param propertyName name of a property to be added to all resources
 * param propertyValue value of that property
 */
public void importResources(Vector excludeList, Vector writtenFilenames, Vector fileCodes, String propertyName, String propertyValue) throws CmsException {
	NodeList fileNodes, propertyNodes;
	Element currentElement, currentProperty;
	String source, destination, type, user, group, access;
	Hashtable properties;
	Vector types = new Vector(); // stores the file types for which the property already exists

	// first lock the resource to import
	// m_cms.lockResource(m_importPath);
	try {
		// get all file-nodes
		fileNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_FILE);

		// walk through all files in manifest
		for (int i = 0; i < fileNodes.getLength(); i++) {
			currentElement = (Element) fileNodes.item(i);

			// get all information for a file-import
			source = getTextNodeValue(currentElement, C_EXPORT_TAG_SOURCE);
			destination = getTextNodeValue(currentElement, C_EXPORT_TAG_DESTINATION);
			type = getTextNodeValue(currentElement, C_EXPORT_TAG_TYPE);
			user = getTextNodeValue(currentElement, C_EXPORT_TAG_USER);
			group = getTextNodeValue(currentElement, C_EXPORT_TAG_GROUP);
			access = getTextNodeValue(currentElement, C_EXPORT_TAG_ACCESS);
			if (!inExcludeList(excludeList, m_importPath + destination)) {

				// get all properties for this file
				propertyNodes = currentElement.getElementsByTagName(C_EXPORT_TAG_PROPERTY);
				// clear all stores for property information
				properties = new Hashtable();
				// add the module property to properties
				if (propertyName != null && propertyValue != null && !"".equals(propertyName)) {
					if (!types.contains(type)) {
						types.addElement(type);
						createPropertydefinition(propertyName, "" + C_PROPERTYDEF_TYPE_OPTIONAL, type);
					}
					properties.put(propertyName, propertyValue);
				}
				// walk through all properties
				for (int j = 0; j < propertyNodes.getLength(); j++) {
					currentProperty = (Element) propertyNodes.item(j);
					// get all information for this property
					String name = getTextNodeValue(currentProperty, C_EXPORT_TAG_NAME);
					String propertyType = getTextNodeValue(currentProperty, C_EXPORT_TAG_TYPE);
					String value = getTextNodeValue(currentProperty, C_EXPORT_TAG_VALUE);
					// store these informations
					if ((name != null) && (value != null)) {
						properties.put(name, value);
						createPropertydefinition(name, propertyType, type);
					}
				}

				// import the specified file and write maybe put it on the lists writtenFilenames,fileCodes
				importFile(source, destination, type, user, group, access, properties, writtenFilenames, fileCodes);
			} else {
				System.out.print("skipping " + destination);
			}
		}
	} catch (Exception exc) {
		throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
	}
	try {
		m_importZip.close();
	} catch (IOException exc) {
		throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
	}
}
/**
 * Checks whether the path is on the list of files which are excluded from the import
 *  
 * @return boolean true if path is on the excludeList
 * @param excludeList list of pathnames which should not be (over) written
 * @param path a complete path of a resource
 */
private boolean inExcludeList(Vector excludeList, String path) {
	boolean onList = false;
	if (excludeList == null) {
		return onList;
	}
	int i=0;
	while (!onList && i<excludeList.size()) {
		onList = (path.equals(excludeList.elementAt(i)));
		i++;
	} 
	return onList;
}
}
