package com.example.batchprocessing.upper;

import com.example.batchprocessing.domain.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class PersonToUppercaseProcessor implements ItemProcessor<Person, Person> {

	private String notAcceptedFirstName = "";

	private static final Logger log = LoggerFactory.getLogger(PersonToUppercaseProcessor.class);

	public PersonToUppercaseProcessor(String notAcceptedFirstName) {
		this.notAcceptedFirstName = notAcceptedFirstName;
	}

	@Override
	public Person process(final Person person) throws Exception {
		final String firstName = person.getFirstName().toUpperCase();
		final String lastName = person.getLastName().toUpperCase();

		final Person transformedPerson = new Person(firstName, lastName);

		log.info("Converting (" + person + ") into (" + transformedPerson + ")");

		if (person.getFirstName().equals(notAcceptedFirstName)) {
			throw new RuntimeException(notAcceptedFirstName + " never dies");
		}

		return transformedPerson;
	}
}
