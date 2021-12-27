package wiki.heh.spring.annotation;

import java.lang.annotation.*;

/**
 * @author heh
 * @date 2021/12/27
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
    String value() default "";
}
