/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/Attic/CmsSitemapLinkStrategyHandler.java,v $
 * Date   : $Date: 2010/12/21 10:59:56 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlSitemap;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.sitemap.CmsSitemapEntry;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Sitemap link strategy.<p>
 *
 * @author  Ruediger Kurz
 *
 * @version $Revision: 1.10 $ 
 * 
 * @since 8.0.0
 */
public class CmsSitemapLinkStrategyHandler extends A_CmsLinkStrategyHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapLinkStrategyHandler.class);

    /**
     * @see org.opencms.staticexport.I_CmsLinkStrategyHandler#getRfsName(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getRfsName(CmsObject cms, String vfsName, String parameters) {

        String rfsName = vfsName;

        try {
            // if the path starts with "/system/" the sitemap export name isn't relevant
            if (!vfsName.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
                CmsSitemapEntry sitemapEntry = OpenCms.getSitemapManager().getEntryForUri(cms, vfsName);
                if (sitemapEntry.isVfs()) {
                    String sitemapUri = null;
                    CmsResource res = cms.readResource(vfsName);
                    if (CmsResourceTypeXmlContent.isXmlContent(res)) {
                        String detailViewProp = null;
                        detailViewProp = cms.readPropertyObject(
                            res,
                            CmsPropertyDefinition.PROPERTY_ADE_SITEMAP_DETAILVIEW,
                            true).getValue("");
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(detailViewProp)) {
                            String detailName = cms.readNewestUrlNameForId(res.getStructureId());
                            if (detailName == null) {
                                detailName = res.getStructureId().toString();
                            }
                            // if detail view found, adjust the uri
                            sitemapUri = detailViewProp;
                            if (!sitemapUri.endsWith("/")) {
                                rfsName += "/";
                            }
                            sitemapUri += detailName;
                            sitemapUri += "/";
                        }
                    }
                    if (sitemapUri != null) {
                        sitemapUri = OpenCms.getSiteManager().getSiteRoot(res.getRootPath()) + sitemapUri;
                        sitemapEntry = OpenCms.getSitemapManager().getEntryForUri(cms, sitemapUri);
                        vfsName = sitemapEntry.getRootPath();
                    }
                }
                // get the export name of the according sitemap
                String exportname = OpenCms.getSitemapManager().getExportnameForSiteRoot(
                    OpenCms.getSiteManager().getSiteRoot(sitemapEntry.getRootPath()));
                String storedSiteRoot = cms.getRequestContext().getSiteRoot();
                try {
                    String siteRoot = OpenCms.getSiteManager().getSiteRoot(sitemapEntry.getRootPath());
                    if (siteRoot != null) {
                        cms.getRequestContext().setSiteRoot(siteRoot);
                    }
                    if ((exportname != null) && sitemapEntry.isSitemap()) {
                        // if an export name was found replace the site root with the found export name
                        rfsName = CmsStringUtil.joinPaths("/" + exportname + "/" + sitemapEntry.getSitePath(cms));
                    } else {
                        rfsName = sitemapEntry.getRootPath();
                    }
                } finally {
                    cms.getRequestContext().setSiteRoot(storedSiteRoot);
                }
            } else {
                rfsName = getRfsNameWithExportName(cms, vfsName);
            }

            String extension = CmsFileUtil.getExtension(rfsName);
            // check if the VFS resource is a JSP page with a ".jsp" ending 
            // in this case the rfs name suffix must be build with special care,
            // usually it must be set to ".html"             
            boolean isJsp = extension.equals(".jsp");
            if (isJsp) {
                String suffix = null;
                try {
                    CmsResource res = cms.readResource(vfsName);
                    isJsp = (CmsResourceTypeJsp.isJSP(res));
                    // if the resource is a plain resource then no change in suffix is required
                    if (isJsp) {
                        suffix = cms.readPropertyObject(vfsName, CmsPropertyDefinition.PROPERTY_EXPORTSUFFIX, true).getValue(
                            ".html");
                    }
                } catch (CmsVfsResourceNotFoundException e) {
                    // resource has been deleted, so we are not able to get the right extension from the properties
                    // try to figure out the right extension from file system
                    File rfsFile = new File(CmsFileUtil.normalizePath(OpenCms.getStaticExportManager().getExportPath(
                        cms.getRequestContext().addSiteRoot(vfsName))
                        + rfsName));
                    File parent = rfsFile.getParentFile();
                    if (parent != null) {
                        File[] paramVariants = parent.listFiles(new CmsPrefixFileFilter(rfsFile.getName()));
                        if ((paramVariants != null) && (paramVariants.length > 0)) {
                            // take the first
                            suffix = paramVariants[0].getAbsolutePath().substring(rfsFile.getAbsolutePath().length());
                        }
                    } else {
                        // if no luck, try the default extension
                        suffix = ".html";
                    }
                }
                if ((suffix != null) && !extension.equals(suffix.toLowerCase())) {
                    rfsName += suffix;
                    extension = suffix;
                }
            }
            if (parameters != null) {
                // build the RFS name for the link with parameters
                if (CmsResource.isFolder(rfsName)) {
                    rfsName = OpenCms.getStaticExportManager().addDefaultFileNameToFolder(
                        rfsName,
                        !CmsResource.isFolder(vfsName));
                }
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(extension)) {
                    extension = ".html";
                }
                rfsName = CmsFileUtil.getRfsPath(rfsName, extension, parameters);
                // we have found a rfs name for a vfs resource with parameters, save it to the database
                try {
                    cms.writeStaticExportPublishedResource(
                        rfsName,
                        CmsStaticExportManager.EXPORT_LINK_WITH_PARAMETER,
                        parameters,
                        System.currentTimeMillis());
                } catch (CmsException e) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_WRITE_FAILED_1, rfsName), e);
                }
            }
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            // ignore exception, return vfsName as rfsName
            rfsName = vfsName;
        }

        // add export rfs prefix and return result
        if (!vfsName.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
            return OpenCms.getStaticExportManager().getRfsPrefix(cms.getRequestContext().addSiteRoot(vfsName)).concat(
                rfsName);
        } else {
            // check if we are generating a link to a related resource in the same rfs rule
            String source = cms.getRequestContext().addSiteRoot(cms.getRequestContext().getUri());
            Iterator<CmsStaticExportRfsRule> it = OpenCms.getStaticExportManager().getRfsRules().iterator();
            while (it.hasNext()) {
                CmsStaticExportRfsRule rule = it.next();
                if (rule.getSource().matcher(source).matches() && rule.match(vfsName)) {
                    return rule.getRfsPrefix().concat(rfsName);
                }
            }
            // this is a link across rfs rules 
            return OpenCms.getStaticExportManager().getRfsPrefix(cms.getRequestContext().getSiteRoot() + "/").concat(
                rfsName);
        }
    }

    /**
     * @see org.opencms.staticexport.I_CmsLinkStrategyHandler#getVfsNameInternal(org.opencms.file.CmsObject, java.lang.String)
     */
    public CmsStaticExportData getVfsNameInternal(CmsObject cms, String rfsName) throws CmsVfsResourceNotFoundException {

        String storedSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("/");

            for (Map.Entry<String, String> exportNameRes : OpenCms.getStaticExportManager().getExportnames().entrySet()) {
                if (rfsName.startsWith(exportNameRes.getKey())) {
                    String folderName = exportNameRes.getValue();
                    String vfsPart = rfsName.substring(exportNameRes.getKey().length());
                    String vfsName = null;
                    if (!CmsResource.isFolder(folderName) && cms.existsResource(folderName)) {
                        CmsResource res = cms.readResource(folderName);
                        if (CmsResourceTypeXmlSitemap.isSitemap(res)) {
                            vfsName = OpenCms.getSiteManager().getSiteRoot(res.getRootPath()) + "/" + vfsPart;
                        }
                    } else {
                        vfsName = folderName + vfsPart;
                    }
                    try {
                        if (vfsName != null) {
                            return OpenCms.getStaticExportManager().readResource(cms, vfsName);
                        }
                    } catch (CmsException e1) {
                        // continue with trying out the other exportname to find a match (may be a multiple prefix)
                        continue;
                    }
                }
            }
            // try to read name of export resource by reading the resource directly
            try {
                return OpenCms.getStaticExportManager().readResource(cms, rfsName);
            } catch (Throwable t) {
                // resource not found
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(Messages.ERR_EXPORT_FILE_FAILED_1, new String[] {rfsName}),
                        t);
                }
            }

            // finally check if its a modified jsp resource        
            int extPos = rfsName.lastIndexOf('.');
            // first cut of the last extension
            if (extPos >= 0) {
                String cutName = rfsName.substring(0, extPos);
                int pos = cutName.lastIndexOf('.');
                if (pos >= 0) {
                    // now check if remaining String ends with ".jsp"
                    String extension = cutName.substring(pos).toLowerCase();
                    if (".jsp".equals(extension)) {
                        return getVfsNameInternal(cms, cutName);
                    }
                }
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            cms.getRequestContext().setSiteRoot(storedSiteRoot);
        }
        throw new CmsVfsResourceNotFoundException(Messages.get().container(Messages.ERR_CREATE_FOLDER_1, rfsName));
    }

    /**
     * @see org.opencms.staticexport.I_CmsLinkStrategyHandler#isExportLink(org.opencms.file.CmsObject, java.lang.String)
     */
    public boolean isExportLink(CmsObject cms, String vfsName) {

        String cacheKey = OpenCms.getStaticExportManager().getCacheKey(cms.getRequestContext().getSiteRoot(), vfsName);
        Boolean exportResource = OpenCms.getStaticExportManager().getCacheExportLinks().get(cacheKey);
        if (exportResource != null) {
            return exportResource.booleanValue();
        }
        boolean result = false;
        CmsSite currentSite = OpenCms.getSiteManager().getSite(vfsName, cms.getRequestContext().getSiteRoot());

        // if the site is exclusive secured and the link is a secure link this resource is not to be exported
        if (currentSite.isExclusiveUrl() && isSecureLink(cms, vfsName)) {
            result = false;
        } else {
            // ! otherwise check if we have an export link
            try {
                // static export must always be checked with the export users permissions,
                // not the current users permissions
                CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
                exportCms.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());

                // read the export property from the sitemap or from the VFS
                CmsSitemapEntry entry = null;
                try {
                    entry = OpenCms.getSitemapManager().getEntryForUri(exportCms, vfsName);
                } catch (Exception e) {
                    // no sitemap entry found: go on
                    LOG.debug(e.getLocalizedMessage(), e);
                }

                // get the detail id from the vfs name
                String path = vfsName;
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
                String detailId = "";
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(path)) {
                    detailId = CmsResource.getName(path);
                }

                if ((entry != null) && entry.isSitemap()) {
                    // if the entry was found and it is a sitemap entry
                    // read the export property from the sitemap entry
                    Map<String, String> props = entry.getProperties(true);
                    if ((props.get(CmsPropertyDefinition.PROPERTY_EXPORT) != null)
                        && props.get(CmsPropertyDefinition.PROPERTY_EXPORT).equals(CmsStringUtil.TRUE)) {
                        result = true;
                    }
                } else {
                    // let's look up export property in VFS
                    String exportValue;
                    CmsResource res;
                    if (CmsUUID.isValidUUID(detailId)) {
                        res = exportCms.readResource(new CmsUUID(detailId));
                    } else {
                        res = exportCms.readResource(vfsName);
                    }
                    exportValue = exportCms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_EXPORT, true).getValue();
                    if (exportValue == null) {
                        // no setting found for "export" property
                        if (OpenCms.getStaticExportManager().getExportPropertyDefault()) {
                            // if the default is "true" we always export
                            result = true;
                        } else {
                            // check if the resource is exportable by suffix
                            result = OpenCms.getStaticExportManager().isSuffixExportable(vfsName);
                        }
                    } else {
                        // "export" value found, if it was "true" we export
                        result = Boolean.valueOf(exportValue).booleanValue();
                    }
                }
            } catch (CmsException e) {
                // no export required (probably security issues, e.g. no access for export user)
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        OpenCms.getStaticExportManager().getCacheExportLinks().put(cacheKey, Boolean.valueOf(result));

        return result;
    }

    /**
     * @see org.opencms.staticexport.I_CmsLinkStrategyHandler#isSecureLink(org.opencms.file.CmsObject, java.lang.String)
     */
    public boolean isSecureLink(CmsObject cms, String vfsName) {

        if (!cms.getRequestContext().currentProject().isOnlineProject()) {
            return false;
        }
        String cacheKey = OpenCms.getStaticExportManager().getCacheKey(cms.getRequestContext().getSiteRoot(), vfsName);
        Boolean secureResource = OpenCms.getStaticExportManager().getCacheSecureLinks().get(cacheKey);
        if (secureResource == null) {
            // read the export property from the sitemap or from the VFS
            CmsSitemapEntry entry = null;
            try {
                entry = OpenCms.getSitemapManager().getEntryForUri(cms, vfsName);
            } catch (Exception e) {
                // no sitemap entry found: go on
                // no secure link required (probably security issues, e.g. no access for current user)
                // however other users may be allowed to read the resource, so the result can't be cached
                secureResource = Boolean.FALSE;
                LOG.debug(e.getLocalizedMessage(), e);
            }
            if ((entry != null) && entry.isSitemap()) {
                String prop = entry.getProperties(true).get(CmsPropertyDefinition.PROPERTY_SECURE);
                secureResource = Boolean.valueOf(prop);
                // only cache result if read was successfull
                OpenCms.getStaticExportManager().getCacheSecureLinks().put(cacheKey, secureResource);
            } else {
                try {
                    String secureProp = cms.readPropertyObject(vfsName, CmsPropertyDefinition.PROPERTY_SECURE, true).getValue();
                    secureResource = Boolean.valueOf(secureProp);
                    // only cache result if read was successfull
                    OpenCms.getStaticExportManager().getCacheSecureLinks().put(cacheKey, secureResource);
                } catch (CmsVfsResourceNotFoundException e) {
                    secureResource = Boolean.FALSE;
                    // resource does not exist, no secure link will be required for any user
                    OpenCms.getStaticExportManager().getCacheSecureLinks().put(cacheKey, secureResource);
                } catch (Exception e) {
                    // no secure link required (probably security issues, e.g. no access for current user)
                    // however other users may be allowed to read the resource, so the result can't be cached
                    secureResource = Boolean.FALSE;
                }
            }
        }
        return secureResource.booleanValue();
    }
}
