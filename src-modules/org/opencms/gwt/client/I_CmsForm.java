/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/Attic/I_CmsForm.java,v $
 * Date   : $Date: 2010/03/01 08:57:33 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import java.util.List;

/**
 * Defines a ui form object.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public interface I_CmsForm {

    /**
     * Adds a field to the form.<p>
     * 
     * @param field the field to add
     */
    void addField(I_CmsFormField field);

    /**
     * Disables the whole form.<p>
     */
    void disable();

    /** 
     * Returns all form fields.<p>
     * 
     * @return the form fields
     */
    List<I_CmsFormField> getFields();

    /**
     * Removes a given field from the form.<p>
     * 
     * @param field the field to remove
     */
    void removeField(I_CmsFormField field);

    /**
     * Submits the form.<p>
     */
    void submit();

    /**
     * Validates all fields of the form.<p>
     * 
     * @return <code>true</code> if valid
     */
    boolean validate();

}
