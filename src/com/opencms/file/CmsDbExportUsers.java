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
 * @version $Revision: 1.1 $ $Date: 2000/02/11 18:59:59 $
 */

class CmsDbExportUsers implements I_CmsConstants {

	/**
	 * ResourceBroker, user und project
	 *  to access all methods and objects
	 */
	private I_CmsResourceBroker RB = null;
	private A_CmsUser user = null;
	private A_CmsProject project = null;

	//to convert into XML format
	private I_CmsXmlParser parser = null;
	private Document docXml = null;
	private Element firstElement = null;
	private Element newElement = null;
	private Element grandparentElement = null;
	private Element sectionElement = null;
	private Element parentElement = null;
	private Node newNode = null;
	
	
/**
 * Constructor, creates a new CmsDBExportFile object.
 * 
 * @param eRB current ResourceBroker
 * @param luser current user logged in
 * @param lproject current project
 * @param docXML XML object
 * 
 */
	CmsDbExportUsers(I_CmsResourceBroker eRB, A_CmsUser luser, A_CmsProject lproject, Document docXml)
		throws Exception {
		
		RB=eRB;
		user=luser;
		project=lproject;
		this.docXml=docXml;
	}
	
	/**
	* initiate the file export
	* 
	* @return the filled XML object
	* 
	*/	
	public Document export()
		throws CmsException, Exception {
			
		//get the documents node, first element in the XML object
		firstElement = docXml.getDocumentElement();
		
		// add all groups and users to the XML object
		groupExport();
		userExport();

		return docXml;
	}
	
	/**
	* exports all groups 
	*/
	private void groupExport()
		throws CmsException, Exception {
	
		String help =null;
		int gi;
		
		newElement= docXml.createElement(C_TGROUPS);
		firstElement.appendChild(newElement);
		sectionElement=newElement;
		
		Vector groups=RB.getGroups(user,project);
			Enumeration genum=groups.elements();
			while (genum.hasMoreElements()) {
				newElement= docXml.createElement(C_TGROUPOBJ);
				sectionElement.appendChild(newElement);
				parentElement=newElement;
				
				A_CmsGroup g=(A_CmsGroup)genum.nextElement();
				
				newElement= docXml.createElement(C_TGNAME);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(g.getName());
				newElement.appendChild(newNode);
				
				newElement= docXml.createElement(C_TGDESC);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(g.getDescription());
				newElement.appendChild(newNode);
		
				newElement= docXml.createElement(C_TGFLAG);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(String.valueOf(g.getFlags()));
				newElement.appendChild(newNode);
				
				/**
				 * get name of parent group
				 * if none value for XML is "none"
				 */
				gi = g.getParentId();
				if(gi!=(-1)) {
					Enumeration genum2=groups.elements();
					while (genum2.hasMoreElements()) {
						A_CmsGroup pg=(A_CmsGroup)genum2.nextElement();
						if(pg.getId()==gi) {
							help=(pg.getName());
							break;
						}
					}
				} else {
					help="";
				}
				newElement= docXml.createElement(C_TGPARENTGROUP);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(help);
				newElement.appendChild(newNode);
			
				// get all user in group
				newElement= docXml.createElement(C_TGROUPUSERS);
				parentElement.appendChild(newElement);
				grandparentElement=parentElement;
				parentElement=newElement;
				
				Vector ug=RB.getUsersOfGroup(user,project,g.getName());
				Enumeration ugenum=ug.elements();
				while(ugenum.hasMoreElements()) {
					A_CmsUser u=(A_CmsUser)ugenum.nextElement();
					newElement= docXml.createElement(C_TGUSER);
					parentElement.appendChild(newElement);
					newNode = docXml.createTextNode(u.getName());
					newElement.appendChild(newNode);
				}
		
			}
	}// end groupExport()


/**
 * exports all users with additional info
 */
	private void userExport()
		throws CmsException, Exception {
	
		newElement= docXml.createElement(C_TUSERS);
		firstElement.appendChild(newElement);
		sectionElement=newElement;
		
		Vector users=RB.getUsers(user,project);
			Enumeration enum=users.elements();
			while (enum.hasMoreElements()) {
				newElement= docXml.createElement(C_TUSEROBJ);
				sectionElement.appendChild(newElement);
				parentElement=newElement;
				
				A_CmsUser u=(A_CmsUser)enum.nextElement();
				
				newElement= docXml.createElement(C_TULOGIN);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(u.getName());
				newElement.appendChild(newNode);
				
				newElement= docXml.createElement(C_TUPASSWD);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode("Kennwort");
				newElement.appendChild(newNode);
				
				newElement= docXml.createElement(C_TUNAME);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(u.getLastname());
				newElement.appendChild(newNode);
				
				newElement= docXml.createElement(C_TUFIRSTNAME);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(u.getFirstname());
				newElement.appendChild(newNode);
				
				newElement= docXml.createElement(C_TUDESC);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(u.getDescription());
				newElement.appendChild(newNode);

				newElement= docXml.createElement(C_TUEMAIL);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(u.getEmail());
				newElement.appendChild(newNode);
				
				A_CmsGroup g = u.getDefaultGroup();
				newElement= docXml.createElement(C_TUDGROUP);
				parentElement.appendChild(newElement);
				newNode = docXml.createTextNode(g.getName());
				newElement.appendChild(newNode);
				
				//get all user groups
				newElement= docXml.createElement(C_TUSERGROUPS);
				parentElement.appendChild(newElement);
				grandparentElement=parentElement;
				parentElement=newElement;
				Vector ug=RB.getGroupsOfUser(user,project,u.getName());
				Enumeration ugenum=ug.elements();
				while (ugenum.hasMoreElements()){
					A_CmsGroup ng=(A_CmsGroup)ugenum.nextElement();
					newElement= docXml.createElement(C_TUGROUP);
					parentElement.appendChild(newElement);
					newNode = docXml.createTextNode(ng.getName());
					newElement.appendChild(newNode);
				}
				

				String help=null;
				if(u.getDisabled()==false) {
					help = new String("false");
				} else {
					help = new String("true");	
				}
				newElement= docXml.createElement(C_TUDISABLED);
				grandparentElement.appendChild(newElement);
				newNode = docXml.createTextNode(help);
				newElement.appendChild(newNode);
				
				newElement= docXml.createElement(C_TUFLAG);
				grandparentElement.appendChild(newElement);
				newNode = docXml.createTextNode(String.valueOf(u.getFlags()));
				newElement.appendChild(newNode);

				
				// get additional Info
				newElement= docXml.createElement(C_TUADDINFO);
				grandparentElement.appendChild(newElement);
				parentElement=newElement;
				
				Hashtable h = u.getAdditionalInfo();
				Enumeration enum2=h.keys();
				Enumeration enum3=h.elements();
				while (enum2.hasMoreElements())	{
				String keyhelp=(String)enum2.nextElement();
								
					if(!(keyhelp.equals(C_ADDITIONAL_INFO_DEFAULTGROUP_ID))) {
							newElement= docXml.createElement(C_TUINFOKEY);
							parentElement.appendChild(newElement);
							newNode = docXml.createTextNode(keyhelp);
							newElement.appendChild(newNode);
					
							newElement= docXml.createElement(C_TUINFOVALUE);
							parentElement.appendChild(newElement);
							newNode = docXml.createTextNode(enum3.nextElement().toString());
							newElement.appendChild(newNode);
						}
				}

			}
	
	}// end userExport()
	

}
		
