/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/dbpool/Attic/CmsCallableStatement.java,v $
* Date   : $Date: 2001/07/31 15:50:13 $
* Version: $Revision: 1.2 $
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

import java.sql.*;
import java.util.*;
import java.io.*;
import java.math.*;
import source.org.apache.java.util.*;

/**
 * This class is used to create an connection-pool for opencms.
 *
 * @author a.schouten
 */
public class CmsCallableStatement extends CmsPreparedStatement implements CallableStatement {

  /**
   * The constructor to create a new statement
   */
   CmsCallableStatement(PreparedStatement originalStatement, CmsConnection con) {
      m_con = con;
      m_originalStatement = originalStatement;
   }

  public String getString(int parameterIndex) throws SQLException {
    return ((CallableStatement)m_originalStatement).getString(parameterIndex);
  }

  public boolean getBoolean(int parameterIndex) throws SQLException {
    return ((CallableStatement)m_originalStatement).getBoolean(parameterIndex);
  }

  public byte getByte(int parameterIndex) throws SQLException {
    return ((CallableStatement)m_originalStatement).getByte(parameterIndex);
  }

  public short getShort(int parameterIndex) throws SQLException {
    return ((CallableStatement)m_originalStatement).getShort(parameterIndex);
  }

  public int getInt(int parameterIndex) throws SQLException {
    return ((CallableStatement)m_originalStatement).getInt(parameterIndex);
  }

  public long getLong(int parameterIndex) throws SQLException {
    return ((CallableStatement)m_originalStatement).getLong(parameterIndex);
  }

  public float getFloat(int parameterIndex) throws SQLException {
    return ((CallableStatement)m_originalStatement).getFloat(parameterIndex);
  }

  public double getDouble(int parameterIndex) throws SQLException {
    return ((CallableStatement)m_originalStatement).getDouble(parameterIndex);
  }

  public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
    return ((CallableStatement)m_originalStatement).getBigDecimal(parameterIndex, scale);
  }

  public byte[] getBytes(int parameterIndex) throws SQLException {
    return ((CallableStatement)m_originalStatement).getBytes(parameterIndex);
  }

  public java.sql.Date getDate(int parameterIndex) throws SQLException {
    return ((CallableStatement)m_originalStatement).getDate(parameterIndex);
  }

  public Time getTime(int parameterIndex) throws SQLException {
    return ((CallableStatement)m_originalStatement).getTime(parameterIndex);
  }

  public Timestamp getTimestamp(int parameterIndex) throws SQLException {
    return ((CallableStatement)m_originalStatement).getTimestamp(parameterIndex);
  }

  public Object getObject(int parameterIndex) throws SQLException {
    return ((CallableStatement)m_originalStatement).getObject(parameterIndex);
  }

  public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
    return ((CallableStatement)m_originalStatement).getBigDecimal(parameterIndex);
  }

  public Object getObject(int i, Map map) throws SQLException {
    return ((CallableStatement)m_originalStatement).getObject(i, map);
  }

  public Ref getRef(int i) throws SQLException {
    return ((CallableStatement)m_originalStatement).getRef(i);
  }

  public Blob getBlob(int i) throws SQLException {
    return ((CallableStatement)m_originalStatement).getBlob(i);
  }

  public Clob getClob(int i) throws SQLException {
    return ((CallableStatement)m_originalStatement).getClob(i);
  }

  public Array getArray(int i) throws SQLException {
    return ((CallableStatement)m_originalStatement).getArray(i);
  }

  public java.sql.Date getDate(int parameterIndex, Calendar cal) throws SQLException {
    return ((CallableStatement)m_originalStatement).getDate(parameterIndex, cal);
  }

  public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
    return ((CallableStatement)m_originalStatement).getTime(parameterIndex, cal);
  }

  public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
    return ((CallableStatement)m_originalStatement).getTimestamp(parameterIndex, cal);
  }

  public boolean wasNull() throws SQLException {
    return ((CallableStatement)m_originalStatement).wasNull();
  }

  public void registerOutParameter(int paramIndex, int sqlType, String typeName) throws SQLException {
    ((CallableStatement)m_originalStatement).registerOutParameter(paramIndex, sqlType, typeName);
  }

  public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
    ((CallableStatement)m_originalStatement).registerOutParameter(parameterIndex, sqlType);
  }

  public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
    ((CallableStatement)m_originalStatement).registerOutParameter(parameterIndex, sqlType, scale);
  }
}