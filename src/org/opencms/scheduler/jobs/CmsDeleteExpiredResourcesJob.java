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

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsResourceManager;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishManager;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.util.CmsStringUtil;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A schedulable OpenCms job to delete expired resources.<p>
 *
 * The user to execute the process should have have access to the required "Workplace manager" role.<p>
 *
 * The "Offline" project has to be configured for this job because the operations cannot be performed in the "Online" project.<p>
 *
 * Job parameters:<p>
 * <dl>
 * <dt><code>expirationdays={Number/Integer}</code></dt>
 * <dd>Amount of days a resource has to be expired to be deleted.</dd>
 * <dt><code>resourcetypes={csv list}</code></dt>
 * <dd>Comma separated list of resource type names to specify the types of expired resources that may be deleted.
 * If left out, expired resources of all types will be deleted. .</dd>
 * <dt><code>folder={csv list}</code></dt>
 * <dd>Allows to specify a comma separated list of folders in which all expired resources will be deleted. If omitted "/" will be taken as single folder
 * for this operation. </dd>
 * </dl>
 * <p>
 *
 * The property "delete.expired" (<code>{@link CmsPropertyDefinition#PROPERTY_DELETE_EXPIRED}</code>) may be used
 * to override the global setting of the parameter <code>expirationdays</code>. A value of "never", "false" or "none" will
 * prevent resources from being deleted. Other values are "true" (default) or the amount of days a resource has
 * to be expired for qualification of deletion.<p>
 *
 * Only published / unchanged files will be processed. Anything with unpublished changes will not
 * be touched by the job. <p>
 *
 * Folders with expiration dates are ignored by default. Only if the scheduler parameter "resourcetypes" contains "folder"
 * a folder that has been expired will be deleted (with all contained resources). <p>
 *
 * @since 7.5.0
 */
public class CmsDeleteExpiredResourcesJob implements I_CmsScheduledJob {

    /** Name of the parameter where to configure the amount of days a resource has to be expired before deletion. */
    public static final String PARAM_EXPIRATIONSDAYS = "expirationdays";

    /** Name of the parameter where to configure the resource types for resources to delete if expired. */
    public static final String PARAM_RESOURCETYPES = "resourcetypes";

    /** Name of the parameter where to configure the folder below which the operation will be done. */
    public static final String PARAM_FOLDER = "folder";

    /** Constant for calculation. */
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

    /** Setting for the <code>{@link CmsPropertyDefinition#PROPERTY_DELETE_EXPIRED}</code> to disallow deletion. */
    public static final String PROPERTY_VALUE_DELETE_EXPIRED_NEVER = "never";

    /** Setting for the <code>{@link CmsPropertyDefinition#PROPERTY_DELETE_EXPIRED}</code> to disallow deletion. */
    public static final String PROPERTY_VALUE_DELETE_EXPIRED_NONE = "none";

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(org.opencms.file.CmsObject, java.util.Map)
     */
    public String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

        // this job requires a higher runlevel than is allowed for all jobs:
        if (OpenCms.getRunLevel() == OpenCms.RUNLEVEL_4_SERVLET_ACCESS) {
            long currenttime = System.currentTimeMillis();

            // read the parameter for the versions to keep
            int expirationdays = 30;
            String expirationdaysparam = parameters.get(PARAM_EXPIRATIONSDAYS);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(expirationdaysparam)) {
                try {
                    expirationdays = Integer.parseInt(expirationdaysparam);
                } catch (NumberFormatException nfe) {
                    // don't care
                }
            }

            // read the parameter if to clear versions of deleted resources
            String resTypes = parameters.get(PARAM_RESOURCETYPES);
            String[] resTypesArr = null;
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(resTypes)) {
                resTypesArr = CmsStringUtil.splitAsArray(resTypes, ',');
            }

            // read the optional parameter for the time range to keep versions
            String[] topFoldersArr = new String[] {"/"};
            String topfolders = parameters.get(PARAM_FOLDER);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(topfolders)) {
                topFoldersArr = CmsStringUtil.splitAsArray(topfolders, ',');
            }

            // create a temp project for publishing everything together at the end:
            CmsProject project = cms.createTempfileProject();
            cms.getRequestContext().setCurrentProject(project);

            I_CmsReport report = new CmsLogReport(
                cms.getRequestContext().getLocale(),
                CmsDeleteExpiredResourcesJob.class);
            report.println(Messages.get().container(Messages.RPT_DELETE_EXPIRED_START_0), I_CmsReport.FORMAT_HEADLINE);

            // collect all resources:
            List<CmsResource> resources = Collections.emptyList();
            CmsResourceFilter filter = CmsResourceFilter.ALL.addExcludeState(CmsResourceState.STATE_DELETED);
            filter = filter.addRequireExpireBefore(currenttime);

            // if we have configured resource types reading is more complicated because inclusion of several types
            // is not supported by resource filter api:
            int changedFiles = 0;
            if (resTypesArr != null) {
                I_CmsResourceType type;
                CmsResourceManager resManager = OpenCms.getResourceManager();
                for (int i = resTypesArr.length - 1; i >= 0; i--) {
                    type = resManager.getResourceType(resTypesArr[i]);
                    filter = filter.addRequireType(type.getTypeId());
                    for (int j = topFoldersArr.length - 1; j >= 0; j--) {
                        resources = cms.readResources(topFoldersArr[j], filter, true);
                        changedFiles += deleteExpiredResources(cms, report, resources, expirationdays, currenttime);
                    }
                }

            } else {
                filter = filter.addRequireFile();
                for (int j = topFoldersArr.length - 1; j >= 0; j--) {
                    resources = cms.readResources(topFoldersArr[j], filter, true);
                    changedFiles += deleteExpiredResources(cms, report, resources, expirationdays, currenttime);
                }
            }
            if (changedFiles > 0) {
                CmsPublishManager publishManager = OpenCms.getPublishManager();
                publishManager.publishProject(cms, report);
                // this is to not scramble the logging output:
                publishManager.waitWhileRunning();
            }
            report.println(Messages.get().container(Messages.RPT_DELETE_EXPIRED_END_0), I_CmsReport.FORMAT_HEADLINE);
        }
        return null;
    }

    /**
     * Deletes the expired resources if the have been expired longer than the given amount of days. <p>
     *
     * At this level the resource type is not checked again. <p>
     *
     * @param resources a <code>List</code> containing <code>CmsResource</code> instances to process.
     * @param cms needed to delete resources
     * @param report needed to print messages to
     * @param expirationdays the amount of days a resource has to be expired before it is deleted
     * @param currenttime the current time in milliseconds since January 1st 1970
     * @return the amount of deleted files
     *
     */
    private int deleteExpiredResources(
        final CmsObject cms,
        final I_CmsReport report,
        final List<CmsResource> resources,
        final int expirationdays,
        final long currenttime) {

        int result = 0;
        CmsResource resource;
        CmsLock lock;
        CmsProperty property;
        String propertyValue;
        long expirationdate;
        int expirationDaysPropertyOverride;
        Iterator<CmsResource> it = resources.iterator();
        String resourcePath;
        while (it.hasNext()) {
            resource = it.next();
            resourcePath = cms.getRequestContext().removeSiteRoot(resource.getRootPath());
            report.print(
                Messages.get().container(Messages.RPT_DELETE_EXPIRED_PROCESSING_1, new String[] {resourcePath}),
                I_CmsReport.FORMAT_DEFAULT);
            report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            if (resource.getState() == CmsResourceState.STATE_UNCHANGED) {
                expirationdate = resource.getDateExpired();
                expirationDaysPropertyOverride = expirationdays;
                try {
                    property = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_DELETE_EXPIRED, true);
                    propertyValue = property.getValue();
                    if (!property.isNullProperty()) {
                        if (PROPERTY_VALUE_DELETE_EXPIRED_NEVER.equals(propertyValue)
                            || PROPERTY_VALUE_DELETE_EXPIRED_NONE.equals(propertyValue)
                            || Boolean.FALSE.toString().equals(propertyValue)) {
                            report.println(
                                Messages.get().container(Messages.RPT_DELETE_EXPIRED_PROPERTY_NEVER_0),
                                I_CmsReport.FORMAT_NOTE);
                            continue;
                        } else {
                            // true is allowed, but any other value will be treated as a configuration error and skip the
                            // resource:

                            if (!Boolean.TRUE.toString().equals(propertyValue)) {
                                // NumberFormatException should skip the resource because the property value was mistyped
                                expirationDaysPropertyOverride = Integer.parseInt(propertyValue);
                            }
                        }
                    }

                    // no Calendar - semantics required for simple timespan check:
                    if ((expirationdate != Long.MAX_VALUE)
                        && ((currenttime - expirationdate) > (expirationDaysPropertyOverride * MILLIS_PER_DAY))) {
                        lock = cms.getLock(resource);
                        if (lock.isNullLock()) {
                            cms.lockResource(resourcePath);
                        } else {
                            if (!lock.getUserId().equals(cms.getRequestContext().getCurrentUser().getId())) {
                                report.println(
                                    Messages.get().container(Messages.RPT_DELETE_EXPIRED_LOCKED_0),
                                    I_CmsReport.FORMAT_WARNING);
                                continue;
                            }
                        }
                        cms.deleteResource(resourcePath, CmsResource.DELETE_PRESERVE_SIBLINGS);
                        result++;
                        report.println(
                            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                            I_CmsReport.FORMAT_OK);

                    } else {
                        report.println(
                            Messages.get().container(
                                Messages.RPT_DELETE_EXPIRED_NOT_EXPIRED_1,
                                new Integer[] {Integer.valueOf(expirationDaysPropertyOverride)}));
                    }
                } catch (Exception e) {
                    report.println(
                        Messages.get().container(
                            Messages.RPT_DELETE_EXPIRED_FAILED_1,
                            new String[] {CmsException.getStackTraceAsString(e)}),
                        I_CmsReport.FORMAT_ERROR);

                }
            } else {
                report.println(Messages.get().container(Messages.RPT_DELETE_EXPIRED_UNPUBLISHED_0));
            }
        }
        return result;
    }
}
