/*
* File   : $Source: /usr/local/cvs/opencms/etc/ocsetup/vfs/system/workplace/templates/js/opencms.js,v $
* Date   : $Date: 2001/07/31 15:50:12 $
* Version: $Revision: 1.22 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
 
 //------------------------------------------
// global variables und functions
// m.schleich 03.01.2000
//------------------------------------------

var admincontenturl="administration_content_top.html";

var selectedTask=0;
var lastVisited="tasks_content_nafm.html";
var treewin=null;

// List of valid characters for INPUT fields containing names:
var charList="0123456789-._~abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

// perform a simple browser check
ns = (document.layers)? true:false;
ie = (document.all)? true:false;

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
var imgEndInaktiv = "_in.gif";
//var pfad="pics/";


function ChOn(imgID,div) //changes a imageobject on mouseover
{
if (ns && div)
    document[div].document[imgID].src = pfad + imgID + imgEndOn;
else
    document.images[imgID].src = pfad + imgID + imgEndOn;
}

function ChOff(imgID,div) //changes a imageobject on mouseout
{
if (ns && div)
    document[div].document[imgID].src = pfad + imgID + imgEndOff;
else
    document.images[imgID].src = pfad + imgID + imgEndOff;
}

function ChPush(imgID,div) //changes a imageobject on clicked or mousedown
{
if (ns && div)
    document[div].document[imgID].src = pfad + imgID + imgEndPush;
else
    document.images[imgID].src = pfad + imgID + imgEndPush;
}

function ChIn(imgID,div) //changes a imageobject on clicked or mousedown
{
if (ns && div)
    document[div].document[imgID].src = pfad + imgID + imgEndInaktiv;
else
    document.images[imgID].src = pfad + imgID + imgEndInaktiv;
}


function ChImg(imgID,src)
{
document.images[imgID].src = src;
}

//------------------------------------------------------------------------------------
// simple encoding of a given string.
// Any "/" character will be replaced by "%2F". This can be used for avoiding slashes 
// in request parameters since some servlet environments have problems with such URLs.
// a.lucas    13.04.2000
//------------------------------------------------------------------------------------
function simpleEscape(text) {
    return text.replace(/\//g, "%2F");
}

//------------------------------------------------------------------------------------
// simple encoding of a given URL.
// Any "/" character in the parameters of the given URL will be replaced by "%2F".
// a.lucas    18.04.2000
//------------------------------------------------------------------------------------
function encodeUrl(url) {
    encodedurl = url
    asteriskIdx = url.indexOf("?");
    if(asteriskIdx > -1 && asteriskIdx < (url.length-1)) {
        encodedurl = url.substring(0, asteriskIdx) + "?" + simpleEscape(url.substring(asteriskIdx + 1));
    }
    return encodedurl;
}

//------------------------------------------------------------------------------------
// updates a frame
// Any URL parameters following the requested path will be simple encoded.
// m.schleich 01.02.2000
// a.lucas    13.04.2000
//------------------------------------------------------------------------------------
function updateFrame(which, frameurl)
{   
    encodedurl = encodeUrl(frameurl);
    eval('window.top.'+which+'.location.href="'+encodedurl+'"');
}

//------------------------------------------------------------------------------------
// updates a variable
// m.schleich 07.02.2000
//------------------------------------------------------------------------------------
function updateVarible(whichframe, whichvar, value)
{
    eval('window.top.'+whichframe+'.'+whichvar+'='+value);

}

//------------------------------------------------------------------------------------
// open new window
// m.schleich 20.12.1999
//------------------------------------------------------------------------------------

var workplace;

function openwin(url, name, w, h) //opens a new window with parameter URL, Windowname (free choosable), width and height
{
    if (url != '#') {
        if(w==0) w=screen.availWidth-20;
        if(h==0) h=screen.availHeight-100;
        encodedurl = encodeUrl(url);

        workplace = window.open(encodedurl,name, 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,width='+w+',height='+h);
        workplace.moveTo(0,0);
        if(workplace != null) {
              if (workplace.opener == null)
              {
                 workplace.opener = self;
              }
              workplace.focus();
        }
        
     }
    return workplace;
}

function openwinfull(url, name, w, h) //opens a new window with parameter URL, Windowname (free choosable), width and height
{
    if (url != '#') {
        if(w==0) w=screen.availWidth-20;
        if(h==0) h=screen.availHeight-100;

        encodedurl = encodeUrl(url);
        workplace = window.open(encodedurl,name, 'toolbar=yes,location=yes,directories=no,status=yes,menubar=1,scrollbars=yes,resizable=yes,width='+w+',height='+h);
        if(workplace != null) {
            if (workplace.opener == null){
                workplace.opener = self;
            }
            workplace.moveTo(0,0);
            workplace.focus();
        }
    }
    
}

//------------------------------------------------------------------------------------
// open new window
// m.schleich 26.01.2000
// m.schreiber 20.03.2000
//------------------------------------------------------------------------------------

var smallwindow;

function opensmallwin(url, name, w, h) //opens a new window with parameter URL, Windowname (free choosable), width and height
{
    encodedurl = encodeUrl(url);
    smallwindow = window.open(encodedurl, name, 'toolbar=no,location=no,directories=no,status=no,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width='+w+',height='+h);
    if(smallwindow != null)
       {
          if (smallwindow.opener == null)
          {
             smallwindow.opener = self;
          }
        }
    //smallwindow.focus();
    return smallwindow;
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
// author:  Michaela Schleich
// company: mindfact interaktive medien ag
// date:    01.11.1999
// update:  14.01.2000
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
    else
    {
        if(eMonat<aktMonat)
        {
            alert("Die Fälligkeit liegt in der Vergangenheit");
            return false;
        }
        else
        {
            if(eTag<aktTag)
            {
                alert("Die Fälligkeit liegt in der Vergangenheit");
                return false;
            }
        }
    }
    
    
    if(eDat.length != 10 || isNaN(eTag) || isNaN(eMonat) || isNaN(eJahr))
        {
        alert("Bitte das Datum in Form von tt.mm.jjjj eingeben.");
        return false;
        }
    else
        {
        if(eMonat < 01 || eMonat > 12)
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
                if(((eTag == 31) && (eMonat == 02)) || ((eTag == 31) && (eMonat == 04)) || ((eTag == 31) && (eMonat == 06)))
                {
                alert("Bitte ueberpruefen Sie den Tag des Datums.");
                return false;
                }
                if((eTag == 30 && eMonat == 02) || (eTag == 31 && eMonat==9) || (eTag==31 && eMonat==11))
                {
                alert("Bitte ueberpruefen Sie den Tag des Datums.");
                return false;
                }
                else
                {
                if(eTag==29 && eMonat == 02)
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
// notImplemented
// 
// author:  Andreas Schouten
// company: mindfact interaktive medien ag
// date:    01.03.2000
//
// functionname: The name of the functionality, that is not implemented.
// displays a "not implemented" message in the workplace
//------------------------------------------------------------------------------------

function notImplemented(functionname)
{
    message = "Die Funktionalität '" + functionname + "' ist derzeit noch nicht implementiert.\n\n";
    //message = message + "Der OpenCms-Kern bietet diese Funktionalität bereits an. Die Umsetzung auf der Workplace wird in Kürze erfolgen.";
    
    // display the message
    alert(message);
}

