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
 * @version $Revision: 1.1 $ $Date: 2000/02/11 18:59:59 $
 */
class CmsDbImport implements I_CmsConstants, I_CmsDbImport {
	
	/**
	 * ResourceBroker, user und project
	 *  to access all methods and objects
	 */
	private I_CmsResourceBroker RB = null;
	private A_CmsUser user = null;
	private A_CmsProject project = null;
	
	//to get XML objects
	private I_CmsXmlParser parser = null;
	private Document docXml = null;
	private Element firstElement = null;
	private NodeList sectionElements = null;
	
	//to update db
	private String importpath=null;
	private String s_gName= null;
	private String s_gDesc= null;
	private String s_gFlag= null;
	private String s_gParent= null;
	
	private String s_uLogin = null;
	private String s_uPasswd = null;
	private String s_uName = new String();
	private String s_uFirstname = new String();
	private String s_uEmail = new String();
	private String s_uDesc = null;
	private String s_uDGroup = null;
	private String s_uDis = null;
	private String s_uFlag = null;
	private String s_uGroup = null;

	//for exception msg
	private Vector errMsg=new Vector();
	
	//For the input XML file
	private File fXml = null;
	private FileReader fXmlReader = null;
	private BufferedReader fXmlStream = null;
	
	
/**
 * Constructor, creates a new CmsDbImport object.
 *
 * @param eRB current ResourceBroker
 * @param luser current user logged in
 * @param lproject current project
 * @param path in which folder (absolute path) to import
 * @param filename which XML file should be imported
 * 
 */
	CmsDbImport(I_CmsResourceBroker eRB, A_CmsUser luser, A_CmsProject lproject, String path, String filename)
		throws IOException, Exception
	{
		RB=eRB;
		user=luser;
		project=lproject;
		importpath=path;
	
		init(filename);
	}

/**
 * Inittialisation
 * 
 * open the XML file
 * reads the XML file into a XML object
 * 
 */
	private void init(String filename)
		throws IOException, Exception {											  
		
		// creats an output stream		
		fXml = new File(filename);
		fXmlReader= new FileReader(fXml);
		fXmlStream = new BufferedReader(fXmlReader);
		
		// creats a new XML object		
		parser = A_CmsXmlContent.getXmlParser();
		docXml = parser.parse(fXmlStream);
	}

	 /**
	* xmlImport
	* initialize the database import
	* 
	* @return a vector with error messages
	* 
	*/	
	public Vector xmlImport()
			throws CmsException, Exception {
			
			//get the documents node, first element in the XML object
			firstElement = docXml.getDocumentElement();
			sectionElements=firstElement.getChildNodes();
			
			int nll=sectionElements.getLength();
			int i, htype;
			for(i=0; i<nll; i++){
				if((sectionElements.item(i).getNodeName()).equals(C_TGROUPS)) {
					//read all groups from the XML object
					groupImport(sectionElements.item(i));
				}
				//read all users from the XML object und update user and users and groupusers
				if((sectionElements.item(i).getNodeName()).equals(C_TUSERS)) {
					userImport(sectionElements.item(i));
				}
				if((sectionElements.item(i).getNodeName()).equals(C_TFILES)) {
					CmsDbImportFiles cmsImport= new CmsDbImportFiles(RB, user, project, importpath, sectionElements.item(i), errMsg);
					errMsg=cmsImport.xmlImport();
				}
			}
				
			//colse the input file
			fXmlStream.close();
			return errMsg;
		}
	
		
/**
 * imports groups 
 * 
 * @param parentNode the first Node, which contains all groups
 * 
 */
	private void groupImport(Node parentNode)
		throws CmsException, Exception {
	
		NodeList parentElements=parentNode.getChildNodes();
			
		int nll=parentElements.getLength();
		int i;
		for(i=0; i<nll; i++) {
			//read all groups from the XML object
			if((parentElements.item(i).getNodeName()).equals(C_TGROUPOBJ)) {
				updateDbGroups(parentElements.item(i));
				}
		}
	}
	
/**
 * imports users
 * 
 * @param parentNode the first Node, which contains all groups
 * 
 */
	private void userImport(Node parentNode)
		throws CmsException, Exception {
	
		NodeList parentElements=parentNode.getChildNodes();
			
		int nll=parentElements.getLength();
		int i;
		for(i=0; i<nll; i++) {
			//read all users from the XML object
			if((parentElements.item(i).getNodeName()).equals(C_TUSEROBJ)) {
				updateDbUsers(parentElements.item(i));
				}
		}
	}
		
