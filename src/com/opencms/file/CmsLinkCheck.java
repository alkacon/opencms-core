/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsLinkCheck.java,v $
* Date   : $Date: 2002/05/17 11:17:45 $
* Version: $Revision: 1.1 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package com.opencms.file;

import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.util.*;
import com.opencms.defaults.*;
import java.util.*;
import java.net.*;
import java.io.*;

/**
 * @description This class contains the functionaility for checking the url of external links
 * @author Edna Falkenhan
 */
public class CmsLinkCheck extends CmsXmlTemplate implements I_CmsCronJob,I_CmsConstants{

    /**
	 * eMail form with the information for creating the eMail
	 */
	private String C_MAIL_TEMPLATE = "/system/workplace/templates/urlcheckform";

    public CmsLinkCheck() {
    }

    /**
     * this method is called by the scheduler
     * @param CmsObject
     * @param parameter
     * @return String - String that is written to the opencms -log
     */
    public String launch(CmsObject cms, String parameter) throws CmsException {
        linksUrlCheck(cms, parameter);
        return "CmsLinkCheck.launch(): Links checked.";
    }

    /**
     * Check all external links
     *
     * @param CmsObject
     * @param parameter
     */
	public void linksUrlCheck(CmsObject cms, String parameter) throws CmsException {
        Vector inList = null; // contains the resourcenames for each url
        Hashtable notAvailable = new Hashtable();
        Vector linkList = new Vector(); // contains all external links from the database

        // get the hashtable with the last check result from the system table
        Hashtable linkckecktable = cms.readLinkCheckTable();
        Hashtable newLinkchecktable = new Hashtable();

        // get templateFile this way because there is no actual file if
        // method is called from scheduler ...
        CmsXmlTemplateFile template=getOwnTemplateFile(cms, C_MAIL_TEMPLATE, "", null, "");
        // set the current date and time
        GregorianCalendar actDate = new GregorianCalendar();
        String actDateString = actDate.get(GregorianCalendar.DATE)+"."
                                    +(actDate.get(GregorianCalendar.MONTH)+1)+"."
                                    +actDate.get(GregorianCalendar.YEAR)+" "
                                    +actDate.get(GregorianCalendar.HOUR_OF_DAY)+":"
                                    +actDate.get(GregorianCalendar.MINUTE)+":"
                                    +actDate.get(GregorianCalendar.SECOND);
        template.setData("actdate", actDateString);
        newLinkchecktable.put(C_LINKCHECKTABLE_DATE, actDateString);

        StringBuffer mailContent = new StringBuffer(template.getProcessedDataValue("single_message"));

        // get all links from the database
        linkList = cms.readFilesByType(I_CmsConstants.C_UNKNOWN_INT, cms.getResourceType(CmsResourceTypeLink.C_TYPE_RESOURCE_NAME).getResourceType());
        for(int i=0; i<linkList.size(); i++){
            CmsFile linkElement = (CmsFile)linkList.elementAt(i);
            String linkName = linkElement.getAbsolutePath();
			String linkUrl = new String(linkElement.getContents());
            // do not check internal links
            if(!linkUrl.startsWith("/")){
                // get the number of failed checks for the link
                int failedCheck = 0;
                String numFromTable = (String)linkckecktable.get(linkName+", "+linkUrl);
                if((numFromTable != null) && (!"".equals(numFromTable.trim()))){
                    failedCheck = Integer.parseInt(numFromTable);
                }

			    // check the url,
                // if the url is not readable add it to the list of not available urls
			    if (!checkUrl(linkUrl)){
				    // get the vector of resourcenames from the hashtable of urls
				    inList = (Vector)notAvailable.get(linkUrl);
				    if(inList == null){
					    inList = new Vector();
                    }
				    inList.addElement(linkName);
                    notAvailable.put(linkUrl, inList);
                    // add the failed link to the new linkchecktable
                    newLinkchecktable.put(linkName+", "+linkUrl, ""+(failedCheck+1));
                }
            }
        }
        // write the linkchecktable to database
        cms.writeLinkCheckTable(newLinkchecktable);

        // get the information for the output
        if((parameter != null) && (!"".equals(parameter.trim()))){
            // if there are not readable urls create the content of the eMail
            // and send it to the specified user(s)
            if(notAvailable.size() > 0){
                Enumeration linkKeys = notAvailable.keys();
                StringBuffer mailUrls = new StringBuffer();
                while (linkKeys.hasMoreElements()){
                    String url = (String)linkKeys.nextElement();
                    template.setData("url", url);
                    Vector linknames = (Vector)notAvailable.get(url);
                    StringBuffer mailLinks = new StringBuffer();
                    for(int j=0; j < linknames.size(); j++){
                        String nextLink = (String)linknames.elementAt(j);
                        template.setData("linkname", nextLink);
                        mailLinks.append(template.getProcessedDataValue("single_link"));
                    }
                    template.setData("links", mailLinks.toString());
                    mailUrls.append(template.getProcessedDataValue("single_url"));
                }
                mailContent.append(mailUrls.toString());
                if("email".equals(parameter.trim())){
                    // get the eMail information
                    String mailSubject = template.getProcessedDataValue("emailsubject");
                    String mailFrom = template.getDataValue("emailfrom");
                    String[] mailTo = getReceiverArray(template.getDataValue("emailto"));
                    String[] mailCc = getReceiverArray(template.getDataValue("emailcc"));
                    String[] mailBcc = getReceiverArray(template.getDataValue("emailbcc"));
                    String mailType = template.getDataValue("emailtype");
                    generateEmail(mailFrom, mailTo, mailCc, mailBcc, mailSubject, mailContent.toString(), mailType);
                } else {
                    generateFile(mailContent.toString(), parameter, actDate);
                }
            }
        }
	}

