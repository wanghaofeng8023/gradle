/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.artifacts.dsl;

import org.gradle.api.Incubating;
import org.gradle.api.NonExtensible;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderConvertible;
import org.gradle.internal.HasInternalProtocol;

import java.util.Map;

/**
 * Factory class for creating {@link Dependency} instances, with strong typing.
 *
 * <p>
 * <b>Note:</b> This interface is not intended for implementation by build script or plugin authors.
 * </p>
 *
 * @since 7.6
 */
@HasInternalProtocol
@NonExtensible
@Incubating
public interface DependencyFactory {
    /**
     * Create an {@link ExternalModuleDependency} from the <code>"<i>group</i>:<i>name</i>:<i>version</i>:<i>classifier</i>@<i>extension</i>"</code> notation.
     *
     * <p>
     * Classifier and extension may each separately be omitted. Version may be omitted if there is no classifier.
     * </p>
     *
     * @param dependencyNotation the dependency notation
     * @return the new dependency
     */
    ExternalModuleDependency createFromCharSequence(CharSequence dependencyNotation);

    /**
     * Lazily create an {@link ExternalModuleDependency}, using the same notation as {@link #createFromCharSequence(CharSequence)}.
     *
     * @param dependencyNotation the dependency notation
     * @return a provider for the new dependency
     */
    default DependencyProvider<ExternalModuleDependency> createFromCharSequence(Provider<? extends CharSequence> dependencyNotation) {
        return createFromProvider(dependencyNotation.map(this::createFromCharSequence));
    }

    /**
     * Create an {@link ExternalModuleDependency} from a {@link MinimalExternalModuleDependency}.
     *
     * @param externalModule the dependency information
     * @return the new dependency
     */
    ExternalModuleDependency createFromExternalModule(MinimalExternalModuleDependency externalModule);

    /**
     * Lazily create an {@link ExternalModuleDependency}, using the same notation as {@link #createFromExternalModule(MinimalExternalModuleDependency)}.
     *
     * @param externalModule the dependency information
     * @return a provider for the new dependency
     */
    default DependencyProvider<ExternalModuleDependency> createFromExternalModule(Provider<? extends MinimalExternalModuleDependency> externalModule) {
        return createFromProvider(externalModule.map(this::createFromExternalModule));
    }

    /**
     * Lazily create an {@link ExternalModuleDependency}, using the same notation as {@link #createFromExternalModule(MinimalExternalModuleDependency)}.
     *
     * @param externalModule the dependency information
     * @return a provider for the new dependency
     */
    default DependencyProvider<ExternalModuleDependency> createFromExternalModule(ProviderConvertible<? extends MinimalExternalModuleDependency> externalModule) {
        return createFromExternalModule(externalModule.asProvider());
    }

    /**
     * Create an {@link ExternalModuleDependency} from a {@link Map}. The map may contain the following keys:
     * <ul>
     *     <li>{@code group}</li>
     *     <li>{@code name}</li>
     *     <li>{@code version}</li>
     *     <li>{@code classifier}</li>
     *     <li>{@code extension}</li>
     * </ul>
     *
     * @param map the dependency map
     * @return the new dependency
     */
    ExternalModuleDependency createFromMap(Map<String, ?> map);

    /**
     * Lazily create an {@link ExternalModuleDependency}, using the same notation as {@link #createFromMap(Map)}.
     *
     * @param map the dependency map
     * @return a provider for the new dependency
     */
    default DependencyProvider<ExternalModuleDependency> createFromMap(Provider<? extends Map<String, ?>> map) {
        return createFromProvider(map.map(this::createFromMap));
    }

    /**
     * Create a {@link FileCollectionDependency} from a {@link FileCollection}.
     *
     * @param fileCollection the file collection
     * @return the new dependency
     */
    FileCollectionDependency createFromFileCollection(FileCollection fileCollection);

    /**
     * Create a {@link ProjectDependency} from a {@link Project}.
     *
     * @param project the project
     * @return the new dependency
     */
    ProjectDependency createFromProject(Project project);

    /**
     * Lazily create a {@link ProjectDependency}, using the same notation as {@link #createFromProject(Project)}.
     *
     * @param project the project
     */
    default DependencyProvider<ProjectDependency> createFromProject(Provider<? extends Project> project) {
        return createFromProvider(project.map(this::createFromProject));
    }

    /**
     * Convert a {@link Provider} for a {@link Dependency} to a {@link DependencyProvider}. This assists with using a generic provider
     * without erasure being an issue.
     *
     * <p>
     * The provider's value will be cached on first access, so that there's only one instance of the dependency created.
     * </p>
     *
     * @param dependencyProvider the dependency provider
     * @return a more strongly-typed and cached dependency provider
     * @param <D> the dependency type
     */
    <D extends Dependency> DependencyProvider<D> createFromProvider(Provider<D> dependencyProvider);
}
