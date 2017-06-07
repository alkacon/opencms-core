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

package org.opencms.i18n;

import org.opencms.test.OpenCmsTestCase;

import java.util.ArrayList;
import java.util.Arrays;
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
        Locale result = localeManager.getBestMatchingLocale(
            Locale.GERMAN,
            localeManager.getDefaultLocales(),
            available);
        assertEquals(Locale.GERMAN, result);

        // simplified direct match
        result = localeManager.getBestMatchingLocale(Locale.GERMANY, localeManager.getDefaultLocales(), available);
        assertEquals(Locale.GERMAN, result);

        // no match, result must be first default
        result = localeManager.getBestMatchingLocale(Locale.FRENCH, localeManager.getDefaultLocales(), available);
        assertEquals(Locale.US, result);
    }

    /** Tests if locale variants are returned correctly. */
    public void testGetLocaleVariants() {

        String base = "base";

        // We do not need the instance, but we have to set the default locale somehow
        @SuppressWarnings("unused")
        CmsLocaleManager manager = new CmsLocaleManager(new Locale("en", "GB"));
        Locale locale1 = new Locale("de", "DE", "WIN");

        // Tests with locale that is completely different from the default locale

        List<String> expected = Arrays.asList(new String[] {base + "_de_DE_WIN", base + "_de_DE", base + "_de"});
        List<String> actual = CmsLocaleManager.getLocaleVariants(base, locale1, false, false);
        assertEquals(expected, actual);

        expected = Arrays.asList(new String[] {base + "_de_DE_WIN", base + "_de_DE", base + "_de", base});
        actual = CmsLocaleManager.getLocaleVariants(base, locale1, true, false);
        assertEquals(expected, actual);

        expected = Arrays.asList(new String[] {base + "_de_DE_WIN", base + "_de_DE", base + "_de", base + "_en_GB"});
        actual = CmsLocaleManager.getLocaleVariants(base, locale1, false, true);
        assertEquals(expected, actual);

        expected = Arrays.asList(
            new String[] {base + "_de_DE_WIN", base + "_de_DE", base + "_de", base, base + "_en_GB"});
        actual = CmsLocaleManager.getLocaleVariants(base, locale1, true, true);
        assertEquals(expected, actual);

        // Tests with locale that is a variant of the default locale
        Locale locale2 = new Locale("en", "GB", "WIN");
        expected = Arrays.asList(new String[] {base + "_en_GB_WIN", base + "_en_GB", base + "_en"});
        actual = CmsLocaleManager.getLocaleVariants(base, locale2, false, true);
        assertEquals(expected, actual);

        expected = Arrays.asList(new String[] {base + "_en_GB_WIN", base + "_en_GB", base + "_en", base});
        actual = CmsLocaleManager.getLocaleVariants(base, locale2, true, true);
        assertEquals(expected, actual);

        // Tests with null as locale
        expected = Arrays.asList(new String[] {});
        actual = CmsLocaleManager.getLocaleVariants(base, null, false, false);
        assertEquals(expected, actual);

        expected = Arrays.asList(new String[] {base});
        actual = CmsLocaleManager.getLocaleVariants(base, null, true, false);
        assertEquals(expected, actual);

        expected = Arrays.asList(new String[] {base + "_en_GB"});
        actual = CmsLocaleManager.getLocaleVariants(base, null, false, true);
        assertEquals(expected, actual);

        expected = Arrays.asList(new String[] {base, base + "_en_GB"});
        actual = CmsLocaleManager.getLocaleVariants(base, null, true, true);
        assertEquals(expected, actual);

    }
}