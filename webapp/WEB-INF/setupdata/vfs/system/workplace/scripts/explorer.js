/*
 * File   : $Source: /usr/local/cvs/opencms/etc/ocsetup/vfs/system/workplace/templates/js/explorer.js,v $
 * Date   : $Date: 2002/08/23 08:56:50 $
 * Version: $Revision: 1.53 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2001 - 2003  The OpenCms Group
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

// contains information about the current help page [must be revised for new online help].
var help_url="ExplorerAnsicht/index.html";
// The flaturl to use for changing folders in filelist-only mode.
var flaturl="";
// If we are in project-view mode this is set to true
var projectView = false;

var openfolderMethod="openthisfolder";
var showKon=true;

// the empty UUID
var nullUUID = "00000000-0000-0000-0000-000000000000";

 /**
  *  returns the actual help page
  */
 function show_help(){
    return help_url;
 }

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
 function file(name, path, title, type, dateolc, whoChanged, date, size, status, project, owner, group, permission, lockedBy, lockedInProjectName, lockedInProjectId){
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
     this.lockedInProjectName = lockedInProjectName;
     this.lockedInProjectId = lockedInProjectId;
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
     this.newButtonActive;
     this.check_name;
     this.check_title;
     this.check_date;
     this.check_size;
     this.check_type;
     this.check_perm;
     this.check_group;
     this.check_owner;
     this.check_status;
     this.check_lockedBy;
     this.userName;
     this.shown = false;
     this.lastid = nullUUID;
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
 function res(text,nicename,icon,createLink,uploadable){
     this.text = text;
     this.nicename = nicename;
     this.icon = icon;
     this.createLink = createLink;
     this.uploadable = uploadable;
 }

var windowed=0;
var ns,ie,gecko;

 /**
  *  detects the users browser
  */
    function whichBrowser(){

        ns = (document.layers)? true:false;
        ie = (document.all)? true:false;
        gecko = (navigator.userAgent.toLowerCase().indexOf('gecko') != -1);

     if(ie){
         brcfg.allLayers = 'doc.all.';
         brcfg.showLayer = '.style.visibility="visible"';
         brcfg.hideLayer = '.style.visibility="hidden"';

         brcfg.docu = 'doc.all.';
         brcfg.distanceLeft = '.style.left=';
         brcfg.distanceTop = '.style.top=';

         brcfg.showKontext = "<a style=\"cursor:hand;\" onClick=\"javascript:top.showKontext(document,'";
         brcfg.showKontextEnd = ",window.event.x,window.event.y);\">";

        if ((navigator.userAgent.indexOf('MSIE 5')>0) || (navigator.userAgent.indexOf('MSIE 6')>0)) {
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

         brcfg.showKontext = "<a href=javascript:top.showKontext(document,'";
 brcfg.showKontextEnd = ",0,0);>";
     }


    if(gecko){
        brcfg.docu = 'doc.all.';
        brcfg.distanceLeft = '.style.left=';
        brcfg.distanceTop = '.style.top=';

         brcfg.showKontext = "<a onClick=javascript:top.setpos(event.pageX,event.pageY,document,'";
         brcfg.showKontextEnd = ",0,0);>";


        brcfg.xOffset = '+3+doc.body.scrollLeft';
        brcfg.yOffset = '+3+doc.body.scrollTop';
    }



 }

 function simpleEscape(text) {
    return text.replace(/\//g, "%2F");
 }

 function openwinfull(url)
 {
    if (url != '#') {
        w=screen.availWidth-50;
        h=screen.availHeight-200;
        workplace = window.open("explorerShowResource.html?url=" + url,'preview', 'toolbar=yes,location=yes,directories=no,status=yes,menubar=1,scrollbars=yes,resizable=yes,width='+w+',height='+h);
        if(workplace != null) {
            workplace.focus();
        }

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
    openurl();
 }
 
 /**
  *  display search form
  */
 function dispSearchForm(){
	top.window.frames[1].frames[0].document.location="ocms_searchform.html";
	top.window.frames[1].frames[1].document.location="explorer_files.html?mode=projectview&submode=search";
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
     explorer_tree = window.body.explorer_tree.document;
     if(window.body.explorer_content.explorer_head) {
        explorer_head = window.body.explorer_content.explorer_head.document;
     }
whichBrowser();

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
 function aC(id, name, parent, isGrey) {
     var nodeName = '_n'+id;
     var parentName = '_n'+parent;
     var theParent = tree.nodes[parentName];

    tree.nodes[nodeName] = new node(id, name, nullUUID, theParent, false, isGrey);

     if ((tree.oldNodes != null) && tree.oldNodes[nodeName] != null) {
         tree.nodes[nodeName].open = tree.oldNodes[nodeName].open;
     }
     if(theParent != null) {
         theParent.childs[theParent.childs.length] = tree.nodes[nodeName];
     }
     if (theParent == null) {
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
     var pfad="";
     addHist(window.frames[1].frames[1].frames[0].document.forms[0].url.value);
     window.frames[1].frames[1].frames[0].document.forms[0].url.value="";
    if (id!=tree.root.id){
        do{
            var nodeName='_n'+id;
            pfad="/"+tree.nodes[nodeName].name;
            top.window.frames[1].frames[1].frames[0].document.forms[0].url.value = pfad +top.window.frames[1].frames[1].frames[0].document.forms[0].url.value ;
            id   = tree.nodes[nodeName].parent.id;
            test = tree.nodes[nodeName].parent.id;

        }while(id!=tree.root.id);
    }
     top.window.frames[1].frames[1].frames[0].document.forms.urlform.url.value +='/';
     openurl();
 }

 /**
  *  open a folder via id
  */
 function folderOpen(id){

         while(id!=tree.root.id){
         var nodeName='_n'+id;
        if(tree.nodes[nodeName]){
                if(tree.nodes[nodeName].childs.length>0)tree.nodes[nodeName].open=true;

                id = tree.nodes[nodeName].parent.id;
            }else return;
        }

 }

function openurl(){
	top.window.frames[1].frames[1].frames[1].document.open();
	top.window.frames[1].frames[1].frames[1].document.writeln("<html>");
	top.window.frames[1].frames[1].frames[1].document.writeln("<HEAD><META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=" + top.frames.head.encoding + "\"></HEAD>");
	top.window.frames[1].frames[1].frames[1].document.writeln("<body><center><br><br><br><br><font face=Helvetica size=2>"+vr.langloading+"</center></body></html>");
	top.window.frames[1].frames[1].frames[1].document.close();
    folder=top.window.frames[1].frames[1].frames[0].document.forms.urlform.url.value;
    selectedpage="";
    if(top.window.frames[1].frames[1].frames[0].document.forms.urlform.pageSelect){
    	selectedpage=top.window.frames[1].frames[1].frames[0].document.forms.urlform.pageSelect.value;
    }
    top.window.frames[1].frames[1].frames[1].document.location="explorer_files.html?folder="+folder+"&check="+vi.checksum+"&selPage="+selectedpage;

}


 /**
  *  specifications of a node
  */
 function node(id, name, type, parent, open, isGrey){
    this.id=id;
    this.name=name;
    this.type=type;
    this.parent=parent;
    this.open=open;
    this.childs=new Array();
    this.isGrey=isGrey;
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

function toggleNode(doc, id, windowed) {

    tree.dfsToggleFound = false;
    dfsToggle(id, tree.root);
    showTree(doc,windowed);
}

/**
 *  write <img> tag
 */
function showPic(doc, pic) {
    doc.write("<img src='" + pic + "' height=16 width=16 border=0 vspace=0 hspace=0 align=left>");
}

/**
 *  write linked <img> tag, used for open-able folders/crosses in the tree frame
 */
function showPicLink(doc, pic, id,windowed) {
    if(windowed==0) doc.write("<a href=javascript:top.toggleNode(document,'"+ id +"',"+windowed+")><img src='"+ pic +"' height=16 width=16 border=0 vspace=0 hspace=0 align=left></a>");
    if(windowed>0) doc.write("<a href=javascript:window.opener.toggleNode(document,'"+ id +"',"+windowed+")><img src='"+ pic +"' height=16 width=16 border=0 vspace=0 hspace=0 align=left></a>");
}


function dfsTree(doc, node, depth, last, shape,windowed) {
    var loop1;

    if (node.parent==null) {
        showPic(doc, tree.icon[9].src); // rootdir
    } else {
        for (loop1=0; loop1<depth-1; loop1++) {
            if (shape[loop1+1] == 1) {
                showPic(doc, tree.icon[10].src); //vert.line
            } else {
                showPic(doc, tree.icon[0].src); //nothing
            }
        }

        if (last) {
            if (node.childs.length > 0) {
                if (node.open) {
                    //wenn actdir ein child vom knopf ist
                    showPicLink(doc, tree.icon[5].src, node.id,windowed); //corner to close
                } else {
                    showPicLink(doc, tree.icon[7].src, node.id,windowed); //corner to open
                }
            } else {
                showPic(doc, tree.icon[1].src); //corner
            }
            shape[depth] = 0;
        } else {
            if (node.childs.length > 0) {
                if (node.open) {
                    showPicLink(doc, tree.icon[6].src, node.id,windowed); //cross to close
                } else {
                    showPicLink(doc, tree.icon[8].src, node.id,windowed); //cross to open
                }
            } else {
                showPic(doc, tree.icon[4].src); //cross
            }
            shape[depth] = 1;
        }
        if(node.id==vr.actDirId){
            showPic(doc, tree.icon[3].src); //folderopen
        } else {
            showPic(doc, tree.icon[2].src); //foldernormal
        }
    }
    
     if(node.isGrey)var foco="class=ig ";
         else var foco="class=tf ";
     if(windowed==0) doc.writeln("<a "+foco+"href='javascript:top.openFolder(&quot;"+ node.id +"&quot;);' target='explorer_files' ;>&nbsp;"+ node.name + "&nbsp;</a></td></tr><tr valign=bottom><td valign=bottom align=left nowrap>");
     if(windowed==1) doc.writeln("<a "+foco+" href=javascript:window.opener.addProjectDir('"+node.id+"');> &nbsp;"+ node.name + "&nbsp;</a></td></tr><tr valign=bottom><td valign=bottom align=left nowrap>");
     if(windowed==2) doc.writeln("<a "+foco+" href=javascript:window.opener.addDir('"+node.id+"');> &nbsp;"+ node.name + "&nbsp;</a></td></tr><tr valign=bottom><td valign=bottom align=left nowrap>");

    if (node.open || node == tree.root) {
        for (var loop1=0; loop1<node.childs.length; loop1++) {
            dfsTree(doc, node.childs[loop1], depth+1, (loop1==(node.childs.length-1)), shape,windowed);
        }
    }
 }

function addDir(nodid){
    var pfad="";
    if (nodid!=tree.root.id){
        do{
            var nodeName='_n'+nodid;
            pfad=tree.nodes[nodeName].name+"/"+pfad;
            nodid   = tree.nodes[nodeName].parent.id;
            test = tree.nodes[nodeName].parent.id;
        } while(nodid!=tree.root.id);
    }
    pfad='/'+pfad;
    if(window.frames[1].frames[1].frames[1]) {
    	window.frames[1].frames[1].frames[1].document.forms[0].folder.value=pfad;
    } else {
    	window.frames[1].frames[1].document.forms[0].folder.value=pfad;
    }

}

function addProjectDir(nodid){
    var pfad="";
    if (nodid!=tree.root.id){
        do{
            var nodeName='_n'+nodid;
            pfad=tree.nodes[nodeName].name+"/"+pfad;
            nodid   = tree.nodes[nodeName].parent.id;
            test = tree.nodes[nodeName].parent.id;
        } while(nodid!=tree.root.id);
    }
    pfad='/'+pfad;
    window.frames[1].frames[1].document.forms[0].tempFolder.value=pfad;
    if (window.frames[1].frames[1].copySelection) {
        window.frames[1].frames[1].copySelection();
    }
}


function opensmallwin(url, name, w, h) //opens a new window with parameter URL, Windowname (free choosable), width and height
{
    smallwindow = window.open(url, name, 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=150,left=660,width='+w+',height='+h);
    if(smallwindow != null)
       {
          if (smallwindow.opener == null)
          {
             smallwindow.opener = self;
          }
        }
    smallwindow.focus();
    return smallwindow;
}


function showWinCopyTree(doc){
    showTree(doc,2);
}

function showWinTree(doc){
    showTree(doc,1);
}

var treewindowexists=false;

function openTreeCopyWin(){
    treewin=opensmallwin('treewindowcopy.html', 'opencms', 170, 300);
    treewindowexists=true;
}

function openTreeWin(){
    treewin=opensmallwin('../../../action/treewindow.html', 'opencms', 170, 300);
    treewindowexists=true;
}

function closeTreeWin(){
    if( treewindowexists==true){
        window.treewin.close();
        treewindowexists=false;
    }
}


/**
 *  displays the tree
 */
function showTree(doc,windowed) {

    var showTreeHead="<html><head>" + 
        "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=" + top.frames.head.encoding + "\">" +
        "<title>opencms explorer tree</title>" +
        "<style type='text/css'> a { text-decoration: none; color: #000000; font-family:arial; font-size:8pt;} a.ig { text-decoration: none; color: silver; font-family:arial; font-size:8pt;} a.tf { text-decoration: none; color: #000000; font-family:MS Sans Serif, Arial, helvetica, sans-serif; font-size:8px;} var a:hover { text-decoration: none; color: #FFFFFF; background:#000066 font-family:arial; font-size:8pt;} body { margin-left:3px; margin-right:0px; margin-top:4px; margin-bottom:0px; margin-height:0px; marginspace:0; margin-top:3px;} </style>" + 
        "</head><body><font face='arial' size=2><table border=0 cellpadding=0 cellspacing=0><tr><td valign=bottom align=left nowrap>";

    var showTreeFoot="</font></TD></TR></table></body></html>";

    doc.open();
    doc.writeln(showTreeHead);

    dfsTree(doc, tree.root, 0, false, new Array(),windowed);
    doc.writeln(showTreeFoot);
    doc.close();
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
     var newDir = directory.substring(0, directory.length -1);
     window.frames[1].frames[1].frames[0].document.forms[0].url.value=newDir.substring(0, newDir.lastIndexOf("/")+1);
     if(window.frames[1].frames[1].frames[0].document.forms[0].url.value.length<3)window.frames[1].frames[1].frames[0].document.forms[0].url.value="/";
     openurl();
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
     if(vi.newButtonActive==false || vr.actProject==vr.onlineProject) bt_new.src = vi.iconPath+'bt_new_in.gif';
     else bt_new.src = vi.iconPath+'bt_new_off.gif';

     bt_up = new Image(32,32);
     if(window.frames[1].frames[1].frames[0].document.forms[0].url.value=="/") bt_up.src = vi.iconPath+'bt_up_in.gif';
     else bt_up.src = vi.iconPath+'bt_up_off.gif';

     bt_search = new Image(32,32);
     bt_search.src = vi.iconPath+'bt_search_off.gif';
     
     eval("doc.images[0].src=bt_back.src;");
     eval("doc.images[1].src=bt_up.src;");
     eval("doc.images[2].src=bt_search.src;");
     eval("doc.images[3].src=bt_new.src;");
     eval("doc.images[4].src=bt_folder.src;");
 }

 /**
  *  display "explorer_head" frame
  */
 function displayHead(doc, pages, actpage){



 if(vr.actDirectory=="/")dirup="";
else dirup="<a href=javascript:top.dirUp(); onmouseover=\"top.chon(document,'bt_up');\" onmouseout=\"top.choff(document,'bt_up');\">";

var pageSelect="";
if(pages>1){
    pageSelect= "<td class=menubold><p class=einzug><b>&nbsp;"+vr.langpage+"&nbsp;&nbsp;&nbsp;</b></p></td>"+
                "<td class=menu>"+
                "<p class=einzug>"+
                "<select name='pageSelect' width=50 style='width:50px' onchange='javascript:top.openurl();return false;'>";
    for(i=1;i<=pages;i++){
        if(i==actpage){
            pageSelect+="<option value='"+i+"' selected>"+i;
        }else{
            pageSelect+="<option value='"+i+"'>"+i;
        }
    }
    pageSelect+="</select></p></td>";
}

    var headHead="<html><head>"+
            "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=" + top.frames.head.encoding + "\">"+
            "<title>opencms</title>"+
            "<style type='text/css'>"+
            "<!"+"--"+
            "body { margin-left:3px; margin-right:0px; margin-top:3px; margin-bottom:0px; marginspace=0;}"+
            "p.einzug { FONT-FAMILY: MS Sans Serif, Arial, helvetica, sans-serif; FONT-SIZE: 8px; TEXT-INDENT: 5px;}"+
            "INPUT.textfeld2 { BACKGROUND-COLOR: white;  COLOR: black; FONT-FAMILY: MS Sans Serif, Arial, helvetica, sans-serif; FONT-SIZE: 8px; FONT-WEIGHT: normal; WIDTH: 425px }"+
            "/"+"/"+"--></style>"+
            "</head><body bgcolor=#c0c0c0 background="+vi.iconPath+"bg_grau.gif topmargin=0 leftmargin=0>"+
            "<form name=urlform onSubmit='javascript:top.openurl();return false;'>"+
            "<table cellspacing=0 cellpadding=0 border=0 valign=top>"+
            "<tr valign=center>"+
            "<td class=menu nowrap width=32px>"+
            "<a href=javascript:top.histGoBack(); onmouseover=\"top.chon(document,'bt_back');\" onmouseout=\"top.choff(document,'bt_back');\" >"+
            "<img alt='"+vr.langback+"' src='"+vi.iconPath+"bt_back_off.gif' width=32 height=32  border=0 name='bt_back'></a></td>"+
            "<td class=menu nowrap width=32px>"+
            dirup+
            "<img alt='"+vr.langup+"' name='bt_up' src='"+vi.iconPath+"bt_up_off.gif' width=32 height=32 border=0 name=bt_up ></a></td>"+
            "<td class=menu nowrap width=32px>"+
            "<a href=javascript:top.dispSearchForm(); onmouseover=\"top.chon(document,'bt_search');\" onmouseout=\"top.choff(document,'bt_search');\" >"+
            "<img alt='"+vr.langsearch+"' src='"+vi.iconPath+"bt_search_off.gif' width=32 height=32  border=0 name='bt_search'></a></td>";

    var headFoot="<td class=menu width=30px nowrap align=right>&nbsp;</td>"+
            "<td class=menubold nowrap align=right valign=middle><img border=0 id='bt_folder' src='"+vi.iconPath+"ic_file_folder.gif' width=16 height=16></td>"+
            "<td class=menubold nowrap align=right valign=middle><p class=einzug> <b>&nbsp;"+vr.langadress+"&nbsp;</b> </td>"+
            "<td class=menu nowrap align=left valign=middle>"+
            "<input value="+vr.actDirectory+" size=50 maxlength=255 name=url id=url class=textfeld2>"+
            "</td>"+pageSelect+"</tr></table></form></body></html>";

    doc.open();
    doc.writeln(headHead);

    if(vr.actProject!=vr.onlineProject && vi.newButtonActive==true){
        doc.writeln("<td class=menu nowrap width=32px>"+
            //"<a href=\"javascript: top.updateFrame('body.explorer_content.explorer_files','explorer_files_new.html');\" target='explorer_files'"+
            "<a href='explorer_files_new.html' target='explorer_files' "+
            "onmouseout=\"top.choff(document, 'bt_new');\" "+
            "onmouseover=\"top.chon(document, 'bt_new');\">");
        doc.writeln("<img alt='"+vr.langnew+"' src='"+vi.iconPath+"bt_new_off.gif' width=32  height=32 border=0 name='bt_new'></a></td>");
    } else {
        doc.writeln("<td class=menu nowrap width=32px>");
        doc.writeln("<img alt='"+vr.langnew+"' width=32 height=32 border=0 name='bt_new_in'></a></td>");
    }

    doc.writeln(headFoot);

    displayHeadPics(doc);

doc.close();
}


/**
 *  started, after window is resized (only in netscape)
 */
function resized(doc){
    if(g_isShowing==false){

        g_isShowing = true;

        rT();
        showTree(explorer_tree,0);

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
    vr.actProject=setto;
}

/**
 *  sets the directory the user is in...
 */
function setDirectory(id, setto){
    vr.actDirId=id;
    vr.actDirectory=setto;
}

function enableNewButton(showit){
    vi.newButtonActive=showit;
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
             if(i==9)out+=("i");
             else{
                 if(b==0)out+=("r");
                 if(b==1)out+=("w");
                 if(b==2){
                     out+=("v");
                 }
             }
        } else out+=("-");
        b++;
        if(b==3){b=0;}
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
     check[2]='vi.check_date';
     check[3]='vi.check_size';
     check[4]='vi.check_status';
     check[5]='vi.check_owner';
     check[6]='vi.check_group';
     check[7]='vi.check_perm';
     check[8]='vi.check_lockedBy';
     check[9]='vi.check_name';

     for(i=0;i<=9;i++){
         if((cols & Math.pow(2,i))>0)eval(check[i]+"=true;");
            else eval(check[i]+"=false;");
     }
 }

 /**
  *  add a file to filelist
  */
 function aF(name, path, title, type, dateolc, whoChanged, date, size, status, project, owner, group, permission, lockedBy, lockedInProjectName, lockedInProjectId){
    if(path=="")path=vr.actDirectory;
    vi.liste[vi.liste.length] = new file( name, path, title, type, dateolc, whoChanged, date, size, status, project, owner, group, permission, lockedBy, lockedInProjectName, lockedInProjectId);
 }


 function openthisfolder(thisdir){

     addHist(window.frames[1].frames[1].frames[0].document.forms[0].url.value);
     window.frames[1].frames[1].frames[0].document.forms[0].url.value +=thisdir+'/';
     openurl();

 }

 function openthisfolderflat(thisdir){
    eval(flaturl + "?folder=" + vr.actDirectory+thisdir+"/\"");
 }



function clicked(doc){
    hideMenu(doc, vi.lastLayer);
}

function setpos(x,y,doc,welche,id,i){
x+=3;
y+=3;
nsx=x;
nsy=y;

showKontext(doc, welche, id,x,y);

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
             "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=" + top.frames.head.encoding + "\">" +
             "<style type='text/css'>"+
             "<!"+"--"+
             "h1 { font-size:48pt; color:#FF0000; font-style:italic; } "+
             // t: topic
             "td.t { background:#c0c0c0; font-size:8pt} "+
             "a { text-decoration: none; } "+
             "td{ font-family: arial, helvetica; font-size: 9pt; }; "+

            // file changed
             "td.fc{ color: #B40000; } "+
             "a.fc{  color: #B40000; font-family: arial, helvetica; font-size: 9pt; } "+
             "a:visited.fc{ color: #B40000; }"+
             "a:hover.fc { background:#000088; color:#FFFFFF; text-decoration: none; } "+

            // file new
             "td.fn{ color: #0000aa; } "+
             "a.fn{  color: #0000aa; } "+
             "a:visited.fn{ color: #0000aa; } "+
             "a:hover.fn{ background:#000066 ; color:#FFFFFF; text-decoration: none; } "+

            // file deleted
            "td.fd{ color: #000000; text-decoration: line-through;} "+
             "a.fd{ color: #000000; font-family: arial, helvetica; font-size: 9pt; text-decoration: line-through;} "+
             "a:visited.fd{ color: #000000; text-decoration: line-through;} "+
             "a:hover.fd{ background:#000066; color:#FFFFFF; text-decoration: line-through; } "+

            // file not in project
             "td.fp{ color: silver;} "+
             "a.fp{ color: #888888; } "+
             "a:visited.fp{ color: #888888;} "+
             "a:hover.fp { background:#000066; color:#FFFFFF; text-decoration: none; } "+

            // normal file
             "td.nf{ color:#000000; } "+
             "a.nf{ color: #000000; } "+
             "a:visited.nf{ color: #000000; } "+
             "a:hover.nf { background:#000066; color:#FFFFFF; text-decoration: none; } "+

            // km: km
             "div.km{ position: absolute; top: 0px; left: 0px; width: 150px; text-indent: 2px; background-color: #c0c0c0; visibility: hidden; z-index: 100;} "+
            // fk: fk
             "table.fk{ width: 150px; background-color: #c0c0c0; } "+
            // kl: kl
             "a.kl { background-color:#c0c0c0; color: black; text-decoration: none;} "+
             "a:hover.kl { color:#FFFFFF; } "+
             "td.inactive{ color:#8c8c8c; } "+
             "/"+"/"+"--></style></head>";
    var returnplace=wo.location.href;
    
    if((openfolderMethod != "openthisfolderflat") && (!projectView)){
        returnplace=returnplace.substring(0, returnplace.lastIndexOf("/")) + "/explorer_files.html";
    }
    returnplace=returnplace.replace(/\?/g, "%3F");
    returnplace=returnplace.replace(/\&/g, "%26");
    returnplace=returnplace.replace(/\=/g, "%3D");
    returnplace=returnplace.replace(/\//g, "%2F");

    wo.open();
    wo.writeln(temp);

    if(ie)wo.writeln("<body bgcolor='#ffffff' onclick=javascript:top.hideLastone(document);>");
    if(gecko)wo.writeln("<body bgcolor='#ffffff' onclick=javascript:top.hideLastone(document);");
    if(ns)wo.writeln("<body bgcolor='#ffffff' "+
                     "onLoad='captureEvents(Event.CLICK);top.whichdoc(document); "+
                     "onClick = top.mouseClickedNs;' "+
                     "onResize=javascript:top.resized(document);>");
     
     wo.writeln("<table cellpadding=1 cellspacing=1 border=0><tr>");

     wo.writeln("<td nowrap class=t width=20>&nbsp;</td>");
     wo.writeln("<td nowrap class=t width=20><img src=\"" + vi.iconPath+"empty.gif" + "\" border=0 width=16 height=1></td>");
     wo.writeln("<td nowrap class=t width=20><img src=\"" + vi.iconPath+"empty.gif" + "\" border=0 width=16 height=1></td>");
     if(vi.check_name)wo.writeln("<td nowrap class=t width=100>&nbsp;"+vr.descr[0]+"</td>");

     if(vi.check_title)wo.writeln("<td nowrap class=t width=100>&nbsp;"+vr.descr[1]+"</td>");
     if(vi.check_type)wo.writeln("<td nowrap class=t width=100>&nbsp;"+vr.descr[2]+"</td>");
     if(vi.check_date)wo.writeln("<td nowrap class=t width=100>&nbsp;"+vr.descr[3]+"</td>");
     if(vi.check_size)wo.writeln("<td nowrap class=t width=50>&nbsp;"+vr.descr[4]+"</td>");
     if(vi.check_status)wo.writeln("<td nowrap class=t width=100>&nbsp;"+vr.descr[5]+"</td>");
     if(vi.check_owner)wo.writeln("<td nowrap class=t width=100>&nbsp;"+vr.descr[6]+"</td>");
     if(vi.check_group)wo.writeln("<td nowrap class=t width=100>&nbsp;"+vr.descr[7]+"</td>");
     if(vi.check_perm)wo.writeln("<td nowrap class=t width=100>&nbsp;"+vr.descr[8]+"</td>");
     if(vi.check_lockedBy)wo.writeln("<td nowrap class=t width=100>&nbsp;"+vr.descr[9]+"</td>");
     wo.writeln("</tr>");

 for(var i=0; i<vi.liste.length; i++){
         
         var vi_icon;
         var vi_text;      
         var noaccess = false; 
         // TODO: Figure out the value of plainresid at runtime
         var plainresid = 3;         
         	
         if (typeof vi.resource[vi.liste[i].type] == 'undefined') {
         // The user has no access to this resource type
         	noaccess = true;
         	vi_icon = vi.resource[plainresid].icon;
         	vi_text = vi.resource[plainresid].text;
         } else {
            vi_icon = vi.resource[vi.liste[i].type].icon;
            vi_text = vi.resource[vi.liste[i].type].text;
         }

         if((vi.liste[i].project!=vr.actProject) || (noaccess)){
              ssclass="fp";
         } else {
             ssclass="nf";
             if(vi.liste[i].status==1)ssclass="fc";
             if(vi.liste[i].status==2)ssclass="fn";
             if(vi.liste[i].status==3)ssclass="fd";
         }

         wo.writeln("<tr>");

         wo.writeln("<td align=center>");

         if(showKon && ! noaccess) {
            wo.writeln(brcfg.showKontext + i + "'," + i + brcfg.showKontextEnd);
         }
         wo.write("<img src='"+vi_icon+"' border=0 width=16 height=16>");
         if(showKon && ! noaccess) {
            wo.write("</a>");
         }
         wo.writeln("</td>");

        if(vi.liste[i].project == vr.actProject) {
            wo.write("<td nowrap align=center>");
            // the ressource is in the current project, so display the lock and projectstate

            var lockIcon;

             if(vi.liste[i].lockedBy!=""){
                if( (vr.userName == vi.liste[i].lockedBy) && (vi.liste[i].lockedInProjectId == vi.liste[i].project)){
                     lockIcon=vi.iconPath+'ic_lockuser.gif';
                }else{
                    lockIcon=vi.iconPath+'ic_lock.gif';
                }
                lockedBystring="alt=\""+vr.altlockedby+" "+vi.liste[i].lockedBy+ vr.altlockedin + vi.liste[i].lockedInProjectName + "\"";
                wo.write("<img src='"+lockIcon+"' "+lockedBystring+" border=0 width=16 height=16></a>");
             }
            wo.write("</td>");

            wo.write("<td nowrap align=center>");
            var projectIcon;
            var projectAltText;
            if(vi.liste[i].status != 0) {
                if (vi.liste[i].lockedInProjectId == vi.liste[i].project) {
                    projectIcon=vi.iconPath+'ic_inthisproject.gif';
                    projectAltText = vr.altbelongto + vi.liste[i].lockedInProjectName;
                } else {
                    projectIcon=vi.iconPath+'ic_inanotherproject.gif';
                    projectAltText = vr.altbelongto + vi.liste[i].lockedInProjectName;
                }
            } else {
                projectIcon=vi.iconPath+'ic_innoproject.gif';
                projectAltText = "";
            }

            wo.write("<img src='"+projectIcon+"' alt='"+projectAltText+"' border=0 width=16 height=16></a>");
            wo.write("</td>");
        } else {
            // nothing to do here
            wo.write("<td></td><td></td>");
        }


         if(vi.check_name){
             if(vi.liste[i].type==0) { 
                 wo.write("<td nowrap class="+ssclass+">");
                 if(this.projectView) {
                    wo.write("&nbsp;"+ vi.liste[i].path + "&nbsp;");
                    wo.writeln("</td>");
                 } else if(flaturl != ""){
                    wo.write("<a href=javascript:top." + openfolderMethod + "('"+vi.liste[i].name+"'); class="+ssclass+">");
                    wo.write("&nbsp;"+vi.liste[i].name+"&nbsp;");
                    wo.write("</a>");
                    wo.writeln("</td>");
                 } else {
                    wo.write("<a href=javascript:top." + openfolderMethod + "('"+vi.liste[i].name+"'); class="+ssclass+">");
                    wo.write("&nbsp;"+vi.liste[i].name+"&nbsp;");
                    wo.write("</a>");
                    wo.writeln("</td>");
                 }
             } else {
                 if(this.projectView) {                
                    wo.write("<td  nowrap class="+ssclass+">");
                    wo.writeln("&nbsp;" + vi.liste[i].path + vi.liste[i].name+ "&nbsp;</td>");
                 } else if(flaturl != ""){
                    wo.write("<td  nowrap class="+ssclass+"><a href=javascript:top.openwinfull('");
                    wo.write(vr.servpath+vr.actDirectory+vi.liste[i].name);
                    wo.writeln("'); class="+ssclass+">&nbsp;"+vi.liste[i].name+"&nbsp;</a></td>");
                 } else {
                    wo.write("<td  nowrap class="+ssclass+"><a href=javascript:top.openwinfull('");
                    wo.write(vr.servpath+vr.actDirectory+vi.liste[i].name);
                    wo.writeln("'); class="+ssclass+">&nbsp;"+vi.liste[i].name+"&nbsp;</a></td>");
                 }
             }
         }
         if(vi.check_title)wo.writeln("<td nowrap class="+ssclass+">&nbsp;"+vi.liste[i].title+"&nbsp;</td>");
         if(vi.check_type)wo.writeln("<td class="+ssclass+">&nbsp;"+vi_text+"</td>");
         if(vi.check_date)wo.writeln("<td nowrap class="+ssclass+">&nbsp;"+vi.liste[i].date+"</td>");
         if(vi.check_size)wo.writeln("<td class="+ssclass+">&nbsp;"+vi.liste[i].size+"</td>");
         if(vi.check_status)wo.writeln("<td class="+ssclass+">&nbsp;"+vr.stati[vi.liste[i].status]+"</td>");
         if(vi.check_owner)wo.writeln("<td class="+ssclass+">&nbsp;"+vi.liste[i].owner+"</td>");
         if(vi.check_group)wo.writeln("<td class="+ssclass+">&nbsp;"+vi.liste[i].group+"</td>");
         if(vi.check_perm)wo.write("<td class="+ssclass+">&nbsp;"+permShow(vi.liste[i].permission,wo)+"</td>");
         if(vi.check_lockedBy)wo.writeln("<td class="+ssclass+">&nbsp;"+vi.liste[i].lockedBy+"</td>");
         wo.writeln("</td></tr>");
     }

     wo.writeln("</tr></table>");
   
    for(i=0;i<vi.liste.length;i++){

         var access = true;          
         if (typeof vi.resource[vi.liste[i].type] == 'undefined') {         
            // the user has no access to this resource type
         	access = false;
         } 
    
		 if (access) {
         wo.writeln("<div id='men"+i+"' class='km'>");
         wo.writeln("<table CELLPADDING=1 CELLSPACING=0 BORDER=0 bgcolor=#777777><tr><td>");
         wo.writeln("<table width=150 CELLPADDING=1 CELLSPACING=0 BORDER=0 class=fk>");

         var resourceName = vr.actDirectory+vi.liste[i].name;
         if(projectView){
            if(vi.liste[i].type == 0){
                resourceName = vi.liste[i].path.substring(0, vi.liste[i].path.lastIndexOf("/"));
            } else {
                resourceName = vi.liste[i].path+vi.liste[i].name;
            }
         }
         for(a=0;a<vi.menus[vi.liste[i].type].items.length;a++){

             // 0:unchanged",1:changed",2:new",3:deleted" 

             if(vr.actProject==vr.onlineProject){
                // online project 
                if(vi.menus[vi.liste[i].type].items[a].rules.charAt(0)=='i'){
                    if(vi.menus[vi.liste[i].type].items[a].name=="-")
                        wo.writeln("<tr><td><hr noshade size=1></td></tr>");
                    else
                        wo.writeln("<TR><TD class=inactive>"+vi.menus[vi.liste[i].type].items[a].name+"</TD></TR>");
                }else{
                    if(vi.menus[vi.liste[i].type].items[a].rules.charAt(0)=='a'){
                        if(vi.menus[vi.liste[i].type].items[a].name=="-"){
                            wo.writeln("<tr><td><hr noshade  size=1></td></tr>");
                        } else {
                        if(vi.liste[i].type==0) wo.writeln("<TR><TD><A class=kl href='"+vi.menus[vi.liste[i].type].items[a].link+"&lasturl="+returnplace+"&file="+resourceName+"/'>"+vi.menus[vi.liste[i].type].items[a].name+"</a></td></tr>");
                         else wo.writeln("<TR><TD><A class=kl href='"+vi.menus[vi.liste[i].type].items[a].link+"&lasturl="+returnplace+"&file="+resourceName+"' target="+vi.menus[vi.liste[i].type].items[a].target+">"+vi.menus[vi.liste[i].type].items[a].name+"</a></td></tr>");
                        }
                    }
                }
             }else{
                // offline project 
                if(vi.liste[i].project==vr.onlineProject){
                    // resource is from onlineproject
                    if(vi.menus[vi.liste[i].type].items[a].rules.charAt(1)=='i'){
                        if(vi.menus[vi.liste[i].type].items[a].name=="-")
                            wo.writeln("<tr><td><hr noshade  size=1></td></tr>");
                        else
                            wo.writeln("<TR><TD class=inactive>"+vi.menus[vi.liste[i].type].items[a].name+"</TD></TR>");
                    }else{
                        if(vi.menus[vi.liste[i].type].items[a].rules.charAt(1)=='a'){
                            if(vi.menus[vi.liste[i].type].items[a].name=="-"){
                                wo.writeln("<tr><td><hr noshade  size=1></td></tr>");
                            } else {
                            if(vi.liste[i].type==0) wo.writeln("<TR><TD><A class=kl href='"+vi.menus[vi.liste[i].type].items[a].link+"&lasturl="+returnplace+"&file="+resourceName+"/'>"+vi.menus[vi.liste[i].type].items[a].name+"</a></td></tr>");
                             else wo.writeln("<TR><TD><A class=kl href='"+vi.menus[vi.liste[i].type].items[a].link+"&lasturl="+returnplace+"&file="+resourceName+"' target="+vi.menus[vi.liste[i].type].items[a].target+">"+vi.menus[vi.liste[i].type].items[a].name+"</a></td></tr>");
                            }
                        }
                    }
                }else{
                    // resource is in this project => we have to differ 5 cases
                    if(vi.liste[i].lockedBy == ''){
                        // free 
                        display = vi.menus[vi.liste[i].type].items[a].rules.charAt(vi.liste[i].status+2);
                    }else{
                        if(vi.liste[i].lockedInProjectId == vr.actProject){
                            // locked in this project from ...
                            if(vi.liste[i].lockedBy == vr.userName){
                                // ... the current user
                                display = vi.menus[vi.liste[i].type].items[a].rules.charAt(vi.liste[i].status+6);
                            }else{
                                // ... someone else
                                display = vi.menus[vi.liste[i].type].items[a].rules.charAt(vi.liste[i].status+10);
                            }
                        }else{
                            // locked in an other project from ...
                            if(vi.liste[i].lockedBy == vr.userName){
                                // ... the current user 
                                display = vi.menus[vi.liste[i].type].items[a].rules.charAt(vi.liste[i].status+14);
                            }else{
                                // ... someone else 
                                display = vi.menus[vi.liste[i].type].items[a].rules.charAt(vi.liste[i].status+18);
                            }
                        }
                    }
                    if(display == 'i'){
                        if(vi.menus[vi.liste[i].type].items[a].name=="-") wo.writeln("<tr><td><hr noshade  size=1></td></tr>");
                        else wo.writeln("<TR><TD class=inactive>"+vi.menus[vi.liste[i].type].items[a].name+"</TD></TR>");
                    }else{
                        if(display == 'a'){
                            if(vi.menus[vi.liste[i].type].items[a].name=="-"){
                                wo.writeln("<tr><td><hr noshade  size=1></td></tr>");
                            }else{
                                if(vi.liste[i].type==0) wo.writeln("<TR><TD><A class=kl href='"+vi.menus[vi.liste[i].type].items[a].link+"&lasturl="+returnplace+"&file="+resourceName+"/' >"+vi.menus[vi.liste[i].type].items[a].name+"</a></td></tr>");
                                    else wo.writeln("<TR><TD><A class=kl href='"+vi.menus[vi.liste[i].type].items[a].link+"&lasturl="+returnplace+"&file="+resourceName+"' target="+vi.menus[vi.liste[i].type].items[a].target+">"+vi.menus[vi.liste[i].type].items[a].name+"</a></td></tr>");
                            }
                        }
                    }
                }
            }
        }
        wo.writeln("</table></td></tr></table></div>");
        }
    }
  
    
    wo.writeln("<br></body></html>");
    wo.close();
}

/**
 *  do an update (filelist display)
 *  pages: are the nummber of pages that are selectable from the head
 *  (if a filelist has more than X entrys, 0 otherwise)
 *  actpage: the aktual shown page
 */

function dU(doc, pages, actpage){
    vi.lastLayer=null;
    vi.locklength=0;
    vi.doc=doc;
    showCols(vr.viewcfg);
    printList(doc);
    if(window.body.explorer_content.explorer_head){
    	folderOpen(vr.actDirId);
    	displayHead(window.body.explorer_content.explorer_head.document, pages, actpage);
    	showTree(window.body.explorer_tree.document,0);
    }

}

/**
 *  do an update (filelist display)
 *  I think this is used if there is no head to display (projectlist, picgallery, etc.)
 */
function dUL(doc){
    vi.lastLayer=null;
    vi.locklength=0;
    vi.doc=doc;
    showCols(vr.viewcfg);
    printList(doc);
}

/**
 *  reset data (filelist)
 */
function rD(){
    vi.liste = new Array();
    vi.icons = new Array();
}

/* km functions --------------------------------------------------- */

/**
 *  called, if mousebutton is clicked (only netscape)
 */
var nsx=0,nsy=0;
function mouseClickedNs(doc){
    nsx=doc.x;
    nsy=doc.y;
    hideLastone(vi.dokument);
}

/**
 *  set vi.dokument to current document
 */
function whichdoc(doc){
    vi.dokument=doc;
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

if(ie || ns){

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
    }


    if(gecko){
        hideMenu(doc, vi.lastLayer);
        doc.getElementById("men"+welche).style.visibility = "visible";
        doc.getElementById("men"+welche).style.left = x;
        doc.getElementById("men"+welche).style.top =  y;
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
        if(ie||ns){
            eval(brcfg.allLayers+"men"+welche+brcfg.hideLayer);
        }
        if(gecko){
            doc.getElementById("men"+welche).style.visibility = "hidden";   
        }
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