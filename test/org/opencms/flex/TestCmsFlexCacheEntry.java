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

package org.opencms.flex;

import org.opencms.test.OpenCmsTestCase;

/**
 * Tests for the CmsFlexCacheEntry.<p>
 */
public class TestCmsFlexCacheEntry extends OpenCmsTestCase {

    /**
     * Tests the method getAbsoluteUri.<p>
     */
    public void testSetDateLastModifiedToPreviousTimeout() {

        CmsFlexCacheEntry entry = new CmsFlexCacheEntry();
        long timeout = 1;

        entry.complete();
        entry.setDateExpiresToNextTimeout(timeout);
        entry.setDateLastModifiedToPreviousTimeout(timeout);

        long timeExpire = entry.getDateExpires();
        long timeLastMod = entry.getDateLastModified();
        long timeoutDiff = (timeExpire - timeLastMod) / 60000;
        assertEquals(timeout, timeoutDiff);

    }

}
