package com.panshao;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.MethodAppearanceFineTuner;
import freemarker.log.Logger;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

public class OpenApiV3Parser {

    private static final String TEMPLATE_LOCATION = "E:\\ideaWorkspace2\\base-learning\\openApiV3-parser\\src\\main\\resources";

    private static final String CLASS_PATH = OpenApiV3Parser.class.getResource("/").getPath();
    private static final String TARGET_PATH = CLASS_PATH.substring(0, CLASS_PATH.lastIndexOf("classes"));

    public static void main(String[] args){
        String yamlFile = "api.yaml";
        String projectName = "test-pa-dsr-data";
        String packageName = "com.panshao.papi.dsr.data";

        String[] packageArray = packageName.split("\\.");
        String packageDirs = "";
        for(String pack : packageArray){
            packageDirs += pack + "/";
        }

        // TODO 生成项目结构跟固定文件
        String srcDir = projectName + "/src/main/java/" + packageDirs;
        String srcResourcesDir = projectName + "/" + "src/main/resources/";

        String testDir = projectName + "/src/test/java/" + packageDirs;
        String testResourcesDir = projectName + "/src/test/resources/";

        String modelDir = srcDir + "model/";
        String configDir = srcDir + "config/";
        String controllerDir = srcDir + "controller/";
        String exceptionDir = srcDir + "exception/";
        String utilDir = srcDir + "util/";

        List<String> dirList = Arrays.asList(srcDir,
                srcResourcesDir,
                testDir,
                testResourcesDir,
                modelDir,
                configDir,
                controllerDir,
                exceptionDir,
                utilDir
        );

        for(String dir : dirList) {
            File file = new File(TARGET_PATH + dir);
            if(!file.exists()){
                file.mkdirs();
            }
        }

        // TODO main class
        generateMainClass(TARGET_PATH + srcDir, yamlFile);

        // TODO generate .gitignore 文件
        generateGitIgnoreClass(TARGET_PATH + projectName + "/", yamlFile);

        // TODO generate sonarqube 文件
        generateSonarqubeConfig(TARGET_PATH + projectName + "/", yamlFile);

        // TODO generate pom.xml
        generatePomXml(TARGET_PATH + srcDir, yamlFile);

        // TODO generate application.yaml
        generateApplicationYaml(TARGET_PATH + srcResourcesDir, yamlFile);

        // TODO  generate application-{env}.yaml
        for(String env : Arrays.asList("local", "dev", "sit", "uat")){
            generateApplicationEnvYaml(TARGET_PATH + srcResourcesDir, yamlFile, env);
        }

        // TODO generate i18n dir and file
        generateI18nFile(TARGET_PATH + srcDir, yamlFile);

        // TODO generate application-test.yaml
        generateApplicationTestYaml(TARGET_PATH + testResourcesDir, yamlFile);

        // TODO header, model, apiEndpoint details
        generateHeaderClass(TARGET_PATH + modelDir, yamlFile);

        generateModelClass(TARGET_PATH + modelDir, yamlFile);

        generateApiEndpoint(TARGET_PATH + controllerDir, yamlFile);

        // TODO generate validate class
        generateValidateClass(TARGET_PATH + modelDir, yamlFile);

        // TODO generate InvalidParameter Exception class

        // TODO generate Business Exception class

        // TODO generate System Exception class

    }

    public static void generateMainClass(String destDir, String yamlName){

    }

    public static void generateGitIgnoreClass(String destDir, String yamlName){

    }

    public static void generateSonarqubeConfig(String destDir, String yamlName){

    }

    public static void generatePomXml(String destDir, String yamlName){

    }

    public static void generateApplicationYaml(String destDir, String yamlName){

    }

    public static void generateApplicationEnvYaml(String destDir, String yamlName){

    }

    public static void generateApplicationEnvYaml(String destDir, String yamlName, String env){

    }

    public static void generateI18nFile(String destDir, String yamlName){

    }

    public static void generateApplicationTestYaml(String destDir, String yamlName){

    }

    public static void generateValidateClass(String destDir, String yamlName){

    }

    public static void generateApiEndpoint(String destDir, String yamlName){
        ParseOptions parseOptions = new ParseOptions();
        // in order to get #ref property
        parseOptions.setFlatten(true);
        final OpenAPI openAPI = new OpenAPIV3Parser().read(yamlName, null, parseOptions);

        Paths paths = openAPI.getPaths();

        paths.forEach((k, v) -> {
            Operation operation = null;
            String operationFlag = null;
            if(v.getPost() != null){
                operation = v.getPost();
                operationFlag = "post";
            }else if(v.getGet() != null){
                operation = v.getGet();
                operationFlag = "get";
            }else if(v.getPut() != null){
                operation = v.getPut();
                operationFlag = "put";
            }else if(v.getDelete() != null){
                operation = v.getDelete();
                operationFlag = "delete";
            }

            String requestBodyName = null;
            String responseBodyName = null;

            Schema requestSchema = operation.getRequestBody().
                    getContent().
                    get("application/json").
                    getSchema();

            requestBodyName = requestSchema.get$ref().
                    substring(requestSchema.
                            get$ref().
                            lastIndexOf("/") + 1);

            Schema responseSchema = operation.getResponses().
                    get("200").
                    getContent().
                    get("application/json").
                    getSchema();

            responseBodyName = responseSchema.
                    get$ref().
                    substring(responseSchema.
                            get$ref().
                            lastIndexOf("/") + 1);

            String lastEndpoint = k.substring(k.lastIndexOf("/") + 1);
            String method = CaseUtils.toCamelCase(lastEndpoint, false, '-');

            String className = StringUtils.capitalize( method + "Controller");

            Map<String, Object> modelMap = new HashMap<>();
            modelMap.put("endpoint", k);
            modelMap.put("operationFlag", operationFlag);
            modelMap.put("method", method);
            modelMap.put("requestBodyName", requestBodyName);
            modelMap.put("responseBodyName", responseBodyName);
            modelMap.put("className", className);

            parseFreeMarkTemplate(destDir, "apiEndPoint.ftl", className + ".java", modelMap, TEMPLATE_LOCATION);
        });
    }

