/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.git.ui;

import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.tools.git.CmsGitCheckin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Main widget for the Git check-in tool.<p>
 */
public class CmsGitToolOptionsPanel extends VerticalLayout {

    /**
     * Enum describing the type of action to perform.<p>
     */
    enum ActionType {
        /** Check in. */
        checkIn,

        /** Reset to HEAD. */
        resetHead,

        /** Reset to remote head. */
        resetRemoteHead;
    }

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** True when advanced options are currently visible. */
    protected boolean m_advancedVisible = false;

    /** Map of check boxes for selectable modules, with the module names as keys. */
    Map<String, CheckBox> m_moduleCheckboxes = Maps.newHashMap();

    /** Field for 'Add and commit' setting. */
    private CheckBox m_addAndCommit;

    /** Cancel button. */
    private Button m_cancel;

    /** The check-in bean. */
    private CmsGitCheckin m_checkinBean;

    /** Button for check-in. */
    private Button m_checkinSelected;

    /** Field for 'Commit message' setting. */
    private TextArea m_commitMessage;

    /** Field for 'Copy and unzip' setting. */
    private CheckBox m_copyAndUnzip;

    /** Current window (using an array so the Vaadin field binder doesn't process this. */
    private Window[] m_currentWindow = new Window[] {null};

    /** Button for deselecting all modules. */
    private Button m_deselectAll;

    /** The field for the email address. */
    private TextField m_emailField;

    /** Field for 'Exclude lib' setting. */
    private CheckBox m_excludeLib;

    /** Field for 'Ignroe unclean' setting. */
    private CheckBox m_ignoreUnclean;

    /** Selected mode. */
    private ActionType m_mode = ActionType.checkIn;

    /** Container for the module selector. */
    private VerticalLayout m_moduleSelectionContainer;

    /** Module selector. */
    private ComboBox m_moduleSelector;

    /** Field for 'Pull after commit' setting. */
    private CheckBox m_pullAfterCommit;

    /** Field for 'Pull first' setting. */
    private CheckBox m_pullFirst;

    /** Field for 'Push automatically' setting. */
    private CheckBox m_pushAutomatically;

    /** Button used to toggle advanced options. */
    private Button m_toggleOptions;

    /** The field for the git user name. */
    private TextField m_userField;

