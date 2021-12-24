package wiki.heh.demo.controller;

import wiki.heh.demo.service.TestService;
import wiki.heh.spring.annotation.Autowired;
import wiki.heh.spring.annotation.Controller;
import wiki.heh.spring.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author heh
 * @date 2021/12/24
 */
@Controller
public class TestController {

    @Autowired
    private TestService service;

    @RequestMapping("/name")
    public void test(HttpServletRequest request, HttpServletResponse response, String name) {
        String result = service.get(name);
        System.out.println(result);
        try {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
