# fast-generator-data

## 项目说明
- fast-data-generator是一款基于mybatisplus+springboot+MYSQL简单易上手,操作简单,非开放人员也能快速上手，能快速生成大量测试数据,支持多数据源
-脚手架来自gitee开源项目maku

## 项目特点
- 支持MySQL、Oracle、SQLServer、PostgreSQL、达梦8、MONGO、KAFKA、API等主流数据源
- 支持批量导入表、json导入及同步表结构等功能
- 支持多种mock，如js、正则 、枚举、关联 等mock规则(底层采用mockjs)
- 支持测试数据关联外键
- 支持自定义mock规则
- 支持快速生成大数量符合规范标准的测试数据
- 支持测试数据按照指定字段导出为excel
- 支持同步API接口，并进行简单的测试

## 部署方式
- 通过git下载源码
- 如使用MySQL8.0（其他数据库类似），则创建数据库maku_generator，数据库编码为utf8mb4
- 执行db/mysql.sql文件，初始化数据
- 修改application.yml，更新MySQL账号和密码、数据库名称
- 运行GeneratorApplication.java，则可启动项目
- 项目访问路径：http://localhost:8088/maku-generator/index.html
- 

## 效果图

![输入图片说明](images/1.png)

![输入图片说明](images/2.png)

![输入图片说明](images/3.png)

![输入图片说明](images/4.png)