    /**
     * Creates a new instance.<p>
     *
     * @param checkinBean the bean to be used for the check-in operation.
     */
    public CmsGitToolOptionsPanel(CmsGitCheckin checkinBean) {
        m_checkinBean = checkinBean;
        if (!checkinBean.isConfigFileReadable()) {
            setMargin(true);
            Label errorLabel = new Label(
                "<h1>\n"
                    + "        Git plugin is not configured correctly.\n"
                    + "    </h1>\n"
                    + "    <p>\n"
                    + "        There were problems reading the configuration file <code>module-checkin.conf</code> under <code>WEB-INF/git-scripts/</code>.\n"
                    + "    </p>\n"
                    + "    <p>\n"
                    + "        Please ensure that the file is present and contains a valid configuration. You may use the file <code>module-checkin.conf.demo</code> under <code>WEB-INF/git-scripts/</code> as template for your own configuration.\n"
                    + "    </p>");
            errorLabel.setContentMode(ContentMode.HTML);
            addComponent(errorLabel);
            return;
        }
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        for (final String moduleName : m_checkinBean.getConfiguredModules()) {
            addSelectableModule(moduleName);
        }
        updateNewModuleSelector();
        m_pullFirst.setValue(Boolean.valueOf(m_checkinBean.getDefaultAutoPullBefore()));
        m_pullAfterCommit.setValue(Boolean.valueOf(m_checkinBean.getDefaultAutoPullAfter()));
        m_addAndCommit.setValue(Boolean.valueOf(m_checkinBean.getDefaultAutoCommit()));
        m_pushAutomatically.setValue(Boolean.valueOf(m_checkinBean.getDefaultAutoPush()));
        m_commitMessage.setValue(Strings.nullToEmpty(m_checkinBean.getDefaultCommitMessage()));
        m_copyAndUnzip.setValue(Boolean.valueOf(m_checkinBean.getDefaultCopyAndUnzip()));
        m_excludeLib.setValue(Boolean.valueOf(m_checkinBean.getDefaultExcludeLibs()));
        m_ignoreUnclean.setValue(Boolean.valueOf(m_checkinBean.getDefaultIngoreUnclean()));
        m_userField.setValue(Strings.nullToEmpty(m_checkinBean.getDefaultGitUserName()));
        m_emailField.setValue(Strings.nullToEmpty(m_checkinBean.getDefaultGitUserEmail()));
        setAdvancedVisible(false);
        m_toggleOptions.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                setAdvancedVisible(!m_advancedVisible);
            }
        });
        m_checkinSelected.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                runAction(ActionType.checkIn);

            }
        });
        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                A_CmsUI.get().getPage().setLocation(CmsVaadinUtils.getWorkplaceLink());
            }
        });
        m_deselectAll.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                for (Map.Entry<String, CheckBox> entry : m_moduleCheckboxes.entrySet()) {
                    CheckBox checkBox = entry.getValue();
                    checkBox.setValue(Boolean.FALSE);
                }
            }
        });
    }

    /**
     * Opens a modal window with the given component as content.<p>
     *
     * @param component the window content
     *
     * @return the window which is opened
     */
    public Window addAsWindow(Component component) {

        // Have to use an array because Vaadin declarative tries to bind the field otherwise
        if (m_currentWindow[0] != null) {
            m_currentWindow[0].close();
            m_currentWindow[0] = null;
        }

        Window window = new Window();
        window.setContent(component);
        window.setCaption("Git check-in results");
        window.setWidth("1000px");
        window.setModal(true);
        window.setResizable(false);
        A_CmsUI.get().addWindow(window);
        m_currentWindow[0] = window;
        return window;
    }

    /**
     * Adds a check box and info widget for a module which should be selectable for check-in.<p>
     *
     * @param moduleName the name of the module
     */
    public void addSelectableModule(final String moduleName) {

        boolean enabled = OpenCms.getModuleManager().hasModule(moduleName);
        CheckBox moduleCheckBox = new CheckBox();
        String iconUri = CmsWorkplace.getResourceUri("tools/modules/buttons/modules.png");
        CmsResourceInfo info = new CmsResourceInfo(moduleName, "", iconUri);
        HorizontalLayout line = new HorizontalLayout();
        line.setWidth("100%");
        line.addComponent(moduleCheckBox);
        info.setWidth("100%");
        line.addComponent(info);
        line.setComponentAlignment(moduleCheckBox, Alignment.MIDDLE_CENTER);
        line.setExpandRatio(info, 1.0f);
        moduleCheckBox.setEnabled(enabled);
        moduleCheckBox.setValue(Boolean.valueOf(enabled)); // If enabled, then checked by default
        m_moduleCheckboxes.put(moduleName, moduleCheckBox);
        m_moduleSelectionContainer.addComponent(line, m_moduleSelectionContainer.getComponentCount() - 1);
    }

    /**
     * Gets the modules which are selected for check-in.<p>
     *
     * @return the selected modules
     */
    public Collection<String> getSelectedModules() {

        List<String> result = Lists.newArrayList();
        for (Map.Entry<String, CheckBox> entry : m_moduleCheckboxes.entrySet()) {
            if (entry.getValue().getValue().booleanValue()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Executes one of the dialog actions.<p>
     *
     * @param action the action to perform
     */
    public void runAction(ActionType action) {

        m_mode = action;
        setActionFlags();
        setCommonParameters();
        m_checkinBean.clearModules();
        for (String moduleName : getSelectedModules()) {
            m_checkinBean.addModuleToExport(moduleName);
        }
        int result = m_checkinBean.checkIn();
        String log = m_checkinBean.getLogText();
        String message = null;
        boolean error = false;
        switch (action) {
            case checkIn:
                List<Button> resetButtons = new ArrayList<Button>();

                Button resetHead = new Button("Reset head");
                resetHead.setDescription(
                    "Reset local repository to HEAD. You lose uncommitted changes but get a clean repository.");
                resetHead.addClickListener(new ClickListener() {

                    private static final long serialVersionUID = 1L;

                    public void buttonClick(ClickEvent event) {

                        runAction(ActionType.resetHead);
                    }
                });

                Button resetRemoteHead = new Button("Reset remote head");
                resetRemoteHead.setDescription(
                    "Reset local repository to the head of the remote branch for conflict resolving. You lose all local changes, even committed, but unpushed ones.");
                resetRemoteHead.addClickListener(new ClickListener() {

                    private static final long serialVersionUID = 1L;

                    public void buttonClick(ClickEvent event) {

                        runAction(ActionType.resetRemoteHead);
                    }
                });

                if (result == 0) {
                    message = "Modules exported and checked in successfully.";
                } else if (result == 10) {
                    message = "Export and check in failed because of an unclean repository. Please consult the log file.";
                    error = true;
                    resetButtons.add(resetRemoteHead);
                    resetButtons.add(resetHead);
                } else {
                    message = "Export and check in failed. Please consult the log file.";
                    error = true;
                    resetButtons.add(resetRemoteHead);
                }
                CmsGitActionResultPanel panel = new CmsGitActionResultPanel(message, log, error, resetButtons);
                addAsWindow(panel);
                break;
            case resetHead:
                if (result == 0) {
                    message = "The repository was reset. You can repeat your commit with the \"Pull first\" option (WARNING: Be aware that you may overwrite changes from the remote repository.)";
                } else {
                    error = true;
                    message = "Reset failed. You may have an incorrect configuration or you have to manually resolve a Git conflict.";
                }
                CmsGitActionResultPanel resetResult = new CmsGitActionResultPanel(
                    message,
                    log,
                    error,
                    new ArrayList<Button>());
                addAsWindow(resetResult);
                break;
            case resetRemoteHead:
                if (result == 0) {
                    message = "The repository was reset. You can repeat your commit. Maybe you need the \"Pull first\" option to avoid conflicts.";
                } else {
                    error = true;
                    message = "Reset failed. You may have an incorrect configuration or you have to manually resolve a GIT conflict. ";
                }
                CmsGitActionResultPanel resetRemoteResult = new CmsGitActionResultPanel(
                    message,
                    log,
                    error,
                    new ArrayList<Button>());
                addAsWindow(resetRemoteResult);
                break;
            default:
                // Should never happen
        }
    }

    /**
     * Sets the flags for the current action.<p>
     */
    public void setActionFlags() {

        switch (m_mode) {
            case checkIn:
                m_checkinBean.setResetHead(false);
                m_checkinBean.setResetRemoteHead(false);
                break;
            case resetHead:
                m_checkinBean.setResetHead(true);
                m_checkinBean.setResetRemoteHead(false);
                break;
            case resetRemoteHead:
                m_checkinBean.setResetHead(false);
                m_checkinBean.setResetRemoteHead(true);
                break;
            default:
                break;
        }

    }

    /**
     * Changes visibility of the advanced options.<p>
     *
     * @param visible true if the options should be shown
     */
    public void setAdvancedVisible(boolean visible) {

        for (Component component : getAdvancedOptions()) {
            component.setVisible(visible);
        }
        m_advancedVisible = visible;
        m_toggleOptions.setCaption(visible ? "Hide options" : "Show options");
    }

    /**
     * Updates the selection widget for adding new modules.<p>
     */
    public void updateNewModuleSelector() {

        ComboBox newModuleSelector = createModuleSelector();
        ((AbstractLayout)(m_moduleSelector.getParent())).replaceComponent(m_moduleSelector, newModuleSelector);
        m_moduleSelector = newModuleSelector;
        m_moduleSelector.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                String moduleName = (String)(event.getProperty().getValue());
                addSelectableModule(moduleName);
                updateNewModuleSelector();
            }
        });
    }

    /**
     * Creates a new module selector, containing only the modules for which no check box is already displayed.<p>
     *
     * @return the new module selector
     */
    private ComboBox createModuleSelector() {

        ComboBox result = new ComboBox();
        result.setPageLength(20);
        result.setWidth("350px");
        result.setFilteringMode(FilteringMode.CONTAINS);
        result.setNewItemsAllowed(false);
        result.setNullSelectionAllowed(false);
        List<String> moduleNames = Lists.newArrayList();
        for (CmsModule module : OpenCms.getModuleManager().getAllInstalledModules()) {
            String moduleName = module.getName();
            if (!m_moduleCheckboxes.containsKey(moduleName)) {
                moduleNames.add(moduleName);
            }
        }
        Collections.sort(moduleNames);
        for (String moduleName : moduleNames) {
            result.addItem(moduleName);
        }

        return result;
    }

    /**
     * Gets the fields which should be shown/hidden when the user toggles the advanced options.<p>
     *
     * @return the list of advanced options
     */
    private List<? extends Component> getAdvancedOptions() {

        return Arrays.asList(
            m_ignoreUnclean,
            m_pullFirst,
            m_copyAndUnzip,
            m_addAndCommit,
            m_pullAfterCommit,
            m_pushAutomatically,
            m_excludeLib);
    }

    /**
     * Transfers the parameters from the form to the check-in bean.<p>
     */
    private void setCommonParameters() {

        m_checkinBean.setPullBefore(m_pullFirst.getValue().booleanValue());
        m_checkinBean.setPullAfter(m_pullAfterCommit.getValue().booleanValue());
        m_checkinBean.setPush(m_pushAutomatically.getValue().booleanValue());
        m_checkinBean.setExcludeLibs(m_excludeLib.getValue().booleanValue());
        m_checkinBean.setCommit(m_addAndCommit.getValue().booleanValue());
        m_checkinBean.setIgnoreUnclean(m_ignoreUnclean.getValue().booleanValue());
        m_checkinBean.setCopyAndUnzip(m_copyAndUnzip.getValue().booleanValue());
        m_checkinBean.setGitUserEmail(m_emailField.getValue());
        m_checkinBean.setGitUserName(m_userField.getValue());
        String commitMessage = m_commitMessage.getValue();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(commitMessage)) {
            m_checkinBean.setCommitMessage(commitMessage);
        }
    }

}
