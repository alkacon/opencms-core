/**
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/web/Attic/CmsXmlOnlineBewerbungSmall.java,v $ 
 * Author : $Author: a.schouten $
 * Date   : $Date: 2000/02/29 16:44:47 $
 * Version: $Revision: 1.7 $
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

import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.*;

import java.util.*;
import java.io.*;

/**
 * This class is used to display the application form of mindfact and makes it
 * possible to send the application form as a mail.
 * 
 * @author $Author: a.schouten $
 * @version $Name:  $ $Revision: 1.7 $ $Date: 2000/02/29 16:44:47 $
 * @see com.opencms.template.CmsXmlTemplate
 */
public class CmsXmlOnlineBewerbungSmall extends CmsXmlTemplate {
		
	// Parameters
	private static final String C_TEXT="text";
	private static final String C_SURNAME="surname";
	private static final String C_CO="co";
	private static final String C_EMAIL="email";	
	private static final String C_ERRORNUMBER="errorNumber";
	private static final String C_ACTION="action";
	// Error message
	private static final String C_ERR_TEXT="Bewerbungstext";
	private static final String C_ERR_SURNAME="Nachname";
	private static final String C_ERR_CO="C/o";
	private static final String C_ERR_EMAIL="Email";
	
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
	private static final String C_HASH_LINK="link";
	
	
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
		String certificates="";
		String text=destroyCmsXmlTag((String)parameters.get(C_TEXT));
		String surname=destroyCmsXmlTag((String)parameters.get(C_SURNAME));		
		String co=destroyCmsXmlTag((String)parameters.get(C_CO));
		String email=destroyCmsXmlTag((String)parameters.get(C_EMAIL));
		// errorNumber is the number of errors that is occured so it contains
		// the number of site that it have to go back.
		String errorNumber=destroyCmsXmlTag((String)parameters.get(C_ERRORNUMBER));
		// action defines the way that this method must handle
		String action=destroyCmsXmlTag((String)parameters.get(C_ACTION));
		// convert null an ""
		text=(text==null?"":text);
		surname=(surname==null?"":surname);
		co=(co==null?"":co);
		email=(email==null?"":email);
		errorNumber=(errorNumber==null?"":errorNumber);
		action=(action==null?"":action);		
		
