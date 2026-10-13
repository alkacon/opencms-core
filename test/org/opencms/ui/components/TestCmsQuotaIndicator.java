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

package org.opencms.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.gwt.shared.CmsQuotaStatus;
import org.opencms.gwt.shared.CmsQuotaStatus.State;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;

/**
 * Checks display limits, warning visibility and transitions back to non-numeric states.<p>
 */
public class TestCmsQuotaIndicator {

    @Test
    public void testCapacityDisplayAndOverflow() {

        long mb = 1000 * 1000;
        CmsQuotaIndicator indicator = new CmsQuotaIndicator(Locale.ENGLISH, false);
        indicator.setCapacityDisplay(true);
        indicator.setStatus(new CmsQuotaStatus("/a/", 25 * mb, 30 * mb, 100 * mb, State.NORMAL));
        assertEquals("Size: 100 MB", ((Label)indicator.getComponent(0)).getValue());
        indicator.setCapacityLabel("Budget");
        assertEquals("Budget: 100 MB", ((Label)indicator.getComponent(0)).getValue());
        indicator.setCapacityLabel(null);
        assertEquals("Size: 100 MB", ((Label)indicator.getComponent(0)).getValue());
        CssLayout details = (CssLayout)indicator.getComponent(2);
        assertTrue(details.isVisible());
        assertEquals("25 MB used (25 %)", ((Label)details.getComponent(0)).getValue());
        assertEquals("75 MB free", ((Label)details.getComponent(1)).getValue());
        assertEquals(null, ((ProgressBar)indicator.getComponent(1)).getDescription());
        indicator.setStatus(new CmsQuotaStatus("/a/", 100 * mb, 100 * mb, 100 * mb, State.EXCEEDED));
        assertEquals("0 B free", ((Label)details.getComponent(1)).getValue());
        indicator.setStatus(new CmsQuotaStatus("/a/", 110 * mb, 110 * mb, 100 * mb, State.EXCEEDED));
        assertEquals("10 MB over budget", ((Label)details.getComponent(1)).getValue());
        assertEquals(1.0f, ((ProgressBar)indicator.getComponent(1)).getValue(), 0.001f);
        indicator.setStatus(null);
        assertFalse(details.isVisible());
        assertFalse(indicator.getComponent(1).isVisible());
        indicator.setStatus(new CmsQuotaStatus("/a/", 25 * mb, 30 * mb, 0, State.UNLIMITED));
        assertTrue(details.isVisible());
        assertEquals("Size: unlimited", ((Label)indicator.getComponent(0)).getValue());
        assertEquals("25 MB used", ((Label)details.getComponent(0)).getValue());
        assertFalse(details.getComponent(1).isVisible());
        assertTrue(indicator.getComponent(1).isVisible());
        assertEquals(1.0f, ((ProgressBar)indicator.getComponent(1)).getValue(), 0.001f);
        assertTrue(indicator.getStyleName().contains("o-quota-unlimited"));
        indicator.setStatus(new CmsQuotaStatus("/a/", 25 * mb, 30 * mb, 100 * mb, State.NORMAL));
        assertTrue(details.getComponent(1).isVisible());
        assertEquals("75 MB free", ((Label)details.getComponent(1)).getValue());
        assertEquals(0.25f, ((ProgressBar)indicator.getComponent(1)).getValue(), 0.001f);
        indicator.setCapacityDisplay(false);
        indicator.setStatus(new CmsQuotaStatus("/a/", 25 * mb, 30 * mb, 100 * mb, State.NORMAL));
        assertFalse(details.isVisible());
        assertTrue(((Label)indicator.getComponent(0)).getValue().contains("25 MB of 100 MB"));
    }

    @Test
    public void testCompactWarningsOnly() {

        CmsQuotaIndicator indicator = new CmsQuotaIndicator(Locale.ENGLISH, true);
        indicator.setStatus(new CmsQuotaStatus("/a/", 799, 799, 1000, State.NORMAL));
        assertFalse(indicator.isVisible());
        indicator.setStatus(new CmsQuotaStatus("/a/", 800, 800, 1000, State.WARNING));
        assertTrue(indicator.isVisible());
        assertFalse(indicator.getComponent(1).isVisible());
        indicator.setStatus(new CmsQuotaStatus("/a/", 800, 800, 0, State.UNLIMITED));
        assertFalse(indicator.isVisible());
        indicator.setStatus(null);
        assertTrue(indicator.isVisible());
    }

