package com.opencms.file.mySql;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/mySql/Attic/CmsQueries.java,v $
 * Date   : $Date: 2001/03/01 13:43:31 $
 * Version: $Revision: 1.5 $
 *
 * Copyright (C) 2000  The OpenCms Group
 *
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 *
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

public class CmsQueries extends com.opencms.file.genericSql.CmsQueries
{
	public CmsQueries()
	{
		super();
		// mySQL specific statement (no use of nested select-statements.
		C_RESOURCES_GET_LOST_ID = "SELECT A.FILE_ID FROM " + C_DATABASE_PREFIX + "FILES A LEFT JOIN " + C_DATABASE_PREFIX + "RESOURCES B ON A.FILE_ID=B.FILE_ID WHERE B.FILE_ID is NULL";
		C_GROUPS_GETUSERSOFGROUP = "SELECT U.USER_INFO, U.USER_ID, U.USER_NAME,U.USER_PASSWORD, " + "U.USER_RECOVERY_PASSWORD, U.USER_DESCRIPTION, " + "U.USER_FIRSTNAME,U.USER_LASTNAME,U.USER_EMAIL, " + "U.USER_LASTLOGIN,U.USER_LASTUSED,U.USER_FLAGS, " + "U.USER_DEFAULT_GROUP_ID, DG.PARENT_GROUP_ID, " + "DG.GROUP_NAME, DG.GROUP_DESCRIPTION, DG.GROUP_FLAGS, " + "U.USER_ADDRESS, U.USER_SECTION, U.USER_TYPE " + " FROM " + C_DATABASE_PREFIX + "GROUPS  G, " + C_DATABASE_PREFIX + "USERS  U, " + C_DATABASE_PREFIX + "GROUPUSERS  GU, " + C_DATABASE_PREFIX + "GROUPS  DG " + "where G.GROUP_NAME = ? AND U.USER_ID=GU.USER_ID " + "AND GU.GROUP_ID = G.GROUP_ID " + "AND U.USER_DEFAULT_GROUP_ID = DG.GROUP_ID " + "AND U.USER_TYPE = ? ORDER BY USER_NAME";
		C_USERS_WRITE = "UPDATE " + C_DATABASE_PREFIX + "USERS " + "SET USER_DESCRIPTION = ?, " + "USER_FIRSTNAME = ?, " + "USER_LASTNAME = ?, " + "USER_EMAIL = ?, " + "USER_LASTLOGIN = ?, " + "USER_LASTUSED = ?, " + "USER_FLAGS = ?, " + "USER_INFO = ?, " + "USER_DEFAULT_GROUP_ID = ?, " + "USER_ADDRESS = ?, " + "USER_SECTION = ?, " + "USER_TYPE = ? " + "WHERE USER_ID = ? ";
		C_LOG_COMMENT = C_TABLENAME_TASKLOG + ".coment";
		C_TASK_READ = "SELECT * FROM " + C_TABLENAME_TASK + " WHERE id=?";
		C_TASK_FIND_AGENT = "SELECT * " + "FROM " + C_DATABASE_PREFIX + "GROUPUSERS, " + C_DATABASE_PREFIX + "USERS " + "WHERE GROUP_ID=? AND " + C_DATABASE_PREFIX + "GROUPUSERS.USER_ID=" + C_DATABASE_PREFIX + "USERS.USER_ID " + "ORDER BY USER_LASTUSED ASC";
		C_SYSTEMID_LOCK = "LOCK TABLES " + C_DATABASE_PREFIX + "SYSTEMID WRITE";
		C_SYSTEMID_UNLOCK = "UNLOCK TABLES ";

        // mySQL specific because of the date format ...
        C_USERS_GETUSERS_BY_LASTNAME = "SELECT * FROM " +
            C_DATABASE_PREFIX + "USERS, " + C_DATABASE_PREFIX +
            "GROUPS WHERE LOWER(USER_LASTNAME) LIKE LOWER(?) AND " +
            "USER_DEFAULT_GROUP_ID = GROUP_ID AND USER_TYPE = ? " +
            "AND USER_FLAGS = ? ";
        C_USERS_GETUSERS_BY_LASTNAME_TAIL = "ORDER BY USER_LASTNAME";
        C_USERS_GETUSERS_BY_LASTNAME_NEVER = C_USERS_GETUSERS_BY_LASTNAME +
            "AND USER_LASTLOGIN = '1970.01.01 01:00:00'" +
            C_USERS_GETUSERS_BY_LASTNAME_TAIL;
        C_USERS_GETUSERS_BY_LASTNAME_ONCE = C_USERS_GETUSERS_BY_LASTNAME +
            "AND USER_LASTLOGIN > '1970.01.01 01:00:00'" +
            C_USERS_GETUSERS_BY_LASTNAME_TAIL;
        C_USERS_GETUSERS_BY_LASTNAME_WHATEVER = C_USERS_GETUSERS_BY_LASTNAME +
            C_USERS_GETUSERS_BY_LASTNAME_TAIL;

	}
}
