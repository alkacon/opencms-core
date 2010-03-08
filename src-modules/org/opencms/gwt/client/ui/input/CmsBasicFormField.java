/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsBasicFormField.java,v $
 * Date   : $Date: 2010/03/08 16:47:06 $
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

package org.opencms.gwt.client.ui.input;

/**
 * Basic implementation of the I_CmsFormField class.<p>
 */
public class CmsBasicFormField implements I_CmsFormField {

    private String m_description;

    private String m_id;
    private String m_label;
    private I_CmsValidator m_validator;
    private I_CmsFormWidget m_widget;

    /**
     * Constructs a new form field.<p>
     * 
     * @param id the id of the form field
     * @param description the description of the form field
     * @param label the label of the form field
     * @param widget the widget of the form field
     */
    public CmsBasicFormField(String id, String description, String label, I_CmsFormWidget widget) {

        super();
        m_id = id;
        m_description = description;
        m_label = label;
        m_widget = widget;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#getDescription()
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#getId()
     */
    public String getId() {

        return m_id;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#getLabel()
     */
    public String getLabel() {

        return m_label;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#getWidget()
     */
    public I_CmsFormWidget getWidget() {

        return m_widget;
    }

    /**
     * 
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#setId(java.lang.String)
     */
    public void setId(String id) {

        m_id = id;
    }

    /**
     * 
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#setValidator(org.opencms.gwt.client.ui.input.I_CmsValidator)
     */
    public void setValidator(I_CmsValidator validator) {

        m_validator = validator;
    }

    /**
     * 
     * @see org.opencms.gwt.client.ui.input.I_CmsFormField#validate(org.opencms.gwt.client.ui.input.I_CmsValidationHandler)
     */
    public void validate(I_CmsValidationHandler handler) {

        if (m_validator != null) {
            m_validator.validate(getWidget(), handler);
        }
    }

}
