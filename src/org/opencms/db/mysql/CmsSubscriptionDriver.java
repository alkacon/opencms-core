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

package org.opencms.db.mysql;

import org.opencms.db.CmsDbConsistencyException;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsVisitEntry;
import org.opencms.db.CmsVisitEntryFilter;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.db.generic.Messages;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.OpenCms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MySQL implementation of the subscription driver.<p>
 *
 *  @since 8.0.0
 */
public class CmsSubscriptionDriver extends org.opencms.db.generic.CmsSubscriptionDriver {

    /**
     * @see org.opencms.db.generic.CmsSubscriptionDriver#initSqlManager(java.lang.String)
     */
    @Override
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.generic.CmsSubscriptionDriver#markResourceAsVisitedBy(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.file.CmsResource, org.opencms.file.CmsUser)
     */
    @Override
    public void markResourceAsVisitedBy(CmsDbContext dbc, String poolName, CmsResource resource, CmsUser user)
    throws CmsDataAccessException {

        boolean entryExists = false;
        CmsVisitEntryFilter filter = CmsVisitEntryFilter.ALL.filterResource(resource.getStructureId()).filterUser(
            user.getId());
        // delete existing visited entry for the resource
        if (readVisits(dbc, OpenCms.getSubscriptionManager().getPoolName(), filter).size() > 0) {
            entryExists = true;
            deleteVisits(dbc, OpenCms.getSubscriptionManager().getPoolName(), filter);
        }

        CmsVisitEntry entry = new CmsVisitEntry(user.getId(), System.currentTimeMillis(), resource.getStructureId());

        addVisit(dbc, poolName, entry);

        if (!entryExists) {
            // new entry, check if maximum number of stored visited resources is exceeded
            PreparedStatement stmt = null;
            Connection conn = null;
            ResultSet res = null;
            int count = 0;

            try {
                conn = m_sqlManager.getConnection(poolName);
                stmt = m_sqlManager.getPreparedStatement(conn, dbc.currentProject(), "C_VISITED_USER_COUNT_1");

                stmt.setString(1, user.getId().toString());
                res = stmt.executeQuery();

                if (res.next()) {
                    count = res.getInt(1);
                    while (res.next()) {
                        // do nothing only move through all rows because of mssql odbc driver
                    }
                } else {
                    throw new CmsDbConsistencyException(
                        Messages.get().container(Messages.ERR_COUNTING_VISITED_RESOURCES_1, user.getName()));
                }

                int maxCount = OpenCms.getSubscriptionManager().getMaxVisitedCount();
                if (count > maxCount) {
                    // delete old visited log entries
                    m_sqlManager.closeAll(dbc, null, stmt, res);
                    stmt = m_sqlManager.getPreparedStatement(
                        conn,
                        dbc.currentProject(),
                        "C_MYSQL_VISITED_USER_DELETE_2");

                    stmt.setString(1, user.getId().toString());
                    stmt.setInt(2, count - maxCount);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                throw new CmsDbSqlException(
                    Messages.get().container(Messages.ERR_GENERIC_SQL_1, CmsDbSqlException.getErrorQuery(stmt)),
                    e);
            } finally {
                m_sqlManager.closeAll(dbc, conn, stmt, res);
            }
        }
    }

}
