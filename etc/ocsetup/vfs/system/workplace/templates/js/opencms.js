//------------------------------------------
// global variables und functions
// m.schleich 03.01.2000
//------------------------------------------
var admincontenturl="administration_content_top.html";

// Formularfelder (Textfelder) auf Inhalt überprüfen
function check_textfeld(formular, feld)
{
if(document[formular][feld].value=="")
	{
		alert('Bitte geben Sie einen Namen an!');
		document[formular][feld].focus();
		return false;
	}
return true;
}

//------------------------------------------------------------------------------------
// special functions
// m.schleich 05.01.1999
//------------------------------------------------------------------------------------
function default_dateiangaben() //to set defaults in user sets for filelist
{
	document.EINST_EXPL.DANGABEN[0].checked=true;
	document.EINST_EXPL.DANGABEN[1].checked=false;
	document.EINST_EXPL.DANGABEN[2].checked=true;
	document.EINST_EXPL.DANGABEN[3].checked=false;
	document.EINST_EXPL.DANGABEN[4].checked=true;
	document.EINST_EXPL.DANGABEN[5].checked=false;
	document.EINST_EXPL.DANGABEN[6].checked=true;
	document.EINST_EXPL.DANGABEN[7].checked=false;
	document.EINST_EXPL.DANGABEN[8].checked=false;
}

//------------------------------------------------------------------------------------
// mouseover- and clicked-effects
// m.schleich 20.12.1999
//------------------------------------------------------------------------------------

var imgEndOn = "_on.gif";
var imgEndOff = "_off.gif";
var imgEndPush = "_push.gif";
var imgEndInakitv = "_in.gif";
//var pfad="../pics/";


function ChOn(imgID) //changes a imageobject on mouseover
{
document.images[imgID].src = pfad + imgID + imgEndOn;
}

function ChOff(imgID) //changes a imageobject on mouseout
{
document.images[imgID].src = pfad + imgID + imgEndOff;
}

function ChPush(imgID) //changes a imageobject on clicked or mousedown
{
document.images[imgID].src = pfad + imgID + imgEndPush;
}


function ChImg(imgID,src)
{
document.images[imgID].src = src;
}


//------------------------------------------------------------------------------------
// updates a frame
// m.schleich 01.02.2000
//------------------------------------------------------------------------------------
function updateFrame(which, frameurl)
{
	eval('window.top.'+which+'.location.href="'+frameurl+'"');

}

//------------------------------------------------------------------------------------
// updates a variable
// m.schleich 07.02.2000
//------------------------------------------------------------------------------------
function updateVarible(whichframe, whichvar, value)
{
	eval('window.top.'+whichframe+'.'+whichvar+'='+value);

}

//------------------------------------------------------------------------------------
// open new window
// m.schleich 20.12.1999
//------------------------------------------------------------------------------------

var workplace;

function openwin(url, name, w, h) //opens a new window with parameter URL, Windowname (free choosable), width and height
{
    if (url != '#') {
		if(w==0) w=screen.availWidth-20;
		if(h==0) h=screen.availHeight-100;

		workplace = window.open(url,name, 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,width='+w+',height='+h);
		workplace.moveTo(0,0);
		if(workplace != null)
		   {
		      if (workplace.opener == null)
		      {
		         workplace.opener = self;
		      }
	   		}
		workplace.focus();
     }

}

//------------------------------------------------------------------------------------
// open new window
// m.schleich 26.01.2000
//------------------------------------------------------------------------------------

var smallwindow;

function opensmallwin(url, name, w, h) //opens a new window with parameter URL, Windowname (free choosable), width and height
{
	smallwindow = window.open(url, name, 'toolbar=no,location=no,directories=no,status=no,menubar=0,scrollbars=no,resizable=no,width='+w+',height='+h);
	if(smallwindow != null)
	   {
	      if (smallwindow.opener == null)
	      {
	         smallwindow.opener = self;
	      }
   		}
	//smallwindow.focus();

}

//------------------------------------------------------------------------------------
// which radiobutton is checked
// m.schleich 11.01.1999
// 
// parameters:
// formname: name of the form
// radionae: name of the radiobottuns group
// i: how many radiobuttons are there
// return: the value of the checked radiobutton
//------------------------------------------------------------------------------------

function check_radio(formname, radioname, i)
{
	for(j=0;j<i;j++)
	{
		if(document[formname][radioname][j].checked)
		{
			return document[formname][radioname][j].value;
		}

	}

}

//------------------------------------------------------------------------------------
// ceck date
// 
// author:	Michaela Schleich
// company:	mindfact interaktive medien ag
// date:	01.11.1999
// update:	14.01.2000
//
// input date (tt.mm.jjjj)
//
// returns FALSE if date is not a valid date
// returns TRUE if date valid
//------------------------------------------------------------------------------------
function checkDate(eDat)
{
// get date
	schaltjahr=false;
	aktDat = new Date;
	aktTag= aktDat.getDate();
	aktMonat= aktDat.getMonth()+1;
	aktJahr= aktDat.getFullYear();
	

// split the userdate
	eTag = eDat.substring(0,2);
	eMonat = eDat.substring(3,5);
	eJahr = eDat.substring(6,10);
	

	if(eJahr<aktJahr)
	{
		alert("Die Fälligkeit liegt in der Vergangenheit");
		return false;
	}
	else
	{
		if(eMonat<aktMonat)
		{
			alert("Die Fälligkeit liegt in der Vergangenheit");
			return false;
		}
		else
		{
			if(eTag<aktTag)
			{
				alert("Die Fälligkeit liegt in der Vergangenheit");
				return false;
			}
		}
	}
	
	
	if(eDat.length != 10 || isNaN(eTag) || isNaN(eMonat) || isNaN(eJahr))
		{
		alert("Bitte das Datum in Form von tt.mm.jjjj eingeben.");
		return false;
		}
	else
		{
		if(eMonat < 01 || eMonat > 12)
		{
		alert("Bitte überprüfen Sie den eingegeben Monat.");
		return false;
		}
		else
			{
			if(eTag > 31 || eTag<=00)
			{
			alert("Bitte ueberprüfen Sie den Tag des Datums.");
			return false;
			}
			else
			{

// if must be split, to long for browser interpreter
				if(((eTag == 31) && (eMonat == 02)) || ((eTag == 31) && (eMonat == 04)) || ((eTag == 31) && (eMonat == 06)))
				{
				alert('Bitte ueberpruefen Sie den Tag des Datums.');
				return false;
				}
				if((eTag == 30 && eMonat == 02) || (eTag == 31 && eMonat==9) || (eTag==31 && eMonat==11))
				{
				alert('Bitte ueberpruefen Sie den Tag des Datums.');
				return false;
				}
				else
				{
				if(eTag==29 && eMonat == 02)
					{
					sj = eJahr%4;
					if(sj==0)
					schaltjahr=true;

					sj = eJahr%100;
					if(sj==0)
					schaltjahr=false;
		
					sj = eJahr%400;
					if(sj==0)
					schaltjahr=true;

					if(!schaltjahr)
					{
					alert(eJahr + " ist kein Schaltjahr.");
					return false;
					}
					}
				}
			}
		}		
	}
	return true;
}
