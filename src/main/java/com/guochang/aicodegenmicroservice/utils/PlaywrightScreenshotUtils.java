package com.guochang.aicodegenmicroservice.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.guochang.aicodegenmicroservice.common.ErrorCode;
import com.guochang.aicodegenmicroservice.exception.BusinessException;
import com.guochang.aicodegenmicroservice.exception.ThrowUtils;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static com.guochang.aicodegenmicroservice.constant.AppConstant.SCREENSHOT_ROOT_DIR;

@Slf4j
public class PlaywrightScreenshotUtils {

    // Playwright 核心对象（全局单例，避免重复初始化）
    private static Playwright playwright;
    // 浏览器实例（无头模式）
    private static Browser browser;
    // 页面实例（用于加载 URL 并截图）
    private static Page page;

    // 静态初始化：创建浏览器和页面，项目启动时执行
    static {
        try {
            // 初始化 Playwright
            playwright = Playwright.create();
            // 启动 Chromium 浏览器（支持 Chrome/Edge 内核）
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true) // 无头模式（无界面运行，节省资源）
                    .setArgs(Arrays.asList(
                            "--window-size=1600,900",
                            "--disable-gpu",
                            "--no-sandbox"
                    )).setTimeout(30000) // 浏览器启动超时（30秒）
            );
            // 创建新页面
            page = browser.newPage();
            // 配置页面默认超时（加载、操作等）
            page.setDefaultTimeout(30000); // 30秒
            log.info("Playwright 浏览器初始化成功");
        } catch (Exception e) {
            log.error("Playwright 初始化失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "封面图生成工具初始化失败");
        }
    }

    /**
     * 生成并保存网页封面图
     * @param webUrl 已部署应用的可访问 URL
     * @return 压缩后的封面图本地路径
     */
    public static String saveWebPageScreenshot(String webUrl) {
        // 1. 参数校验（确保 URL 非空）
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "应用 URL 为空");

        // 2. 准备保存路径（创建临时目录）
        //保存图片到临时目录
        String rootPath = SCREENSHOT_ROOT_DIR + UUID.randomUUID().toString().substring(0, 8);
        FileUtil.mkdir(rootPath);
        final String IMAGE_SUFFIX = ".png";
        String imagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;

        try {
            // 4. 加载网页并等待渲染完成
            page.navigate(webUrl); // 访问目标 URL
            // 等待页面加载状态：NETWORKIDLE 表示网络空闲（适合 Vue 等 SPA 应用，确保动态内容加载完成）
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // 5. 截图并保存原始图片
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get(imagePath)) // 保存路径
                    .setOmitBackground(false) // 保留页面背景（避免透明）
            );
            log.info("原始截图保存成功：{}", imagePath);

            //压缩图片
            final String COMPRESS_SUFFIX = "_compressed.jpg";
            String compressImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESS_SUFFIX;

            // 6. 压缩图片（降低体积，便于存储和传输）
            ImgUtil.compress(
                    FileUtil.file(imagePath), // 原始图片
                    FileUtil.file(compressImagePath), // 压缩后路径
                    0.7f // 压缩质量（0.7 表示 70% 质量）
            );
            log.info("图片压缩成功：{}", compressImagePath);

            // 7. 清理临时文件（删除原始大图）
            boolean isDeleted = FileUtil.del(imagePath);
            if (!isDeleted) {
                log.warn("原始截图删除失败：{}", imagePath);
            }

            // 8. 返回压缩后的图片路径
            return compressImagePath;

        } catch (Exception e){
            log.error("封面图生成失败：{}", webUrl, e);
            // 清理异常情况下的临时文件
            FileUtil.del(rootPath);
            return null;
        }
    }

    /**
     * 资源释放：项目关闭时调用，释放浏览器资源
     */
    @PreDestroy
    public void destroy() {
        if (page != null) {
            page.close(); // 关闭页面
        }
        if (browser != null) {
            browser.close(); // 关闭浏览器
        }
        if (playwright != null) {
            playwright.close(); // 关闭 Playwright
        }
        log.info("Playwright 资源已释放");
    }
}