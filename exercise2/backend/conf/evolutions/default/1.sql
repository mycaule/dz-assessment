-- !Ups

create table content_types (
  "name" varchar(255) primary key not null
);

create table tags (
  "name" varchar(255) primary key not null
);

create table bookmarks (
  id serial primary key,
  obj_id integer not null,
  obj_type varchar(255) references content_types(name) not null,
  tag_name varchar(255) references tags(name) not null,
  UNIQUE (obj_id, obj_type, tag_name)
);

-- The pair (obj_id, obj_type) must be valid with Deezer API

-- !Downs
