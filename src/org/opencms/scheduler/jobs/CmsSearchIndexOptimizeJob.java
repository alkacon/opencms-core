/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.I_CmsIndexWriter;
import org.opencms.util.CmsStringUtil;

import java.util.List;
import java.util.Map;

/**
 * A schedulable OpenCms job that optimizes the Lucene based search indexes at runtime.<p>
 * 
 * Only indexes which return <code>true</code> for  
 * {@link CmsSearchIndex#isUpdatedIncremental()} are being optimized.
 * By default, all such indexes are optimized if this job is run.<p>
 * 
 * Job parameters:<p>
 * <dl>
 * <dt><code>optimizeIndexes={comma separated list of index names}</code></dt>
 * <dd>Specifies list of indexes to be optimized. Only the indexes in this list are being optimized.
 * This parameter overrides an exclude list given with <code>excludeIndexes</code>.</dd>
 * <dt><code>excludeIndexes={comma separated list of index names}</code></dt>
 * <dd>Specifies list of indexes to be excluded from optimization.</dd>
 * </dl>
 * 
 * @since 8.5.0 
 */
public class CmsSearchIndexOptimizeJob implements I_CmsScheduledJob {

    /** Parameter to control which indexes are excluded from optimization. */
    public static final String PARAM_INDEXES_EXCLUDED = "excludeIndexes";

    /** Parameter to control which indexes are optimized. */
    public static final String PARAM_INDEXES_OPTIMIZED = "optimizeIndexes";

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(CmsObject, Map)
     */
    public String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

        List<String> optimizeIndexes = null;
        List<String> excludeIndexes = null;

        String oi = parameters.get(PARAM_INDEXES_OPTIMIZED);
        if (oi != null) {
            optimizeIndexes = CmsStringUtil.splitAsList(oi, ',', true);
            if (optimizeIndexes.isEmpty()) {
                optimizeIndexes = null;
            }
        } else {
            oi = parameters.get(PARAM_INDEXES_EXCLUDED);
            if (oi != null) {
                excludeIndexes = CmsStringUtil.splitAsList(oi, ',', true);
                if (excludeIndexes.isEmpty()) {
                    excludeIndexes = null;
                }
            }
        }

        for (CmsSearchIndex index : OpenCms.getSearchManager().getSearchIndexes()) {
            if (index.isUpdatedIncremental()) {
                // only indexes that are updated incremental need to be optimized

                if (((optimizeIndexes == null) && (excludeIndexes == null))
                    || ((optimizeIndexes != null) && optimizeIndexes.contains(index.getName()))
                    || ((excludeIndexes != null) && !excludeIndexes.contains(index.getName()))) {
                    // make sure index is either included or not excluded by name

                    I_CmsIndexWriter writer = index.getIndexWriter(null, false);
                    writer.optimize();
                }
            }
        }

        return null;
    }
}