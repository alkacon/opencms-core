// perform a simple browser check
var ns = (document.layers)? true:false;

// prefixes for the images
var imgEndOn = "_on.gif";
var imgEndOff = "_off.gif";
var imgEndPush = "_push.gif";
var imgEndInaktiv = "_in.gif";

// changes a imageobject on mouseover 
function ChOn(imgID, div) {
if (ns && div)
	document[div].document[imgID].src = pfad + imgID + imgEndOn;
else
	document.images[imgID].src = pfad + imgID + imgEndOn;
}

// changes a imageobject on mouseout
function ChOff(imgID, div) {
if (ns && div)
	document[div].document[imgID].src = pfad + imgID + imgEndOff;
else
	document.images[imgID].src = pfad + imgID + imgEndOff;
}

// changes a imageobject on clicked or mousedown 
function ChPush(imgID, div) {
if (ns && div)
	document[div].document[imgID].src = pfad + imgID + imgEndPush;
else
	document.images[imgID].src = pfad + imgID + imgEndPush;
}

// changes a imageobject on clicked or mousedown
function ChIn(imgID, div)  {
if (ns && div)
	document[div].document[imgID].src = pfad + imgID + imgEndInaktiv;
else
	document.images[imgID].src = pfad + imgID + imgEndInaktiv;
}

// changes an images src property
function ChImg(imgID, src) {
	document.images[imgID].src = src;
}