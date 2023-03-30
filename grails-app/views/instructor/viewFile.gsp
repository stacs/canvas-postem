<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
</head>

<body>
    <div class="panel panel-default">
        <div class="panel-heading"> <label>${displayName} </label> (<g:if test="${released == 'false'}">Released</g:if><g:else>Unreleased</g:else>)</div>
        <div class="panel-body">
            Below are your students and the feedback you provided in this file.  Use the search box below to look for a specific student.
        </div>
    </div>
    <input type="text" id="myInput" onkeyup="myFunction()" placeholder="Search..." style="float:right"><br />

    <table id="myTable" class="table table-striped table-bordered" data-toggle="table" data-pagination="true" data-pagination-v-align="bottom" data-smart-display="true" data-page-size="10" data-page-list="[5, 10, 20, 50, 100, All]">
        <thead>
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
    <a href="${createLink(action: 'index', params: [courseId: courseId, userId: userId  ])}" class="btn btn-custom" style="text-decoration:none" role="button">Back</a>

    <script>
        function myFunction() {
            var input, filter, table, tr, td, i, j, td_val;
            input = document.getElementById("myInput");
            filter = input.value.toUpperCase();
            table = document.getElementById("myTable");
            tr = table.getElementsByTagName("tr");
            for (i = 0; i < tr.length; i++) {
                td = tr[i].getElementsByTagName("td");
                for(j = 0; j < td.length; j++){
                    td_val = td[j];
                    if (td_val) {
                        if (td_val.innerHTML.toUpperCase().indexOf(filter) > -1) {
                            tr[i].style.display = "";
                            break;
                        } else {
                            tr[i].style.display = "none";
                        }
                    }
                }
            }
        }
    </script>
</body>
</html>