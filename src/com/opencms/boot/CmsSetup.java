/*
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
package com.opencms.boot;

import source.org.apache.java.util.*;
import java.io.*;
import java.util.*;

/**
 * Bean with get / set methods for all properties stored in the
 * 'opencms.properties' file. The path to the opencms home folder and
 * its config folder can also be stored an retrieved as well as a vector
 * containing possible error messages thrown by the setup.
 *
 * @author Magnus Meurer
 */
public class CmsSetup {

  /** Contains error messages, displayed by the setup wizard */
  private static Vector errors = new Vector();

  /** Contains the properties from the opencms.properties file */
  private ExtendedProperties m_extProp;

  /** Contains the absolute path to the opencms home directory */
  private String m_basePath;

  /** Contains the relative path frome the opencms home folder to the config folder */
  private String m_configFolder;

  /** Indicates if the user has chosen standard (false)
   *  or advanced (true) setup
   */
  private boolean m_setupType;


  /** Contains the connection string to the database
   *  used by the setup wizard to connect to the db
   */
  private String m_dbSetupConStr;

  /** Contains the user of the database
   *  used by the setup wizard to connect to the db
   */
  private String m_dbSetupUser;

  /** Contains the password of the database
   *  used by the setup wizard to connect to the db
   */
  private String m_dbSetupPwd;

  /** Indicates if the database work connection is different (true)
   *  or equal (false) to the database setup connection
   */
  private boolean m_extraWork;



  /** This method reads the properties from the opencms.propertie file
   *  and sets the CmsSetup properties with the matching values.
   *  This method should be called when the first page of the OpenCms
   *  Setup Wizard is called, so the input fields of the wizard are pre-defined
   */
  public void initProperties(String props)  {
    String path = getWorkFolder() + props;
    try {
      m_extProp = new ExtendedProperties(path);
    }
    catch (Exception e) {
      errors.add(e.toString());
    }
  }

  /** This method sets the extended Properties by the given key with
   *  the given value. A backslash ('\') is added before each comma (',')
   *  in the value string, so the properties can be read correctly afterwards.
   *  @param key The key of the property
   *  @param value The value of the property
   */
  public void setProperties(String key, String value) {
    try {
      char [] chars = value.toCharArray();
      String modifiedValue = "";
      for(int i = 0; i < chars.length; i++) {
        if (chars[i] == ',')  {
          modifiedValue += '\\';
        }
        modifiedValue +=chars[i];
      }
      m_extProp.put(key,modifiedValue);
    }
    catch (Exception e) {
    }
  }


  /** Sets the path to the OpenCms home directory */
  public void setBasePath(String basePath)  {
    m_basePath = basePath;
  }

  /** Returns the absolute path to the OpenCms home directory */
  public String getBasePath() {
    return m_basePath.replace('\\','/').replace('/',File.separatorChar);
  }

  /** Sets the setup type to the given value: standard (false), advanced (true) */
  public void setSetupType(boolean setupType)  {
    m_setupType = setupType;
  }

  /** Returns the value of the setup type: standard (false), advanced (true) */
  public boolean getSetupType() {
    return m_setupType;
  }

  /** Sets the resource broker to the given value */
  public void setResourceBroker(String resourceBroker)  {
    setProperties("resourcebroker",resourceBroker);
  }

  /** Gets the resource broker */
  public String getResourceBroker() {
    return m_extProp.get("resourcebroker").toString();
  }

  /** Sets the connection string used by the setup to the given value */
  public void setDbSetupConStr(String dbSetupConStr)  {
    m_dbSetupConStr = dbSetupConStr;
    // Properties
    setProperties("pool." + getResourceBroker() + ".url",dbSetupConStr);
  }

  /** Returns the connection string to the database used by the setup
   *  either from the properties or from the string.
   *  @param fromProperties indicates if it should be read from the properties
   *  or from the String
   */
  public String getDbSetupConStr(boolean fromProperties)  {
    if(fromProperties)  {
      Object ConStr =  m_extProp.get("pool." + getResourceBroker() + ".url");
      if(ConStr != null)  {
        return m_extProp.get("pool." + getResourceBroker() + ".url").toString();
      }
      else  {
        return "";
      }
    }
    else  {
      return m_dbSetupConStr;
    }
  }

  /** Sets the user of the database used by the setup to the given value */
  public void setDbSetupUser(String dbSetupUser)  {
    // String
    m_dbSetupUser = dbSetupUser;
    // Properties
    setProperties("pool." + getResourceBroker() + ".user",dbSetupUser);
  }

