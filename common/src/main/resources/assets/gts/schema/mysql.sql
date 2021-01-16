CREATE TABLE `{prefix}listings` (
    `id`                VARCHAR(36)     NOT NULL,
    `lister`            VARCHAR(36)     NOT NULL,
    `listing`           MEDIUMTEXT      NOT NULL,
    PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8;

CREATE TABLE `{prefix}auction_claims` (
    `auction`           VARCHAR(36)     NOT NULL,
    `lister`            TINYINT(1)      NOT NULL,
    `winner`            TINYINT(1)      NOT NULL,
    `others`            MEDIUMTEXT      NOT NULL,
    PRIMARY KEY (`auction`)
) DEFAULT CHARSET = utf8;

CREATE TABLE `{prefix}player_settings` (
    `uuid`              VARCHAR(36)     NOT NULL,
    `pub_notif`         TINYINT(1)      NOT NULL,
    `sell_notif`        TINYINT(1)      NOT NULL,
    `bid_notif`         TINYINT(1)      NOT NULL,
    `outbid_notif`      TINYINT(1)      NOT NULL,
    PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8;

CREATE TABLE `{prefix}stashes` (
    `uuid`              VARCHAR(36)     NOT NULL,
    `data`              TEXT            NOT NULL,
    PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8;

SELECT count(*) INTO @result FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='{database}' AND TABLE_NAME='{prefix}auction_claims';
set @query = IF(@result < 4, 'SELECT * FROM `{prefix}auction_claims`', 'ALTER TABLE `{prefix}auction_claims` ADD `others` MEDIUMTEXT NOT NULL;');
prepare stmt from @query;
EXECUTE stmt;