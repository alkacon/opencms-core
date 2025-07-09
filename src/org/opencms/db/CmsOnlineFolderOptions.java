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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.util.CmsStringUtil;

import java.util.Collections;
import java.util.List;

/**
 * Configuration parameters for the 'online folder' feature.
 */
public class CmsOnlineFolderOptions {

    /** Configuration key for the indexing interval.  */
    public static final String PARAM_INDEXING_INTERVAL = "indexing-interval";

    /** Configuration key for the flex cache clear delay. */
    public static final String PARAM_FLEX_CACHE_DELAY = "flex-cache-delay";

    /** The paths which are defined as online folders. */
    private List<String> m_paths;

    /** Additional parameters. */
    private CmsParameterConfiguration m_params;

    /**
     * Creates a new instance.
     *
     * @param paths the paths to be used as online folders
     * @param params additional parameters
     */
    public CmsOnlineFolderOptions(List<String> paths, CmsParameterConfiguration params) {

        m_paths = Collections.unmodifiableList(paths);
        m_params = CmsParameterConfiguration.unmodifiableVersion(params);
    }

    /**
     * Gets the duration that has to pass after an instant publish event with no further instant publish events before the Flex cache is cleared.
     *
     * @return the flex cache delay
     */
    public long getFlexCacheDelay() {

        String delayStr = m_params.getString(PARAM_FLEX_CACHE_DELAY, "5s");
        return CmsStringUtil.parseDuration(delayStr, 5000);

    }

    /**
     * Gets the amount of time across which (non-deletion) changes from instant publish events are combined for indexing.
     *
     * @return the indexing interval
     */
    public long getIndexingInterval() {

        String indexingIntervalStr = m_params.getString(PARAM_INDEXING_INTERVAL, "2s");
        return CmsStringUtil.parseDuration(indexingIntervalStr, 2000);

    }

    /**
     * Gets the additional parameters.
     * @return the additional parameters
     */
    public CmsParameterConfiguration getParams() {

        return m_params;
    }

    /**
     * Gets the configured paths.
     *
     * @return the configured paths
     */
    public List<String> getPaths() {

        return m_paths;
    }

}