  /** Returns the user of the database used by the setup
   *  either from the properties or from the string.
   *  @param fromProperties indicates if it should be read from the properties
   *  or from the String
   */
  public String getDbSetupUser(boolean fromProperties) {
    if(fromProperties)  {
      Object User =  m_extProp.get("pool." + getResourceBroker() + ".user");
      if(User != null)  {
        return m_extProp.get("pool." + getResourceBroker() + ".user").toString();
      }
      else  {
        return "";
      }
    }
    else  {
      return m_dbSetupUser;
    }
  }

  /** Sets the password of the database used by the setup to the given value */
  public void setDbSetupPwd(String dbSetupPwd)  {
    // String
    m_dbSetupPwd = dbSetupPwd;
    // Properties
    setProperties("pool." + getResourceBroker() + ".password",dbSetupPwd);
  }

  /** Returns the password of the database used by the setup
   *  either from the properties or from the string.
   *  @param fromProperties indicates if it should be read from the properties
   *  or from the String
   */
  public String getDbSetupPwd(boolean fromProperties) {
    if(fromProperties)  {
      Object Pwd =  m_extProp.get("pool." + getResourceBroker() + ".password");
      if (Pwd != null)  {
        return m_extProp.get("pool." + getResourceBroker() + ".password").toString();
      }
      else  {
        return "";
      }
    }
    else  {
      return m_dbSetupPwd;
    }
  }

  /** Sets the connection string to the database to the given value */
  public void setDbWorkConStr(String dbWorkConStr)  {
    setProperties("pool." + getResourceBroker() + ".url",dbWorkConStr);
  }

