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

package org.opencms.workplace.tools.git;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsObject;
import org.opencms.importexport.CmsImportExportException;
import org.opencms.jsp.CmsJspBean;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.module.CmsModuleManager;
import org.opencms.report.CmsPrintStreamReport;
import org.opencms.security.CmsRoleViolationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/** The class provides methods to automatically export modules from OpenCms and check in the exported,
 *  unzipped modules into some git repository.
 *  The feature is only available under Linux at the moment. It uses a shell script.
 *  Which modules are exported to and checked in to which git repository is configured in the file
 *  <code>/WEB-INF/git-scripts/module-checkin.sh</code>.
 *   */
public class CmsGitCheckin extends CmsJspBean {

    /** The default configuration file used for the git check in. */
    private static final String DEFAULT_CONFIG_FILENAME = "module-checkin.conf";
    /** The default script file used for the git check in. */
    private static final String DEFAULT_SCRIPT_FILENAME = "module-checkin.sh";
    /** The variable under which the default modules are listed in the configuration file. */
    private static final String DEFAULT_MODULES_TO_EXPORT = "DEFAULT_MODULES_TO_EXPORT";
    /** The variable under which the default pull mode is set for pulling before any other action. */
    private static final String DEFAULT_PULL_MODE_BEFORE = "GIT_PULL_BEFORE";
    /** The variable under which the default pull mode is set for pulling after the commit. */
    private static final String DEFAULT_PULL_MODE_AFTER = "GIT_PULL_AFTER";
    /** The variable under which the default push mode is set. */
    private static final String DEFAULT_PUSH_MODE = "GIT_PUSH";
    /** The variable under which the default commit mode is set. */
    private static final String DEFAULT_COMMIT_MODE = "GIT_COMMIT";
    /** The variable under which the default commit mode is set. */
    private static final String DEFAULT_IGNORE_UNCLEAN = "GIT_IGNORE_UNCLEAN";
    /** The variable under which the repository path is set. */
    private static final String DEFAULT_REPOSITORY_PATH = "REPOSITORY_HOME";
    /** The variable under which the export path is set. */
    private static final String DEFAULT_MODULE_EXPORT_PATH = "MODULE_EXPORT_FOLDER";
    /** The variable under which the export mode is set. */
    private static final String DEFAULT_EXPORT_MODE = "MODULE_EXPORT_MODE";
    /** The variable under which the default commit message is set. */
    private static final String DEFAULT_COMMIT_MESSAGE = "COMMIT_MESSAGE";
    /** The variable under which the default exclude libs option is set. */
    private static final String DEFAULT_EXCLUDE_LIBS = "DEFAULT_EXCLUDE_LIBS";
    /** The default path under <code>WEB-INF/</code> to the script and configuration file. */
    private static final String DEFAULT_RFS_PATH = "git-scripts/";
    /** The log file for the git check in. */
    private static final String DEFAULT_LOGFILE_PATH = "logs/git.log";

