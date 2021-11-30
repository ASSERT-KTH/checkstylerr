package cn.merryyou.editor.editor.domain;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;
/**
 * @Desc: markdown 保存实体类
 * @Date:  2018/9/28 上午10:48.
 */

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@Table(name = "editor")
public class Editor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String content;

    private String textContent="";

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP",insertable = true,updatable = false)
    private Date createTime;

    @Override
    public String toString() {
        return "Editor{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }
}

/**
 CREATE  TABLE editor (

 id int(11) AUTO_INCREMENT not NULL  PRIMARY KEY  ,
 content TEXT,
 textContent TEXT ,
 createTime TIMESTAMP DEFAULT  CURRENT_TIMESTAMP
 )
 */