    public static void generateModelClass(String destDir, String yamlName){
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true); // implicit
        parseOptions.setResolveFully(true);
        final OpenAPI openAPI = new OpenAPIV3Parser().read(yamlName, null, parseOptions);

        Components components = openAPI.getComponents();
        Map<String, Schema> schemas = components.getSchemas();
        Set<String> uniqueEntitySet = new HashSet<>();
        recursiveProperties("", schemas, 0, uniqueEntitySet, destDir);
    }

    public static void recursiveProperties(String objectName, Map<String, Schema> properties,
                                           int layer, Set<String> uniqueEntitySet, String destDir){
        if(properties == null || properties.isEmpty()){
            return;
        }
        layer++;
        String property = "";
        Map<String, String> propertyMap = new HashMap<>();
        for(String key : properties.keySet()){
            Schema schema = properties.get(key);
            // System.out.println("layer =" + layer + ", objectName=" + objectName + ", key = " + key);
            recursiveProperties(key, schema.getProperties(), layer, uniqueEntitySet, destDir);

            if (StringUtils.isNotEmpty(objectName)) {
                 property +=  "{name = "+ key + ", type = " + schema.getType() + "}";

                if("object".equals(schema.getType())){
                    propertyMap.put(key, key);
                }else if("array".equals(schema.getType())) {
                    propertyMap.put(key, "List<" + StringUtils.capitalize(key) + ">");
                }else{
                    propertyMap.put(key, schema.getType());
                }
            }
        }

        if(StringUtils.isNotEmpty(objectName)){
            if(uniqueEntitySet.contains(objectName)){
                return;
            }
            uniqueEntitySet.add(objectName);

            System.out.println("layer = " + layer + ",objectName ===================== " + objectName + ", properties = " + property );

            String className = StringUtils.capitalize(objectName);

            Map<String, Object> modelMap = new HashMap<>();
            modelMap.put("propertyMap", propertyMap);
            modelMap.put("className", className);

            parseFreeMarkTemplate(destDir, "model.ftl", className + ".java", modelMap, TEMPLATE_LOCATION);
        }
    }

    public static void generateHeaderClass(String destDir, String yamlName){
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true); // implicit
        parseOptions.setResolveFully(true);
        final OpenAPI openAPI = new OpenAPIV3Parser().read(yamlName, null, parseOptions);


        Paths paths = openAPI.getPaths();
        paths.forEach((k, v) -> {

            String[] kArray = k.split("/");
            String lastEndpoint = kArray[kArray.length - 1];
            String pre = CaseUtils.toCamelCase(lastEndpoint, true, '-');

            String className = pre + "RequestHeader";

            List<Parameter> parameters = v.getPost().getParameters();
            Map<String, Object> modelMap = new HashMap<>();
            modelMap.put("parameters", parameters);
            modelMap.put("className", className);
            parseFreeMarkTemplate(destDir, "header.ftl", className + ".java", modelMap, TEMPLATE_LOCATION);
        });
    }

    public static void parseFreeMarkTemplate(String destDir, String templateName, String fileName, Map<String, Object> modelMap, String templateLocation) {
        try {
            Logger.selectLoggerLibrary(Logger.LIBRARY_SLF4J);
            /* Create and adjust the configuration singleton */
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);

            DefaultObjectWrapperBuilder owb = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_31);
            owb.setMethodAppearanceFineTuner(new MethodAppearanceFineTuner() {
                @Override
                public void process(BeansWrapper.MethodAppearanceDecisionInput in, BeansWrapper.MethodAppearanceDecision out) {
                    out.setMethodShadowsProperty(false);
                }
            });
            cfg.setObjectWrapper(owb.build());

            cfg.setDirectoryForTemplateLoading(new File(templateLocation));
            // Recommended settings for new projects:
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            cfg.setFallbackOnNullLoopVariable(false);

            /* Get the template (uses cache internally) */
            Template temp = cfg.getTemplate(templateName);

            /* Merge data-model with template */
            File file = new File(destDir + fileName);
            Writer out = new OutputStreamWriter(new FileOutputStream(file));
            temp.process(modelMap, out);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("exception fileName = " + fileName + ", modelMap = " + modelMap +
                    ", templateLocation = " + templateLocation + " " + e.getMessage());
        }
    }
}
