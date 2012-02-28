/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

var treeHeadHtml1 =
    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n" +
    "<html>\n<head>\n" +
    "<meta HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=";

var mousedownHandler = "function(e) {"+
"var target;"+
"if (!e) var e = window.event;"+
"if (e.target) {"+  
"   target = e.target;"+ 
"} else if (e.srcElement) {"+ 
"target = e.srcElement;"+ 
"}"+
"if (target.nodeType == 3) {"+
    "target = target.parentNode;"+
"}"+
"if (target && target.tagName && target.tagName.match(/HTML/i)) {"+
"   return true;"+
"}"+
"return false;"+ 
"}";


var treeHeadHtml2 =
    "\">\n" +
    "<title>OpenCms explorer tree</title>\n" +
    "<script type=\"text/javascript\">\n" +
    "document.oncontextmenu = new Function('return false;');\n" +
    "document.onmousedown = " + mousedownHandler + ";\n" +
    "document.onmouseup = new Function('return false;');\n" +
    "function linkOver(obj) {\n" +
    "var cls = obj.className;\n" +
    "if (cls.charAt(cls.length - 1) !== 'i') {\n" +
    "\tcls = cls + 'i';\n" +
    "}\n" +
    "obj.className = cls;\n" +
    "}\n" +
    "function linkOut(obj) {\n" +
    "var cls = obj.className;\n" +
    "if (cls.charAt(cls.length - 1) === 'i') {\n" +
    "\tcls = cls.substring(0, cls.length-1);\n" +
    "}\n" +
    "obj.className = cls;\n" +
    "}\n" +
    "</script>\n" +
    "<style type=\"text/css\">\n" +
    "body  { font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 11px; padding: 2px 0px 0px 2px; margin: 0px; background-color: Window; }\n" +
    "p, td { vertical-align: bottom; font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 11px; white-space: nowrap; color: WindowText; }\n" +
    "a     { vertical-align: bottom; text-decoration: none; cursor: pointer; }\n" +
    "a.ig  { vertical-align: bottom; text-decoration: none; color: #888888; }\n" +
    "a.igi { vertical-align: bottom; text-decoration: underline; color: #000088; }\n" +
    "a.tf  { vertical-align: bottom; text-decoration: none; color: #000000; }\n" +
    "a.tfi { vertical-align: bottom; text-decoration: underline; color: #000088; }\n" +
    "a.fc  { vertical-align: bottom; color: #b40000; } " +
    "a.fci { vertical-align: bottom; text-decoration: underline; color: #000088; } " +
    "a.fn  { vertical-align: bottom; color: #0000aa; } " +
    "a.fni { vertical-align: bottom; text-decoration: underline; color: #000088; } " +
    "a:hover { vertical-align: bottom; text-decoration: underline; color: #000088; }\n" +
    "span.pad { }\n" +
    "img.icon { width: 16px; height: 16px; border: 0px; padding: 0px; margin: 0px; vertical-align: bottom; }\n" +
    "</style>\n" +
    "</head>\n<body>\n" +
    "<font face='arial' size=2>\n<table border=0 cellpadding=0 cellspacing=0>\n";

var treeFootHtml =
    "</table>\n</font>\n</body>\n</html>";

var tree = null;
var vr = null;

var nodeListToLoad = null;

var m_includeFiles = false;
var m_projectAware = true;
var m_rootFolder = null;
var m_sitePrefix = null;
var m_treeType = null;

function treeObject(){
    this.dfsToggleFound = false;
    this.icon = [];
    this.nodes = [];
    this.root = null;
}

function resourceTypeObject(id, text, nicename, icon){
    this.id = id;
    this.text = text;
    this.nicename = nicename;
    this.icon = icon;
}

function resourceObject(encoding, contextPath, workplacePath, skinPath) {
    this.encoding = encoding;
    this.actDirId = 0;
    this.contextPath = contextPath;
    this.workplacePath = workplacePath;
    this.skinPath = skinPath;
    this.resource = [];
    this.scrollTopType = 0;
    this.scrollTop = 0;
    this.scrollLeft = 0;
}

function addIcon(w, h, source) {
    var a = tree.icon.length;
    tree.icon[a] = source;
}

