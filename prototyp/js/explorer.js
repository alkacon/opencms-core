/*
 * File   : $Source: /alkacon/cvs/opencms/prototyp/js/Attic/explorer.js,v $
 * Date   : $Date: 2000/11/15 08:58:54 $
 * Version: $Revision: 1.2 $
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


/**
 *  contains information about a menuItem
 */
function menuItem(name,link,target,rules){
    this.name = name;
    this.link = link;
    this.target = target;
    this.rules = rules;
}

/**
 *  tree 
 */
function expltree(){
    this.dfsToggleFound = false;
    this.icon = new Array();

    this.nodes = new Array();
    this.oldNodes = null;
    this.root = null;
}

/**
 *  one entry in the filelist 
 */
function file(name, path, title, type, dateolc, whoChanged, date, size, status, project, owner, group, permission, lockedBy){
    this.name = name;
    this.path = path;
    this.title = title;
    this.type = type;
    this.date = dateolc;
    this.whoChanged=whoChanged;
    this.created = date;
    this.size = size;
    this.status = status;
    this.project = project;
    this.owner = owner;
    this.group = group;
    this.permission = permission;
    this.lockedBy = lockedBy;
}

/**
 *  vars index (later vr) contains information on the filelist
 */
function vars_index() {
    this.icons = new Array();
    this.liste = new Array();
    this.lockIcons = new Array();
    this.lockStatus = new Array();
    this.iconPath = new Array();
    this.check_title;
    this.check_date;
    this.check_size;
    this.check_type;
    this.check_perm;
    this.check_group;
    this.check_owner;
    this.check_status;
    this.userName;
    this.shown = false;
    this.lastid=0;
    this.lastLayer=null;
    this.condition=0;
    this.dokument=null;
    this.resource = new Array();
    this.menus = new Array();
    this.checksum;
}

/**
 *  contains information for the users browser
 *  i.e: how to make a layer hidden
 */
function browser_cfg(){
    this.allLayers;
    this.showLayer;
    this.hideLayer;
    this.showKontext;
    this.showKontextEnd;
    this.bodyString;
    this.docu;
    this.distanceLeft;
    this.distanceTop;
	this.xOffset;
	this.yOffset;
}

/**
 *  resources
 */
function res(text,icon){
    this.text = text;
    this.icon = icon;
}

/**
 *  detects the users browser
 */
function whichBrowser(){
    var ns,ie;

    ns = (document.layers)? true:false;
    ie = (document.all)? true:false;
    
    if(ie){
        brcfg.allLayers = 'doc.all.';
        brcfg.showLayer = '.style.visibility="visible"';
        brcfg.hideLayer = '.style.visibility="hidden"';

        brcfg.docu = 'doc.all.';
        brcfg.distanceLeft = '.style.left=';
        brcfg.distanceTop = '.style.top=';

        brcfg.bodyString = "<body onload=top.preloadPics(document) onclick=javascript:top.hideLastone(document);>";
        brcfg.showKontext = "<a onClick=javascript:top.showKontext(document,'";
        brcfg.showKontextEnd = ",window.event.x,window.event.y);>";

    	if (navigator.userAgent.indexOf('MSIE 5')>0){
    		brcfg.xOffset = '+3+doc.body.scrollLeft';
    		brcfg.yOffset = '+3+doc.body.scrollTop';
    	}else{
        	brcfg.xOffset ='+3'; 
        	brcfg.yOffset ='+3'; 
        }
    }
    if(ns){
        brcfg.allLayers = 'doc.layers.';
        brcfg.showLayer = '.visibility="show"';
        brcfg.hideLayer = '.visibility="hide"';

        brcfg.docu = 'doc.';
        brcfg.distanceLeft = '.left=';
        brcfg.distanceTop = '.top=';

    	brcfg.xOffset = '+3';
       	brcfg.yOffset = '+3';
        brcfg.bodyString="<body "+
                "onLoad='captureEvents(Event.CLICK);top.whichdoc(document); "+
                "onClick = top.mouseClickedNs;' "+
                "onResize=javascript:top.resized(document);> ";
        
        brcfg.showKontext = "<a href=javascript:top.showKontext(document,'";
        brcfg.showKontextEnd = ",0,0);>";
    }
}

/**
 *  initializes history array
 */
function initHist(){
    for(i=0;i<10;i++){
        g_history[i]="";
    }
    g_histLoc=0;
}

