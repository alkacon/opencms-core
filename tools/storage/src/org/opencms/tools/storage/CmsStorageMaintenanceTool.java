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

package org.opencms.tools.storage;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.CmsDbContext;
import org.opencms.db.storage.CmsStorageManager;
import org.opencms.db.storage.I_CmsDbStorage;
import org.opencms.db.storage.I_CmsEnumerableStorage;
import org.opencms.db.storage.I_CmsStorage;
import org.opencms.loader.CmsImageCacheFactory;
import org.opencms.util.CmsStringUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Command line entry point for OpenCms storage maintenance checks.<p>
 */
public final class CmsStorageMaintenanceTool {

    /**
     * Configured storage backend metadata.<p>
     */
    private static class Backend {

        /** The configured backend id / stored identifier. */
        private String m_id;

        /** The backend type. */
        private String m_type;
    }

    /**
     * Parsed command line options.<p>
     */
    private static class CommandLine {

        /** Additional JDBC driver jars. */
        private List<Path> m_driverJars = new ArrayList<>();

        /** If true, help should be printed. */
        private boolean m_help;

        /** The selected maintenance mode. */
        private String m_mode = MODE_ALL;

        /** Whether deleting orphans should actually modify storage. */
        private boolean m_execute;

        /** Page size for database orphan candidates. */
        private int m_batchSize = DEFAULT_BATCH_SIZE;

        /** Maximum number of orphans to delete, 0 means unlimited. */
        private int m_deleteLimit = 1000;

        /** Maximum number of details printed per check. */
        private int m_sampleLimit = 20;

        /** The opencms.properties path. */
        private Path m_propertiesPath;

        /** The WEB-INF path. */
        private Path m_webInfPath;
    }

    /**
     * Orphan scan/delete statistics.<p>
     */
    private static class OrphanStats {

        /** Deleted blobs. */
        private long m_deleted;

        /** Failed deletes. */
        private long m_deleteFailures;

        /** Orphaned blobs. */
        private long m_orphans;

        /** Stored blobs. */
        private long m_stored;
    }

    /**
     * Periodically prints progress without flooding the console.<p>
     */
    private static class ProgressReporter {

        /** Label printed before the progress details. */
        private String m_label;

        /** Last count which was printed. */
        private long m_lastCount;

        /** Time of the last progress output. */
        private long m_lastOutputNanos = System.nanoTime();

        /**
         * Creates a progress reporter.<p>
         *
         * @param label the label
         */
        ProgressReporter(String label) {

            m_label = label;
        }

        /**
         * Prints progress after a count or time interval has elapsed.<p>
         *
         * @param count the current item count
         * @param details additional details
         */
        void report(long count, String details) {

            long now = System.nanoTime();
            if (((count - m_lastCount) >= PROGRESS_ITEM_INTERVAL)
                || ((now - m_lastOutputNanos) >= PROGRESS_TIME_INTERVAL_NANOS)) {
                System.out.println("  " + m_label + ": " + count + details);
                m_lastCount = count;
                m_lastOutputNanos = now;
            }
        }
    }

    /**
     * One storage reference from the database.<p>
     */
    private static class Reference {

        /** Hash column value. */
        private String m_hash;

        /** Number of referencing rows. */
        private long m_rows;

        /** Storage column value. */
        private String m_storage;
    }

    /**
     * Tool runner implementation.<p>
     */
    private static class Runner {

        /**
         * Adds one configured backend.<p>
         *
         * @param backends the backend map
         * @param properties the OpenCms properties
         * @param id the backend id
         */
        private void addBackend(Map<String, Backend> backends, CmsParameterConfiguration properties, String id) {

            if (CmsStringUtil.isEmptyOrWhitespaceOnly(id) || backends.containsKey(id)) {
                return;
            }
            Backend backend = new Backend();
            backend.m_id = id;
            String prefix = "storage.backend." + id + ".";
            if (I_CmsDbStorage.STORAGE_TYPE.equals(id)) {
                backend.m_type = I_CmsDbStorage.STORAGE_TYPE;
            } else {
                backend.m_type = properties.getString(prefix + "type", null);
            }
            backends.put(backend.m_id, backend);
        }

        /**
         * Deletes one orphan, unless the run is a dry-run.<p>
         *
         * @param storage the storage backend
         * @param dbc the database context
         * @param hash the hash
         * @param commandLine the command line
         * @param stats the stats
         *
         * @throws Exception if deletion fails
         */
        private void deleteOrphan(
            I_CmsStorage storage,
            CmsDbContext dbc,
            String hash,
            CommandLine commandLine,
            OrphanStats stats)
        throws Exception {

            if (!commandLine.m_execute) {
                return;
            }
            if ((commandLine.m_deleteLimit > 0) && (stats.m_deleted >= commandLine.m_deleteLimit)) {
                return;
            }
            storage.deleteContent(dbc, hash);
            stats.m_deleted++;
        }