    /**
     * check if the url is valid
     *
     * @param url The url to check
     * @return boolean If false the url could not be accessed
     */
    public static boolean checkUrl(String url){
        try {
            URL checkedUrl = new URL(url);
            if(url.toLowerCase().startsWith("http")){
                HttpURLConnection httpcon = (HttpURLConnection)checkedUrl.openConnection();

                if (httpcon.getResponseCode() == 200) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } catch (MalformedURLException mue) {
            return false;
        } catch (IOException ioe){
            return false;
        }
    }

    /**
     * checks if the url is valid and returns the response message
     *
     * @param url The url to check
     * @return String return the response message
     */
    public String checkUrlGetMessage(String url){
        try {
            URL checkedUrl = new URL(url);
            HttpURLConnection con = (HttpURLConnection)checkedUrl.openConnection();

            if (con.getResponseCode() == 200) {
                return "";
            } else {
                return con.getResponseMessage();
            }
        } catch (MalformedURLException mue) {
            return "MalformedURLException: "+mue.getMessage();
        } catch (IOException ioe){
            return "IOException: "+ioe.getMessage();
        }
    }

	/**
	 * Generates the eMail
     *
     * @param mailFrom The email adress of the sender
     * @param mailTo The email adress(es) of the receiver(s)
     * @param mailCc The email adress(es) of the CC-receiver(s)
     * @param mailBcc The email adress(es) of the BCC-receiver(s)
     * @param mailSubject The subject of the mail
     * @param mailContent The content of the mail
     * @param mailType The type of the mail
	 */
	private void generateEmail(String mailFrom, String[] mailTo, String[] mailCc, String[] mailBcc,
                                String mailSubject, String mailContent, String mailType) throws CmsException{
        // create a new CmsMail object and start sending the mails
        CmsMail mail = null;
        if (MailUtils.checkEmail(mailFrom)){
            if (MailUtils.checkEmail((String)mailTo[0])){
                if (mailCc.length > 0 && mailBcc.length > 0) {
                    mail=new CmsMail(mailFrom, mailTo, mailCc, mailBcc, mailSubject, mailContent, mailType);
                    mail.start();
                } else if (mailBcc.length > 0) {
                    mail=new CmsMail(mailFrom, mailTo, mailBcc, mailSubject, mailContent, mailType);
                    mail.start();
                } else {
                    mail=new CmsMail(mailFrom, mailTo, mailSubject, mailContent, mailType);
                    mail.start();
                }
            } else {
            }
        }
	}

	/**
	 * Generates the file
     *
     * @param content The content to store into the file
     * @param pathname The path where the file should be stored
     * @param actDate The date and time when the urls were checked
	 */
	private void generateFile(String content, String pathname, GregorianCalendar actDate) throws CmsException{
        StringBuffer filename = new StringBuffer("check_");
        String year = actDate.get(GregorianCalendar.YEAR)+"";
        String month = (actDate.get(GregorianCalendar.MONTH)+1)+"";
        if(month.length() == 1){
            month = "0"+month;
        }
        String day = (actDate.get(GregorianCalendar.DATE)+"");
        if(day.length() == 1){
            day = "0"+day;
        }
        String hour = actDate.get(GregorianCalendar.HOUR_OF_DAY)+"";
        if(hour.length() == 1){
            hour = "0"+hour;
        }
        String minute = actDate.get(GregorianCalendar.MINUTE)+"";
        if(minute.length() == 1){
            minute = "0"+minute;
        }
        filename.append(year.substring(2));
        filename.append(month);
        filename.append(day);
        filename.append("_"+hour+"-");
        filename.append(minute);
        filename.append(".html");
        // create a new file in the folder with the pathname and writes the filecontent
        try{
            File outputFile = new File(pathname, filename.toString());
            FileWriter writer = new FileWriter(outputFile);
            writer.write(content);
            writer.close();
        } catch (IOException e){
            throw new CmsException("Cannot write output file.", e);
        }
	}

    /**
     * Returns the receivers in a string as string array.
     * The receivers are separated by semicolon
     *
     * @param receivers The string that contains the receivers
     * @return String[] The receivers as elements in an string array
     */
    private String[] getReceiverArray(String receivers){
        String[] retArray = null;
        if(receivers != null){
            if (!"".equals(receivers.trim())){
                StringTokenizer tokens = new StringTokenizer(receivers, ";");
                Vector vec = new Vector();
                while (tokens.hasMoreElements()) {
                    vec.addElement( tokens.nextElement());
                }
                retArray= new String[vec.size()];
                vec.copyInto(retArray);
            } else {
                retArray = new String[] {};
            }
        } else {
            retArray = new String[] {};
        }
        return retArray;
    }
}