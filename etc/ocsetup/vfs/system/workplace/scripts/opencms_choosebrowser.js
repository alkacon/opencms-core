/*
* File   : $Source: /usr/local/cvs/opencms/etc/ocsetup/vfs/system/workplace/templates/js/opencms_choosebrowser.js,v $
* Date   : $Date: 2001/12/14 15:42:27 $
* Version: $Revision: 1.7 $
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


//------------------------------------------------------------------------------------
// content exchange between 2 layers (used in explorer_files_neu_page.html)
// m.stanke 19.04.2000
//------------------------------------------------------------------------------------
var data1=null;
var data2=null;
var data3=null;

function saveLayerData2(from,to,srcLayer,destLayer) {
    
    if (ie) {
        data1 = document.forms[from].NEUNAME.value;
        data2 = document.forms[from].NEUTITEL.value;
        data3 = document.forms[from].TEMPLATE.selectedIndex;
        data4 = document.forms[from].NEUKEYWORDS.value;
        data5 = document.forms[from].NEUDESCRIPTION.value;
        data6 = document.forms[from].DEFAULT_BODY.selectedIndex;
        document.forms[to].NEUNAME.value = data1;
        document.forms[to].NEUTITEL.value = data2;
        document.forms[to].TEMPLATE.selectedIndex = data3;
        document.forms[to].NEUKEYWORDS.value = data4;
        document.forms[to].NEUDESCRIPTION.value = data5;
        document.forms[to].DEFAULT_BODY.selectedIndex = data6;
    }
    else if (ns) {
        data1 = document[srcLayer].document.forms[from].NEUNAME.value;
        data2 = document[srcLayer].document.forms[from].NEUTITEL.value;
        data3 = document[srcLayer].document.forms[from].TEMPLATE.selectedIndex;
        data4 = document[srcLayer].document.forms[from].NEUKEYWORDS.value;
        data5 = document[srcLayer].document.forms[from].NEUDESCRIPTION.value;
        data6 = document[srcLayer].document.forms[from].DEFAULT_BODY.selectedIndex;
        document[destLayer].document.forms[to].NEUNAME.value = data1;
        document[destLayer].document.forms[to].NEUTITEL.value = data2;
        document[destLayer].document.forms[to].TEMPLATE.selectedIndex = data3;
        document[destLayer].document.forms[to].NEUKEYWORDS.value = data4;
        document[destLayer].document.forms[to].NEUDESCRIPTION.value = data5;    
        document[destLayer].document.forms[to].DEFAULT_BODY.selectedIndex = data6;    
    }
}

//------------------------------------------------------------------------------------
// Functions for DHTML error dialogs
// m.schreiber 22.03.2000
//------------------------------------------------------------------------------------

function chInputTxt(formName,field,txt,layerName)
{
    if (ie || layerName==null)
        eval('document.' + formName +'.'+ field + '.value="'+ txt +'";');
    else if (ns && layerName!=null) 
        eval('document["'+layerName+'"].document.forms["'+formName+'"].'+ field + '.value="'+ txt +'";');
}

function toggleLayer(layerName)
{
if (shown) {
    eval('hidelyr("'+ layerName +'")');
    shown=false;
    }
else {
    eval('showlyr("'+ layerName +'")');
    shown=true;
    }
}

function chButtonTxt(formName,field,txt1,txt2,layerName)
{
    if (shown)
        chInputTxt(formName,field,txt2,layerName);
    else
        chInputTxt(formName,field,txt1,layerName);
}

function justifyLayers(lyr1,lyr2) {
    if (ie) {
        eval('y1 = document.all["'+ lyr1 +'"].offsetTop');
        eval('lyrHeight = document.all["'+ lyr1 +'"].offsetHeight');
        y2 = y1 + lyrHeight;
        eval('document.all["'+ lyr2 +'"].style.top = y2-22');
        }
    else if (ns) {
        eval('y1 = document["'+ lyr1 +'"].top');
        eval('lyrHeight = document["'+ lyr1 +'"].clip.height');
        y2 = y1 + lyrHeight; 
        eval('document["'+ lyr2 +'"].top = y2-22');
        }
}

function newObj(layerName,visibility){
    eval('oCont=new makeObj("'+ layerName +'")');
    eval('oCont.css.visibility="'+ visibility +'"');
    eval('centerLayer("'+ layerName +'")');
}

function errorinit() {
    newObj('errormain','hidden');
    newObj('errordetails','hidden');
    justifyLayers('errormain','errordetails');
    showlyr('errormain');
    pageWidth=bw.ns4?innerWidth:document.body.offsetWidth;
    pageHeight=bw.ns4?innerHeight:document.body.offsetHeight;
    window.onresize=resized;
}