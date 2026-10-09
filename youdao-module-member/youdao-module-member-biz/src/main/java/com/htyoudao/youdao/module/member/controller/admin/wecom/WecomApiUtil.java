package com.htyoudao.youdao.module.member.controller.admin.wecom;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.htyoudao.youdao.module.member.controller.admin.wecom.config.WecomConfig;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class WecomApiUtil {

    private static final String GET_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";
    private static final String GET_GROUP_LIST_URL = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/groupchat/list";
    private static final String GET_GROUP_MEMBER_URL = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/groupchat/get";
    private static final String UNIONID_TO_EXTERNAL_USERID_URL = "https://qyapi.weixin.qq.com/cgi-bin/corpgroup/unionid_to_external_userid";
    private static final String BATCH_GET_EXTERNAL_CONTACT_URL = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/batch/get_by_user";
    private static final String GET_EXTERNAL_CONTACT_URL = "https://qyapi.weixin.qq.com/cgi-bin/externalcontact/get";

    @Resource
    private WecomConfig wecomConfig;

    public static void main(String[] args) throws IOException {
        WecomApiUtil wecomApiUtil = new WecomApiUtil();
        wecomApiUtil.wecomConfig = new WecomConfig();

        // 设置要处理的群ID列表
        System.out.println(wecomApiUtil.getGroupDetail("wriyNIVAAAn6793MYyeOTWB2k8w0KxWQ"));
    }


    /**
     * 获取企业微信access_token
     */
    public String getAccessToken() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(GET_TOKEN_URL + "?corpid=" + wecomConfig.getCorpId() + "&corpsecret=" + wecomConfig.getCorpSecret());
        CloseableHttpResponse response = httpClient.execute(httpGet);
        String result = EntityUtils.toString(response.getEntity());
        JSONObject jsonObject = JSON.parseObject(result);
        return jsonObject.getString("access_token");
    }



    /**
     * 获取客户群列表
     */
    public String unionidToExternalUserid() throws IOException {
        String accessToken = getAccessToken();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(UNIONID_TO_EXTERNAL_USERID_URL + "?access_token=" + accessToken);


        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("unionid", "oreXa69A2WxSPMcjB-4Fe73CO5YQ");
        requestBody.put("openid", "oHVqn6yZxaiB23y9oX5yKBsXDvN8");
//        requestBody.put("corpid", wecomConfig.getCorpId());

        StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");

        CloseableHttpResponse response = httpClient.execute(httpPost);
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * 获取客户群列表
     */
    public Set<String> getGroupList() throws IOException {
        String accessToken = getAccessToken();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(GET_GROUP_LIST_URL + "?access_token=" + accessToken);
        
        // 构建请求体
        Set<String> chatIds = new HashSet<>();
        String nextCursor = null;
        do {
            JSONObject requestBody = new JSONObject();
            requestBody.put("limit", 1000);
            requestBody.put("cursor", nextCursor);
            StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");

            CloseableHttpResponse response = httpClient.execute(httpPost);
            String result = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = JSON.parseObject(result);
            nextCursor = jsonObject.getString("next_cursor");
            JSONArray groupList = jsonObject.getJSONArray("group_chat_list");
            for (int i = 0; i < groupList.size(); i++) {
                JSONObject group = groupList.getJSONObject(i);
                chatIds.add(group.getString("chat_id"));
            }
        }while (nextCursor != null);

        return chatIds;
    }

    /**
     * 获取客户群详情（包含成员列表和版本号）
     * @return 返回完整的响应对象，包含 errcode、errmsg 和 group_chat
     */
    public JSONObject getGroupDetail(String chatId) throws IOException {
        String accessToken = getAccessToken();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(GET_GROUP_MEMBER_URL + "?access_token=" + accessToken);

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("chat_id", chatId);

        StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");

        CloseableHttpResponse response = httpClient.execute(httpPost);
        String result = EntityUtils.toString(response.getEntity());
        JSONObject jsonObject = JSON.parseObject(result);
        return jsonObject;
    }


    /**
     * 获取外部联系人详情
     */
    public JSONArray getExternalContactArray(List<String> userIds) throws IOException {
        String accessToken = getAccessToken();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(BATCH_GET_EXTERNAL_CONTACT_URL + "?access_token=" + accessToken);

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("userid_list", userIds);

        StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");

        CloseableHttpResponse response = httpClient.execute(httpPost);
        String result = EntityUtils.toString(response.getEntity());
        JSONObject jsonObject = JSON.parseObject(result);
        return jsonObject.getJSONArray("external_contact_list");
    }


    /**
     * 获取外部联系人详情
     */
    public JSONObject getExternalContactDetail(String userId) throws IOException {
        String accessToken = getAccessToken();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(
            GET_EXTERNAL_CONTACT_URL
            + "?access_token=" + accessToken
            + "&external_userid=" + userId
        );


        CloseableHttpResponse response = httpClient.execute(httpGet);
        String result = EntityUtils.toString(response.getEntity());
        JSONObject jsonObject = JSON.parseObject(result);
        return jsonObject;
    }



    /**
     * 获取群成员列表
     */
    public JSONArray getGroupMembers(String chatId) throws IOException {
        JSONObject response = getGroupDetail(chatId);
        JSONObject groupDetail = response.getJSONObject("group_chat");
        return groupDetail.getJSONArray("member_list");
    }


}
