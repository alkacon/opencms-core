package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsImportFolder.java,v $
 * Date   : $Date: 2000/08/08 14:08:22 $
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

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.lang.reflect.*;
import com.opencms.core.*;
import com.opencms.template.*;
import org.w3c.dom.*;

/**
 * This class holds the functionaility to import resources from the filesystem 
 * into the cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.2 $ $Date: 2000/08/08 14:08:22 $
 */
public class CmsImportFolder implements I_CmsConstants {
	
	/**
	 * The import-file to load resources from
	 */
	private String m_importFile;
	
	/**
	 * The import-path to write resources into the cms.
	 */
	private String m_importPath;
	
	/**
	 * The cms-object to do the operations.
	 */
	private CmsObject m_cms;
	
	/**
	 * The folder-object to import from.
	 */
	private File m_importResource;
	
	/**
	 * This constructs a new CmsImport-object which imports the resources.
	 * 
	 * @param importFile the file or folder to import from.
	 * @param importPath the path to the cms to import into.
	 * @exception CmsException the CmsException is thrown if something goes wrong.
	 */
	public CmsImportFolder(String importFile, String importPath, CmsObject cms) 
		throws CmsException {
		
		try {

			m_importFile = importFile;
			m_importPath = importPath;
			m_cms = cms;

 
			// open the import resource
			getImportResource();
		
			// frist lock the path to import into.
		
			m_cms.lockResource(m_importPath);

			// import the resources
			importResources(m_importResource, m_importPath);
		
			// all is done, unlock the resources
		
			m_cms.unlockResource(m_importPath);

		} catch( Exception exc ) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
	}
	/**
	 * Returns a byte-array containing the content of the file.
	 * 
	 * @param filename The name of the file to read.
	 * @return bytes[] The content of the file.
	 */
	private byte[] getFileBytes(File file) 
		throws Exception{
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
	/**
	 * Gets the file-type for the filename.
	 */
	private String getFileType(String filename) 
		throws CmsException {
		String suffix = filename.substring(filename.lastIndexOf('.')+1);
		suffix = suffix.toLowerCase(); // file extension of filename
		   
		// read the known file extensions from the database
		Hashtable extensions = m_cms.readFileExtensions(); 
		String resType = new String();
		if (extensions != null) {
			resType = (String) extensions.get(suffix);	
		}
		if (resType == null) {
			resType = "plain";	
		}
		return resType;
	}
	/**
	 * Gets the import resource and stores it in object-member.
	 */
	private void getImportResource() 
		throws CmsException {
		try {
			// get the import resource
			m_importResource = new File(m_importFile);
			
			// if it is a file throw exception.
			if(m_importResource.isFile()) {
				throw new CmsException("Import resource is not a folder");
			}
		} catch(Exception exc) {
			throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
		}
	}
	/**
	 * Imports the resources from the folder to the importPath.
	 * 
	 * @param folder the file-object to import from.
	 * @param importPath the import-path to import into.
	 */
	private void importResources(File folder, String importPath) 
		throws Exception {
		String[] diskFiles = folder.list();	
		File currentFile;
		
		for(int i = 0; i < diskFiles.length; i++) {
			currentFile = new File(folder, diskFiles[i]);
			
			if(currentFile.isDirectory()) {
				// create directory in cms
				m_cms.createFolder(importPath, currentFile.getName());
				importResources(currentFile, importPath + currentFile.getName() + "/");
			} else {
				// import file into cms
				String type = getFileType( currentFile.getName() );
				byte[] content = getFileBytes(currentFile);
				// create the file
				m_cms.createFile(importPath, currentFile.getName(), content, type);				
			}
		}
	}
}