/**
 *  add new entry
 */
function addHist(what){
var i;
    if(g_history[g_histLoc]!=what){
        if((g_histLoc)>9){
            for(i=0;i<10;i++){
                g_history[i]=g_history[i+1];
            }
            g_histLoc=i;
        }else g_histLoc++;
        
        g_history[g_histLoc]=what;
    }
}

/**
 *  one step back
 */
function histGoBack(){
    if(g_histLoc>0){//g_histLoc=1;
    window.frames[1].frames[1].frames[0].document.forms[0].url.value=g_history[g_histLoc];
    g_histLoc--;
    }
}

/**
 *  display complete history
 */
function dispHist(){
    var i;
    document.writeln("-----------------------------------------------<br>");
    for(i=0;i<10;i++){
        if(i==g_histLoc-1)document.writeln("<b>"+g_history[i]+"</b> !<br>");
            else document.writeln(g_history[i]+"<br>");
    }
    document.writeln("-----------------------------------------------<br>");
}

/**
 *  creates document 'doc'
 *  fills 'text' into document 'doc'
 */
function framefill(doc, text){ /*  fuellt frame 'doc' mit 'text' */
	doc.open();
	doc.write(text);
	doc.close();
}

/**
 *  creates framesets for the explorer-view
 */
function display_ex(){

    var frameStr1 = 
        '<html>' +
        '<frameset border=2 frameborder=yes framespacing=2 cols=20%,80%>' +
        '<frame name=explorer_tree id=explorer_tree src="about:blank">' +
        '<frame name=explorer_content id=explorer_content src="about:blank">' +
        '</frameset>' +
        '</html>'

    var frameStr2 = 
        '<html>' +
        '<frameset border="0" frameborder=no framespacing=0 rows="40,*">' +
        '<frame name=explorer_head id=explorer_head src="about:blank" scrolling="no">' +
        '<frame name=explorer_files id=explorer_files src="about:blank">' +
        '</frameset>' +
        '</html>'

    var frametree = '<html><body><font face="helvetica" size=2>tree</body></html>'
    var framehead= '<html><body><font face="helvetica" size=2>head</body></html>'
    var framebody = '<html><body><font face="helvetica" size=2>body</body></html>'

	explorer = window.body.document;
	framefill(explorer, frameStr1);

    explorer_content = window.body.explorer_content.document;
	framefill(explorer_content, frameStr2);

    explorer_tree = window.body.explorer_tree.document;
	framefill(explorer_tree, frametree);

    explorer_head = window.body.explorer_content.explorer_head.document;
    framefill(explorer_head, framehead);

    window.body.explorer_content.explorer_files.document.location="explorer_files.html";
}

/* explorer_tree / tree functions ------------------------------------------ */

/**
 *  adds an icon for the treeview
 */
function addIcon(w,h,source){
    var a;
    a = tree.icon.length;
    tree.icon[a] = new Image(w,h);
    tree.icon[a].src = source;
}

/**
 *  adds icons for the treeview
 */
function addTreeIcons(){
    addIcon(16 ,16 ,vi.iconPath+"empty.gif");
    addIcon(16 ,16 ,vi.iconPath+"ic_tree_end.gif");
    addIcon(16 ,16 ,vi.iconPath+"ic_tree_folder.gif");
    addIcon(16 ,16 ,vi.iconPath+"ic_tree_folderopen.gif");
    addIcon(16 ,16 ,vi.iconPath+"ic_tree_kreuz.gif");
    addIcon(16 ,16 ,vi.iconPath+"ic_tree_mend.gif");
    addIcon(16 ,16 ,vi.iconPath+"ic_tree_mkreuz.gif");
    addIcon(16 ,16 ,vi.iconPath+"ic_tree_pend.gif");
    addIcon(16 ,16 ,vi.iconPath+"ic_tree_pkreuz.gif");
    addIcon(16 ,16 ,vi.iconPath+"ic_tree_start.gif");
    addIcon(16 ,16 ,vi.iconPath+"ic_tree_vert.gif");
}

/**
 *  adds a child-node
 */
