/*
 * File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.workplace.administration/resources/system/workplace/resources/admin/javascript/list.js,v $
 * Date   : $Date: 2005/06/23 13:46:06 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/**
 * Executes a list action.<p>
 *
 * @param listId the id of the list
 * @param action the id of the action to execute
 * @param confirmation a confirmation string or the id of a div tag with the confirmation string
 * @param listItem the affected list item
 */
function listAction(listId, action, confirmation, listItem) {
	var form = document.forms[listId + '-form'];
	
	// use the param content as default
	var confText = confirmation;
	try {
	    // try to user the param as an id of a div tag, and use its content
		confText = document.getElementById(confirmation).firstChild.nodeValue;
	} catch (e) {
	    // ignore
	}
	if (confText!='null' && confText!='') {
		if (!confirm(confText)) {
			return false;
		}
	}
	form.action.value='listsingleaction';
	form.listaction.value=action;
	form.selitems.value=listItem;
	submitForm(form);
}

/**
 * Executes a list independent action.<p>
 *
 * @param listId the id of the list
 * @param action the id of the independent action to execute
 * @param confirmation a confirmation text 
 */
function listIndepAction(listId, action, confirmation) {
	var form = document.forms[listId + '-form'];
	if (confirmation!='null' && confirmation!='') {
		if (!confirm(confirmation)) {
			return false;
		}
	}
	form.action.value='listindependentaction';
	form.listaction.value=action;
	submitForm(form);
}

/**
 * Selects/Deselects all no-disabled multi action checkboxes.<p>
 *
 * @param listId the id of the list
 */
function listSelect(listId) {
	var form = document.forms[listId + '-form'];
	for (i = 0 ; i < form.elements.length; i++) {
		if ((form.elements[i].type == 'checkbox') && (form.elements[i].name == 'listMultiAction')) {
			if (!(form.elements[i].value == 'DISABLED' || form.elements[i].disabled)) {
				form.elements[i].checked = form.listSelectAll.checked;
			}
		}
	}
	return true;
}

/**
 * Executes a list multi action.<p>
 *
 * @param listId the id of the list
 * @param action the id of the multi action to execute
 * @param confirmation a confirmation text
 * @param noselectionhelp a help text displayed when there are no selected items 
 */
function listMAction(listId, action, confirmation, noselectionhelp) {
	var form = document.forms[listId + '-form'];
	var count = 0;
	var listItems = '';
	for (i = 0 ; i < form.elements.length; i++) {
		if ((form.elements[i].type == 'checkbox') && (form.elements[i].name == 'listMultiAction')) {
			if (form.elements[i].checked && !(form.elements[i].value == 'DISABLED' || form.elements[i].disabled)) {
				count++;
				if (listItems!='') {
					listItems = listItems + '|';
				}
				listItems = listItems + form.elements[i].value;
			}
		}
	}
	if (count==0) {
		alert(noselectionhelp);
		return false;
	}
	if (confirmation!='null' && confirmation!='') {
		if (!confirm(confirmation)) {
			return false;
		}
	}
	form.action.value='listmultiaction';
	form.listaction.value=action;
	form.selitems.value=listItems;
	submitForm(form);
}

/**
 * Executes a list search action.<p>
 *
 * @param listId the id of the list
 * @param action the id of the search action to execute
 * @param confirmation a confirmation text
 */
function listSearchAction(listId, action, confirmation) {
	var form = document.forms[listId + '-form'];
	if (confirmation!='null' && confirmation!='') {
		if (!confirm(confirmation)) {
			return false;
		}
	}
	form.action.value = 'listsearch';
	if (action=='showall') {
		form.searchfilter.value = '';
	} else if (action=='search') {
		form.searchfilter.value = form.listSearchFilter.value;
	}
	submitForm(form);
	return;
}

/**
 * Sorts a list column.<p>
 *
 * @param listId the id of the list
 * @param column the id of the column to sort
 */
function listSort(listId, column) {
	var form = document.forms[listId + '-form'];
	form.action.value = 'listsort';
	form.sortcol.value = column;
	submitForm(form);
}

/**
 * Sets the visible page of a list.<p>
 *
 * @param listId the id of the list
 * @param page the page number to set
 */
function listSetPage(listId, page) {
	var form = document.forms[listId + '-form'];
	form.action.value = 'listselectpage';
	form.page.value = page;
	submitForm(form);
}
