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

package org.opencms.ade.galleries.client.preview.util;

import org.opencms.ade.galleries.shared.CmsPoint;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A coordinate system transform represented as a composition of multiple other transforms.<p>
 */
public class CmsCompositeTransform implements I_CmsTransform {

    /** The transforms this transform is composed of. */
    private List<I_CmsTransform> m_transforms;

    /**
     * Creates a new instance.<p>
     *
     * @param transforms the list of transforms this transform should be composed of
     */
    public CmsCompositeTransform(List<I_CmsTransform> transforms) {

        m_transforms = transforms;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        List<String> comps = new ArrayList<>();
        for (I_CmsTransform tf : m_transforms) {
            comps.add(tf.toString());
        }
        return CmsStringUtil.listAsString(comps, " ");
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.util.I_CmsTransform#transformBack(org.opencms.ade.galleries.shared.CmsPoint)
     */
    public CmsPoint transformBack(CmsPoint point) {

        for (int i = m_transforms.size() - 1; i >= 0; i--) {
            point = m_transforms.get(i).transformBack(point);
        }
        return point;
    }

    /**
     * @see org.opencms.ade.galleries.client.preview.util.I_CmsTransform#transformForward(org.opencms.ade.galleries.shared.CmsPoint)
     */
    public CmsPoint transformForward(CmsPoint point) {

        for (I_CmsTransform transform : m_transforms) {
            point = transform.transformForward(point);
        }
        return point;
    }

}
