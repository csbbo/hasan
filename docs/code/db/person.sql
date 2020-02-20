-- MySQL创建两张表分别使用InnoDB，MyISAM存储引擎，通过存储过程调用函数生成大批量的测试数据

DROP TABLE IF EXISTS `person_info_large`;
create table person_info_large(
	id int(7) AUTO_INCREMENT PRIMARY KEY,
	account varchar(10) DEFAULT NULL,
	name varchar(20) DEFAULT NULL,
	area varchar(20) DEFAULT NULL,
	title varchar(20) DEFAULT NULL,
	motto varchar(50) DEFAULT NULL,
	UNIQUE KEY account(account),
	KEY index_area_title(area,title)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `person_info_myisam`;
create table person_info_myisam(
	id int(7) AUTO_INCREMENT PRIMARY KEY,
	account varchar(10) DEFAULT NULL,
	name varchar(20) DEFAULT NULL,
	area varchar(20) DEFAULT NULL,
	title varchar(20) DEFAULT NULL,
	motto varchar(50) DEFAULT NULL,
	UNIQUE KEY account(account),
	KEY index_area_title(area,title)
)ENGINE=MyISAM DEFAULT CHARSET=utf8;

delimiter &&
DROP FUNCTION IF EXISTS `rand_str`;
CREATE FUNCTION rand_str(n int) RETURNS varchar(255)
begin
	declare chars_str varchar(100) default "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
	declare return_str varchar(255) default "";
	declare i int default 0;
	while i < n do
		set return_str=concat(return_str,substring(chars_str,floor(1+rand()*62),1));
		set i=i+1;
	end while;
	return return_str;
end &&
delimiter ;


delimiter &&
DROP PROCEDURE IF EXISTS `insert_data`;
CREATE PROCEDURE insert_data(IN n int)
BEGIN
	DECLARE I INT DEFAULT 1;
		WHILE (i <= n) DO
			INSERT INTO person_info_large(account,name,area,title,motto) VALUES(i,rand_str(20),rand_str(20),rand_str(20),rand_str(50));
			INSERT INTO person_info_myisam(account,name,area,title,motto) VALUES(i,rand_str(20),rand_str(20),rand_str(20),rand_str(50));
			set i=i+1;
		END WHILE;
END &&
delimiter ;

