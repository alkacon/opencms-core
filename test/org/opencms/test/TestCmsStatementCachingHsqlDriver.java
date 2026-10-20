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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.UUID;

import org.hsqldb.jdbc.JDBCConnection;
import org.hsqldb.jdbc.JDBCDriver;
import org.hsqldb.jdbc.JDBCPreparedStatement;
import org.junit.jupiter.api.Test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/** Regression tests for the test-only HSQLDB plan anchors. */
public class TestCmsStatementCachingHsqlDriver {

    @Test
    public void closesPhysicalConnectionAndSupportsUnwrap() throws Exception {

        Connection c = connection(4);
        JDBCConnection raw = c.unwrap(JDBCConnection.class);
        assertSame(c, c.unwrap(Connection.class));
        assertTrue(c.isWrapperFor(JDBCConnection.class));
        prepare(c, "VALUES(1)");
        c.close();
        c.close();
        assertTrue(raw.isClosed());
        assertThrows(SQLException.class, () -> c.prepareStatement("VALUES(1)"));
    }

    @Test
    public void keepsParametersIndependentAndPropagatesSqlExceptions() throws Exception {

        try (Connection c = connection(4);
        PreparedStatement a = c.prepareStatement("VALUES(CAST(? AS INTEGER))");
        PreparedStatement b = c.prepareStatement("VALUES(CAST(? AS INTEGER))")) {
            assertNotSame(a, b);
            a.setInt(1, 12);
            b.setInt(1, 34);
            try (ResultSet r = a.executeQuery()) {
                assertTrue(r.next());
                assertEquals(12, r.getInt(1));
            }
            try (ResultSet r = b.executeQuery()) {
                assertTrue(r.next());
                assertEquals(34, r.getInt(1));
            }
            assertThrows(SQLException.class, () -> c.prepareStatement("SELECT * FROM nonexistent"));
        }
    }

    @Test
    public void refreshesAfterDdlOnAnotherConnection() throws Exception {

        String url = "jdbc:hsqldb:mem:" + UUID.randomUUID() + ";shutdown=true";
        try (Connection raw = new JDBCDriver().connect(url, new Properties());
        Connection c = CmsStatementCachingHsqlDriver.wrap(raw, 4);
        Connection other = new JDBCDriver().connect(url, new Properties());
        Statement ddl = other.createStatement()) {
            ddl.execute("CREATE TABLE t(i INTEGER)");
            long before = prepare(c, "SELECT * FROM t");
            ddl.execute("DROP TABLE t");
            ddl.execute("CREATE TABLE t(s VARCHAR(20))");
            long after = prepare(c, "SELECT * FROM t");
            assertNotEquals(before, after);
            assertEquals(after, prepare(c, "SELECT * FROM t"));
            try (PreparedStatement p = c.prepareStatement("SELECT * FROM t")) {
                assertEquals("S", p.getMetaData().getColumnName(1));
            }
        }
    }

    @Test
    public void retainsPlansAcrossHikariCheckouts() throws Exception {

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(CmsStatementCachingHsqlDriver.class.getName());
        config.setJdbcUrl("jdbc:hsqldb:mem:" + UUID.randomUUID() + ";shutdown=true");
        config.setMaximumPoolSize(1);
        try (HikariDataSource pool = new HikariDataSource(config)) {
            long first;
            try (Connection c = pool.getConnection(); PreparedStatement p = c.prepareStatement("VALUES(1)")) {
                first = p.unwrap(JDBCPreparedStatement.class).getStatementID();
            }
            try (Connection c = pool.getConnection(); PreparedStatement p = c.prepareStatement("VALUES(1)")) {
                long next = p.unwrap(JDBCPreparedStatement.class).getStatementID();
                if (Integer.getInteger("opencms.test.hsqldb.statementCacheSize", 256).intValue() > 0) {
                    assertEquals(first, next);
                } else {
                    assertNotEquals(first, next);
                }
            }
        }
    }

    @Test
    public void retainsPlansAndEvictsLeastRecentlyUsed() throws Exception {

        try (Connection c = connection(2)) {
            long first = prepare(c, "VALUES(1)");
            long second = prepare(c, "VALUES(2)");
            assertEquals(first, prepare(c, "VALUES(1)"));
            prepare(c, "VALUES(3)");
            assertEquals(first, prepare(c, "VALUES(1)"));
            assertNotEquals(second, prepare(c, "VALUES(2)"));
        }
    }

    @Test
    public void separatesSchemasAndPreparationOptions() throws Exception {

        try (Connection c = connection(8); Statement ddl = c.createStatement()) {
            ddl.execute("CREATE SCHEMA other AUTHORIZATION DBA");
            ddl.execute("CREATE TABLE public.t(i INTEGER GENERATED BY DEFAULT AS IDENTITY)");
            ddl.execute("CREATE TABLE other.t(i INTEGER GENERATED BY DEFAULT AS IDENTITY)");
            long first = prepare(c, "SELECT * FROM t");
            c.setSchema("OTHER");
            assertNotEquals(first, prepare(c, "SELECT * FROM t"));
            c.setSchema("PUBLIC");
            assertEquals(first, prepare(c, "SELECT * FROM t"));
            try (PreparedStatement p = c.prepareStatement("INSERT INTO t VALUES(DEFAULT)", new int[] {1})) {
                p.executeUpdate();
                try (ResultSet keys = p.getGeneratedKeys()) {
                    assertTrue(keys.next());
                }
            }
            try (PreparedStatement p = c.prepareStatement(
                "SELECT * FROM t",
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY,
                ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
                assertEquals(ResultSet.TYPE_SCROLL_INSENSITIVE, p.getResultSetType());
            }
        }
    }

    private Connection connection(int capacity) throws SQLException {

        Connection raw = new JDBCDriver().connect(
            "jdbc:hsqldb:mem:" + UUID.randomUUID() + ";shutdown=true",
            new Properties());
        return CmsStatementCachingHsqlDriver.wrap(raw, capacity);
    }

    private long prepare(Connection c, String sql) throws SQLException {

        try (PreparedStatement p = c.prepareStatement(sql)) {
            return ((JDBCPreparedStatement)p).getStatementID();
        }
    }
}
