
package org.opencms.util.ant;

import javax.swing.UIManager;

/**
 * Ant task for swing gui based module selection.<p>
 * 
 * Task that prompts user for property values to allow interactive builds.<p>
 *
 * @author <a href="mailto:m.moossen@alkacon.com">Michael Moossen</a> 
 * @version $Revision: 1.1 $
 * @since 6.0
 */
public class CmsAntTaskModulePrompt extends org.apache.tools.ant.Task {

    /** the module list separator constant. */
    public static final String LIST_SEPARATOR = ",";

    private String m_allModules; // required
    private String m_defaultValue;
    private String m_propertyName; // required

    /**
     * PropertyPrompt default constructor.<p>
     */
    public CmsAntTaskModulePrompt() {

        super();
    }

    /**
     * Run the ModulePrompt task.<p>
     * 
     * Sets the given property to <code>__ABORT__</code> if canceled,
     * or to a list of selected modules if not.<p>
     * 
     * @exception org.apache.tools.ant.BuildException
     *
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws org.apache.tools.ant.BuildException {

        log("Prompting user for " + m_propertyName);

        String value = new CmsModuleSelectionDialog(this).getModuleSelection();

        if (value == null) {
            value = "__ABORT__";
            log("cancelled");
        } else {
            log("sel: " + value);
        }
        getProject().setProperty(m_propertyName, value);
    }

    /**
     * Returns the <code>{@link #LIST_SEPARATOR}</code> separated list of all available modules.<p>
     * 
     * @return Returns the all-modules list
     */
    public String getAllModules() {

        return m_allModules;
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
     * Returns the property to store the user selection.<p>
     * 
     * @return Returns the m_propertyName.
     */
    public String getPropertyName() {

        return m_propertyName;
    }

    /**
     * Initializes this task.<p>
     */
    public void init() {

        super.init();
        m_defaultValue = "";
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // noop
        }
    }

    /**
     * Sets the <code>{@link #LIST_SEPARATOR}</code> separated list of all available modules.<p>
     * 
     * @param allModules all-modules list to set
     */
    public void setAllModules(String allModules) {

        this.m_allModules = allModules;
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
     * Sets the property for storing the selected value.
     * 
     * @param propertyName The property to set.
     */
    public void setPropertyName(String propertyName) {

        this.m_propertyName = propertyName;
    }

}