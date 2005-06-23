/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/validation/Attic/CmsPointerLinkValidator.java,v $
 * Date   : $Date: 2005/06/23 10:47:25 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * @author Jan Baudisch 
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 6.0.0 
 */
public class CmsPointerLinkValidator implements I_CmsScheduledJob {

    /** The report for the output. */
    private I_CmsReport m_report;

    /**
     * Checks if the given url is valid.<p>
     *
     * @param url the url to check
     * @param cms a OpenCms context object
     * 
     * @return false if the url could not be accessed
     */
    public static boolean checkUrl(CmsObject cms, String url) {

        try {
            if (url.toLowerCase().startsWith("/")) {
                return cms.existsResource(cms.getRequestContext().removeSiteRoot(url));
            } else {
                URL checkedUrl = new URL(url);
                HttpURLConnection httpcon = (HttpURLConnection)checkedUrl.openConnection();
                return (httpcon.getResponseCode() == 200);
            }
        } catch (MalformedURLException mue) {
            return false;
        } catch (IOException ioe) {
            return false;
        }
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

        if (Boolean.valueOf((String)parameters.get("writeLog")).booleanValue()) {
            m_report = new CmsLogReport(I_CmsReport.C_BUNDLE_NAME, cms.getRequestContext().getLocale(), getClass());
        }

        validateLinks(cms);
        return "CmsExternLinkValidator.launch(): Links checked.";
    }

    /** 
     * Sets the report for the output.<p>
     * 
     * @param report the report for the output
     */
    public void setReport(I_CmsReport report) {

        m_report = report;
    }

    /**
     * Validate all links.<p>
     * 
     * @param cms a OpenCms context object
     * 
     * @throws CmsException if something goes wrong 
     */
    public void validateLinks(CmsObject cms) throws CmsException {

        if (m_report == null) {
            m_report = new CmsLogReport();
        }

        m_report.println(
            Messages.get().container(Messages.RPT_VALIDATE_EXTERNAL_LINKS_BEGIN_0),
            I_CmsReport.C_FORMAT_HEADLINE);

        // get all links
        List links = cms.readResources(
            I_CmsConstants.C_ROOT,
            CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(CmsResourceTypePointer.getStaticTypeId()));
        Iterator iterator = links.iterator();
        Map brokenLinks = new HashMap();

        for (int i = 1; iterator.hasNext(); i++) {
            CmsFile link = cms.readFile(cms.getSitePath((CmsResource)iterator.next()));
            String linkUrl = new String(link.getContents());

            // print to the report
            m_report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_SUCCESSION_1,
                new Integer(i),
                new Integer(links.size())), I_CmsReport.C_FORMAT_NOTE);
            m_report.print(Messages.get().container(Messages.RPT_VALIDATE_LINK_0), I_CmsReport.C_FORMAT_NOTE);
            m_report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                link.getRootPath()));
            m_report.print(Messages.get().container(Messages.GUI_LINK_POINTING_TO_0), I_CmsReport.C_FORMAT_NOTE);
            m_report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                linkUrl));
            m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            // check link and append it to the list of broken links, if test fails
            if (!checkUrl(cms, linkUrl)) {
                brokenLinks.put(link.getRootPath(), linkUrl);
                m_report.println(Messages.get().container(Messages.RPT_BROKEN_0), I_CmsReport.C_FORMAT_ERROR);
            } else {
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.C_FORMAT_OK);
            }
        }

        m_report.println(Messages.get().container(
            Messages.RPT_LINK_VALIDATION_STAT_2,
            new Integer(links.size()),
            new Integer(brokenLinks.size())), I_CmsReport.C_FORMAT_HEADLINE);
        m_report.println(
            Messages.get().container(Messages.RPT_VALIDATE_EXTERNAL_LINKS_END_0),
            I_CmsReport.C_FORMAT_HEADLINE);

        OpenCms.getLinkManager().setPointerLinkValidationResult(new CmsPointerLinkValidationResult(brokenLinks));

    }
}
