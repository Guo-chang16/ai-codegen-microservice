package com.guochang.aicodegenmicroservice.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
@Description("生成html代码结果")
public class HtmlCodeResult {
    @Description("html代码")
    private String htmlCode;

    @Description("html代码的描述")
    private String description;
}
