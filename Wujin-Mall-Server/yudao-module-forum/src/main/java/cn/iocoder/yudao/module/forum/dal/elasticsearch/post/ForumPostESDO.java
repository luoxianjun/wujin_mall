package cn.iocoder.yudao.module.forum.dal.elasticsearch.post;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import java.time.LocalDateTime;

@Data
@Document(indexName = "forum_post")
public class ForumPostESDO {

    @Id
    private Long id;

    /**
     * title 字段使用双分析器：
     * - 主字段用 ik_max_word/ik_smart（中文分词）
     * - .standard 子字段用 standard 分析器（英文分词，不过滤 stop words）
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
            otherFields = {
                    @InnerField(suffix = "standard", type = FieldType.Text, analyzer = "standard")
            }
    )
    private String title;

    /**
     * content 字段同理，支持中英文混合搜索
     */
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart"),
            otherFields = {
                    @InnerField(suffix = "standard", type = FieldType.Text, analyzer = "standard")
            }
    )
    private String content;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Field(type = FieldType.Integer)
    private Integer category;

    @Field(type = FieldType.Keyword)
    private String categories;

}
