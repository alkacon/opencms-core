/*
 * xbPositionableElement - 
 * use DHMTL to position an element displaces (offsetX, offsetY)
 * from a geometric position in the viewport.
 *
 * Modified by Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 */

function xbPositionableElement(id, sideX, sideY, offsetX, offsetY, refElement, visible)
{
  this.id              = id;
  this.name            = 'xbPositionableElement_' + (++xbPositionableElement._name);
  this.runId           = null;  
  this.refreshInterval = 100;
  this.div             = null;
  
  if (typeof(sideX) != 'string')
  {
    sideX = 'left'
  }
  else
  {
    sideX = sideX.toLowerCase();
  }

  if (typeof(sideY) != 'string')
  {
    sideY = 'top'
  }
  else
  {
    sideY = sideY.toLowerCase();
  }

  if (typeof(offsetX) != 'number')
  {
    offsetX = 0;
  }

  if (typeof(offsetY) != 'number')
  {
    offsetY = 0;
  }
  
  if (typeof(refElement) != 'undefined') {
      this.refElement = refElement;
  } else {
      this.refElement = null;
  }
  
  if (typeof(visible) != 'undefined') {
      this.visible = visible;
  } else {
      this.visible = true;
  }
  
  this.sideX   = sideX;
  this.sideY   = sideY;
  this.offsetX = offsetX;
  this.offsetY = offsetY;
  
  this.refX = 0;
  this.refY = 0;

  window[this.name] = this;
}  

xbPositionableElement._name = -1;

xbPositionableElement.prototype.init = function ()
{
  // since init() should be called after the document has loaded, we can finally assign element
  if (!this.div)
  {
    if (document.getElementById)
    {
      this.div = document.getElementById(this.id);
      this.styleObj = this.div.style;   
    }
    else if (document.layers)
    {
      this.div = document.layers[this.id];
      this.styleObj = this.div;    
    }
    else if (document.all)
    {
      this.div = document.all[this.id];
      this.styleObj = this.div.style;         
    }

    this.width = this._getInnerSize("width");
  } 
  
  this._updatePosition();
}

xbPositionableElement.prototype.start = function ()
{
  this.stop();  
  this.init();
  this.runId = setTimeout(this.name + '.start()', this.refreshInterval);  
};

xbPositionableElement.prototype.stop = function ()
{
  if (this.runId)
    clearTimeout(this.runId);
    
  this.runId = null;
};

xbPositionableElement.prototype.show = function ()
{
    this.visible = true;
    this.start();    
    if (document.getElementById || document.all) {
        this.styleObj.visibility = "visible";
    } else if (document.layers) {
        this.styleObj.visibility = "show";
    }        
};

xbPositionableElement.prototype.hide = function ()
{
    this.visible = false;
    this.init();
    if (document.getElementById || document.all) {
        this.styleObj.visibility = "hidden";
    } else if (document.layers) {
        this.styleObj.visibility = "hide";
    }    
};

xbPositionableElement.prototype.checkPointer = function ()
{
    if (! this.visible || ! this.div) return false;    
    var left = parseInt( this.styleObj.left );
    var top = parseInt( this.styleObj.top );
    var width = parseInt( this._getInnerSize('width') );
    var height = parseInt( this._getInnerSize('height') );
    var onElement =  (mousePos[0] > left) && (mousePos[0] < (left + width)) && (mousePos[1] > top) && (mousePos[1] < (top + height));    
    // if (onElement) window.status = "Menu position - Left: " + left + " Top: " + top + " Width: " + width + " Height: " + height;
    return onElement;
};

xbPositionableElement.prototype._getInnerSize = function (propName)
{
  var val = 0;

  if (document.layers)
  {
    // navigator 4
    val = this.div.document[propName];
  }
  else if (typeof(this.div.style[propName]) == 'number')
  {
    // opera
    // bug in Opera 6 width/offsetWidth. Use clientWidth
    if (propName == 'width' && typeof(this.div.clientWidth) == 'number')
      val = this.div.clientWidth;
    else
      val =  this.styleObj[propName];
  }
  else
  {
    //mozilla and IE
    switch (propName)
    {
    case 'height':
       if (typeof(this.div.offsetHeight) == 'number')
         val =  this.div.offsetHeight;
       if (val == 0)
         val =  this.height;
       break;
 
    case 'width':
       if (typeof(this.div.offsetWidth) == 'number')
         val = this.div.offsetWidth;
       if (val == 0)
         val =  this.width;
       break;
    }
  }
  return val;
};

