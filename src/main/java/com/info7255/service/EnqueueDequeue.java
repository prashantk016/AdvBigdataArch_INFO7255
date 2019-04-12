package com.info7255.service;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.info7255.util.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class EnqueueDequeue {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RestHighLevelClient ecClient;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

  //  @EventListener(ApplicationReadyEvent.class)
    public void dequeue() {
        while (true) {
        	
            RedisConnection rc = stringRedisTemplate.getConnectionFactory().getConnection();

            //Index_all
            byte[] bytes = rc.rPopLPush(Utils.IndexAllQueue.getBytes(), Utils.IndexAllQueueBackUp.getBytes());
            if (bytes != null && bytes.length !=0) {
                try {
                    JSONObject jo = new JSONObject(new String(bytes));
                    String objectType = jo.getString("objectType");
                    String objectId = jo.getString("objectId");
                    ecClient.index(new IndexRequest("index_all", objectType, objectId).source(bytes, XContentType.JSON));
                    logger.info("Index=index_all, "+"Type="+objectType+" , Id="+objectId+" added to ElasticSearch.");
                } catch (IOException e) {
                    logger.error("ElasticSearch add index error:"+e);
                }
                String message = new String(bytes, StandardCharsets.UTF_8);
                logger.info("Message get: "+message);
            }

//            //Index_join
//            byte[] bytes2 = rc.rPopLPush(Utils.IndexJoinQueue.getBytes(), Utils.IndexJoinQueueBackUp.getBytes());
//            if (bytes2 != null && bytes2.length !=0) {
//                try {
//                    JSONObject jo = new JSONObject(new String(bytes2));
//                    String routingId = null;
//                    String objectId = jo.getString("objectId");
//                    try {
//                        routingId = jo.getString("_routingId");
//                        jo.remove("_routingId");
//                    } catch (JSONException e) {
//                        logger.info("ObjectId=" + objectId + " does not contain routingId.");
//                    }
//                    if (routingId != null) {
//                        ecClient.index(new IndexRequest("index_join", "_doc", objectId).source(jo.toMap(), XContentType.JSON).routing(routingId));
//                    } else {
//                        ecClient.index(new IndexRequest("index_join", "_doc", objectId).source(jo.toMap(), XContentType.JSON));
//                    }
//                    logger.info("Index=index_join, Type=_doc , Id=" + objectId + " added/updated to ElasticSearch.");
//                } catch (IOException e) {
//                    logger.error("ElasticSearch add index error:"+e);
//                }
//            }
            rc.close();
        }
    }
}


