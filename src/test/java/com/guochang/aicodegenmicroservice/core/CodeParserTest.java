package com.guochang.aicodegenmicroservice.core;

import com.guochang.aicodegenmicroservice.ai.model.HtmlCodeResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeParserTest {
    @Test
    void parseHtmlCode() {
        String codeContent = """
                随便写一段描述：
                ```html
                ؜<DOCTYPE html !>
                <html>
                <head>
                    <title>测试页面</title>
                </head>
                <body>
                    <h1>Hello World!</h1>
                </body>
                </html>
                ```
                随便写一段描述
                """;
        HtmlCodeResult result = CodeParser.parseHtmlCode(codeContent);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
        System.out.println(result.getHtmlCode());
    }
}