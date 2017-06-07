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

package org.opencms.ade.galleries.client.preview;

/**
 * Image format restriction. To be used within the image format tab of the image preview.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsFormatRestriction {

    /** Value to indicate this dimension is not set. */
    int DIMENSION_NOT_SET = -1;

    /**
     * Adjust the given cropping parameter bean to this format restriction.<p>
     * Execute on selection of this restriction.<p>
     *
     * @param croppingParam the cropping parameter bean to adjust
     */
    void adjustCroppingParam(CmsCroppingParamBean croppingParam);

    /**
     * Returns the preset height for this format restriction.<p>
     *
     * @param orgHeight the original image height
     * @param orgWidth the original image width
     *
     * @return the height
     */
    int getHeight(int orgHeight, int orgWidth);

    /**
     * Returns the label for the format.<p>
     *
     * @return the label
     */
    String getLabel();

    /**
     * Returns the format name.<p>
     *
     * @return the format name
     */
    String getName();

    /**
     * Returns the preset width for this format restriction.<p>
     *
     * @param orgHeight the original image height
     * @param orgWidth the original image width
     *
     * @return the width
     */
    int getWidth(int orgHeight, int orgWidth);

    /**
     * Returns if this format allows cropping.<p>
     *
     * @return <code>true</code> if cropping is allowed
     */
    boolean isCroppingEnabled();

    /**
     * Returns if this format has a fixed height/width ratio.<p>
     *
     * @return <code>true</code> if the ratio is fixed
     */
    boolean isFixedRatio();

    /**
     * Returns if height is editable.<p>
     *
     * @return <code>true</code> if height is editable
     */
    boolean isHeightEditable();

    /**
     * Returns if width is editable.<p>
     *
     * @return <code>true</code> if width is editable
     */
    boolean isWidthEditable();

    /**
     * Checks whether the given cropping parameter matches these restrictions.<p>
     *
     * @param croppingParam the cropping parameter to match
     *
     * @return <code>true</code> if the restrictions are matched by the given cropping parameter
     */
    boolean matchesCroppingParam(CmsCroppingParamBean croppingParam);
}