function aC(id, name, parent) {
    var nodeName = '_n'+id;
    var parentName = '_n'+parent;
    var theParent = tree.nodes[parentName];

	tree.nodes[nodeName] = new node(id, name, 0, theParent, false);

    if ((tree.oldNodes != null) && tree.oldNodes[nodeName] != null) {
        tree.nodes[nodeName].open = tree.oldNodes[nodeName].open;
    }
    if(theParent != null) { 
        theParent.childs[theParent.childs.length] = tree.nodes[nodeName]; 
    }
    if (id == 0) { 
        tree.root = tree.nodes[nodeName]; 
    }
}

/**
 *  reset the tree
 */
function rT() {
    tree.oldNodes = tree.nodes;
    tree.nodes = new Array();
}

/**
 *  open a folder via id
 */
function openFolder(id){
	var nodeName = '_n'+id;
    tree.nodes[nodeName].open = 1;
    
    //window.frames[1].frames[1].frames[0].document.forms[0].url.value=window.frames[1].frames[1].frames[0].document.forms[0].url.value+tree.nodes[nodeName].name;
    //TODO: send dirchecksum ( vi.checksum ) and ... to server
	top.window.body.explorer_content.explorer_files.location = "explorer_files.html";
}

/**
 *  show images in tree
 */
function showImages(doc) {
    var i;

	for (i=0; i<doc.images.length; i++){
		number = doc.images[i].name;
		doc.images[i].src = tree.icon[number].src;
	}
}

/**
 *  specifications of a node
 */
function node(id, name, type, parent, open){
    this.id=id;
    this.name=name;
	this.type=type;
	this.parent=parent;
	this.open=open;
	this.childs=new Array();
}

function dfsToggle(id, node) {
	if (id == node.id) {
		node.open = !node.open;
		tree.dfsToggleFound = true;
		return;
	}
	for (var loop1=0; loop1<node.childs.length; loop1++) {
		dfsToggle(id, node.childs[loop1]);
		if (tree.dfsToggleFound) return;
	}
}

function toggleNode(doc, id) {
	tree.dfsToggleFound = false;
	dfsToggle(id, tree.root);
	showTree(doc);
}

/**
 *  write <img> tag
 */
function showPic(doc, pic) {
	doc.write("<img name='" + pic + "' height=16 width=16 border=0 vspace=0 hspace=0 align=left>");
}

/**
 *  write linked <img> tag, used for open-able folders/crosses in the tree frame
 */
function showPicLink(doc, pic, id) {
	doc.write("<a href=javascript:top.toggleNode(document,"+ id +")><img name='"+ pic +"' height=16 width=16 border=0 vspace=0 hspace=0 align=left></a>");
}

function dfsTree(doc, node, depth, last, shape) {
    var loop1;

	if (node.parent==null) {
		showPic(doc, 9); // rootdir
	} else {
		for (loop1=0; loop1<depth-1; loop1++) {
			if (shape[loop1+1] == 1) {
				showPic(doc, 10); //vert.line
			} else {
				showPic(doc, 0); //nothing
			}
		}
		if (last) {
			if (node.childs.length > 0) {
				if (node.open) {
					showPicLink(doc, 5, node.id); //corner to close
				} else {
					showPicLink(doc, 7, node.id); //corner to open 
				}
			} else {
				showPic(doc, 1); //corner
			}
			shape[depth] = 0;
		} else {
			if (node.childs.length > 0) {
				if (node.open) {
					showPicLink(doc, 6, node.id); //cross to close
				} else {
					showPicLink(doc, 8, node.id); //cross to open
				}
			} else {
				showPic(doc, 4); //cross
			}
			shape[depth] = 1;
		}
		if (node.open) {
			showPic(doc, 3); //folderopen
		} else {
			showPic(doc, 2); //foldernormal
		}
	}

	if (node.parent==null) doc.writeln("<a href='javascript:top.openFolder(&quot;"+ node.id +"&quot;);' target='explorer_files' class='treefolder' ;> &nbsp;"+ node.name + "</a></td></tr><tr valign=bottom><td valign=bottom align=left nowrap>");
    	else doc.writeln("<a href='javascript:top.openFolder(&quot;"+ node.id +"&quot;);' target='explorer_files'  class='treefolder' ;> &nbsp;"+ node.name + "</a></td></tr><tr valign=bottom><td valign=bottom align=left nowrap>");

	if (node.open || node == tree.root) {
		for (var loop1=0; loop1<node.childs.length; loop1++) {
			dfsTree(doc, node.childs[loop1], depth+1, (loop1==(node.childs.length-1)), shape);	
		}
	}
}

