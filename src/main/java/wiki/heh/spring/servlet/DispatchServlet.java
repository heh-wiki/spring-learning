package wiki.heh.spring.servlet;

import wiki.heh.spring.annotation.Autowired;
import wiki.heh.spring.annotation.Controller;
import wiki.heh.spring.annotation.RequestMapping;
import wiki.heh.spring.annotation.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author heh
 * @date 2021/12/24
 */
public class DispatchServlet extends HttpServlet {

    /**
     * 属性配置文件
     */
    private Properties contextConfig = new Properties();

    private List<String> classNameList = new ArrayList<>();

    /**
     * IOC 容器
     */
    Map<String, Object> iocMap = new HashMap<String, Object>();

    Map<String, Method> handlerMapping = new HashMap<String, Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //7、运行阶段
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().write("500 服务器出错啦:\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * 7、运行阶段，进行拦截，匹配
     *
     * @param req      请求
     * @param response 响应
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse response) throws InvocationTargetException, IllegalAccessException {

        String url = req.getRequestURI();

        String contextPath = req.getContextPath();

        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");
        System.out.println("[第7步] request url-->" + url);
        if (!this.handlerMapping.containsKey(url)) {
            try {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write("404 找不到资源啦!!");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Method method = this.handlerMapping.get(url);

        System.out.println("[第7步] method-->" + method);


        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());

        System.out.println("[第7步] iocMap.get(beanName)->" + iocMap.get(beanName));

        // 第一个参数是获取方法，后面是参数，多个参数直接加，按顺序对应
        method.invoke(iocMap.get(beanName), req, response);

        System.out.println("[第7步] method.invoke put {" + iocMap.get(beanName) + "}.");
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

        //1、加载配置文件
        doLoadConfig(servletConfig.getInitParameter("contextConfigLocation"));

        //2、扫描相关的类
        doScanner(contextConfig.getProperty("scan-package"));

        //3、初始化 IOC 容器，将所有相关的类实例保存到 IOC 容器中
        doInstance();

        //4、依赖注入
        doAutowired();

        //5、初始化 HandlerMapping
        initHandlerMapping();

        System.out.println("手写《假Spring》初始化完成");

        //6、打印数据
        doTestPrintData();
    }


    /**
     * 获取类的首字母小写的名称
     *
     * @param className ClassName
     * @return java.lang.String
     */
    private String toLowerFirstCase(String className) {
        char[] charArray = className.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }

    /**
     * 1、加载配置文件
     *
     * @param contextConfigLocation web.xml --> servlet/init-param
     */
    private void doLoadConfig(String contextConfigLocation) {

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);

        try {
            // 保存在内存
            contextConfig.load(inputStream);

            System.out.println("[第一步] 配置数据已保存到 contextConfig 中.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 2、扫描相关的类
     *
     * @param scanPackage properties --> scan-package
     */
    private void doScanner(String scanPackage) {

        // package's . ==> /
        URL resourcePath = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));

        if (resourcePath == null) {
            return;
        }

        File classPath = new File(resourcePath.getFile());

        for (File file : classPath.listFiles()) {

            if (file.isDirectory()) {

                System.out.println("[第二步] {" + file.getName() + "} 是一个目录.");

                // 子目录递归
                doScanner(scanPackage + "." + file.getName());

            } else {

                if (!file.getName().endsWith(".class")) {
                    System.out.println("[第二步] {" + file.getName() + "} 不是类文件.");
                    continue;
                }

                String className = (scanPackage + "." + file.getName()).replace(".class", "");

                // 保存在内容
                classNameList.add(className);

                System.out.println("[第二步] {" + className + "} 已保存在 classNameList 中.");
            }
        }
    }

    /**
     * 3、初始化 IOC 容器，将所有相关的类实例保存到 IOC 容器中
     */
    private void doInstance() {
        if (classNameList.isEmpty()) {
            return;
        }

        try {
            for (String className : classNameList) {

                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(Controller.class)) {
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    Object instance = clazz.newInstance();

                    // 保存在 ioc 容器
                    iocMap.put(beanName, instance);
                    System.out.println("[第三步] 存放{" + beanName + "}到IOC容器中.");

                } else if (clazz.isAnnotationPresent(Service.class)) {

                    String beanName = toLowerFirstCase(clazz.getSimpleName());

                    // 如果注解包含自定义名称
                    Service xService = clazz.getAnnotation(Service.class);
                    if (!"".equals(xService.value())) {
                        beanName = xService.value();
                    }

                    Object instance = clazz.newInstance();
                    iocMap.put(beanName, instance);
                    System.out.println("[第三步] 存放{" + beanName + "}到IOC容器中.");

                    // 找类的接口
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (iocMap.containsKey(i.getName())) {
                            throw new Exception("The Bean Name Is Exist.");
                        }

                        iocMap.put(i.getName(), instance);
                        System.out.println("[第三步] 存放{" + i.getName() + "}到IOC容器中.");
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 4、依赖注入
     */
    private void doAutowired() {
        if (iocMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : iocMap.entrySet()) {

            Field[] fields = entry.getValue().getClass().getDeclaredFields();

            for (Field field : fields) {
                if (!field.isAnnotationPresent(Autowired.class)) {
                    continue;
                }

                System.out.println("[第四步] Existence XAutowired.");

                // 获取注解对应的类
                Autowired xAutowired = field.getAnnotation(Autowired.class);
                String beanName = xAutowired.value().trim();

                // 获取 XAutowired 注解的值
                if ("".equals(beanName)) {
                    System.out.println("[第四步] xAutowired.value() is null");
                    beanName = field.getType().getName();
                }

                // 只要加了注解，都要加载，不管是 private 还是 protect
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(), iocMap.get(beanName));

                    System.out.println("[第四步] field set {" + entry.getValue() + "} - {" + iocMap.get(beanName) + "}.");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 5、初始化 HandlerMapping
     */
    private void initHandlerMapping() {

        if (iocMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();

            if (!clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }

            String baseUrl = "";

            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping xRequestMapping = clazz.getAnnotation(RequestMapping.class);
                baseUrl = xRequestMapping.value();
            }

            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }

                RequestMapping xRequestMapping = method.getAnnotation(RequestMapping.class);

                String url = ("/" + baseUrl + "/" + xRequestMapping.value()).replaceAll("/+", "/");

                handlerMapping.put(url, method);

                System.out.println("[第五步] handlerMapping 添加 {" + url + "} - {" + method + "}.");

            }
        }

    }

    /**
     * 6、打印数据
     */
    private void doTestPrintData() {

        System.out.println("[第六步]----打印数据------------------------");
        System.out.println("contextConfig.propertyNames()-->" + contextConfig.propertyNames());
        System.out.println("[classNameList]-->");
        for (String str : classNameList) {
            System.out.println(str);
        }
        System.out.println("[iocMap]-->");
        for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
            System.out.println(entry);
        }
        System.out.println("[handlerMapping]-->");
        for (Map.Entry<String, Method> entry : handlerMapping.entrySet()) {
            System.out.println(entry);
        }
        System.out.println("[第六步]----打印完成-----------------------");

        System.out.println("====启动成功====");
    }

}
