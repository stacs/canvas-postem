package postem.tool

class StudentController {

    def canvasFileService
    def CSVService

    def index() {
        def fileList = canvasFileService.listFiles((String)params.courseId)
        def filteredList = CSVService.filterByUser(fileList,(String)params.user)
        [courseFiles: filteredList]
    }

    def viewFile(){
        String fileURL = params.fileURL
        String fileId = params.fileId
        //log user viewing file
        def lastViewed = UserFileViewLog.findByFileIdAndLoginId(fileId,(String)params.user)
        if(lastViewed != null){
            lastViewed.lastViewed = new Date()
        }
        else{
            lastViewed = new UserFileViewLog(loginId: params.user, fileId: fileId, lastViewed: new Date())
        }
        lastViewed.save()
        def parsedFileMap = CSVService.parseFileForUser(fileURL,(String)params.user)
        def headerArray = parsedFileMap.get('headers')
        parsedFileMap.remove('headers')
        [headers: headerArray, contents: parsedFileMap.values(), courseId : params.courseId , user: params.user]
    }
}
