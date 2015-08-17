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

package org.opencms.ade.postupload.client.ui;

import org.opencms.gwt.client.property.CmsSimplePropertyEditor;
import org.opencms.gwt.client.property.I_CmsPropertyEditorHandler;
import org.opencms.gwt.client.ui.input.form.CmsInfoBoxFormFieldPanel;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Map;

/**
 * A property editor for the upload property dialog.<p>
 */
public class CmsUploadPropertyEditor extends CmsSimplePropertyEditor {

    /**
     * Creates a new instance.<p>
     *
     * @param propConfig the property configuration
     * @param handler the property editor handler to use
     */
    public CmsUploadPropertyEditor(Map<String, CmsXmlContentProperty> propConfig, I_CmsPropertyEditorHandler handler) {

        super(propConfig, handler);
    }

    /**
     * @see org.opencms.gwt.client.property.CmsSimplePropertyEditor#setupFieldContainer()
     */
    @Override
    protected void setupFieldContainer() {

        CmsInfoBoxFormFieldPanel panel = new CmsInfoBoxFormFieldPanel(m_handler.getPageInfo());
        m_form.setWidget(panel);
    }

}
