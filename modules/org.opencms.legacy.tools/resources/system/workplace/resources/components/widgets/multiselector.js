function toggleMultiSelectWidget(source) {
	
	function onChange(evt){
		var value=$(this).val();
		// set the value on the hidden input
		$(this).siblings("input:hidden").val(value);
	}

	// check if the checkbox is checked
	if ($(source).attr("checked")) {
		// the checkbox is checked, enable select box
		$(source).siblings("select").attr("disabled", false).click(onChange);
	} else {
		// the checkbox is not checked
		// disable select box
		$(source).siblings("select").attr("disabled", true).unbind("click", onChange);
	}
}