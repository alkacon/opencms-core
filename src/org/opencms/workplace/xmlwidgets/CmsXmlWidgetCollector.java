/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/xmlwidgets/Attic/CmsXmlWidgetCollector.java,v $
 * Date   : $Date: 2004/12/07 16:53:59 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.I_CmsXmlContentValueVisitor;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Visitor implementation that collects the different widgets for all visited values and all widgets for the found values.<p> 
 * 
 * This implementation is needed when creating the html output of the xmlcontent editor 
 * {@link org.opencms.workplace.editors.CmsXmlContentEditor}.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.5.4
 */
public class CmsXmlWidgetCollector implements I_CmsXmlContentValueVisitor {

    /** Static reference to the log. */
    private static Log m_log = OpenCms.getLog(CmsXmlWidgetCollector.class);
    
    /** The locale to get the values from. */
    private Locale m_locale;

    /** The widgets found in the xml content. */
    private Map m_widgets;
    
    /** The unique widgets found in the xml content.  */
    private List m_uniqueWidgets;

    /**
     * Creates a new widget collector node visitor.<p> 
     */
    public CmsXmlWidgetCollector() {

        initialize(null);
    }
    
    /**
     * Creates a new widget collector node visitor.<p> 
     * 
     * @param locale the Locale to get the widgets from
     */
    public CmsXmlWidgetCollector(Locale locale) {
        
        initialize(locale);
    }
    
    /**
     * Returns the locale to get the widgets from.<p>
     * 
     * @return the locale to get the widgets from
     */
    public Locale getLocale() {
        
        return m_locale;
    }

    /**
     * Returns the unique widgets that were found in the content.<p>
     * 
     * @return the unique widgets that were found in the content
     */
    public List getUniqueWidgets() {

        return m_uniqueWidgets;
    }
    
    /**
     * Returns all widgets that were found in the content.<p>
     * 
     * @return all widgets that were found in the content
     */
    public Map getWidgets() {

        return m_widgets;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentValueVisitor#visit(org.opencms.xml.types.I_CmsXmlContentValue)
     */
    public void visit(I_CmsXmlContentValue value) {

        if (m_log.isDebugEnabled()) {
            m_log.debug("Visiting " + value.getPath());
        }

        if (value.isSimpleType()) {
            // only visit simple values
            boolean useLocale = m_locale != null;
            if ((useLocale && value.getLocale() == getLocale()) || (! useLocale)) {
                try {
                    // get widget for value
                    I_CmsXmlWidget widget = value.getContentDefinition().getContentHandler().getWidget(value);
                    if (! m_uniqueWidgets.contains(widget)) {
                        m_uniqueWidgets.add(widget);
                    }
                    m_widgets.put(value, widget);
                } catch (CmsXmlException e) {
                    // should usually not happen
                    if (m_log.isErrorEnabled()) {
                        m_log.error("Could not access widget for content value " + value, e);
                    }
                }
            }
        }
    }
    
    /**
     * Initializes the necessary members of the collector.<p> 
     * 
     * @param locale the Locale to get the widgets from
     */
    private void initialize(Locale locale) {
        
        // start with a new instance of the widgets and unique widgets
        m_widgets = new HashMap(25);
        m_uniqueWidgets = new ArrayList(12);
        // store Locale to use when collecting the widgets
        m_locale = locale;
    }
}