/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/dbpool/Attic/CmsPreparedStatement.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
* Version: $Revision: 1.6 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.dbpool;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;

/**
 * An object that represents a precompiled SQL statement.
 *
 * @author Andreas Schouten
 * @author Mark Foley
 * @see java.sql.PreparedStatement
 */
public class CmsPreparedStatement extends CmsStatement implements PreparedStatement {

  /**
   * The default-constructor to create a new statement
   */
   CmsPreparedStatement() {
      super();
   }

  /**
   * The constructor to create a new statement
   */
   CmsPreparedStatement(PreparedStatement originalStatement, CmsConnection con) {
      m_con = con;
      m_originalStatement = originalStatement;
   }

  public void setNull(int parameterIndex, int sqlType) throws SQLException {
    ((PreparedStatement)m_originalStatement).setNull(parameterIndex, sqlType);
  }

  public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
    ((PreparedStatement)m_originalStatement).setNull(paramIndex, sqlType, typeName);
  }

  public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setBoolean(parameterIndex, x);
  }

  public void setByte(int parameterIndex, byte x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setByte(parameterIndex, x);
  }

  public void setShort(int parameterIndex, short x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setShort(parameterIndex, x);
  }

  public void setInt(int parameterIndex, int x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setInt(parameterIndex, x);
  }

  public void setLong(int parameterIndex, long x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setLong(parameterIndex, x);
  }

  public void setFloat(int parameterIndex, float x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setFloat(parameterIndex, x);
  }

  public void setDouble(int parameterIndex, double x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setDouble(parameterIndex, x);
  }

  public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setBigDecimal(parameterIndex, x);
  }

  public void setString(int parameterIndex, String x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setString(parameterIndex, x);
  }
  
  public void setBytes(int parameterIndex, byte x[]) throws SQLException {
    if(x.length < 2000)
        ((PreparedStatement)m_originalStatement).setBytes(parameterIndex, x);
    else
        ((PreparedStatement)m_originalStatement).setBinaryStream(parameterIndex, new ByteArrayInputStream(x), x.length);
  }  

  public void setDate(int parameterIndex, java.sql.Date x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setDate(parameterIndex, x);
  }

  public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws SQLException {
    ((PreparedStatement)m_originalStatement).setDate(parameterIndex, x, cal);  }


  public void setTime(int parameterIndex, Time x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setTime(parameterIndex, x);
  }

  public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
    ((PreparedStatement)m_originalStatement).setTime(parameterIndex, x, cal);
  }


  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setTimestamp(parameterIndex, x);
  }

  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
    ((PreparedStatement)m_originalStatement).setTimestamp(parameterIndex, x, cal);
  }


  public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
    ((PreparedStatement)m_originalStatement).setAsciiStream(parameterIndex, x, length);
  }
  
  /** @deprecated This is deprecated in JDBC 3.0 but still must be implemented */ 
  public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
    ((PreparedStatement)m_originalStatement).setUnicodeStream(parameterIndex, x, length);
  }

  public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
    ((PreparedStatement)m_originalStatement).setBinaryStream(parameterIndex, x, length);
  }

  public void clearParameters() throws SQLException {
    ((PreparedStatement)m_originalStatement).clearParameters();
  }

  public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
    ((PreparedStatement)m_originalStatement).setObject(parameterIndex, x, targetSqlType, scale);
  }

  public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
    ((PreparedStatement)m_originalStatement).setObject(parameterIndex, x, targetSqlType);
  }

  public void setObject(int parameterIndex, Object x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setObject(parameterIndex, x);
  }

  public boolean execute() throws SQLException {
    return ((PreparedStatement)m_originalStatement).execute();
  }

  public ResultSet executeQuery() throws SQLException {
    return ((PreparedStatement)m_originalStatement).executeQuery();
  }

  public int executeUpdate() throws SQLException {
    return ((PreparedStatement)m_originalStatement).executeUpdate();
  }

  public ResultSetMetaData getMetaData() throws SQLException {
    return ((PreparedStatement)m_originalStatement).getMetaData();
  }

  public void setBlob(int i, Blob x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setBlob(i, x);
  }

  public void setArray(int i, Array x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setArray(i, x);
  }

  public void setClob(int i, Clob x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setClob(i, x);
  }

  public void setRef(int i, Ref x) throws SQLException {
    ((PreparedStatement)m_originalStatement).setRef(i, x);
  }

  public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
    ((PreparedStatement)m_originalStatement).setCharacterStream(parameterIndex, reader, length);
  }

  public void addBatch() throws SQLException {
    ((PreparedStatement)m_originalStatement).addBatch();
  }
  
  public java.sql.ParameterMetaData getParameterMetaData() throws java.sql.SQLException {
    return ((PreparedStatement)m_originalStatement).getParameterMetaData();
  }
  
  public void setURL(int param, java.net.URL uRL) throws java.sql.SQLException {
    ((PreparedStatement)m_originalStatement).setURL(param, uRL);
  }
  
}