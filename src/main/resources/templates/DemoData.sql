use PayMyBuddy;

INSERT INTO users (username, email, password) VALUES
                  ('Alice',  'Alice@exemple.fr',  '$2a$12$ETriS/gpvIpjYdvsnxfhIuHByZM9vyBvUJk5Zl5UKDhkR7U0WbHeK'),
                  ('John',   'John@exemple.fr',   '$2a$12$Yykn6BnnA2.gk/R3VBRxYe5rY1ysr3ooV.bnltrJeiofnIuYa6eK.'),
                  ('Martin', 'Martin@exemple.fr', '$2a$12$NsHd6f0vZydvhX8LIbGiQOt9nE6HSzydZfbDoM/Iwn1oXP5zK7F2y'),
                  ('Milo',   'Milo@exemple.fr',   '$2a$12$esreQeohkk8wNBwDwmhdfuUoErRc6z7vxplovOtxHYf02jr7mc0HK'),
                  ('Dylan',  'Dylan@exemple.fr',  '$2a$12$NcRyJJM0tx8gf4JM5gvaGux3mJttINqCNukiCTXExAqZQkN2WeLHe'),
                  ('Sara',   'Sara@exemple.fr',   '$2a$12$Osa6ggyMEvLVlnUb7z5ZJuafp04ZDCeiCyOnxPCJksecVU1UTZCIm');

INSERT INTO transactions (sender_id, receiver_id, description, amount) VALUES
                        (1, 2, 'Dîner', 25.50),
                        (3, 1, 'Gazole', 300.00),
                        (4, 5, 'Billet du Concert', 75.00),
                        (2, 6, 'Cadeau', 50.00),
                        (5, 3, 'Airbnb', 220.00),
                        (6, 4, 'Courses', 15.75);

INSERT INTO user_connections (user_id, connection_id) VALUES
                             (1, 2), -- Alice is connected to John
                             (1, 3), -- Alice is connected to Martin
                             (2, 4), -- John is connected to Milo
                             (2, 5), -- John is connected to Dylan
                             (3, 6), -- Martin is connected to Sara
                             (5, 6); -- Dylan is connected to Sara