/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/edit/Attic/A_CmsPropertyFormBuilder.java,v $
 * Date   : $Date: 2011/02/18 14:32:08 $
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

package org.opencms.ade.sitemap.client.edit;

import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.gwt.client.ui.input.form.CmsForm;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetMultiFactory;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Abstract superclass for classes which are used to construct property forms for the sitemap entry editor.<p>
 * 
 * Instances of subclasses are intended to be single-use objects, i.e. should be used only for a single form.
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public abstract class A_CmsPropertyFormBuilder {

    /** The sitemap entry editor instance. */
    protected CmsSitemapEntryEditor m_editor;

    /** The form instance. */
    protected CmsForm m_form;

    /** The map of property definitions. */
    protected Map<String, CmsXmlContentProperty> m_propertyDefs;

    /** The list of property names. */
    protected List<String> m_propertyNames;

    /** The factory for creating the widgets. */
    protected I_CmsFormWidgetMultiFactory m_widgetFactory;

    /**
     * The function which builds the form fields and should be implemented by subclasses.<p>
     * 
     * @param entry the entry for which to build the form fields 
     */
    public abstract void buildFields(CmsClientSitemapEntry entry);

    /**
     * Sets the property names.<p>
     * 
     * @param propNames the property names
     */
    public void setAllPropertyNames(Collection<String> propNames) {

        m_propertyNames = new ArrayList<String>(propNames);
        Collections.sort(m_propertyNames);
    }

    /**
     * Sets the sitemap entry editor instance.<p>
     * 
     * @param editor the sitemap entry editor instance 
     */
    public void setEditor(CmsSitemapEntryEditor editor) {

        m_editor = editor;
    }

    /**
     * Sets the form instance.<p>
     * 
     * @param form the form instance 
     */
    public void setForm(CmsForm form) {

        m_form = form;
    }

    /**
     * Sets the property definitions.<p>
     * 
     * @param propertyDefs the property definitions
     */
    public void setPropertyDefinitions(Map<String, CmsXmlContentProperty> propertyDefs) {

        m_propertyDefs = propertyDefs;
    }

    /**
     * Sets the widget factory used to create widgets for the fields.<p>
     * 
     * @param factory the widget factory 
     */
    public void setWidgetFactory(I_CmsFormWidgetMultiFactory factory) {

        m_widgetFactory = factory;
    }

}
