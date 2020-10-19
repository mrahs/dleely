/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 This file is part of Dleely.
 Dleely is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 ******************************************************************************/

--create_db
--create_table_item
CREATE TABLE item(
    id IDENTITY(1) NOT NULL UNIQUE,
    name VARCHAR DEFAULT '' NOT NULL NULL_TO_DEFAULT,
    ref VARCHAR UNIQUE NOT NULL,
    info VARCHAR DEFAULT '' NOT NULL NULL_TO_DEFAULT,
    privy BOOLEAN DEFAULT false NOT NULL NULL_TO_DEFAULT,
    dateadd TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL NULL_TO_DEFAULT,
    datemod TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL NULL_TO_DEFAULT,
);

--create_table_tag
CREATE TABLE tag(
    id IDENTITY(1) NOT NULL UNIQUE,
    name VARCHAR NOT NULL UNIQUE,
    parent_id BIGINT REFERENCES tag(id) ON DELETE SET NULL,
    group_id BIGINT,
    dateadd TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL NULL_TO_DEFAULT,
    datemod TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL NULL_TO_DEFAULT,
    CHECK (id <> parent_id)
);

--create_table_tag_item
CREATE TABLE tag_item(
    item_id BIGINT REFERENCES item(id) ON DELETE CASCADE ON UPDATE CASCADE,
    tag_id BIGINT REFERENCES tag(id) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY (item_id, tag_id),
    UNIQUE (item_id, tag_id)
);
--end

--clear_db
DROP ALL OBJECTS;
--end

--backup_db
SCRIPT TO ? COMPRESSION DEFLATE CIPHER AES PASSWORD ? CHARSET 'UTF-8';
--end

--restore_db
RUNSCRIPT FROM ? COMPRESSION DEFLATE CIPHER AES PASSWORD ? CHARSET 'UTF-8';
--end

--select_item_by_id
SELECT * FROM item WHERE id = ?;
--end

--select_item_by_ref
SELECT * FROM item WHERE ref = ?;
--end

--select_items_by_id
SELECT * FROM item WHERE id IN (CSV);
--end

--select_all_items
SELECT * FROM item;
--end

--select_all_items_2
SELECT * FROM item LIMIT ? OFFSET ?;
--end

--select_untagged_items
SELECT * FROM item WHERE id NOT IN (SELECT DISTINCT item_id FROM tag_item);
--end

--select_item_tags
SELECT * FROM tag WHERE id IN (SELECT tag_id FROM tag_item WHERE item_id = ?);
--end

--select_tag_by_id
SELECT * FROM tag WHERE id = ?;
--end

--select_tag_by_name
SELECT * FROM tag WHERE name = ?;
--end

--select_tags_by_id
SELECT * FROM tag WHERE id IN (CSV);
--end

--select_tags
SELECT * FROM tag;
--end

--select_tags_2
SELECT * FROM tag LIMIT ? OFFSET ?;
--end

--select_unused_tags
SELECT * FROM tag WHERE id NOT IN (SELECT DISTINCT tag_id FROM tag_item);
--end

--select_most_used_tag
SELECT * FROM tag WHERE id IN (
    SELECT tag_id FROM tag_item
    GROUP BY tag_id
    ORDER BY COUNT(tag_id) DESC
    LIMIT 1
);
--end

--select_most_5_tags
SELECT * FROM tag WHERE id IN (
    SELECT tag_id FROM tag_item
    GROUP BY tag_id
    ORDER BY COUNT(tag_id) DESC
    LIMIT 5
);
--end

--select_date_time_count
select FORMATDATETIME(dateadd, 'yyyy-MM-dd') as "Date", count(*) as "Count"
from item
group by dateadd;
--end

--select_most_tagged_item
SELECT * FROM item WHERE id IN (
    SELECT item_id FROM tag_item
    GROUP BY item_id
    ORDER BY COUNT(item_id) DESC
    LIMIT 1
);
--end

