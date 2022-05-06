# MedianForLargeFiles
Algorithm to calculate the median of a file too large for memory.

It will read the whole input file (one number at a time) and store the values sorted to disk. After it has been fully read its median is retrieved.

Run with
```
./gradlew run --args="[filename] {memory}"
```
```
[filename] = path to the file to read. A list of doubles is expected with each number in a new line. (see number.txt as example)
{memory} - OPTIONAL = maximum amount of data to be loaded into memory (Megabytes) - default is 2048
           NOTE: Due to some overheads you shouldn't set this to more than 50% of your available memory
```

## Implementation
* Half of the memory specified is used for an in-memory cache to avoid constant file writes
* The other half specifies the maximum file size
* Numbers are written into the cache until it's full and then a part of the cache data is merged with the data stored in files and flushed
* Each file stores a certain value range
* We logically keep track of files and split them into new files once they become too large
* When retrieving the median only the file containing the median is loaded from disk

## Current optimization potential
* The application will allocate more memory than specified in the parameter - copying and sorting could in the worst case increase memory usage by 100%
* The data written to the file system is not compressed - thus requiring as much disk space as the input file is in size.

## Dependencies
* ch.qos.logback = for logging
* it.unimi.dsi:fastutil = for lists with primitive data types

Requires at least Java 15
