/* parse the entered query String */
function parseSearchQuery(theForm, message) {
	var queryValue = theForm.elements["query2"].value;
	var testValue = queryValue.replace(/ /g, "");
	if (testValue.length < 3) {
		alert(message);
		return (false);
	}
	queryValue = queryValue.replace(/\+/g, "%2b");
	queryValue = queryValue.replace(/\-/g, "%2d");
	theForm.elements["query"].value = queryValue;
	return (true);
}

/* open the print version popup window */
function openPrintVersion() {
	window.open(document.location.pathname + "?print=true", "print", "width=670,height=750,dependent=yes,status=no,toolbar=no,location=no,scrollbars=yes");
}

/* open the imprint popup window */
function openImprint(imprintUri, pageUri, theLocale) {
	pageUri = encodeURIComponent(pageUri); 
	pageUri = "?__locale=" + theLocale + "&uri=" + pageUri;
	window.open(imprintUri + pageUri, "imprint", "width=670,height=550,dependent=yes,status=no,toolbar=no,location=no,scrollbars=yes,resizable=yes");
}

/* open the recommend page form popup window */
function openRecommendForm(recommendUri, pageUri, theLocale) {	
	if (window.location.search != "") {
		pageUri += window.location.search;		
	}
	pageUri = encodeURIComponent(pageUri); 
	pageUri = "?__locale=" + theLocale + "&uri=" + pageUri;
	window.open(recommendUri + pageUri, "recommend", "width=670,height=700,dependent=yes,status=no,toolbar=no,location=no,scrollbars=yes,resizable=yes");
}

/* open the recommend page form popup window */
function openLetterForm(letterUri, pageUri, theLocale) {	
	if (window.location.search != "") {
		pageUri += window.location.search;		
	}
	pageUri = encodeURIComponent(pageUri); 
	pageUri = "?__locale=" + theLocale + "&uri=" + pageUri;
	window.open(letterUri + pageUri, "contact", "width=670,height=700,dependent=yes,status=no,toolbar=no,location=no,scrollbars=yes,resizable=yes");
}