package nl.runnable.alfresco.models;

import org.alfresco.repo.dictionary.M2Namespace;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Based class for Model registrars. Registers Models by dependency order.
 *
 * @author Laurent Van der Linden
 */
public abstract class AbstractModelRegistrar implements ModelRegistrar {
	private final static Logger logger = LoggerFactory.getLogger(AbstractModelRegistrar.class);

	/* Configuration */

	private List<M2ModelResource> models = Collections.emptyList();

	@Override
	public void registerModels() {
		final Map<String, M2ModelResource> namespaceProviders = new HashMap<String, M2ModelResource>();
		final List<M2ModelResource> modelsToRegister = getModels();
		for (final M2ModelResource m2ModelResource : modelsToRegister) {
			for (final M2Namespace m2Namespace : m2ModelResource.getM2Model().getNamespaces()) {
				logger.debug("{} will provide namespace '{}'", m2ModelResource.getName(), m2Namespace.getUri());
				namespaceProviders.put(m2Namespace.getUri(), m2ModelResource);
			}
		}
		final Set<M2ModelResource> registeredModels = new HashSet<M2ModelResource>(modelsToRegister.size());
		for (final M2ModelResource model : modelsToRegister) {
			final Set<M2ModelResource> visitedModels = new HashSet<M2ModelResource>(modelsToRegister.size());
			visitModel(model, namespaceProviders, registeredModels, visitedModels);
		}
	}

	protected void visitModel(M2ModelResource modelResource, Map<String, M2ModelResource> namespaceProviders,
	                             Set<M2ModelResource> registeredModels, Set<M2ModelResource> visitedModels) {
		visitedModels.add(modelResource);
		final List<M2Namespace> imports = modelResource.getM2Model().getImports();
		for (final M2Namespace anImport : imports) {
			final M2ModelResource providingModel = namespaceProviders.get(anImport.getUri());
			if (providingModel != null && !registeredModels.contains(providingModel)) {
				Assert.isTrue(
						!visitedModels.contains(providingModel),
						String.format("Circular dependency detected between %s and %s", modelResource.getName(),
								providingModel.getName()));
				logger.debug("Discovered {} dependency on '{}', resolving {} first", new Object[] { modelResource.getName(),
						anImport.getUri(), providingModel.getName() });
				visitModel(providingModel, namespaceProviders, registeredModels, visitedModels);
			}
		}
		if (!registeredModels.contains(modelResource)) {
			try {
				registerModel(modelResource);
				registeredModels.add(modelResource);
			} catch (final DictionaryException e) {
				if (logger.isWarnEnabled()) {
					logger.warn(String.format("Could not register model '%s'", modelResource.getName()), e);
				}
			}
		}
	}

	protected abstract void registerModel(final M2ModelResource modelResource);

	/* Configuration */

	public void setModels(final List<M2ModelResource> models) {
		Assert.notNull(models, "Models cannot be null.");
		this.models = models;
	}

	public List<M2ModelResource> getModels() {
		return models;
	}
}
