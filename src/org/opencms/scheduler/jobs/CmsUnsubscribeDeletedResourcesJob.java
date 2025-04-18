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
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.util.CmsStringUtil;

import java.util.Map;

/**
 * A scheduled OpenCms job to unsubscribe deleted resources.<p>
 *
 * Job parameters:<p>
 * <dl>
 * <dt><code>deleteddays={Number/Integer}</code></dt>
 * <dd>Amount of days a resource has to be deleted to be unsubscribed (defaults to 30).</dd>
 * </dl>
 * <p>
 *
 * @since 8.0.0
 */
public class CmsUnsubscribeDeletedResourcesJob implements I_CmsScheduledJob {

    /** Name of the parameter where to configure the amount of days a resource has to be expired before deletion. */
    public static final String PARAM_DELETEDDAYS = "deleteddays";

    /** Constant for calculation. */
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(org.opencms.file.CmsObject, java.util.Map)
     */
    public String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

        // read the parameter for the deleted days
        int deletedDays = 30;
        String deleteddaysParam = parameters.get(PARAM_DELETEDDAYS);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(deleteddaysParam)) {
            try {
                deletedDays = Integer.parseInt(deleteddaysParam);
            } catch (NumberFormatException nfe) {
                // don't care
            }
        }
        long deletedTo = System.currentTimeMillis() - (MILLIS_PER_DAY * deletedDays);
        OpenCms.getSubscriptionManager().unsubscribeAllDeletedResources(cms, deletedTo);

        return null;
    }

}