        /**
         * Reads and hashes one database storage BLOB in bounded chunks.<p>
         *
         * @param connection the JDBC connection
         * @param hash the expected content hash
         * @param digest the message digest
         * @return the number of content bytes
         * @throws SQLException if reading fails
         */
        private long digestDatabaseContentChunks(Connection connection, String hash, MessageDigest digest)
        throws SQLException {

            long contentLength;
            try (PreparedStatement statement = connection.prepareStatement(SQL_DB_CONTENT_LENGTH)) {
                statement.setString(1, hash);
                try (ResultSet rows = statement.executeQuery()) {
                    if (!rows.next()) {
                        throw new SQLException("Database storage content does not exist for hash " + hash + ".");
                    }
                    contentLength = rows.getLong("CONTENT_LENGTH");
                    if (rows.wasNull()) {
                        contentLength = 0;
                    }
                }
            }
            if (contentLength < 0) {
                throw new SQLException("Negative database storage BLOB length reported for hash " + hash + ".");
            }
            long offset = 0;
            try (PreparedStatement statement = connection.prepareStatement(SQL_DB_CONTENT_CHUNK)) {
                while (offset < contentLength) {
                    int expectedLength = (int)Math.min(DB_CONTENT_READ_CHUNK_SIZE, contentLength - offset);
                    statement.setLong(1, offset + 1);
                    statement.setInt(2, expectedLength);
                    statement.setString(3, hash);
                    try (ResultSet rows = statement.executeQuery()) {
                        if (!rows.next()) {
                            throw new SQLException(
                                "Database storage content disappeared while reading hash " + hash + ".");
                        }
                        byte[] chunk = rows.getBytes("CONTENT_CHUNK");
                        if ((chunk == null) || (chunk.length != expectedLength)) {
                            throw new SQLException(
                                "Expected "
                                    + expectedLength
                                    + " database storage BLOB bytes at offset "
                                    + offset
                                    + " for hash "
                                    + hash
                                    + ", but received "
                                    + (chunk == null ? 0 : chunk.length)
                                    + ".");
                        }
                        digest.update(chunk);
                        offset += chunk.length;
                    }
                }
            }
            return contentLength;
        }

        /**
         * Streams and hashes one external storage reference.<p>
         *
         * @param connection the JDBC connection
         * @param storageManager the storage manager
         * @param dbc the database context
         * @param reference the reference
         * @param digest the message digest
         * @param chunkDatabaseContent whether database content should be read in chunks
         * @return the number of content bytes
         * @throws Exception if reading fails
         */
        private long digestStorageReference(
            Connection connection,
            CmsStorageManager storageManager,
            CmsDbContext dbc,
            Reference reference,
            MessageDigest digest,
            boolean chunkDatabaseContent)
        throws Exception {

            if (chunkDatabaseContent && I_CmsDbStorage.STORAGE_TYPE.equals(reference.m_storage)) {
                return digestDatabaseContentChunks(connection, reference.m_hash, digest);
            }
            boolean[] streamOpened = new boolean[1];
            long[] contentBytes = new long[1];
            storageManager.loadContentFrom(dbc, new byte[0], reference.m_storage, reference.m_hash, in -> {
                streamOpened[0] = true;
                byte[] buffer = new byte[VERIFY_BUFFER_SIZE];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    if (read > 0) {
                        digest.update(buffer, 0, read);
                        contentBytes[0] += read;
                    }
                }
            });
            if (!streamOpened[0]) {
                throw new IllegalStateException("Storage backend returned no content stream.");
            }
            return contentBytes[0];
        }

        /**
         * Gets configured storage backend metadata.<p>
         *
         * @param properties the OpenCms properties
         *
         * @return the configured backends keyed by backend id
         */
        private Map<String, Backend> getBackends(CmsParameterConfiguration properties) {

            Map<String, Backend> result = new LinkedHashMap<>();
            String active = properties.getString("storage.active", I_CmsDbStorage.STORAGE_TYPE).trim();
            addBackend(result, properties, active);
            for (String legacy : parseCommaSeparated(properties.getString("storage.legacy", ""))) {
                addBackend(result, properties, legacy);
            }
            return result;
        }

        /**
         * Returns the text for a delete limit.<p>
         *
         * @param deleteLimit the delete limit
         * @return the text
         */
        private String getDeleteLimitText(int deleteLimit) {

            return deleteLimit == 0 ? "unlimited" : String.valueOf(deleteLimit);
        }

        /**
         * Creates the delete stats suffix.<p>
         *
         * @param stats the stats
         * @param delete whether delete mode is active
         * @param commandLine the command line
         * @return the suffix
         */
        private String getDeleteStats(OrphanStats stats, boolean delete, CommandLine commandLine) {

            if (!delete) {
                return "";
            }
            if (!commandLine.m_execute) {
                return ", would delete=" + stats.m_orphans;
            }
            return ", deleted=" + stats.m_deleted + ", delete failures=" + stats.m_deleteFailures;
        }

