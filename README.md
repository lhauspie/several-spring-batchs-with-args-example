# Several Spring Batchs with args

The purpose of this repo is to show how to have several Spring Batchs in the same jar and how to choose which one to run.

## How to build

```
$ mvn clean package
```

## How to run

```
$ java -Dbatch=<batch_name> -jar target/several-spring-batchs-with-args-example-0.0.1-SNAPSHOT.jar
```

`batch_name` in this example could be `uppercase` or `lowercase` to uppercase or lowercase the data from input file to save them in database.

If the `batch_name` property is not set, then no batch will be running.


For the example, you can add the `filter` argument at the end of the command line to make the job failing if a `firstname` equals the `filter` arg value.

```
$ java -Dbatch=<batch_name> -jar target/several-spring-batchs-with-args-example-0.0.1-SNAPSHOT.jar --filter=John
```

This can be useful to test on your own to get the exit code of the job.

`echo $?` will display `0` if the job succeed and something else otherwise.


## Comment about the code organisation

The purpose of this example is to learn how to have several batchs in the same jar.
So all the configuration was duplicated even if the content is the same.

If you want to have the same batch but with a different processor depending on the value of `-Dbatch`,
it would make more sense to combine the `@ConditionalOnProperty(name = "batch", havingValue = "uppercase")` annotation with the `@Bean` annotations on different processors.

`Configuration.java` example:
```java
    @Bean
    @ConditionalOnProperty(name = "batch", havingValue = "lowercase")
    public PersonToLowercaseProcessor processorToLowercase(ApplicationArguments args) {
        List<String> filterArg = args.getOptionValues("filter");
        return new PersonToLowercaseProcessor(Optional.ofNullable(filterArg).map(f -> f.get(0)).orElse(null));
    }
    
    @Bean
    @ConditionalOnProperty(name = "batch", havingValue = "uppercase")
    public PersonToUppercaseProcessor processorToUppercase(ApplicationArguments args) {
        List<String> filterArg = args.getOptionValues("filter");
        return new PersonToUppercaseProcessor(Optional.ofNullable(filterArg).map(f -> f.get(0)).orElse(null));
    }
    
    @Bean
    public Step step1(FlatFileItemReader<Person> reader, ItemProcessor<Person, Person> processor, JdbcBatchItemWriter<Person> writer) {
        return stepBuilderFactory.get("step1")
            .<Person, Person> chunk(10)
            .reader(readerToUppercase())
            .processor(processor)
            .writer(writer)
            ...
            .build();
    }
```