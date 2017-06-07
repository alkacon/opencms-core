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

package org.opencms.gwt.client.ui.resourcehelp;

import org.opencms.gwt.client.Messages;

/**
 * Message accessor class for the 'Undo changes' dialog.<p>
 */
public final class CmsResourceHelpMessages {

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageClose() {

        return Messages.get().key(Messages.GUI_CLOSE_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageNext() {

        return Messages.get().key(Messages.GUI_NEXT_HELP_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageOf() {

        return Messages.get().key(Messages.GUI_OF_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messagePrev() {

        return Messages.get().key(Messages.GUI_PREV_HELP_0);
    }
    
    /**
     * Message accessor.<p>
     *
     * @return the message text
     */
    public static String messageDoNotShow() {

        return Messages.get().key(Messages.GUI_DO_NOT_SHOW_HELP_0);
    }

    /**
     * Hide default constructor.<p>
     */
    private CmsResourceHelpMessages() {

        // do nothing
    }

}
