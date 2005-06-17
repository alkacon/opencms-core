/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWidgetDialogParameter.java,v $
 * Date   : $Date: 2005/06/17 12:54:55 $
 * Version: $Revision: 1.7 $
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
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.A_CmsWidget;
import org.opencms.widgets.CmsWidgetException;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetParameter;
import org.opencms.widgets.Messages;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

/**
 * Implements the widget parameter interface for the use of OpenCms widgets on dialogs that
 * are not based on XML contents.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 * @since 5.9.1
 */
public class CmsWidgetDialogParameter implements I_CmsWidgetParameter {

    /** The name of the default dialog page. */
    public static final String DEFAULT_DIALOG_PAGE = "default";

    /** The maximum number of occurences of a widget dialog element in a list of elements. */
    public static final int MAX_OCCURENCES = 200;

    /** The (optional) base collection for read / writing collection based parameters. */
    protected Object m_baseCollection;

    /** The (optional) base object for read / writing the parameter value to. */
    protected Object m_baseObject;

    /** The (optinal) object property to read / write this parameter value to. */
    protected String m_baseObjectProperty;

    /** The default value of the parameter. */
    protected String m_defaultValue;

    /** The name of the dialog (page) the widget is used on. */
    protected String m_dialogPage;

    /** Indicates if the widget value has an error. */
    protected Throwable m_error;

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

    /** Optional localized key prefix identificator. */
    protected String m_prefix;

    /** The value of the parameter. */
    protected String m_value;

    /** The widget used for the parameter. */
    protected I_CmsWidget m_widget;

    /**
     * Create a new Widget parameter.<p>
     * 
     * @param base the base of the parameter
     * @param index the index of this parameter in the list 
     */
    public CmsWidgetDialogParameter(CmsWidgetDialogParameter base, int index) {

        this(
            null,
            base.m_defaultValue,
            base.getName(),
            base.getWidget(),
            base.getDialogPage(),
            base.getMinOccurs(),
            base.getMaxOccurs(),
            index);

        m_baseObject = base.m_baseObject;
        m_baseObjectProperty = base.m_baseObjectProperty;
        m_baseCollection = base.m_baseCollection;
    }

