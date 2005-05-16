/*
* File   : $Source: /usr/local/cvs/opencms/etc/ocsetup/vfs/system/workplace/templates/js/opencms_dialogscroll.js,v $
* Date   : $Date: 2001/07/31 15:50:12 $
* Version: $Revision: 1.3 $
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
 
// ===============================================
// JAVASCRIPT-FUNKTIONEN OPENCMS
//
// Scrolling layer for use within dialog boxes
//
// author:          Matthias Schreiber
// company:         mindfact interaktive medien ag
// date:            20.03.2000
// update:      
// update author:   
// ===============================================


/********************************************************************************
Copyright (C) 1999 Thomas Brattli
This script is made by and copyrighted to Thomas Brattli at www.bratta.com
Visit for more great scripts. This may be used freely as long as this msg is intact!
I will also appriciate any links you could give me.
********************************************************************************/
//Default browsercheck, added to all scripts!
function checkBrowser(){
    this.ver=navigator.appVersion
    this.dom=document.getElementById?1:0
    this.ie5=(this.ver.indexOf("MSIE 5")>-1 && this.dom)?1:0;
    this.ie4=(document.all && !this.dom)?1:0;
    this.ns5=(this.dom && parseInt(this.ver) >= 5) ?1:0;
    this.ns4=(document.layers && !this.dom)?1:0;
    this.bw=(this.ie5 || this.ie4 || this.ns4 || this.ns5)
    return this
}
bw=new checkBrowser();

/*****************

You set the width and height of the divs inside the style tag, you only have to
change the divCont.
!!! Remeber to set the clip the same as the width and height.
You can remove the divUp and divDown layers if you want. 
This script should also work if you make the divCont position:relative.
Then you should be able to place this inside a table or something. Just remember
that Netscape crash very easily with relative positioned divs and tables.

Updated with a fix for error if moving over layer before pageload.

****************/

var loaded;

//If you want it to move faster you can set this lower:
var speed=25;

//Sets variables to keep track of what's happening
var loop, timer, pageWidth, pageHeight;

function getWindowWidth() {
    if( ns==true ) {
        windowWidth = innerWidth;
    }
    else if( ie==true ) {
        windowWidth = document.body.clientWidth;
    }
    return windowWidth;
}

//Object constructor
function makeObj(obj,nest){
    nest=(!nest) ? '':'document.'+nest+'.';
    this.el=bw.dom?document.getElementById(obj):bw.ie4?document.all[obj]:bw.ns4?eval(nest+'document.'+obj):0;
    this.css=bw.dom?document.getElementById(obj).style:bw.ie4?document.all[obj].style:bw.ns4?eval(nest+'document.'+obj):0;
    this.scrollHeight=bw.ns4?this.css.document.height:this.el.offsetHeight; 
    this.clipHeight=bw.ns4?this.css.clip.height:this.el.offsetHeight;
    this.up=goUp;this.down=goDown;
    this.moveIt=moveIt; this.x; this.y;
    this.obj = obj + "Object";
    eval(this.obj + "=this");
    return this;
}
function moveIt(x,y){
    this.x=x;this.y=y;
    this.css.left=this.x;
    this.css.top=this.y;
}

//Makes the object go up
function goDown(move){
    if(this.y>-this.scrollHeight+oCont.clipHeight){
        this.moveIt(0,this.y-move);
        if(loop) setTimeout(this.obj+".down("+move+")",speed);
    }
}
//Makes the object go down
function goUp(move){
    if(this.y<0){
        this.moveIt(0,this.y-move);
        if(loop) setTimeout(this.obj+".up("+move+")",speed);
    }
}

//Calls the scrolling functions. Also checks whether the page is loaded or not.
function scroll(speed){
    if(loaded){
        loop=true;
        if(speed>0) return oScroll.down(speed)
        else oScroll.up(speed);
    }
}

//Stops the scrolling (called on mouseout)
function noScroll(){
    loop=false;
    if(timer) clearTimeout(timer);
}

//Forces browser to reload page if window was resized
function resized(){
        pageWidth2=bw.ns4?innerWidth:document.body.offsetWidth;
        pageHeight2=bw.ns4?innerHeight:document.body.offsetHeight;
        if(pageWidth!=pageWidth2 || pageHeight!=pageHeight2) location.reload();
}

//---------------------------------------------
// move(pos)
// Makes the scrolling layer jump to its start or end
// (depends on parameter value)
// 
// author: Matthias Schreiber
// date:   20.03.2000
// update: 22.03.2000
//
// @param pos: Position to jump to (permitted values: 'start' or 'end') 
// 
//---------------------------------------------

function move(pos) {
    if ((pos=='start')&&(oScroll.y<0))
        oScroll.moveIt(0,0);
    else if ((pos=='end')&&(oScroll.y>-oScroll.scrollHeight+oCont.clipHeight)) {
        oScroll.moveIt(0,-oScroll.scrollHeight+oCont.clipHeight);
    }
}

//---------------------------------------------
// centerLayer(layerName)
// Method performs horizontal adjustment of one
// specified layer. The alignment is "center".
//
// author: Matthias Schreiber
// date:   20.03.2000
// update: 22.03.2000
//
// @param layerName: ID of the layer to be centered
// 
//---------------------------------------------

function centerLayer(layerName){
    totalWidth=getWindowWidth();
    if (ie) {
        eval('lyrWidth = document.all["'+ layerName +'"].offsetWidth');
        eval('document.all["'+ layerName +'"].style.left=Math.round((totalWidth-lyrWidth)/2)');
    }
    else if (ns) {
        eval('lyrWidth = document["'+ layerName +'"].clip.width');
        eval('document["'+ layerName +'"].left=Math.round((totalWidth-lyrWidth)/2)');
    }
}

//Makes the object
function scrollInit(staticContainer,scrollContainer,scrollContent){
    eval('oCont=new makeObj("'+ scrollContainer +'")');
    eval('oScroll=new makeObj("'+ scrollContent +'","'+ scrollContainer +'")');
    oScroll.moveIt(0,0);
    oCont.css.visibility='visible';
    loaded=true;
    eval('centerLayer("'+ staticContainer +'")');
    eval('centerLayer("'+ scrollContainer +'")');
    pageWidth=bw.ns4?innerWidth:document.body.offsetWidth;
    pageHeight=bw.ns4?innerHeight:document.body.offsetHeight;
    window.onresize=resized;
}
