/*
 * File   : $Source: /alkacon/cvs/opencms-ocee/org.opencms.ocee.vfsdoctor/src/org/opencms/ocee/vfsdoctor/CmsVfsDoctorSqlConsole.java,v $
 * Date   : $Date: 2010/09/06 13:38:58 $
 * Version: $Revision: 1.21 $
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
 * All rights reserved.
 *
 * This source code is the intellectual property of Alkacon Software GmbH.
 * It is PROPRIETARY and CONFIDENTIAL.
 * Use of this source code is subject to license terms.
 *
 * In order to use this source code, you need written permission from
 * Alkacon Software GmbH. Redistribution of this source code, in modified
 * or unmodified form, is not allowed unless written permission by
 * Alkacon Software GmbH has been given.
 *
 * ALKACON SOFTWARE GMBH MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THIS SOURCE CODE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. ALKACON SOFTWARE GMBH SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOURCE CODE OR ITS DERIVATIVES.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 */

package org.opencms.ui.apps.dbmanager.sqlconsole;

import org.opencms.db.I_CmsVfsDriver;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsSecurityException;
import org.opencms.ui.apps.Messages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

/**
 * Manages the SQL console.<p>
 *
 * @author Michael Moossen
 *
 * @version $Revision: 1.21 $
 *
 * @since 6.0.0
 */
