/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/web/Attic/CmsXmlOnlineBewerbungContentDefinition.java,v $
 * Date   : $Date: 2000/02/20 16:07:41 $
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

package com.opencms.web;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;
import java.io.*;

/**
 * This class is used to access the application form's XML-datablocks.
 * 
 * @author $Author: w.babachan $
 * @version $Name:  $ $Revision: 1.5 $ $Date: 2000/02/20 16:07:41 $
 * @see com.opencms.template.CmsXmlTemplateFile
 */
public class CmsXmlOnlineBewerbungContentDefinition extends CmsXmlTemplateFile{
		
	// Xml Datablocks
	private static final String C_SELECTED="selected";
	private static final String C_TEXT="text";
	private static final String C_CERTIFICATES="certificates";
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
	private static final String C_ACTION="action";
	private static final String C_ERRORNUMBER="errorNumber";
	private static final String C_ERRORMESSAGE="errorMessage";
	private static final String C_MAILSERVER="mailserver";
	private static final String C_TO="to";
	private static final String C_CC="cc";
	private static final String C_BCC="bcc";
	private static final String C_FROM="from";
	private static final String C_SUBJECT="subject";
	private static final String C_BEWERBUNGSTEXT="bewerbungsText";
	private static final String C_MESSAGE="message";
	private static final String C_ERROR="error";
	// Hashtable keys for building a form
	private static final String C_HASH_CERTIFICATES="certificates";
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

	
    /**
     * Default constructor.
     */
    public CmsXmlOnlineBewerbungContentDefinition() throws CmsException {
        super();
    }
    
	
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlOnlineBewerbungContentDefinition(A_CmsObject cms, CmsFile file)
													throws CmsException {
        super();
        init(cms, file);
    }

	
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     */        
    public CmsXmlOnlineBewerbungContentDefinition(A_CmsObject cms,
							String filename) throws CmsException {
		super();            
        init(cms, filename);
    }
		
	/**
     * This method gets the value of bewerbungsText datablock.
     * @return It returns the value of bewerbungsText datablock.
     */
    public String getBewerbungsText(Hashtable mailInfo) throws CmsException {
		
		setData(C_TEXT,(String)mailInfo.get(C_HASH_TEXT));
		setData(C_CERTIFICATES,(String)mailInfo.get(C_HASH_CERTIFICATES));
		setData(C_OLDPOSITION,(String)mailInfo.get(C_HASH_OLDPOSITION));
		setData(C_NEWPOSITION,(String)mailInfo.get(C_HASH_NEWPOSITION));
		setData(C_BASE,(String)mailInfo.get(C_HASH_BASE));
		setData(C_ENTRY,(String)mailInfo.get(C_HASH_ENTRY));
		setData(C_HOW,(String)mailInfo.get(C_HASH_HOW));
		setData(C_SALARY,(String)mailInfo.get(C_HASH_SALARY));
		setData(C_ANREDE,(String)mailInfo.get(C_HASH_ANREDE));
		setData(C_TITEL,(String)mailInfo.get(C_HASH_TITEL));
		setData(C_FIRSTNAME,(String)mailInfo.get(C_HASH_FIRSTNAME));
		setData(C_SURNAME,(String)mailInfo.get(C_HASH_SURNAME));
		setData(C_BIRTHDATE,(String)mailInfo.get(C_HASH_BIRTHDATE));
		setData(C_CITIZEN,(String)mailInfo.get(C_HASH_CITIZEN));
		setData(C_FAMILY,(String)mailInfo.get(C_HASH_FAMILY));
		setData(C_CO,(String)mailInfo.get(C_HASH_CO));
		setData(C_STREET,(String)mailInfo.get(C_HASH_STREET));
		setData(C_PLZ,(String)mailInfo.get(C_HASH_PLZ));
		setData(C_CITY,(String)mailInfo.get(C_HASH_CITY));
		setData(C_COMPANYFON,(String)mailInfo.get(C_HASH_COMPANYFON));
		setData(C_PRIVATEFON,(String)mailInfo.get(C_HASH_PRIVATEFON));
		setData(C_MOBILEFON,(String)mailInfo.get(C_HASH_MOBILEFON));
		setData(C_FAX,(String)mailInfo.get(C_HASH_FAX));
		setData(C_EMAIL,(String)mailInfo.get(C_HASH_EMAIL));
		setData(C_URL,(String)mailInfo.get(C_HASH_URL));
        return getProcessedDataValue(C_BEWERBUNGSTEXT);
	}
		
	
	/**
     * This method gets the value of "selected" datablock.
     * @return It returns the value of "selected" datablock.
     */
    public String getOption(String optionName,String optionValue) throws CmsException {
		String option="";
		// for the first time there is no Parameter therefore get
		// the default value.
		// decodeField method converts umlaute letters from HTML format 
		// in original form (e.g. &auml -> ä).		
		if (optionValue.equals("")){			
			optionValue=decodeField(getDataValue(optionName+1));
		}
		// build the selectbox dynamic and choose the selected option		
		for(int i=1;i<51;i++){
			if (hasData(optionName+i)) {
				if (optionValue.equals(decodeField(getDataValue(optionName+i)))) {				
					option=option+"<option selected>"+decodeField(getDataValue(optionName+i))+"\n";				
				} else {
					option=option+"<option>"+decodeField(getDataValue(optionName+i))+"\n";
				}
			}
		}
		return option;
	}
		
