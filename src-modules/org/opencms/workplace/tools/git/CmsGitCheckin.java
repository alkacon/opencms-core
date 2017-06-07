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

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsObject;
import org.opencms.importexport.CmsImportExportException;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.module.CmsModuleManager;
import org.opencms.report.CmsPrintStreamReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsFileUtil.FileWalkState;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections.Closure;
import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Node;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/** The class provides methods to automatically export modules from OpenCms and check in the exported,
 *  unzipped modules into some git repository.
 *  The feature is only available under Linux at the moment. It uses a shell script.
 *  Which modules are exported to and checked in to which git repository is configured in the file
 *  <code>/WEB-INF/git-scripts/module-checkin.sh</code>.
 *   */
public class CmsGitCheckin {

    /** The log file for the git check in. */
    private static final String DEFAULT_LOGFILE_PATH = OpenCms.getSystemInfo().getWebInfRfsPath() + "logs/git.log";
    /** The variable under which the export path is set. */
    /** The default path to the script. */
    private static final String DEFAULT_RFS_PATH = OpenCms.getSystemInfo().getWebInfRfsPath() + "git-scripts/";
    /** The default folder for configuration files. */
    private static final String DEFAULT_CONFIG_FOLDER = DEFAULT_RFS_PATH + "config/";
    /** The default script file used for the git check in. */
    private static final String DEFAULT_SCRIPT_FILE = DEFAULT_RFS_PATH + "module-checkin.sh";
    /** The default configuration file used for the git check in. */
    private static final String DEFAULT_CONFIG_FILE = DEFAULT_RFS_PATH + "module-checkin.conf";
    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGitCheckin.class);
    /** Lock used to prevent simultaneous execution of checkIn method. */
    private static final Object STATIC_LOCK = new Object();

    /** Flag, indicating if an automatic pull should be performed after commit. */
    private Boolean m_autoPullAfter;
    /** Flag, indicating if an automatic pull should be performed first. */
    private Boolean m_autoPullBefore;
    /** Flag, indicating if an automatic push should be performed in the end. */
    private Boolean m_autoPush;
    /** The checkout flag. */
    private boolean m_checkout;
    /** The CMS context. */
    private CmsObject m_cms;
    /** The commit message. */
    private String m_commitMessage;
    /** Flag, indicating if modules should be exported and unzipped. */
    private Boolean m_copyAndUnzip;

    /** The commit mode. */
    private Boolean m_commitMode;

    /** Fetch and reset option. */
    private boolean m_fetchAndResetBeforeImport;

    /** Stream for the log file. */
    private PrintStream m_logStream;

    /** The modules that should really be exported and checked in. */
    private Collection<String> m_modulesToExport;

    /** Flag, indicating if reset on HEAD should be performed. */
    private boolean m_resetHead;

    /** Flag, indicating if reset on ${origin}/${branch} should be performed. */
    private boolean m_resetRemoteHead;

    /** Flag, indicating if the lib/ folder of the modules should be deleted before the commit. */
    private Boolean m_excludeLibs;
    /** The git user email. */
    private String m_gitUserEmail;
    /** The git user name. */
    private String m_gitUserName;
    /** Flag, indicating if execution of the script should go on for an unclean repository. */
    private Boolean m_ignoreUnclean;

    /** The current configuration. */
    private CmsGitConfiguration m_currentConfiguration;
    /** The available configurations. */
    private List<CmsGitConfiguration> m_configurations = new LinkedList<CmsGitConfiguration>();

    /**
     * Default constructor. Initializing member variables with default values.
     *
     * @param cms the CMS context to use
     */
    public CmsGitCheckin(CmsObject cms) {
        m_cms = cms;
        m_configurations = readConfigFiles();
        for (CmsGitConfiguration config : m_configurations) {
            if (config.isValid()) {
                m_currentConfiguration = config;
                break;
            }
        }
    }

    /**
     * Creates ZIP file data from the files / subfolders of the given root folder, and sends it to the given stream.<p>
     *
     * The stream passed as an argument is closed after the data is written.
     *
     * @param root the folder to zip
     * @param zipOutput the output stream which the zip file data should be written to
     *
     * @throws Exception if something goes wrong
     */
    public static void zipRfsFolder(final File root, final OutputStream zipOutput) throws Exception {

        final ZipOutputStream zip = new ZipOutputStream(zipOutput);
        try {
            CmsFileUtil.walkFileSystem(root, new Closure() {

                @SuppressWarnings("resource")
                public void execute(Object stateObj) {

                    try {
                        FileWalkState state = (FileWalkState)stateObj;
                        for (File file : state.getFiles()) {
                            String relativePath = Paths.get(root.getAbsolutePath()).relativize(
                                Paths.get(file.getAbsolutePath())).toString();
                            ZipEntry entry = new ZipEntry(relativePath);
                            entry.setTime(file.lastModified());
                            zip.putNextEntry(entry);
                            zip.write(CmsFileUtil.readFully(new FileInputStream(file)));
                            zip.closeEntry();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception)(e.getCause());
            } else {
                throw e;
            }

        }
        zip.flush();
        zip.close();

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
        m_modulesToExport.add(moduleName);
    }

    /**
     * Start export and check in of the selected modules.
     * @return The exit code of the check in procedure (like a script's exit code).
     */
    public int checkIn() {

        try {
            synchronized (STATIC_LOCK) {
                m_logStream = new PrintStream(new FileOutputStream(DEFAULT_LOGFILE_PATH, false));
                CmsObject cms = getCmsObject();
                if (cms != null) {
                    return checkInInternal();
                } else {
                    m_logStream.println("No CmsObject given. Did you call init() first?");
                    return -1;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -2;
        }
    }

    /**
     * Clears the selected modules.<p>
     */
    public void clearModules() {

        if (m_modulesToExport != null) {
            m_modulesToExport.clear();
        }
    }

    /**
     * Gets the checkout flag.<p>
     *
     * @return the checkout flag
     */
    public boolean getCheckout() {

        return m_checkout;
    }

    /**
     * Gets the CMS context.<p>
     *
     * @return the CMS context
     */
    public CmsObject getCmsObject() {

        return m_cms;
    }

    /**
     * Returns the commitMessage.<p>
     *
     * @return the commitMessage
     */
    public String getCommitMessage() {

        return m_commitMessage;
    }

    /** Returns the available configurations.
     * @return the available configurations.
     */
    public Collection<CmsGitConfiguration> getConfigurations() {

        return m_configurations;
    }

    /**
     * Returns the currently used configuration.
     * @return the currently used configuration.
     */
    public CmsGitConfiguration getCurrentConfiguration() {

        return m_currentConfiguration;
    }

    /**
     * Returns the gitUserEmail.<p>
     *
     * @return the gitUserEmail
     */
    public String getGitUserEmail() {

        return m_gitUserEmail;
    }

    /**
     * Returns the gitUserName.<p>
     *
     * @return the gitUserName
     */
    public String getGitUserName() {

        return m_gitUserName;
    }

    /** Returns the collection of all installed modules.
     *
     * @return the collection of all installed modules.
     */
    public Collection<String> getInstalledModules() {

        return OpenCms.getModuleManager().getModuleNames();
    }

    /** Returns the path to the log file.
     * @return the path to the log file.
     */
    public String getLogFilePath() {

        return DEFAULT_LOGFILE_PATH;
    }

    /**
     * Gets the log text.<p>
     *
     * @return the log text
     */
    @SuppressWarnings("resource")
    public String getLogText() {

        try {
            String logFilePath = getLogFilePath();
            byte[] logData = CmsFileUtil.readFully(new FileInputStream(logFilePath));
            return new String(logData, "UTF-8");
        } catch (@SuppressWarnings("unused") IOException e) {
            return "Error reading log file: " + getLogFilePath();
        }
    }

    /**
     * Returns true if at least one valid configuration is present.
     * @return true if at least one valid configuration is present.
     */
    public boolean hasValidConfiguration() {

        return !m_configurations.isEmpty();
    }

    /**
     * Returns true if the 'fetch and reset' flag is set.<p>
     *
     * @return true if 'fetch and reset' flag is set
     */
    public boolean isFetchAndResetBeforeImport() {

        return m_fetchAndResetBeforeImport;
    }

    /** Tests if a module is installed.
     * @param moduleName name of the module to check.
     * @return Flag, indicating if the module is installed.
     */
    public boolean isModuleInstalled(final String moduleName) {

        return (OpenCms.getModuleManager().getModule(moduleName) != null);
    }

    /**
     * Sets the checkout flag.<p>
     *
     * @param checkout the checkout flag
     */
    public void setCheckout(boolean checkout) {

        m_checkout = checkout;
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

    /** Setter for the copy and unzip mode.
     * @param copyAndUnzip the copy and unzip mode to set.
     */
    public void setCopyAndUnzip(final boolean copyAndUnzip) {

        m_copyAndUnzip = Boolean.valueOf(copyAndUnzip);
    }

    /**
     * Sets the current configuration if it is a valid configuration. Otherwise the configuration is not set.
     * @param configuration the configuration to set.
     * @return flag, indicating if the configuration is set.
     */
    public boolean setCurrentConfiguration(CmsGitConfiguration configuration) {

        if ((null != configuration) && configuration.isValid()) {
            m_currentConfiguration = configuration;
            return true;
        }
        return false;
    }

    /** Setter for the exclude libs flag.
     * @param excludeLibs flag, indicating if the lib/ folder of the modules should be deleted before the commit.
     */
    public void setExcludeLibs(final boolean excludeLibs) {

        m_excludeLibs = Boolean.valueOf(excludeLibs);
    }

    /**
     * Sets the 'fetch and reset' flag.<p>
     *
     * If this flag is set, the script will be used to fetch the remote branch and reset the current branch to the remote state before
     * trying to import the modules.<p>
     *
     * @param fetchAndReset the 'fetch and reset' flag
     */
    public void setFetchAndResetBeforeImport(boolean fetchAndReset) {

        m_fetchAndResetBeforeImport = fetchAndReset;
    }

    /** Setter for the git user email.
     * @param useremail the git user email to set.
     */
    public void setGitUserEmail(final String useremail) {

        m_gitUserEmail = useremail;
    }

    /** Setter for the git user name.
     * @param username the git user name to set.
     */
    public void setGitUserName(final String username) {

        m_gitUserName = username;
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
     * Adds the configuration from <code>configFile</code> to the {@link Collection} of {@link CmsGitConfiguration},
     * if a valid configuration is read. Otherwise it logs the failure.
     * @param configurations Collection of configurations where the new configuration should be added.
     * @param configFile file to read the new configuration from.
     */
    private void addConfigurationIfValid(final Collection<CmsGitConfiguration> configurations, final File configFile) {

        CmsGitConfiguration config = null;
        try {
            if (configFile.isFile()) {
                config = new CmsGitConfiguration(configFile);
                if (config.isValid()) {
                    configurations.add(config);
                } else {
                    throw new Exception(
                        "Could not read git configuration file" + config.getConfigurationFile().getAbsolutePath());
                }
            }
        } catch (NullPointerException npe) {
            LOG.error("Could not read git configuration.", npe);
        } catch (Exception e) {
            String file = (null != config)
                && (null != config.getConfigurationFile())
                && (null != config.getConfigurationFile().getAbsolutePath())
                ? config.getConfigurationFile().getAbsolutePath()
                : "<unknown>";
            LOG.warn("Trying to read invalid git configuration from " + file + ".", e);
        }
    }

    /**
     * Export modules and check them in. Assumes the log stream already open.
     * @return exit code of the commit-script.
     */
    private int checkInInternal() {

        m_logStream.println("[" + new Date() + "] STARTING Git task");
        m_logStream.println("=========================");
        m_logStream.println();

        if (m_checkout) {
            m_logStream.println("Running checkout script");

        } else if (!(m_resetHead || m_resetRemoteHead)) {
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
        if ((exitCode == 0) && m_checkout) {
            boolean importOk = importModules();
            if (!importOk) {
                return -1;
            }
        }
        m_logStream.println("[" + new Date() + "] FINISHED Git task");
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
            commitMessage = " -msg \"" + m_commitMessage.replace("\"", "\\\"") + "\"";
        }
        String gitUserName = "";
        if (m_gitUserName != null) {
            if (m_gitUserName.trim().isEmpty()) {
                gitUserName = " --ignore-default-git-user-name";
            } else {
                gitUserName = " --git-user-name \"" + m_gitUserName + "\"";
            }
        }
        String gitUserEmail = "";
        if (m_gitUserEmail != null) {
            if (m_gitUserEmail.trim().isEmpty()) {
                gitUserEmail = " --ignore-default-git-user-email";
            } else {
                gitUserEmail = " --git-user-email \"" + m_gitUserEmail + "\"";
            }
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
        String exportFolder = " --export-folder \"" + m_currentConfiguration.getModuleExportPath() + "\"";
        String exportMode = " --export-mode " + m_currentConfiguration.getExportMode();
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
        String copyAndUnzip = "";
        if (m_copyAndUnzip != null) {
            copyAndUnzip = m_copyAndUnzip.booleanValue() ? " --copy-and-unzip" : " --no-copy-and-unzip";
        }

        String configFilePath = m_currentConfiguration.getFilePath();

        return "\""
            + DEFAULT_SCRIPT_FILE
            + "\""
            + exportModules
            + commitMessage
            + gitUserName
            + gitUserEmail
            + autoPullBefore
            + autoPullAfter
            + autoPush
            + exportFolder
            + exportMode
            + excludeLibs
            + commitMode
            + ignoreUncleanMode
            + copyAndUnzip
            + " \""
            + configFilePath
            + "\"";
    }

    /** Returns the command to run by the shell to normally run the checkin script.
     * @return the command to run by the shell to normally run the checkin script.
     */
    private String checkoutScriptCommand() {

        String configFilePath = m_currentConfiguration.getFilePath();
        return "\"" + DEFAULT_SCRIPT_FILE + "\"" + " --checkout " + " \"" + configFilePath + "\"";
    }

    /**
     * Export the modules that should be checked in into git.
     */
    private void exportModules() {

        // avoid to export modules if unnecessary
        if (((null != m_copyAndUnzip) && !m_copyAndUnzip.booleanValue())
            || ((null == m_copyAndUnzip) && !m_currentConfiguration.getDefaultCopyAndUnzip())) {
            m_logStream.println();
            m_logStream.println("NOT EXPORTING MODULES - you disabled copy and unzip.");
            m_logStream.println();
            return;
        }
        CmsModuleManager moduleManager = OpenCms.getModuleManager();

        Collection<String> modulesToExport = ((m_modulesToExport == null) || m_modulesToExport.isEmpty())
        ? m_currentConfiguration.getConfiguredModules()
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

    /**
     * Imports a module from the given zip file.<p>
     *
     * @param file the module file to import
     * @throws CmsException if soemthing goes wrong
     *
     * @return true if there were no errors during the import
     */
    private boolean importModule(File file) throws CmsException {

        m_logStream.println("Trying to import module from " + file.getAbsolutePath());
        CmsModuleImportExportHandler importHandler = new CmsModuleImportExportHandler();
        CmsModule module = CmsModuleImportExportHandler.readModuleFromImport(file.getAbsolutePath());
        String moduleName = module.getName();
        I_CmsReport report = new CmsPrintStreamReport(
            m_logStream,
            OpenCms.getWorkplaceManager().getWorkplaceLocale(getCmsObject()),
            false);
        if (OpenCms.getModuleManager().hasModule(moduleName)) {
            OpenCms.getModuleManager().deleteModule(m_cms, moduleName, true /*replace module*/, report);
        }
        CmsImportParameters params = new CmsImportParameters(file.getAbsolutePath(), "/", false);
        importHandler.setImportParameters(params);
        importHandler.importData(m_cms, report);
        file.delete();
        if (report.hasError() || report.hasWarning()) {
            m_logStream.println("Import failed, see opencms.log for details");
            return false;
        }
        return true;
    }

    /**
     * Imports the selected modules from the git repository.<p>
     *
     * @return true if there were no errors during the import
     */
    @SuppressWarnings("resource")
    private boolean importModules() {

        boolean result = true;

        try {
            m_logStream.println("Checking module dependencies.");
            Multimap<String, String> dependencies = HashMultimap.create();
            Set<String> unsortedModules = Sets.newHashSet(m_modulesToExport);
            for (String module : m_modulesToExport) {
                String manifestPath = CmsStringUtil.joinPaths(
                    m_currentConfiguration.getModulesPath(),
                    module,
                    m_currentConfiguration.getResourcesSubFolder(),
                    "manifest.xml");
                Document doc = CmsXmlUtils.unmarshalHelper(
                    CmsFileUtil.readFully(new FileInputStream(manifestPath)),
                    new CmsXmlEntityResolver(null));

                List<?> depNodes = doc.getRootElement().selectNodes("//dependencies/dependency/@name");
                for (Object nodeObj : depNodes) {
                    Node node = ((Node)nodeObj);
                    String dependency = node.getText();
                    if (m_modulesToExport.contains(dependency)) {
                        // we can only handle dependencies between selected modules
                        // and just have to assume that other dependencies are fulfilled
                        dependencies.put(module, dependency);
                    }
                }
            }
            List<String> sortedModules = Lists.newArrayList();
            // if there are no cycles, this loop will find one element on each iteration
            for (int i = 0; i < m_modulesToExport.size(); i++) {
                String nextModule = null;
                for (String key : unsortedModules) {
                    if (dependencies.get(key).isEmpty()) {
                        nextModule = key;
                        break;
                    }
                }
                if (nextModule != null) {
                    sortedModules.add(nextModule);
                    unsortedModules.remove(nextModule);
                    for (String key : Sets.newHashSet(dependencies.keySet())) { // copy key set to avoid concurrent modification exception
                        dependencies.get(key).remove(nextModule);
                    }
                }
            }
            m_logStream.println("Modules sorted by dependencies: " + sortedModules);
            for (String moduleName : sortedModules) {
                String dir = CmsStringUtil.joinPaths(
                    m_currentConfiguration.getModulesPath(),
                    moduleName,
                    m_currentConfiguration.getResourcesSubFolder());
                File dirEntry = new File(dir);
                if (!dirEntry.exists()) {
                    continue;
                }
                try {
                    m_logStream.println("Creating temp file for module " + moduleName);
                    File outputFile = File.createTempFile(moduleName + "-", ".zip");
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    m_logStream.println("Zipping module structure to " + outputFile.getAbsolutePath());
                    zipRfsFolder(dirEntry, fos);
                    result &= importModule(outputFile);
                    outputFile.delete();
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                    e.printStackTrace(m_logStream);
                    result = false;
                }
            }
        } catch (Exception e) {

            LOG.error(e.getLocalizedMessage(), e);
            m_logStream.println("Unable to check dependencies for modules, giving up.");
            e.printStackTrace(m_logStream);
            result = false;
        }
        return result;
    }

    /**
     * Read all configuration files.
     * @return the list with all available configurations
     */
    private List<CmsGitConfiguration> readConfigFiles() {

        List<CmsGitConfiguration> configurations = new LinkedList<CmsGitConfiguration>();

        // Default configuration file for backwards compatibility
        addConfigurationIfValid(configurations, new File(DEFAULT_CONFIG_FILE));

        // All files in the config folder
        File configFolder = new File(DEFAULT_CONFIG_FOLDER);
        if (configFolder.isDirectory()) {
            for (File configFile : configFolder.listFiles()) {
                addConfigurationIfValid(configurations, configFile);
            }
        }
        return configurations;
    }

    /** Returns the command to run by the shell to reset to HEAD.
     * @return the command to run by the shell to reset to HEAD.
     */
    private String resetHeadScriptCommand() {

        String configFilePath = m_currentConfiguration.getFilePath();

        return "\"" + DEFAULT_SCRIPT_FILE + "\" --reset-head" + " \"" + configFilePath + "\"";
    }

    /** Returns the command to run by the shell to reset to ${origin}/${branch}.
     * @return the command to run by the shell to reset to ${origin}/${branch}.
     */
    private String resetRemoteHeadScriptCommand() {

        String configFilePath = m_currentConfiguration.getFilePath();

        return "\"" + DEFAULT_SCRIPT_FILE + "\" --reset-remote-head" + " \"" + configFilePath + "\"";
    }

    /**
     * Runs the shell script for committing and optionally pushing the changes in the module.
     * @return exit code of the script.
     */
    private int runCommitScript() {

        if (m_checkout && !m_fetchAndResetBeforeImport) {
            m_logStream.println("Skipping script....");
            return 0;
        }
        try {
            m_logStream.flush();
            String commandParam;
            if (m_resetRemoteHead) {
                commandParam = resetRemoteHeadScriptCommand();
            } else if (m_resetHead) {
                commandParam = resetHeadScriptCommand();
            } else if (m_checkout) {
                commandParam = checkoutScriptCommand();
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
            Redirect redirect = Redirect.appendTo(new File(DEFAULT_LOGFILE_PATH));
            builder.redirectOutput(redirect);
            builder.redirectError(redirect);
            Process scriptProcess = builder.start();
            int exitCode = scriptProcess.waitFor();
            scriptProcess.getOutputStream().close();
            m_logStream = new PrintStream(new FileOutputStream(DEFAULT_LOGFILE_PATH, true));
            return exitCode;
        } catch (InterruptedException | IOException e) {
            e.printStackTrace(m_logStream);
            return -1;
        }

    }
}
