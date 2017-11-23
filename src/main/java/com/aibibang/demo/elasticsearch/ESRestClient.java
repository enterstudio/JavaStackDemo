package com.aibibang.demo.elasticsearch;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aibibang.demo.elasticsearch.http.HttpClient;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author：Truman.P.Du
 * @createDate: 2017年8月11日 上午8:45:32
 * @version:1.0
 * @description: Elasticsearch rest 工具类
 */
public class ESRestClient {
	private static final Logger LOG = LoggerFactory.getLogger(ESRestClient.class);
	private HttpClient httpClient;

	public ESRestClient(String hosts) {
		httpClient = new HttpClient(hosts);
	}

	/**
	 * 判断index模板是否存在
	 *
	 * @param templateName
	 * @return true/false
	 * @throws Exception
	 */
	public boolean templateExists(String templateName) throws Exception {
		HttpHead httpHead = new HttpHead("/_template/" + templateName);
		return httpClient.getStatusCode(httpHead);
	}

	/**
	 * 判断index是否存在
	 * 
	 * @param indexName
	 * @return
	 * @throws Exception
	 */
	public boolean indicesExists(String indexName) throws Exception {
		HttpHead httpHead = new HttpHead("/" + indexName);
		return httpClient.getStatusCode(httpHead);
	}

