/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/form/Attic/A_CmsFormFieldPanel.java,v $
 * Date   : $Date: 2011/02/14 10:02:24 $
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

package org.opencms.gwt.client.ui.input.form;

import org.opencms.gwt.client.ui.input.I_CmsFormField;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * The abstract class for form field container widgets.<p>
 * 
 *  @author Georg Westenberger
 *  
 *  @version $Revision: 1.1 $
 *  
 *  @since 8.0.0
 */
public abstract class A_CmsFormFieldPanel extends Composite {

    /**
     * Adds a field to the given field group.<p>
     * 
     * @param field the field to add 
     * @param fieldGroup the field group id 
     */
    public abstract void addField(I_CmsFormField field, String fieldGroup);

    /**
     * Adds a new row with a given label and input widget to the form.<p>
     * 
     * @param labelText the label text for the form field
     * @param description the description of the form field 
     * @param widget the widget for the form field 
     *  
     * @return the newly added form row 
     */
    protected CmsFormRow createRow(String labelText, String description, Widget widget) {

        CmsFormRow row = new CmsFormRow();
        Label label = row.getLabel();
        label.setText(labelText);
        label.setTitle(description);
        row.getWidgetContainer().add(widget);
        //getPanel(panelId).add(row);
        return row;
    }
}
