package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsExport.java,v $
 * Date   : $Date: 2000/11/02 16:04:58 $
 * Version: $Revision: 1.10 $
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
import com.opencms.core.*;
import com.opencms.template.*;
import org.w3c.dom.*;

import com.opencms.util.*;

/**
 * This class holds the functionaility to export resources from the cms 
 * to the filesystem.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.10 $ $Date: 2000/11/02 16:04:58 $
 */
public class CmsExport implements I_CmsConstants {
	
	/**
	 * The export-zipfile to store resources to
	 */
	private String m_exportFile;
	
	/**
	 * The export-stream (zip) to store resources to
	 */
	private ZipOutputStream m_exportZipStream = null;
	
	/**
	 * The export-path to read resources from the cms.
	 */
	private String m_exportPath;
	
	/**
	 * The cms-object to do the operations.
	 */
	private CmsObject m_cms;
	
	/**
	 * The xml manifest-file.
	 */
	private Document m_docXml;
	
	/**
	 * The xml-elemtent to store fileinformations to.
	 */
	private Element m_filesElement;
	
	/**
	 * Decides, if the system should be included to the export.
	 */
	private boolean m_excludeSystem;
	
	/**
	 * This constructs a new CmsImport-object which imports the resources.
	 * 
	 * @param importFile the file or folder to import from.
	 * @param importPath the path to the cms to import into.
	 * @param cms the cms-object to work with.
	 * @exception CmsException the CmsException is thrown if something goes wrong.
	 */
	public CmsExport(String exportFile, String[] exportPaths, CmsObject cms) 
		throws CmsException {
		this(exportFile, exportPaths, cms, false);
	}
	/**
	 * This constructs a new CmsImport-object which imports the resources.
	 * 
	 * @param importFile the file or folder to import from.
	 * @param importPath the path to the cms to import into.
	 * @param cms the cms-object to work with.
	 * @param Node moduleNode module informations in a Node for module-export.
	 * @exception CmsException the CmsException is thrown if something goes wrong.
	 */
	public CmsExport(String exportFile, String[] exportPaths, CmsObject cms, Node moduleNode) 
		throws CmsException {
		this(exportFile, exportPaths, cms, false, moduleNode);
	}
/**
 * This constructs a new CmsImport-object which imports the resources.
 * 
 * @param importFile the file or folder to import from.
 * @param exportPaths the paths of folders and files to write into the exportFile
 * @param cms the cms-object to work with.
 * @param excludeSystem if true, the system folder is excluded, if false exactly the resources in
 *        exportPaths are included
 * @exception CmsException the CmsException is thrown if something goes wrong.
 */
public CmsExport(String exportFile, String[] exportPaths, CmsObject cms, boolean excludeSystem) throws CmsException {
	this(exportFile, exportPaths, cms, excludeSystem, null);
}
	/**
	 * This constructs a new CmsImport-object which imports the resources.
	 * 
	 * @param importFile the file or folder to import from.
	 * @param exportPaths the paths of folders and files to write into the exportFile
	 * @param cms the cms-object to work with.
	 * @param excludeSystem if true, the system folder is excluded, if false exactly the resources in
	 *        exportPaths are included
	 * @param Node moduleNode module informations in a Node for module-export.
	 * @exception CmsException the CmsException is thrown if something goes wrong.
	 */
	public CmsExport(String exportFile, String[] exportPaths, CmsObject cms, boolean excludeSystem, Node moduleNode) 
		throws CmsException {
		
		m_exportFile = exportFile; 
		m_cms = cms;
		m_excludeSystem = excludeSystem;

		Vector folderNames = new Vector();
		Vector fileNames = new Vector();
		for (int i=0; i<exportPaths.length; i++) { 
			if (exportPaths[i].endsWith(C_ROOT)) {
				folderNames.addElement(exportPaths[i]);
			} else {
				fileNames.addElement(exportPaths[i]);
			}
		} 
		
		// open the import resource
		getExportResource();
		
		// create the xml-config file
		getXmlConfigFile(moduleNode);
		
		// remove the possible redundancies in the list of paths
		checkRedundancies(folderNames, fileNames);
		
		// export the folders
		for (int i=0; i<folderNames.size(); i++) {
			String path = (String) folderNames.elementAt(i);
			// first add superfolders to the xml-config file
			addSuperFolders(path);
			exportResources(path);
		}

		// export the single files
		addSingleFiles(fileNames);
		
		// write the document to the zip-file
		writeXmlConfigFile( );
		
		try {
			m_exportZipStream.close();
		} catch(IOException exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
	}
	/**
	 * Adds a element to the xml-document.
	 * @param element The element to add the subelement to.
	 * @param name The name of the new subelement.
	 * @param value The value of the element.
	 */
	private void addCdataElement(Element element, String name, String value) {
		Element newElement = m_docXml.createElement(name);
		element.appendChild(newElement);
		CDATASection text = m_docXml.createCDATASection(value);
		newElement.appendChild(text);
	}
	/**
	 * Adds a element to the xml-document.
	 * @param element The element to add the subelement to.
	 * @param name The name of the new subelement.
	 * @param value The value of the element.
	 */
	private void addElement(Element element, String name, String value) {
		Element newElement = m_docXml.createElement(name);
		element.appendChild(newElement);
		Text text = m_docXml.createTextNode(value);
		newElement.appendChild(text);
	}
/**
 * adds all files with names in fileNames to the xml-config file
 * Creation date: (10.08.00 17:12:05)
 * @param fileNames java.util.Vector of Strings, e.g. /folder/index.html
 */
public void addSingleFiles(Vector fileNames) throws CmsException {
	if (fileNames != null) {
		for (int i = 0; i < fileNames.size(); i++) {
			CmsFile file = m_cms.readFile((String) fileNames.elementAt(i));
			exportFile(file);
		}
	}
}
/**
 * Adds the superfolders of path to the config file, starting at the top, excluding the root folder
 * Creation date: (10.08.00 11:07:58)
 * @param path java.lang.String the path of the folder in the filesystem
 */
public void addSuperFolders(String path) throws CmsException {
	Vector superFolders = new Vector();
	while (path.length() > C_ROOT.length()) {
		superFolders.addElement(path);
		path = path.substring(0, path.length() - 1);
		path = path.substring(0, path.lastIndexOf(C_ROOT)+1);
	}
	for (int i = superFolders.size()-1; i >= 0; i--) { 
		CmsFolder folder = m_cms.readFolder((String) superFolders.elementAt(i));
		writeXmlEntrys(folder);
	}
}
/** Check whether some of the resources are redundant because a superfolder has also
  *  been selected or a file is included in a folder and change the parameter Vectors
  *  
  * @param folderNames contains the full pathnames of all folders
  * @param fileNames contains the full pathnames of all files 
  */

private void checkRedundancies(Vector folderNames, Vector fileNames) {
	int i, j;
	if (folderNames == null) {
		return;
	}
	Vector redundant = new Vector();
	int n = folderNames.size(); 
	if (n > 1) {
		// otherwise no check needed, because there is only one resource

		for (i = 0; i < n; i++) {
			redundant.addElement(new Boolean(false));
		}
		for (i = 0; i < n - 1; i++) {
			for (j = i + 1; j < n; j++) {
				if (((String) folderNames.elementAt(i)).length() < ((String) folderNames.elementAt(j)).length()) {
					if (((String) folderNames.elementAt(j)).startsWith((String) folderNames.elementAt(i))) {
						redundant.setElementAt(new Boolean(true), j);
					}
				} else {
					if (((String) folderNames.elementAt(i)).startsWith((String) folderNames.elementAt(j))) {
						redundant.setElementAt(new Boolean(true), i);
					}
				}
			}
		}
		for (i = n - 1; i >= 0; i--) {
			if (((Boolean) redundant.elementAt(i)).booleanValue()) {
				folderNames.removeElementAt(i);
			}
		}
	}
	// now remove the files who are included automatically in a folder
	// otherwise there would be a zip exception
 
	for (i = fileNames.size() - 1; i >= 0; i--) {
		for (j = 0; j < folderNames.size(); j++) {
			if (((String) fileNames.elementAt(i)).startsWith((String) folderNames.elementAt(j))) {
				fileNames.removeElementAt(i);
			}
		}
	}
}
	/**
	 * Exports one single file with all its data and content.
	 * 
	 * @param file the file to be exported,
	 * @exception throws a CmsException if something goes wrong.
	 */
	private void exportFile(CmsFile file) 
		throws CmsException {
		String source = getSourceFilename(file.getAbsolutePath());
		
		System.out.print("Exporting " + source + " ...");
		
		try {
			// create the manifest-entrys
			writeXmlEntrys((CmsResource) file);
			// store content in zip-file
			ZipEntry entry = new ZipEntry(source);
			m_exportZipStream.putNextEntry(entry);
			m_exportZipStream.write(file.getContents());
			m_exportZipStream.closeEntry();
		} catch(Exception exc) {
			System.out.println("Error");
			exc.printStackTrace();
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
		
		System.out.println("OK");
	}
	/**
	 * Exports all needed sub-resources to the zip-file.
	 * 
	 * @param path to complete path to the resource to export
	 * @exception throws CmsException if something goes wrong.
	 */
	private void exportResources(String path) 
		throws CmsException { 
		// get all subFolders
		Vector subFolders = m_cms.getSubFolders(path);
		// get all files in folder
		Vector subFiles = m_cms.getFilesInFolder(path);
		
		// walk through all files and export them
		for(int i = 0; i < subFiles.size(); i++) {
			CmsResource file = (CmsResource) subFiles.elementAt(i);
			exportFile(m_cms.readFile(file.getAbsolutePath()));
		}
		
		// walk through all subfolders and export them
		for(int i = 0; i < subFolders.size(); i++) {
			CmsResource folder = (CmsResource) subFolders.elementAt(i);
	
			// check if this is a system-folder and if it should be included.
			if(folder.getAbsolutePath().startsWith("/system/")) {
				if(!m_excludeSystem) {
					// export this folder
					writeXmlEntrys(folder);
					// export all resources in this folder
					exportResources(folder.getAbsolutePath());			
				}
			} else {
				// export this folder
				writeXmlEntrys(folder);
				// export all resources in this folder
				exportResources(folder.getAbsolutePath());			
			}
		}		
	}
	/**
	 * Gets the import resource and stores it in object-member.
	 */
	private void getExportResource() 
		throws CmsException {
		try {
			// add zip-extension, if needed
			if( !m_exportFile.toLowerCase().endsWith(".zip") ) {
				m_exportFile += ".zip";
			}
			
			// create the export-zipstream
			m_exportZipStream = new ZipOutputStream(new FileOutputStream(m_exportFile));
			
		} catch(Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
	}
	/**
	 * Substrings the source-filename, so it is shrinked to the needed part for
	 * import/export.
	 * @param absoluteName The absolute path of the resource.
	 * @return The shrinked path.
	 */
	private String getSourceFilename(String absoluteName) {
		// String path = absoluteName.substring(m_exportPath.length());
		String path = absoluteName; // keep absolute name to distinguish resources
		if (path.startsWith("/")) {
			path = path.substring(1);	
		}
		if(path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}
	/**
	 * Creates the xml-file and appends the initial tags to it.
	 * @param Node moduleNode a node with module informations.
	 */
	private void getXmlConfigFile(Node moduleNode) 
		throws CmsException {
		
		try {
			
			// creates the document
			m_docXml = A_CmsXmlContent.getXmlParser().createEmptyDocument(C_EXPORT_TAG_EXPORT);
			// abbends the initital tags

			// add some comments here
			Node exportNode = m_docXml.getFirstChild();
			exportNode.appendChild( m_docXml.createComment("Creator   : " + m_cms.getRequestContext().currentUser().getName()));
			exportNode.appendChild( m_docXml.createComment("Createdate: " + Utils.getNiceDate(new Date().getTime())));

			if(moduleNode != null) {
				// this is a module export - import module informations here
				exportNode.appendChild(A_CmsXmlContent.getXmlParser().importNode(m_docXml, moduleNode));
			}
			
			m_filesElement = m_docXml.createElement(C_EXPORT_TAG_FILES);
			m_docXml.getDocumentElement().appendChild(m_filesElement);

		} catch(Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
	}
	/**
	 * Writes the xml-config file (manifest) to the zip-file.
	 */
	private void writeXmlConfigFile() 
		throws CmsException {
		try {
			ZipEntry entry = new ZipEntry(C_EXPORT_XMLFILENAME);
			m_exportZipStream.putNextEntry(entry);
			A_CmsXmlContent.getXmlParser().getXmlText(m_docXml, m_exportZipStream);
			m_exportZipStream.closeEntry();
		} catch(Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
	}
	/**
	 * Writes the data for a resources (like acces-rights) to the manifest-xml-file.
	 * @param resource The resource to get the data from.
	 * @exception throws a CmsException if something goes wrong.
	 */
	private void writeXmlEntrys(CmsResource resource)
		throws CmsException {
		String source, type, user, group, access, launcherStartClass;

		// get all needed informations from the resource
		source = getSourceFilename(resource.getAbsolutePath());
		type = m_cms.getResourceType(resource.getType()).getResourceName();
		user = m_cms.readOwner(resource).getName();
		group = m_cms.readGroup(resource).getName();
		access = resource.getAccessFlags() + "";
        launcherStartClass = resource.getLauncherClassname();
		
		// write these informations to the xml-manifest
		Element file = m_docXml.createElement(C_EXPORT_TAG_FILE);
		m_filesElement.appendChild(file);
		
		// only write source if resource is a file
		if(resource.isFile()) {
			addElement(file, C_EXPORT_TAG_SOURCE, source);
		}
		addElement(file, C_EXPORT_TAG_DESTINATION, source);
		addElement(file, C_EXPORT_TAG_TYPE, type);
		addElement(file, C_EXPORT_TAG_USER, user);
		addElement(file, C_EXPORT_TAG_GROUP, group);
		addElement(file, C_EXPORT_TAG_ACCESS, access);
        if(launcherStartClass != null && !"".equals(launcherStartClass) && !C_UNKNOWN_LAUNCHER.equals(launcherStartClass)) {
            addElement(file, C_EXPORT_TAG_LAUNCHER_START_CLASS, launcherStartClass);
        }
		
		// append the node for properties
		Element properties = m_docXml.createElement(C_EXPORT_TAG_PROPERTIES);
		file.appendChild(properties);
		
		// read the properties
		Hashtable fileProperties = m_cms.readAllProperties(resource.getAbsolutePath());
		Enumeration keys = fileProperties.keys();
		
		// create xml-elements for the properties
		while(keys.hasMoreElements()) {
			// append the node for a property
			Element property = m_docXml.createElement(C_EXPORT_TAG_PROPERTY);
			properties.appendChild(property);

			String key = (String) keys.nextElement();
			String value = (String) fileProperties.get(key);
			String propertyType = m_cms.readPropertydefinition(key, type).getType() + "";
			
			addElement(property, C_EXPORT_TAG_NAME, key);
			addElement(property, C_EXPORT_TAG_TYPE, propertyType);
			addCdataElement(property, C_EXPORT_TAG_VALUE, value);
		}
		
	}
}
