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

package org.opencms.ui.components;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;

/**
 * Shortcut handler triggered on 'Enter' and 'Esc' to trigger OK and Cancel actions.<p>
 */
public abstract class CmsOkCancelActionHandler implements Handler {

    /** The serial version id. */
    private static final long serialVersionUID = 6114433920380720290L;

    /** The enter action. */
    protected static final ShortcutAction ENTER_ACTION = new ShortcutAction(
        "Enter",
        ShortcutAction.KeyCode.ENTER,
        null);

    /** The escape action. */
    protected static final ShortcutAction ESC_ACTION = new ShortcutAction(
        "Escape",
        ShortcutAction.KeyCode.ESCAPE,
        null);

    /** The shortcut actions. */
    private static final ShortcutAction[] SHORTCUT_ACTIONS = new ShortcutAction[] {ENTER_ACTION, ESC_ACTION};

    /**
     * @see com.vaadin.event.Action.Handler#getActions(java.lang.Object, java.lang.Object)
     */
    public Action[] getActions(Object target, Object sender) {

        return SHORTCUT_ACTIONS;
    }

    /**
     * @see com.vaadin.event.Action.Handler#handleAction(com.vaadin.event.Action, java.lang.Object, java.lang.Object)
     */
    public void handleAction(Action action, Object sender, Object target) {

        if (ENTER_ACTION.equals(action)) {
            ok();
        } else if (ESC_ACTION.equals(action)) {
            cancel();
        }
    }

    /**
     * Called on key press 'Esc'.<p>
     */
    protected abstract void cancel();

    /**
     * Called on key press 'Enter'.<p>
     */
    protected abstract void ok();

}
