# MedianForLargeFiles
Algorithm to calculate the median of a file too large for memory.

It will read the whole input file (one number at a time) and store the values sorted to disk. After it has been fully read it median is retrieved.


Run with
```
./gradlew run --args="[filename] {memory}"
```
```
[filename] = path to the file to read. A list of doubles is expected with each number in a new line
[memory] = maximum amount of data to be loaded into memory (Megabytes) - default is 8192
```

## Current optimization potential
* The jvm will allocate more memory than specified in the parameter - since we are using arraylists for in-memory storage it's possible that they expand to a size larger than needed
* The data written to the file system is not compressed - thus requiring as much disk space as the input file is in size.

## Dependencies
* ch.qos.logback = for logging
* it.unimi.dsi:fastutil = for lists with primitive data types