    @Test
    public void testHorizontalPresentationAndRestorePreviousModes() {

        CmsQuotaIndicator indicator = new CmsQuotaIndicator(Locale.ENGLISH, false);
        indicator.setCapacityDisplay(true);
        indicator.setCapacityLabel("Budget");
        indicator.setHorizontal(true);
        Label label = (Label)indicator.getComponent(0);
        ProgressBar bar = (ProgressBar)indicator.getComponent(1);
        Label budgetValue = (Label)indicator.getComponent(3);
        for (long used : new long[] {250, 999, 1100}) {
            indicator.setStatus(new CmsQuotaStatus("/a/", used, used, 1000, CmsQuotaStatus.getUsageState(used, 1000)));
            assertEquals((used / 10) + " %", label.getValue());
            assertTrue(bar.isVisible());
            assertEquals("Budget: 1 KB", budgetValue.getValue());
            assertEquals(Math.min(1f, used / 1000f), bar.getValue(), 0.0001f);
            assertFalse(indicator.getComponent(2).isVisible());
            assertTrue(label.getDescription().contains("used"));
        }
        indicator.setHorizontal(false);
        assertTrue(indicator.getComponent(2).isVisible());
        assertTrue(label.getValue().startsWith("Budget:"));
        assertFalse(indicator.getStyleName().contains("o-size-indicator-horizontal"));
        indicator.setTextOnly(true);
        indicator.setHorizontal(true);
        assertEquals("110 %", label.getValue());
        assertTrue(bar.isVisible());
        indicator.setHorizontal(false);
        assertTrue(label.getValue().startsWith("110 % ·"));
        assertFalse(bar.isVisible());
        indicator.setHorizontal(true);
        indicator.setStatus(new CmsQuotaStatus("/a/", 50, 50, 0, State.UNLIMITED));
        assertEquals("", label.getValue());
        assertEquals("Budget: unlimited", budgetValue.getValue());
        assertEquals(null, label.getDescription());
        assertTrue(bar.isVisible());
        assertEquals(1f, bar.getValue(), 0f);
        indicator.setStatus(null);
        assertEquals("—", label.getValue());
        assertFalse(bar.isVisible());
        assertTrue(label.getDescription().contains("unavailable"));
        indicator.setStatus(new CmsQuotaStatus("/a/", 50, 50, -1, State.UNCONFIGURED));
        assertEquals("No storage budget configured", label.getValue());
        assertFalse(bar.isVisible());
        assertFalse(budgetValue.isVisible());
        assertEquals(null, label.getDescription());
    }

    @Test
    public void testOverflowAndUnavailable() {

        CmsQuotaIndicator indicator = new CmsQuotaIndicator(Locale.ENGLISH, false);
        indicator.setStatus(new CmsQuotaStatus("/a/", 1100, 1200, 1000, State.EXCEEDED));
        ProgressBar bar = (ProgressBar)indicator.getComponent(1);
        assertEquals(1.0f, bar.getValue(), 0.001f);
        assertTrue(((Label)indicator.getComponent(0)).getValue().contains("110"));
        assertTrue(indicator.getStyleName().contains("exceeded"));
        indicator.setStatus(new CmsQuotaStatus("/a/", 1100, 1200, 0, State.UNLIMITED));
        assertFalse(bar.isVisible());
        assertTrue(indicator.isVisible());
        indicator.setStatus(null);
        assertTrue(indicator.isVisible());
        assertFalse(bar.isVisible());
        assertTrue(indicator.getStyleName().contains("unavailable"));
    }

