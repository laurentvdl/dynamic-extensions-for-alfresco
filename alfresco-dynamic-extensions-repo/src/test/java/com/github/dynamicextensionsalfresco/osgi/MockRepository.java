package com.github.dynamicextensionsalfresco.osgi;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.tenant.TenantService;

import java.util.List;

/**
 * Simulated {@link Repository}-like class for use in integration tests.
 * 
 * @author Laurens Fridael
 * 
 */
public class MockRepository {

	public void setDictionaryDAO(final DictionaryDAO dictionaryDAO) {
	}

	public void setTenantService(final TenantService tenantService) {
	}

	public void setModels(final List<String> models) {
	}

	public void setLabels(final List<String> labels) {
	}

}
