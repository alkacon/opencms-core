/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/jobs/CmsHistoryClearJob.java,v $
 * Date   : $Date: 2011/03/23 14:52:56 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.report.CmsLogReport;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.util.CmsStringUtil;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

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
 * <dd>Number/Integer to control how many versions will be kept.</dd>
 * <dt><code>clearDeleted=true|false</code></dt>
 * <dd>Boolean to configure if the versions of deleted resources should be cleared.
 * The default is false.</dd>
 * <dt><code>keepTimeRange</code></dt>
 * <dd>Number/Integer to configure the number of days the versions of deleted resources will 
 * be kept. That means that all versions wich are older than the specified number will be deleted.
 * This parameter is optional and only makes sense if the clearDeleted parameter is set to true.</dd>
 * </dl>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 7.0.0
 */
public class CmsHistoryClearJob implements I_CmsScheduledJob {

    /** Name of the parameter where to configure how many versions are kept. */
    public static final String PARAM_KEEPVERSIONS = "keepVersions";

    /** Name of the parameter where to configure if versions of deleted resources are cleared. */
    public static final String PARAM_CLEARDELETED = "clearDeleted";

    /** Name of the parameter where to configure the number of days the versions will be kept. */
    public static final String PARAM_KEEPTIMERANGE = "keepTimeRange";

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(org.opencms.file.CmsObject, java.util.Map)
     */
    public String launch(CmsObject cms, Map parameters) throws Exception {

        // read the parameter for the versions to keep
        int keepVersions = Integer.parseInt((String)parameters.get(PARAM_KEEPVERSIONS));

        // read the parameter if to clear versions of deleted resources
        boolean clearDeleted = Boolean.valueOf((String)parameters.get(PARAM_CLEARDELETED)).booleanValue();

        // read the optional parameter for the time range to keep versions
        String keepTimeRangeStr = (String)parameters.get(PARAM_KEEPTIMERANGE);
        int keepTimeRange = -1;
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(keepTimeRangeStr)) {
            keepTimeRange = Integer.parseInt(keepTimeRangeStr);
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
        cms.deleteHistoricalVersions(keepVersions, keepDeletedVersions, timeDeleted, report);

        return null;
    }

}
