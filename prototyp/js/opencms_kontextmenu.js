/*
 * File   : $Source: /alkacon/cvs/opencms/prototyp/js/Attic/opencms_kontextmenu.js,v $
 * Date   : $Date: 2000/09/21 15:32:43 $
 * Version: $Revision: 1.11 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

// mouseevent
document.onclick = mouseClicked;
if(ns){document.captureEvents(Event.CLICK);}

//------------------------------------------
// global variables and global functions
// m.schleich 03.01.2000
//------------------------------------------
var shown = false;
var letztelyr='null';
var x=0;
var y=0;

// which operation is clicked
//==FILES=================================
var DO_LOCK=1;
var DO_LOCKUSER=2;
var DO_UNLOCK=3;

var DO_COPY=4;
var DO_MOVE=5;
var DO_RENAME=6;
var DO_DELETE=7;
var DO_REPLACE=18;

var DO_CHOWN=8;
var DO_CHGRP=9;
var DO_CHMOD=10;
var DO_CHTYPE=11;
var DO_META=12;
var DO_METAVIEW=13;

var DO_EDIT=14;
var DO_SeiteEDIT=15;
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
var DO_LOCKF=34;
var DO_LOCKUSERF=35;
var DO_UNLOCKF=36;
var DO_RENFOLDER=37;
var DO_DELFOLDER=38;

var DO_HIST=40;
var DO_HISTVIEW=41;
var DO_EDITNEWS=42;
var DO_DELETENEWS=43;

var DO_RECOVER=50;
var DO_TAKEOVER=51;


//==PROJECTS==============================
var DO_PUNLOCK=1;
var DO_PONLINELOCK=2;
var DO_PONLINE=3;
var DO_PDELETE=4;

var DO_PTASK=10;
var DO_TASK=99;

var DO_PREACT=20;
//==ADMINISTRATION/MODULES=================================
var DO_DELMOD=60;
var DO_PREFMOD=61;
var DO_EXPMOD=62;
//==TASKS=================================
var DO_TASKREQUEST= 70;
var DO_TASKACCEPT=  71;
var DO_TASKCOMMENT= 72;
var DO_TASKOK=      73;
var DO_TASKDELETE=  74;
var DO_TASKFORWARD= 75;
var DO_TASKDATE=    76;
var DO_TASKPRIO=    77;
var DO_TASKTAKEOVER=78;
//========================================


// mousekoordinates on click
function mouseClicked(e)
{
		if (ie) {x=event.x; y=event.y;}
		if (ns) {x=e.x; y=e.y;}
		hidemenu(letztelyr);
}

//----------------------------------------
// function to display the filecontextmenu
// parameters:
// welche: layer name
// id: each link for layer must have a unic id number
// m.schleich 03.12.1999
//----------------------------------------
function showkontext(welche, id)
{
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
			if (ns) top.location.href='edit_text_ns.html';
			else if (ie) top.location.href='edit_text_ie.html';
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
		
		case 18:
		{
			location.href='administration_content_pic_replace.html';
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
		case 34:
				{
					location.href='explorer_files_lockf.html';
					break;
		}
		case 35:
				{
					location.href='explorer_files_lockchangef.html';
					break;
		}
		case 36:
				{
					location.href='explorer_files_unlockf.html';
					break;
		}
		case 37:
		{
			location.href='news_content_folder_rename.html';
			break;
		}
		case 38:
		{
			location.href='news_content_folder_delete.html';
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
		case 42:
		{
			location.href='news_content_nachricht_edit.html';
			break;
		}
		case 43:
		{
			location.href='news_content_nachricht_delete.html';
			break;
		}			
		
		
		case 50:
		{
			location.href='explorer_files_recover.html';
			break;
		}
		case 51:
		{
			location.href='explorer_files_takeover.html';
			break;
		}
		case 60:
		{
			location.href='administration_content_module_overwrite.html';
			break;
		}
		case 61:
		{
			location.href='administration_content_modules_proper.html';
			break;
		}
		case 62:
		{
			location.href='administration_content_modexp_blank.html';
			break;
		}
		
		case 70:
		{
			location.href='tasks_content_dialogquestion.html';
			break;
		}
		case 71:
		{
			location.href='tasks_content_dialogaccept.html';
			break;
		}
		case 72:
		{
			location.href='tasks_content_dialogcomment.html';
			break;
		}
		case 73:
		{
			location.href='tasks_content_dialogok.html';
			break;
		}
		case 74:
		{
			location.href='tasks_content_dialogdelete.html';
			break;
		}
		case 75:
		{
			location.href='tasks_content_dialoggive.html';
			break;
		}
		case 76:
		{
			location.href='tasks_content_dialogdate.html';
			break;
		}
		case 77:
		{
			location.href='tasks_content_dialogprio.html';
			break;
		}
		case 78:
		{
			location.href='tasks_content_dialogtake.html';
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
			location.href='administration_content_publish_project.html';
			break;
		}
		case 4:
		{
			location.href='administration_content_delete_project.html';
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
