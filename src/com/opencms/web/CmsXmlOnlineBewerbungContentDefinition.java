/**
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/web/Attic/CmsXmlOnlineBewerbungContentDefinition.java,v $ 
 * Author : $Author: w.babachan $
 * Date   : $Date: 2000/02/19 12:49:02 $
 * Version: $Revision: 1.2 $
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
 * This class is used to access the application form's XML-datablocks.
 * 
 * @author $Author: w.babachan $
 * @version $Name:  $ $Revision: 1.2 $ $Date: 2000/02/19 12:49:02 $
 * @see com.opencms.template.CmsXmlTemplateFile
 */
public class CmsXmlOnlineBewerbungContentDefinition extends CmsXmlTemplateFile{
		
	// Xml Datablocks
	private static final String C_SELECTED="selected";
	private static final String C_TEXT="text";
	private static final String C_CERTIFICATES="file1";
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
     * This method gets the value of text datablock.
     * @return It returns the value of text datablock.
     */
    public String getText() throws CmsException {
        return getDataValue(C_TEXT);
    }
	
	
	/**
     * This method gets the value of certificate datablock.
     * @return It returns the value of certificate datablock.
     */
    public String getCertificates() throws CmsException {
        return getDataValue(C_CERTIFICATES);
    }
	
	
	/**
     * This method gets the value of oldPosition datablock.
     * @return It returns the value of oldPosition datablock.
     */
    public String getOldPosition() throws CmsException {
        return getDataValue(C_OLDPOSITION);
    }
	
	
	/**
     * This method gets the value of entry datablock.
     * @return It returns the value of entry datablock.
     */
    public String getEntry() throws CmsException {
        return getDataValue(C_ENTRY);
    }
	
	
	/**
     * This method gets the value of salary datablock.
     * @return It returns the value of salary datablock.
     */
    public String getSalary() throws CmsException {
        return getDataValue(C_SALARY);
    }
	
	
	/**
     * This method gets the value of titel datablock.
     * @return It returns the value of titel datablock.
     */
    public String getTitel() throws CmsException {
        return getDataValue(C_TITEL);
    }
	
	
	/**
     * This method gets the value of firstname datablock.
     * @return It returns the value of firstname datablock.
     */
    public String getFirstname() throws CmsException {
        return getDataValue(C_FIRSTNAME);
    }
	
	
	/**
     * This method gets the value of surname datablock.
     * @return It returns the value of surname datablock.
     */
    public String getSurname() throws CmsException {
        return getDataValue(C_SURNAME);
    }
	
	
	/**
     * This method gets the value of birthdate datablock.
     * @return It returns the value of birthdate datablock.
     */
    public String getBirthdate() throws CmsException {
        return getDataValue(C_BIRTHDATE);
    }
	
	
	/**
     * This method gets the value of citizen datablock.
     * @return It returns the value of citizen datablock.
     */
    public String getCitizen() throws CmsException {
        return getDataValue(C_CITIZEN);
    }
	
	
	/**
     * This method gets the value of co datablock.
     * @return It returns the value of co datablock.
     */
    public String getCo() throws CmsException {
        return getDataValue(C_CO);
    }
	
	
	/**
     * This method gets the value of street datablock.
     * @return It returns the value of street datablock.
     */
    public String getStreet() throws CmsException {
        return getDataValue(C_STREET);
    }
	
	
	/**
     * This method gets the value of plz datablock.
     * @return It returns the value of plz datablock.
     */
    public String getPlz() throws CmsException {
        return getDataValue(C_PLZ);
    }
	
	
	/**
     * This method gets the value of city datablock.
     * @return It returns the value of city datablock.
     */
    public String getCity() throws CmsException {
        return getDataValue(C_CITY);
    }
	
	
	/**
     * This method gets the value of companyFon datablock.
     * @return It returns the value of companyFon datablock.
     */
    public String getCompanyFon() throws CmsException {
        return getDataValue(C_COMPANYFON);
    }
	
	
	/**
     * This method gets the value of privateFon datablock.
     * @return It returns the value of privateFon datablock.
     */
    public String getPrivateFon() throws CmsException {
        return getDataValue(C_PRIVATEFON);
    }
	
	
	/**
     * This method gets the value of mobileFon datablock.
     * @return It returns the value of mobileFon datablock.
     */
    public String getMobileFon() throws CmsException {
        return getDataValue(C_MOBILEFON);
    }
	
	
	/**
     * This method gets the value of fax datablock.
     * @return It returns the value of fax datablock.
     */
    public String getFax() throws CmsException {
        return getDataValue(C_FAX);
    }
	
	
	/**
     * This method gets the value of email datablock.
     * @return It returns the value of email datablock.
     */
    public String getEmail() throws CmsException {
        return getDataValue(C_EMAIL);
    }
	
	
	/**
     * This method gets the value of url datablock.
     * @return It returns the value of url datablock.
     */
    public String getUrl() throws CmsException {
        return getDataValue(C_URL);
    }
	
	
	/**
     * This method gets the value of action datablock.
     * @return It returns the value of action datablock.
     */
    public String getAction() throws CmsException {
        return getDataValue(C_ACTION);
    }
	
	
	/**
     * This method gets the value of errorNumber datablock.
     * @return It returns the value of errorNumber datablock.
     */
    public String getErrorNumber() throws CmsException {
        return getDataValue(C_ERRORNUMBER);
    }
	
	
	/**
     * This method gets the value of errorMessage datablock.
     * @return It returns the value of errorMessage datablock.
     */
    public String getErrorMessage() throws CmsException {
        return getDataValue(C_ERRORMESSAGE);
	}
	
	
	/**
     * This method gets the value of newPosition datablock.
     * @return It returns the value of newPosition datablock.
     */
    public String getNewPosition() throws CmsException {
        return getDataValue(C_NEWPOSITION);
	}
	
	
	/**
     * This method gets the value of newPosition datablock.
     * @return It returns the value of newPosition datablock.
     */
    public String getNewPosition(int i) throws CmsException {
        return getDataValue(C_NEWPOSITION+i);
	}
	
	
	/**
     * This method gets the value of base datablock.
     * @return It returns the value of base datablock.
     */
    public String getBase() throws CmsException {
        return getDataValue(C_BASE);
	}
	
	
	/**
     * This method gets the value of base datablock.
     * @return It returns the value of base datablock.
     */
    public String getBase(int i) throws CmsException {
        return getDataValue(C_BASE+i);
	}
	
	
	/**
     * This method gets the value of how datablock.
     * @return It returns the value of how datablock.
     */
    public String getHow() throws CmsException {
        return getDataValue(C_HOW);
	}
	
	
	/**
     * This method gets the value of how datablock.
     * @return It returns the value of how datablock.
     */
    public String getHow(int i) throws CmsException {
        return getDataValue(C_HOW+i);
	}
	
	
	/**
     * This method gets the value of anrede datablock.
     * @return It returns the value of anrede datablock.
     */
    public String getAnrede() throws CmsException {
        return getDataValue(C_ANREDE);
	}
	
	
	/**
     * This method gets the value of anrede datablock.
     * @return It returns the value of anrede datablock.
     */
    public String getAnrede(int i) throws CmsException {
        return getDataValue(C_ANREDE+i);
	}
	
	
	/**
     * This method gets the value of family datablock.
     * @return It returns the value of family datablock.
     */
    public String getFamily() throws CmsException {
        return getDataValue(C_FAMILY);
	}
	
	
	/**
     * This method gets the value of family datablock.
     * @return It returns the value of family datablock.
     */
    public String getFamily(int i) throws CmsException {
        return getDataValue(C_FAMILY+i);
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
        return getDataValue(C_BEWERBUNGSTEXT);
	}
	
	
	/**
     * This method gets the value of "selected" datablock.
     * @return It returns the value of "selected" datablock.
     */
    public String getSelected(int i) throws CmsException {
        return getDataValue(C_SELECTED+i);
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
        return getDataValue(C_ERROR);
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
     * This method sets the value of "selected" datablock.
     */
    public void setSelected(int i,String selected) throws CmsException {
        setData(C_SELECTED+i,selected);
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
	
}