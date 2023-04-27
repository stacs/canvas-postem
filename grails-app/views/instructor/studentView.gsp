<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <script type="text/javascript">
                function changeList(fileURL) {
                       var choice = document.getElementById('participantselect').value;
                       if(choice == 'null'){
                           $("#studentInfo").html("No Student is selected");
                       }
                       else{
                            $.ajax({
                                    method: "POST",
                                    dataType: 'json',
                                    url: "/lts-postem/instructor/studentFileInfo",
                                    data: {
                                         "fileURL" : fileURL,
                                         "user" : choice
                                     },
                                    success: function(response){
                                          let html = "";
                                          let i = 0;
                                          let headers = response["headers"];
                                          let result = response[choice];
                                          html += "<table class='table table-bordered' data-toggle='table'><thead style='background-color: #f5f5f5;'><tr>";
                                          for (i = 0; i < headers.length; i++) {
                                            html += "<th>" + headers[i] + "</th>";

                                          }
                                          html += "</tr></thead><tbody><tr>";
                                          for (i = 0; i < result.length; i++) {
                                                html += "<td>" + result[i] + "</td>";
                                          }
                                          html += "</tr></tbody></table>";
                                          $("#studentInfo").html(html);
                                    }
                            });
                       }
                }
    </script>
</head>

<body>
    <div class="panel panel-default">
        <div class="panel-heading"><label>View Student in ${displayName}</label></div>
    </div>
    <div class="form-group">
        <label for="participantselect">Select a Student</label>
        <g:select id="participantselect" name="participantchoice"
          from="${logins}" optionKey="key" optionValue="value"
          noSelection="${['null': 'Please Choose...']}"
          onchange="JavaScript:changeList('${fileURL}')" />

    </div>
    <div id="output" aria-label="student information" tabindex="0" aria-describedby="studentInfo" class="table_wrapper"><span id="studentInfo">
    </span></div>
    <br><br>
    <a href="${createLink(action: 'index', params: [courseId: courseId, userId: userId  ])}" class="btn btn-custom-dark" style="text-decoration:none" role="button">Back</a>
</body>
</html>