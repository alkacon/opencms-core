/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsElementOptionBar.java,v $
 * Date   : $Date: 2010/04/06 09:49:44 $
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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.draganddrop.CmsDragContainerElement;
import org.opencms.gwt.client.ui.CmsHoverPanel;
import org.opencms.gwt.client.ui.css.I_CmsToolbarButtonLayoutBundle;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Composite;

/**
 * A panel to be displayed inside a container element to provide optional functions like edit, move, remove... <p> 
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsElementOptionBar extends Composite {

    /** The CSS class to be assigned to each option-bar. */
    private static String CSS_CLASS = I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().optionBar();

    /** The option buttons. */
    private Map<String, CmsElementOptionButton> m_options;

    /** The panel. */
    private CmsHoverPanel m_panel;

    /**
     * Constructor.<p>
     */
    public CmsElementOptionBar() {

        m_panel = new CmsHoverPanel();

        m_options = new LinkedHashMap<String, CmsElementOptionButton>();
        initWidget(m_panel);
        this.setStyleName(CSS_CLASS);
    }

    /**
     * Creates an option-bar for the given drag element.<p>
     * 
     * @param element the element to create the option-bar for
     * @param buttons the list of buttons to display
     * 
     * @return the created option-bar
     */
    public static CmsElementOptionBar createOptionBarForElement(
        CmsDragContainerElement element,
        List<I_CmsContainerpageToolbarButton> buttons) {

        CmsElementOptionBar optionBar = new CmsElementOptionBar();
        if (buttons != null) {
            Iterator<I_CmsContainerpageToolbarButton> it = buttons.iterator();
            while (it.hasNext()) {
                I_CmsContainerpageToolbarButton button = it.next();
                if (button.hasElementFunctions()) {
                    CmsElementOptionButton option = button.createOptionForElement(element);
                    optionBar.add(option);
                }
            }
        }

        return optionBar;
    }

    /**
     * Adds another option button.<p>
     * 
     * @param w the button to add
     */
    public void add(CmsElementOptionButton w) {

        m_options.put(w.getToolbarButton().getName(), w);
        m_panel.add(w);

    }

    /**
     * Clears the bar.<p>
     */
    public void clear() {

        m_options.clear();
        m_panel.clear();

    }

    /**
     * Returns the option button with the give index.<p>
     * 
     * @param index the index
     * 
     * @return the button
     */
    public CmsElementOptionButton getOption(int index) {

        return (CmsElementOptionButton)m_panel.getWidget(index);
    }

    /**
     * Returns the number of buttons contained.<p>
     * 
     * @return the button count
     */
    public int getOptionCount() {

        return m_panel.getWidgetCount();
    }

    /**
     * Gets the index of the specified button.<p>
     * 
     * @param child the button
     * 
     * @return the index
     */
    public int getOptionIndex(CmsElementOptionButton child) {

        // TODO: Auto-generated method stub
        return m_panel.getWidgetIndex(child);
    }

    /**
     * Returns an button iterator.<p>
     * 
     * @return the iterator
     */
    public Iterator<CmsElementOptionButton> iterator() {

        // TODO: Auto-generated method stub
        return m_options.values().iterator();
    }

    /**
     * Removes the given button from the panel.<p>
     * 
     * @param w the button to remove
     * 
     * @return true if the button was present
     */
    public boolean remove(CmsElementOptionButton w) {

        m_options.remove(w.getToolbarButton().getName());
        return m_panel.remove(w);
    }

    /**
     * Removes the button with the given index.<p>
     * 
     * @param index the index of the button to remove
     * 
     * @return false if there was no button with the given index
     */
    public boolean remove(int index) {

        m_options.remove(((CmsElementOptionButton)m_panel.getWidget(index)).getToolbarButton().getName());
        return m_panel.remove(index);
    }

}
