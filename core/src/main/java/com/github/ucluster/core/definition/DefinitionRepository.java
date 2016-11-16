package com.github.ucluster.core.definition;

import java.util.Map;

public interface DefinitionRepository<D extends Definition> {

    D find(Map<String, String> metadata);
}
