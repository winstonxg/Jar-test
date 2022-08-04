package com.github.catvod.spider;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.Misc;
import com.github.catvod.utils.okhttp.OKCallBack;
import com.github.catvod.utils.okhttp.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.Response;

/**
 * Author: @SDL
 */
public class Kmys extends Spider {
    private String apiDomain = "";
    private String staticDomain = "";

    private String appId = "5"; // 飞瓜 1 酷猫 5

    private String device = "8094a1cc05b48ed0dfda3d9dc0b2077f1657938026279";

    @Override
    public void init(Context context) {
        super.init(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences("sp_Kmys", Context.MODE_PRIVATE);
        try {
            device = sharedPreferences.getString("device", null);
        } catch (Throwable th) {
        } finally {
            if (device == null) {
                device = Misc.MD5(UUID.randomUUID().toString(), Misc.CharsetUTF8).toLowerCase();
                sharedPreferences.edit().putString("device", device).commit();
            }
        }
    }

    private HashMap<String, String> getHeaders(String url) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("versionNumber", "360");
        headers.put("versionName", "3.6.0");
        headers.put("device", device);
        headers.put("ed", device);
        headers.put("appId", appId);
        headers.put("platformId", "7");
        headers.put("User-Agent", "okhttp/3.14.7");
        return headers;
    }

    private void checkDomain() {
        if (staticDomain.isEmpty()) {
            String url = "http://feigua2021.oss-cn-beijing.aliyuncs.com/static/config/video/" + appId + ".json";
            HashMap<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "okhttp/3.14.7");
            headers.put("ed", device);
            String json = OkHttpUtil.string(url, headers);
            try {
                JSONObject obj = new JSONObject(json);
                apiDomain = obj.getString("apiDomain");
                staticDomain = obj.getString("staticDomain");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            checkDomain();
            String url = staticDomain + "/static/" + appId + "/config/lib-new.json";
            String content = OkHttpUtil.string(url, getHeaders(url));
            JSONObject jsonObject = new JSONObject(content).getJSONObject("data");

            JSONArray years = jsonObject.getJSONArray("year");
            JSONArray types = jsonObject.getJSONArray("types");

            JSONArray classes = new JSONArray();
            JSONObject filterConfig = new JSONObject();

            for (int i = 0; i < types.length(); i++) {
                JSONObject item = types.getJSONObject(i);
                String typeId = item.getString("typeId").trim();
                if (typeId.isEmpty())
                    continue;
                String typeName = item.getString("typeName");
                JSONObject newCls = new JSONObject();
                newCls.put("type_id", typeId);
                newCls.put("type_name", typeName);
                classes.put(newCls);

                JSONArray tags = item.getJSONArray("tags");
                JSONArray extendsAll = new JSONArray();
                // 类型
                JSONObject newTypeExtend = new JSONObject();
                newTypeExtend.put("key", "type");
                newTypeExtend.put("name", "类型");
                JSONArray newTypeExtendKV = new JSONArray();
                JSONObject kv = new JSONObject();
                kv.put("n", "全部");
                kv.put("v", "-1");
                newTypeExtendKV.put(kv);
                for (int j = 0; j < tags.length(); j++) {
                    JSONObject child = tags.getJSONObject(j);
                    kv = new JSONObject();
                    kv.put("n", child.getString("typeName"));
                    kv.put("v", child.getString("typeId"));
                    newTypeExtendKV.put(kv);
                }
                newTypeExtend.put("value", newTypeExtendKV);
                extendsAll.put(newTypeExtend);
                // 排序
                newTypeExtend = new JSONObject();
                newTypeExtend.put("key", "sort");
                newTypeExtend.put("name", "排序");
                newTypeExtendKV = new JSONArray();
                kv = new JSONObject();
                kv.put("n", "最热");
                kv.put("v", "2");
                newTypeExtendKV.put(kv);
                kv = new JSONObject();
                kv.put("n", "最新");
                kv.put("v", "1");
                newTypeExtendKV.put(kv);
                kv = new JSONObject();
                kv.put("n", "好评");
                kv.put("v", "3");
                newTypeExtendKV.put(kv);
                newTypeExtend.put("value", newTypeExtendKV);
                extendsAll.put(newTypeExtend);
                // 地区
                JSONArray regions = item.getJSONArray("children");
                newTypeExtend = new JSONObject();
                newTypeExtend.put("key", "area");
                newTypeExtend.put("name", "地区");
                newTypeExtendKV = new JSONArray();
                kv = new JSONObject();
                kv.put("n", "全部");
                kv.put("v", "-1");
                newTypeExtendKV.put(kv);
                for (int j = 0; j < regions.length(); j++) {
                    JSONObject child = regions.getJSONObject(j);
                    kv = new JSONObject();
                    kv.put("n", child.getString("typeName"));
                    kv.put("v", child.getString("typeId"));
                    newTypeExtendKV.put(kv);
                }
                newTypeExtend.put("value", newTypeExtendKV);
                extendsAll.put(newTypeExtend);

                // 年份
                newTypeExtend = new JSONObject();
                newTypeExtend.put("key", "year");
                newTypeExtend.put("name", "年份");
                newTypeExtendKV = new JSONArray();
                kv = new JSONObject();
                kv.put("n", "全部");
                kv.put("v", "-1");
                newTypeExtendKV.put(kv);
                for (int j = 0; j < years.length(); j++) {
                    String year = years.getString(j).trim();
                    if (year.isEmpty())
                        continue;
                    kv = new JSONObject();
                    kv.put("n", year);
                    kv.put("v", year);
                    newTypeExtendKV.put(kv);
                }
                newTypeExtend.put("value", newTypeExtendKV);
                extendsAll.put(newTypeExtend);
                filterConfig.put(typeId, extendsAll);
            }
            JSONObject result = new JSONObject();
            result.put("class", classes);
            if (filter) {
                result.put("filters", filterConfig);
            }
            return result.toString();
        } catch (Throwable th) {

        }
        return "";
    }

