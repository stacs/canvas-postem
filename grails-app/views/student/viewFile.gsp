<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <script type="text/javascript">
            $(document).ready(function(){
                 var table = $('#studentViewFileTable').DataTable({
                         paging: false,
                         searching: false,
                         fixedColumns:   {
                             left: 1
                         }
                 });
            });
        </script>
</head>

<body>
    <table class="table table-bordered" id="studentViewFileTable" data-toggle="table" >
        <thead style="background-color: #f5f5f5;">
        <tr>
            <g:each in="${headers}" var="header">
                <th>${header}</th>
            </g:each>
        </tr>
        </thead>
        <tbody>
        <g:each in="${contents}" var="content">
            <tr>
                <g:each in="${content}" var="contentRow">
                    <td>${contentRow}</td>
                </g:each>
            </tr>
        </g:each>
        </tbody>
    </table>
    <a href="${createLink(action: 'index', params: [courseId: courseId, user: user ])}" class="btn btn-custom-dark" style="text-decoration:none" role="button">Back</a>
</body>
</html>