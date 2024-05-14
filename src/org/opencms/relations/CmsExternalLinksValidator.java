/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.relations;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.util.CmsUriSplitter;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;

/**
 * Class to validate pointer links.<p>
 *
 * @since 6.0.0
 */
public class CmsExternalLinksValidator implements I_CmsScheduledJob {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExternalLinksValidator.class);

    /** The report for the output. */
    private I_CmsReport m_report;

    /**
     * Checks if the given url is valid.<p>
     *
     * @param check the url to check
     * @param cms a OpenCms context object
     *
     * @return false if the url could not be accessed
     */
    public static boolean checkUrl(CmsObject cms, String check) {

        // first, create an URI from the string representation
        URI uri = null;
        try {
            uri = new CmsUriSplitter(check, true).toURI();
        } catch (URISyntaxException exc) {
            return false;
        }
        try {
            if (!uri.isAbsolute()) {
                return cms.existsResource(cms.getRequestContext().removeSiteRoot(uri.getPath()));
            }
            URL url = uri.toURL();
            String protocol = url.getProtocol();
            if ("http".equals(protocol) || "https".equals(protocol)) {
                // ensure that file is encoded properly
                HttpURLConnection httpcon = (HttpURLConnection)url.openConnection();
                adjustConnection(httpcon);
                int responseCode = httpcon.getResponseCode();
                // accepting all status codes 2xx success and 3xx - redirect
                return ((responseCode >= 200) && (responseCode < 400));
            }
            return true;
        } catch (MalformedURLException mue) {
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Adjust the connection to retrieve the newsletter. Currently, SSL-Certificate verification can be disabled.
     *
     * @param con the connection to manipulate
     */
    private static void adjustConnection(HttpURLConnection con) {

        con.setConnectTimeout(2000); // wait at most two second for the connection
        con.setReadTimeout(8000); // wait at most 8 seconds for reading
        if (con instanceof HttpsURLConnection) {
            HttpsURLConnection httpsUrlConnection = (HttpsURLConnection)con;
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {

                    // do nothing
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {

                    // do nothing
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {

                    return null;
                }
            }};

            // Set the all-trusting trust manager for the connection
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new SecureRandom());
                httpsUrlConnection.setSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                LOG.warn(e, e);
            }

            // do not verify hostnames
            httpsUrlConnection.setHostnameVerifier(new HostnameVerifier() {

                @Override
                public boolean verify(String arg0, SSLSession arg1) {

                    return true;
                }
            });
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
    public String launch(CmsObject cms, Map<String, String> parameters) throws CmsException {

        if (Boolean.valueOf(parameters.get("writeLog")).booleanValue()) {
            m_report = new CmsLogReport(cms.getRequestContext().getLocale(), CmsExternalLinksValidator.class);
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
    @SuppressWarnings("deprecation")
    public void validateLinks(CmsObject cms) throws CmsException {

        if (m_report == null) {
            m_report = new CmsLogReport(cms.getRequestContext().getLocale(), CmsExternalLinksValidator.class);
        }

        m_report.println(
            Messages.get().container(Messages.RPT_VALIDATE_EXTERNAL_LINKS_BEGIN_0),
            I_CmsReport.FORMAT_HEADLINE);

        // get all links

        int pointerId = OpenCms.getResourceManager().getResourceType(
            CmsResourceTypePointer.getStaticTypeName()).getTypeId();
        CmsResourceFilter filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(pointerId);
        List<CmsResource> links = cms.readResources("/", filter);
        Iterator<CmsResource> iterator = links.iterator();
        Map<String, String> brokenLinks = new HashMap<String, String>();

        for (int i = 1; iterator.hasNext(); i++) {
            CmsFile link = cms.readFile(cms.getSitePath(iterator.next()), filter);
            String linkUrl = new String(link.getContents());

            // print to the report
            m_report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_1,
                    Integer.valueOf(i),
                    Integer.valueOf(links.size())),
                I_CmsReport.FORMAT_NOTE);
            m_report.print(Messages.get().container(Messages.RPT_VALIDATE_LINK_0), I_CmsReport.FORMAT_NOTE);
            m_report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    link.getRootPath()));
            m_report.print(Messages.get().container(Messages.GUI_LINK_POINTING_TO_0), I_CmsReport.FORMAT_NOTE);
            m_report.print(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, linkUrl));
            m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            // check link and append it to the list of broken links, if test fails
            if (!checkUrl(cms, linkUrl)) {
                brokenLinks.put(link.getRootPath(), linkUrl);
                m_report.println(Messages.get().container(Messages.RPT_BROKEN_0), I_CmsReport.FORMAT_ERROR);
            } else {
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            }
        }

        m_report.println(
            Messages.get().container(
                Messages.RPT_LINK_VALIDATION_STAT_2,
                Integer.valueOf(links.size()),
                Integer.valueOf(brokenLinks.size())),
            I_CmsReport.FORMAT_HEADLINE);
        m_report.println(
            Messages.get().container(Messages.RPT_VALIDATE_EXTERNAL_LINKS_END_0),
            I_CmsReport.FORMAT_HEADLINE);

        OpenCms.getLinkManager().setPointerLinkValidationResult(new CmsExternalLinksValidationResult(brokenLinks));
    }

}
