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

package org.opencms.gwt.client;

import org.opencms.gwt.client.util.CmsMediaQuery;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Singleton class that evaluates, and keeps track of changes for, a fixed set of media queries and sets CSS classes
 * on the body element depending on which of the media queries match.
 */
public class CmsMediaQueryRuleManager {

    /** The instance. */
    protected static CmsMediaQueryRuleManager instance;

    /**
     * Initializes the rules.
     */
    protected CmsMediaQueryRuleManager() {

        addRule("oc-touch-only", CmsCoreProvider.TOUCH_ONLY_RULE);
        addRule("oc-screensize-small", "(max-width: " + CmsWidthConstants.smallHigh() + ")");
        addRule(
            "oc-screensize-medium",
            "(min-width: "
                + CmsWidthConstants.mediumLow()
                + ") and (max-width: "
                + CmsWidthConstants.mediumHigh()
                + ")");
        addRule("oc-screensize-large", "(min-width: " + CmsWidthConstants.largeLow() + ")");
    }

    /**
     * Gets the instance.
     *
     * @return the instance
     */
    public static CmsMediaQueryRuleManager get() {

        if (instance == null) {
            instance = new CmsMediaQueryRuleManager();
        }
        return instance;
    }

    /** Initializes the manager and sets up the rules. */
    public static void initialize() {

        CmsMediaQueryRuleManager.get();

    }

    /**
     * Installs a new media query rule.
     *
     * @param cssClass the CSS class to add if the media query matches
     * @param mediaQueryText the text of the media query
     */
    public void addRule(String cssClass, String mediaQueryText) {

        CmsMediaQuery mediaQuery = CmsMediaQuery.parse(mediaQueryText);
        updateBodyClass(cssClass, mediaQuery.matches());
        mediaQuery.addListener(match -> {
            updateBodyClass(cssClass, match.booleanValue());
        });
    }

    /**
     * Adds or removes a CSS class from the body.
     *
     * @param cssClass the CSS class
     * @param enabled true if the class should be added, false to remove it
     */
    public void updateBodyClass(String cssClass, boolean enabled) {

        Element body = RootPanel.getBodyElement();
        if (enabled) {
            body.addClassName(cssClass);
        } else {
            body.removeClassName(cssClass);
        }
    }

}