	/**
	 * 创建索引
	 * 
	 * @param indexName
	 * @param settings
	 * @return
	 * @throws Exception
	 */
	public boolean createIndex(String indexName, String settings) throws Exception {
		StringEntity entity = new StringEntity(settings, ContentType.APPLICATION_JSON);
		String result = httpClient.httpPut("/" + indexName, entity);
		try {
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("acknowledged") && json.getBoolean("acknowledged")) {
				return true;
			}
		} catch (Exception e) {
			throw new Exception(result);
		}
		return false;
	}

	/**
	 * 模糊查询所有index
	 * 
	 * @param prefixName
	 * @return
	 */
	public Set<String> fuzzyGetIndices(String prefixName) {
		Set<String> indices = new HashSet<String>();
		try {
			String result = httpClient.httpGet("/" + prefixName + "*");
			JSONObject json = JSONObject.parseObject(result);
			if (!json.isEmpty()) {
				indices = json.keySet();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return indices;
	}

	/**
	 * 删除index
	 * 
	 * @param indexName
	 * @return
	 * @throws Exception
	 */
	public boolean deleteIndex(String indexName) throws Exception {
		try {
			String result = httpClient.httpDelete("/" + indexName);
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("acknowledged") && json.getBoolean("acknowledged")) {
				return true;
			}
			return false;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 删除index
	 * 
	 * @param index
	 * @param type
	 * @param id
	 * @return
	 */
	public boolean deleteIndex(String index, String type, String id) {
		try {
			String result = httpClient.httpDelete("/" + index + "/" + type + "/" + id + "?refresh=true");
			JSONObject json = JSONObject.parseObject(result);
			if ("deleted".equals(json.get("result")) || "not_found".equals(json.get("result"))) {
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * deleteByQueryIndex
	 *
	 * @param indexName
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public boolean deleteByQueryIndex(String indexName, String query) throws Exception {
		try {
			StringEntity entity = new StringEntity(query, ContentType.APPLICATION_JSON);
			String result = httpClient.httpPost("/" + indexName + "/_delete_by_query", entity);
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("error")) {
				LOG.info("deleteByQueryIndex failed! errors: " + json.toString());
				return false;
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 添加 index template
	 *
	 * @param templateName
	 * @param templateData
	 * @return true/false
	 * @throws Exception
	 */
	public boolean putTemplate(String templateName, String templateData) throws Exception {
		StringEntity entity = new StringEntity(templateData, ContentType.APPLICATION_JSON);
		String result = httpClient.httpPut("/_template/" + templateName, entity);
		try {
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("acknowledged") && json.getBoolean("acknowledged")) {
				return true;
			}
		} catch (Exception e) {
			throw new Exception(result);
		}
		return false;
	}

	/**
	 * 新曾document
	 * 
	 * @param index
	 * @param type
	 * @param document
	 * @return
	 * @throws Exception
	 */
	public boolean postIndexDocument(String index, String type, String document) throws Exception {
		StringEntity entity = new StringEntity(document, ContentType.APPLICATION_JSON);
		String result = httpClient.httpPost("/" + index + "/" + type + "?refresh=true", entity);
		try {
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("_shards")) {
				JSONObject shards = json.getJSONObject("_shards");
				if (shards.getLongValue("failed") == 0)
					return true;
			}
		} catch (Exception e) {
			throw new Exception(result);
		}
		return false;
	}
	
    /**
     * 新曾document
     * @param index
     * @param type
     * @param id
     * @param document
     * @return
     * @throws Exception
     */
	public boolean putIndexDocument(String index, String type,String id, String document) throws Exception {
		StringEntity entity = new StringEntity(document, ContentType.APPLICATION_JSON);
		String result = httpClient.httpPut("/" + index + "/" + type + "/"+id+"?refresh=true", entity);
		try {
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("_shards")) {
				JSONObject shards = json.getJSONObject("_shards");
				if (shards.getLongValue("failed") == 0)
					return true;
			}
		} catch (Exception e) {
			throw new Exception(result);
		}
		return false;
	}

	/**
	 * 根据ID更新document
	 *
	 * @param index
	 * @param type
	 * @param id
	 * @param document
	 * @return
	 * @throws Exception
	 */
	public boolean updateIndexDocument(String index, String type, String id, String document) throws Exception {
		StringEntity entity = new StringEntity(document, ContentType.APPLICATION_JSON);
		String result = httpClient.httpPut("/" + index + "/" + type + "/" + id + "?refresh=true", entity);
		try {
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("_shards")) {
				JSONObject shards = json.getJSONObject("_shards");
				if (shards.getLongValue("failed") == 0)
					return true;
			}
		} catch (Exception e) {
			throw new Exception(result);
		}
		return false;
	}

	/**
	 * 指定字段排序查询所有的document
	 *
	 * @param index
	 * @param type
	 * @param sortName
	 * @param isAsc
	 * @return
	 * @throws Exception
	 */
	public String getAll(String index, String type, String sortName, boolean isAsc) throws Exception {
		String result = "";
		long size = count(index, type);
		try {
			result = httpClient.httpGet("/" + index + "/" + type + "/" + "_search?size=" + size + "&sort=" + sortName
					+ ":" + (isAsc ? "asc" : "desc"));
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("_shards")) {
				JSONObject shards = json.getJSONObject("_shards");
				if (shards.getLongValue("failed") == 0)
					return json.toJSONString();
			}
		} catch (Exception e) {
			throw new Exception(result);
		}
		return result;
	}

	/**
	 * 根据index,type获取所有document
	 * 
	 * @param index
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public String getAll(String index, String type) throws Exception {
		String result = "";
		long size = count(index, type);
		try {
			result = httpClient.httpGet("/" + index + "/" + type + "/" + "_search?size=" + size);
			JSONObject json = JSONObject.parseObject(result);
			if (json.containsKey("_shards")) {
				JSONObject shards = json.getJSONObject("_shards");
				if (shards.getLongValue("failed") == 0)
					return json.toJSONString();
			}
		} catch (Exception e) {
			throw new Exception(result);
		}
		return result;
	}

	/**
	 * 多个查询
	 * 
	 * @param index
	 * @param type
	 * @param documents
	 * @return
	 * @throws Exception
	 */
	public String msearch(String index, String type, Set<JSONObject> documents) throws Exception {
		StringBuilder jsonData = new StringBuilder();
		for (JSONObject json : documents) {
			jsonData.append("{}\n");
			jsonData.append(json.toString()).append("\n");
		}
		StringEntity entity = new StringEntity(jsonData.toString(), ContentType.APPLICATION_JSON);
		String result = "";
		try {
			result = httpClient.httpPost("/" + index + "/" + type + "/_msearch", entity);
		} catch (Exception e) {
			new Exception(result);
		}
		return result;
	}

	/**
	 * 统计指定index，type数量
	 * 
	 * @param index
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public long count(String index, String type) throws Exception {
		String result = httpClient.httpGet("/" + index + "/" + type + "/_count");
		long total = 0;
		JSONObject json = JSONObject.parseObject(result);
		if (json.containsKey("count")) {
			total = json.getLongValue("count");
		}
		return total;
	}

	/**
	 * 根据ID获取指定index数据
	 *
	 * @param index
	 * @param type
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public String getIndex(String index, String type, String id) throws Exception {
		String result = httpClient.httpGet("/" + index + "/" + type + "/" + id);
		return result;
	}

	/**
	 * 批量插入文档
	 *
	 * @param index
	 * @param type
	 * @param documents
	 * @throws Exception
	 */
	public void bulkIndexDocument(String index, String type, Set<JSONObject> documents) throws Exception {
		StringBuilder jsonData = new StringBuilder();
		JSONObject action = new JSONObject();
		JSONObject meta_data = new JSONObject();
		meta_data.put("_index", index);
		meta_data.put("_type", type);
		action.put("index", meta_data);
		for (JSONObject json : documents) {
			jsonData.append(action.toString()).append("\n");
			jsonData.append(json.toString()).append("\n");
		}
		bulkIndexDocument(jsonData.toString());
	}

	/**
	 * 指定业务批量插入文档
	 *
	 * @param index
	 * @param type
	 * @param documents
	 *            中profileId 可以指定数据id
	 * @throws Exception
	 */
	public void bulkIndexDocumentBusiness(String index, String type, JSONArray documents) throws Exception {
		StringBuilder jsonData = new StringBuilder();
		for (int i = 0; i < documents.size(); i++) {
			JSONObject source = documents.getJSONObject(i);
			String id = null;
			JSONObject action = new JSONObject();
			JSONObject meta_data = new JSONObject();
			meta_data.put("_index", index);
			meta_data.put("_type", type);
			if (source.containsKey("profileId")) {
				id = source.getString("profileId");
				source.remove("profileId");
				meta_data.put("_id", id);
			}
			action.put("index", meta_data);
			jsonData.append(action.toString()).append("\n");
			jsonData.append(source.toString()).append("\n");
		}
		bulkIndexDocument(jsonData.toString());
	}

	/**
	 * 批量插入文档
	 *
	 * @param documents
	 * @throws Exception
	 */
	public void bulkIndexDocument(String document) throws Exception {
		StringEntity entity = new StringEntity(document, ContentType.APPLICATION_JSON);
		String result = httpClient.httpPost("/_bulk", entity);
		JSONObject json = JSONObject.parseObject(result);
		if (json.containsKey("error") || json.getBoolean("errors")) {
			throw new RuntimeException("bulkIndexDocument failed! errors: " + json.toString());
		}
	}

	public static void main(String[] args) throws Exception {
		ESRestClient esRestClient = new ESRestClient("192.168.0.101:8200");
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					CopyOnWriteArrayList<String> urls = esRestClient.httpClient.urls;
					for(String url:urls) {
						System.out.println(url);
					}
				}
				
			}
		}).start();
		for(int i=0;i<Integer.MAX_VALUE;i++) {
			
			try {
				String documents = "{\"user\":\"value"+i+"\"}";
				System.out.println(esRestClient.putIndexDocument("twitter", "tweet",i+"", documents));
				//System.out.println(esRestClient.getIndex("twitter", "tweet",i+""));
				//esRestClient.deleteIndex("twitter", "tweet",i+"");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}
}
