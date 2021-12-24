package wiki.heh.demo.service;

import wiki.heh.spring.annotation.Service;

/**
 * @author heh
 * @date 2021/12/24
 */
@Service
public class TestService {

    public String get(String v) {
        return "经过service返回的->" + v;
    }
}
