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
 * @version $Revision: 1.1 $ $Date: 2000/02/11 18:59:59 $
 */

class CmsDbExport implements I_CmsConstants, I_CmsDbExport {
	
	/**
	 * ResourceBroker, user und project
	 *  to access all methods and objects
	 */
	private I_CmsResourceBroker RB = null;
	private A_CmsUser user = null;
	private A_CmsProject project = null;
	
	private String exportFolder=null;
	private int exportType;
	
	/**
	 * to convert into XML format 
	 */
	private I_CmsXmlParser parser = null;
	private Document docXml = null;
	private Element firstElement = null;
	private Element newElement = null;
	private Element grandparentElement = null;
	private Element sectionElement = null;
	private Element parentElement = null;
	private Node newNode = null;
	
	/**
	 * for the XML output file
	 */
	private File fXml = null;
	private FileOutputStream fXmlStream = null;

	
/**
 * Constructor, creates a new CmsDbExport object.
 *
 * @param eRB current ResourceBroker
 * @param luser current user logged in
 * @param lproject current project
 * @param filename current file, to which the export XML is written
 * @param sourcefolder folder, which has to be exported
 * 
 */
	CmsDbExport(I_CmsResourceBroker eRB, A_CmsUser luser, A_CmsProject lproject, String filename, String exportFolder, int exportType)
		throws IOException, Exception {
		RB=eRB;
		user=luser;
		project=lproject;
		this.exportType=exportType;
		this.exportFolder=exportFolder;
	
		init(filename);
	}
	

/**
 * Inittialisation
 * 
 * creats a new XML object for, the import
 * creats an outputstream for the XML file
 * 
 */
	private void init(String filename)
		throws IOException, Exception {											  
		
		// creats an output stream		
		fXml = new File(filename);
		fXmlStream = new FileOutputStream(fXml);
		
		// creats a new XML object		
		parser = A_CmsXmlContent.getXmlParser();
		docXml = parser.createEmptyDocument(C_FELEMENT);	
	}


	/**
	 * 
	 * decides what to export;
	 * 
	 */
		public void export()
			throws CmsException, IOException, Exception {
			
			
			switch (exportType) {
			case C_EXPORTUSERSFILES: {
						CmsDbExportUsers usersExport=new CmsDbExportUsers(RB, user, project, docXml);
						docXml=usersExport.export();
						CmsDbExportFile fileExport=new CmsDbExportFile(RB, user, project, docXml, exportFolder);
						docXml=fileExport.export();
						break;
					}
			case C_EXPORTONLYUSERS: {
						CmsDbExportUsers usersExport=new CmsDbExportUsers(RB, user, project, docXml);
						docXml=usersExport.export();
						break;
					}
			case C_EXPORTONLYFILES: {
						CmsDbExportFile fileExport=new CmsDbExportFile(RB, user, project, docXml, exportFolder);
						docXml=fileExport.export();
						break;
					}
			}
			
			//writes the XML object to an output stream			 
			parser.getXmlText(docXml, fXmlStream);
			
			//writes the output strem to the file		
			fXmlStream.close();
		}
}