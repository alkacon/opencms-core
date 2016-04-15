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

package org.opencms.ui.components;

import com.vaadin.ui.ProgressBar;

/**
 * Security level indicator, used to indicate the password security level.<p>
 */
public class CmsSecurityIndicator extends ProgressBar {

    /** The serial version id. */
    private static final long serialVersionUID = 704354009186080564L;

    /** The current security level style name. */
    private String m_currentLevel;

    /**
     * Constructor.<p>
     */
    public CmsSecurityIndicator() {
        super();
        addStyleName(OpenCmsTheme.SEC_INDICATOR);
    }

    /**
     * @see com.vaadin.ui.ProgressBar#setValue(java.lang.Float)
     */
    @Override
    public void setValue(Float newValue) {

        if (newValue.floatValue() > 1) {
            newValue = Float.valueOf(1f);
        }
        if (newValue.floatValue() < 0) {
            newValue = Float.valueOf(0);
        }
        super.setValue(newValue);

        String levelClass = getSecurityStyleName(newValue.floatValue());
        if (!levelClass.equals(m_currentLevel)) {
            if (m_currentLevel != null) {
                removeStyleName(m_currentLevel);
            }
            addStyleName(levelClass);
            m_currentLevel = levelClass;
        }
    }

    /**
     * Returns the security level style name for the given level.<p>
     *
     * @param level the security level
     *
     * @return the style name
     */
    private String getSecurityStyleName(float level) {

        if (level == 1) {
            level = 0.99f;
        }
        return OpenCmsTheme.SEC_LEVEL_PREFIX + (((int)(level * 10)) % 10);
    }
}
