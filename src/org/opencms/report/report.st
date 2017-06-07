group report;

FORMAT_DEFAULT(message) ::= <<
<span class="o-report-default">$message$</span>
>>
  
FORMAT_ERROR(message) ::= <<
<span class="o-report-error">$message$</span>
>>
 
FORMAT_HEADLINE(message) ::= <<
<span class="o-report-headline">$message$</span>
>>

FORMAT_NOTE(message) ::= <<
<span class="o-report-note">$message$</span>
>>
 
FORMAT_OK(message) ::= <<
<span class="o-report-ok">$message$</span>
>>

FORMAT_WARNING(message) ::= <<
<span class="o-report-warning">$message$</span>
>>

NEWLINE() ::= <<
<br>
>>

EXCEPTION(message) ::= <<
<div class="o-report-exception">
$EXCEPTION_INTERNAL(message)$
</div>  
>>

EXCEPTION_INTERNAL(message) ::= <<
$message.class.name$: $message.message$<br>
$message.stackTrace:{traceElement |
<span class="o-report-stacktraceline">at $traceElement.className$.$traceElement.methodName$($traceElement.fileName$:$traceElement.lineNumber$)</span><br>
}$
$if(message.cause)$
Caused by: $EXCEPTION_INTERNAL(message.cause)$
$else$
$endif$
>>