/**
 *  displays the tree
 */
function showTree(doc) {

    var showTreeHead="<html><head><title>opencms explorer tree</title>"+
            "<style type='text/css'>"+
            "a { text-decoration: none; color: #000000; font-family:arial; font-size:8pt;}"+
            "a:hover { text-decoration: none; color: #FFFFFF; background:#111199 font-family:arial; font-size:8pt;}"+
            "<!"+"--"+
            "body { margin-left:3px; margin-right:0px; margin-top:4px; margin-bottom:0px; margin-height:0px; marginspace=0; margin-top:3px;}"+
            "/"+"/"+"--></style>"+
            "</head><body>"+
	        "<table border=0 cellpadding=0 cellspacing=0>"+
	        "<tr><td valign=bottom align=left nowrap>"+
	        "<font face='arial' size=2>";

    var showTreeFoot="</font>"+
	        "</table>"+
	        "</body></html>";

	doc.open();
    doc.writeln(showTreeHead);
	dfsTree(doc, tree.root, 0, false, new Array());
    doc.writeln(showTreeFoot);
	doc.close();

	showImages(doc);
}

function updateFrame(which, frameurl){
	eval('window.top.'+which+'.location.href="'+frameurl+'"');
}

/**
 *  changes a imageobject on mouseover
 */
function chon(doc, imgID, div){
    var imgEndOn = "_on.gif";
    var imgEndOff = "_off.gif";
    doc.images[imgID].src = vi.iconPath+ imgID + imgEndOn;
}

/**
 *  changes a imageobject on mouseout
 */
function choff(doc, imgID, div){ 
    var imgEndOn = "_on.gif";
    var imgEndOff = "_off.gif";
    doc.images[imgID].src = vi.iconPath + imgID + imgEndOff;
}

/**
 *  moves from /home/tom/ to /home/
 */
function dirUp(){
    var temp;
    var marke=0;
    var directory=window.frames[1].frames[1].frames[0].document.forms[0].url.value;
    var zaehler=0;
    
    addHist(window.frames[1].frames[1].frames[0].document.forms[0].url.value);

    for(i=directory.length;i>=0;i--){

        if(marke==1)window.frames[1].frames[1].frames[0].document.forms[0].url.value=directory.charAt(i)+window.frames[1].frames[1].frames[0].document.forms[0].url.value;

        if(directory.charAt(i)=='/'){
            zaehler++;
            if(zaehler==2){
                marke=1;
                window.frames[1].frames[1].frames[0].document.forms[0].url.value="";
            }
        }
    }
    window.frames[1].frames[1].frames[0].document.forms[0].url.value=window.frames[1].frames[1].frames[0].document.forms[0].url.value+"/";
    if(window.frames[1].frames[1].frames[0].document.forms[0].url.value.length<3)window.frames[1].frames[1].frames[0].document.forms[0].url.value="/";
    
//TODO: LINK TO SERVER
}


/**
 *  displays preloaded pictures in head-frame
 */
function displayHeadPics(doc){
    bt_back = new Image(16,16);
    bt_back.src = vi.iconPath+'bt_back_off.gif';
    
    bt_folder = new Image(32,32);
    bt_folder.src = vi.iconPath+'ic_file_folder.gif';
    
    bt_new = new Image(32,32);
    if(vr.actProject!=vr.onlineProject) bt_new.src = vi.iconPath+'bt_new_in.gif';
    else bt_new.src = vi.iconPath+'bt_new_off.gif';
    
    bt_up = new Image(32,32);
    bt_up.src = vi.iconPath+'bt_up_off.gif';
    
    eval("doc.images[0].src=bt_back.src;");
    eval("doc.images[1].src=bt_up.src;");
    eval("doc.images[2].src=bt_new.src;");
    eval("doc.images[3].src=bt_folder.src;");
}

/**
 *  display "explorer_head" frame
 */
