# MySQL 5.7 多主一从（多源复制）同步配置
多主一从，也称为多源复制，数据流向：
主库1 -> 从库s
主库2 -> 从库s
主库n -> 从库s

应用场景
数据汇总，可将多个主数据库同步汇总到一个从数据库中，方便数据统计分析。
读写分离，从库只用于查询，提高数据库整体性能。

部署环境
注：使用docker部署mysql实例，方便快速搭建演示环境。但本文重点是讲解主从配置，因此简略描述docker环境构建mysql容器实例。

数据库：MySQL 5.7.x  （相比5.5，5.6而言，5.7同步性能更好，支持多源复制，可实现多主一从，主从库版本应保证一致）
操作系统：CentOS 7.x
容器：Docker 17.09.0-ce
镜像：mysql:5.7
主库300：IP=192.168.10.212; PORT=4300; server-id=300; database=test3; table=user
主库400：IP=192.168.10.212; PORT=4400; server-id=400; database=test4; table=user
主库500：IP=192.168.10.212; PORT=4500; server-id=500; database=test5; table=user
从库10345：IP=192.168.10.212; PORT=4345; server-id=10345; database=test3,test4,test5; table=user

配置约束
主从库必须保证网络畅通可访问
主库必须开启binlog日志
主从库的server-id必须不同

# 【主库300】操作及配置
[client]
port = 3306
deault-character-set = utf8mb4

[mysql]
port = 3306
deault-character-set = utf8mb4

[mysqld]
server-id = 300     #必须唯一
log_bin = mysql-bin   #开启及设置二进制日志文件名称
binlog_format = MIXED
sync_binlog = 1
expire_logs_days = 7    #二进制日志自动删除/过期的天数。默认值为0，表示不自动删除
binlog-do-db = test3    #要同步的数据库
binlog-ignore-db = mysql   #不需要同步的数据库
binlog_ignore_db = information_schema
binlog_ignore_db = performation_schema
binlog_ignore_db = sys

charcter-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 安装启动
docker run -d -p 4300:3306 --name=mysql-300 -v /datavol/mysql-300/conf:/etc/mysql/conf.d -v /datavol/mysql-300/mysql:/var/lib/mysql -e MYSQL-ROOT-PASSWORD=123456 mysql:5.7
进入容器
docker exec -it mysql-300 bash
进入mysql
mysql -u root -p
连接mysql主数据库，键入命令mysql -u root -p，输入密码后登录数据库。创建用户用于从库同步复制，授予复制、同步访问的权限
grant replication slave on *.* to 'slave'@'%' identified by '123456';
log_bin是否开启
show variables like 'log_bin';
查看master状态
show master status \G;

# 【主库400】操作及配置
[client]
port = 3306
deault-character-set = utf8mb4

[mysql]
port = 3306
deault-character-set = utf8mb4

[mysqld]
server-id = 400     #必须唯一
log_bin = mysql-bin   #开启及设置二进制日志文件名称
binlog_format = MIXED
sync_binlog = 1
expire_logs_days = 7    #二进制日志自动删除/过期的天数。默认值为0，表示不自动删除
binlog-do-db = test4    #要同步的数据库
binlog-ignore-db = mysql   #不需要同步的数据库
binlog_ignore_db = information_schema
binlog_ignore_db = performation_schema
binlog_ignore_db = sys

charcter-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 安装启动
docker run -d -p 4400:3306 --name=mysql-400 -v /datavol/mysql-400/conf:/etc/mysql/conf.d -v /datavol/mysql-400/mysql:/var/lib/mysql -e MYSQL-ROOT-PASSWORD=123456 mysql:5.7
进入容器
docker exec -it mysql-400 bash
进入mysql
mysql -u root -p
连接mysql主数据库，键入命令mysql -u root -p，输入密码后登录数据库。创建用户用于从库同步复制，授予复制、同步访问的权限
grant replication slave on *.* to 'slave'@'%' identified by '123456';
log_bin是否开启
show variables like 'log_bin';
查看master状态
show master status \G;

# 【主库500】操作及配置
[client]
port = 3306
deault-character-set = utf8mb4

[mysql]
port = 3306
deault-character-set = utf8mb4

[mysqld]
server-id = 500     #必须唯一
log_bin = mysql-bin   #开启及设置二进制日志文件名称
binlog_format = MIXED
sync_binlog = 1
expire_logs_days = 7    #二进制日志自动删除/过期的天数。默认值为0，表示不自动删除
binlog-do-db = test5    #要同步的数据库
binlog-ignore-db = mysql   #不需要同步的数据库
binlog_ignore_db = information_schema
binlog_ignore_db = performation_schema
binlog_ignore_db = sys

charcter-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 安装启动
docker run -d -p 4500:3306 --name=mysql-500 -v /datavol/mysql-500/conf:/etc/mysql/conf.d -v /datavol/mysql-500/mysql:/var/lib/mysql -e MYSQL-ROOT-PASSWORD=123456 mysql:5.7
进入容器
docker exec -it mysql-400 bash
进入mysql
mysql -u root -p
连接mysql主数据库，键入命令mysql -u root -p，输入密码后登录数据库。创建用户用于从库同步复制，授予复制、同步访问的权限
grant replication slave on *.* to 'slave'@'%' identified by '123456';
log_bin是否开启
show variables like 'log_bin';
查看master状态
show master status \G;

