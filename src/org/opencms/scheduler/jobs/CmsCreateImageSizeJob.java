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
import org.opencms.main.OpenCms;
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
 * Job parameters:<p>
 * <dl>
 * <dt><code>downscale=true|false</code></dt>
 * <dd>Controls if images are automatically downscaled according to the configured image
 * downscale settings, by default this is <code>false</code>.</dd>
 * </dl>
 *
 * @since 6.0.2
 */
public class CmsCreateImageSizeJob implements I_CmsScheduledJob {

    /**
     * This job parameter controls if images are automatically downscaled according to the configured image
     * downscale settings, by default this is <code>false</code>.
     *
     * Possible values are <code>true</code> or <code>false</code> (default).
     * If this is set to <code>true</code>, then all images are checked against the
     * configured image downscale settings (see {@link CmsImageLoader#CONFIGURATION_DOWNSCALE}).
     * If the image is too large, it is automatically downscaled.<p>
     */
    public static final String PARAM_DOWNSCALE = "downscale";

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(CmsObject, Map)
     */
    public String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

        if (!CmsImageLoader.isEnabled()) {
            // scaling functions are not available
            return Messages.get().getBundle().key(Messages.LOG_IMAGE_SCALING_DISABLED_0);
        }

        // read the downscale parameter
        boolean downscale = Boolean.valueOf(parameters.get(PARAM_DOWNSCALE)).booleanValue();

        I_CmsReport report = new CmsLogReport(cms.getRequestContext().getLocale(), CmsCreateImageSizeJob.class);
        report.println(Messages.get().container(Messages.RPT_IMAGE_SIZE_START_0), I_CmsReport.FORMAT_HEADLINE);

        List<CmsResource> resources = Collections.emptyList();
        try {
            // get all image resources
            resources = cms.readResources(
                "/",
                CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(
                    OpenCms.getResourceManager().getResourceType(
                        CmsResourceTypeImage.getStaticTypeName()).getTypeId()));
        } catch (CmsException e) {
            report.println(e);
        }

        int count = 0;
        // now iterate through all resources
        for (int i = 0; i < resources.size(); i++) {

            try {

                CmsResource res = resources.get(i);
                report.print(
                    Messages.get().container(
                        Messages.RPT_IMAGE_SIZE_PROCESS_3,
                        String.valueOf(i + 1),
                        String.valueOf(resources.size()),
                        res.getRootPath()),
                    I_CmsReport.FORMAT_HEADLINE);

                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                // check if the resource is locked by another user
                // we cannot process resources that are locked by someone else
                CmsLock lock = cms.getLock(res);
                if (lock.isNullLock() || lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {

                    // read the file content
                    CmsFile file = cms.readFile(res);
                    // get the image size information
                    CmsImageScaler scaler = new CmsImageScaler(file.getContents(), file.getRootPath());

                    if (scaler.isValid()) {
                        // the image can be scaled, width and height are known
                        boolean updated = false;

                        // check if the image must be downscaled
                        CmsImageScaler downScaler = null;
                        if (downscale) {
                            // scheduled job parameter is set for downscaling
                            downScaler = CmsResourceTypeImage.getDownScaler(cms, res.getRootPath());
                        }

                        if (scaler.isDownScaleRequired(downScaler)) {
                            // downscaling is required - just write the file again, in this case everything is updated
                            lockResource(cms, lock, res);
                            cms.writeFile(file);
                            // calculate the downscaled image size (only used for the output report)
                            scaler = scaler.getDownScaler(downScaler);
                            // the resource was updated
                            updated = true;
                        } else {
                            // check if the "image.size" property must be updated
                            CmsProperty prop = cms.readPropertyObject(
                                res,
                                CmsPropertyDefinition.PROPERTY_IMAGE_SIZE,
                                false);
                            // update the property if it does not exist or it is different than the newly calculated one
                            if (prop.isNullProperty() || !prop.getValue().equals(scaler.toString())) {
                                // lock resource
                                lockResource(cms, lock, res);
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
                                // the resource was updated
                                updated = true;
                            }
                        }

                        if (updated) {
                            // the resource was updated
                            unlockResource(cms, lock, res);
                            // increase counter
                            count++;
                            // write report information
                            report.println(
                                Messages.get().container(Messages.RPT_IMAGE_SIZE_UPDATE_1, scaler.toString()),
                                I_CmsReport.FORMAT_DEFAULT);

                        } else {
                            // no changes have been made to the resource
                            report.println(
                                Messages.get().container(Messages.RPT_IMAGE_SIZE_SKIP_1, scaler.toString()),
                                I_CmsReport.FORMAT_DEFAULT);
                        }
                    } else {
                        // no valid image scaler
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

        return Messages.get().getBundle().key(Messages.LOG_IMAGE_SIZE_UPDATE_COUNT_1, Integer.valueOf(count));
    }

    /**
     * Locks the given resource (if required).<p>
     *
     * @param cms the OpenCms user context
     * @param lock the previous lock status of the resource
     * @param res the resource to lock
     *
     * @throws CmsException in case something goes wrong
     */
    private void lockResource(CmsObject cms, CmsLock lock, CmsResource res) throws CmsException {

        if (lock.isNullLock()) {
            cms.lockResource(res.getRootPath());
        }
    }

    /**
     * Unlocks the given resource (if required).<p>
     *
     * @param cms the OpenCms user context
     * @param lock the lock of the resource
     * @param res the resource to lock
     *
     * @throws CmsException in case something goes wrong
     */
    private void unlockResource(CmsObject cms, CmsLock lock, CmsResource res) throws CmsException {

        if (lock.isNullLock()) {
            cms.unlockResource(res.getRootPath());
        }
    }
}