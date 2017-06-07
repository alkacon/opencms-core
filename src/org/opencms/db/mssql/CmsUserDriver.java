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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db.mssql;

import org.opencms.db.generic.CmsSqlManager;
import org.opencms.db.generic.CmsUserQueryBuilder;

import com.google.common.base.Joiner;

/**
 * MS SQL implementation of the user driver methods.<p>
 *
 * @since 6.0.0
 */
public class CmsUserDriver extends org.opencms.db.generic.CmsUserDriver {

    /** Records whether this driver class has been instantiated. */
    private static boolean m_isInstantiated;

    /**
     * Creates a new driver instance.<p>
     */
    public CmsUserDriver() {

        m_isInstantiated = true;
    }

    /**
     * Returns true if the user driver has been instantiated.<p>
     *
     * We use this to check whether the used database is MSSQL.
     *
     * TODO: Make lazy user lists work with MSSQL, too.
     *
     * @return true if the user driver has been instantiated
     */
    public static boolean isInstantiated() {

        return m_isInstantiated;
    }

    /**
     * @see org.opencms.db.generic.CmsUserDriver#createUserQueryBuilder()
     */
    @Override
    public CmsUserQueryBuilder createUserQueryBuilder() {

        return new CmsUserQueryBuilder() {

            /**
             * @see org.opencms.db.generic.CmsUserQueryBuilder#generateConcat(java.lang.String[])
             */
            @Override
            protected String generateConcat(String... expressions) {

                return Joiner.on(" + ").join(expressions);
            }

            /**
             * @see org.opencms.db.generic.CmsUserQueryBuilder#generateTrim(java.lang.String)
             */
            @Override
            protected String generateTrim(String expression) {

                return "LTRIM(RTRIM(" + expression + "))";
            }

            /**
             * @see org.opencms.db.generic.CmsUserQueryBuilder#useWindowFunctionsForPaging()
             */
            @Override
            protected boolean useWindowFunctionsForPaging() {

                return true;
            }
        };

    }

    /**
     * @see org.opencms.db.I_CmsUserDriver#initSqlManager(String)
     */
    @Override
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

}