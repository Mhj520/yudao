package com.zenyone.verify.listener;

import com.zenyone.core.helper.LoggerHelper;
import com.zenyone.core.model.LicenseResult;
import com.zenyone.core.model.LicenseVerifyManager;
import com.zenyone.core.utils.CommonUtils;
import com.zenyone.verify.config.LicenseVerifyProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * <p>项目启动时安装证书&定时检测lic变化，自动更替lic</p>
 *
 * @author admin
 * @version v1.0.0

 * @date created on 00:02 上午 2020/8/22
 */
@Component
public class LicenseVerifyInstallListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private LicenseVerifyProperties properties;

    private static final String LICENSE_PATH_NOT_CONFIGURED = "许可证路径未配置，无法进行许可证校验";
    private static final String LICENSE_FILE_NOT_EXIST = "未发现许可证";

    private static final String LICENSE_INSTALL_START = "++++++++ 开始安装证书 ++++++++";
    private static final String LICENSE_INSTALL_SUCCESS = "++++++++ 证书安装成功 ++++++++";
    private static final String LICENSE_INSTALL_FAIL = "++++++++ 证书安装失败 ++++++++";

    /**文件唯一身份标识 == 相当于人类的指纹一样*/
    private static String md5 = "";
    private static boolean isLoad = false;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
//        boolean b = TokenAccessUtil.hasAccess();
//        if(!b){
//            System.out.println(Base64Util.decode("57O757uf5o6I5p2D5aSx6LSl77yM6K+36IGU57O7566h55CG5ZGY77yB"));
//            forceExit();
//        }
        if(CommonUtils.isNotEmpty(properties.getLicensePath())){
            install();
            try{
                String readMd5 = getMd5(properties.getLicensePath());
                isLoad = true;
                if(LicenseVerifyInstallListener.md5 == null || "".equals(LicenseVerifyInstallListener.md5)){
                    LicenseVerifyInstallListener.md5 =readMd5;
                }
            }catch (Exception e){
                // 结束进程
                forceExit();
            }
        }else{
            LoggerHelper.error(LICENSE_PATH_NOT_CONFIGURED);
            // 结束进程
            forceExit();
        }
    }
    private void forceExit() {
        SpringApplication.exit(applicationContext, () -> -1);
    }
    /**5秒检测一次，不能太快也不能太慢*/
    @Scheduled(cron = "0/5 * * * * ?")
    protected void timer() throws Exception {
        if(!isLoad){
            return;
        }
        String readMd5 = getMd5(properties.getLicensePath());
        // 不相等，说明lic变化了
        if(!readMd5.equals(LicenseVerifyInstallListener.md5)){
            install();
            LicenseVerifyInstallListener.md5 = readMd5;
        }
    }

    private void install() {
        LoggerHelper.info(LICENSE_INSTALL_START);
        LicenseVerifyManager licenseVerifyManager = new LicenseVerifyManager();
        /** 走定义校验证书并安装 */
        LicenseResult result = licenseVerifyManager.install(properties.getVerifyParam());
        if(result.getResult()){
            LoggerHelper.info(LICENSE_INSTALL_SUCCESS);
        }else{
            forceExit();
            LoggerHelper.info(LICENSE_INSTALL_FAIL);
        }
    }

    /**
     * <p>获取文件的md5</p>
     */
    public String getMd5(String filePath) throws Exception {
        File file;
        String md5 = "";
        try {
            file = ResourceUtils.getFile(filePath);
            if (file.exists()) {
                FileInputStream is = new FileInputStream(file);
                byte[] data = new byte[is.available()];
                is.read(data);
                md5 = DigestUtils.md5DigestAsHex(data);
                is.close();
            }
        } catch (FileNotFoundException e) {
            LoggerHelper.error(LICENSE_FILE_NOT_EXIST);
        }
        return md5;
    }

}
