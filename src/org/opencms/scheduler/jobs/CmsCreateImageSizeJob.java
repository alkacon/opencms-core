/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/jobs/CmsCreateImageSizeJob.java,v $
 * Date   : $Date: 2005/10/09 07:15:20 $
 * Version: $Revision: 1.1.2.2 $
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.loader.CmsImageLoader;
import org.opencms.loader.CmsImageScaler;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.scheduler.I_CmsScheduledJob;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A schedulable OpenCms job to calculate image size information.<p>
 * 
 * Image size information is stored in the <code>{@link CmsPropertyDefinition#PROPERTY_IMAGE_SIZE}</code> property 
 * of an image file must have the format "h:x,w:y" with x and y being positive Integer vaulues.<p>
 * 
 * This job does not have any parameters.<p>
 * 
 * @author Michael Emmerich
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.0.2 
 */
public class CmsCreateImageSizeJob implements I_CmsScheduledJob {

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(CmsObject, Map)
     */
    public String launch(CmsObject cms, Map parameters) throws Exception {

        if (!CmsImageLoader.isEnabled()) {
            // scaling functions are not available
            return Messages.get().key(Messages.LOG_IMAGE_SCALING_DISABLED_0);
        }

        I_CmsReport report = new CmsLogReport(cms.getRequestContext().getLocale(), CmsCreateImageSizeJob.class);
        report.println(Messages.get().container(Messages.RPT_IMAGE_SIZE_START_0), I_CmsReport.FORMAT_HEADLINE);

        List resources = Collections.EMPTY_LIST;
        try {
            // get all image resources
            resources = cms.readResources(
                "/",
                CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(CmsResourceTypeImage.getStaticTypeId()));
        } catch (CmsException e) {
            report.println(e);
        }

        int count = 0;
        // now iterate through all resources
        for (int i = 0; i < resources.size(); i++) {

            try {

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

                    // get the size info property
                    CmsProperty prop = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_IMAGE_SIZE, false);
                    if (prop == null) {
                        prop = CmsProperty.getNullProperty();
                    }
                    // read the file content
                    CmsFile file = CmsFile.upgrade(res, cms);
                    // get the image size information
                    CmsImageScaler scaler = new CmsImageScaler(file.getContents(), file.getRootPath());

                    if (scaler.isValid()) {
                        // update the property if it does not exist or it is different than the newly calculated one
                        if (prop.isNullProperty() || !prop.getValue().equals(scaler.toString())) {

                            boolean unlockFlag = false;
                            // lock the resource if not locked so far
                            if (lock.isNullLock()) {
                                cms.lockResource(res.getRootPath());
                                unlockFlag = true;
                            }
                            // set the shared value of the property or create a new one if required
                            if (prop.isNullProperty()) {
                                prop = new CmsProperty(
                                    CmsPropertyDefinition.PROPERTY_IMAGE_SIZE,
                                    null,
                                    scaler.toString());
                            } else {
                                // delete any individual proprety value (just in case)
                                prop.setStructureValue(CmsProperty.DELETE_VALUE);
                                // set the calculated value as shared property
                                prop.setResourceValue(scaler.toString());
                            }
                            // write the property
                            cms.writePropertyObject(res.getRootPath(), prop);
                            // unlock the resource if it was not locked before
                            if (unlockFlag) {
                                cms.unlockResource(res.getRootPath());
                            }
                            // increase conter 
                            count++;
                            // write report information
                            report.println(
                                Messages.get().container(Messages.RPT_IMAGE_SIZE_UPDATE_1, scaler.toString()),
                                I_CmsReport.FORMAT_DEFAULT);

                        } else {
                            report.println(
                                Messages.get().container(Messages.RPT_IMAGE_SIZE_SKIP_1, scaler.toString()),
                                I_CmsReport.FORMAT_DEFAULT);
                        }
                    } else {
                        report.println(
                            Messages.get().container(Messages.RPT_IMAGE_SIZE_UNABLE_TO_CALCULATE_0),
                            I_CmsReport.FORMAT_DEFAULT);
                    }

                } else {
                    // the resource is locked by someone else
                    report.println(
                        Messages.get().container(Messages.RPT_IMAGE_SIZE_LOCKED_0),
                        I_CmsReport.FORMAT_DEFAULT);
                }

            } catch (CmsException e) {
                report.println(e);
            }
        }

        report.println(Messages.get().container(Messages.RPT_IMAGE_SIZE_END_0), I_CmsReport.FORMAT_HEADLINE);

        return Messages.get().key(Messages.LOG_IMAGE_SIZE_UPDATE_COUNT_1, new Integer(count));
    }
}