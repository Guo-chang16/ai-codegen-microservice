package com.guochang.aicodegenmicroservice.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
public class MultiFileCodeResult {
    @Description("生成html代码结果")
    private String htmlCode;
    @Description("css代码")
    private String cssCode;
    @Description("js代码")
    private String jsCode;
    @Description("生成代码的描述")
    private String description;
}
