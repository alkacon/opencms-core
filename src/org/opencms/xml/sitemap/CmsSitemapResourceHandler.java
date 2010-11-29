/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapResourceHandler.java,v $
 * Date   : $Date: 2010/11/29 10:33:35 $
 * Version: $Revision: 1.16 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsResourceInitException;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.sitemap.properties.CmsSimplePropertyValue;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Resource init handler that loads a resource given its sitemap's URI.<p>
 *
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.16 $
 * 
 * @since 7.9.2
 */
public class CmsSitemapResourceHandler implements I_CmsResourceInit {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapResourceHandler.class);

    /**
     * Default constructor.<p>
     */
    public CmsSitemapResourceHandler() {

        // empty
    }

    /**
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(CmsResource resource, CmsObject cms, HttpServletRequest req, HttpServletResponse res)
    throws CmsResourceInitException, CmsPermissionViolationException {

        // check if the resource was already found
        boolean abort = (resource != null);
        // check if the resource comes from the root site
        abort |= CmsStringUtil.isEmptyOrWhitespaceOnly(cms.getRequestContext().getSiteRoot());
        // check if the resource comes from the /system/ folder
        abort |= cms.getRequestContext().getUri().startsWith(CmsWorkplace.VFS_PATH_SYSTEM);
        if (abort) {
            // skip in all cases above 
            return resource;
        }

        // check if the resource is in the site map
        try {
            // find the site map entry
            CmsSitemapEntry entry = OpenCms.getSitemapManager().getEntryForUri(cms, cms.getRequestContext().getUri());
            if ((entry == null) || entry.isVfs()) {
                return resource;
            }

            if (entry.isRedirect()) {
                handleSitemapRedirect(cms, res, entry);
            }

            // read the resource
            resource = cms.readResource(entry.getStructureId());
            if (resource != null) {
                // set the request uri to the right file
                cms.getRequestContext().setUri(cms.getSitePath(resource));
                // test if this file is only available for internal access operations
                if (resource.isInternal()) {
                    throw new CmsException(Messages.get().container(
                        org.opencms.main.Messages.ERR_READ_INTERNAL_RESOURCE_1,
                        cms.getRequestContext().getUri()));
                }

                // check online project
                if (cms.getRequestContext().currentProject().isOnlineProject()) {
                    // check if resource is secure
                    String secureProp = entry.getProperties(true).get(CmsPropertyDefinition.PROPERTY_SECURE);
                    boolean secure = Boolean.valueOf(secureProp).booleanValue();

                    if (secure) {
                        // resource is secure, check site config
                        CmsSite site = OpenCms.getSiteManager().getCurrentSite(cms);
                        // check the secure url
                        String secureUrl = null;
                        try {
                            secureUrl = site.getSecureUrl();
                        } catch (Exception e) {
                            LOG.error(Messages.get().getBundle().key(
                                org.opencms.main.Messages.ERR_SECURE_SITE_NOT_CONFIGURED_1,
                                resource.getRootPath()), e);
                            throw new CmsException(Messages.get().container(
                                org.opencms.main.Messages.ERR_SECURE_SITE_NOT_CONFIGURED_1,
                                resource.getRootPath()), e);
                        }
                        boolean usingSec = req.getRequestURL().toString().toUpperCase().startsWith(
                            secureUrl.toUpperCase());
                        if (site.isExclusiveUrl() && !usingSec) {
                            // secure resource without secure protocol, check error config
                            if (site.isExclusiveError()) {
                                // trigger 404 error
                                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                                    org.opencms.main.Messages.ERR_REQUEST_SECURE_RESOURCE_0));
                            } else {
                                // redirect
                                String target = OpenCms.getLinkManager().getOnlineLink(cms, entry.getRootPath());
                                try {
                                    res.sendRedirect(target);
                                    return null;
                                } catch (Exception e) {
                                    // ignore, but should never happen
                                }
                            }
                        }
                    }
                }
            }
            if (req != null) {
                // set the element
                req.setAttribute(CmsSitemapManager.ATTR_SITEMAP_ENTRY, entry);
            }
            // set the resource path
            cms.getRequestContext().setUri(cms.getSitePath(resource));
        } catch (CmsPermissionViolationException e) {
            // trigger the permission denied handler
            throw e;
        } catch (CmsResourceInitException e) {
            throw e;
        } catch (Throwable e) {
            String uri = cms.getRequestContext().getUri();
            CmsMessageContainer msg = Messages.get().container(Messages.ERR_SITEMAP_1, uri);
            LOG.error(msg.key(), e);
            throw new CmsResourceInitException(msg, e);
        }
        return resource;
    }

    /**
     * Handles a request to a sitemap entry which has a redirect.<p>
     *     
     * @param cms the CMS context 
     * @param res the response 
     * @param entry the sitemap entry
     *  
     * @throws IOException if something goes wrong 
     * @throws CmsResourceInitException if the redirect succeeds 
     */
    private void handleSitemapRedirect(CmsObject cms, HttpServletResponse res, CmsSitemapEntry entry)
    throws IOException, CmsResourceInitException {

        Map<String, String> props = entry.getProperties();
        String extKey = CmsSitemapManager.Property.externalRedirect.getName();
        String intKey = CmsSitemapManager.Property.internalRedirect.getName();
        String extProp = props.get(extKey);
        String intProp = props.get(intKey);
        boolean hasInt = !CmsStringUtil.isEmptyOrWhitespaceOnly(intProp);
        CmsLink actualLink = null;

        String target = "";
        if (hasInt) {
            CmsUUID structureId = new CmsUUID(intProp);
            // use "abcd" as a dummy URI so that the link URI if filled in correctly given the structure id  
            actualLink = new CmsLink(null, CmsRelationType.HYPERLINK, structureId, "abcd", true);
        } else {
            actualLink = new CmsLink(null, CmsRelationType.HYPERLINK, null, extProp, false);
        }
        target = actualLink.getLink(cms);
        res.sendRedirect(target);
        throw new CmsResourceInitException(new CmsMessageContainer(
            Messages.get(),
            Messages.LOG_SITEMAP_REDIRECT_1,
            target));
    }
}