//package wiki.heh.spring.servlet;
//
//import wiki.heh.spring.annotation.Autowired;
//import wiki.heh.spring.annotation.Controller;
//import wiki.heh.spring.annotation.RequestMapping;
//import wiki.heh.spring.annotation.Service;
//
//import javax.servlet.ServletConfig;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.net.URL;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//
///**
// * @author heh
// * @date 2021/12/24
// */
//public class DispatchServlet1 extends HttpServlet {
//    private Map<String, Object> mapping = new HashMap<String, Object>();
//
//    @Override
//    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        this.doPost(request, response);
//    }
//
//    @Override
//    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        try {
//
//            String url = request.getRequestURI();
//            String contextPath = request.getContextPath();
//            url = url.replace(contextPath, "").replaceAll("/+", "/");
//            if (!this.mapping.containsKey(url)) {
//                //找不到对应的url映射404
//                response.setContentType("text/html;charset=UTF-8");
//                response.getWriter().write("404 ->找不到资源啦!!");
//                return;
//            }
//            Method method = (Method) this.mapping.get(url);
//            Map<String, String[]> params = request.getParameterMap();
//            Object o = this.mapping.get(method.getDeclaringClass().getName());
//            method.invoke(o, new Object[]{request, response, params.get("name")[0]});
//        } catch (Exception e) {
//            response.setContentType("text/html;charset=UTF-8");
//            response.getWriter().write("500 服务器异常啦！！ " + Arrays.toString(e.getStackTrace()));
//        }
//    }
//
//
//    @Override
//    public void init(ServletConfig config) throws ServletException {
//        InputStream is = null;
//        try {
//            Properties configContext = new Properties();
//            //web.xml里面找到配置文件
//            is = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
//            configContext.load(is);
//            String scanPackage = configContext.getProperty("scan-package");
//            doScanner(scanPackage);
//            for (String className : mapping.keySet()) {
//                if (!className.contains(".")) {
//                    continue;
//                }
//                Class<?> clazz = Class.forName(className);
//                if (clazz.isAnnotationPresent(Controller.class)) {
//                    mapping.put(className, clazz.newInstance());
//                    String baseUrl = "";
//                    if (clazz.isAnnotationPresent(RequestMapping.class)) {
//                        RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
//                        baseUrl = requestMapping.value();
//                    }
//                    Method[] methods = clazz.getMethods();
//                    for (Method method : methods) {
//                        if (!method.isAnnotationPresent(RequestMapping.class)) {
//                            continue;
//                        }
//                        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
//                        String url = (baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
//                        mapping.put(url, method);
//                        System.out.println("Mapped " + url + "-->" + method);
//                    }
//                } else if (clazz.isAnnotationPresent(Service.class)) {
//                    Service service = clazz.getAnnotation(Service.class);
//                    String beanName = service.value();
//                    if ("".equals(beanName)) {
//                        beanName = clazz.getName();
//                    }
//                    Object instance = clazz.newInstance();
//                    mapping.put(beanName, instance);
//                    for (Class<?> i : clazz.getInterfaces()) {
//                        mapping.put(i.getName(), instance);
//                    }
//                } else {
//                    continue;
//                }
//            }
//            for (Object object : mapping.values()) {
//                if (object == null) {
//                    continue;
//                }
//                Class clazz = object.getClass();
//                //判断是否存在Controller注解
//                if (clazz.isAnnotationPresent(Controller.class)) {
//                    Field[] fields = clazz.getDeclaredFields();
//                    for (Field field : fields) {
//                        if (!field.isAnnotationPresent(Autowired.class)) {
//                            continue;
//                        }
//                        Autowired autowired = field.getAnnotation(Autowired.class);
//                        String beanName = autowired.value();
//                        if ("".equals(beanName)) {
//                            beanName = field.getType().getName();
//                        }
//                        field.setAccessible(true);
//                        try {
//                            //通过反射重新赋值
//                            field.set(mapping.get(clazz.getName()), mapping.get(beanName));
//                        } catch (IllegalAccessException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (is != null) {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        System.out.println("MVC 初始化完成\n");
//        System.out.println("映射有" + mapping.size() + "条");
////        mapping.forEach((k, v) -> {
////            System.out.println(k + "->" + v);
////        });
//    }
//
//    //扫描指定路径下的包
//    private void doScanner(String scanPackage) {
//        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
//        File classDir = new File(url.getFile());
//        for (File file : classDir.listFiles()) {
//            if (file.isDirectory()) {
//                doScanner(scanPackage + "." + file.getName());
//            } else {
//                if (!file.getName().endsWith(".class")) {
//                    continue;
//                }
//                String clazzName = (scanPackage + "." + file.getName().replace(".class", ""));
//                mapping.put(clazzName, null);
//            }
//        }
//    }
//}