function addTreeIcons() {
    addIcon(16, 16, vr.skinPath + "tree/empty.gif");
    addIcon(16, 16, vr.skinPath + "tree/end.gif");
    addIcon(16, 16, vr.skinPath + "tree/folder.gif");
    addIcon(16, 16, vr.skinPath + "tree/folder_open.gif");
    addIcon(16, 16, vr.skinPath + "tree/cross.gif");
    addIcon(16, 16, vr.skinPath + "tree/mend.gif");
    addIcon(16, 16, vr.skinPath + "tree/mcross.gif");
    addIcon(16, 16, vr.skinPath + "tree/pend.gif");
    addIcon(16, 16, vr.skinPath + "tree/pcross.gif");
    addIcon(16, 16, vr.skinPath + "tree/start.gif");
    addIcon(16, 16, vr.skinPath + "tree/line.gif");
    addIcon(16, 16, vr.skinPath + "tree/upend.gif");
    addIcon(16, 16, vr.skinPath + "tree/upcross.gif");
    addIcon(16, 16, vr.skinPath + "tree/ufolder.gif");
}

function initResources(encoding, workplacePath, skinPath, contextPath) {
    vr = new resourceObject(encoding, contextPath, workplacePath, skinPath);
    tree = new treeObject();
    addTreeIcons();
}

function nodeObject(name, type, folder, id, parentId, state, grey, open){
    this.name = name;
    this.type = type;
    this.folder = (folder === 1) ? true : false;
    this.id = id;
    this.parentId = parentId;
    this.state = state;
    this.grey = (grey === 1) ? true : false;
    this.open = open;
    this.childs = null;
}

// returns a node based on its ID
function getNodeById(nodeId) {
    // this emulates the following line of code: return tree.nodes[nodeId];
    // this is necessary, because node IDs are sometimes passed as Strings and sometimes as numbers, which will lead to
    // trouble in FF 3.5 with enabled JIT. Please note that the loose comparison "==" must be used here rather than "==="
    nodeId = parseInt( nodeId );
    for (var currNode in tree.nodes) {
        if (currNode == nodeId) {
            return tree.nodes[ currNode ];
        }
    }
    return null;
}

function nodeToLoad(id, name) {
    this.id = id;
    this.name = name;
}

function addResourceType(id, text, nicename, icon) {
    vr.resource[id] = new resourceTypeObject(id, text, nicename, icon);
}

function inExplorer() {
    if (window.top.initHist) {
        // explorer frameset version
        return true;
    } else {
        // windowed version
        return false;
    }
}

function initTree() {
    tree.nodes = [];
    tree.root = null;
    tree.dfsToggleFound = false;
    vr.actDirId = null;
    if (inExplorer()) {
        window.top.initHist();
    }
}

// generic helper: get a random number between "min" and "max"
function getRandom(min, max) {
  return (Math.round(Math.random()*(max-min)))+min;
}

// generic helper: get the scroll top of the current document
function saveScroll(doc) {
    var st = 0;
    if (doc.documentElement && (doc.documentElement.scrollTop || doc.documentElement.scrollLeft)) {
        vr.scrollTop = doc.documentElement.scrollTop;
        vr.scrollLeft = doc.documentElement.scrollLeft;
        vr.scrollTopType = 1;
    } else if (doc.body && (doc.body.scrollTop || doc.body.scrollLeft)) {
        vr.scrollTop = doc.body.scrollTop;
        vr.scrollLeft = doc.body.scrollLeft;
        vr.scrollTopType = 2;
    } else {
        vr.scrollTop = 0;
        vr.scrollLeft = 0;
    }
    return st;
}

// generic helper: set the scroll top of the current document
function restoreScroll(doc) {
    if (vr.scrollTopType === 1) {
        doc.documentElement.scrollTop = vr.scrollTop;
        doc.documentElement.scrollLeft = vr.scrollLeft;
    } else if (vr.scrollTopType === 2) {
        doc.body.scrollTop = vr.scrollTop;
        doc.body.scrollLeft = vr.scrollLeft;
    }
}

function dfsToggle(id, nodeId) {
    var node = getNodeById(nodeId);
    if (node) {
        if (id == nodeId) {
            node.open = !node.open;
            tree.dfsToggleFound = true;
            return;
        }
        if (node.childs) {
            for (var loop1=0; loop1<node.childs.length; loop1++) {
                dfsToggle(id, node.childs[loop1]);
                if (tree.dfsToggleFound) {
                  return;
                }
            }
        }
    }
}

function loadSubnodes(doc, pic, node) {
    doc.write("<a href=\"javascript:parent.loadNode(document, " + node.id + ");\"><img src=\"" + pic + "\" class=\"icon\" align=\"left\"></a>");
}

function showPic(doc, pic) {
    doc.write("<img src=\"" + pic + "\" class=\"icon\" align=\"left\">");
}

