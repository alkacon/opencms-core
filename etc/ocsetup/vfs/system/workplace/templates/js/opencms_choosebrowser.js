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

