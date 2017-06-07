/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.workplace.tools.git;

import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Properties;

/** Access to a single git configuration file. */
public class CmsGitConfiguration {

    /** The system module export path. */
    private static final String SYSTEM_MODULE_EXPORTPATH = OpenCms.getSystemInfo().getPackagesRfsPath()
        + CmsSystemInfo.FOLDER_MODULES;

    /** The variable under which the default commit message is set. */
    private static final String DEFAULT_COMMIT_MESSAGE = "COMMIT_MESSAGE";
    /** The variable under which the default commit mode is set. */
    private static final String DEFAULT_COMMIT_MODE = "GIT_COMMIT";
    /** The variable under which the default copy and unzip behavior is set. */
    private static final String DEFAULT_COPY_AND_UNZIP = "COPY_AND_UNZIP";
    /** The variable under which the default exclude libs option is set. */
    private static final String DEFAULT_EXCLUDE_LIBS = "DEFAULT_EXCLUDE_LIBS";
    /** The variable under which the export mode is set. */
    private static final String DEFAULT_EXPORT_MODE = "MODULE_EXPORT_MODE";
    /** The variable under which the default git user email is set. */
    private static final String DEFAULT_GIT_USER_EMAIL = "GIT_USER_EMAIL";
    /** The variable under which the default git user name is set. */
    private static final String DEFAULT_GIT_USER_NAME = "GIT_USER_NAME";
    /** The variable under which the default commit mode is set. */
    private static final String DEFAULT_IGNORE_UNCLEAN = "GIT_IGNORE_UNCLEAN";
    /** The variable under wich the default module export path is set. */
    private static final String DEFAULT_MODULE_EXPORT_PATH = "MODULE_EXPORT_FOLDER";
    /** The variable under which the default modules are listed in the configuration file. */
    private static final String DEFAULT_MODULES_TO_EXPORT = "DEFAULT_MODULES_TO_EXPORT";
    /** The variable under which the default pull mode is set for pulling after the commit. */
    private static final String DEFAULT_PULL_MODE_AFTER = "GIT_PULL_AFTER";
    /** The variable under which the default pull mode is set for pulling before any other action. */
    private static final String DEFAULT_PULL_MODE_BEFORE = "GIT_PULL_BEFORE";
    /** The variable under which the default push mode is set. */
    private static final String DEFAULT_PUSH_MODE = "GIT_PUSH";
    /** The variable under which the repository path is set. */
    private static final String DEFAULT_REPOSITORY_PATH = "REPOSITORY_HOME";
    /** The variable under which the resources sub-folder for the modules is configured. */
    private static final String DEFAULT_MODULE_RESOURCES_SUBFOLDER = "MODULE_RESOURCES_SUBFOLDER";
    /** The variable under which the module path of the repository is configured. */
    private static final String DEFAULT_MODULE_PATH = "MODULE_PATH";

    /** The commit message as configured. */
    private String m_defaultCommitMessage = "Autocommit of exported modules.";
    /** The default commit mode. */
    private boolean m_defaultCommitMode;
    /** The default copy and unzip mode. */
    private boolean m_defaultCopyAndUnzip;
    /** Flag, indicating if the lib/ folders of the modules should be removed before the commit. */
    private boolean m_defaultExcludeLibs;
    /** The export mode as configured - is ignored if the system export folder is chosen. */
    private int m_defaultExportMode;
    /** The git user email as configured. */
    private String m_defaultGitUserEmail;
    /** The git user name as configured. */
    private String m_defaultGitUserName;
    /** Flag, indicating if execution of the script should go on for an unclean repository. */
    private boolean m_defaultIgnoreUnclean;
    /** The default module export path as configured. */
    private String m_defaultModuleExportPath = "";
    /** The pull mode as configured. */
    private boolean m_defaultPullModeAfter;
    /** The pull mode as configured. */
    private boolean m_defaultPullModeBefore;
    /** The push mode as configured. */
    private boolean m_defaultPushMode;
    /** Collection of the modules to export and check in, as configured in the config file. */
    private Collection<String> m_configuredModules = new ArrayList<String>();
    /** The path to the folder containing the modules. */
    private String m_modulesPath;
    /** The configured resources sub folder of a module. */
    private String m_resourcesSubfolder;
    /** The repository path as configured. */
    private String m_repositoryPath = "";

    /** Flag, indicating if the config-file is missing. */
    private boolean m_isValid;