function showPicLink(doc, pic, node) {
    doc.write("<a href=\"javascript:parent.toggleNode(document, " + node.id + ");\"><img src=\"" + pic + "\" class=\"icon\" align=\"left\"></a>");
}

function dfsTree(doc, node, depth, last, shape) {
    var loop1;

    doc.write("<tr><td>");
    
    if (node.folder && node.childs) {
       var seen = {};
       var newChildren = [];
       for (var i = 0; i < node.childs.length; i++) {
          var currentChild = node.childs[i];
          if (!seen[currentChild]) {
             newChildren.push(currentChild);
          }
          seen[currentChild] = true;
       }
       node.childs = newChildren;
    }

    if (!node.parentId) {
        showPic(doc, tree.icon[9]); // root folder
    } else {
        for (loop1=0; loop1<depth-1; loop1++) {
            if (shape[loop1+1] === 1) {
                showPic(doc, tree.icon[10]); // vertical line
            } else {
                showPic(doc, tree.icon[0]); // empty
            }
        }

        if (last) {
            if ((node.folder) && (!node.childs)) {
                loadSubnodes(doc, tree.icon[11], node); // corner unknown
            } else if ((node.folder) && (node.childs.length > 0)) {
                if (node.open) {
                    showPicLink(doc, tree.icon[5], node); // corner minus
                } else {
                    showPicLink(doc, tree.icon[7], node); // corner plus
                }
            } else {
                showPic(doc, tree.icon[1]); // corner
            }
            shape[depth] = 0;
        } else {
            if ((node.folder) && (!node.childs)) {
                loadSubnodes(doc, tree.icon[12], node); // cross unknown
            } else if ((node.folder) && (node.childs.length > 0)) {
                if (node.open) {
                    showPicLink(doc, tree.icon[6], node); // cross minus
                } else {
                    showPicLink(doc, tree.icon[8], node); // cross plus
                }
            } else {
                showPic(doc, tree.icon[4]); // cross
            }
            shape[depth] = 1;
        }

        if (node.folder) {
            // this node is a folder
            if(node.id == vr.actDirId) {
                showPic(doc, tree.icon[3]); // folder open
            } else {
                if (node.childs) {
                    showPic(doc, tree.icon[2]); // folder closed
                } else {
                    showPic(doc, tree.icon[13]); // folder closed, grey
                }
            }
        } else {
            // this node is not a folder
            if (vr.resource[node.type]) {
                showPic(doc, vr.skinPath + vr.resource[node.type].icon);
            } else {
                // unknown type, use "plain" icon
                showPic(doc, vr.skinPath + vr.resource[3].icon);
            }
        }
    }
    
    var linkClass;
    if (node.grey) {
        // grey folder
        linkClass = "ig";
    } else {
        switch (node.state) {
            case 1:
                // changed folder
                linkClass = "fc";
                break;
            case 2:
                // new folder
                linkClass = "fn";
                break;
            default:
                // common folder
                linkClass = "tf";
        }
    } 

    doc.write("&nbsp;<a class=\"" + linkClass + "\" onclick=\"parent.doAction(document, " + node.id + ");\" onmouseover=\"linkOver(this);\" onmouseout=\"linkOut(this);\">" + node.name + "</a>");

    doc.writeln("</td></tr>");

    if ((node.open || (node === tree.root)) && (node.childs)) {
        for (loop1=0; loop1<node.childs.length; loop1++) {
            dfsTree(doc, getNodeById(node.childs[loop1]), depth+1, (loop1===(node.childs.length-1)), shape);
        }
    }
}

// ensures that all folders up to the current folder are open
function setCurrentFolder(nodeId) {
    var node = getNodeById(nodeId);
    if ((node) && (!node.folder) && (node.parentId)) {
        // if a file is selected set it's parent folder
        node = getNodeById(node.parentId);
    }
    if (node) {
        vr.actDirId = node.id;
        while ((node) && (node.id != tree.root.id)) {
            node.open = true;
            node = getNodeById(node.parentId);
        }
    }
}

function showTree(doc, nodeId) {
    if (nodeId) {
        setCurrentFolder(nodeId);
        var node = getNodeById(nodeId);
        if (node && (!node.childs)) {
            node.childs = [];
        }
    }
    doc.open();
    doc.write(treeHeadHtml1);
    doc.write(vr.encoding);
    doc.write(treeHeadHtml2);
    dfsTree(doc, tree.root, 0, false, []);
    doc.writeln(treeFootHtml);
    doc.close();
}

