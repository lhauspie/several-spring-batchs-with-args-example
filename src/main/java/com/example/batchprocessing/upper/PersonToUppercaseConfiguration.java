package com.example.batchprocessing.upper;

import com.example.batchprocessing.JobCompletionNotificationListener;
import com.example.batchprocessing.domain.Person;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

// tag::setup[]
@Configuration
@EnableBatchProcessing
@ConditionalOnProperty(name = "batch", havingValue = "uppercase")
public class PersonToUppercaseConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	// end::setup[]

	// tag::readerwriterprocessor[]
	@Bean
	public FlatFileItemReader<Person> readerToUppercase() {
		return new FlatFileItemReaderBuilder<Person>()
			.name("personItemReader")
			.resource(new ClassPathResource("sample-data.csv"))
			.delimited()
			.names(new String[]{"firstName", "lastName"})
			.fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
				setTargetType(Person.class);
			}})
			.build();
	}

	@Bean
	public PersonToUppercaseProcessor processorToUppercase(ApplicationArguments args) {
		List<String> filterArg = args.getOptionValues("filter");
		return new PersonToUppercaseProcessor(Optional.ofNullable(filterArg).map(f -> f.get(0)).orElse(null));
	}

	@Bean
	public JdbcBatchItemWriter<Person> writerToLowercase(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Person>()
			.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
			.sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
			.dataSource(dataSource)
			.build();
	}
	// end::readerwriterprocessor[]

	// tag::jobstep[]
	@Bean
	public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
		return jobBuilderFactory.get("importUserJob")
			.incrementer(new RunIdIncrementer())
			.listener(listener)
			.flow(step1)
			.end()
			.build();
	}

	@Bean
	public Step step1(JdbcBatchItemWriter<Person> writerToUppercase, PersonToUppercaseProcessor processor) {
		return stepBuilderFactory.get("step1")
			.<Person, Person> chunk(10)
			.reader(readerToUppercase())
			.processor(processor)
			.writer(writerToUppercase)
			.faultTolerant()
			.retryLimit(3)
			.retry(RuntimeException.class)
			.build();
	}
	// end::jobstep[]
}
