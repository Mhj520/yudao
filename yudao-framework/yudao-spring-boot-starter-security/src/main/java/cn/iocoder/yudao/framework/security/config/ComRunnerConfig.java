package cn.iocoder.yudao.framework.security.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;


@Configuration
@Lazy
public class ComRunnerConfig implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ComRunnerConfig.class);
    public static final String PUBLIC_KEY_BASE64 = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEJyys/h5QCx/J+1oI+RrbFGG8v6P26ClyT5dkSU8pv/bn5K5CFkaLcz/PdjGk3HQzJTP5Ao3kd6PT7uF3uNSKSQ==";
    public static final String PRIVATE_KEY_BASE64 = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgd8N40bAZLtD45GktYnaM78lAnkc92SCa1d6S4iR2P8mgCgYIKoEcz1UBgi2hRANCAAQnLKz+HlALH8n7Wgj5GtsUYby/o/boKXJPl2RJTym/9ufkrkIWRotzP892MaTcdDMlM/kCjeR3o9Pu4Xe41IpJ";
    public static final String VER_PATH1_BASE64 = "BARkO9taUxNXuMvzRtkANJLiKgTwys8K8cBgifLlBnh9nw2pbjfJyvN9KEaV3TGy5hcn8xEsmfk6FP5Pwhwop0i1rUJXqC/IyDLSohDiUrkL/CYh5Rr+aQP6fgbAGdhMY1BeiKANArWE9RQcMM48Zju3ggqxfAZSoOVycRQP1N+dlsg4Wch4fRY=";
    public static final String VER_PATH2_BASE64 = "BCh/sQWeNJ9vhprxP+l7ix9Qpb5RR7SzpjH8Q+IlRbfAJwNLsYtsEG+JNoW5h0ReaSZnDaD9uLxDioCYtx7ffIApQ/xU8uq/BaRHKQr3DBuvMCBewvToK4gMbWw5ULdwGJ32qUxzdtWKg7zSqpcUeo/Jtr3dH26jL2EokPh4883dp8V1Rw==";
    public static final String VER_HINT_BASE64 = "BH/vrSTdO662YpF1PLkh4ftCxuATVedQeVXV4zkW+d3gwQ3M4K+G38PyjL+GV7cIlIsfY9xYb6lT0BJOpcGFERAWevyk8kNS9y0b5VUFs3pLvhJaMDy1DM2FeXk2rMrNm7w7tuplxvAS3kGm5bp9KI+HkAvlZA==";

//    @Autowired
//    private LicenseVerifyAutoConfigure licenseVerifyAutoConfigure;
    @PostConstruct
    private void initConfig() {
        try{

            SM2 sm2 = SmUtil.sm2(PRIVATE_KEY_BASE64, PUBLIC_KEY_BASE64);
            loadClass(sm2, VER_PATH1_BASE64);
            loadClass(sm2, VER_PATH2_BASE64);
        }catch (Exception e) {
            String hint = decrypt(VER_HINT_BASE64);
            logger.error("License verification failed. Hint: {}", hint, e); // 使用占位符
            throw new RuntimeException("License verification failed: " + hint, e); // 重新抛出异常，并提供提示信息
        }
    }
    private void loadClass(SM2 sm2, String encryptedBase64) throws ClassNotFoundException{

        String className = decrypt(encryptedBase64);
        try {
            Class.forName(className, true, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            logger.error("Class not found: {}", className); // 记录类名
            throw e; // 重新抛出异常
        }
    }

    private String decrypt(String encryptedBase64) {
        try{
            SM2 sm2 = SmUtil.sm2(PRIVATE_KEY_BASE64, PUBLIC_KEY_BASE64);
            String decrypted = StrUtil.utf8Str(sm2.decrypt(encryptedBase64, KeyType.PrivateKey));
            return decrypted;
        }catch (Exception e){
            // 记录更详细的日志, 比如加密的字符串, 使用的公私钥等
            logger.error("Decryption failed for: {}", encryptedBase64, e);
            throw new RuntimeException("Decryption failed",e); // 重新抛出异常，或者自定义异常
        }

    }

    public static void main(String[] args) {
        ClassLoader cl1 = ComRunnerConfig.class.getClassLoader();
        ClassLoader cl2 = LicenseVerifyAutoConfigure.class.getClassLoader();

        System.out.println("ClassLoader for ComRunnerConfig: " + cl1);
        System.out.println("ClassLoader for LicenseVerifyAutoConfigure: " + cl2);
    }
}
