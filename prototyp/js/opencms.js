/*
 * File   : <CVS Filename>
 * Date   : <CVS Checkout Date>
 * Version: <CVS Version>
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

//------------------------------------------
// global variables und functions
// m.schleich 03.01.2000
//------------------------------------------

var admincontenturl="administration_content_top.html";
//var taskcontenturl="tasks_content_nafm.html";
var selectedTask=0;
var lastVisited="tasks_content_nafm.html";
var formID;

// List of valid characters for INPUT fields containing names:
var charList="0123456789-._~abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

// perform a simple browser check
ns = (document.layers)? true:false;
ie = (document.all)? true:false;

// Formularfelder (Textfelder) auf Inhalt überprüfen
function check_textfeld(formular, feld)
{
if(document[formular][feld].value=="")
	{
		alert('Bitte geben Sie einen Namen an!');
		document[formular][feld].focus();
		return false;
	}
return true;
}
// get date
	aktDat = new Date;
	aktTag= aktDat.getDate();
	aktMonat= aktDat.getMonth()+1;
	aktJahr= aktDat.getFullYear();
	
//------------------------------------------------------------------------------------
// special functions
// m.schleich 05.01.1999
//------------------------------------------------------------------------------------
function default_dateiangaben() //to set defaults in user sets for filelist
{
	document.EINST_EXPL.DANGABEN[0].checked=true;
	document.EINST_EXPL.DANGABEN[1].checked=false;
	document.EINST_EXPL.DANGABEN[2].checked=true;
	document.EINST_EXPL.DANGABEN[3].checked=false;
	document.EINST_EXPL.DANGABEN[4].checked=true;
	document.EINST_EXPL.DANGABEN[5].checked=false;
	document.EINST_EXPL.DANGABEN[6].checked=true;
	document.EINST_EXPL.DANGABEN[7].checked=false;
	document.EINST_EXPL.DANGABEN[8].checked=false;
}


//------------------------------------------------------------------------------------
// mouseover- and clicked-effects
// m.schleich 20.12.1999
//------------------------------------------------------------------------------------

var imgEndOn = "_on.gif";
var imgEndOff = "_off.gif";
var imgEndPush = "_push.gif";
var imgEndInakitv = "_in.gif";
var pfad="pics/";


function ChOn(imgID) //changes a imageobject on mouseover
{
document.images[imgID].src = pfad + imgID + imgEndOn;
}

function ChOff(imgID) //changes a imageobject on mouseout
{
document.images[imgID].src = pfad + imgID + imgEndOff;
}

function ChPush(imgID) //changes a imageobject on clicked or mousedown
{
document.images[imgID].src = pfad + imgID + imgEndPush;
}

function ChIn(imgID) //changes a imageobject on clicked or mousedown
{
document.images[imgID].src = pfad + imgID + imgEndInaktiv;
}

function ChImg(imgID,src)
{
document.images[imgID].src = src;
}

//------------------------------------------------------------------------------------
// updates a frame
// m.schleich 01.02.2000
//------------------------------------------------------------------------------------
function updateFrame(which, frameurl)
{
	eval('window.top.'+which+'.location.href="'+frameurl+'"');

}

//------------------------------------------------------------------------------------
// open new window
// m.schleich 20.12.1999
//------------------------------------------------------------------------------------

var workplace;

function openwin(url, name, w, h) //opens a new window with parameter URL, Windowname (free choosable), width and height
{
	if(w==0) w=screen.availWidth-20;
	if(h==0) h=screen.availHeight-100;

	workplace = window.open(url,name, 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,width='+w+',height='+h);
	if(workplace != null)
	   {
	      if (workplace.opener == null)
	      {
	         workplace.opener = self;
	      }
   		}
	else
	{
		workplace.moveTo(0,0);
	}
	workplace.focus();
	
}

//------------------------------------------------------------------------------------
// open new window
// m.schleich 26.01.2000
//------------------------------------------------------------------------------------

var smallwindow;

function opensmallwin(url, name, w, h) //opens a new window with parameter URL, Windowname (free choosable), width and height
{
	smallwindow = window.open(url, name, 'toolbar=no,location=no,directories=no,status=no,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width='+w+',height='+h);
	if(smallwindow != null)
	   {
	      if (smallwindow.opener == null)
	      {
	         smallwindow.opener = self;
	      }
   		}
	//smallwindow.focus();

}


//------------------------------------------------------------------------------------
// which radiobutton is checked
// m.schleich 11.01.1999
// 
// parameters:
// formname: name of the form
// radionae: name of the radiobottuns group
// i: how many radiobuttons are there
// return: the value of the checked radiobutton
//------------------------------------------------------------------------------------

function check_radio(formname, radioname, i)
{
	for(j=0;j<i;j++)
	{
		if(document[formname][radioname][j].checked)
		{
			return document[formname][radioname][j].value;
		}

	}

}

//------------------------------------------------------------------------------------
// ceck date
// 
// author:	Michaela Schleich
// company:	mindfact interaktive medien ag
// date:	01.11.1999
// update:	14.01.2000
//
// input date (tt.mm.jjjj)
//
// returns FALSE if date is not a valid date
// returns TRUE if date valid
//------------------------------------------------------------------------------------
function checkDate(eDat)
{
// get date
	schaltjahr=false;
	aktDat = new Date;
	aktTag= aktDat.getDate();
	aktMonat= aktDat.getMonth()+1;
	aktJahr= aktDat.getFullYear();
	

// split the userdate
	eTag = eDat.substring(0,2);
	eMonat = eDat.substring(3,5);
	eJahr = eDat.substring(6,10);
	
	if(eJahr<aktJahr)
	{
		alert("Die Fälligkeit liegt in der Vergangenheit");
		return false;
	}
	
	if((eMonat<aktMonat) && (eJahr==aktJahr))
	{
		alert("Die Fälligkeit liegt in der Vergangenheit");
		return false;
	}
	
	if((eTag<aktTag) && (eMonat==aktMonat) && (eJahr==aktJahr))
	{
		alert("Die Fälligkeit liegt in der Vergangenheit");
		return false;
	}

	
	if(eDat.length != 10 || isNaN(eTag) || isNaN(eMonat) || isNaN(eJahr))
		{
		alert("Bitte das Datum in Form von tt.mm.jjjj eingeben.");
		return false;
		}
	else
		{
		if(eMonat < 1 || eMonat > 12)
		{
		alert("Bitte überprüfen Sie den eingegeben Monat.");
		return false;
		}
		else
			{
			if(eTag > 31 || eTag<=00)
			{
			alert("Bitte ueberprüfen Sie den Tag des Datums.");
			return false;
			}
			else
			{

// if must be split, to long for browser interpreter
				if(((eTag == 31) && (eMonat == 2)) || ((eTag == 31) && (eMonat == 4)) || ((eTag == 31) && (eMonat == 6)))
				{
				alert('Bitte ueberpruefen Sie den Tag des Datums.');
				return false;
				}
				if((eTag == 30 && eMonat == 2) || (eTag == 31 && eMonat==9) || (eTag==31 && eMonat==11))
				{
				alert('Bitte ueberpruefen Sie den Tag des Datums.');
				return false;
				}
				else
				{
				if(eTag==29 && eMonat == 2)
					{
					sj = eJahr%4;
					if(sj==0)
					schaltjahr=true;

					sj = eJahr%100;
					if(sj==0)
					schaltjahr=false;
		
					sj = eJahr%400;
					if(sj==0)
					schaltjahr=true;

					if(!schaltjahr)
					{
					alert(eJahr + " ist kein Schaltjahr.");
					return false;
					}
					}
				}
			}
		}		
	}
	return true;
}

//------------------------------------------------------------------------------------
// checkPiclistNav() 
// 
// author:	Matthias Schreiber
// company:	mindfact interaktive medien ag
// date:	07.02.2000
// update:	
//
// Checks if there is a previous and/or next content document.
// Due to the result the correct navigation document for the picture browser is loaded.
// 
// 
//------------------------------------------------------------------------------------
function checkPiclistNav() 
{
	version="";
	
	if ((EOL == true)&&(BOL == true))
		version = "3";
		else if (BOL == true)
			version = "2";
			else if (EOL == true)
				version = "1";
				
	parent.frames[0].document.location.href="edit_html_piclist_head" + version + ".html";
}	


//------------------------------------------------------------------------------------
// checkFormData(formField) 
// 
// author:	Matthias Schreiber
// company:	mindfact interaktive medien ag
// date:	13.03.2000
// update:	14.03.2000
//
// Method checkFormData performs a client-side check of form fields containing names.
// Form can be surrounded by <DIV>-section (layer).
// 
// @param form Name of the form where the field to be checked is situated
// @param field Name of the field to be checked
// @param lyr Name of the layer in which the form is embedded. If there's no layer set it to null.
// 
//------------------------------------------------------------------------------------

function checkFormData(form,field,lyr)
{
	var result=true;
	var at_pos, entry, strLength;
	
	if (ie || lyr==null)
		entry = eval('document.' + form +'.'+ field + '.value');
	else if (ns && lyr!=null) 
		entry = eval('document["'+lyr+'"].document.forms["'+form+'"].'+ field +'.value');
	
	strLength=entry.length;
	
	for (i=0; i < strLength; i++) 
	{
		if (charList.indexOf(entry.charAt(i))==-1)
		{
			alert('Unerlaubtes Zeichen in Eingabefeld: '+ entry.charAt(i));
			if (ie || lyr==null)
				eval('document.' + form +'.'+ field + '.focus()');
			else if (ns && lyr!=null) 
				eval('document["'+lyr+'"].document.forms["'+form+'"].'+ field + '.focus()');
			return false;
		}
	}
	return result;
}

//------------------------------------------------------------------------------------
// checkFilePath(formName,srcField,fileSeperators)
// 
// author:	Matthias Schreiber
// company:	mindfact interaktive medien ag
// date:	16.03.2000
// update:	
//
// Method checkFilePath performs a client-side check of form fields containing absolute filepaths.
// 
// 
// @param formName Name of the form where the field to be checked is situated
// @param srcfield Name of the field to be checked
// @param fileSeperators All the file seperators which are used by your OS (for win it's: ':\\')
// 
//------------------------------------------------------------------------------------

function checkFilePath(formName,srcField,fileSeperators)
{
	var at_pos, entry, strLength;
	
	charList = charList + fileSeperators;
	entry = eval('document.' + formName +'.'+ srcField + '.value');
	strLength=entry.length;
	
	for (i=0; i < strLength; i++) 
	{
		if (charList.indexOf(entry.charAt(i))==-1)
		{
			alert('Unerlaubtes Zeichen in Eingabefeld: '+ entry.charAt(i));
			eval('document.' + formName +'.'+ srcField + '.focus()');
			return false;
		}
	}
	return true;
}