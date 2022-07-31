package com.github.catvod.spider;

import android.util.Base64;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.spider.merge.FF;
import com.github.catvod.spider.merge.r9;
import java.io.ByteArrayInputStream;
import java.util.Map;

public class Proxy extends Spider {
    public static int d = -1;

    public static Object[] proxy(Map<String, String> map) {
        try {
            String str = (String) map.get("do");
            String str2 = "UTF-8";
            if (str.equals("live")) {
                if (((String) map.get("type")).equals("txt")) {
                    String str3 = (String) map.get("ext");
                    if (!str3.startsWith("http")) {
                        str3 = new String(Base64.decode(str3, 10), str2);
                    }
                    return r9.E(str3);
                }
            } else if (str.equals("ck")) {
                return new Object[]{Integer.valueOf(200), "text/plain; charset=utf-8", new ByteArrayInputStream("ok".getBytes(str2))};
            } else if (str.equals("push")) {
                return PushAgent.vod(map);
            } else {
                if (str.equals("kmys")) {
                    return Kmys.vod(map);
                }
                if (str.equals("czspp")) {
                    return Czsapp.loadsub((String) map.get("url"));
                }
            }
        } catch (Throwable unused) {
        }
        return null;
    }

    static void d() {
        if (d <= 0) {
            for (int i = 9978; i < 10000; i++) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("http://127.0.0.1:");
                stringBuilder.append(i);
                stringBuilder.append("/proxy?do=ck");
                if (FF.pW(stringBuilder.toString(), null).equals("ok")) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Found local server port ");
                    stringBuilder.append(i);
                    SpiderDebug.log(stringBuilder.toString());
                    d = i;
                    break;
                }
            }
        }
    }

    public static String localProxyUrl() {
        d();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://127.0.0.1:");
        stringBuilder.append(d);
        stringBuilder.append("/proxy");
        return stringBuilder.toString();
    }
}