function displayHead(doc){

    var headHead="<html><head><title>opencms</title>"+
            "<style type='text/css'>"+
            "<!"+"--"+
            "body { margin-left:3px; margin-right:0px; margin-top:4px; margin-bottom:0px; margin-height:0px; marginspace=0; margin-top:3px;}"+
            "/"+"/"+"--></style>"+
            "</head><body bgcolor=#c0c0c0 background="+vi.iconPath+"bg_grau.gif>"+
            "<form name=urlform>"+
            "<table cellspacing=0 cellpadding=0 border=0 valign=top>"+
            "<tr valign=center>"+
            "<td class=menu nowrap width=32px>"+
            "<a href=javascript:top.histGoBack(); onmouseover=\"top.chon(document,'bt_back');\" onmouseout=\"top.choff(document,'bt_back');\" >"+
            "<img alt='back' width=32 height=32  border=0 name='bt_back' ></a></td>"+ 
            "<td class=menu nowrap width=32px>"+
            "<a href=javascript:top.dirUp(); onmouseover=\"top.chon(document,'bt_up');\" onmouseout=\"top.choff(document,'bt_up');\">"+
            "<img alt=up width=32 height=32 border=0 name=bt_up ></a></td>";

    var headFoot="<td class=menu width=30px nowrap align=right>&nbsp;</td>"+
            "<td class=menubold nowrap align=right valign=middle><img border=0 id='bt_folder' name='bt_folder' width=16 height=16></td>"+ 
            "<td class=menubold nowrap align=right valign=middle><p class=einzug><font face=arial size=2> adress: </td>"+
            "<td class=menu nowrap align=left valign=middle><p class=einzug>"+
            "<input class=textfeld2 value="+vr.actDirectory+" size=50 maxlength=255 name=url id=url>"+
            "</td></tr></table></form></body></html>";

    doc.open();
    doc.writeln(headHead);

    if(vr.actProject!=vr.onlineProject){
        doc.writeln("<td class=menu nowrap width=32px>");
        doc.writeln("<img alt=new width=32 height=32 border=0 name='bt_new_in'></a></td>"); 
    } else {
        doc.writeln("<td class=menu nowrap width=32px>"+
            "<a href=\"javascript: top.updateFrame('body.explorer_content.explorer_files','explorer_files_new.html');\""+
            "onmouseout=\"top.choff(document, 'bt_new');\" "+
            "onmouseover=\"top.chon(document, 'bt_new');\">");
        doc.writeln("<img alt=new width=32  height=32 border=0 name='bt_new'></a></td>"); 
    }

    doc.writeln(headFoot);
    doc.close();
    displayHeadPics(doc);
}

/**
 *  load pictures into top frame 
 */
function preloadPics(doc){
    var i;

    for(i=0;i<vi.liste.length;i++){
        vi.icons[i] = new Image(16,16);
        vi.icons[i].src = vi.resource[vi.liste[i].type].icon;

        vi.lockIcons[i] = new Image(16,16);
        if(vi.liste[i].lockedBy == ''){
            vi.lockIcons[i].src = vi.iconPath+'empty.gif';
        } else {
            if(vr.userName != vi.liste[i].lockedBy){
                vi.lockIcons[i].src=vi.iconPath+'ic_lock.gif';
            }
            if(vr.userName == vi.liste[i].lockedBy){
                vi.lockIcons[i].src=vi.iconPath+'ic_lockuser.gif';
            }
        }
    }
	showPics(doc)
}

function showPics(doc){
    var i;
	for(i=0; i<vi.icons.length; i++){
		eval("doc.images['res_pic"+i+"'].src=vi.icons["+i+"].src;");
        eval("doc.images['lock_"+i+"'].src=vi.lockIcons["+i+"].src");
	}
}

/**
 *  started, after window is resized (only in netscape)
 */
function resized(doc){
    if(g_isShowing==false){

        g_isShowing = true;
        showPics(doc);
        rT();
        showTree(explorer_tree);

        doc.releaseEvents(Event.CLICK);
        doc.captureEvents(Event.CLICK);
        doc.onClick=top.mouseClickedNs;

        displayHeadPics(explorer_head);

        g_isShowing = false;
    }
}

/* explorer_content / file list functions ---------------------------------- */

/**
 *  set checksum for directory-tree
 *  checksum is sent from server when changing the directory, to verify
 *  if the tree must be updated.
 */
function setChecksum(check){
    vi.checksum=check;
}

/**
 *  set onlineproject of current project
 */
function setOnlineProject(setto){
    vr.onlineProject=setto;
}

/**
 *  sets the project the user is in ...
 */
function setProject(setto){
    initHist();
    vr.actProject=setto;
}

/**
 *  sets the directory the user is in...
 */
function setDirectory(setto){
    vr.actDirectory=setto; 
}

/**
 *  generate a permission string
 */
