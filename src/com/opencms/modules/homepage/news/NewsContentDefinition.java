package com.opencms.modules.homepage.news;

import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.template.*;
import com.opencms.defaults.*;
import com.opencms.file.mySql.*;

import java.util.*;
import java.text.*;
import java.io.*;
import java.sql.*;

/**
 * This is the Content Definition for the news entries.
 * The class privides some constructors filter methods to access entries as well
 * as some methods to convert dates and strings.
 */
public class NewsContentDefinition extends A_ContentDefinition implements I_CmsContent {

	// constants for day or month selection
	private static Integer C_FLAG_DAY = new Integer(0);
	private static Integer C_FLAG_MONTH = new Integer(1);

	// the news entry data...
	private int m_id = -1;
	private String m_headline = "";
	private String m_description = "";
	private String m_a_info1 = "";
	private String m_a_info2 = "";
	private String m_a_info3 = "";
	private String m_text = "";
	private String m_author = "";
	private String m_link = "";
	private String m_linkText = "";
	private GregorianCalendar m_date = null;
	private int m_channel = -1;
	private String m_lockstate = "";

	private boolean newElement = false;  // indicates if an element should be written or updated

	private static String m_lsvalue="lockuser"; // default for new enties;
	private static CmsDbPool m_pool = null;
	// private static NewsKeyAccess m_NewsKeyAccess = null;

	// Constants for table news_entry
	private static Integer C_SELECT_ALL_KEY = new Integer(1);
	private static String  C_SELECT_ALL = "select * from news_entry";
	private static Integer C_INSERT_ENTRY_KEY = new Integer(2);
	private static String  C_INSERT_ENTRY = "insert into news_entry values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static Integer C_DELETE_ENTRY_KEY = new Integer(3);
	private static String  C_DELETE_ENTRY = "delete from news_entry where ID = ?";
	private static Integer C_COUNT_KEY = new Integer(4);
	private static String  C_COUNT = "select count(*) from news_entry";
	private static Integer C_SELECT_ID_KEY = new Integer(5);
    private static String  C_SELECT_ID = "select * from news_entry where ID = ?";
	private static Integer C_GET_ALL_KEY = new Integer(6);
	private static String  C_GET_ALL = "select * from news_entry";
	private static Integer C_UPDATE_ENTRY_KEY = new Integer(7);
	private static String  C_UPDATE_ENTRY = "update news_entry set headline = ?, description = ?, text = ?, author = ?, link = ?, linkText = ?, date = ?, lockstate = ?, channel = ?, a_info1 = ?, a_info2 = ?, a_info3 = ? where id = ?";
	private static Integer C_ORDER_KEY = new Integer(8);
	private static String  C_ORDER = "select * from news_entry order by ? desc";
	private static Integer C_SELECT_CHANNEL_ID_KEY = new Integer(9);
	private static String  C_SELECT_CHANNEL_ID = "select count(*) from news_entry where channel = ?";
	private static Integer C_SELECT_MONTH_KEY = new Integer(10);
	private static String  C_SELECT_MONTH = "select * from news_entry where month(date)= ? and channel = ? order by date desc";
	private static Integer C_SELECT_DAY_KEY = new Integer(11);
	private static String  C_SELECT_DAY = "select * from news_entry where date = ? and channel = ?";
	private static Integer C_SELECT_FIRST_N_OF_CHANNEL_KEY = new Integer(12);
	private static String  C_SELECT_FIRST_N_OF_CHANNEL = "select * from news_entry where channel = ? order by date desc";

	public static int	  C_TABLE_NEWS = 2;   // ID 2 leads to the next ID of table news_entry

	/**
	 * Constructor
	 */
	public NewsContentDefinition() {
		super();
	}

