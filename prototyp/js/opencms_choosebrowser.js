/*
 * File   : $Source: /alkacon/cvs/opencms/prototyp/js/Attic/opencms_choosebrowser.js,v $
 * Date   : $Date: 2000/03/21 16:48:31 $
 * Version: $Revision: 1.4 $
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

//---------------------------------
// help to choose the right browser
// m.schleich 21.12.1999
// changed am 03.01.2000
//---------------------------------
ns = (document.layers)? true:false;
ie = (document.all)? true:false;
if(ie)
{
	if (navigator.userAgent.indexOf('MSIE 5')>0)
	{
		ie5 = true;
	}
	else
	{
		ie5 = false;
	}
}

if(ns)
{
	var layerzeigen_01= 'document.layers.';
	var layerzeigen_02= '.visibility="show"';
	var layerverstecken_01= 'document.layers.';
	var layerverstecken_02= '.visibility="hide"';
	var xpos_01= 'document.';
	var xpos_02= '.left=';
	var ypos_01= 'document.';
	var ypos_02= '.top=';
	var xoffset= '+3';
	var yoffset= '+3';
	var imgonlyr='document.top.document.'
	var lyrtxt='document.days.document.write(txt); document.days.document.close()';
}
else
{
	var layerzeigen_01= 'document.all.';
	var layerzeigen_02= '.style.visibility="visible"';
	var layerverstecken_01= 'document.all.';
	var layerverstecken_02= '.style.visibility="hidden"';
	var xpos_01= 'document.all.';
	var xpos_02= '.style.left=';
	var ypos_01= 'document.all.';
	var ypos_02= '.style.top=';
	var xoffset= '+3';
	var yoffset= '+3';
	var imgonlyr='document.'
	var lyrtxt = 'document.all.days.innerHTML = txt';
	
	if(ie5)
	{
		var xpos_01= 'document.all.';
		var xpos_02= '.style.left=';
		var ypos_01= 'document.all.';
		var ypos_02= '.style.top=';
		var xoffset= '+3+document.body.scrollLeft';
		var yoffset= '+3+document.body.scrollTop';
	}
}

var shown=false;

//------------------------------------------------------------------------------------
// show and hyde layers
// m.schleich 26.01.2000
//------------------------------------------------------------------------------------
function switchlyr(show, hide)
{
		eval(layerverstecken_01+hide+layerverstecken_02);
		eval(layerzeigen_01+show+layerzeigen_02);
}

function showlyr(welche)
{

		eval(layerzeigen_01+welche+layerzeigen_02);
		//shown = true;
}
function hidelyr(welche)
{

		eval(layerverstecken_01+welche+layerverstecken_02);
		//shown = false;
}

//------------------------------------------------------------------------------------
// content exchange between 2 layers (used in explorer_files_neu_ordner.html)
// m.schreiber 21.02.2000
//------------------------------------------------------------------------------------
var data1=null;
var data2=null;

function saveLayerData(from,to,srcLayer,destLayer) {
	
	if (ie) {
		data1 = document.forms[from].NEUNAME.value;
		data2 = document.forms[from].NEUTITEL.value;
		document.forms[to].NEUNAME.value = data1;
		document.forms[to].NEUTITEL.value = data2;
	}
	else if (ns) {
		data1 = document[srcLayer].document.forms[from].NEUNAME.value;
		data2 = document[srcLayer].document.forms[from].NEUTITEL.value;
		document[destLayer].document.forms[to].NEUNAME.value = data1;
		document[destLayer].document.forms[to].NEUTITEL.value = data2;
	}
}