		// CententDefinition		
		CmsXmlOnlineBewerbungContentDefinition datablock=(CmsXmlOnlineBewerbungContentDefinition)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);	
		
		datablock.setText(text);
		datablock.setSurname(surname);
		datablock.setCo(co);
		datablock.setEmail(email);
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
				
				HttpServletRequest req=(HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest();
				
				Hashtable mailInfo=new Hashtable();
				// this is nessesary to build the "BewerbungText" datablock				
				mailInfo.put(C_HASH_TEXT,(text.equals("")?"nicht angegeben":text));				
				mailInfo.put(C_HASH_CERTIFICATES,"nicht angegeben");
				mailInfo.put(C_HASH_OLDPOSITION,"nicht angegeben");
				mailInfo.put(C_HASH_NEWPOSITION,"nicht angegeben");	
	 			mailInfo.put(C_HASH_BASE,"nicht angegeben");
				mailInfo.put(C_HASH_ENTRY,"nicht angegeben");
				mailInfo.put(C_HASH_SALARY,"nicht angegeben");
				mailInfo.put(C_HASH_HOW,"nicht angegeben");
				mailInfo.put(C_HASH_ANREDE,"nicht angegeben");
				mailInfo.put(C_HASH_TITEL,"nicht angegeben");
				mailInfo.put(C_HASH_FIRSTNAME,"nicht angegeben");
				mailInfo.put(C_HASH_SURNAME,(surname.equals("")?"nicht angegeben":surname));
				mailInfo.put(C_HASH_BIRTHDATE,"nicht angegeben");
				mailInfo.put(C_HASH_CITIZEN,"nicht angegeben");
				mailInfo.put(C_HASH_FAMILY,"nicht angegeben");
				mailInfo.put(C_HASH_CO,(co.equals("")?"nicht angegeben":co));
				mailInfo.put(C_HASH_STREET,"nicht angegeben");
				mailInfo.put(C_HASH_PLZ,"nicht angegeben");
				mailInfo.put(C_HASH_CITY,"nicht angegeben");
				mailInfo.put(C_HASH_COMPANYFON,"nicht angegeben");
				mailInfo.put(C_HASH_PRIVATEFON,"nicht angegeben");
				mailInfo.put(C_HASH_MOBILEFON,"nicht angegeben");
				mailInfo.put(C_HASH_FAX,"nicht angegeben");
				mailInfo.put(C_HASH_EMAIL,(email.equals("")?"nicht angegeben":email));
				mailInfo.put(C_HASH_URL,"nicht angegeben");
				mailInfo.put(C_HASH_IP,req.getRemoteAddr());				
				// write in database
				String link=startWorkflow(cms,mailInfo);
				mailInfo.put(C_HASH_LINK,link);
				writeInDatabase(mailInfo);
				// this is nessesary because of "nicht angegeben" must be send
				// or displayed if the user has nothing entered.
				datablock.setText((String)mailInfo.get(C_HASH_TEXT));
				datablock.setSurname((String)mailInfo.get(C_HASH_SURNAME));
				datablock.setCo((String)mailInfo.get(C_HASH_CO));
				datablock.setEmail((String)mailInfo.get(C_HASH_EMAIL));
				
				Hashtable mailTable=new Hashtable();
				
				String from=(String)datablock.getFrom();
				String an=(String)datablock.getTo();
				String cc=(String)datablock.getCc();
				String bcc=(String)datablock.getBcc();
				String host=(String)datablock.getMailserver();
				String subject=(String)datablock.getSubject(surname,"");
				String content=(String)datablock.getSmallBewerbungsText(mailInfo);
				
				mailTable.put(C_HASH_CERTIFICATES,certificates);
				mailTable.put(C_HASH_FROM,from);
				mailTable.put(C_HASH_AN,an);
				mailTable.put(C_HASH_CC,cc);
				mailTable.put(C_HASH_BCC,bcc);
				mailTable.put(C_HASH_HOST,host);
				mailTable.put(C_HASH_SUBJECT,subject);
				mailTable.put(C_HASH_CONTENT,content);
				
				// send mail
				CmsXmlMailThread mailToCompany=new CmsXmlMailThread(mailTable);				
				mailToCompany.start();
				if (!email.equals("")) {
					mailTable.put(C_HASH_AN,email);
					CmsXmlMailThread mailToApplicant=new CmsXmlMailThread(mailTable);
					mailToApplicant.start();
				}
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
		
		value[1]=destroyCmsXmlTag((String)parameters.get(C_SURNAME));
		value[1]=(value[1]==null?"":value[1]);
		parameter[1]=C_ERR_SURNAME;
		
		value[2]=destroyCmsXmlTag((String)parameters.get(C_CO));
		value[2]=(value[2]==null?"":value[2]);
		parameter[2]=C_ERR_CO;
		
		value[3]=destroyCmsXmlTag((String)parameters.get(C_EMAIL));
		value[3]=(value[3]==null?"":value[3]);
		parameter[3]=C_ERR_EMAIL;
		
		// check the parameters, if there is an error then build an error message
		for (int i=0;i<4;i++) {
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
		
		// Surname
		if (value[1].trim().equals("") || value[1].length()>30) {
			if (errorMessage.indexOf(parameter[1])==-1) {
				errorMessage=errorMessage+parameter[1]+", ";
			}
		}
		
		// c/o
		if (value[2].length()>30) {
			if (errorMessage.indexOf(parameter[2])==-1) {
				errorMessage=errorMessage+parameter[2]+", ";
			}
		}
		
		// Email
		if (!value[3].trim().equals("")) {
			if (value[3].indexOf('@')==-1 || value[3].indexOf('.')==-1) {
				if (errorMessage.indexOf(parameter[3])==-1) {
					errorMessage=errorMessage+parameter[3]+", ";
				}
			}
		}
		if (value[3].length()>30) {
			if (errorMessage.indexOf(parameter[3])==-1) {
				errorMessage=errorMessage+parameter[3]+", ";
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
			// TODO: use pattern correctly
			// String pattern="01234567890 (abcdefghijklmnopqrstuvwxyz)[ABCDEFGHIJKLMNOPQRSTUVWXYZ]{‰ˆ¸ƒ÷‹ﬂ}<.@:,;?$|!&+-#_=%*/>";
			for (int i=0;i<value.length();i++) {
				if (value.indexOf(value.charAt(i))==-1) {
					throw new Exception(parameter);
				}
			}
		}
		
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
			con=DriverManager.getConnection("jdbc:mysql://localhost:3306/opencms?user=root;password=");			
			//con.setAutoCommit(false);
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
	
	private String startWorkflow(A_CmsObject cms, Hashtable formData)
		throws CmsException {
		
		String secretkey = null;
		A_CmsTask project = null;
		
		try {
			// Connection for the DB insert
			Class.forName("org.gjt.mm.mysql.Driver");						
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/opencms","root","");
			Statement stmt = con.createStatement();
			
			secretkey = createSecret();
			System.out.println(secretkey);
			
			String recname = (String)formData.get(C_HASH_SURNAME);
			String recstreet = (String)formData.get(C_HASH_STREET);
			String recpostalcode = (String)formData.get(C_HASH_PLZ);
			String reccity = (String)formData.get(C_HASH_CITY);
			String recphone = (String)formData.get(C_HASH_PRIVATEFON);
			String recfax = (String)formData.get(C_HASH_FAX);
			String recemail = (String)formData.get(C_HASH_EMAIL);
			String receducation = "-";
			String recbirthdate = (String)formData.get(C_HASH_BIRTHDATE);
			String rectraining = "-";
			String recexperience = (String)formData.get(C_HASH_OLDPOSITION);
			String recstarttime = (String)formData.get(C_HASH_ENTRY);
			String recjob = (String)formData.get(C_HASH_NEWPOSITION);
			String co = (String)formData.get(C_HASH_CO);
			String text = (String)formData.get(C_HASH_TEXT);
			
			
			String insert = "INSERT INTO GlobeRecruitingData SET " +
							"name='" + recname + "', " + 
							"street='" + recstreet + "'," +
							"postalcode='" + recpostalcode + "', " + 
							"city='" + reccity + "', " +
							"phone='" + recphone +"', " +
							"fax='" + recfax +"', " +
							"email='" + recemail + "', " + 
							"education='" + receducation + "', " + 
							"birthdate='" + recbirthdate +"', " +
							"training='" + rectraining +"', " + 
							"experience='" + recexperience + "', " + 
							"rowversion=0," +
							"starttime='" + recstarttime + "', " +
							"incomingtime=CURRENT_TIMESTAMP, " + 
							"job='" + recjob + "'," +
							"secretkey='" + secretkey + "'";
			

			stmt.executeUpdate(insert);
			
			int recdataid = getLastInsertId(con);
			
			A_CmsGroup role = cms.readGroup("Personalabt.");
			long timeout = System.currentTimeMillis()+ 241920000;
			
			// get the tasktypes for the recruiting workflow
			int projtyperef = cms.getTaskType("recruiting");
			int tasktyperef = cms.getTaskType("StartApplication");
			int followingtasktyperef = cms.getTaskType("CheckApplication");
			
			String projname = "Messekontakt auf der CeBIT ";
			String taskname = "Messekontakt von " + recname;
			
			String taskcomment = "Emailadresse: " + recemail + "<BR>\n";
			taskcomment += "Firma: " + co + "<BR>\n";
			taskcomment += "Text: " + text + "<BR>\n";
			
			project = cms.createProject(projname, projtyperef, null, timeout, 1);
			
			A_CmsTask newtask = cms.createTask(project.getId(), null, null, taskname, "", tasktyperef, timeout, 1);
			cms.writeTaskLog(newtask.getId(), taskcomment, 100);


			cms.endTask(newtask.getId());
			cms.writeTaskLog(newtask.getId(), "Bewerbung eingegangen.",1);
			
			newtask = cms.createTask(project.getId(), null, role.getName(), taskname, "", followingtasktyperef, timeout, 1);
			cms.setTaskPar(project.getId(), "Recruiting", Integer.toString(recdataid));
			cms.writeTaskLog(newtask.getId(), taskcomment, 100);
			
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
		}		
		return "http://etm.mindfact.com/extern/etm/recruiting/ApplicantInformation.asp?id=" + project.getId() +"&s=" + secretkey;
	}
	
	private int getLastInsertId(Connection con)
		throws CmsException {
		ResultSet res =null;
		int id = -1;
		
		try {
			PreparedStatement statementGetLastInsertId = con.prepareStatement("SELECT LAST_INSERT_ID() AS id");
			res = statementGetLastInsertId.executeQuery();
			id = res.getInt("id");
		} catch (SQLException e){
			throw new CmsException(e.getMessage(),CmsException.C_SQL_ERROR, e);			
		}
		return id;
	}

	private String createSecret(){

		String result = "";
		
		for(int i=0; i<16; i++) {
			result = result + Integer.toString((int)Math.round(10 * Math.random()));
		}
		return result;
	}	
}


