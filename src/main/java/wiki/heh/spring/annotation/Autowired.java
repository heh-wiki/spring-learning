package wiki.heh.spring.annotation;

import java.lang.annotation.*;

/**
 * @author heh
 * @date 2021/12/24
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    String value() default "";
}
