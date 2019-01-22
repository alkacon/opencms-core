var domYes=document.getElementById?1:0;

function getElemById(id) {
	
	// help method to get an object by id, independent of the client's browser
	if (domYes) {
		return document.getElementById(id);
	} else {
		return document.all[id];
	}
}

function setChildListBox(parentObject, childObject, childArray) {

	// clear child listbox
	for(var i=childObject.length;i>0;i--) {
		childObject.options[i] = null;
	} 
	// add default value
	childObject.options[0] = new Option("Select Value","");
	
	// get select item
	var selIndex = parentObject.options[parentObject.selectedIndex].value;
	if (selIndex == "") {
		// if nothing selected, disable and hide child list box
		childObject.disabled = true;
		getElemById(childObject.id + "Display").style.display="none";
	} else {
		// fill the child list box with the associated items
		var childIndex = 1;
		for (i = 0; i < childArray.length; i++) {
			if (childArray[i][1] == selIndex) {
				childObject.options[childIndex] = new Option(childArray[i][2], childArray[i][0]);
				childIndex++;
			}
		}
		// if at least one item available
		if (childIndex > 1) {
			// display the child list box
		    childObject.disabled = false;
		    getElemById(childObject.id + "Display").style.display="";
        }
	}

	// select the first option
	childObject.selectedIndex = 0;
	childObject.onchange();
	
	// set the widget value
	setWidgetValue(childObject.id.substring(0, childObject.id.length - 6));
}

function setWidgetValue(id) {
	
	// set the widget value
	var value = "";
    for(i=1;i<5;i++) {
    	var parent = getElemById(id + "cat" + i + "Id");
    	var child = getElemById(id + "cat" + (i+1) + "Id");
    	if (parent.selectedIndex != "") {
    		value = parent.options[parent.selectedIndex].value;
    	}
    	if (!child || (child.selectedIndex == "")) {
    		break;
    	}
    }
    getElemById(id).value = value;
}
