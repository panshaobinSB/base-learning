package com.example.demo;

public class ${className} {
<#if propertyMap??>
    <#list propertyMap?keys as key>
        private ${propertyMap[key]!?cap_first} ${key!};

    </#list>
</#if>
<#if propertyMap??>
    <#list propertyMap?keys as key>
        public void set${key!?cap_first}(${propertyMap[key]!?cap_first} ${key!}) {
            this.${key!} = ${key!};
        }

        public ${propertyMap[key]!?cap_first} get${key!?cap_first}() {
            return this.${key!};
        }

    </#list>
</#if>

}
