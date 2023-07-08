import com.github.gradle.node.npm.task.NpmTask

plugins {
    id("com.github.node-gradle.node")
}

node {
    version.set("18.16.0")
    npmVersion.set("9.5.1")
    download.set(true)
}

val cleanTask = tasks.register("clean") {
    doFirst {
        delete("dist")
    }
}

val buildTaskUsingNpm = tasks.register<NpmTask>("npmBuild") {
    dependsOn(cleanTask, tasks.npmInstall)
    npmCommand.set(listOf("run", "build"))
}

tasks.register("build") {
    dependsOn(buildTaskUsingNpm)
}
