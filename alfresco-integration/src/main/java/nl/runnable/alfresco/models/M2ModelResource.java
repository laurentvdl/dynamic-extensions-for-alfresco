package nl.runnable.alfresco.models;

import org.alfresco.repo.dictionary.M2Model;
import org.springframework.core.io.Resource;

/**
 * Wrapper for an M2Model and the resource it was loaded from.
 * This wrapper adds resource information for any interested {@link nl.runnable.alfresco.models.ModelRegistrar}.
 *
 * @author Laurent Van der Linden
 */
public class M2ModelResource {
	private Resource resource;
	private M2Model m2Model;

	public M2ModelResource(Resource resource, M2Model m2Model) {
		this.resource = resource;
		this.m2Model = m2Model;
	}

	public Resource getResource() {
		return resource;
	}

	public M2Model getM2Model() {
		return m2Model;
	}

	public String getName() {
		return m2Model.getName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		M2ModelResource that = (M2ModelResource) o;

		if (!m2Model.equals(that.m2Model)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return m2Model.hashCode();
	}
}
