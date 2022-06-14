/*
 * Copyright 2021 the original author or authors.
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

package org.gradle.api.plugins.jvm.internal;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.artifacts.dsl.ConfigurationDependencyHandler;
import org.gradle.api.internal.DynamicObjectAware;
import org.gradle.api.plugins.jvm.JvmComponentDependencies;
import org.gradle.internal.metaobject.BeanDynamicObject;
import org.gradle.internal.metaobject.DynamicInvokeResult;
import org.gradle.internal.metaobject.DynamicObject;

import javax.inject.Inject;
import java.util.Map;

public class DefaultJvmComponentDependencies implements JvmComponentDependencies, DynamicObjectAware {
    private final ConfigurationDependencyHandler implementation;
    private final ConfigurationDependencyHandler compileOnly;
    private final ConfigurationDependencyHandler runtimeOnly;
    private final ConfigurationDependencyHandler annotationProcessor;
    private final DynamicObject dynamicObject;

    @Inject
    public DefaultJvmComponentDependencies(ConfigurationDependencyHandler implementation, ConfigurationDependencyHandler compileOnly, ConfigurationDependencyHandler runtimeOnly, ConfigurationDependencyHandler annotationProcessor) {
        this.implementation = implementation;
        this.compileOnly = compileOnly;
        this.runtimeOnly = runtimeOnly;
        this.annotationProcessor = annotationProcessor;

        Map<String, DynamicObject> dynamicByName = ImmutableMap.of(
            "implementation", new BeanDynamicObject(implementation),
            "compileOnly", new BeanDynamicObject(compileOnly),
            "runtimeOnly", new BeanDynamicObject(runtimeOnly),
            "annotationProcessor", new BeanDynamicObject(annotationProcessor)
        );
        this.dynamicObject = new BeanDynamicObject(this, ConfigurationDependencyHandler.class) {
            @Override
            public boolean hasMethod(String name, Object... arguments) {
                // We have a method if:
                // - we have a method with the same signature; or
                // - it is an attempt to invoke this object with arguments matching one of the `add` calls
                if (super.hasMethod(name, arguments)) {
                    return true;
                }
                DynamicObject byName = dynamicByName.get(name);
                if (byName == null) {
                    return false;
                }
                return byName.hasMethod("add", arguments);
            }

            @Override
            public DynamicInvokeResult tryInvokeMethod(String name, Object... arguments) {
                DynamicInvokeResult result = super.tryInvokeMethod(name, arguments);
                if (result.isFound()) {
                    return result;
                }
                DynamicObject byName = dynamicByName.get(name);
                if (byName == null) {
                    return result;
                }
                return byName.tryInvokeMethod("add", arguments);
            }
        };
    }

    @Override
    public ConfigurationDependencyHandler getImplementation() {
        return this.implementation;
    }

    @Override
    public ConfigurationDependencyHandler getCompileOnly() {
        return this.compileOnly;
    }

    @Override
    public ConfigurationDependencyHandler getRuntimeOnly() {
        return this.runtimeOnly;
    }

    @Override
    public ConfigurationDependencyHandler getAnnotationProcessor() {
        return this.annotationProcessor;
    }

    @Override
    public DynamicObject getAsDynamicObject() {
        return dynamicObject;
    }
}
