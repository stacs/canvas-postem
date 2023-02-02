package postem.tool

import org.springframework.web.multipart.MultipartFile

class InstructorController {

    def canvasFileService
    def CSVService

    def index() {
        String courseId = params.courseId
        [courseFiles: canvasFileService.listFiles(courseId)]
    }

    def viewFile(){
        String fileURL = params.fileURL
        String released = params.released
        String fileId = params.fileId
        def userActivity = UserFileViewLog.findAllByFileId(fileId)
        def parsedFileMap = CSVService.parseFile(fileURL)
        def headerArray = parsedFileMap.get('headers')
        parsedFileMap.remove('headers')
        [headers: headerArray, contents: parsedFileMap.values(), displayName: params.displayName, released: released, userActivity: userActivity, courseId : params.courseId , userId: params.userId]
    }

    def editFile() {
        String editType = params.editType
        String fileId = params.fileId
        String displayName = params.displayName
        [editType: editType, fileId: fileId, displayName: displayName, courseId : params.courseId , userId: params.userId]
    }

    def upload() {
        String courseId = params.courseId
        MultipartFile f = params.myFile
        def users = canvasFileService.listUserLogins(courseId)
        if(CSVService.isEmptyFile(f)){
            render(view: 'index', model: [courseFiles: canvasFileService.listFiles(courseId), status: 'error', description: 'error.emptyfile'])
        }
        else{
            def badUsers = CSVService.validateFile(f,users)
            if(!badUsers.empty){
                render(view: 'index', model: [courseFiles: canvasFileService.listFiles(courseId), status: 'error', badUsers: badUsers])
            }
            else{
                String fileTitle = params.fileTitle + '.csv'
                Boolean releaseFeedback = false
                if(params.releaseCheckbox == 'on'){
                    releaseFeedback = true
                }
                canvasFileService.upload(f,true, releaseFeedback, courseId, fileTitle, params.userId)
                render(view: 'index', model: [courseFiles: canvasFileService.listFiles(courseId), status: 'success'])
            }
        }

    }

    def uploadNewVersion() {
        String courseId = params.courseId
        MultipartFile f = params.myFile
        def users = canvasFileService.listUserLogins(courseId)
        if(CSVService.isEmptyFile(f)){
            render(view: 'index', model: [courseFiles: canvasFileService.listFiles(courseId), status: 'error', description: 'error.emptyfile'])
        }
        else{
            def badUsers = CSVService.validateFile(f,users)
            if(!badUsers.empty){
                render(view: 'editFile', model: [courseFiles: canvasFileService.listFiles(courseId), status: 'error', editType: 'add', badUsers: badUsers, displayName: params.fileTitle])
            }
            else{
                String fileTitle = params.fileTitle + '.csv'
                Boolean releaseFeedback = false
                if(params.releaseCheckbox == 'on'){
                    releaseFeedback = true
                }
                canvasFileService.upload(f,true, releaseFeedback, courseId, fileTitle, params.userId)
                render(view: 'index', model: [courseFiles: canvasFileService.listFiles(courseId), status: 'success'])
            }
        }

    }

    def delete(){
        String fileId = params.fileId
        canvasFileService.deleteFile(fileId)
        render(view: 'index', model: [courseFiles: canvasFileService.listFiles(params.courseId), status: 'success'])
    }

    def release(){
        String fileId = params.fileId
        canvasFileService.hideFile(fileId,true)
        render(view: 'index', model: [courseFiles: canvasFileService.listFiles(params.courseId), status: 'success'])
    }

    def unrelease(){
        String fileId = params.fileId
        canvasFileService.hideFile(fileId,false)
        render(view: 'index', model: [courseFiles: canvasFileService.listFiles(params.courseId), status: 'success'])
    }

    def downloadFile(){
        def headers = ['Login ID', 'Last Name', 'First Name']
        def userList = canvasFileService.listUserDetails(params.courseId)

        response.setContentType("text/csv")
        response.setHeader("Content-disposition", "filename=\"template.csv\"")
        def outs = response.outputStream
        response.outputStream << headers.join(',') + '\n'
        userList.each { user ->
            outs << user.join(',') + '\n'
        }
        outs.flush()
        outs.close()
    }

    def downloadCSV(){
        String fileURL = params.fileURL
        String fileId = params.fileId
        String displayName = params.displayName
        def userActivity = UserFileViewLog.findAllByFileId(fileId)
        Map<String, String[]> parsedFileMap = CSVService.parseFile(fileURL)

        response.setContentType("text/csv")
        response.setHeader("Content-disposition", "filename=\"${displayName}.csv\"")
        def outs = response.outputStream

        for(row in parsedFileMap){
            if(row.key == 'headers'){
                outs <<  row.value.join(',') + ',Last Viewed' + '\n'
            }
        }

        for(row in parsedFileMap){
            if(row.key != 'headers'){
                outs << row.value.join(',') + ','
                if(userActivity.any{it.loginId == row.key}){
                    for(activity in userActivity){
                        if(activity.loginId == row.key){
                            outs << activity.lastViewed.toString() + '\n'
                            break
                        }
                    }

                }
                else{
                    outs << 'Never' + '\n'
                }
            }
        }
        outs.flush()
        outs.close()
    }

    def renameFile(){
        String fileId = params.fileId
        String fileName = params.fileName + '.csv'
        canvasFileService.updateFileName(fileId, fileName)
        render(view: 'index', model: [courseFiles: canvasFileService.listFiles(params.courseId), status: 'success'])
    }

    def handleSizeLimitExceededException() {
        def courseId = session.courseId
        render(view: 'index', model: [courseFiles: canvasFileService.listFiles(courseId), status: 'error', description: 'error.filesize.exceeded'])
    }
}
