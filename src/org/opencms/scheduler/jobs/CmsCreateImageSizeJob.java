/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/jobs/CmsCreateImageSizeJob.java,v $
 * Date   : $Date: 2005/09/27 12:15:56 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.scheduler.jobs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.scheduler.I_CmsScheduledJob;

import java.util.List;
import java.util.Map;

/**
 * A schedulable OpenCms job to create image size information.<p>
 * 
 * Image size information is stored in the "image.size" property of an image file and contains 
 * "h:x,w:y" with x and y as integer vaulues.
 * 
 * This job does not have any parameters.<p>
 * 
 * @author Michael Emmerich
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.2 
 */
public class CmsCreateImageSizeJob implements I_CmsScheduledJob {

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(CmsObject, Map)
     */
    public String launch(CmsObject cms, Map parameters) throws Exception {

        I_CmsReport report = null;

        try {
            report = new CmsLogReport(cms.getRequestContext().getLocale(), CmsCreateImageSizeJob.class);

            report.println(Messages.get().container(Messages.RPT_IMAGE_SIZE_START_0), I_CmsReport.FORMAT_HEADLINE);

            // get all image resources
            List resources = cms.readResources(
                "/",
                CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(CmsResourceTypeImage.getStaticTypeId()));

            // now iterate through all resources
            for (int i = 0; i < resources.size(); i++) {
                CmsResource res = (CmsResource)resources.get(i);

                report.print(Messages.get().container(
                    Messages.RPT_IMAGE_SIZE_PROCESS_3,
                    String.valueOf(i + 1),
                    String.valueOf(resources.size()),
                    res.getRootPath()), I_CmsReport.FORMAT_HEADLINE);
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                // check if the resource is locked by another user
                // we cannot process resources that are locked by someone else
                CmsLock lock = cms.getLock(res);
                if (lock.isNullLock() || lock.getUserId().equals(cms.getRequestContext().currentUser().getId())) {
                    // lock the resource if not locked so far
                    boolean unlockFlag = false;
                    if (lock.isNullLock()) {
                        cms.lockResource(res.getRootPath());
                        unlockFlag = true;
                    }

                    // get the size info property
                    CmsProperty prop = cms.readPropertyObject(res, CmsResourceTypeImage.PROPERTY_IMAGESIZE, false);
                    if (prop == null) {
                        prop = CmsProperty.getNullProperty();
                    }
                    // get the image szte information
                    String sizeInfo = getSizeInfo(res);

                    // update the property if it does not exist or it is different than the newly calculated one
                    if (prop.isNullProperty() || !prop.getValue().equals(sizeInfo)) {

                        // set the shared value of the property or create a new one if required
                        if (prop.isNullProperty()) {
                            prop = new CmsProperty(CmsResourceTypeImage.PROPERTY_IMAGESIZE, null, sizeInfo);
                        } else {
                            prop.setResourceValue(sizeInfo);
                        }
                        // write the property
                        cms.writePropertyObject(res.getRootPath(), prop);
                        report.println(
                            Messages.get().container(Messages.RPT_IMAGE_SIZE_UPDATE_1, sizeInfo),
                            I_CmsReport.FORMAT_DEFAULT);
                    } else {
                        report.println(
                            Messages.get().container(Messages.RPT_IMAGE_SIZE_SKIP_1, sizeInfo),
                            I_CmsReport.FORMAT_DEFAULT);
                    }

                    // unlock the resource if it was not locked before
                    if (unlockFlag) {
                        cms.unlockResource(res.getRootPath());
                    }

                } else {
                    // the resource is locked by someone else
                    report.println(
                        Messages.get().container(Messages.RPT_IMAGE_SIZE_LOCKED_0),
                        I_CmsReport.FORMAT_DEFAULT);
                }
            }
        } catch (CmsException e) {
            report.println(e);
        } finally {
            report.println(Messages.get().container(Messages.RPT_IMAGE_SIZE_END_0), I_CmsReport.FORMAT_HEADLINE);
        }

        return null;
    }

    /**
     * Gets the image size info information to be stored as the "image.size" property.<p>
     * 
     * @param res the resource to get the size info from
     * @return string represnetation of the image size info information
     */
    private String getSizeInfo(CmsResource res) {

        StringBuffer sizeInfo = new StringBuffer();
        sizeInfo.append("h:");
        // TODO: add the corrent value here
        sizeInfo.append("100");
        sizeInfo.append(",w:");
        // TODO: add the corrent value here
        sizeInfo.append("100");
        return sizeInfo.toString();
    }

}