	/**
     * This method gets the value of mailserver datablock.
     * @return It returns the value of mailserver datablock.
     */
    public String getMailserver() throws CmsException {
        return getDataValue(C_MAILSERVER);
    }
	
	
	/**
     * This method gets the value of from datablock.
     * @return It returns the value of from datablock.
     */
    public String getFrom() throws CmsException {
        return getDataValue(C_FROM);
    }
	
	
	/**
     * This method gets the value of to datablock.
     * @return It returns the value of to datablock.
     */
    public String getTo() throws CmsException {
        return getDataValue(C_TO);
    }
	
	
	/**
     * This method gets the value of CC datablock.
     * @return It returns the value of CC datablock.
     */
    public String getCc() throws CmsException {
        return getDataValue(C_CC);
    }
	
	
	/**
     * This method gets the value of bcc datablock.
     * @return It returns the value of bcc datablock.
     */
    public String getBcc() throws CmsException {
        return getDataValue(C_BCC);
    }
	
	
	/**
     * This method gets the value of error datablock.
     * @return It returns the value of error datablock.
     */
	public String getError(String error) throws CmsException {
		setData(C_MESSAGE,error);
        return getProcessedDataValue(C_ERROR);
    }
	
	
	/**
     * This method gets the value of subject datablock.
     * @return It returns the value of subject datablock.
     */
    public String getSubject() throws CmsException {
        return getDataValue(C_SUBJECT);
	}
	
	
	/**
     * This method sets the value of certificates datablock.     
     */
    public void setCertificates(String certificates) throws CmsException {
        setData(C_CERTIFICATES,certificates);
    }
	
