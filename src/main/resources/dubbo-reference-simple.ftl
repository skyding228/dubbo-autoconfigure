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