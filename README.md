# 分布式锁

## 使用方式

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
 
 * 使用
 
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