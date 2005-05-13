/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/xmlwidgets/Attic/CmsWidgetParameter.java,v $
 * Date   : $Date: 2005/05/13 09:04:18 $
 * Version: $Revision: 1.3 $
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

package org.opencms.workplace.xmlwidgets;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.CmsWidgetDialog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

import sun.security.action.GetLongAction;

/**
 * Implements the widget parameter interface for the use of OpenCms widgets on dialogs that
 * are not based on XML contents.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 * @since 5.9.1
 */
public class CmsWidgetParameter implements I_CmsWidgetParameter {

    /** The name of the default dialog page. */
    public static final String DEFAULT_DIALOG_PAGE = "default";

    /** The maximum number of occurences of a widget dialog element in a list of elements. */
    public static final int MAX_OCCURENCES = 50;
    
    /** The (optional) base object for read / writing the parameter value to. */
    protected Object m_baseObject;

    /** The (optinal) object property to read / write this parameter value to. */
    protected String m_baseObjectProperty;

    /** The default value of the parameter. */
    protected String m_defaultValue;

    /** The name of the dialog (page) the widget is used on. */
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
     * @param index the index of this parameter in the list 
     */
    public CmsWidgetParameter(CmsWidgetParameter base, int index) {

        this(
            null,
            base.m_defaultValue,
            base.getName(),
            base.getWidget(),
            base.getDialog(),
            base.getMinOccurs(),
            base.getMaxOccurs(),
            index);

        m_baseObject = base.m_baseObject;
        m_baseObjectProperty = base.m_baseObjectProperty;
    }
    
    /**
     * Create a new Widget parameter.<p>
     * 
     * @param base the base of the parameter
     * @param index the index of this parameter in the list
     * @param originalIndex the original index in the previous version of the list
     */
    public CmsWidgetParameter(CmsWidgetParameter base, int index, int originalIndex) {

        this(
            null,
            base.m_defaultValue,
            base.getName(),
            base.getWidget(),
            base.getDialog(),
            base.getMinOccurs(),
            base.getMaxOccurs(),
            index);

        m_baseObject = base.m_baseObject;
        m_baseObjectProperty = base.m_baseObjectProperty;
        
        if (m_baseObject instanceof List) {
            // base object is a list - make sure to set possible old value 
            List baseList = (List)m_baseObject;
            if (originalIndex < baseList.size()) {
                Object o = baseList.get(originalIndex);
                if (o != null) {
                    m_value = o.toString();
                }
            }
        } else if (m_baseObject instanceof SortedMap) {
            // base object is a sorted map - make sure to set possible old value 
            SortedMap baseMap = (SortedMap)m_baseObject;
            List keyList = new ArrayList(baseMap.keySet());
            if (originalIndex < keyList.size()) {
                Object key = keyList.get(originalIndex);
                Object value = baseMap.get(key);
                StringBuffer val = new StringBuffer();
                val.append(key != null ? key.toString() : "");
                val.append('=');
                val.append(value != null ? value.toString() : "");
                m_value = val.toString();
            }
        }
    }
    
    /**
     * Checks if a value for this widget base type with the given id is available.<p> 
     * 
     * @param index the index to check
     * 
     * @return <code>true</code> if a value for this widget base type with the given id is available
     */
    public boolean hasValue(int index) {

        if (m_baseObject instanceof List) {
            return index < ((List)m_baseObject).size();
        } else if (m_baseObject instanceof SortedMap) {
            return index < ((SortedMap)m_baseObject).size();
        }
        return false;
    }
    
    /**
     * Create a new Widget parameter based on a given object's property.<p>
     * 
     * @param base the base object to map the parameter to / from
     * @param property the base object property to map the parameter to / from
     * @param dialogPage the dialog page to use the widget on
     * @param widget the widget used for this parameter
     */
    public CmsWidgetParameter(Object base, String property, String dialogPage, I_CmsXmlWidget widget) {

        this(base, property, dialogPage, widget, 1, 1);
    }
    
