javaLibrary {
    dependencies {
        implementation(project(":annotations"))
        implementation(project(":shared"))
        implementation("org.ow2.asm:asm:9.10.1")
    }

    testing {
        dependencies {
            implementation(project(":vm"))
        }
    }
}
