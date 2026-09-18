/*
 * This library is part of OpenCms -
 * The Open Source Content Management System
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

package org.opencms.db.storage.s3;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Result of an S3 multi-object delete operation.<p>
 */
public final class CmsS3DeleteResult {

    /** The successfully deleted object keys. */
    private final Set<String> m_deletedKeys;

    /** The failures by object key. */
    private final Map<String, Exception> m_failures;

    /**
     * Creates a delete result.<p>
     *
     * Keys without a reported failure are treated as successfully deleted because
     * S3 deletes are idempotent and quiet responses may omit successful keys.<p>
     *
     * @param requestedKeys the requested object keys
     * @param failures the failures by object key
     */
    public CmsS3DeleteResult(Collection<String> requestedKeys, Map<String, Exception> failures) {

        LinkedHashSet<String> deletedKeys = new LinkedHashSet<String>(requestedKeys);
        m_failures = Collections.unmodifiableMap(new LinkedHashMap<String, Exception>(failures));
        deletedKeys.removeAll(failures.keySet());
        m_deletedKeys = Collections.unmodifiableSet(deletedKeys);
    }

    /** Returns the successfully deleted object keys. */
    public Set<String> getDeletedKeys() {

        return m_deletedKeys;
    }

    /** Returns the failures by object key. */
    public Map<String, Exception> getFailures() {

        return m_failures;
    }

    /** Returns whether every requested object was deleted successfully. */
    public boolean isSuccessful() {

        return m_failures.isEmpty();
    }
}
