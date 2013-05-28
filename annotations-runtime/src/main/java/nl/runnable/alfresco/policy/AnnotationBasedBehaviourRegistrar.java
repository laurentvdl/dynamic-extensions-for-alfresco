package nl.runnable.alfresco.policy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.runnable.alfresco.AbstractAnnotationBasedRegistrar;
import nl.runnable.alfresco.behaviours.annotations.AssociationPolicy;
import nl.runnable.alfresco.behaviours.annotations.Behaviour;
import nl.runnable.alfresco.behaviours.annotations.ClassPolicy;
import nl.runnable.alfresco.behaviours.annotations.Event;
import nl.runnable.alfresco.behaviours.annotations.PropertyPolicy;

import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.Policy;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyType;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Registers {@link Behaviour}-annotated classes from beans in a {@link BeanFactory}.
 * 
 * @author Laurens Fridael
 * 
 */
public class AnnotationBasedBehaviourRegistrar extends AbstractAnnotationBasedRegistrar {

	/* Dependencies */

	private PolicyComponent policyComponent;

	/* Operations */

	/**
	 * Binds Behaviours to beans in the {@link BeanFactory} based on Behaviour annotations.
	 */
	public void bindBehaviours() {
		for (final String beanName : getBeanFactory().getBeanDefinitionNames()) {
			final Behaviour behaviour = getBeanFactory().findAnnotationOnBean(beanName, Behaviour.class);
			if (behaviour != null) {
				bindBehaviours(getBeanFactory().getBean(beanName), behaviour);
			}
		}
	}

