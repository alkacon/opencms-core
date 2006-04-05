

/* parse the entered query String */
function parseSearchQuery(theForm, message) {
  var queryValue = theForm.elements["query2"].value;
  var testValue = queryValue.replace(/ /g, "");
  if (testValue.length < 3) {
    alert(message);
    return (false);
  }
  theForm.elements["query"].value = queryValue;
  theForm.submit();
  return (true);
}
