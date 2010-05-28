/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsSelectBox.java,v $
 * Date   : $Date: 2010/05/28 08:22:28 $
 * Version: $Revision: 1.26 $
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

import org.opencms.gwt.client.I_CmsHasInit;
import org.opencms.gwt.client.ui.input.form.CmsWidgetFactoryRegistry;
import org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory;
import org.opencms.gwt.client.util.CmsPair;

import java.util.List;
import java.util.Map;

/**
 * Widget for selecting one of multiple items from a drop-down list which opens
 * after the user clicks on the widget.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.26 $ 
 * 
 * @since 8.0.0
 * 
 */
public class CmsSelectBox extends A_CmsSelectBox<CmsLabelSelectCell> implements I_CmsHasInit {

    /** Text metrics key. */
    private static final String TM_OPENER_LABEL = "OpenerLabel";

    /** The widget type identifier. */
    private static final String WIDGET_TYPE = "select";

    /** The widget displayed in the opener. */
    protected CmsLabel m_openerWidget;

    /**
     * Default constructor.<p>
     */
    public CmsSelectBox() {

        super();
    }

    /**
     * Constructs a new select box from a list of key-value pairs.<p>
     * 
     * The first component of each pair is the option value, the second is the text to be displayed for the option value.<p>
     * 
     * @param items the items
     */
    public CmsSelectBox(List<CmsPair<String, String>> items) {

        super();
        setItems(items);
    }

    /**
     * Constructs a new select box from a map.<p>
     * 
     * The keys of the map are the values of the select options, and the values of the map are the labels to be displayed
     * for each option.
     * 
     * @param items the map of select options 
     */
    public CmsSelectBox(Map<String, String> items) {

        super();
        setItems(items);
    }

    /**
     * Initializes this class.<p>
     */
    public static void initClass() {

        // registers a factory for creating new instances of this widget
        CmsWidgetFactoryRegistry.instance().registerFactory(WIDGET_TYPE, new I_CmsFormWidgetFactory() {

            /**
             * @see org.opencms.gwt.client.ui.input.form.I_CmsFormWidgetFactory#createWidget(java.util.Map)
             */
            public I_CmsFormWidget createWidget(Map<String, String> widgetParams) {

                return new CmsSelectBox(widgetParams);
            }
        });
    }

    /**
     * Adds a new selection cell.<p>
     * 
     * @param value the value of the select option 
     * @param text the text to be displayed for the select option 
     */
    public void addOption(String value, String text) {

        CmsLabelSelectCell cell = new CmsLabelSelectCell(value, text);
        addOption(cell);
    }

    /**
     * Sets the items as key-value pairs.<p>
     * 
     * The first component of each pair is the option value, the second is the text to be displayed for the option value.<p>
     * 
     * @param items the items
     */
    public void setItems(List<CmsPair<String, String>> items) {

        clearItems();
        for (CmsPair<String, String> item : items) {
            addOption(item.getFirst(), item.getSecond());
        }
    }

    /**
     * Sets the items using a map from option values to label texts.<p>
     * 
     * @param items the map containing the select options
     */
    public void setItems(Map<String, String> items) {

        clearItems();
        for (Map.Entry<String, String> entry : items.entrySet()) {
            addOption(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#truncateOpener(java.lang.String, int)
     */
    @Override
    public void truncateOpener(String prefix, int width) {

        m_openerWidget.truncate(prefix + '_' + TM_OPENER_LABEL, width);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#initOpener()
     */
    @Override
    protected void initOpener() {

        m_openerWidget = new CmsLabel();
        m_openerWidget.addStyleName(CSS.selectBoxOpener());
        m_opener.add(m_openerWidget);
    }

    /**
     * @see org.opencms.gwt.client.ui.input.A_CmsSelectBox#updateOpener(java.lang.String)
     */
    @Override
    protected void updateOpener(String newValue) {

        CmsLabel label = m_openerWidget;
        CmsLabelSelectCell cell = m_selectCells.get(newValue);
        label.setText(cell.getText());

    }
}
