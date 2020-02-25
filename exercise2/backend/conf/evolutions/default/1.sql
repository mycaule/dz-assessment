-- !Ups

create table shared_model (
  "foo" varchar(255) primary key not null,
  "bar" integer not null
);

create type content_type as enum ('track', 'album', 'artist');

create table tags (
  "name" varchar(255) primary key not null
);

create table bookmarks (
  "id" serial primary key,
  "obj_id" integer not null,
  "obj_type" content_type,
  "tag_name" varchar(255) references tags(name)
);

-- The pair (obj_id, obj_type) must be valid with Deezer API

-- !Downs

drop table if exists shared_model;
