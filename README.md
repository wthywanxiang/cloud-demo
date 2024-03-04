#  微服务demo说明

##  整体架构分析

![image-20240303205751384](C:\Users\wanxiang\Desktop\微服务架构说明\image-20240303205751384.png)

demo-common，所有模块依赖的部分，其中全部或者大部分模块都要使用的依赖（比如JDBC），都从本模块引入。其中公用的工具类也写入本模块（比如ServerResponse）。

demo-gateway，统一的请求入口。所有前端请求接口都从本模块进入，配置路由规则，由gateway的断言功能进行请求转发到某一模块。

demo-child1与demo-child2都是服务的架构模块，可以随意添加修改。

##   pom文件

###  cloud-demo

cloud-demo（即整个demo）下的pom文件，不需要引入任何依赖，仅需在此进行子模块聚合。

每添加一个新的模块都在此加入新的module

![image-20240303213651854](C:\Users\wanxiang\Desktop\微服务架构说明\image-20240303213651854.png)

###  非demo-common

非demo-common，即为网关模块（网关模块需单独引入gateway依赖），服务模块等诸多子模块，无需再多引入新的依赖，直接依赖demo-common模块即可。

![image-20240303213942676](C:\Users\wanxiang\Desktop\微服务架构说明\image-20240303213942676.png)

###  demo-common

demo-common子模块的pom文件可以理解为正常的单体springboot项目的pom文件，引入依赖正常添加即可。

##  application文件

![image-20240303210507932](C:\Users\wanxiang\Desktop\微服务架构说明\image-20240303210507932.png)

mysql、nacos、redis配置需根据实际主机地址进行更改。

##  docker配置nacos

docker安装mysql、redis不在此处说明，详情参考其它博客。

由于nacos/nacos-server较为特殊，特别在此说明。





特别注意运行docker部分！！！！！！！！！！





####   docker拉取nacos镜像

```
docker pull nacos/nacos-server
```

####  创建文件映射(根据个人习惯创建映射即可，我比较习惯/mydata)

```
mkdir -p /mydata/nacos/logs/                     
mkdir -p /mydata/nacos/init.d/     
```

####  创建配置文件

````
vim /mydata/nacos/init.d/custom.properties
````

文件内容如下（记得修改数据库信息）：

````
server.contextPath=/nacos
server.servlet.contextPath=/nacos
server.port=8848

#spring.datasource.platform=mysql

# 数据库ip和数据库名字
db.url.0=jdbc:mysql://xx.xx.xx.x:3306/nacos_config?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true
# 数据库用户
db.user=root
# 数据库密码
db.password=root


nacos.cmdb.dumpTaskInterval=3600
nacos.cmdb.eventTaskInterval=10
nacos.cmdb.labelTaskInterval=300
nacos.cmdb.loadDataAtStart=false

management.metrics.export.elastic.enabled=false
management.metrics.export.influx.enabled=false

server.tomcat.accesslog.enabled=true
server.tomcat.accesslog.pattern=%h %l %u %t "%r" %s %b %D %{User-Agent}i