        /**
         * Returns a short printable description of a failure.<p>
         *
         * @param failure the failure
         * @return the printable description
         */
        private String getFailureSummary(Throwable failure) {

            String message = getPrintableExceptionMessage(failure.getMessage());
            return failure.getClass().getName() + (message == null ? "" : ": " + message);
        }

        /**
         * Returns a short text exception message, or an omission notice for unsafe messages.<p>
         *
         * @param message the exception message
         * @return the printable message, or null
         */
        private String getPrintableExceptionMessage(String message) {

            if (message == null) {
                return null;
            }
            if (message.length() > MAX_PRINTABLE_EXCEPTION_MESSAGE_LENGTH) {
                return "[message omitted because it is unusually large and may contain binary content]";
            }
            for (int i = 0; i < message.length(); i++) {
                char character = message.charAt(i);
                if (Character.isISOControl(character)
                    && (character != '\n')
                    && (character != '\r')
                    && (character != '\t')) {
                    return "[message omitted because it contains binary control characters]";
                }
            }
            return message;
        }

        /**
         * Checks if the delete limit has been reached.<p>
         *
         * @param commandLine the command line
         * @param stats the stats
         * @return true if no more blobs should be deleted
         */
        private boolean isDeleteLimitReached(CommandLine commandLine, OrphanStats stats) {

            return commandLine.m_execute
                && (commandLine.m_deleteLimit > 0)
                && (stats.m_deleted >= commandLine.m_deleteLimit);
        }

        /**
         * Returns whether the current command line may delete storage data.<p>
         *
         * @param commandLine the command line
         * @return true if storage data may be deleted
         */
        private boolean isDeleting(CommandLine commandLine) {

            return MODE_DELETE_ORPHANS.equals(commandLine.m_mode) && commandLine.m_execute;
        }

        /**
         * Checks whether a storage reference still exists in the content tables.<p>
         *
         * @param connection the JDBC connection
         * @param storage the storage id
         * @param hash the hash
         * @return true if a reference exists
         * @throws SQLException if checking fails
         */
        private boolean isReferenced(Connection connection, String storage, String hash) throws SQLException {

            try (PreparedStatement statement = connection.prepareStatement(SQL_REFERENCE_EXISTS)) {
                statement.setString(1, storage);
                statement.setString(2, hash);
                statement.setString(3, storage);
                statement.setString(4, hash);
                try (ResultSet rows = statement.executeQuery()) {
                    return rows.next();
                }
            }
        }

