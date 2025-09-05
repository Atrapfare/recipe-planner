package de.unistuttgart.iste.sopra.api;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.*;

/**
 * Custom annotation for marking components and controllers as part of API version 1.
 * This annotation combines {@link Component} and {@link RequestMapping} for a unified declaration.
 * All components annotated with {@code @ApiVersion1} are mapped to the base path `/api/v1`.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@RequestMapping("/api/v1")
public @interface ApiVersion1 {

    /**
     * Alias for the {@link Component} annotation's value.
     * Allows specifying a custom name for the component.
     *
     * @return The name of the component.
     */
    @AliasFor(annotation = Component.class)
    String value() default "";
}