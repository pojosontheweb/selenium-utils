<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/woko/jsp/taglibs.jsp"%>
<c:set var="run" value="${renderPropertyValue.owningObject}"/>
<w:objectKey var="runId" object="${run}"/>
<div class="logs">
    <h2>Logs</h2>
    <p>
        Logs are updating live. Last ${renderPropertyValue.limit} shown.
    </p>
    <table class="logs-wrapper">
    </table>
</div>
<script type="text/javascript">
    $(function() {
        var poll = function() {
            setTimeout(function() {
                wokoClient.loadObject('Run', '${runId}', {
                    onSuccess: function(run) {
                        if (run.finishedOn) {
                            window.location.reload();
                        } else {
                            // update the logs
                            var logsWrapper = $('.logs-wrapper');
                            logsWrapper.empty();
                            run.logs.forEach(function(log) {
                                var date = log.logDate;
                                var text = log.text;
                                logsWrapper.append(
                                        $('<tr>')
                                                .append(
                                                $('<td>')
                                                        .addClass('date')
                                                        .text(date)
                                        )
                                                .append(
                                                $('<td>')
                                                        .addClass('text')
                                                        .text(text)
                                        )
                                )
                            });

                            poll();
                        }
                    }
                })
            }, 1000);
        };

        poll();

    });
</script>
