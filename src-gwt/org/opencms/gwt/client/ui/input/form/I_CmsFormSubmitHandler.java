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

package org.opencms.gwt.client.ui.input.form;

import java.util.Map;
import java.util.Set;

/**
 * This is an interface for classes which process submitted form data.<p>
 */
public interface I_CmsFormSubmitHandler {

    /**
     * The method which should be called when a {@link CmsForm} is submitted.<p>
     *
     * The map passed as a parameter will contain key-value pairs where the key is the
     * name of the field and the value is the value obtained from the field. It is explicitly
     * allowed that the value is null; this means that the property is set to 'default'.
     *
     * @param form the form
     * @param fieldValues a map of field values
     * @param editedFields the fields which have been edited
     */
    void onSubmitForm(CmsForm form, Map<String, String> fieldValues, Set<String> editedFields);

}
