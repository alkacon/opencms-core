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

package org.opencms.scheduler.jobs;

import org.opencms.file.CmsObject;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * A schedulable OpenCms job to clear the history.<p>
 *
 * The user to execute the process should have have access to the required "Workplace manager" role.<p>
 *
 * If there is an Uri set for the scheduled job, which should only be folders, it will be used
 * for clearing the history only in there (and the subfolders).<p>
 *
 * Job parameters:<p>
 * <dl>
 * <dt><code>keepVersions={Number/Integer}</code></dt>
 * <dd>Number/Integer to control how many versions will be kept. Use -1 if you only want to perform the 'clear deleted resources' part of the job.</dd>
 * <dt><code>clearDeleted=true|false</code></dt>
 * <dd>Boolean to configure if the versions of deleted resources should be cleared.
 * The default is false.</dd>
 * <dt><code>clearDeletedTypes=image,binary</code></dt>
 * <dd>OPTIONAL. A comma-separated list of resource types to consider for clearing the deleted resources. If not configured, the resource type will not be restricted.</dd>
 * <dt><code>clearDeletedPath=/sites/default</code></dt>
 * <dd>OPTIONAL. A path below which resources will be considered when clearing the deleted resources. If not configured, the resource path will not be restricted.</dd>
 * <dt><code>keepTimeRange</code></dt>
 * <dd>Number/Integer to configure the number of days the versions of deleted resources will
 * be kept. That means that all versions wich are older than the specified number will be deleted.
 * This parameter is optional and only makes sense if the clearDeleted parameter is set to true.</dd>
 * </dl>
 *
 * @since 7.0.0
 */
public class CmsHistoryClearJob implements I_CmsScheduledJob {

    /**Parameter for selecting the path of deleted resources to clear. */
    public static final String PARAM_CLEAR_DELETED_PATHS = "clearDeletedPaths";

    /** Parameter for selecting the types of deleted resources to clear. */
    public static final String PARAM_CLEAR_DELETED_TYPES = "clearDeletedTypes";

    /** Name of the parameter where to configure if versions of deleted resources are cleared. */
    public static final String PARAM_CLEARDELETED = "clearDeleted";

    /** Name of the parameter where to configure the number of days the versions will be kept. */
    public static final String PARAM_KEEPTIMERANGE = "keepTimeRange";

    /** Name of the parameter where to configure how many versions are kept. */
    public static final String PARAM_KEEPVERSIONS = "keepVersions";

    private static final Log LOG = CmsLog.getLog(CmsHistoryClearJob.class);

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(org.opencms.file.CmsObject, java.util.Map)
     */
    public String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

        // read the parameter for the versions to keep
        int keepVersions = Integer.parseInt(parameters.get(PARAM_KEEPVERSIONS));

        // read the parameter if to clear versions of deleted resources
        boolean clearDeleted = Boolean.valueOf(parameters.get(PARAM_CLEARDELETED)).booleanValue();

        // read the optional parameter for the time range to keep versions
        String keepTimeRangeStr = parameters.get(PARAM_KEEPTIMERANGE);
        int keepTimeRange = -1;
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(keepTimeRangeStr)) {
            keepTimeRange = Integer.parseInt(keepTimeRangeStr);
        }

        final String clearDeletedTypesStr = parameters.get(PARAM_CLEAR_DELETED_TYPES);
        final Set<Integer> clearDeletedTypes = new HashSet<>();
        if (clearDeletedTypesStr != null) {
            for (String token : clearDeletedTypesStr.split(",")) {
                String typeName = token.trim();
                if (OpenCms.getResourceManager().hasResourceType(typeName)) {
                    try {
                        clearDeletedTypes.add(
                            Integer.valueOf(OpenCms.getResourceManager().getResourceType(typeName).getTypeId()));
                    } catch (CmsLoaderException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        }

        final String clearDeletedPathsStr = parameters.get(PARAM_CLEAR_DELETED_PATHS);
        final List<String> clearDeletedPaths = new ArrayList<>();
        if (clearDeletedPathsStr != null) {
            for (String token : clearDeletedPathsStr.split(",")) {
                token = token.trim();
                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(token)) {
                    clearDeletedPaths.add(token);
                }
            }
        }
        // calculate the date from where to clear deleted versions
        long timeDeleted = -1;
        int keepDeletedVersions;
        if (clearDeleted) {
            keepDeletedVersions = 1;
            GregorianCalendar cal = new GregorianCalendar();
            cal.add(Calendar.DAY_OF_YEAR, (keepTimeRange) * -1);
            timeDeleted = cal.getTimeInMillis();
        } else {
            keepDeletedVersions = -1;
        }

        // create a new report
        CmsLogReport report = new CmsLogReport(cms.getRequestContext().getLocale(), CmsHistoryClearJob.class);

        // delete the versions
        cms.deleteHistoricalVersions(keepVersions, keepDeletedVersions, timeDeleted, res -> {
            boolean delete = true;
            if (clearDeletedTypesStr != null) {
                delete &= clearDeletedTypes.contains(res.getTypeId());
            }
            if (clearDeletedPathsStr != null) {
                delete &= clearDeletedPaths.stream().anyMatch(
                    prefix -> CmsStringUtil.isPrefixPath(prefix, res.getRootPath()));
            }
            return delete;
        }, report);

        return null;
    }

}
