package com.info7255.beans;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

public class ElasticSearchConnect {
	//private static final String indexerURI = "http://localhost:9200/planindex";
	
	public ElasticSearchConnect(){}
	
	//index the data
	public void runTask(String id, JSONObject jsonObject) {
		try {
			
			String objectType = jsonObject.getString("objectType");
			String indexer = "/planindexpc"+"/"+"_doc"+"/"+ id;
			URL url = new URL("http", "localhost", 9200, indexer);
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost postRequest = new HttpPost(url.toURI());
			StringEntity entity = new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);
			postRequest.setEntity(entity);
			CloseableHttpResponse httpRespose = httpClient.execute(postRequest);		
			System.out.println(httpRespose.getEntity());
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	}
	
	//delete the index
	public boolean deleteTask(String body) {
		
		try {
			
			JSONObject jsonObject = new JSONObject(body);
			String objectId = jsonObject.getString("objectId");
			//String indexer = "/planindexpc"+"/"+"plan"+"/"+ objectId;
			String indexer = "/planindexpc"+"/"+"_doc"+"/"+ objectId;
			URL url = new URL("http", "localhost", 9200, indexer);
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpDelete deleteRequest = new HttpDelete(url.toURI());
			CloseableHttpResponse httpRespose = httpClient.execute(deleteRequest);
			System.out.println(httpRespose.getEntity());
			return true;
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
		 
	}

}
