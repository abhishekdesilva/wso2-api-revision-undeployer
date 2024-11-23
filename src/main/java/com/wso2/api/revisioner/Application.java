package com.wso2.api.revisioner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;

import com.wso2.api.revisioner.utils.FileUtils;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.http.HttpHeaders;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.json.JSONObject;
import org.springframework.context.ConfigurableApplicationContext;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@SpringBootApplication
public class Application {

    private static String HOST = "";
    private static String TRANSPORT_PORT = "";
    private static String CLIENT_KEY = "";
    private static String CLIENT_SECRET = "";
    private static String USERNAME = "";
    private static String PASSWORD = "";
    private static String API_LIMIT = "";
    private static String PUBLISHER_CONTEXT = "";
    private static String NEW_GATEWAY_NAME = "";
    private static String OLD_GATEWAY_NAME = "";

    private static String APPLICATION_HOST = "application.host";
    private static String APPLICATION_TRANSPORT_PORT = "application.transport.port";
    private static String APPLICATION_CLIENT_KEY = "application.client.key";
    private static String APPLICATION_CLIENT_SECRET = "application.client.secret";
    private static String APPLICATION_USERNAME = "application.username";
    private static String APPLICATION_PASSWORD = "application.password";
    private static String APPLICATION_API_LIMIT = "application.api.limit";
    private static String APPLICATION_PUBLISHER_CONTEXT = "application.publisher.context";
    private static String APPLICATION_NEW_GW_NAME = "application.new.gw.name";
    private static String APPLICATION_OLD_GW_NAME = "application.old.gw.name";

    private static String GATEWAY_NAME = "";
    private static String VHOST = "";
    private static boolean DISPLAY_ON_DEV_PORTAL = true;

