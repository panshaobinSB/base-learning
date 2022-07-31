package com.example.demo;

@RestController
public class ${className} {

    @RequestMapping(
        value = "${endpoint}",
        method = RequestMethod.${operationFlag?upper_case}
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<${responseBodyName?cap_first}> ${method}(${requestBodyName?cap_first} ${requestBodyName}){
        ${responseBodyName?cap_first} response = new ${responseBodyName?cap_first};
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
