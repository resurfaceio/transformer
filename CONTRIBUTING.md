# Contributing to resurfaceio-transformer
&copy; 2016-2022 Resurface Labs Inc.

## Coding Conventions

Our code style is whatever IntelliJ IDEA does by default, with the exception of allowing lines up to 130 characters.
If you don't use IDEA, that's ok, but your code may get reformatted.

## Git Workflow

Initial setup:

```
git clone git@github.com:resurfaceio/transformer.git resurfaceio-transformer
cd resurfaceio-transformer
```

Test and package:

```
mvn package
```

Committing changes:

```
git add -A
git commit -m "#123 Updated readme"       (123 is the GitHub issue number)
git pull --rebase                         (avoid merge bubbles)
git push origin master
```

## Release Process

Push this new version to CloudSmith:

```
mvn deploy
```

Tag release version:

```
git tag v2.x.x
git push origin master --tags
```

Start the next version by incrementing the version number. (located in pom.xml)
