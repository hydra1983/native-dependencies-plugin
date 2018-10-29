/*
* Copyright (C) 2016 Author <dictfb#gmail.com>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.github.hydra1983.android

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class NativeDependenciesPlugin implements Plugin<Project> {

    final Logger log = Logging.getLogger NativeDependenciesPlugin

    @Override
    void apply(Project project) {

        if (!hasAndroidPlugin(project)) {
            log.info 'No Android plugin detecting. Skipping resolve native dependencies.'
            return
        }

        project.extensions.create("nativeso", NativeDependenciesPluginExtension)
        project.afterEvaluate { evaluateResult ->
            if (evaluateResult.state.getFailure() == null) {
                Task task = project.task("collectso", type: NativeSoFilesCollectorTask)
                def dependencies = []
                dependencies.addAll(project.configurations.compile.files.findAll {
                    it.path.endsWith(".so")
                })
                // https://qiita.com/MirrgieRiana/items/d3271f6979c1451207a6
                // http://bbs.bugcode.cn/t/95606
                dependencies.addAll(project.configurations.releaseCompileClasspath.files.findAll {
                    it.path.endsWith(".so")
                })
                task.dependencies = dependencies
                project.tasks.findByName('preBuild').dependsOn task
            }
        }
    }

    static def hasAndroidPlugin(Project project) {
        return (project.pluginManager.hasPlugin("com.android.application")
            || project.pluginManager.hasPlugin("com.android.library"))
    }
}
