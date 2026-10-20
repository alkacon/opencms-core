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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.hsqldb.Session;
import org.hsqldb.jdbc.JDBCConnection;
import org.hsqldb.jdbc.JDBCDriver;

/**
 * Test-only driver retaining unused statements to keep HSQLDB compiled plans alive.
 * Configure this driver on HikariCP, below its logical connection wrappers.
 * Set opencms.test.hsqldb.statementCacheSize to zero to disable (default: 256).
 * Only embedded connections are wrapped; network connections pass through.
 */
public class CmsStatementCachingHsqlDriver extends JDBCDriver {

    private static final class Anchors implements InvocationHandler {

        private final Connection connection;
        private final int capacity;
        private final Session session;
        private final Map<Key, PreparedStatement> anchors = new LinkedHashMap<>(16, 0.75f, true);
        private long schemaVersion = Long.MIN_VALUE;

        Anchors(Connection connection, int capacity) {

            this.connection = connection;
            this.capacity = capacity;
            session = (Session)((JDBCConnection)connection).getSession();
        }

        @Override
        public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            String name = method.getName();
            if (name.equals("equals")) {
                return Boolean.valueOf(proxy == args[0]);
            }
            if (name.equals("hashCode")) {
                return Integer.valueOf(System.identityHashCode(proxy));
            }
            if (name.equals("unwrap") && ((Class<?>)args[0]).isInstance(proxy)) {
                return proxy;
            }
            if (name.equals("isWrapperFor") && ((Class<?>)args[0]).isInstance(proxy)) {
                return Boolean.TRUE;
            }
            if (name.equals("close") || name.equals("abort")) {
                try {
                    clear();
                } finally {
                    call(method, args);
                }
                return null;
            }
            if (name.equals("prepareStatement") && !connection.isClosed()) {
                // Discard anchors after DDL, including DDL executed by another connection.
                long version = session.database.schemaManager.getSchemaChangeTimestamp();
                if (schemaVersion != version) {
                    clear();
                    schemaVersion = version;
                }
                Key key = new Key(method, args, connection.getSchema(), connection.getHoldability());
                if (!anchors.containsKey(key)) {
                    PreparedStatement anchor = (PreparedStatement)call(method, args);
                    anchors.put(key, anchor);
                    if (anchors.size() > capacity) {
                        Iterator<PreparedStatement> iterator = anchors.values().iterator();
                        PreparedStatement eldest = iterator.next();
                        iterator.remove();
                        eldest.close();
                    }
                } else {
                    anchors.get(key); // refresh access order
                }
            }
            // Every caller owns a fresh JDBC statement, with independent parameters/results.
            return call(method, args);
        }

        private Object call(Method method, Object[] args) throws Throwable {

            try {
                return method.invoke(connection, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private void clear() throws SQLException {

            SQLException failure = null;
            for (PreparedStatement statement : anchors.values()) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    if (failure == null) {
                        failure = e;
                    } else {
                        failure.addSuppressed(e);
                    }
                }
            }
            anchors.clear();
            if (failure != null) {
                throw failure;
            }
        }
    }

    private static final class Key {

        private final Object[] parts;

        Key(Method method, Object[] args, String schema, int holdability) {

            Object[] options = args.clone();
            for (int i = 0; i < options.length; i++) {
                if (options[i] instanceof int[]) {
                    options[i] = ((int[])options[i]).clone();
                } else if (options[i] instanceof String[]) {
                    options[i] = ((String[])options[i]).clone();
                }
            }
            parts = new Object[] {method, options, schema, Integer.valueOf(holdability)};
        }

        @Override
        public boolean equals(Object other) {

            return (other instanceof Key) && Arrays.deepEquals(parts, ((Key)other).parts);
        }

        @Override
        public int hashCode() {

            return Arrays.deepHashCode(parts);
        }
    }

    /** Wraps a physical embedded connection. Package access also permits focused tests. */
    static Connection wrap(Connection connection, int capacity) {

        return (Connection)Proxy.newProxyInstance(
            CmsStatementCachingHsqlDriver.class.getClassLoader(),
            new Class<?>[] {Connection.class},
            new Anchors(connection, capacity));
    }

    @Override
    public Connection connect(String url, Properties properties) throws SQLException {

        Connection connection = super.connect(url, properties);
        int capacity = Integer.getInteger("opencms.test.hsqldb.statementCacheSize", 256).intValue();
        if ((connection == null)
            || (capacity <= 0)
            || !(((JDBCConnection)connection).getSession() instanceof Session)) {
            return connection;
        }
        return wrap(connection, capacity);
    }
}