	/**
	 * Constructor to read an Element from the database
	 * is needed by A_Backoffice like this!
	 */
	public NewsContentDefinition(CmsObject cms, Integer ID) throws CmsException {
		java.util.Date date = null;
		m_date = new GregorianCalendar();
		//System.err.println("NewsContentDefinition: will lesen, ID= "+ID);
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
				m_headline = res.getString(2);
				m_description = res.getString(3);
				m_text = res.getString(4);
				m_author = res.getString(5);
				m_link = res.getString(6);
				m_linkText = res.getString(7);
				java.util.Date tempDate = ((java.util.Date)res.getDate(8));
				m_date.setTime( tempDate ); // SQLDate to Date to GregorianCalendar
				//System.err.println("NewsContentDefinition" + m_date.get(Calendar.DATE)+"." + m_date.get(Calendar.MONTH)+"." + m_date.get(Calendar.YEAR) );
				m_lockstate = res.getString(9);
				m_channel = res.getInt(10);
				m_a_info1 = res.getString(11);
				m_a_info2 = res.getString(12);
				m_a_info3 = res.getString(13);
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
			}catch (Exception e) {
				//System.err.println("NewsContentDefinition: Exception caught");
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
	public NewsContentDefinition(String head,
								 String descr,
								 String text,
								 String author,
								 String link,
								 String linkText,
								 GregorianCalendar date,
								 int channel,
								 String a_info1,
								 String a_info2,
								 String a_info3) {
		//System.err.println("NewsContentDefinition: will erzeugen!");
		getPool();
		m_headline = head;
		m_description = descr;
		m_text = text;
		m_author = author;
		m_link = link;
		m_linkText = linkText;
		m_date = date;
		m_channel = channel;
		m_a_info1 = a_info1;
		m_a_info2 = a_info2;
		m_a_info3 = a_info3;
		m_lockstate = m_lsvalue;  // default
		newElement = true;        // this is a new Element
		try{
			m_id = NewsKeyAccess.getNextId(C_TABLE_NEWS);
		} catch(CmsException e){
			//System.err.println("Exception in NewsContentDefinition.NewsContentDefinition(String, String)!");
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
	private NewsContentDefinition(int id,
								  String headline,
								  String descr,
								  String text,
								  String author,
								  String link,
								  String linkText,
								  java.sql.Date date,
								  String lockstate,
								  int channel,
								  String a_info1,
								  String a_info2,
								  String a_info3) {
		m_id = id;
		m_headline = headline;
		m_description = descr;
		m_text = text;
		m_author = author;
		m_link = link;
		m_linkText = linkText;
		m_date = new GregorianCalendar();
		m_date.setTime( (java.util.Date)date ); // SQLDate to Date to GregorianCalendar
		m_channel = channel;
		m_a_info1 = a_info1;
		m_a_info2 = a_info2;
		m_a_info3 = a_info3;
		m_lockstate = lockstate;
	}

	/**
	 * write an Element to the database
	 * @param cms An instance of CmsObject
	 */
	public void write(CmsObject cms) throws Exception{
		PreparedStatement statement = null;
		ResultSet res = null;
		java.util.Date uDate = m_date.getTime();  	// GregorianCalendar to Date
		java.sql.Date sqlDate = new java.sql.Date(uDate.getTime());  // Date to SQLDate
		if(newElement == true) {
			// Element not jet in Database -> insert Statement
			try {
				// write the data to database
		 		statement = m_pool.getPreparedStatement(C_INSERT_ENTRY_KEY);
				statement.setInt(1, m_id);
				statement.setString(2, m_headline);
				statement.setString(3, m_description);
				statement.setString(4, m_text);
				statement.setString(5, m_author);
				statement.setString(6, m_link);
				statement.setString(7, m_linkText);
				statement.setDate(8, sqlDate);
				statement.setString(9, m_lockstate);
				statement.setInt(10, m_channel);
				statement.setString(11, m_a_info1);
				statement.setString(12, m_a_info2);
				statement.setString(13, m_a_info3);
				statement.execute();
				newElement = false; // element is no longer new!
			}catch(Exception e) {
				throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(),
				CmsException.C_SQL_ERROR, e);
			}finally{
				if(statement != null) {
					m_pool.putPreparedStatement(C_INSERT_ENTRY_KEY, statement);
				}
			}
		}else {
			// Element is already in Database -> update statement
			update(cms, sqlDate);
		}
	}

	/**
	 * method to updata an existing entry.
	 * This method is only called by the write()-method.
	 * @param CmsObject
	 * @param the new date (java.sql.Date)
	 */
	private void update(CmsObject cms, java.sql.Date sqlDate) throws CmsException {
		PreparedStatement statement = null;
		ResultSet res = null;
		try {
			// write the data to database
		 	statement = m_pool.getPreparedStatement(C_UPDATE_ENTRY_KEY);
			statement.setString(1, m_headline);
			statement.setString(2, m_description);
			statement.setString(3, m_text);
			statement.setString(4, m_author);
			statement.setString(5, m_link);
			statement.setString(6, m_linkText);
			statement.setDate(7, sqlDate);
			statement.setString(8, m_lockstate);
			statement.setInt(9, m_channel);
			statement.setString(10, m_a_info1);
			statement.setString(11, m_a_info2);
			statement.setString(12, m_a_info3);
			statement.setInt(13, m_id);
			statement.execute();
		}catch(Exception e) {
			throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(),
				CmsException.C_SQL_ERROR, e);
		}finally{
			if(statement != null) {
				m_pool.putPreparedStatement(C_UPDATE_ENTRY_KEY, statement);
			}
		}
	}

	/**
	 * delete an Element from the database
	 * @param cms An instance of CmsObject
	 */
	public void delete(CmsObject cms) throws CmsException {
		PreparedStatement statement = null;
		ResultSet res = null;
		try {
			// delet data from database
		 	statement = m_pool.getPreparedStatement(C_DELETE_ENTRY_KEY);
			statement.setInt(1, m_id);
			statement.execute();
		}catch(Exception e) {
			throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(),
			CmsException.C_SQL_ERROR, e);
		}finally{
			if(statement != null) {
				m_pool.putPreparedStatement(C_DELETE_ENTRY_KEY, statement);
			}
		}
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

	/**
	 * create the pool and initialize the statements
	 */
	private static synchronized void createPool() {
		try{
		m_pool = new CmsDbPool("org.gjt.mm.mysql.Driver", "jdbc:mysql://localhost:3306/opencms41", "root", "", 5);

		m_pool.initPreparedStatement(C_SELECT_ALL_KEY, C_SELECT_ALL);
		m_pool.initPreparedStatement(C_INSERT_ENTRY_KEY, C_INSERT_ENTRY);
		m_pool.initPreparedStatement(C_DELETE_ENTRY_KEY, C_DELETE_ENTRY);
		m_pool.initPreparedStatement(C_COUNT_KEY, C_COUNT);
		m_pool.initPreparedStatement(C_SELECT_ID_KEY, C_SELECT_ID);
		m_pool.initPreparedStatement(C_GET_ALL_KEY, C_GET_ALL);
		m_pool.initPreparedStatement(C_UPDATE_ENTRY_KEY, C_UPDATE_ENTRY);
		m_pool.initPreparedStatement(C_ORDER_KEY, C_ORDER);
		m_pool.initPreparedStatement(C_SELECT_CHANNEL_ID_KEY, C_SELECT_CHANNEL_ID);
		m_pool.initPreparedStatement(C_SELECT_MONTH_KEY, C_SELECT_MONTH);
		m_pool.initPreparedStatement(C_SELECT_DAY_KEY, C_SELECT_DAY);
		m_pool.initPreparedStatement(C_SELECT_FIRST_N_OF_CHANNEL_KEY, C_SELECT_FIRST_N_OF_CHANNEL);
		}
		catch (CmsException e){
			System.err.println("NewsContentDefinition: create Pool caught CmsException!"+ e.getMessage());
		};
	}

	/**
	 * declare the entries that should be displayed in the Backoffice list
	 * @return a Vector
	 */
	public static Vector getFieldMethods(CmsObject cms) {
		Vector methods = new Vector();
		try {
			methods.addElement(NewsContentDefinition.class.getMethod("getHeadline", new Class[0]));
			methods.addElement(NewsContentDefinition.class.getMethod("getDescription", new Class[0]));
			methods.addElement(NewsContentDefinition.class.getMethod("getAuthor", new Class[0]));
			methods.addElement(NewsContentDefinition.class.getMethod("getLink", new Class[0]));
			methods.addElement(NewsContentDefinition.class.getMethod("getDate", new Class[0]));
			methods.addElement(NewsContentDefinition.class.getMethod("getChannel", new Class[0]));
		}catch(NoSuchMethodException e) {
			System.err.println("Exception in NewsContentDefinition.getFieldMethods(CmsObject)!"+ e.getMessage());
		}
		return methods;
	}

	/**
	 * declare the Names of the fields
	 * @return a Vector
	 */
	public static Vector getFieldNames(CmsObject cms) {
		Vector names = new Vector();
		names.addElement("Headline");
		names.addElement("Description");
		names.addElement("Author");
		names.addElement("Link");
		names.addElement("Date");
		names.addElement("Channel");
		return names;
	}

	/**
	 * declare the methods to filter the list entries
	 * @return A Vector
	 */
	public static Vector getFilterMethods(CmsObject cms) {
		Vector filterMethods = new Vector();
		Vector channels = NewsChannelContentDefinition.getChannelList(); // get all actual channels
		String showText = "";
		Integer actChannelId = null;  // has to be Integer because of Reflection!
	// add the always shown first filterMethod...
		try {
			filterMethods.addElement(new FilterMethod("Sort all by", NewsContentDefinition.class.getMethod("getSortedList", new Class[] {String.class}), new Object[] {}));
		}catch(NoSuchMethodException e) {
			System.err.println("Exception in NewsContentDefinition.getFilterMethods(CmsObject)!"+ e.getMessage());
		}
		// try to add some filters dynamically
		for(int i=0; i< channels.size(); i++) {
			showText = ((NewsChannelContentDefinition)channels.elementAt(i)).getName();
			actChannelId = new Integer(((NewsChannelContentDefinition)channels.elementAt(i)).getIntId());
			try {
				filterMethods.addElement(new FilterMethod(showText +": list first ...", NewsContentDefinition.class.getMethod("getNewsList", new Class[] {Integer.class, String.class}), new Object[] {actChannelId}, "2"));
				filterMethods.addElement(new FilterMethod(showText +": get day ...", NewsContentDefinition.class.getMethod("getDynamicList", new Class[] {Integer.class, Integer.class, String.class}), new Object[] {actChannelId, C_FLAG_DAY}));
				filterMethods.addElement(new FilterMethod(showText +": get month ..." ,  NewsContentDefinition.class.getMethod("getDynamicList", new Class[] {Integer.class, Integer.class, String.class}), new Object[] {actChannelId, C_FLAG_MONTH}));
			}catch(NoSuchMethodException e) {
				System.err.println("Exception in NewsContentDefinition.getFilterMethods(CmsObject)!"+ e.getMessage());
			}
		}
	try {
			filterMethods.addElement(new FilterMethod("Sort all by2", NewsContentDefinition.class.getMethod("getSortedList2", new Class[] {}), new Object[] {}));
		}catch(NoSuchMethodException e) {
			System.err.println("Exception in NewsContentDefinition.getFilterMethods(CmsObject)!"+ e.getMessage());
		}

		return filterMethods;
	}

	/**
	 * one filter method
	 * this method creates a list of all enties
	 *
	 * @return a Vector with all Elementes
	 */
	public static Vector getNewsList(Integer channel, String stringN) {
    System.err.println("channel: "+channel.intValue() + "Param: " + stringN);
		Vector list = new Vector();
		PreparedStatement statement = null;
		ResultSet res = null;
		int i =  0;
		int n = -1;
		getPool();           // get a pool first
		//System.err.println("[NewsContentDefinition.getNewsList] Start - channel: "+ ""+channel.intValue() + "stringN: " + stringN);
		try{
			n = Integer.parseInt(stringN);
		}catch(NumberFormatException e) {
			//System.err.println("Exception in NewsContentDefinition.getNewsList()!"+e.getMessage());
			n = -1;
		}
		try {
		 	statement = m_pool.getPreparedStatement(C_SELECT_FIRST_N_OF_CHANNEL_KEY);
			statement.setInt(1, channel.intValue());
			res = statement.executeQuery();
			while(res.next() && (i != n)){
				// read the item from the resultset
				// insert Element into list
				list.addElement(new NewsContentDefinition(res.getInt(1),
														  res.getString(2),  // Headline
														  res.getString(3),  // Descr.
														  res.getString(4),  // Text
														  res.getString(5),  // Author
														  res.getString(6),  // link
														  res.getString(7),  // linkText
														  res.getDate(8),    // Date
														  res.getString(9),  // Lockstate
														  res.getInt(10),    // Channel
														  res.getString(11), // a_info
														  res.getString(12), // a_info
														  res.getString(13)  // a_info
														));
				i++;
			}
		}catch(Exception e) {
			System.err.println("Exception in NewsContentDefinition.getNewsList()!");
		}finally{
			if(statement != null) {
				m_pool.putPreparedStatement(C_SELECT_FIRST_N_OF_CHANNEL_KEY, statement);
			}
			try{
				res.close();
			}catch (Exception e) {
				System.err.println("Exception in NewsContentDefinition.getNewsList(CmsObject)!"+ e.getMessage());
			}
		}
		return list;
	}
	/**
	 * one filter method
	 * this method creates a list of all enties
	 *
	 * @return a Vector with all Elementes
	 */
	public static Vector getSortedList(String str) {
		Vector list = new Vector();
		PreparedStatement statement = null;
		ResultSet res = null;
		getPool();           // get a pool first
		if(str == null || str == " ") { str = ""; }
		str.trim();
		str.toLowerCase();
		//System.err.println("[NewsContentDefinition.getSortedList()] Start - str: " +str);
		try {
		 	statement = m_pool.getPreparedStatement(C_ORDER_KEY);
			if( str.equals("") ) {
				statement.setString(1, "date");
			}
			else if( str.equals("id") ) {
				statement.setString(1, "id");
			}
			else if(str.equals("headline") || str.equals("schlagzeile") ) {
				statement.setString(1, "headline");
			}
			else if(str.equals("description") || str.equals("beschreibung")) {
				statement.setString(1, "description");
			}
			else if(str.equals("text") || str.equals("inhalt")) {
				statement.setString(1, "text");
			}
			else if(str.equals("author")) {
				statement.setString(1, "author");
			}
			else if(str.equals("link")) {
				statement.setString(1, "link");
			}
			else if(str.equals("date") || str.equals("Datum")) {
				statement.setString(1, "date");
			}
			else if(str.equals("channel") || str.equals("kanal")) {
				statement.setString(1, "channel");
			}
			else {
				statement.setString(1, "date");	// all other inputs!
			}
			res = statement.executeQuery();
			while(res.next()){
				// read the item from the resultset
				// insert Element into list
				list.addElement(new NewsContentDefinition(res.getInt(1),
														  res.getString(2), // Headline
														  res.getString(3), // Descr.
														  res.getString(4), // Text
														  res.getString(5), // Author
														  res.getString(6), // link
														  res.getString(7), // linkText
														  res.getDate(8),   // Date
														  res.getString(9), // Lockstate
														  res.getInt(10),   // Channel
														  res.getString(11),// a_info
														  res.getString(12),// a_info
														  res.getString(13) // a_info
														));
			}
		}catch(Exception e) {
			System.err.println("[NewsContentDefinition.getSortedList() Exception1:]" + e.getMessage());
		}finally{
			if(statement != null) {
				m_pool.putPreparedStatement(C_ORDER_KEY, statement);
			}
			try{
				res.close();
			}catch (Exception e) {
				System.err.println("[NewsContentDefinition.getSortedList() Exception2:]" + e.getMessage());
			}
		}
		return list;
	}

  /**
	 * one filter method
	 * this method creates a list of all enties
	 *
	 * @return a Vector with all Elementes
	 */
	public static Vector getSortedList2() {

    String str = "";
		Vector list = new Vector();
		PreparedStatement statement = null;
		ResultSet res = null;
		getPool();           // get a pool first
		if(str == null || str == " ") { str = ""; }
		str.trim();
		str.toLowerCase();
		//System.err.println("[NewsContentDefinition.getSortedList()] Start - str: " +str);
		try {
		 	statement = m_pool.getPreparedStatement(C_ORDER_KEY);
			if( str.equals("") ) {
				statement.setString(1, "date");
			}
			else if( str.equals("id") ) {
				statement.setString(1, "id");
			}
			else if(str.equals("headline") || str.equals("schlagzeile") ) {
				statement.setString(1, "headline");
			}
			else if(str.equals("description") || str.equals("beschreibung")) {
				statement.setString(1, "description");
			}
			else if(str.equals("text") || str.equals("inhalt")) {
				statement.setString(1, "text");
			}
			else if(str.equals("author")) {
				statement.setString(1, "author");
			}
			else if(str.equals("link")) {
				statement.setString(1, "link");
			}
			else if(str.equals("date") || str.equals("Datum")) {
				statement.setString(1, "date");
			}
			else if(str.equals("channel") || str.equals("kanal")) {
				statement.setString(1, "channel");
			}
			else {
				statement.setString(1, "date");	// all other inputs!
			}
			res = statement.executeQuery();
			while(res.next()){
				// read the item from the resultset
				// insert Element into list
				list.addElement(new NewsContentDefinition(res.getInt(1),
														  res.getString(2), // Headline
														  res.getString(3), // Descr.
														  res.getString(4), // Text
														  res.getString(5), // Author
														  res.getString(6), // link
														  res.getString(7), // linkText
														  res.getDate(8),   // Date
														  res.getString(9), // Lockstate
														  res.getInt(10),   // Channel
														  res.getString(11),// a_info
														  res.getString(12),// a_info
														  res.getString(13) // a_info
														));
			}
		}catch(Exception e) {
			System.err.println("[NewsContentDefinition.getSortedList() Exception1:]" + e.getMessage());
		}finally{
			if(statement != null) {
				m_pool.putPreparedStatement(C_ORDER_KEY, statement);
			}
			try{
				res.close();
			}catch (Exception e) {
				System.err.println("[NewsContentDefinition.getSortedList() Exception2:]" + e.getMessage());
			}
		}
		return list;
	}
	/**
	 * one filter method
	 * @return a Vector with all selected Elementes
	 */
	public static Vector getDynamicList(Integer channelId, Integer flag, String str) {
		Vector list = new Vector();
		java.sql.Date day = null;
		PreparedStatement statement = null;
		ResultSet res = null;
		getPool();           // get a pool first
		int month = 1;
		//System.err.println("[NewsContentDefinition.getDynamicList()] Start - channelId:" +""+channelId.intValue() + "flag: " + ""+flag.intValue() +"str: " + str);
		if(flag.equals(C_FLAG_MONTH)) {
			try{
				month = Integer.parseInt(str);
			}catch(NumberFormatException e) {
				System.err.println("[NewsContentDefinition.getDynamicList()] Exception while trying to parse the month: "+e.getMessage());
			}
			if(month <1 || month >12) month = 1;
		}else {
			// "day"
			if( (str == null) || (str.equals("")) || (str.equals(" "))) {
				try{
					day = string2sqlDate("01.01.2000"); // this default value seems to be needed by A_Backoffice
				}catch(ParseException e) {
					System.err.println("[NewsContentDefinition.getDynamicList()] Exception while trying to parse the date: "+e.getMessage());
				}
			}else {
				try{;
					day = string2sqlDate(str);  // now a real date is parsed
				}catch(ParseException e) {
					System.err.println("[NewsContentDefinition.getDynamicList()] Exception while trying to parse the date: "+e.getMessage());
				}
			}
		}
		try{
			if(flag.equals(C_FLAG_MONTH)){
				statement = m_pool.getPreparedStatement(C_SELECT_MONTH_KEY);
				statement.setInt(1, month);
				statement.setInt(2, channelId.intValue());
			}else{
				// "day"
				statement = m_pool.getPreparedStatement(C_SELECT_DAY_KEY);
				statement.setDate(1, day);
				statement.setInt(2, channelId.intValue());
			}
			res = statement.executeQuery();
			while(res.next()){
				// read the item from the resultset
				// insert Element into list
				list.addElement(new NewsContentDefinition(res.getInt(1),
														  res.getString(2), // Headline
														  res.getString(3), // Descr.
														  res.getString(4), // Text
														  res.getString(5), // Author
														  res.getString(6), // link
														  res.getString(7), // linkText
														  res.getDate(8),   // Date
														  res.getString(9), // Lockstate
														  res.getInt(10),   // Channel
														  res.getString(11),// a_info
														  res.getString(12),// a_info
														  res.getString(13) // a_info
														));
			}
		}catch(Exception e) {
			System.err.println("[NewsContentDefinition.getDynamicList() Exception while trying to execute the statement:]" + e.getMessage());
		}finally{
			if(statement != null) {
				if(flag.equals(C_FLAG_MONTH)) {
					m_pool.putPreparedStatement(C_SELECT_MONTH_KEY, statement);
				}else{
					// "day"
					m_pool.putPreparedStatement(C_SELECT_DAY_KEY, statement);
				}
			}
			try{
				res.close();
			}catch (Exception e) {
				System.err.println("[NewsContentDefinition.getDynamicList() Exception while trying to close the Resultset:]" + e.getMessage());
			}
		}
		return list;
	}

	/**
	 * This method indicates if a channel with a certain ID is used by a news entry.
	 * If this is so, the channel should not be deleted.
	 * @param the channel Id
	 * @return true if the channel is used
	 */
	public static boolean channelIsUsed(int channelId) {
		//System.err.println("[NewsContentDefinition.channelIsUsed()] channelId" + ""+channelId);
		PreparedStatement statement = null;
		ResultSet res = null;
		int ret = -1;
		getPool();           // get a pool first
		try {
		 	statement = m_pool.getPreparedStatement(C_SELECT_CHANNEL_ID_KEY);
			statement.setInt(1, channelId);
			res = statement.executeQuery();
			if(res.next()){
				ret = res.getInt(1);
				//System.err.println("channelIsUsed: " + res.getInt(1) );
			}
			System.err.println("channelIsUsed: " + res.getInt(1) );
		}catch(SQLException e) {
			System.err.println("Exception in NewsContentDefinition.channelIsUsed()!");
		}finally{
			if(statement != null) {
				m_pool.putPreparedStatement(C_SELECT_CHANNEL_ID_KEY, statement);
			}
			try{
				res.close();
			}catch (Exception e) {
				System.err.println("Exception in NewsContentDefinition.getNewsList(CmsObject)!"+ e.getMessage());
			}
			if(ret > 0) return true;
			else return false;
		}
	}

	/**
	 * sets the lockstate of the CD.
	 * @param lockstate The lockstate of this CD object.
	 * (possibles states: locked by you,locked by other, unlocked)
	 */
	public void setLockstate(String lockstate) {
		m_lockstate = lockstate;
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
	 * @return m_id The unique id (as a String)
	 */
	public String getUniqueId(CmsObject cms) {
		return ""+m_id;
	}

	/**
	 * get the Unique Id as int
	 * @return m_id (as int)
	 */
	public int getIntId() {
		return m_id;
	}

	/**
	 * get the headline
	 * @return the headline as String
	 */
	public String getHeadline() {
		return m_headline;
	}

	/**
	 * set the headline
	 * @param the headline as String
	 */
	public void setHeadline(String headline) {
		m_headline = headline;
	}

	/**
	 * get the description
	 * @return the description as String
	 */
	public String getDescription() {
		return m_description;
	}

	/**
	 * set the description
	 * @param the description as String
	 */
	public void setDescription(String description) {
		m_description = description;
	}

	/**
	 * get the news text
	 * @return the text as a String
	 */
	public String getText() {
		return m_text;
	}

	/**
	 * set the text
	 * @param the text as a String
	 */
	public void setText(String text) {
		m_text = text;
	}

	/**
	 * get the author
	 * @return the author as a String
	 */
	public String getAuthor() {
		return m_author;
	}

	/**
	 * set the author
	 * @param the author as a String
	 */
	public void setAuthor(String author) {
		m_author = author;
	}

	/**
	 * get the link
	 * @return the link as a String
	 */
	public String getLink() {
		return m_link;
	}

	/**
	 * set the link
	 * @param the link as a String
	 */
	public void setLink(String link) {
		m_link = link;
	}

	/**
	 * get the Text for the link
	 * @return the Text as a String
	 */
	public String getLinkText() {
		return m_linkText;
	}

	/**
	 * set the Text for the link
	 * @param the Text as a String
	 */
	public void setLinkText(String linkText) {
		m_linkText = linkText;
	}

	/**
	 * get the data of the news entry
	 * @retrun the date as a String
	 */
	public String getDate() {
		return date2string(m_date);
	}

	/**
	 * set the data of the news entry
	 * @param the date as a GregorianCalendar date
	 */
	public void setDate(GregorianCalendar date) {
		m_date = date;
	}

	/**
	 * get AdditionalInformation
	 * @return the info as a String
	 */
	public String getA_info1() {
		return m_a_info1;
	}

	/**
	 * set AdditionalInformation
	 * @param the info as a String
	 */
	public void setA_info1(String a_info1) {
		m_a_info1 = a_info1;
	}

	/**
	 * get AdditionalInformation
	 * @return the info as a String
	 */
	public String getA_info2() {
		return m_a_info2;
	}

	/**
	 * set AdditionalInformation
	 * @param the info as a String
	 */
	public void setA_info2(String a_info2) {
		m_a_info2 = a_info2;
	}

	/**
	 * get AdditionalInformation
	 * @return the info as a String
	 */
	public String getA_info3() {
		return m_a_info3;
	}

	/**
	 * set AdditionalInformation
	 * @param the info as a String
	 */
	public void setA_info3(String a_info3) {
		m_a_info3 = a_info3;
	}


	/**
	 * method to access the real name of the channel
	 * @return String     the name of the channel
	 */
	public String getChannel() {
		NewsChannelContentDefinition temp = null;
		// create a Object to access the name of the channel
		try{
			temp = new NewsChannelContentDefinition(null, new Integer(m_channel));
		}catch(CmsException e) {
			System.err.println("[" + this.getClass().getName() + "] " + e.getMessage());
		}
		return temp.getName();
	}

	/**
	 * set the channel
	 * @param the channel as int
	 */
	public void setChannel(int channel) {
		m_channel = channel;
	}

	/**
	 * method to convert a GregrianCalendar date to a String
	 * @parame the GregrianCalendar date
	 * @return the date as a String
	 */
	public static String date2string(GregorianCalendar cal) {
		DateFormat df;
		df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		return df.format(cal.getTime());
	}

	/**
	 * method to convert a string date to a GregorianCalendar date.
	 * The method throws a ParseException if the string cannot be converted.
	 * @param the date as a String
	 * @return the date as a GregorianCalendar
	 */
	public static GregorianCalendar string2date(String sDate) throws ParseException {
		DateFormat df;
		GregorianCalendar ret = new GregorianCalendar();
		df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		java.util.Date date = df.parse(sDate);
		//System.err.println("string2date " + date.toString());
		ret.setTime(date);
		//System.err.println("string2date " + ret.get(Calendar.DATE)+"." + ret.get(Calendar.MONTH)+"." + ret.get(Calendar.YEAR) );
		return ret;
	}

	/**
	 * method to convert a string date to a sqlDate date.
	 * The method throws a ParseException if the string cannot be converted.
	 * @param the date as a String
	 * @return the date as a sqlDate
	 */
	public static java.sql.Date string2sqlDate(String sDate) throws ParseException {
		DateFormat df;
		df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		java.util.Date uDate = df.parse(sDate);
		//System.err.println("string2sqlDate " + uDate.toString());
		java.sql.Date sqlDate = new java.sql.Date(uDate.getTime());  // Date to SQLDate
		return sqlDate;
	}

}