function toggleNode(doc, id) {
    saveScroll(doc);
    tree.dfsToggleFound = false;
    dfsToggle(id, tree.root.id);
    showTree(doc);
    restoreScroll(doc);
}

function showLoadedNodes(doc, id) {
    if (!vr.actDirId) {
        setCurrentFolder(id);
    }
    if (!getNodeById(id).open) {
        toggleNode(doc, id);
    } else {
        saveScroll(doc);
        showTree(doc);
        restoreScroll(doc, vr.scrollTop);
    }
}

function setNoChilds(nodeId) {
    var node = getNodeById(nodeId);
    if (node) {
        node.childs = [];
    }
}

//returns the node id from a given name
function getNodeIdByName(nodeName) {
    var node = tree.root;
    // special case for the shared folder
    var isShared = (window.sharedFolderName == tree.root.name); 

    // remove first slash; split the path into an array
    var nameParts = nodeName.substr(1).split("/");

    // search the tree and try to find a matching folder for each part of the path
    if (nodeName != "/") {
      for (var i=0; i<nameParts.length && node; i++) {
         if (isShared && i == 0 && ("/" + nameParts[i] + "/" == window.sharedFolderName)) {
            continue;
         }
         var children = node.childs;
        
         // clear the current node until we find the next child node.
         // if no child is found, then we know that the search was not successful, because 'node' will remain null
         node = null;

         for (var j=0; children && j<children.length; j++) {
            var subnode = getNodeById(children[j]);
            if (subnode && subnode.name === nameParts[i]) {
               // found the next sub-node => continue searching on the next level
               node = subnode;
               break;
            }
         }
      }
    }
    if (node) {
        return node.id;
    } else {
        return null;
    }
}


// returns the node name from a given id
function getNodeNameById(nodeId) {
    var node = getNodeById(nodeId);
    var result = "";
    if (node) {
        if (node.id == tree.root.id) {
            return "/";
        } else {
            var isFolder = (node.folder);
            do {
                result = "/" + node.name + result;
                node = getNodeById(node.parentId);
            } while ((node) && (node !== tree.root));
            if (isFolder && result.charAt(result.length - 1) !== '/') {
                result += "/";
            }
            return result;
        }
    } else {
        return null;
    }
}


function aC(name, type, folder, id, parentId, state, grey) {
    var theParent = getNodeById(parentId);
    var oldNode = getNodeById(id);

    if (!oldNode) {
        // the node is not already present in the tree, insert it
        if (!theParent) {
            // this is the "root" node
            tree.root      = new nodeObject(name, type, folder, id, null, state, grey, false);
            tree.nodes[id] = tree.root;
        } else {
            // this is a regular subnode
            tree.nodes[id] = new nodeObject(name, type, folder, id, parentId, state, grey, false);
            if (!theParent.childs) {
                theParent.childs = [];
            }
            theParent.childs[theParent.childs.length] = id;
        }
    } else {
        // the node is already part of the tree, just update it
        var newNode = new nodeObject(name, type, folder, id, parentId, oldNode.state, oldNode.grey, oldNode.open);
        newNode.childs = oldNode.childs;
        tree.nodes[id] = newNode;
        // the parents "child node list" must have been cleared before adding new nodes
        theParent.childs[theParent.childs.length] = id;
    }
}

function includeFiles() {
    return m_includeFiles;
}

function setIncludeFiles(value) {
    m_includeFiles = value;
}

function isProjectAware() {
    return m_projectAware;
}

function setProjectAware(value) {
    m_projectAware = value;
}

function getTreeType() {
    return m_treeType;
}

function setTreeType(value) {
    m_treeType = value;
}

function getSitePrefix() {
    return m_sitePrefix;
}

function setSitePrefix(value) {
    m_sitePrefix = value;
}

function getRootFolder() {
    if (!m_rootFolder) {
        return "/";
    } else {
        return m_rootFolder;
    }
}

function setRootFolder(value) {
    m_rootFolder = value;
}

function doActionFolderOpen(doc, nodeId) {
    setCurrentFolder(nodeId);
    nodeId = vr.actDirId;
    var params = "";
    if (includeFiles()) {
        params += "&includefiles=true";
    }
    if (!isProjectAware()) {
        params += "&projectaware=false";
    }
    if (getNodeById(nodeId).childs) {
        setNoChilds(nodeId);
    }
    var nodeName = "";
    if (getNodeById(nodeId).id == tree.root.id) {
        nodeName = "/";
        params += "&rootloaded=true";
    } else {
        nodeName = getNodeNameById(nodeId);
    }
    if (getTreeType()) {
        params += "&type=" + getTreeType();
    }
    if (m_rootFolder) {
        nodeName = m_rootFolder + nodeName.substring(1);        
    }
    var target = "views/explorer/tree_files.jsp?resource=" + nodeName + params;
    tree_files.location.href = vr.contextPath + vr.workplacePath + target;
    if (inExplorer()) {
      window.top.cancelNextOpen = false;
      window.top.openFolder(nodeName);
    }
}

