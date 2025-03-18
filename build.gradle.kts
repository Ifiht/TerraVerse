import xyz.jpenilla.runpaper.task.RunServer

plugins {
    java
    kotlin("jvm") version "2.1.20-RC3"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.evlis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

val prodPlugins = runPaper.downloadPluginsSpec {
    modrinth("terra", "6.6.1-BETA-bukkit")
    modrinth("multiverse-core", "4.3.15-pre.2")
    modrinth("multiverse-inventories", "4.2.7-pre")
    modrinth("multiverse-portals", "4.3.0-pre")
    modrinth("multiverse-netherportals", "4.3.0-pre.3")
}

val testPlugins = runPaper.downloadPluginsSpec {
    from(prodPlugins) // Copy everything from prod
    github("Ifiht", "AutoStop", "v1.2.0", "AutoStop-1.2.0.jar")
}

// Test PaperMC run & immediately shut down, for github actions
tasks.register<RunServer>("runServerTest") {
    //dependsOn(tasks.shadowJar)
    minecraftVersion("1.21.4")
    downloadPlugins.from(testPlugins)
    pluginJars.from(tasks.shadowJar)
}
// Start a local PaperMC test server for login & manual testing
tasks.register<RunServer>("runServerInteractive") {
    //dependsOn(tasks.shadowJar)
    minecraftVersion("1.21.4")
    downloadPlugins.from(prodPlugins)
    pluginJars.from(tasks.shadowJar)
}