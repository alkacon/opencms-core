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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db.generic;

import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Filter object that describes what to clean up in a publish history cleanup operation.
 */
public class CmsPublishHistoryCleanupFilter {

    /** Mode of operation for publish history cleanup. */
    public enum Mode {

        /** Removes all publish history entries unreferenced by publish jobs, with a fixed set of exceptions */
        allUnreferenced,

        /** Remove entries for single publish history id. */
        single;
    }

    /** List of history ids for which the entries should not be deleted. */
    private List<CmsUUID> m_exceptions = new ArrayList<>();

    /** History id for which entries should be deleted. */
    private CmsUUID m_historyId = null;

    /** The mode. */
    private Mode m_mode;

    /**
     * Hidden constructor.
     */
    private CmsPublishHistoryCleanupFilter() {

        // do nothing
    }

    /**
     * Creates a new filter for removing all unreferenced publish history entries, except the ones with the given history ids.
     *
     * @param exceptions the history ids for which entries should not be deleted
     * @return the filter
     */
    public static CmsPublishHistoryCleanupFilter allUnreferencedExcept(List<CmsUUID> exceptions) {

        CmsPublishHistoryCleanupFilter result = new CmsPublishHistoryCleanupFilter();
        result.m_mode = Mode.allUnreferenced;
        result.m_exceptions.addAll(exceptions);
        return result;
    }

    /**
     * Creates a filter for removing the publish history entries for a single history id.
     *
     * @param publishJobId the history id
     * @return the filter
     */
    public static CmsPublishHistoryCleanupFilter forHistoryId(CmsUUID publishJobId) {

        CmsPublishHistoryCleanupFilter result = new CmsPublishHistoryCleanupFilter();
        result.m_mode = Mode.single;
        result.m_historyId = publishJobId;
        return result;
    }

    /**
     * Gets the list of history ids for which entries should not be deleted.
     *
     * @return the list of history ids for which entries should not be deleted
     */
    public List<CmsUUID> getExceptions() {

        return Collections.unmodifiableList(m_exceptions);
    }

    /**
     * Gets the history id.
     *
     * @return the history id
     */
    public CmsUUID getHistoryId() {

        return m_historyId;
    }

    /**
     * Gets the mode.
     *
     * @return the mode
     */
    public Mode getMode() {

        return m_mode;
    }

}
