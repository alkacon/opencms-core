/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/web/Attic/CmsXmlOnlineBewerbung.java,v $
 * Date   : $Date: 2000/02/20 16:46:37 $
 * Version: $Revision: 1.7 $
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

package com.opencms.web;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.*;

import java.util.*;
import java.io.*;

/**
 * This class is used to display the application form of mindfact and makes it
 * possible to send the application form as a mail.
 * 
 * @author $Author: w.babachan $
 * @version $Name:  $ $Revision: 1.7 $ $Date: 2000/02/20 16:46:37 $
 * @see com.opencms.template.CmsXmlTemplate
 */
public class CmsXmlOnlineBewerbung extends CmsXmlTemplate {
	
	// static constants
	private static final String C_PATH="/tmp/";
	// Parameters
	private static final String C_TEXT="text";
	private static final String C_FILE1="file1";
	private static final String C_FILE1_CONTENT="file1.content";
	private static final String C_OLDPOSITION="oldPosition";
	private static final String C_NEWPOSITION="newPosition";
	private static final String C_BASE="base";
	private static final String C_ENTRY="entry";
	private static final String C_SALARY="salary";
	private static final String C_HOW="how";
	private static final String C_ANREDE="anrede";
	private static final String C_TITEL="titel";
	private static final String C_FIRSTNAME="firstname";
	private static final String C_SURNAME="surname";
	private static final String C_BIRTHDATE="birthdate";
	private static final String C_CITIZEN="citizen";
	private static final String C_FAMILY="family";
	private static final String C_CO="co";
	private static final String C_STREET="street";
	private static final String C_PLZ="plz";
	private static final String C_CITY="city";
	private static final String C_COMPANYFON="companyFon";
	private static final String C_PRIVATEFON="privateFon";
	private static final String C_MOBILEFON="mobileFon";
	private static final String C_FAX="fax";
	private static final String C_EMAIL="email";	
	private static final String C_URL="url";
	private static final String C_ERRORNUMBER="errorNumber";
	private static final String C_ACTION="action";
	// Error message
	private static final String C_ERR_TEXT="Bewerbungstext";
	private static final String C_ERR_CERTIFICATES="Bewerbung uploaden";
	private static final String C_ERR_OLDPOSITION="Derzeitige Tätigkeit";
	private static final String C_ERR_NEWPOSITION="Angestrebte Position";
	private static final String C_ERR_BASE="bevorzugter Standort";
	private static final String C_ERR_ENTRY="Frühester Eintrittstermin";
	private static final String C_ERR_SALARY="Gehaltswunsch";
	private static final String C_ERR_HOW="Aufmerksam geworden";
	private static final String C_ERR_ANREDE="Anrede";
	private static final String C_ERR_TITEL="Titel";
	private static final String C_ERR_FIRSTNAME="Vorname";
	private static final String C_ERR_SURNAME="Nachname";
	private static final String C_ERR_BIRTHDATE="Geburtsdatum";
	private static final String C_ERR_CITIZEN="Staatsangehörigkeit";
	private static final String C_ERR_FAMILY="Familienstand";
	private static final String C_ERR_CO="C/o";
	private static final String C_ERR_STREET="Straße";
	private static final String C_ERR_PLZ="PLZ";
	private static final String C_ERR_CITY="Ort";
	private static final String C_ERR_COMPANYFON="Fon gesch.";
	private static final String C_ERR_PRIVATEFON="Fon privat";
	private static final String C_ERR_MOBILEFON="Fon mobil";
	private static final String C_ERR_FAX="Fax";
	private static final String C_ERR_EMAIL="Email";
	private static final String C_ERR_URL="Url";
	// Datablocks
	private static final String C_DATA_NEWPOSITION="newPosition";
	private static final String C_DATA_BASE="base";	
	private static final String C_DATA_HOW="how";
	private static final String C_DATA_ANREDE="anrede";	
	private static final String C_DATA_FAMILY="family";
	// Hashtable keys for sending a mail
	private static final String C_HASH_CERTIFICATES="certificates";
	private static final String C_HASH_FROM="from";
	private static final String C_HASH_AN="an";
	private static final String C_HASH_CC="cc";
	private static final String C_HASH_BCC="bcc";
	private static final String C_HASH_HOST="host";
	private static final String C_HASH_SUBJECT="subject";
	private static final String C_HASH_CONTENT="content";
	// Hashtable keys for building a form
	private static final String C_HASH_TEXT="text";		
	private static final String C_HASH_OLDPOSITION="oldPosition";
	private static final String C_HASH_NEWPOSITION="newPosition";
	private static final String C_HASH_BASE="base";
	private static final String C_HASH_ENTRY="entry";
	private static final String C_HASH_SALARY="salary";
	private static final String C_HASH_HOW="how";
	private static final String C_HASH_ANREDE="anrede";
	private static final String C_HASH_TITEL="titel";
	private static final String C_HASH_FIRSTNAME="firstname";
	private static final String C_HASH_SURNAME="surname";
	private static final String C_HASH_BIRTHDATE="birthdate";
	private static final String C_HASH_CITIZEN="citizen";
	private static final String C_HASH_FAMILY="family";
	private static final String C_HASH_CO="co";
	private static final String C_HASH_STREET="street";
	private static final String C_HASH_PLZ="plz";
	private static final String C_HASH_CITY="city";
	private static final String C_HASH_COMPANYFON="companyFon";
	private static final String C_HASH_PRIVATEFON="privateFon";
	private static final String C_HASH_MOBILEFON="mobileFon";
	private static final String C_HASH_FAX="fax";
	private static final String C_HASH_EMAIL="email";	
	private static final String C_HASH_URL="url";
	private static final String C_HASH_IP="ip";
	
	
    /**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile,
							   String elementName, Hashtable parameters,
							   String templateSelector) {
        return false;
    }
	    
	
    /**
     * Reads in the template file and starts the XML parser for the expected
     * content type <class>CmsXmlWpTemplateFile</code>
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public CmsXmlTemplateFile getOwnTemplateFile(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        CmsXmlOnlineBewerbungContentDefinition xmlTemplateDocument = new CmsXmlOnlineBewerbungContentDefinition(cms, templateFile);       
        return xmlTemplateDocument;
    }        
	
	
    /**
     * Gets the content of a defined section in a given template file and its 
     * subtemplates with the given parameters. 
     * 
	 * @see getContent(A_CmsObject cms, String templateFile, String elementName,   
	 * Hashtable parameters)
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * 
     * @return It returns an array of bytes that contains the page.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String 
                             elementName, Hashtable parameters, String 
                             templateSelector) throws CmsException {
		
		// read parameter values		
		String errorMessage="";
		String text=destroyCmsXmlTag((String)parameters.get(C_TEXT));
		String certificates=destroyCmsXmlTag((String)parameters.get(C_FILE1));
		String certificatesContent=destroyCmsXmlTag((String)parameters.get(C_FILE1_CONTENT));
		String oldPosition=destroyCmsXmlTag((String)parameters.get(C_OLDPOSITION));
		String newPosition=destroyCmsXmlTag((String)parameters.get(C_NEWPOSITION));
		String base=destroyCmsXmlTag((String)parameters.get(C_BASE));	
		String entry=destroyCmsXmlTag((String)parameters.get(C_ENTRY));		
		String salary=destroyCmsXmlTag((String)parameters.get(C_SALARY));		
		String how=destroyCmsXmlTag((String)parameters.get(C_HOW));		
		String anrede=destroyCmsXmlTag((String)parameters.get(C_ANREDE));		
		String titel=destroyCmsXmlTag((String)parameters.get(C_TITEL));		
		String firstname=destroyCmsXmlTag((String)parameters.get(C_FIRSTNAME));		
		String surname=destroyCmsXmlTag((String)parameters.get(C_SURNAME));		
		String birthdate=destroyCmsXmlTag((String)parameters.get(C_BIRTHDATE));
		String citizen=destroyCmsXmlTag((String)parameters.get(C_CITIZEN));
		String family=destroyCmsXmlTag((String)parameters.get(C_FAMILY));
		String co=destroyCmsXmlTag((String)parameters.get(C_CO));
		String street=destroyCmsXmlTag((String)parameters.get(C_STREET));
		String plz=destroyCmsXmlTag((String)parameters.get(C_PLZ));
		String city=destroyCmsXmlTag((String)parameters.get(C_CITY));
		String companyFon=destroyCmsXmlTag((String)parameters.get(C_COMPANYFON));
		String privateFon=destroyCmsXmlTag((String)parameters.get(C_PRIVATEFON));
		String mobileFon=destroyCmsXmlTag((String)parameters.get(C_MOBILEFON));
		String fax=destroyCmsXmlTag((String)parameters.get(C_FAX));
		String email=destroyCmsXmlTag((String)parameters.get(C_EMAIL));
		String url=destroyCmsXmlTag((String)parameters.get(C_URL));	
		// errorNumber is the number of errors that is occured so it contains
		// the number of site that it have to go back.
		String errorNumber=destroyCmsXmlTag((String)parameters.get(C_ERRORNUMBER));
		// action defines the way that this method must handle
		String action=destroyCmsXmlTag((String)parameters.get(C_ACTION));
		// convert null an ""
		text=(text==null?"":text);
		certificates=(certificates==null?"":certificates);
		certificatesContent=(certificatesContent==null?"":certificatesContent);
		oldPosition=(oldPosition==null?"":oldPosition);
		newPosition=(newPosition==null?"":newPosition);
		base=(base==null?"":base);
		entry=(entry==null?"":entry);
		salary=(salary==null?"":salary);
		how=(how==null?"":how);
		anrede=(anrede==null?"":anrede);		
		titel=(titel==null?"":titel);
		firstname=(firstname==null?"":firstname);
		surname=(surname==null?"":surname);
		birthdate=(birthdate==null?"":birthdate);
		citizen=(citizen==null?"":citizen);
		family=(family==null?"":family);
		co=(co==null?"":co);
		street=(street==null?"":street);
		plz=(plz==null?"":plz);
		city=(city==null?"":city);
		companyFon=(companyFon==null?"":companyFon);
		privateFon=(privateFon==null?"":privateFon);
		mobileFon=(mobileFon==null?"":mobileFon);
		fax=(fax==null?"":fax);
		email=(email==null?"":email);
		url=(url==null?"":url);
		errorNumber=(errorNumber==null?"":errorNumber);
		action=(action==null?"":action);		
		if (certificates.equals("unknown")) {
			 certificates="";
		}		
		// CententDefinition		
		CmsXmlOnlineBewerbungContentDefinition datablock=(CmsXmlOnlineBewerbungContentDefinition)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);	
		
		datablock.setText(text);
		datablock.setCertificates(certificates);
		datablock.setOldPosition(oldPosition);	
		datablock.setNewPosition(datablock.getOption(C_DATA_NEWPOSITION,newPosition));
		datablock.setBase(datablock.getOption(C_DATA_BASE,base));
		datablock.setEntry(entry);
		datablock.setHow(datablock.getOption(C_DATA_HOW,how));
		datablock.setAnrede(datablock.getOption(C_DATA_ANREDE,anrede));
		datablock.setSalary(salary);
		datablock.setTitel(titel);
		datablock.setFirstname(firstname);
		datablock.setSurname(surname);
		datablock.setBirthdate(birthdate);
		datablock.setCitizen(citizen);
		datablock.setFamily(datablock.getOption(C_DATA_FAMILY,family));
		datablock.setCo(co);
		datablock.setStreet(street);
		datablock.setPlz(plz);
		datablock.setCity(city);
		datablock.setCompanyFon(companyFon);
		datablock.setPrivateFon(privateFon);
		datablock.setMobileFon(mobileFon);
		datablock.setFax(fax);
		datablock.setEmail(email);
		datablock.setUrl(url);
		datablock.setAction(action);
		datablock.setErrorNumber(errorNumber);
		datablock.setErrorMessage(errorMessage);
		
		if (action.equals("sendMail")) {
			
			errorMessage=formIsCorrect(parameters);
			
			if (!errorMessage.equals("")) {				
				errorNumber=new Integer((new Integer(errorNumber).intValue())-1).toString();
				datablock.setErrorNumber(errorNumber);
				
				datablock.setErrorMessage(datablock.getError(errorMessage));
				
				return startProcessing(cms, datablock, elementName, parameters, null);
				
			} else {
				
				// save File
				//certificates=saveFile(certificates,certificatesContent);
				
				HttpServletRequest req=(HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest();
				
				Hashtable mailInfo=new Hashtable();
				// this is nessesary to build the "BewerbungText" datablock				
				mailInfo.put(C_HASH_TEXT,(text.equals("")?"nicht angegeben":text));				
				mailInfo.put(C_HASH_CERTIFICATES,(certificates.equals("")?"nicht angegeben":certificates));
				mailInfo.put(C_HASH_OLDPOSITION,(oldPosition.equals("")?"nicht angegeben":oldPosition));
				mailInfo.put(C_HASH_NEWPOSITION,((newPosition.equals("") || newPosition.equals("Bitte auswählen"))?"nicht angegeben":newPosition));	
	 			mailInfo.put(C_HASH_BASE,((base.equals("") || base.equals("Bitte auswählen"))?"nicht angegeben":base));
				mailInfo.put(C_HASH_ENTRY,(entry.equals("")?"nicht angegeben":entry));
				mailInfo.put(C_HASH_SALARY,(salary.equals("")?"nicht angegeben":salary));
				mailInfo.put(C_HASH_HOW,((how.equals("") || how.equals("Bitte auswählen"))?"nicht angegeben":how));
				mailInfo.put(C_HASH_ANREDE,((anrede.equals("") || anrede.equals("Bitte auswählen"))?"nicht angegeben":anrede));
				mailInfo.put(C_HASH_TITEL,(titel.equals("")?"nicht angegeben":titel));
				mailInfo.put(C_HASH_FIRSTNAME,(firstname.equals("")?"nicht angegeben":firstname));
				mailInfo.put(C_HASH_SURNAME,(surname.equals("")?"nicht angegeben":surname));
				mailInfo.put(C_HASH_BIRTHDATE,(birthdate.equals("")?"nicht angegeben":birthdate));
				mailInfo.put(C_HASH_CITIZEN,(citizen.equals("")?"nicht angegeben":citizen));
				mailInfo.put(C_HASH_FAMILY,((family.equals("") || family.equals("Bitte auswählen"))?"nicht angegeben":family));
				mailInfo.put(C_HASH_CO,(co.equals("")?"nicht angegeben":co));
				mailInfo.put(C_HASH_STREET,(street.equals("")?"nicht angegeben":street));
				mailInfo.put(C_HASH_PLZ,(plz.equals("")?"nicht angegeben":plz));
				mailInfo.put(C_HASH_CITY,(city.equals("")?"nicht angegeben":city));
				mailInfo.put(C_HASH_COMPANYFON,(companyFon.equals("")?"nicht angegeben":companyFon));
				mailInfo.put(C_HASH_PRIVATEFON,(privateFon.equals("")?"nicht angegeben":privateFon));
				mailInfo.put(C_HASH_MOBILEFON,(mobileFon.equals("")?"nicht angegeben":mobileFon));
				mailInfo.put(C_HASH_FAX,(fax.equals("")?"nicht angegeben":fax));
				mailInfo.put(C_HASH_EMAIL,(email.equals("")?"nicht angegeben":email));
				mailInfo.put(C_HASH_URL,(url.equals("")?"nicht angegeben":url));
				mailInfo.put(C_HASH_IP,req.getRemoteAddr());
				// this is nessesary because of "nicht angegeben" must be send
				// or displayed if the user has nothing entered.
				datablock.setText((String)mailInfo.get(C_HASH_TEXT));
				datablock.setCertificates((String)mailInfo.get(C_HASH_CERTIFICATES));
				datablock.setOldPosition((String)mailInfo.get(C_HASH_OLDPOSITION));
				datablock.setNewPosition((String)mailInfo.get(C_HASH_NEWPOSITION));
				datablock.setBase((String)mailInfo.get(C_HASH_BASE));
				datablock.setEntry((String)mailInfo.get(C_HASH_ENTRY));
				datablock.setHow((String)mailInfo.get(C_HASH_HOW));
				datablock.setSalary((String)mailInfo.get(C_HASH_SALARY));
				datablock.setAnrede((String)mailInfo.get(C_HASH_ANREDE));
				datablock.setTitel((String)mailInfo.get(C_HASH_TITEL));
				datablock.setFirstname((String)mailInfo.get(C_HASH_FIRSTNAME));
				datablock.setSurname((String)mailInfo.get(C_HASH_SURNAME));
				datablock.setBirthdate((String)mailInfo.get(C_HASH_BIRTHDATE));
				datablock.setCitizen((String)mailInfo.get(C_HASH_CITIZEN));
				datablock.setFamily((String)mailInfo.get(C_HASH_FAMILY));
				datablock.setCo((String)mailInfo.get(C_HASH_CO));
				datablock.setStreet((String)mailInfo.get(C_HASH_STREET));
				datablock.setPlz((String)mailInfo.get(C_HASH_PLZ));
				datablock.setCity((String)mailInfo.get(C_HASH_CITY));
				datablock.setCompanyFon((String)mailInfo.get(C_HASH_COMPANYFON));
				datablock.setPrivateFon((String)mailInfo.get(C_HASH_PRIVATEFON));
				datablock.setMobileFon((String)mailInfo.get(C_HASH_MOBILEFON));
				datablock.setFax((String)mailInfo.get(C_HASH_FAX));
				datablock.setEmail((String)mailInfo.get(C_HASH_EMAIL));
				datablock.setUrl((String)mailInfo.get(C_HASH_URL));
				
				Hashtable mailTable=new Hashtable();
				
				String from=(String)datablock.getFrom();
				String an=(String)datablock.getTo();
				String cc=(String)datablock.getCc();
				String bcc=(String)datablock.getBcc();
				String host=(String)datablock.getMailserver();
				String subject=(String)datablock.getSubject();
				String content=(String)datablock.getBewerbungsText(mailInfo);
									
				mailTable.put(C_HASH_CERTIFICATES,certificates);
				mailTable.put(C_HASH_FROM,from);
				mailTable.put(C_HASH_AN,an);
				mailTable.put(C_HASH_CC,cc);
				mailTable.put(C_HASH_BCC,bcc);
				mailTable.put(C_HASH_HOST,host);
				mailTable.put(C_HASH_SUBJECT,subject);
				mailTable.put(C_HASH_CONTENT,content);
										
				// write in database
				writeInDatabase(mailInfo);
				
				// send mail
				//CmsXmlMailThread mail=new CmsXmlMailThread(mailTable);
				//mail.start();
				
				return startProcessing(cms, datablock, elementName, parameters, "Answer");
			}
		}
		// At the first call of this method there is no action defined.
		// set the error number -2 to go back two site after mailing the application form
		datablock.setErrorNumber("-2");
		datablock.setAction("sendMail");
		datablock.setErrorMessage("");
		return startProcessing(cms, datablock, elementName, parameters, null);
	}
		
	
	/**
	 * This method checked the parameter, if there is a CmsXml tag then it
	 * destroys it and return a Parameter ohne CmsXml tag.
	 * 	 
	 * @param parameter The String that must be checked.
	 * @return It returns a string ohne CmsXml tag.
	*/
	private String destroyCmsXmlTag(String parameter){
		// search if there is a CmsXml tag then remove it from parameter.
		if (parameter!=null) {
			parameter.replace('<','[');
			parameter.replace('>',']');
		}
		return parameter;
	}
	
