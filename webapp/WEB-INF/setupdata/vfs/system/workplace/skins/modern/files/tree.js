/*
 * File   : $Source: $
 * Date   : $Date: $
 * Version: $Revision: $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2003 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
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
	"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n"
	+ "<html>\n<head>\n"
	+ "<meta HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=";

var treeHeadHtml2 =
	"\">\n"
	+ "<title>opencms explorer tree</title>\n"
	+ "<style type=\"text/css\">\n"
	+ "body  { font-family: verdana, sans-serif; font-size: 11px; padding: 2px 0px 0px 2px; margin: 0px; backgound-color: #ffffff; }\n"
	+ "p, td { vertical-align: bottom; font-family: verdana, sans-serif; font-size: 11px; white-space: nowrap; color: #000000; }\n"
	+ "a     { vertical-align: bottom; text-decoration: none; }\n"
	+ "a.ig  { vertical-align: bottom; text-decoration: none; color: #888888; }\n"
	+ "a.tf  { vertical-align: bottom; text-decoration: none; color: #000000; }\n"
	+ "a:hover { vertical-align: bottom; text-decoration: underline; color: #000088; }\n"
	+ "span.pad { }\n"
	+ "img.icon { width: 16px; height: 16px; border: 0px; padding: 0px; margin: 0px; vertical-align: bottom; }\n"
	+ "</style>\n"
	+ "</head>\n<body>\n"
	+ "<font face='arial' size=2>\n<table border=0 cellpadding=0 cellspacing=0>\n";

var treeFootHtml =
	"</table>\n</font>\n</body>\n</html>";


var tree = null;
var vr = null;

var nodeListToLoad = null;

function initResources(encoding, workplacePath, skinPath, contextPath) {
	vr = new resourceObject(encoding, contextPath, workplacePath, skinPath);
	tree = new treeObject();
	addTreeIcons();
}


function treeObject(){
	this.dfsToggleFound = false;
	this.icon = new Array();
	this.nodes = new Array();
	this.root = null;
}


function nodeObject(name, type, id, parentId, grey, open){
	this.name = name;
	this.type = type;
	this.id = id;
	this.parentId = parentId;
	this.grey = grey;
	this.open = open;
	this.childs = null;
}


function nodeToLoad(id, name) {
	this.id = id;
	this.name = name;
}


function resourceTypeObject(id, text, nicename, icon){
	this.id = id;
	this.text = text;
	this.nicename = nicename;
	this.icon = icon;
}


function resourceObject(encoding, contextPath, workplacePath, skinPath) {
	this.encoding = encoding;
	this.actDirId;
	this.contextPath = contextPath;
	this.workplacePath = workplacePath;
	this.skinPath = skinPath;
	this.resource = new Array();
	this.scrollTopType = 0;
	this.scrollTop = 0;
	this.scrollLeft = 0;
}


function addResourceType(id, text, nicename, icon) {
	vr.resource[id] = new resourceTypeObject(id, text, nicename, icon);
}


function initTree() {
	tree.nodes = new Array();
	tree.root = null;
	tree.dfsToggleFound = false;
	vr.actDirId = null;
	if (inExplorer()) {
		top.initHist();
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
	if (vr.scrollTopType == 1) {
		doc.documentElement.scrollTop = vr.scrollTop;
		doc.documentElement.scrollLeft = vr.scrollLeft;
	} else if (vr.scrollTopType == 2) {
		doc.body.scrollTop = vr.scrollTop;
		doc.body.scrollLeft = vr.scrollLeft;
	}
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


function dfsToggle(id, nodeId) {
	var node = tree.nodes[nodeId];
	if (node != null) {
		if (id == nodeId) {
			node.open = !node.open;
			tree.dfsToggleFound = true;
			return;
		}
		if (node.childs != null) {
			for (var loop1=0; loop1<node.childs.length; loop1++) {
				dfsToggle(id, node.childs[loop1]);
				if (tree.dfsToggleFound) return;
			}
		}
	}
}


function toggleNode(doc, id) {
	saveScroll(doc);
	tree.dfsToggleFound = false;
	dfsToggle(id, tree.root.id);
	showTree(doc);
	restoreScroll(doc);
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

	if (node.parentId == null) {
		showPic(doc, tree.icon[9]); // root folder
	} else {
		for (loop1=0; loop1<depth-1; loop1++) {
			if (shape[loop1+1] == 1) {
				showPic(doc, tree.icon[10]); // vertical line
			} else {
				showPic(doc, tree.icon[0]); // empty
			}
		}

		if (last) {
			if ((node.type == 0) && (node.childs == null)) {
				loadSubnodes(doc, tree.icon[11], node); // corner unknown
			} else if ((node.type == 0) && (node.childs.length > 0)) {
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
			if ((node.type == 0) && (node.childs == null)) {
				loadSubnodes(doc, tree.icon[12], node); // cross unknown
			} else if ((node.type == 0) && (node.childs.length > 0)) {
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

		if (node.type == 0) {
			// this node is a folder
			if(node.id == vr.actDirId) {
				showPic(doc, tree.icon[3]); // folder open
			} else {
				if (node.childs != null) {
					showPic(doc, tree.icon[2]); // folder closed
				} else {
					showPic(doc, tree.icon[13]); // folder closed, grey
				}
			}
		} else {
			// this node is not a folder
			if (vr.resource[node.type] != null) {
				showPic(doc, vr.skinPath + vr.resource[node.type].icon);
			} else {
				// unknown type, use "plain" icon
				showPic(doc, vr.skinPath + vr.resource[3].icon);
			}
		}
	}

	var color="class=\"tf\" ";
	if(node.grey) {
		color="class=\"ig\" ";
	}

	doc.write("&nbsp;<a " + color + " href=\"javascript:parent.doAction(document, " + node.id + ");\">" + node.name + "</a>");

	doc.writeln("</td></tr>");

	if ((node.open || (node == tree.root)) && (node.childs != null)) {
		for (var loop1=0; loop1<node.childs.length; loop1++) {
			dfsTree(doc, tree.nodes[node.childs[loop1]], depth+1, (loop1==(node.childs.length-1)), shape);
		}
	}
}


function inExplorer() {
	if (top.initHist) {
		// explorer frameset version
		return true;
	} else {
		// windowed version
		return false;
	}
}


function showTree(doc, nodeId) {
	if (nodeId != null) {
		setCurrentFolder(nodeId);
		if ((tree.nodes[nodeId] != null) && (tree.nodes[nodeId].childs == null)) {
			tree.nodes[nodeId].childs = new Array();
		}
	}
	doc.open();
	doc.write(treeHeadHtml1);
	doc.write(vr.encoding);
	doc.write(treeHeadHtml2);
	dfsTree(doc, tree.root, 0, false, new Array());
	doc.writeln(treeFootHtml);
	doc.close();
}


function showLoadedNodes(doc, id) {
	if (vr.actDirId == null) {
		setCurrentFolder(id);
	}
	if (! tree.nodes[id].open) {
		toggleNode(doc, id);
	} else {
		saveScroll(doc);
		showTree(doc);
		restoreScroll(doc, vr.scrollTop);
	}
}


function setNoChilds(nodeId) {
    var node = tree.nodes[nodeId];
    if (node != null) {
        tree.nodes[nodeId].childs = new Array();
    }
}


// returns the node id from a given name
function getNodeIdByName(nodeName) {
	var result = "";
	var node = tree.root;
	if (nodeName != "/") {
		while ((node != null) && (result != nodeName)) {
			result += "/";
			var subnode = null;
			var childs = node.childs;
				if (childs != null) {
				for (i=0; i<childs.length; i++) {
					subnode = tree.nodes[childs[i]];
					var subname = result + subnode.name;
					if ((subnode != null) && ((nodeName == subname) || (nodeName.indexOf(subname + "/") == 0))) {
						result = result + subnode.name;
						i = childs.length;
					}
				}
			}
			node = subnode;
		}
	}
	if (node  != null) {
		return node.id;
	} else {
		return null;
	}
}


// returns the node name from a given id
function getNodeNameById(nodeId) {
	var node = tree.nodes[nodeId];
	var result = "";
	if (node != null) {
		if (node.id == tree.root.id) {
			return "/";
		} else {
			var isFolder = (node.type == 0);
			do {
				result = ("/" + node.name + result);
				node = tree.nodes[node.parentId];
			} while ((node != null) && (node != tree.root));
			if (isFolder && result.charAt(result.length-1) != '/') {
				result += "/";
			}
			return result;
		}
	} else {
		return null;
	}
}


function aC(name, type, id, parentId, grey) {
	var theParent = tree.nodes[parentId];
	var oldNode = tree.nodes[id];

	if (oldNode == null) {
		// the node is not already present in the tree, so insert it
		if (theParent == null) {
			// this is the "root" node
			tree.nodes[id] = new nodeObject(name, type, id, null, grey, false);
			tree.root = tree.nodes[id];
		} else {
			// this is a regular subnode
			tree.nodes[id] = new nodeObject(name, type, id, parentId, grey, false);
			if (theParent.childs == null) {
				theParent.childs = new Array();
			}
			theParent.childs[theParent.childs.length] = id;
		}
	} else {
		// the node is already part of the tree, so just update it
		newNode = new nodeObject(name, type, id, parentId, oldNode.grey, oldNode.open);
		newNode.childs = oldNode.childs;
		tree.nodes[id] = newNode;
		// the parents "child node list" must have been cleared before adding new nodes
		theParent.childs[theParent.childs.length] = id;
	}
}


var m_includeFiles = false;


function includeFiles() {
	return m_includeFiles;
}


function setIncludeFiles(value) {
	m_includeFiles = value;
}


var m_treeType = null;

function getTreeType() {
	return m_treeType;
}


function setTreeType(value) {
	m_treeType = value;
}


var m_sitePrefix = null;

function getSitePrefix() {
	return m_sitePrefix;
}


function setSitePrefix(value) {
	m_sitePrefix = value;
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


function doActionFolderOpen(doc, nodeId) {
    setCurrentFolder(nodeId);
    nodeId = vr.actDirId;
    var params = "";
    if (includeFiles()) {
    	params += "&includefiles=true"
    }
    if (tree.nodes[nodeId].childs != null) {
        setNoChilds(nodeId);
    }
    var nodeName = "";
    if (tree.nodes[nodeId].id == tree.root.id) {
        nodeName = "/";
        params += "&rootloaded=true";
    } else {
        nodeName = getNodeNameById(nodeId);
    }
    if (getTreeType() != null) {
    	params += "&type=" + getTreeType();
    }
    var target = "tree_files.html?resource=" + nodeName + params;
    tree_files.location.href = vr.contextPath + vr.workplacePath + target;
    if (inExplorer()) {
        top.openFolder(nodeName);
    }
}

function doActionInsertSelected(doc, nodeId) {
	var filePrefix = "";
	if (getSitePrefix() != null) {
		filePrefix = getSitePrefix();
	}
	window.opener.setFormValue(filePrefix + getNodeNameById(nodeId));
}


// called if a new folder is loded from the explorer file list
function updateCurrentFolder(doc, folderName) {
	if ((folderName != "/") && (folderName.charAt(folderName.length-1) == '/')) {
			folderName = folderName.substring(0, folderName.length-1);
	}
	var nodeId = getNodeIdByName(folderName);
	var nodeName = null;
	var params = null;
	if (nodeId != null) {
		// node was already loaded, update it
		if (vr.actDirId != nodeId) {
			setCurrentFolder(nodeId);
			params = "&rootloaded=true";
		}
	} else {
		// node not loaded (or invalid), check last node that is loaded
		vr.actDirId = null;
		var lastKnown = folderName;
		var known = false;
		var lastKnownId = null;
		while (!known && (lastKnown != null) && !("/" == lastKnown)) {
			lastKnown = lastKnown.substring(0, lastKnown.lastIndexOf("/"));
			if (lastKnown == "") {
				lastKnown = "/";
			}
			lastKnownId = getNodeIdByName(lastKnown);
			known = (lastKnownId != null);
		}
		nodeId = lastKnownId;
		nodeName = folderName;
		params = "&lastknown=" + lastKnown;
	}
	if (params != null) {
		addNodeToLoad(nodeId, nodeName);
		loadNodeList(doc, params);
	}
}

function addNodeToLoad(nodeId, nodeName) {
	if (nodeName == null) {
		nodeName = getNodeNameById(nodeId);
	}
	if (nodeName.charAt(nodeName.length-1) != '/') {
		nodeName += "/";
	}
	node = new nodeToLoad(nodeId, nodeName);	
	if (nodeListToLoad == null) {
		nodeListToLoad = new Array();
	}
	nodeListToLoad[nodeListToLoad.length] = node;
}

function loadNodeList(doc, params) {
	if (nodeListToLoad.length <= 0) {
		return;
	}	
	if (params == null) {
		params = "";
	}
    	if (includeFiles()) {
    		params += "&includefiles=true"
    	}
   	if (getTreeType() != null) {
    		params += "&type=" + getTreeType();
    	}    	
	var nodeNames = "";
	for (i=0; i < nodeListToLoad.length; i++) {
		node = nodeListToLoad[i];
		if ((node.id != null) && (tree.nodes[node.id].childs != null)) {
			setNoChilds(node.id);
		}		
		nodeNames += node.name;	
		if (i < nodeListToLoad.length-1) {
			nodeNames += "|";
		}
	}		
	nodeListToLoad = null;
	var target = "tree_files.html?resource=" + nodeNames + params;
	tree_files.location.href = vr.contextPath + vr.workplacePath + target;	
}

// called if a +/- is clicked in the tree, or from updateCurrentFolder
function loadNode(doc, nodeId, nodeName, params) {
	addNodeToLoad(nodeId, nodeName);
	loadNodeList(doc, params);
}


// ensures that all folders up to the current folder are open
function setCurrentFolder(nodeId) {
	node = tree.nodes[nodeId];
	if ((node != null) && (node.type != 0) && (node.parentId != null)) {
		// if a file is selected set it's parent folder
		node = tree.nodes[node.parentId];
	}
	if (node != null) {
		vr.actDirId = node.id;
		while ((node != null) && (node.id != tree.root.id)) {
			node.open = true;
			node = tree.nodes[node.parentId];
		}
	}
}