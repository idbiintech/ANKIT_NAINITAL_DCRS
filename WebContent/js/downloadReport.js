$(document).ready(function(){
$('#downloadReport').click(function(){
        debugger;
        $('#presentmentform')[0].action="downloadReport.do";
        $('#presentmentform').submit();
 	});
})