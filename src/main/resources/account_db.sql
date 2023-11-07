drop database if exists account_db;

create database if not exists account_db;

use account_db;

create table account(
    id int auto_increment primary key,
    name varchar(40) not null,
    amount int
);

insert into account (name, amount) values ('Aung Aung', 200000);
insert into account (name, amount) values ('Thida', 200000);

create table transfer_log(
    id int auto_increment primary key,
    from_account int not null,
    to_account int not null,
    transfer_time  timestamp default CURRENT_TIMESTAMP,
    amount int,
    from_amount int,
    to_amount int,
    status varchar(20),
    foreign key fk_transfer_account_1 (from_account) references account(id),
    foreign key fk_transfer_account_2 (to_account) references account(id)
);