// listen the event mous click
document.onclick = mouseGeklickt;

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
	document.captureEvents(Event.CLICK);
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

//------------------------------------------
// global variables and global functions
// m.schleich 03.01.2000
//------------------------------------------
var shown = false;
var letztelyr='null';
var x=0;
var y=0;

// parameter for the kontextmenu
var kontextparam;

// which operation is clicked
var DO_LOCK=1;
var DO_LOCKUSER=2;
var DO_UNLOCK=3;

var DO_COPY=4;
var DO_MOVE=5;
var DO_RENAME=6;
var DO_DELETE=7;

var DO_CHOWN=8;
var DO_CHGRP=9;
var DO_CHMOD=10;
var DO_CHTYPE=11;
var DO_META=12;
var DO_METAVIEW=13;

var DO_EDIT=14;
var DO_HTMLEDIT=15;
var DO_VIEW=16;
var DO_EXPORT=17;

var DO_TEMPLATEEDIT=12;
var DO_DATAEDIT=13;

var DO_EDITDOC=20;
var DO_ATE=21;
var DO_MAILEDIT=22;

var DO_COPYF=30;
var DO_MOVEF=31;
var DO_RENAMEF=32;
var DO_DELETEF=33;

var DO_HIST=40;
var DO_HISTVIEW=41;
//=======================================
var DO_PUNLOCK=1;
var DO_PONLINELOCK=2;
var DO_PONLINE=3;

var DO_PTASK=10;
var DO_TASK=99;

var DO_PREACT=20;


// mousekoordinates on click
function mouseGeklickt(e)
{
		if (ie) {x=event.x; y=event.y;}
		if (ns) {x=e.x; y=e.y;}
		hidemenu(letztelyr);
}

//----------------------------------------
// function to display the filecontextmenu
// parameters:
// welche: layer name
// parameter: parameter for menu
// id: each link for layer must have a unic id number
// m.schleich 03.12.1999
//----------------------------------------
function showkontext(welche, parameter, id)
{
	// set the kontextparameter
	kontextparam = parameter;
	
	if (!shown || id!=altid)
	{
		if(y >= (screen.availHeight/2))
		{
			if(ie)lyrheight=checklyrheight(welche);
			if(ns)lyrheight=document.layers[welche].clip.height;
			
			lyrheight='-'+lyrheight;
			eval(ypos_01+welche+ypos_02+'y'+yoffset+lyrheight);
		}
		else
		{
			lyrheight='-0';
		}
		eval(xpos_01+welche+xpos_02+'x'+xoffset);
		eval(ypos_01+welche+ypos_02+'y'+yoffset+lyrheight);
		
		hidemenu(letztelyr);
		eval(layerzeigen_01+welche+layerzeigen_02);
		
		shown = true;
	}
	else
	{
		hidemenu(letztelyr);
		shown = false;
	}
	letztelyr=welche;
	altid=id;
}


// hides the file context (layer)
function hidemenu(welche)
{
	if(welche!='null')
	{
		eval(layerverstecken_01+welche+layerverstecken_02);
		//shown=false;
	}
	else
	{
		return;
	}
}

function checklyrheight(welche)
{
	switch(welche)
	     {
	       case 'plain':
	       	{
	         return 220;
	         break;
	       	}
			case 'plainlock':
	     	{
	         return 220;
	         break;
	       	}
			case 'plainlockuser':
			{
			 return 220;
			 break;
	       	}
			case 'pic':
	     	{
	         return 220;
	         break;
	       	}
	       case 'online_p':
	       	{
	         return 81;
	         break;
	        }
			default:
			{
			 return 81;
			 break;
	        }
        }

}

function doAction(action) // which action on layer is clicked
{
	hidemenu(letztelyr);
	
	switch(action)
	{
		case 1:
		{
			location.href='explorer_files_lock.html';
			break;
		}
		case 2:
		{
			location.href='explorer_files_lockchange.html';
			break;
		}
		case 3:
		{
			location.href='explorer_files_unlock.html';
			break;
		}
		
		case 4:
		{
			location.href='explorer_files_copy.html';
			break;
		}
		case 5:
		{
			location.href='explorer_files_move.html';
			break;
		}
		case 6:
		{
			location.href='explorer_files_rename.html';
			break;
		}
		case 7:
		{
			location.href='explorer_files_delete.html';
			break;
		}
		
		case 8:
		{
			location.href='explorer_files_owner.html';
			break;
		}
		case 9:
		{
			location.href='explorer_files_group.html';
			break;
		}
		case 10:
		{
			location.href='explorer_files_right.html';
			break;
		}
		case 11:
		{
			location.href='explorer_files_type.html';
			break;
		}
		case 12:
		{
			location.href='explorer_files_meta.html';
			break;
		}
		case 13:
		{
			location.href='explorer_files_metashow.html';
			break;
		}
		
		case 14:
		{
			top.location.href='edit_text.html';
			break;
		}
		case 15:
		{
			top.location.href='edit_html.html';
			break;
		}
		case 16:
		{
			top.location.href='edit_view.html';
			break;
		}
		
		case 30:
		{
			location.href='explorer_files_copyf.html';
			break;
		}
		case 31:
		{
			location.href='explorer_files_movef.html';
			break;
		}
		case 32:
		{
			location.href='explorer_files_renamef.html';
			break;
		}
		case 33:
		{
			location.href='explorer_files_deletef.html';
			break;
		}
		
		case 40:
		{
			location.href='explorer_files_history.html';
			break;
		}
		case 41:
		{
			location.href='explorer_files_historyshow.html';
			break;
		}
	}
}

function doPAction(action) // which action on layer Project is clicked
{
	hidemenu(letztelyr);
	
	switch(action)
	{
		case 1:
		{
			location.href='administration_content_lockchange.html';
			break;
		}
		case 2:
		{
			confirm('Sind Sie sicher, dass das Projekt veröffentlicht werden soll? \nEs sind noch Dateien in Bearbeitung.');
			break;
		}
		case 3:
		{
			location.href='administration_content_publish_project.html?' + kontextparam;
			break;
		}
		case 10:
		{
			location.href='tasks_content_new_forproject.html';
			break;
		}
		case 20:
		{
			location.href='administration_content_projecthistory_react.html';
			break;
		}
		case 99:
		{
			location.href='tasks_content_new_forfile.html';
			break;
		}
	}
}
