/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/validation/Attic/CmsHtmlLinkValidator.java,v $
 * Date   : $Date: 2004/11/22 18:03:06 $
 * Version: $Revision: 1.24 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Validates HTML links in the (body) content of Cms resources in the OpenCms VFS. HTML links are 
 * considered as href attribs in anchor tags and src attribs in image tags.<p>
 * 
 * Validating links means to answer the question, whether we would have broken links in the 
 * online project if a file or a list of files would get published. External links to targets 
 * outside the OpenCms VFS don't get validated.<p>
 * 
 * Objects using the CmsHtmlLinkValidator are responsible to handle detected broken links.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.24 $ $Date: 2004/11/22 18:03:06 $
 * @since 5.3.0
 */
public class CmsHtmlLinkValidator extends Object {

    /**
     * The driver manager.<p>
     */
    protected CmsDriverManager m_driverManager;

    /**
     * Default constructor.<p>
     * 
     * @param driverManager The Cms driver manager
     */
    public CmsHtmlLinkValidator(CmsDriverManager driverManager) {
        m_driverManager = driverManager;
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        try {
            m_driverManager = null;
        } catch (Throwable t) {
            // ignore
        }

        super.finalize();
    }

    /**
     * Validates HTML links (hrefs and img tags) in the (body) content of the specified list of Cms resources.<p>
     * 
     * The result is printed to a shell report.<p>
     * 
     * @param cms the current user's Cms object the current request context
     * @param offlineResources a list of offline Cms resources
     * @return a Map with Lists of invalid hrefs keyed by resource names
     */
    public Map validateResources(CmsObject cms, List offlineResources) {
        return validateResources(cms, offlineResources, new CmsShellReport());
    }

    /**
     * Validates HTML links (hrefs and img tags) in the (body) content of the specified list of Cms resources.<p>
     * 
     * The result is printed to the given report.<p>
     * 
     * @param cms the current user's Cms object
     * @param offlineResources a list of offline Cms resources
     * @param report an instance of I_CmsReport to print messages
     * @return a map with lists of invalid links keyed by resource names
     */
    public Map validateResources(CmsObject cms, List offlineResources, I_CmsReport report) {
        CmsResource resource = null;
        List brokenLinks = null;
        Map offlineFilesLookup = null;
        List links = null;
        List validatableResources = null;
        Map invalidResources = new HashMap();
        String resourceName = null;
        int i = I_CmsConstants.C_UNKNOWN_ID, j = I_CmsConstants.C_UNKNOWN_ID;
        I_CmsResourceType resourceType = null;
        boolean foundBrokenLinks = false;

        report.println(report.key("report.htmllink_validator.begin"), I_CmsReport.C_FORMAT_HEADLINE);

        // populate a lookup map with the offline resources that 
        // actually get published keyed by their resource names.
        // second, resources that don't get validated are ignored.
        offlineFilesLookup = new HashMap();
        validatableResources = new ArrayList();
        for (i = 0; i < offlineResources.size(); i++) {
            resource = (CmsResource) offlineResources.get(i);
            offlineFilesLookup.put(resource.getRootPath(), resource);

            try {
                if ((resourceType = OpenCms.getResourceManager().getResourceType(resource.getTypeId())) instanceof I_CmsHtmlLinkValidatable) {
                    if (resource.getState() != I_CmsConstants.C_STATE_DELETED) {
                        // don't validate links on deleted resources
                        validatableResources.add(resource);
                    }
                }
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error retrieving resource type of " + resourceName, e);
                }
            }
        }

