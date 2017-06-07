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

package org.opencms.main;

import org.opencms.ade.detailpage.CmsDetailPageResourceHandler;
import org.opencms.db.CmsAlias;
import org.opencms.db.CmsRewriteAliasMatcher;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.gwt.shared.alias.CmsAliasMode;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;
import org.opencms.workplace.CmsWorkplace;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Resource init handler for detail-pages.<p>
 *
 * @since 8.0.0
 */
public class CmsAliasResourceHandler implements I_CmsResourceInit {

    /** The attribute containing the detail content resource. */
    public static final String ATTR_DETAIL_CONTENT_RESOURCE = "__opencms_detail_content_resource";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDetailPageResourceHandler.class);

    /**
     * Default constructor.<p>
     */
    public CmsAliasResourceHandler() {

        // empty
    }

    /**
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(
        CmsResource resource,
        CmsObject cms,
        HttpServletRequest req,
        HttpServletResponse res) throws CmsResourceInitException, CmsSecurityException {

        // check if the resource was already found or the path starts with '/system/'
        boolean abort = (resource != null) || cms.getRequestContext().getUri().startsWith(CmsWorkplace.VFS_PATH_SYSTEM);
        if (abort) {
            // skip in all cases above
            return resource;
        }

        String path = cms.getRequestContext().getUri();
        path = CmsFileUtil.removeTrailingSeparator(path);
        String siteRoot = cms.getRequestContext().getSiteRoot();
        if ("".equals(siteRoot)) {
            siteRoot = OpenCms.getSiteManager().getSiteRoot(path);
            if (siteRoot == null) {
                siteRoot = "";
            }
        }
        try {
            String sitePath = path;
            String oldSiteRoot = cms.getRequestContext().getSiteRoot();
            try {
                cms.getRequestContext().setSiteRoot(siteRoot);
                sitePath = cms.getRequestContext().removeSiteRoot(path);
            } finally {
                cms.getRequestContext().setSiteRoot(oldSiteRoot);
            }
            CmsRewriteAliasMatcher rewriteAliases = OpenCms.getAliasManager().getRewriteAliasMatcher(cms, siteRoot);
            CmsRewriteAliasMatcher.RewriteResult rewriteResult = rewriteAliases.match(sitePath);
            if ((rewriteResult != null) && (res != null)) {
                String link = OpenCms.getLinkManager().substituteLink(cms, rewriteResult.getNewPath());
                if (rewriteResult.getAlias().getMode().isRedirect()) {
                    redirectToTarget(
                        req,
                        res,
                        link,
                        rewriteResult.getAlias().getMode() == CmsAliasMode.permanentRedirect);
                } else {
                    cms.getRequestContext().setUri(rewriteResult.getNewPath());
                }
                return null;
            }
            List<CmsAlias> aliases = OpenCms.getAliasManager().getAliasesForPath(cms, siteRoot, sitePath);
            assert aliases.size() < 2;
            if (aliases.size() == 1) {
                CmsAlias alias = aliases.get(0);
                CmsResource aliasTarget = cms.readResource(alias.getStructureId());

                if (alias.isRedirect()) {
                    String link = OpenCms.getLinkManager().substituteLink(cms, aliasTarget);
                    boolean isPermanent = alias.isPermanentRedirect();
                    // response may be null if we're coming from the locale manager
                    redirectToTarget(req, res, link, isPermanent);
                } else {
                    // not a redirect, just proceed with the aliased resource
                    cms.getRequestContext().setUri(cms.getSitePath(aliasTarget));
                    return aliasTarget;
                }
            }
        } catch (CmsPermissionViolationException e) {
            // trigger the permission denied handler
            throw e;
        } catch (CmsResourceInitException e) {
            // just rethrow so the catch(Throwable e) code isn't executed
            throw e;
        } catch (Throwable e) {
            String uri = cms.getRequestContext().getUri();
            CmsMessageContainer msg = org.opencms.ade.detailpage.Messages.get().container(
                org.opencms.ade.detailpage.Messages.ERR_RESCOURCE_NOT_FOUND_1,
                uri);
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsResourceInitException(msg, e);
        }

        return null;
    }

    /**
     * Helper method for sending a redirect to a new URI.<p>
     *
     * @param req the current request
     * @param res the current response
     * @param link the redirect target
     * @param isPermanent if true, sends a 'moved permanently' redirect
     *
     * @throws IOException
     * @throws CmsResourceInitException
     */
    private void redirectToTarget(HttpServletRequest req, HttpServletResponse res, String link, boolean isPermanent)
    throws IOException, CmsResourceInitException {

        CmsResourceInitException resInitException = new CmsResourceInitException(getClass());
        if (res != null) {
            // preserve request parameters for the redirect
            String query = req.getQueryString();
            if (query != null) {
                link += "?" + query;
            }
            // disable 404 handler
            resInitException.setClearErrors(true);
            if (isPermanent) {
                res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                res.setHeader("Location", link);
            } else {
                res.sendRedirect(link);
            }
        }
        throw resInitException;
    }

}