    /** Path to the configuration file. */
    private String m_configPath;
    /** Path to the script for git check in. */
    private String m_scriptPath;
    /** Path to the log file. */
    private String m_logFilePath;
    /** Stream for the log file. */
    private PrintStream m_logStream;
    /** Collection of the modules to export and check in, as configured in the config file. */
    private Collection<String> m_configuredModules = new ArrayList<String>();
    /** The modules that should really be exported and checked in. */
    private Collection<String> m_modulesToExport;
    /** The pull mode as configured. */
    private boolean m_defaultPullModeBefore;
    /** The pull mode as configured. */
    private boolean m_defaultPullModeAfter;
    /** The push mode as configured. */
    private boolean m_defaultPushMode;
    /** The export mode as configured - is ignored if the system export folder is chosen. */
    private int m_defaultExportMode;
    /** The repository path as configured. */
    private String m_repositoryPath = "";
    /** The default module export path as configured. */
    private String m_defaultModuleExportPath = "";
    /** The module export path as used by the OpenCms installation. */
    private String m_systemModuleExportPath;
    /** The commit message as configured. */
    private String m_defaultCommitMessage = "Autocommit of exported modules.";
    /** Flag, indicating if the lib/ folders of the modules should be removed before the commit. */
    private boolean m_defaultExcludeLibs;
    /** Flag, indicating if execution of the script should go on for an unclean repository. */
    private boolean m_defaultIgnoreUnclean;
    /** The default commit mode. */
    private boolean m_defaultCommitMode;
    /** Flag, indicating if an automatic pull should be performed first. */
    private Boolean m_autoPullBefore;
    /** Flag, indicating if an automatic pull should be performed after commit. */
    private Boolean m_autoPullAfter;
    /** Flag, indicating if an automatic push should be performed in the end. */
    private Boolean m_autoPush;
    /** The commit message. */
    private String m_commitMessage;
    /** The commit mode. */
    private Boolean m_commitMode;
    /** Flag, indicating if the lib/ folder of the modules should be deleted before the commit. */
    private Boolean m_excludeLibs;
    /** Flag, indicating if execution of the script should go on for an unclean repository. */
    private Boolean m_ignoreUnclean;
    /** Flag, indicating if reset on ${origin}/${branch} should be performed. */
    private boolean m_resetRemoteHead;
    /** Flag, indicating if reset on HEAD should be performed. */
    private boolean m_resetHead;
    /** Flag, indicating if the config-file is missing. */
    private boolean m_configFileReadable;

    /**
     * Default constructor. Initializing member variables with default values.
     */
    public CmsGitCheckin() {

        String webinfPath = OpenCms.getSystemInfo().getWebInfRfsPath();
        String scriptsPath = webinfPath + DEFAULT_RFS_PATH;
        m_configPath = scriptsPath + DEFAULT_CONFIG_FILENAME;
        m_scriptPath = scriptsPath + DEFAULT_SCRIPT_FILENAME;
        m_logFilePath = webinfPath + DEFAULT_LOGFILE_PATH;
        readConfigFile();
        m_systemModuleExportPath = OpenCms.getSystemInfo().getPackagesRfsPath() + CmsSystemInfo.FOLDER_MODULES;
    }

    /** Adds a module to the modules that should be exported.
     * If called at least once, the explicitly added modules will be exported
     * instead of the default modules.
     *
     * @param moduleName the name of the module to export.
     */
    public void addModuleToExport(final String moduleName) {

        if (m_modulesToExport == null) {
            m_modulesToExport = new HashSet<String>();
        }
        if (m_configuredModules.contains(moduleName)) {
            m_modulesToExport.add(moduleName);
        }
    }

