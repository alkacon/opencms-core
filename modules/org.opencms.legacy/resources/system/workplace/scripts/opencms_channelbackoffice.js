/**
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

/*
* function to move a selectbox entry from one selectbox to another
*/
function moveChannel(theList,theList2){
    if (theList2.value != null && theList2.value != "") {
        newOption = new Option(theList2.value, theList2.value, false, false); 
        theList.options[theList.length] = newOption;
        
        for(i=0;i<theList2.length;i++){
            if(theList2.options[i].selected){
                theList2.options[i]=null;
            }
        }
    }
}

/* function to copy currently selected and not selected channels into hidden input fields
* as a comma separated list of channel names
*/
function copySelection() {
    var available = "";
	var selected = "";
	for (i=0; i<document.Editor.channelAvailable.length;i++) {
		if (i==0) {
			available = available + document.Editor.channelAvailable.options[i].value;
		} else {
			available = available + "," + document.Editor.channelAvailable.options[i].value;
		}
	}
	for (j=0; j<document.Editor.channelSelected.length;j++) {
		if (j==0) {
			selected = selected + document.Editor.channelSelected.options[j].value;
		} else {
			selected = selected + "," + document.Editor.channelSelected.options[j].value;
		}
	}
	if (available == "") {
		document.Editor.availablechannels.value = "empty";
	} else {
		document.Editor.availablechannels.value = available;		
	}
	if (selected == "") {
		document.Editor.selectedchannels.value = "empty";
	} else {
		document.Editor.selectedchannels.value = selected;
	}
}
