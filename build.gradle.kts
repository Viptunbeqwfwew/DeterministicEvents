
plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

tasks.processResources {
    val authorsRaw = project.findProperty("authors") as String
    val authorsJson = authorsRaw.split(",").joinToString(",") { "\"${it.trim()}\"" }

    inputs.property("modDescription", project.extra["modDescription"])
    inputs.property("modUrl", project.extra["modUrl"])
    inputs.property("modAuthors", authorsJson)

    filesMatching("mcmod.info") {
        name = "${project.extra["modId"]}.info"

        expand(
            inputs.properties
        )
    }
}
// minecraft.extraRunJvmArguments.add("-Dlegacy.debugClassLoading=true")
// minecraft.extraRunJvmArguments.add("-Dlegacy.debugClassLoadingSave=true")
