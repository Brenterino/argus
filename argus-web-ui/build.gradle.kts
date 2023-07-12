import com.github.gradle.node.npm.task.NpmTask

plugins {
    java
    id("com.github.node-gradle.node")
}

node {
    version.set("18.16.0")
    npmVersion.set("9.5.1")
    download.set(true)
}

val npmBuild = tasks.register<NpmTask>("npmBuild") {
    dependsOn(tasks.npmInstall)
    npmCommand.set(listOf("run", "build"))
}

tasks {
    clean {
        delete("dist")
    }

    build {
        dependsOn(npmBuild)
    }
}
