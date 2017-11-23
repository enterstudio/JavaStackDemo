package com.aibibang.demo.redis;

import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.util.JedisClusterCRC16;

/** 
* @author: Truman.P.Du 
* @since: 2016年7月19日 上午9:15:56 
* @version: v1.0
* @description:
*/
public class RedisStatusTest {
	public static JedisCluster jc = null;
	
	static {
		Set<HostAndPort> jedisClusterNode = new HashSet<HostAndPort>();
		HostAndPort hostAndPort = null;
		
		String[] connectArray = {"192.168.0.101:8002","192.168.0.102:8001"};
		for (String connect : connectArray) {
			String host = connect.split(":")[0].trim();
			String port = connect.split(":")[1].trim();
			hostAndPort = new HostAndPort(host, Integer.parseInt(port));
			jedisClusterNode.add(hostAndPort);
		}
		jc = new JedisCluster(jedisClusterNode);
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		while(true){
			/*for(int i=0;i<10000;i++){
				System.out.println(JedisClusterCRC16.getSlot("key"+i));
				System.out.println(jc.set("key"+i,"value"+i));
			}*/
			for(int i=0;i<10000;i++){
				System.out.println(JedisClusterCRC16.getSlot("key"+i));
				System.out.println(jc.get("key"+i));
			}
			try {Thread.sleep(2000);} catch (InterruptedException e) {}
			System.out.println(JedisClusterCRC16.getSlot("key9301"));
			System.out.println(jc.get("key9301"));
			/*Map<String, JedisPool> nodes = jc.getClusterNodes();
			Jedis jedis = nodes.get("10.16.238.93:8008").getResource();
			System.out.println(jedis.clusterNodes());*/
			//System.out.println(nodes.containsKey("10.16.238.93:8008"));
			//if(nodes.containsKey("10.16.238.93:8002"))System.out.println("true");
			try {Thread.sleep(2000);} catch (InterruptedException e) {}
			finally{
				//jedis.close();
			}
		}
	}

}