	/**
	* updateDbGroups
	* writes the groups into the db
	* 
	* @param parentNode contains one group
	* 
	*/
		private void updateDbGroups(Node parentNode)
			throws Exception {
	
			NodeList parentElements=parentNode.getChildNodes();
			
			int nll=parentElements.getLength();
			int i, htype;
			for(i=0; i<nll; i++) {
				if((parentElements.item(i).getNodeName()).equals(C_TGNAME) && (parentElements.item(i).hasChildNodes())) {
					s_gName=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TGPARENTGROUP)&&(parentElements.item(i).hasChildNodes())) {
					s_gParent=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TGDESC) && (parentElements.item(i).hasChildNodes())) {
					s_gDesc=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TGFLAG) && (parentElements.item(i).hasChildNodes())) {
					s_gFlag=parentElements.item(i).getFirstChild().getNodeValue();
				}
			}// end for
			try {
				A_CmsGroup group = RB.addGroup(user, project, s_gName, s_gDesc, Integer.parseInt(s_gFlag), s_gParent);
			}catch (CmsException e){
					errMsg.addElement(e.getMessage());			
			}
		}// end updateDbGroups
		
		
		/**
		* updateDbUsers
		* writes the user into the db
		* 
		* @param parentNode contains one user
		* 
		*/
		private void updateDbUsers(Node parentNode)
			throws Exception {
	
			NodeList parentElements=parentNode.getChildNodes();
			
			Hashtable h_addInfo = new Hashtable();
			int nll=parentElements.getLength();
			int i, htype;
			for(i=0; i<nll; i++) {
				if((parentElements.item(i).getNodeName()).equals(C_TULOGIN) && (parentElements.item(i).hasChildNodes())) {
					s_uLogin=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUPASSWD)&&(parentElements.item(i).hasChildNodes())) {
					s_uPasswd=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUNAME) && (parentElements.item(i).hasChildNodes())) {
					s_uName=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUFIRSTNAME) && (parentElements.item(i).hasChildNodes())) {
					s_uFirstname=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUDESC) && (parentElements.item(i).hasChildNodes())) {
					s_uDesc=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUEMAIL) && (parentElements.item(i).hasChildNodes())) {
					s_uEmail=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUDGROUP) && (parentElements.item(i).hasChildNodes())) {
					s_uDGroup=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUDISABLED) && (parentElements.item(i).hasChildNodes())) {
					s_uDis=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUFLAG) && (parentElements.item(i).hasChildNodes())) {
					s_uFlag=parentElements.item(i).getFirstChild().getNodeValue();
				}
				//additional Info
				if((parentElements.item(i).getNodeName()).equals(C_TUNAME) && (parentElements.item(i).hasChildNodes())) {
					s_uName=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUFIRSTNAME) && (parentElements.item(i).hasChildNodes())) {
					s_uFirstname=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUEMAIL) && (parentElements.item(i).hasChildNodes())) {
					s_uEmail=parentElements.item(i).getFirstChild().getNodeValue();
				}
				if((parentElements.item(i).getNodeName()).equals(C_TUADDINFO) && (parentElements.item(i).hasChildNodes())) {
					NodeList childElements=parentElements.item(i).getChildNodes();
					int cnll=childElements.getLength();
					int j=0;
					String key =null;
					for(j=0; j<cnll; j++) {
						if((childElements.item(j).getNodeName()).equals(C_TUINFOKEY) && (childElements.item(j).hasChildNodes())) {
							key=childElements.item(j).getFirstChild().getNodeValue();
						}
						if((childElements.item(j).getNodeName()).equals(C_TUINFOVALUE) && (childElements.item(j).hasChildNodes())) {
							String value=childElements.item(j).getFirstChild().getNodeValue();
							h_addInfo.put(key, value);
						}
					}
				}
			}// end for
			try {
				A_CmsUser newUser = RB.addUser(user, project, s_uLogin, s_uPasswd, s_uDGroup, s_uDesc, h_addInfo, Integer.parseInt(s_uFlag));
				newUser.setEmail(s_uEmail);
				newUser.setFirstname(s_uFirstname);
				newUser.setLastname(s_uName);
				RB.writeUser(user, project, newUser);
			}catch (CmsException e){
				errMsg.addElement(e.getMessage());			
			}
			
			// add user to group
			for(i=0;i<nll; i++) {
				if((parentElements.item(i).getNodeName()).equals(C_TUSERGROUPS) && (parentElements.item(i).hasChildNodes())) {
					
					NodeList childElements=parentElements.item(i).getChildNodes();
					int cnll=childElements.getLength();
					int j=0;
					for(j=0; j<cnll; j++) {
						if((childElements.item(j).getNodeName()).equals(C_TUGROUP) && (childElements.item(j).hasChildNodes())) {
							try {
								s_uGroup=(childElements.item(j).getFirstChild().getNodeValue());
								RB.addUserToGroup(user, project, s_uLogin, s_uGroup);
							}catch (CmsException e){
								errMsg.addElement(e.getMessage());
							}
						}
					}
				}
			
			
		}//end add user to group
	}// end updateDbUsers
		
}