function permShow(wert,wo){
    var i;
    var a=wert;
    var b=0;
    out = new String()

    for(i=0;i<10;i++){
       if((a & Math.pow(2,i))>0){
            if(i==9)out+=("x");
                else{
                    if(b==0)out+=("r");
                    if(b==1)out+=("v");
                    if(b==2){
                        out+=("w");
                        b=-1;
                    }
                }
            } else out+=("-");
       b++;
    }
    return(out);
}

/**
 *  configuration of columns to displayed 
 */
function showCols(cols){
    var i;
    var check = new Array(8)

    check[0]='vi.check_title';
    check[1]='vi.check_type';
    check[2]='vi.check_size';
    check[3]='vi.check_date';
    check[4]='vi.check_status';
    check[5]='vi.check_owner';
    check[6]='vi.check_group';
    check[7]='vi.check_perm';

    for(i=0;i<8;i++){
        if((cols & Math.pow(2,i))>0)eval(check[i]+"=true;");
           else eval(check[i]+"=false;");
    }
}

/**
 *  add a file to filelist
 */
function aF(name, path, title, type, dateolc, whoChanged, date, size, status, project, owner, group, permission, lockedBy){
	vi.liste[vi.liste.length] = new file( name, path, title, type, dateolc, whoChanged, date, size, status, project, owner, group, permission, lockedBy);
}

/**
 *  creates content of the main-filelist-frame
 *  chooses which icon to use, which lockedBy icon, which color, etc...
 *  creates the context menus for all files
 */
