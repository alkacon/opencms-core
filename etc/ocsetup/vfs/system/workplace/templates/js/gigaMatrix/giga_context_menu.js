

var currSiteNodeId = '';


function gigaContextMenuCollection(gigaContextMenuCollectionId) {

/*

Giga Context Menu (a.k.a Pop-menus) v0.1 - Written by Fini A. Alring <alring@email.com|fini@framfab.dk>  

Features:
Object oriented design allowing easy implementation of new features
"Unlimited" branched menus
Uses CSS for most fonts, colors.
Seperator lines
Ghosted items
Cross-browser support using WarpCoreBreach v1.0 - Let it be!!! 
Lock/open/close mechanism for pop-up's.  
Auto close if other sister and it's cousins menu is attempting to open.  - hard one! Time is 10:05 a.m. 
M size autowidth, set with to null, and it will auto size.
- BUGGY : if only 1 item is inserted is doesn't work. ;(

Comming features:

http://www.webreference.com/dhtml/diner/
3 Positioning system, allowing re-use of the pop-up's, must also pass parameter to indicate what it is dealing with.
3* close "all"


2* item method, enable(true|false) using ghosted var.  
2* Better API for handling changes to the menus during runtime, i.e. an item being unghosted,hidden,renamed etc.
2* Screen/window border system to avoid menus moving outside the visible area (if possible ;-)


1* renderMenuItem(true|false);



*/

    /* Properties */

	this.menus = new Array(); // gigaContextMenu() obj's
	this.numOfMenus = 0;
	this.gigaContextMenuCollectionId = gigaContextMenuCollectionId;
	this.offsetX = 0;
	this.offsetY = 0;
	this.rootMenuId = '';
	
	
    /* Methods */
	
	this.addMenu = _addMenu;
	this.hideAll = _hideAll;
}


function _addMenu( menuId, menuWidth ) {

    this.numOfMenus++;
    if(this.numOfMenus == 1) {
    
    	this.rootMenuId = menuId;
    }
    this.menus[menuId] = new gigaContextMenu( this.gigaContextMenuCollectionId, menuId, menuWidth );
}


function _hideAll() {


	for(var m = 1; m <= this.numOfMenus; m++) {
	
		eval(this.gigaContextMenuCollectionId).menus[m].showMenu(false, null)
	
	}

	/*
	if(this.branchOpen) {
	   tmpSister = eval(this.gigaContextMenuCollectionId).menus[eval(this.gigaContextMenuCollectionId).menus[this.menuId].branchOpen];

	   tmpCousine = tmpSister;
	   
	   while(tmpCousine.branchOpen != false) {
	   
	      tmpCousine = eval(this.gigaContextMenuCollectionId).menus[tmpCousine.branchOpen]
		  
		  tmpCousine.showMenu(false, '');
	   } 
	 
	    tmpSister.showMenu(false, '');
	}
	*/
	
}



function gigaContextMenu( gigaContextMenuCollectionId, menuId, menuWidth ) {

	/* Properties */
	
	this.numberOfItems = 0;
	this.gigaContextMenuCollectionId = gigaContextMenuCollectionId;
	this.menuId = menuId;
	this.menuItems = new Array(); // _addGigaContextMenuItem() obj's
	this.menuItemsIdx = new Array(); // numeric index
	this.menuWidth = menuWidth;
	this.menuXpos = 0;
	this.menuYpos = 0;
	
	this.parentMenu = '';
	this.branchOpen = false;
	
	
    /* Methods */
	
	this.addItem = _addItem;
	this.drawMenu = _drawMenu;
	this.moveMenu = _moveMenu;
	this.showMenu = _showMenu;
	this.toggleMenu = _toggleMenu;
	this.hideChildren = _hideChildren;
	this.posContextMenu = _posContextMenu;
	
    /* Objects */

	this.warpLayer =  new warpLayer(0,0,menuWidth,2,'#BFBFBF'); // create a layer for menu content.
	
	/* Etc. */
	
	//writeLayer(this.warpLayer.name,'Hello Alien Buddy!');


}


function _posContextMenu() {

  var mX = mouseX;
  var mY = mouseY;
  eval(this.gigaContextMenuCollectionId).menus[ this.menuId ].moveMenu(mX - 0, mY - 0);
  eval(this.gigaContextMenuCollectionId).offsetX = mX - 0;
  eval(this.gigaContextMenuCollectionId).offsetY = mY - 0;
 
}


function _showMenu(doShow, parentMenu) {
		
	if(parentMenu == null) {
		
		 this.posContextMenu();
		 this.hideChildren();
 	} 
	
	 if((parentMenu != null) && (doShow == true)) {

	 
	     	if(eval(this.gigaContextMenuCollectionId).menus[parentMenu].branchOpen == false) {
	 	 
		    this.parentMenu = parentMenu; 
		    eval(this.gigaContextMenuCollectionId).menus[parentMenu].branchOpen = this.menuId;
		 }
		 else {
		  doShow = false;
		 }
	 }
	 else {
	 	if((parentMenu != null) && (parentMenu != '')) { 
	 	 	eval(this.gigaContextMenuCollectionId).menus[parentMenu].branchOpen = false;
		}
	 	this.parentMenu = '';
	 }
	 
	 this.warpLayer.display(doShow);
}


