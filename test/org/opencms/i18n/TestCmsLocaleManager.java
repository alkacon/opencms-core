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

package org.opencms.i18n;

import org.opencms.test.OpenCmsTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Tests for the OpenCms locale manager.<p>
 * 
 * @since 6.0.0
 */
public class TestCmsLocaleManager extends OpenCmsTestCase {

    /**
     * Tests selection of the default locale.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testDefaultLocaleSelection() throws Exception {

        CmsLocaleManager localeManager = new CmsLocaleManager();

        List<Locale> available = new ArrayList<Locale>();

        localeManager.addDefaultLocale(Locale.US.toString());
        localeManager.addDefaultLocale(Locale.UK.toString());
        localeManager.addDefaultLocale(Locale.GERMANY.toString());
        localeManager.addDefaultLocale(Locale.ENGLISH.toString());
        localeManager.addDefaultLocale(Locale.GERMAN.toString());

        available.add(Locale.GERMAN);
        available.add(Locale.US);

        // direct match
        Locale result = localeManager.getBestMatchingLocale(Locale.GERMAN, localeManager.getDefaultLocales(), available);
        assertEquals(Locale.GERMAN, result);

        // simplified direct match
        result = localeManager.getBestMatchingLocale(Locale.GERMANY, localeManager.getDefaultLocales(), available);
        assertEquals(Locale.GERMAN, result);

        // no match, result must be first default
        result = localeManager.getBestMatchingLocale(Locale.FRENCH, localeManager.getDefaultLocales(), available);
        assertEquals(Locale.US, result);
    }
}