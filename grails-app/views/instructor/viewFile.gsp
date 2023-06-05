<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <script type="text/javascript">
             function addAriaClasses(){

                 $('.paginate_button').each(function () {
                        $(this).attr("aria-label", "Go to page " + $(this).text());
                 });

                 $('.page-link').each(function () {
                       let attr = $(this).attr('aria-current');
                       if (typeof attr !== 'undefined' && attr !== false) {
                          $(this).attr("aria-label", "Page " + $(this).text() + ". Current Page. " + $("#studentViewTable_info").html());
                       }
                       let attr2 = $(this).attr('aria-disabled');
                       if (typeof attr2 !== 'undefined' && attr2 !== false) {
                            $(this).attr("tabindex", "-1");
                       }
                 })

                 $("#studentViewTable_previous").attr("aria-label", "Go to previous page");
                 $("#studentViewTable_next").attr("aria-label", "Go to next page");
            }
            $(document).ready(function(){
                 var table = $('#studentViewTable').DataTable({
                         fixedColumns:   {
                             left: 1
                         },
                         initComplete: function(settings, json) {
                           addAriaClasses();
                         }
                 });
                 $('#studentViewTable').on( 'draw.dt', function () {
                     addAriaClasses();
                  } );
            });
        </script>
</head>

<body>
    <div class="panel panel-default">
        <div class="panel-heading"> <label>${displayName} </label> (<g:if test="${released == 'false'}">Released</g:if><g:else>Unreleased</g:else>)</div>
        <div class="panel-body">
            Below are your students and the feedback you provided in this file.  Use the search box below to look for a specific student.
        </div>
    </div>
    <table class="table table-bordered" id="studentViewTable" data-toggle="table" data-pagination="true" data-pagination-v-align="bottom" data-smart-display="true" data-page-size="10" data-sort-name="title" data-sort-order="asc">
        <thead style="background-color: #f5f5f5;">
            <tr>
                <g:each in="${headers}" var="header">
                    <th data-sortable="true" scope="col">${header}</th>
                </g:each>
                <th data-sortable="true" scope="col">Last Checked</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${contents}" var="content">
                <tr>
                    <g:set var="num" value="${1}" />
                    <g:each in="${content}" var="contentRow">
                        <g:if test="${num++ == 1}">
                            <g:set var="loginId" value="${contentRow}"/>
                            <th class="${(userActivity.any {it.loginId == loginId}) ? '' : 'success'}" scope="row">${contentRow}</th>
                        </g:if>
                        <g:else>
                            <td>${contentRow}</td>
                        </g:else>
                    </g:each>
                    <g:if test="${userActivity.any {it.loginId == loginId}}">
                        <g:each in="${userActivity}" var="activity">
                            <g:if test="${activity.loginId == loginId}">
                                <td>${activity.lastViewed}</td>
                            </g:if>
                        </g:each>
                    </g:if>
                    <g:else>
                        <td>Never</td>
                    </g:else>
                </tr>
            </g:each>

        </tbody>
    </table>
    <br><br>
    <a href="${createLink(action: 'index', params: [courseId: courseId, userId: userId  ])}" class="btn btn-custom-dark" style="text-decoration:none" role="button">Back</a>

</body>
</html>