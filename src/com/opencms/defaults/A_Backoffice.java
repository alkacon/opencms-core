package com.opencms.defaults;

/*
 *
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

import com.opencms.template.*;
import com.opencms.workplace.*;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;

import java.util.*;
import java.io.*;

import java.lang.reflect.*;
import java.lang.String;


/**
 * Abstract class for generic backoffice display. It automatically
 * generates the 	head section with filters and buttons,
 * 					body section with the table data,
 * 					lock states of the entries. if there are any,
 *					delete dialog,
 *					lock dialog.
 * calls the		edit dialog of the calling backoffice class.
 *					new dialog of the calling backoffice class.
 * fills the template with the current table data getting
 * the content definition class from the used backoffice class.
 * The methods and data provided by the content definition class
 * is accessed by reflection. This way it is possible to reuse
 * this class for any content definition class, that just have
 * to extend the A_ContentDefintion class!
 * Creation date: (27.10.00 10:04:42)
 * author: Michael Knoll
 * version 1.0
 */
public abstract class A_Backoffice extends CmsWorkplaceDefaultNoCache {
	
	public static final String C_BACKOFFICE_LABEL = "backoffice.label.";
	
	public static final String C_BACKOFFICE_MESSAGEBOX_DIALOG = "backoffice.messagebox.dialog.";

/**
 * Gets the content of a given template file.
 * This method displays any content provided by a content definition
 * class on the template. The used backoffice class does not need to use a
 * special getContent method. It just has to extend the methods of this class!
 * Using reflection, this method creates the table headline and table content
 * with the layout provided by the template automatically!
 * @param cms A_CmsObject Object for accessing system resources
 * @param templateFile Filename of the template file 
 * @param elementName <em>not used here</em>.
 * @param parameters <em>not used here</em>.
 * @param templateSelector template section that should be processed.
 * @return Processed content of the given template file.
 * @exception CmsException 
 */

public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {

	//return var
	byte[] returnProcess = null;

	// session will be created or fetched
	I_CmsSession session = (CmsSession) cms.getRequestContext().getSession(true);
	//create new workplace templatefile object
	CmsXmlWpTemplateFile template = new CmsXmlWpTemplateFile(cms, templateFile);

	//get parameters
	String selectbox = (String) parameters.get("selectbox");
	String id = (String) parameters.get("id");
	String idlock = (String) parameters.get("idlock");
	String iddelete = (String) parameters.get("iddelete");
	String idedit = (String) parameters.get("idedit");
	String action = (String) parameters.get("action");
	String ok = (String) parameters.get("ok");

	if (action != null)
		parameters.put("action", action);

	//move id values to id, remove old markers
	if (idlock != null) {
		id = idlock;
		session.putValue("idlock", idlock);
		session.removeValue("idedit");
		session.removeValue("idnew");
		session.removeValue("iddelete");			
	}
	if (idedit != null) {
		id = idedit;
		session.putValue("idedit", idedit);
		session.removeValue("idlock");
		session.removeValue("idnew");
		session.removeValue("iddelete");
	}
	if (iddelete != null) {
		id = iddelete;
		session.putValue("iddelete", iddelete);
		session.removeValue("idedit");
		session.removeValue("idnew");
		session.removeValue("idlock");
	}
	if ((id != null) && (id.equals("new"))) {
		session.putValue("idnew", id);
		session.removeValue("idedit");
		session.removeValue("idnew");
		session.removeValue("iddelete");
		session.removeValue("idlock");		
	}

	//get marker id from session
	String idsave = (String) session.getValue("idsave");
	if (ok == null)
		idsave = null;

	//get marker for accessing the new dialog
	String idnewsave = (String) session.getValue("idnew");
	//access to new dialog
	if ((id != null) && (id.equals("new")) || ((idsave != null) && (idsave.equals("new")))) {
		if (idsave != null)
			parameters.put("idsave", idsave);
		if (id != null)
			parameters.put("id", id);
		//process the "new entry" form
		returnProcess = getContentNew(cms, template, elementName, parameters, templateSelector);
		//finally retrun processed data
		return returnProcess;
	}

	//go to the appropriate getContent methods 
	if ((selectbox == null) && (id == null) && (idsave == null) && (action == null)) {
		//process the head frame´containing the filter 
		returnProcess = getContentHead(cms, template, elementName, parameters, templateSelector);
		//finally return processed data
		return returnProcess;
		
	} else {
		//process the body frame containing the table
		if (selectbox != null) {
			//process the list output
			returnProcess = getContentList(cms, template, elementName, parameters, templateSelector);
			//finally return processed data
			return returnProcess;
			
		} else {
			//get marker for accessing the edit dialog
			String ideditsave = (String) session.getValue("idedit");
			
			//go to the edit dialog
			if ((idedit != null) || (ideditsave != null)) {
				//store id parameters for edit dialog
				if (idsave != null)
					parameters.put("idsave", idsave);
				if (id != null)
					parameters.put("id", id);
				returnProcess = getContentEdit(cms, template, elementName, parameters, templateSelector);
				//finally return processed data
				return returnProcess;
				
			} else {
				//store id parameters for delete and lock
				if (idsave != null) {
					parameters.put("id", idsave);
				} else {
					parameters.put("id", id);
				}
				//get marker for accessing the delete dialog
				String iddeletesave = (String) session.getValue("iddelete");
				//access delete dialog
				if ((iddelete != null) || (iddeletesave != null)) {
					returnProcess = getContentDelete(cms, template, elementName, parameters, templateSelector);
					return returnProcess;
					
				} else {
					//access lock dialog	
					returnProcess = getContentLock(cms, template, elementName, parameters, templateSelector);
					//finally return processed data
					return returnProcess;
				}
			}
		}
	}
}
/**
 * gets the content definition class
 * @returns class content definition class
 * Must be implemented in the extending backoffice class!
 */
 
public abstract Class getContentDefinitionClass() ;
/**
 * gets the content definition class method constructor
 * @returns content definition object
 */

private Object getContentDefinitionConstructor(CmsObject cms, Class cdClass, Integer id) {

	Object o = null;
	try {
		Constructor c = cdClass.getConstructor(new Class[] {CmsObject.class, Integer.class});
		o = c.newInstance(new Object[] {cms, id});
	} catch (InvocationTargetException ite) {
		System.err.println("Backoffice getContentDefinitionConstructor: Invocation target exception!");
		ite.printStackTrace();
	} catch (NoSuchMethodException nsm) {
		System.err.println("Backoffice getContentDefinitionConstructor: Requested method was not found!");
	} catch (InstantiationException ie) {
		System.err.println("Backoffice getContentDefinitionConstructor: the class is abstract!!");
	} catch (Exception e) {
		System.err.println("Backoffice getContentDefinitionConstructor: Output section throwed an exception!");
		e.printStackTrace();
	}
	return o;
}
/**
 * Gets the content of a given template file.
 * <P>
 * While processing the template file the table entry
 * <code>entryTitle<code> will be displayed in the delete dialog
 * 
 * @param cms A_CmsObject Object for accessing system resources
 * @param templateFile Filename of the template file 
 * @param elementName not used here
 * @param parameters get the parameters action for the button activity 
 * 					 and id for the used content definition instance object
 * @param templateSelector template section that should be processed.
 * @return Processed content of the given template file.
 * @exception CmsException 
 */
 
private byte[] getContentDelete(CmsObject cms, CmsXmlWpTemplateFile template, String elementName, Hashtable parameters, String templateSelector) throws CmsException {

	//return var
	byte[] processResult = null;

	// session will be created or fetched
	I_CmsSession session = (CmsSession) cms.getRequestContext().getSession(true);
	//get the class of the content definition	
	Class cdClass = getContentDefinitionClass();

	//get (stored) id parameter
	String id = (String) parameters.get("id");
	if (id == null)
		id = "";
	if (id != "") {
		session.putValue("idsave", id);
	} else {
		String idsave = (String) session.getValue("idsave");
		if (idsave == null)
			idsave = "";
		id = idsave;
		session.removeValue("idsave");
	}

	// get value of hidden input field action
	String action = (String) parameters.get("action");

	//no button pressed, go to the default section!
	//delete dialog, displays the title of the entry to be deleted
	if (action == null || action.equals("")) {
		if (id != "") {
			//set template section
			templateSelector = "default";
			/*-------
			//get integer value from id
			Integer idInteger = Integer.valueOf(id);
			//access content definition constructor by reflection
			//displays the the unique title of the cd object (to be implemented) 
			String title = "no title";
			Object o = null;
			o = getContentDefinitionConstructor(cms, cdClass, idInteger);
			try {
				Method getTitleMethod = (Method) cdClass.getMethod("getTitle", new Class[] {});
				title = (String) getTitleMethod.invoke(o, new Object[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			--------*/
			
			//set data in the template
			template.setData("newsentry", id);
			template.setData("setaction", "default");
		}
	// confirmation button pressed, process data!
	} else {
		
		//set template section
		templateSelector = "done";
		//remove marker
		session.removeValue("idsave");

		//delete the content definition instance		
		Integer idInteger = Integer.valueOf(id);

		//access content definition constructor by reflection
		Object o = null;
		o = getContentDefinitionConstructor(cms, cdClass, idInteger);		
		//get delete method and delete content definition instance
		try {
			Method deleteMethod = (Method) cdClass.getMethod("delete", new Class[] {CmsObject.class});
			deleteMethod.invoke(o, new Object[] {cms});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//finally start the processing
	processResult = startProcessing(cms, template, elementName, parameters, templateSelector);
	return processResult;
}
/**
 * gets the content of a edited entry form.
 * Has to be overwritten in your backoffice class!
 */
public abstract byte[] getContentEdit(CmsObject cms,CmsXmlWpTemplateFile templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException;
/**
 * Gets the content of a given template file.
 * <P>
 * 
 * @param cms A_CmsObject Object for accessing system resources
 * @param templateFile Filename of the template file 
 * @param elementName not used here
 * @param parameters get the parameters action for the button activity
 * 					 and id for the used content definition instance object
 *					 and the author, title, text content for setting the new/changed data
 * @param templateSelector template section that should be processed.
 * @return Processed content of the given template file.
 * @exception CmsException 
 */

private byte[] getContentHead(CmsObject cms, CmsXmlWpTemplateFile template, String elementName, Hashtable parameters, String templateSelector) throws CmsException {

	//return var
	byte[] processResult = null;

	//get the class of the content definition	
	Class cdClass = getContentDefinitionClass();

	//init vars
	String singleSelection = "";
	String allSelections = "";

	//create new or fetch existing session
	CmsSession session = (CmsSession) cms.getRequestContext().getSession(true);
	//get filter method from session
	String selectBoxValue = (String) session.getValue("selectbox");

	//get vector of filter names from the content definition
	Vector filterMethods = new Vector();
	try {
		filterMethods = (Vector) cdClass.getMethod("getFilterMethods", new Class[] {CmsObject.class}).invoke(null, new Object[] {cms});
	} catch (Exception e) {
		e.printStackTrace();
	}

	//no filter selected so far, store a default filter in the session
	FilterMethod filterMethod = null;
	if (selectBoxValue == null) {
		FilterMethod defaultFilter = (FilterMethod) filterMethods.firstElement();
		session.putValue("selectbox", defaultFilter.getFilterName());
	}

	//set the select box
	for (int i = 0; i < filterMethods.size(); i++) {
		FilterMethod currentFilter = (FilterMethod) filterMethods.elementAt(i);
		
		//insert filter in the template selectbox 
		template.setData("selectname", currentFilter.getFilterName());
		template.setData("selectnumber", "" + i);
		//add additional inputfield, if filter allows parameters
		template.setData("selectparameter", "" + currentFilter.hasUserParameter());
		
		//get processed data from the template
		try {
			singleSelection = template.getProcessedDataValue("singleselection", this);
			allSelections += singleSelection;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//if getCreateUrl equals null, the "create new entry" button 
	//will not be displayed in the template
	String createButton = (String) getCreateUrl(cms);
	if (createButton == null) {
		String cb = template.getDataValue("nowand");
		template.setData("createbutton", cb);
	} else {
		String cb = template.getDataValue("wand");
		template.setData("createbutton", cb);
	}

	//insert tablecontent in template		
	template.setData("selectionbox", "" + allSelections);

	//finally start the processing
	processResult = startProcessing(cms, template, elementName, parameters, templateSelector);
	return processResult;
}
/**
 * Gets the content of a given template file.
 * This method displays any content provided by a content definition
 * class on the template. The used backoffice class does not need to use a
 * special getContent method. It just has to extend the methods of this class!
 * Using reflection, this method creates the table headline and table content
 * with the layout provided by the template automatically!
 * @param cms A_CmsObject Object for accessing system resources
 * @param templateFile Filename of the template file 
 * @param elementName <em>not used here</em>.
 * @param parameters <em>not used here</em>.
 * @param templateSelector template section that should be processed.
 * @return Processed content of the given template file.
 * @exception CmsException 
 */
private byte[] getContentList(CmsObject cms, CmsXmlWpTemplateFile template, String elementName, Hashtable parameters, String templateSelector) throws CmsException {

	//return var
	byte[] processResult = null;

	// session will be created or fetched
	I_CmsSession session = (CmsSession) cms.getRequestContext().getSession(true);
	//get the class of the content definition	
	Class cdClass = getContentDefinitionClass();

	//read value of the inputfield filterparameter
	String filterParam = (String) parameters.get("filterparameter");
	//read value of the selected filter
	String filterMethodName = (String) parameters.get("selectbox");
	if (filterMethodName == null)
		filterMethodName = "";

	//init vars				
	String tableHead = "";
	String singleRow = "";
	String allEntrys = "";
	String entry = "";
	int columns = 0;

	// get number of columns
	Vector columnsVector = new Vector();
	String fieldNamesMethod = "getFieldNames";
	Class paramClasses[] = {CmsObject.class};
	Object params[] = {cms};
	columnsVector = (Vector) getContentMethodObject(cms, cdClass, fieldNamesMethod, paramClasses, params);
	columns = columnsVector.size();

	//create tableheadline
	CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
	for (int i = 0; i < columns; i++) {
		tableHead += (template.getDataValue("tabledatabegin")) + lang.getLanguageValue(C_BACKOFFICE_LABEL + columnsVector.elementAt(i).toString().toLowerCase()) + (template.getDataValue("tabledataend"));
	}
	//set template data for table headline content
	template.setData("tableheadline", tableHead);

	//get vector of filterMethods
	//& select the appropriate filter method, 
	//  if no filter is appropriate, select a default filter
	//& get number of rows for output	
	Vector tableContent = new Vector();
	try {
		Vector filterMethods = (Vector) cdClass.getMethod("getFilterMethods", new Class[] {CmsObject.class}).invoke(null, new Object[] {cms});
		FilterMethod filterMethod = null;
		FilterMethod filterName = (FilterMethod) filterMethods.elementAt(Integer.parseInt(filterMethodName));
		filterMethodName = filterName.getFilterName();
		//loop trough the filter methods and set the chosen one
		for (int i = 0; i < filterMethods.size(); i++) {
			FilterMethod currentFilter = (FilterMethod) filterMethods.elementAt(i);
			if (currentFilter.getFilterName().equals(filterMethodName)) {
				filterMethod = currentFilter;
				break;
			}
		}
		// the chosen filter does not exist, use the first one!		
		if (filterMethod == null) {
			filterMethod = (FilterMethod) filterMethods.firstElement();
		}

		// now apply the filter with the cms object, the filter method and additional user parameters
		tableContent = (Vector) cdClass.getMethod("applyFilter", new Class[] {CmsObject.class, FilterMethod.class, String.class}).invoke(null, new Object[] {cms, filterMethod, filterParam});
	} catch (InvocationTargetException ite) {
		//error occured while applying the filter
		System.err.println("Backoffice apply filter: InvocationTargetException!");
		ite.getTargetException().printStackTrace();
		templateSelector = "error";
		template.setData("filtername", filterMethodName);
		template.setData("filtererror", ite.getTargetException().getMessage());
	} catch (NoSuchMethodException nsm) {
		System.err.println("Backoffice apply filter: Requested method was not found!");
	} catch (Exception e) {
		System.err.println("Backoffice apply filter: Other Exception!");
		e.printStackTrace();
		templateSelector = "error";
		template.setData("filtername", filterMethodName);
	}
	//get the number of rows
	int rows = tableContent.size();

	// get the field methods from the content definition
	Vector fieldMethods = new Vector();
	try {
		fieldMethods = (Vector) cdClass.getMethod("getFieldMethods", new Class[] {CmsObject.class}).invoke(null, new Object[] {cms});
	} catch (Exception exc) {
		System.err.println("Backoffice fieldMethods throwed an exception");
		exc.printStackTrace();
	}

	// create output from the table data
	String fieldEntry = "";
	String id = "";
	for (int i = 0; i < rows; i++) {
		entry = "";
		singleRow = "";
		Object entryObject = new Object();
		
		//set data of single row
		entryObject = tableContent.elementAt(i);
		for (int j = 0; j < columns; j++) {
			// call the field methods  
			Method getMethod = (Method) fieldMethods.elementAt(j);
			try {
				fieldEntry = (String) getMethod.invoke(entryObject, new Object[0]);
			} catch (InvocationTargetException ite) {
				System.err.println("Backoffice table entry: Catched Exception while using reflection!");
				ite.getTargetException().printStackTrace();
			} catch (Exception e) {
				System.err.println("Backoffice table entry: Other exception!");
				e.printStackTrace();
			}
			
			//insert table entry
			if (fieldEntry != null)
				entry += (template.getDataValue("tabledatabegin")) + fieldEntry + (template.getDataValue("tabledataend"));
		}
		
		//get the unique id belonging to an entry
		try {
			id = (String) cdClass.getMethod("getUniqueId", new Class[] {CmsObject.class}).invoke(entryObject, new Object[] {cms});
		} catch (InvocationTargetException ite) {
			System.err.println("Backoffice getUniqueId: Catched Exception while using reflection!");
			ite.getTargetException().printStackTrace();
		} catch (NoSuchMethodException nsm) {
			System.err.println("Backoffice getUniqueId: Requested method was not found!");
		} catch (Exception e) {
			System.err.println("Backoffice getUniqueId: Output section throwed an exception!");
			e.printStackTrace();
		}

		//insert unique id in contextmenue
		if (id != null)
			template.setData("uniqueid", id);

		//set the lockstates for the actual entry
		setLockstates(template, cdClass, entryObject);

		//insert single table row in template 	
		template.setData("entry", entry);
		
		// processed row from template
		singleRow = template.getProcessedDataValue("singlerow", this);
		allEntrys += (template.getDataValue("tablerowbegin")) + singleRow + (template.getDataValue("tablerowend"));
	}

	//insert tablecontent in template		
	template.setData("tablecontent", "" + allEntrys);
	//save select box value into session
	session.putValue("selectbox", filterMethodName);

	//finally start the processing
	processResult = startProcessing(cms, template, elementName, parameters, templateSelector);
	return processResult;
}
/**
 * Gets the content of a given template file.
 * <P>
 * While processing the template file the table entry
 * <code>entryTitle<code> will be displayed in the delete dialog
 * 
 * @param cms A_CmsObject Object for accessing system resources
 * @param templateFile Filename of the template file 
 * @param elementName not used here
 * @param parameters get the parameters action for the button activity 
 * 					 and id for the used content definition instance object
 * @param templateSelector template section that should be processed.
 * @return Processed content of the given template file.
 * @exception CmsException 
 */

private byte[] getContentLock(CmsObject cms, CmsXmlWpTemplateFile template, String elementName, Hashtable parameters, String templateSelector) throws CmsException {

	//return var
	byte[] processResult = null;
	
	// session will be created or fetched
	I_CmsSession session = (CmsSession) cms.getRequestContext().getSession(true);
	//get the class of the content definition	
	Class cdClass = getContentDefinitionClass();
	
	//get (stored) id parameter
	String id = (String) parameters.get("id");
	if (id == null)
		id = "";
	if (id != "") {
		session.putValue("idsave", id);
	} else {
		String idsave = (String) session.getValue("idsave");
		if (idsave == null)
			idsave = "";
		id = idsave;		
		session.removeValue("idsave");
	}
	
	parameters.put("idlock",id);
	
	// get value of hidden input field action
	String action = (String) parameters.get("action");
	
	//no button pressed, go to the default section!
	if (action == null || action.equals("")) {
		//lock dialog, displays the title of the entry to be changed in lockstate
		Integer idInteger = Integer.valueOf(id);
		String ls = "";

		//access content definition constructor by reflection
		String title = "no title";
		Object o = null;
		o = getContentDefinitionConstructor(cms, cdClass, idInteger);
		try {
			Method getLockstateMethod = (Method) cdClass.getMethod("getLockstate", new Class[] {});
			ls = (String) getLockstateMethod.invoke(o, new Object[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//get the dialog from the langauge file and set it in the template
		CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
		template.setData("lockdialog", lang.getLanguageValue(C_BACKOFFICE_MESSAGEBOX_DIALOG + ls));

		//set the title of the selected entry 
		template.setData("newsentry", id);

		//go to default template section	
		template.setData("setaction", "default");
		templateSelector = "default";
		parameters.put("action","done");

	// confirmation button pressed, process data!
	} else {
		templateSelector = "done";
		session.removeValue("idlocksave");
		session.removeValue("idsave");

		//access content definition constructor by reflection
		Integer idInteger = Integer.valueOf(id);
		String ls = "";
		Object o = null;
		o = getContentDefinitionConstructor(cms, cdClass, idInteger);
		try {
			Method getLockstateMethod = (Method) cdClass.getMethod("getLockstate", new Class[] {});
			ls = (String) getLockstateMethod.invoke(o, new Object[0]);
			System.err.println("lock ls:"+ls);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//show the possible cases of a lockstate in the template
		//and change lockstate in content definition (and in DB or VFS)
		if (ls.equals("lockuser")) {
			//steal lock (userlock -> lock)		
			try {
				Method setLockstateMethod = (Method) cdClass.getMethod("setLockstate", new Class[] {String.class});
				setLockstateMethod.invoke(o, new Object[] {"lock"});
			} catch (Exception e) {
				e.printStackTrace();
			}
			//write to DB
			try {
				Method writeMethod = (Method) cdClass.getMethod("write", new Class[] {CmsObject.class});
				writeMethod.invoke(o, new Object [] {cms});
			} catch (Exception e) {
				e.printStackTrace();
			}
			templateSelector = "done";
		} else {
			if (ls.equals("lock")) {
				//unlock (lock -> nolock)
				try {
					Method setLockstateMethod = (Method) cdClass.getMethod("setLockstate", new Class[] {String.class});
					setLockstateMethod.invoke(o, new Object[] {"nolock"});
				} catch (Exception e) {
					e.printStackTrace();
				}
				//write to DB
				try {
					Method writeMethod = (Method) cdClass.getMethod("write", new Class[] {CmsObject.class});
					writeMethod.invoke(o, new Object [] {cms});
				} catch (Exception e) {
					e.printStackTrace();
				}
				templateSelector = "done";
			} else {
				//lock (nolock -> lock)
				try {
					Method setLockstateMethod = (Method) cdClass.getMethod("setLockstate", new Class[] {String.class});
					setLockstateMethod.invoke(o, new Object[] {"lock"});
				} catch (Exception e) {
					e.printStackTrace();
				}
				//write to DB
				try {
					Method writeMethod = (Method) cdClass.getMethod("write", new Class[] {CmsObject.class});
					writeMethod.invoke(o, new Object [] {cms});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	//finally start the processing
	processResult = startProcessing(cms, template, elementName, parameters, templateSelector);
	return processResult;
}
/**
 * gets the content definition class method object
 * @returns object content definition class method object
 */

private Object getContentMethodObject(CmsObject cms, Class cdClass, String method, Class paramClasses[], Object params[]) {

	//return value
	Object retObject = null;
	if (method != "") {
		try {
			retObject = cdClass.getMethod(method, paramClasses).invoke(null, params);
		} catch (InvocationTargetException ite) {
			System.err.println("Backoffice " + method + ": InvocationTargetException !");
			ite.getTargetException().printStackTrace();
		} catch (NoSuchMethodException nsm) {
			System.err.println("Backoffice " + method + ": Requested method was not found!");
		} catch (Exception e) {
			System.err.println("Backoffice " + method + ": Other Exception!");
			e.printStackTrace();
		}
	}
	return retObject;
}
/**
 * gets the content of a new entry form.
 * Has to be overwritten in your backoffice class!
 */

public abstract byte[] getContentNew(CmsObject cms, CmsXmlWpTemplateFile templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException;
/**
 * gets the create url by using the cms object
 * @returns a string with the create url
 */
 
public abstract String getCreateUrl(CmsObject cms);
/**
 * gets the delete url by using the cms object
 * @returns a string with the delete url
 */
 
public abstract String getDeleteUrl(CmsObject cms);
/**
 * gets the edit url by using the cms object
 * @returns a string with the edit url
 */
 
public abstract String getEditUrl(CmsObject cms);
/**
 *set the lockstates in the list output 
 */

private void setLockstates(CmsXmlWpTemplateFile template, Class cdClass, Object entryObject) {

	//init lock state vars	
	String la = "false";
	Object laObject = new Object();
	String ls = "";

	//is the content definition object (i.e. the table entry) lockable?
	try {
		//get the method
		Method laMethod = cdClass.getMethod("isLockable", new Class[] {});
		//get the returned object
		laObject = laMethod.invoke(null, null);
	} catch (InvocationTargetException ite) {
		System.err.println("Backoffice isLockable: Invocation target exception!");
		ite.getTargetException().printStackTrace();
	} catch (NoSuchMethodException nsm) {
		System.err.println("Backoffice isLockable: Requested method was not found!");
	} catch (Exception e) {
		System.err.println("Backoffice isLockable: other exception!");
		e.printStackTrace();
	}

	//cast the returned object to a string
	la = (String) laObject.toString();
	//if the cd is lockable...
	if (la.equals("true")) {
		//...get the lockstate of an entry		
		try {
			//get the method lockstate
			Method lsMethod = cdClass.getMethod("getLockstate", new Class[] {});
			//get returned object
			Object lsObject = lsMethod.invoke(entryObject, null);
			//caste the object to string
			ls = (String) lsObject;
		} catch (InvocationTargetException ite) {
			System.err.println("Backoffice getLockstate: Invocation target exception!");
			ite.getTargetException().printStackTrace();
		} catch (NoSuchMethodException nsm) {
			System.err.println("Backoffice getLockstate: Requested method was not found!");
		} catch (Exception e) {
			System.err.println("Backoffice getLockstate: Other exception!");
			e.printStackTrace();
		}
		
	} else {
		//the entry is not lockable: use standard contextmenue
		template.setData("backofficecontextmenue", "backofficeedit");
	}
	try {
		//show the possible cases of a lockstate in the template
		if (ls.equals("lockuser")) {
			ls = template.getDataValue("lockuser");
			template.setData("lockedby", ls);
			template.setData("backofficecontextmenue", "backofficelockuser");
		} else {
			if (ls.equals("lock")) {
				ls = template.getDataValue("lock");
				template.setData("lockedby", ls);
				template.setData("backofficecontextmenue", "backofficelock");
			} else {
				ls = template.getDataValue("nolock");
				template.setData("lockedby", ls);
				template.setData("backofficecontextmenue", "backofficenolock");
			}
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
}
}
