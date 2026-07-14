dependencies {
    add("implementation", project(":annotations"))
    add("implementation", project(":shared"))
    add("implementation", "org.ow2.asm:asm:9.10.1")

    // for test
    add("testImplementation", project(":vm"))
}
