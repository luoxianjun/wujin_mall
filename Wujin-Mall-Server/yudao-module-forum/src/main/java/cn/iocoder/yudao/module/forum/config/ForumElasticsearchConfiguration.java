package cn.iocoder.yudao.module.forum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "cn.iocoder.yudao.module.forum.dal.elasticsearch")
public class ForumElasticsearchConfiguration {
}
