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

package org.opencms.gwt.client.property.definition;

import org.opencms.gwt.client.Messages;

/**
 * Messages for the property definition dialog.<p>
 */
public class CmsPropertyDefinitionMessages {

    /**
     * Hidden default constructor.<p>
     */
    protected CmsPropertyDefinitionMessages() {

    }

    /**
     * Message accessor.<p>
     *
     * @return the message
     */
    public static String alreadyExists() {

        return Messages.get().key(Messages.ERR_PROPERTY_EXISTS_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message
     */
    public static String cancel() {

        return Messages.get().key(Messages.GUI_CANCEL_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message
     */
    public static String labelExistingProperties() {

        return Messages.get().key(Messages.GUI_LABEL_EXISTING_PROPERTIES_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message
     */
    public static String labelNewProperty() {

        return Messages.get().key(Messages.GUI_LABEL_NEW_PROPERTY_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message
     */
    public static String messageDialogCaption() {

        return Messages.get().key(Messages.GUI_CAPTION_DEFINE_PROPERTY_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message
     */
    public static final String messageEmpty() {

        return Messages.get().key(Messages.ERR_EMPTY_PROPERTY_NAME_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message
     */
    public static String messageInvalidName() {

        return Messages.get().key(Messages.ERR_INVALID_PROPERTY_NAME_0);

        // return "Valid characters are only letters, digits and \"-._~$\".";
    }

    /**
     * Message accessor.<p>
     *
     * @return the message
     */
    public static String ok() {

        return Messages.get().key(Messages.GUI_OK_0);

    }

}
