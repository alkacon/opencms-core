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

package org.opencms.ade.sitemap;

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for the ADE sitemap navigation position calculation algorithm.<p>
 */
public class TestNavPosCalculator extends OpenCmsTestCase {

    /**
     * Test constructor.<p>
     */
    public TestNavPosCalculator() {

        super("TestNavPosCalculator");
    }

    /**
     * Helper method to create a list.<p>
     *
     * @param args the list elements
     *
     * @return the list of elements
     */
    private static <X> List<X> list(X... args) {

        List<X> result = new ArrayList<X>();
        for (X x : args) {
            result.add(x);
        }
        return result;
    }

    /**
     * Tests filtering of non-navigation entries from the navigation list.<p>
     */
    public void testFilterNonNavEntries() {

        CmsJspNavElement a = dummyNav("foo", 1);
        CmsJspNavElement b = nonNav("bar");
        CmsJspNavElement c = dummyNav("baz", 3);
        CmsSitemapNavPosCalculator npc = new CmsSitemapNavPosCalculator(list(a, b, c), dummyRes("fasdf"), 3);
        List<CmsJspNavElement> navs = npc.getResultList();
        int pos = npc.getInsertPositionInResult();
        assertEquals(-1, navs.indexOf(b));
        checkIntegrity(navs, pos);
    }

    /**
     * Tests insertion between entries with the same nav pos value.<p>
     */
    public void testInserBetweenEquals() {

        CmsJspNavElement a = dummyNav("foo", 1);
        CmsJspNavElement b = dummyNav("bar", 1);
        CmsJspNavElement c = dummyNav("baz", 3);
        CmsSitemapNavPosCalculator npc = new CmsSitemapNavPosCalculator(list(a, b, c), dummyRes("fasdf"), 1);
        List<CmsJspNavElement> navs = npc.getResultList();
        System.out.print("testInsertBetweenEquals=");
        printNav(navs);
        assertTrue(checkIncreasing(navs));
    }

    /**
     * Tests insertion after a Float.MAX_VALUE navigation entry.<p>
     *
     */
    public void testInsertAfterMax() {

        CmsJspNavElement a = dummyNav("foo", 1);
        CmsJspNavElement b = dummyNav("bar", Float.MAX_VALUE);
        CmsJspNavElement c = dummyNav("baz", Float.MAX_VALUE);
        CmsSitemapNavPosCalculator npc = new CmsSitemapNavPosCalculator(list(a, b, c), dummyRes("fasdf"), 3);
        List<CmsJspNavElement> navs = npc.getResultList();
        int pos = npc.getInsertPositionInResult();

        checkIntegrity(navs, pos);
        assertTrue(checkIncreasing(navs));
        System.out.print("testInsertAfterMax=");
        printNav(navs);
    }

    /**
     * Tests insertion between two Float.MAX_VALUE navigation entries.<p>
     */
    public void testInsertAtMax() {

        CmsJspNavElement a = dummyNav("foo", 1);
        CmsJspNavElement b = dummyNav("bar", Float.MAX_VALUE);
        CmsJspNavElement c = dummyNav("baz", Float.MAX_VALUE);
        CmsSitemapNavPosCalculator npc = new CmsSitemapNavPosCalculator(list(a, b, c), dummyRes("fasdf"), 2);
        List<CmsJspNavElement> navs = npc.getResultList();
        int pos = npc.getInsertPositionInResult();
        checkIntegrity(navs, pos);
        assertTrue(checkIncreasing(navs));
        System.out.print("testInsertAtMax=");
        printNav(navs);
    }

    /**
     * Tests insertion before a Float.MAX_VALUE navigation entry.<p>
     */
    public void testInsertBeforeMax() {

        CmsJspNavElement a = dummyNav("foo", 1);
        CmsJspNavElement b = dummyNav("bar", Float.MAX_VALUE);
        CmsJspNavElement c = dummyNav("baz", Float.MAX_VALUE);
        CmsSitemapNavPosCalculator npc = new CmsSitemapNavPosCalculator(list(a, b, c), dummyRes("fasdf"), 1);
        List<CmsJspNavElement> navs = npc.getResultList();
        int pos = npc.getInsertPositionInResult();
        checkIntegrity(navs, pos);
        assertFalse(checkIncreasing(navs));
        System.out.print("testInsertBeforeMax=");
        printNav(navs);
    }

    /**
     * Tests insertion between navigation entries with different NavPos values.<p>
     */
    public void testInsertBetweenBlocks() {

        CmsJspNavElement a = dummyNav("foo", 1);
        CmsJspNavElement b = dummyNav("bar", 1);
        CmsJspNavElement c = dummyNav("baz", 3);
        CmsSitemapNavPosCalculator npc = new CmsSitemapNavPosCalculator(list(a, b, c), dummyRes("fasdf"), 2);
        List<CmsJspNavElement> navs = npc.getResultList();
        CmsJspNavElement moved = navs.get(2);
        System.out.print("testInsertBetweenBlocks=");
        printNav(navs);
        assertFalse(checkIncreasing(navs));
        int pos = npc.getInsertPositionInResult();
        checkIntegrity(navs, pos);
        assertTrue(checkNonDecreasing(navs));
        assertTrue(1 < moved.getNavPosition());
        assertTrue(moved.getNavPosition() < 3);
    }

