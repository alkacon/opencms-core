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
//import com.opencms.util.*;

import java.util.*;
//import java.io.*;

import java.lang.reflect.*;
//import java.lang.String;


/**
 * Abstract class for generic backoffice display. It automatically
 * generates the <ul><li>head section with filters and buttons,</li>
 * 					<li>body section with the table data,</li>
 * 					<li>lock states of the entries. if there are any,</li>
 *					<li>delete dialog,</li>
 *					<li>lock dialog.</li></ul>
 * calls the <ul><li>edit dialog of the calling backoffice class</li>
 *				<li>new dialog of the calling backoffice class</li></ul>
 * using the content definition class defined by the getContentDefinition method.
 * The methods and data provided by the content definition class
 * is accessed by reflection. This way it is possible to re-use
 * this class for any content definition class, that just has
 * to extend the A_ContentDefinition class!
 * Creation date: (27.10.00 10:04:42)
 * author: Michael Knoll
 * version 1.0
 */
public abstract class A_Backoffice extends CmsWorkplaceDefaultNoCache {
	
	

/**
 * gets the backoffice url of the module by using the cms object
 * @returns a string with the backoffice url
 */
 
public abstract String getBackofficeUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws Exception;
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
	String filterParam = (String) parameters.get("filterparameter");	
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
		//process the head frame containing the filter 
		returnProcess = getContentHead(cms, template, elementName, parameters, templateSelector);
		//finally return processed data
		return returnProcess;
	} else {
		//process the body frame containing the table
		if (selectbox != null) {
			//process the list output
			if (filterParam != null) parameters.put("filterparameter",filterParam);
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
				if (((iddelete != null) || (iddeletesave != null)) && (idlock == null)) {
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
		if (A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice: Invocation target exception!");
		}
	} catch (NoSuchMethodException nsm) {
		if (A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice: Requested method was not found!");
		}
	} catch (InstantiationException ie) {
		if (A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice: the reflected class is abstract!");
		}
	} catch (Exception e) {
		if (A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice: Other exception!");
		}
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
			templateSelector = "delete";

			//create appropriate class name with underscores for labels
			String moduleName = "";
			moduleName = (String) getClass().toString(); //get name
			moduleName = moduleName.substring(5); //remove 'class' substring at the beginning
			moduleName = moduleName.trim();
			moduleName = moduleName.replace('.', '_'); //replace dots with underscores

			//create new language file object
			CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);

			//get the dialog from the langauge file and set it in the template
			template.setData("deletetitle", lang.getLanguageValue("messagebox.title.delete"));
			template.setData("deletedialog", lang.getLanguageValue("messagebox.message1.delete"));
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
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice: delete method throwed an exception!");
			}
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
	String filterParam = (String) parameters.get("filterparameter");
	//store filterparameter in the session
	if (filterParam != null) session.putValue("filterparameter", filterParam);
	
	//create appropriate class name with underscores for labels
	String moduleName = "";
	moduleName = (String) getClass().toString(); //get name
	moduleName = moduleName.substring(5); //remove 'class' substring at the beginning
	moduleName = moduleName.trim();
	moduleName = moduleName.replace('.', '_'); //replace dots with underscores
	
	//create new language file object
	CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
	//set labels in the template
	template.setData("filter", lang.getLanguageValue(moduleName + ".label.filter"));
	template.setData("filterparameterlabel", lang.getLanguageValue(moduleName + ".label.filterparameter"));

	//get vector of filter names from the content definition
	Vector filterMethods = new Vector();
	try {
		filterMethods = (Vector) cdClass.getMethod("getFilterMethods", new Class[] {CmsObject.class}).invoke(null, new Object[] {cms});
	} catch (InvocationTargetException ite) {
		//error occured while applying the filter
		if (A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice getContentHead: InvocationTargetException!");
		}
	} catch (NoSuchMethodException nsm) {
		if (A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice getContentHead: Requested method was not found!");
		}
	} catch (Exception e) {
		if (A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice getContentHead: Problem occured with your filter methods!");
		}
	}

	//no filter selected so far, store a default filter in the session
	FilterMethod filterMethod = null;
	if (selectBoxValue == null) {
		FilterMethod defaultFilter = (FilterMethod) filterMethods.firstElement();
		session.putValue("selectbox", defaultFilter.getFilterName());
	}
	
	if (filterParam != null) parameters.put("filterparameter", filterParam);
	
	//set the select box
	for (int i = 0; i < filterMethods.size(); i++) {
		FilterMethod currentFilter = (FilterMethod) filterMethods.elementAt(i);
		//insert filter in the template selectbox 
		template.setData("selectname", currentFilter.getFilterName());
		template.setData("selectnumber", "" + i);
		//add additional inputfield, if filter allows parameters
		template.setData("selectparameter", "" + currentFilter.hasUserParameter());
		template.setData("filterparameter", filterParam);
		//get processed data from the template
		try {
			singleSelection = template.getProcessedDataValue("singleselection", this);
			allSelections += singleSelection;
		} catch (Exception e) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice getContentHead: Error while getting processedDataValue from Backoffice template!");
			}
		}
	}

	//if getCreateUrl equals null, the "create new entry" button 
	//will not be displayed in the template
	String createButton = null;
	try {
		createButton = (String) getCreateUrl(cms, null, null, null);
	} catch (Exception e) {
	}
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
	
	//change template to head section to read out the filter
	templateSelector = "head";
	
	//read value of the inputfield filterparameter
	String filterParam = (String) session.getValue("filterparameter");
	if (filterParam == "") filterParam = null;
	
	//read value of the selected filter
	String filterMethodName = (String) parameters.get("selectbox");
	if (filterMethodName == null)
		filterMethodName = "";
		
	//change template to list section for data list output 	
	templateSelector = "list";
	
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

	//create appropriate class name with underscores for labels
	String moduleName = "";
	moduleName = (String) getClass().toString(); //get name
	moduleName = moduleName.substring(5); //remove 'class' substring at the beginning
	moduleName = moduleName.trim();
	moduleName = moduleName.replace('.', '_'); //replace dots with underscores

	//create new language file object
	CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);

	//create tableheadline
	for (int i = 0; i < columns; i++) {
		tableHead += (template.getDataValue("tabledatabegin")) 
		+ lang.getLanguageValue(moduleName + ".label." + columnsVector.elementAt(i).toString().toLowerCase().trim()) 
		+ (template.getDataValue("tabledataend"));
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
		if (A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice: apply filter throwed an InvocationTargetException!");
		}
		templateSelector = "error";
		template.setData("filtername", filterMethodName);
		template.setData("filtererror", ite.getTargetException().getMessage());
		session.removeValue("filterparameter");
	} catch (NoSuchMethodException nsm) {
		if (A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice: apply filter method was not found!");
		}
	} catch (Exception e) {
		if (A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice: apply filter: Other Exception!");
		}
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
		if (A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice getFieldMethods throwed an exception");
		}
	}

	// create output from the table data
	String fieldEntry = "";
	String id = "";
	String url = "";
	for (int i = 0; i < rows; i++) {
		//init	
		entry = "";
		singleRow = "";
		Object entryObject = new Object();
		entryObject = tableContent.elementAt(i); //cd object in row #i

		//get the url belonging to an entry
		try {
			url = (String) cdClass.getMethod("getUrl", new Class[] {}).invoke(entryObject, new Object[] {});
		} catch (InvocationTargetException ite) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice: getUrl throwed an InvocationTargetException!");
			}
			ite.getTargetException().printStackTrace();
		} catch (NoSuchMethodException nsm) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice: getUrl method was not found!");
			}
		} catch (Exception e) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice: getUrl: Other exception!");
			}
		}
		if (url == null)
			url = "";

		//set data of single row
		for (int j = 0; j < columns; j++) {
			// call the field methods
			Method getMethod = null;
			try {
				getMethod = (Method) fieldMethods.elementAt(j);
			} catch (Exception e) {
				if (A_OpenCms.isLogging()) {
					A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Could not get field method!");
				}
			}							
			try {
				//apply methods on content definition object
				fieldEntry = (String) getMethod.invoke(entryObject, new Object[0]);
			} catch (InvocationTargetException ite) {
				if (A_OpenCms.isLogging()) {
					A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice content definition object throwed an InvocationTargetException!");
				}
			} catch (Exception e) {
				if (A_OpenCms.isLogging()) {
					A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice content definition object: Other exception!");
				}
			}

			//insert table entry
			if (fieldEntry != null)
				entry += (template.getDataValue("tabledatabegin")) + (template.getDataValue("urlbegin")) + url + (template.getDataValue("urlend")) + fieldEntry + (template.getDataValue("tabledataend"));
		}

		//get the unique id belonging to an entry
		try {
			id = (String) cdClass.getMethod("getUniqueId", new Class[] {CmsObject.class}).invoke(entryObject, new Object[] {cms});
		} catch (InvocationTargetException ite) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice: getUniqueId throwed an InvocationTargetException!");
			}
		} catch (NoSuchMethodException nsm) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice: getUniqueId method was not found!");
			}
		} catch (Exception e) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice: getUniqueId: Other exception!");
			}
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
	parameters.put("idlock", id);

	// get value of hidden input field action
	String action = (String) parameters.get("action");

	//no button pressed, go to the default section!
	if (action == null || action.equals("")) {
		//lock dialog, displays the title of the entry to be changed in lockstate
		templateSelector = "lock";
		Integer idInteger = Integer.valueOf(id);
		String ls = "";

		//access content definition object specified by id through reflection
		String title = "no title";
		Object o = null;
		o = getContentDefinitionConstructor(cms, cdClass, idInteger);
		try {
			Method getLockstateMethod = (Method) cdClass.getMethod("getLockstate", new Class[] {});
			ls = (String) getLockstateMethod.invoke(o, new Object[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//create appropriate class name with underscores for labels
		String moduleName = "";
		moduleName = (String) getClass().toString(); //get name
		moduleName = moduleName.substring(5); //remove 'class' substring at the beginning
		moduleName = moduleName.trim();
		moduleName = moduleName.replace('.', '_'); //replace dots with underscores
		//create new language file object
		CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
		//get the dialog from the langauge file and set it in the template
		template.setData("locktitle", lang.getLanguageValue("messagebox.title." + ls));
		template.setData("lockstate", lang.getLanguageValue("messagebox.message1." + ls));

		//set the title of the selected entry 
		template.setData("newsentry", id);

		//go to default template section	
		template.setData("setaction", "default");
		parameters.put("action", "done");

		// confirmation button pressed, process data!
	} else {
		templateSelector = "done";
		session.removeValue("idsave");

		//access content definition constructor by reflection
		Integer idInteger = Integer.valueOf(id);
		String ls = "";
		Object o = null;
		o = getContentDefinitionConstructor(cms, cdClass, idInteger);
		try {
			Method getLockstateMethod = (Method) cdClass.getMethod("getLockstate", new Class[] {});
			ls = (String) getLockstateMethod.invoke(o, new Object[0]);
		} catch (Exception e) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + " Backoffice getContentLock: Method getLockstate throwed an exception!");
			}
		}

		//show the possible cases of a lockstate in the template
		//and change lockstate in content definition (and in DB or VFS)
		if (ls.equals("lockuser")) {
			//steal lock (userlock -> lock)		
			try {
				Method setLockstateMethod = (Method) cdClass.getMethod("setLockstate", new Class[] {String.class});
				setLockstateMethod.invoke(o, new Object[] {"lock"});
			} catch (Exception e) {
				if (A_OpenCms.isLogging()) {
					A_OpenCms.log(C_OPENCMS_INFO, getClassName() + " Backoffice getContentLock: Method setLockstate throwed an exception!");
				}
			}
			//write to DB
			try {
				Method writeMethod = (Method) cdClass.getMethod("write", new Class[] {CmsObject.class});
				writeMethod.invoke(o, new Object[] {cms});
			} catch (Exception e) {
				if (A_OpenCms.isLogging()) {
					A_OpenCms.log(C_OPENCMS_INFO, getClassName() + " Backoffice getContentLock: Method write throwed an exception!");
				}
			}
			templateSelector = "done";
		} else {
			if (ls.equals("lock")) {
				//unlock (lock -> nolock)
				try {
					Method setLockstateMethod = (Method) cdClass.getMethod("setLockstate", new Class[] {String.class});
					setLockstateMethod.invoke(o, new Object[] {"nolock"});
				} catch (Exception e) {
					if (A_OpenCms.isLogging()) {
						A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice getContentLock: Could not set lockstate!");
					}
				}
				//write to DB
				try {
					Method writeMethod = (Method) cdClass.getMethod("write", new Class[] {CmsObject.class});
					writeMethod.invoke(o, new Object[] {cms});
				} catch (Exception e) {
					if (A_OpenCms.isLogging()) {
						A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice getContentLock: Could not set lockstate!");
					}
				}
				templateSelector = "done";
			} else {
				//lock (nolock -> lock)
				try {
					Method setLockstateMethod = (Method) cdClass.getMethod("setLockstate", new Class[] {String.class});
					setLockstateMethod.invoke(o, new Object[] {"lock"});
				} catch (Exception e) {
					if (A_OpenCms.isLogging()) {
						A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice getContentLock: Could not set lockstate!");
					}
				}
				//write to DB/VFS
				try {
					Method writeMethod = (Method) cdClass.getMethod("write", new Class[] {CmsObject.class});
					writeMethod.invoke(o, new Object[] {cms});
				} catch (Exception e) {
					if (A_OpenCms.isLogging()) {
						A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice getContentLock: Could not write to content definition!");
					}
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
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + method + " throwed an InvocationTargetException!");
			}
			ite.getTargetException().printStackTrace();
		} catch (NoSuchMethodException nsm) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + method + ": Requested method was not found!");
			}
		} catch (Exception e) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + method + ": Other Exception!");
			}
		}
	}
	return retObject;
}
/**
 * gets the content of a new entry form.
 * Has to be overwritten in your backoffice class!
 */