    /**
     * Create a new Widget parameter based on a given object's property.<p>
     * 
     * @param base the base object to map the parameter to / from
     * @param property the base object property to map the parameter to / from
     * @param dialogPage the dialog page to use the widget on
     * @param widget the widget used for this parameter
     * @param minOccurs the required minimum numer of occurences of this parameter
     * @param maxOccurs the maximum allowed numer of occurences of this parameter
     */
    public CmsWidgetParameter(Object base, String property, String dialogPage, I_CmsXmlWidget widget, int minOccurs, int maxOccurs) {

        if ((base instanceof List) || (base instanceof SortedMap)) {
            
            // this is a list, use custom list mappings
            init(null, null, property, widget, dialogPage, 0, MAX_OCCURENCES, 0);

            m_baseObject = base;
            m_baseObjectProperty = null;
            
        } else {
             
            // generic object:use reflection to map object properties
            init(null, null, property, widget, dialogPage, 1, 1, 0);
    
            m_baseObject = base;
            m_baseObjectProperty = property;
    
            PropertyUtilsBean bean = new PropertyUtilsBean();
            // make sure the base object has the requested property
            if (!bean.isReadable(m_baseObject, m_baseObjectProperty)
                || !bean.isWriteable(m_baseObject, m_baseObjectProperty)) {
                throw new CmsIllegalArgumentException(Messages.get().container(
                    Messages.ERR_NO_PROPERTY_2,
                    base.getClass().getName(),
                    property));
            }
    
            Object value;
            try {
                value = bean.getNestedProperty(m_baseObject, m_baseObjectProperty);
            } catch (Exception e) {
                throw new CmsRuntimeException(Messages.get().container(
                    Messages.ERR_PROPERTY_READ_2,
                    property,
                    base.getClass().getName()), e);
            }
    
            if (value != null) {
                m_defaultValue = String.valueOf(value);
                setStringValue(null, m_defaultValue);
            }
        }
        m_minOccurs = minOccurs;
        m_maxOccurs = maxOccurs;
    }

    /**
     * Create a new Widget parameter based on a given object's property.<p>
     * 
     * @param base the base object to map the parameter to / from
     * @param property the base object property to map the parameter to / from
     * @param widget the widget used for this parameter
     */
    public CmsWidgetParameter(Object base, String property, I_CmsXmlWidget widget) {

        this(base, property, DEFAULT_DIALOG_PAGE, widget);
    }

    /**
     * Create a new Widget parameter.<p>
     * 
     * @param name the name of the parameter
     * @param widget the widget used for this parameter
     */
    public CmsWidgetParameter(String name, I_CmsXmlWidget widget) {

        this(null, null, name, widget, DEFAULT_DIALOG_PAGE, 1, 1, 0);
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

        this(null, null, name, widget, DEFAULT_DIALOG_PAGE, minOccurs, maxOccurs, 0);
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
        init(value, defaultValue, name, widget, dialog, minOccurs, maxOccurs, index);
    }
    
    /**
     * Initializes a widget parameter with the given values.<p>
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
    protected void init(
        String value,
        String defaultValue,
        String name,
        I_CmsXmlWidget widget,
        String dialog,
        int minOccurs,
        int maxOccurs,
        int index) {
        
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
     * "Commits" (writes) the value of this widget back to the underlying base object.<p> 
     * 
     * @param dialog the widget dialog where the parameter is used on
     * 
     * @throws CmsException in case the String value of the widget is invalid for the base Object
     */
    public void commitValue(CmsWidgetDialog dialog) throws CmsException {

        if ((m_baseObject != null) && (m_baseObjectProperty != null)) {

            PropertyUtilsBean bean = new PropertyUtilsBean();
            ConvertUtilsBean conveter = new ConvertUtilsBean();
            Object value = null;
            try {
                Class type = bean.getPropertyType(m_baseObject, m_baseObjectProperty);
                value = conveter.convert(m_value, type);
                bean.setNestedProperty(m_baseObject, m_baseObjectProperty, value);
            } catch (InvocationTargetException e) {
                throw new CmsWidgetException(Messages.get().container(
                    Messages.ERR_PROPERTY_WRITE_3,
                    value,
                    dialog.key(A_CmsXmlWidget.getLabelKey(this), getKey()),
                    m_baseObject.getClass().getName()), e.getTargetException(), this);
            } catch (Exception e) {
                throw new CmsWidgetException(Messages.get().container(
                    Messages.ERR_PROPERTY_WRITE_3,
                    value,
                    dialog.key(A_CmsXmlWidget.getLabelKey(this), getKey()),
                    m_baseObject.getClass().getName()), e, this);
            }
        }
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
