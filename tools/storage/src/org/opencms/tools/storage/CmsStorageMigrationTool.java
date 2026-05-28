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
 * For further information about Alkacon Software, please see the
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
import org.opencms.configuration.CmsStoragePolicyConfiguration;
import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsResourceState;
import org.opencms.db.storage.CmsStorageManager;
import org.opencms.db.storage.CmsStorageManager.StorageResult;
import org.opencms.db.storage.policy.CmsStoragePolicyContext;
import org.opencms.db.storage.policy.I_CmsStoragePolicy;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Command line entry point for the OpenCms storage migration tool.<p>
 */
public final class CmsStorageMigrationTool {

    /**
     * A candidate row read from the database.<p>
     */
    private static class Candidate {

        /** The binary content. */
        private byte[] m_content;

        /** The content date. */
        private long m_dateContent;

        /** The current content hash. */
        private String m_hash;

        /** The publish tag from value. */
        private int m_publishTagFrom;

        /** The resource id. */
        private CmsUUID m_resourceId;

        /** The current storage id. */
        private String m_storage;

        /** The resource type id. */
        private int m_typeId;
    }

    /**
     * Tool runner implementation.<p>
     */
    private static class CmsStorageMigrationToolRunner {

        /**
         * Adds default storage policy resource type ids from the VFS configuration.<p>
         *
         * @param configuration the storage policy configuration
         * @param root the VFS configuration root element
         */
        private void configureDefaultStoragePolicyTypeIds(CmsStoragePolicyConfiguration configuration, Element root) {

            if (!org.opencms.db.storage.policy.CmsDefaultStoragePolicy.class.getName().equals(
                getStoragePolicyName(configuration))) {
                return;
            }
            CmsParameterConfiguration parameters = configuration.getConfiguration();
            String key = org.opencms.db.storage.policy.CmsDefaultStoragePolicy.PARAM_RESOURCE_TYPE_IDS;
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(parameters.getString(key, null))) {
                return;
            }
            Set<String> typeIds = new LinkedHashSet<>();
            NodeList types = root.getElementsByTagName("type");
            for (int i = 0; i < types.getLength(); i++) {
                Element type = (Element)types.item(i);
                String className = type.getAttribute("class");
                if (org.opencms.file.types.CmsResourceTypeBinary.class.getName().equals(className)
                    || org.opencms.file.types.CmsResourceTypeImage.class.getName().equals(className)) {
                    typeIds.add(type.getAttribute("id"));
                }
            }
            if (!typeIds.isEmpty()) {
                configuration.addConfigurationParameter(key, CmsStringUtil.collectionAsString(typeIds, ","));
            }
        }

        /**
         * Creates the query plan for candidate scanning.<p>
         *
         * @param configuration the policy configuration
         * @return the query plan
         */
        private QueryPlan createQueryPlan(CmsStoragePolicyConfiguration configuration) {

            if (configuration == null) {
                return QueryPlan.generic();
            }
            String policyClass = getStoragePolicyName(configuration);
            if (org.opencms.db.storage.policy.CmsNoExternalStoragePolicy.class.getName().equals(policyClass)) {
                return QueryPlan.noExternal();
            }
            if (org.opencms.db.storage.policy.CmsDefaultStoragePolicy.class.getName().equals(policyClass)) {
                CmsParameterConfiguration parameters = configuration.getConfiguration();
                int threshold = parameters.getInteger(
                    org.opencms.db.storage.policy.CmsDefaultStoragePolicy.PARAM_THRESHOLD,
                    org.opencms.db.storage.policy.CmsDefaultStoragePolicy.DEFAULT_THRESHOLD);
                List<Integer> typeIds = parseIntegerList(
                    parameters.getString(
                        org.opencms.db.storage.policy.CmsDefaultStoragePolicy.PARAM_RESOURCE_TYPE_IDS,
                        ""));
                return QueryPlan.defaultPolicy(typeIds, threshold);
            }
            return QueryPlan.generic();
        }

        /**
         * Creates a synthetic resource for policy evaluation.<p>
         *
         * @param candidate the candidate
         * @return the resource
         */
        private CmsResource createResource(Candidate candidate) {

            CmsUUID nullId = CmsUUID.getNullUUID();
            return new CmsResource(
                nullId,
                candidate.m_resourceId,
                "/",
                candidate.m_typeId,
                false,
                0,
                nullId,
                CmsResourceState.STATE_UNCHANGED,
                0,
                nullId,
                0,
                nullId,
                CmsResource.DATE_RELEASED_DEFAULT,
                CmsResource.DATE_EXPIRED_DEFAULT,
                1,
                candidate.m_content.length,
                candidate.m_dateContent,
                1);
        }

        /**
         * Creates the storage policy instance.<p>
         *
         * @param configuration the policy configuration
         * @return the policy
         * @throws Exception if creating the policy fails
         */
        private I_CmsStoragePolicy createStoragePolicy(CmsStoragePolicyConfiguration configuration) throws Exception {

            String className = getStoragePolicyName(configuration);
            Object instance = Class.forName(className).newInstance();
            if (!(instance instanceof I_CmsStoragePolicy)) {
                throw new IllegalArgumentException(className + " does not implement " + I_CmsStoragePolicy.class);
            }
            I_CmsStoragePolicy result = (I_CmsStoragePolicy)instance;
            if (result instanceof I_CmsConfigurationParameterHandler) {
                I_CmsConfigurationParameterHandler handler = (I_CmsConfigurationParameterHandler)result;
                if (configuration != null) {
                    CmsParameterConfiguration parameters = configuration.getConfiguration();
                    for (String key : parameters.keySet()) {
                        handler.addConfigurationParameter(key, parameters.get(key));
                    }
                }
                handler.initConfiguration();
            }
            return result;
        }