    /**
     * Tests insertion at the end of the navigation.<p>
     */
    public void testInsertEndNormal() {

        CmsJspNavElement a = dummyNav("foo", 1);
        CmsJspNavElement b = dummyNav("bar", 2);
        CmsJspNavElement c = dummyNav("baz", 3);
        CmsSitemapNavPosCalculator npc = new CmsSitemapNavPosCalculator(list(a, b, c), dummyRes("fasdf"), 3);
        List<CmsJspNavElement> navs = npc.getResultList();

        int pos = npc.getInsertPositionInResult();
        checkIntegrity(navs, pos);
        assertTrue(checkIncreasing(navs));
        System.out.print("testInsertEndNormal=");
        printNav(navs);
    }

    /**
     * Tests insertion at the front of the  navigation.<p>
     */
    public void testInsertFrontNormal() {

        CmsJspNavElement a = dummyNav("foo", 1);
        CmsJspNavElement b = dummyNav("bar", 2);
        CmsJspNavElement c = dummyNav("baz", 3);
        CmsSitemapNavPosCalculator npc = new CmsSitemapNavPosCalculator(list(a, b, c), dummyRes("fasdf"), 0);
        List<CmsJspNavElement> navs = npc.getResultList();
        int pos = npc.getInsertPositionInResult();
        checkIntegrity(navs, pos);
        assertTrue(checkIncreasing(navs));
        System.out.print("testInsertFrontNormal=");
        printNav(navs);
    }

    /**
     * Tests the normal insertion case.<p>
     */
    public void testInsertNormal() {

        CmsJspNavElement a = dummyNav("foo", 1);
        CmsJspNavElement b = dummyNav("bar", 2);
        CmsJspNavElement c = dummyNav("baz", 3);
        CmsSitemapNavPosCalculator npc = new CmsSitemapNavPosCalculator(list(a, b, c), dummyRes("fasdf"), 1);
        List<CmsJspNavElement> navs = npc.getResultList();
        int pos = npc.getInsertPositionInResult();
        checkIntegrity(navs, pos);
        System.out.print("testInsertNormal=");
        printNav(navs);
        assertTrue(checkIncreasing(navs));
    }

    /**
     * Helper method to check whether a list of navigation entries have increasing nav pos values.<p>
     *
     * @param navs the list of navigation entries
     *
     * @return true if the navigation entries have increasing nav pos values
     */
    private boolean checkIncreasing(List<CmsJspNavElement> navs) {

        if (navs.size() < 2) {
            return true;
        }
        for (int i = 0; i < (navs.size() - 1); i++) {
            if (navs.get(i).getNavPosition() >= navs.get(i + 1).getNavPosition()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Performs a sanity check after a navigation entry insertion.<p>
     *
     * This checks whether the list of entries have non-decreasing navigation positions and
     * whether the sequence (left neighbor of inserted entry, inserted entry, right neighbor of inserted entry)
     * have increasing navigation positions.
     *
     * @param navs the list of navigation entries
     * @param insertPosition the insertion position
     */
    private void checkIntegrity(List<CmsJspNavElement> navs, int insertPosition) {

        float navPos = navs.get(insertPosition).getNavPosition();
        if (insertPosition > 0) {
            assertTrue(navs.get(insertPosition - 1).getNavPosition() < navPos);
        }
        if (insertPosition < (navs.size() - 1)) {
            assertTrue(navs.get(insertPosition + 1).getNavPosition() > navPos);
        }
        assertTrue(checkNonDecreasing(navs));
        for (int i = 0; i < navs.size(); i++) {
            assertTrue(navs.get(i).getNavPosition() > 0);
        }
    }

    /**
     * Helper method to check whether a list of navigation entries have non-decreasing navigation positions.<p>
     *
     * @param navs the navigation entries
     *
     * @return true if the sequence of navigation positions is non-decreasing
     */
    private boolean checkNonDecreasing(List<CmsJspNavElement> navs) {

        if (navs.size() < 2) {
            return true;
        }
        for (int i = 0; i < (navs.size() - 1); i++) {
            if (navs.get(i).getNavPosition() > navs.get(i + 1).getNavPosition()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a dummy navigation entry.<p>
     *
     * @param name the name of the navigation entry
     * @param navPos the navigation position
     *
     * @return the dummy navigation entry
     */
    private CmsJspNavElement dummyNav(String name, float navPos) {

        Map<String, String> props = new HashMap<String, String>();
        props.put(CmsPropertyDefinition.PROPERTY_NAVPOS, "" + navPos);
        props.put(CmsPropertyDefinition.PROPERTY_NAVTEXT, name);
        return new CmsJspNavElement(name, dummyRes(name), props);
    }

    /**
     * Helper method to create a dummy resource.<p>
     *
     * @param path the resource path
     *
     * @return the dummyy resource
     */
    private CmsResource dummyRes(String path) {

        return new CmsResource(
            CmsUUID.getConstantUUID(path),
            null,
            path,
            0,
            true,
            0,
            null,
            null,
            0,
            null,
            0,
            null,
            0,
            0,
            0,
            0,
            0,
            0);
    }

    /**
     * Helper method for generating a dummy CmsJspNavElement instance which is not in the navigation.<p>
     *
     * @param name the name of the navigation entry
     *
     * @return the dummy navigation entry
     */
    private CmsJspNavElement nonNav(String name) {

        Map<String, String> props = new HashMap<String, String>();
        return new CmsJspNavElement(name, dummyRes(name), props);
    }

    /**
     * Helper method for printing a list of navigation entries.<p>
     *
     * @param navs the list of navigation entries
     */
    private void printNav(List<CmsJspNavElement> navs) {

        System.out.print("[");
        for (CmsJspNavElement nav : navs) {
            System.out.print(nav.getResourceName() + "::" + nav.getNavPosition() + " ");
        }
        System.out.println("]");
    }

}
