package org.openspaces.core.util;

import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * General utility methods for working with annotations, handling bridge methods
 * (which the compiler generates for generic declarations) as well as super
 * methods (for optional &quot;annotation inheritance&quot;). Note that none of
 * this is provided by the JDK's introspection facilities themselves.
 *
 * <p>As a general rule for runtime-retained annotations (e.g. for transaction
 * control, authorization or service exposure), always use the lookup methods on
 * this class (e.g., {@link #findAnnotation(Method, Class)},
 * {@link #getAnnotation(Method, Class)}, and {@link #getAnnotations(Method)})
 * instead of the plain annotation lookup methods in the JDK. You can still
 * explicitly choose between lookup on the given class level only
 * ({@link #getAnnotation(Method, Class)}) and lookup in the entire inheritance
 * hierarchy of the given method ({@link #findAnnotation(Method, Class)}).
 */
public abstract class AnnotationUtils {

    /**
     * The attribute name for annotations with a single element
     */
    static final String VALUE = "value";


    /**
     * Get all {@link Annotation Annotations} from the supplied {@link Method}.
     * <p>Correctly handles bridge {@link Method Methods} generated by the compiler.
     *
     * @param method the method to look for annotations on
     * @return the annotations found
     * @see org.springframework.core.BridgeMethodResolver#findBridgedMethod(Method)
     */
    public static Annotation[] getAnnotations(Method method) {
        return BridgeMethodResolver.findBridgedMethod(method).getAnnotations();
    }

    /**
     * Get a single {@link Annotation} of <code>annotationType</code> from the
     * supplied {@link Method}.
     * <p>Correctly handles bridge {@link Method Methods} generated by the compiler.
     *
     * @param method         the method to look for annotations on
     * @param annotationType the annotation class to look for
     * @return the annotations found
     * @see org.springframework.core.BridgeMethodResolver#findBridgedMethod(Method)
     */
    public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationType) {
        return BridgeMethodResolver.findBridgedMethod(method).getAnnotation(annotationType);
    }

    /**
     * Get a single {@link Annotation} of <code>annotationType</code> from the
     * supplied {@link Method}, traversing its super methods if no annotation
     * can be found on the given method itself.
     * <p>Annotations on methods are not inherited by default, so we need to handle
     * this explicitly.
     *
     * @param method         the method to look for annotations on
     * @param annotationType the annotation class to look for
     * @return the annotation of the given type, or <code>null</code> if none found
     */
    public static <A extends Annotation> A findAnnotation(Method method, Class<A> annotationType) {
        A annotation = getAnnotation(method, annotationType);
        Class<?> cl = method.getDeclaringClass();
        while (annotation == null) {
            cl = cl.getSuperclass();
            if (cl == null || cl.equals(Object.class)) {
                break;
            }
            try {
                Method equivalentMethod = cl.getDeclaredMethod(method.getName(), method.getParameterTypes());
                annotation = getAnnotation(equivalentMethod, annotationType);
            }
            catch (NoSuchMethodException ex) {
                // We're done...
            }
        }
        return annotation;
    }

    /**
     * Find a single {@link Annotation} of <code>annotationType</code> from the
     * supplied {@link Class}, traversing its interfaces and super classes
     * if no annotation can be found on the given class itself.
     * <p>This method explicitly handles class-level annotations which are not
     * declared as {@link java.lang.annotation.Inherited inherited} as well as
     * annotations on interfaces.
     *
     * @param clazz          the class to look for annotations on
     * @param annotationType the annotation class to look for
     * @return the annotation of the given type found, or <code>null</code>
     */
    public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
        Assert.notNull(clazz, "Class must not be null");
        A annotation = clazz.getAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        for (Class<?> ifc : clazz.getInterfaces()) {
            annotation = findAnnotation(ifc, annotationType);
            if (annotation != null) {
                return annotation;
            }
        }
        if (clazz.getSuperclass() == null || Object.class.equals(clazz.getSuperclass())) {
            return null;
        }
        return findAnnotation(clazz.getSuperclass(), annotationType);
    }

    /**
     * Find the first {@link Class} in the inheritance hierarchy of the
     * specified <code>clazz</code> (including the specified
     * <code>clazz</code> itself) which declares an annotation for the
     * specified <code>annotationType</code>, or <code>null</code> if not
     * found. If the supplied <code>clazz</code> is <code>null</code>,
     * <code>null</code> will be returned.
     * <p>If the supplied <code>clazz</code> is an interface, only the interface
     * itself will be checked; the inheritance hierarchy for interfaces will not
     * be traversed.
     * <p>The standard {@link Class} API does not provide a mechanism for
     * determining which class in an inheritance hierarchy actually declares an
     * {@link Annotation}, so we need to handle this explicitly.
     *
     * @param annotationType the Class object corresponding to the annotation type
     * @param clazz          the Class object corresponding to the class on which to
     *                       check for the annotation, or <code>null</code>.
     * @return the first {@link Class} in the inheritance hierarchy of the
     *         specified <code>clazz</code> which declares an annotation for the specified
     *         <code>annotationType</code>, or <code>null</code> if not found.
     * @see Class#isAnnotationPresent(Class)
     * @see Class#getDeclaredAnnotations()
     */
    public static Class<?> findAnnotationDeclaringClass(Class<? extends Annotation> annotationType, Class<?> clazz) {
        Assert.notNull(annotationType, "Annotation type must not be null");
        if (clazz == null || clazz.equals(Object.class)) {
            return null;
        }
        return (isAnnotationDeclaredLocally(annotationType, clazz)) ?
                clazz : findAnnotationDeclaringClass(annotationType, clazz.getSuperclass());
    }

    /**
     * Determine whether an annotation for the specified <code>annotationType</code>
     * is declared locally on the supplied <code>clazz</code>.
     * The supplied {@link Class} object may represent any type.
     * <p>Note: This method does <strong>not</strong> determine if the annotation
     * is {@link java.lang.annotation.Inherited inherited}. For greater clarity
     * regarding inherited annotations, consider using
     * {@link #isAnnotationInherited(Class, Class)} instead.
     *
     * @param annotationType the Class object corresponding to the annotation type
     * @param clazz          the Class object corresponding to the class on which to
     *                       check for the annotation
     * @return <code>true</code> if an annotation for the specified
     *         <code>annotationType</code> is declared locally on the supplied <code>clazz</code>
     * @see Class#getDeclaredAnnotations()
     * @see #isAnnotationInherited(Class, Class)
     */
    public static boolean isAnnotationDeclaredLocally(Class<? extends Annotation> annotationType, Class<?> clazz) {
        Assert.notNull(annotationType, "Annotation type must not be null");
        Assert.notNull(clazz, "Class must not be null");
        boolean declaredLocally = false;
        for (Annotation annotation : Arrays.asList(clazz.getDeclaredAnnotations())) {
            if (annotation.annotationType().equals(annotationType)) {
                declaredLocally = true;
                break;
            }
        }
        return declaredLocally;
    }

    /**
     * Determine whether an annotation for the specified <code>annotationType</code>
     * is present on the supplied <code>clazz</code> and is
     * {@link java.lang.annotation.Inherited inherited}
     * (i.e., not declared locally for the class).
     * <p>If the supplied <code>clazz</code> is an interface, only the interface
     * itself will be checked. In accord with standard meta-annotation
     * semantics, the inheritance hierarchy for interfaces will not be
     * traversed. See the {@link java.lang.annotation.Inherited JavaDoc} for the
     * &#064;Inherited meta-annotation for further details regarding annotation
     * inheritance.
     *
     * @param annotationType the Class object corresponding to the annotation type
     * @param clazz          the Class object corresponding to the class on which to
     *                       check for the annotation
     * @return <code>true</code> if an annotation for the specified
     *         <code>annotationType</code> is present on the supplied <code>clazz</code>
     *         and is {@link java.lang.annotation.Inherited inherited}
     * @see Class#isAnnotationPresent(Class)
     * @see #isAnnotationDeclaredLocally(Class, Class)
     */
    public static boolean isAnnotationInherited(Class<? extends Annotation> annotationType, Class<?> clazz) {
        Assert.notNull(annotationType, "Annotation type must not be null");
        Assert.notNull(clazz, "Class must not be null");
        return (clazz.isAnnotationPresent(annotationType) && !isAnnotationDeclaredLocally(annotationType, clazz));
    }

    /**
     * Retrieve the given annotation's attributes as a Map.
     *
     * @param annotation the annotation to retrieve the attributes for
     * @return the Map of annotation attributes, with attribute names as keys
     *         and corresponding attribute values as values
     */
    public static Map<String, Object> getAnnotationAttributes(Annotation annotation) {
        Map<String, Object> attrs = new HashMap<String, Object>();
        Method[] methods = annotation.annotationType().getDeclaredMethods();
        for (int j = 0; j < methods.length; j++) {
            Method method = methods[j];
            if (method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
                try {
                    attrs.put(method.getName(), method.invoke(annotation));
                }
                catch (Exception ex) {
                    throw new IllegalStateException("Could not obtain annotation attribute values", ex);
                }
            }
        }
        return attrs;
    }

    /**
     * Retrieve the <em>value</em> of the <code>&quot;value&quot;</code>
     * attribute of a single-element Annotation, given an annotation instance.
     *
     * @param annotation the annotation instance from which to retrieve the value
     * @return the attribute value, or <code>null</code> if not found
     * @see #getValue(Annotation, String)
     */
    public static Object getValue(Annotation annotation) {
        return getValue(annotation, VALUE);
    }

    /**
     * Retrieve the <em>value</em> of a named Annotation attribute, given an
     * annotation instance.
     *
     * @param annotation    the annotation instance from which to retrieve the value
     * @param attributeName the name of the attribute value to retrieve
     * @return the attribute value, or <code>null</code> if not found
     * @see #getValue(Annotation)
     */
    public static Object getValue(Annotation annotation, String attributeName) {
        try {
            Method method = annotation.annotationType().getDeclaredMethod(attributeName, new Class[0]);
            return method.invoke(annotation);
        }
        catch (Exception ex) {
            return null;
        }
    }

    /**
     * Retrieve the <em>default value</em> of the
     * <code>&quot;value&quot;</code> attribute of a single-element
     * Annotation, given an annotation instance.
     *
     * @param annotation the annotation instance from which to retrieve
     *                   the default value
     * @return the default value, or <code>null</code> if not found
     * @see #getDefaultValue(Annotation, String)
     */
    public static Object getDefaultValue(Annotation annotation) {
        return getDefaultValue(annotation, VALUE);
    }

    /**
     * Retrieve the <em>default value</em> of a named Annotation attribute,
     * given an annotation instance.
     *
     * @param annotation    the annotation instance from which to retrieve
     *                      the default value
     * @param attributeName the name of the attribute value to retrieve
     * @return the default value of the named attribute, or <code>null</code>
     *         if not found.
     * @see #getDefaultValue(Class, String)
     */
    public static Object getDefaultValue(Annotation annotation, String attributeName) {
        return getDefaultValue(annotation.annotationType(), attributeName);
    }

    /**
     * Retrieve the <em>default value</em> of the
     * <code>&quot;value&quot;</code> attribute of a single-element
     * Annotation, given the {@link Class annotation type}.
     *
     * @param annotationType the <em>annotation type</em> for which the
     *                       default value should be retrieved
     * @return the default value, or <code>null</code> if not found
     * @see #getDefaultValue(Class, String)
     */
    public static Object getDefaultValue(Class<? extends Annotation> annotationType) {
        return getDefaultValue(annotationType, VALUE);
    }

    /**
     * Retrieve the <em>default value</em> of a named Annotation attribute,
     * given the {@link Class annotation type}.
     *
     * @param annotationType the <em>annotation type</em> for which the
     *                       default value should be retrieved
     * @param attributeName  the name of the attribute value to retrieve.
     * @return the default value of the named attribute, or <code>null</code>
     *         if not found
     * @see #getDefaultValue(Annotation, String)
     */
	public static Object getDefaultValue(Class<? extends Annotation> annotationType, String attributeName) {
		try {
			Method method = annotationType.getDeclaredMethod(attributeName, new Class[0]);
			return method.getDefaultValue();
		}
		catch (Exception ex) {
			return null;
		}
	}

}