function doActionInsertSelected(doc, nodeId) {
    var filePrefix = "";
    if (getSitePrefix()) {
        filePrefix = getSitePrefix();
    }
    getForm().setFormValue(filePrefix + getNodeNameById(nodeId));
}

function getForm() {
   if (window['setFormValue']) {
      return window;
   }
   return window.opener; 
}


// called if the folder name is clicked in the tree
function doAction(doc, nodeId) {
    if (inExplorer()) {
        doActionFolderOpen(doc, nodeId);
    } else {
        doActionInsertSelected(doc, nodeId);
        doActionFolderOpen(doc, nodeId);
    }
}

function addNodeToLoad(nodeId, nodeName) {
    if (!nodeName) {
        nodeName = getNodeNameById(nodeId);
    } else if (!nodeId) {
        if ((nodeName !== "/") && (nodeName.charAt(nodeName.length-1) === '/')) {
            nodeName = nodeName.substring(0, nodeName.length-1);
        }
        nodeId = getNodeIdByName(nodeName);
    }
    if (nodeName.charAt(nodeName.length-1) !== '/') {
        nodeName += "/";
    }
    if (m_rootFolder) {
        nodeName = m_rootFolder + nodeName.substring(1);        
    }
    var node = new nodeToLoad(nodeId, nodeName);    
    if (!nodeListToLoad) {
        nodeListToLoad = [];
    }
    nodeListToLoad[nodeListToLoad.length] = node;
}

function loadNodeList(doc, params) {
    if (nodeListToLoad.length <= 0) {
        return;
    }    
    if (!params) {
        params = "";
    }
    if (includeFiles()) {
        params += "&includefiles=true";
    }
    if (!isProjectAware()) {
        params += "&projectaware=false";
    }
      if (getTreeType()) {
            params += "&type=" + getTreeType();
        }        
    var nodeNames = "";
    for (var i=0; i < nodeListToLoad.length; i++) {
        var node = nodeListToLoad[i];
        if ((node.id) && (getNodeById(node.id).childs)) {
            setNoChilds(node.id);
        }        
        nodeNames += node.name;    
        if (i < nodeListToLoad.length-1) {
            nodeNames += "|";
        }
    }
    nodeListToLoad = null;
    var target = "views/explorer/tree_files.jsp?resource=" + nodeNames + params;
    tree_files.location.href = vr.contextPath + vr.workplacePath + target;    
}

// called if a new folder is loaded from the explorer file list
function updateCurrentFolder(doc, folderName) {
    if ((folderName !== "/") && (folderName.charAt(folderName.length-1) === '/')) {
            folderName = folderName.substring(0, folderName.length-1);
    }
    if (m_rootFolder) {
        folderName = folderName.substring(m_rootFolder.length-1);
        if (folderName == "") {
            // assure that folder name is never empty (i.e. the root folder itself)
            folderName = "/";
          }
    }
    var nodeId = getNodeIdByName(folderName);
    var nodeName = null;
    var params = null;
    if (nodeId) {
        // node was already loaded, update it
        if (vr.actDirId !== nodeId) {
            setCurrentFolder(nodeId);
            params = "&rootloaded=true";
        }
    } else {
        // node not loaded (or invalid), check last node that is loaded
        vr.actDirId = null;
        var lastKnown = folderName;
        var known = false;
        var lastKnownId = null;
        while (!known && (lastKnown) && !("/" === lastKnown)) {
            lastKnown = lastKnown.substring(0, lastKnown.lastIndexOf("/"));
            if (lastKnown === "") {
                lastKnown = "/";
            }
            lastKnownId = getNodeIdByName(lastKnown);
            known = (lastKnownId);
        }
        nodeId = lastKnownId;
        nodeName = folderName;
        params = "&lastknown=" + lastKnown;
    }
    if (params) {
        addNodeToLoad(nodeId, nodeName);
        loadNodeList(doc, params);
    }
}

// called if a +/- is clicked in the tree, or from updateCurrentFolder
function loadNode(doc, nodeId, nodeName, params) {
    addNodeToLoad(nodeId, nodeName);
    loadNodeList(doc, params);
}