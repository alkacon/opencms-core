/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/Attic/CmsVfsLinkStrategyHandler.java,v $
 * Date   : $Date: 2010/10/20 15:22:48 $
 * Version: $Revision: 1.3 $
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
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.workplace.CmsWorkplace;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * VFS link strategy.<p>
 *
 * @author  Ruediger Kurz
 *
 * @version $Revision: 1.3 $ 
 * 
 * @since 8.0.0
 */
public class CmsVfsLinkStrategyHandler extends A_CmsLinkStrategyHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsLinkStrategyHandler.class);

    /**
     * @see org.opencms.staticexport.I_CmsLinkStrategyHandler#getRfsName(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getRfsName(CmsObject cms, String vfsName, String parameters) {

        String rfsName = getRfsNameWithExportName(cms, vfsName);
        try {
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
    public CmsStaticExportData getVfsNameInternal(CmsObject cms, String rfsName) {

        String storedSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("/");

            // try to find a match with the "exportname" folders
            String path = rfsName;
            // in case of folders, remove the trailing slash
            // in case of files, remove the filename and trailing slash
            path = path.substring(0, path.lastIndexOf('/'));
            // cache the export names
            Map<String, String> exportnameResources = OpenCms.getStaticExportManager().getExportnames();
            while (true) {
                // exportnameResources are only folders!
                String expName = exportnameResources.get(path + '/');
                if (expName == null) {
                    if (path.length() == 0) {
                        break;
                    }
                    path = path.substring(0, path.lastIndexOf('/'));
                    continue;
                }
                // this will be a root path!
                String vfsName = expName + rfsName.substring(path.length() + 1);
                try {
                    return OpenCms.getStaticExportManager().readResource(cms, vfsName);
                } catch (CmsVfsResourceNotFoundException e) {
                    // if already checked all parts of the path we can stop here. 
                    // This is the case if the "/" is set as "exportname" on any vfs resource
                    if (path.length() == 0) {
                        break;
                    }
                    // continue with trying out the other exportname to find a match (may be a multiple prefix)
                    path = path.substring(0, path.lastIndexOf('/'));
                    continue;
                } catch (CmsException e) {
                    // should never happen
                    LOG.error(e.getLocalizedMessage(), e);
                    break;
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
            return null;
        } finally {
            cms.getRequestContext().setSiteRoot(storedSiteRoot);
        }
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
        try {
            // static export must always be checked with the export users permissions,
            // not the current users permissions
            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            exportCms.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());
            // let's look up export property in VFS
            String exportValue = exportCms.readPropertyObject(vfsName, CmsPropertyDefinition.PROPERTY_EXPORT, true).getValue();
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
        } catch (CmsException e) {
            // no export required (probably security issues, e.g. no access for export user)
            LOG.debug(e.getLocalizedMessage(), e);
        }
        OpenCms.getStaticExportManager().getCacheExportLinks().put(cacheKey, Boolean.valueOf(result));

        return result;
    }

    /**
     * @see org.opencms.staticexport.I_CmsLinkStrategyHandler#isSecureLink(org.opencms.file.CmsObject, java.lang.String)
     */
    @Override
    public boolean isSecureLink(CmsObject cms, String vfsName) {

        if (!cms.getRequestContext().currentProject().isOnlineProject()) {
            return false;
        }
        String cacheKey = OpenCms.getStaticExportManager().getCacheKey(cms.getRequestContext().getSiteRoot(), vfsName);
        Boolean secureResource = OpenCms.getStaticExportManager().getCacheSecureLinks().get(cacheKey);
        if (secureResource == null) {
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
        return secureResource.booleanValue();
    }
}
