<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <script type="text/javascript">
            $(document).ready(function(){
                 var table = $('#studentViewTable').DataTable({
                         fixedColumns:   {
                             left: 1
                         }
                 });
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
    <table class="table table-bordered" id="studentViewTable" data-toggle="table" data-pagination="true" data-pagination-v-align="bottom" data-smart-display="true" data-page-size="10" data-page-list="[5, 10, 20, 50, 100, All]">
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
                            <td class="${(userActivity.any {it.loginId == loginId}) ? '' : 'success'}" scope="row">${contentRow}</td>
                        </g:if>
                        <g:else>
                            <td scope="row">${contentRow}</td>
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