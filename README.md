# Carte Report Engine

HTML reports and SVG-based charts.

*WARNING: This code is nowhere near being complete nor API-stable.
Use it at your own risk, and contributions would be welcome.*

<br/>

## Building the main modules from source

After cloning the main repository:
```shell
git clone https://github.com/css4j/carte.git
```
run the `gradlew` wrapper to build (requires Java 11 or later):
```shell
cd carte
./gradlew build
```

A variety of Gradle tasks can be executed:

- `./gradlew build` (normal build).

- `./gradlew build publishToMavenLocal` (to install in local Maven repository).

- `./gradlew uberjar` (to create a fat _jar_ with all dependencies).

- `./gradlew publish` (to deploy to a Maven repository, as described in the `publishing` block of
[io.sf.carte.java-conventions.gradle](https://github.com/css4j/carte/blob/master/buildSrc/src/main/groovy/io.sf.carte.java-conventions.gradle)).

<br/>

## Usage from a Gradle project
If your Gradle project depends on Carte, you can use this project's own Maven repository in a `repositories` section of
your build file, for easy access to dependencies:
```groovy
repositories {
    maven {
        url "https://css4j.github.io/maven/"
        mavenContent {
            releasesOnly()
        }
        content {
            includeGroup 'io.sf.carte'
            includeGroup 'io.sf.jclf'
            includeGroup 'com.github.css4j'
       }
    }
}
```
please use this repository **only** for the artifact groups listed in the
`includeGroup` statements.

Then, to use the core Carte module put the following in your `build.gradle` file:

```groovy
dependencies {
    implementation "io.sf.carte:carte:${carteVersion}"
}
```
where `carteVersion` could be defined in a `gradle.properties` file. Similarly,
to use `carte-jmh`:
```groovy
dependencies {
    implementation "io.sf.carte:carte-jmh:${carteVersion}"
}
```

<br/>

## Running the JMH report application

To run the benchmark report app on JMH-produced JSON files, you have to prepare
a configuration file and execute:

1) `./gradlew build uberjar`
2) `java -jar carte-jmh/build/libs/carte-jmh-<version>-all.jar --config=<path-to-config-file> *.json`

You could use the `example` files as a starting point, and run the
`benchmark-charts.sh` script that produces the graphs in
https://css4j.github.io/dom-mark.html.

Look inside the `dom-benchmark.xml` file:

- In the element with `documentStore` id of that file, it is configured that the
file `~/www/css4j.github.io/dom-mark.html` is a `DocumentStore` where the SVG
graphs will be put.

- And the element with a `fileStore` id tells that the directory
`~/www/css4j.github.io/benchmark` would be a `FileStore` containing the fallback
images.

All that you have to do is to download your copy of the `dom-mark.html` file and
modify the paths in `dom-benchmark.xml` as necessary. Then:

```shell
cd carte-jmh/examples
./benchmark-charts.sh dom-*.json html-build.json iterator*.json xml*.json ele*.json
```

And you will reproduce the JMH charts.