function _toggleMenu(parentMenu) {

	 var showConfirm = false;
	 var tmpSister;
	 var tmpCousine;

	 if((parentMenu != null) && (this.warpLayer.visible() == false)) {
	 
	    this.parentMenu = parentMenu; 
	 
	     	if(eval(this.gigaContextMenuCollectionId).menus[parentMenu].branchOpen != false) {

			   tmpSister = eval(this.gigaContextMenuCollectionId).menus[eval(this.gigaContextMenuCollectionId).menus[parentMenu].branchOpen];
			   tmpCousine = tmpSister;
			   
			   while(tmpCousine.branchOpen != false) {
			   
			      tmpCousine = eval(this.gigaContextMenuCollectionId).menus[tmpCousine.branchOpen]
				  
				  tmpCousine.showMenu(false, '');
			   } 
			 
			   tmpSister.showMenu(false, '');
	

			   eval(this.gigaContextMenuCollectionId).menus[parentMenu].branchOpen = this.menuId;
			   showConfirm = true;
		 }
	     
		 if(eval(this.gigaContextMenuCollectionId).menus[parentMenu].branchOpen == false) {
	 	 
		    eval(this.gigaContextMenuCollectionId).menus[parentMenu].branchOpen = this.menuId;
			showConfirm = true;
		 }

	 }
	 else {

		  if(this.branchOpen == false) {
		  
	 	  	  if((parentMenu != null)) { 
			  
	 	 	       eval(this.gigaContextMenuCollectionId).menus[parentMenu].branchOpen = false;
       	   		   showConfirm = true;
		      }
	 	      this.parentMenu = '';
		  }

	 }
     
     
     	 if(parentMenu == null) {

		showConfirm = true;
		this.posContextMenu();

	 }
	 else {



		
	//	var xOffset = eval(this.gigaContextMenuCollectionId).menus[ parentMenu ].menuXpos;
	//	var yOffset = eval(this.gigaContextMenuCollectionId).menus[ parentMenu ].menuYpos;
	//alert(xOffset + '-' + yOffset)	 
		 
		 
		 
		//eval(this.gigaContextMenuCollectionId).menus[ this.menuId ].moveMenu(,);
	   /*
	   
		this.menuXpos = eval(this.gigaContextMenuCollectionId).menus[this.menuId].menuXpos + (eval(this.gigaContextMenuCollectionId).menus[this.menuId].menuWidth-2) + eval(this.gigaContextMenuCollectionId).menus[ this.parentMenu ].menuXpos;
		this.menuYpos = eval(this.gigaContextMenuCollectionId).menus[this.menuId].menuYpos  + eval(this.gigaContextMenuCollectionId).menus[ this.parentMenu ].menuYpos + this.itemYpos;
			   
	   */
	   
	   
		this.warpLayer.move(this.menuXpos + eval(this.gigaContextMenuCollectionId).offsetX, this.menuYpos + eval(this.gigaContextMenuCollectionId).offsetY);


	 
	 }
     
	if(showConfirm) {
	      this.warpLayer.display(!this.warpLayer.visible());
     	}
}


function _hideChildren() {
	
	if(this.branchOpen) {
	   tmpSister = eval(this.gigaContextMenuCollectionId).menus[eval(this.gigaContextMenuCollectionId).menus[this.menuId].branchOpen];

	   tmpCousine = tmpSister;
	   
	   while(tmpCousine.branchOpen != false) {
	   
	      tmpCousine = eval(this.gigaContextMenuCollectionId).menus[tmpCousine.branchOpen]
		  
		  tmpCousine.showMenu(false, '');
	   } 
	 
	    tmpSister.showMenu(false, '');
	}
}


function _moveMenu (x,y) {

	this.menuXpos = x;
	this.menuYpos = y;
	this.warpLayer.move(x,y);
}


