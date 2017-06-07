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

package org.opencms.ade.editprovider.client;

import org.opencms.gwt.client.util.CmsPositionBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to calculate positions for a set of direct edit buttons so that
 * they don't overlap.<p>
 *
 * @since 8.0.0
 */
public class CmsEditablePositionCalculator {

    /**
     * A comparator class which compares position beans by their left edge.<p>
     */
    protected class LeftComparator implements Comparator<CmsPositionBean> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(CmsPositionBean o1, CmsPositionBean o2) {

            int l1 = o1.getLeft();
            int l2 = o2.getLeft();
            if (l1 < l2) {
                return -1;
            }
            if (l1 > l2) {
                return +1;
            }
            return 0;
        }
    }

    /** A map of positions by element id. */
    private Map<String, CmsPositionBean> m_positionMap = new HashMap<String, CmsPositionBean>();

    /** The internal list of positions. */
    private List<CmsPositionBean> m_positions = new ArrayList<CmsPositionBean>();

    /** The assumed width of a direct edit button bar. */
    private static int WIDTH = 65;

    /** The assumed height of a direct edit button bar. */
    private static int HEIGHT = 24;

    /**
     * Creates a new instance.<p>
     *
     * @param positions the map of original positions by element id (will not be altered)
     */
    public CmsEditablePositionCalculator(Map<String, CmsPositionBean> positions) {

        for (Map.Entry<String, CmsPositionBean> entry : positions.entrySet()) {
            CmsPositionBean newPos = new CmsPositionBean(entry.getValue());
            m_positionMap.put(entry.getKey(), newPos);
            m_positions.add(newPos);
        }
    }

    /**
     * Calculates non-overlapping positions for the button bars and returns them in a map with
     * the element ids as keys.<p>
     *
     * @return the map of non-overlapping positions
     */
    public Map<String, CmsPositionBean> calculatePositions() {

        int maxCollisions = 500;
        // if there are more than 500 collisions, the style is probably messed up; give up.
        while (checkCollision() && (maxCollisions > 0)) {
            maxCollisions -= 1;
        }
        return m_positionMap;
    }

    /**
     * Checks whether a collision occurs and handle it if necessary.<p>
     *
     * @return true if a collision occured
     */
    protected boolean checkCollision() {

        // sort the positions by their left x coordinate, so we can easily exclude
        // pairs of positions which don't overlap horizontally
        sortByLeft();
        int i;
        for (i = 0; i < m_positions.size(); i++) {
            for (int j = i + 1; (j < m_positions.size())
                && intersectsHorizontally(m_positions.get(i), m_positions.get(j)); j++) {
                if (intersectsVertically(m_positions.get(i), m_positions.get(j))) {
                    handleCollision(m_positions.get(i), m_positions.get(j));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Handles a collision by moving the lower position down.<p>
     *
     * @param p1 the first position
     * @param p2 the second position
     */
    protected void handleCollision(CmsPositionBean p1, CmsPositionBean p2) {

        CmsPositionBean positionToChange = p1;
        if (p1.getTop() <= p2.getTop()) {
            positionToChange = p2;
        }
        positionToChange.setTop(positionToChange.getTop() + 25);
    }

    /**
     * Checks for intersection of two one-dimensional intervals.<p>
     *
     * @param a1 the left edge of the first interval
     * @param a2 the right edge of the first interval
     * @param b1 the left edge of the second interval
     * @param b2 the right edge of the second interval
     *
     * @return true if the intervals intersect
     */
    protected boolean intersectIntervals(int a1, int a2, int b1, int b2) {

        return !((a2 < b1) || (a1 > b2));
    }

    /**
     * Checks whether two positions intersect horizontally.<p>
     *
     * @param p1 the first position
     * @param p2 the second position
     *
     * @return true if the positions intersect horizontally
     */
    protected boolean intersectsHorizontally(CmsPositionBean p1, CmsPositionBean p2) {

        return intersectIntervals(p1.getLeft(), p1.getLeft() + WIDTH, p2.getLeft(), p2.getLeft() + WIDTH);
    }

    /**
     * Checks whether two positions intersect vertically.<p>
     *
     * @param p1 the first position
     * @param p2 the second position
     *
     * @return if the positions intersect vertically
     */
    protected boolean intersectsVertically(CmsPositionBean p1, CmsPositionBean p2) {

        return intersectIntervals(p1.getTop(), p1.getTop() + HEIGHT, p2.getTop(), p2.getTop() + HEIGHT);
    }

    /**
     * Sorts the internal list of positions by their left edge.<p>
     */
    protected void sortByLeft() {

        Collections.sort(m_positions, new LeftComparator());
    }

}