function printList(wo){
    var i;
    var lockedBystring;
    var ssclass;
    var temp="<html><head>"+
            "<style type='text/css'>"+
            "<!"+"--"+
            "h1 { font-size:48pt; color:#FF0000; font-style:italic; } "+
            "td.topic { background:#c0c0c0; font-size:9pt} "+
            "a { text-decoration: none; } "+
            "td{ font-family: arial, helvetica; font-size: 9pt; }; "+

            "td.filechanged{ color: #aa0000; background:#FFFFFF; } "+
            "a.filechanged{  color: #aa0000; font-family: arial, helvetica; font-size: 9pt; } "+
            "a:visited.filechanged{ color: #aa0000; }"+
            "a:hover.filechanged { background:#000088; color:#FFFFFF; text-decoration: none; } "+

            "td.filenew{ color: #0000aa; background:#FFFFFFF; } "+
            "a.filenew{  color: #0000aa; } "+
            "a:visited.filenew{ color: #0000aa; } "+
            "a:hover.filenew{ background:#000088 ; color:#FFFFFF; text-decoration: none; } "+

            "td.filedeleted{ color: #000000; background:#FFFFFF; text-decoration: line-through;} "+
            "a.filedeleted{ color: #000000; font-family: arial, helvetica; font-size: 9pt; text  -decoration: line-through;} "+
            "a:visited.filedeleted{ color: #000000; text-decoration: line-through;} "+
            "a:hover.filedeleted{ background:#000088; color:#FFFFFF; text-decoration: underline; } "+

            "td.filenotinproject{ background:#FFFFFF; color:#bbbbbb;} "+
            "a.filenotinproject{ color: #888888; } "+
            "a:visited.filenotinproject{ color: #555555;} "+
            "a:hover.filenotinproject { background:#000088; color:#FFFFFF; text-decoration: none; } "+

            "td.filenormal{ background:#FFFFFF; color:#000000; } "+
            "a.filenormal{ color: #000000; } "+
            "a:visited.filenormal{ color: #000000; } "+
            "a:hover.filenormal { background:#000088; color:#FFFFFF; text-decoration: none; } "+

            "div.kontextmenu{ position: absolute; top: 0px; left: 0px; width: 150px; text-indent: 2px; background-color: #c0c0c0; visibility: hidden; z-index: 100;} "+
            "table.filekontext{ width: 150px; background-color: #c0c0c0; } "+
            "a.kontextlink { background-color:#c0c0c0; color: black; text-decoration: none;} "+
            "a:hover.kontextlink { color:#FFFFFF; } "+
            "td.inactive{ color:#8c8c8c; } "+
            "/"+"/"+"--></style></head>";
	wo.open();
    wo.writeln(temp);
    wo.writeln(brcfg.bodyString);

    wo.writeln("<table cellpadding=1 cellspacing=1 border=0><tr>");

    wo.writeln("<td class=topic width=20>&nbsp;</td>");
    wo.writeln("<td class=topic width=20>&nbsp;</td>");
    wo.writeln("<td class=topic width=120>"+vr.descr[0]+"</td>");

    if(vi.check_title)wo.writeln("<td class=topic width=80>"+vr.descr[1]+"</td>");
    if(vi.check_type)wo.writeln("<td class=topic width=100>"+vr.descr[2]+"</td>");
    if(vi.check_date)wo.writeln("<td class=topic width=75>"+vr.descr[3]+"</td>");
    if(vi.check_size)wo.writeln("<td class=topic width=60>"+vr.descr[4]+"</td>");
    if(vi.check_status)wo.writeln("<td class=topic width=60>"+vr.descr[5]+"</td>");
    if(vi.check_owner)wo.writeln("<td class=topic width=85>"+vr.descr[6]+"</td>");
    if(vi.check_group)wo.writeln("<td class=topic width=85>"+vr.descr[7]+"</td>");
    if(vi.check_perm)wo.writeln("<td class=topic width=45>"+vr.descr[8]+"</td>");
    wo.writeln("</tr>");

    for(var i=0; i<vi.liste.length; i++){

        if(vi.liste[i].project!=vr.actProject){
             ssclass="filenotinproject";
        } else {
            ssclass="filenormal";
            if(vi.liste[i].status==1)ssclass="filechanged";
            if(vi.liste[i].status==2)ssclass="filenew";
            if(vi.liste[i].status==3)ssclass="filedeleted";
        }

        wo.writeln("<tr>");

        wo.writeln("<td align=center>");

        wo.writeln(brcfg.showKontext + i + "'," + i + brcfg.showKontextEnd);

        wo.write("<img name='res_pic"+i+"' border=0 width=16 height=16></a>");
        wo.writeln("</td>");

        if(vi.liste[i].lockedBy!=""){
            lockedBystring="alt=\""+vr.lockedBy+" "+vi.liste[i].lockedBy+"\"";
        } else {
            lockedBystring="";
        }

        wo.write("<td align=center><img name='lock_"+i+"' "+lockedBystring+" border=0 width=16 height=16></a></td>");

        wo.writeln("<td class="+ssclass+"><a href='#' class="+ssclass+">"+vi.liste[i].name+"</a></td>");
        if(vi.check_title)wo.writeln("<td class="+ssclass+">"+vi.liste[i].title+"&nbsp;</td>");
        if(vi.check_type)wo.writeln("<td class="+ssclass+">"+vi.resource[vi.liste[i].type].text+"</td>");
        if(vi.check_date)wo.writeln("<td class="+ssclass+">"+vi.liste[i].date+"</td>");
        if(vi.check_size)wo.writeln("<td class="+ssclass+">"+vi.liste[i].size+"</td>");
        if(vi.check_status)wo.writeln("<td class="+ssclass+">"+vr.stati[vi.liste[i].status]+"</td>");
        if(vi.check_owner)wo.writeln("<td class="+ssclass+">"+vi.liste[i].owner+"</td>");
        if(vi.check_group)wo.writeln("<td class="+ssclass+">"+vi.liste[i].group+"</td>");

        if(vi.check_perm){
            wo.write("<td class="+ssclass+">"+permShow(vi.liste[i].permission,wo)+"</td>");
        }
        wo.writeln("</td></tr>");
    }
    wo.writeln("</tr></table>");

    for(i=0;i<vi.liste.length;i++){
        wo.writeln("<div id='men"+i+"' class='kontextmenu'>");
        wo.writeln("<table CELLPADDING=1 CELLSPACING=0 BORDER=0 bgcolor=#777777><tr><td>");
        wo.writeln("<table width=150 CELLPADDING=1 CELLSPACING=0 BORDER=0 class=filekontext>");

        for(a=0;a<vi.menus[vi.liste[i].type].items.length;a++){

            /* 0:unchanged",1:changed",2:new",3:deleted" */

            if(vr.actProject==1){ /* online project? */
                if(vi.menus[vi.liste[i].type].items[a].rules.charAt(0)=='0'){
                    wo.writeln("");
                }
                if(vi.menus[vi.liste[i].type].items[a].rules.charAt(0)=='1'){
                    wo.writeln("<TR><TD class=inactive>"+vi.menus[vi.liste[i].type].items[a].name+"</TD></TR>");
                }
                if(vi.menus[vi.liste[i].type].items[a].rules.charAt(0)=='2'){
                    wo.writeln("<TR><TD><A class=kontextlink href='"+vi.menus[vi.liste[i].type].items[a].link+"'>"+vi.menus[vi.liste[i].type].items[a].name+"</a></td></tr>");
                }
            }else{
                /* if not locked */
                if(vi.liste[i].lockedBy == ''){
                    display = vi.menus[vi.liste[i].type].items[a].rules.charAt(vi.liste[i].status+1);
                }else{
                    /* if locked by someone else */
                    if(vi.liste[i].lockedBy != vr.userName)display=vi.menus[vi.liste[i].type].items[a].rules.charAt(vi.liste[i].status+9);
                    /* if locked by me*/
                    if(vi.liste[i].lockedBy == vr.userName)display=vi.menus[vi.liste[i].type].items[a].rules.charAt(vi.liste[i].status+5);
                }
                if(display == 0){
                    wo.writeln("");
                }
                if(display == 1){
                    wo.writeln("<TR><TD class=inactive>"+vi.menus[vi.liste[i].type].items[a].name+"</TD></TR>");
                }
                if(display == 2){
                    wo.writeln("<TR><TD><A class=kontextlink href='"+vi.menus[vi.liste[i].type].items[a].link+"'>"+vi.menus[vi.liste[i].type].items[a].name+"</a></td></tr>");
                }
            }
        }
        wo.writeln("</table></td></tr></table></div>");
    }
    wo.writeln("</body></html>");
    wo.close();
}