	protected void bindBehaviours(final Object bean, final Behaviour behaviour) {
		Assert.notNull(bean, "Bean cannot be null.");

		final QName[] classNames = parseQNames(behaviour.value(), behaviour);
		final NotificationFrequency notificationFrequency = behaviour.event().toNotificationFrequency();

		for (final Entry<PolicyType, List<BehaviourMethod>> entry : getBehaviourMethodsByType(bean.getClass())
				.entrySet()) {
			for (final BehaviourMethod behaviourMethod : entry.getValue()) {
				switch (entry.getKey()) {
				case Class:
					bindClassPolicyBehaviour(bean, behaviourMethod.method, behaviourMethod.policyName, classNames,
							notificationFrequency);
					break;
				case Association:
					bindAssocationPolicyBehaviour(bean, behaviourMethod.method, behaviourMethod.policyName, classNames,
							notificationFrequency);
					break;
				case Property:
					bindPropertyPolicyBehaviour(bean, behaviourMethod.method, behaviourMethod.policyName, classNames,
							notificationFrequency);
					break;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void bindClassPolicyBehaviour(final Object bean, final Method method, final QName policyName,
			QName[] classNames, NotificationFrequency notificationFrequency) {
		final ClassPolicy classPolicy = AnnotationUtils.findAnnotation(method, ClassPolicy.class);
		if (classPolicy != null) {
			classNames = parseQNames(classPolicy.value(), classPolicy);
			if (classPolicy.event().equals(Event.INHERITED_OR_ALL) == false) {
				notificationFrequency = classPolicy.event().toNotificationFrequency();
			}
		}
		final JavaBehaviour behaviour = new JavaBehaviour(bean, method.getName(), notificationFrequency);
		if (classNames.length > 0) {
			for (final QName className : classNames) {
				if (className != null) {
					if (logger.isDebugEnabled()) {
						logger.debug(
								"Binding class Behaviour for policy {} and class {} for method {} with notification frequency {}.",
								new Object[] { policyName, className, method, notificationFrequency });
					}
					getPolicyComponent().bindClassBehaviour(policyName, className, behaviour);
				}
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Binding class Behaviour for policy {} for method {} with notification frequency {}.",
						new Object[] { policyName, method, notificationFrequency });
			}
			getPolicyComponent().bindClassBehaviour(policyName, bean, behaviour);
		}
		warnAboutInapplicablePolicyAnnotations(method, AssociationPolicy.class, PropertyPolicy.class);
	}

	@SuppressWarnings("unchecked")
	protected void bindAssocationPolicyBehaviour(final Object bean, final Method method, final QName policyName,
			QName[] classNames, NotificationFrequency notificationFrequency) {
		final AssociationPolicy associationPolicy = AnnotationUtils.findAnnotation(method, AssociationPolicy.class);
		QName assocationName = null;
		if (associationPolicy != null) {
      final String[] associationPolicyValues = associationPolicy.value();
      if (associationPolicyValues.length > 0) {
        // fall back to @Behaviour classnames if none provided on @AssociationPolicy
        classNames = parseQNames(associationPolicyValues, associationPolicy);
      }
			assocationName = parseQName(associationPolicy.association(), associationPolicy);
			if (associationPolicy.event().equals(Event.INHERITED_OR_ALL) == false) {
				notificationFrequency = associationPolicy.event().toNotificationFrequency();
			}
		}
		final JavaBehaviour behaviour = new JavaBehaviour(bean, method.getName(), notificationFrequency);
		if (classNames.length > 0) {
			for (final QName className : classNames) {
				if (assocationName != null) {
					if (logger.isDebugEnabled()) {
						logger.debug(
								"Binding association Behaviour for policy {}, class {} and association {} for method {} with notification frequency {}.",
								new Object[] { policyName, className, assocationName, method, notificationFrequency });
					}
					getPolicyComponent().bindAssociationBehaviour(policyName, className, assocationName, behaviour);
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug(
								"Binding association Behaviour for policy {} and class {} for method {} with notification frequency {}.",
								new Object[] { policyName, className, method, notificationFrequency });
					}
					getPolicyComponent().bindAssociationBehaviour(policyName, className, behaviour);
				}
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"Binding association Behaviour for policy {} for method {} with notification frequency {}.",
						new Object[] { policyName, method, notificationFrequency });
			}
			getPolicyComponent().bindAssociationBehaviour(policyName, bean, behaviour);
		}
		warnAboutInapplicablePolicyAnnotations(method, ClassPolicy.class, PropertyPolicy.class);
	}

	/*
	 * There don't seem to be any property policy interfaces defined in the Alfresco repository code. So binding
	 * property-level policies is left untested.
	 */
	@SuppressWarnings("unchecked")
	protected void bindPropertyPolicyBehaviour(final Object bean, final Method method, final QName policyName,
			QName[] classNames, NotificationFrequency notificationFrequency) {
		final PropertyPolicy propertyPolicy = AnnotationUtils.findAnnotation(method, PropertyPolicy.class);
		QName propertyName = null;
		if (propertyPolicy != null) {
			classNames = parseQNames(propertyPolicy.value(), propertyPolicy);
			propertyName = parseQName(propertyPolicy.property(), propertyPolicy);
			if (propertyPolicy.event().equals(Event.INHERITED_OR_ALL) == false) {
				notificationFrequency = propertyPolicy.event().toNotificationFrequency();
			}
		}
		final JavaBehaviour behaviour = new JavaBehaviour(bean, method.getName(), notificationFrequency);
		if (classNames.length > 1) {
			for (final QName className : classNames) {
				if (propertyName != null) {
					if (logger.isDebugEnabled()) {
						logger.debug(
								"Binding property Behaviour for policy {}, class {} and property {} for method {} with notification frequency {}.",
								new Object[] { policyName, className, propertyName, method, notificationFrequency });
					}
					getPolicyComponent().bindPropertyBehaviour(policyName, className, propertyName, behaviour);
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug(
								"Binding property Behaviour for policy {} and class {} for method {} with notification frequency {}.",
								new Object[] { policyName, className, method, notificationFrequency });
					}
					getPolicyComponent().bindPropertyBehaviour(policyName, className, behaviour);
				}
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Binding property Behaviour for policy {} for method {} with notification frequency {}.",
						new Object[] { policyName, method, notificationFrequency });
			}
			getPolicyComponent().bindPropertyBehaviour(policyName, bean, behaviour);
		}