public byte[] getContentNew(CmsObject cms, CmsXmlWpTemplateFile templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {

	parameters.put("id", "new");
	return getContentNew(cms, templateFile, elementName, parameters, templateSelector);	
}
/**
 * gets the create url by using the cms object
 * @returns a string with the create url
 */
 
public abstract String getCreateUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws Exception;
/**
 * gets the edit url by using the cms object
 * @returns a string with the edit url
 */
 
public abstract String getEditUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) throws Exception;
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
		if (A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice setLockstates: Method isLockable throwed an Invocation target exception!");
		}
	} catch (NoSuchMethodException nsm) {
		if (A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice setLockstates: Requested method isLockable was not found!");
		}
	} catch (Exception e) {
		if (A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice setLockstates: Method isLockable throwed an exception!");
		}
	}

	//cast the returned object to a string
	la = (String) laObject.toString();
	if (la.equals("false")) {
		try{
			//the entry is not lockable: use standard contextmenue
			template.setData("backofficecontextmenue", "backofficeedit");
			template.setData("lockedby", template.getDataValue("nolock"));
		} catch  (Exception e) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice setLockstates:'not lockable' section hrowed an exception!");
			}
		}	
	} else {
		//...get the lockstate of an entry		
		try {
			//get the method lockstate
			Method lsMethod = cdClass.getMethod("getLockstate", new Class[] {});
			//get returned object
			Object lsObject = lsMethod.invoke(entryObject, null);
			//caste the object to string
			ls = (String) lsObject;
		} catch (InvocationTargetException ite) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice setLockstates: Method getLockstate throwed an Invocation target exception!");
			}
		} catch (NoSuchMethodException nsm) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice setLockstates: Requested method getLockstate was not found!");
			}
		} catch (Exception e) {
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice setLockstates: Method getLockstate throwed an exception!");
			}
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
			if (A_OpenCms.isLogging()) {
				A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice setLockstates throwed an exception!");
			}
		}
	}
}
}
