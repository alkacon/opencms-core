/*
		Fini's HTML Table Functions v1.0 by Fini A: Alring (C) 2000

		Purpose: A clean way to produce tables dynamically.
*/

function addTable(cnt, opt) {

	if(!opt) {
		var opt = '';
	}
	else {
		opt = ' ' + opt;
	}

	return '<table' + opt + '>' + cnt + '</table>';
}


function addTR(cnt, opt) {

	if(!opt) {
		var opt = '';
	}
	else {
		opt = ' ' + opt;
	}
	
	return '<tr' + opt + '>' + cnt + '</tr>';
}


function addTD(cnt, opt) {

	if(!opt) {
		var opt = '';
	}
	else {
		opt = ' ' + opt;
	}
	
	return '<td' + opt + '>' + cnt + '</td>';
}


function addLink(href, cnt, opt) {

	if(!opt) {
		var opt = '';
	}
	else {
		opt = ' ' + opt;
	}

	return '<a href=\"' + href + '\"' + opt + '>' + cnt + '</a>';
}


function addSpan(cnt, opt) {

	if(!opt) {
		var opt = '';
	}
	else {
		opt = ' ' + opt;
	}

	return '<span ' + opt + '>' + cnt + '</span>';
}