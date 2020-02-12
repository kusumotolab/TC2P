# TC2P
TC2P is a technique which mines the tree-based code change patterns from given the git repository.

## Requirement
- JDK 1.8

## Build
Please clone this repository and execute this command.

```shell script 
./gradlew jar
```

## Usage
### Preprocess (Save the tree to SQLite)
Before mining code change patterns, you must save edit trees from a given repository.
So, please execute this command.

```shell script
java -jar build/libs/TC2P-alpha.jar preprocess -r <TARGET_REPOSITORY>
```

### Mining Code Change Pattern from SQLite

```shell script
java -jar build/libs/TC2P-alpha.jar mining -f <FREQUENCY> -p <PROJECT_NAME>
```
