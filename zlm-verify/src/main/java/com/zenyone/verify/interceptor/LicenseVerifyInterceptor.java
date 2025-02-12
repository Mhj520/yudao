package com.zenyone.verify.interceptor;

import com.zenyone.core.ex.CommonException;
import com.zenyone.core.model.LicenseExtraParam;
import com.zenyone.core.model.LicenseResult;
import com.zenyone.core.model.LicenseVerifyManager;
import com.zenyone.core.result.ResultCode;
import com.zenyone.core.utils.CommonUtils;
import com.zenyone.verify.annotion.VLicense;
import com.zenyone.verify.config.LicenseVerifyProperties;
import com.zenyone.core.listener.ACustomVerifyListener;
import de.schlichtherle.license.LicenseContent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.List;

/**
 * <p>License验证拦截器</p>
 *
 * @author admin
 * @version v1.0.0

 * @date created on 00:32 上午 2020/8/22
 */
public class LicenseVerifyInterceptor implements HandlerInterceptor {

    @Autowired
    private LicenseVerifyProperties properties;

    public LicenseVerifyInterceptor() {
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            VLicense annotation = method.getAnnotation(VLicense.class);
            if (CommonUtils.isNotEmpty(annotation)) {
                LicenseVerifyManager licenseVerifyManager = new LicenseVerifyManager();
                /** 1、校验证书是否有效 */
                LicenseResult verifyResult = licenseVerifyManager.verify(properties.getVerifyParam());
                if(!verifyResult.getResult()){
                    throw new CommonException(verifyResult.getMessage());
                }
                LicenseContent content = verifyResult.getContent();
                LicenseExtraParam licenseCheck = (LicenseExtraParam) content.getExtra();
                if (verifyResult.getResult()) {
                    /** 增加业务系统监听，是否自定义验证 */
                    List<ACustomVerifyListener> customListenerList = ACustomVerifyListener.getCustomListenerList();
                    boolean compare = true;
                    for (ACustomVerifyListener listener : customListenerList) {
                        boolean verify = listener.verify(licenseCheck);
                        compare = compare && verify;
                    }
                    return compare;
                }
                throw new CommonException(ResultCode.INTERNAL,verifyResult.getException().getMessage());
            }
        }
        return true;
    }
}
