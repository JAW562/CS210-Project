CREATE TABLE m6_table (ps STRING PRIMARY, i INTEGER, b BOOLEAN);
INSERT INTO m6_table VALUES ("x", null, true);
INSERT INTO m6_table VALUES ("xy", 5, false);
INSERT INTO m6_table VALUES ("xx", 4, true);
INSERT INTO m6_table VALUES ("y", 1, null);
INSERT INTO m6_table VALUES ("yy", 2, true);
INSERT INTO m6_table VALUES ("yx", 3, false);
INSERT INTO m6_table VALUES ("z", null, null);
SHOW TABLE m6_table;
SHOW TABLES;
SELECT * FROM m6_table;
select * from m6_table;
SeLeCt * fRoM m6_table;
SELECT FROM m6_table;
SELECT *, ps, i, b FROM m6_table;
SELECT ps FROM m6_table;
SELECT ps, i, b FROM m6_table;
select ps, i, b from m6_table;
SELECT PS FROM m6_table;
SELECT ps m6_table;
ps FROM m6_table;
SELECT ps, i, b;
SELECT ps i b FROM m6_table;
SELECT ps AS primary FROM m6_table;
SELECT ps AS primary, i, b FROM m6_table;
SELECT ps AS primary, i AS number, b AS flag FROM m6_table;
select ps as primary, i as number, b as flag from m6_table;
SELECT ps, i AS number, b AS flag FROM m6_table;
SELECT ps, b, i FROM m6_table;
SELECT i, b, ps FROM m6_table;
SELECT i AS number, b AS flag, ps AS primary FROM m6_table;
SELECT i AS number, b AS flag, ps FROM m6_table;
SELECT ps, i AS number_1, i AS number_2 FROM m6_table;
SELECT ps, i, i AS i_copy FROM m6_table;
SELECT ps, i AS i_copy, i FROM m6_table;
SELECT b, b AS b_copy, ps FROM m6_table;
SELECT b AS b_copy, b, ps FROM m6_table;
SELECT ps, i, i FROM m6_table;
SELECT ps, b, i, b FROM m6_table;
SELECT ps, ps FROM m6_table;
SELECT ps AS primary, ps AS primary FROM m6_table;
SELECT i, b, ps AS primary, ps, ps FROM m6_table;
SELECT ps, i AS b, b AS i FROM m6_table;
SELECT ps, i AS b, i AS i_copy, b AS i, b AS b_copy, ps AS ps_copy FROM m6_table;
SELECT i AS b, b AS ps, ps AS i FROM m6_table;
SELECT ps AS primary_1, ps AS primary_2 FROM m6_table;
SELECT ps AS primary_1, ps FROM m6_table;
SELECT ps, ps AS primary_2 FROM m6_table;
SELECT i, b FROM m6_table;
SELECT * FROM m6_table WHERE ps = "y";
SELECT * FROM m6_table WHERE ps <> "y";
SELECT * FROM m6_table WHERE ps < "y";
SELECT * FROM m6_table WHERE ps > "y";
SELECT * FROM m6_table WHERE ps <= "y";
SELECT * FROM m6_table WHERE ps >= "y";
SELECT * FROM m6_table WHERE ps = "";
SELECT * FROM m6_table WHERE ps <> "";
SELECT * FROM m6_table WHERE ps < "";
SELECT * FROM m6_table WHERE ps > "";
SELECT * FROM m6_table WHERE ps <= "";
SELECT * FROM m6_table WHERE ps >= "";
SELECT * FROM m6_table WHERE i = 3;
SELECT * FROM m6_table WHERE i <> 3;
SELECT * FROM m6_table WHERE i < 3;
SELECT * FROM m6_table WHERE i > 3;
SELECT * FROM m6_table WHERE i <= 3;
SELECT * FROM m6_table WHERE i >= 3;
SELECT * FROM m6_table WHERE b = true;
SELECT * FROM m6_table WHERE b <> true;
SELECT * FROM m6_table WHERE b = false;
SELECT * FROM m6_table WHERE b <> false;
SELECT * FROM m6_table WHERE b < true;
SELECT * FROM m6_table WHERE b > false;
SELECT * FROM m6_table WHERE b <= true;
SELECT * FROM m6_table WHERE b >= false;
SELECT * FROM m6_table WHERE ps = null;
SELECT * FROM m6_table WHERE ps <> null;
SELECT * FROM m6_table WHERE i = null;
SELECT * FROM m6_table WHERE i <> null;
SELECT * FROM m6_table WHERE b = null;
SELECT * FROM m6_table WHERE b <> null;
SELECT * FROM m6_table WHERE ps < null;
SELECT * FROM m6_table WHERE ps > null;
SELECT * FROM m6_table WHERE ps <= null;
SELECT * FROM m6_table WHERE ps >= null;
SELECT * FROM m6_table WHERE i < null;
SELECT * FROM m6_table WHERE i > null;
SELECT * FROM m6_table WHERE i <= null;
SELECT * FROM m6_table WHERE i >= null;
SELECT * FROM m6_table WHERE b < null;
SELECT * FROM m6_table WHERE b > null;
SELECT * FROM m6_table WHERE b <= null;
SELECT * FROM m6_table WHERE b >= null;
SELECT * FROM m6_table WHERE ps = 3;
SELECT * FROM m6_table WHERE ps = true;
SELECT * FROM m6_table WHERE i = "y";
SELECT * FROM m6_table WHERE i = true;
SELECT * FROM m6_table WHERE b = "y";
SELECT * FROM m6_table WHERE b = 3;
SELECT * FROM m6_table  WHERE  ps = "y";
SELECT * FROM m6_table WHERE ps="y";
SELECT b, i, ps FROM m6_table WHERE ps <> "z";
SELECT ps AS s FROM m6_table WHERE ps <> "z";
SELECT * FROM m6_table WHERE x = 3;
SELECT * FROM m6_table  i = 3;
SELECT * FROM m6_table WHERE  = 3;
SELECT * FROM m6_table WHERE i = ;
SELECT * FROM m6_table WHERE ps equals "y";
SELECT * FROM m6_table WHERE ps = asdf;
SELECT * FROM m6_tableWHEREps = "y";
