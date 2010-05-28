/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsLabelSelectCell.java,v $
 * Date   : $Date: 2010/05/28 08:22:28 $
 * Version: $Revision: 1.2 $
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

import org.opencms.gwt.client.ui.I_CmsTruncable;

/**
 * This class represents a single select option in the selector of the select box.
 */
public class CmsLabelSelectCell extends A_CmsSelectCell implements I_CmsTruncable {

    /** The value of the select option. */
    protected String m_value;

    /** The label of which this select cell consists. */
    private CmsLabel m_label = new CmsLabel();

    /** The text of the select option. */
    private String m_text;

    /**
     * Creates a new select cell.<p>
     * 
     * @param value the value of the select option
     * @param text the text to display for the select option
     */
    public CmsLabelSelectCell(String value, String text) {

        super();
        m_value = value;
        m_text = text;
        initWidget(m_label);
        m_label.setText(m_text);
    }

    /**
     * Returns the text as which the select option should be displayed to the user.<p>
     * 
     * @return the text of the select option
     */
    public String getText() {

        return m_text;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectCell#getValue()
     */
    @Override
    public String getValue() {

        return m_value;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int labelWidth) {

        m_label.truncate(textMetricsKey, labelWidth);
    }

}
