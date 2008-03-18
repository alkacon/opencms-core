/**
 * Plugin for OpenCms, implementing the save & exit buttons and the keyboard shortcuts.
 */
var EditArea_opencms= {
	/**
	 * Get called once this file is loaded (editArea still not initialized)
	 *
	 * @return nothing
	 */
	init: function(){
	}
	/**
	 * Returns the HTML code for the OpenCms buttons.
	 *
	 * @param {string} ctrl_name: the name of the control to add
	 * @return HTML code for a specific control or false.
	 */
	,get_control_html: function(ctrl_name){
		switch(ctrl_name){
			case "ocms_save":
				// Control id, button img, command
				return parent.editAreaLoader.get_button_html('ocms_save', 'save.png', 'save_cmd', false, this.baseURL);
			case "ocms_save_exit":
				// Control id, button img, command
				return parent.editAreaLoader.get_button_html('ocms_save_exit', 'save_exit.png', 'save_exit_cmd', false, this.baseURL);
			case "ocms_exit":
				// Control id, button img, command
				return parent.editAreaLoader.get_button_html('ocms_exit', 'exit.png', 'exit_cmd', false, this.baseURL);
		}
		return false;
	}
	/**
	 * Get called once EditArea is fully loaded and initialised
	 *
	 * @return nothing
	 */
	,onload: function(){
	}

	/**
	 * The OpenCms keyboard shortcuts for saving or exiting the editor.
	 *
	 * @param (event) e: the keydown event
	 * @return true - pass to next handler in chain, false - stop chain execution
	 * @type boolean
	 */
	,onkeydown: function(e){
		if (!e) {
			// if the browser did not pass the event information to the
			// function, we will have to obtain it from the event register
			if (window.event) {
				//DOM
				e = window.event;
			} else {
				// total failure, we have no way of referencing the event
				return true;
			}
		}

		if (typeof(e.which) == 'number') {
			//NS 4, NS 6+, Mozilla 0.9+, Opera
			key = e.which;
		} else if (typeof(e.keyCode) == 'number') {
			//IE, NS 6+, Mozilla 0.9+
			key = e.keyCode;
		} else if (typeof(e.charCode) == 'number') {
			//also NS 6+, Mozilla 0.9+
			key = e.charCode;
		} else {
			// total failure, we have no way of obtaining the key code
			return true;
		}

		if (e.ctrlKey) {
			if (key == 83) {
				// 's' pressed
				if (e.shiftKey == true) {
					// save content and exit
					top.edit.buttonAction(2);
				} else {
					// save content without exiting
					top.edit.buttonAction(3);
				}
				return false;
			}
			if (e.shiftKey && key == 88) {
				// 'x' pressed, exit editor
				top.edit.confirmExit();
				return false;
			}
		}

		return true;
	}

	/**
	 * Executes a specific OpenCms command.
	 *
	 * @param {string} cmd: the name of the command being executed
	 * @param {unknown} param: the parameter of the command
	 * @return true - pass to next handler in chain, false - stop chain execution
	 */
	,execCommand: function(cmd, param){
		// handle commands
		switch(cmd){
			case "save_cmd":
				top.edit.buttonAction(3);
				return false;
			case "save_exit_cmd":
				top.edit.buttonAction(2);
				return false;
			case "exit_cmd":
				top.edit.confirmExit();
				return false;
		}
		// pass to next handler in chain
		return true;
	}

};

// adds the plugin class to the list of available EditArea plugins
editArea.add_plugin("opencms", EditArea_opencms);
