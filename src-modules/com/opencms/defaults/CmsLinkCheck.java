/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/defaults/Attic/CmsLinkCheck.java,v $
 * Date   : $Date: 2006/08/19 13:40:59 $
 * Version: $Revision: 1.6.8.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.opencms.defaults;

import org.opencms.file.CmsObject;
import org.opencms.mail.CmsHtmlMail;
import org.opencms.mail.CmsMailTransport;
import org.opencms.mail.CmsSimpleMail;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.opencms.legacy.CmsLegacyException;
import com.opencms.template.CmsXmlTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * This class contains the functionaility for checking the validity of external links.<p>
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsLinkCheck extends CmsXmlTemplate implements I_CmsScheduledJob {

    /** The defintion of the filename to store the serialized hashtable in the rfs. */
    static final String LINKTABLE_FILENAME = OpenCms.getSystemInfo().getPackagesRfsPath() + "linkcheck";
    /** The key for the date of the last linkcheck in the linkchecktable. */
    public static final String C_LINKCHECKTABLE_DATE = "linkcheckdate";
    
    /**
     * Constructor, does nothing.<p>
     */
    public CmsLinkCheck() {
        // empty
    }

    /**
     * Checks if the given url is valid and returns the response message.<p>
     *
     * @param url the url to check
     * @return the response message
     */
    public String checkUrlGetMessage(String url) {
        try {
            URL checkedUrl = new URL(url);
            HttpURLConnection con = (HttpURLConnection)checkedUrl.openConnection();
            if (con.getResponseCode() == 200) {
                return "";
            } else {
                return con.getResponseMessage();
            }
        } catch (MalformedURLException mue) {
            return "MalformedURLException: " + mue.getMessage();
        } catch (IOException ioe) {
            return "IOException: " + ioe.getMessage();
        }
    }

    /**
     * Generates a warning email message.<p>
     *
     * @param mailFrom the email adress of the sender
     * @param mailTo the email adress(es) of the receiver(s)
     * @param mailCc the email adress(es) of the CC-receiver(s)
     * @param mailBcc the email adress(es) of the BCC-receiver(s)
     * @param mailSubject the subject of the mail
     * @param mailContent the content of the mail
     * @param mailType the type of the mail
     * @throws CmsException if something goes wrong 
     */
    private void generateEmail(
        String mailFrom, 
        List mailTo, 
        List mailCc, 
        List mailBcc, 
        String mailSubject, 
        String mailContent, 
        String mailType
    ) throws CmsException {
        // create a new CmsMail object and start sending the mails        
        if (mailType.toLowerCase().indexOf("html") != -1) {
            CmsHtmlMail mail = new CmsHtmlMail();
            try {
                mail.setFrom(mailFrom);
            } catch (MessagingException e) {
                throw new CmsLegacyException("[" + this.getClass().getName() + "] " + "Error in sending email, invalid recipient email address.", CmsLegacyException.C_BAD_NAME);
            }
            mail.setSubject(mailSubject);
            mail.setHtmlMsg(mailContent);
            mail.setTextMsg(mailContent);
            if (mailTo != null) {
                mail.setTo(mailTo);    
            }
            if (mailCc != null) {
                mail.setCc(mailCc);
            }
            if (mailBcc != null) {
                mail.setCc(mailBcc);
            }
            new CmsMailTransport(mail).send();
        } else {
            CmsSimpleMail mail = new CmsSimpleMail();
            try {
                mail.setFrom(mailFrom);
            } catch (MessagingException e) {
                throw new CmsLegacyException("[" + this.getClass().getName() + "] " + "Error in sending email, invalid recipient email address.", CmsLegacyException.C_BAD_NAME);
            }
            mail.setSubject(mailSubject);
            mail.setMsg(mailContent);
            if (mailTo != null) {
                mail.setTo(mailTo);    
            }
            if (mailCc != null) {
                mail.setCc(mailCc);
            }
            if (mailBcc != null) {
                mail.setCc(mailBcc);
            }
            new CmsMailTransport(mail).send();
        }       
    }

    /**
     * Generates the file.<p>
     *
     * @param content the content to store into the file
     * @param pathname the path where the file should be stored
     * @param actDate the date and time when the urls were checked
     * @throws CmsException if something goes wrong 
     */
    private void generateFile(String content, String pathname, GregorianCalendar actDate) throws CmsException {
        StringBuffer filename = new StringBuffer("check_");
        String year = actDate.get(Calendar.YEAR) + "";
        String month = (actDate.get(Calendar.MONTH) + 1) + "";
        if (month.length() == 1) {
            month = "0" + month;
        }
        String day = (actDate.get(Calendar.DATE) + "");
        if (day.length() == 1) {
            day = "0" + day;
        }
        String hour = actDate.get(Calendar.HOUR_OF_DAY) + "";
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        String minute = actDate.get(Calendar.MINUTE) + "";
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        filename.append(year.substring(2));
        filename.append(month);
        filename.append(day);
        filename.append("_" + hour + "-");
        filename.append(minute);
        filename.append(".html");
        // create a new file in the folder with the pathname and writes the filecontent
        try {
            File outputFile = new File(pathname, filename.toString());
            FileWriter writer = new FileWriter(outputFile);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            throw new CmsLegacyException("Cannot write output file.", e);
        }
    }

    /**
     * Returns the current date as formatted String.<p>
     * 
     * @param actDate the current date
     * @return the current date as String dd.mm.yyyy hh:min
     */
    private String getDateString(GregorianCalendar actDate) {
        String month = (actDate.get(Calendar.MONTH) + 1) + "";
        if (month.length() == 1) {
            month = "0" + month;
        }
        String day = (actDate.get(Calendar.DATE) + "");
        if (day.length() == 1) {
            day = "0" + day;
        }
        String hour = actDate.get(Calendar.HOUR_OF_DAY) + "";
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        String minute = actDate.get(Calendar.MINUTE) + "";
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        return (day + "." + month + "." + actDate.get(Calendar.YEAR) + " " + hour + ":" + minute);
    }

    /**
     * Returns the mail receivers in a string as string array.<p>
     * 
     * The receivers are separated by a semicolon.<p>
     *
     * @param receivers The string that contains the receivers
     * @return String[] The receivers as elements in an string array
     */
    private List getReceiverList(String receivers) {
        List retArray = new ArrayList();
        if (receivers != null) {
            if (!"".equals(receivers.trim())) {
                StringTokenizer tokens = new StringTokenizer(receivers, ";");
                retArray = new ArrayList();
                while (tokens.hasMoreElements()) {
                    try {
                        retArray.add(new InternetAddress((String)tokens.nextElement()));
                    } catch (AddressException e) {
                        if (CmsLog.getLog(this).isErrorEnabled()) {
                            CmsLog.getLog(this).error(e);
                        }   
                    }
                }
            }
        }
        return retArray;
    }

    /**
     * This method is called by the cron scheduler.<p>
     * 
     * @param cms a OpenCms context object
     * @param parameters link check parameters
     * @return the String that is written to the OpenCms log
     * @throws CmsException if something goes wrong 
     */
    public String launch(CmsObject cms, Map parameters) throws CmsException {
        String parameter = (String)parameters.get((String)parameters.keySet().iterator().next());
        if (parameter == null) {
            parameter = "";
        }
        linksUrlCheck(cms, parameter);
        return "CmsLinkCheck.launch(): Links checked.";
    }

    /**
     * Checks all existing external links.<p>
     *
     * @param cms a OpenCms context object
     * @param parameter link check parameters
     * @throws CmsException if something goes wrong 
     */
    public void linksUrlCheck(CmsObject cms, String parameter) throws CmsException {

        // noop, this function is no longer required / used in legacy code
    }
   
    /**
     * Writes the Linkchecktable.
     *
     * @param newLinkchecktable The hashtable that contains the links that were not reachable
     * @throws CmsException if something goes wrong
     */
    public static void writeLinkCheckTable(Hashtable newLinkchecktable) throws CmsException {
        FileOutputStream fOut = null;
        try {
            File tableInRfs = new File(LINKTABLE_FILENAME); 
            if (tableInRfs.exists()) {
                tableInRfs.delete();
                tableInRfs.createNewFile();
            }
            // serialize the object
            fOut = new FileOutputStream(tableInRfs);
            ObjectOutputStream oout = new ObjectOutputStream(fOut);
            oout.writeObject(newLinkchecktable);
            oout.close();
            oout.flush();                                                           
        } catch (IOException e) {
            throw new CmsLegacyException("Error writing serialized hashtable. "+e);
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }        
    }
    
    /**
     * Gets the Linkchecktable.
     *
     * @return the linkchecktable
     * 
     * @throws CmsException if something goes wrong 
     */
    public static Hashtable readLinkCheckTable() throws CmsException {
        Hashtable result = new Hashtable();
        FileInputStream fIn = null;
        byte[] buffer;
        int charsRead;
        int size;
        try {
        File tableInRfs = new File(LINKTABLE_FILENAME); 
        if (tableInRfs.exists()) {
            fIn = new FileInputStream(tableInRfs);
            charsRead = 0;
            size = new Long(tableInRfs.length()).intValue();
            buffer = new byte[size];
            while (charsRead < size) {
                charsRead += fIn.read(buffer, charsRead, size - charsRead);
            }
            
            // now deserialize the object
            ByteArrayInputStream bin = new ByteArrayInputStream(buffer);
            ObjectInputStream oin = new ObjectInputStream(bin);
            Serializable s = (Serializable) oin.readObject();
            result = (Hashtable) s;
        }
        } catch (Exception e) {  
            throw new CmsLegacyException("Error writing serialized hashtable. "+e);
        } finally {
        try {
            if (fIn != null) {
                fIn.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }  
        return result;
    }
}