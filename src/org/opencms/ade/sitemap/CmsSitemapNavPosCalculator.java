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

import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Helper class for recalculating navigation positions when a user has changed the order of navigation entries in the sitemap
 * editor.<p>
 *
 *  This is harder than it sounds because we need to handle special cases like e.g. the user inserting an entry
 * between two existing entries with the same navigation position, which means we need to update the navigation positions
 * of multiple entries to force the ordering which the user wanted.<p>
 */
public class CmsSitemapNavPosCalculator {

    /**
     * Internal class which encapsulates information about a position in the navigation list.<p>
     */
    private class PositionInfo {

        /** Flag which indicates whether the position is inside the navigation list. */
        private boolean m_exists;

        /** The navigation position as a float. */
        private float m_navPos;

        /**
         * Creates a new position info bean.<p>
         *
         * @param exists true if the position is not out of bounds
         *
         * @param navPos the navigation position
         */
        public PositionInfo(boolean exists, float navPos) {

            m_exists = exists;
            m_navPos = navPos;
        }

        /**
         * Gets the navigation position.
         *
         * @return the navigation position
         */
        public float getNavPos() {

            return m_navPos;
        }

        /**
         * Checks whether there is a maximal nav pos value at the position.<p>
         *
         * @return true if there is a maximal nav pos value at the position
         */
        public boolean isMax() {

            return m_navPos == Float.MAX_VALUE;
        }

        /**
         * Returns true if the position is neither out of bounds nor a position with a maximal nav pos value.<p>
         *
         * @return true if the position is neither out of bounds nor a position with a maximal nav pos value
         */
        public boolean isNormal() {

            return !isOutOfBounds() && !isMax();
        }

        /**
         * Returns true if the position is not in the list of navigation entries.<p>
         *
         * @return true if the position is not in the list of navigation entries
         */
        public boolean isOutOfBounds() {

            return !m_exists;
        }
    }

