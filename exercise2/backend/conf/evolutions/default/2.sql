-- !Ups
INSERT INTO tags (name)
VALUES
  ('house'),
  ('electronic'),
  ('dance'),
  ('disco');

INSERT INTO bookmarks (obj_id, obj_type, tag_name)
VALUES
  (302127, 'album', 'dance'),
  (302127, 'album', 'electronic'),
  (27, 'artist', 'house'),
  (27, 'artist', 'electronic'),
  (27, 'artist', 'dance'),
  (27, 'artist', 'disco'),
  (3135556, 'track', 'electronic');

 -- !Downs
