package com.example.batchprocessing.lower;

import com.example.batchprocessing.domain.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class PersonToLowercaseProcessor implements ItemProcessor<Person, Person> {

	private String notAcceptedFirstName = "";

	private static final Logger log = LoggerFactory.getLogger(PersonToLowercaseProcessor.class);

	public PersonToLowercaseProcessor(String notAcceptedFirstName) {
		this.notAcceptedFirstName = notAcceptedFirstName;
	}

	@Override
	public Person process(final Person person) throws Exception {
		final String firstName = person.getFirstName().toLowerCase();
		final String lastName = person.getLastName().toLowerCase();

		final Person transformedPerson = new Person(firstName, lastName);

		log.info("Converting (" + person + ") into (" + transformedPerson + ")");

		if (person.getFirstName().equals(notAcceptedFirstName)) {
			throw new RuntimeException(notAcceptedFirstName + " never dies");
		}

		return transformedPerson;
	}
}