	/**
	 * This method checks the value of parameters, if they are correct then it returns null,
	 * otherwise it returns an error message.
	 * 
	 * @param req The clients request.
	 * 
	 * @return it returns an error message if all fields are'nt correct and null if the fields are correct.
	 */
	private String formIsCorrect(Hashtable parameters) throws CmsException {
		
		
		String errorMessage="";
		
		String[] parameter=new String[25];
		String[] value=new String[25];
		
		// write the parameters and related messages in an array
		
		value[0]=destroyCmsXmlTag((String)parameters.get(C_TEXT));
		value[0]=(value[0]==null?"":value[0]);
		parameter[0]=C_ERR_TEXT;
		
		value[1]=destroyCmsXmlTag((String)parameters.get(C_FILE1));
		value[1]=(value[1]==null?"":value[1]);
		if (!value[1].toLowerCase().trim().equals("unknown")) {
			value[1]="";
		}
		parameter[1]=C_ERR_CERTIFICATES;
		
		
		value[2]=destroyCmsXmlTag((String)parameters.get(C_OLDPOSITION));
		value[2]=(value[2]==null?"":value[2]);
		parameter[2]=C_ERR_OLDPOSITION;
		
		value[3]=destroyCmsXmlTag((String)parameters.get(C_NEWPOSITION));
		value[3]=(value[3]==null?"":value[3]);
		parameter[3]=C_ERR_NEWPOSITION;
		
		value[4]=destroyCmsXmlTag((String)parameters.get(C_BASE));
		value[4]=(value[4]==null?"":value[4]);
		parameter[4]=C_ERR_BASE;
		
		value[5]=destroyCmsXmlTag((String)parameters.get(C_ENTRY));
		value[5]=(value[5]==null?"":value[5]);
		parameter[5]=C_ERR_ENTRY;
		
		value[6]=destroyCmsXmlTag((String)parameters.get(C_SALARY));
		value[6]=(value[6]==null?"":value[6]);
		parameter[6]=C_ERR_SALARY;
		
		value[7]=destroyCmsXmlTag((String)parameters.get(C_HOW));
		value[7]=(value[7]==null?"":value[7]);
		parameter[7]=C_ERR_HOW;
		
		value[8]=destroyCmsXmlTag((String)parameters.get(C_ANREDE));
		value[8]=(value[8]==null?"":value[8]);
		parameter[8]=C_ERR_ANREDE;
		
		value[9]=destroyCmsXmlTag((String)parameters.get(C_TITEL));
		value[9]=(value[9]==null?"":value[9]);
		parameter[9]=C_ERR_TITEL;
		
		value[10]=destroyCmsXmlTag((String)parameters.get(C_FIRSTNAME));
		value[10]=(value[10]==null?"":value[10]);
		parameter[10]=C_ERR_FIRSTNAME;
		
		value[11]=destroyCmsXmlTag((String)parameters.get(C_SURNAME));
		value[11]=(value[11]==null?"":value[11]);
		parameter[11]=C_ERR_SURNAME;
		
		value[12]=destroyCmsXmlTag((String)parameters.get(C_BIRTHDATE));
		value[12]=(value[12]==null?"":value[12]);
		parameter[12]=C_ERR_BIRTHDATE;
		
		value[13]=destroyCmsXmlTag((String)parameters.get(C_CITIZEN));
		value[13]=(value[13]==null?"":value[13]);
		parameter[13]=C_ERR_CITIZEN;
		
		value[14]=destroyCmsXmlTag((String)parameters.get(C_FAMILY));
		value[14]=(value[14]==null?"":value[14]);
		parameter[14]=C_ERR_FAMILY;
		
		value[15]=destroyCmsXmlTag((String)parameters.get(C_CO));
		value[15]=(value[15]==null?"":value[15]);
		parameter[15]=C_ERR_CO;
		
		value[16]=destroyCmsXmlTag((String)parameters.get(C_STREET));
		value[16]=(value[16]==null?"":value[16]);
		parameter[16]=C_ERR_STREET;
		
		value[17]=destroyCmsXmlTag((String)parameters.get(C_PLZ));
		value[17]=(value[17]==null?"":value[17]);
		parameter[17]=C_ERR_PLZ;
		
		value[18]=destroyCmsXmlTag((String)parameters.get(C_CITY));
		value[18]=(value[18]==null?"":value[18]);
		parameter[18]=C_ERR_CITY;
		
		value[19]=destroyCmsXmlTag((String)parameters.get(C_COMPANYFON));
		value[19]=(value[19]==null?"":value[19]);
		parameter[19]=C_ERR_COMPANYFON;
		
		value[20]=destroyCmsXmlTag((String)parameters.get(C_PRIVATEFON));
		value[20]=(value[20]==null?"":value[20]);
		parameter[20]=C_ERR_PRIVATEFON;
		
		value[21]=destroyCmsXmlTag((String)parameters.get(C_MOBILEFON));
		value[21]=(value[21]==null?"":value[21]);
		parameter[21]=C_ERR_MOBILEFON;
		
		value[22]=destroyCmsXmlTag((String)parameters.get(C_FAX));
		value[22]=(value[22]==null?"":value[22]);
		parameter[22]=C_ERR_FAX;
		
		value[23]=destroyCmsXmlTag((String)parameters.get(C_EMAIL));
		value[23]=(value[23]==null?"":value[23]);
		parameter[23]=C_ERR_EMAIL;
		
		value[24]=destroyCmsXmlTag((String)parameters.get(C_URL));
		value[24]=(value[24]==null?"":value[24]);
		parameter[24]=C_ERR_URL;
		
		// check the parameters, if there is an error then build an error message
		for (int i=0;i<25;i++) {
			try {				
				check(value[i],parameter[i]);
			} catch (Exception e) {
				errorMessage=errorMessage+e.getMessage()+", ";
			}
		}
		
		// text
		if (value[0].trim().equals("")) {
			if (errorMessage.indexOf(parameter[0])==-1) {
				errorMessage=errorMessage+parameter[0]+", ";
			}
		}
		
		// Current Job
		if (value[2].trim().equals("") || value[2].length()>50) {
			if (errorMessage.indexOf(parameter[2])==-1) {
				errorMessage=errorMessage+parameter[2]+", ";
			}
		}
		
		// Desired Job
		if (value[3].trim().equals("") || value[3].trim().equals("Bitte auswählen") || value[3].length()>50) {
			if (errorMessage.indexOf(parameter[3])==-1) {
				errorMessage=errorMessage+parameter[3]+", ";
			}
		}
		
		// Desired location
		if (value[4].trim().equals("") || value[4].trim().equals("Bitte auswählen") || value[4].length()>30) {
			if (errorMessage.indexOf(parameter[4])==-1) {
				errorMessage=errorMessage+parameter[4]+", ";
			}
		}
		
		// Desired entry date
		if (value[5].trim().equals("") || (!dateIsCorrect(value[5])) || value[5].length()>10) {
			if (errorMessage.indexOf(parameter[5])==-1) {
				errorMessage=errorMessage+parameter[5]+", ";
			}
		}
		
		// Salary
		if (!value[6].trim().equals("") || value[6].length()>10) {
			try {
				int salary=new Integer(value[6]).parseInt(value[6]);
			} catch (Exception e) {				
				if (errorMessage.indexOf(parameter[6])==-1) {
					errorMessage=errorMessage+parameter[6]+", ";
				}
			}
		}
		
		// how have you found us (Aufmerksam geworden durch...)
		if (value[7].trim().equals("") || value[7].trim().equals("Bitte auswählen") || value[7].length()>30) {
			if (errorMessage.indexOf(parameter[7])==-1) {				
				errorMessage=errorMessage+parameter[7]+", ";
			}
		}
		
		// Salutation (anrede)
		if (value[8].trim().equals("") || value[8].trim().equals("Bitte auswählen") || value[8].length()>15) {
			if (errorMessage.indexOf(parameter[8])==-1) {
				errorMessage=errorMessage+parameter[8]+", ";
			}
		}
		
		// Title
		if (value[9].length()>30) {
			if (errorMessage.indexOf(parameter[9])==-1) {
				errorMessage=errorMessage+parameter[9]+", ";
			}
		}
		
		// Firstname
		if (value[10].trim().equals("") || value[10].length()>30) {
			if (errorMessage.indexOf(parameter[10])==-1) {
				errorMessage=errorMessage+parameter[10]+", ";
			}
		}
		
		// Surname
		if (value[11].trim().equals("") || value[11].length()>30) {
			if (errorMessage.indexOf(parameter[11])==-1) {
				errorMessage=errorMessage+parameter[11]+", ";
			}
		}
		
		// Birthdate		
		if (value[12].trim().equals("") || (!dateIsCorrect(value[12])) || value[12].length()>10) {
			if (errorMessage.indexOf(parameter[12])==-1) {
				errorMessage=errorMessage+parameter[12]+", ";
			}
		}
		
		// Citizenship		
		if (value[13].trim().equals("") || value[13].length()>30) {
			if (errorMessage.indexOf(parameter[13])==-1) {
				errorMessage=errorMessage+parameter[13]+", ";
			}
		}
		
		// Family
		if (value[14].length()>15) {
			if (errorMessage.indexOf(parameter[14])==-1) {
				errorMessage=errorMessage+parameter[14]+", ";
			}
		}
		
		// c/o
		if (value[15].length()>30) {
			if (errorMessage.indexOf(parameter[15])==-1) {
				errorMessage=errorMessage+parameter[15]+", ";
			}
		}
		
		// Street
		if (value[16].trim().equals("") || value[16].length()>30) {
			if (errorMessage.indexOf(parameter[16])==-1) {
				errorMessage=errorMessage+parameter[16]+", ";
			}
		}
		
		// PLZ
		if (value[17].trim().equals("") || value[17].length()>5) {
			if (errorMessage.indexOf(parameter[17])==-1) {
				errorMessage=errorMessage+parameter[17]+", ";
			}
		} else {
			int plz=0;
			try {
				plz=new Integer(value[17]).parseInt(value[17]);
			} catch (Exception e) {
				plz=0;
			}
			if (plz<10000 || plz>99999) {
				if (errorMessage.indexOf(parameter[17])==-1) {
					errorMessage=errorMessage+parameter[17]+", ";
				}
			}
		}
		
		// City
		if (value[18].trim().equals("") || value[18].length()>30) {
			if (errorMessage.indexOf(parameter[18])==-1) {
				errorMessage=errorMessage+parameter[18]+", ";
			}
		}
		
		// Company fon
		if (value[19].length()>20) {
			if (errorMessage.indexOf(parameter[19])==-1) {
				errorMessage=errorMessage+parameter[19]+", ";
			}
		}
		
		// Private fon
		if (value[20].trim().equals("") || value[20].length()>30) {
			if (errorMessage.indexOf(parameter[20])==-1) {
				errorMessage=errorMessage+parameter[20]+", ";
			}
		}
		
		// Mobile fon
		if (value[21].length()>20) {
			if (errorMessage.indexOf(parameter[21])==-1) {
				errorMessage=errorMessage+parameter[21]+", ";
			}
		}
		
		// Fax
		if (value[22].length()>20) {
			if (errorMessage.indexOf(parameter[22])==-1) {
				errorMessage=errorMessage+parameter[22]+", ";
			}
		}
		
		// Email
		if (!value[23].trim().equals("") || value[23].length()>30) {
			if (value[23].indexOf('@')==-1 || value[23].indexOf('.')==-1) {
				if (errorMessage.indexOf(parameter[23])==-1) {
					errorMessage=errorMessage+parameter[23]+", ";
				}
			}
		}
	
		// URL
		if (!value[24].trim().equals("") || value[24].length()>30) {
			int first=value[24].indexOf('.');
			int second=value[24].indexOf('.',first+1);
			if (first==-1 || second==-1) {
				if (errorMessage.indexOf(parameter[24])==-1) {
					errorMessage=errorMessage+parameter[24]+", ";
				}
			}
		}
		
		if (!errorMessage.equals("")) {
			errorMessage=errorMessage.substring(0,errorMessage.lastIndexOf(","))+".";
		}
		
		return errorMessage;
	}
		
	
	/**
	 * This method checks the parameter, if there is an illegal charecter
	 * then it returns an error.
	 * 	 
	 * @param value The String that must be checked.
	 * @param parameter the name of feld that it is used to build an error message.
	 * 
	 * @exception Exception throws always the Exception.
	*/
	private void check(String value, String parameter) throws Exception {
		
		if (!value.equals("")) {
			String pattern="01234567890 (abcdefghijklmnopqrstuvwxyz)[ABCDEFGHIJKLMNOPQRSTUVWXYZ]{äöüÄÖÜß}<.@:,;?$|!&+-#_=%*/>";
			for (int i=0;i<value.length();i++) {
				if (value.indexOf(value.charAt(i))==-1) {
					throw new Exception(parameter);
				}
			}
		}
		
	}
	
	
	/**
	 * This method checks the date format, if there is an illegal chareckter then
	 * it returns an error.
	 * 	 
	 * @param date The date that must be checked.
	 * 
	 * @return It returns true if the date is correct.
	*/
	private boolean dateIsCorrect(String date) {
		
		int day,month,year;
		try {
			day=new Integer(date.substring(0,2)).parseInt(date.substring(0,2));
			month=new Integer(date.substring(3,5)).parseInt(date.substring(3,5));
			year=new Integer(date.substring(6)).parseInt(date.substring(6));
		} catch (Exception e) {
			return false;
		}
		if (date.length()!=10) {
			return false;
		}
		if (day<1 || day>31) {
			return false;
		}
		if (month<1 || month>12) {
			return false;
		}
		if (year<1900 || year>2900) {
			return false;
		}
		if (date.charAt(2)!='.' || date.charAt(5)!='.') {
			return false;
		}
		return true;
	}
	
	
	/**
	 * This method saves the attachment at the server
	 * 
	 */
	private String saveFile(String certificates,String certificatesContent) {
		
		if (!certificates.equals("")) {
			try {
				int counter=0;
				String path=C_PATH;
				String tmpName=certificates;
				String filename=certificates;
			
				File tmpFile=new File(path,filename);
				// if the file exists then count the files and build a new filename with the latest countnumber.
				while (tmpFile.exists()) {
					if (counter<10) {
						tmpName=filename.substring(0,filename.lastIndexOf('.'))+"0"+counter+filename.substring(filename.lastIndexOf('.'));
					} else {
						tmpName=filename.substring(0,filename.lastIndexOf('.'))+counter+filename.substring(filename.lastIndexOf('.'));
					}
					tmpFile=new File(path,tmpName);
					counter++;
				}
				certificates=tmpFile.getName();
				OutputStream  file=new FileOutputStream(tmpFile);
				file.write(certificatesContent.getBytes());
				file.close();
			
			} catch (Exception e) {
				if (A_OpenCms.isLogging()) {
				   A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL,e.getMessage());
				}
			}
		}
		return certificates;
	}
	
	
	/**
	 * This method writes the mail informations in database
	 * 
	 * @param mailInfo mailInfo is a hashtable that contains mail information
	 * to be written in database
	 */
	private void writeInDatabase(Hashtable mailInfo) {
		String insert="";
		Connection con=null;
		Statement stmt=null;
		
		try {
			
			// get the values
			String text=(String)mailInfo.get(C_HASH_TEXT);
			String certificates=(String)mailInfo.get(C_HASH_CERTIFICATES);
			String oldposition=(String)mailInfo.get(C_HASH_OLDPOSITION);
			String newposition=(String)mailInfo.get(C_HASH_NEWPOSITION);
			String base=(String)mailInfo.get(C_HASH_BASE);
			String entry=(String)mailInfo.get(C_HASH_ENTRY);
			String salary=(String)mailInfo.get(C_HASH_SALARY);
			String how=(String)mailInfo.get(C_HASH_HOW);
			String anrede=(String)mailInfo.get(C_HASH_ANREDE);
			String titel=(String)mailInfo.get(C_HASH_TITEL);
			String firstname=(String)mailInfo.get(C_HASH_FIRSTNAME);
			String surname=(String)mailInfo.get(C_HASH_SURNAME);
			String birthdate=(String)mailInfo.get(C_HASH_BIRTHDATE);
			String citizen=(String)mailInfo.get(C_HASH_CITIZEN);
			String family=(String)mailInfo.get(C_HASH_FAMILY);
			String co=(String)mailInfo.get(C_HASH_CO);
			String street=(String)mailInfo.get(C_HASH_STREET);
			String plz=(String)mailInfo.get(C_HASH_PLZ);
			String city=(String)mailInfo.get(C_HASH_CITY);
			String companyfon=(String)mailInfo.get(C_HASH_COMPANYFON);
			String privatefon=(String)mailInfo.get(C_HASH_PRIVATEFON);
			String mobilefon=(String)mailInfo.get(C_HASH_MOBILEFON);
			String fax=(String)mailInfo.get(C_HASH_FAX);
			String email=(String)mailInfo.get(C_HASH_EMAIL);
			String url=(String)mailInfo.get(C_HASH_URL);
			String ip=(String)mailInfo.get(C_HASH_IP);
			
			// Database driver
			Class.forName("org.gjt.mm.mysql.Driver");			
			con=DriverManager.getConnection("jdbc:mysql://localhost:3306/mindfactApplication","root","");
			con.setAutoCommit(false);
			stmt=con.createStatement();
			
			// Date and time is required to show the date and time that the record is build in database.
			GregorianCalendar gDate=new GregorianCalendar();
			gDate.setTime(new java.util.Date());
			
			int day=gDate.get(Calendar.DAY_OF_MONTH);
			int month=(gDate.get(Calendar.MONTH))+1;
			int year=gDate.get(Calendar.YEAR);
			int hour=gDate.get(Calendar.HOUR_OF_DAY);
			int minute=gDate.get(Calendar.MINUTE);
			int second=gDate.get(Calendar.SECOND);
			// Date in form YYYYMMDD
			int date=(year*10000)+(month*100)+day;
			// Time in form HHMMSS
			int time=(hour*10000)+(minute*100)+second;
			// Build the insert instruction
			insert="INSERT INTO userData (anrede,title,firstname,surname,birthdate,citizen,family,co,street,plz,city,";
			insert=insert + "companyFon,privateFon,mobileFon,fax,email,url,oldPosition,newPosition,base,entry,salary,how,";
			insert=insert + "application,certificates,ip,DAY_ID,SHORT_TIME_ID) VALUES (";
			insert=insert + "'" + anrede + "','" + titel;
			insert=insert + "','" + firstname + "','" + surname;
			insert=insert + "','" + birthdate + "','" + citizen;
			insert=insert + "','" + family + "','" + co;
			insert=insert + "','" + street + "','" + plz;
			insert=insert + "','" + city + "','" + companyfon;
			insert=insert + "','" + privatefon + "','" + mobilefon;
			insert=insert + "','" + fax + "','" + email;
			insert=insert + "','" + url + "','" + oldposition;
			insert=insert + "','" + newposition + "','" + base;
			insert=insert + "','" + entry + "','" + salary;
			insert=insert + "','" + how + "','" + text;
			insert=insert + "','" + certificates + "','" + ip + "','" + date + "','" + time +"')";
			
			stmt.executeUpdate(insert);
			con.commit();
			con.close();
		} catch (ClassNotFoundException e1) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL,e1.getMessage());
			}
		} catch (SQLException e2){
			  while((e2=e2.getNextException())!=null){
				  if (A_OpenCms.isLogging()) {
					  A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL,e2.getMessage());
				  }					
			}
			try {
				con.rollback();
			}
			catch (SQLException e3){				
				while((e3=e3.getNextException())!=null){
					if (A_OpenCms.isLogging()) {
						A_OpenCms.log(A_OpenCms.C_OPENCMS_CRITICAL,e3.getMessage());
					}
				}
			}
								
		}
	}
}