    /** Dummy file name for the inserted dummy navigation element. */
    public static final String DUMMY_PATH = "@moved@";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapNavPosCalculator.class);

    /** The insert position in the final result list. */
    private int m_insertPositionInResult;

    /** The final result list. */
    private List<CmsJspNavElement> m_resultList;

    /**
     * Creates a new sitemap navigation position calculator and performs the navigation position calculation for a given
     * insertion operation.<p>
     *
     * @param navigation the existing navigation element list
     * @param movedElement the resource which should be inserted
     * @param insertPosition the insertion position in the list
     */
    public CmsSitemapNavPosCalculator(List<CmsJspNavElement> navigation, CmsResource movedElement, int insertPosition) {

        List<CmsJspNavElement> workList = new ArrayList<CmsJspNavElement>(navigation);
        CmsJspNavElement dummyNavElement = new CmsJspNavElement(
            DUMMY_PATH,
            movedElement,
            new HashMap<String, String>());

        // There may be another navigation element for the same resource in the navigation, so remove it
        for (int i = 0; i < workList.size(); i++) {
            CmsJspNavElement currentElement = workList.get(i);
            if ((i != insertPosition)
                && currentElement.getResource().getStructureId().equals(movedElement.getStructureId())) {
                workList.remove(i);
                break;
            }
        }
        if (insertPosition > workList.size()) {
            // could happen if the navigation was concurrently changed by another user
            insertPosition = workList.size();
        }
        // First, insert the dummy element at the correct position in the list.
        workList.add(insertPosition, dummyNavElement);

        // now remove elements which aren't actually part of the navigation
        Iterator<CmsJspNavElement> it = workList.iterator();
        while (it.hasNext()) {
            CmsJspNavElement nav = it.next();
            if (!nav.isInNavigation() && (nav != dummyNavElement)) {
                it.remove();
            }
        }
        insertPosition = workList.indexOf(dummyNavElement);
        m_insertPositionInResult = insertPosition;

        /*
         * Now calculate the "block" of the inserted element.
         * The block is the range of indices for which the navigation
         * positions need to be updated. This range only needs to contain
         * more than the inserted element if it was inserted either between two elements
         * with the same navigation position or after an element with Float.MAX_VALUE
         * navigation position. In either of those two cases, the block will contain
         * all elements with the same navigation position.
         */

        int blockStart = insertPosition;
        int blockEnd = insertPosition + 1;

        PositionInfo before = getPositionInfo(workList, insertPosition - 1);
        PositionInfo after = getPositionInfo(workList, insertPosition + 1);
        boolean extendBlock = false;
        float blockValue = 0;

        if (before.isMax()) {
            blockValue = Float.MAX_VALUE;
            extendBlock = true;
        } else if (before.isNormal() && after.isNormal() && (before.getNavPos() == after.getNavPos())) {
            blockValue = before.getNavPos();
            extendBlock = true;
        }
        if (extendBlock) {
            while ((blockStart > 0) && (workList.get(blockStart - 1).getNavPosition() == blockValue)) {
                blockStart -= 1;
            }
            while ((blockEnd < workList.size())
                && ((blockEnd == (insertPosition + 1)) || (workList.get(blockEnd).getNavPosition() == blockValue))) {
                blockEnd += 1;
            }
        }

        /*
         * Now calculate the new navigation positions for the elements in the block using the information
         * from the elements directly before and after the block, and set the positions in the nav element
         * instances.
         */
        PositionInfo beforeBlock = getPositionInfo(workList, blockStart - 1);
        PositionInfo afterBlock = getPositionInfo(workList, blockEnd);

        // now calculate the new navigation positions for the elements in the block (

        List<Float> newNavPositions = interpolatePositions(beforeBlock, afterBlock, blockEnd - blockStart);
        for (int i = 0; i < (blockEnd - blockStart); i++) {
            workList.get(i + blockStart).setNavPosition(newNavPositions.get(i).floatValue());
        }
        m_resultList = Collections.unmodifiableList(workList);
    }

    /**
     * Gets the insert position in the final result list.<p>
     *
     * @return the insert position in the final result
     */
    public int getInsertPositionInResult() {

        return m_insertPositionInResult;
    }

    /**
     * Gets the changed navigation entries from the final result list.<p>
     *
     * @return the changed navigation entries for the final result list
     */
    public List<CmsJspNavElement> getNavigationChanges() {

        List<CmsJspNavElement> newNav = getResultList();
        List<CmsJspNavElement> changedElements = new ArrayList<CmsJspNavElement>();
        for (CmsJspNavElement elem : newNav) {
            if (elem.hasChangedNavPosition()) {
                changedElements.add(elem);
            }
        }
        return changedElements;
    }

    /**
     * Gets the final result list.<p>
     *
     * @return the final  result list
     */
    public List<CmsJspNavElement> getResultList() {

        return m_resultList;
    }

    /**
     * Gets the position info bean for a given position.<p>
     *
     * @param navigation the navigation element list
     * @param index the index in the navigation element list
     *
     * @return the position info bean for a given position
     */
    private PositionInfo getPositionInfo(List<CmsJspNavElement> navigation, int index) {

        if ((index < 0) || (index >= navigation.size())) {
            return new PositionInfo(false, -1);
        }
        float navPos = navigation.get(index).getNavPosition();
        return new PositionInfo(true, navPos);
    }

    /**
     * Helper method to generate a list of floats between two given values.<p>
     *
     * @param min the lower bound
     * @param max the upper bound
     * @param steps the number of floats to generate
     *
     * @return the generated floats
     */
    private List<Float> interpolateBetween(float min, float max, int steps) {

        float delta = (max - min) / (steps + 1);
        List<Float> result = new ArrayList<Float>();
        float num = min;

        for (int i = 0; i < steps; i++) {
            num += delta;
            result.add(Float.valueOf(num));
        }
        return result;
    }

    /**
     * Helper method to generate an ascending list of floats below a given number.<p>
     *
     * @param max the upper bound
     * @param steps the number of floats to generate
     *
     * @return the generated floats
     */
    private List<Float> interpolateDownwards(float max, int steps) {

        List<Float> result = new ArrayList<Float>();
        if (max > 0) {
            // We try to generate a "nice" descending list of non-negative floats
            // where the step size is bigger for bigger "max" values.
            float base = (max > 1) ? (float)Math.floor(max) : max;
            float stepSize = 1000f;

            // reduce step size until the smallest element is greater than max/10.
            while ((base - (steps * stepSize)) < (max / 10.0f)) {
                stepSize = reduceStepSize(stepSize);
            }
            // we have determined the step size, now we generate the actual numbers
            for (int i = 0; i < steps; i++) {
                result.add(Float.valueOf(base - ((i + 1) * stepSize)));
            }
            Collections.reverse(result);
        } else {
            LOG.warn("Invalid navpos value: " + max);
            for (int i = 0; i < steps; i++) {
                result.add(Float.valueOf(max - (i + 1)));
            }
            Collections.reverse(result);
        }
        return result;
    }

    /**
     * Helper method to generate an ascending list of floats.<p>
     *
     * @param steps the number of floats to generate
     *
     * @return the generated floats
     */
    private List<Float> interpolateEmpty(int steps) {

        List<Float> result = new ArrayList<Float>();
        for (int i = 0; i < steps; i++) {
            result.add(Float.valueOf(1 + i));
        }
        return result;
    }

    /**
     * Generates the new navigation positions for a range of navigation items.<p>
     *
     * @param left the position info for the navigation entry left of the range
     * @param right the position info for the navigation entry right of the range
     * @param steps the number of entries in the range
     *
     * @return the list of new navigation positions
     */
    private List<Float> interpolatePositions(PositionInfo left, PositionInfo right, int steps) {

        if (left.isOutOfBounds()) {
            if (right.isNormal()) {
                return interpolateDownwards(right.getNavPos(), steps);
            } else if (right.isMax() || right.isOutOfBounds()) {
                return interpolateEmpty(steps);
            } else {
                // can't happen
                assert false;
            }
        } else if (left.isNormal()) {
            if (right.isOutOfBounds() || right.isMax()) {
                return interpolateUpwards(left.getNavPos(), steps);
            } else if (right.isNormal()) {
                return interpolateBetween(left.getNavPos(), right.getNavPos(), steps);
            } else {
                // can't happen
                assert false;
            }
        } else {
            // can't happen
            assert false;
        }
        return null;

    }

    /**
     * Helper method for generating an ascending list of floats above a given number.<p>
     *
     * @param min the lower bound
     * @param steps the number of floats to generate
     *
     * @return the generated floats
     */
    private List<Float> interpolateUpwards(float min, int steps) {

        List<Float> result = new ArrayList<Float>();
        for (int i = 0; i < steps; i++) {
            result.add(Float.valueOf(min + 1 + i));
        }
        return result;
    }

    /**
     * Reduces the step size for generating descending navpos sequences.<p>
     *
     * @param oldStepSize the previous step size
     *
     * @return the new (smaller) step size
     */
    private float reduceStepSize(float oldStepSize) {

        if (oldStepSize > 1) {
            // try to reduce unnecessary digits after the decimal point
            return oldStepSize / 10f;
        } else {
            return oldStepSize / 2f;
        }
    }
}
