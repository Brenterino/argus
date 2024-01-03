plugins {
    id("architectury-plugin")
    id("dev.architectury.loom") apply false
}

val minecraftVersion: String by project

architectury {
    minecraft = minecraftVersion
}

allprojects {
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 17
    }
}