	/**
     * This method sets the value of text datablock.     
     */
    public void setText(String text) throws CmsException {
        setData(C_TEXT,text);
    }
	
	
	/**
     * This method sets the value of oldPosition datablock.
     */
    public void setOldPosition(String oldPosition) throws CmsException {
         setData(C_OLDPOSITION,oldPosition);
    }
	
	
	/**
     * This method sets the value of entry datablock.
     */
    public void setEntry(String entry) throws CmsException {
        setData(C_ENTRY,entry);
    }
	
	
	/**
     * This method sets the value of salary datablock.
     */
    public void setSalary(String salary) throws CmsException {
        setData(C_SALARY,salary);
    }
	
	
	/**
     * This method sets the value of titel datablock.
     */
    public void setTitel(String titel) throws CmsException {
        setData(C_TITEL,titel);
    }
	
	
	/**
     * This method sets the value of firstname datablock.
     */
    public void setFirstname(String firstname) throws CmsException {
        setData(C_FIRSTNAME,firstname);
    }
	
	
	/**
     * This method sets the value of surname datablock.
     */
    public void setSurname(String surname) throws CmsException {
        setData(C_SURNAME,surname);
    }
	
	
	/**
     * This method sets the value of birthdate datablock.
     */
    public void setBirthdate(String birthdate) throws CmsException {
        setData(C_BIRTHDATE,birthdate);
    }
	
	
	/**
     * This method sets the value of citizen datablock.
     */
    public void setCitizen(String citizen) throws CmsException {
        setData(C_CITIZEN,citizen);
    }
	
	
	/**
     * This method sets the value of co datablock.
     */
    public void setCo(String co) throws CmsException {
        setData(C_CO,co);
    }
	
	
	/**
     * This method sets the value of street datablock.
     */
    public void setStreet(String street) throws CmsException {
        setData(C_STREET,street);
    }
	
	
	/**
     * This method sets the value of plz datablock.
     */
    public void setPlz(String plz) throws CmsException {
        setData(C_PLZ,plz);
    }
	
	
	/**
     * This method sets the value of city datablock.
     */
    public void setCity(String city) throws CmsException {
        setData(C_CITY,city);
    }
	
	
	/**
     * This method sets the value of companyFon datablock.
     */
    public void setCompanyFon(String companyFon) throws CmsException {
        setData(C_COMPANYFON,companyFon);
    }
	
	
	/**
     * This method sets the value of privateFon datablock.
     */
    public void setPrivateFon(String privateFon) throws CmsException {
        setData(C_PRIVATEFON,privateFon);
    }
	
	
	/**
     * This method sets the value of mobileFon datablock.
     */
    public void setMobileFon(String mobileFon) throws CmsException {
        setData(C_MOBILEFON,mobileFon);
    }
	
	
	/**
     * This method sets the value of fax datablock.
     */
    public void setFax(String fax) throws CmsException {
        setData(C_FAX,fax);
    }
	
	
	/**
     * This method sets the value of email datablock.
     */
    public void setEmail(String email) throws CmsException {
        setData(C_EMAIL,email);
    }
	
	
	/**
     * This method sets the value of url datablock.
     */
    public void setUrl(String url) throws CmsException {
        setData(C_URL,url);
    }
	
	
	/**
     * This method sets the value of errorNumber datablock.
     */
    public void setErrorNumber(String errorNumber) throws CmsException {
        setData(C_ERRORNUMBER,errorNumber);
    }
	
	
	/**
     * This method sets the value of action datablock.
     */
    public void setAction(String action) throws CmsException {
        setData(C_ACTION,action);
	}
	
	
	/**
     * This method sets the value of errorMessage datablock.
     */
    public void setErrorMessage(String errorMessage) throws CmsException {
        setData(C_ERRORMESSAGE,errorMessage);
	}
		
	
	/**
     * This method sets the value of newPosition datablock.
     */
    public void setNewPosition(String newPosition) throws CmsException {
        setData(C_NEWPOSITION,newPosition);
	}
	
	
	/**
     * This method sets the value of base datablock.
     */
    public void setBase(String base) throws CmsException {
        setData(C_BASE,base);
	}
	
	
	/**
     * This method sets the value of how datablock.
     */
    public void setHow(String how) throws CmsException {
        setData(C_HOW,how);
	}
	
	
	/**
     * This method sets the value of anrede datablock.
     */
    public void setAnrede(String anrede) throws CmsException {
        setData(C_ANREDE,anrede);
	}
	
	
	/**
     * This method sets the value of family datablock.
     */
    public void setFamily(String family) throws CmsException {
        setData(C_FAMILY,family);
	}
	
	
	/**	 
	 * This method returns umlaute letters from HTML format in original format,
	 * e.g. &auml; to ä and .....
	 * 
	 * @param field The field that must be decoded. 
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
	
}