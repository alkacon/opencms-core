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
 * @version $Revision: 1.2 $ $Date: 2000/02/14 14:05:05 $
 */
class CmsDbImportFiles implements I_CmsConstants {
	
	
	/** ResourceBroker to access all methods and objects */
	private I_CmsResourceBroker RB = null;
	/** User to access all resourcbroker methods and objects */
	private A_CmsUser user = null;
	/** Project to access all resourcbroker methods and objects */
	private A_CmsProject project = null;
	
	/** to get the entries in the XML object */
	private Node firstNode = null;
	/** to navigate in the XML object	 */
	private NodeList sectionElements = null;
	/** to navigate in the XML object	 */
	private NodeList resourceElements = null;
	
	/** to update and the db and creates the folder and files - resource name */
	private String s_fName=null;
	/** to update and the db and creates the folder and files - resource typename */
	private String s_fTypename=null;
	/** to update and the db and creates the folder and files - file content */
	private String s_fContent=null;

	/** the folder in which to import the new resources	 */
	private String importPath=null;
	
	/** vector to return the error messages */
	private Vector errMsg=new Vector();
	
	
	/**
	 * Constructor, creates a new CmsDbImport object.
	 *
	 * @param eRB current ResourceBroker
	 * @param luser current user logged in
	 * @param lproject current project
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
		RB=eRB;
		user=luser;
		project=lproject;
		importPath=path;
		
		firstNode=fileNode;
		
		errMsg=errLog;
	}


	/**
	 * xmlImport
	 * imports the exported resources in the db
	 *
	 * @return errMsg vector with all error messages
	 * 
	 * @exception throws IOException
	 * @exception throws Exception
	 * 
	 */
	public Vector xmlImport()
			throws CmsException, Exception {
			
			//get all file nodes
			sectionElements=firstNode.getChildNodes();
			
			int nll=sectionElements.getLength();
			int i;

			for(i=0; i<nll; i++){
				if((sectionElements.item(i).getNodeName()).equals(C_TFILEOBJ)) {
					resourceElements = sectionElements.item(i).getChildNodes();
					readResource();
				}
			}
			return errMsg;
	}
	// end xmlImport
	
	/**
	 * readResource
	 * get one resource of the XML object
	 *
	 */
	private void readResource() {
	
		Hashtable h_fMeta=new Hashtable();
		byte[] fContent=new byte[1];
	
		
		int nll=resourceElements.getLength();
		int i;
		for(i=0; i<nll; i++) {
			String help=resourceElements.item(i).getNodeName();
			
				if(help.equals(C_TFNAME)){
					s_fName=resourceElements.item(i).getFirstChild().getNodeValue();
				}
				if(help.equals(C_TFTYPENAME)){
					s_fTypename=resourceElements.item(i).getFirstChild().getNodeValue();
				}
				
				// Metadefinitions
				
				if( (help.equals(C_TFMETAINFO)) && (resourceElements.item(i).hasChildNodes()) ){
					h_fMeta=writeMetaDef(resourceElements.item(i).getChildNodes(), s_fTypename);
				}
				
				// if resource is a file
				if( (help.equals(C_FCONTENT)) && ( !(s_fTypename.equals(C_TYPE_FOLDER_NAME)) ) && (resourceElements.item(i).hasChildNodes()) ){
					s_fContent=resourceElements.item(i).getFirstChild().getNodeValue();
					fContent=readContent(s_fContent);
				}
		}
		//end for
		
		if(s_fTypename.equals(C_TYPE_FOLDER_NAME)){
			s_fName=s_fName.substring(0,(s_fName.length()-1));
			try {
				CmsFolder newFolder = RB.createFolder(user,project,importPath, s_fName, h_fMeta);
			} catch (CmsException e) {
				//System.out.println(e);
				errMsg.addElement(e.getMessage());
			}
			

		} else {
			String picimportPath= importPath+s_fName.substring(0,s_fName.lastIndexOf("/")+1);
			s_fName=s_fName.substring((s_fName.lastIndexOf("/")+1),s_fName.length());
			//System.out.println(picimportPath);
			//System.out.println(s_fName);
			try {
				CmsFile newFile = RB.createFile(user, project, picimportPath ,s_fName, fContent, s_fTypename, h_fMeta);
			} catch (CmsException e) {
				
			}
		}
		
		
	}
	//end readResource
	
	
	/**
	 * writeMetaDef
	 * get metadef and metainfo
	 * the metadef must first be updated, before the resource can be written
	 *	
	 * @param meta list with all Metainformation
	 * 
	 * @return a hashtable with metadefinition for given resource
	 */
	private Hashtable writeMetaDef(NodeList meta, String s_rtype) {
	
		Hashtable h_meta= new Hashtable();
		String s_key=new String();
		String s_type=new String();
		String s_value=new String();
		String help =null;
		
		int nll=meta.getLength();
		int i;
		
		for(i=0; i<nll; i++) {
			help=meta.item(i).getNodeName();
			
			if(help.equals(C_TFMETANAME)) {
				s_key=meta.item(i).getFirstChild().getNodeValue();
			}
			if(help.equals(C_TFMETATYPE)) {
				s_type=meta.item(i).getFirstChild().getNodeValue();
			}
			if(help.equals(C_TFMETAVALUE)) {
				s_value=meta.item(i).getFirstChild().getNodeValue();
				h_meta.put(s_key,s_value);
				try {
					A_CmsMetadefinition newMetaDef = RB.createMetadefinition(user,project, s_key, s_rtype, 0);
				} catch (CmsException e) {
					//System.out.println(e);
					errMsg.addElement(e.getMessage());
				}
			}
			
		}
		return h_meta;
	}
	//end writeMetaDef()
	
	
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
		
		byte[] fContent=new byte[(s_content.length()/2)];
		
		Vector v_erg= new Vector();
		int sl=(s_content.length())-1;
		int i, code, erg;
		
		for(i=0; i<sl; i+=2) {
			 char sh1=s_content.charAt(i);
			 char sh0=s_content.charAt(i+1);
			 code= decodeHex(sh1);
			 erg=code*16;
			 code= decodeHex(sh0);
			 erg= (erg+code)-128;
			 v_erg.addElement(Integer.toString(erg));
			 //System.out.print(sh1);
			 //System.out.print(sh0);
			 //System.out.println("erg"+erg+" "+Integer.toString(erg));
		}

		sl=v_erg.size();
		
		for(i=0; i<sl; i++) {
			fContent[i]=(byte)Integer.parseInt( v_erg.elementAt(i).toString());	
			//System.out.println(fContent[i]);
			//System.out.println(fContent[i]);
		}
	
		//System.out.println("in content");
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
	
			
}
 //end class