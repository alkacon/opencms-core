/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsDbExport.java,v $
 * Date   : $Date: 2000/06/05 13:37:54 $
 * Version: $Revision: 1.5 $
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
 * Exports Groups, Users and Files from database into XML file
 * 
 * @author Michaela Schleich
 * @version $Revision: 1.5 $ $Date: 2000/06/05 13:37:54 $
 */

class CmsDbExport implements I_CmsConstants, I_CmsDbExport {
	
	/** ResourceBroker to access all methods and objects */
	private I_CmsResourceBroker m_RB = null;
	/** User to access all resourcbroker methods and objects */
	private CmsUser m_user = null;
	/** Project to access all resourcbroker methods and objects */
	private CmsProject m_project = null;
	
	/** need to convert into XML format */
	private I_CmsXmlParser m_parser = null;
	/** need to initiate an XML object */
	private Document m_docXml = null;
	/** first element of an XML object(document node) need to insert other elements*/
	
	/** which folder to export	 */
	private String m_exportFolder=null;
	/** 
	 * what to export
	 * 
	 * resources and users: C_EXPORTUSERSFILES;
	 * only users:			C_EXPORTONLYUSERS;
	 * only resources:		C_EXPORTONLYFILES;
	 * 
	 */
	private int exportType;
	
	/** for the XML output file		*/
	private File fXml = null;
	/** for the XML output stream		*/
	private FileOutputStream fXmlStream = null;

	
	/**
	 * Constructor, creates a new CmsDbExport object.
	 * 
     * @param eRB current ResourceBroker
	 * @param luser current m_user logged in
	 * @param lproject current m_project
	 * @param filename current file, to which the export XML is written
	 * @param sourcefolder folder, which has to be exported
	 * 
	 * @exception throws IOException
	 * @exception throws Exception
	 * 
	 */
	CmsDbExport(I_CmsResourceBroker eRB, CmsUser luser, CmsProject lproject, String filename, String exportFolder, int exportType)
		throws IOException, Exception {
		m_RB=eRB;
		m_user=luser;
		m_project=lproject;
		this.exportType=exportType;
		this.m_exportFolder=exportFolder;
	
		init(filename);
	}
	

	/**
	 * Inittialisation
	 * 
	 * creats a new XML object for, the export
	 * creats an output stream for the XML file
	 * 
	 * @exception throws IOException
	 * @exception throws Exception
	 * 
	 */
	private void init(String filename)
		throws IOException, Exception {											  
		
		// creats an output stream		
		fXml = new File(filename);
		fXmlStream = new FileOutputStream(fXml);
		
		// creats a new XML object		
		m_parser = A_CmsXmlContent.getXmlParser();
		m_docXml = m_parser.createEmptyDocument(C_FELEMENT);	
	}


	/**
	 * 
	 * decides what to export
	 * and initiate the corresponding class(es)
	 * and writes the XML output file
	 * 
	 * @exception throws IOException
	 * @exception throws CmsException
	 * @exception throws Exception
	 * 
	 */
	public void export()
		throws CmsException, IOException, Exception {
			
			
			switch (exportType) {
			case C_EXPORTUSERSFILES: {
						CmsDbExportUsers usersExport=new CmsDbExportUsers(m_RB, m_user, m_project, m_docXml);
						m_docXml=usersExport.export();
						CmsDbExportFile fileExport=new CmsDbExportFile(m_RB, m_user, m_project, m_docXml, m_exportFolder);
						m_docXml=fileExport.export();
						break;
					}
			case C_EXPORTONLYUSERS: {
						CmsDbExportUsers usersExport=new CmsDbExportUsers(m_RB, m_user, m_project, m_docXml);
						m_docXml=usersExport.export();
						break;
					}
			case C_EXPORTONLYFILES: {
						CmsDbExportFile fileExport=new CmsDbExportFile(m_RB, m_user, m_project, m_docXml, m_exportFolder);
						m_docXml=fileExport.export();
						break;
					}
			}
			
			//writes the XML object to an output stream			 
			m_parser.getXmlText(m_docXml, fXmlStream);
			
			//writes the output strem to the file		
			fXmlStream.close();
		}
}