--insert_item
INSERT INTO item(name, ref, info, privy, dateadd, datemod)
SELECT * FROM
    (SELECT
        TRIM(BOTH FROM ?) AS name,
		TRIM(BOTH FROM ?) AS ref,
	    TRIM(BOTH FROM ?) AS info,
		CAST(? AS BOOLEAN) AS privy,
		CAST(? AS TIMESTAMP) dateadd,
		CAST(? AS TIMESTAMP) AS datemod)
	AS entry
WHERE NOT EXISTS (SELECT id FROM item WHERE ref = entry.ref);
--end

--insert_tag
INSERT INTO tag(name, parent_id, group_id, dateadd, datemod)
SELECT * FROM
	(SELECT
        TRIM(BOTH FROM ?) AS name,
        CAST(? AS BIGINT) AS parent_id,
        CAST(? AS BIGINT) AS group_id,
        CAST(? AS TIMESTAMP) AS dateadd,
        CAST(? AS TIMESTAMP) AS datemod)
    AS entry
WHERE NOT EXISTS (SELECT id FROM tag WHERE name = entry.name);
--end

--insert_tag_item_map
INSERT INTO tag_item (item_id, tag_id) values(?, (SELECT id FROM tag WHERE name = ?));
--end

--update_item
UPDATE item SET
    name = TRIM(BOTH FROM ?),
    ref = TRIM(BOTH FROM ?),
    info = TRIM(BOTH FROM ?),
    privy = ?,
    dateadd = ?,
    datemod = ?
WHERE id = ?
AND NOT EXISTS (
	SELECT id
	FROM item
	WHERE
		ref = ? AND
		id <> ?
);
--end

--update_tag
UPDATE tag SET
    name = TRIM(BOTH FROM ?),
    parent_id = ?,
    group_id = ?,
    dateadd = ?,
    datemod = ?
WHERE id = ?
AND NOT EXISTS (
	SELECT id
	FROM tag
	WHERE
		name = ? AND
		id <> ?
);
--end

--remove_item
DELETE FROM item WHERE id = ?;
--end

--remove_item_tags
DELETE FROM tag_item WHERE item_id = ?;
--end

--remove_tag
DELETE FROM tag WHERE id = ?;
--end

--remove_tag_items
DELETE FROM item WHERE id IN (SELECT item_id FROM tag_item WHERE tag_id = ?);
--end

--remove_unused_tags
DELETE FROM tag WHERE id NOT IN (SELECT DISTINCT tag_id FROM tag_item);
--end

--remove_items
DELETE FROM item WHERE id IN (CSV);
--end

--replace_tag
UPDATE tag_item SET tag_id = ?
WHERE tag_id = ?
AND item_id NOT IN (
    SELECT item_id FROM tag_item
    WHERE tag_id = ? OR tag_id = ?
    GROUP BY item_id
    HAVING COUNT(item_id) = 2
);
--end

--count_items
SELECT COUNT(id) AS TOTAL FROM item;
--end

--count_untagged_items
SELECT COUNT(id) AS TOTAL FROM item WHERE id NOT IN (SELECT DISTINCT item_id FROM tag_item);
--end

--count_tags
SELECT COUNT(id) AS TOTAL FROM tag;
--end

--count_tag_items
SELECT COUNT(item_id) AS TOTAL FROM tag_item WHERE tag_id = ?;
--end

--count_item_tags
SELECT COUNT(tag_id) AS TOTAL FROM tag_item WHERE item_id = ?;
--end

--count_unused_tags
SELECT COUNT(id) AS TOTAL FROM tag WHERE id NOT IN (SELECT DISTINCT tag_id FROM tag_item);
--end

--search_tags
SELECT * FROM tag WHERE name LIKE ?;
--end

--search_items
SELECT * FROM item
WHERE id IN (
    SELECT item_id FROM tag_item
    WHERE tag_id IN (
        SELECT DISTINCT id FROM tag WHERE name IN (CSV)
    )
    GROUP BY item_id
    HAVING COUNT(item_id) = ?
)
--end