/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/CmsSetup.java,v $
* Date   : $Date: 2002/06/30 22:49:24 $
* Version: $Revision: 1.13 $
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

  /** properties from dbsetup.properties */
  private Properties m_dbSetupProps;

  /** Contains the absolute path to the opencms home directory */
  private String m_basePath;

  /** Indicates if the user has chosen standard (false)
   *  or advanced (true) setup
   */
  private boolean m_setupType;

  /**
   * name of the database (mysql)
   */
  private String m_database;


  /**
   * database password used to drop and create database
   */
  private String m_dbCreatePwd;

  /**
   * replacer string
   */
  private Hashtable m_replacer;


  /** This method reads the properties from the opencms.propertie file
   *  and sets the CmsSetup properties with the matching values.
   *  This method should be called when the first page of the OpenCms
   *  Setup Wizard is called, so the input fields of the wizard are pre-defined
   */
  public void initProperties(String props)  {
    String path = getConfigFolder() + props;
    try {
      FileInputStream fis = new FileInputStream(new File(path));
      m_extProp = new ExtendedProperties();
      m_extProp.load(fis);
      fis.close();
      m_dbSetupProps = new Properties();
      m_dbSetupProps.load(getClass().getClassLoader().getResourceAsStream("com/opencms/boot/dbsetup.properties"));
    }
    catch (Exception e) {
        e.printStackTrace();
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
    if (! m_basePath.endsWith(File.separator)) {
        // Make sure that Path always ends with a separator, not always the case in different environments
        // since getServletContext().getRealPath("/") does not end with a "/" in all servlet runtimes
        m_basePath += File.separator;
    }    
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
    Object temp =  m_extProp.get("resourcebroker");
    if(temp != null)  {
        return temp.toString();
    }
    else  {
      return "";
    }

  }

  /** Returns all resource Broker found in 'dbsetupscripts.properties' */
  public Vector getResourceBrokers() {
      Vector values = new Vector();

      String value = m_dbSetupProps.getProperty("resourceBrokers");
      StringTokenizer tokenizer = new StringTokenizer(value,",");
      while(tokenizer.hasMoreTokens())  {
          values.add(tokenizer.nextToken());
      }
      return values;
  }



  /** Sets the connection string to the database to the given value */
  public void setDbWorkConStr(String dbWorkConStr)  {
    setProperties("pool." + getResourceBroker() + ".url",dbWorkConStr);
    setProperties("pool." + getResourceBroker() + "backup.url",dbWorkConStr);
    setProperties("pool." + getResourceBroker() + "online.url",dbWorkConStr);
  }


  /** Sets the user of the database to the given value */
  public void setDbWorkUser(String dbWorkUser)  {
    setProperties("pool." + getResourceBroker() + ".user",dbWorkUser);
    setProperties("pool." + getResourceBroker() + "backup.user",dbWorkUser);
    setProperties("pool." + getResourceBroker() + "online.user",dbWorkUser);
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
    setProperties("pool." + getResourceBroker() + "backup.password",dbWorkPwd);
    setProperties("pool." + getResourceBroker() + "online.password",dbWorkPwd);
  }

  /** Returns a conenction string */
  public String getDbWorkConStr()  {
    Object ConStr =  m_extProp.get("pool." + getResourceBroker() + ".url");
    if(ConStr != null)  {
      return ConStr.toString();
    }
    else  {
      return "";
    }
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

  /** Returns the path to the opencms config folder */
  public String getConfigFolder() {
    return (m_basePath + "WEB-INF/config/").replace('\\','/').replace('/',File.separatorChar);
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
  public void setHistoryEnabled(String historyEnabled)  {
    setProperties("history.enabled",historyEnabled);
  }

  /** Returns the value for deleting published project parameters */
  public String getHistoryEnabled()  {
    Object temp =  m_extProp.get("history.enabled");
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

  /** Sets the value for exportpoint nr to the given value */
  public void setExportPoint(String exportPoint, int nr)  {
    setProperties("exportpoint."+nr,exportPoint);
  }

  /** Returns the value for exportpoint nr */
  public String getExportPoint(int nr)  {
    Object temp =  m_extProp.get("exportpoint."+nr);
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return null;
    }
  }

  /** Sets the value for exportpoint path nr to the given value */
  public void setExportPointPath(String exportPointPath, int nr)  {
    setProperties("exportpoint.path."+nr,exportPointPath);
  }

  /** Returns the value for exportpoint path nr */
  public String getExportPointPath(int nr)  {
    Object temp =  m_extProp.get("exportpoint.path."+nr);
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return null;
    }
  }


  /** Sets the value for redirect nr to the given value */
  public void setRedirect(String redirect, int nr)  {
    setProperties("redirect."+nr,redirect);
  }

  /** Returns the value for redirect nr */
  public String getRedirect(int nr)  {
    Object temp =  m_extProp.get("redirect."+nr);
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return null;
    }
  }

  /** Sets the value for redirect location nr to the given value */
  public void setRedirectLocation(String redirectLocation, int nr)  {
    setProperties("redirectlocation."+nr, redirectLocation);
  }

  /** Returns the value for redirect location nr */
  public String getRedirectLocation(int nr)  {
    Object temp =  m_extProp.get("redirectlocation."+nr);
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return null;
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

  /** Enables/Disables channel opencms_elementcache in the log messages */
  public void setLoggingChannelOpencms_elementcache(String loggingChannelOpencms_elementcache)  {
    setProperties("log.channel.opencms_elementcache",loggingChannelOpencms_elementcache);
  }

  /** Indicates if channel opencms_elementcache is enabled in the log messages */
  public String getLoggingChannelOpencms_elementcache()  {
    Object temp =  m_extProp.get("log.channel.opencms_elementcache");
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


  public void setElementCache(String elementCache)  {
      setProperties("elementcache.enabled",elementCache);
  }

  public String getElementCache() {
      Object temp = m_extProp.get("elementcache.enabled");
      if(temp!=null)  {
          return temp.toString();
      }
      else  {
         return "";
      }
  }

  public void setElementCacheURI(String elementCacheURI)  {
      setProperties("elementcache.uri",elementCacheURI);
  }

  public String getElementCacheURI() {
      Object temp = m_extProp.get("elementcache.uri");
      if(temp!=null)  {
          return temp.toString();
      }
      else  {
         return "";
      }
  }

  public void setElementCacheElements(String elementCacheElements)  {
      setProperties("elementcache.elements",elementCacheElements);
  }

  public String getElementCacheElements() {
      Object temp = m_extProp.get("elementcache.elements");
      if(temp!=null)  {
          return temp.toString();
      }
      else  {
         return "";
      }
  }

  public void setElementCacheVariants(String elementCacheVariants)  {
      setProperties("elementcache.variants",elementCacheVariants);
  }

  public String getElementCacheVariants() {
      Object temp = m_extProp.get("elementcache.variants");
      if(temp!=null)  {
          return temp.toString();
      }
      else  {
         return "";
      }
  }

  public String getDbCreateConStr()   {
      Object constr = m_dbSetupProps.get(getResourceBroker()+".constr");
      if(constr != null)  {
          return constr.toString();
      }
      else  {
          return "";
      }
  }

  public void setDbCreateConStr(String dbCreateConStr)  {
      m_dbSetupProps.put(getResourceBroker()+".constr",dbCreateConStr);
  }

  public String getDbCreateUser()   {
      Object constr = m_dbSetupProps.get(getResourceBroker()+".user");
      if(constr != null)  {
          return constr.toString();
      }
      else  {
          return "";
      }
  }

  public void setDbCreateUser(String dbCreateUser)  {
      m_dbSetupProps.put(getResourceBroker()+".user",dbCreateUser);
  }

  public String getDbCreatePwd()   {
      if(m_dbCreatePwd != null)  {
          return m_dbCreatePwd;
      }
      else  {
          return "";
      }
  }

  public void setDbCreatePwd(String dbCreatePwd)  {
      m_dbCreatePwd = dbCreatePwd;
  }


  public boolean getWizardEnabled()  {
      Object temp =  m_extProp.get("wizard.enabled");
      if(temp != null)  {
          if(temp.toString().equals("true"))  {
              return true;
          }
          else  {
              return false;
          }
      }
      else  {
        return true;
      }
  }

  public void lockWizard()  {
      setProperties("wizard.enabled","false");
  }

  public Properties getDbSetupProps()  {
      return m_dbSetupProps;
  }

  public String getDb() {
    return m_database;
  }

  public void setDb(String db)  {
    m_database = db;
  }

  public Hashtable getReplacer() {
      return m_replacer;
  }

  public void setReplacer(Hashtable replacer)  {
      m_replacer = replacer;
  }

  public String getStaticExport() {
    Object temp = m_extProp.get("staticexport.enabled");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  public void setStaticExport(String staticExport)  {
    setProperties("staticexport.enabled",staticExport);
  }


  public String getStaticExportPath() {
    Object temp = m_extProp.get("staticexport.path");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  public void setStaticExportPath(String staticExportPath)  {
    setProperties("staticexport.path",staticExportPath);
  }


  public String getUrlPrefixExport() {
    Object temp = m_extProp.get("url_prefix_export");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  public void setUrlPrefixExport(String urlPrefixExport)  {
    setProperties("url_prefix_export",urlPrefixExport);
  }


  public String getUrlPrefixHttp() {
    Object temp = m_extProp.get("url_prefix_http");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  public void setUrlPrefixHttp(String urlPrefixHttp)  {
    setProperties("url_prefix_http",urlPrefixHttp);
  }


  public String getUrlPrefixHttps() {
    Object temp = m_extProp.get("url_prefix_https");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  public void setUrlPrefixHttps(String urlPrefixHttps)  {
    setProperties("url_prefix_https",urlPrefixHttps);
  }


  public String getUrlPrefixServername() {
    Object temp = m_extProp.get("url_prefix_servername");
    if(temp != null)  {
      return temp.toString();
    }
    else  {
      return "";
    }
  }

  public void setUrlPrefixServername(String urlPrefixServername)  {
    setProperties("url_prefix_servername",urlPrefixServername);
  }

}
