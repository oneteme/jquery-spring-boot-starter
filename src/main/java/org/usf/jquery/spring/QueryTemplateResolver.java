package org.usf.jquery.spring;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.function.Supplier;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.usf.jquery.mvc.MvcRequest;
import org.usf.jquery.mvc.QueryInterpreter;
import org.usf.jquery.mvc.QueryTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
public class QueryTemplateResolver implements HandlerMethodArgumentResolver {
	
	private final QueryInterpreter interpreter = new QueryInterpreter();

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasMethodAnnotation(QueryTemplate.class) 
				&& parameter.getNestedParameterType() == MvcRequest.class;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {		
		var ann = parameter.getMethodAnnotation(QueryTemplate.class);
		if(nonNull(ann)) {
			if(parameter.getNestedParameterType() == MvcRequest.class) {
				return cacheAttribute(webRequest, MvcRequest.class, 
						()-> interpreter.resolveQueryComposer(parameter.getMethod(), webRequest.getParameterMap()));
			}
			throw new IllegalStateException("unsupported parameter type: " + parameter.getNestedParameterType());
		}
		throw new IllegalStateException("missing @QueryRequest annotation");
	}

	private static <T> T cacheAttribute(NativeWebRequest webRequest, Class<T> clazz, Supplier<? extends T> supplier) {
		var att = webRequest.getAttribute(clazz.getName(), 0);
		if(isNull(att)) {
			att = supplier.get();
			webRequest.setAttribute(clazz.getName(), att, 0);
		}
		return clazz.cast(att);
	}
}

