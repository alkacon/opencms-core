/**
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/web/Attic/CmsXmlOnlineBewerbung.java,v $ 
 * Author : $Author: w.babachan $
 * Date   : $Date: 2000/02/18 10:00:26 $
 * Version: $Revision: 1.1 $
 * Release: $Name:  $
 *
 * Copyright (c) 2000 Mindfact interaktive medien ag.   All Rights Reserved.
 *
 * THIS SOFTWARE IS NEITHER FREEWARE NOR PUBLIC DOMAIN!
 *
 * To use this software you must purchease a licencse from Mindfact.
 * In order to use this source code, you need written permission from Mindfact.
 * Redistribution of this source code, in modified or unmodified form,
 * is not allowed.
 *
 * MINDAFCT MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THIS SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. MINDFACT SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */

package com.opencms.web;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;
import java.io.*;

/**
 * This class is used to display the application form of mindfact and makes it
 * possible to send the application form as a mail.
 * 
 * @author $Author: w.babachan $
 * @version $Name:  $ $Revision: 1.1 $ $Date: 2000/02/18 10:00:26 $
 * @see com.opencms.template.CmsXmlTemplate
 */
public class CmsXmlOnlineBewerbung extends CmsXmlTemplate {
	
	// Parameters
	private static final String C_TEXT="text";
	private static final String C_FILE1="file1";
	private static final String C_CONTENT="file1.content";
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
		if (action==null) {
			action="";
		}							  
		// CententDefinition
		CmsXmlOnlineBewerbungContentDefinition datablock=null;		
		// for the first time there is no Parameter therefore get
		// the default value.
		// decodeField method converts umlaute letters from HTML format 
		// in original form (e.g. &auml -> ä).
		if (newPosition==null || newPosition.equals("")){
			newPosition=decodeField(datablock.getNewPosition(1));
		}
		// build the selectbox dynamic and choose the selected option
		for(int i=1;i<41;i++){
			if (newPosition.equals(decodeField(datablock.getNewPosition(i)))) {
				datablock.setSelected(i,"selected");				
			} else {
				datablock.setSelected(i,"");	
			}
		}		
		
		// for the first time there is no Parameter therefore get
		// the default value.
		// decodeField method converts umlaute letters from HTML format 
		// in original form (e.g. &auml -> ä).
		if (base==null || base.equals("")){
			base=decodeField(datablock.getBase(1));
		}
		// build the selectbox dynamic and choose the selected option
		for(int i=1;i<41;i++){
			if (base.equals(decodeField(datablock.getBase(i)))){
				datablock.setSelected(i,"selected");				
			} else {
				datablock.setSelected(i,"");	
			}
		}
		
		// for the first time there is no Parameter therefore get
		// the default value.
		// decodeField method converts umlaute letters from HTML format 
		// in original form (e.g. &auml -> ä).
		if (how==null || how.equals("")){
			how=decodeField(datablock.getHow(1));
		}
		// build the selectbox dynamic and choose the selected option
		for(int i=1;i<41;i++){
			if (how.equals(decodeField(datablock.getHow(i)))) {
				datablock.setSelected(i,"selected");				
			} else {
				datablock.setSelected(i,"");	
			}
		}
		
		// for the first time there is no Parameter therefore get
		// the default value.
		// decodeField method converts umlaute letters from HTML format 
		// in original form (e.g. &auml -> ä).
		if (anrede==null || anrede.equals("")){
			anrede=decodeField(datablock.getAnrede(1));
		}
		// build the selectbox dynamic and choose the selected option
		for(int i=1;i<41;i++){
			if (anrede.equals(decodeField(datablock.getAnrede(i)))){
				datablock.setSelected(i,"selected");
			} else {
				datablock.setSelected(i,"");	
			}
		}
		
		// for the first time there is no Parameter therefore get
		// the default value.
		// decodeField method converts umlaute letters from HTML format 
		// in original form (e.g. &auml -> ä).
		if (family==null || family.equals("")){
			family=decodeField(datablock.getFamily(1));
		}
		// build the selectbox dynamic and choose the selected option
		for(int i=1;i<41;i++){
			if (family.equals(decodeField(datablock.getFamily(i)))) {
				datablock.setSelected(i,"selected");				
			} else {
				datablock.setSelected(i,"");	
			}
		}
		
		datablock.setText(text);
		datablock.setCertificates(certificates);
		datablock.setOldPosition(oldPosition);		
		datablock.setEntry(entry);
		datablock.setSalary(salary);
		datablock.setTitel(titel);
		datablock.setFirstname(firstname);
		datablock.setSurname(surname);
		datablock.setBirthdate(birthdate);
		datablock.setCitizen(citizen);						
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
			
		CmsXmlTemplateFile xmlTemplateDocument=(CmsXmlTemplateFile)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
		
