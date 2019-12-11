package com.example.demo;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration extends DefaultBatchConfigurer{
	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	public DataSource dataSource;
	
	@Bean
	public DataSource dataSource() {
		final DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost/springbatch?useTimezone=true&serverTimezone=UTC");
		dataSource.setUsername("root");
		dataSource.setPassword("");
		
		return dataSource;
	}

	// Reader para leitura de arquivo csv
//	@Bean
//	public FlatFileItemReader<Model> reader() {
//		return new FlatFileItemReaderBuilder<Model>()
//				.name("modelItemReader")
//				.resource(new ClassPathResource("entrada.csv"))
//				.delimited()
//				.names(new String[] { "status", "origem", "destino" })
//				.fieldSetMapper(new BeanWrapperFieldSetMapper<Model>() {
//					{
//						setTargetType(Model.class);
//					}
//				}).build();
//	}
	
	@Bean
	public JdbcCursorItemReader<Model> reader() {
		return new JdbcCursorItemReaderBuilder<Model>()
				.name("JdbcCursorItemReader")
				.sql("SELECT status, origem, destino FROM model")
				.dataSource(dataSource)
				.rowMapper(new ModelRowMapper())
				.build();
	}
	
	
	@Bean
	public PoolingItemProcessor processor() {
		return new PoolingItemProcessor();
	}
	
	// Writer para incluir os itens lidos no banco
//	@Bean
//	public JdbcBatchItemWriter<Model> writer() {
//		return new JdbcBatchItemWriterBuilder<Model>()
//				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Model>())
//				.sql("INSERT INTO model (status, origem, destino) VALUES(:status, :origem, :destino)")
//				.dataSource(dataSource)
//				.build();
//	}
	
	@Bean
	public FlatFileItemWriter<Model> writer() {
		return new FlatFileItemWriterBuilder<Model>()
				.name("FlatFileItemWriter")
				.resource(new ClassPathResource("saida.txt"))
				.lineAggregator(new DelimitedLineAggregator<Model>() {{
					setDelimiter(",");
					setFieldExtractor(new BeanWrapperFieldExtractor<Model>() {{
						setNames(new String[] {"status","origem","destino"});
					}});
				}})
				.build();
	}
	
	
	@Bean
	public Job exportModel() {
		return jobBuilderFactory.get("exportModel")
				.incrementer(new RunIdIncrementer())
				.flow(step1())
				.end()
				.build();
				
	}
	
	@Bean
	public Step step1() {   
		return stepBuilderFactory.get("step1")
				.<Model,Model> chunk(10)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}
}
