/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.staticexport;

import org.opencms.db.storage.I_CmsStorageDelivery;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsStoredContentInfo;
import org.opencms.loader.CmsStoredContentDeliveryHelper;
import org.opencms.loader.I_CmsStaticExportDirectResponseLoader;
import org.opencms.loader.I_CmsStaticExportStreamLoader;
import org.opencms.loader.I_CmsStoredContentDirectDeliveryLoader;
import org.opencms.main.CmsException;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Support class for direct delivery of stored content during static export.<p>
 */
public class CmsStoredContentDeliverySupport {

    /**
     * Handles a streamed static export through direct stored content delivery if possible.<p>
     *
     * @param manager the static export manager
     * @param loader the resource loader
     * @param cms the CMS context
     * @param exportResource the resource to export
     * @param req the current request
     * @param res the current response
     * @param exportFileName the target export file name
     *
     * @return <code>true</code> if the export has been handled without writing the local export file
     *
     * @throws CmsException if something goes wrong
     */
    public boolean handle(
        CmsStaticExportManager manager,
        CmsStoredContentDeliveryConfiguration configuration,
        I_CmsStaticExportStreamLoader loader,
        CmsObject cms,
        CmsResource exportResource,
        HttpServletRequest req,
        HttpServletResponse res,
        String exportFileName)
    throws CmsException {

        if (tryHandleStoredContentDelivery(
            manager,
            configuration,
            loader,
            cms,
            exportResource,
            req,
            res,
            exportFileName)) {
            return true;
        }
        if (!configuration.isSuffixEnabled(exportFileName)) {
            return false;
        }
        return tryHandleDirectResponse(manager, loader, cms, exportResource, req, res, exportFileName);
    }

    /**
     * Handles an on-demand static export directly through the loader if possible.<p>
     *
     * @param manager the static export manager
     * @param loader the resource loader
     * @param cms the CMS context
     * @param exportResource the resource to export
     * @param req the current request
     * @param res the current response
     * @param exportFileName the target export file name
     *
     * @return <code>true</code> if the export has been handled without writing the local export file
     *
     * @throws CmsException if something goes wrong
     */
    protected boolean tryHandleDirectResponse(
        CmsStaticExportManager manager,
        I_CmsStaticExportStreamLoader loader,
        CmsObject cms,
        CmsResource exportResource,
        HttpServletRequest req,
        HttpServletResponse res,
        String exportFileName)
    throws CmsException {

        if ((req == null) || !(loader instanceof I_CmsStaticExportDirectResponseLoader)) {
            return false;
        }
        try {
            if (((I_CmsStaticExportDirectResponseLoader)loader).tryExportDirectResponse(
                cms,
                exportResource,
                req,
                res)) {
                manager.removeStoredContentExportFile(exportFileName);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new CmsStaticExportException(
                Messages.get().container(Messages.ERR_OUTPUT_STREAM_1, exportFileName),
                e);
        }
    }

    /**
     * Handles a static export through direct stored content delivery if possible.<p>
     *
     * @param manager the static export manager
     * @param configuration the stored content delivery configuration
     * @param loader the resource loader
     * @param cms the CMS context
     * @param exportResource the resource to export
     * @param req the current request
     * @param res the current response
     * @param exportFileName the target export file name
     *
     * @return <code>true</code> if the export has been handled without writing the local export file
     *
     * @throws CmsException if something goes wrong
     */
    protected boolean tryHandleStoredContentDelivery(
        CmsStaticExportManager manager,
        CmsStoredContentDeliveryConfiguration configuration,
        I_CmsStaticExportStreamLoader loader,
        CmsObject cms,
        CmsResource exportResource,
        HttpServletRequest req,
        HttpServletResponse res,
        String exportFileName)
    throws CmsException {

        if (!(loader instanceof I_CmsStoredContentDirectDeliveryLoader)) {
            return false;
        }
        if (!configuration.isSuffixEnabled(exportResource.getRootPath())) {
            return false;
        }
        I_CmsStoredContentDirectDeliveryLoader directDeliveryLoader = (I_CmsStoredContentDirectDeliveryLoader)loader;
        if (!directDeliveryLoader.isStoredContentDirectDeliveryEnabled(cms, exportResource, req, res)) {
            return false;
        }
        CmsStoredContentInfo info = cms.readStoredContentInfo(exportResource);
        if ((info == null) || !info.isExternallyStored()) {
            return false;
        }
        I_CmsStorageDelivery storage = cms.getStoredContentDelivery(info.getStorage());
        if (!(new CmsStoredContentDeliveryHelper()).canDeliver(info, storage)) {
            return false;
        }
        manager.removeStoredContentExportFile(exportFileName);
        if ((req == null) || (res == null)) {
            return true;
        }
        try {
            return directDeliveryLoader.exportStoredContentTo(cms, exportResource, req, res);
        } catch (IOException e) {
            throw new CmsStaticExportException(
                Messages.get().container(Messages.ERR_OUTPUT_STREAM_1, exportFileName),
                e);
        }
    }
}
