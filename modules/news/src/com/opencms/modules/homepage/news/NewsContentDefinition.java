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
import java.net.*;               // URL
//import javax.servlet.*;
import javax.servlet.http.*;     // HttpServletRequest

/**
 * This is the Content Definition for the news entries.
 * The class privides some constructors filter methods to access entries as well
 * as some methods to convert dates and strings.
 */
public class NewsContentDefinition extends A_CmsContentDefinition implements I_CmsContent {

	// constants for day or month selection
	private static final Integer C_FLAG_DAY = new Integer(0);
	private static final Integer C_FLAG_MONTH = new Integer(1);

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
	private int m_lockstate = -1;

    private static Locale m_locale = new Locale("Default", ""+Locale.getDefault());
    //private static Locale m_locale = new Locale("en", ""+Locale.UK);

	private boolean newElement = false;  // indicates if an element should be written or updated

	private static String c_pool = null;
	// private static NewsKeyAccess m_NewsKeyAccess = null;

	// Constants for table news_entry
	private static final String  C_SELECT_ALL = "select * from news_entry";
	private static final String  C_INSERT_ENTRY = "insert into news_entry values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String  C_DELETE_ENTRY = "delete from news_entry where ID = ?";
	private static final String  C_COUNT = "select count(*) from news_entry";
    private static final String  C_SELECT_ID = "select * from news_entry where ID = ?";
	private static final String  C_GET_ALL = "select * from news_entry";
	private static final String  C_UPDATE_ENTRY = "update news_entry set headline = ?, description = ?, text = ?, author = ?, link = ?, linkText = ?, date = ?, lockstate = ?, channel = ?, a_info1 = ?, a_info2 = ?, a_info3 = ? where id = ?";
	private static final String  C_ORDER = "select * from news_entry order by ? desc";
	private static final String  C_SELECT_CHANNEL_ID = "select count(*) from news_entry where channel = ?";
	private static final String  C_SELECT_MONTH = "select * from news_entry where month(date)= ? and channel = ? order by date desc";
	private static final String  C_SELECT_DAY = "select * from news_entry where (date between ? and ?) and (channel = ?)";
	private static final String  C_SELECT_FIRST_N_OF_CHANNEL = "select * from news_entry where channel = ? order by date desc";