		if (action.equals("sendMail")) {
			errorMessage=formIsCorrect(parameters);
			if (errorMessage!=null || (!errorMessage.equals(""))) {
				errorNumber=new Integer((new Integer(errorNumber).intValue())-1).toString();
				datablock.setErrorNumber(errorNumber);
				datablock.setErrorMessage(datablock.getError(errorMessage));
				return startProcessing(cms, xmlTemplateDocument, elementName, parameters, null);
			} else {
				return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "Answer");
			}
		}
		// At the first call of this method there is no action defined.
		// set the error number -2 to go back two site after mailing the application form
		datablock.setErrorNumber("-2");
		datablock.setAction("sendMail");
		datablock.setErrorMessage("");
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, null);
	}
	
	
	/**	 
	 * This method returns umlaute letters from HTML format in original format,
	 * e.g. &auml; to ä and .....
	 * 
	 * @param field The field that must be decoded.
	 * 
	 * @return It returns the decoded field.
	*/
	private String decodeField(String field) {
		// change all umlaute letters from HTML format in original format.
		if (field!=null) {
			while(field.indexOf("&auml;")!=-1) {
				field=field.substring(0,field.indexOf("&auml;"))+"ä"+field.substring(field.indexOf("&auml;")+6);
			}
			while(field.indexOf("&ouml;")!=-1) {
				field=field.substring(0,field.indexOf("&ouml;"))+"ö"+field.substring(field.indexOf("&ouml;")+6);
			}
			while(field.indexOf("&uuml;")!=-1) {
				field=field.substring(0,field.indexOf("&uuml;"))+"ü"+field.substring(field.indexOf("&uuml;")+6);
			}
			while(field.indexOf("&Auml;")!=-1) {
				field=field.substring(0,field.indexOf("&Auml;"))+"Ä"+field.substring(field.indexOf("&Auml;")+6);
			}
			while(field.indexOf("&Ouml;")!=-1) {
				field=field.substring(0,field.indexOf("&Ouml;"))+"Ö"+field.substring(field.indexOf("&Ouml;")+6);
			}
			while(field.indexOf("&Uuml;")!=-1) {
				field=field.substring(0,field.indexOf("&Uuml;"))+"Ü"+field.substring(field.indexOf("&Uuml;")+6);
			}
			while(field.indexOf("&szlig;")!=-1) {
				field=field.substring(0,field.indexOf("&szlig;"))+"ß"+field.substring(field.indexOf("&szlig;")+7);
			}
		}
		return field;
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
		
		// CententDefinition
		CmsXmlOnlineBewerbungContentDefinition datablock=null;
		
		String[] parameter=new String[25];
		String[] value=new String[25];
		// write the parameters and related messages in an array
		value[0]=datablock.getText();
		parameter[0]=C_ERR_TEXT;
		value[1]=datablock.getCertificates();
		parameter[1]=C_ERR_CERTIFICATES;
		value[2]=datablock.getOldPosition();
		parameter[2]=C_ERR_OLDPOSITION;
		value[3]=datablock.getNewPosition();
		parameter[3]=C_ERR_NEWPOSITION;
		value[4]=datablock.getBase();
		parameter[4]=C_ERR_BASE;
		value[5]=datablock.getEntry();
		parameter[5]=C_ERR_ENTRY;
		value[6]=datablock.getSalary();
		parameter[6]=C_ERR_SALARY;
		value[7]=datablock.getHow();
		parameter[7]=C_ERR_HOW;
		value[8]=datablock.getAnrede();
		parameter[8]=C_ERR_ANREDE;
		value[9]=datablock.getTitel();
		parameter[9]=C_ERR_TITEL;
		value[10]=datablock.getFirstname();
		parameter[10]=C_ERR_FIRSTNAME;
		value[11]=datablock.getSurname();
		parameter[11]=C_ERR_SURNAME;
		value[12]=datablock.getBirthdate();
		parameter[12]=C_ERR_BIRTHDATE;
		value[13]=datablock.getCitizen();
		parameter[13]=C_ERR_CITIZEN;
		value[14]=datablock.getFamily();
		parameter[14]=C_ERR_FAMILY;
		value[15]=datablock.getCo();
		parameter[15]=C_ERR_CO;
		value[16]=datablock.getStreet();
		parameter[16]=C_ERR_STREET;
		value[17]=datablock.getPlz();
		parameter[17]=C_ERR_PLZ;
		value[18]=datablock.getCity();
		parameter[18]=C_ERR_CITY;
		value[19]=datablock.getCompanyFon();
		parameter[19]=C_ERR_COMPANYFON;
		value[20]=datablock.getPrivateFon();
		parameter[20]=C_ERR_PRIVATEFON;
		value[21]=datablock.getMobileFon();
		parameter[21]=C_ERR_MOBILEFON;
		value[22]=datablock.getFax();
		parameter[22]=C_ERR_FAX;
		value[23]=datablock.getEmail();
		parameter[23]=C_ERR_EMAIL;
		value[24]=datablock.getUrl();
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
		if (value[0]==null || value[0].trim().equals("")) {
			if (errorMessage.indexOf(parameter[0])==-1) {
				errorMessage=errorMessage+parameter[0]+", ";
			}
		}		
		// Current Job
		if (value[2]==null || value[2].trim().equals("")) {
			if (errorMessage.indexOf(parameter[2])==-1) {
				errorMessage=errorMessage+parameter[2]+", ";
			}
		}
		if (value[2]!=null && value[2].length()>50) {
			if (errorMessage.indexOf(parameter[2])==-1) {
				errorMessage=errorMessage+parameter[2]+", ";
			}
		}
		// Desired Job
		if (value[3]==null || value[3].trim().equals("") || value[3].trim().equals("Bitte auswählen")) {
			if (errorMessage.indexOf(parameter[3])==-1) {
				errorMessage=errorMessage+parameter[3]+", ";
			}
		}
		if (value[3]!=null && value[3].length()>50) {
			if (errorMessage.indexOf(parameter[3])==-1) {
				errorMessage=errorMessage+parameter[3]+", ";
			}
		}
		// Desired location
		if (value[4]==null || value[4].trim().equals("") || value[4].trim().equals("Bitte auswählen")) {
			if (errorMessage.indexOf(parameter[4])==-1) {
				errorMessage=errorMessage+parameter[4]+", ";
			}
		}
		if (value[4]!=null && value[4].length()>30) {
			if (errorMessage.indexOf(parameter[4])==-1) {
				errorMessage=errorMessage+parameter[4]+", ";
			}
		}
		// Desired entry date
		if (value[5]==null || value[5].trim().equals("") || (!dateIsCorrect(value[5]))) {
			if (errorMessage.indexOf(parameter[5])==-1) {
				errorMessage=errorMessage+parameter[5]+", ";
			}
		}
		if (value[5]!=null && value[5].length()>10) {
			if (errorMessage.indexOf(parameter[5])==-1) {
				errorMessage=errorMessage+parameter[5]+", ";
			}
		}
		// Salary
		if (value[6]!=null && (!value[6].trim().equals(""))) {
			try {
				int salary=new Integer(value[6]).parseInt(value[6]);
			} catch (Exception e) {				
				if (errorMessage.indexOf(parameter[6])==-1) {
					errorMessage=errorMessage+parameter[6]+", ";
				}
			}
		}
		if (value[6]!=null && value[6].length()>10) {
			if (errorMessage.indexOf(parameter[6])==-1) {
				errorMessage=errorMessage+parameter[6]+", ";
			}
		}
		// how have you found us (Aufmerksam geworden durch...)
		if (value[7]==null || value[7].trim().equals("") || value[7].trim().equals("Bitte auswählen")) {
			if (errorMessage.indexOf(parameter[7])==-1) {				
				errorMessage=errorMessage+parameter[7]+", ";
			}
		}
		if (value[7]!=null && value[7].length()>30) {
			if (errorMessage.indexOf(parameter[7])==-1) {
				errorMessage=errorMessage+parameter[7]+", ";
			}
		}
		// Salutation (anrede)
		if (value[8]==null || value[8].trim().equals("") || value[8].trim().equals("Bitte auswählen")) {
			if (errorMessage.indexOf(parameter[8])==-1) {
				errorMessage=errorMessage+parameter[8]+", ";
			}
		}
		if (value[8]!=null && value[8].length()>15) {
			if (errorMessage.indexOf(parameter[8])==-1) {
				errorMessage=errorMessage+parameter[8]+", ";
			}
		}
		// Title
		if (value[9]!=null && value[9].length()>30) {
			if (errorMessage.indexOf(parameter[9])==-1) {
				errorMessage=errorMessage+parameter[9]+", ";
			}
		}
		// Firstname
		if (value[10]==null || value[10].trim().equals("")) {
			if (errorMessage.indexOf(parameter[10])==-1) {
				errorMessage=errorMessage+parameter[10]+", ";
			}
		}
		if (value[10]!=null && value[10].length()>30) {
			if (errorMessage.indexOf(parameter[10])==-1) {
				errorMessage=errorMessage+parameter[10]+", ";
			}
		}
		// Surname
		if (value[11]==null || value[11].trim().equals("")) {
			if (errorMessage.indexOf(parameter[11])==-1) {
				errorMessage=errorMessage+parameter[11]+", ";
			}
		}
		if (value[11]!=null && value[11].length()>30) {
			if (errorMessage.indexOf(parameter[11])==-1) {
				errorMessage=errorMessage+parameter[11]+", ";
			}
		}
		// Birthdate		
		if (value[12]==null || value[12].trim().equals("") || (!dateIsCorrect(value[12]))) {
			if (errorMessage.indexOf(parameter[12])==-1) {
				errorMessage=errorMessage+parameter[12]+", ";
			}
		}
		if (value[12]!=null && value[12].length()>10) {
			if (errorMessage.indexOf(parameter[12])==-1) {
				errorMessage=errorMessage+parameter[12]+", ";
			}
		}
		// Citizenship		
		if (value[13]==null || value[13].trim().equals("")) {
			if (errorMessage.indexOf(parameter[13])==-1) {
				errorMessage=errorMessage+parameter[13]+", ";
			}
		}
		if (value[13]!=null && value[13].length()>30) {
			if (errorMessage.indexOf(parameter[13])==-1) {
				errorMessage=errorMessage+parameter[13]+", ";
			}
		}
		// Family
		if (value[14]!=null && value[14].length()>15) {
			if (errorMessage.indexOf(parameter[14])==-1) {
				errorMessage=errorMessage+parameter[14]+", ";
			}
		}
		// c/o
		if (value[15]!=null && value[15].length()>30) {
			if (errorMessage.indexOf(parameter[15])==-1) {
				errorMessage=errorMessage+parameter[15]+", ";
			}
		}
		// Street
		if (value[16]==null || value[16].trim().equals("")) {
			if (errorMessage.indexOf(parameter[16])==-1) {
				errorMessage=errorMessage+parameter[16]+", ";
			}
		}
		if (value[16]!=null && value[16].length()>30) {
			if (errorMessage.indexOf(parameter[16])==-1) {
				errorMessage=errorMessage+parameter[16]+", ";
			}
		}
		// PLZ
		if (value[17]==null || value[17].trim().equals("")) {
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
		if (value[17]!=null && value[17].length()>5) {
			if (errorMessage.indexOf(parameter[17])==-1) {
				errorMessage=errorMessage+parameter[17]+", ";
			}
		}
		// City
		if (value[18]==null || value[18].trim().equals("")) {
			if (errorMessage.indexOf(parameter[18])==-1) {
				errorMessage=errorMessage+parameter[18]+", ";
			}
		}
		if (value[18]!=null && value[18].length()>30) {
			if (errorMessage.indexOf(parameter[18])==-1) {
				errorMessage=errorMessage+parameter[18]+", ";
			}
		}
		// Company fon
		if (value[19]!=null && value[19].length()>20) {
			if (errorMessage.indexOf(parameter[19])==-1) {
				errorMessage=errorMessage+parameter[19]+", ";
			}
		}
		// Private fon
		if (value[20]==null || value[20].trim().equals("")) {
			if (errorMessage.indexOf(parameter[20])==-1) {
				errorMessage=errorMessage+parameter[20]+", ";
			}
		}
		if (value[20]!=null && value[20].length()>30) {
			if (errorMessage.indexOf(parameter[20])==-1) {
				errorMessage=errorMessage+parameter[20]+", ";
			}
		}
		// Mobile fon
		if (value[21]!=null && value[21].length()>20) {
			if (errorMessage.indexOf(parameter[21])==-1) {
				errorMessage=errorMessage+parameter[21]+", ";
			}
		}
		// Fax
		if (value[22]!=null && value[22].length()>20) {
			if (errorMessage.indexOf(parameter[22])==-1) {
				errorMessage=errorMessage+parameter[22]+", ";
			}
		}
		// Email
		if (value[23]!=null && (!value[23].trim().equals(""))) {
			if (value[23].indexOf('@')==-1 || value[23].indexOf('.')==-1) {
				if (errorMessage.indexOf(parameter[23])==-1) {
					errorMessage=errorMessage+parameter[23]+", ";
				}
			}
		}
		if (value[23]!=null && value[23].length()>30) {
			if (errorMessage.indexOf(parameter[23])==-1) {
				errorMessage=errorMessage+parameter[23]+", ";
			}
		}
		// URL
		if (value[24]!=null && (!value[24].trim().equals(""))) {
			int first=value[24].indexOf('.');
			int second=value[24].indexOf('.',first+1);
			if (first==-1 || second==-1) {
				if (errorMessage.indexOf(parameter[24])==-1) {
					errorMessage=errorMessage+parameter[24]+", ";
				}
			}
		}
		if (value[24]!=null && value[24].length()>30) {
			if (errorMessage.indexOf(parameter[24])==-1) {
				errorMessage=errorMessage+parameter[24]+", ";
			}
		}
		
		if (!errorMessage.equals("")) {
			errorMessage=errorMessage.substring(0,errorMessage.lastIndexOf(","))+".";
		} else {
			errorMessage=null;
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
		
		if (value!=null && (!value.equals(""))) {
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
}


