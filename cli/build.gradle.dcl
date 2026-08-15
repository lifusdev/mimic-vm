javaApplication {
    javaVersion = 21
    mainClass = "com.mimicvm.cli.Main"

    dependencies {
        implementation(project(":transformer"))
    }
}
