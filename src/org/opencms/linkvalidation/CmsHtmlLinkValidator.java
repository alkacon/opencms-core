/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/linkvalidation/Attic/CmsHtmlLinkValidator.java,v $
 * Date   : $Date: 2004/01/19 09:20:29 $
 * Version: $Revision: 1.1 $
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

package org.opencms.linkvalidation;

import org.opencms.db.CmsDriverManager;
import org.opencms.main.OpenCms;
import org.opencms.page.CmsPageException;
import org.opencms.page.CmsXmlPage;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkTable;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsResourceTypePlain;
import com.opencms.file.CmsResourceTypeXmlPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates HTML links (hrefs and img attribs. in anchor and image tags) in the (body) content of 
 * files.<p>
 * 
 * Only plain (text) files and XML pages are supported by this validator currently.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2004/01/19 09:20:29 $
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
     * @param offlineFiles a list of Cms resources
     * @return a Map with Lists of invalid hrefs keyed by resource names
     */
    public Map validateResources(CmsObject cms, List offlineFiles) {
        return validateResources(cms, offlineFiles, new CmsShellReport());
    }

    /**
     * Validates HTML links (hrefs and img tags) in the (body) content of the specified list of Cms resources.<p>
     * 
     * The result is printed to the given report.<p>
     * 
     * @param cms the current user's Cms object
     * @param offlineFiles a list of Cms resources
     * @param report an instance of I_CmsReport to print messages
     * @return a map with lists of invalid links keyed by resource names
     */
    public Map validateResources(CmsObject cms, List offlineFiles, I_CmsReport report) {
        CmsResource resource = null;
        int resourceType = I_CmsConstants.C_UNKNOWN_ID;
        List brokenLinks = null;
        Map offlineFilesLookup = null;
        List links = null;
        Map invalidResources = (Map) new HashMap();
        String resourceName = null;
        int index = I_CmsConstants.C_UNKNOWN_ID;

        report.println(report.key("report.linkvalidation.begin"), I_CmsReport.C_FORMAT_HEADLINE);

        // populate a lookup list with the offline resource names that actually get published
        offlineFilesLookup = (Map) new HashMap();
        for (index = 0; index < offlineFiles.size(); index++) {
            resource = (CmsResource) offlineFiles.get(index);
            offlineFilesLookup.put(resource.getRootPath(), resource);
        }

        Iterator i = offlineFiles.iterator();
        while (i.hasNext()) {
            brokenLinks = null;
            resource = (CmsResource) i.next();
            resourceType = resource.getType();
            resourceName = resource.getRootPath();

            if (resource.getState() == I_CmsConstants.C_STATE_DELETED) {
                // continue if the current resource is deleted
                continue;
            }

            if (resourceType != CmsResourceTypePlain.C_RESOURCE_TYPE_ID && resourceType != CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID) {
                // continue if the current resource type won't get handled here
                continue;
            }

            report.print(report.key("report.linkvalidation.file"), I_CmsReport.C_FORMAT_NOTE);
            report.print(resourceName);
            report.print(report.key("report.dots"));

            // the (body) content of plain and xml page resources gets validated            
            if (resourceType == CmsResourceTypePlain.C_RESOURCE_TYPE_ID) {
                links = findPlainLinks(cms, resource);
            } else if (resourceType == CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID) {
                links = findXmlPageLinks(cms, resource);
            }

            if (links.size() > 0) {
                brokenLinks = validateLinks(links, offlineFilesLookup);
            }

            if (brokenLinks != null && brokenLinks.size() > 0) {
                // the resource contains broken links
                invalidResources.put(resourceName, brokenLinks);
                report.println(report.key("report.linkvalidation.file.has_broken_links"), I_CmsReport.C_FORMAT_WARNING);
            } else {
                // the resource contains *NO* broken links
                report.println(report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
            }
        }

        report.println(report.key("report.linkvalidation.end"), I_CmsReport.C_FORMAT_HEADLINE);

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
        List brokenLinks = (List) new ArrayList();
        String link = null;
        boolean isValidLink = true;
        CmsResource unpublishedResource = null;
        List validatedLinks = (List) new ArrayList();

        Iterator i = links.iterator();
        while (i.hasNext()) {
            link = ((String) i.next()).trim();
            isValidLink = true;
            
            if (validatedLinks.contains(link)) {
                // skip links that are already validated
                continue;
            }

            if (link.startsWith("http") || link.startsWith("HTTP")) {
                // skip external WWW resources
                continue;
            }

            // the link is valid...

            try {
                // ... if the linked resource exists in the online project
                m_driverManager.readFileHeaderInProject(I_CmsConstants.C_PROJECT_ONLINE_ID, link, false);

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

    /**
     * Returns a list with the URIs of all linked resources (either via href attribs or img tags) 
     * inside the specified file content string.<p>
     * 
     * @param cms the current user's Cms object
     * @param resource a CmsResource with links
     * @return a list with the URIs of all linked resources (either via href attribs or img tags)
     */
    protected List findPlainLinks(CmsObject cms, CmsResource resource) {
        List links = (List) new ArrayList();
        String link = null;
        Pattern pattern = null;
        Matcher matcher = null;
        String encoding = null;
        String defaultEncoding = null;
        CmsFile file = null;
        String content = null;

        try {
            file = m_driverManager.readFile(cms.getRequestContext(), resource.getRootPath());
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error reading file content of " + resource.getRootPath(), e);
            }

            return Collections.EMPTY_LIST;
        }

        try {
            defaultEncoding = cms.getRequestContext().getEncoding();
            encoding = m_driverManager.readProperty(cms.getRequestContext(), resource.getRootPath(), cms.getRequestContext().getAdjustedSiteRoot(resource.getRootPath()), I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, true, defaultEncoding);
            content = new String(file.getContents(), encoding);
        } catch (Exception e) {
            content = new String(file.getContents());
        }

        // regex pattern to find all src attribs in img tags, plus all href attribs in anchor tags
        // don't forget to update the group index on the matcher after changing the regex!
        int flags = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;
        pattern = Pattern.compile("<(img|a)(\\s+)(.*?)(src|href)=(\"|\')(.*?)(\"|\')(.*?)>", flags);

        matcher = pattern.matcher(content);
        while (matcher.find()) {
            link = matcher.group(6);

            if (link.length() > 0 && !link.startsWith("]") && !link.endsWith("[")) {
                // skip href or src attribs split inside CDATA sections by the XML template mechanism
                links.add(link);
            }
        }

        return links;
    }

    /**
     * Returns a list with the URIs of all linked resources (either via href attribs or img tags)
     * listed in the link table of a XML page.<p>
     * 
     * @param cms the current user's Cms object
     * @param resource a CmsResource with links
     * @return a list with the URIs of all linked resources (either via href attribs or img tags)
     */
    protected List findXmlPageLinks(CmsObject cms, CmsResource resource) {
        List links = (List) new ArrayList();
        CmsFile file = null;
        CmsXmlPage xmlPage = null;
        Set languages = null;
        String languageName = null;
        List elementNames = null;
        String elementName = null;
        CmsLinkTable linkTable = null;
        String linkName = null;
        CmsLink link = null;

        try {
            file = m_driverManager.readFile(cms.getRequestContext(), resource.getRootPath());
        } catch (CmsException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error reading file content of " + resource.getRootPath(), e);
            }

            return Collections.EMPTY_LIST;
        }

        try {
            xmlPage = CmsXmlPage.read(cms, file);
            languages = xmlPage.getLanguages();

            // iterate over all languages
            Iterator i = languages.iterator();
            while (i.hasNext()) {
                languageName = (String) i.next();
                elementNames = xmlPage.getNames(languageName);

                // iterate over all body elements per language
                Iterator j = elementNames.iterator();
                while (j.hasNext()) {
                    elementName = (String) j.next();
                    linkTable = xmlPage.getLinkTable(elementName, languageName);

                    // iterate over all links inside a body element
                    Iterator k = linkTable.iterator();
                    while (k.hasNext()) {
                        linkName = (String) k.next();
                        link = linkTable.getLink(linkName);

                        // external links are ommitted
                        if (link.isInternal()) {
                            links.add(link.getTarget());
                        }
                    }
                }
            }
        } catch (CmsPageException e) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error processing HTML content of " + resource.getRootPath(), e);
            }

            return Collections.EMPTY_LIST;
        }

        return links;
    }

}
