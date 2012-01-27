function toggleMultiSelectWidget(source) {

	// check if the checkbox is checked
	if ($(source).attr("checked")) {
		// the checkbox is checked, enable select box
		$(source).siblings("select").attr("disabled", false);
	} else {
		// the checkbox is not checked
		// disable select box
		$(source).siblings("select").attr("disabled", true);
	}
}