        /**
         * Gets the storage policy class name.<p>
         *
         * @param configuration the policy configuration
         * @return the storage policy class name
         */
        private String getStoragePolicyName(CmsStoragePolicyConfiguration configuration) {

            if ((configuration != null) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(configuration.getClassName())) {
                return configuration.getClassName();
            }
            return org.opencms.db.storage.policy.CmsDefaultStoragePolicy.class.getName();
        }

        /**
         * Checks if a table has an index with the given leading column.<p>
         *
         * @param connection the JDBC connection
         * @param table the table name
         * @param column the leading column name
         * @return true if such an index exists
         * @throws SQLException if reading metadata fails
         */
        private boolean hasLeadingIndexColumn(Connection connection, String table, String column) throws SQLException {

            String catalog = connection.getCatalog();
            if (hasLeadingIndexColumn(connection, catalog, table, column)) {
                return true;
            }
            if (hasLeadingIndexColumn(connection, null, table, column)) {
                return true;
            }
            return hasLeadingIndexColumn(connection, null, table.toLowerCase(), column);
        }

        /**
         * Checks if a table has an index with the given leading column.<p>
         *
         * @param connection the JDBC connection
         * @param catalog the catalog
         * @param table the table name
         * @param column the leading column name
         * @return true if such an index exists
         * @throws SQLException if reading metadata fails
         */
        private boolean hasLeadingIndexColumn(Connection connection, String catalog, String table, String column)
        throws SQLException {

            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet indexes = metaData.getIndexInfo(catalog, null, table, false, false)) {
                while (indexes.next()) {
                    if ((indexes.getShort("ORDINAL_POSITION") == 1)
                        && column.equalsIgnoreCase(indexes.getString("COLUMN_NAME"))) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Checks if the candidate storage state needs to be migrated.<p>
         *
         * @param candidate the candidate
         * @param externalStorageRequired true if the policy requires external storage
         * @param activeStorageId the active storage id
         * @return true if the candidate needs migration
         */
        private boolean isMigrationRequired(
            Candidate candidate,
            boolean externalStorageRequired,
            String activeStorageId) {

            boolean isExternal = CmsStringUtil.isNotEmpty(candidate.m_storage)
                && CmsStringUtil.isNotEmpty(candidate.m_hash);
            if (externalStorageRequired) {
                return !isExternal || !activeStorageId.equals(candidate.m_storage);
            }
            return isExternal;
        }

        /**
         * Loads the effective content bytes for a candidate.<p>
         *
         * @param storageManager the storage manager
         * @param dbc the database context
         * @param candidate the candidate
         * @return the content bytes
         * @throws Exception if loading fails
         */
        private byte[] loadContent(CmsStorageManager storageManager, CmsDbContext dbc, Candidate candidate)
        throws Exception {

            byte[] result = storageManager.loadContent(dbc, candidate.m_content, candidate.m_storage, candidate.m_hash);
            if (result == null) {
                return new byte[0];
            }
            return result;
        }

        /**
         * Performs the migration for one scope.<p>
         *
         * @param connection the JDBC connection
         * @param toolSqlManager the tool SQL manager
         * @param storageManager the storage manager
         * @param storagePolicy the storage policy
         * @param scope the migration scope
         * @param batchSize the batch size
         * @return the migration stats
         * @throws Exception if the migration fails
         */
        private ScanStats migrateScope(
            Connection connection,
            CmsStorageToolSupport.ToolSqlManager toolSqlManager,
            CmsStorageManager storageManager,
            I_CmsStoragePolicy storagePolicy,
            Scope scope,
            QueryPlan queryPlan,
            String activeStorageId,
            int batchSize)
        throws Exception {

            ScanStats result = new ScanStats();
            CmsDbContext dbc = new CmsDbContext();
            System.out.println();
            System.out.println("Migrating " + scope.m_label + "...");
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            toolSqlManager.setCurrentConnection(connection);
            long pendingWrites = 0;
            try (PreparedStatement selectStatement = connection.prepareStatement(scope.createSelectSql(queryPlan));
            PreparedStatement updateStatement = connection.prepareStatement(scope.m_updateSql)) {
                queryPlan.bind(selectStatement);
                selectStatement.setFetchSize(Math.max(1, batchSize));
                try (ResultSet rows = selectStatement.executeQuery()) {
                    while (rows.next()) {
                        Candidate candidate = readCandidate(rows);
                        candidate.m_content = loadContent(storageManager, dbc, candidate);
                        result.m_scannedRows += 1;
                        result.m_scannedBytes += candidate.m_content.length;
                        CmsStoragePolicyContext context = new CmsStoragePolicyContext(
                            candidate.m_content,
                            createResource(candidate));
                        boolean externalStorageRequired = storagePolicy.isExternalStorageRequired(context);
                        if (!isMigrationRequired(candidate, externalStorageRequired, activeStorageId)) {
                            result.m_skippedRows += 1;
                            continue;
                        }
                        result.m_migrationRows += 1;
                        result.m_migrationBytes += candidate.m_content.length;
                        if (externalStorageRequired && CmsStringUtil.isNotEmpty(candidate.m_storage)) {
                            result.m_legacyRows += 1;
                        } else if (!externalStorageRequired) {
                            result.m_returnRows += 1;
                        }
                        StorageResult storageResult = storageManager.prepareContent(dbc, context);
                        int updatedRows = updateCandidate(updateStatement, scope, candidate, storageResult);
                        if (updatedRows == 1) {
                            result.m_migratedRows += 1;
                            pendingWrites += 1;
                        } else {
                            result.m_staleRows += 1;
                        }
                        if (pendingWrites >= Math.max(1, batchSize)) {
                            connection.commit();
                            pendingWrites = 0;
                            System.out.println(
                                "  committed " + result.m_migratedRows + " rows for " + scope.m_label + "...");
                        }
                    }
                }
                connection.commit();
                return result;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                toolSqlManager.clearCurrentConnection();
                connection.setAutoCommit(originalAutoCommit);
            }
        }

        /**
         * Parses the command line.<p>
         *
         * @param args the command line arguments
         * @return the parsed command line
         */
        private CommandLine parseCommandLine(String[] args) {

            CommandLine result = new CommandLine();
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if ("--help".equals(arg) || "-h".equals(arg)) {
                    result.m_help = true;
                } else if ("--dry-run".equals(arg)) {
                    result.m_dryRun = true;
                } else if ("--execute".equals(arg)) {
                    result.m_dryRun = false;
                } else if ("--no-validate-storage".equals(arg)) {
                    result.m_validateStorage = false;
                } else if ("--verify".equals(arg)) {
                    result.m_verify = true;
                } else if ("--webinf".equals(arg)) {
                    result.m_webInfPath = Paths.get(requireValue(args, ++i, arg));
                } else if ("--properties".equals(arg)) {
                    result.m_propertiesPath = Paths.get(requireValue(args, ++i, arg));
                } else if ("--vfs-config".equals(arg)) {
                    result.m_vfsConfigPath = Paths.get(requireValue(args, ++i, arg));
                } else if ("--batch-size".equals(arg)) {
                    result.m_batchSize = Integer.parseInt(requireValue(args, ++i, arg));
                } else if ("--driver-dir".equals(arg)) {
                    CmsStorageToolSupport.addDriverDirectory(
                        result.m_driverJars,
                        Paths.get(requireValue(args, ++i, arg)));
                } else if ("--driver-jar".equals(arg)) {
                    result.m_driverJars.add(Paths.get(requireValue(args, ++i, arg)));
                } else if ("--tables".equals(arg)) {
                    result.m_tables = parseTables(requireValue(args, ++i, arg));
                } else {
                    throw new IllegalArgumentException("Unknown argument: " + arg);
                }
            }
            return result;
        }

        /**
         * Parses a comma separated integer list.<p>
         *
         * @param value the comma separated value
         * @return the integer list
         */
        private List<Integer> parseIntegerList(String value) {

            if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
                return Collections.emptyList();
            }
            List<Integer> result = new ArrayList<>();
            for (String token : CmsStringUtil.splitAsList(value, ',', true)) {
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(token)) {
                    result.add(Integer.valueOf(token));
                }
            }
            return result;
        }

        /**
         * Parses the table option.<p>
         *
         * @param value the option value
         * @return the selected tables
         */
        private List<String> parseTables(String value) {

            List<String> result = new ArrayList<>();
            for (String token : value.split(",")) {
                String table = token.trim();
                if ("offline".equals(table) || "contents".equals(table)) {
                    result.add(table);
                } else {
                    throw new IllegalArgumentException("Unsupported table selector: " + table);
                }
            }
            if (result.isEmpty()) {
                throw new IllegalArgumentException("At least one table selector is required.");
            }
            return result;
        }

        /**
         * Prints scan stats.<p>
         *
         * @param label the label
         * @param stats the stats
         */
        private void printStats(String label, ScanStats stats) {

            System.out.println();
            System.out.println(label);
            System.out.println("  scanned rows     : " + stats.m_scannedRows);
            System.out.println("  scanned bytes    : " + stats.m_scannedBytes);
            System.out.println("  migration rows   : " + stats.m_migrationRows);
            System.out.println("  migration bytes  : " + stats.m_migrationBytes);
            if (stats.m_legacyRows > 0) {
                System.out.println("  legacy rows      : " + stats.m_legacyRows);
            }
            if (stats.m_returnRows > 0) {
                System.out.println("  return rows      : " + stats.m_returnRows);
            }
            if (stats.m_migratedRows > 0) {
                System.out.println("  migrated rows    : " + stats.m_migratedRows);
            }
            if (stats.m_staleRows > 0) {
                System.out.println("  unchanged rows   : " + stats.m_staleRows);
            }
            System.out.println("  skipped rows     : " + stats.m_skippedRows);
        }

        /**
         * Prints usage information.<p>
         */
        private void printUsage() {

            System.out.println("OpenCms storage migration tool");
            System.out.println();
            System.out.println("Usage:");
            System.out.println("  java -jar opencms-storage-migration.jar --webinf /path/to/WEB-INF --dry-run");
            System.out.println(
                "  java -jar opencms-storage-migration.jar --webinf /path/to/WEB-INF --execute --verify");
            System.out.println();
            System.out.println("Options:");
            System.out.println("  --webinf <path>              WEB-INF folder containing config/opencms.properties");
            System.out.println("  --properties <path>          Explicit opencms.properties path");
            System.out.println("  --vfs-config <path>          Explicit opencms-vfs.xml path");
            System.out.println("  --driver-jar <path>          Additional JDBC driver jar");
            System.out.println("  --driver-dir <path>          Directory with additional JDBC driver jars");
            System.out.println("  --tables <list>              offline, contents, or offline,contents");
            System.out.println("  --batch-size <number>        JDBC fetch size and commit interval");
            System.out.println("  --dry-run                    Analyze only; this is the default");
            System.out.println(
                "  --execute                    Write blobs to the active backend and update content tables");
            System.out.println("  --verify                     Re-scan after migration and fail if candidates remain");
            System.out.println("  --no-validate-storage        Skip storage backend health checks");
            System.out.println("  --help                       Show this help");
        }

        /**
         * Processes one scope according to the selected mode.<p>
         *
         * @param connection the JDBC connection
         * @param toolSqlManager the tool SQL manager
         * @param storageManager the storage manager
         * @param storagePolicy the storage policy
         * @param scope the scope
         * @param commandLine the command line
         * @return the stats
         * @throws Exception if processing fails
         */
        private ScanStats processScope(
            Connection connection,
            CmsStorageToolSupport.ToolSqlManager toolSqlManager,
            CmsStorageManager storageManager,
            I_CmsStoragePolicy storagePolicy,
            Scope scope,
            QueryPlan queryPlan,
            CommandLine commandLine)
        throws Exception {

            if (!commandLine.m_tables.contains(scope.m_tableSelector)) {
                return new ScanStats();
            }
            ScanStats stats = commandLine.m_dryRun
            ? scanScope(
                connection,
                toolSqlManager,
                storageManager,
                storagePolicy,
                scope,
                queryPlan,
                commandLine.m_activeStorageId,
                commandLine.m_batchSize)
            : migrateScope(
                connection,
                toolSqlManager,
                storageManager,
                storagePolicy,
                scope,
                queryPlan,
                commandLine.m_activeStorageId,
                commandLine.m_batchSize);
            printStats(scope.m_label, stats);
            return stats;
        }

        /**
         * Reads a candidate.<p>
         *
         * @param rows the result set
         * @return the candidate
         * @throws SQLException if reading fails
         */
        private Candidate readCandidate(ResultSet rows) throws SQLException {

            Candidate result = new Candidate();
            result.m_resourceId = new CmsUUID(rows.getString("RESOURCE_ID"));
            result.m_content = rows.getBytes("FILE_CONTENT");
            if (result.m_content == null) {
                result.m_content = new byte[0];
            }
            result.m_storage = rows.getString("STORAGE");
            result.m_hash = rows.getString("HASH");
            result.m_typeId = rows.getInt("RESOURCE_TYPE");
            result.m_dateContent = rows.getLong("DATE_CONTENT");
            result.m_publishTagFrom = rows.getInt("PUBLISH_TAG_FROM");
            return result;
        }

        /**
         * Reads the storage policy configuration from opencms-vfs.xml.<p>
         *
         * @param vfsConfig the VFS configuration path
         * @return the storage policy configuration
         * @throws Exception if parsing fails
         */
        private CmsStoragePolicyConfiguration readStoragePolicyConfiguration(Path vfsConfig) throws Exception {

            File file = vfsConfig.toFile();
            if (!file.isFile()) {
                throw new IOException("VFS configuration file does not exist: " + vfsConfig);
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver((publicId, systemId) -> new InputSource(new java.io.StringReader("")));
            Element root = builder.parse(file).getDocumentElement();
            NodeList policies = root.getElementsByTagName("storage-policy");
            if (policies.getLength() == 0) {
                return null;
            }
            Element policy = (Element)policies.item(0);
            CmsStoragePolicyConfiguration result = new CmsStoragePolicyConfiguration();
            result.setClassName(policy.getAttribute("class"));
            NodeList children = policy.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child instanceof Element && "param".equals(child.getNodeName())) {
                    Element param = (Element)child;
                    result.addConfigurationParameter(param.getAttribute("name"), param.getTextContent());
                }
            }
            configureDefaultStoragePolicyTypeIds(result, root);
            result.initConfiguration();
            return result;
        }

        /**
         * Requires an argument value.<p>
         *
         * @param args the arguments
         * @param index the value index
         * @param option the option name
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
                if ((commandLine.m_propertiesPath == null) || (commandLine.m_vfsConfigPath == null)) {
                    throw new IllegalArgumentException(
                        "Either --webinf or both --properties and --vfs-config must be configured.");
                }
                commandLine.m_webInfPath = commandLine.m_propertiesPath.getParent().getParent();
            }
            CmsStorageToolSupport.addDriverDirectory(commandLine.m_driverJars, commandLine.m_webInfPath.resolve("lib"));
            if (commandLine.m_propertiesPath == null) {
                commandLine.m_propertiesPath = commandLine.m_webInfPath.resolve(DEFAULT_PROPERTIES);
            }
            if (commandLine.m_vfsConfigPath == null) {
                commandLine.m_vfsConfigPath = commandLine.m_webInfPath.resolve(DEFAULT_VFS_CONFIG);
            }
        }

        /**
         * Runs the tool.<p>
         *
         * @param args command line arguments
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
                commandLine.m_activeStorageId = properties.getString("storage.active", "db").trim();
                CmsStoragePolicyConfiguration policyConfiguration = readStoragePolicyConfiguration(
                    commandLine.m_vfsConfigPath);
                org.opencms.db.generic.CmsSqlManager sqlManager = CmsStorageToolSupport.createSqlManager(properties);
                CmsStorageToolSupport.DatabaseConfiguration database = CmsStorageToolSupport.readDatabaseConfiguration(
                    properties,
                    commandLine.m_driverJars);
                CmsStorageToolSupport.ToolSqlManager toolSqlManager = new CmsStorageToolSupport.ToolSqlManager(
                    sqlManager,
                    () -> CmsStorageToolSupport.openConnection(database));
                CmsStorageManager storageManager = new CmsStorageManager(
                    toolSqlManager,
                    properties,
                    policyConfiguration);
                if (commandLine.m_validateStorage) {
                    storageManager.validateStorages(new CmsDbContext());
                }

                I_CmsStoragePolicy storagePolicy = createStoragePolicy(policyConfiguration);
                QueryPlan queryPlan = createQueryPlan(policyConfiguration);
                System.out.println("OpenCms storage migration tool");
                System.out.println("Mode              : " + (commandLine.m_dryRun ? "dry-run" : "execute"));
                System.out.println("WEB-INF           : " + commandLine.m_webInfPath);
                System.out.println("Properties        : " + commandLine.m_propertiesPath);
                System.out.println("VFS config        : " + commandLine.m_vfsConfigPath);
                System.out.println("Storage policy    : " + getStoragePolicyName(policyConfiguration));
                System.out.println("Query strategy    : " + queryPlan.m_description);
                System.out.println("Active storage    : " + commandLine.m_activeStorageId);
                System.out.println("JDBC URL          : " + database.m_jdbcUrl);

                ScanStats total = new ScanStats();
                try (Connection connection = CmsStorageToolSupport.openConnection(database)) {
                    warnAboutMissingIndexes(connection);
                    total.add(
                        processScope(
                            connection,
                            toolSqlManager,
                            storageManager,
                            storagePolicy,
                            SCOPE_OFFLINE,
                            queryPlan,
                            commandLine));
                    total.add(
                        processScope(
                            connection,
                            toolSqlManager,
                            storageManager,
                            storagePolicy,
                            SCOPE_CONTENTS_ONLINE,
                            queryPlan,
                            commandLine));
                    total.add(
                        processScope(
                            connection,
                            toolSqlManager,
                            storageManager,
                            storagePolicy,
                            SCOPE_CONTENTS_HISTORY,
                            queryPlan,
                            commandLine));
                    if (!commandLine.m_dryRun && commandLine.m_verify) {
                        ScanStats verification = verify(
                            connection,
                            toolSqlManager,
                            storageManager,
                            storagePolicy,
                            queryPlan,
                            commandLine);
                        if (verification.m_migrationRows > 0) {
                            printStats("VERIFY remaining candidates", verification);
                            return 2;
                        }
                    }
                } finally {
                    storageManager.close();
                }
                printStats("TOTAL", total);
                return 0;
            } catch (Exception e) {
                System.err.println("Storage migration failed: " + e.getMessage());
                e.printStackTrace(System.err);
                return 1;
            }
        }

        /**
         * Scans one candidate scope.<p>
         *
         * @param connection the JDBC connection
         * @param storagePolicy the storage policy
         * @param scope the scope
         * @param batchSize the fetch size
         * @return the scan stats
         * @throws SQLException if the query fails
         */
        private ScanStats scanScope(
            Connection connection,
            CmsStorageToolSupport.ToolSqlManager toolSqlManager,
            CmsStorageManager storageManager,
            I_CmsStoragePolicy storagePolicy,
            Scope scope,
            QueryPlan queryPlan,
            String activeStorageId,
            int batchSize)
        throws Exception {

            ScanStats result = new ScanStats();
            CmsDbContext dbc = new CmsDbContext();
            System.out.println();
            System.out.println("Scanning " + scope.m_label + "...");
            toolSqlManager.setCurrentConnection(connection);
            try (PreparedStatement statement = connection.prepareStatement(scope.createSelectSql(queryPlan))) {
                queryPlan.bind(statement);
                statement.setFetchSize(Math.max(1, batchSize));
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        Candidate candidate = readCandidate(rows);
                        candidate.m_content = loadContent(storageManager, dbc, candidate);
                        result.m_scannedRows += 1;
                        result.m_scannedBytes += candidate.m_content.length;
                        boolean externalStorageRequired = storagePolicy.isExternalStorageRequired(
                            new CmsStoragePolicyContext(candidate.m_content, createResource(candidate)));
                        if (isMigrationRequired(candidate, externalStorageRequired, activeStorageId)) {
                            result.m_migrationRows += 1;
                            result.m_migrationBytes += candidate.m_content.length;
                            if (externalStorageRequired && CmsStringUtil.isNotEmpty(candidate.m_storage)) {
                                result.m_legacyRows += 1;
                            } else if (!externalStorageRequired) {
                                result.m_returnRows += 1;
                            }
                        } else {
                            result.m_skippedRows += 1;
                        }
                    }
                }
            } finally {
                toolSqlManager.clearCurrentConnection();
            }
            return result;
        }

        /**
         * Updates one migrated candidate.<p>
         *
         * @param statement the prepared update statement
         * @param scope the scope
         * @param candidate the candidate
         * @param storageResult the storage result
         * @return the updated row count
         * @throws SQLException if the update fails
         */
        private int updateCandidate(
            PreparedStatement statement,
            Scope scope,
            Candidate candidate,
            StorageResult storageResult)
        throws SQLException {

            byte[] fileContent = storageResult.getFileContent();
            if (fileContent.length < 2000) {
                statement.setBytes(1, fileContent);
            } else {
                statement.setBinaryStream(1, new ByteArrayInputStream(fileContent), fileContent.length);
            }
            statement.setString(2, storageResult.getStorage());
            statement.setString(3, storageResult.getHash());
            statement.setString(4, candidate.m_resourceId.toString());
            int index = 5;
            if (scope.m_hasPublishTagFrom) {
                statement.setInt(index++, candidate.m_publishTagFrom);
            }
            statement.setString(index++, candidate.m_storage);
            statement.setString(index++, candidate.m_hash);
            statement.setString(index++, candidate.m_storage);
            statement.setString(index++, candidate.m_hash);
            if (scope.m_hasDateContentGuard) {
                statement.setLong(index++, candidate.m_dateContent);
            }
            return statement.executeUpdate();
        }

        /**
         * Verifies the selected scopes after migration.<p>
         *
         * @param connection the JDBC connection
         * @param storagePolicy the storage policy
         * @param commandLine the command line
         * @return the verification stats
         * @throws SQLException if verification fails
         */
        private ScanStats verify(
            Connection connection,
            CmsStorageToolSupport.ToolSqlManager toolSqlManager,
            CmsStorageManager storageManager,
            I_CmsStoragePolicy storagePolicy,
            QueryPlan queryPlan,
            CommandLine commandLine)
        throws Exception {

            System.out.println();
            System.out.println("Verifying migration result...");
            ScanStats result = new ScanStats();
            if (commandLine.m_tables.contains(SCOPE_OFFLINE.m_tableSelector)) {
                result.add(
                    scanScope(
                        connection,
                        toolSqlManager,
                        storageManager,
                        storagePolicy,
                        SCOPE_OFFLINE,
                        queryPlan,
                        commandLine.m_activeStorageId,
                        commandLine.m_batchSize));
            }
            if (commandLine.m_tables.contains(SCOPE_CONTENTS_ONLINE.m_tableSelector)) {
                result.add(
                    scanScope(
                        connection,
                        toolSqlManager,
                        storageManager,
                        storagePolicy,
                        SCOPE_CONTENTS_ONLINE,
                        queryPlan,
                        commandLine.m_activeStorageId,
                        commandLine.m_batchSize));
            }
            if (commandLine.m_tables.contains(SCOPE_CONTENTS_HISTORY.m_tableSelector)) {
                result.add(
                    scanScope(
                        connection,
                        toolSqlManager,
                        storageManager,
                        storagePolicy,
                        SCOPE_CONTENTS_HISTORY,
                        queryPlan,
                        commandLine.m_activeStorageId,
                        commandLine.m_batchSize));
            }
            return result;
        }

        /**
         * Prints warnings for missing candidate scan indexes.<p>
         *
         * @param connection the JDBC connection
         */
        private void warnAboutMissingIndexes(Connection connection) {

            warnAboutMissingStorageIndex(connection, "CMS_OFFLINE_CONTENTS", "IDX_CMS_OFFLINE_CONTENTS_STORAGE");
            warnAboutMissingStorageIndex(connection, "CMS_CONTENTS", "IDX_CMS_CONTENTS_STORAGE");
        }

        /**
         * Prints a warning if a storage-leading candidate scan index is missing.<p>
         *
         * @param connection the JDBC connection
         * @param table the table
         * @param suggestedIndex the suggested index name
         */
        private void warnAboutMissingStorageIndex(Connection connection, String table, String suggestedIndex) {

            try {
                if (!hasLeadingIndexColumn(connection, table, "STORAGE")) {
                    System.out.println(
                        "WARNING: "
                            + table
                            + " has no index with leading column STORAGE. "
                            + "Very large migrations may scan this table. Consider creating "
                            + suggestedIndex
                            + " on "
                            + table
                            + "(STORAGE, HASH) for the migration.");
                }
            } catch (SQLException e) {
                System.out.println("WARNING: Could not inspect indexes for " + table + ": " + e.getMessage());
            }
        }
    }

    /**
     * Parsed command line options.<p>
     */
    private static class CommandLine {

        /** The active storage id. */
        private String m_activeStorageId;

        /** Batch size for JDBC fetches. */
        private int m_batchSize = 1000;

        /** If true, no writes are performed. */
        private boolean m_dryRun = true;

        /** Additional JDBC driver jars. */
        private List<Path> m_driverJars = new ArrayList<>();

        /** If true, help should be printed. */
        private boolean m_help;

        /** The opencms.properties path. */
        private Path m_propertiesPath;

        /** The selected logical tables. */
        private List<String> m_tables = new ArrayList<>(Arrays.asList("offline", "contents"));

        /** If true, storage backends are validated before scanning. */
        private boolean m_validateStorage = true;

        /** If true, the migration result is verified after writes. */
        private boolean m_verify;

        /** The opencms-vfs.xml path. */
        private Path m_vfsConfigPath;

        /** The WEB-INF path. */
        private Path m_webInfPath;
    }

    /**
     * Candidate query plan.<p>
     */
    private static class QueryPlan {

        /** The candidate WHERE condition. */
        private String m_condition;

        /** The strategy description. */
        private String m_description;

        /** Query parameters. */
        private List<Integer> m_parameters = new ArrayList<>();

        /**
         * Creates the default storage policy query plan.<p>
         *
         * @param resourceTypeIds the configured resource type ids
         * @param threshold the configured size threshold
         * @return the query plan
         */
        private static QueryPlan defaultPolicy(List<Integer> resourceTypeIds, int threshold) {

            QueryPlan result = new QueryPlan();
            result.m_description = "default policy prefilter";
            if (resourceTypeIds.isEmpty()) {
                result.m_condition = "(C.STORAGE IS NOT NULL AND C.HASH IS NOT NULL)";
            } else {
                String placeholders = CmsStringUtil.collectionAsString(
                    Collections.nCopies(resourceTypeIds.size(), "?"),
                    ",");
                result.m_condition = "((C.STORAGE IS NOT NULL AND C.HASH IS NOT NULL) "
                    + "OR (C.STORAGE IS NULL AND C.HASH IS NULL AND R.RESOURCE_TYPE IN ("
                    + placeholders
                    + ") AND R.RESOURCE_SIZE>?))";
                result.m_parameters.addAll(resourceTypeIds);
                result.m_parameters.add(Integer.valueOf(threshold));
            }
            return result;
        }

        /**
         * Creates the generic query plan.<p>
         *
         * @return the query plan
         */
        private static QueryPlan generic() {

            QueryPlan result = new QueryPlan();
            result.m_description = "generic full policy scan";
            result.m_condition = "((C.STORAGE IS NULL AND C.HASH IS NULL) "
                + "OR (C.STORAGE IS NOT NULL AND C.HASH IS NOT NULL))";
            return result;
        }

        /**
         * Creates the no-external-storage query plan.<p>
         *
         * @return the query plan
         */
        private static QueryPlan noExternal() {

            QueryPlan result = new QueryPlan();
            result.m_description = "external rows only";
            result.m_condition = "(C.STORAGE IS NOT NULL AND C.HASH IS NOT NULL)";
            return result;
        }

        /**
         * Binds the query parameters.<p>
         *
         * @param statement the statement
         * @throws SQLException if binding fails
         */
        private void bind(PreparedStatement statement) throws SQLException {

            for (int i = 0; i < m_parameters.size(); i++) {
                statement.setInt(i + 1, m_parameters.get(i).intValue());
            }
        }
    }

    /**
     * Scan statistics.<p>
     */
    private static class ScanStats {

        /** Bytes selected by the storage policy. */
        private long m_migrationBytes;

        /** External rows selected from a non-active backend. */
        private long m_legacyRows;

        /** Rows selected by the storage policy. */
        private long m_migrationRows;

        /** Rows updated by the migration. */
        private long m_migratedRows;

        /** Local bytes scanned. */
        private long m_scannedBytes;

        /** Local rows scanned. */
        private long m_scannedRows;

        /** Rows skipped by the storage policy. */
        private long m_skippedRows;

        /** External rows selected for migration back to local storage. */
        private long m_returnRows;

        /** Rows no longer matching the guarded update. */
        private long m_staleRows;

        /**
         * Adds another stats object.<p>
         *
         * @param stats the stats to add
         */
        private void add(ScanStats stats) {

            m_migrationBytes += stats.m_migrationBytes;
            m_legacyRows += stats.m_legacyRows;
            m_migrationRows += stats.m_migrationRows;
            m_migratedRows += stats.m_migratedRows;
            m_scannedBytes += stats.m_scannedBytes;
            m_scannedRows += stats.m_scannedRows;
            m_skippedRows += stats.m_skippedRows;
            m_returnRows += stats.m_returnRows;
            m_staleRows += stats.m_staleRows;
        }
    }

    /**
     * Migration scope definition.<p>
     */
    private static class Scope {

        /** If true, the update guards the resource date. */
        private boolean m_hasDateContentGuard;

        /** If true, the update uses PUBLISH_TAG_FROM. */
        private boolean m_hasPublishTagFrom;

        /** Human readable label. */
        private String m_label;

        /** SQL used to select candidates. */
        private String m_selectSql;

        /** Table selector option value. */
        private String m_tableSelector;

        /** SQL used to update candidates. */
        private String m_updateSql;

        /**
         * Creates a scope.<p>
         *
         * @param label the label
         * @param tableSelector the table selector
         * @param selectSql the select SQL
         * @param updateSql the update SQL
         * @param hasPublishTagFrom if true, the scope uses PUBLISH_TAG_FROM
         * @param hasDateContentGuard if true, the scope guards DATE_CONTENT
         */
        private Scope(
            String label,
            String tableSelector,
            String selectSql,
            String updateSql,
            boolean hasPublishTagFrom,
            boolean hasDateContentGuard) {

            m_label = label;
            m_tableSelector = tableSelector;
            m_selectSql = selectSql;
            m_updateSql = updateSql;
            m_hasPublishTagFrom = hasPublishTagFrom;
            m_hasDateContentGuard = hasDateContentGuard;
        }

        /**
         * Creates the select SQL for the query plan.<p>
         *
         * @param queryPlan the query plan
         * @return the select SQL
         */
        private String createSelectSql(QueryPlan queryPlan) {

            return String.format(m_selectSql, queryPlan.m_condition);
        }
    }

    /** The default VFS configuration path below WEB-INF. */
    private static final String DEFAULT_VFS_CONFIG = "config/opencms-vfs.xml";

    /** The default properties path below WEB-INF. */
    private static final String DEFAULT_PROPERTIES = "config/opencms.properties";

    /** SQL for historical CMS_CONTENTS candidates. */
    private static final String SQL_CONTENTS_HISTORY = "SELECT C.RESOURCE_ID, C.PUBLISH_TAG_FROM, C.FILE_CONTENT, "
        + "C.STORAGE, C.HASH, R.RESOURCE_TYPE, R.DATE_CONTENT FROM CMS_CONTENTS C, CMS_HISTORY_RESOURCES R "
        + "WHERE C.RESOURCE_ID=R.RESOURCE_ID AND C.PUBLISH_TAG_FROM=R.PUBLISH_TAG "
        + "AND C.ONLINE_FLAG=0 AND %s";

    /** SQL for current online CMS_CONTENTS candidates. */
    private static final String SQL_CONTENTS_ONLINE = "SELECT C.RESOURCE_ID, C.PUBLISH_TAG_FROM, C.FILE_CONTENT, "
        + "C.STORAGE, C.HASH, R.RESOURCE_TYPE, R.DATE_CONTENT FROM CMS_CONTENTS C, CMS_ONLINE_RESOURCES R "
        + "WHERE C.RESOURCE_ID=R.RESOURCE_ID AND C.ONLINE_FLAG=1 AND %s";

    /** SQL for CMS_OFFLINE_CONTENTS candidates. */
    private static final String SQL_OFFLINE = "SELECT C.RESOURCE_ID, 0 AS PUBLISH_TAG_FROM, C.FILE_CONTENT, "
        + "C.STORAGE, C.HASH, R.RESOURCE_TYPE, R.DATE_CONTENT FROM CMS_OFFLINE_CONTENTS C, CMS_OFFLINE_RESOURCES R "
        + "WHERE C.RESOURCE_ID=R.RESOURCE_ID AND %s";

    /** Scope for historical CMS_CONTENTS. */
    private static final Scope SCOPE_CONTENTS_HISTORY = new Scope(
        "CMS_CONTENTS history",
        "contents",
        SQL_CONTENTS_HISTORY,
        "UPDATE CMS_CONTENTS SET FILE_CONTENT=?, STORAGE=?, HASH=? "
            + "WHERE RESOURCE_ID=? AND PUBLISH_TAG_FROM=? AND ONLINE_FLAG=0 "
            + "AND ((STORAGE IS NULL AND HASH IS NULL AND ? IS NULL AND ? IS NULL) OR (STORAGE=? AND HASH=?))",
        true,
        false);

    /** Scope for current online CMS_CONTENTS. */
    private static final Scope SCOPE_CONTENTS_ONLINE = new Scope(
        "CMS_CONTENTS online",
        "contents",
        SQL_CONTENTS_ONLINE,
        "UPDATE CMS_CONTENTS SET FILE_CONTENT=?, STORAGE=?, HASH=? "
            + "WHERE RESOURCE_ID=? AND PUBLISH_TAG_FROM=? AND ONLINE_FLAG=1 "
            + "AND ((STORAGE IS NULL AND HASH IS NULL AND ? IS NULL AND ? IS NULL) OR (STORAGE=? AND HASH=?)) "
            + "AND EXISTS (SELECT 1 FROM CMS_ONLINE_RESOURCES R "
            + "WHERE R.RESOURCE_ID=CMS_CONTENTS.RESOURCE_ID AND R.DATE_CONTENT=?)",
        true,
        true);

    /** Scope for CMS_OFFLINE_CONTENTS. */
    private static final Scope SCOPE_OFFLINE = new Scope(
        "CMS_OFFLINE_CONTENTS",
        "offline",
        SQL_OFFLINE,
        "UPDATE CMS_OFFLINE_CONTENTS SET FILE_CONTENT=?, STORAGE=?, HASH=? "
            + "WHERE RESOURCE_ID=? "
            + "AND ((STORAGE IS NULL AND HASH IS NULL AND ? IS NULL AND ? IS NULL) OR (STORAGE=? AND HASH=?)) "
            + "AND EXISTS (SELECT 1 FROM CMS_OFFLINE_RESOURCES R "
            + "WHERE R.RESOURCE_ID=CMS_OFFLINE_CONTENTS.RESOURCE_ID AND R.DATE_CONTENT=?)",
        false,
        true);

    /**
     * Hidden constructor.<p>
     */
    private CmsStorageMigrationTool() {

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

        return new CmsStorageMigrationToolRunner().run(args);
    }
}
