package wiki.heh.spring.annotation;

import java.lang.annotation.*;

/**
 * @author heh
 * @date 2021/12/24
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    String value() default "";
}
