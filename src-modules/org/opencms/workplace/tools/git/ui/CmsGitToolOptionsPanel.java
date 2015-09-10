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
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

public class CmsGitToolOptionsPanel extends VerticalLayout {

    enum ActionType {
        checkIn, resetHead, resetRemoteHead;
    }

    private CheckBox m_ignoreUnclean;
    private CheckBox m_pullFirst;
    private CheckBox m_copyAndUnzip;
    private CheckBox m_addAndCommit;
    private TextArea m_commitMessage;
    private CheckBox m_pullAfterCommit;
    private CheckBox m_pushAutomatically;
    private CheckBox m_excludeLib;
    private Button m_checkinSelected;

    private Button m_cancel;

    private ActionType m_mode = ActionType.checkIn;

    private ComboBox m_moduleSelector;

    private VerticalLayout m_moduleSelectionContainer;

    private CmsGitCheckin m_checkinBean;
    private Button m_toggleOptions;

    private Set<String> m_selectedModules = Sets.newHashSet();

    private Map<String, CheckBox> m_moduleCheckboxes = Maps.newHashMap();

    private Button m_deselectAll;
    protected boolean m_advancedVisible = false;

    private Window[] m_currentWindow = new Window[] {null};

    private TextField m_emailField;
    private TextField m_userField;

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

            public void buttonClick(ClickEvent event) {

                runAction(ActionType.checkIn);

            }
        });
        m_cancel.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                A_CmsUI.get().getPage().setLocation(CmsVaadinUtils.getWorkplaceLink());
            }
        });
        m_deselectAll.addClickListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {

                for (Map.Entry<String, CheckBox> entry : m_moduleCheckboxes.entrySet()) {
                    CheckBox checkBox = entry.getValue();
                    checkBox.setValue(Boolean.FALSE);
                }
            }
        });
    }

    public Window addAsWindow(Component component) {

        // Have to use a stupid array because Vaadin declarative tries to bind the field otherwise
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

    public Collection<String> getDirectlySelectedModules() {

        List<String> result = Lists.newArrayList();
        for (Map.Entry<String, CheckBox> entry : m_moduleCheckboxes.entrySet()) {
            if (entry.getValue().getValue().booleanValue()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public void runAction(ActionType mode) {

        m_mode = mode;
        setActionFlags();
        setCommonParameters();
        m_checkinBean.clearModules();
        for (String moduleName : getDirectlySelectedModules()) {
            m_checkinBean.addModuleToExport(moduleName);
        }
        int result = m_checkinBean.checkIn();
        String log = m_checkinBean.getLogText();
        String message = null;
        boolean error = false;
        switch (mode) {
            case checkIn:
                List<Button> resetButtons = new ArrayList<Button>();

                Button resetHead = new Button("Reset head");
                resetHead.setDescription(
                    "Reset local repository to HEAD. You lose uncommitted changes but get a clean repository.");
                resetHead.addClickListener(new ClickListener() {

                    public void buttonClick(ClickEvent event) {

                        runAction(ActionType.resetHead);
                    }
                });

                Button resetRemoteHead = new Button("Reset remote head");
                resetRemoteHead.setDescription(
                    "Reset local repository to the head of the remote branch for conflict resolving. You lose all local changes, even committed, but unpushed ones.");
                resetRemoteHead.addClickListener(new ClickListener() {

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
        }

    }

    public void setAdvancedVisible(boolean visible) {

        for (Component component : getAdvancedOptions()) {
            component.setVisible(visible);
        }
        m_advancedVisible = visible;
        m_toggleOptions.setCaption(visible ? "Hide options" : "Show options");
    }

    public void updateNewModuleSelector() {

        ComboBox newModuleSelector = createModuleSelector();
        ((AbstractLayout)(m_moduleSelector.getParent())).replaceComponent(m_moduleSelector, newModuleSelector);
        m_moduleSelector = newModuleSelector;
        m_moduleSelector.addValueChangeListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {

                String moduleName = (String)(event.getProperty().getValue());
                addSelectableModule(moduleName);
                updateNewModuleSelector();
            }
        });
    }

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
