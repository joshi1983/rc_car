create table `car_control_state`
(
	`id` int primary key auto_increment,
	`steering_value` float not null default 0,
	`speed_value` float not null default 0,
	`is_desired` tinyint(1) not null default 0
);

create table `user`
(
	`id` int primary key auto_increment,
	`username` varchar(20) not null unique,
	`password_hash` varchar(32) not null
);

create table `preference`
(
	`id` int primary key auto_increment,
	`is_recording` tinyint(1) default 0
);

insert into `preference`(id, is_recording) values
(1, 0);

insert into `car_control_state`
(`id`, `steering_value`, `speed_value`, `is_desired`) values
(1, 0, 0, 1),
(2, 0, 0, 0);

insert into `user`(`id`, `username`, `password_hash`) values
(1, 'josh', '');

