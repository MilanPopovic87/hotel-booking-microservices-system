-- =========================
-- USERS (excluding admin, created by @PostConstruct)
-- =========================
INSERT INTO users (username, password, role)
VALUES ('demo', '$2a$10$ljwIPHLqr/VDtOQGEsGyueLwBkWg2zDy1kVp3VKJOCJehg3C7HNe6', 'USER'),
       ('alice', '$2a$10$ItpLgDoFfNVdUwnu7ZeosucNkZfzPbM5q0BRjPG5/fmllb0xP47kq', 'USER'),
       ('bob', '$2a$10$7UL8loOO.X8Ikgil7k3yT.b9srRPm2KnnSvEzk1Q.dmyDkn4R6WJO', 'USER'),
       ('charlie', '$2a$10$L3eTar1baACISifoe1IgXek/3LwHGv.YPBdlJhyFCcnd.rAHdm30C', 'USER'),
       ('diana', '$2a$10$xtonflIkHMW7vWKte1XvJ.2LGW.r1fZUeKmSi73iuwMx523d5cf5S', 'USER');