    /**
     * Start export and check in of the selected modules.
     * @return The exit code of the check in procedure (like a script's exit code).
     */
    public int checkIn() {

        try {
            m_logStream = new PrintStream(new FileOutputStream(m_logFilePath, false));
            CmsObject cms = getCmsObject();
            if (cms != null) {
                return checkInInternal();
            } else {
                m_logStream.println("No CmsObject given. Did you call init() first?");
                return -1;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -2;
        }
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

    /** Returns the default exclude libs flag.
     * @return the default exclude libs flag.
     */
    public boolean getDefaultExcludeLibs() {

        return m_defaultExcludeLibs;
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

    /** Returns the path to the log file.
     * @return the path to the log file.
     */
    public String getLogFilePath() {

        return m_logFilePath;
    }

    /** Returns the RFS path where module .zip-files are read before check in.
     * @return the RFS path where module .zip-files are read before check in.
     */
    public String getModuleExportPath() {

        return m_defaultModuleExportPath.isEmpty() ? m_systemModuleExportPath : m_defaultModuleExportPath;
    }

    /** Returns the path to the home directory of the repository where the modules are checked in.
     * @return the path to the home directory of the repository where the modules are checked in.
     */
    public String getRepositoryPath() {

        return m_repositoryPath;
    }

    /** Returns a flag, indicating if the config file could be read.
     * @return a flag, indicating if the config file could be read.
     */
    public boolean isConfigFileReadable() {

        return m_configFileReadable;
    }

    /** Tests if a module is installed.
     * @param moduleName name of the module to check.
     * @return Flag, indicating if the module is installed.
     */
    public boolean isModuleInstalled(final String moduleName) {

        return (OpenCms.getModuleManager().getModule(moduleName) != null);
    }

    /** Setter for the commit mode.
     * @param autoCommit the commit mode to set.
     */
    public void setCommit(final boolean autoCommit) {

        m_commitMode = Boolean.valueOf(autoCommit);
    }

    /** Setter for the commit message.
     * @param message the commit message to set.
     */
    public void setCommitMessage(final String message) {

        m_commitMessage = message;
    }

    /** Setter for the exclude libs flag.
     * @param excludeLibs flag, indicating if the lib/ folder of the modules should be deleted before the commit.
     */
    public void setExcludeLibs(final boolean excludeLibs) {

        m_excludeLibs = Boolean.valueOf(excludeLibs);
    }

    /** Setter for the ignore-unclean flag.
     * @param ignore flag, indicating if an unclean repository should be ignored.
     */
    public void setIgnoreUnclean(final boolean ignore) {

        m_ignoreUnclean = Boolean.valueOf(ignore);
    }

    /** Setter for the pull-after flag.
     * @param autoPull flag, indicating if a pull should be performed directly after the commit.
     */
    public void setPullAfter(final boolean autoPull) {

        m_autoPullAfter = Boolean.valueOf(autoPull);
    }

    /** Setter for the pull-before flag.
     * @param autoPull flag, indicating if a pull should be performed first.
     */
    public void setPullBefore(final boolean autoPull) {

        m_autoPullBefore = Boolean.valueOf(autoPull);
    }

    /** Setter for the auto-push flag.
     * @param autoPush flag, indicating if a push should be performed in the end.
     */
    public void setPush(final boolean autoPush) {

        m_autoPush = Boolean.valueOf(autoPush);
    }

    /** Setter for the reset-head flag.
     * @param reset flag, indicating if a reset to the HEAD should be performed or not.
     */
    public void setResetHead(final boolean reset) {

        m_resetHead = reset;
    }

    /** Setter for the reset-remote-head flag.
     * @param reset flag, indicating if a reset to the ${origin}/${branch} should be performed or not.
     */
    public void setResetRemoteHead(final boolean reset) {

        m_resetRemoteHead = reset;
    }

    /**
     * Export modules and check them in. Assumes the log stream already open.
     * @return exit code of the commit-script.
     */
    private int checkInInternal() {

        m_logStream.println("[" + new Date() + "] STARTING Git checkin task");
        m_logStream.println("=========================");
        m_logStream.println();

        if (!(m_resetHead || m_resetRemoteHead)) {
            m_logStream.println("Exporting relevant modules");
            m_logStream.println("--------------------------");
            m_logStream.println();

            exportModules();

            m_logStream.println();
            m_logStream.println("Calling script to check in the exports");
            m_logStream.println("--------------------------------------");
            m_logStream.println();

        } else {

            m_logStream.println();
            m_logStream.println("Calling script to reset the repository");
            m_logStream.println("--------------------------------------");
            m_logStream.println();

        }
        int exitCode = runCommitScript();

        if (exitCode != 0) {
            m_logStream.println();
            m_logStream.println("ERROR: Something went wrong. The script got exitcode " + exitCode + ".");
            m_logStream.println();
        }
        m_logStream.println("[" + new Date() + "] FINISHED Git checkin task");
        m_logStream.println();
        m_logStream.close();

        return exitCode;
    }

    /** Returns the command to run by the shell to normally run the checkin script.
     * @return the command to run by the shell to normally run the checkin script.
     */
    private String checkinScriptCommand() {

        String exportModules = "";
        if ((m_modulesToExport != null) && !m_modulesToExport.isEmpty()) {
            StringBuffer exportModulesParam = new StringBuffer();
            for (String moduleName : m_modulesToExport) {
                exportModulesParam.append(" ").append(moduleName);
            }
            exportModulesParam.replace(0, 1, " \"");
            exportModulesParam.append("\" ");
            exportModules = " --modules " + exportModulesParam.toString();

        }
        String commitMessage = "";
        if (m_commitMessage != null) {
            commitMessage = " -msg \"" + m_commitMessage + "\"";
        }
        String autoPullBefore = "";
        if (m_autoPullBefore != null) {
            autoPullBefore = m_autoPullBefore.booleanValue() ? " --pull-before " : " --no-pull-before";
        }
        String autoPullAfter = "";
        if (m_autoPullAfter != null) {
            autoPullAfter = m_autoPullAfter.booleanValue() ? " --pull-after " : " --no-pull-after";
        }
        String autoPush = "";
        if (m_autoPush != null) {
            autoPush = m_autoPush.booleanValue() ? " --push " : " --no-push";
        }
        String exportFolder = " --export-folder \"" + getModuleExportPath() + "\"";
        String exportMode = " --export-mode " + getExportMode();
        String excludeLibs = "";
        if (m_excludeLibs != null) {
            excludeLibs = m_excludeLibs.booleanValue() ? " --exclude-libs" : " --no-exclude-libs";
        }
        String commitMode = "";
        if (m_commitMode != null) {
            commitMode = m_commitMode.booleanValue() ? " --commit" : " --no-commit";
        }
        String ignoreUncleanMode = "";
        if (m_ignoreUnclean != null) {
            ignoreUncleanMode = m_ignoreUnclean.booleanValue() ? " --ignore-unclean" : " --no-ignore-unclean";
        }

        return "\""
            + m_scriptPath
            + "\""
            + exportModules
            + commitMessage
            + autoPullBefore
            + autoPullAfter
            + autoPush
            + exportFolder
            + exportMode
            + excludeLibs
            + commitMode
            + ignoreUncleanMode
            + " \""
            + m_configPath
            + "\"";
    }

    /**
     * Export the modules that should be checked in into git.
     */
    private void exportModules() {

        CmsModuleManager moduleManager = OpenCms.getModuleManager();

        Collection<String> modulesToExport = ((m_modulesToExport == null) || m_modulesToExport.isEmpty())
        ? m_configuredModules
        : m_modulesToExport;

        for (String moduleName : modulesToExport) {
            CmsModule module = moduleManager.getModule(moduleName);
            if (module != null) {
                CmsModuleImportExportHandler handler = CmsModuleImportExportHandler.getExportHandler(
                    getCmsObject(),
                    module,
                    "Git export handler");
                try {
                    handler.exportData(getCmsObject(), new CmsPrintStreamReport(
                        m_logStream,
                        OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject()),
                        false));
                } catch (CmsRoleViolationException | CmsConfigurationException | CmsImportExportException e) {
                    e.printStackTrace(m_logStream);
                }
            }
        }
    }

    /** Helper to read a line from the config file.
     * @param line the line to read.
     * @return the value of the variable set at this line.
     */
    private String getValueFromLine(final String line) {

        String value = line.substring(line.indexOf("=") + 1);
        // remove quotes
        value = value.trim();
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    /** Reads the config file and stores configured values in member variables. */
    private void readConfigFile() {

        File configFile = new File(m_configPath);
        if (configFile.exists()) {
            if (configFile.canRead()) {
                BufferedReader configReader = null;
                try {
                    configReader = new BufferedReader(new FileReader(configFile));
                    String line = configReader.readLine();
                    while (line != null) {
                        if (line.startsWith(DEFAULT_MODULES_TO_EXPORT)) {
                            String value = getValueFromLine(line);
                            if (value.contains(",")) {
                                m_configuredModules = Arrays.asList(value.split(","));
                            } else {
                                m_configuredModules = Arrays.asList(value.split(" "));
                            }
                        }
                        if (line.startsWith(DEFAULT_PULL_MODE_BEFORE)) {
                            String value = getValueFromLine(line);
                            value = value.trim();
                            if (value.equals("1")) {
                                m_defaultPullModeBefore = true;
                            }
                        }
                        if (line.startsWith(DEFAULT_PULL_MODE_AFTER)) {
                            String value = getValueFromLine(line);
                            value = value.trim();
                            if (value.equals("1")) {
                                m_defaultPullModeAfter = true;
                            }
                        }
                        if (line.startsWith(DEFAULT_PUSH_MODE)) {
                            String value = getValueFromLine(line);
                            value = value.trim();
                            if (value.equals("1")) {
                                m_defaultPushMode = true;
                            }
                        }
                        if (line.startsWith(DEFAULT_MODULE_EXPORT_PATH)) {
                            String value = getValueFromLine(line);
                            m_defaultModuleExportPath = value.trim();
                        }
                        if (line.startsWith(DEFAULT_REPOSITORY_PATH)) {
                            String value = getValueFromLine(line);
                            m_repositoryPath = value.trim();
                        }
                        if (line.startsWith(DEFAULT_EXPORT_MODE)) {
                            String value = getValueFromLine(line);
                            m_defaultExportMode = (value.trim().equals("1")) ? 1 : 0;
                        }
                        if (line.startsWith(DEFAULT_COMMIT_MESSAGE)) {
                            String value = getValueFromLine(line);
                            m_defaultCommitMessage = value;
                        }
                        if (line.startsWith(DEFAULT_EXCLUDE_LIBS)) {
                            String value = getValueFromLine(line);
                            m_defaultExcludeLibs = (value.trim().equals("1")) ? true : false;
                        }
                        if (line.startsWith(DEFAULT_COMMIT_MODE)) {
                            String value = getValueFromLine(line);
                            m_defaultCommitMode = (value.trim().equals("1")) ? true : false;
                        }
                        if (line.startsWith(DEFAULT_IGNORE_UNCLEAN)) {
                            String value = getValueFromLine(line);
                            m_defaultIgnoreUnclean = (value.trim().equals("1")) ? true : false;
                        }
                        line = configReader.readLine();
                    }
                    m_configFileReadable = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (null != configReader) {
                        try {
                            configReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /** Returns the command to run by the shell to reset to HEAD.
     * @return the command to run by the shell to reset to HEAD.
     */
    private String resetHeadScriptCommand() {

        return "\"" + m_scriptPath + "\" --reset-head" + " \"" + m_configPath + "\"";
    }

    /** Returns the command to run by the shell to reset to ${origin}/${branch}.
     * @return the command to run by the shell to reset to ${origin}/${branch}.
     */
    private String resetRemoteHeadScriptCommand() {

        return "\"" + m_scriptPath + "\" --reset-remote-head" + " \"" + m_configPath + "\"";
    }

    /**
     * Runs the shell script for committing and optionally pushing the changes in the module.
     * @return exit code of the script.
     */
    private int runCommitScript() {

        try {
            m_logStream.flush();
            String commandParam;
            if (m_resetRemoteHead) {
                commandParam = resetRemoteHeadScriptCommand();
            } else if (m_resetHead) {
                commandParam = resetHeadScriptCommand();
            } else {
                commandParam = checkinScriptCommand();
            }
            String[] cmd = {"bash", "-c", commandParam};
            m_logStream.println("Calling the script as follows:");
            m_logStream.println();
            m_logStream.println(cmd[0] + " " + cmd[1] + " " + cmd[2]);
            ProcessBuilder builder = new ProcessBuilder(cmd);
            m_logStream.close();
            m_logStream = null;
            Redirect redirect = Redirect.appendTo(new File(m_logFilePath));
            builder.redirectOutput(redirect);
            builder.redirectError(redirect);
            Process scriptProcess = builder.start();
            int exitCode = scriptProcess.waitFor();
            scriptProcess.getOutputStream().close();
            m_logStream = new PrintStream(new FileOutputStream(m_logFilePath, true));
            return exitCode;
        } catch (InterruptedException | IOException e) {
            e.printStackTrace(m_logStream);
            return -1;
        }

    }
}
