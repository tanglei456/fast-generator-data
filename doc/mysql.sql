-- data_generator.gen_datasource definition

CREATE TABLE `gen_datasource` (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                  `db_type` varchar(200) DEFAULT NULL COMMENT '数据库类型',
                                  `conn_name` varchar(200) NOT NULL COMMENT '连接名',
                                  `conn_url` varchar(500) DEFAULT NULL COMMENT 'URL',
                                  `username` varchar(200) DEFAULT NULL COMMENT '用户名',
                                  `password` varchar(200) DEFAULT NULL COMMENT '密码',
                                  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COMMENT='数据源管理';


-- data_generator.gen_field_type definition

CREATE TABLE `gen_field_type` (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                  `column_type` varchar(200) DEFAULT NULL COMMENT '字段类型',
                                  `attr_type` varchar(200) DEFAULT NULL COMMENT '属性类型',
                                  `package_name` varchar(200) DEFAULT NULL COMMENT '属性包名',
                                  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                  `group_name` varchar(100) DEFAULT NULL,
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `column_type` (`column_type`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COMMENT='字段类型管理';


-- data_generator.gen_mock_rule definition

CREATE TABLE `gen_mock_rule` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '规则id',
                                 `name` varchar(100) NOT NULL COMMENT '规则名字',
                                 `description` varchar(1000) DEFAULT NULL COMMENT '规则描述',
                                 `type` varchar(100) NOT NULL COMMENT '类型: 1.默认 2.自定义',
                                 `create_time` date DEFAULT NULL COMMENT '创建时间',
                                 `relative_field_name` varchar(100) DEFAULT NULL,
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8;


-- data_generator.gen_table definition

CREATE TABLE `gen_table` (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                             `table_name` varchar(200) DEFAULT NULL COMMENT '表名',
                             `class_name` varchar(200) DEFAULT NULL COMMENT '类名',
                             `table_comment` varchar(200) DEFAULT NULL COMMENT '说明',
                             `package_name` varchar(200) DEFAULT NULL COMMENT '项目包名',
                             `datasource_id` bigint(20) DEFAULT NULL COMMENT '数据源ID',
                             `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                             `data_number` bigint(20) DEFAULT NULL,
                             `foreign_key` varchar(100) DEFAULT NULL,
                             `remark` varchar(100) DEFAULT NULL,
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `table_name` (`table_name`)
) ENGINE=InnoDB AUTO_INCREMENT=5420 DEFAULT CHARSET=utf8mb4 COMMENT='代码生成表';


-- data_generator.gen_table_field definition

CREATE TABLE `gen_table_field` (
                                   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
                                   `table_id` bigint(20) DEFAULT NULL COMMENT '表ID',
                                   `field_name` varchar(200) DEFAULT NULL COMMENT '字段名称',
                                   `field_type` varchar(200) DEFAULT NULL COMMENT '字段类型',
                                   `field_comment` varchar(200) DEFAULT NULL COMMENT '字段说明',
                                   `attr_name` varchar(200) DEFAULT NULL COMMENT '属性名',
                                   `attr_type` varchar(200) DEFAULT NULL COMMENT '属性类型',
                                   `package_name` varchar(200) DEFAULT NULL COMMENT '属性包名',
                                   `sort` int(11) DEFAULT NULL COMMENT '排序',
                                   `parent_id` bigint(20) DEFAULT NULL COMMENT '父id',
                                   `data_number` bigint(20) DEFAULT NULL COMMENT '生成数据量',
                                   `foreign_key` varchar(100) DEFAULT NULL COMMENT '外键',
                                   `mock_name` varchar(1000) DEFAULT NULL COMMENT 'mock名字',
                                   `auto_increment` tinyint(1) DEFAULT NULL COMMENT '是否自增',
                                   `primary_pk` tinyint(1) DEFAULT NULL COMMENT '主键',
                                   `full_field_name` varchar(100) DEFAULT NULL COMMENT '全字段名',
                                   `unique_index` tinyint(1) DEFAULT NULL,
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9999716051695 DEFAULT CHARSET=utf8mb4 COMMENT='代码生成表字段';


-- data_generator.sys_job definition

CREATE TABLE `sys_job` (
                           `job_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
                           `job_name` varchar(64) NOT NULL DEFAULT '' COMMENT '任务名称',
                           `job_group` varchar(64) NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
                           `invoke_target` varchar(500) NOT NULL COMMENT '调用目标字符串',
                           `cron_expression` varchar(255) DEFAULT '' COMMENT 'cron执行表达式',
                           `misfire_policy` varchar(20) DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
                           `concurrent` char(1) DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
                           `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1暂停）',
                           `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                           `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                           `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                           `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                           `remark` varchar(500) DEFAULT '' COMMENT '备注信息',
                           PRIMARY KEY (`job_id`,`job_name`,`job_group`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='定时任务调度表';


-- data_generator.sys_job_log definition

CREATE TABLE `sys_job_log` (
                               `job_log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
                               `job_name` varchar(64) NOT NULL COMMENT '任务名称',
                               `job_group` varchar(64) NOT NULL COMMENT '任务组名',
                               `invoke_target` varchar(500) NOT NULL COMMENT '调用目标字符串',
                               `job_message` varchar(500) DEFAULT NULL COMMENT '日志信息',
                               `status` char(1) DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
                               `exception_info` varchar(2000) DEFAULT '' COMMENT '异常信息',
                               `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                               PRIMARY KEY (`job_log_id`)
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8 COMMENT='定时任务调度日志表';


INSERT INTO data_generator.gen_mock_rule (id,name,description,`type`,create_time,relative_field_name) VALUES
                                                                                                          (1,'@ctitle','标题','1',NULL,''),
                                                                                                          (2,'@idcard','身份证号','1',NULL,NULL),
                                                                                                          (3,'@name','名字','1',NULL,NULL),
                                                                                                          (4,'@phone','电话','1',NULL,'phone,mobile'),
                                                                                                          (5,'@regexp','正则表达式','1',NULL,NULL),
                                                                                                          (6,'@enum','枚举','1',NULL,NULL),
                                                                                                          (11,'@string','字符串','1','2023-01-12',''),
                                                                                                          (12,'@string(lower)','小写字符串','1','2023-01-12',''),
                                                                                                          (13,'@string(upper)','大写字符串','1','2023-01-12',''),
                                                                                                          (14,'@string(symbol)','符号字符串','1','2023-01-12','');
INSERT INTO data_generator.gen_mock_rule (id,name,description,`type`,create_time,relative_field_name) VALUES
                                                                                                          (15,'@string(number)','数字字符串','1','2023-01-12',''),
                                                                                                          (16,'@integer','整数','1','2023-01-12',''),
                                                                                                          (17,'@string(number,7,10)','数字字符串','1','2023-01-12',''),
                                                                                                          (18,'@string(symbol,1,3)','符号','1','2023-01-12',''),
                                                                                                          (19,'@string(upper,1,3)','大写字符串','1','2023-01-12',''),
                                                                                                          (20,'@string(lower,1,3)','小写字符串','1','2023-01-12',''),
                                                                                                          (21,'@integer(1000)','整数','1','2023-01-12',''),
                                                                                                          (22,'@integer(1,10)','整数','1','2023-01-12',''),
                                                                                                          (23,'@chinese','中文','1','2023-01-12',''),
                                                                                                          (24,'@chinese(15)','中文','1','2023-01-12','');
INSERT INTO data_generator.gen_mock_rule (id,name,description,`type`,create_time,relative_field_name) VALUES
                                                                                                          (25,'@chinese(1,15)','中文','1','2023-01-12',''),
                                                                                                          (27,'@cparagraph','大段文本','1','2023-01-12',''),
                                                                                                          (28,'@cword','中文词组','1','2023-01-12',''),
                                                                                                          (29,'@cfirst','中文姓','1','2023-01-12',''),
                                                                                                          (30,'@clast','中文名','1','2023-01-12',''),
                                                                                                          (31,'@zip','邮编','1','2023-01-13',''),
                                                                                                          (32,'@uid','带字符uid(UUID)','1','2023-01-13','_id'),
                                                                                                          (33,'@county','区（如东坡区)','1','2023-01-13',''),
                                                                                                          (34,'@city','市','1','2023-01-13',''),
                                                                                                          (35,'@city(true)','市(含省)','1','2023-01-13','');
INSERT INTO data_generator.gen_mock_rule (id,name,description,`type`,create_time,relative_field_name) VALUES
                                                                                                          (36,'@province','省','1','2023-01-13',''),
                                                                                                          (37,'@natural','自然数','1','2023-01-13',''),
                                                                                                          (38,'@natural(10000)','自然数(指定最小值)','1','2023-01-13',''),
                                                                                                          (39,'@natural(1,100)','自然数(含最大值和最小值)','1','2023-01-13',''),
                                                                                                          (40,'@ip','网络ip','1','2023-01-13',''),
                                                                                                          (41,'@domain','域名','1','2023-01-13',''),
                                                                                                          (42,'@url','url','1','2023-01-13','url'),
                                                                                                          (43,'@id','数字id(雪花算法)','1','2023-01-13',''),
                                                                                                          (44,'@js','js脚本','1','2023-01-16',''),
                                                                                                          (45,'@word','英文词组','1','2023-02-01','');
INSERT INTO data_generator.gen_mock_rule (id,name,description,`type`,create_time,relative_field_name) VALUES
                                                                                                          (47,'@contact','字符串拼接,可引用对象','1','2023-02-08',''),
                                                                                                          (48,'@Date','日期类型,可选择范围','1','2023-02-09','');
