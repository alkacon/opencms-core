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
 * @version $Revision: 1.2 $ $Date: 2000/02/14 14:05:05 $
 */

class CmsDbExport implements I_CmsConstants, I_CmsDbExport {
	
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
	
	/** which folder to export	 */
	private String exportFolder=null;
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
	 * @param luser current user logged in
	 * @param lproject current project
	 * @param filename current file, to which the export XML is written
	 * @param sourcefolder folder, which has to be exported
	 * 
	 * @exception throws IOException
	 * @exception throws Exception
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
		parser = A_CmsXmlContent.getXmlParser();
		docXml = parser.createEmptyDocument(C_FELEMENT);	
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