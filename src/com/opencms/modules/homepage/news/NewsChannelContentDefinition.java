package com.opencms.modules.homepage.news;

import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.template.*;
import com.opencms.defaults.*;
import com.opencms.file.mySql.*;

import java.util.*;
import java.io.*;
import java.sql.*;

/**
 * Content Definition for News Channels
 */
public class NewsChannelContentDefinition extends A_ContentDefinition implements I_CmsContent {
	private int m_id = -1;
	private String m_name = "";
	private String m_descr = "";
	private String m_lockstate = "";
	private static String m_lsvalue="lockuser"; // default for new enties;
	private boolean newElement = false;

	private static CmsDbPool m_pool = null;
	//private static NewsKeyAccess m_NewsKeyAccess = null;

	// Constants for table news_channel
	private static Integer C_SELECT_ALL_KEY = new Integer(1);
	private static String  C_SELECT_ALL = "select * from news_channel";
	private static Integer C_INSERT_CHANNEL_KEY = new Integer(2);
	private static String  C_INSERT_CHANNEL = "insert into news_channel values(?,?,?,?)";
	private static Integer C_DELETE_CHANNEL_KEY = new Integer(3);
	private static String  C_DELETE_CHANNEL = "delete from news_channel where ID = ?";
	private static Integer C_COUNT_KEY = new Integer(4);
	private static String  C_COUNT = "select count(*) from news_channel";
	private static Integer C_SELECT_ID_KEY = new Integer(5);
    private static String  C_SELECT_ID = "select * from news_channel where ID = ?";
	private static Integer C_GET_ALL_KEY = new Integer(6);
	private static String  C_GET_ALL = "select * from news_channel order by name";
	private static Integer C_UPDATE_CHANNEL_KEY = new Integer(7);
	private static String  C_UPDATE_CHANNEL = "update news_channel set name = ?, description = ?, lockstate = ? where id = ?";
	private static Integer C_SELECT_NAME_KEY = new Integer(8);
	private static String  C_SELECT_NAME = "select * from news_channel where name = ?";

	public static int	  C_TABLE_CHANNELS = 1;   // ID 1 lead to the next ID of table news_channels

	/**
	 * Constructor
	 */
	public NewsChannelContentDefinition() {
		super();
	}

