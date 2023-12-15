package kr.co.bbmc.paycast.util;

import static kr.co.bbmc.paycast.util.XmlUtilKt.parsePlayerOptionXML;
import static kr.co.bbmc.selforderutil.FileUtils.BBMC_TEMP_DIRECTORY;
import static kr.co.bbmc.selforderutil.FileUtils.ScheduleRootFolder;

import android.content.Context;

import com.orhanobut.logger.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import kr.co.bbmc.selforderutil.FileUtils;
import kr.co.bbmc.selforderutil.OptUtil;
import kr.co.bbmc.selforderutil.PlayerOptionEnv;

public class ConfigUtil {

    private static DocumentBuilder dBuilder = null;
    private static Document doc = null;
    private static NodeList nList = null;
    private static NodeList nScheduleList = null;

    public static void readRequestedStbConfigFile(String tempSavedConfigFile, PlayerOptionEnv option, Context c)
    {
        String fileName = BBMC_TEMP_DIRECTORY + tempSavedConfigFile+".xml";
        Logger.i("File name : " + fileName);
        option = parsePlayerOptionXML(fileName, option, c);
        File file = new File(fileName);
        if (file.exists())
        {
            Logger.d("File exist!!");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            //Log.d(TAG, "Holidays Current Element tempSavedConfigFile=" + fileName);
            try {
                dBuilder = factory.newDocumentBuilder();
                try {
                    doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    nList = doc.getElementsByTagName("Holidays");
                    nScheduleList = doc.getElementsByTagName("Schedules");
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

            List<Date> holidays = new ArrayList<Date>();

            if (nList != null) {
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    //Log.d(TAG, "Holidays Current Element :" + nNode.getNodeName());
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        NodeList n = eElement.getChildNodes();
                        for (int i = 0; i < n.getLength(); i++) {
                            Node node = n.item(i);
                            if (node.getNodeType() != Node.TEXT_NODE) {
                                if (node.getNodeName().equals("date")) {
                                    String hDay = String.valueOf(node.getAttributes());
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                                    hDay.replace("-", "").replace("/", "");
                                    Date date = null;
                                    try {
                                        date = sdf.parse(hDay);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    holidays.add(date);
                                    //Log.e(TAG, "1 hDay=" + hDay + " getTextContent()=" + node.getTextContent());
                                }
                                else if (node.getNodeName().equals("Holiday")) {
                                    NamedNodeMap attr = node.getAttributes();
                                    for(int j = 0; j<attr.getLength(); j++) {
                                        Node nodeAttr = attr.item(j);
                                        String hDay = nodeAttr.getNodeValue();
                                        Logger.e("Holiday hDay=" + hDay);
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                        hDay.replace("-", "").replace("/", "");
                                        Date date = null;
                                        try {
                                            date = sdf.parse(hDay);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        holidays.add(date);
                                        //Log.e(TAG, "2 hDay=" + hDay + " getTextContent()=" + node.getTextContent());
                                    }
                                } else {
                                    //Log.e(TAG, "else node.getNodeName()=" + node.getNodeName());
                                    ;
                                }
                            }

                        }
                    }
                }
                OptUtil.SaveHolidayOptions(FileUtils.Holiday, holidays);

                // 2. 스케줄 목록
                ArrayList<String> galleryFiles = new ArrayList<>();
                File dInfo = new File(ScheduleRootFolder);
                if (dInfo.exists())
                {
                    List<String> fInfo = FileUtils.searchByFilefilter(ScheduleRootFolder, ".scd");

                    for(String fileInfo:fInfo) {
                        String filename=fileInfo.substring(fileInfo.lastIndexOf("/")+1);
                        galleryFiles.add(filename);
                        //Log.d(TAG, "galleryFiles list filename="+filename);
                    }
                }

                List<String> scheduleList = new ArrayList<String>();

                if(nScheduleList!=null)
                {
                    for (int temp = 0; temp < nScheduleList.getLength(); temp++) {
                        Node nNode = nScheduleList.item(temp);
                        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element eElement = (Element) nNode;
                            NodeList n = eElement.getChildNodes();
                            for (int i = 0; i < n.getLength(); i++) {
                                Node node = n.item(i);
                                if (node.getNodeType() != Node.TEXT_NODE) {
                                    if (node.getNodeName().equals("Schedule")) {
                                        NamedNodeMap attrs = node.getAttributes();

                                        for(int a = 0; a<attrs.getLength(); a++) {
                                            Node nodeAttr = attrs.item(a);
                                            if (nodeAttr.getNodeName().equals("name")) {
                                                String shedulefile = nodeAttr.getNodeValue();
                                                scheduleList.add(shedulefile);
                                                //galleryFiles.remove(shedulefile);
                                                //Log.e(TAG, "1 shedulefile=" + ScheduleRootFolder+shedulefile );
                                            }
                                        }
                                    } else {
                                        //Log.d(TAG, "node.getNodeName()=" + node.getNodeName());
                                        ;
                                    }
                                }

                            }
                        }
                    }
                    if(scheduleList.size()>0) {
                        if(galleryFiles.size() > 0) {
                            for (String schFile : scheduleList) {
                                galleryFiles.remove(schFile);
                            }
                        }
                    }
                    else {
                        File bbmcDir = new File(FileUtils.BBMC_PAYCAST_CONTENT_DIRECTORY+"Image");
                        if (bbmcDir.exists())
                        {
                            File[] fInfo = bbmcDir.listFiles();

                            if((fInfo!=null)&&(fInfo.length>0)) {
                                for (File fileInfo : fInfo) {
                                    fileInfo.delete();
                                }
                            }
                        }
                        bbmcDir = new File(FileUtils.BBMC_PAYCAST_CONTENT_DIRECTORY+"Video");
                        if (bbmcDir.exists())
                        {
                            File[] fInfo = bbmcDir.listFiles();

                            if((fInfo!=null)&&(fInfo.length>0)) {
                                for (File fileInfo : fInfo) {
                                    fileInfo.delete();
                                }
                            }
                        }
                        bbmcDir = new File(FileUtils.BBMC_PAYCAST_CONTENT_DIRECTORY+"Text");
                        if (bbmcDir.exists())
                        {
                            File[] fInfo = bbmcDir.listFiles();

                            if((fInfo!=null)&&(fInfo.length>0)) {
                                for (File fileInfo : fInfo) {
                                    fileInfo.delete();
                                }
                            }
                        }
                        bbmcDir = new File(FileUtils.BBMC_PAYCAST_CONTENT_DIRECTORY+"Flash");
                        if (bbmcDir.exists())
                        {
                            File[] fInfo = bbmcDir.listFiles();

                            if((fInfo!=null)&&(fInfo.length>0)) {
                                for (File fileInfo : fInfo) {
                                    fileInfo.delete();
                                }
                            }
                        }
                        bbmcDir = new File(FileUtils.BBMC_PAYCAST_CONTENT_DIRECTORY+"Audio");
                        if (bbmcDir.exists())
                        {
                            File[] fInfo = bbmcDir.listFiles();

                            if((fInfo!=null)&&(fInfo.length>0)) {
                                for (File fileInfo : fInfo) {
                                    fileInfo.delete();
                                }
                            }
                        }
                        bbmcDir = new File(FileUtils.BBMC_PAYCAST_CONTENT_DIRECTORY+"PowerPoint");
                        if (bbmcDir.exists())
                        {
                            File[] fInfo = bbmcDir.listFiles();

                            if((fInfo!=null)&&(fInfo.length>0)) {
                                for (File fileInfo : fInfo) {
                                    fileInfo.delete();
                                }
                            }
                        }
                    }
                    if(galleryFiles.size() > 0) {
                        for (String schFile : galleryFiles) {
                            File f = new File(ScheduleRootFolder + schFile);
                            if (f.exists()) {
                                //Log.e(TAG, "delete schedule file =" + f.getPath()+f.getName());
                                boolean result = f.delete();
                                //Log.e(TAG, "delete result =" + result);
                            }
                        }
                    }
                    if(galleryFiles!=null)
                        galleryFiles.clear();
                    if(scheduleList!=null)
                        scheduleList.clear();
                }
            }

        }
    }
}
