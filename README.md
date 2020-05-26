# csv2gdst

A simple utility (very simple) for extending existing Drools gdst files with csv files.

# Usage

## Compile 
```shell script
mvn clean package
```

## Prepare

You'll need to download a copy of the gdst file from Business Central. 

The import CSV files must have a header row defining the variables as defined in the gdst file.

```csv
descriptionCol,$aVar,$anotherVar,$lastVar
This is a row,1,2,3
This is another row,4,5,6
``` 

To create a template CSV file complete with existing data, use the `convert` sub-command

```shell script
java -jar target/csv2gdst.jar convert -i=<input_gdst_file> -o=<output_csv_file>
```

To create a template CSV file with headers only, use the `convert` sub-command with the -t option

```shell script
java -jar target/csv2gdst.jar convert -t -i=<input_gdst_file> -o=<output_csv_file>
```

## Import

```shell script
java -jar target/csv2gdst.jar import -i=<input_gdst_file> -o=<output_gdst_file> <csv_file>
```

By default, existing data entries will be left as is. To replace the data entries, add the `-r` option.

```shell script
java -jar target/csv2gdst.jar import -i=<input_gdst_file> -r -o=<output_gdst_file> <csv_file>
```

# License

This project is licensed under either of

 * Apache License, Version 2.0, ([LICENSE-APACHE](LICENSE-APACHE) or
   http://www.apache.org/licenses/LICENSE-2.0)
 * MIT license ([LICENSE-MIT](LICENSE-MIT) or
   http://opensource.org/licenses/MIT)

at your option.

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in csv2gdst by you, as defined in the Apache-2.0 license, shall be
dual licensed as above, without any additional terms or conditions.
