package kr.co.bbmc.paycast.util;

import static kr.co.bbmc.paycast.service.ServiceConstantKt.MAX_CONNECTION_TIMEOUT;
import static kr.co.bbmc.paycast.service.ServiceConstantKt.MAX_SOCKET_TIMEOUT;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import kr.co.bbmc.paycast.data.model.CustomAddedOptionData;
import kr.co.bbmc.paycast.data.model.CustomOptionData;
import kr.co.bbmc.paycast.data.model.CustomRequiredOptionData;
import kr.co.bbmc.paycast.data.model.DataModel;
import kr.co.bbmc.selforderutil.FileUtils;
import kr.co.bbmc.selforderutil.KioskPayDataInfo;

public class NetUtil {

    public static String HttpKioskResponseString(String url, KioskPayDataInfo kioskPayDataInfo, ArrayList orderlist) throws JSONException {
        String inputLine = null;
        StringBuffer outResult = new StringBuffer();
        URL serverUrl = null;

        try {
            serverUrl = new URL(url);
        } catch (MalformedURLException var38) {
            var38.printStackTrace();
            return "";
        }

        HttpURLConnection httpConn = null;

        try {
            httpConn = (HttpURLConnection)serverUrl.openConnection();
        } catch (IOException var37) {
            var37.printStackTrace();
            return "";
        }

        httpConn.setDoOutput(true);

        try {
            httpConn.setRequestMethod("POST");
        } catch (ProtocolException var36) {
            var36.printStackTrace();
            return "";
        }

        httpConn.setRequestProperty("Content-Type", "application/json");
        httpConn.setRequestProperty("Accept-Charset", "UTF-8");
        httpConn.setConnectTimeout(MAX_CONNECTION_TIMEOUT);
        httpConn.setReadTimeout(MAX_SOCKET_TIMEOUT);
        JSONArray json1 = new JSONArray();
        JSONObject k = new JSONObject();
        if (kioskPayDataInfo.transType.equalsIgnoreCase("D1")) {
            k.put("trtype", "AD");
        } else if (kioskPayDataInfo.transType.equalsIgnoreCase("D4")) {
            k.put("trtype", "CA");
        } else if (kioskPayDataInfo.transType.equalsIgnoreCase("RF")) {
            k.put("trtype", "RF");
        } else {
            k.put("trtype", "DE");
        }

        k.put("tid", kioskPayDataInfo.tid);
        k.put("mid", kioskPayDataInfo.mid);
        k.put("fnCd", kioskPayDataInfo.fnCd);
        k.put("fnName", kioskPayDataInfo.fnName);
        k.put("fnCd1", kioskPayDataInfo.fnCd1);
        k.put("fnName1", kioskPayDataInfo.fnName1);
        k.put("storeId", kioskPayDataInfo.storeIdpay);
        k.put("cnt", kioskPayDataInfo.totalindex);
        k.put("amt", kioskPayDataInfo.goodsAmt);
        k.put("num", kioskPayDataInfo.orderNumber);
        k.put("date", kioskPayDataInfo.orderDate);
        k.put("authCode", kioskPayDataInfo.authCode);
        k.put("deviceId", kioskPayDataInfo.deviceId);
        if (!kioskPayDataInfo.transType.equalsIgnoreCase("RF")) {
            if (kioskPayDataInfo.payMethod.equalsIgnoreCase("1")) {
                k.put("payMethod", "CARD");
            } else if (kioskPayDataInfo.payMethod.equalsIgnoreCase("2")) {
                k.put("payMethod", "CASH");
            } else {
                k.put("payMethod", kioskPayDataInfo.payMethod);
            }
        } else {
            k.put("payMethod", "RF");
        }

        k.put("tel", kioskPayDataInfo.telephone);
        if (kioskPayDataInfo.paOrderId == null) {
            kioskPayDataInfo.paOrderId = "";
        }

        k.put("paOrderId", kioskPayDataInfo.paOrderId);
        JSONArray json = new JSONArray();

        JSONObject j;
        for(int i = 0; i < orderlist.size(); ++i) {
            j = new JSONObject();
            DataModel order = (DataModel)orderlist.get(i);

            String log;
            try {
                j.put("id", order.getProductId());
            } catch (JSONException var35) {
                var35.printStackTrace();
                log = String.format("HttpKioskResponseString id Error e=%s", var35.toString());
                FileUtils.writeDebug(log, "PayCast");
                return "";
            }

            try {
                j.put("name", order.getText());
            } catch (JSONException var34) {
                var34.printStackTrace();
                log = String.format("HttpKioskResponseString name Error e=%s", var34.toString());
                FileUtils.writeDebug(log, "PayCast");
                return "";
            }

            try {
                j.put("count", String.valueOf(order.getCount()));
            } catch (JSONException var33) {
                var33.printStackTrace();
                log = String.format("HttpKioskResponseString count Error e=%s", var33.toString());
                FileUtils.writeDebug(log, "PayCast");
                return "";
            }

            float price = 0.0F;
            if (order.getPrice() != null && !order.getPrice().isEmpty()) {
                price = Float.valueOf(order.getPrice());
            }

            int count = Integer.valueOf(order.getCount());
            String reqString;
            if (price != 0.0F && count != 0) {
                try {
                    j.put("price", String.valueOf(price / (float)count));
                } catch (JSONException var32) {
                    var32.printStackTrace();
                    reqString = String.format("HttpKioskResponseString 1 price Error e=%s", var32.toString());
                    FileUtils.writeDebug(reqString, "PayCast");
                    return "";
                }
            } else {
                try {
                    j.put("price", "0");
                } catch (JSONException var31) {
                    var31.printStackTrace();
                    reqString = String.format("HttpKioskResponseString 2 price Error e=%s", var31.toString());
                    FileUtils.writeDebug(reqString, "PayCast");
                    return "";
                }
            }

            String isPackage = "0";
            if (order.isPackage()) {
                isPackage = "1";
            }

            String addString;
            try {
                j.put("pack", isPackage);
            } catch (JSONException var30) {
                var30.printStackTrace();
                addString = String.format("HttpKioskResponseString pack Error e=%s", var30.toString());
                FileUtils.writeDebug(addString, "PayCast");
                return "";
            }

            reqString = "";
            addString = "";
            String oldAddOptId = "";
            if (order.getOptionList() != null && order.getOptionList().size() > 0) {
                for(int c = 0; c < order.getOptionList().size(); ++c) {
                    CustomOptionData listItem = (CustomOptionData) order.getOptionList().get(c);
                    int r;
                    if (listItem.getReqTitle() != null && !listItem.getReqTitle().isEmpty() && listItem.getRequiredOptList() != null && listItem.getRequiredOptList().size() > 0) {
                        for(r = 0; r < listItem.getRequiredOptList().size(); ++r) {
                            CustomRequiredOptionData itemReqData = (CustomRequiredOptionData) listItem.getRequiredOptList().get(r);
                            if (!reqString.isEmpty()) {
                                reqString = reqString + ",";
                            }

                            reqString = String.format("%s%s_%s(%s)", reqString, itemReqData.getOptId(), itemReqData.getOptMenuName(), itemReqData.getOptMenuPrice());
                        }
                    }

                    if (listItem.getAddTitle() != null && !listItem.getAddTitle().isEmpty() && listItem.getAddOptList() != null && listItem.getAddOptList().size() > 0) {
                        for(r = 0; r < listItem.getAddOptList().size(); ++r) {
                            CustomAddedOptionData itemAddData = (CustomAddedOptionData) listItem.getAddOptList().get(r);
                            if (!addString.isEmpty()) {
                                addString = addString + "||";
                            } else {
                                addString = String.format("%s%s_", addString, itemAddData.getOptId());
                                oldAddOptId = itemAddData.getOptId();
                            }

                            if (oldAddOptId.equalsIgnoreCase(itemAddData.getOptId())) {
                                addString = String.format("%s%s(%s)", addString, itemAddData.getOptMenuName(), itemAddData.getOptMenuPrice());
                            } else {
                                addString = String.format("%s,%s_%s(%s)", addString, itemAddData.getOptId(), itemAddData.getOptMenuName(), itemAddData.getOptMenuPrice());
                                oldAddOptId = itemAddData.getOptId();
                            }
                        }
                    }
                }
            }

            try {
                j.put("ess", reqString);
            } catch (JSONException var29) {
                var29.printStackTrace();
                log = String.format("HttpKioskResponseString ess Error e=%s", var29.toString());
                FileUtils.writeDebug(log, "PayCast");
                return "";
            }

            try {
                j.put("add", addString);
            } catch (JSONException var28) {
                var28.printStackTrace();
                log = String.format("HttpKioskResponseString add Error e=%s", var28.toString());
                FileUtils.writeDebug(log, "PayCast");
                return "";
            }

            json.put(j);
        }

        k.put("menu", json);
        json1.put(k);
        OutputStream os = null;

        try {
            os = httpConn.getOutputStream();
        } catch (IOException var27) {
            var27.printStackTrace();
            return "";
        }

        try {
            os.write(json1.toString().getBytes("UTF-8"));
        } catch (IOException var26) {
            var26.printStackTrace();
            return "";
        }

        try {
            os.flush();
        } catch (IOException var25) {
            var25.printStackTrace();
            return "";
        }

        j = null;
        int resCode;
        try {
            resCode = httpConn.getResponseCode();
        } catch (IOException var24) {
            var24.printStackTrace();
            return "";
        }

        Log.e("NetworkUtil", "HttpKioskResponseString() resCode=" + resCode);
        if (resCode == 200 || resCode == 201) {
            BufferedReader in;
            try {
                in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
            } catch (IOException var23) {
                var23.printStackTrace();
                return "";
            }

            while(true) {
                try {
                    if ((inputLine = in.readLine()) == null) {
                        break;
                    }
                } catch (IOException var39) {
                    var39.printStackTrace();
                    return "";
                }

                outResult.append(inputLine);
            }
        }

        return outResult.toString();
    }
}
