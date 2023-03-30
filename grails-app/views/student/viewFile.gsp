<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
</head>

<body>
    <table id="myTable" class="table table-striped table-bordered">
        <tr>
            <g:each in="${headers}" var="header">
                <th>${header}</th>
            </g:each>
        </tr>
        <g:each in="${contents}" var="content">
            <tr>
                <g:each in="${content}" var="contentRow">
                    <td>${contentRow}</td>
                </g:each>
            </tr>
        </g:each>
    </table>
    <a href="${createLink(action: 'index', params: [courseId: courseId, user: user ])}" class="btn btn-custom" style="text-decoration:none" role="button">Back</a>
</body>
</html>