		warnAboutInapplicablePolicyAnnotations(method, ClassPolicy.class, AssociationPolicy.class);
	}

	/* Utility operations */

	private QName getPolicyQName(final Class<? extends Policy> policyClass) {
		QName qName = null;
		try {
			final Field field = policyClass.getField("QNAME");
			/* Field must be static, public and of type QName. */
			final int modifiers = field.getModifiers();
			if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)
					&& QName.class.isAssignableFrom(field.getType())) {
				qName = (QName) field.get(null);
			}
		} catch (final NoSuchFieldException e) {
			/* No need to handle this. Simply leave qName at null. */
		} catch (final SecurityException e) {
			throw new RuntimeException(e);
		} catch (final IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return qName;
	}

	private void warnAboutInapplicablePolicyAnnotations(final Method method,
			final Class<? extends Annotation>... annotationClasses) {
		for (final Class<? extends Annotation> annotationClass : annotationClasses) {
			final Annotation annotation = AnnotationUtils.findAnnotation(method, annotationClass);
			if (annotation != null && logger.isWarnEnabled()) {
				logger.warn(
						"Found {} annotation on method {}. This annotation is not applicable here and will be ignored.",
						annotation.annotationType().getName(), method);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Map<PolicyType, List<Class<? extends Policy>>> getPolicyInterfacesByType(final Class<?> clazz) {
		final Map<PolicyType, List<Class<? extends Policy>>> policyInterfacesByType = new HashMap<PolicyType, List<Class<? extends Policy>>>();
		for (final Class<?> interfaceClass : clazz.getInterfaces()) {
			PolicyType policyType = null;
			if (org.alfresco.repo.policy.ClassPolicy.class.isAssignableFrom(interfaceClass)) {
				policyType = PolicyType.Class;
			} else if (org.alfresco.repo.policy.AssociationPolicy.class.isAssignableFrom(interfaceClass)) {
				policyType = PolicyType.Association;
			} else if (org.alfresco.repo.policy.PropertyPolicy.class.isAssignableFrom(interfaceClass)) {
				policyType = PolicyType.Property;
			}
			if (policyType != null) {
				if (policyInterfacesByType.containsKey(policyType) == false) {
					policyInterfacesByType.put(policyType, new ArrayList<Class<? extends Policy>>());
				}
				policyInterfacesByType.get(policyType).add((Class<? extends Policy>) interfaceClass);
			}
		}
		return policyInterfacesByType;
	}

	private Map<PolicyType, List<BehaviourMethod>> getBehaviourMethodsByType(final Class<? extends Object> beanClass) {
		final Map<PolicyType, List<Class<? extends Policy>>> policyInterfacesByType = getPolicyInterfacesByType(beanClass);
		final Map<PolicyType, List<BehaviourMethod>> behaviourMethodsByType = new HashMap<PolicyType, List<BehaviourMethod>>();
		for (final Entry<PolicyType, List<Class<? extends Policy>>> entry : policyInterfacesByType.entrySet()) {
			final List<BehaviourMethod> behaviourMethods = new ArrayList<BehaviourMethod>();
			for (final Class<? extends Policy> policyInterface : entry.getValue()) {
				if (policyInterface.getMethods().length == 1) {
					final Method policyInterfaceMethod = policyInterface.getMethods()[0];
					final Method behaviourMethod = ReflectionUtils.findMethod(beanClass,
							policyInterfaceMethod.getName(), policyInterfaceMethod.getParameterTypes());
					if (behaviourMethod != null) {
						behaviourMethods.add(new BehaviourMethod(getPolicyQName(policyInterface), behaviourMethod));
					}
				}
			}
			if (behaviourMethods.isEmpty() == false) {
				behaviourMethodsByType.put(entry.getKey(), behaviourMethods);
			}
		}
		return behaviourMethodsByType;
	}

	/* Dependencies */

	public void setPolicyComponent(final PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	protected PolicyComponent getPolicyComponent() {
		return policyComponent;
	}

	/* Utility classes */

	private static class BehaviourMethod {

		final QName policyName;

		final Method method;

		private BehaviourMethod(final QName policyName, final Method method) {
			this.policyName = policyName;
			this.method = method;
		}

	}

}
