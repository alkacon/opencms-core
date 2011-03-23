/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/util/ant/CmsAntTaskSelectionPrompt.java,v $
 * Date   : $Date: 2011/03/23 14:56:57 $
 * Version: $Revision: 1.14 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.util.ant;

import javax.swing.UIManager;

/**
 * Ant task for a highly configurable Swing GUI based selection dialog.<p>
 * 
 * Task that prompts user for selection to allow interactive builds.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.14 $
 * 
 * @since 6.0.0
 */
public class CmsAntTaskSelectionPrompt extends org.apache.tools.ant.Task {

    /** Options list separator constant. */
    public static final String LIST_SEPARATOR = ",";

    /** List of all options. */
    private String m_allValues; // required
    /** The amount of columns to use for display of selection elements. */
    private int m_columns = 2;
    /** List of by default selected options. */
    private String m_defaultValue = "";
    /** Prompt message. */
    private String m_prompt = "Please make your choice:";
    /** Destination property. */
    private String m_property; // required
    /** Mode flag. */
    private boolean m_singleSelection;

    /** Title message. */
    private String m_title = "Selection Dialog";

    /**
     * Default constructor.<p>
     */
    public CmsAntTaskSelectionPrompt() {

        super();
    }

    /**
     * Run the task.<p>
     * 
     * Sets the given property to <code>__ABORT__</code> if canceled, or to a list of selected
     * modules if not.<p>
     * 
     * @throws org.apache.tools.ant.BuildException in case something goes wrong
     * 
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws org.apache.tools.ant.BuildException {

        log("Prompting user for " + m_property);

        String value = new CmsAntTaskSelectionDialog(this).getSelection();

        if (value == null) {
            value = "__ABORT__";
        } else {
            log("user selection: " + value);
        }
        getProject().setProperty(m_property, value);
    }

    /**
     * Returns the <code>{@link #LIST_SEPARATOR}</code> separated list of all available modules.<p>
     * 
     * @return Returns the all-modules list
     */
    public String getAllValues() {

        return m_allValues;
    }

    /**
     * Returns the columns.<p>
     * 
     * @return the columns.
     */
    public int getColumns() {

        return m_columns;
    }

    /**
     * Returns the <code>{@link #LIST_SEPARATOR}</code> separated list of pre-selected modules.<p>
     * 
     * @return Returns the pre-selected module list
     */
    public String getDefaultValue() {

        return m_defaultValue;
    }

    /**
     * Returns the prompt.<p>
     * 
     * @return the prompt
     */
    public String getPrompt() {

        return m_prompt;
    }

    /**
     * Returns the property to store the user selection.<p>
     * 
     * @return Returns the m_propertyName.
     */
    public String getProperty() {

        return m_property;
    }

    /**
     * Returns the title.<p>
     * 
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Initializes this task.<p>
     */
    @Override
    public void init() {

        super.init();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // noop
        }
    }

    /**
     * Returns the Single Selection flag.<p>
     * 
     * @return the single Selection flag
     */
    public boolean isSingleSelection() {

        return m_singleSelection;
    }

    /**
     * Sets the <code>{@link #LIST_SEPARATOR}</code> separated list of all available modules.<p>
     * 
     * @param allValues all-modules list to set
     */
    public void setAllValues(String allValues) {

        this.m_allValues = allValues;
    }

    /**
     * Sets the columns.<p>
     * 
     * @param cols the columns to set
     */
    public void setColumns(String cols) {

        m_columns = Integer.parseInt(cols);
    }

    /**
     * Sets the <code>{@link #LIST_SEPARATOR}</code> separated list of pre-selected modules.<p>
     * 
     * @param defaultValue the pre-selected module list to set
     */
    public void setDefaultValue(String defaultValue) {

        this.m_defaultValue = defaultValue;
    }

    /**
     * Sets the prompt.<p>
     * 
     * @param prompt the prompt to set
     */
    public void setPrompt(String prompt) {

        m_prompt = prompt;
    }

    /**
     * Sets the property for storing the selected value.
     * 
     * @param property The property to set.
     */
    public void setProperty(String property) {

        this.m_property = property;
    }

    /**
     * Sets the single Selection flag.<p>
     * 
     * @param singleSelection the single Selection flag to set
     */
    public void setSingleSelection(boolean singleSelection) {

        m_singleSelection = singleSelection;
    }

    /**
     * Sets the title.<p>
     * 
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }

}