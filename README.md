# Auto configure dubbo
register dubbo reference or service to Spring automatically.

This plugin can generate dubbo configuration file automatically base on a freemarker template which you can tell it how to do.

## Maven dependency
```xml
<dependency>
    <groupId>com.github.skyding228</groupId>
    <artifactId>dubbo-autoconfigure</artifactId>
    <version>1.0</version>
</dependency>
```

## Core class
`DubboPostProcessor` has three required properties.

- freemarkerRelativePath

the freemarker template path relative to classpath.You can access all the interfaces that need to be registered to Spring through `interfaces`.
The `interfaces` is a `ArrayList<InterfaceInfo>`,more details at `com.skyding.dubbo.autoconfigure.InterfaceInfo`.

- interfacePackages

where to scan interfaces ,use comma to split multiple packages.  Notice: It will NOT scan the subpackages.

- serviceSide

It's `Boolean` type. you should set `true` when configured in service side ,false in consumer side.

## Configure dubbo reference side
- The jar has a default freemarker template file
```dubbo-reference-simple.ftl
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
	xsi:schemaLocation="http://www.springframework.org/schema/beans          
    http://www.springframework.org/schema/beans/spring-beans.xsd          
    http://code.alibabatech.com/schema/dubbo          
    http://code.alibabatech.com/schema/dubbo/dubbo.xsd">
	<#list interfaces as inf>
    <dubbo:reference interface="${inf.clazz.name}" version="1.0" id="${inf.clazz.simpleName?uncap_first}" timeout="15000"/>
	</#list>
</beans> 
```
- Configure a bean in Spring

Assume your interfaces which you want to expose locate in `com.zx.sms.dao`, and your freemarker template is `dubbo-reference-simple.ftl`.
```xml
	<bean class="com.skyding.dubbo.autoconfigure.DubboPostProcessor">
		<property name="freemarkerRelativePath" value="dubbo-reference-simple.ftl"></property>
		<property name="interfacePackages" value="com.zx.sms.dao"></property>
		<property name="serviceSide" value="false"></property>
	</bean>
```

## Configure dubbo service side
- The jar has a default freemarker template file
```dubbo-service-simple.ftl
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans.xsd          
    http://code.alibabatech.com/schema/dubbo          
    http://code.alibabatech.com/schema/dubbo/dubbo.xsd">
	<#list interfaces as inf>
    <dubbo:service interface="${inf.clazz.name}" version="1.0" ref="${inf.ref}" timeout="15000"/>
    </#list>
</beans>  
```
- Configure a bean in Spring

Assume your interfaces which you want to expose locate in `com.zx.sms.dao`, and your freemarker template is `dubbo-service-simple.ftl`.
```xml
	<bean class="com.skyding.dubbo.autoconfigure.DubboPostProcessor">
		<property name="freemarkerRelativePath" value="dubbo-service-simple.ftl"></property>
		<property name="interfacePackages" value="com.zx.sms.dao"></property>
		<property name="serviceSide" value="true"></property>
	</bean>
```