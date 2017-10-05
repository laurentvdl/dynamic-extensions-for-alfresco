package com.github.dynamicextensionsalfresco.webscripts.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {

	/**
	 * The request parameter name. If not specified, the parameter's Java variable name will be used instead. The
	 * variable name is only available if classes are compiled with debug info.
	 * @return the parameter name
	 */
	String value() default "";

	/**
	 * The default value. If a default value is specified, the {@link #required()} setting has no effect.
     * @return the default value
	 */
	String defaultValue() default "";

	/**
	 * Indicates whether the parameter is required. This setting has no effect if {@link #defaultValue()} is specified
     * @return if a parameter is required, default is true.
	 */
	boolean required() default true;

	/**
	 * Specifies the delimiter for splitting the request parameter into multiple values. This setting only has effect if
	 * the argument is an Array.
     * @return the delimiter used for splitting the parameter.
	 */
	String delimiter() default "";

}