	/**
	 * Constructor to read an Element from the database
	 * is needed by A_Backoffice like this!
	 */
	public NewsChannelContentDefinition(CmsObject cms, Integer ID) throws CmsException {
		//System.err.println("NewsChannelContentDefinition: will lesen, ID= !"+ID);
		PreparedStatement statement = null;
		ResultSet res = null;
		getPool();    // get the pool first
		try {
		 	statement = m_pool.getPreparedStatement(C_SELECT_ID_KEY);
			statement.setInt(1, ID.intValue());  // choose id of the Element  "where id == "
			res = statement.executeQuery();
			if(res.next()){
				// read the items from the resultset
				m_id = res.getInt(1);
				m_name = res.getString(2);
				m_descr = res.getString(3);
				m_lockstate = res.getString(4);
			}
		}catch(SQLException e) {
			throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(),
				CmsException.C_SQL_ERROR, e);
		}finally{
			if(statement != null) {
				m_pool.putPreparedStatement(C_SELECT_ID_KEY, statement);
			}
			try{
				res.close();
			}catch (Exception SqlE) {
			}
		}
	}

	/**
	 * Constructor to read an Element from the database
	 * is needed by A_Backoffice like this!
	 */
	public NewsChannelContentDefinition(String name) throws CmsException {
		//System.err.println("NewsChannelContentDefinition: will lesen, name= "+name);
		PreparedStatement statement = null;
		ResultSet res = null;
		getPool();    // get the pool first
		try {
		 	statement = m_pool.getPreparedStatement(C_SELECT_NAME_KEY);
			statement.setString(1, name);  // choose id of the Element  "where id == "
			res = statement.executeQuery();
			if(res.next()){
				// read the items from the resultset
				m_id = res.getInt(1);
				m_name = res.getString(2);
				m_descr = res.getString(3);
				m_lockstate = res.getString(4);
			}
		}catch(SQLException e) {
			throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(),
				CmsException.C_SQL_ERROR, e);
		}finally{
			if(statement != null) {
				m_pool.putPreparedStatement(C_SELECT_NAME_KEY, statement);
			}
			try{
				res.close();
			}catch (Exception e) {
				System.err.println("[" + this.getClass().getName() + "] " + e.getMessage());
			}
		}
	}

	/**
	 * Constructor to create an Element
	 *
	 * @param name
	 * @param descr
	 */
	public NewsChannelContentDefinition(String name, String descr) {
		//System.err.println("NewsChannelContentDefinition: will erzeugen!");
		getPool();
		m_name = name;
		m_descr = descr;
		m_lockstate = m_lsvalue;
		newElement = true;
		try{
			m_id = NewsKeyAccess.getNextId(C_TABLE_CHANNELS);
		} catch(CmsException e){
			//System.err.println("Exception in NewsChannelContentDefinition.NewsChannelContentDefinition(String, String)!");
			System.err.println("[" + this.getClass().getName() + "] " + e.getMessage());
		}
	}

	/**
	 * Constructor to create an Element with a certain ID
	 * needed to create a list of Elementes
	 *
	 * @param id    The unique id
	 * @param name  The name
	 * @param descr The descr
	 */
	private NewsChannelContentDefinition(int id, String name, String descr, String lockstate) {
		m_id = id;
		m_name = name;
		m_descr = descr;
		m_lockstate = lockstate;
	}

	/**
	 * write an Element to the database
	 * @param cms An instance of CmsObject
	 */
	public void write(CmsObject cms) throws CmsException{
		PreparedStatement statement = null;
		if(newElement == true) {
			try {
				// write the data to database
		 		statement = m_pool.getPreparedStatement(C_INSERT_CHANNEL_KEY);
				statement.setInt(1, m_id);
				statement.setString(2, m_name);
				statement.setString(3, m_descr);
				statement.setString(4, m_lockstate);
				statement.execute();
				newElement = false;
			}catch(SQLException e) {
				throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(),
					CmsException.C_SQL_ERROR, e);
			}finally{
				if(statement != null) {
					m_pool.putPreparedStatement(C_INSERT_CHANNEL_KEY, statement);
				}
			}
		}else {
			update(cms);
		}
	}

	/**
	 * delete an Element from the database
	 *
	 * @param cms An instance of CmsObject
	 */
	public void delete(CmsObject cms) throws CmsException {
		PreparedStatement statement = null;
		ResultSet res = null;
		if(!NewsContentDefinition.channelIsUsed(m_id)){
		try {
			// delet data from database
		 	statement = m_pool.getPreparedStatement(C_DELETE_CHANNEL_KEY);
			statement.setInt(1, m_id);
			statement.execute();
		}catch(Exception e) {
			throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(),
			CmsException.C_SQL_ERROR, e);
		}finally{
			if(statement != null) {
				m_pool.putPreparedStatement(C_DELETE_CHANNEL_KEY, statement);
			}
		}
		}else{
			// System.err.println("NewsChannelContentDefinition.delete() Channel " +m_id+ " wird noch verwendet und kann nicht gelöscht werden!");
		  throw new CmsException("The channel is still in use and can't be deleted!");
    }
	}

	private void update(CmsObject cms) throws CmsException {
		PreparedStatement statement = null;
		ResultSet res = null;
		try {
			// write the data to database
		 	statement = m_pool.getPreparedStatement(C_UPDATE_CHANNEL_KEY);
			statement.setString(1, m_name);
			statement.setString(2, m_descr);
			statement.setString(3, m_lockstate);
			statement.setInt(4, m_id);
			res = statement.executeQuery();
		}catch(Exception e) {
			throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(),
				CmsException.C_SQL_ERROR, e);
		}finally{
			if(statement != null) {
				m_pool.putPreparedStatement(C_UPDATE_CHANNEL_KEY, statement);
			}
			try{
				res.close();
			}catch (SQLException e) {
				System.err.println("[" + this.getClass().getName() + "] " + e.getMessage());
			}
		}
	}

	/**
	 * create a pool if neccessary
	 *
	 * @return m_pool
	 */
	private static CmsDbPool getPool() {
		if(m_pool == null) {
			createPool();
		}
		return m_pool;
	}

	/**
	 * create the pool and initialize the statements
	 */
	private static synchronized void createPool() {
		try{
		m_pool = new CmsDbPool("org.gjt.mm.mysql.Driver", "jdbc:mysql://localhost:3306/opencms41", "root", "", 5);

		m_pool.initPreparedStatement(C_SELECT_ALL_KEY, C_SELECT_ALL);
		m_pool.initPreparedStatement(C_INSERT_CHANNEL_KEY, C_INSERT_CHANNEL);
		m_pool.initPreparedStatement(C_DELETE_CHANNEL_KEY, C_DELETE_CHANNEL);
		m_pool.initPreparedStatement(C_COUNT_KEY, C_COUNT);
		m_pool.initPreparedStatement(C_SELECT_ID_KEY, C_SELECT_ID);
		m_pool.initPreparedStatement(C_GET_ALL_KEY, C_GET_ALL);
		m_pool.initPreparedStatement(C_UPDATE_CHANNEL_KEY, C_UPDATE_CHANNEL);
		m_pool.initPreparedStatement(C_SELECT_NAME_KEY, C_SELECT_NAME);
		}
		catch (CmsException e){
			System.err.println("[NewsChannelContentDefinition.createPool()] " + e.getMessage());
		};
	}

	/**
	 * declare the entries that should be displayed in the Backoffice list
	 *
	 * @return a Vector
	 */
	public static Vector getFieldMethods(CmsObject cms) {
		Vector methods = new Vector();
		try {
			methods.addElement(NewsChannelContentDefinition.class.getMethod("getId", new Class[0]));
			methods.addElement(NewsChannelContentDefinition.class.getMethod("getName", new Class[0]));
			methods.addElement(NewsChannelContentDefinition.class.getMethod("getDescription", new Class[0]));
		}catch(NoSuchMethodException e) {
			System.err.println("Exception in NewsChannelContentDefinition.getFieldMethods(CmsObject)!");
		}
		return methods;
	}

	/**
	 * declare the Names of the fields
	 *
	 * @return a Vector
	 */
	public static Vector getFieldNames(CmsObject cms) {
		Vector names = new Vector();
		names.addElement("Id");
		names.addElement("Name");
		names.addElement("Description");
		return names;
	}

	/**
	 * declare the methods to filter the list entries
	 *
	 * @return A Vector
	 */
	public static Vector getFilterMethods(CmsObject cms) {
		Vector filterMethods = new Vector();
		try {
			filterMethods.addElement(new FilterMethod("Show all", NewsChannelContentDefinition.class.getMethod("getChannelList", new Class[] {}), new Object[] {}));
		}catch(NoSuchMethodException e) {
			System.err.println("Exception in NewsChannelContentDefinition.getFilterMethods(CmsObject)!");
		}
		return filterMethods;
	}

	/**
	 * one filter method
	 * this method creates a list of all enties
	 *
	 * @return a Vector with all Elementes
	 */
	public static Vector getChannelList(){
		Vector list = new Vector();
		PreparedStatement statement = null;
		ResultSet res = null;
		int id = -1;
		String name = null;
		String descr = null;
		String lockstate = null;
		getPool();           // get a pool first
		try {
		 	statement = m_pool.getPreparedStatement(C_GET_ALL_KEY);
			res = statement.executeQuery();
			while(res.next()){
				// read the item from the resultset
				id = res.getInt(1);
				name = res.getString(2);
				descr = res.getString(3);
				lockstate = res.getString(4);
				// insert Element into list
				list.addElement(new NewsChannelContentDefinition(id, name, descr, lockstate));
			}
		}catch(Exception e) {
			System.err.println("Exception in NewsChannelContentDefinition.getChannelList()!");
		}finally{
			if(statement != null) {
				m_pool.putPreparedStatement(C_GET_ALL_KEY, statement);
			}
			try{
				res.close();
			}catch (SQLException e) {
				System.err.println("Exception in NewsChannelContentDefinition.getChannelList(CmsObject)!"+ e.getMessage());

			}
		}
		return list;
	}

	/**
	 * get the id
	 * @return String
	 */
	public String getId() {
		return ""+m_id;
	}

	public int getIntId() {
		return m_id;
	}

	/**
	 * get the name
	 * @return the name
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * set the name
	 * @param String name
	 */
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * get the description
	 * @return the description (String)
	 */
	public String getDescription() {
		return m_descr;
	}

	/**
	 * set the description
	 * @param String description
	 */
	public void setDescription(String description) {
		m_descr = description;
	}

	/**
	 * sets the lockstate of the CD.
	 * @param lockstate The lockstate of this CD object.
	 * (possibles states: locked by you,locked by other, unlocked)
	 */
	public void setLockstate(String lockstate) {
		m_lockstate = lockstate;
		try{
			update(null);
		}catch(CmsException e) {
			//System.err.println("NewsChannelContentDefinition: CmsException in setLockstate()!");
			System.err.println("[" + this.getClass().getName() + "] " + e.getMessage());
		}
	}

	/**
	 * gets the lockstate of the CD
	* (possibles states: locked by user,locked by other, unlocked)
	*/
	public String getLockstate() {
		return m_lockstate;
	}

	/**
	* if the content should be lockable
	* the method returns true
	* @returns a boolean
	*/
	public static boolean isLockable() {
		return true;
	}

	/**
	 * get the unique Id (for abstract Backoffice class)
	 *
	 * @return m_id The unique id (as a String)
	 */
	public String getUniqueId(CmsObject cms) {
		return ""+m_id;
	}
}
