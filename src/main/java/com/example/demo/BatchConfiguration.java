package com.example.demo;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.classify.Classifier;
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
	
//	@Autowired
//	public JobLauncher jobLauncher;
	
//	@Scheduled(fixedDelay = 5000)
//	public void perform() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
//		System.out.println("Job Started at :" + new Date());
//
//		JobParameters param = new JobParametersBuilder().addString("JobID", String.valueOf(System.currentTimeMillis()))
//				.toJobParameters();
//
//		JobExecution execution = jobLauncher.run(exportModel(), param);
//
//		System.out.println("Job finished with status :" + execution.getStatus());
//	}
	
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
				.sql("SELECT id, status, origem, destino FROM model")
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
	public ClassifierCompositeItemWriter<Model> classifierCompositeItemWriter(ItemWriter<Model> saidaWriter, ItemWriter<Model> outWriter){
		
		ClassifierCompositeItemWriter<Model> classifierCompositeItemWriter = new ClassifierCompositeItemWriter<>();
		Classifier<Model, ItemWriter<? super Model>> classifier = m -> {
			if(m.getStatus().equals("1")) {
				return saidaWriter;
			}else {
				return outWriter;
			}
		};
		
		classifierCompositeItemWriter.setClassifier(classifier);
		return classifierCompositeItemWriter;
		
	}
	
	@Bean
	public FlatFileItemWriter<Model> saidaWriter() {
		return new FlatFileItemWriterBuilder<Model>()
				.name("FlatFileItemWriter")
				.resource(new ClassPathResource("saida.txt"))
				.lineAggregator(new DelimitedLineAggregator<Model>() {{
					setDelimiter(",");
					setFieldExtractor(new BeanWrapperFieldExtractor<Model>() {{
						setNames(new String[] {"id","status","origem","destino"});
					}});
				}})
				.build();
	}
	
	@Bean
	public FlatFileItemWriter<Model> outWriter() {
		return new FlatFileItemWriterBuilder<Model>()
				.name("FlatFileItemWriter")
				.resource(new ClassPathResource("out.txt"))
				.lineAggregator(new DelimitedLineAggregator<Model>() {{
					setDelimiter(",");
					setFieldExtractor(new BeanWrapperFieldExtractor<Model>() {{
						setNames(new String[] {"id","status","origem","destino"});
					}});
				}})
				.build();
	}
	
	@Bean
	public Job exportModel() {
		return jobBuilderFactory.get("exportModel")
				.incrementer(new RunIdIncrementer())
				.start(step1())
//				.end()
				.build();
				
	}
	
	@Bean
	public Step step1() {   
		return stepBuilderFactory.get("step1")
				.<Model,Model> chunk(10)
				.reader(reader())
				.processor(processor())
				.writer(classifierCompositeItemWriter(saidaWriter(), outWriter()))
				.stream(saidaWriter())
				.stream(outWriter())
				.build();
	}
	
	
}
