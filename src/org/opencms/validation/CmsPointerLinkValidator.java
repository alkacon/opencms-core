/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/validation/Attic/CmsPointerLinkValidator.java,v $
 * Date   : $Date: 2005/06/16 07:34:53 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.validation;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.scheduler.I_CmsScheduledJob;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class to validate pointer links.<p>
 * 
 * @author Jan Baudisch (j.baudisch@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2005/06/16 07:34:53 $
 * @since 5.9.2
 */
public class CmsPointerLinkValidator implements I_CmsScheduledJob {

    /** The report for the output. */
    private I_CmsReport m_report;
    
    /** 
     * Constructs a new pointer link validator.<p>
     * 
     * @param report the report for the output
     */
    public CmsPointerLinkValidator(I_CmsReport report) {
        m_report = report;    
    }
    
    /** 
     * Constructs a new pointer link validator.<p>
     * 
     */
    public CmsPointerLinkValidator() {
        m_report = new CmsLogReport();    
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
        validateLinks(cms);
        return "CmsExternLinkValidator.launch(): Links checked.";
    }
    
    /**
     * Validate all links.<p>
     * 
     * @param cms a OpenCms context object
     * @throws CmsException if something goes wrong 
     */
    public void validateLinks(CmsObject cms) throws CmsException {
        // get all links
        m_report.println(Messages.get().container(Messages.RPT_VALIDATE_EXTERNAL_LINKS_BEGIN_0), I_CmsReport.C_FORMAT_HEADLINE);

        List links = cms.readResources(I_CmsConstants.C_ROOT, CmsResourceFilter.ONLY_VISIBLE_NO_DELETED
            .addRequireType(CmsResourceTypePointer.getStaticTypeId()));
        Iterator iterator = links.iterator();
        Map brokenLinks = new HashMap();
        
        for (int i = 1; iterator.hasNext(); i++) {
            CmsFile link = cms.readFile(cms.getSitePath((CmsResource)iterator.next()));
            String linkUrl = new String(link.getContents());
            m_report.printMessageWithParam(i, links.size(), 
                Messages.get().container(Messages.RPT_VALIDATE_LINK_0), linkUrl);
            // check link and append it to the list of broken links, if test fails
            if (!checkUrl(linkUrl)) {
                brokenLinks.put(link.getRootPath(), linkUrl);
                m_report.println(Messages.get().container(
                    Messages.RPT_BROKEN_0), I_CmsReport.C_FORMAT_ERROR);  
            } else {
                m_report.println(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_OK_0), I_CmsReport.C_FORMAT_OK);   
            }    
        }    
        m_report.println(Messages.get().container(Messages.RPT_LINK_VALIDATION_STAT_2, new Integer(links.size()), 
            new Integer(brokenLinks.size())), I_CmsReport.C_FORMAT_HEADLINE);
        m_report.println(Messages.get().container(Messages.RPT_VALIDATE_EXTERNAL_LINKS_END_0), I_CmsReport.C_FORMAT_HEADLINE);
        
        OpenCms.getLinkManager().setPointerLinkValidationResult(new CmsPointerLinkValidationResult(brokenLinks));

    }

    /**
     * Checks if the given url is valid.<p>
     *
     * @param url the url to check
     * @return false if the url could not be accessed
     */
    public static boolean checkUrl(String url) {
        try {
            URL checkedUrl = new URL(url);
            if (url.toLowerCase().startsWith("http")) {
                HttpURLConnection httpcon = (HttpURLConnection)checkedUrl.openConnection();
                return (httpcon.getResponseCode() == 200);
            } else {
                return true;
            }
        } catch (MalformedURLException mue) {
            return false;
        } catch (IOException ioe) {
            return false;
        } 
    }    
}