        /**
         * Parses the command line.<p>
         *
         * @param args the command line arguments
         *
         * @return the parsed command line
         */
        private CommandLine parseCommandLine(String[] args) {

            CommandLine result = new CommandLine();
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if ("--help".equals(arg) || "-h".equals(arg)) {
                    result.m_help = true;
                } else if ("--webinf".equals(arg)) {
                    result.m_webInfPath = Paths.get(requireValue(args, ++i, arg));
                } else if ("--properties".equals(arg)) {
                    result.m_propertiesPath = Paths.get(requireValue(args, ++i, arg));
                } else if ("--driver-dir".equals(arg)) {
                    CmsStorageToolSupport.addDriverDirectory(
                        result.m_driverJars,
                        Paths.get(requireValue(args, ++i, arg)));
                } else if ("--driver-jar".equals(arg)) {
                    result.m_driverJars.add(Paths.get(requireValue(args, ++i, arg)));
                } else if ("--batch-size".equals(arg)) {
                    result.m_batchSize = Integer.parseInt(requireValue(args, ++i, arg));
                    if (result.m_batchSize <= 0) {
                        throw new IllegalArgumentException("--batch-size must be greater than zero.");
                    }
                } else if ("--delete-limit".equals(arg)) {
                    result.m_deleteLimit = Integer.parseInt(requireValue(args, ++i, arg));
                    if (result.m_deleteLimit < 0) {
                        throw new IllegalArgumentException("--delete-limit must not be negative.");
                    }
                } else if ("--execute".equals(arg)) {
                    result.m_execute = true;
                } else if ("--mode".equals(arg)) {
                    result.m_mode = requireValue(args, ++i, arg);
                    if (!MODES.contains(result.m_mode)) {
                        throw new IllegalArgumentException("Unsupported mode: " + result.m_mode);
                    }
                } else if ("--sample-limit".equals(arg)) {
                    result.m_sampleLimit = Integer.parseInt(requireValue(args, ++i, arg));
                } else {
                    throw new IllegalArgumentException("Unknown argument: " + arg);
                }
            }
            return result;
        }

        /**
         * Parses a comma-separated list.<p>
         *
         * @param value the raw value
         *
         * @return the parsed values
         */
        private List<String> parseCommaSeparated(String value) {

            List<String> result = new ArrayList<>();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
                return result;
            }
            for (String item : value.split(",")) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty() && !result.contains(trimmed)) {
                    result.add(trimmed);
                }
            }
            return result;
        }

        /**
         * Prints a failure without dumping a potentially binary JDBC message.<p>
         *
         * @param failure the failure
         */
        private void printFailure(Throwable failure) {

            Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
            printFailure(failure, "Storage maintenance failed: ", seen, 0);
        }

        /**
         * Prints one throwable and its nested failures without unsafe message contents.<p>
         *
         * @param failure the failure
         * @param prefix the line prefix
         * @param seen already printed failures
         * @param depth nesting depth
         */
        private void printFailure(Throwable failure, String prefix, Set<Throwable> seen, int depth) {

            if ((failure == null) || !seen.add(failure) || (depth > 16)) {
                return;
            }
            System.err.println(prefix + getFailureSummary(failure));
            if (failure instanceof SQLException) {
                SQLException sqlFailure = (SQLException)failure;
                System.err.println(
                    "  SQLState=" + sqlFailure.getSQLState() + ", errorCode=" + sqlFailure.getErrorCode());
            }
            for (StackTraceElement element : failure.getStackTrace()) {
                System.err.println("\tat " + element);
            }
            for (Throwable suppressed : failure.getSuppressed()) {
                printFailure(suppressed, "Suppressed: ", seen, depth + 1);
            }
            printFailure(failure.getCause(), "Caused by: ", seen, depth + 1);
        }

        /**
         * Prints usage information.<p>
         */
        private void printUsage() {

            System.out.println("OpenCms storage maintenance tool");
            System.out.println();
            System.out.println("Usage:");
            System.out.println("  java -jar opencms-storage-maintenance.jar --webinf /path/to/WEB-INF --mode all");
            System.out.println();
            System.out.println("Options:");
            System.out.println("  --webinf <path>              WEB-INF folder containing config/opencms.properties");
            System.out.println("  --properties <path>          Explicit opencms.properties path");
            System.out.println("  --driver-jar <path>          Additional JDBC driver jar");
            System.out.println("  --driver-dir <path>          Directory with additional JDBC driver jars");
            System.out.println(
                "  --batch-size <number>         CMS_STORAGE orphan page size (default: " + DEFAULT_BATCH_SIZE + ")");
            System.out.println(
                "  --mode <mode>                all, validate-backends, verify-references, scan-orphans, delete-orphans");
            System.out.println("  --execute                    Actually delete orphans in delete-orphans mode");
            System.out.println(
                "  --delete-limit <number>      Maximum number of orphan blobs to delete, 0 for unlimited");
            System.out.println("  --sample-limit <number>      Number of detail lines to print per check");
            System.out.println("  --help                       Show this help");
            System.out.println();
            System.out.println("Deletion is only performed in delete-orphans mode with --execute.");
        }

        /**
         * Reads all distinct external storage references from the content tables.<p>
         *
         * @param connection the JDBC connection
         *
         * @return references keyed by storage id and hash
         *
         * @throws SQLException if reading fails
         */
        private Map<String, Reference> readReferences(Connection connection) throws SQLException {

            Map<String, Reference> result = new LinkedHashMap<>();
            ProgressReporter progress = new ProgressReporter("database references read");
            try (PreparedStatement statement = connection.prepareStatement(SQL_REFERENCES);
            ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    Reference reference = new Reference();
                    reference.m_storage = rows.getString("STORAGE");
                    reference.m_hash = rows.getString("HASH");
                    reference.m_rows = rows.getLong("ROW_COUNT");
                    result.put(referenceKey(reference.m_storage, reference.m_hash), reference);
                    progress.report(result.size(), "");
                }
            }
            return result;
        }

        /**
         * Creates a reference key.<p>
         *
         * @param storage the storage id
         * @param hash the hash
         *
         * @return the key
         */
        private String referenceKey(String storage, String hash) {

            return storage + "\n" + hash;
        }

        /**
         * Requires an argument value.<p>
         *
         * @param args the arguments
         * @param index the value index
         * @param option the option name
         *
         * @return the value
         */
        private String requireValue(String[] args, int index, String option) {

            if (index >= args.length) {
                throw new IllegalArgumentException("Missing value for " + option);
            }
            return args[index];
        }

        /**
         * Resolves default paths.<p>
         *
         * @param commandLine the command line
         */
        private void resolveDefaults(CommandLine commandLine) {

            if (commandLine.m_webInfPath == null) {
                if (commandLine.m_propertiesPath == null) {
                    throw new IllegalArgumentException("Either --webinf or --properties must be configured.");
                }
                commandLine.m_webInfPath = commandLine.m_propertiesPath.getParent().getParent();
            }
            CmsStorageToolSupport.addDriverDirectory(commandLine.m_driverJars, commandLine.m_webInfPath.resolve("lib"));
            if (commandLine.m_propertiesPath == null) {
                commandLine.m_propertiesPath = commandLine.m_webInfPath.resolve(DEFAULT_PROPERTIES);
            }
        }

        /**
         * Runs the tool.<p>
         *
         * @param args command line arguments
         *
         * @return the process exit code
         */
        private int run(String[] args) {

            try {
                CommandLine commandLine = parseCommandLine(args);
                if (commandLine.m_help) {
                    printUsage();
                    return 0;
                }
                CmsStorageToolSupport.initializeOpenCmsLogging();
                resolveDefaults(commandLine);

                CmsParameterConfiguration properties = new CmsParameterConfiguration(
                    commandLine.m_propertiesPath.toString());
                CmsImageCacheFactory.validateConfiguration(properties);
                org.opencms.db.generic.CmsSqlManager sqlManager = CmsStorageToolSupport.createSqlManager(properties);
                CmsStorageToolSupport.DatabaseConfiguration database = CmsStorageToolSupport.readDatabaseConfiguration(
                    properties,
                    commandLine.m_driverJars);
                CmsStorageToolSupport.ToolSqlManager toolSqlManager = new CmsStorageToolSupport.ToolSqlManager(
                    sqlManager,
                    () -> CmsStorageToolSupport.openConnection(database));
                CmsStorageManager storageManager = new CmsStorageManager(toolSqlManager, properties);
                Map<String, Backend> backends = getBackends(properties);

                System.out.println("OpenCms storage maintenance tool");
                System.out.println("Mode              : " + commandLine.m_mode);
                System.out.println("WEB-INF           : " + commandLine.m_webInfPath);
                System.out.println("Properties        : " + commandLine.m_propertiesPath);
                System.out.println("JDBC URL          : " + database.m_jdbcUrl);
                System.out.println("Configured storage: " + CmsStringUtil.collectionAsString(backends.keySet(), ", "));
                System.out.println("Read-only         : " + !isDeleting(commandLine));
                if (MODE_ALL.equals(commandLine.m_mode)
                    || MODE_SCAN_ORPHANS.equals(commandLine.m_mode)
                    || MODE_DELETE_ORPHANS.equals(commandLine.m_mode)) {
                    System.out.println("Batch size        : " + commandLine.m_batchSize);
                }
                if (MODE_DELETE_ORPHANS.equals(commandLine.m_mode)) {
                    System.out.println("Delete limit      : " + getDeleteLimitText(commandLine.m_deleteLimit));
                    System.out.println("Execute deletes   : " + commandLine.m_execute);
                }

                int result = 0;
                try (Connection connection = CmsStorageToolSupport.openConnection(database)) {
                    toolSqlManager.setCurrentConnection(connection);
                    try {
                        if (MODE_ALL.equals(commandLine.m_mode) || MODE_VALIDATE_BACKENDS.equals(commandLine.m_mode)) {
                            validateBackends(storageManager);
                        }
                        if (MODE_ALL.equals(commandLine.m_mode) || MODE_VERIFY_REFERENCES.equals(commandLine.m_mode)) {
                            if (verifyReferences(
                                connection,
                                storageManager,
                                commandLine.m_sampleLimit).m_failedReferences > 0) {
                                result = 2;
                            }
                        }
                        if (MODE_ALL.equals(commandLine.m_mode) || MODE_SCAN_ORPHANS.equals(commandLine.m_mode)) {
                            scanOrphans(connection, storageManager, backends, commandLine, false);
                        }
                        if (MODE_DELETE_ORPHANS.equals(commandLine.m_mode)) {
                            scanOrphans(connection, storageManager, backends, commandLine, true);
                        }
                    } finally {
                        toolSqlManager.clearCurrentConnection();
                    }
                } finally {
                    storageManager.close();
                }
                return result;
            } catch (Exception e) {
                printFailure(e);
                return 1;
            }
        }

        /**
         * Scans CMS_STORAGE for unreferenced database blobs.<p>
         *
         * @param connection the JDBC connection
         * @param storageManager the storage manager
         * @param commandLine the command line
         * @param delete whether delete mode is active
         * @param sampleLimit the maximum number of details
         *
         * @return the orphan stats
         *
         * @throws Exception if scanning fails
         */
        private OrphanStats scanDatabaseOrphans(
            Connection connection,
            CmsStorageManager storageManager,
            CommandLine commandLine,
            boolean delete,
            int sampleLimit)
        throws Exception {

            OrphanStats stats = new OrphanStats();
            List<String> samples = new ArrayList<>();
            I_CmsStorage storage = storageManager.getStorage(I_CmsDbStorage.STORAGE_TYPE);
            CmsDbContext dbc = new CmsDbContext();
            ProgressReporter progress = new ProgressReporter("db orphan candidates scanned");
            String lastHash = "";
            try (PreparedStatement statement = connection.prepareStatement(SQL_DB_ORPHANS_PAGE)) {
                statement.setMaxRows(commandLine.m_batchSize);
                while (true) {
                    List<String> page = new ArrayList<>(commandLine.m_batchSize);
                    statement.setString(1, lastHash);
                    try (ResultSet rows = statement.executeQuery()) {
                        while (rows.next()) {
                            page.add(rows.getString("HASH"));
                        }
                    }
                    if (page.isEmpty()) {
                        break;
                    }
                    for (String hash : page) {
                        lastHash = hash;
                        stats.m_orphans++;
                        if (samples.size() < sampleLimit) {
                            samples.add(hash);
                        }
                        if (delete && (storage != null) && !isDeleteLimitReached(commandLine, stats)) {
                            if (!isReferenced(connection, I_CmsDbStorage.STORAGE_TYPE, hash)) {
                                try {
                                    deleteOrphan(storage, dbc, hash, commandLine, stats);
                                } catch (Exception e) {
                                    stats.m_deleteFailures++;
                                    if (samples.size() < sampleLimit) {
                                        samples.add(hash + " : delete failed: " + getFailureSummary(e));
                                    }
                                }
                            }
                        }
                        progress.report(
                            stats.m_orphans,
                            ", deleted=" + stats.m_deleted + ", delete failures=" + stats.m_deleteFailures);
                    }
                    if (page.size() < commandLine.m_batchSize) {
                        break;
                    }
                }
            }
            System.out.println("  db: orphaned blobs=" + stats.m_orphans + getDeleteStats(stats, delete, commandLine));
            for (String sample : samples) {
                System.out.println("    " + sample);
            }
            return stats;
        }

        /**
         * Scans one enumerable backend for unreferenced blobs.<p>
         *
         * @param storage the storage backend
         * @param connection the JDBC connection
         * @param references the database references
         * @param commandLine the command line
         * @param delete whether delete mode is active
         * @param sampleLimit the maximum number of details
         *
         * @return the orphan stats
         *
         * @throws Exception if scanning fails
         */
        private OrphanStats scanEnumerableStorageOrphans(
            I_CmsEnumerableStorage storage,
            Connection connection,
            Map<String, Reference> references,
            CommandLine commandLine,
            boolean delete,
            int sampleLimit)
        throws Exception {

            String storageIdentifier = storage.getStorageIdentifier();
            OrphanStats stats = new OrphanStats();
            List<String> samples = new ArrayList<>();
            CmsDbContext dbc = new CmsDbContext();
            ProgressReporter progress = new ProgressReporter(storageIdentifier + " stored blobs scanned");
            I_CmsEnumerableStorage.I_CmsContentHashVisitor visitor = hash -> {
                stats.m_stored++;
                if (!references.containsKey(referenceKey(storageIdentifier, hash))) {
                    stats.m_orphans++;
                    if (samples.size() < sampleLimit) {
                        samples.add(hash);
                    }
                    if (delete
                        && !isDeleteLimitReached(commandLine, stats)
                        && !isReferenced(connection, storageIdentifier, hash)) {
                        try {
                            deleteOrphan(storage, dbc, hash, commandLine, stats);
                        } catch (Exception e) {
                            stats.m_deleteFailures++;
                            if (samples.size() < sampleLimit) {
                                samples.add(hash + " : delete failed: " + getFailureSummary(e));
                            }
                        }
                    }
                }
                progress.report(
                    stats.m_stored,
                    ", orphaned="
                        + stats.m_orphans
                        + ", deleted="
                        + stats.m_deleted
                        + ", delete failures="
                        + stats.m_deleteFailures);
            };
            storage.visitContentHashes(new CmsDbContext(), visitor);
            System.out.println(
                "  "
                    + storageIdentifier
                    + ": stored blobs="
                    + stats.m_stored
                    + ", orphaned blobs="
                    + stats.m_orphans
                    + getDeleteStats(stats, delete, commandLine));
            for (String sample : samples) {
                System.out.println("    " + sample);
            }
            return stats;
        }

        /**
         * Scans configured backends for blobs without DB references.<p>
         *
         * @param connection the JDBC connection
         * @param storageManager the storage manager
         * @param backends the configured backends
         * @param commandLine the command line
         * @param delete whether delete mode is active
         *
         * @throws Exception if scanning fails
         */
        private void scanOrphans(
            Connection connection,
            CmsStorageManager storageManager,
            Map<String, Backend> backends,
            CommandLine commandLine,
            boolean delete)
        throws Exception {

            Map<String, Reference> references = readReferences(connection);
            System.out.println();
            System.out.println(delete ? "Deleting orphaned blobs..." : "Scanning orphaned blobs...");
            for (Backend backend : backends.values()) {
                if (I_CmsDbStorage.STORAGE_TYPE.equals(backend.m_type)) {
                    scanDatabaseOrphans(connection, storageManager, commandLine, delete, commandLine.m_sampleLimit);
                } else {
                    I_CmsStorage storage = storageManager.getStorage(backend.m_id);
                    if (storage instanceof I_CmsEnumerableStorage) {
                        scanEnumerableStorageOrphans(
                            (I_CmsEnumerableStorage)storage,
                            connection,
                            references,
                            commandLine,
                            delete,
                            commandLine.m_sampleLimit);
                    } else {
                        System.out.println(
                            "  "
                                + backend.m_id
                                + ": skipped, backend enumeration is not available for type "
                                + backend.m_type);
                    }
                }
            }
        }

        /**
         * Converts bytes to a lower-case hexadecimal string.<p>
         *
         * @param bytes the bytes
         * @return the hexadecimal string
         */
        private String toHex(byte[] bytes) {

            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                String hex = Integer.toHexString(value & 0xff);
                if (hex.length() == 1) {
                    result.append('0');
                }
                result.append(hex);
            }
            return result.toString();
        }

        /**
         * Returns true if database BLOBs should be read in bounded chunks.<p>
         *
         * @param connection the JDBC connection
         * @return true for MySQL and MariaDB
         * @throws SQLException if database metadata can not be read
         */
        private boolean useChunkedDatabaseContentReads(Connection connection) throws SQLException {

            String productName = connection.getMetaData().getDatabaseProductName();
            return (productName != null)
                && (productName.toLowerCase(Locale.ROOT).contains("mysql")
                    || productName.toLowerCase(Locale.ROOT).contains("mariadb"));
        }

        /**
         * Validates configured storage backends.<p>
         *
         * @param storageManager the storage manager
         *
         * @throws Exception if validation fails
         */
        private void validateBackends(CmsStorageManager storageManager) throws Exception {

            System.out.println();
            System.out.println("Validating configured storage backends...");
            storageManager.validateStorages(new CmsDbContext());
            System.out.println("  ok");
        }

        /**
         * Verifies that all DB storage references can be loaded.<p>
         *
         * @param connection the JDBC connection
         * @param storageManager the storage manager
         * @param sampleLimit the maximum number of details
         *
         * @return the verification stats
         *
         * @throws Exception if scanning fails
         */
        private VerificationStats verifyReferences(
            Connection connection,
            CmsStorageManager storageManager,
            int sampleLimit)
        throws Exception {

            System.out.println();
            System.out.println("Verifying database storage references...");
            VerificationStats stats = new VerificationStats();
            List<String> samples = new ArrayList<>();
            CmsDbContext dbc = new CmsDbContext();
            ProgressReporter progress = new ProgressReporter("references verified");
            boolean chunkDatabaseContent = useChunkedDatabaseContentReads(connection);
            for (Reference reference : readReferences(connection).values()) {
                stats.m_references++;
                stats.m_referencingRows += reference.m_rows;
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-512");
                    long contentBytes = digestStorageReference(
                        connection,
                        storageManager,
                        dbc,
                        reference,
                        digest,
                        chunkDatabaseContent);
                    String actualHash = toHex(digest.digest());
                    if (!reference.m_hash.equalsIgnoreCase(actualHash)) {
                        throw new IllegalStateException(
                            "Content hash mismatch: expected " + reference.m_hash + ", actual " + actualHash);
                    }
                    stats.m_verifiedReferences++;
                    stats.m_verifiedBytes += contentBytes;
                } catch (Exception e) {
                    stats.m_failedReferences++;
                    if (samples.size() < sampleLimit) {
                        samples.add(reference.m_storage + " " + reference.m_hash + " : " + getFailureSummary(e));
                    }
                }
                progress.report(
                    stats.m_references,
                    ", verified="
                        + stats.m_verifiedReferences
                        + ", failed="
                        + stats.m_failedReferences
                        + ", bytes="
                        + stats.m_verifiedBytes);
            }
            System.out.println("  distinct references : " + stats.m_references);
            System.out.println("  referencing rows    : " + stats.m_referencingRows);
            System.out.println("  verified references : " + stats.m_verifiedReferences);
            System.out.println("  verified bytes      : " + stats.m_verifiedBytes);
            System.out.println("  failed references   : " + stats.m_failedReferences);
            for (String sample : samples) {
                System.out.println("    " + sample);
            }
            return stats;
        }
    }

    /**
     * Reference verification stats.<p>
     */
    private static class VerificationStats {

        /** Distinct references which could not be loaded or verified. */
        private long m_failedReferences;

        /** Distinct references. */
        private long m_references;

        /** Referencing content rows. */
        private long m_referencingRows;

        /** Distinct references which could be loaded. */
        private long m_verifiedReferences;

        /** Bytes read while verifying references. */
        private long m_verifiedBytes;
    }

    /** Default page size for database orphan candidates. */
    private static final int DEFAULT_BATCH_SIZE = 100;

    /** Chunk size for reading database storage BLOBs from MySQL/MariaDB. */
    private static final int DB_CONTENT_READ_CHUNK_SIZE = 4 * 1024 * 1024;

    /** The default properties path below WEB-INF. */
    private static final String DEFAULT_PROPERTIES = "config/opencms.properties";

    /** Maximum exception message length which is safe to print. */
    private static final int MAX_PRINTABLE_EXCEPTION_MESSAGE_LENGTH = 1000;

    /** Maintenance mode for all read-only checks. */
    private static final String MODE_ALL = "all";

    /** Maintenance mode for deleting orphaned blobs. */
    private static final String MODE_DELETE_ORPHANS = "delete-orphans";

    /** Maintenance mode for orphan scanning. */
    private static final String MODE_SCAN_ORPHANS = "scan-orphans";

    /** Maintenance mode for backend validation. */
    private static final String MODE_VALIDATE_BACKENDS = "validate-backends";

    /** Maintenance mode for reference verification. */
    private static final String MODE_VERIFY_REFERENCES = "verify-references";

    /** Supported maintenance modes. */
    private static final Set<String> MODES = new LinkedHashSet<>(
        Arrays.asList(
            MODE_ALL,
            MODE_VALIDATE_BACKENDS,
            MODE_VERIFY_REFERENCES,
            MODE_SCAN_ORPHANS,
            MODE_DELETE_ORPHANS));

    /** Number of processed items between progress output. */
    private static final int PROGRESS_ITEM_INTERVAL = 1000;

    /** Maximum time between progress output. */
    private static final long PROGRESS_TIME_INTERVAL_NANOS = 30L * 1000L * 1000L * 1000L;

    /** SQL page for database storage blobs without content table references. */
    private static final String SQL_DB_ORPHANS_PAGE = "SELECT S.HASH FROM CMS_STORAGE S "
        + "WHERE S.HASH>? "
        + "AND NOT EXISTS (SELECT 1 FROM CMS_OFFLINE_CONTENTS C WHERE C.STORAGE='db' AND C.HASH=S.HASH) "
        + "AND NOT EXISTS (SELECT 1 FROM CMS_CONTENTS C WHERE C.STORAGE='db' AND C.HASH=S.HASH) "
        + "ORDER BY S.HASH";

    /** SQL for the logical size of one database storage BLOB. */
    private static final String SQL_DB_CONTENT_LENGTH = "SELECT OCTET_LENGTH(FILE_CONTENT) AS CONTENT_LENGTH "
        + "FROM CMS_STORAGE WHERE HASH=?";

    /** SQL for one bounded database storage BLOB chunk. */
    private static final String SQL_DB_CONTENT_CHUNK = "SELECT SUBSTRING(FILE_CONTENT, ?, ?) AS CONTENT_CHUNK "
        + "FROM CMS_STORAGE WHERE HASH=?";

    /** Buffer size used while streaming storage content. */
    private static final int VERIFY_BUFFER_SIZE = 64 * 1024;

    /** SQL for all external storage references in content tables. */
    private static final String SQL_REFERENCES = "SELECT STORAGE, HASH, SUM(ROW_COUNT) AS ROW_COUNT FROM ("
        + "SELECT STORAGE, HASH, COUNT(*) AS ROW_COUNT FROM CMS_OFFLINE_CONTENTS "
        + "WHERE STORAGE IS NOT NULL AND HASH IS NOT NULL GROUP BY STORAGE, HASH "
        + "UNION ALL "
        + "SELECT STORAGE, HASH, COUNT(*) AS ROW_COUNT FROM CMS_CONTENTS "
        + "WHERE STORAGE IS NOT NULL AND HASH IS NOT NULL GROUP BY STORAGE, HASH"
        + ") X GROUP BY STORAGE, HASH";

    /** SQL for checking whether a storage reference still exists. */
    private static final String SQL_REFERENCE_EXISTS = "SELECT 1 FROM CMS_OFFLINE_CONTENTS WHERE STORAGE=? AND HASH=? "
        + "UNION SELECT 1 FROM CMS_CONTENTS WHERE STORAGE=? AND HASH=?";

    /**
     * Hidden constructor.<p>
     */
    private CmsStorageMaintenanceTool() {

        // utility class
    }

    /**
     * Main entry point.<p>
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        int exitCode = run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    /**
     * Runs the tool and returns the exit code without terminating the JVM.<p>
     *
     * @param args the command line arguments
     *
     * @return the process exit code
     */
    static int run(String[] args) {

        return new Runner().run(args);
    }
}
