package com.guochang.aicodegenmicroservice.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.guochang.aicodegenmicroservice.common.ErrorCode;
import com.guochang.aicodegenmicroservice.exception.BusinessException;
import com.guochang.aicodegenmicroservice.exception.ThrowUtils;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

import static com.guochang.aicodegenmicroservice.constant.AppConstant.SCREENSHOT_ROOT_DIR;

@Slf4j
public class WebScreenshotUtils {

private static final WebDriver webDriver;

static {
    final int DEFAULT_WIDTH = 1600;
    final int DEFAULT_HEIGHT =900;
    webDriver=initChromeDriver(DEFAULT_WIDTH,DEFAULT_HEIGHT);
    log.info("初始化webDriver成功");
}

@PreDestroy
public void destroy() {
    webDriver.quit();
}

public static String saveWebPageScreenshot(String webUrl) {
   try{ //非空校验
    ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "传递参数为空");

    //保存图片到临时目录
    String rootPath = SCREENSHOT_ROOT_DIR + UUID.randomUUID().toString().substring(0, 8);
    FileUtil.mkdir(rootPath);
    final String IMAGE_SUFFIX = ".png";
    String imagePath = rootPath + File.separator + RandomUtil.randomNumbers(5);

    //等待网页加载
    webDriver.get(webUrl);
    waitforPageLoad(webDriver);

    //截图
    byte[] imageBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
    saveImage(imageBytes, imagePath);
    log.info("原始截图保存成功！{}", imagePath);

    //压缩图片
    final String COMPRESS_SUFFIX = "_compressed.jpg";
    String compressImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;
    compressImage(imagePath, compressImagePath);

    //删除原始图片
    boolean del = FileUtil.del(imagePath);

    return compressImagePath;

    }catch (Exception e) {
        log.error("保存截图失败", e);
        return null;
    }

}




    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            WebDriverManager.chromedriver().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }




    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes,imagePath);
        } catch (Exception e) {
            log.error("保存截图失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }



    private static void compressImage(String originalImagePath, String compressImagePath) {
        //压缩图片
        final float COMPRESS_QUALITY = 0.7f;
        //创建图片对象
        try {
            ImgUtil.compress(FileUtil.file(originalImagePath),
                    FileUtil.file(compressImagePath),
                    COMPRESS_QUALITY);
        } catch (Exception e) {
            log.info("图片压缩失败");
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"图片压缩失败");
        }
    }


    private static void waitforPageLoad(WebDriver webDriver) {
        // 创建 WebDriverWait 对象
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        // 等待页面加载完成
        wait.until(driver-> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
    }
}
