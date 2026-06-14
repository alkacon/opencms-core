/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.test;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsShell;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.OpenCmsTestLogAppender;
import org.apache.logging.log4j.core.config.Configurator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;

/**
 * Drop-in replacement for {@link OpenCmsTestRunner} that boots OpenCms from a pre-built database
 * snapshot instead of running the full import for every test.<p>
 *
 * A test opts in to the snapshot setup simply by extending this class instead of
 * {@link OpenCmsTestRunner}; its own setup code stays the same. This works because every
 * {@code setupOpenCms(...)} overload funnels into the single terminal version, which this class
 * overrides: the first call for a given fixture builds a snapshot template once (running the
 * regular, slow setup against a file based hsqldb), and every later call for the same fixture
 * (across all test classes in the JVM) just copies the template database and boots OpenCms
 * against the copy, avoiding the expensive import and publish steps. Each call returns a fresh,
 * isolated instance.<p>
 *
 * Snapshot setup is only supported for the hsqldb test database; for any other database product
 * the override transparently falls back to {@code super.setupOpenCms(...)}.<p>
 *
 * See {@code test/org/opencms/test/snapshot-setup.md} for the design and tuning notes.<p>
 */
public class OpenCmsTestSnapRunner extends OpenCmsTestRunner {

    /**
     * System property that, when set to <code>true</code>, disables the snapshot optimization so
     * every setup runs the traditional full import (the same behaviour as a non-hsqldb product).
     * Useful for comparing the old and new setup paths without changing the test classes.<p>
     */
    public static final String PROP_SNAPSHOT_DISABLED = "opencms.test.snapshot.disabled";

    /** Maximum time (ms) to wait for background offline indexing before capturing a snapshot. */
    private static final long SNAPSHOT_INDEX_WAIT_MILLIS = 600000;

    /** RFS folders (below the test WEB-INF) captured and restored with each snapshot. */
    private static final String[] SNAPSHOT_RFS_FOLDERS = {"config", "index", "solr"};

    /** Root directory holding all snapshot template and working databases for this JVM run. */
    private static File m_snapshotRoot;

    /** Lazily built snapshot template databases for this JVM run, keyed by fixture. */
    private static final Map<String, File> m_snapshotTemplates = new HashMap<String, File>();

    /** Flag indicating that the current OpenCms instance was restored from a snapshot. */
    private boolean m_snapshotInstanceOpen;

    /**
     * Default JUnit constructor.<p>
     */
    public OpenCmsTestSnapRunner() {

        // NOOP
    }

