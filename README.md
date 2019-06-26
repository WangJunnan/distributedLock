# 分布式锁 (分别基于 redis zookeeper 实现)

## 快速使用

> 实际集成方式视具体项目

 * 下载项目`mvn install`到本地
 * 项目引用
 ```
 <dependency>
    <groupId>com.walm</groupId>
    <artifactId>lock</artifactId>
    <version>1.0-SNAPSHOT</version>
 </dependency>
 ```
 
 * 使用 Redis lock
 
 
 ```java
 private RedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory("172.17.41.32", null, 6379);
 private DistributedLock distributedLock = new RedisDistributedLock(redisConnectionFactory, "test:lock");
 
 try {
    if (distributedLock.lock("key",  5, 5, TimeUnit.SECONDS)) {
    // get lock sucess
    // todo something
    }
 } catch (Exception e) {
     //
 } finally {
     distributedLock.unlock("key");
 }
 
 ```
 
 * 使用 zookeeper lock
 
  ```java
  public static DistributedLock zkDistributedLock = new ZkDistributedLock("172.17.41.32:2181");
  
  try {
     if (zkDistributedLock.lock("key")) {
     // get lock sucess
     // todo something
     }
  } catch (Exception e) {
      //
  } finally {
      zkDistributedLock.unlock("key");
  }
  
  ```
 
 