    @Test
    public void testSmallUsageAndTruncatedPercentages() {

        long gb = 1000L * 1000 * 1000;
        long mb = 1000L * 1000;
        CmsQuotaIndicator indicator = new CmsQuotaIndicator(Locale.ENGLISH, false);
        Label text = (Label)indicator.getComponent(0);
        CmsQuotaStatus small = new CmsQuotaStatus("/a/", mb, mb, 2 * gb, State.NORMAL);
        assertTrue(small.getPercentage() > 0);
        indicator.setStatus(small);
        assertTrue(text.getValue().contains("1 MB of 2 GB"));
        assertTrue(text.getValue().contains("0 %"));
        assertFalse(text.getValue().contains("<"));
        indicator.setStatus(new CmsQuotaStatus("/a/", 0, 0, 2 * gb, State.NORMAL));
        assertTrue(text.getValue().contains("0 B"));
        assertTrue(text.getValue().contains("0 %"));
        assertFalse(text.getValue().contains("<"));
        indicator.setStatus(new CmsQuotaStatus("/a/", 1, 1, 2 * gb, State.NORMAL));
        assertTrue(text.getValue().contains("1 B"));
        indicator.setStatus(new CmsQuotaStatus("/a/", 1000, 1000, 2 * gb, State.NORMAL));
        assertTrue(text.getValue().contains("1 KB"));
        indicator.setStatus(new CmsQuotaStatus("/a/", gb / 4, gb / 4, 2 * gb, State.NORMAL));
        assertTrue(text.getValue().contains("12 %"));
        indicator.setStatus(new CmsQuotaStatus("/a/", gb - 1, gb - 1, gb, State.WARNING));
        assertTrue(text.getValue().contains("99 %"));
        indicator.setStatus(new CmsQuotaStatus("/a/", gb, gb, gb, State.EXCEEDED));
        assertTrue(text.getValue().contains("100 %"));
        indicator = new CmsQuotaIndicator(Locale.GERMAN, false);
        indicator.setStatus(small);
        text = (Label)indicator.getComponent(0);
        assertTrue(text.getValue().contains("1 MB"));
        assertTrue(text.getValue().contains("2 GB"));
        assertTrue(text.getValue().contains("0 %"));
        indicator.setStatus(new CmsQuotaStatus("/a/", 100 * 1000, 100 * 1000, 100 * mb, State.NORMAL));
        assertTrue(text.getValue().contains("100 KB"));
        assertTrue(text.getValue().contains("100 MB"));
        assertFalse(text.getValue().contains(" GB"));
        indicator.setStatus(new CmsQuotaStatus("/a/", 1536, 1536, 10 * 1000, State.NORMAL));
        assertTrue(text.getValue().contains("2 KB"));
        assertTrue(text.getValue().contains("10 KB"));
        indicator.setStatus(new CmsQuotaStatus("/a/", 100 * 1000, 100 * 1000, gb / 10, State.NORMAL));
        assertTrue(text.getValue().contains("100 MB"));
    }

    @Test
    public void testTextInsideBarAndStateChanges() {

        CmsQuotaIndicator indicator = new CmsQuotaIndicator(Locale.ENGLISH, false);
        indicator.setTextInsideBar(true);
        indicator.setStatus(new CmsQuotaStatus("/a/", 1100, 1200, 1000, State.EXCEEDED));
        assertTrue(indicator.getComponent(0).isVisible());
        ProgressBar bar = (ProgressBar)indicator.getComponent(1);
        assertTrue(bar.isVisible());
        assertEquals(null, bar.getDescription());
        assertTrue(((Label)indicator.getComponent(0)).getValue().contains("110 %"));
        assertFalse(((Label)indicator.getComponent(0)).getValue().contains("exceeded"));
        assertTrue(indicator.getStyleName().contains("o-quota-inline"));
        indicator.setStatus(null);
        assertFalse(bar.isVisible());
        assertTrue(indicator.getComponent(0).isVisible());
        assertFalse(indicator.getStyleName().contains("o-quota-inline"));
        indicator.setStatus(new CmsQuotaStatus("/a/", 800, 900, 1000, State.WARNING));
        assertTrue(indicator.getComponent(0).isVisible());
        assertTrue(bar.isVisible());
        indicator.setTextInsideBar(false);
        assertTrue(bar.getDescription().contains("80 %"));
        assertFalse(indicator.getStyleName().contains("o-quota-inline"));
    }

    @Test
    public void testTextOnlyUsageAndReturnToCapacityDisplay() {

        long mb = 1000000L;
        CmsQuotaIndicator indicator = new CmsQuotaIndicator(Locale.ENGLISH, false);
        indicator.setCapacityDisplay(true);
        indicator.setTextOnly(true);
        for (long used : new long[] {250, 999, 1100}) {
            indicator.setStatus(
                new CmsQuotaStatus(
                    "/a/",
                    used * mb,
                    used * mb,
                    1000 * mb,
                    CmsQuotaStatus.getUsageState(used * mb, 1000 * mb)));
            assertTrue(indicator.isVisible());
            assertFalse(indicator.getComponent(1).isVisible());
            assertFalse(indicator.getComponent(2).isVisible());
            String size = used == 1100 ? "1.1 GB" : used + " MB";
            assertEquals((used / 10) + " % · " + size + " of 1 GB", ((Label)indicator.getComponent(0)).getValue());
        }
        indicator.setTextOnly(false);
        assertTrue(indicator.getComponent(1).isVisible());
        assertTrue(indicator.getComponent(2).isVisible());
        assertEquals("Size: 1 GB", ((Label)indicator.getComponent(0)).getValue());
        for (State state : new State[] {State.UNLIMITED, State.UNCONFIGURED, State.UNAVAILABLE}) {
            indicator.setTextOnly(true);
            indicator.setStatus(new CmsQuotaStatus("/a/", 250 * mb, 250 * mb, 0, state));
            assertTrue(indicator.isVisible());
            assertFalse(indicator.getComponent(1).isVisible());
            assertFalse(indicator.getComponent(2).isVisible());
            assertFalse(((Label)indicator.getComponent(0)).getValue().contains("%"));
        }
    }
}
