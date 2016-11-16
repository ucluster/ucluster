package com.github.ucluster.core.definition;

import com.github.ucluster.core.ApiRequest;

public interface DefinitionRepository<D extends Definition> {

    D find(ApiRequest.Metadata metadata);
}