    /** The configuration file. */
    private File m_configFile;

    /**
     * Default constructor to wrap a configuration file.
     * @param configFile the configuration file.
     */
    CmsGitConfiguration(final File configFile) {
        m_configFile = configFile;
        readConfigFile();
    }

    /**
     * Returns the wrapped configuration file.
     * @return the wrapped configuration file.
     */
    public File getConfigurationFile() {

        return m_configFile;
    }

    /** Returns the modules specified in the config file.
     * @return the modules specified in the config file.
     */
    public Collection<String> getConfiguredModules() {

        return m_configuredModules;
    }

    /** Returns a flag, indicating if automatic commit is enabled by default.
     * @return a flag, indicating if automatic commit is enabled by default.
     */
    public boolean getDefaultAutoCommit() {

        return m_defaultCommitMode;
    }

    /** Returns a flag, indicating if automatic pulling after the commit is enabled by default.
     * @return a flag, indicating if automatic pulling after the commit is enabled by default.
     */
    public boolean getDefaultAutoPullAfter() {

        return m_defaultPullModeAfter;
    }

    /** Returns a flag, indicating if automatic pulling first is enabled by default.
     * @return a flag, indicating if automatic pulling first is enabled by default.
     */
    public boolean getDefaultAutoPullBefore() {

        return m_defaultPullModeBefore;
    }

    /** Returns a flag, indicating if automatic pushing is enabled by default.
     * @return a flag, indicating if automatic pushing is enabled by default.
     */
    public boolean getDefaultAutoPush() {

        return m_defaultPushMode;
    }

    /** Returns the default commit message.
     * @return the default commit message.
     */
    public String getDefaultCommitMessage() {

        return m_defaultCommitMessage;
    }

    /** Returns the default copy-and-unzip flag.
     * @return the default copy-and-unzip flag.
     */
    public boolean getDefaultCopyAndUnzip() {

        return m_defaultCopyAndUnzip;
    }

    /** Returns the default exclude libs flag.
     * @return the default exclude libs flag.
     */
    public boolean getDefaultExcludeLibs() {

        return m_defaultExcludeLibs;
    }

    /** Returns the configured default git user email, or <code>null</code> if the email is by default not adjusted at all.
     * @return the configured default git user email, or <code>null</code> if the email is by default not adjusted at all.
     */
    public String getDefaultGitUserEmail() {

        return m_defaultGitUserEmail;
    }

    /** Returns the configured default git user name, or <code>null</code> if the name is by default not adjusted at all.
     * @return the configured default git user name, or <code>null</code> if the name is by default not adjusted at all.
     */
    public String getDefaultGitUserName() {

        return m_defaultGitUserName;
    }

    /** Returns the default ignore-unclean flag.
     * @return the default ignore-unclean flag.
     */
    public boolean getDefaultIngoreUnclean() {

        return m_defaultIgnoreUnclean;
    }

    /** Returns the export mode.
     * @return the export mode.
     */
    public int getExportMode() {

        return m_defaultModuleExportPath.isEmpty() ? 1 : m_defaultExportMode;
    }

    /**
     * Returns the path to the config file that is wrapped.
     * @return the path to the config file that is wrapped.
     */
    public String getFilePath() {

        if (null != m_configFile) {
            return m_configFile.getAbsolutePath();
        }
        return null;
    }

    /** Returns the RFS path where module .zip-files are read before check in.
     * @return the RFS path where module .zip-files are read before check in.
     */
    public String getModuleExportPath() {

        return m_defaultModuleExportPath.isEmpty() ? SYSTEM_MODULE_EXPORTPATH : m_defaultModuleExportPath;
    }

    /**
     * Returns the path to where modules are stored in the repository.
     * @return the path to where modules are stored in the repository.
     */
    public String getModulesPath() {

        return m_modulesPath;
    }

    /** Returns the name of the configuration.
     * @return the name of the configuration.
     */
    public String getName() {

        if ((null != m_configFile) && (null != m_configFile.getName())) {
            return m_configFile.getName();
        }
        return "<invalid configuration>";
    }

    /** Returns the path to the home directory of the repository where the modules are checked in.
     * @return the path to the home directory of the repository where the modules are checked in.
     */
    public String getRepositoryPath() {

        return m_repositoryPath;
    }

    /**
     * Returns the subfolder where module resources are placed inside the modules main folder in the repository.
     * @return the subfolder where module resources are placed inside the modules main folder in the repository.
     */
    public String getResourcesSubFolder() {

        return m_resourcesSubfolder;
    }

