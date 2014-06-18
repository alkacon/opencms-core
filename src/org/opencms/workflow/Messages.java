/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workflow;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * The message bundle for the workflow package.<p>
 * 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message key for the resource bundle. */
    public static final String ERR_INVALID_WORKFLOW_ACTION_1 = "ERR_INVALID_WORKFLOW_ACTION_1";

    /** Message key for the resource bundle. */
    public static final String ERR_NEW_PARENT_NOT_IN_WORKFLOW_1 = "ERR_NEW_PARENT_NOT_IN_WORKFLOW_1";

    /** Message key for the resource bundle. */
    public static final String GUI_ALREADY_IN_WORKFLOW_0 = "GUI_ALREADY_IN_WORKFLOW_0";

    /** Message key for the resource bundle. */
    public static final String GUI_BROKEN_LINKS_0 = "GUI_BROKEN_LINKS_0";

    /** Message key for the resource bundle. */
    public static final String GUI_MAIL_PUBLISH_LINK_1 = "GUI_MAIL_PUBLISH_LINK_1";

    /** Message key for the resource bundle. */
    public static final String GUI_MAIL_USER_LINE_1 = "GUI_MAIL_USER_LINE_1";

    /** Message key for the resource bundle. */
    public static final String GUI_WORKFLOW_ACTION_FORCE_PUBLISH_0 = "GUI_WORKFLOW_ACTION_FORCE_PUBLISH_0";

    /** Message key for the resource bundle. */
    public static final String GUI_WORKFLOW_ACTION_PUBLISH_0 = "GUI_WORKFLOW_ACTION_PUBLISH_0";

    /** Message key for the resource bundle. */
    public static final String GUI_WORKFLOW_ACTION_RELEASE_0 = "GUI_WORKFLOW_ACTION_RELEASE_0";

    /** Message key for the resource bundle. */
    public static final String GUI_WORKFLOW_PROJECT_DESCRIPTION_2 = "GUI_WORKFLOW_PROJECT_DESCRIPTION_2";

    /** Message key for the resource bundle. */
    public static final String GUI_WORKFLOW_PROJECT_NAME_2 = "GUI_WORKFLOW_PROJECT_NAME_2";

    /** Message key for the resource bundle. */
    public static final String GUI_WORKFLOW_PUBLISH_0 = "GUI_WORKFLOW_PUBLISH_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workflow.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private Messages() {

        // hide the constructor
    }

    /**
     * Returns an instance of this localized message accessor.<p>
     * 
     * @return an instance of this localized message accessor
     */
    public static I_CmsMessageBundle get() {

        return INSTANCE;
    }

    /**
     * Returns the bundle name for this OpenCms package.<p>
     * 
     * @return the bundle name for this OpenCms package
     */
    public String getBundleName() {

        return BUNDLE_NAME;
    }
}
