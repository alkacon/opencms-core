/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/util/ant/CmsAntTaskSelectionTreePrompt.java,v $
 * Date   : $Date: 2011/03/23 14:56:56 $
 * Version: $Revision: 1.10 $
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
 * Ant task for a highly configurable Swing GUI based selection dialog.
 * <p>
 * 
 * Task that prompts user for selection to allow interactive builds.
 * <p>
 * 
 * @author Michael Moossen (original)
 * 
 * @author Achim Westermann (modification)
 * 
 * @version $Revision: 1.10 $
 * 
 * @since 6.0.0
 */
public class CmsAntTaskSelectionTreePrompt extends org.apache.tools.ant.Task {

    /** Options list separator constant. */
    public static final String LIST_SEPARATOR = ",";

    /** List of all options. */
    private String m_allValues; // required

    /** List of by default selected options. */
    private String m_defaultValue = "";

    /** Property to force initial tree expansion for the given amount of levels. */
    private int m_expansionLevels;

    /** Prompt message. */
    private String m_prompt = "Please make your choice:";

    /** Destination property. */
    private String m_property; // required

    /** Mode flag. */
    private boolean m_singleSelection;

    /** Title message. */
    private String m_title = "Selection Dialog";

    /**
     * Default constructor.
     * <p>
     */
    public CmsAntTaskSelectionTreePrompt() {

        super();
    }

    /**
     * For debuggin only.
     * 
     * @param args cmdline args.
     */
    public static void main(String[] args) {

        CmsAntTaskSelectionTreePrompt prompt = new CmsAntTaskSelectionTreePrompt();
        prompt.setAllValues("org.opencms.test,org.opencms.test.subtest,org.opencms.code,org.opencms.blabla,com.lgt.module,com.lgt.code,com.lgt.dummy");
        prompt.setTitle("title");

        prompt.execute();

    }

    /**
     * Run the task.<p>
     * 
     * Sets the given property to <code>__ABORT__</code> if canceled, or to a list of selected
     * modules if not.<p>
     * 
     * @throws org.apache.tools.ant.BuildException in case an error occurs
     * 
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws org.apache.tools.ant.BuildException {

        log("Prompting user for " + m_property);

        String value = new CmsAntTaskSelectionTreeDialog(this).getSelection();

        if (value == null) {
            value = "__ABORT__";
        } else {
            log("user selection: " + value);
        }
        getProject().setProperty(m_property, value);
    }

    /**
     * Returns the <code>{@link #LIST_SEPARATOR}</code> separated list of all available modules.
     * <p>
     * 
     * @return Returns the all-modules list
     */
    public String getAllValues() {

        return m_allValues;
    }

    /**
     * Returns the <code>{@link #LIST_SEPARATOR}</code> separated list of pre-selected modules.
     * <p>
     * 
     * @return Returns the pre-selected module list
     */
    public String getDefaultValue() {

        return m_defaultValue;
    }

    /**
     * Returns the expansionLevels.
     * <p>
     * 
     * @return the expansionLevels
     */
    public int getExpansionLevels() {

        // the mandatory "root" node is needed but invisible
        return m_expansionLevels - 1;
    }

    /**
     * Returns the prompt.
     * <p>
     * 
     * @return the prompt
     */
    public String getPrompt() {

        return m_prompt;
    }

    /**
     * Returns the property to store the user selection.
     * <p>
     * 
     * @return Returns the m_propertyName.
     */
    public String getProperty() {

        return m_property;
    }

    /**
     * Returns the title.
     * <p>
     * 
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Initializes this task.
     * <p>
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
     * Returns the Single Selection flag.
     * <p>
     * 
     * @return the single Selection flag
     */
    public boolean isSingleSelection() {

        return m_singleSelection;
    }

    /**
     * Overridden to allow debugging.
     * <p>
     * 
     * @see org.apache.tools.ant.Task#log(java.lang.String)
     */
    @Override
    public void log(String arg0) {

        try {
            super.log(arg0);
        } catch (Exception ex) {
            System.err.println(arg0);
        }

    }

    /**
     * Overridden to allow debugging.
     * <p>
     * 
     * @see org.apache.tools.ant.Task#log(java.lang.String, int)
     */
    @Override
    public void log(String arg0, int arg1) {

        try {
            super.log(arg0, arg1);
        } catch (Exception ex) {
            System.err.println(arg0);
        }
    }

    /**
     * Sets the <code>{@link #LIST_SEPARATOR}</code> separated list of all available modules.
     * <p>
     * 
     * @param allValues all-modules list to set
     */
    public void setAllValues(String allValues) {

        this.m_allValues = allValues;
    }

    /**
     * Sets the <code>{@link #LIST_SEPARATOR}</code> separated list of pre-selected modules.
     * <p>
     * 
     * @param defaultValue the pre-selected module list to set
     */
    public void setDefaultValue(String defaultValue) {

        this.m_defaultValue = defaultValue;
    }

    /**
     * Sets the expansionLevels.
     * <p>
     * 
     * @param expansionLevels the expansionLevels to set
     */
    public void setExpansionLevels(int expansionLevels) {

        // the mandatory "root" node is needed but invisible
        m_expansionLevels = expansionLevels + 1;
    }

    /**
     * Sets the prompt.
     * <p>
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
     * Sets the single Selection flag.
     * <p>
     * 
     * @param singleSelection the single Selection flag to set
     */
    public void setSingleSelection(boolean singleSelection) {

        m_singleSelection = singleSelection;
    }

    /**
     * Sets the title.
     * <p>
     * 
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }

}