xbPositionableElement.prototype._getScrollOffset = function (propName)
{
  var rv = 0;

  if (document.body && typeof(document.body.scrollTop) == 'number')
  {
    rv = document.body[propName=='top'?'scrollTop':'scrollLeft'];
  }
  else if (typeof(window.pageYOffset) == 'number')
  {
    rv = window[propName=='top'?'pageYOffset':'pageXOffset']; 
  }

  return rv;
};

xbPositionableElement.prototype._updatePosition = function ()
{
  var windowHeight;
  var windowWidth;
  var x = 0;
  var y = 0;
  
  if (this.refElement != null) {
    if (document.getElementById)
    { 
      this.refX = getElementPos(document.getElementById(this.refElement), "Left");
      this.refY = getElementPos(document.getElementById(this.refElement), "Top"); 
    }
    else if (document.layers)
    {
      this.refX = document.layers[this.refElement].x;
      this.refY = document.layers[this.refElement].y;    
    }
    else if (document.all)
    { 
      this.refX = getElementPos(document.all[this.refElement], "Left");
      this.refY = getElementPos(document.all[this.refElement], "Top");       
    }
  }
  
  if (document.body && typeof(document.body.clientHeight) == 'number')
  {
    windowHeight = document.body.clientHeight;
    windowWidth  = document.body.clientWidth;
  }
  else if (typeof(window.innerHeight) == 'number')
  {
    windowHeight = window.innerHeight - 16;
    windowWidth  = window.innerWidth  - 16;  
  }

  // calculate the positions of the view port position
  switch (this.sideX)
  {
    case 'left':
      x = this._getScrollOffset('left');
      break;

    case 'center':
      x = (windowWidth / 2) - (this._getInnerSize('width')/2) + this._getScrollOffset('left');
      break;
	  
    case 'right':
      x = windowWidth - this._getInnerSize('width') + this._getScrollOffset('left');
      break;
      
    case 'abs':
      x = this.refX;
      break;      
  }

  switch (this.sideY)
  {
    case 'top':
      y = this._getScrollOffset('top');
      break;

    case 'center':
      y = (windowHeight / 2) - (this._getInnerSize('height') / 2) + this._getScrollOffset('top');
      break; 
	  
    case 'bottom':
      y = windowHeight - this._getInnerSize('height') + this._getScrollOffset('top');
      break;
      
    case 'abs':
      y = this.refY;
      break;      
  }
  
  this.styleObj.left = x + this.offsetX;
  this.styleObj.top  = calculateOffsetY(document.getElementById(this.refElement), y, this.offsetY);

};

// ------------------------------------------- Additional functions

function calculateOffsetY(element, y, offsetY) {
	if (typeof(element.height) != "number") {
	// no image element, add given offset
  	return y + offsetY;
  } else {
  	// image element, do not use given offset
  	return y;
  }
};

function getElementPos( element, offsetPos ) {
    var pos = 0;
    if (typeof(element.height) == "number" && offsetPos == "Top") {
    	// image element, add element height
    	pos += element.height;
    }
    while (element != null) {
        pos += element[ "offset" + offsetPos ];
        element = element.offsetParent;
    }
    return pos;
};

function moveMouse(e) {
    mousePos = null;
    if (document.getElementById || document.all) {
        if (document.all) e = event;
        mousePos = [e.clientX + document.body.scrollLeft, e.clientY + document.body.scrollTop];
        e.cancelBubble = true;
    } else if (document.layers) {
        mousePos = [e.pageX, e.pageY];
    }
    // window.status = 'Mouse position - X:' + mousePos[0] + ' Y: ' + mousePos[1];
    return mousePos;
};