    @Override
    public String homeVideoContent() {
        try {
            checkDomain();
            String url = staticDomain + "/static/" + appId + "/index/cloumn/1.json";
            String content = OkHttpUtil.string(url, getHeaders(url));
            JSONObject jsonObject = new JSONObject(content).getJSONObject("data");
            JSONArray list = jsonObject.getJSONArray("list");
            JSONArray videos = new JSONArray();
            for (int i = 0; i < list.length(); i++) {
                JSONArray videoList = list.getJSONObject(i).getJSONArray("videoList");
                for (int j = 0; j < videoList.length(); j++) {
                    JSONObject vObj = videoList.getJSONObject(j);
                    JSONObject v = new JSONObject();
                    v.put("vod_id", vObj.getString("id"));
                    v.put("vod_name", vObj.getString("vodName"));
                    v.put("vod_pic", fixUrl(vObj.getString("vodPicThumb")));
                    v.put("vod_remarks", vObj.getString("lastName"));
                    videos.put(v);
                }
            }
            JSONObject result = new JSONObject();
            result.put("list", videos);
            return result.toString();
        } catch (Throwable th) {

        }
        return "";
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
            checkDomain();
            JSONObject result = new JSONObject();

            String url = apiDomain + "/videolibrary/v2/" + appId + "/" + tid;
            extend = extend == null ? new HashMap<>() : extend;
            if (!extend.containsKey("area")) {
                extend.put("area", "-1");
            }
            if (!extend.containsKey("type")) {
                extend.put("type", "-1");
            }
            if (!extend.containsKey("sort")) {
                extend.put("sort", "2");
            }
            if (!extend.containsKey("year")) {
                extend.put("year", "-1");
            }
            url += "/" + extend.get("area");
            url += "/" + extend.get("type");
            url += "/" + extend.get("sort");
            url += "/-1";
            url += "/" + extend.get("year");
            url += "/-1/" + pg + ".json";

            String content = OkHttpUtil.string(url, getHeaders(url));
            JSONObject jsonObject = new JSONObject(content).getJSONObject("data");
            JSONArray jsonArray = jsonObject.getJSONArray("list");
            JSONArray videos = new JSONArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject vObj = jsonArray.getJSONObject(i);
                JSONObject v = new JSONObject();
                v.put("vod_id", vObj.getString("id"));
                v.put("vod_name", vObj.getString("vodName"));
                v.put("vod_pic", fixUrl(vObj.getString("vodPicThumb")));
                v.put("vod_remarks", vObj.getString("lastName"));
                videos.put(v);
            }

