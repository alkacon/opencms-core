function listAction(listId, action, confirmation, listItem) {
	var form = document.forms[listId + '-form'];
	if (confirmation!='null' && confirmation!='') {
		if (!confirm(confirmation)) {
			return false;
		}
	}
	form.action.value='listsingleaction';
	form.listaction.value=action;
	form.selitems.value=listItem;
	submitForm(form);
}

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

function listMultiAction(listId, action, confirmation, noselectionhelp) {
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

function listSort(listId, column) {
	var form = document.forms[listId + '-form'];
	form.action.value = 'listsort';
	form.sortcol.value = column;
	submitForm(form);
}

function listSetPage(listId, page) {
	var form = document.forms[listId + '-form'];
	form.action.value = 'listselectpage';
	form.page.value = page;
	submitForm(form);
}