/**
 *  do an update (filelist display)
 */
function dU(doc){
    vi.locklength=0;
    showCols(vr.viewcfg);
    printList(doc);
    showTree(explorer_tree);
    displayHead(explorer_head);
}

/**
 *  reset data (filelist)
 */
function rD(){
    initHist();
    vi.liste = new Array();
    vi.icons = new Array();
}

/* kontextmenu functions --------------------------------------------------- */

/**
 *  called, if mousebutton is clicked (only netscape)
 */
var nsx=0,nsy=0;
function mouseClickedNs(doc){
    nsx=doc.x;
    nsy=doc.y;

    //hideMenu(vi.dokument,vi.letztelyr);
    hideLastone(vi.dokument);
}

/**
 *  set vi.dokument to current document
 */
function whichdoc(doc){
    vi.dokument=doc;
    preloadPics(doc);
}

/**
 *  hides last layer (last context menu)
 */
function hideLastone(doc){
    if(vi.condition==0){
        hideMenu(doc, vi.lastLayer);
        vi.condition=0;
    }
    if(vi.condition==1)vi.condition=0;
}

/**
 *  displays a contextmenu
 */
function showKontext(doc, welche, id,x,y){

    vi.condition=1; //ie

    if(x==0){
        x=nsx;
        y=nsy;
    }

    if (!vi.shown || id!=vi.oldId){
        if(y >= (screen.availHeight/2)){
        	eval(brcfg.docu+"men"+welche+brcfg.distanceTop+'y'+brcfg.yOffset);
       	}
		eval(brcfg.docu+"men"+welche+brcfg.distanceTop+'y'+brcfg.yOffset);
        eval(brcfg.docu+"men"+welche+brcfg.distanceLeft+'x'+brcfg.xOffset);

		hideMenu(doc, vi.lastLayer);
		eval(brcfg.allLayers+"men"+welche+brcfg.showLayer);
		vi.shown = true;
   	} else {
		hideMenu(doc, vi.lastLayer);
		vi.shown = false;
   	}
	vi.lastLayer=welche;
	vi.oldId=id;
    vi.dokument=doc;
}

/**
 *  hides the context (layer)
 */
function hideMenu(doc, welche){
	if(welche!=null){
		eval(brcfg.allLayers+"men"+welche+brcfg.hideLayer);
		vi.shown=false;
	}
	else return;
}

/**
 *  contextmenu
 */
function menu(nr) {
    this.nr = nr;
    this.items = new Array();
}

/**
 *  add a menuentry / a menuItem
 */
function addMenuEntry(nr,text,link,target,rules){ 
    if(!vi.menus[nr])vi.menus[nr] = new menu(vi.menus.length);
    vi.menus[nr].items[vi.menus[nr].items.length] = new menuItem(text,link,target,rules);
}