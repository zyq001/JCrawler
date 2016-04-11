#!/bin/bash
HOSTNAME="pxc-mysql.inner.youdao.com"  #数据库信息
PORT="3306"
USERNAME="eadonline4nb"
PASSWORD="new1ife4Th1sAugust"

DBNAME="readease"  #数据库名称
TABLENAME="newest_id" #数据库中表的名称
PAGETABLE="parser_page"
#创建数据库
#create_db_sql="create database IF NOT EXISTS ${DBNAME}"
#mysql -h${HOSTNAME} -P${PORT} -u${USERNAME} -p${PASSWORD} -e "${create_db_sql}"

#创建表
#create_table_sql="create table IF NOT EXISTS ${TABLENAME} ( name varchar(20), id int(11) default 0 )"
#mysql -h${HOSTNAME} -P${PORT} -u${USERNAME} -p${PASSWORD} ${DBNAME} -e "${create_table_sql}"

#插入数据
#insert_sql="insert into ${TABLENAME} values('billchen',2)"
#mysql -h${HOSTNAME} -P${PORT} -u${USERNAME} -p${PASSWORD} ${DBNAME} -e "${insert_sql}"

#查询
#select_sql="select * from ${TABLENAME}"
#mysql -h${HOSTNAME} -P${PORT} -u${USERNAME} -p${PASSWORD} ${DBNAME} -e "${select_sql}"

JPSCOUNT=`jps | wc -l`
while(($JPSCOUNT>3))
do
  #let JPSCOUNT=JPSCOUNT-1
  let JPSCOUNT=`jps | wc -l`
  sleep 1m
  echo $(date +%Y%m%d%H%M) "jps count:" $JPSCOUNT
done
echo $JPSCOUNT
#更新数据
get_id_sql="select id from ${PAGETABLE} order by id desc limit 1"
new_id=`mysql -h${HOSTNAME} -P${PORT} -u${USERNAME} -p${PASSWORD} ${DBNAME} -e "${get_id_sql}"`
echo $new_id
new_id=`echo $new_id | awk '{print $2}'`
echo $new_id

update_sql="update ${TABLENAME} set id=${new_id}"
mysql -h${HOSTNAME} -P${PORT} -u${USERNAME} -p${PASSWORD} ${DBNAME} -e "${update_sql}"
#mysql -h${HOSTNAME} -P${PORT} -u${USERNAME} -p${PASSWORD} ${DBNAME} -e "${select_sql}"
