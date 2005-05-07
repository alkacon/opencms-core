/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/CmsWidgetParameter.java,v $
 * Date   : $Date: 2005/05/07 16:08:28 $
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

package org.opencms.workplace;

import org.opencms.file.CmsObject;
import org.opencms.workplace.xmlwidgets.I_CmsXmlWidget;
import org.opencms.workplace.xmlwidgets.I_CmsWidgetParameter;

/**
 * Implements the widget parameter interface for the use of OpenCms widgets on dialogs that
 * are not based on XML contents.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.9.1
 */
public class CmsWidgetParameter implements I_CmsWidgetParameter {

    /** The name of the default dialog. */
    public static final String DEFAULT_DIALOG = "default";

    /** The default value of the parameter. */
    protected String m_defaultValue;

    /** The name of the dialog the widget is used on. */
    protected String m_dialog;

    /** The id of the parameter on the form. */
    protected String m_id;

    /** The index of this parameter in the (optional) list of parameters. */
    protected int m_index;

    /** The maximum number of occurences of this parameter. */
    protected int m_maxOccurs;

    /** The minimum number of occurences of this parameter. */
    protected int m_minOccurs;

    /** The name of the parameter. */
    protected String m_name;

    /** The value of the parameter. */
    protected String m_value;

    /** The widget used for the parameter. */
    protected I_CmsXmlWidget m_widget;

    /**
     * Create a new Widget parameter.<p>
     * 
     * @param base the base of the parameter
     * @param value the initial value of the parameter
     * @param index the index of this parameter in the list 
     */
    public CmsWidgetParameter(CmsWidgetParameter base, String value, int index) {

        this(
            value,
            null,
            base.getName(),
            base.getWidget(),
            base.getDialog(),
            base.getMinOccurs(),
            base.getMaxOccurs(),
            index);
    }

    /**
     * Create a new Widget parameter.<p>
     * 
     * @param name the name of the parameter
     * @param widget the widget used for this parameter
     */
    public CmsWidgetParameter(String name, I_CmsXmlWidget widget) {

        this(null, null, name, widget, DEFAULT_DIALOG, 1, 1, 0);
    }

    /**
     * Create a new Widget parameter.<p>
     * 
     * @param name the name of the parameter
     * @param widget the widget used for this parameter
     * @param minOccurs the required minimum numer of occurences of this parameter
     * @param maxOccurs the maximum allowed numer of occurences of this parameter
     */
    public CmsWidgetParameter(String name, I_CmsXmlWidget widget, int minOccurs, int maxOccurs) {

        this(null, null, name, widget, DEFAULT_DIALOG, minOccurs, maxOccurs, 0);
    }

    /**
     * Create a new Widget parameter with specified occurence settings.<p>
     * 
     * @param value the initial value of the parameter
     * @param defaultValue the default value of the parameter
     * @param name the id of the parameter
     * @param widget the widget used for this parameter
     * @param dialog the dialog this parameter is used on
     * @param minOccurs the required minimum numer of occurences of this parameter
     * @param maxOccurs the maximum allowed numer of occurences of this parameter
     * @param index the index of this parameter in the list 
     */
    public CmsWidgetParameter(
        String value,
        String defaultValue,
        String name,
        I_CmsXmlWidget widget,
        String dialog,
        int minOccurs,
        int maxOccurs,
        int index) {

        super();
        if (defaultValue == null) {
            m_defaultValue = "";
        } else {
            m_defaultValue = defaultValue;
        }
        if (value == null) {
            m_value = m_defaultValue;
        } else {
            m_value = value;
        }
        m_name = name;
        m_widget = widget;
        m_minOccurs = minOccurs;
        m_maxOccurs = maxOccurs;
        m_index = index;
        m_dialog = dialog;

        // create the id
        m_id = createId(m_name, m_index);
    }

    /**
     * Returns a from id representation for the given widget name and id.<p> 
     * 
     * @param name the widget parameter name
     * @param index the widget parameter index
     * 
     * @return a from id representation for the given widget name and id
     */
    public static String createId(String name, int index) {

        StringBuffer result = new StringBuffer();
        result.append(name);
        result.append('.');
        result.append(index);

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsWidgetParameter#getDefault(org.opencms.file.CmsObject)
     */
    public String getDefault(CmsObject cms) {

        return m_defaultValue;
    }

    /**
     * Returns the name of the dialog (or dialog page) this widget parameter is used on.<p>
     * 
     * This information can be used to create multi-page dialogs where the 
     * widgets are spread over several pages.<p>
     * 
     * @return the name of the dialog (or dialog page) this widget parameter is used on
     */
    public String getDialog() {

        return m_dialog;
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsWidgetParameter#getId()
     */
    public String getId() {

        return m_id;
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsWidgetParameter#getIndex()
     */
    public int getIndex() {

        return m_index;
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsWidgetParameter#getKey()
     */
    public String getKey() {

        return getName();
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsWidgetParameter#getMaxOccurs()
     */
    public int getMaxOccurs() {

        return m_maxOccurs;
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsWidgetParameter#getMinOccurs()
     */
    public int getMinOccurs() {

        return m_minOccurs;
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsWidgetParameter#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsWidgetParameter#getStringValue(org.opencms.file.CmsObject)
     */
    public String getStringValue(CmsObject cms) {

        return m_value;
    }

    /**
     * Returns the widget for this parameter.<p>
     * 
     * @return the widget for this parameter
     */
    public I_CmsXmlWidget getWidget() {

        return m_widget;
    }

    /**
     * Sets the index to the provided value.<p>
     * 
     * @param index the new index value to set
     */
    public void setindex(int index) {

        m_index = index;
        m_id = createId(m_name, m_index);
    }

    /**
     * @see org.opencms.workplace.xmlwidgets.I_CmsWidgetParameter#setStringValue(org.opencms.file.CmsObject, java.lang.String)
     */
    public void setStringValue(CmsObject cms, String value) {

        m_value = value;
    }
}