            int total = jsonObject.getInt("count");
            int limit = jsonObject.getInt("pageSize");
            int totalPg = total % limit == 0 ? (total / limit) : (total / limit + 1);
            result.put("page", jsonObject.getInt("pageIndex"));
            result.put("pagecount", totalPg);
            result.put("limit", limit);
            result.put("total", total);
            result.put("list", videos);
            return result.toString();
        } catch (Throwable th) {

        }
        return "";
    }

    @Override
    public String detailContent(List<String> ids) {
        try {
            checkDomain();
            JSONObject result = new JSONObject();
            String url = staticDomain + "/static/video/detail/" + ids.get(0) + ".json";
            String content = OkHttpUtil.string(url, getHeaders(url));
            JSONObject jsonObject = new JSONObject(content).getJSONObject("data");
            JSONObject vodList = new JSONObject();
            vodList.put("vod_id", jsonObject.getString("id"));
            vodList.put("vod_name", jsonObject.getString("vodName"));
            vodList.put("vod_pic", fixUrl(jsonObject.getString("vodPic")));
            vodList.put("type_name", jsonObject.getString("typeName"));
            vodList.put("vod_year", jsonObject.getString("vodYear"));
            vodList.put("vod_area", jsonObject.getString("vodArea"));
            vodList.put("vod_remarks", "");
            vodList.put("vod_actor", jsonObject.getString("vodActor"));
            vodList.put("vod_director", "");
            vodList.put("vod_content", jsonObject.getString("vodBlurb"));

            String vodPlayUrl = jsonObject.getString("vodPlayUrl");
            vodPlayUrl = rsa(vodPlayUrl.substring(0, 10) + vodPlayUrl.substring(16));

            ArrayList<String> playFrom = new ArrayList<>();
            ArrayList<String> playList = new ArrayList<>();

            JSONArray playSource = new JSONArray(vodPlayUrl);
            for (int i = 0; i < playSource.length(); i++) {
                JSONObject item = playSource.getJSONObject(i);
                String srcName = item.getString("name");
                boolean userProxy = srcName.equals("高速线路");
                ArrayList<String> urls = new ArrayList<>();
                JSONArray playUrls = item.getJSONArray("list");
                for (int j = 0; j < playUrls.length(); j++) {
                    JSONObject urlObj = playUrls.getJSONObject(j);
                    if (userProxy)
                        urls.add(urlObj.getString("name") + "$" + Proxy.localProxyUrl() + "?do=kmys&type=m3u8&url=" + Base64.encodeToString(urlObj.getString("url").getBytes(Misc.CharsetUTF8), Base64.DEFAULT | Base64.URL_SAFE | Base64.NO_WRAP) + ".." + urlObj.getInt("isParse"));
                    else
                        urls.add(urlObj.getString("name") + "$" + urlObj.getString("url") + ".." + urlObj.getInt("isParse"));
                }
                if (urls.isEmpty())
                    continue;
                playFrom.add(srcName);
                playList.add(TextUtils.join("#", urls));
            }

            String vod_play_from = TextUtils.join("$$$", playFrom);
            String vod_play_url = TextUtils.join("$$$", playList);
            vodList.put("vod_play_from", vod_play_from);
            vodList.put("vod_play_url", vod_play_url);

            JSONArray list = new JSONArray();
            list.put(vodList);
            result.put("list", list);
            return result.toString();
        } catch (Throwable th) {

        }
        return "";
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            JSONObject result = new JSONObject();
            String[] info = id.split("\\.\\.");
            String url = info[0];
            int isParse = Integer.parseInt(info[1]);
            if (isParse == 2) {
                result.put("parse", 0);
                result.put("playUrl", "");
                result.put("url", url);
            } else {
                result.put("parse", 0);
                result.put("playUrl", "");
                result.put("url", url);
            }
            return result.toString();
        } catch (Throwable th) {

        }
        return "";
    }

    @Override
    public String searchContent(String key, boolean quick) {
        try {
            checkDomain();
            JSONObject result = new JSONObject();
            String url = apiDomain + "/api/v2/index/search?pageIndex=1&wd=" + URLEncoder.encode(key) + "&limit=10&type=1";
            String content = OkHttpUtil.string(url, getHeaders(url));
            JSONArray videos = new JSONArray();
            JSONObject jsonObject = new JSONObject(content).getJSONObject("data");
            JSONArray jsonArray = jsonObject.getJSONArray("list");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject vObj = jsonArray.getJSONObject(i);
                JSONObject v = new JSONObject();
                String title = vObj.getString("vodName");
                if (!title.contains(key))
                    continue;
                v.put("vod_id", vObj.getString("id"));
                v.put("vod_name", title);
                v.put("vod_pic", fixUrl(vObj.getString("vodPicThumb")));
                v.put("vod_remarks", vObj.getString("lastName"));
                videos.put(v);
            }
            result.put("list", videos);
            return result.toString();
        } catch (Throwable th) {

        }
        return "";
    }

    protected String fixUrl(String src) {
        try {
            if (src.startsWith("//")) {
                Uri parse = Uri.parse(staticDomain);
                src = parse.getScheme() + ":" + src;
            } else if (!src.contains("://")) {
                Uri parse = Uri.parse(staticDomain);
                if (!src.startsWith("/"))
                    src = "/" + src;
                src = parse.getScheme() + "://" + parse.getHost() + src;
            }
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return src;
    }

   public static void getkey(){
        if (signPlayerStr.isEmpty()){
            ////https://video-api.kumaoys.cn/api/v2/b/83708861
            String url = "https://mtv.stvmts.com/api/v2/b/"+((int) (Math.random() * 100000000));
            HashMap hashMap = new HashMap();
            hashMap.put("versionNumber", "360");
            hashMap.put("versionName", "3.6.0");
            hashMap.put("device", "8094a1cc05b48ed0dfda3d9dc0b2077f1657938026279");
            //hashMap.put("appId", "5");
            hashMap.put("platformId", "7");
            hashMap.put("User-Agent", "okhttp/3.14.7");
            hashMap.put("Pragma", "Pragma:: no-cache");
            hashMap.put("Cache-Control", "no-cache");
            hashMap.put("Host", "mtv.stvmts.com");
            hashMap.put("Content-Type", "application/json; charser=utf-8");
            JSONObject jsonObject = new JSONObject();
            try {
                int random = (int)(Math.random()*1.0E8d);
                int time = (int)(System.currentTimeMillis()/1000);
                String signBefore = "p=com.kumao.yingshi&t=" + time + "&r=" + random + "&s=36eff39894f62d333fd3f488cffbf364&pl=1";
                jsonObject.put("s",Misc.MD5(signBefore,Misc.CharsetUTF8));
                jsonObject.put("t",time);
                jsonObject.put("r",random);
                jsonObject.put("i",5);
                jsonObject.put("p",1);
                OkHttpUtil.postJson(OkHttpUtil.defaultClient(), url, jsonObject.toString(), hashMap, new OKCallBack.OKCallBackString() {
                    @Override
                    public void onFailure(Call call, Exception e) {
                    }

                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject jsonObject = new JSONObject(response).getJSONObject("data");
                            String a = new String(Base64.decode(jsonObject.getString("a"),Base64.DEFAULT));
                            String k = new String(Base64.decode(jsonObject.getString("k"),Base64.DEFAULT));
                            String z = new String(Base64.decode(jsonObject.getString("z"),Base64.DEFAULT));
                            String data = RSAUtils.decryptByPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCt/dLGQj1Iimj0LIUMUXgBGUjsfrm6o1/pZjXXVLL3py2vLktNtSoJU+69v1tUXZqiU9BqMHApVmMOtOnkL5J+ENdLIX3bXnNtfNJpYX4Iz8OBMqKdDch80gN8rLkTPReFkBGsMAndKpc0iMdgd6nts/gQ3wUBNJKpmOG35UateQIDAQAB",k+z+a);
                            signPlayerStr = new JSONObject(data).optString("key");
                        } catch (JSONException e) {
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
        OkHttpUtil.get(OkHttpUtil.defaultClient(), realUrl, null, kmysPlayerHeaders, callBack);
        if (callBack.getResult().code() == 200) {
            Headers headers = callBack.getResult().headers();
            String type = headers.get("Content-Type");
            if (type == null) {
                type = "application/octet-stream";
            }
            Object[] result = new Object[3];
            result[0] = 200;
            result[1] = type;
            result[2] = callBack.getResult().body().byteStream();
            return result;
        }
        return null;
    }

    public static Object[] vod(Map<String, String> params) {
        String type = params.get("type");
        String url = params.get("url");
        url = new String(Base64.decode(url, Base64.DEFAULT | Base64.URL_SAFE | Base64.NO_WRAP), Misc.CharsetUTF8);
        if (kmysPlayerHeaders == null) {
            kmysPlayerHeaders = new HashMap<>();
            kmysPlayerHeaders.put("User-Agent", "okhttp/3.14.7");
            kmysPlayerHeaders.put("ed", "8094a1cc05b48ed0dfda3d9dc0b2077f1657938026279");
            kmysPlayerHeaders.put("Connection", "close");
        }
        if (type.equals("m3u8")) {
            return getVodContent(url);
        } else if (type.equals("key")) {
            return getKeyContent(url);
        } else if (type.equals("ts")) {
            return getTsContent(url);
        }
        return null;
    }
}