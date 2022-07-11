package com.akhilesh.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.akhilesh.entity.Book;
import com.google.common.base.Predicates;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@EnableBatchProcessing
public class BookBatchOperationConfiguration {
	
	private static final Logger log = LoggerFactory.getLogger(BookBatchOperationConfiguration.class);

	@Bean
	Job job(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
			ItemReader<Book> itemReader, ItemProcessor<Book, Book> itemProcessor, ItemWriter<Book> itemWriter) {
		log.info("BookBatchOperationConfiguration-job()-started");
		Step step = stepBuilderFactory.get("ETL-File-Load")
				.<Book,Book>chunk(100)
				.reader(itemReader)
				.processor(itemProcessor)
				.writer(itemWriter)
				.build();
		log.info("BookBatchOperationConfiguration-job()-ended");
		return jobBuilderFactory.get("ETL-Load")
				.incrementer(new RunIdIncrementer())
				.start(step)
				.build();
		
	}
	
	@Bean
	FlatFileItemReader<Book> itemReader(@Value("${input}") Resource resource){
		log.info("BookBatchOperationConfiguration-itemReader()");
		FlatFileItemReader<Book> flatFileItemReader = new FlatFileItemReader<>();
		flatFileItemReader.setResource(resource);
		flatFileItemReader.setName("CSV-Reader");
		flatFileItemReader.setLinesToSkip(1);
		flatFileItemReader.setLineMapper(lineMapper());
		return flatFileItemReader;
	}

	@Bean
	LineMapper<Book> lineMapper() {
		log.info("BookBatchOperationConfiguration-lineMapper()");
		DefaultLineMapper<Book> defaultLineMapper = new DefaultLineMapper<>();
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames(new String[] {"author","bookName","noOfCopies","pages","price","publication"});
		
		BeanWrapperFieldSetMapper<Book> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setTargetType(Book.class);
		
		defaultLineMapper.setLineTokenizer(lineTokenizer);
		defaultLineMapper.setFieldSetMapper(fieldSetMapper);
		return defaultLineMapper;
	}
	
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.paths(PathSelectors.any())
				.apis(Predicates.not(RequestHandlerSelectors.basePackage("org.springbootframework.boot")))
				.build().apiInfo(apiDetails());
	}

	
	private ApiInfo apiDetails() {
		Contact mycontact = new Contact("Akhilesh Kumar Patel", "www.google.com", "ak2bitm@gmail.com");
		return new ApiInfo("LMS Api", "Book Search Api part of LMS", "1.1", "Free to use", mycontact, "Api under free liance", "www.google.com");
	}
}