    static {
        Properties properties = FileUtils.readConfiguration();
        HOST = properties.getProperty(APPLICATION_HOST, "");
        TRANSPORT_PORT = properties.getProperty(APPLICATION_TRANSPORT_PORT, "");
        CLIENT_KEY = properties.getProperty(APPLICATION_CLIENT_KEY, "");
        CLIENT_SECRET = properties.getProperty(APPLICATION_CLIENT_SECRET, "");
        USERNAME = properties.getProperty(APPLICATION_USERNAME, "");
        PASSWORD = properties.getProperty(APPLICATION_PASSWORD, "");
        API_LIMIT = properties.getProperty(APPLICATION_API_LIMIT, "");
        PUBLISHER_CONTEXT = properties.getProperty(APPLICATION_PUBLISHER_CONTEXT, "");
        NEW_GATEWAY_NAME = properties.getProperty(APPLICATION_NEW_GW_NAME, "");
        OLD_GATEWAY_NAME = properties.getProperty(APPLICATION_OLD_GW_NAME, "");
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        try {
            fw = FileUtils.getNewFileWriter();
            bw = new BufferedWriter(fw);
            pw = new PrintWriter(bw);
            System.out.println("HOST : " + HOST);
            System.out.println("TRANSPORT_PORT : " + TRANSPORT_PORT);
            System.out.println("CLIENT_KEY : " + CLIENT_KEY);
            System.out.println("CLIENT_SECRET : " + CLIENT_SECRET);
            System.out.println("USERNAME : " + USERNAME);
            System.out.println("PASSWORD : " + PASSWORD);
            System.out.println("API_LIMIT : " + API_LIMIT);
            System.out.println("API_LIMIT : " + PUBLISHER_CONTEXT);
            System.out.println("API_LIMIT : " + NEW_GATEWAY_NAME);
            System.out.println("API_LIMIT : " + OLD_GATEWAY_NAME);

            pw.println("HOST : " + HOST);
            pw.println("TRANSPORT_PORT : " + TRANSPORT_PORT);
            pw.println("CLIENT_KEY : " + CLIENT_KEY);
            pw.println("CLIENT_SECRET : " + CLIENT_SECRET);
            pw.println("USERNAME : " + USERNAME);
            pw.println("PASSWORD : " + PASSWORD);
            pw.println("API_LIMIT : " + API_LIMIT);
            pw.println("PUBLISHER_CONTEXT : " + PUBLISHER_CONTEXT);
            pw.println("NEW_GATEWAY_NAME : " + NEW_GATEWAY_NAME);
            pw.println("OLD_GATEWAY_NAME : " + OLD_GATEWAY_NAME);

            if (!HOST.equals("") && !TRANSPORT_PORT.equals("") && !CLIENT_KEY.equals("") && !CLIENT_SECRET.equals("")
                    && !USERNAME.equals("") && !PASSWORD.equals("") && !API_LIMIT.equals("") && !PUBLISHER_CONTEXT.equals("")
                    && !NEW_GATEWAY_NAME.equals("") && !OLD_GATEWAY_NAME.equals("")) {
                // GENERATE ACCESS TOKEN
                String accessToken = generateAccessToken(pw);
                System.out.println("*************************************************************");
                pw.println("*************************************************************");
                if (accessToken != null) {
                    // RETRIEVE ALL API IDs
                    List<String> apiIds = retrieveAllAPIIds(accessToken, pw);
                    if (apiIds.size() != 0) {
                        System.out.println("*************************************************************");
                        pw.println("*************************************************************");
                        System.out.println("Total Number of APIs to be changed : " + apiIds.size());
                        pw.println("Total Number of APIs to be changed : " + apiIds.size());
                        System.out.println("*************************************************************");
                        pw.println("*************************************************************");
                        for (int i = 0; i < apiIds.size(); i++) {
                            System.out.println("Working on API Number : " + (i + 1) +" : "+apiIds.get(i));
                            pw.println("Working on API Number : " + i +" : "+apiIds.get(i));
                            // RETRIEVE LAST REVISION ID
                            String lastRevisionId = getLastRevision(accessToken, apiIds.get(i), pw);
                            if (lastRevisionId != null) {
                                if (unDeployRevision(accessToken, apiIds.get(i), lastRevisionId, pw)) {
                                    System.out.println("Undeploy Revision Id : " + lastRevisionId);
                                    pw.println("Undeploy Revision Id : " + lastRevisionId);
                                    System.out.println("Undeploy Revision Operation Status : " + true);
                                    pw.println("Undeploy Revision Operation Status : " + true);
                                } else {
                                    System.out.println("Undeploy the Revision : " + lastRevisionId + " was not success.");
                                    pw.println("Undeploy the Revision : " + lastRevisionId + " was not success.");
                                }
                                System.out.println("Waiting for revision to get undeployed in the " + GATEWAY_NAME + " Gateway");
                                pw.println("Waiting for revision to get undeployed in the " + GATEWAY_NAME + " Gateway");
                                Thread.sleep(2000);
                            } else {
                                System.out.println("The API is deployed on the new Gateway:" + GATEWAY_NAME);
                                pw.println("API is deployed on the new Gateway:" + GATEWAY_NAME);
                            }
                        }
                    }
                }
            } else {
                System.out.println("Parameters were not loaded correctly. Please check the integration.properties file.");
                pw.println("Parameters were not loaded correctly. Please check the integration.properties file.");
            }
            pw.flush();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                pw.close();
                bw.close();
                fw.close();
                ctx.close();
            } catch (IOException io) {
                // can't do anything
                ctx.close();
            } catch (Exception e) {
                ctx.close();
            }
        }
    }

    private static String generateAccessToken(PrintWriter pw) {
        final String REQUEST_BODY = "{\"grant_type\": \"password\",\n" +
                "\"username\":\"" + USERNAME + "\",\n" +
                "\"password\":\"" + PASSWORD + "\",\n" +
                "\"scope\":\"apim:api_view apim:api_create apim:api_manage apim:api_delete apim:api_publish apim:subscription_view apim:subscription_block apim:subscription_manage apim:external_services_discover apim:threat_protection_policy_create apim:threat_protection_policy_manage apim:document_create apim:document_manage apim:mediation_policy_view apim:mediation_policy_create apim:mediation_policy_manage apim:client_certificates_view apim:client_certificates_add apim:client_certificates_update apim:ep_certificates_view apim:ep_certificates_add apim:ep_certificates_update apim:publisher_settings apim:pub_alert_manage apim:shared_scope_manage apim:app_import_export apim:api_import_export apim:api_product_import_export apim:api_generate_key apim:common_operation_policy_view apim:common_operation_policy_manage apim:comment_write apim:comment_view apim:admin\"\n" +
                "}";

        String appCredentials = CLIENT_KEY + ":" + CLIENT_SECRET;
        String encodedString = Base64.getEncoder().encodeToString(appCredentials.getBytes());

        HttpPost httpPost = new HttpPost("https://" + HOST + ":" + TRANSPORT_PORT + "/oauth2/token");
        try {
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            StringEntity entity = new StringEntity(REQUEST_BODY);
            httpPost.setEntity(entity);

            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedString);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            CloseableHttpResponse response = client.execute(httpPost);

            if (response.getStatusLine().getStatusCode() == 200) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    //System.out.println("FULL TOKEN RESPONSE : " + sb.toString());
                    JSONObject object = new JSONObject(sb.toString());
                    String accessToken = (String) object.get("access_token");
                    System.out.println("ACCESS TOKEN : " + accessToken);
                    pw.println("ACCESS TOKEN : " + accessToken);
                    return accessToken;

                } catch (IOException e) {
                    System.out.println("An exception has been thrown when attempting to read the response for the token service : " + e);
                    pw.println("An exception has been thrown when attempting to read the response for the token service : " + e);
                    return null;
                } catch (JSONException e) {
                    return null;
                } catch (Exception e) {
                    return null;
                }
            } else {
                System.out.println("Generate Token Returned Status Code : " + response.getStatusLine().getStatusCode());
                pw.println("Generate Token Returned Status Code : " + response.getStatusLine().getStatusCode());
                return null;
            }

        } catch (IOException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (KeyManagementException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static List<String> retrieveAllAPIIds(String accessToken, PrintWriter pw) {
        HttpGet httpGet = new HttpGet("https://" + HOST + ":" + TRANSPORT_PORT + "/" + PUBLISHER_CONTEXT + "?limit=" + API_LIMIT);
        try {
            httpGet.setHeader("Accept", "application/json");

            httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            CloseableHttpResponse response = client.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
//                    System.out.println("Full Response : " + sb.toString());
                    JSONObject object = new JSONObject(sb.toString());
                    List<String> apiObjArr = new ArrayList<>();
                    JSONArray jsonArray = object.getJSONArray("list");
//                    System.out.println("List of All APIs : " + jsonArray);
                    if (jsonArray != null) {
                        //Iterating JSON array
                        for (int i = 0; i < jsonArray.length(); i++) {
                            //Adding each element of JSON array into ArrayList
                            JSONObject tempObj = (JSONObject) jsonArray.get(i);
                            String lifeCycleStatus = tempObj.getString("lifeCycleStatus");
                            String type = tempObj.getString("type");
                            if (!lifeCycleStatus.equals("DEPRECATED") && !lifeCycleStatus.equals("RETIRED")) {
                                apiObjArr.add((String) tempObj.get("id"));
                            } else {
                                System.out.println("Identified the API : " + tempObj.getString("id") + " is " + lifeCycleStatus);
                                pw.println("Identified the API : " + tempObj.getString("id") + " is " + lifeCycleStatus);
                            }
                        }
                    }
//                    System.out.println("All API Ids : "+apiObjArr.toString());
                    return apiObjArr;

                } catch (IOException e) {
                    System.out.println("An exception has been thrown when attempting to read the response for the publisher get all api service : " + e);
                    pw.println("An exception has been thrown when attempting to read the response for the publisher get all api service : " + e);
                    return null;
                } catch (JSONException e) {
                    return null;
                } catch (Exception e) {
                    return null;
                }
            } else {
                System.out.println("Retrieve All APIs Returned Status Code : " + response.getStatusLine().getStatusCode());
                pw.println("Retrieve All APIs Returned Status Code : " + response.getStatusLine().getStatusCode());
                return null;
            }

        } catch (IOException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (KeyManagementException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String createRevision(String accessToken, String apiId, PrintWriter pw) {

        HttpPost httpPost = new HttpPost("https://" + HOST + ":" + TRANSPORT_PORT + "/" + PUBLISHER_CONTEXT + "/" + apiId + "/revisions");
        final String REQUEST_BODY = "{\n" +
                "\"description\": \"Sandbox and Production Endpoints have been changed in the API : " + apiId + "\"\n" +
                "}";
        try {
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            StringEntity entity = new StringEntity(REQUEST_BODY);
            httpPost.setEntity(entity);

            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            CloseableHttpResponse response = client.execute(httpPost);

            if (response.getStatusLine().getStatusCode() == 201) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    JSONObject jsonObject = new JSONObject(sb.toString());
                    System.out.println("Newly created revision Id : " + jsonObject.get("id"));
                    pw.println("Newly created revision Id : " + jsonObject.get("id"));
                    return (String) jsonObject.get("id");

                } catch (IOException e) {
                    System.out.println("An exception has been thrown when attempting to read the response for the publisher create revision service : " + e);
                    pw.println("An exception has been thrown when attempting to read the response for the publisher create revision service : " + e);
                    return null;
                } catch (JSONException e) {
                    return null;
                } catch (Exception e) {
                    return null;
                }
            } else {
                System.out.println("Create Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                pw.println("Create Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                return null;
            }

        } catch (IOException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (KeyManagementException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean unDeployRevision(String accessToken, String apiId, String revisionId, PrintWriter pw) {
        HttpPost httpPost = new HttpPost("https://" + HOST + ":" + TRANSPORT_PORT + "/" + PUBLISHER_CONTEXT + "/" + apiId + "/undeploy-revision?revisionId=" + revisionId);

        System.out.println("Gateway Name : " + GATEWAY_NAME);
        System.out.println("VHOST : " + VHOST);
        System.out.println("Display on DevPortal : " + DISPLAY_ON_DEV_PORTAL);
        final String REQUEST_BODY = "[\n" +
                "    {\n" +
                "        \"revisionUuid\": \"" + revisionId + "\",\n" +
                "        \"name\": \"" + GATEWAY_NAME + "\",\n" +
                "        \"vhost\": \"" + VHOST + "\",\n" +
                "        \"displayOnDevportal\": " + DISPLAY_ON_DEV_PORTAL + "\n" +
                "    }\n" +
                "]";
        try {
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            StringEntity entity = new StringEntity(REQUEST_BODY);
            httpPost.setEntity(entity);

            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            CloseableHttpResponse response = client.execute(httpPost);

            if (response.getStatusLine().getStatusCode() == 201) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    return true;
                } catch (IOException e) {
                    System.out.println("An exception has been thrown when attempting to read the response for the authorization service : " + e);
                    pw.println("An exception has been thrown when attempting to read the response for the authorization service : " + e);
                    return false;
                } catch (Exception e) {
                    return false;
                }
            } else {
                System.out.println("Undeploy Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                pw.println("Undeploy Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                return false;
            }

        } catch (IOException e) {
            return false;
        } catch (NoSuchAlgorithmException e) {
            return false;
        } catch (KeyManagementException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean deployRevision(String accessToken, String apiId, String revisionId, PrintWriter pw) {
        HttpPost httpPost = new HttpPost("https://" + HOST + ":" + TRANSPORT_PORT + "/" + PUBLISHER_CONTEXT + "/" + apiId + "/deploy-revision?revisionId=" + revisionId);

        System.out.println("Gateway Name : " + NEW_GATEWAY_NAME);
        System.out.println("VHOST : " + VHOST);
        System.out.println("Display on DevPortal : " + DISPLAY_ON_DEV_PORTAL);
        final String REQUEST_BODY = "[\n" +
                "    {\n" +
                "        \"name\": \"" + NEW_GATEWAY_NAME + "\",\n" +
                "        \"vhost\": \"" + VHOST + "\",\n" +
                "        \"displayOnDevportal\": " + DISPLAY_ON_DEV_PORTAL + "\n" +
                "    }\n" +
                "]";
        try {
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            StringEntity entity = new StringEntity(REQUEST_BODY);
            httpPost.setEntity(entity);

            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            CloseableHttpResponse response = client.execute(httpPost);

            if (response.getStatusLine().getStatusCode() == 201) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
//                    System.out.println("Full Deploy Response : " + sb.toString());
                    JSONArray jsonArray = new JSONArray(sb.toString());
                    JSONObject jsonObject = (JSONObject) jsonArray.get(0);
                    return true;
                } catch (IOException e) {
                    System.out.println("An exception has been thrown when attempting to read the response for the authorization service : " + e);
                    pw.println("An exception has been thrown when attempting to read the response for the authorization service : " + e);
                    return false;
                } catch (JSONException e) {
                    return false;
                } catch (Exception e) {
                    return false;
                }
            } else {
                System.out.println("Deploy Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                pw.println("Deploy Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                return false;
            }

        } catch (IOException e) {
            return false;
        } catch (NoSuchAlgorithmException e) {
            return false;
        } catch (KeyManagementException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getLastRevision(String accessToken, String apiId, PrintWriter pw) {
        HttpGet httpGet = new HttpGet("https://" + HOST + ":" + TRANSPORT_PORT + "/" + PUBLISHER_CONTEXT + "/" + apiId + "/revisions");
        try {
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            CloseableHttpResponse response = client.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    JSONObject jsonObject = new JSONObject(sb.toString());
                    JSONArray jsonArray = (JSONArray) jsonObject.get("list");
                    for(int i=0; i<jsonArray.length(); i++){
                        JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                        JSONArray deploymentInfoArr = (JSONArray) jsonObject1.get("deploymentInfo");
                        if (deploymentInfoArr != null && deploymentInfoArr.length() > 0) {

                            for(int k=0; k<deploymentInfoArr.length(); k++){
                                JSONObject jsonObject2 = (JSONObject) deploymentInfoArr.get(k);
                                GATEWAY_NAME = jsonObject2.getString("name");
                                System.out.println("name ===== : " + jsonObject2.getString("name"));
                                VHOST = jsonObject2.getString("vhost");
                                DISPLAY_ON_DEV_PORTAL = jsonObject2.getBoolean("displayOnDevportal");
                                if (jsonObject2.getString("name").equals(OLD_GATEWAY_NAME)) {
                                    System.out.println("id ===== : " + jsonObject1.getString("id"));
                                    return jsonObject1.getString("id");
                                }
                            }
                            
                        } else {
                            GATEWAY_NAME = NEW_GATEWAY_NAME;
                            VHOST = "localhost";
                            DISPLAY_ON_DEV_PORTAL = true;
                        }
                    }
                    return null;

                } catch (IOException e) {
                    System.out.println("An exception has been thrown when attempting to read the response for the publisher get revision service : " + e);
                    pw.println("An exception has been thrown when attempting to read the response for the publisher get revision service : " + e);
                    return null;
                } catch (JSONException e) {
                    return null;
                }
            } else {
                System.out.println("Returned Status Code : " + response.getStatusLine().getStatusCode());
                pw.println("Get Inactive Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                return null;
            }

        } catch (IOException e) {
            return null;
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (KeyManagementException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean deleteRevision(String accessToken, String apiId, String revisionId, PrintWriter pw) {
        HttpDelete httpDelete = new HttpDelete("https://" + HOST + ":" + TRANSPORT_PORT + "/" + PUBLISHER_CONTEXT + "/" + apiId + "/revisions/" + revisionId);

        try {
            httpDelete.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            CloseableHttpClient client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            CloseableHttpResponse response = client.execute(httpDelete);

            if (response.getStatusLine().getStatusCode() == 200) {
                StringBuilder sb = new StringBuilder();
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
                        StandardCharsets.UTF_8))) {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    return true;

                } catch (IOException e) {
                    System.out.println("An exception has been thrown when attempting to read the response for the publisher revision delete service : " + e);
                    pw.println("An exception has been thrown when attempting to read the response for the publisher revision delete service : " + e);
                    return false;
                } catch (Exception e) {
                    return false;
                }
            } else {
                System.out.println("Delete Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                pw.println("Delete Revision Returned Status Code : " + response.getStatusLine().getStatusCode());
                return false;
            }

        } catch (IOException e) {
            return false;
        } catch (NoSuchAlgorithmException e) {
            return false;
        } catch (KeyManagementException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
