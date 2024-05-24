DROP TABLE IF EXISTS memo_single_games;
DROP TABLE IF EXISTS memo_users;
DROP TABLE IF EXISTS memo_multi_games;

create table memo_users (
	id UUID primary key,
	user_name VARCHAR(100) not null unique,
	email VARCHAR(100) not null unique,
	password VARCHAR(100) not NULL,
	created_at TIMESTAMP,
    updated_at TIMESTAMP
);

create table memo_single_games (
	id UUID primary key,
	userid UUID REFERENCES memo_users(id),
	won boolean,
	remaining_time integer,
	pairs integer,
	time_max integer,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table memo_multi_games (
	id UUID primary key,
	player1 UUID REFERENCES memo_users(id),
	player2 UUID references memo_users(id),
	winner varchar(100),
	pairs integer,
	player1_guessed_cards integer,
	player2_guessed_cards integer,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);