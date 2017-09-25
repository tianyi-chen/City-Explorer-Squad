CREATE TABLE users (
    user_name VARCHAR(50) PRIMARY KEY,
    ip_address TEXT,
    lat TEXT,
    lon TEXT
    );

CREATE TABLE journeys (
    journey_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    city TEXT,
    date TEXT,
    members INTEGER,
    achievements INTEGER,
    points INTEGER
	);
INSERT INTO journeys (city, date, members, achievements, points) VALUES ('Liverpool', '10-04-2016', 6, 6, 55);
INSERT INTO journeys (city, date, members, achievements, points) VALUES ('Manchester', '12-04-2016', 6, 10, 65);
INSERT INTO journeys (city, date, members, achievements, points) VALUES ('Paris', '22-08-2016', 2, 16, 150);
INSERT INTO journeys (city, date, members, achievements, points) VALUES ('Birmingham', '23-12-2016', 2, 6, 50);
INSERT INTO journeys (city, date, members, achievements, points) VALUES ('London', '01-01-2017', 4, 15, 105);

CREATE TABLE tasks (
    task_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    city TEXT,
    task_name TEXT,
    content TEXT,
    points INTEGER
    );

INSERT INTO tasks (city, task_name,  content, points) VALUES ("London", "London Eye Challenge", "Take a group photo when you get to the highest on London Eye.", 10);
INSERT INTO tasks (city, task_name, content, points) VALUES ("London", "River Thames Cruise", "Take a group photo when you are travelling on Thames.", 5);
INSERT INTO tasks (city, task_name, content, points) VALUES ("London", "Bird trip", "Take a photo of the rest of the group feeding a swan at Hyde Park.", 5);


CREATE TABLE user_journey_pairs (
    user_name TEXT,
	journey_id INTEGER
	);

CREATE TABLE journey_task_pairs (
    journey_id INTEGER,
    task_id INTEGER,
    status TEXT,
    image_name TEXT,
    image_path TEXT
    );

CREATE TABLE gifts (
    gift_id INTEGER PRIMARY KEY AUTO_INCREMENT,
    gift_name TEXT,
    points INTEGER
    );

INSERT INTO gifts (gift_name, points) VALUES ("3 for 2 voucher at XX Cafe", 20);
INSERT INTO gifts (gift_name, points) VALUES ("£5 off London Eye ticket", 25);
INSERT INTO gifts (gift_name, points) VALUES ("Free ice cream sample", 10);
INSERT INTO gifts (gift_name, points) VALUES ("Printed album", 40);