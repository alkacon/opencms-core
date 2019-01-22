CodeMirror.defineMode("htmlembedded_modified", function(config, parserConfig) {

  //config settings
  var scriptStartRegex = parserConfig.scriptStartRegex || /^<%/i,
      scriptEndRegex = parserConfig.scriptEndRegex || /^%>/i,
      commentStartRegex = parserConfig.commentStartRegex || /<%--/i,
      commentEndRegex = parserConfig.commentEndRegex || /--%>/i;

  //inner modes
  var scriptingMode, htmlMixedMode, commentMode;

  //tokenizer when in html mode
  function htmlDispatch(stream, state) {
      if (stream.match(commentStartRegex, true)) {
          state.token=commentDispatch;
          return "comment"; 
      } else {
        if (stream.match(scriptStartRegex, false)) {
            state.token=scriptingDispatch;
            return scriptingMode.token(stream, state.scriptState);
            }
        else
            return htmlMixedMode.token(stream, state.htmlState);
      }
    }


  //tokenizer when in comment mode
  function commentDispatch(stream, state) {
      if (stream.match(commentEndRegex, true)) 
        state.token=htmlDispatch;
      else stream.next();
      return "comment";
      }

  //tokenizer when in scripting mode
  function scriptingDispatch(stream, state) {
      if (stream.match(scriptEndRegex, false))  {
          state.token=htmlDispatch;
          return htmlMixedMode.token(stream, state.htmlState);
         }
      else
          return scriptingMode.token(stream, state.scriptState);
         }

  return {
    startState: function() {
      commentMode   = commentMode   || CodeMirror.getMode(config, "jspcomment");
      scriptingMode = scriptingMode || CodeMirror.getMode(config, parserConfig.scriptingModeSpec);
      htmlMixedMode = htmlMixedMode || CodeMirror.getMode(config, "htmlmixed");
      return {
          token : parserConfig.startOpen ? scriptingDispatch : htmlDispatch,
          htmlState : CodeMirror.startState(htmlMixedMode),
          scriptState : CodeMirror.startState(scriptingMode),
          commentState: CodeMirror.startState(commentMode)
      };
    },

    token: function(stream, state) {
      return state.token(stream, state);
    },

    indent: function(state, textAfter) {
      if (state.token == htmlDispatch)
        return htmlMixedMode.indent(state.htmlState, textAfter);
      else if (state.token == commentDispatch && commentMode.indent)
        return commentMode.indent(state.commentState, textAfter);
      else if (scriptingMode.indent)
        return scriptingMode.indent(state.scriptState, textAfter);
    },

    copyState: function(state) {
      return {
       token : state.token,
       htmlState : CodeMirror.copyState(htmlMixedMode, state.htmlState),
       scriptState : CodeMirror.copyState(scriptingMode, state.scriptState),
       commentState: CodeMirror.copyState(commentMode, state.commentState)
      };
    },

    electricChars: "/{}:",

    innerMode: function(state) {
      if (state.token == scriptingDispatch) return {state: state.scriptState, mode: scriptingMode};
      else if (state.token == commentDispatch) return {state: state.commentState, mode: commentMode};
      else return {state: state.htmlState, mode: htmlMixedMode};
    }
  };
}, "htmlmixed");

CodeMirror.defineMIME("application/x-ejs", { name: "htmlembedded_modified", scriptingModeSpec:"javascript"});
CodeMirror.defineMIME("application/x-aspx", { name: "htmlembedded_modified", scriptingModeSpec:"text/x-csharp"});
CodeMirror.defineMIME("application/x-jsp", { name: "htmlembedded_modified", scriptingModeSpec:"text/x-java"});
CodeMirror.defineMIME("application/x-erb", { name: "htmlembedded_modified", scriptingModeSpec:"ruby"});