nacos.security.ignore.urls=/,/**/*.css,/**/*.js,/**/*.html,/**/*.map,/**/*.svg,/**/*.png,/**/*.ico,/console-fe/public/**,/v1/auth/login,/v1/console/health/**,/v1/cs/**,/v1/ns/**,/v1/cmdb/**,/actuator/**,/v1/console/server/**
nacos.naming.distro.taskDispatchThreadCount=1
nacos.naming.distro.taskDispatchPeriod=200
nacos.naming.distro.batchSyncKeyCount=1000
nacos.naming.distro.initDataRatio=0.9
nacos.naming.distro.syncRetryDelay=5000
nacos.naming.data.warmup=true
nacos.naming.expireInstance=true
````

####  docker运行2.x以下版本nacos（不包括2.0）

````
docker  run \
--name nacos -d \
-p 8848:8848 \
--privileged=true \
--restart=always \
-e JVM_XMS=256m \
-e JVM_XMX=256m \
-e JVM_XMN=64m \
-e JVM_MS=32m \
-e JVM_MMS=64m \
-e MODE=standalone \
-e PREFER_HOST_MODE=hostname \
-v /mydata/nacos/logs:/home/nacos/logs \
-v /mydata/nacos/init.d/custom.properties:/mydata/nacos/init.d/custom.properties \
nacos/nacos-server
````

####  2.x版本nacos说明

由于nacos2版本新增了2个通信端口，也就是9848和9849两个端口。

所以如果采用以上运行命令，docker容器的9848和9849两个端口将会无法映射到外部 ，nacos客户端将会连接nacos-server失败报错。

所以需要采用以下运行命令，将9848和9849两个端口暴露到容器外部。

```
docker  run \
--name nacos -d \
-p 8848:8848 \
-p 9848:9848 \
-p 9849:9849 \
--privileged=true \
--restart=always \
-e JVM_XMS=256m \
-e JVM_XMX=256m \
-e JVM_XMN=64m \
-e JVM_MS=32m \
-e JVM_MMS=64m \
-e MODE=standalone \
-e PREFER_HOST_MODE=hostname \
-v /mydata/nacos/logs:/home/nacos/logs \
-v /mydata/nacos/init.d/custom.properties:/mydata/nacos/init.d/custom.properties \
nacos/nacos-server
```

####  nacos持久化

我用的是mysql，在mysql创建nacos_config数据库。

创建完库之后执行官方提供的初始化sql。

https://github.com/alibaba/nacos/blob/master/config/src/main/resources/META-INF/nacos-db.sql

##  OpenFeign转发请求

demo-child2模块有一个hello接口（打开项目文件即可看到）。

demo-child1模块OpenFeignTestService接口类，通过注解的形式，注明了调用哪一个nacos客户端的哪一个请求。即为：调用demo-child2模块下的hello接口。

需要远程调用哪一个接口，就将接口的全签名复制到负责转发的service接口中。

![image-20240303212012476](C:\Users\wanxiang\Desktop\微服务架构说明\image-20240303212012476.png)

demo-child1中调用OpenFeignTestService与普通service无异。

![image-20240303212234393](C:\Users\wanxiang\AppData\Roaming\Typora\typora-user-images\image-20240303212234393.png)

**demo-child1的端口号为8087，demo-child1的端口号为8088**

下图就是访问本机的8087端口，成功调用到了8088端口的API。

![image-20240303212336680](C:\Users\wanxiang\Desktop\微服务架构说明\image-20240303212336680.png)

##  gateway网关

####  application文件

``` 
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
      globalcors:
      #通过网关配置，统一解决所有接口的跨域问题
        cors-configurations:
          '[/**]':
            allowedOriginPatterns: '*'
            #允许请求中携带的头信息
            allowedHeaders: '*'
            #运行跨域的请求方式
            allowedMethods: '*'
            #跨域检测的有效期单位s
            maxAge: 36000
      routes:
       - id: hello
         uri: lb://demo-child1
         predicates:
         - Path=/**
      enabled: true
    nacos:
      discovery:
        server-addr: 192.168.10.26:8848
  application:
    name: demo-gateway
server:
  port: 8086

```

**网关可以通过配置，统一解决所有接口的跨域问题。还可以通过路由匹配转发请求（uri可以写url，也可以写于网关注册在同一nacos-server的其它服务，格式为     lb://demoName**

routes为数组，可以配置多个路由（我比较懒，所有路由请求都转发到了demo-child1）

![image-20240303212700376](C:\Users\wanxiang\Desktop\微服务架构说明\image-20240303212700376.png)

网关服务端口为8086，通过转发到demo-child1的hello接口，可以调用到8088端口下demo-child2的API服务。

**注意下图与openfegin展示部分的端口号差异。**

![image-20240303213027217](C:\Users\wanxiang\Desktop\微服务架构说明\image-20240303213027217.png)