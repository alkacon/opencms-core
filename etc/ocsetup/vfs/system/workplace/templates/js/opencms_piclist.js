/*
 * File   : $Source: /alkacon/cvs/opencms/etc/ocsetup/vfs/system/workplace/templates/js/Attic/opencms_piclist.js,v $
 * Date   : $Date: 2001/07/16 18:23:27 $
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
// Invokes servlet calls in Picture Browser:
// based on script from old cms version
//
// author:          m.schreiber
// company:         mindfact interaktive medien ag
// date:            08.02.2000
// update:      
// update author:   
// ===============================================


function updateSelection()
{
    if(document.form1.galerie.options[document.form1.galerie.selectedIndex].value)
    {
        top.selectedGallery = document.form1.galerie.selectedIndex;
        parent.frames[1].location.href=document.form1.galerie.options[document.form1.galerie.selectedIndex].value;
    }
}

function getSelection()
{
    //document.form1.galerie.selectedIndex = top.selectedGallery;
}


var DO_NEXT=1;
var DO_PREV=2;
var DO_SEARCH=3;
var DO_UPLOAD=4;


<!-- The update flag is set by the upload dialog. When it is set to one, the picture -->
<!-- list is reloaded (after a new image was added) -->

if (0 == 1)
{
 alert ("Update Flag gesetzt: Seite neu laden!");
 //parent.frames[1].location.href="#"+filter;
}


<!-- Main processing function, evaluates which button is pressed and activates the -->
<!-- according action -->

function doEdit(para)
{
 switch (para)
 {
  case 1: // next page
  {
    //if (parent.frames[0].document.EOL !=true)  // no more pages available? Get this information from the picture list frame.
    if (page < maxpage)
     {
      page++; // increase page number
      filter=document.form1.FilterInput.value;
      //alert("Servlet: Nächste Seite der Galerie laden");
      parent.location.href = "picturebrowser.html?page="+page+"&filter="+filter; //reload complete picture browser
     }
    break;
  }
  case 2: // next page
  {
//    if (parent.frames[0].document.BOL !=true)
//     {
//      page--;
    page--;  //decrease page number
    if (page<1)  // already on first page?
     {
      page=1;
     }
    else
     {
      filter=document.form1.FilterInput.value;
      //alert("Servlet: Vorhergehende Seite der Galerie laden");
      parent.location.href = "picturebrowser.html?page="+page+"&filter="+filter; //reload complete picture browser
     }
    break;
   }
   case 3: // calls a servlet which does a full-text search on image-title and filename.
   {
     filter=document.form1.FilterInput.value;
     //alert("Servlet: Über Titel oder Dateiname suchen");
      parent.location.href = "picturebrowser.html?page=1&filter="+filter; //reload complete picture browser
     break;
   }
   case 4: // upload image, open the upload dialog.
   {
     alert('Not implemented!');
     break;
   }
   default:
   {
        alert("Unknown Exception");
   }
 }
}

