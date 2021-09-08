package com.sean.springmvc.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sean.springmvc.annotation.Controller;
import com.sean.springmvc.annotation.RequestMapping;
import com.sean.springmvc.annotation.ResponseBody;
import com.sean.springmvc.context.WebApplicationContext;
import com.sean.springmvc.handler.MyHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yunfei_li@qq.com
 * @date 2021年09月08日 9:59
 */
public class DispatcherServlet extends HttpServlet {

    // 指定springmvc容器
    private WebApplicationContext webApplicationContext;

    // 创建集合用户用于映射关系
    List<MyHandler> handlerList = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        //加载初始化参数 classpath:springmvc.xml
        String contextConfigLocation = this.getServletConfig().getInitParameter("contextConfigLocation");
        // 创建springmvc 容器
        webApplicationContext = new WebApplicationContext(contextConfigLocation);
        // 进行初始化操作
        webApplicationContext.onRefresh();

        // 初始化请求映射关系
        initHandlerMapping();

    }

    /**
     * 初始化请求映射关系
     */
    private void initHandlerMapping() {
        for (Map.Entry<String, Object> entry : webApplicationContext.iocMap.entrySet()) {
            // 获取 bean 的class类型
            Class<?> clazz = entry.getValue().getClass();
            if(clazz.isAnnotationPresent(Controller.class)) {
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if(method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                        // 获取注解中的值
                        String url = requestMapping.value();
                        // 建议映射地址 于控制器方法
                        MyHandler myHandler = new MyHandler(url, entry.getValue(), method);
                        handlerList.add(myHandler);
                    }
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 进行请求分发处理
        doDispatcher(request, response);
    }

    private void doDispatcher(HttpServletRequest request, HttpServletResponse response) {
        // 根据用户的请求地址查找 handler controller
        MyHandler myHandler = getHandler(request);

        try {
            if(myHandler == null) {
                response.getWriter().print("<h1>404 NOT FOUND!</h1>");
            } else {
                // 调用处理方法之前 进行参数的注入

                // 调用目标方法
                Object result = myHandler.getMethod().invoke(myHandler.getController());
//                // 进行请求转发
//                String page = (String) result;
//                request.getRequestDispatcher(page).forward(request, response);
                if(result instanceof String) {
                    String viewName = (String) result;//跳转jsp页面
//                    String path_prefix = request.getRequestURL().toString();
//                    String contextPath = request.getContextPath();
                    String path_prefix = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort() + request.getContextPath() + "/";
                    // forward:/success.jsp
                    if(viewName.contains(":")) {
                        String viewType = viewName.split(":")[0];
                        String viewPage = viewName.split(":")[1];
                        if("forward".equals(viewType)) {
                            request.getRequestDispatcher(path_prefix + viewPage).forward(request, response);
                        } else {
                            response.sendRedirect(path_prefix + viewPage);
                        }
                    } else {
                        // 默认就是转发
                        request.getRequestDispatcher( viewName).forward(request, response);
                    }
                } else {
                    Method method = myHandler.getMethod();
                    if(method.isAnnotationPresent(ResponseBody.class)) {
                        //将返回值转json
                        ObjectMapper objectMapper = new ObjectMapper();
                        String json = objectMapper.writeValueAsString(result);
                        response.setContentType("text/html;charset=UTF-8");
                        PrintWriter writer = response.getWriter();
                        writer.print(json);
                        writer.flush();
                        writer.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取请求对应的handler
     * @param request
     * @return
     */
    private MyHandler getHandler(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        if(requestURI.startsWith(contextPath)) {
            requestURI = requestURI.replace(contextPath, "");
        }
        for (MyHandler myHandler : handlerList) {
            if(myHandler.getUrl().equals(requestURI)) {
                return myHandler;
            }
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

}