    /**
     * Returns the root directory holding the snapshot template and working databases for
     * this JVM run, creating it (and a shutdown hook that deletes it on JVM exit) on first
     * access. A unique directory per JVM avoids clashes between parallel test JVMs.<p>
     *
     * @return the snapshot root directory
     */
    private static synchronized File getSnapshotRootDir() {

        if (m_snapshotRoot == null) {
            final File root = new File(
                System.getProperty("java.io.tmpdir"),
                "opencms-test-snapshot-" + Long.toHexString(System.nanoTime()));
            root.mkdirs();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> CmsFileUtil.purgeDirectory(root)));
            m_snapshotRoot = root;
        }
        return m_snapshotRoot;
    }

    /**
     * Returns the hsqldb database base path inside the given directory.<p>
     *
     * @param dir the directory
     *
     * @return the database base path
     */
    private static String snapshotDbBase(File dir) {

        return new File(dir, "db").getAbsolutePath();
    }

    /**
     * Shuts down the snapshot OpenCms instance after each test method.<p>
     */
    @AfterEach
    public void closeSnapshotOpenCms() {

        if (!m_snapshotInstanceOpen) {
            return;
        }
        if (m_shell != null) {
            OpenCmsTestLogAppender.setBreakOnError(false);
            try {
                m_shell.exit();
            } catch (Throwable t) {
                // ignore
            }
            m_shell = null;
        }
        m_snapshotInstanceOpen = false;
    }

    /**
     * Overrides the terminal setup to boot from a snapshot: builds the template once per fixture
     * (keyed by all setup arguments including the module list), then restores a fresh copy.<p>
     *
     * @see org.opencms.test.OpenCmsTestRunner#setupOpenCms(TestInfo, String, String, String, String, String, String, boolean, List)
     */
    @Override
    protected CmsObject setupOpenCms(
        TestInfo testInfo,
        String importFolder,
        String targetFolder,
        String configFolder,
        String specialConfigFolder,
        String servletMapping,
        String defaultWebAppName,
        boolean publish,
        List<String> modules) {

        initConfiguration();
        if (!DB_HSQLDB.equals(m_dbProduct) || Boolean.getBoolean(PROP_SNAPSHOT_DISABLED)) {
            // snapshots are hsqldb only, and can be disabled via the property; fall back to the
            // regular import based setup
            return super.setupOpenCms(
                testInfo,
                importFolder,
                targetFolder,
                configFolder,
                specialConfigFolder,
                servletMapping,
                defaultWebAppName,
                publish,
                modules);
        }
        String key = snapshotKey(
            importFolder,
            targetFolder,
            configFolder,
            specialConfigFolder,
            servletMapping,
            defaultWebAppName,
            publish,
            modules);
        File templateDir;
        synchronized (m_snapshotTemplates) {
            templateDir = m_snapshotTemplates.get(key);
            if (templateDir == null) {
                templateDir = buildSnapshotTemplate(
                    testInfo,
                    importFolder,
                    targetFolder,
                    configFolder,
                    specialConfigFolder,
                    servletMapping,
                    defaultWebAppName,
                    publish,
                    modules);
                m_snapshotTemplates.put(key, templateDir);
            }
        }
        return restoreOpenCmsFromSnapshot(testInfo, templateDir, configFolder);
    }

    /**
     * Builds a one-time snapshot template by running the regular (slow) setup plus the module
     * import against a file based hsqldb, then capturing the database and RFS state.<p>
     *
     * The real setup runs via {@code super.setupOpenCms(...)} so this override cannot recurse.<p>
     *
     * @param testInfo the JUnit test info object
     * @param importFolder the folder to import in the "real" FS
     * @param targetFolder the target folder of the import in the VFS
     * @param configFolder the standard configuration folder, or <code>null</code> for the default
     * @param specialConfigFolder the special configuration folder, or <code>null</code> for none
     * @param servletMapping the servlet mapping used by the OpenCms shell
     * @param defaultWebAppName the default webapp name assumed by the OpenCms shell
     * @param publish publish only if set
     * @param modules the modules to import in order, or <code>null</code> for none
     *
     * @return the directory holding the built template database
     */
    private File buildSnapshotTemplate(
        TestInfo testInfo,
        String importFolder,
        String targetFolder,
        String configFolder,
        String specialConfigFolder,
        String servletMapping,
        String defaultWebAppName,
        boolean publish,
        List<String> modules) {

        File templateDir = new File(getSnapshotRootDir(), "template-" + m_snapshotTemplates.size());
        CmsFileUtil.purgeDirectory(templateDir);
        templateDir.mkdirs();
        String dbBase = snapshotDbBase(templateDir);

        // make the test setup connections and the OpenCms runtime use a file based hsqldb
        initConfiguration();
        pointConnectionsToFileDb(dbBase);
        // tune the throwaway build database for fast import writes
        try {
            configureBuildDatabase(dbBase);
        } catch (Exception e) {
            fail("Unable to configure snapshot build database\n" + CmsException.getStackTraceAsString(e));
        }
        String fileUrlConfig = createFileUrlConfigFolder(resolveConfigFolder(configFolder), dbBase);

        // run the regular (slow) setup plus module import once against the file database, via super
        // so this override cannot recurse
        super.setupOpenCms(
            testInfo,
            importFolder,
            targetFolder,
            fileUrlConfig,
            specialConfigFolder,
            servletMapping,
            defaultWebAppName,
            publish,
            modules);

        // wait for background offline indexing to finish so the captured search index is complete
        try {
            OpenCms.getSearchManager().updateOfflineIndexes(SNAPSHOT_INDEX_WAIT_MILLIS);
        } catch (Throwable t) {
            // ignore: search may be disabled or unavailable in this configuration
        }

        // shut OpenCms down and checkpoint the database so the files form a consistent snapshot
        if (m_shell != null) {
            try {
                m_shell.exit();
            } catch (Throwable t) {
                // ignore
            }
            m_shell = null;
        }
        try {
            checkpointAndShutdown(dbBase);
        } catch (Exception e) {
            fail("Unable to checkpoint snapshot database\n" + CmsException.getStackTraceAsString(e));
        }

        // capture the post-import RFS state (config + search index) so it can be restored per test
        File rfsBackup = new File(templateDir, "rfs");
        File webInf = getWebInfDir();
        for (String folder : SNAPSHOT_RFS_FOLDERS) {
            copyDirRecursive(new File(webInf, folder), new File(rfsBackup, folder));
        }
        return templateDir;
    }

    /**
     * Checkpoints and shuts down the file based hsqldb so its files form a consistent snapshot.<p>
     *
     * @param dbBase the file based hsqldb database base path
     *
     * @throws Exception if something goes wrong
     */
    private void checkpointAndShutdown(String dbBase) throws Exception {

        Class.forName("org.hsqldb.jdbcDriver");
        try (Connection conn = DriverManager.getConnection("jdbc:hsqldb:file:" + dbBase + ";hsqldb.tx=mvcc", "sa", "");
        Statement st = conn.createStatement()) {
            st.execute("CHECKPOINT DEFRAG");
            st.execute("SHUTDOWN");
        }
    }

    /**
     * Configures the throwaway build database for fast import writes.<p>
     *
     * The build database is discarded after it has been checkpointed and copied, so durability
     * during the import does not matter. Disabling the transaction log and batching disk writes
     * avoids per-commit log I/O during the (large) import; the final {@code CHECKPOINT} still
     * flushes all data to disk.<p>
     *
     * @param dbBase the file based hsqldb database base path
     *
     * @throws Exception if something goes wrong
     */
    private void configureBuildDatabase(String dbBase) throws Exception {

        Class.forName("org.hsqldb.jdbcDriver");
        try (Connection conn = DriverManager.getConnection("jdbc:hsqldb:file:" + dbBase + ";hsqldb.tx=mvcc", "sa", "");
        Statement st = conn.createStatement()) {
            st.execute("SET FILES LOG FALSE");
            st.execute("SET FILES WRITE DELAY 2000 MILLIS");
        }
    }

    /**
     * Recursively copies a directory and all its files and subdirectories.<p>
     *
     * Does nothing if the source directory does not exist.<p>
     *
     * @param from the source directory
     * @param to the target directory
     */
    private void copyDirRecursive(File from, File to) {

        if (!from.exists()) {
            return;
        }
        to.mkdirs();
        File[] files = from.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            File target = new File(to, f.getName());
            if (f.isDirectory()) {
                copyDirRecursive(f, target);
            } else {
                try {
                    Files.copy(f.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    fail("Unable to copy snapshot RFS files\n" + CmsException.getStackTraceAsString(e));
                }
            }
        }
    }

    /**
     * Copies the (flat) database files of a snapshot directory to a target directory.<p>
     *
     * @param from the source directory
     * @param to the target directory
     */
    private void copySnapshotFiles(File from, File to) {

        File[] files = from.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (!f.isFile()) {
                continue;
            }
            try {
                Files.copy(f.toPath(), new File(to, f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                fail("Unable to copy snapshot database\n" + CmsException.getStackTraceAsString(e));
            }
        }
    }

    /**
     * Creates a temporary copy of the given standard configuration folder with the hsqldb
     * connection rewritten to a file based URL at the given database base path.<p>
     *
     * @param srcConfigFolder the standard configuration folder to copy and rewrite
     * @param dbBase the file based hsqldb database base path
     *
     * @return the absolute path of the created configuration folder
     */
    private String createFileUrlConfigFolder(String srcConfigFolder, String dbBase) {

        File src = new File(srcConfigFolder);
        File dst = new File(getSnapshotRootDir(), "config");
        CmsFileUtil.purgeDirectory(dst);
        dst.mkdirs();
        File[] files = src.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.isFile()) {
                    continue;
                }
                File target = new File(dst, f.getName());
                try {
                    if (f.getName().equals("opencms.properties")) {
                        String content = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                        content = content.replace("jdbc:hsqldb:mem:.", "jdbc:hsqldb:file:" + dbBase);
                        Files.write(target.toPath(), content.getBytes(StandardCharsets.UTF_8));
                    } else {
                        Files.copy(f.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (Exception e) {
                    fail("Unable to create snapshot config folder\n" + CmsException.getStackTraceAsString(e));
                }
            }
        }
        return dst.getAbsolutePath() + File.separator;
    }

    /**
     * Returns the working directory holding the restored snapshot database.<p>
     *
     * @return the snapshot working directory
     */
    private File getSnapshotWorkDir() {

        return new File(getSnapshotRootDir(), "work");
    }

    /**
     * Returns the WEB-INF directory of the primary test data path.<p>
     *
     * @return the WEB-INF directory
     */
    private File getWebInfDir() {

        return new File(m_testDataPath.get(0), "WEB-INF");
    }

    /**
     * Points the test setup and default database connections at a file based hsqldb.<p>
     *
     * @param dbBase the file based hsqldb database base path
     */
    private void pointConnectionsToFileDb(String dbBase) {

        String fileUrl = "jdbc:hsqldb:file:" + dbBase;
        m_setupConnection.m_jdbcUrl = fileUrl;
        m_defaultConnection.m_jdbcUrl = fileUrl;
    }

    /**
     * Restores a fresh OpenCms instance from a snapshot template by copying the template
     * database plus the captured RFS state and booting OpenCms against the copy, without
     * importing.<p>
     *
     * @param testInfo the JUnit test info object
     * @param templateDir the directory holding the template database to restore from
     * @param configFolder the standard configuration folder used to build the snapshot, or
     *            <code>null</code> for the default (used to redirect the database URL)
     *
     * @return an initialized OpenCms context with "Admin" user in the "Offline" project
     */
    private CmsObject restoreOpenCmsFromSnapshot(TestInfo testInfo, File templateDir, String configFolder) {

        initConfiguration();

        boolean verbose = Boolean.getBoolean("opencms.test.verbose");
        if (!verbose) {
            // suppress the (expected) errors logged while OpenCms boots, as the regular setup does
            Configurator.setRootLevel(Level.ERROR);
            Configurator.setLevel("org.opencms.search.A_CmsSearchIndex", Level.OFF);
            Configurator.setLevel("org.opencms.workplace.editors.CmsWorkplaceEditorManager", Level.OFF);
            Configurator.setLevel("org.opencms.workplace.CmsWorkplaceManager", Level.OFF);
        }

        OpenCmsTestLogAppender.setBreakOnError(false);

        // shut down OpenCms and any previous working database so the database files can be replaced
        if (m_shell != null) {
            try {
                m_shell.exit();
            } catch (Throwable t) {
                // ignore
            }
            m_shell = null;
            m_snapshotInstanceOpen = false;
        }

        File workDir = getSnapshotWorkDir();
        String dbBase = snapshotDbBase(workDir);
        shutdownDatabaseQuietly(dbBase);

        // replace the working database with a fresh copy of the template
        CmsFileUtil.purgeDirectory(workDir);
        workDir.mkdirs();
        copySnapshotFiles(templateDir, workDir);

        // point the test connections and the OpenCms runtime at the restored working database
        pointConnectionsToFileDb(dbBase);
        m_resourceStorages = new HashMap<String, OpenCmsTestResourceStorage>();

        // restore the captured post-import RFS state (config + search index) verbatim
        File webInf = getWebInfDir();
        File rfsBackup = new File(templateDir, "rfs");
        for (String folder : SNAPSHOT_RFS_FOLDERS) {
            File live = new File(webInf, folder);
            CmsFileUtil.purgeDirectory(live);
            copyDirRecursive(new File(rfsBackup, folder), live);
        }
        // the captured opencms.properties points at the build database; redirect it to the working copy
        writeFileDbUrlIntoLiveConfig(resolveConfigFolder(configFolder), dbBase);

        PrintStream out = verbose ? System.out : new PrintStream(OutputStream.nullOutputStream());
        CmsObject cms = null;
        try {
            // boot OpenCms against the restored database (no setup scripts, no import)
            m_shell = new CmsShell(
                getTestDataPath("WEB-INF" + File.separator),
                null,
                null,
                "${user}@${project}>",
                null,
                out,
                System.err,
                false);
            m_snapshotInstanceOpen = true;

            cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
            cms.loginUser("Admin", "admin");
            cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
            cms.getRequestContext().setSiteRoot("/sites/default/");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            fail("Unable to restore OpenCms from snapshot\n" + CmsException.getStackTraceAsString(t));
        }

        if (!verbose) {
            // restore the regular log levels for the test run
            Configurator.setRootLevel(Level.WARN);
            Configurator.setLevel("org.opencms.search.A_CmsSearchIndex", Level.WARN);
            Configurator.setLevel("org.opencms.workplace.editors.CmsWorkplaceEditorManager", Level.WARN);
            Configurator.setLevel("org.opencms.workplace.CmsWorkplaceManager", Level.WARN);
        }
        OpenCmsTestLogAppender.setBreakOnError(true);
        printInfoBox(new String[] {"Restored OpenCms snapshot: " + templateDir});
        return cms;
    }

    /**
     * Shuts down a still open file based hsqldb instance for the given database base path
     * so its files can safely be replaced. Does nothing if no database exists yet, and
     * ignores errors (the database may already be shut down).<p>
     *
     * This is required because hsqldb keeps a file database instance registered in the JVM
     * even after all its connections are closed; without an explicit shutdown a later boot
     * against the same path would reattach to the stale instance instead of loading the
     * freshly copied snapshot files.<p>
     *
     * @param dbBase the file based hsqldb database base path
     */
    private void shutdownDatabaseQuietly(String dbBase) {

        if (!new File(dbBase + ".properties").exists()) {
            // nothing has been created at this path yet
            return;
        }
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            try (
            Connection conn = DriverManager.getConnection("jdbc:hsqldb:file:" + dbBase + ";hsqldb.tx=mvcc", "sa", "");
            Statement st = conn.createStatement()) {
                st.execute("SHUTDOWN");
            }
        } catch (Throwable t) {
            // ignore: the database may already be shut down
        }
    }

    /**
     * Builds the snapshot cache key from all setup arguments, appending the module names in
     * order and ignoring a <code>null</code> module list.<p>
     *
     * @param importFolder the import folder
     * @param targetFolder the target folder
     * @param configFolder the standard configuration folder
     * @param specialConfigFolder the special configuration folder
     * @param servletMapping the servlet mapping
     * @param defaultWebAppName the default webapp name
     * @param publish the publish flag
     * @param modules the modules to import, or <code>null</code> for none
     *
     * @return the cache key
     */
    private String snapshotKey(
        String importFolder,
        String targetFolder,
        String configFolder,
        String specialConfigFolder,
        String servletMapping,
        String defaultWebAppName,
        boolean publish,
        List<String> modules) {

        StringBuilder key = new StringBuilder();
        key.append(importFolder).append('|').append(targetFolder).append('|').append(configFolder).append('|').append(
            specialConfigFolder).append('|').append(servletMapping).append('|').append(defaultWebAppName).append(
                '|').append(publish);
        if (modules != null) {
            for (String module : modules) {
                key.append('|').append(module);
            }
        }
        return key.toString();
    }

    /**
     * Writes the file based hsqldb connection URL into the live config's opencms.properties,
     * keeping the remaining (restored, post-import) configuration files unchanged.<p>
     *
     * @param configFolder the standard configuration folder providing the properties template
     * @param dbBase the file based hsqldb database base path
     */
    private void writeFileDbUrlIntoLiveConfig(String configFolder, String dbBase) {

        File src = new File(configFolder, "opencms.properties");
        File dst = new File(new File(getWebInfDir(), CmsSystemInfo.FOLDER_CONFIG_DEFAULT), "opencms.properties");
        try {
            String content = new String(Files.readAllBytes(src.toPath()), StandardCharsets.UTF_8);
            content = content.replace("jdbc:hsqldb:mem:.", "jdbc:hsqldb:file:" + dbBase);
            Files.write(dst.toPath(), content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            fail("Unable to write snapshot db url into live config\n" + CmsException.getStackTraceAsString(e));
        }
    }
}
