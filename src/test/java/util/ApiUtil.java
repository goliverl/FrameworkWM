package util;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import utils.ApiMethodsUtil;

public class ApiUtil {

	public static void main(String[] args) throws Exception {
	
//		HttpResponse respuesta = ApiUtil.sendPostDissableSSL(
//				"https://10.182.92.141:17919/restv2/user/runGet", 
//				{\"NOMBRE_USUARIO\":\"75681455\"});
//		
//		String actualResponseCode = String.valueOf(respuesta.getStatusLine().getStatusCode());
//		String actualResponseMessage = EntityUtils.toString(respuesta.getEntity());
//		
//		
//		System.out.println("codigo de respuesta actual: " + actualResponseCode);
//		System.out.println("respuesta mensaje actual: " + actualResponseMessage);
		
		SSLContextBuilder builder = new SSLContextBuilder();
	    builder.loadTrustMaterial(null, new TrustStrategy() {
	        @Override
	        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            return true;
	        }
	    });

	    SSLConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(builder.build(),
	            SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	    HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslSF).build();
	    HttpPost postRequest = new HttpPost("https://10.182.92.141:17919/restv2/user/runGet");
	    //postRequest.setEntity(new StringEntity("{\"NOMBRE_USUARIO\":\"75681455\"}"));
	    postRequest.setEntity(new StringEntity("{\"NOMBRE_USUARIO\":\"75681455\"}", ContentType.APPLICATION_JSON));
	    

		System.out.println("executing request " + postRequest.getRequestLine());
		HttpResponse response = httpClient.execute(postRequest);
		
		String actualResponseCode = String.valueOf(response.getStatusLine().getStatusCode());
		String actualResponseMessage = EntityUtils.toString(response.getEntity());
		
		
		System.out.println("codigo de respuesta actual: " + actualResponseCode);
		System.out.println("respuesta mensaje actual: " + actualResponseMessage);
		
	}
	
	
	//***********************************************************************
	//***********************************************************************
	//***********************************************************************
	public static HttpResponse sendPostDissableSSL(String url, String body) throws Exception {
		//CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		
//		SSLContext context = SSLContexts.custom()
//	            .loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE)
//	            .build();
//
//	    Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create()
//	            .register("http", PlainConnectionSocketFactory.INSTANCE)
//	            .register("https", new SSLConnectionSocketFactory(context, NoopHostnameVerifier.INSTANCE))
//	            .build();
//
//	    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
//
//	    CloseableHttpClient httpClient = HttpClients.custom()
//	            .setConnectionManager(connectionManager)
//	            .build();
		
		//--------------------------------------------------------------------------------------------------------
		HttpClient httpClient = HttpClients
	            .custom()
	            .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
	            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
	            .build();
		
		//--------------------------------------------------------------------------------------------------------
//		TrustManager[] trustAllCerts = new TrustManager[]{
//                new X509TrustManager() {
//                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                        return null;
//                    }
//                    public void checkClientTrusted(
//                        java.security.cert.X509Certificate[] certs, String authType) {
//                    }
//                    public void checkServerTrusted(
//                        java.security.cert.X509Certificate[] certs, String authType) {
//                    }
//                }
//            };
//
//		HttpClient httpClient;
//      
//        SSLContext sc = SSLContext.getInstance("SSL");
//        sc.init(null, trustAllCerts, new java.security.SecureRandom());
//        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
//                sc);
//        httpClient = HttpClients.custom().setSSLSocketFactory(
//                sslsf).build();
//        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		//--------------------------------------------------------------------------------------------------------
      
		
		//String encoding = Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));

		System.out.println("executing request " + httpPost.getRequestLine());
		HttpResponse response = httpClient.execute(httpPost);
		
//		HttpEntity entity = response.getEntity();
//		
//		return EntityUtils.toString(entity);
		return response;
	}
	
}