    /** Returns a flag, indicating if the config file could be read.
     * @return a flag, indicating if the config file could be read.
     */
    public boolean isValid() {

        return m_isValid;
    }

    /** Helper to read a line from the config file.
     * @param propValue the property value
     * @return the value of the variable set at this line.
     */
    private String getValueFromProp(final String propValue) {

        String value = propValue;
        // remove quotes
        value = value.trim();
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    /** Reads the config file and stores configured values in member variables. */
    private void readConfigFile() {

        m_isValid = false;
        if ((null != m_configFile) && m_configFile.exists()) {
            if (m_configFile.canRead()) {
                Properties props = new Properties();
                try (FileInputStream input = new FileInputStream(m_configFile)) {
                    props.load(input);
                    for (Entry<Object, Object> entry : props.entrySet()) {
                        String key = (String)entry.getKey();
                        String propValue = (String)entry.getValue();
                        if (key.equals(DEFAULT_MODULES_TO_EXPORT)) {
                            String value = getValueFromProp(propValue).trim();
                            if (value.contains(",")) {
                                m_configuredModules = Arrays.asList(value.split(" *, *"));
                            } else {
                                m_configuredModules = Arrays.asList(value.split(" +"));
                            }
                        }

                        if (key.equals(DEFAULT_MODULE_PATH)) {
                            m_modulesPath = getValueFromProp(propValue).trim();
                        }

                        if (key.equals(DEFAULT_MODULE_RESOURCES_SUBFOLDER)) {
                            m_resourcesSubfolder = getValueFromProp(propValue).trim();
                        }

                        if (key.equals(DEFAULT_PULL_MODE_BEFORE)) {
                            String value = getValueFromProp(propValue);
                            value = value.trim();
                            if (value.equals("1")) {
                                m_defaultPullModeBefore = true;
                            }
                        }
                        if (key.equals(DEFAULT_PULL_MODE_AFTER)) {
                            String value = getValueFromProp(propValue);
                            value = value.trim();
                            if (value.equals("1")) {
                                m_defaultPullModeAfter = true;
                            }
                        }
                        if (key.equals(DEFAULT_PUSH_MODE)) {
                            String value = getValueFromProp(propValue);
                            value = value.trim();
                            if (value.equals("1")) {
                                m_defaultPushMode = true;
                            }
                        }
                        if (key.equals(DEFAULT_MODULE_EXPORT_PATH)) {
                            String value = getValueFromProp(propValue);
                            m_defaultModuleExportPath = value.trim();
                        }
                        if (key.equals(DEFAULT_REPOSITORY_PATH)) {
                            String value = getValueFromProp(propValue);
                            m_repositoryPath = value.trim();
                        }
                        if (key.equals(DEFAULT_EXPORT_MODE)) {
                            String value = getValueFromProp(propValue);
                            m_defaultExportMode = (value.trim().equals("1")) ? 1 : 0;
                        }
                        if (key.equals(DEFAULT_COMMIT_MESSAGE)) {
                            String value = getValueFromProp(propValue);
                            m_defaultCommitMessage = value;
                        }
                        if (key.equals(DEFAULT_GIT_USER_NAME)) {
                            String value = getValueFromProp(propValue);
                            m_defaultGitUserName = value;
                        }
                        if (key.equals(DEFAULT_GIT_USER_EMAIL)) {
                            String value = getValueFromProp(propValue);
                            m_defaultGitUserEmail = value;
                        }
                        if (key.equals(DEFAULT_EXCLUDE_LIBS)) {
                            String value = getValueFromProp(propValue);
                            m_defaultExcludeLibs = (value.trim().equals("1")) ? true : false;
                        }
                        if (key.equals(DEFAULT_COMMIT_MODE)) {
                            String value = getValueFromProp(propValue);
                            m_defaultCommitMode = (value.trim().equals("1")) ? true : false;
                        }
                        if (key.equals(DEFAULT_IGNORE_UNCLEAN)) {
                            String value = getValueFromProp(propValue);
                            m_defaultIgnoreUnclean = (value.trim().equals("1")) ? true : false;
                        }
                        if (key.equals(DEFAULT_COPY_AND_UNZIP)) {
                            String value = getValueFromProp(propValue);
                            m_defaultCopyAndUnzip = (value.trim().equals("1")) ? true : false;
                        }
                    }
                    m_isValid = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}