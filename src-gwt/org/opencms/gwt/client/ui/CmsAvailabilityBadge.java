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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.shared.CmsAvailabilityInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget to show availability information for container elements or list elements.
 *
 * <p>This is meant to be attached to the edit button panel for the corresponding element.
 */
public class CmsAvailabilityBadge extends Composite {

    /** The ui-binder for this widget. */
    interface I_UiBinder extends UiBinder<Widget, CmsAvailabilityBadge> {}

    public static final String CLASS_HIDE_AVAILABILITY_BADGE = "oc-hide-availability-badge";

    /** The ui binder instance. */
    private static I_UiBinder m_uiBinder = GWT.create(I_UiBinder.class);

    /** Label to show release information. */
    @UiField
    protected Label m_releaseLabel;

    /** Label to show expiration information. */
    @UiField
    protected Label m_expireLabel;

    public CmsAvailabilityBadge(CmsAvailabilityInfo info) {

        initWidget(m_uiBinder.createAndBindUi(this));
        if (info.getReleasedText() != null) {
            m_releaseLabel.setText(info.getReleasedText());
        } else {
            m_releaseLabel.removeFromParent();
        }
        if (info.getExpiredText() != null) {
            m_expireLabel.setText(info.getExpiredText());
        } else {
            m_expireLabel.removeFromParent();
        }
    }

    /**
     * Creates an availability badge for a CmsAvailabilityInfo object if necessary, i.e. if the info object contains at least one message.
     *
     * @param availabilityInfo the availability info
     * @return the availability badge, or null if no badge is needed
     */
    public static CmsAvailabilityBadge create(CmsAvailabilityInfo availabilityInfo) {

        if (availabilityInfo == null) {
            return null;
        }
        if ((availabilityInfo.getExpiredText() == null) && (availabilityInfo.getReleasedText() == null)) {
            return null;
        }
        return new CmsAvailabilityBadge(availabilityInfo);
    }

}