public final class CmsSqlConsoleExecutor {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSqlConsoleExecutor.class);

    /** SQL manager with specialized queries. */
    private CmsSqlManager m_sqlManager;

    /**
     * Default Constructor.<p>
     */
    public CmsSqlConsoleExecutor() {

        m_sqlManager = new CmsSqlManager();
        m_sqlManager.init(I_CmsVfsDriver.DRIVER_TYPE_ID, "default");
    }

    /**
     * Main method of this class. Executes the given sql query.<p>
     *
     * This method also checks the permissions for executing sql sentences,
     * according to the <code>vfs-doctor.xml</code> configuration file.<p>
     *
     * @param sql the sql query to execute
     * @param pool name of the db pool to use
     * @param report the report to write the output
     * @param errors a list to append errors to
     *
     * @return a <code>{@link List}</code> if the sql is a SELECT sentence, or <code>null</code>.
     */
    public CmsSqlConsoleResults execute(String sql, String pool, I_CmsReport report, List<Throwable> errors) {

        try {
            CmsMessageContainer message = Messages.get().container(Messages.RPT_SQLCONSOLE_BEGIN_0);
            report.println(message, I_CmsReport.FORMAT_HEADLINE);
            if (LOG.isInfoEnabled()) {
                LOG.info(message.key());
            }

            List<String> sentences = normalize(sql);
            if (sentences.size() < 1) {
                write(
                    report,
                    Messages.get().getBundle(report.getLocale()).key(Messages.ERR_SQLCONSOLE_NOTHING_TO_EXECUTE_0));
            } else {
                for (String sentence : sentences) {
                    if (!checkPermissions(sentence)) {
                        writeError(
                            report,
                            new CmsSecurityException(
                                Messages.get().container(
                                    Messages.ERR_SQLCONSOLE_NO_PERMISSIONS_EXEC_SENTENCE_1,
                                    sentence)));
                    } else {
                        if (!sentence.toUpperCase().startsWith("SELECT")
                            && !sentence.toUpperCase().startsWith("SHOW")) {
                            int res = executeUpdate(sentence, pool);
                            message = Messages.get().container(
                                Messages.RPT_SQLCONSOLE_ROWS_AFFECTED_1,
                                Integer.valueOf(res));
                            report.println(message);
                            if (LOG.isInfoEnabled()) {
                                LOG.info(message);
                            }
                        } else {
                            CmsSqlConsoleResults res = executeQuery(sentence, pool);
                            // writeTable(report, res);
                            message = Messages.get().container(
                                Messages.RPT_SQLCONSOLE_NUM_ROWS_RETRIEVED_1,
                                Integer.valueOf(res.getData().size()));
                            report.println(message);
                            if (LOG.isInfoEnabled()) {
                                LOG.info(message);
                            }
                            return res;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            errors.add(e);
        } finally {
            CmsMessageContainer message = Messages.get().container(Messages.RPT_SQLCONSOLE_END_0);
            report.println(message, I_CmsReport.FORMAT_HEADLINE);
            if (LOG.isInfoEnabled()) {
                LOG.info(message.key());
            }
        }
        return null;
    }

    /**
     * Checks the permission to be executed for a single sql sentence.<p>
     * @param sentence the sentence to check for permission
     *
     * @return <code>true</code> if the sentence is allowed to be executed
     */
    private boolean checkPermissions(String sentence) {

        sentence = sentence.toUpperCase();

        // protected user from the case where they accidentally left off the condition in DELETE statements
        // (you can still use dummy conditions like 'WHERE 1=1' if you really want to delete everything)
        if (sentence.contains("DELETE FROM") && !sentence.contains("WHERE")) {
            return false;
        }
        return true;
    }

    /**
     * Executes a single <code>SELECT</code> sql sentence.<p>
     *
     * @param sentence the sentence to execute
     * @param poolName the name of the pool to use
     *
     * @return the list of rows returned by the rdbms
     *
     * @throws SQLException in the case of a error
     */
    @SuppressWarnings("resource")
    private CmsSqlConsoleResults executeQuery(String sentence, String poolName) throws SQLException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet res = null;

        CmsSqlManager sqlManager = m_sqlManager;

        try {
            conn = sqlManager.getConnection(poolName);
            stmt = sqlManager.getPreparedStatementForSql(conn, sentence);
            res = stmt.executeQuery();

            // add headings
            ResultSetMetaData metadata = res.getMetaData();
            List<String> heading = new ArrayList<>();
            for (int i = 0; i < metadata.getColumnCount(); i++) {
                heading.add(metadata.getColumnName(i + 1));
            }
            List<List<Object>> data = new ArrayList<List<Object>>();

            // add contents
            while (res.next()) {
                List<Object> row = new ArrayList<Object>();
                for (int i = 0; i < metadata.getColumnCount(); i++) {
                    Object value = res.getObject(i + 1);
                    if ((value instanceof String)
                        || (value instanceof Integer)
                        || (value instanceof Long)
                        || (value instanceof Float)
                        || (value instanceof Double)) {
                        row.add(value);
                    } else if (value == null) {
                        row.add(null);
                    } else {
                        row.add(String.valueOf(value));
                    }
                }
                data.add(row);
            }
            return new CmsSqlConsoleResults(heading, data);
        } finally {
            sqlManager.closeAll(null, conn, stmt, res);
        }
    }

    /**
     * Executes a single sql sentence.<p>
     *
     * For <code>SELECT</code> sentences use <code>{@link #executeQuery(String, String)}</code>.<p>
     *
     * @param sentence the sentence to execute
     * @param poolName the name of the pool to use
     *
     * @return the number of affected rows
     *
     * @throws SQLException if there is an error
     */
    private int executeUpdate(String sentence, String poolName) throws SQLException {

        Connection conn = null;
        PreparedStatement stmt = null;
        int ret = 0;

        CmsSqlManager sqlManager = m_sqlManager;
        try {
            conn = sqlManager.getConnection(poolName);
            stmt = sqlManager.getPreparedStatementForSql(conn, sentence);
            ret = stmt.executeUpdate();
        } finally {
            sqlManager.closeAll(null, conn, stmt, null);
        }
        return ret;
    }

    /**
     * Returns a list of single sql sentences,
     * removing all special chars like tabs or eol's.<p>
     *
     * @param sql the sql query
     *
     * @return a list of sql sentences
     */
    private List<String> normalize(String sql) {

        String normSql = sql.replaceAll(";", "; ");
        normSql = normSql.replaceAll("\\s+", " ");
        return Arrays.stream(normSql.split(";")).map(s -> s.trim()).collect(Collectors.toList());
    }

    /**
     * Writes the given string to the report,
     * and, if enabled, to the opencms log file.<p>
     *
     * @param report the report
     * @param string the string
     */
    private void write(I_CmsReport report, String string) {

        report.println(
            org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                string.replaceAll("'", "\\\\'")));
        if (LOG.isInfoEnabled()) {
            LOG.info("SQLConsole:" + string);
        }
    }

    /**
     * Writes the given error to the report,
     * and, if enabled, to the opencms log file.<p>
     *
     * @param report the report to write to
     * @param e the exception
     */
    private void writeError(I_CmsReport report, Throwable e) {

        report.println(e);
        if (LOG.isWarnEnabled()) {
            LOG.warn("SQLConsole", e);
        }
    }
}