    /**
     * Create a new Widget parameter.<p>
     * 
     * @param base the base of the parameter
     * @param index the index of this parameter in the list
     * @param originalIndex the original index in the previous version of the list
     */
    public CmsWidgetDialogParameter(CmsWidgetDialogParameter base, int index, int originalIndex) {

        this(
            null,
            base.m_defaultValue,
            base.getName(),
            base.getWidget(),
            base.getDialogPage(),
            base.getMinOccurs(),
            base.getMaxOccurs(),
            index);

        m_baseObject = base.m_baseObject;
        m_baseObjectProperty = base.m_baseObjectProperty;
        m_baseCollection = base.m_baseCollection;

        if (m_baseCollection != null) {
            if (m_baseCollection instanceof List) {
                // base object is a list - make sure to set possible old value 
                List baseList = (List)m_baseCollection;
                if (originalIndex < baseList.size()) {
                    Object o = baseList.get(originalIndex);
                    if (o != null) {
                        m_value = o.toString();
                    }
                }
            } else if (m_baseCollection instanceof SortedMap) {
                // base object is a sorted map - make sure to set possible old value 
                SortedMap baseMap = (SortedMap)m_baseCollection;
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
    }

    /**
     * Create a new Widget parameter based on a given object's property.<p>
     * 
     * @param base the base object to map the parameter to / from
     * @param property the base object property to map the parameter to / from
     * @param widget the widget used for this parameter
     */
    public CmsWidgetDialogParameter(Object base, String property, I_CmsWidget widget) {

        this(base, property, DEFAULT_DIALOG_PAGE, widget);
    }

    /**
     * Create a new Widget parameter based on a given object's property.<p>
     * 
     * @param base the base object to map the parameter to / from
     * @param property the base object property to map the parameter to / from
     * @param dialogPage the dialog page to use the widget on
     * @param widget the widget used for this parameter
     */
    public CmsWidgetDialogParameter(Object base, String property, String dialogPage, I_CmsWidget widget) {

        this(base, property, null, dialogPage, widget, 1, 1);
    }

    /**
     * Create a new Widget parameter based on a given object's property.<p>
     * 
     * @param base the base object to map the parameter to / from
     * @param property the base object property to map the parameter to / from
     * @param defaultValue the default value to use for this parameter
     * @param dialogPage the dialog page to use the widget on
     * @param widget the widget used for this paramete
     * @param minOccurs the required minimum numer of occurences of this parameter
     * @param maxOccurs the maximum allowed numer of occurences of this parameter
     */
    public CmsWidgetDialogParameter(
        Object base,
        String property,
        String defaultValue,
        String dialogPage,
        I_CmsWidget widget,
        int minOccurs,
        int maxOccurs) {

        if ((base instanceof List) || (base instanceof SortedMap)) {

            // this is a list, use custom list mappings
            init(null, defaultValue, property, widget, dialogPage, 0, MAX_OCCURENCES, 0);

            m_baseObject = null;
            m_baseObjectProperty = null;
            m_baseCollection = base;

        } else {

            // generic object:use reflection to map object properties
            init(null, defaultValue, property, widget, dialogPage, minOccurs, maxOccurs, 0);

            m_baseObject = base;
            m_baseObjectProperty = property;
            m_baseCollection = null;

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

                if ((value instanceof List) || (value instanceof SortedMap)) {
                    m_baseCollection = value;
                    m_minOccurs = 0;
                    m_maxOccurs = MAX_OCCURENCES;
                } else {
                    m_defaultValue = String.valueOf(value);
                    m_value = m_defaultValue;
                    if ((m_minOccurs == 0) && !m_value.equals(defaultValue)) {
                        // if value is different from default ensure this widget is displayed
                        m_minOccurs = 1;
                    }
                }
            }
        }
    }

    /**
     * Create a new Widget parameter.<p>
     * 
     * @param name the name of the parameter
     * @param widget the widget used for this parameter
     */
    public CmsWidgetDialogParameter(String name, I_CmsWidget widget) {

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
    public CmsWidgetDialogParameter(String name, I_CmsWidget widget, int minOccurs, int maxOccurs) {

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
    public CmsWidgetDialogParameter(
        String value,
        String defaultValue,
        String name,
        I_CmsWidget widget,
        String dialog,
        int minOccurs,
        int maxOccurs,
        int index) {

        super();
        init(value, defaultValue, name, widget, dialog, minOccurs, maxOccurs, index);
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

        if (m_baseCollection == null) {

            PropertyUtilsBean bean = new PropertyUtilsBean();
            ConvertUtilsBean converter = new ConvertUtilsBean();
            Object value = null;
            try {
                Class type = bean.getPropertyType(m_baseObject, m_baseObjectProperty);
                value = converter.convert(m_value, type);
                bean.setNestedProperty(m_baseObject, m_baseObjectProperty, value);
                setError(null);
            } catch (InvocationTargetException e) {
                setError(e.getTargetException());
                throw new CmsWidgetException(Messages.get().container(
                    Messages.ERR_PROPERTY_WRITE_3,
                    value,
                    dialog.key(A_CmsWidget.getLabelKey(this), getKey()),
                    m_baseObject.getClass().getName()), e.getTargetException(), this);
            } catch (Exception e) {
                setError(e);
                throw new CmsWidgetException(Messages.get().container(
                    Messages.ERR_PROPERTY_WRITE_3,
                    value,
                    dialog.key(A_CmsWidget.getLabelKey(this), getKey()),
                    m_baseObject.getClass().getName()), e, this);
            }
        } else if (m_baseCollection instanceof SortedMap) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_value)) {
                int pos = m_value.indexOf('=');
                if ((pos > 0) && (pos < (m_value.length() - 1))) {
                    String key = m_value.substring(0, pos);
                    String value = m_value.substring(pos + 1);
                    SortedMap map = (SortedMap)m_baseCollection;
                    if (map.containsKey(key)) {
                        Object val = map.get(key);
                        CmsWidgetException error = new CmsWidgetException(Messages.get().container(
                            Messages.ERR_MAP_DUPLICATE_KEY_3,
                            dialog.key(A_CmsWidget.getLabelKey(this), getKey()),
                            key,
                            val), this);
                        setError(error);
                        throw error;
                    }
                    map.put(key, value);
                } else {
                    CmsWidgetException error = new CmsWidgetException(Messages.get().container(
                        Messages.ERR_MAP_PARAMETER_FORM_1,
                        dialog.key(A_CmsWidget.getLabelKey(this), getKey())), this);
                    setError(error);
                    throw error;
                }
            }
        } else if (m_baseCollection instanceof List) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_value)) {
                List list = (List)m_baseCollection;
                list.add(m_value);
            }
        }
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetParameter#getDefault(org.opencms.file.CmsObject)
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
    public String getDialogPage() {

        return m_dialogPage;
    }

    /**
     * Returns the Exception caused when this parameter value was commited, or <code>null</code>
     * if error occured.<p> 
     * 
     * @return the Exception caused when this parameter value was commited
     */
    public Throwable getError() {

        return m_error;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetParameter#getId()
     */
    public String getId() {

        return m_id;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetParameter#getIndex()
     */
    public int getIndex() {

        return m_index;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetParameter#getKey()
     */
    public String getKey() {

        StringBuffer result = new StringBuffer(128);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_prefix)) {
            result.append(m_prefix);
            result.append('.');
        }
        result.append(getName());
        return result.toString();
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetParameter#getMaxOccurs()
     */
    public int getMaxOccurs() {

        return m_maxOccurs;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetParameter#getMinOccurs()
     */
    public int getMinOccurs() {

        return m_minOccurs;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetParameter#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetParameter#getStringValue(org.opencms.file.CmsObject)
     */
    public String getStringValue(CmsObject cms) throws CmsRuntimeException {

        return m_value;
    }

    /**
     * Returns the widget for this parameter.<p>
     * 
     * @return the widget for this parameter
     */
    public I_CmsWidget getWidget() {

        return m_widget;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetParameter#hasError()
     */
    public boolean hasError() {

        return m_error != null;
    }

    /**
     * Checks if a value for this widget base type with the given id is available.<p>
     * 
     * This should only be used if the base object is a collection.<p>
     * 
     * @param index the index to check
     * 
     * @return <code>true</code> if a value for this widget base type with the given id is available
     */
    public boolean hasValue(int index) {

        if (m_baseCollection instanceof List) {
            return index < ((List)m_baseCollection).size();
        } else if (m_baseCollection instanceof SortedMap) {
            return index < ((SortedMap)m_baseCollection).size();
        }
        return false;
    }

    /**
     * Returns <code>true</code> if this widget parameter is mapped to a Collection base object.<p>
     * 
     * @return <code>true</code> if this widget parameter is mapped to a Collection base object
     */
    public boolean isCollectionBase() {

        return (m_baseCollection != null)
            && ((m_baseCollection instanceof List) || (m_baseCollection instanceof SortedMap));
    }

    /**
     * Prepares this widget dialog parameter to be commited.<p>
     * 
     * This is required if the base type is mapped to a Collection object,
     * becasue the collection needs to be cleared before the new values are set.<p>
     */
    public void prepareCommit() {

        if (m_baseCollection instanceof List) {
            List list = (List)m_baseCollection;
            list.clear();
        } else if (m_baseCollection instanceof SortedMap) {
            SortedMap map = (SortedMap)m_baseCollection;
            map.clear();
        }
    }

    /**
     * Sets the error state of this widget.<p>
     *
     * If the argument is <code>null</code> then the state is set to "no error".<p>
     *
     * @param error the error state to set
     */
    public void setError(Throwable error) {

        m_error = error;
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
     * @see org.opencms.widgets.I_CmsWidgetParameter#setKeyPrefix(java.lang.String)
     */
    public void setKeyPrefix(String prefix) {

        m_prefix = prefix;
    }

    /**
     * @see org.opencms.widgets.I_CmsWidgetParameter#setStringValue(org.opencms.file.CmsObject, java.lang.String)
     */
    public void setStringValue(CmsObject cms, String value) throws CmsIllegalArgumentException {

        m_value = value;
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
        I_CmsWidget widget,
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
        if (maxOccurs < MAX_OCCURENCES) {
            m_maxOccurs = maxOccurs;
        } else {
            m_maxOccurs = MAX_OCCURENCES;
        }
        if (minOccurs >= 0) {
            m_minOccurs = minOccurs;
        } else {
            m_minOccurs = 0;
        }
        if (m_minOccurs > m_maxOccurs) {
            m_minOccurs = m_maxOccurs;
        }
        m_index = index;
        m_dialogPage = dialog;
        m_error = null;

        // create the id
        m_id = createId(m_name, m_index);
    }
}