        foundBrokenLinks = false;
        for (i = 0, j = validatableResources.size(); i < j; i++) {
            try {
                brokenLinks = null;
                resource = (CmsResource) validatableResources.get(i);
                resourceName = resource.getRootPath();
                resourceType = OpenCms.getResourceManager().getResourceType(resource.getTypeId());

                report.print("( " + (i + 1) + " / " + j + " ) " + report.key("report.htmllink_validator.validating"), I_CmsReport.C_FORMAT_NOTE);
                report.print(cms.getRequestContext().removeSiteRoot(resourceName));
                report.print(report.key("report.dots"));

                links = ((I_CmsHtmlLinkValidatable) resourceType).findLinks(cms, resource);

                if (links.size() > 0) {
                    brokenLinks = validateLinks(links, offlineFilesLookup);
                }

                if (brokenLinks != null && brokenLinks.size() > 0) {
                    // the resource contains broken links
                    invalidResources.put(resourceName, brokenLinks);
                    foundBrokenLinks = true;
                    report.println(report.key("report.htmllink_validator.found_broken_links"), I_CmsReport.C_FORMAT_WARNING);
                } else {
                    // the resource contains *NO* broken links
                    report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                }
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Error finding links in " + resourceName, e);
                }
            }
        }

        if (foundBrokenLinks) {
            // print a summary if we found broken links in the validated resources
            report.println(report.key("report.htmllink_validator.summary.begin"), I_CmsReport.C_FORMAT_HEADLINE);

            Iterator outer = invalidResources.keySet().iterator();
            while (outer.hasNext()) {
                resourceName = (String) outer.next();
                brokenLinks = (List) invalidResources.get(resourceName);

                report.print(report.key("report.htmllink_validator.summary.broken_links_in"), I_CmsReport.C_FORMAT_NOTE);
                report.print(resourceName);
                report.println("\u0020:");
                Iterator inner = brokenLinks.iterator();
                while (inner.hasNext()) {
                    report.println("\u0020" + (String) inner.next(), I_CmsReport.C_FORMAT_WARNING);
                }
            }

            report.println(report.key("report.htmllink_validator.summary.end"), I_CmsReport.C_FORMAT_HEADLINE);
            report.println(report.key("report.htmllink_validator.error"), I_CmsReport.C_FORMAT_ERROR);
        }

        report.println(report.key("report.htmllink_validator.end"), I_CmsReport.C_FORMAT_HEADLINE);

        return invalidResources;
    }

    /**
     * Validates the URIs in the specified link list.<p>
     * 
     * @param links a list of URIs inside a CmsResource
     * @param offlineFileLookup a map for faster lookup with all (offline) resources that get actually published keyed by their resource names (including the site root)
     * @return a list with the broken links in the specified link list, or an empty list if no broken links were found
     */
    protected List validateLinks(List links, Map offlineFileLookup) {
        List brokenLinks = new ArrayList();
        String link = null;
        boolean isValidLink = true;
        CmsResource unpublishedResource = null;
        List validatedLinks = new ArrayList();

        Iterator i = links.iterator();
        while (i.hasNext()) {
            link = ((String) i.next()).trim();
            isValidLink = true;

            if (validatedLinks.contains(link) || "".equals(link)) {
                // skip links that are already validated or empty
                continue;
            }

            // the link is valid...

            try {
                // ... if the linked resource exists in the online project
                m_driverManager.getVfsDriver().readResource(
                    new CmsDbContext(), 
                    I_CmsConstants.C_PROJECT_ONLINE_ID, 
                    link, true);
                
                // ... and if the linked resource in the online project won't get deleted if it gets actually published
                if (offlineFileLookup.containsKey(link)) {
                    unpublishedResource = (CmsResource) offlineFileLookup.get(link);

                    if (unpublishedResource.getState() == I_CmsConstants.C_STATE_DELETED) {
                        isValidLink = false;
                    }
                }
            } catch (CmsException e) {
                // ... or if the linked resource is a resource that gets actually published
                if (!offlineFileLookup.containsKey(link)) {
                    isValidLink = false;
                }
            }

            if (!isValidLink) {
                brokenLinks.add(link);
            }

            validatedLinks.add(link);
        }

        return brokenLinks;
    }

}
