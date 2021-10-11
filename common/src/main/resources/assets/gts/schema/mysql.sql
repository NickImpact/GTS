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

CREATE TABLE `{prefix}deliveries` (
    `id`                VARCHAR(36)     NOT NULL,
    `target`            VARCHAR(36)     NOT NULL,
    `delivery`          MEDIUMTEXT      NOT NULL,
    PRIMARY KEY (`id`)
) DEFAULT CHARSET = utf8;