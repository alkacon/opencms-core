/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/dbpool/Attic/CmsCallableStatement.java,v $
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

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * The interface used to execute SQL stored procedures.
 *
 * @author Andreas Schouten
 * @see java.sql.CallableStatement
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

  /** @deprecated This is deprecated in JDBC 3.0 but still must be implemented */ 
  public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
    // return ((CallableStatement)m_originalStatement).getBigDecimal(parameterIndex, scale);
    return ((CallableStatement)m_originalStatement).getBigDecimal(parameterIndex);
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
  
  public java.sql.Array getArray(String str) throws java.sql.SQLException {
    return ((CallableStatement)m_originalStatement).getArray(str);
  }
  
  public java.math.BigDecimal getBigDecimal(String str) throws java.sql.SQLException {
    return ((CallableStatement)m_originalStatement).getBigDecimal(str);      
  }
  
  public java.sql.Blob getBlob(String str) throws java.sql.SQLException {
    return ((CallableStatement)m_originalStatement).getBlob(str);      
  }
  
  public boolean getBoolean(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getBoolean(str);
  }
  
  public byte getByte(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getByte(str);
  }
  
  public byte[] getBytes(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getBytes(str);
  }
  
  public java.sql.Clob getClob(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getClob(str);
  }
  
  public java.sql.Date getDate(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getDate(str);
  }
  
  public java.sql.Date getDate(String str, java.util.Calendar calendar) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getDate(str);
  }
  
  public double getDouble(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getDouble(str);
  }
  
  public float getFloat(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getFloat(str);
  }
  
  public int getInt(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getInt(str);
  }
  
  public long getLong(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getLong(str);
  }
  
  public Object getObject(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getObject(str);
  }
  
  public Object getObject(String str, java.util.Map map) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getObject(str, map);
  }
  
  public java.sql.Ref getRef(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getRef(str);
  }
  
  public short getShort(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getShort(str);
  }
  
  public String getString(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getString(str);
  }
  
  public java.sql.Time getTime(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getTime(str);
  }
  
  public java.sql.Time getTime(String str, java.util.Calendar calendar) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getTime(str, calendar);
  }
  
  public java.sql.Timestamp getTimestamp(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getTimestamp(str);
  }
  
  public java.sql.Timestamp getTimestamp(String str, java.util.Calendar calendar) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getTimestamp(str, calendar);
  }
  
  public java.net.URL getURL(int param) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getURL(param);
  }
  
  public java.net.URL getURL(String str) throws java.sql.SQLException {
      return ((CallableStatement)m_originalStatement).getURL(str);
  }
  
  public void registerOutParameter(String str, int param) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).registerOutParameter(str, param);
  }
  
  public void registerOutParameter(String str, int param, int param2) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).registerOutParameter(str, param, param2);
  }
  
  public void registerOutParameter(String str, int param, String str2) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).registerOutParameter(str, param, str2);
  }
  
  public void setAsciiStream(String str, java.io.InputStream inputStream, int param) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setAsciiStream(str, inputStream, param);
  }
  
  public void setBigDecimal(String str, java.math.BigDecimal bigDecimal) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setBigDecimal(str, bigDecimal);
  }
  
  public void setBinaryStream(String str, java.io.InputStream inputStream, int param) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setBinaryStream(str, inputStream, param);
  }
  
  public void setBoolean(String str, boolean param) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setBoolean(str, param);
  }
  
  public void setByte(String str, byte param) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setByte(str, param);
  }
  
  public void setBytes(String str, byte[] values) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setBytes(str, values);
  }
  
  public void setCharacterStream(String str, java.io.Reader reader, int param) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setCharacterStream(str, reader, param);
  }
  
  public void setDate(String str, java.sql.Date date) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setDate(str, date);
  }
  
  public void setDate(String str, java.sql.Date date, java.util.Calendar calendar) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setDate(str, date, calendar);
  }
  
  public void setDouble(String str, double param) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setDouble(str, param);
  }
  
  public void setFloat(String str, float param) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setFloat(str, param);
  }
  
  public void setInt(String str, int param) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setInt(str, param);
  }
  
  public void setLong(String str, long param) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setLong(str, param);
  }
  
  public void setNull(String str, int param) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setNull(str, param);
  }
  
  public void setNull(String str, int param, String str2) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setNull(str, param, str2);
  }
  
  public void setObject(String str, Object obj) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setObject(str, obj);
  }
  
  public void setObject(String str, Object obj, int param) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setObject(str, obj, param);
  }
  
  public void setObject(String str, Object obj, int param, int param3) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setObject(str, obj, param, param3);
  }
  
  public void setShort(String str, short param) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setShort(str, param);
  }
  
  public void setString(String str, String str1) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setString(str, str1);
  }
  
  public void setTime(String str, java.sql.Time time) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setTime(str, time);
  }
  
  public void setTime(String str, java.sql.Time time, java.util.Calendar calendar) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setTime(str, time, calendar);
  }
  
  public void setTimestamp(String str, java.sql.Timestamp timestamp) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setTimestamp(str, timestamp);
  }
  
  public void setTimestamp(String str, java.sql.Timestamp timestamp, java.util.Calendar calendar) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setTimestamp(str, timestamp, calendar);
  }
  
  public void setURL(String str, java.net.URL uRL) throws java.sql.SQLException {
      ((CallableStatement)m_originalStatement).setURL(str, uRL);
  }
  
}