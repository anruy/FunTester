package com.fun.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.fun.config.PropertyUtils;
import com.fun.utils.RString;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class DubboBase {

    private ApplicationConfig applicationConfig = new ApplicationConfig();

    private RegistryConfig registryConfig = new RegistryConfig();

    private String version;

    private String registryAddress;

    ReferenceConfig<GenericService> referenceConfig;

    ReferenceConfigCache configCache;

    public DubboBase(String propertyName) {
        PropertyUtils.Property properties = PropertyUtils.getProperties(propertyName);
        this.registryAddress = properties.getProperty("address");
        this.version = properties.getProperty("version");
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(registryAddress);
        applicationConfig.setName(properties.getProperty("name"));
    }

    /**
     * 不依赖配置文件
     *
     * @param adress
     * @param version
     * @param name
     */
    public DubboBase(String adress, String version, String name) {
        this.registryAddress = adress;
        this.version = version;
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(registryAddress);
        applicationConfig.setName(name);
    }

    /**
     * ReferenceConfig实例很重，封装了与注册中心的连接以及与提供者的连接，
     * 需要缓存，否则重复生成ReferenceConfig可能造成性能问题并且会有内存和连接泄漏。
     * API方式编程时，容易忽略此问题。
     * 这里使用dubbo内置的简单缓存工具类进行缓存
     *
     * @param interfaceClass
     * @return
     */
    public GenericService getGenericService(String interfaceClass) {
        if (referenceConfig == null) {
            referenceConfig = new ReferenceConfig<GenericService>();
            referenceConfig.setApplication(applicationConfig);
            referenceConfig.setRegistry(registryConfig);
            referenceConfig.setVersion(version);
            // 弱类型接口名
            referenceConfig.setInterface(interfaceClass);
            // 声明为泛化接口
            referenceConfig.setGeneric(true);
        }
        configCache = ReferenceConfigCache.getCache(RString.getChinese(5));
        return configCache.get(referenceConfig);
    }

    /**
     * 释放资源
     */
    @SuppressFBWarnings("NP_ALWAYS_NULL")
    public void over() {
        if (null != configCache) configCache.destroy(referenceConfig);
    }


}