function _drawMenu() {

    var htmlOut = '';
    var tmpCnt = '';
	var tmpItem; 
	var branchMenuId;
	var branchIcon = '';
	var tmpVSpace = '';
	var skipLineBreak = false;
	var itemYpos = 0;
	var itemXpos = 0;
	
	
	
	for(var i = 0; i < this.numberOfItems; i++) {
		
		tmpItem = this.menuItems[this.menuItemsIdx[i]];
	
		if((tmpItem.itemHref != '') && (tmpItem.ghosted == false)) {
			
			if(tmpItem.branch == true) {
			
				branchMenuId = tmpItem.itemHref;
				branchIcon = '<img src="../pics/icon_context_branch2.gif" width="13" height="7">';
				tmpItem.itemHref = 'javascript:'
				//skipLineBreak = true;
				
		                itemYpos = (i * 15);
		                
		                this.menuItems[this.menuItemsIdx[i]].itemYpos = itemYpos;

		                itemXpos = -2;
		
		 		eval(this.gigaContextMenuCollectionId).menus[branchMenuId].menuXpos = eval(this.gigaContextMenuCollectionId).menus[this.menuId].menuXpos + eval(this.gigaContextMenuCollectionId).menus[this.menuId].menuWidth + itemXpos;
 		 		eval(this.gigaContextMenuCollectionId).menus[branchMenuId].menuYpos = eval(this.gigaContextMenuCollectionId).menus[this.menuId].menuYpos + itemYpos;
				
				eval(this.gigaContextMenuCollectionId).menus[branchMenuId].warpLayer.move(eval(this.gigaContextMenuCollectionId).menus[this.menuId].menuXpos+eval(this.gigaContextMenuCollectionId).menus[this.menuId].menuWidth+ itemXpos, (eval(this.gigaContextMenuCollectionId).menus[this.menuId].menuYpos) + itemYpos);
				
			
				tmpItem.itemHref += this.gigaContextMenuCollectionId + ".menus['" + branchMenuId + "'].toggleMenu('" + this.menuId + "');"
				
				tmpCnt = addTable(addTR(addTD('<img src="../pics/sitedot.gif" width="10" height="1">' + addLink(tmpItem.itemHref, tmpItem.itemCnt,'class=menuItem')) + addTD(branchIcon,'align="right"')),'border="0" width="100%" cellpadding="0" cellspacing="0"')
					
			}
			else {
			
				tmpCnt = '<img src="../pics/sitedot.gif" width="10" height="1">' + addLink(tmpItem.itemHref, tmpItem.itemCnt,'class=menuItem onclick="' + this.gigaContextMenuCollectionId + '.menus[\'' + eval(this.gigaContextMenuCollectionId).rootMenuId + '\'].hideChildren();' + this.gigaContextMenuCollectionId + '.menus[\'' + eval(this.gigaContextMenuCollectionId).rootMenuId + '\'].showMenu(false)"') + '<br>';
			}
			
		}
		else {

			if(tmpItem.itemCnt == '---') {
			
				tmpCnt = '<img src="../pics/context_sep.gif" width="' + (this.menuWidth-6) + '" height="2" vspace="1"><br>'
			}
			else {
			
			    tmpCnt = '<img src="../pics/sitedot.gif" width="10" height="1"><i>' + addSpan(tmpItem.itemCnt, 'class=ghostItem') + '</i><br>';	
			}
		}
		
		if((i != (this.numberOfItems-1)) && (!skipLineBreak)) {
		
			 tmpVSpace = '<img src="../pics/sitedot.gif" width="1" height="5" alt="" border="0"><br>';
		}
		else {
		
		 tmpVSpace = '';
		}
	    
		skipLineBreak = false;
		htmlOut += tmpCnt + tmpVSpace;	
	}
	
	writeLayer(this.warpLayer.name, htmlOut);
	
}


function _addItem( itemId, itemCnt, itemHref ) {
/*

// auto width - BUGGY ;(
       var tmpItemLength;

	for(var i = 0; i < this.numberOfItems+1; i++) {
	

	
		tmpItemLength = itemCnt.length * 9;
	//	if(this.menuId == 'm006')
	//	alert(itemCnt + '-' + itemCnt.length)
		if(tmpItemLength > this.menuWidth) {
			this.menuWidth = (tmpItemLength);	
		}
	}	
*/


	this.menuItems[itemId] = new gigaContextMenuItem( this.gigaContextMenuCollectionId, this.menuId, itemId, itemCnt, itemHref );
	this.menuItemsIdx[this.numberOfItems++] = itemId;
	
}



function gigaContextMenuItem(gigaContextMenuCollectionId, menuId, itemId, itemCnt, itemHref ) {
    
	if(itemHref.charAt(0) == '@') {
		 
		 this.itemHref = itemHref.substr(1, itemHref.length);
		 this.branch = true; 
	}
	else {
	
		this.itemHref = itemHref;
        this.branch = false;
	}
	
	this.itemCnt = itemCnt;

	this.ghosted = false;
	
	this.itemYpos = 0;
	
}






/* Events and handlers for mouse */

var mouseX, mouseY
	
function mouseMovementIE() {

	mouseX = event.x;
	mouseY = event.y;
}

function mouseMovementNav4(e) {

	mouseX = e.pageX;
	mouseY = e.pageY;
}


if(isNav) {
	document.captureEvents(Event.MOUSEMOVE);
	document.onmousemove = mouseMovementNav4;
}
else {

	document.onmousemove = mouseMovementIE;
}
