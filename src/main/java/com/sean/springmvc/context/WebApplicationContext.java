package com.sean.springmvc.context;

import com.sean.springmvc.annotation.AutoWired;
import com.sean.springmvc.annotation.Controller;
import com.sean.springmvc.annotation.Service;
import com.sean.springmvc.xml.XmlPaser;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yunfei_li@qq.com
 * @date 2021年09月08日 9:55
 */
public class WebApplicationContext {

    String contextConfigLocation;

    //定义集合 存放bean 的权限名
    List<String> classNameLists = new ArrayList<>();

    // 创建map集合 ioc 集合，
    public Map<String, Object> iocMap = new ConcurrentHashMap<>();

    public WebApplicationContext() {
    }

    public WebApplicationContext(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    /**
     * 初始化 Spring 容器
     */
    public void onRefresh() {
        // 解析springmvc配置文件
        String pack = XmlPaser.getBasePackage(contextConfigLocation.split(":")[1]);

        String[] packs = pack.split(",");
        // 进行包扫描
        for (String pa : packs) {
            excuteScanPaclage(pa);
        }
        // 实例化容器 bean
        executeInstance();

        // 自动注入
        executeAutoWired();
    }

    /**
     * 实例化容器 bean
     */
    public void executeInstance() {
        try {
            for (String className : classNameLists) {
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(Controller.class)) {
                    // 控制层 bean
//                    String beanName = clazz.getSimpleName().substring(0,1).toLowerCase(Locale.ROOT)
//                            + clazz.getSimpleName().substring(1,clazz.getSimpleName().length());
                    String beanName = captureName(clazz.getSimpleName());
                    iocMap.put(beanName, clazz.newInstance());

                } else if (clazz.isAnnotationPresent(Service.class)) {
                    // Service bean
                    Service serviceAN = clazz.getAnnotation(Service.class);
                    String beanName = serviceAN.value();
                    iocMap.put(beanName, clazz.newInstance());
                }
            }
        } catch (Exception e) {

        }

    }

    /**
     * 将字符串的首字母转大写
     * @param str   需要转换的字符串
     * @return
     */
    private static String captureName(String str) {
        // 进行字母的ascii编码前移，效率要高于截取字符串进行转换的操作
        char[] cs=str.toCharArray();
        cs[0]-=32;
        return String.valueOf(cs);
    }

    /**
     * 进行自动注入
     */
    public void executeAutoWired() {
        try {
            // 从容器中取出bean  判断bean中是否存在autoWired注解，如果使用了注解，就需要自动注入
            for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
                // 获取容器中的bean
                Object bean = entry.getValue();

                // 获取bean中的属性
                Field[] fields = bean.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(AutoWired.class)) {
                        // 获取注解中的value值，该值就是bean的name
                        AutoWired autoWired = field.getAnnotation(AutoWired.class);
                        String beanName = autoWired.value();
                        // 可能存在属性是private，取消检查机制
                        field.setAccessible(true);

                        field.set(bean, iocMap.get(beanName));

                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 扫描包
     * @param pack
     */
    public void excuteScanPaclage(String pack) {
        URL url = this.getClass().getClassLoader().getResource("/"+pack.replaceAll("\\.", "/"));
        String path = url.getFile();

        File dir = new File(path);
        for (File f : dir.listFiles()) {
            if(f.isDirectory()) {
                // 当前是一个文件目录
                excuteScanPaclage(pack + "." + f.getName());
            } else {
                // 文件目录下文件 获取全路径
                String className = pack + "." + f.getName().replaceAll(".class", "");
                classNameLists.add(className);
            }
        }
    }

}
