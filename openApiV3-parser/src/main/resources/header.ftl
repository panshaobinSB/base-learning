package com.example.demo;

public class ${className} {
<#list parameters as parameter>
    @JsonProperty("${parameter.name}")
    <#if (parameter.schema.minLength)?? || (parameter.schema.maxLength)??>
    @Size(<#if (parameter.schema.minLength)??>min = ${parameter.schema.minLength},</#if><#if (parameter.schema.maxLength)??>max = ${parameter.schema.maxLength}</#if>)
    </#if>
    <#if (parameter.schema.pattern)??>
    @Pattern("${parameter.schema.pattern}")
    </#if>
    <#if parameter.required>
    @NotNull
    </#if>
    private ${parameter.schema.type?cap_first} ${dashedToCamel(parameter.name)};

</#list>

<#list parameters as parameter>
    public void set${dashedToCamel(parameter.name)?cap_first}(${parameter.schema.type?cap_first} ${dashedToCamel(parameter.name)}) {
        this.${dashedToCamel(parameter.name)} = ${dashedToCamel(parameter.name)};
    }

    public ${parameter.schema.type?cap_first} get${dashedToCamel(parameter.name)?cap_first}() {
        return this.${dashedToCamel(parameter.name)};
    }

</#list>
}


<#function dashedToCamel(s)>
    <#return s

    ?replace('(^-+)|(-+$)', '', 'r')

    ?replace('\\-+(\\w)?', ' $1', 'r')

    ?replace('([A-Z])', ' $1', 'r')

    ?capitalize

    ?replace(' ' , '')

    ?uncap_first

    >

</#function>