  /** Returns the connection string to the database from the properties */
  public String getDbWorkConStr()  {
    Object ConStr =  m_extProp.get("pool." + getResourceBroker() + ".url");
    if(ConStr != null)  {
      return m_extProp.get("pool." + getResourceBroker() + ".url").toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the user of the database to the given value */
  public void setDbWorkUser(String dbWorkUser)  {
    setProperties("pool." + getResourceBroker() + ".user",dbWorkUser);
  }

  /** Returns the user of the database from the properties */
  public String getDbWorkUser()  {
    Object ConStr =  m_extProp.get("pool." + getResourceBroker() + ".user");
    if(ConStr != null)  {
      return m_extProp.get("pool." + getResourceBroker() + ".user").toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the password of the database to the given value */
  public void setDbWorkPwd(String dbWorkPwd)  {
    setProperties("pool." + getResourceBroker() + ".password",dbWorkPwd);
  }

  /** Returns the password of the database from the properties */
  public String getDbWorkPwd()  {
    Object ConStr =  m_extProp.get("pool." + getResourceBroker() + ".password");
    if(ConStr != null)  {
      return m_extProp.get("pool." + getResourceBroker() + ".password").toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the flag to true if there is an extra work connection
   *  or false if the database work connection is equal to the
   *  database setup connection. If false, the connection string,
   *  user and password of the work connection are set like the setup
   *  connection
   */
   public void setExtraWork(boolean extraWork) {
    m_extraWork = extraWork;
    if (!extraWork)  {
      setDbWorkConStr(getDbSetupConStr(false));
      setDbWorkUser(getDbSetupUser(false));
      setDbWorkPwd(getDbSetupPwd(false));
    }
  }

  /** Returns true if an extra Work Connection has been selected
   *  or false if not
   */
   public boolean getExtraWork()  {
    return m_extraWork;
  }

  /** Returns the extended properties */
  public ExtendedProperties getProperties() {
    return m_extProp;
  }


  /** Adds a new error message to the vector */
  public static void setErrors(String error) {
    errors.add(error);
  }

  /** Returns the error messages */
  public Vector getErrors() {
    return errors;
  }

  /** Sets the path to the opencms config folder to the given value */
  public void setConfigFolder(String configFolder)  {
    m_configFolder = configFolder;
  }

  /** Returns the path to the opencms config folder */
  public String getWorkFolder() {
    return (m_basePath + m_configFolder).replace('\\','/').replace('/',File.separatorChar);
  }

  /** Returns the database driver belonging to the resource broker */
  public String getDbDriver() {
    return m_extProp.get("pool."+getResourceBroker()+".driver").toString();
  }

  /** Sets the minimum connections to the given value */
  public void setMinConn(String minConn)  {
    setProperties("pool." + getResourceBroker() + ".minConn",minConn);
  }

  /** Returns the min. connections */
  public String getMinConn()  {
    Object temp =  m_extProp.get("pool." + getResourceBroker() + ".minConn");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the maximum connections to the given value */
  public void setMaxConn(String maxConn)  {
    setProperties("pool." + getResourceBroker() + ".maxConn",maxConn);
  }

  /** Returns the max. connections */
  public String getMaxConn()  {
    Object temp =  m_extProp.get("pool." + getResourceBroker() + ".maxConn");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the increase rate to the given value */
  public void setIncreaseRate(String increaseRate)  {
    setProperties("pool." + getResourceBroker() + ".increaseRate",increaseRate);
  }

  /** Returns the increase rate */
  public String getIncreaseRate()  {
    Object temp =  m_extProp.get("pool." + getResourceBroker() + ".increaseRate");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the timeout to the given value */
  public void setTimeout(String timeout)  {
    setProperties("pool." + getResourceBroker() + ".timeout",timeout);
  }

  /** Returns the timeout value */
  public String getTimeout()  {
    Object temp =  m_extProp.get("pool." + getResourceBroker() + ".timeout");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the max. age to the given value */
  public void setMaxAge(String maxAge)  {
    setProperties("pool." + getResourceBroker() + ".maxage",maxAge);
  }

  /** Returns the max. age */
  public String getMaxAge()  {
    Object temp =  m_extProp.get("pool." + getResourceBroker() + ".maxage");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the cache value for user to the given value */
  public void setCacheUser(String cacheUser)  {
    setProperties("cache.user",cacheUser);
  }

  /** Returns the cache value for user */
  public String getCacheUser()  {
    Object temp =  m_extProp.get("cache.user");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the cache value for group to the given value */
  public void setCacheGroup(String cacheGroup)  {
    setProperties("cache.group",cacheGroup);
  }

  /** Returns the cache value for group */
  public String getCacheGroup()  {
    Object temp =  m_extProp.get("cache.group");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the cache value for usergroups to the given value */
  public void setCacheUserGroups(String cacheUserGroups)  {
    setProperties("cache.usergroups",cacheUserGroups);
  }

  /** Returns the cache value for usergroups */
  public String getCacheUserGroups()  {
    Object temp =  m_extProp.get("cache.usergroups");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the cache value for project to the given value */
  public void setCacheProject(String cacheProject)  {
    setProperties("cache.project",cacheProject);
  }

  /** Returns the cache value for project */
  public String getCacheProject()  {
    Object temp =  m_extProp.get("cache.project");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the cache value for online project to the given value */
  public void setCacheOnlineProject(String cacheOnlineProject)  {
    setProperties("cache.onlineproject",cacheOnlineProject);
  }

  /** Returns the cache value for online project */
  public String getCacheOnlineProject()  {
    Object temp =  m_extProp.get("cache.onlineproject");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the cache value for resource to the given value */
  public void setCacheResource(String cacheResource)  {
    setProperties("cache.resource",cacheResource);
  }

  /** Returns the cache value for resource */
  public String getCacheResource()  {
    Object temp =  m_extProp.get("cache.resource");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the cache value for subres to the given value */
  public void setCacheSubres(String cacheSubres)  {
    setProperties("cache.subres",cacheSubres);
  }

  /** Returns the cache value for subres */
  public String getCacheSubres()  {
    Object temp =  m_extProp.get("cache.subres");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the cache value for property to the given value */
  public void setCacheProperty(String cacheProperty)  {
    setProperties("cache.property",cacheProperty);
  }

  /** Returns the cache value for property */
  public String getCacheProperty()  {
    Object temp =  m_extProp.get("cache.property");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the cache value for property def. to the given value */
  public void setCachePropertyDef(String cachePropertyDef)  {
    setProperties("cache.propertydef",cachePropertyDef);
  }

  /** Returns the cache value for property def. */
  public String getCachePropertyDef()  {
    Object temp =  m_extProp.get("cache.propertydef");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the cache value for property def. vector to the given value */
  public void setCachePropertyDefVector(String cachePropertyDefVector)  {
    setProperties("cache.propertydefvector",cachePropertyDefVector);
  }

  /** Returns the cache value for property def. vector */
  public String getCachePropertyDefVector()  {
    Object temp =  m_extProp.get("cache.propertydefvector");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the value for session failover to the given value */
  public void setSessionFailover(String sessionFailover)  {
    setProperties("sessionfailover.enabled",sessionFailover);
  }

  /** Returns the value for session failover */
  public String getSessionFailover()  {
    Object temp =  m_extProp.get("sessionfailover.enabled");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the value for deleting published project parameters to the given value */
  public void setDelPubProParameters(String delPubProParameters)  {
    setProperties("publishproject.delete",delPubProParameters);
  }

  /** Returns the value for deleting published project parameters */
  public String getDelPubProParameters()  {
    Object temp =  m_extProp.get("publishproject.delete");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the value for http streaming to the given value */
  public void setHttpStreaming(String httpStreaming)  {
    setProperties("httpstreaming.enabled",httpStreaming);
  }

  /** Returns the value for http streaming */
  public String getHttpStreaming()  {
    Object temp =  m_extProp.get("httpstreaming.enabled");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the value for exportpoint 0 to the given value */
  public void setExportPoint0(String exportPoint0)  {
    setProperties("exportpoint.0",exportPoint0);
  }

  /** Returns the value for exportpoint 0 */
  public String getExportPoint0()  {
    Object temp =  m_extProp.get("exportpoint.0");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the value for exportpoint path 0 to the given value */
  public void setExportPointPath0(String exportPointPath0)  {
    setProperties("exportpoint.path.0",exportPointPath0);
  }

  /** Returns the value for exportpoint path 0 */
  public String getExportPointPath0()  {
    Object temp =  m_extProp.get("exportpoint.path.0");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the value for exportpoint 1 to the given value */
  public void setExportPoint1(String exportPoint1)  {
    setProperties("exportpoint.1",exportPoint1);
  }

  /** Returns the value for exportpoint 1 */
  public String getExportPoint1()  {
    Object temp =  m_extProp.get("exportpoint.1");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the value for exportpoint path 1 to the given value */
  public void setExportPointPath1(String exportPointPath1)  {
    setProperties("exportpoint.path.1",exportPointPath1);
  }

  /** Returns the value for exportpoint path 1 */
  public String getExportPointPath1()  {
    Object temp =  m_extProp.get("exportpoint.path.1");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the value for exportpoint 2 to the given value */
  public void setExportPoint2(String exportPoint2)  {
    setProperties("exportpoint.2",exportPoint2);
  }

  /** Returns the value for exportpoint 2 */
  public String getExportPoint2()  {
    Object temp =  m_extProp.get("exportpoint.2");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the value for exportpoint path 2 to the given value */
  public void setExportPointPath2(String exportPointPath2)  {
    setProperties("exportpoint.path.2",exportPointPath2);
  }

  /** Returns the value for exportpoint path 2 */
  public String getExportPointPath2()  {
    Object temp =  m_extProp.get("exportpoint.path.2");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the value for opencms logging to the given value */
  public void setLogging(String logging)  {
    setProperties("log",logging);
  }

  /** Returns the value for opencms logging */
  public String getLogging()  {
    Object temp =  m_extProp.get("log");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the value for the log file to the given value */
  public void setLogFile(String logFile)  {
    setProperties("log.file",logFile);
  }

  /** Returns the value for the log file */
  public String getLogFile()  {
    Object temp =  m_extProp.get("log.file");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Enables/Disables timestamps in the opencms logfile */
  public void setLogTimestamp(String logTimestamp)  {
    setProperties("log.timestamp",logTimestamp);
  }

  /** Indicates if timestamps are displayed in the opencms logfile */
  public String getLogTimestamp()  {
    Object temp =  m_extProp.get("log.timestamp");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Enables/Disables memory state in the log messages */
  public void setLogMemory(String logMemory)  {
    setProperties("log.memory",logMemory);
  }

  /** Indicates if memory state is displayed in the log messages */
  public String getLogMemory()  {
    Object temp =  m_extProp.get("log.memory");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the value for the log date format to the given value */
  public void setLogDateFormat(String logDateFormat)  {
    setProperties("log.dateFormat",logDateFormat);
  }

  /** Returns the value for the log date format */
  public String getLogDateFormat()  {
    Object temp =  m_extProp.get("log.dateFormat");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the value for the log queue maxage to the given value */
  public void setLogQueueMaxAge(String logQueueMaxAge)  {
    setProperties("log.queue.maxage",logQueueMaxAge);
  }

  /** Returns the value for the log queue maxage */
  public String getLogQueueMaxAge()  {
    Object temp =  m_extProp.get("log.queue.maxage");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Sets the value for the log queue maxsize to the given value */
  public void setLogQueueMaxSize(String logQueueMaxSize)  {
    setProperties("log.queue.maxsize",logQueueMaxSize);
  }

  /** Returns the value for the log queue maxsize */
  public String getLogQueueMaxSize()  {
    Object temp =  m_extProp.get("log.queue.maxsize");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Enables/Disables channel names in the log messages */
  public void setLoggingChannelName(String loggingChannelName)  {
    setProperties("log.channel",loggingChannelName);
  }

  /** Indicates if channel names are displayed in the log messages */
  public String getLoggingChannelName()  {
    Object temp =  m_extProp.get("log.channel");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Enables/Disables channel opencms_init in the log messages */
  public void setLoggingChannelOpencms_init(String loggingChannelOpencms_init)  {
    setProperties("log.channel.opencms_init",loggingChannelOpencms_init);
  }

  /** Indicates if channel opencms_init is enabled in the log messages */
  public String getLoggingChannelOpencms_init()  {
    Object temp =  m_extProp.get("log.channel.opencms_init");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Enables/Disables channel opencms_debug in the log messages */
  public void setLoggingChannelOpencms_debug(String loggingChannelOpencms_debug)  {
    setProperties("log.channel.opencms_debug",loggingChannelOpencms_debug);
  }

  /** Indicates if channel opencms_debug is enabled in the log messages */
  public String getLoggingChannelOpencms_debug()  {
    Object temp =  m_extProp.get("log.channel.opencms_debug");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Enables/Disables channel opencms_cache in the log messages */
  public void setLoggingChannelOpencms_cache(String loggingChannelOpencms_cache)  {
    setProperties("log.channel.opencms_cache",loggingChannelOpencms_cache);
  }

  /** Indicates if channel opencms_cache is enabled in the log messages */
  public String getLoggingChannelOpencms_cache()  {
    Object temp =  m_extProp.get("log.channel.opencms_cache");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Enables/Disables channel opencms_info in the log messages */
  public void setLoggingChannelOpencms_info(String loggingChannelOpencms_info)  {
    setProperties("log.channel.opencms_info",loggingChannelOpencms_info);
  }

  /** Indicates if channel opencms_info is enabled in the log messages */
  public String getLoggingChannelOpencms_info()  {
    Object temp =  m_extProp.get("log.channel.opencms_info");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Enables/Disables channel opencms_pool in the log messages */
  public void setLoggingChannelOpencms_pool(String loggingChannelOpencms_pool)  {
    setProperties("log.channel.opencms_pool",loggingChannelOpencms_pool);
  }

  /** Indicates if channel opencms_pool is enabled in the log messages */
  public String getLoggingChannelOpencms_pool()  {
    Object temp =  m_extProp.get("log.channel.opencms_pool");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Enables/Disables channel opencms_streaming in the log messages */
  public void setLoggingChannelOpencms_streaming(String loggingChannelOpencms_streaming)  {
    setProperties("log.channel.opencms_streaming",loggingChannelOpencms_streaming);
  }

  /** Indicates if channel opencms_streaming is enabled in the log messages */
  public String getLoggingChannelOpencms_streaming()  {
    Object temp =  m_extProp.get("log.channel.opencms_streaming");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Enables/Disables channel opencms_critical in the log messages */
  public void setLoggingChannelOpencms_critical(String loggingChannelOpencms_critical)  {
    setProperties("log.channel.opencms_critical",loggingChannelOpencms_critical);
  }

  /** Indicates if channel opencms_critical is enabled in the log messages */
  public String getLoggingChannelOpencms_critical()  {
    Object temp =  m_extProp.get("log.channel.opencms_critical");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Enables/Disables channel modules_debug in the log messages */
  public void setLoggingChannelModules_debug(String loggingChannelModules_debug)  {
    setProperties("log.channel.modules_debug",loggingChannelModules_debug);
  }

  /** Indicates if channel modules_debug is enabled in the log messages */
  public String getLoggingChannelModules_debug()  {
    Object temp =  m_extProp.get("log.channel.modules_debug");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Enables/Disables channel modules_info in the log messages */
  public void setLoggingChannelModules_info(String loggingChannelModules_info)  {
    setProperties("log.channel.modules_info",loggingChannelModules_info);
  }

  /** Indicates if channel modules_info is enabled in the log messages */
  public String getLoggingChannelModules_info()  {
    Object temp =  m_extProp.get("log.channel.modules_info");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  /** Enables/Disables channel modules_critical in the log messages */
  public void setLoggingChannelModules_critical(String loggingChannelModules_critical)  {
    setProperties("log.channel.modules_critical",loggingChannelModules_critical);
  }

  /** Indicates if channel modules_critical is enabled in the log messages */
  public String getLoggingChannelModules_critical()  {
    Object temp =  m_extProp.get("log.channel.modules_critical");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }
}