    public static final String	 C_TABLE_NEWS = "NEWS_ENTRY";
    public static final int      C_NOT_LOCKED = -1;

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
    Connection con = null;
		PreparedStatement statement = null;
		ResultSet res = null;
		try {
      con = DriverManager.getConnection(c_pool);
		 	statement = con.prepareStatement(C_SELECT_ID);
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
                java.sql.Timestamp timestamp = res.getTimestamp(8);
				java.util.Date tempDate = (java.util.Date)timestamp;
				m_date.setTime( tempDate ); // SQLDate to Date to GregorianCalendar
				//System.err.println("NewsContentDefinition" + m_date.get(Calendar.DATE)+"." + m_date.get(Calendar.MONTH)+"." + m_date.get(Calendar.YEAR) );
				m_lockstate = res.getInt(9);
				m_channel = res.getInt(10);
				m_a_info1 = res.getString(11);
				m_a_info2 = res.getString(12);
				m_a_info3 = res.getString(13);
			}
		}catch(SQLException e) {
			throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(),
				CmsException.C_SQL_ERROR, e);
		}finally{
			try{
				res.close();
			}catch (Exception e) {
				// ignore the exception
			}
            try{
				statement.close();
			}catch (Exception e) {
				// ignore the exception
			}
            try{
				con.close();
			}catch (Exception e) {
				// ignore the exception
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
		m_lockstate = C_NOT_LOCKED;  // default
		newElement = true;        // this is a new Element
		try{
			m_id = com.opencms.dbpool.CmsIdGenerator.nextId(c_pool, C_TABLE_NEWS);
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
								  java.sql.Timestamp date,
								  int lockstate,
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
        java.util.Date tmpDate = new java.util.Date(date.getTime());
		//m_date.setTime( (java.util.Date)date ); // SQLDate to Date to GregorianCalendar
        m_date.setTime(tmpDate);
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
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet res = null;
		java.util.Date uDate = m_date.getTime();  	// GregorianCalendar to Date
        clearcache(cms); // clear the cache to make changes visible
		//java.sql.Date sqlDate = new java.sql.Date(uDate.getTime());  // Date to SQLDate
        java.sql.Timestamp sqlDate = new java.sql.Timestamp(uDate.getTime());
        //sqlDate.setTime(uDate.getTime());
		if(newElement == true) {
			// Element not jet in Database -> insert Statement
			try {
				// write the data to database
                con = DriverManager.getConnection(c_pool);
		 		statement = con.prepareStatement(C_INSERT_ENTRY);
				statement.setInt(1, m_id);
				statement.setString(2, m_headline);
				statement.setString(3, m_description);
				statement.setString(4, m_text);
				statement.setString(5, m_author);
				statement.setString(6, m_link);
				statement.setString(7, m_linkText);
				//statement.setDate(8, sqlDate);
				statement.setTimestamp(8, sqlDate);
                statement.setInt(9, m_lockstate);
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
                try{
				    statement.close();
			    }catch (Exception e) {
                    // ignore the exception
			    }
                try{
                    con.close();
			    }catch (Exception e) {
				    // ignore the exception
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
	private void update(CmsObject cms, java.sql.Timestamp sqlDate) throws CmsException {
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet res = null;
		try {
			// write the data to database
            con = DriverManager.getConnection(c_pool);
		 	statement = con.prepareStatement(C_UPDATE_ENTRY);
			statement.setString(1, m_headline);
			statement.setString(2, m_description);
			statement.setString(3, m_text);
			statement.setString(4, m_author);
			statement.setString(5, m_link);
			statement.setString(6, m_linkText);
			//statement.setDate(7, sqlDate);
			statement.setTimestamp(7, sqlDate);
            statement.setInt(8, m_lockstate);
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
            try{
				statement.close();
			}catch (Exception e) {
				// ignore the exception
			}
            try{
				con.close();
			}catch (Exception e) {
				// ignore the exception
			}
		}
	}

	/**
	 * delete an Element from the database
	 * @param cms An instance of CmsObject
	 */
	public void delete(CmsObject cms) throws CmsException {
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet res = null;
        clearcache(cms); // clear the cache to make changes visible
		try {
			// delet data from database
            con = DriverManager.getConnection(c_pool);
		 	statement = con.prepareStatement(C_DELETE_ENTRY);
			statement.setInt(1, m_id);
			statement.execute();
		}catch(Exception e) {
			throw new CmsException("[" + this.getClass().getName() + "] " + e.getMessage(),
			CmsException.C_SQL_ERROR, e);
		}finally{
			 try{
				statement.close();
			}catch (Exception e) {
				// ignore the exception
			}
            try{
				con.close();
			}catch (Exception e) {
				// ignore the exception
			}
		}
	}

  /**
	 * create a pool if neccessary
	 *
	 * @return c_pool
	 */
	private static String getPoolName(CmsObject cms) {
		if(c_pool == null) {
      try{
        c_pool = cms.getRegistry().getModuleParameterString("com.opencms.modules.homepage.news", "poolname");
      } catch(CmsException exc) {
        // TODO: Add error-handling here!
      }
		}
		return c_pool;
	}

	/**
	 * declare the entries that should be displayed in the Backoffice list
	 * @return a Vector
	 */
	public static Vector getFieldMethods(CmsObject cms) {
		Vector methods = new Vector();
		try {
			methods.addElement(NewsContentDefinition.class.getMethod("getHeadline", new Class[0]));
			//methods.addElement(NewsContentDefinition.class.getMethod("getDescription", new Class[0]));
			methods.addElement(NewsContentDefinition.class.getMethod("getAuthor", new Class[0]));
			//methods.addElement(NewsContentDefinition.class.getMethod("getLink", new Class[0]));
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
		//names.addElement("Description");
		names.addElement("Author");
		//names.addElement("Link");
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
        String showText = "";
		Integer actChannelId = null;  // has to be Integer because of Reflection!
        Vector channels = null;
        GregorianCalendar actDate = new GregorianCalendar();
        int actMonth = actDate.get(Calendar.MONTH) + 1;
    try {
	  	channels = NewsChannelContentDefinition.getChannelList(); // get all actual channels
    }
    catch(CmsException e) {
      return null;
    }
	  // add the always shown first filterMethod...
		try {
			filterMethods.addElement(new CmsFilterMethod("Sort all by ...", NewsContentDefinition.class.getMethod("getSortedList", new Class[] {String.class}), new Object[] {}));
		}catch(NoSuchMethodException e) {
			System.err.println("Exception in NewsContentDefinition.getFilterMethods(CmsObject)!"+ e.getMessage());
		}
		// try to add some filters dynamically
		for(int i=0; i< channels.size(); i++) {
			showText = ((NewsChannelContentDefinition)channels.elementAt(i)).getName();
			actChannelId = new Integer(((NewsChannelContentDefinition)channels.elementAt(i)).getIntId());
			try {
				filterMethods.addElement(new CmsFilterMethod(showText +": list first ...", NewsContentDefinition.class.getMethod("getNewsList", new Class[] {Integer.class, String.class}), new Object[] {actChannelId}, "15"));
				//filterMethods.addElement(new CmsFilterMethod(showText +": get day ...", NewsContentDefinition.class.getMethod("getDynamicList", new Class[] {Integer.class, Integer.class, String.class}), new Object[] {actChannelId, C_FLAG_DAY}, date2shortString(actDate) ));
				filterMethods.addElement(new CmsFilterMethod(showText +": get month ..." ,  NewsContentDefinition.class.getMethod("getDynamicList", new Class[] {Integer.class, Integer.class, String.class}), new Object[] {actChannelId, C_FLAG_MONTH}, ""+actMonth));
			}catch(NoSuchMethodException e) {
				System.err.println("Exception in NewsContentDefinition.getFilterMethods(CmsObject)!"+ e.getMessage());
			}
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
		Vector list = new Vector();
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet res = null;
		int i =  0;
		int n = -1;
		//System.err.println("[NewsContentDefinition.getNewsList] Start - channel: "+ ""+channel.intValue() + "stringN: " + stringN);
		try{
			n = Integer.parseInt(stringN);
		}catch(NumberFormatException e) {
			//System.err.println("Exception in NewsContentDefinition.getNewsList()!"+e.getMessage());
			n = -1;
		}
		try {
            con = DriverManager.getConnection(c_pool);
		 	statement = con.prepareStatement(C_SELECT_FIRST_N_OF_CHANNEL);
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
														  res.getTimestamp(8),    // Date
														  res.getInt(9),  // Lockstate
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
			try{
				res.close();
			}catch (Exception e) {
				// ignore the exception
			}
            try{
				statement.close();
			}catch (Exception e) {
				// ignore the exception
			}
            try{
				con.close();
			}catch (Exception e) {
				// ignore the exception
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
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet res = null;
		if(str == null || str == " ") { str = ""; }
		str.trim();
		str.toLowerCase();
		//System.err.println("[NewsContentDefinition.getSortedList()] Start - str: " +str);
		try {
            con = DriverManager.getConnection(c_pool);
		 	statement = con.prepareStatement(C_ORDER);
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
														  res.getTimestamp(8),   // Date
														  res.getInt(9),    // Lockstate
														  res.getInt(10),   // Channel
														  res.getString(11),// a_info
														  res.getString(12),// a_info
														  res.getString(13) // a_info
														));
			}
		}catch(Exception e) {
			System.err.println("[NewsContentDefinition.getSortedList() Exception1:]" + e.getMessage());
		}finally{
			try{
				res.close();
			}catch (Exception e) {
				// ignore the exception
			}
            try{
				statement.close();
			}catch (Exception e) {
				// ignore the exception
			}
            try{
				con.close();
			}catch (Exception e) {
				// ignore the exception
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
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet res = null;
		if(str == null || str == " ") { str = ""; }
		str.trim();
		str.toLowerCase();
		//System.err.println("[NewsContentDefinition.getSortedList()] Start - str: " +str);
		try {
            con = DriverManager.getConnection(c_pool);
		 	statement = con.prepareStatement(C_ORDER);
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
														  res.getTimestamp(8),   // Date
														  res.getInt(9), // Lockstate
														  res.getInt(10),   // Channel
														  res.getString(11),// a_info
														  res.getString(12),// a_info
														  res.getString(13) // a_info
														));
			}
		}catch(Exception e) {
			System.err.println("[NewsContentDefinition.getSortedList() Exception1:]" + e.getMessage());
		}finally{
			try{
				res.close();
			}catch (Exception e) {
				// ignore the exception
			}
            try{
				statement.close();
			}catch (Exception e) {
				// ignore the exception
			}
            try{
				con.close();
			}catch (Exception e) {
				// ignore the exception
			}
		}
		return list;
	}
	/**
	 * one filter method
	 * @return a Vector with all selected Elementes
	 */
	public static Vector getDynamicList(Integer channelId, Integer flag, String str) throws CmsException {
		Vector list = new Vector();
		java.sql.Timestamp day = null;
        GregorianCalendar dayEnd = new GregorianCalendar();
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet res = null;
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
					//day = shortString2sqlTimestamp("01.01.2000"); // this default value seems to be needed by A_Backoffice
				    day = date2Timestamp(dayEnd);
                    dayEnd.set(Calendar.HOUR, 23);
                    dayEnd.set(Calendar.MINUTE, 59);
                    dayEnd.set(Calendar.SECOND, 59);
                }catch(Exception e) {
					System.err.println("[NewsContentDefinition.getDynamicList()] Exception while trying to parse the date: "+e.getMessage());
				}
			}else {
				try{;
					day = shortString2sqlTimestamp(str);  // now a real date is parsed
                    dayEnd = shortString2date(str);
                    dayEnd.set(Calendar.HOUR, 23);
                    dayEnd.set(Calendar.MINUTE, 59);
                    dayEnd.set(Calendar.SECOND, 59);
				}catch(ParseException e) {
					System.err.println("[NewsContentDefinition.getDynamicList()] Exception while trying to parse the date: "+e.getMessage());
				}
			}
		}
		try{
            con = DriverManager.getConnection(c_pool);
			if(flag.equals(C_FLAG_MONTH)){
				statement = con.prepareStatement(C_SELECT_MONTH);
				statement.setInt(1, month);
				statement.setInt(2, channelId.intValue());
			}else{
				// "day"
				statement = con.prepareStatement(C_SELECT_DAY);
				statement.setTimestamp(1, day);
                statement.setTimestamp(2, date2Timestamp(dayEnd) );
				statement.setInt(3, channelId.intValue());
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
														  res.getTimestamp(8),   // Date
														  res.getInt(9),    // Lockstate
														  res.getInt(10),   // Channel
														  res.getString(11),// a_info
														  res.getString(12),// a_info
														  res.getString(13) // a_info
														));
			}
		}catch(Exception e) {
      throw new CmsException(e.getMessage());
			//System.err.println("[NewsContentDefinition.getDynamicList() Exception while trying to execute the statement:]" + e.getMessage());
		}finally{
            try{
				res.close();
			}catch (Exception e) {
				// ignore the exception
			}
			try{
				statement.close();
			}catch (Exception e) {
				// ignore the exception
			}
            try{
				con.close();
			}catch (Exception e) {
				// ignore the exception
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
    Connection con = null;
		PreparedStatement statement = null;
		ResultSet res = null;
		int ret = -1;
		try {
            con = DriverManager.getConnection(c_pool);
		 	statement = con.prepareStatement(C_SELECT_CHANNEL_ID);
			statement.setInt(1, channelId);
			res = statement.executeQuery();
			if(res.next()){
				ret = res.getInt(1);
				//System.err.println("channelIsUsed: " + res.getInt(1) );
			}
			//System.err.println("channelIsUsed: " + res.getInt(1) );
		}catch(SQLException e) {
			System.err.println("Exception in NewsContentDefinition.channelIsUsed()!");
		}finally{
			try{
				res.close();
			}catch (Exception e) {
				// ignore the exception
			}
			try{
				statement.close();
			}catch (Exception e) {
				// ignore the exception
			}
            try{
				con.close();
			}catch (Exception e) {
				// ignore the exception
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
	public void setLockstate(int lockstate) {
		m_lockstate = lockstate;
	}

	/**
	 * gets the lockstate of the CD
	* (possibles states: locked by user,locked by other, unlocked)
	*/
	public int getLockstate() {
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
		df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                            DateFormat.MEDIUM, m_locale);
		return df.format(cal.getTime());
	}
    /**
	 * method to convert a GregrianCalendar date to a String
	 * @parame the GregrianCalendar date
	 * @return the date as a String
	 */
	public static String date2shortString(GregorianCalendar cal) {
		DateFormat df;
		df = DateFormat.getDateInstance(DateFormat.MEDIUM, m_locale);
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
		df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                            DateFormat.MEDIUM, m_locale);
		java.util.Date date = df.parse(sDate);
		//System.err.println("string2date " + date.toString());
		ret.setTime(date);
		//System.err.println("string2date " + ret.get(Calendar.DATE)+"." + ret.get(Calendar.MONTH)+"." + ret.get(Calendar.YEAR) );
		return ret;
	}

    /**
	 * method to convert a string date to a GregorianCalendar date.
	 * The method throws a ParseException if the string cannot be converted.
	 * @param the date as a String
	 * @return the date as a GregorianCalendar
	 */
	public static GregorianCalendar shortString2date(String sDate) throws ParseException {
		DateFormat df;
		GregorianCalendar ret = new GregorianCalendar();
		df = DateFormat.getDateInstance(DateFormat.MEDIUM, m_locale);
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
	public static java.sql.Timestamp string2sqlTimestamp(String sDate) throws ParseException {
		DateFormat df;
		//df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, m_locale);
		java.util.Date uDate = df.parse(sDate);
		//System.err.println("string2sqlDate " + uDate.toString());
		java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(uDate.getTime());  // Date to SQLDate
		return sqlTimestamp;
	}

    /**
	 * method to convert a string date to a sqlDate date.
	 * The method throws a ParseException if the string cannot be converted.
	 * @param the date as a String
	 * @return the date as a sqlDate
	 */
	public static java.sql.Timestamp shortString2sqlTimestamp(String sDate) throws ParseException {
		DateFormat df;
		//df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        df = DateFormat.getDateInstance(DateFormat.MEDIUM, m_locale);
		java.util.Date uDate = df.parse(sDate);
		//System.err.println("string2sqlDate " + uDate.toString());
		java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(uDate.getTime());  // Date to SQLDate
		return sqlTimestamp;
	}

    public static java.sql.Timestamp date2Timestamp(GregorianCalendar gDate) {
        java.util.Date date= gDate.getTime();
        return new java.sql.Timestamp( date.getTime() );
    }

  /**
   * method to update the poolname
   * is invoked from NewsChannelContentDefinition
   * @param String pool
   */
  public static void updatePoolName(String pool) {
    c_pool = pool;
  }

  /**
   * method to clear the cache
   * is invoked from write and delete
   * @param CmsObject cms
   */
  private void clearcache(CmsObject cms) {
    URL u = null;
    String p = ((HttpServletRequest)
                cms.getRequestContext().getRequest().getOriginalRequest()
                ).getServletPath();
    String s = ((HttpServletRequest)
                cms.getRequestContext().getRequest().getOriginalRequest()
                ).getServerName();
    try {
        u = new URL("http://" +s +p
                    +"/system/workplace/action/login.html?_clearcache=all");
        URLConnection uc = u.openConnection();
        uc.connect();
        InputStream in = uc.getInputStream();
        //System.err.println("clearcache o.k.!");
    } catch(MalformedURLException e) {
        System.err.println("Exception1: " +e.getMessage() );
    } catch (IOException e) {
        System.err.println("Exception2: " +e.getMessage() );
    }

  }

}
