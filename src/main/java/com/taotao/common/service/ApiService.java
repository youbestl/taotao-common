package com.taotao.common.service;

import com.taotao.common.httpclient.HttpResult;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by JARVIS on 2017/6/13.
 */
@Service
public class ApiService implements BeanFactoryAware {

    /*@Autowired
    private CloseableHttpClient httpclient;*/

    @Autowired(required = false)
    private RequestConfig requestConfig;

    private BeanFactory beanFactory;

    /**
     * GET 请求地址 响应200 返回响应的内容，如果响应404/500 则返回 Null
     *
     * @param url
     * @return
     * @throws IOException
     */
    public String doGet(String url) throws IOException {

        // 创建http GET请求 --->相当于在地址栏输入URL
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            // 执行请求 --->相当于按下回车
            response = getHttpclient().execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } finally {
            //相当于关闭浏览器释放资源
            if (response != null) {
                response.close();
            }
        }
        return null;
    }

    /**
     * 带参数的 doGet 方法
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public String doGet(String url, Map<String, String> params) throws IOException, URISyntaxException {
        URIBuilder builder = new URIBuilder(url);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.setParameter(entry.getKey(), entry.getValue());
        }
        return this.doGet(builder.build().toString());
    }

    /**
     * 带有参数的 post 请求
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public HttpResult doPost(String url, Map<String, String> params) throws IOException {

        // 创建http POST请求
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(this.requestConfig);
        if (null != params) {
            List<NameValuePair> parameters = new ArrayList<NameValuePair>(0);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            // 构造一个form表单式的实体
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);
            // 将请求实体设置到httpPost对象中
            httpPost.setEntity(formEntity);
        }
        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = getHttpclient().execute(httpPost);
            return new HttpResult(response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity(), "UTF-8"));
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * 没有参数的 post 请求
     * @param url
     * @return
     * @throws IOException
     */
    public HttpResult doPost(String url) throws IOException {
        return this.doPost(url, null);
    }

    public CloseableHttpClient getHttpclient() {
        return this.beanFactory.getBean(CloseableHttpClient.class);
    }

    //在单例对象中使用多例对象,不能使用spring注入，而使用 beanFactory 从spring容器中拿到对象
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        //该方法会在spring初始化该bean的调用该方法，传入beanFactory
        this.beanFactory = beanFactory;
    }

    /**
     * 带有 json 参数的 post 请求
     * @param url
     * @param json
     * @return
     * @throws IOException
     */
    public HttpResult doPostJson(String url, String json) throws IOException {

        // 创建http POST请求
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(this.requestConfig);
        if (null != json) {
            // 构造一个form表单式的实体
            StringEntity stringEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
            // 将请求实体设置到httpPost对象中
            httpPost.setEntity(stringEntity);
        }
        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = getHttpclient().execute(httpPost);
            return new HttpResult(response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity(), "UTF-8"));
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
