/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workflow/Attic/Messages.java,v $
 * Date   : $Date: 2005/05/09 15:15:31 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsRequestContext;
import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;
import org.opencms.main.I_CmsConstants;

import java.util.Locale;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @author Achim Westermann (a.westermann@alkacon.com)
 * @since 5.7.3
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_TYPE_ALL_0 = "GUI_TASK_TYPE_ALL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_TYPE_DONE_0 = "GUI_TASK_TYPE_DONE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_TYPE_NEW_0 = "GUI_TASK_TYPE_NEW_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_TYPE_OPEN_0 = "GUI_TASK_TYPE_OPEN_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_TYPE_ACTIVE_0 = "GUI_TASK_TYPE_ACTIVE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_TASK_TYPE_ILLEGAL_0 = "GUI_TASK_TYPE_ILLEGAL_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workflow.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /**
     * Returns an instance of this localized message accessor.<p>
     * 
     * @return an instance of this localized message accessor
     */
    public static I_CmsMessageBundle get() {

        return INSTANCE;
    }

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private Messages() {

        // hide the constructor
    }

    /**
     * Returns the bundle name for this OpenCms package.<p>
     * 
     * @return the bundle name for this OpenCms package
     */
    public String getBundleName() {

        return BUNDLE_NAME;
    }

    /**
     * Small utility-method that allows output of task states in default locale. <p>
     * 
     * @param taskType One of 
     * <ul>
     *  <li>
     *   {@link I_CmsConstants#C_TASKS_ACTIVE}
     *  <li>
     *   {@link I_CmsConstants#C_TASKS_ALL}
     *  <li>
     *   {@link I_CmsConstants#C_TASKS_DONE}
     *  <li>
     *   {@link I_CmsConstants#C_TASKS_NEW}
     *  <li> 
     *   {@link I_CmsConstants#C_TASKS_OPEN}
     * </ul>
     * @return A string describing the state of the task in the default locale.
     * @see I_CmsConstants 
     * @see #toTaskTypeString(int, CmsRequestContext)
     */
    public static String toTaskTypeString(int taskType) {

        return toTaskTypeString(taskType, Locale.getDefault());
    }

    /**
     * Small utility-method that allows output of task states in the locale of the current user request. <p>
     * 
     * @param taskType One of 
     * <ul>
     *  <li>
     *   {@link I_CmsConstants#C_TASKS_ACTIVE}
     *  <li>
     *   {@link I_CmsConstants#C_TASKS_ALL}
     *  <li>
     *   {@link I_CmsConstants#C_TASKS_DONE}
     *  <li>
     *   {@link I_CmsConstants#C_TASKS_NEW}
     *  <li> 
     *   {@link I_CmsConstants#C_TASKS_OPEN}
     * </ul>
     * 
     * @param context the current user's request context. 
     * @return A string describing the state of the task in the default locale.
     * @see I_CmsConstants 
     * @see #toTaskTypeString(int)
     */

    public static String toTaskTypeString(int taskType, CmsRequestContext context) {

        return toTaskTypeString(taskType, context.getLocale());
    }

    /**
     * Small utility-method that allows output of task states in the given <code>Locale</code>. <p>
     * 
     * @param taskType One of 
     * <ul>
     *  <li>
     *   {@link I_CmsConstants#C_TASKS_ACTIVE}
     *  <li>
     *   {@link I_CmsConstants#C_TASKS_ALL}
     *  <li>
     *   {@link I_CmsConstants#C_TASKS_DONE}
     *  <li>
     *   {@link I_CmsConstants#C_TASKS_NEW}
     *  <li> 
     *   {@link I_CmsConstants#C_TASKS_OPEN}
     * </ul>
     * @param locale the locale in which the task state should be given back. 
     * @return A string describing the state of the task in the default locale.
     * @see I_CmsConstants 
     * @see #toTaskTypeString(int)
     */
    public static String toTaskTypeString(int taskType, Locale locale) {

        Object[] dummy = new Object[0];
        switch (taskType) {
            case I_CmsConstants.C_TASKS_ACTIVE:
                return Messages.get().key(locale, Messages.GUI_TASK_TYPE_ACTIVE_0, dummy); 
            case I_CmsConstants.C_TASKS_ALL:
                return Messages.get().key(locale, Messages.GUI_TASK_TYPE_ACTIVE_0, dummy); 
            case I_CmsConstants.C_TASKS_DONE:
                return Messages.get().key(locale, Messages.GUI_TASK_TYPE_ACTIVE_0, dummy); 
            case I_CmsConstants.C_TASKS_NEW:
                return Messages.get().key(locale, Messages.GUI_TASK_TYPE_ACTIVE_0, dummy); 
            case I_CmsConstants.C_TASKS_OPEN:
            default:
                return Messages.get().key(locale, Messages.GUI_TASK_TYPE_ILLEGAL_0, dummy); 
        }
    }

}