
/* globals */

var isNav, isNav6, isIE, layr_fix, style_fix, isDynamic;

var layersOpenedByIE = 0; // used with IE to give it unique id just as netscape does...

// var defLayerColor = '#BFBFBF';

/* end globals */

if (parseInt(navigator.appVersion) >= 4) {
	isDynamic = true; // a 4.0+ browser... (note: can be other than ie & ns)
	
	if (navigator.appName == "Netscape") {
		if (parseInt(navigator.appVersion) == 4) {
		    isNav = true;
		    layr_fix = 'layers.';
		    style_fix = '';
		}
		else {
		    isNav6 = true;
	 	    layr_fix = 'getElementById("';
		  // document.getElementById("pop02").style.left=	
		    style_fix = '").style';
		}
	}
	
	if((navigator.appName == "Microsoft Internet Explorer")) {  // not Netscape, so we ASSUME it's IE.
		isIE = true;
		layr_fix = 'all.';
		style_fix = '.style';
	}
}


function writeLayer(destLayer, HTML, tableBorder) {

	if((tableBorder == null)) {
	
			tableBorder = 2;
	}

	if(isIE) {
		eval('document.all.' + destLayer + '.innerHTML = addTable(addTR(addTD(HTML,"NOWRAP class=displayField valign=top")),"width=100% border=" + tableBorder + " cellpadding=2 cellspacing=0 class=contextMenu bordercolorlight=#7F7F7F bordercolordark=#ffffff")');
	}
	
	if(isNav) {
		eval('document.layers.' + destLayer + '.document.open()');
		eval('document.layers.' + destLayer + '.document.writeln(addTable(addTR(addTD(HTML,"class=displayField valign=top")),"width=100% border=" + tableBorder + " cellpadding=2 cellspacing=1"))');
		eval('document.layers.' + destLayer + '.document.close()');
	}

}

var layerStorage = '';

function warpLayer(x,y,w,h, defLayerColor) { // constructor for a universal Layer Object Model.

	if((defLayerColor == null) || (defLayerColor == '')) {
	
		defLayerColor = '#ffffff';
	}

// properties
	
	if(isNav) {
		var a = new Layer(w, window); // IMPORTANT!! I just used >1 hour locating a bug, it was fixed by setting the Layer Parent to Window. Apparantly it's not that optional ;)
		a.hidden = true;
		a.top = y;
		a.left = x;
		a.bgColor  = defLayerColor
		this.name = a.name;
	}
	
	if(isIE) {

	

		document.all.dpIE.innerHTML += '<span id="ieLayer' + layersOpenedByIE + '" style="position : absolute; z-index : 1; top: ' + y + 'px; left: ' + x + 'px;background-color: ' + defLayerColor + '; width: ' + w + '; height: ' + h + '"></span>';


		this.name = 'ieLayer' + layersOpenedByIE;
		setStyle(this.name, 'visibility', 'hidden');
		layersOpenedByIE++;

        }


	if(isNav6) {

	
	
		node=document.getElementById("dpIE");
		
			beforediv=document.createElement("DIV");
			
			newText=document.createTextNode("What's New");
			beforediv.appendChild(newText);
			
			
			/*
	

		document.getElementById("dpIE").innerHTML = '<span id="ieLayer' + layersOpenedByIE + '" style="position : absolute; z-index : 1; top: ' + y + 'px; left: ' + x + 'px;background-color: #ffffff; width: ' + w + '; height: ' + h + '"></span>';
                         getElementById("pop02").style.

		this.name = 'ieLayer' + layersOpenedByIE;
		setStyle(this.name, 'visibility', 'hidden');
		layersOpenedByIE++;
*/
	}

	
	
	this.height = function() {
	
		if(isIE) {
	
			return eval('document.' + layr_fix + this.name + style_fix + '.pixelHeight');
		}
		if(isNav) {

			return getStyle(this.name,'clip.bottom');
		}
	}
	
	
	this.visible = function() { // returns true if the layer is visible (ie. not hidden).
	
		var winState = getStyle(this.name,'visibility');

		if((winState == 'hide') || (winState == 'hidden')) {
			return false;
		}
	
		if((winState == 'show') || (winState == 'visible')) {
			return true;
		}
	
	}
	
// methods

	this.display = function(winState) { // an easy way to hide or show a layer.
	
		if((winState == false)) {
			void setStyle(this.name,'visibility', 'hidden');		
		}
	
		if((winState == true)) {
			void setStyle(this.name,'visibility', 'visible');
		}
	}
	

	this.deltaMove = function(deltaX, deltaY) { // deltaMove
	

		if(isNav) {
			document.layers[this.name].moveBy(deltaX,deltaY);
		}
		
		if(isIE) {
	
			eval('document.' + layr_fix + this.name + style_fix + '.' + 'pixelLeft' + ' += ' + deltaX);
			eval('document.' + layr_fix + this.name + style_fix + '.' + 'pixelTop' + ' += ' + deltaY);
		}
		
	}

	
	this.move = function(x, y) { // pixelMove
	

		if(isNav) {
			document.layers[this.name].moveTo(x,y);
		}
		
		if(isIE) {
	
			eval('document.' + layr_fix + this.name + style_fix + '.' + 'pixelLeft' + ' = ' + x);
			eval('document.' + layr_fix + this.name + style_fix + '.' + 'pixelTop' + ' = ' + y);
		}
		
	}
	
}


function setStyle(layer1, style1, value1) {

	return eval('document.' + layr_fix + layer1 + style_fix + '.' + style1 + '= "' + value1 + '"');
}


function getStyle(layer1, style1) {

	return eval('document.' + layr_fix + layer1 + style_fix + '.' + style1);
}