package com.opencms.modules.homepage.news;

import com.opencms.core.*;
//import com.opencms.file.*;
//import com.opencms.template.*;
//import com.opencms.defaults.*;
import com.opencms.file.mySql.*;

import java.util.*;
import java.io.*;
import java.sql.*;

/**
 * This class provides a method that return unique Id's which can be used for
 * the entries in the database.
 */
public class NewsKeyAccess {

	private static CmsDbPool m_pool = null;

	// Constants for table news_keys
	public static Integer C_NEWS_KEYS_LOCK_KEY = new Integer(21);
	public static String  C_NEWS_KEYS_LOCK = "lock table news_keys write";
	public static Integer C_NEWS_KEYS_READ_KEY = new Integer(22);
	public static String  C_NEWS_KEYS_READ = "select next_id from news_keys where table_key = ?";
	public static Integer C_NEWS_KEYS_WRITE_KEY = new Integer(23);
	public static String  C_NEWS_KEYS_WRITE = "update news_keys set next_id = ? where table_key = ? ";
	public static Integer C_NEWS_KEYS_UNLOCK_KEY = new Integer(24);
	public static String  C_NEWS_KEYS_UNLOCK = "unlock tables ";

	public static int	  C_TABLE_CHANNELS = 1;   // ID 1 lead to the next ID of table news_channels


	/**
	 * Private method to get the next id for a table.
	 * This method is synchronized, to generate unique id's.
	 *
	 * @param key A key for the table to get the max-id from.
	 * @return next-id The next possible id for this table.
	 */
	public static synchronized int getNextId(int key) throws CmsException {
		//System.err.println("getNextId: key= " + key);
		getPool();
		int newId = -1;
		PreparedStatement statement = null;
		ResultSet res = null;
		try {
			// lock the table first
			statement = ((com.opencms.file.mySql.CmsDbPool)m_pool).getIdStatement(C_NEWS_KEYS_LOCK_KEY);
			statement.executeUpdate();
			// try to read the value...
			statement = ((com.opencms.file.mySql.CmsDbPool)m_pool).getIdStatement(C_NEWS_KEYS_READ_KEY);
			statement.setInt(1,key);
			res = statement.executeQuery();
			if (res.next()){
				newId = res.getInt(1);
				res.close();
			}else{
				System.err.println("[NewsKeyAccess.getNextId] cant read Id!");
				 //throw new CmsException("[" + this.getClass().getName() + "] "+" cant read Id! ",CmsException.C_NO_GROUP);
			}
			// increase the number (id++)
			statement = ((com.opencms.file.mySql.CmsDbPool)m_pool).getIdStatement(C_NEWS_KEYS_WRITE_KEY);
			statement.setInt(1,newId+1);
			statement.setInt(2,key);
			statement.executeUpdate();
			// unlock the table
			statement = ((com.opencms.file.mySql.CmsDbPool)m_pool).getIdStatement(C_NEWS_KEYS_UNLOCK_KEY);
			statement.executeUpdate();

		} catch (SQLException e){
			System.err.println("[NewsKeyAccess.getNextId()] " + e.getMessage());
			//throw new CmsException("["+this.getClass().getName()+"] "+e.getMessage(),CmsException.C_SQL_ERROR, e);
		}
		return(	newId );
	}

	/**
	 * create the pool and initialize the statements
	 */
	private static synchronized void createPool() {
		try{
		m_pool = new CmsDbPool("org.gjt.mm.mysql.Driver", "jdbc:mysql://localhost:3306/opencms41", "root", "", 5);

		m_pool.initIdStatement(C_NEWS_KEYS_LOCK_KEY, C_NEWS_KEYS_LOCK);
		m_pool.initIdStatement(C_NEWS_KEYS_READ_KEY, C_NEWS_KEYS_READ);
		m_pool.initIdStatement(C_NEWS_KEYS_WRITE_KEY, C_NEWS_KEYS_WRITE);
		m_pool.initIdStatement(C_NEWS_KEYS_UNLOCK_KEY, C_NEWS_KEYS_UNLOCK);
		}
		catch (CmsException e){
			System.err.println("[NewsKeyAccess.createPool()] " + e.getMessage());
		};
	}

	/**
	 * create a pool if neccessary
	 * @return m_pool
	 */
	private static CmsDbPool getPool() {
		if(m_pool == null) {
			createPool();
		}
		return m_pool;
	}
}
