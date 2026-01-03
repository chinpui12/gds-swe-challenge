package sg.gov.tech.gds_swe_challenge.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.RecordFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import sg.gov.tech.gds_swe_challenge.batch.UserItemProcessor;
import sg.gov.tech.gds_swe_challenge.dto.UserInput;
import sg.gov.tech.gds_swe_challenge.entity.User;
import sg.gov.tech.gds_swe_challenge.repository.UserRepository;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class UserBatchConfig {

    /**
     * Startup job: Load default users
     */
    @Bean
    public Job loadDefaultUsersJob(JobRepository jobRepository) {
        return new JobBuilder("loadDefaultUsersJob", jobRepository)
                .start(loadDefaultUsersStep(jobRepository))
                .build();
    }

    @Bean
    public Step loadDefaultUsersStep(JobRepository jobRepository) {
        return new StepBuilder("loadDefaultUsersStep", jobRepository)
                .<UserInput, User>chunk(10)
                .reader(defaultUsersReader())
                .processor(userItemProcessor(null))
                .writer(userItemWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<UserInput> defaultUsersReader() {
        return new FlatFileItemReaderBuilder<UserInput>()
                .name("defaultUsersReader")
                .resource(new ClassPathResource("data/default-users.csv"))
                .delimited()
                .names("username", "canInitiateSession")
                .linesToSkip(1)
                .fieldSetMapper(new RecordFieldSetMapper<>(UserInput.class))
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<UserInput, User> userItemProcessor(UserRepository userRepository) {
        return new UserItemProcessor(userRepository);
    }

    @Bean
    public ItemWriter<User> userItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<User>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO app_user (username, can_initiate_session, created_by, updated_by) " +
                        "VALUES (:username, :canInitiateSession, :createdBy, :updatedBy)")
                .dataSource(dataSource)
                .build();
    }
}
