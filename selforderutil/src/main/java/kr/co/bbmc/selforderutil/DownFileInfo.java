package kr.co.bbmc.selforderutil;

import java.io.File;

public class DownFileInfo {
    public String folderName;
    public String fileName;

    public String rootKContent;
    public long fileLength;
    public int downFileId;
    public int stbfileid;
    public int kfileid;
    public String kroot;
    public String playatonce;

    public int downFiled;
    public boolean completed;
    public boolean scheduleContent;

    public DownFileInfo()
    {
    }

    public DownFileInfo(String folderName, String fileName, long fileLength, int downFileId)
    {
        this.folderName = folderName;
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.downFileId = downFileId;

        this.completed = false;
//        this.scheduleContent = true;
    }

    public DownFileInfo(String rootKContent, String folderName, String fileName, long fileLength, int downFileId)
    {
        this.rootKContent = rootKContent;
        this.folderName = folderName;
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.downFileId = downFileId;

        this.completed = false;
//        this.scheduleContent = false;
    }

    // jason:multirepos: 다중 저장소 서버 연동 기능(2013/06/04)
    public String LocalFolderName()
    {
        String retStr = null;

//        rootKContent.isEmpty()
//        if (scheduleContent)
        if(rootKContent==null)
        {
            if (fileName.isEmpty())
            {
                retStr = "";
            }
            else if (fileName.endsWith(".self"))
            {
                retStr = "Menu"+File.separator;
            }
            else if (fileName.endsWith(".scd"))
            {
                retStr = "Schedule"+File.separator;
            }
            else if (fileName.startsWith("A") || fileName.indexOf("]A") > 0)
            {
                retStr = "Content"+File.separator+"Audio"+File.separator;
            }
            else if (fileName.startsWith("C") || fileName.indexOf("]C") > 0)
            {
                retStr = "Content"+File.separator+"Component"+File.separator;
            }
            else if (fileName.startsWith("F") || fileName.indexOf("]F") > 0)
            {
                retStr = "Content"+File.separator+"Flash"+File.separator;
            }
            else if (fileName.startsWith("I") || fileName.indexOf("]I") > 0)
            {
                retStr = "Content"+ File.separator+"Img"+File.separator;        //Image -> Img 2019.07.31.
            }
            else if (fileName.startsWith("P") || fileName.indexOf("]P") > 0)
            {
                retStr = "Content"+File.separator+"PowerPoint"+File.separator;
            }
            else if (fileName.startsWith("T") || fileName.indexOf("]T") > 0)
            {
                retStr = "Content"+File.separator+"Text"+File.separator;
            }
            else if (fileName.startsWith("V") || fileName.indexOf("]V") > 0)
            {
                retStr = "Content"+File.separator+"Video"+File.separator;
            }
            else
            {
                retStr = "";
            }
        }
        else
        {
            retStr = rootKContent + folderName.substring(folderName.indexOf(File.separator));
        }
        return retStr;
    }
    //-
}
