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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
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

package org.opencms.main;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.util.CmsUUID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Resource init handler that loads a resource given its permalink.<p>
 *
 * The permalink must have following format:<br>
 * <code>/${CONTEXT}/${SERVLET}/permalink/${UUID}.${EXT}</code><p>
 *
 * for example:<br>
 * <code>/opencms/opencms/permalink/a7b5d298-b3ab-11d8-b3e3-514d35713fed.html</code><p>
 *
 * @since 6.3
 */
public class CmsPermalinkResourceHandler implements I_CmsResourceInit {

    /** Regex for capturing a UUID. */
    public static final String CAPTURE_UUID_REGEX = "(" + CmsUUID.UUID_REGEX + ")";

    /** The permalink handler path. */
    public static final String PERMALINK_HANDLER = "/permalink/";

    /** Regex for the optional file extension. */
    public static final String SUFFIX_REGEX = "(?:\\.[a-zA-Z0-9]*)?$";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPermalinkResourceHandler.class);

    /** The compiled pattern for detail page permalinks. */
    private Pattern m_detailPattern;

    /** The pattern used to match permalink uris and extract the structure id. */
    private Pattern m_simplePermalinkPattern;

    /**
     * Default constructor.<p>
     */
    public CmsPermalinkResourceHandler() {

        String uriRegex = PERMALINK_HANDLER + CAPTURE_UUID_REGEX + SUFFIX_REGEX;
        String detailUriRegex = PERMALINK_HANDLER + CAPTURE_UUID_REGEX + ":" + CAPTURE_UUID_REGEX + SUFFIX_REGEX;
        m_simplePermalinkPattern = Pattern.compile(uriRegex);
        m_detailPattern = Pattern.compile(detailUriRegex);
    }

    /**
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(
        CmsResource resource,
        CmsObject cms,
        HttpServletRequest req,
        HttpServletResponse res) throws CmsResourceInitException, CmsPermissionViolationException {

        // only do something if the resource was not found
        if (resource == null) {
            String uri = cms.getRequestContext().getUri();
            // check if the resource starts with the PERMALINK_HANDLER
            Matcher matcher = m_simplePermalinkPattern.matcher(uri);
            if (matcher.find()) {
                CmsResource resource1 = resource;
                // get the id of the real resource
                String id = matcher.group(1);
                String storedSiteRoot = cms.getRequestContext().getSiteRoot();
                try {
                    // we now must switch to the root site to read the resource
                    cms.getRequestContext().setSiteRoot("/");
                    // read the resource
                    boolean online = cms.getRequestContext().getCurrentProject().isOnlineProject();
                    CmsResourceFilter filter = online ? CmsResourceFilter.DEFAULT : CmsResourceFilter.IGNORE_EXPIRATION;
                    resource1 = cms.readDefaultFile(id, filter);
                } catch (CmsPermissionViolationException e) {
                    throw e;
                } catch (Throwable e) {
                    CmsMessageContainer msg = Messages.get().container(Messages.ERR_PERMALINK_1, id);
                    if (LOG.isErrorEnabled()) {
                        LOG.error(msg.key(), e);
                    }
                    throw new CmsResourceInitException(msg, e);
                } finally {
                    // restore the siteroot
                    cms.getRequestContext().setSiteRoot(storedSiteRoot);
                    // resource may be null in case of an error
                    if (resource1 != null) {
                        // modify the uri to the one of the real resource
                        cms.getRequestContext().setUri(cms.getSitePath(resource1));
                    }
                }
                resource = resource1;
            } else {
                matcher = m_detailPattern.matcher(uri);
                // detail page permalink. Handle the cases 'getI18NInfo' and 'showResource' differently:
                // In the 'showResource' case, we do a redirect to the real detail page URL
                // In the 'getI18NInfo' case, we return the container page so the locale in the CmsRequestContext is set correctly for the 'showResource' case
                if (matcher.find()) {
                    try {
                        CmsUUID pageId = new CmsUUID(matcher.group(1));
                        CmsUUID detailId = new CmsUUID(matcher.group(2));
                        CmsResource pageResource = cms.readResource(pageId);
                        if (res != null) {
                            CmsResource detailResource = cms.readResource(detailId);
                            String detailName = cms.getDetailName(
                                detailResource,
                                cms.getRequestContext().getLocale(), // the locale in the request context should be the locale of the container page
                                OpenCms.getLocaleManager().getDefaultLocales());
                            CmsResource parentFolder;
                            if (pageResource.isFile()) {
                                parentFolder = cms.readParentFolder(pageResource.getStructureId());
                            } else {
                                parentFolder = pageResource;
                            }
                            String baseLink = OpenCms.getLinkManager().substituteLink(cms, parentFolder);
                            String redirectLink = baseLink + (baseLink.endsWith("/") ? "" : "/") + detailName;
                            CmsResourceInitException resInitException = new CmsResourceInitException(getClass());

                            resInitException.setClearErrors(true);
                            res.sendRedirect(redirectLink);
                            throw resInitException;
                        } else {
                            // we're being called from getI18NInfo; assume that the locale for the container page is the locale we want
                            return pageResource;
                        }
                    } catch (CmsResourceInitException e) {
                        throw e;
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                        throw new CmsResourceInitException(getClass());
                    }
                }
                return null;
            }
        }
        return resource;
    }

}