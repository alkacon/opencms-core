/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/CmsXmlContentWidgetVisitor.java,v $
 * Date   : $Date: 2005/06/23 11:11:54 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.editors;

import org.opencms.main.CmsLog;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.Messages;
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
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 6.0.0 
 */
public class CmsXmlContentWidgetVisitor implements I_CmsXmlContentValueVisitor {

    /** Static reference to the log. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContentWidgetVisitor.class);

    /** The locale to get the values from. */
    private Locale m_locale;

    /** The unique widgets found in the xml content.  */
    private List m_uniqueWidgets;

    /** The values corresponding to the found widgets. */
    private Map m_values;

    /** The widgets found in the xml content. */
    private Map m_widgets;

    /**
     * Creates a new widget collector node visitor.<p> 
     */
    public CmsXmlContentWidgetVisitor() {

        initialize(null);
    }

    /**
     * Creates a new widget collector node visitor.<p> 
     * 
     * @param locale the Locale to get the widgets from
     */
    public CmsXmlContentWidgetVisitor(Locale locale) {

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
     * Returns all simple values that were found in the content.<p>
     * 
     * The map key is the complete xpath of the value.<p>
     * 
     * @return all simple values that were found in the content
     */
    public Map getValues() {

        return m_values;
    }

    /**
     * Returns all widgets that were found in the content.<p>
     * 
     * The map key is the complete xpath of the corresponding value.<p>
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

        if (LOG.isDebugEnabled()) {
            LOG.debug(org.opencms.workplace.editors.Messages.get().key(
                org.opencms.workplace.editors.Messages.LOG_VISITING_1,
                value.getPath()));
        }

        if (value.isSimpleType()) {
            // only visit simple values
            boolean useLocale = m_locale != null;
            if ((useLocale && value.getLocale() == getLocale()) || (!useLocale)) {
                try {
                    // get widget for value
                    I_CmsWidget widget = value.getContentDefinition().getContentHandler().getWidget(value);
                    if (!m_uniqueWidgets.contains(widget)) {
                        m_uniqueWidgets.add(widget);
                    }
                    m_widgets.put(value.getPath(), widget);
                    m_values.put(value.getPath(), value);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().container(Messages.LOG_DEBUG_WIDGETCOLLECTOR_ADD_1, value.getPath()));
                    }
                } catch (CmsXmlException e) {
                    // should usually not happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().container(Messages.ERR_WIDGETCOLLECTOR_ADD_1, value), e);
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
        m_values = new HashMap(25);
        // store Locale to use when collecting the widgets
        m_locale = locale;
    }
}