/*
 * File   : $Source: /alkacon/cvs/opencms/etc/ocsetup/vfs/system/workplace/templates/js/Attic/opencms_editbrowser.js,v $
 * Date   : $Date: 2001/01/11 13:32:30 $
 * Version: $Revision: 1.3 $
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

// ===============================================
// JAVASCRIPT-FUNCTIONS OPENCMS
//
// Invokes servlet calls in Picture and Download Browser:
// based on script from old cms version
//
// authors:			m.schreiber, m.stanke
// company:			mindfact interaktive medien ag
// date:			08.02.2000
// update:		
// update author:	
// ===============================================

// variables for customization: download browser or picture browser? (default:picture browser)
var ctrl='picctrl';
var list='piclist';
var browser='picturebrowser';
var uploadurl = "administration_content_picgallery_upload.html?type=pic&action=upload&lasturl=piclist.html&folder=";

function updateSelection()
{ 
	if(document.form1.galerie.options[document.form1.galerie.selectedIndex].value)
	{
		top.selectedGallery = document.form1.galerie.selectedIndex;  
		var folder= document.form1.galerie.options[document.form1.galerie.selectedIndex].value;
		parent.frames[0].location.href= ctrl + '.html?folder=' + folder;
		parent.frames[1].location.href= list + '.html?folder=' + folder;
		document.form1.FilterInput.value="";
	}
}
  
var DO_NEXT=1;
var DO_PREV=2;
var DO_SEARCH=3;
var DO_UPLOAD=4;


<!-- The update flag is set by the upload dialog. When it is set to one, the -->
<!-- list is reloaded (after a new image was added) -->
 
<!-- Main processing function, evaluates which button is pressed and activates the -->
<!-- according action -->

function doEdit(para)
{
 switch (para)
 {
  case 1: // next page
  {
    //if (parent.frames[0].document.EOL !=true)  // no more pages available? Get this information from the file list frame.
	if (page < maxpage)
     {
      page++; // increase page number
      filter=document.form1.FilterInput.value; 
      parent.location.href = browser + '.html?page='+page+'&filter='+filter; //reload complete browser
     }
    break;
  }
  case 2: // previous page
  { 
    page--;  //decrease page number
    if (page<1)  // already on first page?
     {
      page=1;
     }
    else
     {
	  filter=document.form1.FilterInput.value; 
      parent.location.href = browser+ '.html?page='+page+'&filter='+filter; //reload complete browser
     }
    break;
   }
   case 3: // calls a servlet which does a full-text search on image-title and filename.
   {
     filter=document.form1.FilterInput.value; 
     parent.location.href = browser + '.html?page=1&filter='+filter; //reload complete browser
     break;
   }
   case 4: // upload image, open the upload dialog.
   {
	 // filter=document.form1.galerieFilterInput.value;
	 //gallery=document.form1.galerie.value;
	 gallery=document.form1.galerie.options[document.form1.galerie.selectedIndex].value;
     parent.frames[1].location.href = uploadurl + gallery;
     break;
   }
   default:
   {
		alert("Unknown Exception");
   }
 }
}

